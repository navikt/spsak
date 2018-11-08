package no.nav.foreldrepenger.økonomistøtte.es;


import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryFeil;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TfradragTillegg;
import no.nav.foreldrepenger.økonomistøtte.OppdragskontrollManager;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeAksjon;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndring;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndringLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeStatusLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiTypeSats;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiUtbetFrekvens;

@ApplicationScoped
public class OppdragskontrollEngangsstønad extends OppdragskontrollManager {

    private static final long INITIAL_VALUE = 99L;

    private static final String KODE_ENDRING_NY = ØkonomiKodeEndring.NY.name();
    private static final String KODE_ENDRING_UENDRET = ØkonomiKodeEndring.UEND.name();
    private static final String KODE_ENDRING_LINJE_NY = ØkonomiKodeEndringLinje.NY.name();
    private static final String KODE_ENDRING_LINJE_ENDRING = ØkonomiKodeEndringLinje.ENDR.name();
    private static final String KODE_KLASSIFIK_FODSEL = "FPENFOD-OP";
    private static final String KODE_KLASSIFIK_ADOPSJON = "FPENAD-OP";
    private static final String FRADRAG_TILLEGG = TfradragTillegg.T.name();
    private static final String TYPE_SATS_ES = ØkonomiTypeSats.ENG.name();
    private static final String BRUK_KJOREPLAN = "N";
    private static final String KODE_STATUS_LINJE_OPPHØR = ØkonomiKodeStatusLinje.OPPH.name();

    private BehandlingRepository behandlingRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private FamilieHendelseRepository familieGrunnlagRepository;
    private TpsTjeneste tpsTjeneste;

    OppdragskontrollEngangsstønad() {
        // For CDI
    }

    @Inject
    public OppdragskontrollEngangsstønad(BehandlingRepositoryProvider repositoryProvider, TpsTjeneste tpsTjeneste) {
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.tpsTjeneste = tpsTjeneste;
    }

    @Override
    public void opprettØkonomiOppdrag(Behandling behandling, Optional<Oppdragskontroll> forrigeOppdrag,
                                      Oppdragskontroll oppdragskontroll) {
        BehandlingInfoES behandlingInfo = oppsettBehandlingInfo(behandling);
        Avstemming115 avstemming115 = opprettAvstemming115();
        Oppdrag110 oppdrag110 = opprettOppdrag110ES(behandlingInfo, oppdragskontroll, avstemming115, forrigeOppdrag);
        opprettOppdragsenhet120(oppdrag110);
        Oppdragslinje150 oppdragslinje150 = opprettOppdragslinje150ES(behandlingInfo, oppdrag110, forrigeOppdrag);
        opprettAttestant180(oppdragslinje150, behandlingInfo.getBehVedtak().getAnsvarligSaksbehandler());
    }

    private Oppdrag110 opprettOppdrag110ES(BehandlingInfoES behandlingInfo, Oppdragskontroll oppdragskontroll,
                                           Avstemming115 avstemming115, Optional<Oppdragskontroll> forrigeOppdragOpt) {
        long fagsystemId;
        String kodeEndring;
        if (forrigeOppdragOpt.isPresent()) {
            Oppdrag110 forrigeOppdrag110 = forrigeOppdragOpt.get().getOppdrag110Liste().get(0);
            fagsystemId = forrigeOppdrag110.getFagsystemId();
            kodeEndring = KODE_ENDRING_UENDRET;
        } else {
            fagsystemId = genererFagsystemId(Long.parseLong(behandlingInfo.getFagsak().getSaksnummer().getVerdi()), INITIAL_VALUE);
            kodeEndring = KODE_ENDRING_NY;
        }

        return Oppdrag110.builder()
            .medKodeAksjon(ØkonomiKodeAksjon.EN.getKodeAksjon())
            .medKodeEndring(kodeEndring)
            .medKodeFagomrade(ØkonomiKodeFagområde.REFUTG.name())
            .medFagSystemId(fagsystemId)
            .medUtbetFrekvens(ØkonomiUtbetFrekvens.MÅNED.getUtbetFrekvens())
            .medOppdragGjelderId(behandlingInfo.getPersonIdent().getIdent())
            .medDatoOppdragGjelderFom(LocalDate.of(2000, 1, 1))
            .medSaksbehId(behandlingInfo.getBehVedtak().getAnsvarligSaksbehandler())
            .medOppdragskontroll(oppdragskontroll)
            .medAvstemming115(avstemming115)
            .build();
    }

