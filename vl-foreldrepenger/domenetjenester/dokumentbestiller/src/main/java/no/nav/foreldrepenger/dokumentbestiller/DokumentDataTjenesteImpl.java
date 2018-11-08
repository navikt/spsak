package no.nav.foreldrepenger.dokumentbestiller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.BrevMottaker;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeAggregat;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentAdresse;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerTaskProperties;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentTypeDtoMapper;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;


@ApplicationScoped
public class DokumentDataTjenesteImpl implements DokumentDataTjeneste {

    private String norg2KontaktTelefonNummer;
    private String norg2NavKlageinstansTelefon;
    private DokumentRepository dokumentRepository;
    private BehandlingRepository behandlingRepository;
    private TpsTjeneste tpsTjeneste;
    private ReturadresseKonfigurasjon returadresseKonfigurasjon;
    private ProsessTaskRepository prosessTaskRepository;
    private BrevParametere brevParametere;
    private DokumentTypeDtoMapper dokumentTypeDtoMapper;
    private BehandlingRepositoryProvider repositoryProvider;

    public DokumentDataTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public DokumentDataTjenesteImpl(@KonfigVerdi(value = "norg2.kontakt.telefonnummer") String norg2KontaktTelefonNummer,
                                    @KonfigVerdi(value = "norg2.kontakt.klageinstans.telefonnummer") String norg2NavKlageinstansTelefon,
                                    DokumentRepository dokumentRepository,
                                    BehandlingRepositoryProvider repositoryProvider,
                                    TpsTjeneste tpsTjeneste,
                                    ReturadresseKonfigurasjon returadresseKonfigurasjon,
                                    ProsessTaskRepository prosessTaskRepository,
                                    DokumentTypeDtoMapper dokumentTypeDtoMapper) {

        this.norg2KontaktTelefonNummer = norg2KontaktTelefonNummer;
        this.norg2NavKlageinstansTelefon = norg2NavKlageinstansTelefon;
        this.dokumentRepository = dokumentRepository;
        this.repositoryProvider = repositoryProvider;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.tpsTjeneste = tpsTjeneste;
        this.returadresseKonfigurasjon = returadresseKonfigurasjon;
        this.prosessTaskRepository = prosessTaskRepository;
        this.dokumentTypeDtoMapper = dokumentTypeDtoMapper;
        this.brevParametere = dokumentTypeDtoMapper.getBrevParametere();
    }

