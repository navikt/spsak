package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.UtsettelsePeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility=Visibility.ANY)
public class UtsettelsePeriodeDto {
    
    @JsonProperty("fom")
    private LocalDate fom;
    
    @JsonProperty("tom")
    private LocalDate tom;
    
    @JsonProperty("utsettelseArsak")
    private UtsettelseÅrsak utsettelseÅrsak;

    public UtsettelsePeriodeDto(UtsettelsePeriode periode) {
        this.fom = periode.getPeriode().getFomDato();
        this.tom = periode.getPeriode().getTomDato();
        this.utsettelseÅrsak = periode.getÅrsak();
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public UtsettelseÅrsak getUtsettelseArsak() {
        return utsettelseÅrsak;
    }
}
