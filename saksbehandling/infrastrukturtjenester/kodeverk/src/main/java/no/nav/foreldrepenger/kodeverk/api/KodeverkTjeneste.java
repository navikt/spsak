package no.nav.foreldrepenger.kodeverk.api;

import java.util.List;
import java.util.Map;

public interface KodeverkTjeneste {

    List<KodeverkInfo> hentGjeldendeKodeverkListe();

    Map<String, KodeverkKode> hentKodeverk(String kodeverkNavn, String kodeverkVersjon, String kodeverkSpråk);

}
