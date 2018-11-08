package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring.uttak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagTekstBuilderFormater;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPeriodeAktivitetLagreDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPeriodeLagreDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.overstyring.UttakHistorikkUtil;
import no.nav.fpsak.tidsserie.LocalDateInterval;

public class UttakHistorikkUtilTest {

    private static final Behandling BEHANDLING = mockBehandling();
    private static final LocalDate DEFAULT_FOM = LocalDate.now();
    private static final LocalDate DEFAULT_TOM = LocalDate.now().plusWeeks(1);
    public static final String ORGNR = "000000000";
    public static final String ARBEIDSFORHOLD_ID = "1234";

    @Test
    public void skalLageHistorikkInnslagForPeriodeResultatTypeHvisEndring() {
        PeriodeResultatType ikkeFastsatt = PeriodeResultatType.IKKE_FASTSATT;
        UttakResultatPerioderEntitet gjeldende = enkeltPeriode(ikkeFastsatt);

        List<UttakResultatPeriodeLagreDto> grupper = nyMedResultatType(PeriodeResultatType.INNVILGET);

        List<Historikkinnslag> historikkinnslag = UttakHistorikkUtil.forOverstyring().lagHistorikkinnslag(BEHANDLING,
            AksjonspunktDefinisjon.OVERSTYRING_AV_UTTAKPERIODER, grupper, gjeldende);

        assertThat(historikkinnslag).hasSize(1);
        assertThat(historikkinnslag.get(0).getBehandlingId()).isEqualTo(BEHANDLING.getId());
        assertThat(historikkinnslag.get(0).getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.get(0).getHistorikkinnslagDeler()).hasSize(1);
        Optional<HistorikkinnslagFelt> endretFelt = historikkinnslag.get(0).getHistorikkinnslagDeler().get(0)
            .getEndretFelt(HistorikkEndretFeltType.UTTAK_PERIODE_RESULTAT_TYPE);
        assertThat(endretFelt).isNotEmpty();
        assertThat(endretFelt.get().getHistorikkinnslagDel().getSkjermlenke()).isEmpty();
        assertThat(endretFelt.get().getFraVerdi()).isEqualTo(gjeldende.getPerioder().get(0).getPeriodeResultatType().getKode());
        assertThat(endretFelt.get().getTilVerdi()).isEqualTo(grupper.get(0).getPeriodeResultatType().getKode());
    }

    @Test
    public void skalIkkeLageHistorikkInnslagForPeriodeResultatTypeHvisIngenEndring() {
        UttakResultatPerioderEntitet gjeldende = enkeltPeriode(PeriodeResultatType.INNVILGET);

        List<UttakResultatPeriodeLagreDto> grupper = nyMedResultatType(PeriodeResultatType.INNVILGET);

        List<Historikkinnslag> historikkinnslag = UttakHistorikkUtil.forOverstyring().lagHistorikkinnslag(BEHANDLING,
            AksjonspunktDefinisjon.OVERSTYRING_AV_UTTAKPERIODER, grupper, gjeldende);

        assertThat(historikkinnslag).isEmpty();
    }

    @Test
    public void skalLageHistorikkinnslagAvSplitting() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(3);
        LocalDate tomSplitPeriode1 = fom.plusWeeks(1);
        LocalDate fomSplitPeriode2 = tomSplitPeriode1.plusDays(1);
        UttakResultatPerioderEntitet gjeldende = periodeMedFørOgEtter(PeriodeResultatType.INNVILGET,
            fom, tom, fom.minusMonths(1), fom.minusDays(1), tom.plusDays(1), tom.plusWeeks(1));

        UttakResultatPeriodeLagreDto uendretFør = nyPeriodeMedType(PeriodeResultatType.INNVILGET, fom.minusMonths(1), fom.minusDays(1));
        UttakResultatPeriodeLagreDto splittetFørste = nyPeriodeMedType(PeriodeResultatType.INNVILGET, fom, tomSplitPeriode1);
        UttakResultatPeriodeLagreDto splittetAndre = nyPeriodeMedType(PeriodeResultatType.INNVILGET, fomSplitPeriode2, tom);
        UttakResultatPeriodeLagreDto uendretEtter = nyPeriodeMedType(PeriodeResultatType.AVSLÅTT, tom.plusDays(1), tom.plusWeeks(1));
        List<UttakResultatPeriodeLagreDto> gruppper = Arrays.asList(uendretFør, splittetFørste, splittetAndre, uendretEtter);

