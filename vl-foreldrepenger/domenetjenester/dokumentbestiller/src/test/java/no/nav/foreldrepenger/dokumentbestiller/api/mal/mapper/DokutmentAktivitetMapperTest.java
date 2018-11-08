package no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokutmentAktivitetMapper.mapAnnenAktivitet;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokutmentAktivitetMapper.mapArbeidsforhold;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokutmentAktivitetMapper.mapNæring;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.AnnenAktivitetDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.ArbeidsforholdDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.NæringDto;

public class DokutmentAktivitetMapperTest {

    private String ORGNAVN = "EPLEHUSET AS";
    private String ORGNR = "21542512";

    BeregningsresultatPeriode brp = new BeregningsresultatPeriode();
    BeregningsgrunnlagPeriode bgp = new BeregningsgrunnlagPeriode();

    @Test
    public void map_arbeidsforhold_bruker_andel() {
        Optional<UttakResultatPeriodeAktivitetEntitet> uttakAktivitet = lagUttak(BigDecimal.valueOf(100), BigDecimal.valueOf(100), false, false);
        BeregningsresultatAndel andel = lagAndel(true, 100, 1000, Inntektskategori.ARBEIDSTAKER, 100, AktivitetStatus.ARBEIDSTAKER);
        ArbeidsforholdDto dto = mapArbeidsforhold(uttakAktivitet, andel);
        assertThat(dto.getUtbetalingsgrad()).isEqualTo(uttakAktivitet.get().getUtbetalingsprosent().intValue());
        assertThat(dto.getUttaksgrad()).isEqualTo(uttakAktivitet.get().getUtbetalingsprosent().intValue());
        assertThat(dto.getArbeidsgiverNavn()).isEqualTo(andel.getVirksomhet().getNavn());
        assertThat(dto.getProsentArbeid()).isEqualTo(uttakAktivitet.get().getArbeidsprosent().intValue());
        assertThat(dto.getGradering()).isEqualTo(uttakAktivitet.get().isGraderingInnvilget());
        assertThat(dto.getStillingsprosent()).isEqualTo(andel.getStillingsprosent().intValue());
    }

    @Test
    public void map_arbeidsforhold_arbeidsgiver_andel() {
        Optional<UttakResultatPeriodeAktivitetEntitet> uttakAktivitet = lagUttak(BigDecimal.valueOf(50), BigDecimal.valueOf(60), true, false);
        BeregningsresultatAndel andel = lagAndel(false, 50, 3000, Inntektskategori.ARBEIDSTAKER, 50, AktivitetStatus.ARBEIDSTAKER);
        ArbeidsforholdDto dto = mapArbeidsforhold(uttakAktivitet, andel);
        assertThat(dto.getUtbetalingsgrad()).isEqualTo(uttakAktivitet.get().getUtbetalingsprosent().intValue());
        assertThat(dto.getUttaksgrad()).isEqualTo(uttakAktivitet.get().getUtbetalingsprosent().intValue());
        assertThat(dto.getArbeidsgiverNavn()).isEqualTo(andel.getVirksomhet().getNavn());
        assertThat(dto.getProsentArbeid()).isEqualTo(uttakAktivitet.get().getArbeidsprosent().intValue());
        assertThat(dto.getGradering()).isEqualTo(uttakAktivitet.get().isGraderingInnvilget());
        assertThat(dto.getStillingsprosent()).isEqualTo(andel.getStillingsprosent().intValue());
    }

    @Test
    public void map_arbeidsforhold_uten_uttak() {
        Optional<UttakResultatPeriodeAktivitetEntitet> uttakAktivitet = lagUttak(BigDecimal.valueOf(50), BigDecimal.valueOf(60), true, true);
        BeregningsresultatAndel andel = lagAndel(false, 50, 3000, Inntektskategori.ARBEIDSTAKER, 50, AktivitetStatus.ARBEIDSTAKER);
        ArbeidsforholdDto dto = mapArbeidsforhold(uttakAktivitet, andel);
        assertThat(dto.getUttaksgrad()).isEqualTo(0);
        assertThat(dto.getArbeidsgiverNavn()).isEqualTo(andel.getVirksomhet().getNavn());
        assertThat(dto.getStillingsprosent()).isEqualTo(andel.getStillingsprosent().intValue());
        assertThat(dto.getGradering()).isFalse();
        assertThat(dto.getProsentArbeid()).isEqualTo(0);
        assertThat(dto.getUtbetalingsgrad()).isEqualTo(0);
    }

