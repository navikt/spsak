package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;

public class Inntektsgrunnlag {
    private int inntektRapporteringFristDag;
    private final List<Periodeinntekt> periodeinntekter = new ArrayList<>();

    public Inntektsgrunnlag() {
        // default constructor
    }

    public int getInntektRapporteringFristDag() {
        return inntektRapporteringFristDag;
    }

    public void setInntektRapporteringFristDag(int inntektRapporteringFristDag) {
        this.inntektRapporteringFristDag = inntektRapporteringFristDag;
    }

    public void leggTilPeriodeinntekt(Periodeinntekt periodeinntekt) {
        this.periodeinntekter.add(periodeinntekt);
    }

    public List<Periodeinntekt> getPeriodeinntekter() {
        return periodeinntekter;
    }

    public Optional<Periodeinntekt> getPeriodeinntekt(Inntektskilde inntektskilde, LocalDate dato) {
        return getPeriodeinntektMedKilde(inntektskilde)
            .filter(pi -> pi.inneholder(dato))
            .findFirst();
    }

    public Optional<LocalDate> sistePeriodeMedInntektFørDato(Inntektskilde inntektskilde, LocalDate dato) {
        Optional<Periodeinntekt> perioder = getPeriodeinntektMedKilde(inntektskilde)
            .filter(pi -> dato.isAfter(pi.getTom()))
            .max(Comparator.comparing(Periodeinntekt::getFom));
        return perioder.map(Periodeinntekt::getFom);
    }

    public Optional<Periodeinntekt> getSistePeriodeinntektMedTypeSøknad() {
        return getPeriodeinntektMedKilde(Inntektskilde.SØKNAD)
            .max(Comparator.comparing(Periodeinntekt::getFom));
    }

    public BigDecimal getInntektFraInntektsmelding(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        Optional<Periodeinntekt> periode = getPeriodeinntektInntektsmeldingForArbeidsgiver(arbeidsforhold.getArbeidsforhold())
            .findFirst();
        return periode.map(Periodeinntekt::getInntekt).orElse(BigDecimal.ZERO);
    }

    public Optional<BigDecimal> getÅrsinntekt(int år, Inntektskilde inntektskilde) {
        return getPeriodeinntektMedKilde(inntektskilde)
            .filter(pi -> pi.erFraår(år))
            .map(Periodeinntekt::getInntekt)
            .reduce(BigDecimal::add);
    }

    public boolean finnesInntektsdata(Inntektskilde inntektskilde, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        return getPeriodeinntektMedKilde(inntektskilde)
            .map(Periodeinntekt::getArbeidsgiver)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .anyMatch(arbeidsgiver -> arbeidsforhold.getArbeidsforhold().equals(arbeidsgiver));
    }

    public BigDecimal getSamletInntektISammenligningsperiode(Periode periode) {
        return getPeriodeinntektMedKilde(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING)
            .filter(pi-> periode.inneholder(pi.getFom()))
            .map(Periodeinntekt::getInntekt)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public List<BigDecimal> getPeriodeinntekter(Inntektskilde inntektskilde, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, LocalDate førDato, int måneder) {
        if (arbeidsforhold.erFrilanser()) {
            return getAlleFrilansinntekter(inntektskilde, førDato, måneder, arbeidsforhold.getArbeidsforhold());
        }
        List<BigDecimal> inntekter = new ArrayList<>();
        for (int måned = 0; måned < måneder; måned++) {
            final int siden = måned;
            Optional<Periodeinntekt> beløp = getPeriodeinntektMedKilde(inntektskilde)
                .filter(pi -> pi.getArbeidsgiver().isPresent())
                .filter(pi -> arbeidsforhold.getArbeidsforhold().equals(pi.getArbeidsgiver().get()))//NOSONAR
                .filter(pi -> pi.inneholder(førDato.minusMonths(siden)))
                .findFirst();
            beløp.ifPresent(månedsinntekt -> inntekter.add(månedsinntekt.getInntekt()));
        }
        return inntekter;
    }

    private List<BigDecimal> getAlleFrilansinntekter(Inntektskilde inntektskilde, LocalDate førDato, int måneder, Arbeidsforhold arbeidsforhold) {
        Periode periode = Periode.of(førDato.minusMonths(måneder), førDato);
        return getPeriodeinntektMedKilde(inntektskilde)
            .filter(pi -> pi.getArbeidsgiver().isPresent())
            .filter(pi -> pi.getArbeidsgiver().get().erFrilanser()) //NOSONAR
            .filter(pi -> pi.getArbeidsgiver().get().equals(arbeidsforhold)) //NOSONAR
            .filter(pi -> pi.erInnenforPeriode(periode))
            .map(Periodeinntekt::getInntekt)
            .collect(Collectors.toList());
    }

    private Stream<Periodeinntekt> getPeriodeinntektMedKilde(Inntektskilde inntektskilde) {
        return periodeinntekter.stream()
            .filter(pi -> inntektskilde.equals(pi.getInntektskilde()));
    }

    public Optional<BigDecimal> finnTotaltNaturalytelseBeløpMedOpphørsdatoIPeriodeForArbeidsforhold(Arbeidsforhold arbeidsforhold, LocalDate fom, LocalDate tom) {
        return getNaturalYtelserForArbeidsgiver(arbeidsforhold)
            .filter(ny -> ny.getTom() != null)
            .filter(ny -> !ny.getTom().isBefore(fom))
            .filter(ny -> !ny.getTom().isAfter(tom))
            .map(NaturalYtelse::getBeløp)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add);
    }

    public Optional<BigDecimal> finnTotaltNaturalytelseBeløpTilkommetIPeriodeForArbeidsforhold(Arbeidsforhold arbeidsforhold, LocalDate fom, LocalDate tom) {
        return getNaturalYtelserForArbeidsgiver(arbeidsforhold)
            .filter(ny -> ny.getFom() != null)
            .filter(ny -> !ny.getFom().isBefore(fom))
            .filter(ny -> !ny.getFom().isAfter(tom))
            .map(NaturalYtelse::getBeløp)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add);
    }

    private Stream<NaturalYtelse> getNaturalYtelserForArbeidsgiver(Arbeidsforhold arbeidsforhold) {
        return getPeriodeinntektInntektsmeldingForArbeidsgiver(arbeidsforhold)
            .flatMap(i -> i.getNaturalYtelser().stream());
    }

    private Stream<Periodeinntekt> getPeriodeinntektInntektsmeldingForArbeidsgiver(Arbeidsforhold arbeidsforhold) {
        return getPeriodeinntektMedKilde(Inntektskilde.INNTEKTSMELDING)
            .filter(i -> i.getArbeidsgiver().isPresent())
            .filter(i -> arbeidsforhold.equals(i.getArbeidsgiver().get())); //NOSONAR
    }
}
