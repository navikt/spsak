package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import java.time.LocalDate;
import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;

public class FrilansDto {

    private Boolean harSokerPeriodeMedFrilans;

    @Valid
    @Size(max = 50)
    private Collection<Frilansperiode> perioder;

    private Boolean erNyoppstartetFrilanser;
    private Boolean harInntektFraFosterhjem;
    private Boolean harHattOppdragForFamilie;
    private Collection<Oppdragperiode> oppdragPerioder;

    public Boolean getHarSokerPeriodeMedFrilans() {
        return harSokerPeriodeMedFrilans;
    }

    public void setHarSokerPeriodeMedFrilans(Boolean harSokerPeriodeMedFrilans) {
        this.harSokerPeriodeMedFrilans = harSokerPeriodeMedFrilans;
    }

    public Collection<Frilansperiode> getPerioder() {
        return perioder;
    }

    public void setPerioder(Collection<Frilansperiode> perioder) {
        this.perioder = perioder;
    }

    public Boolean getErNyoppstartetFrilanser() {
        return erNyoppstartetFrilanser;
    }

    public void setErNyoppstartetFrilanser(Boolean erNyoppstartetFrilanser) {
        this.erNyoppstartetFrilanser = erNyoppstartetFrilanser;
    }

    public Boolean getHarInntektFraFosterhjem() {
        return harInntektFraFosterhjem;
    }

    public void setHarInntektFraFosterhjem(Boolean harInntektFraFosterhjem) {
        this.harInntektFraFosterhjem = harInntektFraFosterhjem;
    }

    public Boolean getHarHattOppdragForFamilie() {
        return harHattOppdragForFamilie;
    }

    public void setHarHattOppdragForFamilie(Boolean harHattOppdragForFamilie) {
        this.harHattOppdragForFamilie = harHattOppdragForFamilie;
    }

    public Collection<Oppdragperiode> getOppdragPerioder() {
        return oppdragPerioder;
    }

    public void setOppdragPerioder(Collection<Oppdragperiode> oppdragPerioder) {
        this.oppdragPerioder = oppdragPerioder;
    }

    public static class Frilansperiode {
        @NotNull
        private LocalDate periodeFom;

        @NotNull
        private LocalDate periodeTom;

        public LocalDate getPeriodeFom() {
            return periodeFom;
        }

        public void setPeriodeFom(LocalDate periodeFom) {
            this.periodeFom = periodeFom;
        }

        public LocalDate getPeriodeTom() {
            return periodeTom;
        }

        public void setPeriodeTom(LocalDate periodeTom) {
            this.periodeTom = periodeTom;
        }
    }

    public static class Oppdragperiode {
        @Length(max = 50)
        private String oppdragsgiver;

        private LocalDate fomDato;
        private LocalDate tomDato;

        public String getOppdragsgiver() {
            return oppdragsgiver;
        }

        public void setOppdragsgiver(String oppdragsgiver) {
            this.oppdragsgiver = oppdragsgiver;
        }

        public LocalDate getFomDato() {
            return fomDato;
        }

        public void setFomDato(LocalDate fomDato) {
            this.fomDato = fomDato;
        }

        public LocalDate getTomDato() {
            return tomDato;
        }

        public void setTomDato(LocalDate tomDato) {
            this.tomDato = tomDato;
        }
    }
}
