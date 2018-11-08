package no.nav.foreldrepenger.migrering.konverter.felt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageMedholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

public class HistorikkMigreringAarsak {
    private static final Map<String, Kodeliste> AARSAK_MAP;

    static {
        Map<String, Kodeliste> map = new HashMap<>();
        map.put("Avventer dokumentasjon", Venteårsak.AVV_DOK);
        map.put("Avventer fødsel", Venteårsak.AVV_FODSEL);
        map.put("Utvidet frist", Venteårsak.UTV_FRIST);
        map.put("Bruker har bedt om utvidet frist", Venteårsak.UTV_FRIST);
        map.put("Venter på scanning", Venteårsak.SCANN);
        map.put("Nye opplysninger som oppfyller vilkår", KlageMedholdÅrsak.NYE_OPPLYSNINGER);
        map.put("Ulik regelverkstolkning", KlageMedholdÅrsak.ULIK_REGELVERKSTOLKNING);
        map.put("Ulik skjønnsmessig vurdering", KlageMedholdÅrsak.ULIK_VURDERING);
        map.put("Prosessuell feil", KlageMedholdÅrsak.PROSESSUELL_FEIL);
        map.put("Bruker har klaget for sent", KlageAvvistÅrsak.KLAGET_FOR_SENT);
        map.put("Klage er ugyldig", KlageAvvistÅrsak.KLAGE_UGYLDIG);
        map.put("Henlagt, behandlingen er opprettet ved en feil", BehandlingResultatType.HENLAGT_FEILOPPRETTET);
        map.put("Henlagt, søknaden er feilopprettet", BehandlingResultatType.HENLAGT_FEILOPPRETTET);
        map.put("Henlagt, søknaden er trukket", BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET);
        map.put("Henlagt, brukeren er død", BehandlingResultatType.HENLAGT_BRUKER_DØD);
        map.put("Henlagt, klagen er trukket", BehandlingResultatType.HENLAGT_KLAGE_TRUKKET);
        map.put("Ikke definert", null);
        AARSAK_MAP = Collections.unmodifiableMap(map);
    }

    private Kodeliste aarsak;

    public HistorikkMigreringAarsak(String aarsakStr) {
        aarsak = AARSAK_MAP.get(aarsakStr);
    }

    public Optional<Kodeliste> getAarsak() {
        return Optional.ofNullable(aarsak);
    }
}
