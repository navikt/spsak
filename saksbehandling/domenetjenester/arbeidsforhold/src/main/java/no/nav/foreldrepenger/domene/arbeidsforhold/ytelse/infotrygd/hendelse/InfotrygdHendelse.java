package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Denne klassen er brukt for å opprette en objekt som inneholder noen felter
 * av hendelser som blir hentet fra Infotrygd sin hendelsesfeed.
 *
 * @see https://confluence.adeo.no/display/INFOTRYGD/hendelser-api+-+Database+Tabeller+og+Kolonner
 */

public class InfotrygdHendelse {

    private Long aktoerId;

    private Long sekvensnummer;

    private LocalDateTime regsTidspunkt;

    private LocalDate fom;

    private String identDato;

    private String typeYtelse;

    private String type;

    public InfotrygdHendelse() {
    }

    private InfotrygdHendelse(Builder builder) {
        this.sekvensnummer = builder.sekvensnummer;
        this.type = builder.type;
        this.regsTidspunkt = builder.regsTidspunkt;
        this.aktoerId = builder.aktørId;
        this.fom = builder.fom;
        this.identDato = builder.identDato;
        this.typeYtelse = builder.typeYtelse;
    }

    public Long getAktoerId() {
        return aktoerId;
    }

    public void setAktoerId(Long aktoerId) {
        this.aktoerId = aktoerId;
    }

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public String getIdentDato() {
        return identDato;
    }

    public void setIdentDato(String identDato) {
        this.identDato = identDato;
    }

    /**
     * Type kode for infotrygd ytelsen. Aktuelle koder kan sjekkes fra lenken som er gitt øverst på klassen
     */
    public String getTypeYtelse() {
        return typeYtelse;
    }

    public void setTypeYtelse(String typeYtelse) {
        this.typeYtelse = typeYtelse;
    }

    public Long getSekvensnummer() {
        return sekvensnummer;
    }

    /**
     * Kode som beskriver type av hendelsen. Aktuelle verdier kan sjekkes fra lenken som er gitt øverst på klassen
     */
    public String getType() {
        return type;
    }

    public LocalDateTime getRegsTidspunkt() {
        return regsTidspunkt;
    }

    public void setRegsTidspunkt(LocalDateTime regsTidspunkt) {
        this.regsTidspunkt = regsTidspunkt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long aktørId;

        private LocalDate fom;

        private String identDato;

        private LocalDateTime regsTidspunkt;

        private String typeYtelse;

        private Long sekvensnummer;

        private String type;

        public Builder medAktørId(Long value) {
            this.aktørId = value;
            return this;
        }


        public Builder medFom(LocalDate value) {
            this.fom = value;
            return this;
        }


        public Builder medIdentDato(String value) {
            this.identDato = value;
            return this;
        }

        public Builder medRegTidspunkt(LocalDateTime value) {
            this.regsTidspunkt = value;
            return this;
        }


        public Builder medTypeYtelse(String value) {
            this.typeYtelse = value;
            return this;
        }

        public Builder medSekvensnummer(Long value) {
            this.sekvensnummer = value;
            return this;
        }

        public Builder medType(String value) {
            this.type = value;
            return this;
        }

        public InfotrygdHendelse build() {
            return new InfotrygdHendelse(this);
        }

    }

}
