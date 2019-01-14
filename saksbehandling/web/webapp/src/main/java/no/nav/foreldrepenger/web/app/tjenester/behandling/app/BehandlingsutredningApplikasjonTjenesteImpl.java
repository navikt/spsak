package no.nav.foreldrepenger.web.app.tjenester.behandling.app;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.INFO;
import static no.nav.vedtak.feil.LogLevel.WARN;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;

// FIXME (DIAMANT): Denne tjenesten har altfor mye domene logikk til å høre hjemme i web laget.
// Bør splittes og skille ut domene logikk i egen domene tjeneste som flyttes (p.t.) under Støttetjenester.
// Her er også sammenblanding med DTO'er:
// HistorikkInnslagDtO
// TilgrensendeYtelserDto
@ApplicationScoped
public class BehandlingsutredningApplikasjonTjenesteImpl implements BehandlingsutredningApplikasjonTjeneste {

    private Period defaultVenteFrist;
    private BehandlingRepository behandlingRepository;
    private KodeverkRepository kodeverkRepository;
    private HistorikkTjenesteAdapter historikkApplikasjonTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private RevurderingTjenesteProvider revurderingTjenesteProvider;
    private SaksbehandlingDokumentmottakTjeneste saksbehandlingDokumentmottakTjeneste;

    BehandlingsutredningApplikasjonTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingsutredningApplikasjonTjenesteImpl(@KonfigVerdi(value = "behandling.default.ventefrist.periode") Period defaultVenteFrist,
                                                       GrunnlagRepositoryProvider grunnlagRepositoryProvider,
                                                       HistorikkTjenesteAdapter historikkApplikasjonTjeneste,
                                                       BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                                       RevurderingTjenesteProvider revurderingTjenesteProvider,
                                                       SaksbehandlingDokumentmottakTjeneste saksbehandlingDokumentmottakTjeneste) {
        Objects.requireNonNull(grunnlagRepositoryProvider, "grunnlagRepositoryProvider");
        this.defaultVenteFrist = defaultVenteFrist;
        this.behandlingRepository = grunnlagRepositoryProvider.getBehandlingRepository();
        this.kodeverkRepository = grunnlagRepositoryProvider.getKodeverkRepository();
        this.historikkApplikasjonTjeneste = historikkApplikasjonTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.revurderingTjenesteProvider = revurderingTjenesteProvider;
        this.saksbehandlingDokumentmottakTjeneste = saksbehandlingDokumentmottakTjeneste;
    }

    @Override
    public List<Behandling> hentBehandlingerForSaksnummer(Saksnummer saksnummer) {
        List<Behandling> behandlinger = behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(saksnummer);
        return behandlinger;
    }

    @Override
    public void settBehandlingPaVent(Long behandlingsId, LocalDate frist, Venteårsak venteårsak) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT;

