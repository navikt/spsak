package no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening;


import java.util.List;

public class OpptjeningDto {

    private FastsattOpptjeningDto fastsattOpptjening;
    private List<OpptjeningAktivitetDto> opptjeningAktivitetList;

    public OpptjeningDto() {
        // trengs for deserialisering av JSON
    }

    OpptjeningDto(FastsattOpptjeningDto fastsattOpptjening) {
        this.fastsattOpptjening = fastsattOpptjening;
    }

    public FastsattOpptjeningDto getFastsattOpptjening() {
        return fastsattOpptjening;
    }

    public void setFastsattOpptjening(FastsattOpptjeningDto fastsattOpptjening) {
        this.fastsattOpptjening = fastsattOpptjening;
    }

    public List<OpptjeningAktivitetDto> getOpptjeningAktivitetList() {
        return opptjeningAktivitetList;
    }

    public void setOpptjeningAktivitetList(List<OpptjeningAktivitetDto> opptjeningAktivitetList) {
        this.opptjeningAktivitetList = opptjeningAktivitetList;
    }
}
