package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.UttakPeriodeEndringDto;

public class FastsettePerioderEndringTjenesteImplTest {

    private static final Behandling BEHANDLING = mock(Behandling.class);

    @Test
    public void endretPeriodeSkalVæreEndring() {
        UttakResultatPerioderEntitet opprinneligPerioder = new UttakResultatPerioderEntitet();
        UttakResultatPeriodeEntitet opprinneligPeriode = enkeltPeriodeAktivitet(2, BigDecimal.TEN);
        opprinneligPerioder.leggTilPeriode(opprinneligPeriode);
        UttakResultatPerioderEntitet overstyrtPerioder = new UttakResultatPerioderEntitet();
        UttakResultatPeriodeEntitet overstyrtPeriode = enkeltPeriodeAktivitet(10, BigDecimal.ZERO);
        overstyrtPerioder.leggTilPeriode(overstyrtPeriode);
        UttakResultatEntitet uttakResultat = new UttakResultatEntitet.Builder(mock(Behandlingsresultat.class))
            .medOpprinneligPerioder(opprinneligPerioder)
            .medOverstyrtPerioder(overstyrtPerioder)
            .build();
        FastsettePerioderEndringTjenesteImpl tjeneste = tjeneste(uttakResultat);

        List<UttakPeriodeEndringDto> endringer = tjeneste.finnEndringerMellomOpprinneligOgOverstyrt(BEHANDLING);

        assertThat(endringer).hasSize(1);
        assertThat(endringer.get(0).getFom()).isEqualTo(opprinneligPeriode.getFom());
        assertThat(endringer.get(0).getTom()).isEqualTo(opprinneligPeriode.getTom());
        assertThat(endringer.get(0).getErEndret()).isTrue();
        assertThat(endringer.get(0).getErAvklart()).isFalse();
        assertThat(endringer.get(0).getErSlettet()).isFalse();
        assertThat(endringer.get(0).getErLagtTil()).isFalse();
    }

    @Test
    public void splittingAvPeriodeSkalGiEnSlettingOgToLagtTilEndringer() {
        UttakResultatPerioderEntitet opprinneligPerioder = new UttakResultatPerioderEntitet();
        UttakResultatPeriodeEntitet opprinneligPeriode = minimumPeriode().build();
        opprinneligPerioder.leggTilPeriode(opprinneligPeriode);
        UttakResultatPerioderEntitet overstyrtPerioder = new UttakResultatPerioderEntitet();
        UttakResultatPeriodeEntitet overstyrtPeriode1 = new UttakResultatPeriodeEntitet.Builder(LocalDate.now().minusMonths(1), LocalDate.now().minusWeeks(2))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakResultatPeriodeEntitet overstyrtPeriode2 = new UttakResultatPeriodeEntitet.Builder(LocalDate.now().minusWeeks(2).plusDays(1), LocalDate.now())
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        overstyrtPerioder.leggTilPeriode(overstyrtPeriode1);
        overstyrtPerioder.leggTilPeriode(overstyrtPeriode2);
        UttakResultatEntitet uttakResultat = new UttakResultatEntitet.Builder(mock(Behandlingsresultat.class))
            .medOpprinneligPerioder(opprinneligPerioder)
            .medOverstyrtPerioder(overstyrtPerioder)
            .build();
        FastsettePerioderEndringTjenesteImpl tjeneste = tjeneste(uttakResultat);

        List<UttakPeriodeEndringDto> endringer = tjeneste.finnEndringerMellomOpprinneligOgOverstyrt(BEHANDLING);

        assertThat(endringer).hasSize(3);
        assertThat(endringer.get(0).getFom()).isEqualTo(opprinneligPeriode.getFom());
        assertThat(endringer.get(0).getTom()).isEqualTo(opprinneligPeriode.getTom());
        assertThat(endringer.get(0).getErEndret()).isFalse();
        assertThat(endringer.get(0).getErAvklart()).isFalse();
        assertThat(endringer.get(0).getErSlettet()).isTrue();
        assertThat(endringer.get(0).getErLagtTil()).isFalse();
        assertThat(endringer.get(1).getFom()).isEqualTo(overstyrtPeriode1.getFom());
        assertThat(endringer.get(1).getTom()).isEqualTo(overstyrtPeriode1.getTom());
        assertThat(endringer.get(1).getErEndret()).isFalse();
        assertThat(endringer.get(1).getErAvklart()).isFalse();
        assertThat(endringer.get(1).getErSlettet()).isFalse();
        assertThat(endringer.get(1).getErLagtTil()).isTrue();
        assertThat(endringer.get(2).getFom()).isEqualTo(overstyrtPeriode2.getFom());
        assertThat(endringer.get(2).getTom()).isEqualTo(overstyrtPeriode2.getTom());
        assertThat(endringer.get(2).getErEndret()).isFalse();
        assertThat(endringer.get(2).getErAvklart()).isFalse();
        assertThat(endringer.get(2).getErSlettet()).isFalse();
        assertThat(endringer.get(2).getErLagtTil()).isTrue();
    }

