package no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.konfig.Tid;

public class Personas {
    private Builder builder;
    private AktørId aktørId;
    private no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personopplysning.Builder persInfoBuilder;
    private LocalDate fødselsdato;
    private LocalDate dødsdato;

    public Personas(Builder builder) {
        this.builder = builder;
    }

    public Personas voksenPerson(AktørId aktørId, SivilstandType st, NavBrukerKjønn kjønn, Region region) {
        if (this.aktørId == null) {
            this.aktørId = aktørId;
            this.persInfoBuilder = Personopplysning.builder();
            this.fødselsdato = LocalDate.now().minusYears(30);  // VOKSEN
        } else {
            throw new IllegalArgumentException("En Personas har kun en aktørId, allerede satt til " + this.aktørId + ", angitt=" + aktørId);
        }
        builder.leggTilPersonopplysninger(persInfoBuilder
            .aktørId(aktørId)
            .brukerKjønn(kjønn)
            .fødselsdato(fødselsdato)
            .sivilstand(st)
            .region(region));
        return this;
    }

    public Personas barn(AktørId aktørId, LocalDate fødselsdato) {
        if (this.aktørId == null) {
            this.aktørId = aktørId;
            this.persInfoBuilder = Personopplysning.builder();
            this.fødselsdato = fødselsdato;
        } else {
            throw new IllegalArgumentException("En Personas har kun en aktørId, allerede satt til " + this.aktørId + ", angitt=" + aktørId);
        }
        builder.leggTilPersonopplysninger(persInfoBuilder
            .aktørId(aktørId)
            .fødselsdato(fødselsdato)
            .brukerKjønn(NavBrukerKjønn.MANN)
            .sivilstand(SivilstandType.UOPPGITT)
            .region(Region.NORDEN));
        return this;
    }

    
    public Personas dødsdato(LocalDate dødsdato) {
        this.dødsdato = dødsdato;
        return this;
    }

    public Personas statsborgerskap(Landkoder landkode) {
        // NB: logik her tilsvarer at dersom dødsdato skal settes bør det settes før.
        return statsborgerskap(landkode, fødselsdato, dødsdato);
    }

    public Personas statsborgerskap(Landkoder landkode, LocalDate fom, LocalDate tom) {
        builder.leggTilStatsborgerskap(
            Statsborgerskap.builder().aktørId(aktørId).statsborgerskap(landkode).periode(fom, tom == null ? Tid.TIDENES_ENDE : tom));
        return this;
    }

    public Personas personstatus(PersonstatusType personstatus) {
        return personstatus(personstatus, fødselsdato, dødsdato);
    }

    public Personas personstatus(PersonstatusType personstatus, LocalDate fom, LocalDate tom) {
        builder.leggTilPersonstatus(Personstatus.builder().aktørId(aktørId).personstatus(personstatus).periode(fom, tom == null ? Tid.TIDENES_ENDE : tom));
        return this;
    }

    public Personas kvinne(AktørId aktørId) {
        voksenPerson(aktørId, SivilstandType.SAMBOER, NavBrukerKjønn.KVINNE, Region.NORDEN);
        return this;
    }

    public Personas kvinne(AktørId aktørId, SivilstandType st) {
        voksenPerson(aktørId, st, NavBrukerKjønn.KVINNE, Region.NORDEN);
        return this;
    }

    public Personas kvinne(AktørId aktørId, SivilstandType st, Region region) {
        voksenPerson(aktørId, st, NavBrukerKjønn.KVINNE, region);
        return this;
    }

    public Personas mann(AktørId aktørId, SivilstandType st) {
        voksenPerson(aktørId, st, NavBrukerKjønn.MANN, Region.NORDEN);
        return this;
    }
    
    public Personas fødtBarn(AktørId aktørId, LocalDate fødselsdato) {
        barn(aktørId, fødselsdato);
        return this;
    }


    public Personas bostedsadresse(PersonAdresse.Builder adresseBuilder) {
        return adresse(AdresseType.BOSTEDSADRESSE, adresseBuilder);
    }
    
    public Personas adresse(AdresseType adresseType, PersonAdresse.Builder adresseBuilder) {
        adresseBuilder.aktørId(aktørId);

        if (adresseBuilder.getPeriode() == null) {
            // for test formål
            adresseBuilder.periode(LocalDate.of(2000, 1, 1), Tid.TIDENES_ENDE);
        }
        adresseBuilder.adresseType(adresseType);
        builder.leggTilAdresser(adresseBuilder);
        return this;
    }

    public Builder mann(AktørId aktørId) {
        voksenPerson(aktørId, SivilstandType.SAMBOER, NavBrukerKjønn.MANN, Region.NORDEN);
        return builder;
    }

    public Personas mann(AktørId aktørId, SivilstandType st, Region region) {
        voksenPerson(aktørId, st, NavBrukerKjønn.MANN, region);
        return this;
    }

    public PersonInformasjon build() {
        return builder.build();
    }

}
