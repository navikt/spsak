package no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BeregningsgrunnlagAndeltype")
@DiscriminatorValue(BeregningsgrunnlagAndeltype.DISCRIMINATOR)
public class BeregningsgrunnlagAndeltype extends Kodeliste {

    public static final String DISCRIMINATOR = "BEREGNINGSGRUNNLAG_ANDELTYPE";

    public static final BeregningsgrunnlagAndeltype  BRUKERS_ANDEL = new BeregningsgrunnlagAndeltype("BRUKERS_ANDEL"); //$NON-NLS-1$
    public static final BeregningsgrunnlagAndeltype  EGEN_NÆRING = new BeregningsgrunnlagAndeltype("EGEN_NÆRING"); //$NON-NLS-1$
    public static final BeregningsgrunnlagAndeltype  FRILANS = new BeregningsgrunnlagAndeltype("FRILANS"); //$NON-NLS-1$

    public static final BeregningsgrunnlagAndeltype UDEFINERT = new BeregningsgrunnlagAndeltype("-"); //$NON-NLS-1$

    BeregningsgrunnlagAndeltype() {
        // Hibernate trenger en
    }

    private BeregningsgrunnlagAndeltype(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
