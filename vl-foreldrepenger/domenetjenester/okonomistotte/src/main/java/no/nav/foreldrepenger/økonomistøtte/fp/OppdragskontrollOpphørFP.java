package no.nav.foreldrepenger.økonomistøtte.fp;

import static no.nav.foreldrepenger.økonomistøtte.fp.Oppdragslinje150Verktøy.hentOppdr150ForFeriepengerMedKlassekode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFeriepenger;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFeriepengerPrÅr;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Refusjonsinfo156;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.økonomistøtte.Oppdragsmottaker;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKlassifik;
import no.nav.foreldrepenger.økonomistøtte.ØkonomioppdragRepository;

@ApplicationScoped
public class OppdragskontrollOpphørFP extends OppdragskontrollForeldrepenger {

    OppdragskontrollOpphørFP() {
        // For CDI
    }

    @Inject
    public OppdragskontrollOpphørFP(BehandlingRepositoryProvider repositoryProvider, TpsTjeneste tpsTjeneste,
                                    ØkonomioppdragRepository økonomioppdragRepository) {
        super(repositoryProvider, tpsTjeneste, økonomioppdragRepository);
    }

    @Override
    public void opprettØkonomiOppdrag(Behandling behandling, Optional<Oppdragskontroll> forrigeOppdragOpt, Oppdragskontroll oppdragskontroll) {
        BehandlingInfoFP behandlingInfo = oppsettBehandlingInfo(behandling);
        Oppdragskontroll forrigeOppdrag = forrigeOppdragOpt.orElseThrow(
            () -> new IllegalStateException("Det finnes ikke en oppdrag for fagsak: " + behandlingInfo.getBehandling().getFagsakId())
        );

        boolean erDetFlereKlassekodeForBruker = finnesFlereKlassekodeIForrigeOppdrag(forrigeOppdrag, true);

        if (erDetFlereKlassekodeForBruker) {
            opprettOpphørsoppdragForBrukerMedFlereKlassekode(behandlingInfo, oppdragskontroll, forrigeOppdrag);
        } else {
            opprettOpphørsoppdragForBruker(behandlingInfo, oppdragskontroll, forrigeOppdrag);
        }
        opprettOpphørsoppdragForArbeidsgiver(behandlingInfo, oppdragskontroll, forrigeOppdrag);
    }

    private Optional<Oppdrag110> opprettOpphørsoppdragForBrukerMedFlereKlassekode(BehandlingInfoFP behandlingInfo,
                                                                                  Oppdragskontroll nyOppdragskontroll,
                                                                                  Oppdragskontroll forrigeOppdrag) {
        return opprettOpphørsoppdragForBrukerMedFlereKlassekode(behandlingInfo, nyOppdragskontroll, forrigeOppdrag,
            null, false);
    }

    Optional<Oppdrag110> opprettOpphørsoppdragForBrukerMedFlereKlassekode(BehandlingInfoFP behandlingInfo,
                                                                          Oppdragskontroll nyOppdragskontroll,
                                                                          Oppdragskontroll forrigeOppdrag,
                                                                          LocalDate endringsdato,
                                                                          boolean opphFørEndringsoppdrFeriepg) {

        if (erEndringsdatoEtterSisteDatoAvAlleTidligereOppdrag(endringsdato, forrigeOppdrag)) {
            return Optional.empty();
        }
        List<Oppdragslinje150> tidligereOppdr150Liste = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, true, false);
        Avstemming115 avstemming115 = opprettAvstemming115();
        Oppdrag110 forrigeOppdrag110 = tidligereOppdr150Liste.get(0).getOppdrag110();
        Long fagsystemId = forrigeOppdrag110.getFagsystemId();
        Oppdrag110 nyOppdrag110 = opprettOppdrag110FP(behandlingInfo, nyOppdragskontroll, avstemming115, true,
            false, fagsystemId);
        opprettOppdragsenhet120(nyOppdrag110);

        List<Oppdragslinje150> opp150OpphList = new ArrayList<>();
        LocalDate opphørStatusFom;

        Map<String, List<Oppdragslinje150>> opp150MedSammeKlassekodeMap = tidligereOppdr150Liste.stream()
            .collect(Collectors.groupingBy(Oppdragslinje150::getKodeKlassifik));

