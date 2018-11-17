package no.nav.foreldrepenger.behandlingslager.uttak;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "UttakPeriodeType")
@DiscriminatorValue(UttakPeriodeType.DISCRIMINATOR)
public class UttakPeriodeType extends Kodeliste {

    public static final String DISCRIMINATOR = "UTTAK_PERIODE_TYPE";

    public static final UttakPeriodeType FELLESPERIODE = new UttakPeriodeType("FELLESPERIODE");
    public static final UttakPeriodeType MØDREKVOTE = new UttakPeriodeType("MØDREKVOTE");
    public static final UttakPeriodeType FEDREKVOTE = new UttakPeriodeType("FEDREKVOTE");
    public static final UttakPeriodeType FORELDREPENGER = new UttakPeriodeType("FORELDREPENGER");
    public static final UttakPeriodeType FORELDREPENGER_FØR_FØDSEL = new UttakPeriodeType("FORELDREPENGER_FØR_FØDSEL");
    public static final UttakPeriodeType ANNET = new UttakPeriodeType("ANNET"); // Ved utsettelse, opphold og overføring
    public static final UttakPeriodeType UDEFINERT = new UttakPeriodeType("-");

    public static final Set<UttakPeriodeType> STØNADSPERIODETYPER = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(FORELDREPENGER_FØR_FØDSEL, MØDREKVOTE, FEDREKVOTE, FELLESPERIODE, FORELDREPENGER)));


    UttakPeriodeType() {
    }

    UttakPeriodeType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
