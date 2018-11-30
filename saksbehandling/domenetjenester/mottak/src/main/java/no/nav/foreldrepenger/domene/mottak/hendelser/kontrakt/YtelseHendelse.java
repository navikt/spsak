package no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt;

import java.time.LocalDate;

public class YtelseHendelse extends Hendelse {

    private String typeYtelse;
    private LocalDate fom;
    private String aktoerId;
    private String identDato;

    protected YtelseHendelse() {
        super("YTELSE_HENDELSE");
    }

    public YtelseHendelse(String hendelseKode, String typeYtelse, String aktoerId, LocalDate fom, String identDato) {
        super(hendelseKode);
        this.typeYtelse = typeYtelse;
        this.fom = fom;
        this.aktoerId = aktoerId;
        this.identDato = identDato;
    }

    public LocalDate getFom() {
        return fom;
    }

    public String getAktoerId() {
        return aktoerId;
    }

    public String getTypeYtelse() {
        return typeYtelse;
    }

    public String getIdentDato() {
        return identDato;
    }
}