        for (List<Oppdragslinje150> opp150MedSammeKlassekodeListe : opp150MedSammeKlassekodeMap.values()) {
            if (endringsdato == null) {
                opphørStatusFom = finnFørsteDatoVedtakFom(opp150MedSammeKlassekodeListe);
            } else {
                opphørStatusFom = finnOpphørFomDato(opp150MedSammeKlassekodeListe, endringsdato);
            }
            Optional<Oppdragslinje150> sisteOppdr150BrukerOpt = opp150MedSammeKlassekodeListe.stream()
                .filter(oppdragslinje150 -> oppdragslinje150.getKodeStatusLinje() == null)
                .max(Comparator.comparing(Oppdragslinje150::getDatoVedtakFom));
            if (sisteOppdr150BrukerOpt.isPresent()) {
                Oppdragslinje150 sisteOppdr150Bruker = sisteOppdr150BrukerOpt.get();
                LocalDate sisteDato = sisteOppdr150Bruker.getDatoVedtakTom();
                if (opphørStatusFom.isAfter(sisteDato)) {
                    continue;
                }
                opp150OpphList.add(opprettOppdragslinje150ForStatusOPPH(behandlingInfo, sisteOppdr150Bruker, nyOppdrag110, opphørStatusFom));
            }
        }
        opprettOppdr150LinjeForFeriepengerOPPH(behandlingInfo, opphFørEndringsoppdrFeriepg, opp150OpphList, nyOppdrag110, forrigeOppdrag110);
        opprettAttestant180(opp150OpphList, behandlingInfo.getAnsvarligSaksbehandler());
        return Optional.of(nyOppdrag110);
    }

    boolean erEndringsdatoEtterSisteDatoAvAlleTidligereOppdrag(LocalDate endringsdato, Oppdragskontroll forrigeOppdrag) {
        if (endringsdato != null) {
            List<Oppdragslinje150> tidligereOppdr150Liste = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, true, true);
            LocalDate sisteDato = tidligereOppdr150Liste.stream()
                .map(Oppdragslinje150::getDatoVedtakTom)
                .max(Comparator.comparing(Function.identity()))
                .orElseThrow(() -> new IllegalStateException("Utvikler feil: Mangler dato vedtak tom"));
            return endringsdato.isAfter(sisteDato);
        }
        return false;
    }

    private LocalDate finnFørsteDatoVedtakFom(List<Oppdragslinje150> opp150Liste) {
        return opp150Liste.stream()
            .map(Oppdragslinje150::getDatoVedtakFom)
            .min(Comparator.comparing(Function.identity()))
            .orElseThrow(() -> new IllegalStateException("Utvikler feil: Mangler vedtak fom dato"));
    }

    LocalDate finnOpphørFomDato(List<Oppdragslinje150> opp150Liste, LocalDate endringsdato) {
        LocalDate førsteDatoVedtakFom = finnFørsteDatoVedtakFom(opp150Liste);
        return førsteDatoVedtakFom.isAfter(endringsdato) ? førsteDatoVedtakFom : endringsdato;
    }

    private void opprettOpphørsoppdragForBruker(BehandlingInfoFP behandlingInfo, Oppdragskontroll oppdragskontroll, Oppdragskontroll forrigeOppdrag) {
        Optional<Oppdragslinje150> sisteOppdr150BrukerOpt = Oppdragslinje150Verktøy.finnSisteLinjeIKjedeForBruker(forrigeOppdrag);
        if (sisteOppdr150BrukerOpt.isPresent()) {
            Oppdragslinje150 sisteOppdr150Bruker = sisteOppdr150BrukerOpt.get();
            LocalDate opphørStatusFomForBruker = finnOpphørFomForBruker(forrigeOppdrag);
            opprettOppdragForOpphørBruker(behandlingInfo, oppdragskontroll, opphørStatusFomForBruker, sisteOppdr150Bruker);
        }
    }

    private void opprettOpphørsoppdragForArbeidsgiver(BehandlingInfoFP behandlingInfo, Oppdragskontroll oppdragskontroll, Oppdragskontroll forrigeOppdrag) {
        List<Oppdragslinje150> sisteOppdr150ArbeidsgivereListe = Oppdragslinje150Verktøy.finnSisteLinjeIKjedeForArbeidsgivere(forrigeOppdrag);
        for (Oppdragslinje150 sisteOppdr150 : sisteOppdr150ArbeidsgivereListe) {
            LocalDate opphørStatusFom = finnOpphørFomForArbeidsgiver(forrigeOppdrag, sisteOppdr150);
            opprettOppdragForOpphørArbeidsgiver(behandlingInfo, oppdragskontroll, opphørStatusFom, sisteOppdr150);
        }
    }

    void opprettOppdr150LinjeForFeriepengerOPPH(BehandlingInfoFP behandlingInfo, boolean opphFørEndringsoppdrFeriepg,
                                                List<Oppdragslinje150> opp150OpphList, Oppdrag110 nyOppdrag110,
                                                Oppdrag110 forrigeOppdrag110) {
        if (!opphFørEndringsoppdrFeriepg || skalFeriepengerOpphøres(behandlingInfo, forrigeOppdrag110, Optional.empty())) {
            List<Oppdragslinje150> opp150FeriepengerListe = opprettOppdr150LinjeForFeriepengerOPPH(behandlingInfo, nyOppdrag110,
                forrigeOppdrag110, true, opphFørEndringsoppdrFeriepg);
            opp150OpphList.addAll(opp150FeriepengerListe);
        }
    }

    boolean skalFeriepengerOpphøres(BehandlingInfoFP behandlingInfo, Oppdrag110 forrigeOppdrag110, Optional<Oppdragsmottaker> mottakerOpt) {
        Optional<BeregningsresultatFeriepenger> brFeriepengerOpt = behandlingInfo.getBeregningsresultatFP()
            .flatMap(BeregningsresultatFP::getBeregningsresultatFeriepenger);
        boolean erBrukerMottaker = !mottakerOpt.isPresent();
        List<Oppdragslinje150> gjeldendeFeriepengerFraFør = finnGjeldendeFeriepengerFraFør(behandlingInfo, forrigeOppdrag110, erBrukerMottaker);
        boolean erDetFeriepengerFraFør = !gjeldendeFeriepengerFraFør.isEmpty();
        if (brFeriepengerOpt.isPresent()) {
            BeregningsresultatFeriepenger brFeriepenger = brFeriepengerOpt.get();
            List<LocalDate> feriepengeårListe = gjeldendeFeriepengerFraFør.stream().map(Oppdragslinje150::getDatoVedtakFom).collect(Collectors.toList());
            for (LocalDate feriepengeår : feriepengeårListe) {
                int opptjeningsår = feriepengeår.getYear() - 1;
                boolean finnesFeriepengeårIRevurdering = sjekkOmDetFinnesFeriepengeårIRevurdering(mottakerOpt, brFeriepenger, opptjeningsår, erBrukerMottaker);
                if (finnesFeriepengeårIRevurdering) {
                    continue;
                }
                return true;
            }
            return false;
        }
        return erDetFeriepengerFraFør;
    }

    private boolean sjekkOmDetFinnesFeriepengeårIRevurdering(Optional<Oppdragsmottaker> mottakerOpt, BeregningsresultatFeriepenger brFeriepenger, int opptjeningsår, boolean erBrukerMottaker) {
        if (erBrukerMottaker) {
            return brFeriepenger.getBeregningsresultatFeriepengerPrÅrListe().stream()
                .filter(brFeriepengerPrÅr -> brFeriepengerPrÅr.getBeregningsresultatAndel().erBrukerMottaker())
                .anyMatch(brFeriepengerPrÅr -> brFeriepengerPrÅr.getOpptjeningsår().getYear() == opptjeningsår);
        } else {
            if (mottakerOpt.isPresent()) {
                Oppdragsmottaker mottaker = mottakerOpt.get();
                return brFeriepenger.getBeregningsresultatFeriepengerPrÅrListe().stream()
                    .filter(brFeriepengerPrÅr -> !brFeriepengerPrÅr.getBeregningsresultatAndel().erBrukerMottaker()
                        && brFeriepengerPrÅr.getBeregningsresultatAndel().getArbeidsforholdOrgnr().equals(mottaker.getOrgnr()))
                    .anyMatch(brFeriepengerPrÅr -> brFeriepengerPrÅr.getOpptjeningsår().getYear() == opptjeningsår);
            }
            return false;
        }
    }

    private List<Oppdragslinje150> finnGjeldendeFeriepengerFraFør(BehandlingInfoFP behandlingInfo,
                                                                  Oppdrag110 forrigeOppdrag110,
                                                                  boolean erBrukerMottaker) {
        List<Oppdragslinje150> tidligereOpp150List = hentTidligereOppdragslinje150(behandlingInfo.getFagsak(), forrigeOppdrag110);
        String kodeKlassifik = erBrukerMottaker ? ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik() : ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik();
        List<Oppdragslinje150> oppdr150FeriepengerListe = hentOppdr150ForFeriepengerMedKlassekode(tidligereOpp150List, kodeKlassifik);
        Map<Long, List<Oppdragslinje150>> opp150PerDelytelseId = oppdr150FeriepengerListe.stream()
            .collect(Collectors.groupingBy(Oppdragslinje150::getDelytelseId, TreeMap::new, Collectors.toList()));
        if (!opp150PerDelytelseId.isEmpty()) {
            opp150PerDelytelseId.entrySet().removeIf(entry -> entry.getValue().stream().anyMatch(Oppdragslinje150::gjelderOpphør));
            return opp150PerDelytelseId.values().stream().flatMap(List::stream).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Oppdrag110 opprettOppdragForOpphørBruker(BehandlingInfoFP behandlingInfo, Oppdragskontroll oppdragskontroll, LocalDate
        opphørStatusFom, Oppdragslinje150 sisteOppdr150Bruker) {
        return opprettOppdragForOpphørBruker(behandlingInfo, oppdragskontroll, opphørStatusFom, sisteOppdr150Bruker, false);
    }

    Oppdrag110 opprettOppdragForOpphørBruker(BehandlingInfoFP behandlingInfo, Oppdragskontroll nyOppdragskontroll, LocalDate
        opphørStatusFom, Oppdragslinje150 sisteOppdr150Bruker, boolean opphFørEndringsoppdrFeriepg) {

        List<Oppdragslinje150> opp150OpphList = new ArrayList<>();
        Avstemming115 avstemming115 = opprettAvstemming115();
        Oppdrag110 forrigeOppdrag110 = sisteOppdr150Bruker.getOppdrag110();
        Long fagsystemId = forrigeOppdrag110.getFagsystemId();
        Oppdrag110 nyOppdrag110 = opprettOppdrag110FP(behandlingInfo, nyOppdragskontroll, avstemming115, true, false, fagsystemId);
        opprettOppdragsenhet120(nyOppdrag110);
        opp150OpphList.add(opprettOppdragslinje150ForStatusOPPH(behandlingInfo, sisteOppdr150Bruker, nyOppdrag110, opphørStatusFom));
        opprettOppdr150LinjeForFeriepengerOPPH(behandlingInfo, opphFørEndringsoppdrFeriepg, opp150OpphList, nyOppdrag110, forrigeOppdrag110);
        opprettAttestant180(opp150OpphList, behandlingInfo.getAnsvarligSaksbehandler());

        return nyOppdrag110;
    }

    List<Oppdragslinje150> opprettOppdr150LinjeForFeriepengerOPPH(BehandlingInfoFP behandlingInfo, Oppdrag110 nyOppdrag110,
                                                                  Oppdrag110 forrigeOppdrag110, boolean erBrukerMottaker,
                                                                  boolean opphFørEndringsoppdrFeriepg) {
        List<Oppdragslinje150> opphørtOppdr150FeriepengerListe = new ArrayList<>();
        List<Oppdragslinje150> tidligereOpp150List = hentTidligereOppdragslinje150(behandlingInfo.getFagsak(), forrigeOppdrag110);
        String kodeKlassifik = erBrukerMottaker ? ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik() : ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik();
        List<Oppdragslinje150> tidligereOppdr150FeriepengerListe = hentOppdr150ForFeriepengerMedKlassekode(tidligereOpp150List, kodeKlassifik);
        boolean erFeriepengerBeregningNullForDenneOpp150År = true;
        for (Oppdragslinje150 oppdr150Feriepenger : tidligereOppdr150FeriepengerListe) {
            boolean finnesOpphørForDenneOpp150 = tidligereOppdr150FeriepengerListe.stream()
                .anyMatch(opp150 -> opp150.gjelderOpphør() && opp150.getDelytelseId().equals(oppdr150Feriepenger.getDelytelseId()));
            if (opphFørEndringsoppdrFeriepg) {
                erFeriepengerBeregningNullForDenneOpp150År = erFeriepengerBeregningNullForGittÅret(behandlingInfo, oppdr150Feriepenger, erBrukerMottaker);
            }
            if (!finnesOpphørForDenneOpp150 && erFeriepengerBeregningNullForDenneOpp150År) {
                LocalDate førsteUttaksDag = oppdr150Feriepenger.getDatoVedtakFom();
                Oppdragslinje150 opp150 = opprettOppdragslinje150ForStatusOPPH(behandlingInfo, oppdr150Feriepenger, nyOppdrag110, førsteUttaksDag, true);
                opphørtOppdr150FeriepengerListe.add(opp150);
            }
        }
        return opphørtOppdr150FeriepengerListe;
    }

    private boolean erFeriepengerBeregningNullForGittÅret(BehandlingInfoFP behandlingInfo, Oppdragslinje150 opp150Feriepenger,
                                                          boolean erBrukerMottaker) {
        Optional<BeregningsresultatFeriepenger> brFeriepengerOpt = behandlingInfo.getBeregningsresultatFP()
            .flatMap(BeregningsresultatFP::getBeregningsresultatFeriepenger);
        if (brFeriepengerOpt.isPresent()) {
            BeregningsresultatFeriepenger brFeriepenger = brFeriepengerOpt.get();
            LocalDate opptjeningsdato = opp150Feriepenger.getDatoVedtakFom().minusYears(1);
            List<BeregningsresultatFeriepengerPrÅr> brFeriepengerPrÅrListe = brFeriepenger.getBeregningsresultatFeriepengerPrÅrListe().stream()
                .filter(brFeriepengerPrÅr -> brFeriepengerPrÅr.getOpptjeningsår().getYear() == opptjeningsdato.getYear()).collect(Collectors.toList());
            if (erBrukerMottaker) {
                return brFeriepengerPrÅrListe.stream().noneMatch(brFeriepengerPrÅr -> brFeriepengerPrÅr.getBeregningsresultatAndel().erBrukerMottaker());
            }
            String orgnr = opp150Feriepenger.getRefusjonsinfo156().getRefunderesId();
            return brFeriepengerPrÅrListe.stream().noneMatch(brFeriepengerPrÅr -> !brFeriepengerPrÅr.getBeregningsresultatAndel().erBrukerMottaker()
                && orgnr.equals(endreTilElleveSiffer(brFeriepengerPrÅr.getBeregningsresultatAndel().getArbeidsforholdOrgnr())));
        }
        return true;
    }

    void kobleAndreMeldingselementerTilOpp150Opphør(BehandlingInfoFP behandlingInfo, Oppdragslinje150
        sisteOppdr150, List<Oppdragslinje150> opp150OpphList) {
        opp150OpphList.forEach(nyOppdragslinje150 -> {
            opprettAttestant180(nyOppdragslinje150, behandlingInfo.getAnsvarligSaksbehandler());
            Refusjonsinfo156 forrigeRefusjonsinfo156 = sisteOppdr150.getRefusjonsinfo156();
            opprettRefusjonsinfo156(behandlingInfo, nyOppdragslinje150, forrigeRefusjonsinfo156);
        });
    }

    private LocalDate finnOpphørFomForBruker(Oppdragskontroll forrigeOppdrag) {
        List<Oppdragslinje150> oppdragslinje150Liste = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, true, false);
        return oppdragslinje150Liste.stream()
            .map(Oppdragslinje150::getDatoVedtakFom)
            .min(Comparator.comparing(Function.identity()))
            .orElseThrow(() -> new IllegalStateException("Det finnes ikke dato vedtak fom"));
    }

    private LocalDate finnOpphørFomForArbeidsgiver(Oppdragskontroll forrigeOppdrag, Oppdragslinje150 sisteOppdr150) {
        List<Oppdragslinje150> oppdragslinje150Liste = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, false, false);
        String refunderesId = sisteOppdr150.getRefusjonsinfo156().getRefunderesId();
        return oppdragslinje150Liste.stream().filter(opp150 -> opp150.getRefusjonsinfo156().getRefunderesId().equals(refunderesId))
            .map(Oppdragslinje150::getDatoVedtakFom)
            .min(Comparator.comparing(Function.identity()))
            .orElseThrow(() -> new IllegalStateException("Det finnes ikke dato vedtak fom"));
    }

    private void opprettOppdragForOpphørArbeidsgiver(BehandlingInfoFP behandlingInfo, Oppdragskontroll nyOppdragskontroll, LocalDate
        opphørStatusFom, Oppdragslinje150 sisteOppdr150) {
        List<Oppdragslinje150> opp150OpphList = new ArrayList<>();
        Avstemming115 avstemming115 = opprettAvstemming115();
        Oppdrag110 forrigeOppdrag110 = sisteOppdr150.getOppdrag110();
        long fagsystemId = forrigeOppdrag110.getFagsystemId();
        Oppdrag110 nyOppdrag110 = opprettOppdrag110FP(behandlingInfo, nyOppdragskontroll, avstemming115, false, false, fagsystemId);
        opprettOppdragsenhet120(nyOppdrag110);
        opp150OpphList.add(opprettOppdragslinje150ForStatusOPPH(behandlingInfo, sisteOppdr150, nyOppdrag110, opphørStatusFom));
        List<Oppdragslinje150> opp150FeriepengerListe = opprettOppdr150LinjeForFeriepengerOPPH(behandlingInfo, nyOppdrag110, forrigeOppdrag110, false, false);
        opp150OpphList.addAll(opp150FeriepengerListe);
        kobleAndreMeldingselementerTilOpp150Opphør(behandlingInfo, sisteOppdr150, opp150OpphList);
    }

    Oppdragslinje150 opprettOppdragslinje150ForStatusOPPH(BehandlingInfoFP behandlingInfo, Oppdragslinje150 forrigeOppdr150, Oppdrag110
        oppdrag110, LocalDate datoStatusFom) {

        return opprettOppdragslinje150ForStatusOPPH(behandlingInfo, forrigeOppdr150, oppdrag110, datoStatusFom, false);
    }

    private Oppdragslinje150 opprettOppdragslinje150ForStatusOPPH(BehandlingInfoFP behandlingInfo, Oppdragslinje150 forrigeOppdr150, Oppdrag110
        oppdrag110, LocalDate datoStatusFom, boolean gjelderFeriepenger) {
        LocalDate vedtakFom = forrigeOppdr150.getDatoVedtakFom();
        LocalDate vedtakTom = forrigeOppdr150.getDatoVedtakTom();
        Long delytelseId = forrigeOppdr150.getDelytelseId();
        String kodeKlassifik = forrigeOppdr150.getKodeKlassifik();
        long dagsats = forrigeOppdr150.getSats();

        Oppdragslinje150.Builder oppdragslinje150Builder = Oppdragslinje150.builder();
        settFellesFelterIOppdr150(behandlingInfo, oppdragslinje150Builder, true, gjelderFeriepenger);
        Oppdragslinje150 oppdragslinje150 = oppdragslinje150Builder
            .medDatoStatusFom(datoStatusFom)
            .medDelytelseId(delytelseId)
            .medKodeKlassifik(kodeKlassifik)
            .medVedtakFomOgTom(vedtakFom, vedtakTom)
            .medSats(dagsats)
            .medUtbetalesTilId(forrigeOppdr150.getUtbetalesTilId())
            .medOppdrag110(oppdrag110)
            .build();

        if (!gjelderFeriepenger) {
            int grad = forrigeOppdr150.getGrad170Liste().get(0).getGrad();
            opprettGrad170(oppdragslinje150, grad);
        }

        return oppdragslinje150;
    }
}
