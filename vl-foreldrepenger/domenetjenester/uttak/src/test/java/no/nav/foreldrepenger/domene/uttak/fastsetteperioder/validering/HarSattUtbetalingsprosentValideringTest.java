package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriodeAktivitet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.exception.TekniskException;

public class HarSattUtbetalingsprosentValideringTest {

    @Test
    public void ok_når_utbetalingsprosent_er_satt_og_opprinnelig_periode_er_manuell() {
        UttakResultatPerioder opprinnelig = perioder(PeriodeResultatType.MANUELL_BEHANDLING, null);
        UttakResultatPerioder nye = perioder(PeriodeResultatType.INNVILGET, BigDecimal.valueOf(50));

        HarSattUtbetalingsprosentValidering validator = new HarSattUtbetalingsprosentValidering(opprinnelig);
        assertThatCode(() -> validator.utfør(nye)).doesNotThrowAnyException();
    }

    @Test
    public void ok_når_utbetalignsprosent_er_satt_og_opprinnelig_periode_er_ikke_manuell() {
        UttakResultatPerioder opprinnelig = perioder(PeriodeResultatType.INNVILGET, null);
        UttakResultatPerioder nye = perioder(PeriodeResultatType.INNVILGET, null);

        HarSattUtbetalingsprosentValidering validator = new HarSattUtbetalingsprosentValidering(opprinnelig);
        assertThatCode(() -> validator.utfør(nye)).doesNotThrowAnyException();
    }

    @Test
    public void ikke_ok_når_utbetalingsprosent_mangler_og_opprinnelig_periode_er_manuell() {
        UttakResultatPerioder opprinnelig = perioder(PeriodeResultatType.MANUELL_BEHANDLING, null);
        UttakResultatPerioder nye = perioder(PeriodeResultatType.INNVILGET, null);

        HarSattUtbetalingsprosentValidering validator = new HarSattUtbetalingsprosentValidering(opprinnelig);
        assertThatCode(() -> validator.utfør(nye)).isInstanceOf(TekniskException.class);
    }

    private UttakResultatPerioder perioder(PeriodeResultatType resultat, BigDecimal utbetalingsprosent) {
        List<UttakResultatPeriode> opprinneligeGrupper = Collections.singletonList(periodeGruppe(resultat, utbetalingsprosent));
        return new UttakResultatPerioder(opprinneligeGrupper);
    }

    private UttakResultatPeriode periodeGruppe(PeriodeResultatType resultatType, BigDecimal utbetalingsprosent) {
        List<UttakResultatPeriodeAktivitet> aktiviteter = Collections.singletonList(
            new UttakResultatPeriodeAktivitet.Builder()
                .medArbeidsprosent(BigDecimal.ZERO)
                .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
                .medUtbetalingsgrad(utbetalingsprosent).build()
        );
        return new UttakResultatPeriode.Builder()
            .medTidsperiode(new LocalDateInterval(LocalDate.now(), LocalDate.now().plusDays(1)))
            .medAktiviteter(aktiviteter)
            .medType(resultatType)
            .build();
    }
}
