package no.nav.foreldrepenger.uttaksvilkår;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class FastsettePeriodeRegelOrkestreringFedrekvoteTest extends FastsettePerioderRegelOrkestreringTestBase {


    @Test
    public void fedrekvote_med_tidlig_oppstart_og_gyldig_grunn_fra_første_dag_til_midten_av_perioden_blir_innvilget_med_knekkpunkt() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(false)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(1).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(2), PeriodeVurderingType.UAVKLART_PERIODE)
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(resultater).hasSize(2);

        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(1).minusDays(1), INNVILGET, FEDREKVOTE);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(2), MANUELL_BEHANDLING, FEDREKVOTE);
    }


    @Test
    public void skal_gi_manuell_behandling_når_far_har_gyldig_grunn_til_tidlig_oppstart_men_ikke_omsorg() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(false)
                .medMorRett(true)
                .medSamtykke(true)
                .medGyldigGrunnForTidligOppstartPeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                .medPeriodeUtenOmsorg(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(FORELDREPENGER, 100)
                .build();

        List<FastsettePeriodeResultat> periodeResultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(periodeResultater).hasSize(1);
        List<UttakPeriode> perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());

        verifiserManuellBehandlingPeriode(perioder.get(0), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG);
    }

    @Test
    public void fedrekvote_fra_1_dag_før_6_uker_skal_behandles_manuelt() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate førsteLovligeUttaksdag = fødselsdato.withDayOfMonth(1).minusMonths(3);

        grunnlag.medFørsteLovligeUttaksdag(førsteLovligeUttaksdag)
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(false)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6).minusDays(1), fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.UAVKLART_PERIODE)
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(resultater).hasSize(2);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.plusWeeks(6).minusDays(1), fødselsdato.plusWeeks(6).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
    }


    @Test
    public void fedrekvote_før_6_uker_blir_avslått() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        grunnlag.medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(false)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.UAVKLART_PERIODE)
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(resultater).hasSize(2);
        //verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), FEDREKVOTE, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO); TODO HN
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
    }

    @Test
    public void fedrekvote_bli_manuell_behandling_når_søker_ikke_har_omsorg() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(false)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medPeriodeUtenOmsorg(fødselsdato, fødselsdato.plusWeeks(100))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG);
    }

    @Test
    public void overføring_av_fedrekvote_grunnet_sykdom_skade_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medOverføringAvKvote(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK)
                .medGyldigGrunnForTidligOppstartPeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1))
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(4);

        //3 uker foreldrepenger før fødsel innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        //assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        //assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        //assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(20);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote innvilges
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        //assertThat(perioder.get(3).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(20);
        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    @Test
    public void overføring_av_fedrekvote_grunnet_sykdom_skade_skal_gå_til_manuell_behandling_hvis_ikke_gyldig_grunn() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medOverføringAvKvote(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK)
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(4);

        //3 uker foreldrepenger før fødsel innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        //assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        //assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        //assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(20);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote innvilges
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    public void overføring_av_fedrekvote_ugyldig_årsak_skal_til_manuell_behandling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag
                .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medOverføringAvKvote(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), null, PeriodeVurderingType.UAVKLART_PERIODE)
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(4);

        //3 uker foreldrepenger før fødsel innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote skal til manuell behandling
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

}
