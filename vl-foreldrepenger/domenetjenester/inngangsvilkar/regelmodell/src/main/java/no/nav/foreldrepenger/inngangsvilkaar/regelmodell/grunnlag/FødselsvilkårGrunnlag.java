package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.Kjoenn;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.SoekerRolle;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class FødselsvilkårGrunnlag implements VilkårGrunnlag {

    /**
     * Søkers kjønn
     */
    private Kjoenn soekersKjonn;
    /**
     * Bekreftete fødselsdato
     */
    private LocalDate bekreftetFoedselsdato;
    /**
     * Antall barn ...
     */
    private int antallBarn;
    /**
     * Bekreftete termindato
     */
    private LocalDate bekreftetTermindato;
    /**
     * Mor eller far
     */
    private SoekerRolle soekerRolle;
    /**
     * Søknadsdato
     */
    private LocalDate soeknadsdato;

    private boolean erMorForSykVedFødsel;

    /**
     * Søknad gjelder termin
     */
    private boolean erSøktOmTermin;

    public FødselsvilkårGrunnlag() {
    }

    public FødselsvilkårGrunnlag(Kjoenn soekersKjonn, SoekerRolle soekerRolle, LocalDate soeknadsdato,
                                 boolean erMorForSykVedFødsel, boolean erSøktOmTermin) {
        this.soekersKjonn = soekersKjonn;
        this.soekerRolle = soekerRolle;
        this.soeknadsdato = soeknadsdato;
        this.erMorForSykVedFødsel = erMorForSykVedFødsel;
        this.erSøktOmTermin = erSøktOmTermin;
    }

    public Kjoenn getSoekersKjonn() {
        return soekersKjonn;
    }

    public LocalDate getBekreftetFoedselsdato() {
        return bekreftetFoedselsdato;
    }

    public LocalDate getBekreftetTermindato() {
        return bekreftetTermindato;
    }

    public SoekerRolle getSoekerRolle() {
        return soekerRolle;
    }

    public LocalDate getSoeknadsdato() {
        return soeknadsdato;
    }

    public void setBekreftetFoedselsdato(LocalDate bekreftetFoedselsdato) {
        this.bekreftetFoedselsdato = bekreftetFoedselsdato;
    }

    public void setBekreftetTermindato(LocalDate bekreftetTermindato) {
        this.bekreftetTermindato = bekreftetTermindato;
    }

    public int getAntallBarn() {
        return antallBarn;
    }

    public void setAntallBarn(int antallBarn) {
        this.antallBarn = antallBarn;
    }

    public boolean isErMorForSykVedFødsel() {
        return erMorForSykVedFødsel;
    }

    public boolean isErSøktOmTermin() {
        return erSøktOmTermin;
    }
}
