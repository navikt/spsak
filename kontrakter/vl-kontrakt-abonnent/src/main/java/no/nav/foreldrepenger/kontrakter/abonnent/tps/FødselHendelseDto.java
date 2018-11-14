package no.nav.foreldrepenger.kontrakter.abonnent.tps;

import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.kontrakter.abonnent.HendelseDto;

public class FødselHendelseDto extends HendelseDto {

    public static final String HENDELSE_TYPE = "FØDSEL";
    public static final String AVSENDER = "tps";

    @NotNull
    @Size(min = 1, max = 2)
    private List<String> aktørIdForeldre;

    @NotNull
    private LocalDate fødselsdato;

    public void setAktørIdForeldre(List<String> aktørIdForeldre) {
        this.aktørIdForeldre = aktørIdForeldre;
    }

    public List<String> getAktørIdForeldre() {
        return this.aktørIdForeldre;
    }

    public void setFødselsdato(LocalDate fødselsdato) {
        this.fødselsdato = fødselsdato;
    }

    public LocalDate getFødselsdato() {
        return fødselsdato;
    }

    @Override
    public String getHendelsetype() {
        return HENDELSE_TYPE;
    }

    @Override
    public String getAvsenderSystem() {
        return AVSENDER;
    }
}
