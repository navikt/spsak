package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder.ARBEIDSFORHOLD_3;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedFerie;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;

public class FastsettePeriodeRegelOrkestreringUtbetalingsgradTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void tom_for_dager_skal_gi_null_utbetaling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        ArbeidTidslinje arbeidTidslinje = new ArbeidTidslinje.Builder()
                .medArbeid(fødselsdato.minusWeeks(3), fødselsdato.plusWeeks(30), Arbeid.forOrdinærtArbeid(BigDecimal.ZERO, BigDecimal.valueOf(100), null))
                .build();

        grunnlag.medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medArbeid(ARBEIDSFORHOLD_1, arbeidTidslinje)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(20).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(4);

        UttakPeriode up0 = perioder.get(0).getUttakPeriode();
        verifiserPeriode(up0, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        assertThat(up0.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up1 = perioder.get(1).getUttakPeriode();
        verifiserPeriode(up1, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up1.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up2 = perioder.get(2).getUttakPeriode();
        verifiserPeriode(up2, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up2.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up3 = perioder.get(3).getUttakPeriode();
        verifiserManuellBehandlingPeriode(up3, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(20).minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(up3.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.ZERO);

    }

    @Test
    public void gyldig_utsettelse_gir_ingen_utbetaling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        ArbeidTidslinje arbeidTidslinje = new ArbeidTidslinje.Builder()
                .medArbeid(fødselsdato.minusWeeks(3), fødselsdato.plusWeeks(30), Arbeid.forOrdinærtArbeid(BigDecimal.ZERO, BigDecimal.valueOf(100), null))
                .build();
        grunnlag.medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medArbeid(ARBEIDSFORHOLD_1, arbeidTidslinje)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medUtsettelsePeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), Utsettelseårsaktype.FERIE, PeriodeVurderingType.PERIODE_OK, false, false)
                .medStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(14).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medPeriodeMedFerie(new PeriodeMedFerie(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(5);

        UttakPeriode up0 = perioder.get(0).getUttakPeriode();
        verifiserPeriode(up0, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        assertThat(up0.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up1 = perioder.get(1).getUttakPeriode();
        verifiserPeriode(up1, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up1.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up2 = perioder.get(2).getUttakPeriode();
        verifiserPeriode(up2, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up2.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up3 = perioder.get(3).getUttakPeriode();
        assertThat(up3).isInstanceOf(UtsettelsePeriode.class);
        verifiserPeriode(up3, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), INNVILGET, FELLESPERIODE);
        assertThat(up3.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(0);
        assertThat(up3.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.ZERO);

        UttakPeriode up4 = perioder.get(4).getUttakPeriode();
        verifiserPeriode(up4, fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(14).minusDays(1), INNVILGET, FELLESPERIODE);
        assertThat(up4.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    public void gradering_gir_redusert_utbetalingsgrad() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlagBuilder grunnlag = FastsettePeriodeGrunnlagTestBuilder.enGraderingsperiode(fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1), BigDecimal.valueOf(20));
        List<AktivitetIdentifikator> aktiviteter = Collections.singletonList(ARBEIDSFORHOLD_1);
        leggPåKvoter(grunnlag, aktiviteter);
        grunnlag.medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medGradertStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), aktiviteter, BigDecimal.valueOf(20), PeriodeVurderingType.PERIODE_OK, false, false)
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(3);

        UttakPeriode up0 = perioder.get(0).getUttakPeriode();
        verifiserPeriode(up0, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        assertThat(up0.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up1 = perioder.get(1).getUttakPeriode();
        verifiserPeriode(up1, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up1.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up2 = perioder.get(2).getUttakPeriode();
        verifiserPeriode(up2, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), INNVILGET, FELLESPERIODE);
        assertThat(up2.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    public void gradering_gir_redusert_utbetalingsgrad_avrunding() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        List<AktivitetIdentifikator> aktivititeter = Arrays.asList(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_3);
        FastsettePeriodeGrunnlagBuilder grunnlag = FastsettePeriodeGrunnlagTestBuilder.enGraderingsperiodeMedFlereAktiviteter(fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1), new BigDecimal("17.55"), aktivititeter);
        leggPåKvoter(grunnlag, aktivititeter);
        grunnlag.medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medGradertStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), aktivititeter, BigDecimal.valueOf(20), PeriodeVurderingType.PERIODE_OK, false, false)
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        UttakPeriode uttakPeriode = perioder.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("82.45"));
        assertThat(uttakPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD_3)).isEqualTo(new BigDecimal("82.45"));
    }

    private FastsettePeriodeGrunnlagBuilder leggPåKvoter(FastsettePeriodeGrunnlagBuilder builder, List<AktivitetIdentifikator> aktivititeter) {
        for (AktivitetIdentifikator aktivitetIdentifikator : aktivititeter) {
            builder.medSaldo(aktivitetIdentifikator, FORELDREPENGER_FØR_FØDSEL, 15)
                    .medSaldo(aktivitetIdentifikator, MØDREKVOTE, 50)
                    .medSaldo(aktivitetIdentifikator, FEDREKVOTE, 50)
                    .medSaldo(aktivitetIdentifikator, FELLESPERIODE, 130);
        }
        return builder;
    }
}
