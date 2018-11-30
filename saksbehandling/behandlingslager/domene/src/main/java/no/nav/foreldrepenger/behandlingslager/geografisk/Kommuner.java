package no.nav.foreldrepenger.behandlingslager.geografisk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "Kommuner")
@DiscriminatorValue(Kommuner.DISCRIMINATOR)
public class Kommuner extends Kodeliste {
    public static final String DISCRIMINATOR = "KOMMUNER";

    Kommuner() {
        // Hibernate trenger en
    }

    Kommuner(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
