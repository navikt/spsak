package no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening;


import java.time.LocalDate;
import java.util.List;

public class FastsattOpptjeningDto {

    private LocalDate opptjeningFom;
    private LocalDate opptjeningTom;
    private OpptjeningPeriodeDto opptjeningperiode;
    private List<FastsattOpptjeningAktivitetDto> fastsattOpptjeningAktivitetList;

    public FastsattOpptjeningDto() {
        // trengs for deserialisering av JSON
    }

    FastsattOpptjeningDto(LocalDate fom, LocalDate tom, OpptjeningPeriodeDto opptjeningperiode,
                          List<FastsattOpptjeningAktivitetDto> fastsattOpptjeningAktivitetList) {
        this.opptjeningFom = fom;
        this.opptjeningTom = tom;
        this.opptjeningperiode = opptjeningperiode;
        this.fastsattOpptjeningAktivitetList = fastsattOpptjeningAktivitetList;
    }

    public LocalDate getOpptjeningFom() {
        return opptjeningFom;
    }

    public LocalDate getOpptjeningTom() {
        return opptjeningTom;
    }

    public OpptjeningPeriodeDto getOpptjeningperiode() {
        return opptjeningperiode;
    }

    public List<FastsattOpptjeningAktivitetDto> getFastsattOpptjeningAktivitetList() {
        return fastsattOpptjeningAktivitetList;
    }

    public void setOpptjeningFom(LocalDate opptjeningFom) {
        this.opptjeningFom = opptjeningFom;
    }

    public void setOpptjeningTom(LocalDate opptjeningTom) {
        this.opptjeningTom = opptjeningTom;
    }

    public void setOpptjeningperiode(OpptjeningPeriodeDto opptjeningperiode) {
        this.opptjeningperiode = opptjeningperiode;
    }

    public void setFastsattOpptjeningAktivitetList(List<FastsattOpptjeningAktivitetDto> fastsattOpptjeningAktivitetList) {
        this.fastsattOpptjeningAktivitetList = fastsattOpptjeningAktivitetList;
    }
}
