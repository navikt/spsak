package no.nav.foreldrepenger.behandlingslager.behandling.beregning;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "FaktaOmBeregningTilfelle")
@DiscriminatorValue(FaktaOmBeregningTilfelle.DISCRIMINATOR)
public class FaktaOmBeregningTilfelle extends Kodeliste {

    public static final String DISCRIMINATOR = "FAKTA_OM_BEREGNING_TILFELLE";

    public static final FaktaOmBeregningTilfelle VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD = new FaktaOmBeregningTilfelle("VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD"); //$NON-NLS-1$
    public static final FaktaOmBeregningTilfelle VURDER_SN_NY_I_ARBEIDSLIVET = new FaktaOmBeregningTilfelle("VURDER_SN_NY_I_ARBEIDSLIVET"); //$NON-NLS-1$
    public static final FaktaOmBeregningTilfelle VURDER_NYOPPSTARTET_FL = new FaktaOmBeregningTilfelle("VURDER_NYOPPSTARTET_FL"); //$NON-NLS-1$
    public static final FaktaOmBeregningTilfelle FASTSETT_MAANEDSINNTEKT_FL = new FaktaOmBeregningTilfelle("FASTSETT_MAANEDSINNTEKT_FL"); //$NON-NLS-1$
    public static final FaktaOmBeregningTilfelle FASTSETT_ENDRET_BEREGNINGSGRUNNLAG = new FaktaOmBeregningTilfelle("FASTSETT_ENDRET_BEREGNINGSGRUNNLAG"); //$NON-NLS-1$
    
    public static final FaktaOmBeregningTilfelle VURDER_LØNNSENDRING = new FaktaOmBeregningTilfelle("VURDER_LØNNSENDRING"); //$NON-NLS-1$
    public static final FaktaOmBeregningTilfelle FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING = new FaktaOmBeregningTilfelle("FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING"); //$NON-NLS-1$
    public static final FaktaOmBeregningTilfelle VURDER_AT_OG_FL_I_SAMME_ORGANISASJON = new FaktaOmBeregningTilfelle("VURDER_AT_OG_FL_I_SAMME_ORGANISASJON"); //$NON-NLS-1$
    public static final FaktaOmBeregningTilfelle TILSTØTENDE_YTELSE = new FaktaOmBeregningTilfelle("TILSTØTENDE_YTELSE"); //$NON-NLS-1$

    public static final FaktaOmBeregningTilfelle UDEFINERT = new FaktaOmBeregningTilfelle("-"); //$NON-NLS-1$

    FaktaOmBeregningTilfelle() {
        // Hibernate trenger en
    }

    private FaktaOmBeregningTilfelle(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