        doSetBehandlingPåVent(behandlingsId, aksjonspunktDefinisjon, frist, venteårsak);
    }

    private void doSetBehandlingPåVent(Long behandlingsId, AksjonspunktDefinisjon apDef, LocalDate frist,
                                       Venteårsak venteårsak) {

        LocalDateTime fristTid = bestemFristForBehandlingVent(frist);

        Behandling behandling = behandlingRepository.hentBehandling(behandlingsId);
        BehandlingStegType behandlingStegFunnet = behandling.getAksjonspunktMedDefinisjonOptional(apDef)
            .map(Aksjonspunkt::getBehandlingStegFunnet)
            .orElse(null); // Dersom autopunkt ikke allerede er opprettet, så er det ikke tilknyttet steg
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, apDef, behandlingStegFunnet, fristTid, venteårsak);
    }

    @Override
    public void endreBehandlingPaVent(Long behandlingsId, LocalDate frist, Venteårsak venteårsak) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingsId);
        if (!behandling.isBehandlingPåVent()) {
            throw BehandlingsutredningApplikasjonTjenesteFeil.FACTORY.kanIkkeEndreVentefristForBehandlingIkkePaVent(behandlingsId)
                .toException();
        }
        AksjonspunktDefinisjon aksjonspunktDefinisjon = behandling.getBehandlingPåVentAksjonspunktDefinisjon();
        doSetBehandlingPåVent(behandlingsId, aksjonspunktDefinisjon, frist, venteårsak);
    }

    private LocalDateTime bestemFristForBehandlingVent(LocalDate frist) {
        return frist != null
            ? LocalDateTime.of(frist, LocalDateTime.now(FPDateUtil.getOffset()).toLocalTime())
            : LocalDateTime.now(FPDateUtil.getOffset()).plus(defaultVenteFrist);
    }

    @Override
    public void byttBehandlendeEnhet(Long behandlingId, OrganisasjonsEnhet enhet, String begrunnelse, HistorikkAktør aktør) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        lagHistorikkInnslagForByttBehandlendeEnhet(behandling, HistorikkinnslagType.BYTT_ENHET, enhet.getEnhetId(), enhet.getEnhetNavn(), begrunnelse, aktør);
        oppdaterBehandlingMedNyEnhet(behandling, enhet, begrunnelse);
    }

    private void lagHistorikkInnslagForByttBehandlendeEnhet(Behandling behandling, HistorikkinnslagType historikkinnslagType,
                                                            String enhetId, String enhetNavn, String begrunnelse, HistorikkAktør aktør) {
        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder();
        builder.medHendelse(historikkinnslagType);
        builder.medEndretFelt(HistorikkEndretFeltType.BEHANDLENDE_ENHET,
            behandling.getBehandlendeOrganisasjonsEnhet().getEnhetId() + " " + behandling.getBehandlendeOrganisasjonsEnhet().getEnhetNavn(),
            enhetId + " " + enhetNavn);
        builder.medBegrunnelse(begrunnelse);

        Historikkinnslag innslag = new Historikkinnslag();
        innslag.setAktør(aktør);
        innslag.setType(historikkinnslagType);
        innslag.setBehandlingId(behandling.getId());
        builder.build(innslag);

        historikkApplikasjonTjeneste.lagInnslag(innslag);
    }

    @Override
    public void opprettNyFørstegangsbehandling(Long fagsakId, Saksnummer saksnummer) {

        List<Behandling> behandlinger = behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(saksnummer);

        if (erLovÅOppretteNyBehandling(behandlinger)) {
            String behandlingÅrsaktypeKode = null;
            // FIXME SP : Må finne på noe annet her.
            saksbehandlingDokumentmottakTjeneste.dokumentAnkommet(tilSaksdokument(fagsakId, null, behandlingÅrsaktypeKode));
        } else {
            throw BehandlingsutredningApplikasjonTjenesteFeil.FACTORY.kanIkkeOppretteNyFørstegangsbehandling(fagsakId).toException();
        }
    }

    private BehandlingTema utledBehandlingTema(DokumentTypeId dokumentTypeId) {
        // FIXME SP : Finnes logikk for å avlede behandlingtema for sykepenger?
        final BehandlingTema behandlingTemaKonst = BehandlingTema.SYKEPENGER;
        return kodeverkRepository.finn(BehandlingTema.class, behandlingTemaKonst);
    }

    private InngåendeSaksdokument tilSaksdokument(Long fagsakId, Object mottattDokument, String behandlingÅrsakType) {
        return InngåendeSaksdokument.builder()
            .medBehandlingTema(kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.SYKEPENGER))
            .medFagsakId(fagsakId)
            .build();
    }

    private boolean erLovÅOppretteNyBehandling(List<Behandling> behandlinger) {

        boolean ingenApenYtelsesBeh = behandlinger.stream()
            .noneMatch(b -> (b.getType().equals(BehandlingType.REVURDERING) && !b.erAvsluttet())
                || (b.getType().equals(BehandlingType.FØRSTEGANGSSØKNAD) && !b.erAvsluttet()));

        boolean minstEnAvsluttet = behandlinger.stream().anyMatch(Behandling::erAvsluttet);

        return ingenApenYtelsesBeh && minstEnAvsluttet;
    }

    @Override
    public Behandling opprettRevurdering(Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType) {
        RevurderingTjeneste revurderingTjeneste = revurderingTjenesteProvider.finnRevurderingTjenesteFor(fagsak);
        Boolean kanRevurderingOpprettes = revurderingTjeneste.kanRevurderingOpprettes(fagsak);
        if (!kanRevurderingOpprettes) {
            throw BehandlingsutredningApplikasjonTjenesteFeil.FACTORY.kanIkkeOppretteRevurdering(fagsak.getSaksnummer()).toException();
        }
        return revurderingTjeneste.opprettManuellRevurdering(fagsak, behandlingÅrsakType);
    }

    @Override
    public void kanEndreBehandling(Long behandlingId, Long versjon) {
        Boolean kanEndreBehandling = behandlingRepository.erVersjonUendret(behandlingId, versjon);
        if (!kanEndreBehandling) {
            throw BehandlingsutredningApplikasjonTjenesteFeil.FACTORY.endringerHarForekommetPåBehandlingen().toException();
        }
    }

    private void oppdaterBehandlingMedNyEnhet(Behandling behandling, OrganisasjonsEnhet enhet, String begrunnelse) {
        behandling.setBehandlendeEnhet(enhet);
        behandling.setBehandlendeEnhetÅrsak(begrunnelse);

        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
    }

    interface BehandlingsutredningApplikasjonTjenesteFeil extends DeklarerteFeil {
        BehandlingsutredningApplikasjonTjenesteFeil FACTORY = FeilFactory.create(BehandlingsutredningApplikasjonTjenesteFeil.class); // NOSONAR

        @FunksjonellFeil(feilkode = "FP-992332", feilmelding = "BehandlingId %s er ikke satt på vent, og ventefrist kan derfor ikke oppdateres", løsningsforslag = "Forsett saksbehandlingen", logLevel = ERROR)
        Feil kanIkkeEndreVentefristForBehandlingIkkePaVent(Long behandlingId);

        @TekniskFeil(feilkode = "FP-759759", feilmelding = "BehandlingId %s kan ikke settes på vent for aksjonspunkt %s, da steget hvor aksjonspunkt er funnet er ukjent ", logLevel = ERROR)
        Feil kanIkkeSetteNårAutopunktSittStegErUkjent(Long behandlingId, String apKode);

        @FunksjonellFeil(feilkode = "FP-663487", feilmelding = "Fagsak med saksnummer %s oppfyller ikke kravene for revurdering", løsningsforslag = "", logLevel = INFO)
        Feil kanIkkeOppretteRevurdering(Saksnummer saksnummer);

        @FunksjonellFeil(feilkode = "FP-946285", feilmelding = "BehandlingId %s manglar registeropplysninger", løsningsforslag = "", logLevel = ERROR)
        Feil kanIkkeHenteRegisteropplysninger(Long behandlingsId);

        @FunksjonellFeil(feilkode = "FP-909861", feilmelding = "Det eksisterer allerede en åpen ytelsesbehandling eller det eksisterer ingen avsluttede behandlinger for fagsakId %s", løsningsforslag = "", logLevel = ERROR)
        Feil kanIkkeOppretteNyFørstegangsbehandling(Long fagsakId);

        @FunksjonellFeil(feilkode = "FP-287882", feilmelding = "Ingen MottattDokument av type søknad funnet for fagsakId %s", løsningsforslag = "", logLevel = ERROR)
        Feil ingenSøknaderÅOppretteNyFørstegangsbehandlingPå(Long fagsakId);

        @FunksjonellFeil(feilkode = "FP-837578", feilmelding = "Behandlingen er endret av en annen saksbehandler, eller har blitt oppdatert med ny informasjon av systemet.", løsningsforslag = "Last inn behandlingen på nytt.", logLevel = WARN)
        Feil endringerHarForekommetPåBehandlingen();
    }
}
