package no.nav.foreldrepenger.behandlingslager.geografisk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "Geopolitisk")
@DiscriminatorValue(Geopolitisk.DISCRIMINATOR)
public class Geopolitisk extends Kodeliste {
    public static final String DISCRIMINATOR = "GEOPOLITISK";

    Geopolitisk() {
        // Hibernate trenger en
    }

    Geopolitisk(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
