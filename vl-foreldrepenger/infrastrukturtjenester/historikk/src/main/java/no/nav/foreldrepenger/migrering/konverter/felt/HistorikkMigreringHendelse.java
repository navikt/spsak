package no.nav.foreldrepenger.migrering.konverter.felt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;

public class HistorikkMigreringHendelse {
    private static final Map<String, HistorikkinnslagType> HISTORIKKINNSLAG_TYPE_MAP;

    static {
        Map<String,HistorikkinnslagType> map = new HashMap<>();
        map.put("Automatisk behandling er avbrutt på grunn av oppgave", HistorikkinnslagType.BEH_AVBRUTT_VUR);
        map.put("Behandlingen er gjenopptatt", HistorikkinnslagType.BEH_GJEN);
        map.put("Behandlingen er henlagt", HistorikkinnslagType.AVBRUTT_BEH);
        map.put("Brevet er bestilt", HistorikkinnslagType.BREV_BESTILT);
        map.put("Byttet behandlende enhet", HistorikkinnslagType.BYTT_ENHET);
        map.put("Dokument mottatt", HistorikkinnslagType.VEDLEGG_MOTTATT);
        map.put("Klage behandlet av NAV Familie og Pensjon", HistorikkinnslagType.KLAGE_BEH_NFP);
        map.put("Klage behandlet av NAV Klageinstans", HistorikkinnslagType.KLAGE_BEH_NK);
        map.put("Klage mottatt", HistorikkinnslagType.KLAGEBEH_STARTET);
        map.put("Mangelfull papirsøknad", HistorikkinnslagType.MANGELFULL_SØKNAD);
        map.put("Ny info fra TPS", HistorikkinnslagType.NY_INFO_FRA_TPS);
        map.put("Melding er sendt", HistorikkinnslagType.BREV_SENT);
        map.put("Nye registeropplysninger", HistorikkinnslagType.NYE_REGOPPLYSNINGER);
        map.put("Registrering av papirsøknaden", HistorikkinnslagType.REGISTRER_PAPIRSØK);
        map.put("Resultat: Ingen endring, behandlingen er avsluttet.", HistorikkinnslagType.UENDRET_UTFALL);
        map.put("Revurdering opprettet", HistorikkinnslagType.REVURD_OPPR);
        map.put("Søknad mottatt", HistorikkinnslagType.BEH_STARTET);
        map.put("Varsel om revurdering ikke sendt", HistorikkinnslagType.VRS_REV_IKKE_SNDT);
        map.put("Vedtak fattet og iverksatt", HistorikkinnslagType.VEDTAK_FATTET);
        map.put("Vedtak foreslått og sendt til beslutter", HistorikkinnslagType.FORSLAG_VEDTAK);
        map.put("Vedtak returnert", HistorikkinnslagType.SAK_RETUR);
        HISTORIKKINNSLAG_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    private HistorikkinnslagType historikkinnslagType;
    private String verdi;

    public HistorikkMigreringHendelse(String hendelseString) {
        historikkinnslagType = parseHistorikkinnslagType(hendelseString);
    }

    public Optional<HistorikkinnslagType> getHistorikkinnslagType() {
        return Optional.ofNullable(historikkinnslagType);
    }

    public Optional<String> getVerdi() {
        return Optional.ofNullable(verdi);
    }

    private HistorikkinnslagType parseHistorikkinnslagType(String hendelseString) {
        if (hendelseString != null && hendelseString.startsWith("Behandlingen er satt på vent med frist")) {
            this.verdi = hendelseString.substring(39, 49);
            return HistorikkinnslagType.BEH_VENT;
        }
        return HISTORIKKINNSLAG_TYPE_MAP.get(hendelseString);
    }
}
