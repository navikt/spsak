package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

public class ManuellRegistreringValidatorTekster {

    static final String PAAKREVD_FELT = "Påkrevd felt";
    static final String FREMTIDIG_DATO = "Må være frem i tid";
    static final String TIDLIGERE_DATO = "Må være bak i tid";
    static final String OVERLAPPENDE_PERIODER = "Periodene må ikke overlappe";
    static final String FØR_ELLER_LIK_DAGENS_DATO = "Må være før eller lik dagens dato";
    static final String STARTDATO_FØR_SLUTTDATO = "Startdato må være før sluttdato";
    static final String TERMINDATO_OG_FØDSELSDATO = "Ikke fyll ut både fødsel og termin";
    static final String TERMINDATO_ELLER_FØDSELSDATO = "Fyll ut enten fødsel eller termin";
    static final String LIKT_ANTALL_BARN_OG_FØDSELSDATOER = "Fødselsdatoer må fylles ut for alle barn";
    static final String OPPHOLDSDATO_IKKE_SATT = "Dato må fylles ut";
    static final String OPPHOLDSSKJEMA_TOMT = "Oppholdsland og datoer må fylles ut";
    static final String UGYLDIG_FØDSELSNUMMER = "Ugyldig Fødselsnummer";
    static final String MINDRE_ELLER_LIK_LENGDE = "Feltet må være mindre eller lik";
    static final String UGYLDIG_VERDI = "Ugyldig verdi";
    static final String TERMINDATO_TIDLIGST_TRE_UKER_TILBAKE_I_TID = "Termindato kan ikke være tidligere enn tre uker før dagens dato";
    static final String TERMINBEKREFTELSESDATO_FØR_TERMINDATO = "Terminbekreftelsesdato må være før termindato";

    private ManuellRegistreringValidatorTekster() {
        // Klassen skal ikke instansieres
    }
}
