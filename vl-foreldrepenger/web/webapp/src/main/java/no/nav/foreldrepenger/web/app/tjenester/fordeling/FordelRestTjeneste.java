package no.nav.foreldrepenger.web.app.tjenester.fordeling;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.behandling.BehandlendeFagsystem;
import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.domene.vurderfagsystem.VurderFagsystem;
import no.nav.foreldrepenger.domene.vurderfagsystem.VurderFagsystemTjeneste;
import no.nav.foreldrepenger.sak.tjeneste.OpprettSakOrchestrator;
import no.nav.foreldrepenger.sak.tjeneste.OpprettSakTjeneste;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Mottar dokumenter fra f.eks. FPFORDEL og håndterer dispatch internt for saksbehandlingsløsningen.
 */
@Api(tags = {"fordel"})
@Path("/fordel")
@RequestScoped
@Transaction
public class FordelRestTjeneste {
    // FIXME (u139158): PK-44270 Må versjonere tjenestene og DTO'ene

    private static final String JSON_UTF8 = "application/json; charset=UTF-8";

    private SaksbehandlingDokumentmottakTjeneste dokumentmottakTjeneste;
    private DokumentArkivTjeneste dokumentArkivTjeneste;
    private FagsakTjeneste fagsakTjeneste;
    private OpprettSakOrchestrator opprettSakOrchestrator;
    private OpprettSakTjeneste opprettSakTjeneste;
    private KodeverkRepository kodeverkRepository;
    private VurderFagsystemTjeneste vurderFagsystemTjeneste;
    private FamilieHendelseRepository familieGrunnlagRepository;
    private BehandlingRepository behandlingRepository;

    public FordelRestTjeneste() {// For Rest-CDI
    }

    @Inject
    public FordelRestTjeneste(SaksbehandlingDokumentmottakTjeneste dokumentmottakTjeneste,
                              DokumentArkivTjeneste dokumentArkivTjeneste,
                              FagsakTjeneste fagsakTjeneste, OpprettSakOrchestrator opprettSakOrchestrator, OpprettSakTjeneste opprettSakTjeneste,
                              BehandlingRepositoryProvider repositoryProvider, VurderFagsystemTjeneste vurderFagsystemTjeneste) {
        this.dokumentmottakTjeneste = dokumentmottakTjeneste;
        this.dokumentArkivTjeneste = dokumentArkivTjeneste;
        this.fagsakTjeneste = fagsakTjeneste;
        this.opprettSakOrchestrator = opprettSakOrchestrator;
        this.opprettSakTjeneste = opprettSakTjeneste;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.vurderFagsystemTjeneste = vurderFagsystemTjeneste;
    }

