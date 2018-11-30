package no.nav.foreldrepenger.behandlingslager.geografisk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "Landgrupper")
@DiscriminatorValue(Landgrupper.DISCRIMINATOR)
public class Landgrupper extends Kodeliste {
    public static final String DISCRIMINATOR = "LANDGRUPPER";

    Landgrupper() {
        // Hibernate trenger en
    }

    Landgrupper(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
