package no.nav.foreldrepenger.uttaksvilkår;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class StønadskontoResultat {

    private Map<Stønadskontotype, Integer> stønadskontoer;

    private String evalueringResultat;

    private String innsendtGrunnlag;

    private Integer antallFlerbarnsdager;

    public StønadskontoResultat(Map<Stønadskontotype, Integer> stønadskontoer, Integer antallFlerbarnsdager, String evalueringResultat, String innsendtGrunnlag) {
        Objects.requireNonNull(stønadskontoer);
        Objects.requireNonNull(evalueringResultat);
        Objects.requireNonNull(innsendtGrunnlag);
        this.stønadskontoer = stønadskontoer;
        this.antallFlerbarnsdager = antallFlerbarnsdager;
        this.evalueringResultat = evalueringResultat;
        this.innsendtGrunnlag = innsendtGrunnlag;
    }

    public Map<Stønadskontotype, Integer> getStønadskontoer() {
        return Collections.unmodifiableMap(stønadskontoer);
    }

    public String getEvalueringResultat() {
        return evalueringResultat;
    }

    public String getInnsendtGrunnlag() {
        return innsendtGrunnlag;
    }

    public Integer getAntallFlerbarnsdager() {
        return antallFlerbarnsdager;
    }
}
