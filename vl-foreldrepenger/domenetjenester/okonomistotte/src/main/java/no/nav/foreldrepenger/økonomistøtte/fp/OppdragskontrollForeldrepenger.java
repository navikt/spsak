package no.nav.foreldrepenger.økonomistøtte.fp;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.impl.FinnAnsvarligSaksbehandler;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFeriepenger;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFeriepengerPrÅr;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryFeil;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Grad170;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Refusjonsinfo156;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TfradragTillegg;
import no.nav.foreldrepenger.økonomistøtte.OppdragskontrollManager;
import no.nav.foreldrepenger.økonomistøtte.Oppdragsmottaker;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeAksjon;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndring;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndringLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKlassifik;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeStatusLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiTypeSats;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiUtbetFrekvens;
import no.nav.foreldrepenger.økonomistøtte.ØkonomioppdragRepository;
import no.nav.vedtak.util.FPDateUtil;

public abstract class OppdragskontrollForeldrepenger extends OppdragskontrollManager {

    static final long INITIAL_LØPENUMMER = 99L;
    private static final int INITIAL_TELLER = 100;
    private static final int TELLER_I_ANDRE_ITER = 101;
    private static final int INITIAL_COUNT = 0;

    private TpsTjeneste tpsTjeneste;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private ØkonomioppdragRepository økonomioppdragRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private UttakRepository uttakRepository;

    private static final String KODE_ENDRING_NY = ØkonomiKodeEndring.NY.name();
    private static final String KODE_ENDRING_UENDRET = ØkonomiKodeEndring.UEND.name();
    private static final String KODE_ENDRING_LINJE_NY = ØkonomiKodeEndringLinje.NY.name();
    private static final String KODE_ENDRING_LINJE_ENDRING = ØkonomiKodeEndringLinje.ENDR.name();
    private static final String FRADRAG_TILLEGG = TfradragTillegg.T.name();
    private static final String TYPE_SATS_FP = ØkonomiTypeSats.DAG.name();
    private static final String TYPE_SATS_FERIEPENGER = ØkonomiTypeSats.ENG.name();
    private static final String TYPE_GRAD = "UFOR";
    private static final String BRUK_KJOREPLAN = "N";
    private static final String KODE_STATUS_LINJE_OPPHØR = ØkonomiKodeStatusLinje.OPPH.name();

    protected OppdragskontrollForeldrepenger() {
        // for CDI
    }

    @Inject
    public OppdragskontrollForeldrepenger(BehandlingRepositoryProvider repositoryProvider, TpsTjeneste tpsTjeneste,
                                          ØkonomioppdragRepository økonomioppdragRepository) {
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.tpsTjeneste = tpsTjeneste;
        this.beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
        this.økonomioppdragRepository = økonomioppdragRepository;
        this.uttakRepository = repositoryProvider.getUttakRepository();
    }

