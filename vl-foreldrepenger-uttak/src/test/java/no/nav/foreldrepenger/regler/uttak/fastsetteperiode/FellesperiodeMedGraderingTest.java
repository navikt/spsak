package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodePropertyType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class FellesperiodeMedGraderingTest {

    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);
    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
    private LocalDate førsteLovligeUttaksdag = fødselsdato.minusMonths(3);

    @Test
    public void mor_graderer_med_50_prosent_arbeid_i_10_uker_med_5_uker_igjen_på_saldo() {
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.enGraderingsperiode(graderingFom, graderingTom, BigDecimal.valueOf(50))
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag)
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medGradertStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, graderingFom, graderingTom, Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(50), PeriodeVurderingType.PERIODE_OK, false, false)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 5 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.INNVILGET);
        assertThat(regelresultat.getProperty(FastsettePeriodePropertyType.KNEKKPUNKT, LocalDate.class)).isNull();
    }

    @Test
    public void mor_graderer_med_50_prosent_arbeid_i_10_uker_med_4_uker_igjen_på_saldo() {
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.enGraderingsperiode(graderingFom, graderingTom, BigDecimal.valueOf(50))
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag)
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medGradertStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, graderingFom, graderingTom, Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(50), PeriodeVurderingType.PERIODE_OK, false, false)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 4 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.INNVILGET);
    }


}
