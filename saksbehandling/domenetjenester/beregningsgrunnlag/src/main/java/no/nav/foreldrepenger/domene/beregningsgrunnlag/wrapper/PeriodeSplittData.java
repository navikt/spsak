package no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper;

import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.domene.typer.Beløp;

public class PeriodeSplittData {

    private PeriodeÅrsak periodeÅrsak;
    private Beløp refusjonsbeløp;
    private ArbeidsforholdRef arbeidsforholdRef;
    private String orgNr;
    private Inntektsmelding inntektsmelding;

    private PeriodeSplittData() {
        //privat constructor
    }

    public String getOrgNr() {
        return orgNr;
    }

    public PeriodeÅrsak getPeriodeÅrsak() {
        return periodeÅrsak;
    }

    public Optional<Beløp> getRefusjonsbeløp() {
        return Optional.ofNullable(refusjonsbeløp);
    }

    public Optional<ArbeidsforholdRef> getArbeidsforholdRef() {
        return Optional.ofNullable(arbeidsforholdRef);
    }

    public Inntektsmelding getInntektsmelding() {
        return inntektsmelding;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PeriodeSplittData kladd;

        private Builder() {
            kladd = new PeriodeSplittData();
        }

        public Builder medPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            kladd.periodeÅrsak = periodeÅrsak;
            return this;
        }

        public Builder medInformasjonFraInntektsmelding(Inntektsmelding inntektsmelding) {
            kladd.inntektsmelding = inntektsmelding;
            kladd.orgNr = inntektsmelding.getVirksomhet().getOrgnr();
            kladd.refusjonsbeløp = inntektsmelding.getRefusjonBeløpPerMnd();
            if (inntektsmelding.gjelderForEtSpesifiktArbeidsforhold()) {
                kladd.arbeidsforholdRef = inntektsmelding.getArbeidsforholdRef();
            }
            return this;
        }

        public Builder medInntektsmelding(Inntektsmelding inntektsmelding) {
            kladd.inntektsmelding = inntektsmelding;
            return this;
        }

        public Builder medOrgNr(String orgNr) {
            kladd.orgNr = orgNr;
            return this;
        }

        public Builder medRefusjonsBeløp(Beløp refusjonsbeløp) {
            kladd.refusjonsbeløp = refusjonsbeløp;
            return this;
        }

        public Builder medArbeidsforholdRef(ArbeidsforholdRef arbeidsforholdRef) {
            kladd.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public PeriodeSplittData build() {
            Objects.requireNonNull(kladd.periodeÅrsak);
            Objects.requireNonNull(kladd.inntektsmelding);
            return kladd;
        }
    }
}
