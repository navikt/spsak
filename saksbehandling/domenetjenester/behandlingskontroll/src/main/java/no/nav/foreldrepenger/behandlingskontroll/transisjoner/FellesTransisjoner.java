package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

public class FellesTransisjoner {
    public static final TransisjonIdentifikator UTFØRT = TransisjonIdentifikator.forId("utført");
    public static final TransisjonIdentifikator STARTET = TransisjonIdentifikator.forId("startet");
    public static final TransisjonIdentifikator HENLAGT = TransisjonIdentifikator.forId("henlagt");
    public static final TransisjonIdentifikator SETT_PÅ_VENT = TransisjonIdentifikator.forId("sett-på-vent");
    public static final TransisjonIdentifikator TILBAKEFØRT_TIL_AKSJONSPUNKT = TransisjonIdentifikator.forId("tilbakeført-til-aksjonspunkt");
    public static final TransisjonIdentifikator FREMHOPP_TIL_FATTE_VEDTAK = TransisjonIdentifikator.forId("fremhopp-til-fatte-vedtak");
    public static final TransisjonIdentifikator FREMHOPP_TIL_FORESLÅ_VEDTAK = TransisjonIdentifikator.forId("fremhopp-til-foreslå-vedtak");
    public static final TransisjonIdentifikator FREMHOPP_TIL_KONTROLLERER_SØKERS_OPPLYSNINGSPLIKT = TransisjonIdentifikator.forId("fremhopp-til-kontroller-søkers-opplysningsplikt");
    public static final TransisjonIdentifikator FREMHOPP_TIL_UTTAKSPLAN = TransisjonIdentifikator.forId("fremhopp-til-uttaksplan");
    public static final TransisjonIdentifikator FREMHOPP_TIL_IVERKSETT_VEDTAK = TransisjonIdentifikator.forId("fremhopp-til-kontroller-søkers-opplysningsplikt");

    private FellesTransisjoner() {
        //hindrer instansiering
    }

}
