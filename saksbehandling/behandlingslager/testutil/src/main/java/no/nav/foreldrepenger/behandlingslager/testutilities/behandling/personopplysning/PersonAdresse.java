package no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning;

import no.nav.foreldrepenger.domene.typer.AktørId;
import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public final class PersonAdresse {

    private AktørId aktørId;
    private DatoIntervallEntitet periode;
    private AdresseType adresseType;
    private String adresselinje1;
    private String adresselinje2;
    private String adresselinje3;
    private String adresselinje4;
    private String postnummer;
    private String poststed;
    private String land;

    public AktørId getAktørId() {
        return aktørId;
    }

    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    public AdresseType getAdresseType() {
        return adresseType;
    }

    public String getAdresselinje1() {
        return adresselinje1;
    }

    public String getAdresselinje2() {
        return adresselinje2;
    }

    public String getAdresselinje3() {
        return adresselinje3;
    }

    public String getAdresselinje4() {
        return adresselinje4;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getPoststed() {
        return poststed;
    }

    public String getLand() {
        return land;
    }

    private PersonAdresse(Builder builder) {
        this.aktørId = builder.aktørId;
        this.periode = builder.periode;
        this.adresseType = builder.adresseType;
        this.adresselinje1 = builder.adresselinje1;
        this.adresselinje2 = builder.adresselinje2;
        this.adresselinje3 = builder.adresselinje3;
        this.adresselinje4 = builder.adresselinje4;
        this.postnummer = builder.postnummer;
        this.poststed = builder.poststed;
        this.land = builder.land;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {
        private AktørId aktørId;
        private DatoIntervallEntitet periode;
        private AdresseType adresseType;
        private String adresselinje1;
        private String adresselinje2;
        private String adresselinje3;
        private String adresselinje4;
        private String postnummer;
        private String poststed;
        private String land;

        private Builder() {
        }

        public PersonAdresse build() {
            return new PersonAdresse(this);
        }

        public Builder aktørId(AktørId aktørId) {
            this.aktørId = aktørId;
            return this;
        }

        public Builder periode(LocalDate fom, LocalDate tom) {
            this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
            return this;
        }

        public Builder adresseType(AdresseType adresseType) {
            this.adresseType = adresseType;
            return this;
        }

        public Builder adresselinje1(String adresselinje1) {
            this.adresselinje1 = adresselinje1;
            return this;
        }

        public Builder adresselinje2(String adresselinje2) {
            this.adresselinje2 = adresselinje2;
            return this;
        }

        public Builder adresselinje3(String adresselinje3) {
            this.adresselinje3 = adresselinje3;
            return this;
        }

        public Builder adresselinje4(String adresselinje4) {
            this.adresselinje4 = adresselinje4;
            return this;
        }

        public Builder postnummer(String postnummer) {
            this.postnummer = postnummer;
            return this;
        }

        public Builder poststed(String poststed) {
            this.poststed = poststed;
            return this;
        }

        public Builder land(String land) {
            this.land = land;
            return this;
        }

        public Builder land(Landkoder land) {
            this.land = land.getKode(); // TODO (FC) Skriv om hele veien til Landkoder
            return this;
        }
        
        public DatoIntervallEntitet getPeriode() {
            return periode;
        }
    }
}
