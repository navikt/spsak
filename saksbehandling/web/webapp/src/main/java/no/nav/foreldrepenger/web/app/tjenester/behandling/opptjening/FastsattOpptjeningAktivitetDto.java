package no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening;


import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetKlassifisering;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetType;

public class FastsattOpptjeningAktivitetDto {
    private LocalDate fom;
    private LocalDate tom;
    private OpptjeningAktivitetType type;
    private OpptjeningAktivitetKlassifisering klasse;
    private String aktivitetReferanse;
    private String arbeidsgiverNavn;

    public FastsattOpptjeningAktivitetDto() {
        // trengs for deserialisering av JSON
    }

    FastsattOpptjeningAktivitetDto(LocalDate fom, LocalDate tom, OpptjeningAktivitetType type,
                                           OpptjeningAktivitetKlassifisering klasse, String aktivitetReferanse, String arbeidsgiverNavn) {
        this.fom = fom;
        this.tom = tom;
        this.type = type;
        this.klasse = klasse;
        this.aktivitetReferanse = aktivitetReferanse;
        this.arbeidsgiverNavn = arbeidsgiverNavn;
    }

    public FastsattOpptjeningAktivitetDto(LocalDate fom, LocalDate tom, OpptjeningAktivitetKlassifisering klasse) {
        this.fom = fom;
        this.tom = tom;
        this.klasse = klasse;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public OpptjeningAktivitetType getType() {
        return type;
    }

    public OpptjeningAktivitetKlassifisering getKlasse() {
        return klasse;
    }

    public String getAktivitetReferanse() {
        return aktivitetReferanse;
    }

    public String getArbeidsgiverNavn() {
        return arbeidsgiverNavn;
    }

    void setFom(LocalDate fom) {
        this.fom = fom;
    }

    void setTom(LocalDate tom) {
        this.tom = tom;
    }

    void setType(OpptjeningAktivitetType type) {
        this.type = type;
    }

    void setKlasse(OpptjeningAktivitetKlassifisering klasse) {
        this.klasse = klasse;
    }

    void setAktivitetReferanse(String aktivitetReferanse) {
        this.aktivitetReferanse = aktivitetReferanse;
    }

    void setArbeidsgiverNavn(String arbeidsgiverNavn) {
        this.arbeidsgiverNavn = arbeidsgiverNavn;
    }
}
