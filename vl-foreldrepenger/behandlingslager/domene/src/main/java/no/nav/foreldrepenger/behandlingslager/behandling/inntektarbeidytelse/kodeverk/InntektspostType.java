package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "InntektspostType")
@DiscriminatorValue(InntektspostType.DISCRIMINATOR)
public class InntektspostType extends Kodeliste{
    public static final String DISCRIMINATOR = "INNTEKTSPOST_TYPE"; //$NON-NLS-1$

    public static final InntektspostType UDEFINERT = new InntektspostType("-"); //$NON-NLS-1$
    public static final InntektspostType LØNN = new InntektspostType("LØNN"); //$NON-NLS-1$
    public static final InntektspostType YTELSE = new InntektspostType("YTELSE"); //$NON-NLS-1$
    public static final InntektspostType SELVSTENDIG_NÆRINGSDRIVENDE = new InntektspostType("SELVSTENDIG_NÆRINGSDRIVENDE");//$NON-NLS-1$
    public static final InntektspostType NÆRING_FISKE_FANGST_FAMBARNEHAGE = new InntektspostType("NÆRING_FISKE_FANGST_FAMBARNEHAGE");//$NON-NLS-1$

    private InntektspostType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public InntektspostType() {
        //hibernate
    }
}
