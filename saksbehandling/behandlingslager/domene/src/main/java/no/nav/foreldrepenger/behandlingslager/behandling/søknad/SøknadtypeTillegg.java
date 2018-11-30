package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "SøknadtypeTillegg")
@DiscriminatorValue(SøknadtypeTillegg.DISCRIMINATOR)
public class SøknadtypeTillegg extends Kodeliste {

    public static final String DISCRIMINATOR = "SOKNAD_TYPE_TILLEGG";

    public static final SøknadtypeTillegg UDEFINERT = new SøknadtypeTillegg("-"); //$NON-NLS-1$

    public static final SøknadtypeTillegg OVERFORING_AV_KVOTER = new SøknadtypeTillegg("OVERFORING_AV_KVOTER"); //$NON-NLS-1$
    public static final SøknadtypeTillegg UTSETTELSE = new SøknadtypeTillegg("UTSETTELSE"); //$NON-NLS-1$
    public static final SøknadtypeTillegg GRADERING = new SøknadtypeTillegg("GRADERING"); //$NON-NLS-1$

    SøknadtypeTillegg() {
        // Hibernate trenger en
    }

    private SøknadtypeTillegg(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
