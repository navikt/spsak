package no.nav.foreldrepenger.behandlingslager.behandling.klage;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "KlageMedholdÅrsak")
@DiscriminatorValue(KlageMedholdÅrsak.DISCRIMINATOR)
public class KlageMedholdÅrsak extends Kodeliste {

    public static final String DISCRIMINATOR = "KLAGE_MEDHOLD_AARSAK";

    public static final KlageMedholdÅrsak NYE_OPPLYSNINGER = new KlageMedholdÅrsak("NYE_OPPLYSNINGER"); //$NON-NLS-1$
    public static final KlageMedholdÅrsak ULIK_REGELVERKSTOLKNING = new KlageMedholdÅrsak("ULIK_REGELVERKSTOLKNING"); //$NON-NLS-1$
    public static final KlageMedholdÅrsak ULIK_VURDERING = new KlageMedholdÅrsak("ULIK_VURDERING"); //$NON-NLS-1$
    public static final KlageMedholdÅrsak PROSESSUELL_FEIL = new KlageMedholdÅrsak("PROSESSUELL_FEIL"); //$NON-NLS-1$
    public static final KlageMedholdÅrsak UDEFINERT = new KlageMedholdÅrsak("-"); //$NON-NLS-1$

    KlageMedholdÅrsak() {
        // for hibernate
    }

    private KlageMedholdÅrsak(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
