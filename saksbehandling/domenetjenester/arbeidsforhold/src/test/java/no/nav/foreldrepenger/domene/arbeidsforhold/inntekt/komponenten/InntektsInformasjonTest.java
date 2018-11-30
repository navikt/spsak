package no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;

public class InntektsInformasjonTest {

    @Test
    public void skal_summere_per_arbeidsgiver_per_måned() {
        final String arbeidsgiver1 = "123123123";
        final String arbeidsgiver2 = "321321321";
        final Månedsinntekt månedsinntekt1 = opprettMånedsinntekt(arbeidsgiver1, YearMonth.now(), BigDecimal.TEN);
        final Månedsinntekt månedsinntekt2 = opprettMånedsinntekt(arbeidsgiver1, YearMonth.now(), BigDecimal.TEN);
        final Månedsinntekt månedsinntekt3 = opprettMånedsinntekt(arbeidsgiver1, YearMonth.now(), BigDecimal.TEN);
        final Månedsinntekt månedsinntekt4 = opprettMånedsinntekt(arbeidsgiver2, YearMonth.now().minusMonths(1), BigDecimal.TEN);
        final Månedsinntekt månedsinntekt5 = opprettMånedsinntekt(arbeidsgiver2, YearMonth.now(), BigDecimal.TEN);
        final List<Månedsinntekt> list = Stream.of(månedsinntekt1, månedsinntekt2, månedsinntekt3, månedsinntekt4, månedsinntekt5)
            .collect(Collectors.toList());
        final InntektsInformasjon inntektsInformasjon = new InntektsInformasjon(list, Collections.emptyList(), InntektsKilde.INNTEKT_OPPTJENING);

        final Map<String, Map<YearMonth, BigDecimal>> gruppertPåArbeidsgiver = inntektsInformasjon.getMånedsinntekterGruppertPåArbeidsgiver();

        assertThat(gruppertPåArbeidsgiver).isNotEmpty();
        assertThat(gruppertPåArbeidsgiver.keySet()).hasSize(2);
        assertThat(gruppertPåArbeidsgiver.get(arbeidsgiver1).values()).hasSize(1);
        assertThat(gruppertPåArbeidsgiver.get(arbeidsgiver1).values()).containsExactly(BigDecimal.valueOf(30));
        assertThat(gruppertPåArbeidsgiver.get(arbeidsgiver2).values()).hasSize(2);
        assertThat(gruppertPåArbeidsgiver.get(arbeidsgiver2).values()).containsExactly(BigDecimal.TEN, BigDecimal.TEN);
    }

    private Månedsinntekt opprettMånedsinntekt(String arbeidsgiver, YearMonth måned, BigDecimal beløp) {
        return new Månedsinntekt.Builder()
            .medArbeidsgiver(arbeidsgiver)
            .medMåned(måned)
            .medBeløp(beløp)
            .medYtelse(false)
            .build();
    }
}
