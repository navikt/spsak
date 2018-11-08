package no.nav.foreldrepenger.behandlingslager.geografisk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "LandkodeISO2")
@DiscriminatorValue(LandkodeISO2.DISCRIMINATOR)
public class LandkodeISO2 extends Kodeliste {

    public static final String DISCRIMINATOR = "LANDKODE_ISO2";

    LandkodeISO2() {
        // Hibernate trenger en
    }

    LandkodeISO2(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