    @Test
    public void map_næring() {
        Optional<UttakResultatPeriodeAktivitetEntitet> uttakAktivitet = lagUttak(BigDecimal.valueOf(100), BigDecimal.valueOf(100), false, false);
        BeregningsresultatAndel andel = lagAndel(true, 50, 3000, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, 50, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        BigDecimal pgi1 = BigDecimal.valueOf(200000);
        BigDecimal pgi2 = BigDecimal.valueOf(300000);
        BigDecimal pgi3 = BigDecimal.valueOf(100000);
        BigDecimal snitt = BigDecimal.valueOf(300000);
        LocalDate fom = LocalDate.of(2017, 1, 1);
        LocalDate tom = LocalDate.of(2018, 6, 1);
        Optional<BeregningsgrunnlagPrStatusOgAndel> bgpsa = lagBgpsa(lagPgiListe(pgi1, pgi2, pgi3), snitt, fom, tom, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        NæringDto dto = mapNæring(andel, uttakAktivitet, bgpsa);
        assertThat(dto.getInntekt1()).isEqualTo(pgi1.longValue());
        assertThat(dto.getInntekt2()).isEqualTo(pgi2.longValue());
        assertThat(dto.getInntekt3()).isEqualTo(pgi3.longValue());
        assertThat(dto.getSistLignedeÅr()).isEqualTo(tom.getYear());
        assertThat(dto.getProsentArbeid()).isEqualTo(uttakAktivitet.get().getArbeidsprosent().intValue());
        assertThat(dto.getUttaksgrad()).isEqualTo(uttakAktivitet.get().getArbeidsprosent().intValue());
    }

    @Test
    public void map_næring_tom() {
        BeregningsresultatAndel andel = lagAndel(false, 50, 3000, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, 50, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        NæringDto dto = mapNæring(andel, Optional.empty(), Optional.empty());
        assertThat(dto.getInntekt1()).isEqualTo(0);
        assertThat(dto.getInntekt2()).isEqualTo(0);
        assertThat(dto.getInntekt3()).isEqualTo(0);
        assertThat(dto.getSistLignedeÅr()).isEqualTo(0);
        assertThat(dto.getProsentArbeid()).isEqualTo(0);
        assertThat(dto.getUttaksgrad()).isEqualTo(0);
    }

    @Test
    public void map_annen_aktivitet() {
        BeregningsresultatAndel andel = lagAndel(false, 50, 3000, Inntektskategori.FRILANSER, 50, AktivitetStatus.FRILANSER);
        Optional<UttakResultatPeriodeAktivitetEntitet> uttakAktivitet = lagUttak(BigDecimal.valueOf(100), BigDecimal.valueOf(100), true, false);
        AnnenAktivitetDto dto = mapAnnenAktivitet(andel, uttakAktivitet);
        assertThat(dto.getUttaksgrad()).isEqualTo(uttakAktivitet.get().getUtbetalingsprosent().intValue());
        assertThat(dto.getProsentArbeid()).isEqualTo(uttakAktivitet.get().getArbeidsprosent().intValue());
        assertThat(dto.getAktivitetType()).isEqualTo(andel.getAktivitetStatus().getKode());
        assertThat(dto.getGradering()).isEqualTo(true);
    }

    @Test
    public void map_annen_aktivitet_tom() {
        BeregningsresultatAndel andel = lagAndel(true, 50, 3000, Inntektskategori.FRILANSER, 50, AktivitetStatus.FRILANSER);
        AnnenAktivitetDto dto = mapAnnenAktivitet(andel, Optional.empty());
        assertThat(dto.getUttaksgrad()).isEqualTo(0);
        assertThat(dto.getProsentArbeid()).isEqualTo(0);
        assertThat(dto.getAktivitetType()).isEqualTo(andel.getAktivitetStatus().getKode());
        assertThat(dto.getGradering()).isEqualTo(false);
    }


    private BeregningsresultatAndel lagAndel(boolean brukerErMottaker, int stillingsProsent, int dagsats, Inntektskategori inntektskategori, int utbetalingsgrad, AktivitetStatus status) {
        return new BeregningsresultatAndel.Builder().medUtbetalingsgrad(BigDecimal.valueOf(utbetalingsgrad))
            .medStillingsprosent(BigDecimal.valueOf(stillingsProsent)).medDagsats(dagsats).medBrukerErMottaker(brukerErMottaker)
            .medInntektskategori(inntektskategori).medAktivitetstatus(status)
            .medVirksomhet(new VirksomhetEntitet.Builder().medOrgnr(ORGNR).medNavn(ORGNAVN).build()).build(brp);
    }

    private Optional<UttakResultatPeriodeAktivitetEntitet> lagUttak(BigDecimal arbeidsprosent, BigDecimal utbetalingsprosent, boolean gradering, boolean tomOptional) {
        if (tomOptional) {
            return Optional.empty();
        }
        UttakResultatPeriodeEntitet uttakPeriode = new UttakResultatPeriodeEntitet.Builder(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 2, 2))
            .medGraderingInnvilget(gradering)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        return Optional.of(new UttakResultatPeriodeAktivitetEntitet.Builder(uttakPeriode, new UttakAktivitetEntitet())
            .medArbeidsprosent(arbeidsprosent)
            .medUtbetalingsprosent(utbetalingsprosent)
            .medErSøktGradering(gradering)
            .build());
    }

    private Optional<BeregningsgrunnlagPrStatusOgAndel> lagBgpsa(List<BigDecimal> pgiListe, BigDecimal pgiSnitt, LocalDate fom, LocalDate tom, AktivitetStatus status) {
        return Optional.of(new BeregningsgrunnlagPrStatusOgAndel.Builder().medPgi(pgiSnitt, pgiListe).medBeregningsperiode(fom, tom).medAktivitetStatus(status).build(bgp));
    }

    private List<BigDecimal> lagPgiListe(BigDecimal pgi1, BigDecimal pgi2, BigDecimal pgi3) {
        List<BigDecimal> resultat = new ArrayList<>();
        resultat.add(pgi1);
        resultat.add(pgi2);
        resultat.add(pgi3);
        return resultat;
    }


}
