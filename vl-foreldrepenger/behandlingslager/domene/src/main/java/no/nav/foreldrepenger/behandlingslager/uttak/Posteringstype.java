package no.nav.foreldrepenger.behandlingslager.uttak;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;


@Entity(name = "Posteringstype")
@DiscriminatorValue(Posteringstype.DISCRIMINATOR)
public class Posteringstype extends Kodeliste {
    public static final String DISCRIMINATOR = "POSTERINGSTYPE";

    public static final Posteringstype PERIODE = new Posteringstype("PERIODE");
    public static final Posteringstype OVERFØRING = new Posteringstype("OVERFØRING");


    Posteringstype(String kode) {
        super(kode, DISCRIMINATOR);
    }

    Posteringstype() {
    }
}
