package no.nav.foreldrepenger.migrering.konverter.felt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

public class HistorikkMigreringResultat {
    private static final Map<String, Kodeliste> HISTORIKKINNSLAG_TYPE_MAP;

    // TODO (PKMANTIS-730): Legg til Klage behandlet?
    static {
        Map<String,Kodeliste> map = new HashMap<>();
        map.put("Avslag", VedtakResultatType.AVSLAG);
        map.put("Klagen er avvist", HistorikkResultatType.AVVIS_KLAGE);
        map.put("delvis innvilget", VedtakResultatType.DELVIS_INNVILGET);
        map.put("Grunnlag for beregnet årsinntekt", HistorikkResultatType.BEREGNET_AARSINNTEKT);
        map.put("Innvilget", VedtakResultatType.INNVILGET);
        // TODO (PKMANTIS-730): sannsynligvis samme med Overstyrt beregning
        map.put("Overstyrt vurdering: Utfallet er uendret", HistorikkResultatType.UTFALL_UENDRET);
        map.put("Vedtaket er omgjort", HistorikkResultatType.MEDHOLD_I_KLAGE);
        map.put("Vedtaket er opphevet", HistorikkResultatType.OPPHEVE_VEDTAK);
        map.put("Vedtaket er opprettholdt", HistorikkResultatType.OPPRETTHOLDT_VEDTAK);
        map.put("vedtak i klagebehandling", VedtakResultatType.VEDTAK_I_KLAGEBEHANDLING);
        map.put("vedtak i innsynbehandling", VedtakResultatType.VEDTAK_I_INNSYNBEHANDLING);
        HISTORIKKINNSLAG_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    private Kodeliste resultat;

    public HistorikkMigreringResultat(String resultat) {
        this.resultat = parseResultat(resultat);
    }

    public Optional<Kodeliste> getResultat() {
        return Optional.ofNullable(resultat);
    }

    private Kodeliste parseResultat(String hendelseString) {
        return HISTORIKKINNSLAG_TYPE_MAP.get(hendelseString);
    }
}
