package no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdIdentifikator;

public class InntektsInformasjon {

    private List<Månedsinntekt> månedsinntekter;
    private List<FrilansArbeidsforhold> frilansArbeidsforhold;
    private Map<String, Map<YearMonth, BigDecimal>> inntektPerArbeidsgiver;
    private InntektsKilde kilde;

    public InntektsInformasjon(List<Månedsinntekt> månedsinntekter, List<FrilansArbeidsforhold> frilansArbeidsforhold, InntektsKilde kilde) {
        this.månedsinntekter = månedsinntekter;
        this.frilansArbeidsforhold = frilansArbeidsforhold;
        this.kilde = kilde;
    }

    public List<Månedsinntekt> getMånedsinntekter() {
        return Collections.unmodifiableList(månedsinntekter);
    }

    public Map<ArbeidsforholdIdentifikator, List<FrilansArbeidsforhold>> getFrilansArbeidsforhold() {
        return frilansArbeidsforhold.stream().collect(Collectors.groupingBy(FrilansArbeidsforhold::getIdentifikator));
    }

    public Map<String, Map<YearMonth, BigDecimal>> getMånedsinntekterGruppertPåArbeidsgiver() {
        if (inntektPerArbeidsgiver == null) {
            inntektPerArbeidsgiver = getMånedsinntekter().stream()
                .filter(it -> !it.isYtelse())
                .collect(Collectors.groupingBy(Månedsinntekt::getArbeidsgiver,
                    Collectors.groupingBy(Månedsinntekt::getMåned,
                        Collectors.reducing(BigDecimal.ZERO, Månedsinntekt::getBeløp, BigDecimal::add))));
        }
        return inntektPerArbeidsgiver;
    }

    public List<Månedsinntekt> getYtelsesTrygdEllerPensjonInntekt() {
        return månedsinntekter.stream().filter(Månedsinntekt::isYtelse).collect(Collectors.toList());
    }

    public InntektsKilde getKilde() {
        return kilde;
    }
}
