package no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.FaktaOmBeregningTilfelle;

@Entity(name = "BeregningsgrunnlagFaktaOmBeregningTilfelle")
@Table(name = "BG_FAKTA_BER_TILFELLE")
public class BeregningsgrunnlagFaktaOmBeregningTilfelle extends BaseEntitet {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_FAKTA_BER_TILFELLE")
    private Long id;

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BEREGNINGSGRUNNLAG_ID", nullable = false, updatable = false, unique = true)
    private Beregningsgrunnlag beregningsgrunnlag;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "fakta_beregning_tilfelle", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'"
        + FaktaOmBeregningTilfelle.DISCRIMINATOR + "'"))
    private FaktaOmBeregningTilfelle faktaOmBeregningTilfelle = FaktaOmBeregningTilfelle.UDEFINERT;

    public Long getId() {
        return id;
    }

    public Beregningsgrunnlag getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }


    public FaktaOmBeregningTilfelle getFaktaOmBeregningTilfelle() {
        return faktaOmBeregningTilfelle;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BeregningsgrunnlagFaktaOmBeregningTilfelle)) {
            return false;
        }
        BeregningsgrunnlagFaktaOmBeregningTilfelle that = (BeregningsgrunnlagFaktaOmBeregningTilfelle) o;
        return Objects.equals(beregningsgrunnlag, that.beregningsgrunnlag) &&
            Objects.equals(faktaOmBeregningTilfelle, that.faktaOmBeregningTilfelle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlag, faktaOmBeregningTilfelle);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregningsgrunnlagFaktaOmBeregningTilfelle beregningsgrunnlagFaktaOmBeregningTilfelle;

        public Builder() {
            beregningsgrunnlagFaktaOmBeregningTilfelle = new BeregningsgrunnlagFaktaOmBeregningTilfelle();
        }

        BeregningsgrunnlagFaktaOmBeregningTilfelle.Builder medFaktaOmBeregningTilfelle(FaktaOmBeregningTilfelle tilfelle) {
            beregningsgrunnlagFaktaOmBeregningTilfelle.faktaOmBeregningTilfelle = tilfelle;
            return this;
        }

        public BeregningsgrunnlagFaktaOmBeregningTilfelle build(Beregningsgrunnlag beregningsgrunnlag) {
            beregningsgrunnlagFaktaOmBeregningTilfelle.beregningsgrunnlag = beregningsgrunnlag;
            return beregningsgrunnlagFaktaOmBeregningTilfelle;
        }
    }
}
