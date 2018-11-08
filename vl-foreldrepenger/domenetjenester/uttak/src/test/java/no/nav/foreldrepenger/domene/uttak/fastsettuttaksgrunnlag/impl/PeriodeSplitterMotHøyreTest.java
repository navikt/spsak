package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;

public class PeriodeSplitterMotHøyreTest {

    @Test
    public void justert_periode_er_før_ikke_flyttbar_periode() {
        OppgittPeriode uttakPeriode = builder().medPeriode(LocalDate.of(2018, 6, 11), LocalDate.of(2018, 6, 17)).build();
        OppgittPeriode utsettelsePgaFerie = builder().medÅrsak(UtsettelseÅrsak.FERIE).medPeriode(LocalDate.of(2018, 6, 25), LocalDate.of(2018, 7, 1)).build();

        List<OppgittPeriode> splittedePerioder = PeriodeSplitter.splittPeriodeMotHøyre(Arrays.asList(utsettelsePgaFerie), uttakPeriode, 7);

        assertThat(splittedePerioder).hasSize(1);
        assertThat(splittedePerioder.get(0).getFom()).isEqualTo(LocalDate.of(2018, 6, 18));
        assertThat(splittedePerioder.get(0).getTom()).isEqualTo(LocalDate.of(2018, 6, 24));
    }

    @Test
    public void justert_periode_kommer_etter_ikke_flyttbar_periode() {
        OppgittPeriode uttakPeriode = builder().medPeriode(LocalDate.of(2018, 6, 18), LocalDate.of(2018, 6, 24)).build();
        OppgittPeriode utsettelsePgaFerie = builder().medÅrsak(UtsettelseÅrsak.FERIE).medPeriode(LocalDate.of(2018, 6, 25), LocalDate.of(2018, 7, 1)).build();

        List<OppgittPeriode> splittedePerioder = PeriodeSplitter.splittPeriodeMotHøyre(Arrays.asList(utsettelsePgaFerie), uttakPeriode, 14);

        assertThat(splittedePerioder).hasSize(1);
        assertThat(splittedePerioder.get(0).getFom()).isEqualTo(LocalDate.of(2018, 7, 9));
        assertThat(splittedePerioder.get(0).getTom()).isEqualTo(LocalDate.of(2018, 7, 15));
    }

    @Test
    public void justert_periode_overlapper_på_begynnelsen_av_ikke_flyttbar_periode() {
        OppgittPeriode uttakPeriode = builder().medPeriode(LocalDate.of(2018, 6, 11), LocalDate.of(2018, 6, 17)).build();
        OppgittPeriode utsettelsePgaFerie = builder().medÅrsak(UtsettelseÅrsak.FERIE).medPeriode(LocalDate.of(2018, 6, 25), LocalDate.of(2018, 7, 1)).build();

        List<OppgittPeriode> splittedePerioder = PeriodeSplitter.splittPeriodeMotHøyre(Arrays.asList(utsettelsePgaFerie), uttakPeriode, 9);

        assertThat(splittedePerioder).hasSize(2);

        assertThat(splittedePerioder.get(0).getFom()).isEqualTo(LocalDate.of(2018, 6, 20));
        assertThat(splittedePerioder.get(0).getTom()).isEqualTo(LocalDate.of(2018, 6, 24));

        assertThat(splittedePerioder.get(1).getFom()).isEqualTo(LocalDate.of(2018, 7, 2));
        assertThat(splittedePerioder.get(1).getTom()).isEqualTo(LocalDate.of(2018, 7, 3));
    }


    @Test
    public void justert_periode_flyttes_mellom_to_ikke_flyttbar_perioder() {
        OppgittPeriode uttakPeriode = builder().medPeriode(LocalDate.of(2018, 5, 28), LocalDate.of(2018, 6, 3)).build();
        OppgittPeriode utsettelsePgaFerie1 = builder().medÅrsak(UtsettelseÅrsak.FERIE).medPeriode(LocalDate.of(2018, 6, 4), LocalDate.of(2018, 6, 10)).build();
        OppgittPeriode utsettelsePgaFerie2 = builder().medÅrsak(UtsettelseÅrsak.FERIE).medPeriode(LocalDate.of(2018, 6, 18), LocalDate.of(2018, 6, 24)).build();


        List<OppgittPeriode> splittedePerioder = PeriodeSplitter.splittPeriodeMotHøyre(Arrays.asList(utsettelsePgaFerie1, utsettelsePgaFerie2), uttakPeriode, 7);

        assertThat(splittedePerioder).hasSize(1);

        assertThat(splittedePerioder.get(0).getFom()).isEqualTo(LocalDate.of(2018, 6, 11));
        assertThat(splittedePerioder.get(0).getTom()).isEqualTo(LocalDate.of(2018, 6, 17));
    }

    @Test
    public void justert_periode_flyttes_etter_to_ikke_flyttbar_perioder() {
        OppgittPeriode uttakPeriode = builder().medPeriode(LocalDate.of(2018, 5, 28), LocalDate.of(2018, 6, 3)).build();
        OppgittPeriode utsettelsePgaFerie1 = builder().medÅrsak(UtsettelseÅrsak.FERIE).medPeriode(LocalDate.of(2018, 6, 4), LocalDate.of(2018, 6, 10)).build();
        OppgittPeriode utsettelsePgaFerie2 = builder().medÅrsak(UtsettelseÅrsak.FERIE).medPeriode(LocalDate.of(2018, 6, 18), LocalDate.of(2018, 6, 24)).build();

        List<OppgittPeriode> splittedePerioder = PeriodeSplitter.splittPeriodeMotHøyre(Arrays.asList(utsettelsePgaFerie1, utsettelsePgaFerie2), uttakPeriode, 14);

        assertThat(splittedePerioder).hasSize(1);

        assertThat(splittedePerioder.get(0).getFom()).isEqualTo(LocalDate.of(2018, 6, 25));
        assertThat(splittedePerioder.get(0).getTom()).isEqualTo(LocalDate.of(2018, 7, 1));
    }

    @Test
    public void justert_periode_overlapper_på_slutten_av_ikke_flyttbar_periode() {
        OppgittPeriode uttakPeriode = builder().medPeriode(LocalDate.of(2018, 6, 11), LocalDate.of(2018, 6, 17)).build();
        OppgittPeriode utsettelsePgaFerie = builder().medÅrsak(UtsettelseÅrsak.FERIE).medPeriode(LocalDate.of(2018, 6, 18), LocalDate.of(2018, 6, 24)).build();

        List<OppgittPeriode> splittedePerioder = PeriodeSplitter.splittPeriodeMotHøyre(Arrays.asList(utsettelsePgaFerie), uttakPeriode, 9);

        assertThat(splittedePerioder).hasSize(1);

        assertThat(splittedePerioder.get(0).getFom()).isEqualTo(LocalDate.of(2018, 6, 27));
        assertThat(splittedePerioder.get(0).getTom()).isEqualTo(LocalDate.of(2018, 7, 3));
    }

    private OppgittPeriodeBuilder builder() {
        return OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FEDREKVOTE);
    }

}
