package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "InntektsFilter")
@DiscriminatorValue(InntektsFilter.DISCRIMINATOR)
public class InntektsFilter extends Kodeliste {

    public static final String DISCRIMINATOR = "INNTEKTS_FILTER"; //$NON-NLS-1$

    public static final InntektsFilter UDEFINERT = new InntektsFilter("-"); //$NON-NLS-1$
    public static final InntektsFilter OPPTJENINGSGRUNNLAG = new InntektsFilter("OPPTJENINGSGRUNNLAG"); //$NON-NLS-1$
    public static final InntektsFilter BEREGNINGSGRUNNLAG = new InntektsFilter("BEREGNINGSGRUNNLAG"); //$NON-NLS-1$
    public static final InntektsFilter SAMMENLIGNINGSGRUNNLAG = new InntektsFilter("SAMMENLIGNINGSGRUNNLAG"); //$NON-NLS-1$

    private InntektsFilter(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public InntektsFilter() {
        //hibernate
    }
}