    @Override
    public Long lagreDokumentData(Long behandlingId, DokumentType dokumentType) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        DokumentData dokumentData = opprettDokumentDataForBehandling(behandlingId, dokumentType);
        for (DokumentFelles dokumentFelles : dokumentData.getDokumentFelles()) {
            leggTilFlettefelterIDokumentFelles(dokumentFelles, dokumentType, behandling);
        }
        dokumentRepository.lagre(dokumentData);
        return dokumentData.getId();
    }

    private void leggTilFlettefelterIDokumentFelles(DokumentFelles dokumentFelles, DokumentType dokumentMal, Behandling behandling) {
        List<Flettefelt> flettefelter = hentFlettefelter(dokumentMal, behandling, dokumentFelles);
        dokumentFelles.getDokumentTypeDataListe().addAll(fraFlettefelter(flettefelter, dokumentFelles));
    }

    private List<Flettefelt> hentFlettefelter(DokumentType dokumentMal, Behandling behandling, DokumentFelles dokumentFelles) {
        DokumentTypeDto dokumentTypeDto = dokumentTypeDtoMapper.mapToDto(behandling,
            dokumentFelles.getSakspartNavn(),
            dokumentFelles.getSakspartPersonStatus(), dokumentMal.harPerioder());
        return dokumentMal.getFlettefelter(dokumentTypeDto);
    }

    private List<DokumentTypeData> fraFlettefelter(List<Flettefelt> flettefelter, DokumentFelles dokumentFelles) {
        return flettefelter.stream().map(flettefelt -> fraFlettefelt(flettefelt, dokumentFelles)).collect(Collectors.toList());
    }

    DokumentTypeData fraFlettefelt(Flettefelt f, DokumentFelles dokumentFelles) {
        DokumentTypeData dtd = new DokumentTypeData();
        dtd.setDoksysId(f.getFeltnavn());
        if (f.isStrukturert()) {
            dtd.setStrukturertVerdi(f.getFeltverdi());
        } else {
            dtd.setVerdi(f.getFeltverdi());
        }
        dtd.setDokumentFelles(dokumentFelles);
        return dtd;
    }

    DokumentData opprettDokumentDataForBehandling(Long behandlingId, DokumentType dokumentType) {
        DokumentMalType dokumentMalType = dokumentRepository.hentDokumentMalType(dokumentType.getDokumentMalType());
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Fagsak fagsak = behandling.getFagsak();
        DokumentData dokumentData = DokumentData.opprettNy(dokumentMalType, behandling);
        Optional<VergeAggregat> vergeAggregat = repositoryProvider.getVergeGrunnlagRepository().hentAggregat(behandling);

        if (!vergeAggregat.isPresent()) {
            opprettDokumentDataForMottaker(behandling, fagsak, dokumentData, behandling.getFagsak().getNavBruker().getAktørId(), dokumentType);
        } else {
            VergeAggregat aggregat = vergeAggregat.get();
            BrevMottaker brevMottaker = aggregat.getBrevMottaker();
            if (BrevMottaker.SØKER.equals(brevMottaker)) {
                opprettDokumentDataForMottaker(behandling, fagsak, dokumentData, behandling.getFagsak().getNavBruker().getAktørId(), dokumentType);
            } else if (BrevMottaker.VERGE.equals(brevMottaker)) {
                opprettDokumentDataForMottaker(behandling, fagsak, dokumentData, aggregat.getAktørId(), dokumentType);
            } else if (BrevMottaker.BEGGE.equals(brevMottaker)) {
                opprettDokumentDataForMottaker(behandling, fagsak, dokumentData, behandling.getFagsak().getNavBruker().getAktørId(), dokumentType);
                opprettDokumentDataForMottaker(behandling, fagsak, dokumentData, aggregat.getAktørId(), dokumentType);
            }
        }
        return dokumentData;
    }

    private void opprettDokumentDataForMottaker(Behandling behandling, Fagsak fagsak, DokumentData dokumentData, AktørId aktørId, DokumentType dokumentType) {
        Adresseinfo adresseinfo = innhentAdresseopplysningerForDokumentsending(aktørId)
            .orElseThrow(() -> DokumentBestillerFeil.FACTORY.fantIkkeAdresse(aktørId).toException());

        DokumentAdresse adresse = fra(adresseinfo);
        AktørId aktørIdBruker = fagsak.getAktørId();
        PersonIdent fnrBruker;
        String navnBruker;
        PersonstatusType personstatusBruker;

        String avsenderEnhet = behandling.getBehandlendeOrganisasjonsEnhet().getEnhetNavn();

        if (Objects.equals(aktørId, aktørIdBruker)) {
            fnrBruker = adresseinfo.getPersonIdent();
            navnBruker = adresseinfo.getMottakerNavn();
            personstatusBruker = adresseinfo.getPersonstatus();
        } else {
            Personinfo personinfo = tpsTjeneste.hentBrukerForAktør(aktørIdBruker)
                .orElseThrow(() -> DokumentBestillerFeil.FACTORY.fantIkkeFnrForAktørId(aktørIdBruker).toException());
            fnrBruker = personinfo.getPersonIdent();
            navnBruker = personinfo.getNavn();
            personstatusBruker = personinfo.getPersonstatus();
        }
        DokumentFelles.Builder builder = DokumentFelles.builder(dokumentData)
            .medAutomatiskBehandlet(Boolean.TRUE)
            .medDokumentDato(LocalDate.now(FPDateUtil.getOffset()))
            .medKontaktTelefonNummer(norg2KontaktTelefonnummer(avsenderEnhet))
            .medMottakerAdresse(adresse)
            .medMottakerId(adresseinfo.getPersonIdent())
            .medMottakerNavn(adresseinfo.getMottakerNavn())
            .medNavnAvsenderEnhet(norg2NavnAvsenderEnhet(avsenderEnhet))
            .medPostadresse(norg2Postadresse())
            .medReturadresse(norg2Returadresse())
            .medSaksnummer(fagsak.getSaksnummer())
            .medSakspartId(fnrBruker)
            .medSakspartNavn(navnBruker)
            .medSpråkkode(fagsak.getNavBruker().getSpråkkode())
            .medSakspartPersonStatus(dokumentType.getPersonstatusVerdi(personstatusBruker));

        if (behandling.isToTrinnsBehandling()) {
            builder
                .medAutomatiskBehandlet(Boolean.FALSE)
                .medSignerendeSaksbehandlerNavn(behandling.getAnsvarligSaksbehandler())
                .medSignerendeBeslutterNavn(behandling.getAnsvarligBeslutter())
                .medSignerendeBeslutterGeografiskEnhet("N/A");  // FIXME SOMMERFUGL Denne skal vel ikke hardkodes?
        }
        builder.build();
    }

    private Optional<Adresseinfo> innhentAdresseopplysningerForDokumentsending(AktørId aktørId) {
        Optional<Personinfo> optFnr = tpsTjeneste.hentBrukerForAktør(aktørId);
        return optFnr.map(s -> tpsTjeneste.hentAdresseinformasjon(s.getPersonIdent()));
    }

    private DokumentAdresse fra(Adresseinfo adresseinfo) {
        DokumentAdresse adresse = new DokumentAdresse.Builder()
            .medAdresselinje1(adresseinfo.getAdresselinje1())
            .medAdresselinje2(adresseinfo.getAdresselinje2())
            .medAdresselinje3(adresseinfo.getAdresselinje3())
            .medLand(adresseinfo.getLand())
            .medPostNummer(adresseinfo.getPostNr())
            .medPoststed(adresseinfo.getPoststed())
            .build();

        dokumentRepository.lagre(adresse);
        return adresse;
    }

    private DokumentAdresse opprettAdresse() {
        DokumentAdresse adresse = new DokumentAdresse.Builder()
            .medAdresselinje1(returadresseKonfigurasjon.getBrevReturadresseAdresselinje1())
            .medPostNummer(returadresseKonfigurasjon.getBrevReturadressePostnummer())
            .medPoststed(returadresseKonfigurasjon.getBrevReturadressePoststed())
            .build();
        dokumentRepository.lagre(adresse);
        return adresse;
    }

    private DokumentAdresse norg2Returadresse() {
        return opprettAdresse();
    }

    private DokumentAdresse norg2Postadresse() {
        return opprettAdresse();
    }

    private String norg2KontaktTelefonnummer(String behandlendeEnhetNavn) {
        if (behandlendeEnhetNavn == null) {
            return norg2KontaktTelefonNummer;
        }
        return behandlendeEnhetNavn.contains(returadresseKonfigurasjon.getBrevReturadresseKlageEnhet())
            ? norg2NavKlageinstansTelefon : norg2KontaktTelefonNummer;
    }

    private String norg2NavnAvsenderEnhet(String behandlendeEnhetNavn) {
        if (behandlendeEnhetNavn == null) {
            return returadresseKonfigurasjon.getBrevReturadresseEnhetNavn();
        }
        return behandlendeEnhetNavn.contains(returadresseKonfigurasjon.getBrevReturadresseKlageEnhet())
            ? returadresseKonfigurasjon.getBrevReturadresseKlageEnhet() : returadresseKonfigurasjon.getBrevReturadresseEnhetNavn();
    }

    @Override
    public DokumentData hentDokumentData(Long dokumentDataId) {
        return dokumentRepository.hentDokumentData(dokumentDataId);
    }

    @Override
    public void oppdaterDokumentData(DokumentData dokumentData) {
        dokumentRepository.lagre(dokumentData);
    }

    @Override
    public DokumentMalType hentDokumentMalType(String kode) {
        return dokumentRepository.hentDokumentMalType(kode);
    }

    @Override
    public Collection<DokumentMalType> hentAlleDokumentMalTyper() {
        return dokumentRepository.hentAlleDokumentMalTyper();
    }

    @Override
    public List<DokumentData> hentDokumentDataListe(Long behandlingId, String dokumentMal) {
        return dokumentRepository.hentDokumentDataListe(behandlingId, dokumentMal);
    }

    @Override
    public void opprettDokumentBestillerTask(Long dokumentDataId, HistorikkAktør aktør, String dokumentBegrunnelse) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(DokumentBestillerTaskProperties.TASKTYPE);
        prosessTaskData.setProperty(DokumentBestillerTaskProperties.DOKUMENT_DATA_ID_KEY, String.valueOf(dokumentDataId));
        prosessTaskData.setProperty(DokumentBestillerTaskProperties.HISTORIKK_AKTØR_KEY, aktør.getKode());
        prosessTaskData.setProperty(DokumentBestillerTaskProperties.DOKUMENT_BEGRUNNELSE_ID_KEY, dokumentBegrunnelse);
        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(prosessTaskData);
        dokumentBestilt(dokumentDataId);
    }

    private void dokumentBestilt(Long dokumentDataId) {
        DokumentData dokumentData = hentDokumentData(dokumentDataId);
        dokumentData.setBestiltTid(LocalDateTime.now(FPDateUtil.getOffset()));
        oppdaterDokumentData(dokumentData);
    }

    @Override
    public BrevParametere getBrevParametere() {
        return brevParametere;
    }
}
