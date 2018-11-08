package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;

public class FastsettePeriodeRegelOrkestreringGraderingTest {

    protected FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();

    private FastsettePeriodeGrunnlagBuilder leggPåKvoter(FastsettePeriodeGrunnlagBuilder builder) {
        return builder.medSaldo(FORELDREPENGER_FØR_FØDSEL, 15)
                .medSaldo(MØDREKVOTE, 50)
                .medSaldo(FEDREKVOTE, 50)
                .medSaldo(FELLESPERIODE, 130);
    }

    @Test
    public void periode_med_gradering_og_10_prosent_arbeid_skal_få_riktig_antall_trekkdager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        FastsettePeriodeGrunnlagBuilder grunnlag = FastsettePeriodeGrunnlagTestBuilder.enGraderingsperiode(graderingFom, graderingTom, BigDecimal.valueOf(10));
        leggPåKvoter(grunnlag);
        grunnlag.medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, graderingFom.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medGradertStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, graderingFom, graderingTom, Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(10), PeriodeVurderingType.PERIODE_OK, false, false)
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();

        //4 neste uker mødrekvote innvilges og gradering beholdes
        uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isTrue();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(10));
        assertThat(stønadsPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(45);
    }

    @Test
    public void periode_med_gradering_og_90_prosent_arbeid_skal_få_riktig_antall_trekkdager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        FastsettePeriodeGrunnlagBuilder grunnlag = FastsettePeriodeGrunnlagTestBuilder.enGraderingsperiode(graderingFom, graderingTom, BigDecimal.valueOf(90));
        leggPåKvoter(grunnlag);
        grunnlag
                .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medGradertStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, graderingFom, graderingTom, Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(90), PeriodeVurderingType.PERIODE_OK, false, false)
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();

        //10 neste uker mødrekvote innvilges og gradering beholdes
        uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isTrue();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(90));
        assertThat(stønadsPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(5);
    }

    @Test
    public void periode_med_gradering_og_90_prosent_arbeid_og_lite_igjen_på_saldo_skal_bli_innvilget() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        FastsettePeriodeGrunnlagBuilder grunnlag = FastsettePeriodeGrunnlagTestBuilder.enGraderingsperiode(graderingFom, graderingTom, BigDecimal.valueOf(90));
        leggPåKvoter(grunnlag);
        grunnlag.medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, graderingFom.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medGradertStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, graderingFom, graderingTom, Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(90), PeriodeVurderingType.PERIODE_OK, false, false)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 5) //bare 5 dager fellesigjen
                .build();

        FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(fastsettePeriodeGrunnlag);
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();

        //10 neste uker mødrekvote innvilges og gradering beholdes
        uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isTrue();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(90));
        assertThat(stønadsPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(5);

        //Skal være 0 igjen på konto for fellesperiode
        assertThat(fastsettePeriodeGrunnlag.getTrekkdagertilstand().saldo(ARBEIDSFORHOLD_1, Stønadskontotype.FELLESPERIODE)).isEqualTo(0);
    }

    @Test
    public void periode_med_gradering_og_80_prosent_arbeid_og_det_er_for_lite_igjen_på_saldo_slik_at_perioden_knekkes() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        FastsettePeriodeGrunnlagBuilder grunnlag = FastsettePeriodeGrunnlagTestBuilder.enGraderingsperiode(graderingFom, graderingTom, BigDecimal.valueOf(80));
        leggPåKvoter(grunnlag);
        grunnlag.medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, graderingFom.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medGradertStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, graderingFom, graderingTom, Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(80), PeriodeVurderingType.PERIODE_OK, false, false)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 5) //bare 5 dager felles igjen
                .build();

        FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(fastsettePeriodeGrunnlag);
        assertThat(resultat).hasSize(5);

        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();

        //5 neste uker fellesperiode innvilges
        uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(15).minusDays(1));
        assertThat(stønadsPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isTrue();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(80));
        assertThat(stønadsPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(5);

        //5 siste uker fellesperiode avslås
        uttakPeriode = resultat.get(4).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(15));
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(stønadsPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(stønadsPeriode.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(stønadsPeriode.harGradering()).isTrue();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(80));

        //Skal være 0 igjen på konto for fellesperiode
        assertThat(fastsettePeriodeGrunnlag.getTrekkdagertilstand().saldo(ARBEIDSFORHOLD_1, Stønadskontotype.FELLESPERIODE)).isEqualTo(0);
    }

    @Test
    public void fellesperiode_før_fødsel_skal_ikke_kunne_ha_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlagBuilder grunnlag = FastsettePeriodeGrunnlagTestBuilder.enGraderingsperiode(fødselsdato.minusWeeks(6), fødselsdato.minusWeeks(3).minusDays(1), BigDecimal.valueOf(50));
        leggPåKvoter(grunnlag);
        grunnlag.medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medGradertStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(6), fødselsdato.minusWeeks(3).minusDays(1), Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(50), PeriodeVurderingType.PERIODE_OK, false, false)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK);

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(3);

        //Foreldrepenger før fødsel innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(6));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusWeeks(3).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(50));

        //3 uker før fødsel innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(0));

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.harGradering()).isFalse();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(0));
    }
}
