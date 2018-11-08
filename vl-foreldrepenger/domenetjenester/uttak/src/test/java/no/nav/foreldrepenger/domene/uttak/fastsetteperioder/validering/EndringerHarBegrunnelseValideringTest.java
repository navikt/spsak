package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

public class EndringerHarBegrunnelseValideringTest {

    @Test
    public void okAlleHarBegrunnelse() {
        List<UttakResultatPeriode> opprinneligeGrupper = Collections.singletonList(periodeGruppe(null, PeriodeResultatType.IKKE_FASTSATT));
        UttakResultatPerioder opprinnelig = new UttakResultatPerioder(opprinneligeGrupper);
        List<UttakResultatPeriode> grupper = Collections.singletonList(periodeGruppe("Ny begrunnelse", PeriodeResultatType.INNVILGET));
        UttakResultatPerioder perioder = new UttakResultatPerioder(grupper);
        EndringerHarBegrunnelseValidering validering = new EndringerHarBegrunnelseValidering(opprinnelig);
        assertThatCode(() -> validering.utfør(perioder)).doesNotThrowAnyException();
    }

    @Test
    public void feilVedTomBegrunnelse() {
        List<UttakResultatPeriode> opprinneligeGrupper = Collections.singletonList(periodeGruppe(null, PeriodeResultatType.IKKE_FASTSATT));
        UttakResultatPerioder opprinnelig = new UttakResultatPerioder(opprinneligeGrupper);
        List<UttakResultatPeriode> grupper = Collections.singletonList(periodeGruppe("", PeriodeResultatType.INNVILGET));
        UttakResultatPerioder perioder = new UttakResultatPerioder(grupper);
        EndringerHarBegrunnelseValidering validering = new EndringerHarBegrunnelseValidering(opprinnelig);
        assertThatThrownBy(() -> validering.utfør(perioder)).isInstanceOf(TekniskException.class);
    }

    @Test
    public void feilVedNullBegrunnelse() {
        List<UttakResultatPeriode> opprinneligeGrupper = Collections.singletonList(periodeGruppe(null, PeriodeResultatType.IKKE_FASTSATT));
        UttakResultatPerioder opprinnelig = new UttakResultatPerioder(opprinneligeGrupper);
        List<UttakResultatPeriode> grupper = Collections.singletonList(periodeGruppe(null, PeriodeResultatType.INNVILGET));
        UttakResultatPerioder perioder = new UttakResultatPerioder(grupper);
        EndringerHarBegrunnelseValidering validering = new EndringerHarBegrunnelseValidering(opprinnelig);
        assertThatThrownBy(() -> validering.utfør(perioder)).isInstanceOf(TekniskException.class);
    }

    @Test
    public void okÅMangleBegrunnelseHvisIngenEndring() {
        List<UttakResultatPeriode> opprinneligeGrupper = Collections.singletonList(periodeGruppe(null, PeriodeResultatType.INNVILGET));
        UttakResultatPerioder opprinnelig = new UttakResultatPerioder(opprinneligeGrupper);
        List<UttakResultatPeriode> grupper = Collections.singletonList(periodeGruppe(null, PeriodeResultatType.INNVILGET));
        UttakResultatPerioder perioder = new UttakResultatPerioder(grupper);
        EndringerHarBegrunnelseValidering validering = new EndringerHarBegrunnelseValidering(opprinnelig);
        assertThatCode(() -> validering.utfør(perioder)).isNull();
    }

    @Test
    public void okÅMangleBegrunnelseHvisBareTrekkdagerEndring() {
        List<UttakResultatPeriode> opprinneligeGrupper = Collections.singletonList(periodeGruppe(10));
        UttakResultatPerioder opprinnelig = new UttakResultatPerioder(opprinneligeGrupper);
        List<UttakResultatPeriode> grupper = Collections.singletonList(periodeGruppe(15));
        UttakResultatPerioder perioder = new UttakResultatPerioder(grupper);
        EndringerHarBegrunnelseValidering validering = new EndringerHarBegrunnelseValidering(opprinnelig);
        assertThatCode(() -> validering.utfør(perioder)).isNull();
    }

    private UttakResultatPeriode periodeGruppe(int trekkdager) {
        UttakResultatPeriodeAktivitet periode = new UttakResultatPeriodeAktivitet.Builder()
            .medTrekkdager(trekkdager)
            .medArbeidsprosent(BigDecimal.TEN)
            .medUtbetalingsgrad(BigDecimal.TEN)
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        List<UttakResultatPeriodeAktivitet> aktiviteter = Collections.singletonList(periode);
        return new UttakResultatPeriode.Builder()
            .medBegrunnelse(null)
            .medTidsperiode(new LocalDateInterval(LocalDate.now(), LocalDate.now().plusDays(1)))
            .medType(PeriodeResultatType.INNVILGET)
            .medAktiviteter(aktiviteter)
            .build();
    }

    private UttakResultatPeriode periodeGruppe(String begrunnelse, PeriodeResultatType resultatType) {
        return new UttakResultatPeriode.Builder()
            .medBegrunnelse(begrunnelse)
            .medType(resultatType)
            .medTidsperiode(new LocalDateInterval(LocalDate.now(), LocalDate.now().plusDays(1)))
            .build();
    }
}
