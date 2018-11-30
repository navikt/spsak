package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse;

import java.time.LocalDate;

public abstract class Innhold {
    private String typeYtelse;
    private LocalDate fom;
    private String aktoerId;
    private String identDato;

    public Innhold() {
    }

    public String getTypeYtelse() {
        return this.typeYtelse;
    }

    public void setTypeYtelse(String typeYtelse) {
        this.typeYtelse = typeYtelse;
    }

    public LocalDate getFom() {
        return this.fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public String getAktoerId() {
        return this.aktoerId;
    }

    public void setAktoerId(String aktoerId) {
        this.aktoerId = aktoerId;
    }

    public String getIdentDato() {
        return this.identDato;
    }

    public void setIdentDato(String identDato) {
        this.identDato = identDato;
    }
}
