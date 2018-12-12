package no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag;

import java.time.LocalDate;

import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class SoeknadsfristvilkarGrunnlag implements VilkårGrunnlag {

    /**
     * Dato for skjæringstidspunkt
     */
    private LocalDate førsteSykedagISøknaden;
    /**
     * Dato for når søknad ble mottatt
     */
    private LocalDate soeknadMottatDato;

    public SoeknadsfristvilkarGrunnlag() {
    }

    public SoeknadsfristvilkarGrunnlag(LocalDate førsteSykedagISøknaden, LocalDate mottattDato) {
        this.førsteSykedagISøknaden = førsteSykedagISøknaden;
        this.soeknadMottatDato = mottattDato;
    }

    public LocalDate getFørsteSykedagISøknaden() {
        return førsteSykedagISøknaden;
    }

    public LocalDate getSoeknadMottatDato() {
        return soeknadMottatDato;
    }
}
