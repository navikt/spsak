package no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatPeriode;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Inntektskategori;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;
import no.nav.spsak.tidsserie.LocalDateInterval;

@RuleDocumentationGrunnlag
public class BeregningsresultatFeriepengerRegelModell {
    private Set<Inntektskategori> inntektskategorier;
    private List<BeregningsresultatPeriode> beregningsresultatPerioder;
    private Dekningsgrad dekningsgrad;
    private LocalDateInterval feriepengerPeriode;

    private BeregningsresultatFeriepengerRegelModell() {
        //tom konstruktør
    }

    public Set<Inntektskategori> getInntektskategorier() {
        return inntektskategorier;
    }

    public List<BeregningsresultatPeriode> getBeregningsresultatPerioder() {
        return beregningsresultatPerioder;
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }

    public LocalDateInterval getFeriepengerPeriode() {
        return feriepengerPeriode;
    }

    public static Builder builder(BeregningsresultatFeriepengerRegelModell regelModell) {
        return new Builder(regelModell);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BeregningsresultatFeriepengerRegelModell kladd;

        private Builder(BeregningsresultatFeriepengerRegelModell regelModell) {
            kladd = regelModell;
        }

        public Builder() {
            this.kladd = new BeregningsresultatFeriepengerRegelModell();
        }

        public Builder medInntektskategorier(Set<Inntektskategori> inntektskategorier) {
            kladd.inntektskategorier = inntektskategorier;
            return this;
        }

        public Builder medBeregningsresultatPerioder(List<BeregningsresultatPeriode> beregningsresultatPerioder) {
            kladd.beregningsresultatPerioder = beregningsresultatPerioder;
            return this;
        }

        public Builder medDekningsgrad(Dekningsgrad dekningsgrad) {
            kladd.dekningsgrad = dekningsgrad;
            return this;
        }

        public Builder medFeriepengerPeriode(LocalDate feriepengePeriodeFom, LocalDate feriepengePeriodeTom) {
            kladd.feriepengerPeriode = new LocalDateInterval(feriepengePeriodeFom, feriepengePeriodeTom);
            return this;
        }

        public BeregningsresultatFeriepengerRegelModell build() {
            return kladd;
        }
    }
}