    private Oppdragslinje150 opprettOppdragslinje150ES(BehandlingInfoES behandlingInfo, Oppdrag110 oppdrag110, Optional<Oppdragskontroll> forrigeOppdragOpt) {
        String kodeKlassifik = familieGrunnlagRepository.hentAggregat(behandlingInfo.getBehandling()).getGjeldendeVersjon().getGjelderFødsel()
            ? KODE_KLASSIFIK_FODSEL : KODE_KLASSIFIK_ADOPSJON;

        // Her er det 3 varianter
        // 1) Første oppdrag i saken - alltid innvilgelse
        // 2) Endring med nytt innvilgelsesvedtak - ny linje som erstatter foregående
        // 3) Endring til avslag - sette opphørsstatus på foregående linje

        Oppdragslinje150 oppdrlinje150;
        if (forrigeOppdragOpt.isPresent()) {  // Endring, variant 2 eller 3
            Oppdragskontroll forrigeOppdrag = forrigeOppdragOpt.get();
            oppdrlinje150 = opprettOppdragslinje150LinketTilForrigeOppdrag(behandlingInfo, oppdrag110, forrigeOppdrag, kodeKlassifik);
        } else { // Variant 1
            oppdrlinje150 = opprettOppdragslinje150FørsteOppdragES(behandlingInfo, oppdrag110, kodeKlassifik);
        }
        return oppdrlinje150;
    }

    private Oppdragslinje150 opprettOppdragslinje150FørsteOppdragES(BehandlingInfoES behandlingInfo, Oppdrag110 oppdrag110, String kodeKlassifik) {
        LocalDate vedtaksdato = behandlingInfo.getBehVedtak().getVedtaksdato();
        long teller = incrementInitialValue(INITIAL_VALUE);
        long delytelseId = concatenateValues(oppdrag110.getFagsystemId(), teller);
        long satsEngangsstonad = hentSatsFraBehandling(behandlingInfo.getBehandling());

        Oppdragslinje150.Builder oppdragslinje150Builder = Oppdragslinje150.builder()
            .medKodeEndringLinje(KODE_ENDRING_LINJE_NY)
            .medVedtakId(vedtaksdato.toString())
            .medDelytelseId(delytelseId)
            .medKodeKlassifik(kodeKlassifik)
            .medFradragTillegg(FRADRAG_TILLEGG)
            .medBrukKjoreplan(BRUK_KJOREPLAN)
            .medSaksbehId(behandlingInfo.getBehVedtak().getAnsvarligSaksbehandler())
            .medHenvisning(behandlingInfo.getBehandling().getId())
            .medOppdrag110(oppdrag110)
            .medVedtakFomOgTom(vedtaksdato, vedtaksdato)
            .medSats(satsEngangsstonad)
            .medTypeSats(TYPE_SATS_ES)
            .medUtbetalesTilId(behandlingInfo.getPersonIdent().getIdent()); // FIXME (TOPAS): Her brukes fnr, endres til aktørid ved ny versjon av oppdragsmelding
        return oppdragslinje150Builder.build();
    }