    BehandlingInfoFP oppsettBehandlingInfo(Behandling behandling) {
        Fagsak fagsak = behandling.getFagsak();
        PersonIdent personIdent = tpsTjeneste.hentFnrForAktør(behandling.getAktørId()); // kallet kan fjernes en gang i fremtiden, når Oppdragssystemet ikke lenger krever fnr i sine meldinger.
        BehandlingVedtak behandlingVedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())
            .orElseThrow(() -> BehandlingRepositoryFeil.FACTORY.fantIkkeBehandlingVedtak(behandling.getId()).toException());
        BeregningsresultatFP beregningsresultatFP = null;
        if (erOpphørEtterSkjæringstidspunktEllerIkkeOpphør(behandling)) {
            beregningsresultatFP = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling)
                .orElseThrow(() -> new IllegalStateException("Mangler Beregningsresultat for behandling " + behandling.getId()));
        }
        String ansvarligSaksbehandler = FinnAnsvarligSaksbehandler.finn(behandling);
        return new BehandlingInfoFP(fagsak, behandling, behandlingVedtak, personIdent, beregningsresultatFP, ansvarligSaksbehandler);
    }

    Oppdrag110 opprettNyOppdrag110(BehandlingInfoFP behandlingInfo, Oppdragskontroll oppdragskontroll,
                                   long fagsystemId, boolean brukerErMottaker) {
        Avstemming115 avstemming115FP = opprettAvstemming115();
        Oppdrag110 oppdrag110 = opprettOppdrag110FP(behandlingInfo, oppdragskontroll, avstemming115FP, brukerErMottaker, true, fagsystemId);
        opprettOppdragsenhet120(oppdrag110);
        return oppdrag110;
    }

    Oppdrag110 opprettOppdrag110FP(BehandlingInfoFP behandlingInfo, Oppdragskontroll oppdragskontroll, Avstemming115 avstemming115, boolean brukerErMottaker, boolean erNyMottakerIEndring, long fagsystemId) {
        String kodeEndring;
        if (gjelderOpphør(behandlingInfo.getBehandling())) {
            boolean erOpphørEtterStp = erOpphørEtterSkjæringstidspunkt(behandlingInfo.getBehandling());
            kodeEndring = erOpphørEtterStp && erNyMottakerIEndring ? KODE_ENDRING_NY : KODE_ENDRING_UENDRET;
        } else {
            kodeEndring = erNyMottakerIEndring ? KODE_ENDRING_NY : KODE_ENDRING_UENDRET;
        }
        return Oppdrag110.builder()
            .medKodeAksjon(ØkonomiKodeAksjon.EN.getKodeAksjon())
            .medKodeEndring(kodeEndring)
            .medKodeFagomrade(brukerErMottaker ? ØkonomiKodeFagområde.FP.name() : ØkonomiKodeFagområde.FPREF.name())
            .medFagSystemId(fagsystemId)
            .medUtbetFrekvens(ØkonomiUtbetFrekvens.MÅNED.getUtbetFrekvens())
            .medOppdragGjelderId(behandlingInfo.getPersonIdent().getIdent())
            .medDatoOppdragGjelderFom(LocalDate.of(2000, 1, 1))
            .medSaksbehId(behandlingInfo.getAnsvarligSaksbehandler())
            .medOppdragskontroll(oppdragskontroll)
            .medAvstemming115(avstemming115)
            .build();
    }

    List<Oppdragslinje150> opprettOppdragslinje150FP(BehandlingInfoFP behandlingInfo, Oppdrag110 oppdrag110,
                                                     List<BeregningsresultatAndel> andelListe, Oppdragsmottaker mottaker) {
        List<String> klassekodeListe = getKlassekodeListe(andelListe);
        if (mottaker.erBruker() && klassekodeListe.size() > 1) {
            List<List<BeregningsresultatAndel>> andelerGruppertMedKlassekode = gruppereAndelerMedKlassekode(andelListe);
            return opprettOppdr150ForBrukerMedFlereKlassekode(behandlingInfo, oppdrag110,
                andelerGruppertMedKlassekode, mottaker, Collections.emptyList());
        }
        return opprettOppdragslinje150FP(behandlingInfo, oppdrag110, andelListe, mottaker, null);
    }

    List<Oppdragslinje150> opprettOppdr150ForBrukerMedFlereKlassekode(BehandlingInfoFP behandlingInfo,
                                                                      Oppdrag110 oppdrag110,
                                                                      List<List<BeregningsresultatAndel>> andelerGruppertMedKlassekode,
                                                                      Oppdragsmottaker mottaker, List<Oppdragslinje150> tidligereOppdr150Liste) {
        List<Oppdragslinje150> oppdrlinje150Liste = new ArrayList<>();
        int teller = INITIAL_TELLER;
        int count = INITIAL_COUNT;
        for (List<BeregningsresultatAndel> andelListe : andelerGruppertMedKlassekode) {
            List<Long> delYtelseIdListe = new ArrayList<>();
            for (BeregningsresultatAndel andel : andelListe) {
                Oppdragslinje150 oppdragslinje150 = opprettOppdragslinje150FørsteOppdragFP(behandlingInfo, oppdrag110,
                    mottaker, andel, delYtelseIdListe, tidligereOppdr150Liste, count, teller++);
                int grad = andel.getUtbetalingsgrad().setScale(0, RoundingMode.HALF_EVEN).intValue();
                opprettGrad170(oppdragslinje150, grad);
                oppdrlinje150Liste.add(oppdragslinje150);
            }
            count = count + andelListe.size();
        }
        long sisteSattDelYtelseId = oppdrlinje150Liste.get(oppdrlinje150Liste.size() - 1).getDelytelseId();
        if (tidligereOppdr150Liste.isEmpty() || erFeriepengerEndret(behandlingInfo, tidligereOppdr150Liste, mottaker)) {
            List<Oppdragslinje150> opp150FeriepengerList = lagOppdragslinje150ForFeriepenger(behandlingInfo,
                oppdrag110, mottaker, tidligereOppdr150Liste, sisteSattDelYtelseId);
            oppdrlinje150Liste.addAll(opp150FeriepengerList);
        }
        return oppdrlinje150Liste;
    }

    List<Oppdragslinje150> opprettOppdragslinje150FP(BehandlingInfoFP behandlingInfo, Oppdrag110 oppdrag110,
                                                     List<BeregningsresultatAndel> andelerListe, Oppdragsmottaker mottaker,
                                                     Oppdragslinje150 sisteOppdr150) {
        List<Oppdragslinje150> oppdrlinje150Liste = new ArrayList<>();
        List<Long> delYtelseIdListe = new ArrayList<>();

        int teller = INITIAL_TELLER;
        List<Oppdragslinje150> tidligereOppdr150Liste = sisteOppdr150 != null ? Collections.singletonList(sisteOppdr150) : Collections.emptyList();
        for (BeregningsresultatAndel andel : andelerListe) {
            Oppdragslinje150 oppdragslinje150 = opprettOppdragslinje150FørsteOppdragFP(behandlingInfo, oppdrag110, mottaker, andel,
                delYtelseIdListe, tidligereOppdr150Liste, teller++);
            int grad = andel.getUtbetalingsgrad().setScale(0, RoundingMode.HALF_EVEN).intValue();
            opprettGrad170(oppdragslinje150, grad);
            if (!mottaker.erBruker()) {
                LocalDate maksDato = finnMaksDato(behandlingInfo, mottaker);
                opprettRefusjonsinfo156(behandlingInfo, oppdragslinje150, mottaker, maksDato);
            }
            oppdrlinje150Liste.add(oppdragslinje150);
        }
        long sisteSattDelYtelseId = delYtelseIdListe.get(delYtelseIdListe.size() - 1);
        if (tidligereOppdr150Liste.isEmpty() || erFeriepengerEndret(behandlingInfo, tidligereOppdr150Liste, mottaker)) {
            List<Oppdragslinje150> opp150FeriepengerList = lagOppdragslinje150ForFeriepenger(behandlingInfo,
                oppdrag110, mottaker, tidligereOppdr150Liste, sisteSattDelYtelseId);
            kobleAndreMeldingselementerTilOpp150NyFeriepenger(behandlingInfo, opp150FeriepengerList, mottaker);
            oppdrlinje150Liste.addAll(opp150FeriepengerList);
        }
        return oppdrlinje150Liste;
    }

    List<BeregningsresultatAndel> hentAndeler(BeregningsresultatFP beregningsresultatFP) {
        List<BeregningsresultatPeriode> brPeriodeListe = beregningsresultatFP.getBeregningsresultatPerioder();
        List<BeregningsresultatAndel> andeler = brPeriodeListe.stream().sorted(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom))
            .map(BeregningsresultatPeriode::getBeregningsresultatAndelList).flatMap(List::stream).collect(Collectors.toList());

        return andeler.stream().filter(a -> a.getDagsats() > 0).collect(Collectors.toList());
    }

    void skilleAndelerMellomArbeidsgiverOgBruker(List<BeregningsresultatAndel> brukersAndelListe, List<BeregningsresultatAndel> arbeidsgiversAndelListe,
                                                 List<BeregningsresultatAndel> alleAndelersListe) {
        for (BeregningsresultatAndel andel : alleAndelersListe) {
            if (andel.erBrukerMottaker()) {
                brukersAndelListe.add(andel);
            } else {
                arbeidsgiversAndelListe.add(andel);
            }
        }
    }

    List<String> getKlassekodeListe(List<BeregningsresultatAndel> andelListe) {
        return andelListe.stream().map(BeregningsresultatAndel::getInntektskategori)
            .map(InntektskategoriKlassekodeMapper::inntektskategoriTilKlassekode)
            .distinct()
            .collect(Collectors.toList());
    }

    boolean finnesFlereKlassekodeIForrigeOppdrag(Oppdragskontroll forrigeOppdrag, boolean erBrukerMottaker) {
        List<Oppdragslinje150> tidligereOpp150Liste = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, erBrukerMottaker, false);
        return tidligereOpp150Liste.stream().map(Oppdragslinje150::getKodeKlassifik).distinct().count() > 1L;
    }

    List<List<BeregningsresultatAndel>> gruppereAndelerMedKlassekode(List<BeregningsresultatAndel> andelListe) {
        Map<String, List<BeregningsresultatAndel>> andelPrKlassekodeMap = new LinkedHashMap<>();
        List<String> klassekodeListe = getKlassekodeListe(andelListe);
        for (String klassekode : klassekodeListe) {
            List<BeregningsresultatAndel> andelerMedSammeKlassekode = andelListe.stream()
                .filter(andel -> InntektskategoriKlassekodeMapper.inntektskategoriTilKlassekode(andel.getInntektskategori()).equals(klassekode))
                .sorted(Comparator.comparing(andel -> andel.getBeregningsresultatPeriode().getBeregningsresultatPeriodeFom()))
                .collect(Collectors.toList());
            andelPrKlassekodeMap.put(klassekode, andelerMedSammeKlassekode);
        }
        return new ArrayList<>(andelPrKlassekodeMap.values());
    }

    List<Oppdragslinje150> hentTidligereOppdragslinje150(Fagsak fagsak, Oppdrag110 forrigeOppdrag110) {
        List<Oppdragskontroll> alleTidligereOppdrag = finnAlleOppdragForSak(fagsak);
        return alleTidligereOppdrag.stream()
            .flatMap(oppdragskontroll -> oppdragskontroll.getOppdrag110Liste().stream())
            .filter(oppdrag110 -> oppdrag110.getFagsystemId() == forrigeOppdrag110.getFagsystemId())
            .flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream()).collect(Collectors.toList());
    }

    void settFellesFelterIOppdr150(BehandlingInfoFP behandlingInfoFP, Oppdragslinje150.Builder oppdr150Builder, boolean gjelderOpphør, boolean gjelderFeriepenger) {
        LocalDate vedtaksdato = behandlingInfoFP.getBehandlingVedtak()
            .map(BehandlingVedtak::getVedtaksdato)
            .orElse(LocalDate.now(FPDateUtil.getOffset()));
        String kodeEndringLinje = gjelderOpphør ? KODE_ENDRING_LINJE_ENDRING : KODE_ENDRING_LINJE_NY;
        String typeSats = gjelderFeriepenger ? TYPE_SATS_FERIEPENGER : TYPE_SATS_FP;
        if (gjelderOpphør) {
            oppdr150Builder.medKodeStatusLinje(KODE_STATUS_LINJE_OPPHØR);
        }
        oppdr150Builder.medKodeEndringLinje(kodeEndringLinje)
            .medVedtakId(vedtaksdato.toString())
            .medFradragTillegg(FRADRAG_TILLEGG)
            .medBrukKjoreplan(BRUK_KJOREPLAN)
            .medSaksbehId(behandlingInfoFP.getAnsvarligSaksbehandler())
            .medHenvisning(behandlingInfoFP.getBehandling().getId())
            .medTypeSats(typeSats);
    }

    void opprettGrad170(Oppdragslinje150 oppdragslinje150, int grad) {
        Grad170.builder()
            .medTypeGrad(TYPE_GRAD)
            .medGrad(grad)
            .medOppdragslinje150(oppdragslinje150)
            .build();
    }

    void opprettRefusjonsinfo156(BehandlingInfoFP behandlingInfo, Oppdragslinje150 nyOppdragslinje150, Refusjonsinfo156 forrigeRefusjonsinfo156) {
        Refusjonsinfo156.builder()
            .medDatoFom(behandlingInfo.getVedtaksdato())
            .medMaksDato(forrigeRefusjonsinfo156.getMaksDato())
            .medRefunderesId(forrigeRefusjonsinfo156.getRefunderesId())
            .medOppdragslinje150(nyOppdragslinje150)
            .build();
    }

    private void kobleAndreMeldingselementerTilOpp150NyFeriepenger(BehandlingInfoFP behandlingInfo,
                                                                   List<Oppdragslinje150> opp150FeriepengerList,
                                                                   Oppdragsmottaker mottaker) {
        for (Oppdragslinje150 opp150 : opp150FeriepengerList) {
            if (!mottaker.erBruker()) {
                LocalDate maksDato = finnMaksDato(behandlingInfo, mottaker);
                opprettRefusjonsinfo156(behandlingInfo, opp150, mottaker, maksDato);
            }
        }
    }

    private boolean erFeriepengerEndret(BehandlingInfoFP behandlingInfo,
                                        List<Oppdragslinje150> tidligereOppdr150Liste,
                                        Oppdragsmottaker mottaker) {
        List<Oppdragslinje150> tidligereOpp150FeriepengerListe = hentAlleTidligereOppdr150ForFeriepenger(behandlingInfo.getFagsak(), mottaker, tidligereOppdr150Liste);
        Optional<BeregningsresultatFeriepenger> brFeriepengerOpt = behandlingInfo.getBeregningsresultatFP()
            .flatMap(BeregningsresultatFP::getBeregningsresultatFeriepenger);
        if (brFeriepengerOpt.isPresent()) {
            if (!tidligereOpp150FeriepengerListe.isEmpty()) {
                List<BeregningsresultatFeriepengerPrÅr> brFeriepengerPrÅrList = opprettOpp150FeriepengerListe(mottaker, brFeriepengerOpt.get());
                List<Oppdragslinje150> opp150FeriepengerListe = finnOppdr150MedEndringIFeriepengerBeregning(tidligereOpp150FeriepengerListe, brFeriepengerPrÅrList);
                return !opp150FeriepengerListe.isEmpty();
            } else {
                List<BeregningsresultatFeriepengerPrÅr> brFeriepengerPrÅrRevurdListe = opprettOpp150FeriepengerListe(mottaker, brFeriepengerOpt.get());
                return !brFeriepengerPrÅrRevurdListe.isEmpty();
            }
        }
        return false;
    }

    private List<Oppdragslinje150> finnOppdr150MedEndringIFeriepengerBeregning(List<Oppdragslinje150> tidligereOpp150FeriepengerListe, List<BeregningsresultatFeriepengerPrÅr> brFeriepengerPrÅrList) {
        List<Oppdragslinje150> opp150FeriepengerListe = new ArrayList<>();
        if (tidligereOpp150FeriepengerListe.isEmpty()) {
            return opp150FeriepengerListe;
        }
        List<LocalDate> opptjeningsårListe = brFeriepengerPrÅrList.stream()
            .map(BeregningsresultatFeriepengerPrÅr::getOpptjeningsår)
            .distinct()
            .collect(Collectors.toList());
        for (LocalDate opptjeningsår : opptjeningsårListe) {
            int feriepengeår = opptjeningsår.getYear() + 1;
            long sumFraRevurderingBeh = brFeriepengerPrÅrList.stream()
                .filter(brFeriepengerPrÅr -> brFeriepengerPrÅr.getOpptjeningsår().equals(opptjeningsår))
                .mapToLong(b -> b.getÅrsbeløp().getVerdi().longValue())
                .sum();
            List<Oppdragslinje150> opp150MedMaxDelytelseIdListe = finnOpp150FeriepengerMedMaxDelytelseId(tidligereOpp150FeriepengerListe, feriepengeår);
            boolean erSisteOpp150Opphør = opp150MedMaxDelytelseIdListe.stream().anyMatch(Oppdragslinje150::gjelderOpphør);
            if (erSisteOpp150Opphør) {
                opp150MedMaxDelytelseIdListe.stream()
                    .filter(Oppdragslinje150::gjelderOpphør).findFirst()
                    .ifPresent(opp150FeriepengerListe::add);
                continue;
            }
            opp150MedMaxDelytelseIdListe.forEach(opp150 -> {
                if (opp150.getSats() != sumFraRevurderingBeh) {
                    opp150FeriepengerListe.add(opp150);
                }
            });
        }
        return opp150FeriepengerListe;
    }

    private List<Oppdragslinje150> finnOpp150FeriepengerMedMaxDelytelseId(List<Oppdragslinje150> tidligereOpp150FeriepengerListe, int feriepengeår) {
        NavigableMap<Long, List<Oppdragslinje150>> opp150PrDelytelseIdMap = tidligereOpp150FeriepengerListe.stream()
            .filter(oppdragslinje150 -> oppdragslinje150.getDatoVedtakFom().getYear() == feriepengeår)
            .sorted(Comparator.comparing(Oppdragslinje150::getDelytelseId))
            .collect(Collectors.groupingBy(Oppdragslinje150::getDelytelseId, TreeMap::new, Collectors.toList()));
        return opp150PrDelytelseIdMap.isEmpty() ? Collections.emptyList() : opp150PrDelytelseIdMap.lastEntry().getValue();
    }

    private List<Oppdragslinje150> hentAlleTidligereOppdr150ForFeriepenger(Fagsak fagsak, Oppdragsmottaker mottaker, List<Oppdragslinje150> tidligereOppdr150Liste) {
        if (tidligereOppdr150Liste.isEmpty()) {
            return Collections.emptyList();
        }
        Oppdragslinje150 sisteOppdr150 = tidligereOppdr150Liste.get(0);
        Oppdrag110 sisteOppdr110 = sisteOppdr150.getOppdrag110();
        List<Oppdragslinje150> alleTidligereOppdr150 = hentTidligereOppdragslinje150(fagsak, sisteOppdr110);
        String kodeKlassifik = mottaker.erBruker() ? ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik() : ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik();
        return Oppdragslinje150Verktøy.hentOppdr150ForFeriepengerMedKlassekode(alleTidligereOppdr150, kodeKlassifik);
    }

    private List<Oppdragslinje150> opprettOppdragslinje150ForFeriepenger(BehandlingInfoFP behandlingInfo,
                                                                         List<Oppdragslinje150> tidligereOpp150FeriepengerListe,
                                                                         Oppdrag110 oppdrag110, Oppdragsmottaker mottaker,
                                                                         long sisteSattDelYtelseId) {
        List<Oppdragslinje150> opp150FeriepengerList = new ArrayList<>();
        Optional<BeregningsresultatFeriepenger> brFeriepengerOpt = behandlingInfo.getBeregningsresultatFP()
            .flatMap(BeregningsresultatFP::getBeregningsresultatFeriepenger);
        if (brFeriepengerOpt.isPresent()) {
            BeregningsresultatFeriepenger brFeriepenger = brFeriepengerOpt.get();
            List<BeregningsresultatFeriepengerPrÅr> brFeriepengerPrÅrList = opprettOpp150FeriepengerListe(mottaker, brFeriepenger);
            List<LocalDate> opptjeningsDatoList = brFeriepengerPrÅrList.stream().map(BeregningsresultatFeriepengerPrÅr::getOpptjeningsår).distinct().collect(Collectors.toList());

            List<Oppdragslinje150> endretOpp150FeriepengerListe = finnOppdr150MedEndringIFeriepengerBeregning(tidligereOpp150FeriepengerListe, brFeriepengerPrÅrList);
            long nesteDelytelseId = sisteSattDelYtelseId + 1;
            for (LocalDate opptjeningsDato : opptjeningsDatoList) {
                int opptjeningsår = opptjeningsDato.getYear();
                if (skalOppdragslinje150ForGittÅretOpprettes(tidligereOpp150FeriepengerListe, endretOpp150FeriepengerListe, opptjeningsår)) {
                    LocalDate vedtakFom = LocalDate.of(opptjeningsår + 1, 5, 1);
                    LocalDate vedtakTom = LocalDate.of(opptjeningsår + 1, 5, 31);
                    String kodeKlassifik = mottaker.erBruker() ? ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik() : ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik();
                    long sats = beregnSats(brFeriepengerPrÅrList, opptjeningsDato);

                    Oppdragslinje150.Builder oppdragslinje150Builder = Oppdragslinje150.builder();
                    settFellesFelterIOppdr150(behandlingInfo, oppdragslinje150Builder, false, true);
                    oppdragslinje150Builder.medKodeKlassifik(kodeKlassifik)
                        .medOppdrag110(oppdrag110)
                        .medVedtakFomOgTom(vedtakFom, vedtakTom)
                        .medSats(sats);

                    oppdragslinje150Builder.medDelytelseId(nesteDelytelseId);
                    if (!tidligereOpp150FeriepengerListe.isEmpty()) {
                        long fagsystemId = tidligereOpp150FeriepengerListe.get(0).getOppdrag110().getFagsystemId();
                        int feriepengeår = opptjeningsår + 1;
                        endretOpp150FeriepengerListe.stream().filter(opp150 -> opp150.getDatoVedtakFom().getYear() == feriepengeår)
                            .findFirst().ifPresent(oppdragslinje150 -> {
                            if (!oppdragslinje150.gjelderOpphør()) {
                                oppdragslinje150Builder.medRefDelytelseId(oppdragslinje150.getDelytelseId());
                                oppdragslinje150Builder.medRefFagsystemId(fagsystemId);
                            }
                        });
                    }
                    if (mottaker.erBruker()) {
                        oppdragslinje150Builder.medUtbetalesTilId(endreTilElleveSiffer(mottaker.getId()));
                    }
                    nesteDelytelseId++;
                    opp150FeriepengerList.add(oppdragslinje150Builder.build());
                }
            }
            return opp150FeriepengerList;
        }
        return opp150FeriepengerList;
    }

    private boolean skalOppdragslinje150ForGittÅretOpprettes(List<Oppdragslinje150> tidligereOpp150FeriepengerListe, List<Oppdragslinje150> endretOpp150FeriepengerListe, int opptjeningsår) {
        int feriepengeår = opptjeningsår + 1;
        if (tidligereOpp150FeriepengerListe.isEmpty()) {
            return true;
        }
        boolean erBeregningForGittÅretEndret = endretOpp150FeriepengerListe.stream().anyMatch(oppdr150 -> oppdr150.getDatoVedtakFom().getYear() == feriepengeår);
        boolean erBeregningForGittÅretNytt = tidligereOpp150FeriepengerListe.stream().noneMatch(oppdr150 -> oppdr150.getDatoVedtakFom().getYear() == feriepengeår);
        return erBeregningForGittÅretEndret || erBeregningForGittÅretNytt;
    }

    private long beregnSats(List<BeregningsresultatFeriepengerPrÅr> brFeriepengerPrÅrList, LocalDate opptjeningsDato) {
        return brFeriepengerPrÅrList.stream().filter(brFeriepengerPrÅr -> brFeriepengerPrÅr.getOpptjeningsår().equals(opptjeningsDato))
            .mapToLong(b -> b.getÅrsbeløp().getVerdi().longValue()).sum();
    }

    private List<BeregningsresultatFeriepengerPrÅr> opprettOpp150FeriepengerListe(Oppdragsmottaker mottaker, BeregningsresultatFeriepenger brFeriepenger) {
        List<BeregningsresultatFeriepengerPrÅr> brFeriepengerPrÅrList;
        if (mottaker.erBruker()) {
            brFeriepengerPrÅrList = brFeriepenger.getBeregningsresultatFeriepengerPrÅrListe().stream()
                .filter(brf -> brf.getBeregningsresultatAndel().erBrukerMottaker()).collect(Collectors.toList());
        } else {
            brFeriepengerPrÅrList = brFeriepenger.getBeregningsresultatFeriepengerPrÅrListe().stream().filter(brf -> !brf.getBeregningsresultatAndel().erBrukerMottaker()
                && brf.getBeregningsresultatAndel().getArbeidsforholdOrgnr().equals(mottaker.getOrgnr())).collect(Collectors.toList());
        }
        return brFeriepengerPrÅrList;
    }


    private LocalDate finnMaksDato(BehandlingInfoFP behandlingInfo, Oppdragsmottaker mottaker) {
        List<BeregningsresultatPeriode> brPeriodeListe = behandlingInfo.getBeregningsresultatFP()
            .map(BeregningsresultatFP::getBeregningsresultatPerioder).orElse(Collections.emptyList());
        return brPeriodeListe.stream().flatMap(brPeriode -> brPeriode.getBeregningsresultatAndelList().stream())
            .filter(andel -> !andel.erBrukerMottaker())
            .filter(andel -> andel.getArbeidsforholdOrgnr().equals(mottaker.getId()))
            .filter(andel -> andel.getDagsats() > 0)
            .map(andel -> andel.getBeregningsresultatPeriode().getBeregningsresultatPeriodeTom()).max(Comparator.comparing(Function.identity()))
            .orElseThrow(() -> new IllegalArgumentException("Det finnes ikke en uttaksdato"));
    }


    private void opprettRefusjonsinfo156(BehandlingInfoFP behandlingInfo, Oppdragslinje150 oppdragslinje150, Oppdragsmottaker mottaker, LocalDate maksDato) {
        Refusjonsinfo156.builder()
            .medMaksDato(maksDato)
            .medDatoFom(behandlingInfo.getVedtaksdato())
            .medRefunderesId(endreTilElleveSiffer(mottaker.getId()))
            .medOppdragslinje150(oppdragslinje150)
            .build();
    }

    private Oppdragslinje150 opprettOppdragslinje150FørsteOppdragFP(BehandlingInfoFP behandlingInfo, Oppdrag110 oppdrag110,
                                                                    Oppdragsmottaker mottaker, BeregningsresultatAndel andel,
                                                                    List<Long> delYtelseIdListe,
                                                                    List<Oppdragslinje150> tidligereOppdr150Liste,
                                                                    int teller) {
        return opprettOppdragslinje150FørsteOppdragFP(behandlingInfo, oppdrag110, mottaker, andel, delYtelseIdListe, tidligereOppdr150Liste, INITIAL_COUNT, teller);
    }

    private Oppdragslinje150 opprettOppdragslinje150FørsteOppdragFP(BehandlingInfoFP behandlingInfo, Oppdrag110 oppdrag110,
                                                                    Oppdragsmottaker mottaker, BeregningsresultatAndel andel,
                                                                    List<Long> delYtelseIdListe, List<Oppdragslinje150> tidligereOppdr150Liste,
                                                                    int count, int teller) {
        Oppdragslinje150.Builder oppdragslinje150Builder = opprettOppdragslinje150Builder(behandlingInfo, oppdrag110, mottaker, andel);
        if (tidligereOppdr150Liste.isEmpty()) {
            settRefDelytelseOgFagsystemId(oppdrag110, delYtelseIdListe, count, teller, oppdragslinje150Builder);
        } else {
            if (mottaker.erBruker()) {
                String opphørtKodeklassifik = InntektskategoriKlassekodeMapper.inntektskategoriTilKlassekode(andel.getInntektskategori());
                Optional<Oppdragslinje150> sisteOppdr150Opt = tidligereOppdr150Liste.stream().filter(oppdr150 -> oppdr150.getKodeKlassifik().equals(opphørtKodeklassifik)).findFirst();
                if (sisteOppdr150Opt.isPresent()) {
                    Oppdragslinje150 sisteOppdr150 = sisteOppdr150Opt.get();
                    settRefDelytelseOgFagsystemId(oppdrag110, delYtelseIdListe, count, teller, oppdragslinje150Builder, sisteOppdr150, false);
                } else {
                    Oppdragslinje150 sisteOppdr150 = tidligereOppdr150Liste.get(0);
                    settRefDelytelseOgFagsystemId(oppdrag110, delYtelseIdListe, count, teller, oppdragslinje150Builder, sisteOppdr150, true);
                }
            } else {
                Oppdragslinje150 sisteOppdr150 = tidligereOppdr150Liste.get(0);
                settRefDelytelseOgFagsystemId(oppdrag110, delYtelseIdListe, count, teller, oppdragslinje150Builder, sisteOppdr150, false);
            }
        }
        if (mottaker.erBruker()) {
            oppdragslinje150Builder.medUtbetalesTilId(endreTilElleveSiffer(mottaker.getId()));
        }
        return oppdragslinje150Builder.build();
    }

    private Oppdragslinje150.Builder opprettOppdragslinje150Builder(BehandlingInfoFP behandlingInfo, Oppdrag110 oppdrag110, Oppdragsmottaker mottaker, BeregningsresultatAndel andel) {
        LocalDate vedtakFom = andel.getBeregningsresultatPeriode().getBeregningsresultatPeriodeFom();
        LocalDate vedtakTom = andel.getBeregningsresultatPeriode().getBeregningsresultatPeriodeTom();

        String kodeKlassifik = mottaker.erBruker() ? InntektskategoriKlassekodeMapper.inntektskategoriTilKlassekode(andel.getInntektskategori()) : ØkonomiKodeKlassifik.FPREFAG_IOP.getKodeKlassifik();
        int dagsats = andel.getDagsats();

        Oppdragslinje150.Builder oppdragslinje150Builder = Oppdragslinje150.builder();
        settFellesFelterIOppdr150(behandlingInfo, oppdragslinje150Builder,false, false);
        oppdragslinje150Builder.medKodeKlassifik(kodeKlassifik)
            .medOppdrag110(oppdrag110)
            .medVedtakFomOgTom(vedtakFom, vedtakTom)
            .medSats(dagsats);
        return oppdragslinje150Builder;
    }

    private void settRefDelytelseOgFagsystemId(Oppdrag110 oppdrag110, List<Long> delYtelseIdListe, int count, int teller, Oppdragslinje150.Builder oppdragslinje150Builder,
                                               Oppdragslinje150 sisteOppdr150, boolean erDetNyKlassekodeINyOppdrag) {
        int jx = teller - (INITIAL_TELLER + count);
        long fagsystemId = oppdrag110.getFagsystemId();
        if (jx == 0) {
            if (!erDetNyKlassekodeINyOppdrag) {
                oppdragslinje150Builder.medRefFagsystemId(fagsystemId);
            }
        } else {
            oppdragslinje150Builder.medRefFagsystemId(fagsystemId);
        }
        long delytelseId = settRefDelYtelseId(oppdrag110, oppdragslinje150Builder, sisteOppdr150, jx, erDetNyKlassekodeINyOppdrag);
        delYtelseIdListe.add(delytelseId);
        if (jx > 0) {
            oppdragslinje150Builder.medRefDelytelseId(delYtelseIdListe.get(jx - 1));
        }
    }

    private void settRefDelytelseOgFagsystemId(Oppdrag110 oppdrag110, List<Long> delYtelseIdListe, int count, int teller, Oppdragslinje150.Builder oppdragslinje150Builder) {
        long fagsystemId = oppdrag110.getFagsystemId();
        long delytelseId = concatenateValues(fagsystemId, teller);
        delYtelseIdListe.add(delytelseId);
        oppdragslinje150Builder.medDelytelseId(delytelseId);
        if (teller > INITIAL_TELLER + count) {
            int ix = teller - (TELLER_I_ANDRE_ITER + count);
            oppdragslinje150Builder.medRefFagsystemId(fagsystemId);
            oppdragslinje150Builder.medRefDelytelseId(delYtelseIdListe.get(ix));
        }
    }

    private long settRefDelYtelseId(Oppdrag110 oppdrag110, Oppdragslinje150.Builder oppdragslinje150Builder, Oppdragslinje150 sisteOppdr150, int antallIterasjoner, boolean erDetNyKlassekodeINyOppdrag) {
        long sisteDelytelseIdINyOpp10 = Oppdragslinje150Verktøy.finnMaxDelytelseIdForOpp110(oppdrag110, sisteOppdr150);
        long sisteDelytelseIdIForrigeOpp10 = sisteOppdr150.getOppdrag110().getOppdragslinje150Liste().stream()
            .map(Oppdragslinje150::getDelytelseId)
            .max(Comparator.comparing(Function.identity()))
            .orElse(sisteDelytelseIdINyOpp10);
        long delytelseId = sisteDelytelseIdINyOpp10 > sisteDelytelseIdIForrigeOpp10 ? sisteDelytelseIdINyOpp10 + 1L : sisteDelytelseIdIForrigeOpp10 + 1L + antallIterasjoner;
        oppdragslinje150Builder.medDelytelseId(delytelseId);
        if (antallIterasjoner == 0 && !erDetNyKlassekodeINyOppdrag) {
            oppdragslinje150Builder.medRefDelytelseId(sisteOppdr150.getDelytelseId());
        }
        return delytelseId;
    }

    private List<Oppdragslinje150> lagOppdragslinje150ForFeriepenger(BehandlingInfoFP behandlingInfo,
                                                                     Oppdrag110 oppdrag110, Oppdragsmottaker mottaker,
                                                                     List<Oppdragslinje150> tidligereOppdr150Liste,
                                                                     long sisteSattDelYtelseId) {
        List<Oppdragslinje150> tidligereOpp150FeriepengerListe = hentAlleTidligereOppdr150ForFeriepenger(behandlingInfo.getFagsak(), mottaker, tidligereOppdr150Liste);
        return opprettOppdragslinje150ForFeriepenger(behandlingInfo, tidligereOpp150FeriepengerListe, oppdrag110, mottaker, sisteSattDelYtelseId);
    }

    private List<Oppdragskontroll> finnAlleOppdragForSak(Fagsak fagsak) {
        return økonomioppdragRepository.finnAlleOppdragForSak(fagsak.getSaksnummer());
    }

    private boolean gjelderOpphør(Behandling behandling) {
        return BehandlingResultatType.OPPHØR.equals(behandling.getBehandlingsresultat().getBehandlingResultatType());
    }

    private boolean erOpphørEtterSkjæringstidspunktEllerIkkeOpphør(Behandling behandling) {
        return !gjelderOpphør(behandling) || erOpphørEtterSkjæringstidspunkt(behandling);
    }

    private boolean erOpphørEtterSkjæringstidspunkt(Behandling behandling) {
        Optional<UttakResultatEntitet> uttak = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        return uttak.map(uttakResultatEntitet -> uttakResultatEntitet.getGjeldendePerioder().getPerioder().stream()
            .anyMatch(periode -> PeriodeResultatType.INNVILGET.equals(periode.getPeriodeResultatType()))).orElse(false);
    }

}
