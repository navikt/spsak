package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "PensjonTrygdType")
@DiscriminatorValue(PensjonTrygdType.DISCRIMINATOR)
public class PensjonTrygdType extends YtelseType {

    public static final String DISCRIMINATOR = "PENSJON_TRYGD_BESKRIVELSE"; //$NON-NLS-1$
    public static final PensjonTrygdType UDEFINERT = new PensjonTrygdType("-"); //$NON-NLS-1$

    public static final PensjonTrygdType BIL = new PensjonTrygdType("BIL"); //$NON-NLS-1$

    private PensjonTrygdType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public PensjonTrygdType() {
        //hibernate
    }
}
