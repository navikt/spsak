package no.nav.foreldrepenger.regler.søknadsfrist.grunnlag;

import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

import java.time.LocalDate;
import java.util.Objects;

@RuleDocumentationGrunnlag
public class SøknadsfristGrunnlag {

    private LocalDate søknadMottattDato;
    private LocalDate førsteUttaksdato;
    private Boolean erSøknadOmUttak;
    private Integer antallMånederSøknadsfrist;

    public LocalDate getFørsteUttaksdato() {
        return førsteUttaksdato;
    }

    public boolean isErSøknadOmUttak() {
        return erSøknadOmUttak;
    }

    public LocalDate getFørsteLovligeUttaksdato() {
        return søknadMottattDato.withDayOfMonth(1).minusMonths(antallMånederSøknadsfrist);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        SøknadsfristGrunnlag kladd = new SøknadsfristGrunnlag();

        public Builder medSøknadMottattDato(LocalDate søknadMottattDato) {
            kladd.søknadMottattDato = søknadMottattDato;
            return this;
        }

        public Builder medFørsteUttaksdato(LocalDate uttaksdato) {
            kladd.førsteUttaksdato = uttaksdato;
            return this;
        }

        public Builder medErSøknadOmUttak(boolean erSøknadOmUttak) {
            kladd.erSøknadOmUttak = erSøknadOmUttak;
            return this;
        }

        public Builder medAntallMånederSøknadsfrist(int antallMånederSøknadsfrist) {
            kladd.antallMånederSøknadsfrist = antallMånederSøknadsfrist;
            return this;
        }

        public SøknadsfristGrunnlag build() {
            Objects.requireNonNull(kladd.antallMånederSøknadsfrist);
            Objects.requireNonNull(kladd.søknadMottattDato);
            Objects.requireNonNull(kladd.erSøknadOmUttak);
            if (kladd.erSøknadOmUttak) {
                Objects.requireNonNull(kladd.førsteUttaksdato);
            }
            return kladd;
        }
    }
}
