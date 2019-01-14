package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;

public class Arbeidsforhold {
    private Arbeidsgiver arbeidsgiver;
    private String type;
    private LocalDate arbeidFom;
    private LocalDate arbeidTom;
    private List<Arbeidsavtale> arbeidsavtaler;
    private List<Permisjon> permisjoner;
    private ArbeidsforholdRef arbeidsforholdId;

    private Arbeidsforhold(Arbeidsgiver arbeidsgiver, String type, LocalDate arbeidFom, LocalDate arbeidTom,
                           List<Arbeidsavtale> arbeidsavtaler, List<Permisjon> permisjoner, ArbeidsforholdRef arbeidsforholdId) {
        this.arbeidsgiver = arbeidsgiver;
        this.type = type;
        this.arbeidFom = arbeidFom;
        this.arbeidTom = arbeidTom;
        this.arbeidsavtaler = arbeidsavtaler;
        this.permisjoner = permisjoner;
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public String getType() {
        return type;
    }

    public LocalDate getArbeidFom() {
        return arbeidFom;
    }

    public LocalDate getArbeidTom() {
        return arbeidTom;
    }

    public List<Arbeidsavtale> getArbeidsavtaler() {
        return arbeidsavtaler;
    }

    public List<Permisjon> getPermisjoner() {
        return permisjoner;
    }

    public ArbeidsforholdRef getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public ArbeidsforholdIdentifikator getIdentifikator() {
        return new ArbeidsforholdIdentifikator(arbeidsgiver, arbeidsforholdId, type);
    }

    public static class Builder {
        private Arbeidsgiver arbeidsgiver;
        private String type;
        private LocalDate arbeidFom;
        private LocalDate arbeidTom;
        private List<Arbeidsavtale> arbeidsavtaler = new ArrayList<>();
        private List<Permisjon> permisjoner = new ArrayList<>();
        private ArbeidsforholdRef arbeidsforholdId;


        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            this.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medType(String type) {
            this.type = type;
            return this;
        }

        public Builder medArbeidsforholdId(String arbeidsforholdId) {
            this.arbeidsforholdId = ArbeidsforholdRef.ref(arbeidsforholdId);
            return this;
        }

        public Builder medArbeidFom(LocalDate arbeidFom) {
            this.arbeidFom = arbeidFom;
            return this;
        }

        public Builder medArbeidTom(LocalDate arbeidTom) {
            this.arbeidTom = arbeidTom;
            return this;
        }

        public Builder medArbeidsavtaler(List<Arbeidsavtale> arbeidsavtaler) {
            this.arbeidsavtaler = arbeidsavtaler;
            return this;
        }


        public Builder medAnsettelsesPeriode(Arbeidsavtale avtale) {
            if(this.arbeidsavtaler.isEmpty()) {
                this.arbeidsavtaler = new ArrayList<>();
            }
            this.arbeidsavtaler.add(avtale);
            return this;
        }

        public Builder medPermisjon(List<Permisjon> permisjoner) {
            this.permisjoner = permisjoner;
            return this;
        }

        public Arbeidsforhold build() {
            return new Arbeidsforhold(arbeidsgiver, type, arbeidFom, arbeidTom, arbeidsavtaler, permisjoner, arbeidsforholdId);
        }
    }
}
