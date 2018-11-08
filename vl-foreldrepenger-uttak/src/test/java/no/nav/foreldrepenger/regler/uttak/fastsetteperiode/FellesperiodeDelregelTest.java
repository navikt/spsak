package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodePropertyType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class FellesperiodeDelregelTest {

    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);
    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
    private LocalDate førsteLovligeUttaksdag = fødselsdato.minusMonths(3);

    @Test
    public void fellesperiode_mor_uttak_starter_ved_12_uker_og_slutter_ved_3_uker_før_fødsel_blir_innvilget() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(3), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 13 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isTrue();

    }

    @Test
    public void fellesperiode_mor_uttak_starter_ved_3_uker_før_fødsel_slutter_før_7_uker_blir_avslått() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.plusWeeks(3), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 13 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.getProperty(FastsettePeriodePropertyType.KNEKKPUNKT, LocalDate.class)).isNull();
    }

    @Test
    public void fellesperiode_mor_uttak_starter_ved_3_uker_før_fødsel_slutter_før_fødsel_blir_avslått() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusWeeks(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 13 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.getProperty(FastsettePeriodePropertyType.KNEKKPUNKT, LocalDate.class)).isNull();
    }

    @Test
    public void fellesperiode_mor_uttak_starter_ved_7_uker_etter_fødsel_blir_innvilget() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(10), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 13 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void fellesperiode_far_før_fødsel_slutter_før_fødsel_blir_avslått_uten_knekk() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagFar()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(10), fødselsdato.minusWeeks(5), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 10 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.getProperty(FastsettePeriodePropertyType.KNEKKPUNKT, LocalDate.class)).isNull();

    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlagFar() {
        return basicGrunnlag()
                .medSøkerMor(false);
    }

    @Test
    public void fellesperiode_blir_avslått_etter_uke_7_når_mor_ikke_har_omsorg() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(14), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 13 * 5)
                .medPeriodeUtenOmsorg(fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(14))
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    public void fellesperiode_før_fødsel_innvilges_uavhengig_av_om_søker_har_omsorg_da_det_ikke_er_mulig_å_ha_omsorg_fordi_barnet_ikke_er_født() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(10), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 13 * 5)
                .medPeriodeUtenOmsorg(fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(10))
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void UT1041_mor_før3UkerFørFamilieHendelse_ikkeGradert() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(5), fødselsdato.minusWeeks(4),PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 13 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    public void UT1064_mor_før3UkerFørFamilieHendelse_gradert() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor()
                .medGradertStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(5), fødselsdato.minusWeeks(4),
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN ,PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 13 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilgetAvslåttGradering(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    public void UT1219_mor_tidligstUke7_omsorg_disponibleStønadsdager_gradert_avklart() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor()
                .medGradertStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(9),
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN ,PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    public void UT1047_mor_førUke7() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(9) ,PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    public void UT1220_far_førUke7_etterFamileHendelse_gyldigGrunn_omsorg_disponibleStønadsdager_gradert_avklart() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagFar()
                .medGradertStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(5),
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN ,PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    public void UT1055_far_førUke7_etterFamileHendelse_gyldigGrunn_omsorg_disponibleStønadsdager_utenGradering() {
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagFar()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(5),PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    private void assertInnvilget(Regelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.isUtbetal()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(innvilgetÅrsak);
    }

    private void assertInnvilgetAvslåttGradering(Regelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak, GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak) {
        assertInnvilget(regelresultat, innvilgetÅrsak);
        assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isEqualTo(graderingIkkeInnvilgetÅrsak);
    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlagMor() {
        return basicGrunnlag()
                .medSøkerMor(true);
    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlag() {
        return FastsettePeriodeGrunnlagTestBuilder.create()
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag)
                .medFamiliehendelseDato(fødselsdato)
                .medSamtykke(true)
                .medMorRett(true)
                .medFarRett(true)
                .medSøknadstype(Søknadstype.FØDSEL);
    }
}
