package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class FedrekvoteDelregelTest {

    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);

    @Test
    public void fedrekvote_etter_6_uker_blir_innvilget() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        FastsettePeriodeGrunnlag grunnlag = basicGrunnlag(fødselsdato)
                .medSøkerMor(false)
                .medStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 10 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void fedrekvote_før_fødsel_blir_avslått() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        FastsettePeriodeGrunnlag grunnlag = basicGrunnlag(fødselsdato)
                .medSøkerMor(false)
                .medStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(5), fødselsdato.minusWeeks(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 10 * 5)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    public void fedrekvote_bli_avslått_når_søker_ikke_har_omsorg() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        FastsettePeriodeGrunnlag grunnlag = basicGrunnlag(fødselsdato)
                .medSøkerMor(false)
                .medStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 10 * 5)
                .medPeriodeUtenOmsorg(fødselsdato, fødselsdato.plusWeeks(100))
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    public void UT1177_mor_overføring_sykdom_avklart_førUke7_etterTermin_gyldigGrunn_omsorg_disponibleDager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medOverføringAvKvote(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fom, tom, OverføringÅrsak.SYKDOM_ELLER_SKADE,
                        PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 1000)
                .medGyldigGrunnForTidligOppstartPeriode(LocalDate.MIN, LocalDate.MAX)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    public void UT1174_mor_overføring_innleggelse_avklart_førUke7_etterTermin_gyldigGrunn_omsorg_disponibleDager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medOverføringAvKvote(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fom, tom, OverføringÅrsak.INNLEGGELSE,
                        PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 1000)
                .medGyldigGrunnForTidligOppstartPeriode(LocalDate.MIN, LocalDate.MAX)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    public void UT1026_far_førUke7_etterTermin_gyldigGrunn_omsorg_disponibleDager_ikkeGradert() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        FastsettePeriodeGrunnlag grunnlag = basicGrunnlag(fødselsdato)
                .medSøkerMor(false)
                .medStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 1000)
                .medGyldigGrunnForTidligOppstartPeriode(LocalDate.MIN, LocalDate.MAX)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void UT1217_far_førUke7_etterTermin_gyldigGrunn_omsorg_disponibleDager_gradert_avklart() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        FastsettePeriodeGrunnlag grunnlag = basicGrunnlag(fødselsdato)
                .medSøkerMor(false)
                .medGradertStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4),
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 1000)
                .medGyldigGrunnForTidligOppstartPeriode(LocalDate.MIN, LocalDate.MAX)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    //TODO BIXBITE
    @Ignore("Vil ikke kunne nå dette sluttpunktet før gyldig grunn endres til å ikke se på om perioden er avklart")
    public void UT1167_far_førUke7_etterTermin_gyldigGrunn_omsorg_disponibleDager_gradert_uavklart() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        FastsettePeriodeGrunnlag grunnlag = basicGrunnlag(fødselsdato)
                .medSøkerMor(false)
                .medGradertStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4),
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN, PeriodeVurderingType.UAVKLART_PERIODE)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 1000)
                .medGyldigGrunnForTidligOppstartPeriode(LocalDate.MIN, LocalDate.MAX)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.PERIODE_UAVKLART);
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
    }

    @Test
    public void UT1175_mor_overføring_sykdom_avklart_etterUke7_omsorg_disponibleDager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(10);
        LocalDate tom = fødselsdato.plusWeeks(15);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medOverføringAvKvote(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fom, tom, OverføringÅrsak.SYKDOM_ELLER_SKADE,
                        PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 1000)
                .medGyldigGrunnForTidligOppstartPeriode(LocalDate.MIN, LocalDate.MAX)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    public void UT1176_mor_overføring_innleggelse_avklart_etterUke7_omsorg_disponibleDager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(10);
        LocalDate tom = fødselsdato.plusWeeks(15);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medOverføringAvKvote(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fom, tom, OverføringÅrsak.INNLEGGELSE, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 1000)
                .medGyldigGrunnForTidligOppstartPeriode(fom, tom)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    public void UT1031_far_etterUke7_gyldigGrunn_omsorg_disponibleDager_ikkeGradert() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        FastsettePeriodeGrunnlag grunnlag = basicGrunnlag(fødselsdato)
                .medSøkerMor(false)
                .medStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(9), PeriodeVurderingType.IKKE_VURDERT)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 1000)
                .medGyldigGrunnForTidligOppstartPeriode(LocalDate.MIN, LocalDate.MAX)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void fom_akkurat_6_uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        FastsettePeriodeGrunnlag grunnlag = basicGrunnlag(fødselsdato)
                .medSøkerMor(false)
                .medStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(6).plusDays(1), PeriodeVurderingType.IKKE_VURDERT)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 1000)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void UT1218_far_etterUke7_gyldigGrunn_omsorg_disponibleDager_gradert_avklart() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        FastsettePeriodeGrunnlag grunnlag = basicGrunnlag(fødselsdato)
                .medSøkerMor(false)
                .medGradertStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(15),
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 1000)
                .medGyldigGrunnForTidligOppstartPeriode(LocalDate.MIN, LocalDate.MAX)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void UT1168_far_etterUke7_gyldigGrunn_omsorg_disponibleDager_gradert_uavklart() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        FastsettePeriodeGrunnlag grunnlag = basicGrunnlag(fødselsdato)
                .medSøkerMor(false)
                .medGradertStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(15),
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN, PeriodeVurderingType.UAVKLART_PERIODE)
                .medSaldo(Stønadskontotype.FEDREKVOTE, 1000)
                .medGyldigGrunnForTidligOppstartPeriode(LocalDate.MIN, LocalDate.MAX)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.PERIODE_UAVKLART);
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
    }

    private void assertInnvilget(Regelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.isUtbetal()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(innvilgetÅrsak);
    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlag(LocalDate fødselsdato) {
        return FastsettePeriodeGrunnlagTestBuilder.create()
                .medFørsteLovligeUttaksdag(fødselsdato.withDayOfMonth(1).minusMonths(3))
                .medFamiliehendelseDato(fødselsdato)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medMorRett(true)
                .medFarRett(true)
                .medSamtykke(true);
    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlagMor(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato)
                .medSøkerMor(true)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 1000);
    }
}
