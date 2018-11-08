package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "UttakDokumentasjonKlasse")
@DiscriminatorValue(UttakDokumentasjonKlasse.DISCRIMINATOR)
public class UttakDokumentasjonKlasse extends Kodeliste {

    public static final String DISCRIMINATOR = "UTTAK_DOKUMENTASJON_KLASSE"; //$NON-NLS-1$
    public static final UttakDokumentasjonKlasse UDEFINERT = new UttakDokumentasjonKlasse("-"); //$NON-NLS-1$

    UttakDokumentasjonKlasse() {
        //for Hibernate
    }

    private UttakDokumentasjonKlasse(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
