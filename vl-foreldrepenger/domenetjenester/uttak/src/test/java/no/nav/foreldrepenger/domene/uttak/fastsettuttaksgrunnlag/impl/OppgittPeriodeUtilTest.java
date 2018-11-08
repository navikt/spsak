package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OppholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OverføringÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;

public class OppgittPeriodeUtilTest {

    @Test
    public void sorterEtterFom() {
        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.of(2018, 6, 11), LocalDate.of(2018, 6, 17))
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();
        OppgittPeriode periode2 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(LocalDate.of(2018, 6, 4), LocalDate.of(2018, 6, 10))
            .build();

        List<OppgittPeriode> perioder = OppgittPeriodeUtil.sorterEtterFom(Arrays.asList(periode1, periode2));

        assertThat(perioder.get(0)).isEqualTo(periode2);
        assertThat(perioder.get(1)).isEqualTo(periode1);
    }

    @Test
    public void første_søkte_dato_kan_være_overføringsperiode() {

        OppgittPeriode førstePeriodeOverføring = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medÅrsak(OverføringÅrsak.SYKDOM_ANNEN_FORELDER)
            .medPeriode(LocalDate.of(2018, 6, 11), LocalDate.of(2018, 6, 17))
            .build();

        OppgittPeriode andrePeriodeUttaksperiode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(LocalDate.of(2018, 6, 18), LocalDate.of(2018, 6, 28))
            .build();

        OppgittFordeling oppgittFordeling = opprettOppgittFordeling(Arrays.asList(førstePeriodeOverføring, andrePeriodeUttaksperiode));

        Optional<LocalDate> førsteSøkteUttaksdato = OppgittPeriodeUtil.finnFørsteSøkteUttaksdato(oppgittFordeling);

        assertThat(førsteSøkteUttaksdato.get()).isEqualTo(førstePeriodeOverføring.getFom());
    }

    @Test
    public void første_søkte_dato_kan_være_vanlig_uttaksperiode() {

        OppgittPeriode førstePeriodeUttaksperiode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(LocalDate.of(2018, 6, 11), LocalDate.of(2018, 6, 17))
            .build();

        OppgittPeriode andrePeriodeOverføring = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medÅrsak(OverføringÅrsak.SYKDOM_ANNEN_FORELDER)
            .medPeriode(LocalDate.of(2018, 6, 18), LocalDate.of(2018, 6, 28))
            .build();

        OppgittFordeling oppgittFordeling = opprettOppgittFordeling(Arrays.asList(førstePeriodeUttaksperiode, andrePeriodeOverføring));

        Optional<LocalDate> førsteSøkteUttaksdato = OppgittPeriodeUtil.finnFørsteSøkteUttaksdato(oppgittFordeling);

        assertThat(førsteSøkteUttaksdato.get()).isEqualTo(førstePeriodeUttaksperiode.getFom());
    }

    @Test
    public void første_søkte_dato_kan_være_utsettelseperioder() {

        OppgittPeriode førstePeriodeUtsettelse = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medÅrsak(UtsettelseÅrsak.FERIE)
            .medPeriode(LocalDate.of(2018, 6, 11), LocalDate.of(2018, 6, 17))
            .build();

        OppgittPeriode andrePeriodeUttaksperiode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(LocalDate.of(2018, 6, 18), LocalDate.of(2018, 6, 28))
            .build();

        OppgittFordeling oppgittFordeling = opprettOppgittFordeling(Arrays.asList(førstePeriodeUtsettelse, andrePeriodeUttaksperiode));

        Optional<LocalDate> førsteSøkteUttaksdato = OppgittPeriodeUtil.finnFørsteSøkteUttaksdato(oppgittFordeling);

        assertThat(førsteSøkteUttaksdato.get()).isEqualTo(førstePeriodeUtsettelse.getFom());
    }

    @Test
    public void første_søkte_dato_skal_ikke_være_oppholdsperioder() {

        OppgittPeriode førstePeriodeOpphold = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medÅrsak(OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER)
            .medPeriode(LocalDate.of(2018, 6, 11), LocalDate.of(2018, 6, 17))
            .build();

        OppgittPeriode andrePeriodeUttaksperiode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(LocalDate.of(2018, 6, 18), LocalDate.of(2018, 6, 28))
            .build();

        OppgittFordeling oppgittFordeling = opprettOppgittFordeling(Arrays.asList(førstePeriodeOpphold, andrePeriodeUttaksperiode));

        Optional<LocalDate> førsteSøkteUttaksdato = OppgittPeriodeUtil.finnFørsteSøkteUttaksdato(oppgittFordeling);

        assertThat(førsteSøkteUttaksdato.get()).isEqualTo(andrePeriodeUttaksperiode.getFom());
    }

    private OppgittFordeling opprettOppgittFordeling(List<OppgittPeriode> oppgittePerioder) {
        return new OppgittFordelingEntitet(oppgittePerioder, true);
    }

}
