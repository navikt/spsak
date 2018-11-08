package no.nav.foreldrepenger.behandlingslager.hendelser;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "ForretningshendelseType")
@DiscriminatorValue(ForretningshendelseType.DISCRIMINATOR)
public class ForretningshendelseType extends Kodeliste {

    public static final String DISCRIMINATOR = "FORRETNINGSHENDELSE_TYPE";
    public static final ForretningshendelseType UDEFINERT = new ForretningshendelseType("-"); //$NON-NLS-1$
    public static final ForretningshendelseType INGEN_HENDELSE = new ForretningshendelseType("INGEN_HENDELSE"); //$NON-NLS-1$
    public static final ForretningshendelseType FØDSEL = new ForretningshendelseType("FØDSEL"); //$NON-NLS-1$
    public static final ForretningshendelseType YTELSE_INNVILGET = new ForretningshendelseType("YTELSE_INNVILGET"); //$NON-NLS-1$
    public static final ForretningshendelseType YTELSE_ENDRET = new ForretningshendelseType("YTELSE_ENDRET"); //$NON-NLS-1$
    public static final ForretningshendelseType YTELSE_OPPHØRT = new ForretningshendelseType("YTELSE_OPPHØRT"); //$NON-NLS-1$
    public static final ForretningshendelseType YTELSE_ANNULERT = new ForretningshendelseType("YTELSE_ANNULERT"); //$NON-NLS-1$

    @SuppressWarnings("unused")
    private ForretningshendelseType() {
        // Hibernate
    }

    public ForretningshendelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static boolean erYtelseHendelseType(ForretningshendelseType forretningshendelseType) {
        return YTELSE_INNVILGET.equals(forretningshendelseType)
            || YTELSE_ENDRET.equals(forretningshendelseType)
            || YTELSE_OPPHØRT.equals(forretningshendelseType)
            || YTELSE_ANNULERT.equals(forretningshendelseType);
    }
}