    @Test
    public void skalReturnereTomListeHvisOverstyrtErNull() {
        UttakResultatPerioderEntitet opprinneligPerioder = new UttakResultatPerioderEntitet();
        UttakResultatPeriodeEntitet opprinneligPeriode = enkeltPeriodeAktivitet(2, BigDecimal.ZERO);
        opprinneligPerioder.leggTilPeriode(opprinneligPeriode);
        UttakResultatEntitet uttakResultat = new UttakResultatEntitet.Builder(mock(Behandlingsresultat.class))
            .medOpprinneligPerioder(opprinneligPerioder)
            .medOverstyrtPerioder(null)
            .build();
        FastsettePerioderEndringTjenesteImpl tjeneste = tjeneste(uttakResultat);

        List<UttakPeriodeEndringDto> endringer = tjeneste.finnEndringerMellomOpprinneligOgOverstyrt(BEHANDLING);

        assertThat(endringer).hasSize(0);
    }

    @Test
    public void endretPeriodeSkalVæreEndringFlereAktiviteter() {
        UttakResultatPerioderEntitet opprinneligPerioder = new UttakResultatPerioderEntitet();

        UttakResultatPeriodeEntitet opprinneligPeriode = minimumPeriode().build();
        UttakResultatPeriodeAktivitetEntitet opprinneligAktivitet1 = periodeAktivitet(opprinneligPeriode, 1);
        UttakResultatPeriodeAktivitetEntitet opprinneligAktivitet2 = periodeAktivitet(opprinneligPeriode, 1);
        UttakResultatPeriodeAktivitetEntitet opprinneligAktivitet3 = periodeAktivitet(opprinneligPeriode, 2);
        opprinneligPeriode.leggTilAktivitet(opprinneligAktivitet1);
        opprinneligPeriode.leggTilAktivitet(opprinneligAktivitet2);
        opprinneligPeriode.leggTilAktivitet(opprinneligAktivitet3);

        opprinneligPerioder.leggTilPeriode(opprinneligPeriode);
        UttakResultatPerioderEntitet overstyrtPerioder = new UttakResultatPerioderEntitet();
        UttakResultatPeriodeEntitet overstyrtPeriode = minimumPeriode().build();
        overstyrtPeriode.leggTilAktivitet(opprinneligAktivitet1);
        overstyrtPeriode.leggTilAktivitet(opprinneligAktivitet3);
        overstyrtPerioder.leggTilPeriode(overstyrtPeriode);
        UttakResultatEntitet uttakResultat = new UttakResultatEntitet.Builder(mock(Behandlingsresultat.class))
            .medOpprinneligPerioder(opprinneligPerioder)
            .medOverstyrtPerioder(overstyrtPerioder)
            .build();
        FastsettePerioderEndringTjenesteImpl tjeneste = tjeneste(uttakResultat);

        List<UttakPeriodeEndringDto> endringer = tjeneste.finnEndringerMellomOpprinneligOgOverstyrt(BEHANDLING);

        assertThat(endringer).hasSize(1);
        assertThat(endringer.get(0).getFom()).isEqualTo(opprinneligPeriode.getFom());
        assertThat(endringer.get(0).getTom()).isEqualTo(opprinneligPeriode.getTom());
        assertThat(endringer.get(0).getErEndret()).isTrue();
        assertThat(endringer.get(0).getErAvklart()).isFalse();
        assertThat(endringer.get(0).getErSlettet()).isFalse();
        assertThat(endringer.get(0).getErLagtTil()).isFalse();
    }

    private UttakResultatPeriodeAktivitetEntitet periodeAktivitet(UttakResultatPeriodeEntitet periode, int trekkdager) {
        UttakAktivitetEntitet uttakAktivitet = uttakAktivitet();
        return new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivitet)
            .medArbeidsprosent(BigDecimal.ZERO)
            .medTrekkdager(trekkdager)
            .build();
    }

    private UttakResultatPeriodeEntitet enkeltPeriodeAktivitet(int trekkdager, BigDecimal utbetalingsgrad) {
        UttakResultatPeriodeEntitet.Builder periodeBuilder = minimumPeriode();
        UttakResultatPeriodeEntitet periode = periodeBuilder.build();
        UttakAktivitetEntitet uttakAktivitet = uttakAktivitet();
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivitet)
            .medArbeidsprosent(BigDecimal.ZERO)
            .medTrekkdager(trekkdager)
            .medUtbetalingsprosent(utbetalingsgrad)
            .build();
        periode.leggTilAktivitet(periodeAktivitet);
        return periode;
    }

    private UttakAktivitetEntitet uttakAktivitet() {
        return new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr("orgnr").build(), ArbeidsforholdRef.ref("id"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
    }

    private UttakResultatPeriodeEntitet.Builder minimumPeriode() {
        return new UttakResultatPeriodeEntitet.Builder(LocalDate.now().minusMonths(1), LocalDate.now())
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT);
    }

    private FastsettePerioderEndringTjenesteImpl tjeneste(UttakResultatEntitet uttakResultat) {
        return new FastsettePerioderEndringTjenesteImpl(uttakRepository(uttakResultat));
    }

    private UttakRepository uttakRepository(UttakResultatEntitet uttakResultat) {
        UttakRepository mock = mock(UttakRepository.class);
        when(mock.hentUttakResultat(BEHANDLING)).thenReturn(uttakResultat);
        return mock;
    }

}
