package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "OverføringÅrsak")
@DiscriminatorValue(OverføringÅrsak.DISCRIMINATOR)
public class OverføringÅrsak extends Årsak {
    public static final String DISCRIMINATOR = "OVERFOERING_AARSAK_TYPE";

    public static final OverføringÅrsak INSTITUSJONSOPPHOLD_ANNEN_FORELDRE = new OverføringÅrsak("INSTITUSJONSOPPHOLD_ANNEN_FORELDER");
    public static final OverføringÅrsak SYKDOM_ANNEN_FORELDER = new OverføringÅrsak("SYKDOM_ANNEN_FORELDER");
    public static final OverføringÅrsak UDEFINERT = new OverføringÅrsak("-");

    public OverføringÅrsak(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public OverføringÅrsak() {
    }
}