    private Oppdragslinje150 opprettOppdragslinje150LinketTilForrigeOppdrag(BehandlingInfoES behandlingInfo, Oppdrag110 oppdrag110, Oppdragskontroll forrigeOppdrag, String kodeKlassifik) {
        Long delytelseId;
        long sats;
        String kodeEndringLinje;
        String kodeStatusLinje = null;
        Oppdrag110 opprOppdrag110 = forrigeOppdrag.getOppdrag110Liste().get(0);
        Long refFagsystemId = opprOppdrag110.getFagsystemId();
        Long refDelytelseId = opprOppdrag110.getOppdragslinje150Liste().get(0).getDelytelseId();
        Behandling tidligereBehandling = behandlingRepository.hentBehandling(forrigeOppdrag.getBehandlingId());
        BehandlingVedtak tidligereVedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(tidligereBehandling.getId())
            .orElseThrow(() -> BehandlingRepositoryFeil.FACTORY.fantIkkeBehandlingVedtak(tidligereBehandling.getId()).toException());
        LocalDate tidligereVedtaksdato = tidligereVedtak.getVedtaksdato();
        LocalDate vedtaksdato = behandlingInfo.getBehVedtak().getVedtaksdato();
        LocalDate statusdato = null;
        if (VedtakResultatType.AVSLAG.equals(behandlingInfo.getBehVedtak().getVedtakResultatType())) { // Variant 3
            delytelseId = opprOppdrag110.getOppdragslinje150Liste().get(0).getDelytelseId();
            sats = hentSatsFraBehandling(tidligereBehandling);
            kodeEndringLinje = KODE_ENDRING_LINJE_ENDRING;
            kodeStatusLinje = KODE_STATUS_LINJE_OPPHØR;
            statusdato = tidligereVedtaksdato;
            refFagsystemId = null;
            refDelytelseId = null;
        } else { // Variant 2
            delytelseId = incrementInitialValue(opprOppdrag110.getOppdragslinje150Liste().get(0).getDelytelseId());
            sats = hentSatsFraBehandling(behandlingInfo.getBehandling());
            kodeEndringLinje = KODE_ENDRING_LINJE_NY;
        }
        return Oppdragslinje150.builder()
            .medKodeEndringLinje(kodeEndringLinje)
            .medKodeStatusLinje(kodeStatusLinje)
            .medDatoStatusFom(statusdato)
            .medVedtakId(vedtaksdato.toString())
            .medDelytelseId(delytelseId)
            .medKodeKlassifik(kodeKlassifik)
            .medVedtakFomOgTom(tidligereVedtaksdato, tidligereVedtaksdato)
            .medSats(sats)
            .medFradragTillegg(FRADRAG_TILLEGG)
            .medTypeSats(TYPE_SATS_ES)
            .medBrukKjoreplan(BRUK_KJOREPLAN)
            .medSaksbehId(behandlingInfo.getBehVedtak().getAnsvarligSaksbehandler())
            // FIXME (TOPAS): Fjern Fnr fra modellen.  Kan det slås opp ved oversending til Økonomi?
            .medUtbetalesTilId(behandlingInfo.getPersonIdent().getIdent()) // FIXME (TOPAS): Her brukes fnr, endres til aktørid ved ny versjon av oppdragsmelding
            .medHenvisning(behandlingInfo.getBehandling().getId())
            .medOppdrag110(oppdrag110)
            .medRefFagsystemId(refFagsystemId)
            .medRefDelytelseId(refDelytelseId)
            .build();
    }

    private long hentSatsFraBehandling(Behandling behandling) {
        Optional<Beregning> beregning = behandling.getBehandlingsresultat().getBeregningResultat().getSisteBeregning();
        return beregning.map(Beregning::getBeregnetTilkjentYtelse).orElse(0L);
    }

    private BehandlingInfoES oppsettBehandlingInfo(Behandling behandling) {
        Fagsak fagsak = behandling.getFagsak();
        PersonIdent personIdent = tpsTjeneste.hentFnrForAktør(behandling.getAktørId());// kallet kan fjernes en gang i fremtiden, når Oppdragssystemet ikke lenger krever fnr i sine meldinger.
        BehandlingVedtak behVedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())
            .orElseThrow(() -> BehandlingRepositoryFeil.FACTORY.fantIkkeBehandlingVedtak(behandling.getId()).toException());
        return new BehandlingInfoES(fagsak, behandling, behVedtak, personIdent);
    }

}
