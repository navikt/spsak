package no.nav.foreldrepenger.behandlingslager.fagsak;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "FagsakYtelseType")
@DiscriminatorValue(FagsakYtelseType.DISCRIMINATOR)
public class FagsakYtelseType extends Kodeliste {

    public static final String DISCRIMINATOR = "FAGSAK_YTELSE"; //$NON-NLS-1$
    public static final FagsakYtelseType FORELDREPENGER = new FagsakYtelseType("FP"); //$NON-NLS-1$

    public static final FagsakYtelseType UDEFINERT = new FagsakYtelseType("-"); //$NON-NLS-1$

    FagsakYtelseType() {
        // Hibernate trenger den
    }

    public FagsakYtelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public final boolean gjelderForeldrepenger() {
        return FORELDREPENGER.getKode().equals(this.getKode());
    }

}
