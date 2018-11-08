package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "AvkortingÅrsak")
@DiscriminatorValue(AvkortingÅrsak.DISCRIMINATOR)
public class AvkortingÅrsak extends Årsak {
    public static final String DISCRIMINATOR = "AVKORTING_AARSAK_TYPE";

    public static final AvkortingÅrsak UDEFINERT = new AvkortingÅrsak("-");
    public static final AvkortingÅrsak SØKT_FOR_SENT = new AvkortingÅrsak("SØKT_FOR_SENT");
    public static final AvkortingÅrsak IKKE_OMSORG = new AvkortingÅrsak("IKKE_OMSORG");
    public static final AvkortingÅrsak MAKSGRENSE_OVERSKREDET = new AvkortingÅrsak("MAKSGRENSE_OVERSREDET");


    AvkortingÅrsak(String kode) {
        super(kode, DISCRIMINATOR);
    }

    AvkortingÅrsak() {
    }
}
