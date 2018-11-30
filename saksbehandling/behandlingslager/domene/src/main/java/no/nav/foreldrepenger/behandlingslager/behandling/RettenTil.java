package no.nav.foreldrepenger.behandlingslager.behandling;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "RettenTil")
@DiscriminatorValue(RettenTil.DISCRIMINATOR)
public class RettenTil extends Kodeliste {

    public static final String DISCRIMINATOR = "RETTEN_TIL";

    public static final RettenTil HAR_RETT_TIL_FP = new RettenTil("HAR_RETT_TIL_FP"); //$NON-NLS-1$
    public static final RettenTil HAR_IKKE_RETT_TIL_FP = new RettenTil("HAR_IKKE_RETT_TIL_FP"); //$NON-NLS-1$

    public static final RettenTil UDEFINERT = new RettenTil("-"); //$NON-NLS-1$

    public RettenTil() {
        //
    }

    private RettenTil(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
