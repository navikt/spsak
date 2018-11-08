package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "OppholdÅrsak")
@DiscriminatorValue(OppholdÅrsak.DISCRIMINATOR)
public class OppholdÅrsak extends Årsak {
    public static final String DISCRIMINATOR = "OPPHOLD_AARSAK_TYPE";

    public static final OppholdÅrsak UDEFINERT = new OppholdÅrsak("-");
    public static final OppholdÅrsak INGEN = new OppholdÅrsak("INGEN");
    public static final OppholdÅrsak MØDREKVOTE_ANNEN_FORELDER = new OppholdÅrsak("UTTAK_MØDREKVOTE_ANNEN_FORELDER");
    public static final OppholdÅrsak FEDREKVOTE_ANNEN_FORELDER = new OppholdÅrsak("UTTAK_FEDREKVOTE_ANNEN_FORELDER");
    public static final OppholdÅrsak KVOTE_FELLESPERIODE_ANNEN_FORELDER = new OppholdÅrsak("UTTAK_FELLESP_ANNEN_FORELDER");
    public static final OppholdÅrsak KVOTE_FORELDREPENGER_ANNEN_FORELDER = new OppholdÅrsak("UTTAK_FORELDREPENGER_ANNEN_FORELDER");

    OppholdÅrsak(String kode) {
        super(kode, DISCRIMINATOR);
    }

    OppholdÅrsak() {
    }
}
