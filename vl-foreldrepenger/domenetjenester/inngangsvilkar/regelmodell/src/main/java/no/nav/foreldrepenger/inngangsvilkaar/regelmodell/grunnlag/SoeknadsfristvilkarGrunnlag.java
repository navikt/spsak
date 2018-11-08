package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag;

import java.time.LocalDate;

import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class SoeknadsfristvilkarGrunnlag implements VilkårGrunnlag {

    /**
     * Om søknaden er levert elektronisk
     */
    private boolean elektroniskSoeknad;
    /**
     * Dato for skjæringstidspunkt
     */
    private LocalDate skjaeringstidspunkt;
    /**
     * Dato for når søknad ble mottatt
     */
    private LocalDate soeknadMottatDato;

    public SoeknadsfristvilkarGrunnlag() {
    }

    public SoeknadsfristvilkarGrunnlag(boolean elektroniskSoeknad, LocalDate skjaeringstidspunkt, LocalDate soeknadMottatDato) {
        this.elektroniskSoeknad = elektroniskSoeknad;
        this.skjaeringstidspunkt = skjaeringstidspunkt;
        this.soeknadMottatDato = soeknadMottatDato;
    }

    public boolean isElektroniskSoeknad() {
        return elektroniskSoeknad;
    }

    public LocalDate getSkjaeringstidspunkt() {
        return skjaeringstidspunkt;
    }

    public LocalDate getSoeknadMottatDato() {
        return soeknadMottatDato;
    }
}
