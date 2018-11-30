package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "InntektPeriodeType")
@DiscriminatorValue(InntektPeriodeType.DISCRIMINATOR)
public class InntektPeriodeType extends Kodeliste {

    public static final String DISCRIMINATOR = "INNTEKT_PERIODE_TYPE";
    public static final InntektPeriodeType DAGLIG = new InntektPeriodeType("DAGLG"); //$NON-NLS-1$
    public static final InntektPeriodeType UKENTLIG = new InntektPeriodeType("UKNLG"); //$NON-NLS-1$
    public static final InntektPeriodeType BIUKENTLIG = new InntektPeriodeType("14DLG"); //$NON-NLS-1$
    public static final InntektPeriodeType MÅNEDLIG = new InntektPeriodeType("MNDLG"); //$NON-NLS-1$
    public static final InntektPeriodeType ÅRLIG = new InntektPeriodeType("AARLG"); //$NON-NLS-1$
    public static final InntektPeriodeType FASTSATT25PAVVIK = new InntektPeriodeType("INNFS"); //$NON-NLS-1$
    public static final InntektPeriodeType PREMIEGRUNNLAG = new InntektPeriodeType("PREMGR"); //$NON-NLS-1$
    public static final InntektPeriodeType UDEFINERT = new InntektPeriodeType("-"); //$NON-NLS-1$

    InntektPeriodeType() {
        // Hibernate trenger den
    }

    private InntektPeriodeType(String kode) {
        super(kode, DISCRIMINATOR);
    }


    public String getPeriode() {
        String periode = getJsonField("periode"); //$NON-NLS-1$
        return periode;
    }
}
