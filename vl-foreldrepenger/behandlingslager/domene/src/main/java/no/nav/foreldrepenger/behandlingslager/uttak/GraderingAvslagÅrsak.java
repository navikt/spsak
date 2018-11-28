package no.nav.foreldrepenger.behandlingslager.uttak;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "GraderingAvslagÅrsak")
@DiscriminatorValue(GraderingAvslagÅrsak.DISCRIMINATOR)
public class GraderingAvslagÅrsak extends Kodeliste {
    static final String DISCRIMINATOR = "GRADERING_AVSLAG_AARSAK";
    
    public static final GraderingAvslagÅrsak UKJENT = new GraderingAvslagÅrsak("-");
    private GraderingAvslagÅrsak(String kode) {
        super(kode, DISCRIMINATOR);
    }

    GraderingAvslagÅrsak() {
        // For hibernate
    }
}
