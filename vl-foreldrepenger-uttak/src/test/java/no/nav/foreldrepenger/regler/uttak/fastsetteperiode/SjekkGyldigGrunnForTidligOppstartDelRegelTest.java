package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;
import no.nav.foreldrepenger.uttaksvilkår.FastsettePeriodeResultat;
import no.nav.foreldrepenger.uttaksvilkår.FastsettePerioderRegelOrkestrering;

public class SjekkGyldigGrunnForTidligOppstartDelRegelTest {
    FastsettePerioderRegelOrkestrering regelOrkestrering = new FastsettePerioderRegelOrkestrering();


    @Test
    @SuppressWarnings("Duplicates")
    public void fedrekvote_med_tidlig_oppstart_og_gyldig_grunn_blir_innvilget() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
            .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(false)
            .medFarRett(true)
            .medMorRett(true)
            .medSamtykke(true)
            .medGyldigGrunnForTidligOppstartPeriode(fødselsdato, fødselsdato.plusWeeks(6))
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6), PeriodeVurderingType.PERIODE_OK)
            .medSaldo(FEDREKVOTE, 10 * 5)
            .build();

        List<FastsettePeriodeResultat> periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(2);
        List<UttakPeriode> perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());
        assertThat(perioder.stream().map(UttakPeriode::getPerioderesultattype).collect(toList())).containsExactly(INNVILGET, INNVILGET);
        assertThat(perioder.stream().map(UttakPeriode::getStønadskontotype).collect(toList())).containsExactly(FEDREKVOTE, FEDREKVOTE);
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void fellesperiode_med_tidlig_oppstart_og_gyldig_grunn_hele_perioden_blir_innvilget() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
            .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(false)
            .medFarRett(true)
            .medMorRett(true)
            .medSamtykke(true)
            .medGyldigGrunnForTidligOppstartPeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medSaldo(FELLESPERIODE, 10 * 5)
            .build();

        List<FastsettePeriodeResultat> periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(1);
        assertThat(periodeResultater.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(periodeResultater.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
    }

    @Test
    public void fedrekvote_med_tidlig_oppstart_uten_gyldig_grunn_deler_av_perioden_skal_behandles_manuelt() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
            .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(false)
            .medFarRett(true)
            .medMorRett(true)
            .medSamtykke(true)
            .medGyldigGrunnForTidligOppstartPeriode(fødselsdato, fødselsdato.plusWeeks(2))
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(10), PeriodeVurderingType.UAVKLART_PERIODE)
            .medSaldo(FEDREKVOTE, 10 * 5)
            .build();

        List<FastsettePeriodeResultat> periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(2);
        List<UttakPeriode> perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());

        UttakPeriode ugyldigPeriode = perioder.get(0);
        assertThat(ugyldigPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(ugyldigPeriode.getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(ugyldigPeriode.getStønadskontotype()).isEqualTo(FEDREKVOTE);

        UttakPeriode gyldigPeriode = perioder.get(1);
        assertThat(gyldigPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(gyldigPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(gyldigPeriode.getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(gyldigPeriode.getStønadskontotype()).isEqualTo(FEDREKVOTE);
    }

    @Test
    public void fedrekvote_med_tidlig_oppstart_og_vurdert_OK_av_saksbehandler_blir_innvilget_med_knekk() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
            .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(false)
            .medFarRett(true)
            .medMorRett(true)
            .medSamtykke(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(10), PeriodeVurderingType.PERIODE_OK)
            .medSaldo(FEDREKVOTE, 10 * 5)
            .build();

        List<FastsettePeriodeResultat> periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(2);
        List<UttakPeriode> perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());

        verifiserPeriode(perioder.get(0), fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, FEDREKVOTE);
        verifiserPeriode(perioder.get(1), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10), INNVILGET, FEDREKVOTE);
    }

    @Test
    public void fedrekvote_med_tidlig_oppstart_og_vurdert_OK_av_saksbehandler_blir_innvilget() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
            .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(false)
            .medFarRett(true)
            .medMorRett(true)
            .medSamtykke(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medSaldo(FEDREKVOTE, 10 * 5)
            .build();

        List<FastsettePeriodeResultat> periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(1);
        List<UttakPeriode> perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());

        verifiserPeriode(perioder.get(0), fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), INNVILGET, FEDREKVOTE);
    }

    @Test
    public void fedrekvote_med_tidlig_oppstart_og_vurdert_uavklart_av_saksbehandler_går_til_manuell_behandling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
            .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(false)
            .medFarRett(true)
            .medMorRett(true)
            .medSamtykke(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), PeriodeVurderingType.UAVKLART_PERIODE)
            .medSaldo(FEDREKVOTE, 10 * 5)
            .build();

        List<FastsettePeriodeResultat> periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(1);
        List<UttakPeriode> perioder = periodeResultater.stream()
            .map(FastsettePeriodeResultat::getUttakPeriode)
            .sorted(comparing(UttakPeriode::getFom))
            .collect(toList());

        verifiserPeriode(perioder.get(0), fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
    }

    @Test
    public void fedrekvote_med_tidlig_oppstart_og_vurdert_OK_av_saksbehandler_blir_innvilget_med_knekk_som_saksbehandler_har_registrert() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
            .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(false)
            .medFarRett(true)
            .medMorRett(true)
            .medSamtykke(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), PeriodeVurderingType.ENDRE_PERIODE)
            .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD,fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.UAVKLART_PERIODE)
            .medSaldo(FEDREKVOTE, 10 * 5)
            .build();

        List<FastsettePeriodeResultat> periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(3);
        List<UttakPeriode> perioder = periodeResultater.stream()
            .map(FastsettePeriodeResultat::getUttakPeriode)
            .sorted(comparing(UttakPeriode::getFom))
            .collect(toList());

        verifiserPeriode(perioder.get(0), fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), INNVILGET, FEDREKVOTE);
        verifiserPeriode(perioder.get(1), fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4).minusDays(1), INNVILGET, FEDREKVOTE);
        verifiserPeriode(perioder.get(2), fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(6).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
    }


    private void verifiserPeriode(UttakPeriode periode, LocalDate forventetFom, LocalDate forventetTom, Perioderesultattype forventetResultat, Stønadskontotype stønadskontotype) {
        assertThat(periode.getFom()).isEqualTo(forventetFom);
        assertThat(periode.getTom()).isEqualTo(forventetTom);
        assertThat(periode.getPerioderesultattype()).isEqualTo(forventetResultat);
        assertThat(periode.getStønadskontotype()).isEqualTo(stønadskontotype);
    }
}
