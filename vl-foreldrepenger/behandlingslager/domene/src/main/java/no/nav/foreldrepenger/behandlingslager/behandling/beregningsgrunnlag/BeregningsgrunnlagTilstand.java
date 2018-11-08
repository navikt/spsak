package no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BeregningsgrunnlagTilstand")
@DiscriminatorValue(BeregningsgrunnlagTilstand.DISCRIMINATOR)
public class BeregningsgrunnlagTilstand extends Kodeliste {

    public static final String DISCRIMINATOR = "BEREGNINGSGRUNNLAG_TILSTAND";

    public static final BeregningsgrunnlagTilstand OPPRETTET = new BeregningsgrunnlagTilstand("OPPRETTET"); //$NON-NLS-1$
    public static final BeregningsgrunnlagTilstand FORESLÅTT = new BeregningsgrunnlagTilstand("FORESLÅTT"); //$NON-NLS-1$
    public static final BeregningsgrunnlagTilstand FASTSATT = new BeregningsgrunnlagTilstand("FASTSATT"); //$NON-NLS-1$
    public static final BeregningsgrunnlagTilstand KOFAKBER_UT = new BeregningsgrunnlagTilstand("KOFAKBER_UT"); //$NON-NLS-1$
    public static final BeregningsgrunnlagTilstand FASTSATT_INN = new BeregningsgrunnlagTilstand("FASTSATT_INN"); //$NON-NLS-1$

    public static final BeregningsgrunnlagTilstand UDEFINERT = new BeregningsgrunnlagTilstand("-"); //$NON-NLS-1$

    BeregningsgrunnlagTilstand() {
        // Hibernate trenger en
    }

    private BeregningsgrunnlagTilstand(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
