package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsprosenter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeBehandler;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeBehandlerImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class MødrekvotePeriodeRegelTest {

    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);

    @Test
    public void mødrekvoteperiode_med_nok_dager_på_konto() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 10 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void tidligere_mødreperiode_dekker_periode_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 10 * 5)
                .build();

        Arbeidsprosenter arbeidsprosenter = new Arbeidsprosenter();
        ArbeidTidslinje arbeidTidslinje = new ArbeidTidslinje.Builder().medArbeid(fødselsdato,
                fødselsdato.plusWeeks(10), Arbeid.forOrdinærtArbeid(BigDecimal.ZERO, BigDecimal.valueOf(100), null)).build();
        arbeidsprosenter.leggTil(ARBEIDSFORHOLD_1, arbeidTidslinje);

        //Innvilg første periode
        FastsettePeriodeBehandler fastsettePeriodeBehandler = new FastsettePeriodeBehandlerImpl(grunnlag);
        fastsettePeriodeBehandler.innvilgAktuellPeriode(null, InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL, false, null, arbeidsprosenter, true);

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));
        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void mødrekvote_slutter_på_fredag_og_første_uker_slutter_på_søndag_blir_innvilget() {
        LocalDate fødselsdato = LocalDate.of(2017, 12, 31);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(2), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 10 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));
        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void mødrekvote_de_første_6_ukene_etter_fødsel_skal_innvilges_også_når_mor_ikke_har_omsorg() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 10 * 5)
                .medPeriodeUtenOmsorg(fødselsdato.minusWeeks(10), fødselsdato.plusWeeks(15))
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void mødrekvote_etter_første_6_ukene_etter_fødsel_skal_ikke_innvilges_når_mor_har_nok_på_kvoten_men_ikke_har_omsorg() throws Exception {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(7), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 10 * 5)
                .medPeriodeUtenOmsorg(fødselsdato.minusWeeks(10), fødselsdato.plusWeeks(15))
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    public void mødrekvote_etter_første_6_ukene_etter_fødsel_skal_ikke_innvilges_når_mor_har_noe_men_ikke_nok_på_kvoten_og_ikke_har_omsorg() throws Exception {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(7), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 1)
                .medPeriodeUtenOmsorg(fødselsdato.minusWeeks(10), fødselsdato.plusWeeks(15))
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    public void UT1007_mor_etterTermin_innenFor6Uker_ikkeGradering_disponibleDager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.isUtbetal()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void UT1008_mor_innenFor6UkerEtterFødsel_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medGradertStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4),
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.isUtbetal()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
        assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isEqualTo(GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    public void UT1221_mor_etterTermin_etter6Uker_omsorg_disponibleDager_gradering_avklart() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medGradertStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(11),
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.isUtbetal()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlagMor(LocalDate fødselsdato) {
        return FastsettePeriodeGrunnlagTestBuilder.create()
                .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .medFamiliehendelseDato(fødselsdato)
                .medSamtykke(true)
                .medMorRett(true)
                .medFarRett(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medSøkerMor(true);
    }
}
