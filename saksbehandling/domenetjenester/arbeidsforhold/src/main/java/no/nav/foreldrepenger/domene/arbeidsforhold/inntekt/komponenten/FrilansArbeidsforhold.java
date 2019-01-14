package no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Arbeidsgiver;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Organisasjon;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Person;
import no.nav.foreldrepenger.domene.typer.AktørId;

public class FrilansArbeidsforhold {

    private ArbeidsforholdRef arbeidsforholdId;
    private LocalDate fom;
    private LocalDate tom;
    private AktørId arbeidsgiverAktørId;
    private String arbeidsgiverOrgnr;
    private ArbeidType arbeidsforholdType;
    private BigDecimal stillingsprosent;
    private BigDecimal beregnetAntallTimerPrUke;
    private BigDecimal avtaltArbeidstimerPerUke;
    private LocalDate sistEndringIStillingsprosent;
    private LocalDate sistEndringILønn;

    FrilansArbeidsforhold() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public BigDecimal getStillingsprosent() {
        return stillingsprosent;
    }

    void setStillingsprosent(BigDecimal stillingsprosent) {
        this.stillingsprosent = stillingsprosent;
    }

    public BigDecimal getBeregnetAntallTimerPrUke() {
        return beregnetAntallTimerPrUke;
    }

    void setBeregnetAntallTimerPrUke(BigDecimal beregnetAntallTimerPrUke) {
        this.beregnetAntallTimerPrUke = beregnetAntallTimerPrUke;
    }

    public BigDecimal getAvtaltArbeidstimerPerUke() {
        return avtaltArbeidstimerPerUke;
    }

    void setAvtaltArbeidstimerPerUke(BigDecimal avtaltArbeidstimerPerUke) {
        this.avtaltArbeidstimerPerUke = avtaltArbeidstimerPerUke;
    }

    public ArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdId;
    }

    void setArbeidsforholdRef(ArbeidsforholdRef arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public boolean harArbeidsforholdRef() {
        return arbeidsforholdId != null;
    }

    public LocalDate getFom() {
        return fom;
    }

    void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public AktørId getArbeidsgiverAktørId() {
        return arbeidsgiverAktørId;
    }

    void setArbeidsgiverAktørId(AktørId arbeidsgiverAktørId) {
        this.arbeidsgiverAktørId = arbeidsgiverAktørId;
    }

    public String getArbeidsgiverOrgnr() {
        return arbeidsgiverOrgnr;
    }

    void setArbeidsgiverOrgnr(String arbeidsgiverOrgnr) {
        this.arbeidsgiverOrgnr = arbeidsgiverOrgnr;
    }

    public ArbeidType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    void setArbeidsforholdType(ArbeidType arbeidsforholdType) {
        this.arbeidsforholdType = arbeidsforholdType;
    }

    public LocalDate getSistEndringIStillingsprosent() {
        return sistEndringIStillingsprosent;
    }

    void setSistEndringIStillingsprosent(LocalDate sistEndringIStillingsprosent) {
        this.sistEndringIStillingsprosent = sistEndringIStillingsprosent;
    }

    public LocalDate getSistEndringILønn() {
        return sistEndringILønn;
    }

    void setSistEndringILønn(LocalDate sistEndringILønn) {
        this.sistEndringILønn = sistEndringILønn;
    }

    public ArbeidsforholdIdentifikator getIdentifikator() {
        return new ArbeidsforholdIdentifikator(tilArbeidsgiver(), arbeidsforholdId, arbeidsforholdType.getKode());
    }

    private Arbeidsgiver tilArbeidsgiver() {
        if (arbeidsgiverAktørId != null) {
            return new Person(arbeidsgiverAktørId);
        } else if (arbeidsgiverOrgnr != null && !arbeidsgiverOrgnr.isEmpty()) {
            return new Organisasjon(arbeidsgiverOrgnr);
        }
        return null;
    }

    public static class Builder {

        private FrilansArbeidsforhold kladd = new FrilansArbeidsforhold();

        private Builder() {

        }

        public Builder medArbeidsforholdId(String id) {
            kladd.setArbeidsforholdRef(ArbeidsforholdRef.ref(id));
            return this;
        }

        public Builder medFom(LocalDate fom) {
            kladd.setFom(fom);
            return this;
        }

        public Builder medTom(LocalDate tom) {
            kladd.setTom(tom);
            return this;
        }

        public Builder medType(ArbeidType type) {
            kladd.setArbeidsforholdType(type);
            return this;
        }

        public Builder medArbeidsgiverOrgnr(String orgnr) {
            kladd.setArbeidsgiverOrgnr(orgnr);
            return this;
        }

        public Builder medArbeidsgiverAktørId(AktørId aktørId) {
            kladd.setArbeidsgiverAktørId(aktørId);
            return this;
        }

        public Builder medSisteEndringIStillingsprosent(LocalDate endringsDato) {
            kladd.setSistEndringIStillingsprosent(endringsDato);
            return this;
        }

        public Builder medSisteEndringILønn(LocalDate endringsDato) {
            kladd.setSistEndringILønn(endringsDato);
            return this;
        }

        public Builder medStillingsprosent(BigDecimal stillingsprosent) {
            kladd.setStillingsprosent(stillingsprosent);
            return this;
        }

        public Builder medBeregnetAntallTimerPerUke(BigDecimal antallTimerPerUke) {
            kladd.setBeregnetAntallTimerPrUke(antallTimerPerUke);
            return this;
        }

        public Builder medAvtaltAntallTimerPerUke(BigDecimal avtaltArbeidstimerPerUke) {
            kladd.setAvtaltArbeidstimerPerUke(avtaltArbeidstimerPerUke);
            return this;
        }

        public FrilansArbeidsforhold build() {
            return kladd;
        }
    }
}
