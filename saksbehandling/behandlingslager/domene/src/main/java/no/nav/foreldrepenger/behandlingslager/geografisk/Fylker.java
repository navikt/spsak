package no.nav.foreldrepenger.behandlingslager.geografisk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "Fylker")
@DiscriminatorValue(Fylker.DISCRIMINATOR)
public class Fylker extends Kodeliste {
    public static final String DISCRIMINATOR = "FYLKER";

    Fylker() {
        // Hibernate trenger en
    }

    Fylker(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
