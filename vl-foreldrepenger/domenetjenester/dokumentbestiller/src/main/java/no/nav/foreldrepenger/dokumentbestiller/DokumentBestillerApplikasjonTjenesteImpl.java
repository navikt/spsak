package no.nav.foreldrepenger.dokumentbestiller;

import static no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerFeil.FACTORY;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynDokumentEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.InnsynRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalRestriksjon;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.HenleggBehandlingDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InnvilgelseForeldrepengerDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.KlageOversendtKlageinstansDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.RevurderingDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.UendretUtfallDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillVedtakBrevDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BrevmalDto;
import no.nav.foreldrepenger.dokumentbestiller.brev.DokumentBestillerTjenesteUtil;
import no.nav.foreldrepenger.dokumentbestiller.brev.DokumentToBrevDataMapper;
import no.nav.foreldrepenger.dokumentbestiller.brev.LagDokumentRelatertTilBehandling;
import no.nav.foreldrepenger.dokumentbestiller.brev.SjekkDokumentTilgjengelig;
import no.nav.foreldrepenger.dokumentbestiller.brev.es.LagEngangsstønadDokumentData;
import no.nav.foreldrepenger.dokumentbestiller.brev.fp.LagForeldrepengerDokumentData;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.ProduserIkkeredigerbartDokumentDokumentErRedigerbart;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.ProduserIkkeredigerbartDokumentDokumentErVedlegg;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Dokumentbestillingsinformasjon;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.FerdigstillForsendelseRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.KnyttVedleggTilForsendelseRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastResponse;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserIkkeredigerbartDokumentRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserIkkeredigerbartDokumentResponse;
import no.nav.vedtak.felles.integrasjon.dokument.produksjon.DokumentproduksjonConsumer;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class DokumentBestillerApplikasjonTjenesteImpl implements DokumentBestillerApplikasjonTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(DokumentBestillerApplikasjonTjenesteImpl.class);
    private DokumentproduksjonConsumer dokumentproduksjonProxyService;
    private DokumentDataTjeneste dokumentDataTjeneste;
    private BrevParametere brevParametere;
    private BehandlingRepository behandlingRepository;
    private InnsynRepository innsynRepository;
    private BehandlingToDokumentbestillingDataMapper behandlingToDokumentbestillingDataMapper;
    private DokumentToBrevDataMapper dokumentToBrevDataMapper;
    private BrevHistorikkinnslag brevHistorikkinnslag;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    public DokumentBestillerApplikasjonTjenesteImpl() {
        // for cdi proxy
    }

    @Inject
    public DokumentBestillerApplikasjonTjenesteImpl(DokumentproduksjonConsumer dokumentproduksjonConsumer,
                                                    DokumentDataTjeneste dokumentDataTjeneste,
                                                    BehandlingRepositoryProvider repositoryProvider,
                                                    BehandlingToDokumentbestillingDataMapper behandlingToDokumentbestillingDataMapper,
                                                    DokumentToBrevDataMapper dokumentToBrevDataMapper,
                                                    BrevHistorikkinnslag brevHistorikkinnslag,
                                                    BehandlingskontrollTjeneste behandlingskontrollTjeneste) {

        Objects.requireNonNull(repositoryProvider, "repositoryProvider");
        this.dokumentproduksjonProxyService = dokumentproduksjonConsumer;
        this.dokumentDataTjeneste = dokumentDataTjeneste;
        this.brevParametere = dokumentDataTjeneste.getBrevParametere();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.innsynRepository = repositoryProvider.getInnsynRepository();
        this.behandlingToDokumentbestillingDataMapper = behandlingToDokumentbestillingDataMapper;
        this.dokumentToBrevDataMapper = dokumentToBrevDataMapper;
        this.brevHistorikkinnslag = brevHistorikkinnslag;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public void knyttVedleggTilForsendelse(JournalpostId knyttesTilJournalpostId, JournalpostId knyttesFraJournalpostId, String dokumentId,
                                           String endretAvNavn) {
        KnyttVedleggTilForsendelseRequest request = new KnyttVedleggTilForsendelseRequest();
        request.setDokumentId(dokumentId);
        request.setEndretAvNavn(endretAvNavn);
        request.setKnyttesFraJournalpostId(knyttesFraJournalpostId.getVerdi());
        request.setKnyttesTilJournalpostId(knyttesTilJournalpostId.getVerdi());
        try {
            dokumentproduksjonProxyService.knyttVedleggTilForsendelse(request);
        } catch (Exception e) {
            throw BrevFeil.FACTORY.knyttingAvVedleggFeil(dokumentId, e).toException();
        }
    }

    @Override
    public byte[] forhandsvisDokument(Long dokumentDataId) {
        byte[] dokument = null;
        // Innhente dokumentdata og behandlingsdata fra Behandlinga
        final DokumentData dokumentData = dokumentDataTjeneste.hentDokumentData(dokumentDataId);

        Element brevXmlElement = dokumentToBrevDataMapper.mapTilBrevdata(dokumentData, dokumentData.getFørsteDokumentFelles());

        ProduserDokumentutkastRequest produserDokumentutkastRequest = new ProduserDokumentutkastRequest();
        produserDokumentutkastRequest.setDokumenttypeId(dokumentData.getDokumentMalType().getDoksysKode());
        produserDokumentutkastRequest.setBrevdata(brevXmlElement);

        ProduserDokumentutkastResponse produserDokumentutkastResponse = dokumentproduksjonProxyService.produserDokumentutkast(produserDokumentutkastRequest);
        if (produserDokumentutkastResponse != null && produserDokumentutkastResponse.getDokumentutkast() != null) {
            dokument = produserDokumentutkastResponse.getDokumentutkast();
            dokumentForhandsvist(dokumentDataId);
        }

        return dokument;
    }

    // TODO (ONYX) forhandsvisVedtaksbrev og produserVedtaksbrev burde refaktoreres til å bruke felles logikk
    @Override
    public byte[] forhandsvisVedtaksbrev(BestillVedtakBrevDto dto, Predicate<Behandling> revurderingMedUendretUtfall) {
        Long behandlingId = dto.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        Long dokumentDataId = lagreDokumentdata(dto, behandling, behandlingsresultat, revurderingMedUendretUtfall);
        return forhandsvisDokument(dokumentDataId);
    }

    private Long lagreDokumentdata(BestillVedtakBrevDto dto,
                                   Behandling behandling,
                                   Behandlingsresultat behandlingsresultat,
                                   Predicate<Behandling> revurderingMedUendretUtfallEllerFritekstBrev) {
        final boolean erRevurderingMedUendretUtfall = revurderingMedUendretUtfallEllerFritekstBrev.test(behandling);
        final BehandlingVedtak behandlingVedtak = behandlingsresultat.getBehandlingVedtak();
        if (behandling.getFagsakYtelseType().gjelderForeldrepenger()) {
            return new LagForeldrepengerDokumentData(dokumentDataTjeneste, erRevurderingMedUendretUtfall).lagDokumentData(
                true,
                dto,
                behandling,
                behandlingsresultat,
                behandlingVedtak);
        } else {
            return new LagEngangsstønadDokumentData(dokumentDataTjeneste, erRevurderingMedUendretUtfall).lagDokumentData(
                true,
                dto,
                behandling,
                behandlingsresultat,
                behandlingVedtak);
        }
    }

    @Override
    public void produserVedtaksbrev(BehandlingVedtak behandlingVedtak) {
        Long dokumentDataId;
        final Behandlingsresultat behandlingsresultat = behandlingVedtak.getBehandlingsresultat();
        final Behandling behandling = behandlingsresultat.getBehandling();
        final FagsakYtelseType ytelseType = behandling.getFagsakYtelseType();
        if (Vedtaksbrev.INGEN.equals(behandlingsresultat.getVedtaksbrev())) {
            return;
        } else if (ytelseType.gjelderForeldrepenger()) {
            dokumentDataId = new LagForeldrepengerDokumentData(dokumentDataTjeneste, false).lagDokumentData(
                false,
                null,
                behandling,
                behandlingsresultat,
                behandlingVedtak);
        } else {
            dokumentDataId = new LagEngangsstønadDokumentData(dokumentDataTjeneste, false).lagDokumentData(
                false,
                null,
                behandling,
                behandlingsresultat,
                behandlingVedtak);
        }
        produserDokument(dokumentDataId, HistorikkAktør.VEDTAKSLØSNINGEN, null);
    }

    @Override
    public void ferdigstillForsendelse(JournalpostId journalpostId, String endretAvNavn) {
        FerdigstillForsendelseRequest request = new FerdigstillForsendelseRequest();
        request.setJournalpostId(journalpostId.getVerdi());
        request.setEndretAvNavn(endretAvNavn);
        try {
            dokumentproduksjonProxyService.ferdigstillForsendelse(request);
        } catch (Exception e) {
            throw BrevFeil.FACTORY.ferdigstillingAvDokumentFeil(journalpostId, e).toException();
        }
    }

    @Override
    public void produserDokument(Long dokumentDataId, HistorikkAktør aktør, String dokumentBegrunnelse) {
        DokumentData dokumentData = dokumentDataTjeneste.hentDokumentData(dokumentDataId);
        Collection<InnsynDokumentEntitet> vedlegg = hentAlleVedleggForBehandling(dokumentData.getBehandling().getId(), dokumentData.getDokumentMalType().getKode());
        boolean harVedlegg = !vedlegg.isEmpty();
        for (DokumentFelles dokumentFelles : dokumentData.getDokumentFelles()) {
            try {
                final Dokumentbestillingsinformasjon dokumentbestillingsinformasjon = behandlingToDokumentbestillingDataMapper.mapFraBehandling(dokumentData,
                    dokumentFelles, harVedlegg);

                final ProduserIkkeredigerbartDokumentRequest produserIkkeredigerbartDokumentRequest = new ProduserIkkeredigerbartDokumentRequest();
                produserIkkeredigerbartDokumentRequest.setDokumentbestillingsinformasjon(dokumentbestillingsinformasjon);

                // Generere XML iht mal og validere mot xsd
                Element brevXmlElement = dokumentToBrevDataMapper.mapTilBrevdata(dokumentData, dokumentFelles);

                produserIkkeredigerbartDokumentRequest.setBrevdata(brevXmlElement);

                ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = dokumentproduksjonProxyService
                    .produserIkkeredigerbartDokument(produserIkkeredigerbartDokumentRequest);
                JournalpostId journalpostId = new JournalpostId(produserIkkeredigerbartDokumentResponse.getJournalpostId());
                dokumentSendt(dokumentDataId, dokumentFelles.getId(), journalpostId,
                    produserIkkeredigerbartDokumentResponse.getDokumentId(), aktør, dokumentBegrunnelse);

                if (harVedlegg) {
                    knyttAlleVedleggTilDokument(vedlegg, journalpostId, dokumentData.getBehandling().getEndretAv());
                    ferdigstillForsendelse(journalpostId, dokumentData.getBehandling().getEndretAv());
                }
            } catch (ProduserIkkeredigerbartDokumentDokumentErRedigerbart | ProduserIkkeredigerbartDokumentDokumentErVedlegg funksjonellFeil) {
                throw FACTORY.feilFraDokumentProduksjon(dokumentDataId, funksjonellFeil).toException();
            }
        }
    }

    private void knyttAlleVedleggTilDokument(Collection<InnsynDokumentEntitet> vedlegg, JournalpostId journalpostId, String endretAv) {
        vedlegg.forEach(v -> knyttVedleggTilForsendelse(journalpostId, v.getJournalpostId(), v.getDokumentId(), endretAv));
    }

    private Collection<InnsynDokumentEntitet> hentAlleVedleggForBehandling(long behandlingId, String dokumentMalKode) {
        List<InnsynEntitet> innsynListe = innsynRepository.hentForBehandling(behandlingId);
        if (!innsynListe.isEmpty() && DokumentMalType.INNSYNSKRAV_SVAR.equals(dokumentMalKode)) {
            List<InnsynDokumentEntitet> dokumenter = innsynRepository.hentDokumenterForInnsyn(innsynListe.get(0).getId());
            return DokumentBestillerTjenesteUtil.filtrerUtDuplikater(dokumenter);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void dokumentForhandsvist(Long dokumentDataId) {
        DokumentData dokumentData = dokumentDataTjeneste.hentDokumentData(dokumentDataId);
        dokumentData.setForhåndsvistTid(LocalDateTime.now(FPDateUtil.getOffset()));
        dokumentDataTjeneste.oppdaterDokumentData(dokumentData);
        LOG.info("Dokument av type {} i behandling id {} er forhåndsvist", dokumentData.getDokumentMalType().getKode(), dokumentData.getBehandling().getId()); //$NON-NLS-1$
    }

    private void dokumentSendt(Long dokumentDataId, Long dokumentFellesId, JournalpostId journalpostId, String dokumentId, HistorikkAktør historikkAktør,
                               String dokumentBegrunnelse) {
        DokumentData dokumentData = dokumentDataTjeneste.hentDokumentData(dokumentDataId);
        if (dokumentData.getBestiltTid() == null) {
            dokumentData.setBestiltTid(LocalDateTime.now(FPDateUtil.getOffset()));
        }
        dokumentData.setSendtTid(LocalDateTime.now(FPDateUtil.getOffset()));
        dokumentData.getDokumentFelles().stream().filter(df -> df.getId().equals(dokumentFellesId)).forEach(df -> {
            df.setDokumentId(dokumentId);
            df.setJournalpostId(journalpostId);
        });
        dokumentDataTjeneste.oppdaterDokumentData(dokumentData);
        brevHistorikkinnslag.opprettHistorikkinnslagForSendtBrev(historikkAktør, dokumentBegrunnelse, dokumentData, dokumentId, journalpostId);
        LOG.info("Dokument av type {} i behandling id {} er produsert med dokument id {} og journalpost id {}",
            dokumentData.getDokumentMalType().getKode(), dokumentData.getBehandling().getId(),
            dokumentId,
            journalpostId); // $NON-NLS-1$
    }

    @Override
    public List<String> hentMottakere(Long behandlingId) {
        // TODO (ONYX): Hard-coded for now.
        return Collections.singletonList("Søker");
    }

    @Override
    public List<BrevmalDto> hentBrevmalerFor(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        List<DokumentMalType> kandidater = new ArrayList<>(dokumentDataTjeneste.hentAlleDokumentMalTyper());
        List<DokumentMalType> fjernes = filtrerUtilgjengeligBrevmaler(behandling, kandidater, automatiskOpprettet(behandling));
        kandidater.removeAll(fjernes);
        return tilBrevmalDto(behandling, sorterte(kandidater));
    }

    private boolean automatiskOpprettet(Behandling behandling) {
        return !behandling.erManueltOpprettet();
    }

    private List<DokumentMalType> sorterte(List<DokumentMalType> kandidater) {
        List<DokumentMalType> sorterte = new ArrayList<>();
        kandidater.stream()
            .filter(dm -> DokumentMalRestriksjon.INGEN.equals(dm.getDokumentMalRestriksjon()))
            .forEach(sorterte::add);
        kandidater.stream()
            .filter(dm -> !(DokumentMalRestriksjon.INGEN.equals(dm.getDokumentMalRestriksjon())))
            .forEach(sorterte::add);
        return sorterte;
    }

    @Override
    public Long lagreDokumentdata(BestillBrevDto bestillBrevDto) {
        DokumentType dokumentType = lagDokument(bestillBrevDto);
        return dokumentDataTjeneste.lagreDokumentData(bestillBrevDto.getBehandlingId(), dokumentType);
    }

    private DokumentType lagDokument(BestillBrevDto bestillBrevDto) {
        if (DokumentMalType.HENLEGG_BEHANDLING_DOK.equals(bestillBrevDto.getBrevmalkode())) {
            return new HenleggBehandlingDokument();
        } else if (DokumentMalType.REVURDERING_DOK.equals(bestillBrevDto.getBrevmalkode())) {
            return new RevurderingDokument(brevParametere, bestillBrevDto.getFritekst(), bestillBrevDto.getÅrsakskode());
        } else if (DokumentMalType.UENDRETUTFALL_DOK.equals(bestillBrevDto.getBrevmalkode())) {
            return new UendretUtfallDokument();
        } else if (DokumentMalType.KLAGE_OVERSENDT_KLAGEINSTANS_DOK.equals(bestillBrevDto.getBrevmalkode())) {
            return new KlageOversendtKlageinstansDokument(brevParametere, bestillBrevDto.getFritekst());
        } else if (DokumentMalType.INNVILGELSE_FORELDREPENGER_DOK.equals(bestillBrevDto.getBrevmalkode())) {
            return new InnvilgelseForeldrepengerDokument(brevParametere);
        } else {
            return lagDokumentRelatertTilBehandling(bestillBrevDto);
        }
    }

    private DokumentType lagDokumentRelatertTilBehandling(BestillBrevDto dto) {
        Behandling behandling = behandlingRepository.hentBehandling(dto.getBehandlingId());
        return new LagDokumentRelatertTilBehandling(dokumentDataTjeneste).lagDokumentData(behandling, dto.getBrevmalkode(), dto.getFritekst());
    }

    @Override
    public byte[] hentForhåndsvisningDokument(BestillBrevDto bestillBrevDto) {
        Long dokumentdataId = lagreDokumentdata(bestillBrevDto);
        return forhandsvisDokument(dokumentdataId);
    }

    @Override
    public Long bestillDokument(BestillBrevDto bestillBrevDto, HistorikkAktør aktør, String dokumentBegrunnelse) {
        Long dokumentDataId = lagreDokumentdata(bestillBrevDto);
        DokumentData dokumentData = dokumentDataTjeneste.hentDokumentData(dokumentDataId);
        brevHistorikkinnslag.opprettHistorikkinnslagForBestiltBrev(aktør, dokumentBegrunnelse, dokumentData);
        dokumentDataTjeneste.opprettDokumentBestillerTask(dokumentDataId, HistorikkAktør.VEDTAKSLØSNINGEN, dokumentBegrunnelse);
        return dokumentDataId;
    }

    @Override
    public Long bestillDokument(BestillBrevDto bestillBrevDto, HistorikkAktør aktør) {
        return bestillDokument(bestillBrevDto, aktør, null);
    }

    @Override
    public boolean erDokumentProdusert(Long behandlingId, String dokumentMalTypeKode) {
        return new SjekkDokumentTilgjengelig(dokumentDataTjeneste)
            .erDokumentProdusert(behandlingId, dokumentMalTypeKode);
    }

    @Override
    public void settBehandlingPåVent(Long behandlingId, Venteårsak venteårsak) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT,
            LocalDateTime.now().plusDays(14), venteårsak);
    }

    // Fjerner dokumentmaler som aldri er relevante for denne behandlingstypen
    private List<DokumentMalType> filtrerUtilgjengeligBrevmaler(Behandling behandling, List<DokumentMalType> kandidater, boolean automatiskOpprettet) {
        List<DokumentMalType> fjernes = kandidater.stream()
            .filter(dm -> !dm.erGenerisk())
            .collect(Collectors.toList());
        if (behandling.erKlage() || DokumentBestillerTjenesteUtil.harBehandledeAksjonspunktVarselOmRevurdering(behandling)) {
            fjernes.add(dokumentDataTjeneste.hentDokumentMalType(DokumentMalType.FORLENGET_DOK));
            fjernes.add(dokumentDataTjeneste.hentDokumentMalType(DokumentMalType.FORLENGET_MEDL_DOK));
            fjernes.add(dokumentDataTjeneste.hentDokumentMalType(DokumentMalType.REVURDERING_DOK));
        } else if (behandling.erRevurdering()) {
            if (!automatiskOpprettet) {
                fjernes.add(dokumentDataTjeneste.hentDokumentMalType(DokumentMalType.FORLENGET_DOK));
                fjernes.add(dokumentDataTjeneste.hentDokumentMalType(DokumentMalType.FORLENGET_MEDL_DOK));
            }
        } else {
            fjernes.add(dokumentDataTjeneste.hentDokumentMalType(DokumentMalType.REVURDERING_DOK));
        }
        return fjernes;
    }

    // Markerer som ikke tilgjengelige de brevmaler som ikke er aktuelle i denne behandlingen
    private List<BrevmalDto> tilBrevmalDto(Behandling behandling, List<DokumentMalType> dmtList) {
        List<BrevmalDto> brevmalDtoList = new ArrayList<>(dmtList.size());
        for (DokumentMalType dmt : dmtList) {
            boolean tilgjengelig = new SjekkDokumentTilgjengelig(dokumentDataTjeneste).sjekkOmTilgjengelig(behandling, dmt);
            brevmalDtoList.add(new BrevmalDto(dmt.getKode(), dmt.getNavn(), dmt.getDokumentMalRestriksjon(), tilgjengelig));
        }
        return brevmalDtoList;
    }
}
