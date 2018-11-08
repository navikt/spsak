package no.nav.foreldrepenger.uttaksvilkår;

import java.time.LocalDate;
import java.util.Optional;

public class SøknadsfristResultat {
    private LocalDate tidligsteLovligeUttak;
    private boolean regelOppfylt;
    private String årsakKodeIkkeVurdert;
    private final String evalueringResultat;
    private final String innsendtGrunnlag;


    SøknadsfristResultat(String evalueringResultat, String innsendtGrunnlag) {
        this.evalueringResultat = evalueringResultat;
        this.innsendtGrunnlag = innsendtGrunnlag;
    }

    public LocalDate getTidligsteLovligeUttak() {
        return tidligsteLovligeUttak;
    }

    public boolean isRegelOppfylt() {
        return regelOppfylt;
    }

    public Optional<String> getÅrsakKodeIkkeVurdert() {
        return Optional.ofNullable(årsakKodeIkkeVurdert);
    }

    public String getEvalueringResultat() {
        return evalueringResultat;
    }

    public String getInnsendtGrunnlag() {
        return innsendtGrunnlag;
    }

    static class Builder {
        SøknadsfristResultat kladd;

        Builder(String evalueringResultat, String innsendtGrunnlag) {
            kladd = new SøknadsfristResultat(evalueringResultat, innsendtGrunnlag);
        }

        Builder medTidligsteLovligeUttak(LocalDate tidligsteLovligeUttak) {
            kladd.tidligsteLovligeUttak = tidligsteLovligeUttak;
            return this;
        }

        Builder medSøknadsfristOppfylt() {
            kladd.regelOppfylt = true;
            return this;
        }

        Builder medSøknadsfristIkkeOppfylt(String årsakKode) {
            kladd.regelOppfylt = false;
            kladd.årsakKodeIkkeVurdert = årsakKode;
            return this;
        }

        SøknadsfristResultat build() {
            return kladd;
        }
    }

}
