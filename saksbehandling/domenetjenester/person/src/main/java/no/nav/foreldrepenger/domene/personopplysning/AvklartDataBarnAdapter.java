package no.nav.foreldrepenger.domene.personopplysning;

import no.nav.foreldrepenger.domene.typer.AktørId;
import java.time.LocalDate;

public class AvklartDataBarnAdapter {

    private AktørId aktørId;
    private LocalDate fødselsdato;
    private Integer nummer;

    public AvklartDataBarnAdapter(AktørId aktørId, LocalDate fødselsdato, Integer nummer) {
        this.aktørId = aktørId;
        this.fødselsdato = fødselsdato;
        this.nummer = nummer;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public LocalDate getFødselsdato() {
        return fødselsdato;
    }

    public Integer getNummer() {
        return nummer;
    }
}