        List<Historikkinnslag> historikkinnslag = UttakHistorikkUtil.forOverstyring().lagHistorikkinnslag(BEHANDLING,
            AksjonspunktDefinisjon.OVERSTYRING_AV_UTTAKPERIODER, gruppper, gjeldende);

        assertThat(historikkinnslag).hasSize(1);
        assertThat(historikkinnslag.get(0).getBehandlingId()).isEqualTo(BEHANDLING.getId());
        assertThat(historikkinnslag.get(0).getType()).isEqualTo(HistorikkinnslagType.OVST_UTTAK_SPLITT);
        assertThat(historikkinnslag.get(0).getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.get(0).getHistorikkinnslagDeler()).hasSize(1);
        HistorikkinnslagDel del = historikkinnslag.get(0).getHistorikkinnslagDeler().get(0);
        assertThat(del.getEndredeFelt()).hasSize(2);
        assertThat(del.getEndredeFelt().get(0).getFeltType()).isEqualTo(HistorikkinnslagFeltType.ENDRET_FELT);
        assertThat(del.getEndredeFelt().get(0).getFraVerdi()).isEqualTo(asHistorikkVerdiString(fom, tom));
        assertThat(del.getEndredeFelt().get(0).getTilVerdi()).isEqualTo(asHistorikkVerdiString(fom, tomSplitPeriode1));
        assertThat(del.getEndredeFelt().get(1).getFeltType()).isEqualTo(HistorikkinnslagFeltType.ENDRET_FELT);
        assertThat(del.getEndredeFelt().get(1).getFraVerdi()).isEqualTo(asHistorikkVerdiString(fom, tom));
        assertThat(del.getEndredeFelt().get(1).getTilVerdi()).isEqualTo(asHistorikkVerdiString(fomSplitPeriode2, tom));
    }

    @Test
    public void skalLageHistorikkinnslagAvBådeSplittingEndringAvPeriode() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(3);
        LocalDate tomSplitPeriode1 = fom.plusWeeks(1);
        LocalDate fomSplitPeriode2 = tomSplitPeriode1.plusDays(1);
        UttakResultatPerioderEntitet gjeldende = enkeltPeriode(PeriodeResultatType.INNVILGET, fom, tom);

        UttakResultatPeriodeLagreDto splittetFørste = nyPeriodeMedType(PeriodeResultatType.INNVILGET, fom, tomSplitPeriode1);
        UttakResultatPeriodeLagreDto splittetAndre = nyPeriodeMedType(PeriodeResultatType.AVSLÅTT, fomSplitPeriode2, tom);
        List<UttakResultatPeriodeLagreDto> grupper = Arrays.asList(splittetFørste, splittetAndre);

        List<Historikkinnslag> historikkinnslag = UttakHistorikkUtil.forOverstyring().lagHistorikkinnslag(BEHANDLING,
            AksjonspunktDefinisjon.OVERSTYRING_AV_UTTAKPERIODER, grupper, gjeldende);

        assertThat(historikkinnslag).hasSize(2);
        assertThat(historikkinnslag.get(0).getType()).isEqualTo(HistorikkinnslagType.OVST_UTTAK_SPLITT);
        assertThat(historikkinnslag.get(1).getType()).isEqualTo(HistorikkinnslagType.OVST_UTTAK);
    }

    @Test
    public void skalLageHistorikkinnslagAvBådeSplittingEndringAvPeriodeVedFastsetting() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(3);
        LocalDate tomSplitPeriode1 = fom.plusWeeks(1);
        LocalDate fomSplitPeriode2 = tomSplitPeriode1.plusDays(1);
        UttakResultatPerioderEntitet gjeldende = enkeltPeriode(PeriodeResultatType.INNVILGET, fom, tom);

        UttakResultatPeriodeLagreDto splittetFørste = nyPeriodeMedType(PeriodeResultatType.INNVILGET, fom, tomSplitPeriode1);
        UttakResultatPeriodeLagreDto splittetAndre = nyPeriodeMedType(PeriodeResultatType.AVSLÅTT, fomSplitPeriode2, tom);
        List<UttakResultatPeriodeLagreDto> grupper = Arrays.asList(splittetFørste, splittetAndre);

        List<Historikkinnslag> historikkinnslag = UttakHistorikkUtil.forFastsetting().lagHistorikkinnslag(BEHANDLING,
            AksjonspunktDefinisjon.FASTSETT_UTTAKPERIODER, grupper, gjeldende);

        assertThat(historikkinnslag).hasSize(2);
        assertThat(historikkinnslag.get(0).getType()).isEqualTo(HistorikkinnslagType.FASTSATT_UTTAK_SPLITT);
        assertThat(historikkinnslag.get(1).getType()).isEqualTo(HistorikkinnslagType.FASTSATT_UTTAK);
    }

    private UttakResultatPerioderEntitet periodeMedFørOgEtter(PeriodeResultatType type,
                                                              LocalDate fom,
                                                              LocalDate tom,
                                                              LocalDate førFom,
                                                              LocalDate førTom,
                                                              LocalDate etterFom,
                                                              LocalDate etterTom) {
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        UttakResultatPeriodeEntitet periode = periode(type, fom, tom);
        periode.leggTilAktivitet(periodeAktivitet(periode));
        UttakResultatPeriodeEntitet førPeriode = periode(PeriodeResultatType.INNVILGET, førFom, førTom);
        førPeriode.leggTilAktivitet(periodeAktivitet(førPeriode));
        UttakResultatPeriodeEntitet etterPeriode = periode(PeriodeResultatType.AVSLÅTT, etterFom, etterTom);
        førPeriode.leggTilAktivitet(periodeAktivitet(etterPeriode));
        perioder.leggTilPeriode(førPeriode);
        perioder.leggTilPeriode(periode);
        perioder.leggTilPeriode(etterPeriode);
        return perioder;
    }

    private UttakResultatPeriodeEntitet periode(PeriodeResultatType type, LocalDate fom, LocalDate tom) {
        return new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(type, PeriodeResultatÅrsak.UKJENT)
            .build();
    }

    private UttakResultatPeriodeAktivitetEntitet periodeAktivitet(UttakResultatPeriodeEntitet periode) {
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr(ORGNR).build(), ArbeidsforholdRef.ref(ARBEIDSFORHOLD_ID))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        return new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivitet)
            .medArbeidsprosent(BigDecimal.ZERO)
            .build();
    }

    private String asHistorikkVerdiString(LocalDate fom, LocalDate tom) {
        return HistorikkinnslagTekstBuilderFormater.formatString(new LocalDateInterval(fom, tom));
    }

    private UttakResultatPerioderEntitet enkeltPeriode(PeriodeResultatType type, LocalDate fom, LocalDate tom) {
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        UttakResultatPeriodeEntitet periode = periode(type, fom, tom);
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr("000000000").build(), ArbeidsforholdRef.ref("1234"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivitet)
            .medArbeidsprosent(BigDecimal.ZERO)
            .build();
        periode.leggTilAktivitet(periodeAktivitet);
        perioder.leggTilPeriode(periode);
        return perioder;
    }

    private List<UttakResultatPeriodeLagreDto> nyMedResultatType(PeriodeResultatType type) {
        UttakResultatPeriodeLagreDto nyGruppe = nyPeriodeMedType(type);
        return Collections.singletonList(nyGruppe);
    }

    private UttakResultatPeriodeLagreDto nyPeriodeMedType(PeriodeResultatType type) {
        return nyPeriodeMedType(type, DEFAULT_FOM, DEFAULT_TOM);
    }

    private UttakResultatPeriodeLagreDto nyPeriodeMedType(PeriodeResultatType resultatType, LocalDate fom, LocalDate tom) {
        List<UttakResultatPeriodeAktivitetLagreDto> aktiviteter = new ArrayList<>();
        UttakResultatPeriodeAktivitetLagreDto aktivitetLagreDto = new UttakResultatPeriodeAktivitetLagreDto.Builder()
            .medArbeidsforholdOrgnr(ORGNR)
            .medArbeidsforholdId(ARBEIDSFORHOLD_ID)
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        aktiviteter.add(aktivitetLagreDto);
        return new UttakResultatPeriodeLagreDto.Builder()
            .medTidsperiode(fom, tom)
            .medAktiviteter(aktiviteter)
            .medType(resultatType)
            .medÅrsak(PeriodeResultatÅrsak.UKJENT)
            .medFlerbarnsdager(false)
            .medSamtidigUttak(false)
            .build();
    }

    private UttakResultatPerioderEntitet enkeltPeriode(PeriodeResultatType type) {
        return enkeltPeriode(type, DEFAULT_FOM, DEFAULT_TOM);
    }

    private static Behandling mockBehandling() {
        Behandling mock = mock(Behandling.class);
        when(mock.getId()).thenReturn(123L);
        return mock;
    }

}
