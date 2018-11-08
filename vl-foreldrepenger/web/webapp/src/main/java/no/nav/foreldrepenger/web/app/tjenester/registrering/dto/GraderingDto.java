package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

public class GraderingDto {

    @NotNull
    private LocalDate periodeFom;

    @NotNull
    private LocalDate periodeTom;

    @Pattern(regexp = "[\\d]{9}")
    private String orgNr;

    @NotNull
    @ValidKodeverk
    private UttakPeriodeType periodeForGradering;

    @DecimalMax("100.00")
    @DecimalMin("0.00")
    @Digits(integer = 3, fraction = 2)
    @JsonDeserialize(using = BigDecimalSerializer.class)
    private BigDecimal prosentandelArbeid;

    private Boolean skalGraderes;
    private boolean erArbeidstaker;
    private boolean harSamtidigUttak;

    @DecimalMax("200.00")
    @DecimalMin("0.00")
    @Digits(integer = 3, fraction = 2)
    @JsonDeserialize(using = BigDecimalSerializer.class)
    private BigDecimal samtidigUttaksprosent;

    private boolean flerbarnsdager;

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

    public String getOrgNr() {
        return orgNr;
    }

    public void setOrgNr(String orgNr) {
        this.orgNr = orgNr;
    }

    public UttakPeriodeType getPeriodeForGradering() {
        return periodeForGradering;
    }

    public void setPeriodeForGradering(UttakPeriodeType periodeForGradering) {
        this.periodeForGradering = periodeForGradering;
    }

    public BigDecimal getProsentandelArbeid() {
        return prosentandelArbeid;
    }

    public void setProsentandelArbeid(BigDecimal prosentandelArbeid) {
        this.prosentandelArbeid = prosentandelArbeid;
    }

    public Boolean getSkalGraderes() {
        return skalGraderes;
    }

    public void setSkalGraderes(Boolean skalGraderes) {
        this.skalGraderes = skalGraderes;
    }

    public boolean isErArbeidstaker() {
        return erArbeidstaker;
    }

    public void setErArbeidstaker(boolean erArbeidstaker) {
        this.erArbeidstaker = erArbeidstaker;
    }

    public boolean getHarSamtidigUttak() {
        return harSamtidigUttak;
    }

    public void setHarSamtidigUttak(boolean harSamtidigUttak) {
        this.harSamtidigUttak = harSamtidigUttak;
    }

    public BigDecimal getSamtidigUttaksprosent() {
        return samtidigUttaksprosent;
    }

    public void setSamtidigUttaksprosent(BigDecimal samtidigUttaksprosent) {
        this.samtidigUttaksprosent = samtidigUttaksprosent;
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    public void setFlerbarnsdager(boolean flerbarnsdager) {
        this.flerbarnsdager = flerbarnsdager;
    }

    public static class BigDecimalSerializer extends JsonDeserializer<BigDecimal> {

        public BigDecimalSerializer() {
        }

        @Override
        public BigDecimal deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
            ObjectCodec oc = jp.getCodec();
            JsonNode node = oc.readTree(jp);
            return new BigDecimal(node.asText().replace(',', '.'));
        }
    }


}
