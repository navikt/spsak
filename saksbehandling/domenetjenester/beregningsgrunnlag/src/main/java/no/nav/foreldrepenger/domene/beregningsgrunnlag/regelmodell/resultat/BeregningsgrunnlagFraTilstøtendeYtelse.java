package no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.Grunnbeløp;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.TilstøtendeYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class BeregningsgrunnlagFraTilstøtendeYtelse {
    private TilstøtendeYtelse tilstøtendeYtelse;
    private List<BeregningsgrunnlagAndelTilstøtendeYtelse> beregningsgrunnlagAndelList = new ArrayList<>();
    private BigDecimal grunnbeløp;
    private BigDecimal redusertGrunnbeløp;
    private List<Grunnbeløp> grunnbeløpSatser = new ArrayList<>();

    private BeregningsgrunnlagFraTilstøtendeYtelse() {
    }

    public TilstøtendeYtelse getTilstøtendeYtelse() {
        return tilstøtendeYtelse;
    }

    public List<BeregningsgrunnlagAndelTilstøtendeYtelse> getBeregningsgrunnlagAndeler() {
        return Collections.unmodifiableList(beregningsgrunnlagAndelList);
    }

    public BigDecimal getGrunnbeløp() {
        return grunnbeløp;
    }

    public BigDecimal getRedusertGrunnbeløp() {
        return redusertGrunnbeløp;
    }

    public long verdiAvG(LocalDate dato) {
        Optional<Grunnbeløp> optional = grunnbeløpSatser.stream()
            .filter(g -> !dato.isBefore(g.getFom()) && !dato.isAfter(g.getTom()))
            .findFirst();

        if (optional.isPresent()) {
            return optional.get().getGVerdi();
        } else {
            throw new IllegalArgumentException("Kjenner ikke G-verdi for året " + dato.getYear());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagFraTilstøtendeYtelse original) {
        return new Builder(original);
    }

    public static class Builder {
        private BeregningsgrunnlagFraTilstøtendeYtelse kladd;

        private Builder() {
            this(new BeregningsgrunnlagFraTilstøtendeYtelse());
        }

        public Builder(BeregningsgrunnlagFraTilstøtendeYtelse original) {
            kladd = original;
        }

        public Builder medYtelse(TilstøtendeYtelse tilstøtendeYtelse) {
            kladd.tilstøtendeYtelse = tilstøtendeYtelse;
            return this;
        }

        public Builder medGrunnbeløp(BigDecimal grunnbeløp) {
            kladd.grunnbeløp = grunnbeløp;
            return this;
        }

        public Builder medRedusertGrunnbeløp(BigDecimal redusertGrunnbeløp) {
            kladd.redusertGrunnbeløp = redusertGrunnbeløp;
            return this;
        }

        public Builder leggTilBeregningsgrunnlagAndel(BeregningsgrunnlagAndelTilstøtendeYtelse beregningsgrunnlagAndel) {
            kladd.beregningsgrunnlagAndelList.add(beregningsgrunnlagAndel);
            return this;
        }

        public Builder medGrunnbeløpSatser(List<Grunnbeløp> grunnbeløpSatser) {
            kladd.grunnbeløpSatser.clear();
            kladd.grunnbeløpSatser.addAll(grunnbeløpSatser);
            return this;
        }

        public BeregningsgrunnlagFraTilstøtendeYtelse build() {
            if (kladd.grunnbeløpSatser.isEmpty()) {
                throw new IllegalStateException("Grunnbeløpsatser må legges inn i regelmodell");
            }
            return kladd;
        }
    }
}