    @POST
    @Timed
    @Path("/vurderFagsystem")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Informasjon om en fagsak"// , notes = ("Varsel om en ny journalpost som skal behandles i systemet.")
    )
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public BehandlendeFagsystemDto vurderFagsystem(@ApiParam("Krever behandlingstemaOffisiellKode") @Valid VurderFagsystemDto vurderFagsystemDto) {
        VurderFagsystem vurderFagsystem = map(vurderFagsystemDto);
        BehandlendeFagsystem behandlendeFagsystem = vurderFagsystemTjeneste.vurderFagsystem(vurderFagsystem);
        return map(behandlendeFagsystem);

    }

    @POST
    @Timed
    @Path("/fagsak/informasjon")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Informasjon om en fagsak"// , notes = ("Varsel om en ny journalpost som skal behandles i systemet.")
    )
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public FagsakInfomasjonDto fagsak(@ApiParam("Saksnummeret det skal hentes saksinformasjon om") @Valid SaksnummerDto saksnummerDto) {
        Optional<Fagsak> optFagsak = fagsakTjeneste.finnFagsakGittSaksnummer(new Saksnummer(saksnummerDto.getSaksnummer()), false);
        if (!optFagsak.isPresent()) {
            // FIXME (u139158): PK- hvordan skal dette håndteres?
            // throw BehandleDokumentServiceFeil.FACTORY.finnerIkkeFagsak(removeLineBreaks(saksnummer.toString())).toException();
            return null;
        }
        final Optional<Behandling> behandling = behandlingRepository.hentSisteBehandlingForFagsakId(optFagsak.get().getId());
        FamilieHendelse familieHendelse = null;
        if (behandling.isPresent()) {
            familieHendelse = familieGrunnlagRepository.hentAggregatHvisEksisterer(behandling.get()).map(FamilieHendelseGrunnlag::getGjeldendeVersjon).orElse(null);
        }
        BehandlingTema behandlingTemaFraKodeverksRepo = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.fraFagsak(optFagsak.get(), familieHendelse));
        String behandlingstemaOffisiellKode = behandlingTemaFraKodeverksRepo.getOffisiellKode();
        AktørId aktørId = optFagsak.get().getAktørId();
        return new FagsakInfomasjonDto(aktørId.getId(), behandlingstemaOffisiellKode);
    }

    @POST
    @Timed
    @Path("/fagsak/opprett")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Ny journalpost skal behandles.", notes = ("Varsel om en ny journalpost som skal behandles i systemet."))
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public SaksnummerDto opprettSak(@ApiParam("Oppretter fagsak") @Valid OpprettSakDto opprettSakDto) {
        Optional<String> journalpostId = opprettSakDto.getJournalpostId();
        BehandlingTema behandlingTema = kodeverkRepository.finnForKodeverkEiersKode(BehandlingTema.class, opprettSakDto.getBehandlingstemaOffisiellKode(),
            BehandlingTema.UDEFINERT);

        AktørId aktørId = new AktørId(opprettSakDto.getAktørId());

        Saksnummer s;
        if (journalpostId.isPresent()) {
            s = opprettSakOrchestrator.opprettSak(new JournalpostId(journalpostId.get()), behandlingTema, aktørId);
        } else {
            s = opprettSakOrchestrator.opprettSak(behandlingTema, aktørId);
        }
        return new SaksnummerDto(s.getVerdi());
    }

    @POST
    @Timed
    @Path("/fagsak/knyttJournalpost")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Knytt journalpost til fagsak.", notes = ("Før en journalpost journalføres på en fagsak skal fagsaken oppdateres med journalposten."))
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public void knyttSakOgJournalpost(@ApiParam("Saksnummer og JournalpostId som skal knyttes sammen") @Valid JournalpostKnyttningDto journalpostKnytningDto) {
        opprettSakTjeneste.knyttSakOgJournalpost(new Saksnummer(journalpostKnytningDto.getSaksnummer()), new JournalpostId(journalpostKnytningDto.getJournalpostId()));
    }

    @POST
    @Timed
    @Path("/journalpost")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Ny journalpost skal behandles.", notes = ("Varsel om en ny journalpost som skal behandles i systemet."))
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public void mottaJournalpost(@ApiParam("Krever saksnummer, journalpostId og behandlingstemaOffisiellKode") @Valid JournalpostMottakDto mottattJournalpost) {

        InngåendeSaksdokument saksdokument = map(mottattJournalpost);
        dokumentmottakTjeneste.dokumentAnkommet(saksdokument);
    }

    private VurderFagsystem map(VurderFagsystemDto dto) {
        VurderFagsystem v = new VurderFagsystem();
        dto.getJournalpostId().map(jpi -> new JournalpostId(jpi)).ifPresent(v::setJournalpostId);
        v.setStrukturertSøknad(dto.isStrukturertSøknad());
        v.setAktørId(new AktørId(dto.getAktørId()));
        BehandlingTema behandlingTema = kodeverkRepository.finnForKodeverkEiersKode(BehandlingTema.class, dto.getBehandlingstemaOffisiellKode(),
            BehandlingTema.UDEFINERT);

        v.setBehandlingTema(behandlingTema);
        v.setAdopsjonsbarnFodselsdatoer(dto.getAdopsjonsBarnFodselsdatoer());

        dto.getBarnTermindato().ifPresent(v::setBarnTermindato);
        dto.getBarnFodselsdato().ifPresent(v::setBarnFodselsdato);
        dto.getOmsorgsovertakelsedato().ifPresent(v::setOmsorgsovertakelsedato);
        dto.getÅrsakInnsendingInntektsmelding().ifPresent(v::setÅrsakInnsendingInntektsmelding);
        dto.getVirksomhetsnummer().ifPresent(v::setVirksomhetsnummer);
        dto.getArbeidsforholdsid().ifPresent(v::setArbeidsforholdsid);
        dto.getForsendelseMottatt().ifPresent(v::setForsendelseMottatt);
        dto.getForsendelseMottattTidspunkt().ifPresent(v::setForsendelseMottattTidspunkt);
        dto.getStartDatoForeldrepengerInntektsmelding().ifPresent(v::setStartDatoForeldrepengerInntektsmelding);

        dto.getSaksnummer().ifPresent(sn -> v.setSaksnummer(new Saksnummer(sn)));
        dto.getAnnenPart().map(AktørId::new).ifPresent(v::setAnnenPart);

        v.setDokumentTypeId(DokumentTypeId.UDEFINERT);
        v.setDokumentKategori(DokumentKategori.UDEFINERT);
        if (dto.getDokumentTypeIdOffisiellKode() != null) {
            v.setDokumentTypeId(kodeverkRepository.finnForKodeverkEiersKode(DokumentTypeId.class, dto.getDokumentTypeIdOffisiellKode(), DokumentTypeId.UDEFINERT));
        }
        if (dto.getDokumentKategoriOffisiellKode() != null) {
            v.setDokumentKategori(kodeverkRepository.finnForKodeverkEiersKode(DokumentKategori.class, dto.getDokumentKategoriOffisiellKode(), DokumentKategori.UDEFINERT));
        }

        return v;
    }

    private BehandlendeFagsystemDto map(BehandlendeFagsystem behandlendeFagsystem) {
        BehandlendeFagsystemDto dto;
        if (behandlendeFagsystem.getSaksnummer().isPresent()) {
            dto = new BehandlendeFagsystemDto(behandlendeFagsystem.getSaksnummer().get().getVerdi()); // NOSONAR
        } else {
            dto = new BehandlendeFagsystemDto();
        }
        switch (behandlendeFagsystem.getBehandlendeSystem()) {
            case VEDTAKSLØSNING:
                dto.setBehandlesIVedtaksløsningen(true);
                break;
            case INFOTRYGD:
                dto.setSjekkMotInfotrygd(true);
                break;
            case MANUELL_VURDERING:
                dto.setManuellVurdering(true);
                break;
            case PRØV_IGJEN:
                dto.setPrøvIgjen(true);
                dto.setPrøvIgjenTidspunkt(behandlendeFagsystem.getPrøvIgjenTidspunkt());
                break;
            default:
                throw new IllegalArgumentException("Utviklerfeil, manglende mapping");
        }
        return dto;
    }

    private InngåendeSaksdokument map(JournalpostMottakDto mottattJournalpost) {
        BehandlingTema behandlingTema = kodeverkRepository.finnForKodeverkEiersKode(BehandlingTema.class, mottattJournalpost.getBehandlingstemaOffisiellKode(),
            BehandlingTema.UDEFINERT);
        JournalpostId journalpostId = new JournalpostId(mottattJournalpost.getJournalpostId());

        Saksnummer saksnummer = new Saksnummer(mottattJournalpost.getSaksnummer());
        Optional<Fagsak> fagsak = fagsakTjeneste.finnFagsakGittSaksnummer(saksnummer, false);
        if (!fagsak.isPresent()) {
            // FIXME (u139158): PK- hvordan skal dette håndteres?
            // throw BehandleDokumentServiceFeil.FACTORY.finnerIkkeFagsak(removeLineBreaks(saksnummer.toString())).toException();
            throw new IllegalStateException("Finner ingen fagsak for saksnummer " + saksnummer);
        }

        DokumentTypeId dokumentTypeId = mottattJournalpost.getDokumentTypeIdOffisiellKode().isPresent() ?
            kodeverkRepository.finnForKodeverkEiersKode(DokumentTypeId.class, mottattJournalpost.getDokumentTypeIdOffisiellKode().get(), DokumentTypeId.UDEFINERT) :
            DokumentTypeId.UDEFINERT; // NOSONAR

        DokumentKategori dokumentKategori = mottattJournalpost.getDokumentKategoriOffisiellKode() != null ?
            kodeverkRepository.finnForKodeverkEiersKode(DokumentKategori.class, mottattJournalpost.getDokumentKategoriOffisiellKode(), DokumentKategori.UDEFINERT) :
            DokumentKategori.UDEFINERT; // NOSONAR

        dokumentTypeId = utledDokumentTypeId(saksnummer, journalpostId, dokumentTypeId);
        dokumentKategori = utledDokumentKategori(dokumentKategori, dokumentTypeId);

        InngåendeSaksdokument.Builder builder = InngåendeSaksdokument.builder()
            .medFagsakId(fagsak.get().getId())
            .medBehandlingTema(behandlingTema)
            .medElektroniskSøknad(mottattJournalpost.getPayloadXml().isPresent())
            .medJournalpostId(new JournalpostId(mottattJournalpost.getJournalpostId()))
            .medDokumentTypeId(dokumentTypeId)
            .medDokumentKategori(dokumentKategori)
            .medJournalførendeEnhet(mottattJournalpost.getJournalForendeEnhet());

        mottattJournalpost.getForsendelseId().ifPresent(builder::medForsendelseId);

        if (mottattJournalpost.getPayloadXml().isPresent()) {
            builder.medPayloadXml(mottattJournalpost.getPayloadXml().get()); // NOSONAR
        }

        builder.medForsendelseMottatt(mottattJournalpost.getForsendelseMottatt().orElse(FPDateUtil.iDag())); // NOSONAR

        return builder.build();
    }

    private DokumentTypeId utledDokumentTypeId(Saksnummer saksnummer, JournalpostId journalpostId, DokumentTypeId dokumentTypeId) {
        if (!DokumentTypeId.UDEFINERT.equals(dokumentTypeId)) {
            return dokumentTypeId;
        }
        return dokumentArkivTjeneste.utledDokumentTypeFraTittel(saksnummer, journalpostId);
    }

    private DokumentKategori utledDokumentKategori(DokumentKategori dokumentKategori, DokumentTypeId dokumentTypeId) {
        if (DokumentTypeId.getSøknadTyper().contains(dokumentTypeId)) {
            return DokumentKategori.SØKNAD;
        }
        if (DokumentTypeId.KLAGE_DOKUMENT.equals(dokumentTypeId)) {
            return DokumentKategori.KLAGE_ELLER_ANKE;
        }
        return dokumentKategori;
    }
}
