package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "OffentligYtelseType")
@DiscriminatorValue(OffentligYtelseType.DISCRIMINATOR)
public class OffentligYtelseType extends YtelseType {

    public static final String DISCRIMINATOR = "YTELSE_FRA_OFFENTLIGE"; //$NON-NLS-1$
    public static final OffentligYtelseType UDEFINERT = new OffentligYtelseType("-"); //$NON-NLS-1$

    private OffentligYtelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public OffentligYtelseType() {
        //hibernate
    }
}
