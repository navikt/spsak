package no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.AnnenAktivitetDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.ArbeidsforholdDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.NæringDto;

public class DokutmentAktivitetMapper {

    private DokutmentAktivitetMapper() {
        //SONAR
    }

    public static ArbeidsforholdDto mapArbeidsforhold(Optional<UttakResultatPeriodeAktivitetEntitet> uttakAktivitet, BeregningsresultatAndel andel) {
        String virksomhetNavn = andel.getVirksomhet() == null ? "Andel" : andel.getVirksomhet().getNavn();
        String arbeidsforholdId = andel.getArbeidsforholdRef() == null ? null : andel.getArbeidsforholdRef().getReferanse();
        ArbeidsforholdDto dto = new ArbeidsforholdDto();
        if (uttakAktivitet.isPresent()) {
            dto.setUttaksgrad(uttakAktivitet.get().getUtbetalingsprosent().intValue());
            dto.setProsentArbeid(uttakAktivitet.get().getArbeidsprosent().intValue());
            dto.setUtbetalingsgrad(uttakAktivitet.get().getUtbetalingsprosent().intValue());
        }
        dto.setArbeidsgiverNavn(virksomhetNavn);
        dto.setArbeidsforholdId(arbeidsforholdId);
        dto.setGradering(uttakAktivitet.map(UttakResultatPeriodeAktivitetEntitet::isGraderingInnvilget).orElse(false));
        dto.setStillingsprosent(andel.getStillingsprosent().intValue());
        dto.setDagsats(andel.getDagsats());
        return dto;
    }

    public static NæringDto mapNæring(BeregningsresultatAndel andel,
                                      Optional<UttakResultatPeriodeAktivitetEntitet> uttakAktivitet,
                                      Optional<BeregningsgrunnlagPrStatusOgAndel> bgPrStatusOgAndel) {
        NæringDto dto = new NæringDto();
        dto.setDagsats(andel.getDagsats());
        if (uttakAktivitet.isPresent()) {
            dto.setUttaksgrad(uttakAktivitet.get().getUtbetalingsprosent().intValue());
            dto.setProsentArbeid(uttakAktivitet.get().getArbeidsprosent().intValue());
        }
        dto.setGradering(uttakAktivitet.map(UttakResultatPeriodeAktivitetEntitet::isGraderingInnvilget).orElse(false));
        if (bgPrStatusOgAndel.isPresent()) {
            Optional.ofNullable(bgPrStatusOgAndel.get().getBeregningsperiodeTom()).ifPresent(tomDato -> dto.setSistLignedeÅr(tomDato.getYear()));
            Optional.ofNullable(bgPrStatusOgAndel.get().getPgi1()).ifPresent(inntekt1 -> dto.setInntekt1(inntekt1.longValue()));
            Optional.ofNullable(bgPrStatusOgAndel.get().getPgi2()).ifPresent(inntekt2 -> dto.setInntekt2(inntekt2.longValue()));
            Optional.ofNullable(bgPrStatusOgAndel.get().getPgi3()).ifPresent(inntekt3 -> dto.setInntekt3(inntekt3.longValue()));
        }
        return dto;
    }

    public static AnnenAktivitetDto mapAnnenAktivitet(BeregningsresultatAndel andel, Optional<UttakResultatPeriodeAktivitetEntitet> uttakAktivitet) {
        AnnenAktivitetDto dto = new AnnenAktivitetDto();
        dto.setAktivitetType(andel.getAktivitetStatus().getKode());
        if (uttakAktivitet.isPresent()) {
            dto.setUttaksgrad(uttakAktivitet.get().getUtbetalingsprosent().intValue());
            dto.setProsentArbeid(uttakAktivitet.get().getArbeidsprosent().intValue());
        }
        dto.setDagsats(andel.getDagsats());
        dto.setGradering(uttakAktivitet.map(UttakResultatPeriodeAktivitetEntitet::isGraderingInnvilget).orElse(false));
        return dto;
    }
}
