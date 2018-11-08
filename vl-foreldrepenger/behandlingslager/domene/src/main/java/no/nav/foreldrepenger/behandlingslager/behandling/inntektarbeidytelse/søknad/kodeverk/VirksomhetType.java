package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "VirksomhetType")
@DiscriminatorValue(VirksomhetType.DISCRIMINATOR)
public class VirksomhetType extends Kodeliste {

    public static final String DISCRIMINATOR = "VIRKSOMHET_TYPE"; //$NON-NLS-1$
    public static final VirksomhetType UDEFINERT = new VirksomhetType("-"); //$NON-NLS-1$
    public static final VirksomhetType FISKE = new VirksomhetType("FISKE"); //$NON-NLS-1$
    public static final VirksomhetType DAGMAMMA = new VirksomhetType("DAGMAMMA"); //$NON-NLS-1$
    public static final VirksomhetType FRILANSER = new VirksomhetType("FRILANSER"); //$NON-NLS-1$
    public static final VirksomhetType JORDBRUK_SKOGBRUK = new VirksomhetType("JORDBRUK_SKOGBRUK"); //$NON-NLS-1$
    public static final VirksomhetType ANNEN = new VirksomhetType("ANNEN"); //$NON-NLS-1$

    private VirksomhetType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public VirksomhetType() {
        //hibernate
    }
}
