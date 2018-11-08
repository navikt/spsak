package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class AnnenForelderDto implements AbacDto {
    @Size(max = 11, min = 11)
    @Digits(integer = 11, fraction = 0)
    private String foedselsnummer;
    private Boolean kanIkkeOppgiAnnenForelder;
    @Valid
    private KanIkkeOppgiBegrunnelse kanIkkeOppgiBegrunnelse;
    private Boolean annenForelderInformert;

    public String getFoedselsnummer() {
        return foedselsnummer;
    }

    public void setFoedselsnummer(String foedselsnummer) {
        this.foedselsnummer = foedselsnummer;
    }

    public Boolean getKanIkkeOppgiAnnenForelder() {
        return kanIkkeOppgiAnnenForelder;
    }

    public void setKanIkkeOppgiAnnenForelder(Boolean kanIkkeOppgiAnnenForelder) {
        this.kanIkkeOppgiAnnenForelder = kanIkkeOppgiAnnenForelder;
    }

    public KanIkkeOppgiBegrunnelse getKanIkkeOppgiBegrunnelse() {
        return kanIkkeOppgiBegrunnelse;
    }

    public void setKanIkkeOppgiBegrunnelse(KanIkkeOppgiBegrunnelse kanIkkeOppgiBegrunnelse) {
        this.kanIkkeOppgiBegrunnelse = kanIkkeOppgiBegrunnelse;
    }

    public Boolean getAnnenForelderInformert() {
        return annenForelderInformert;
    }

    public void setAnnenForelderInformert(Boolean annenForelderInformert) {
        this.annenForelderInformert = annenForelderInformert;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        //fødselsnummer kan være null når forelder ikke kan oppgis
        return foedselsnummer == null
            ? AbacDataAttributter.opprett()
            : AbacDataAttributter.opprett().leggTilFødselsnummer(foedselsnummer);
    }

    public static class KanIkkeOppgiBegrunnelse {
        @NotNull
        @Size(min = 1, max = 100)
        @Pattern(regexp = InputValideringRegex.KODEVERK)
        private String arsak;
        @Size(max = 4000)
        @Pattern(regexp = InputValideringRegex.FRITEKST)
        private String begrunnelse;
        @Size(max = 20)
        @Pattern(regexp = InputValideringRegex.FRITEKST)
        private String utenlandskFoedselsnummer;
        @Size(max = 100)
        @Pattern(regexp = InputValideringRegex.NAVN)
        private String land;

        public String getArsak() {
            return arsak;
        }

        public void setArsak(String arsak) {
            this.arsak = arsak;
        }

        public String getBegrunnelse() {
            return begrunnelse;
        }

        public void setBegrunnelse(String begrunnelse) {
            this.begrunnelse = begrunnelse;
        }

        public String getUtenlandskFoedselsnummer() {
            return utenlandskFoedselsnummer;
        }

        public void setUtenlandskFoedselsnummer(String utenlandskFoedselsnummer) {
            this.utenlandskFoedselsnummer = utenlandskFoedselsnummer;
        }

        public String getLand() {
            return land;
        }

        public void setLand(String land) {
            this.land = land;
        }
    }
}
