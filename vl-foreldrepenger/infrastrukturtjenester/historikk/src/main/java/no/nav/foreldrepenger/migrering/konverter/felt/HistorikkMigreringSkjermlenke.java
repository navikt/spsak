package no.nav.foreldrepenger.migrering.konverter.felt;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.JsonObject;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.migrering.api.HistorikkMigreringConstants;

public class HistorikkMigreringSkjermlenke {
    private static final Map<String, SkjermlenkeType> SKJERMLENKE_TYPE_MAP;

    static {
        Map<String, SkjermlenkeType> map = new HashMap<>();
        map.put(HistorikkMigreringConstants.ADOPSJON, SkjermlenkeType.FAKTA_OM_ADOPSJON);
        map.put(HistorikkMigreringConstants.BEREGNING, SkjermlenkeType.BEREGNING_ENGANGSSTOENAD);
        map.put(HistorikkMigreringConstants.FOEDSEL, SkjermlenkeType.FAKTA_OM_FOEDSEL);
        map.put(HistorikkMigreringConstants.MEDLEMSKAP, SkjermlenkeType.FAKTA_OM_MEDLEMSKAP);
        map.put(HistorikkMigreringConstants.FORELDREANSVAR, SkjermlenkeType.FAKTA_OM_OMSORG_OG_FORELDREANSVAR);
        map.put(HistorikkMigreringConstants.KLAGE_NFP, SkjermlenkeType.KLAGE_BEH_NFP);
        map.put(HistorikkMigreringConstants.KLAGE_NK, SkjermlenkeType.KLAGE_BEH_NK);
        map.put(HistorikkMigreringConstants.KONTROLL_AV_SAKSOPPLYSNINGER, SkjermlenkeType.KONTROLL_AV_SAKSOPPLYSNINGER);
        map.put(HistorikkMigreringConstants.FAKTA_OM_OMSORG, SkjermlenkeType.FAKTA_FOR_OMSORG);
        map.put(HistorikkMigreringConstants.OMSORG, SkjermlenkeType.FAKTA_OM_OMSORG_OG_FORELDREANSVAR);
        map.put(HistorikkMigreringConstants.OPPLYSNINGSPLIKT, SkjermlenkeType.OPPLYSNINGSPLIKT);
        map.put(HistorikkMigreringConstants.OPPTJENING, SkjermlenkeType.FAKTA_FOR_OPPTJENING);
        map.put(HistorikkMigreringConstants.SOEKNADSFRIST, SkjermlenkeType.SOEKNADSFRIST);
        map.put(HistorikkMigreringConstants.VEDTAK, SkjermlenkeType.VEDTAK);
        SKJERMLENKE_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    private static final Map<String, SkjermlenkeType> OVERSTYRING_SKJERMLENKE_TYPE_MAP;

    static {
        Map<String, SkjermlenkeType> map = new HashMap<>();
        map.put(HistorikkMigreringConstants.ADOPSJON, SkjermlenkeType.PUNKT_FOR_ADOPSJON);
        map.put(HistorikkMigreringConstants.BEREGNING, SkjermlenkeType.BEREGNING_ENGANGSSTOENAD);
        map.put(HistorikkMigreringConstants.FOEDSEL, SkjermlenkeType.PUNKT_FOR_FOEDSEL);
        map.put(HistorikkMigreringConstants.FORELDREANSVAR, SkjermlenkeType.PUNKT_FOR_FORELDREANSVAR);
        map.put(HistorikkMigreringConstants.MEDLEMSKAP, SkjermlenkeType.PUNKT_FOR_MEDLEMSKAP);
        map.put(HistorikkMigreringConstants.OMSORG, SkjermlenkeType.PUNKT_FOR_OMSORG);
        map.put(HistorikkMigreringConstants.OPPLYSNINGSPLIKT, SkjermlenkeType.OPPLYSNINGSPLIKT);
        map.put(HistorikkMigreringConstants.SOEKNADSFRIST, SkjermlenkeType.SOEKNADSFRIST);
        OVERSTYRING_SKJERMLENKE_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    private SkjermlenkeType skjermlenkeType;

    public HistorikkMigreringSkjermlenke(HistorikkinnslagType historikkinnslagType, JsonObject tekstObject, JsonObject skjermlenkeObj) {
        if (skjermlenkeObj == null) {
            return;
        }
        Map<String, SkjermlenkeType> map = SKJERMLENKE_TYPE_MAP;
        if (gjelderBehandlingspunkt(historikkinnslagType, tekstObject)) {
            map = OVERSTYRING_SKJERMLENKE_TYPE_MAP;
        }
        skjermlenkeType = parseSkjermlenke(skjermlenkeObj, map);
    }

    private boolean gjelderBehandlingspunkt(HistorikkinnslagType historikkinnslagType, JsonObject tekstObject) {
        if (historikkinnslagType.equals(HistorikkinnslagType.OVERSTYRT)) {
            return true;
        }
        return historikkinnslagType.equals(HistorikkinnslagType.FAKTA_ENDRET) && harEndretFeltForBehandlingspunkt(tekstObject);
    }

    private boolean harEndretFeltForBehandlingspunkt(JsonObject tekstObject) {
        List<JsonObject> endredeFelter = HistorikkMigreringEndretFelt.getEndredeFelter(tekstObject);
        return endredeFelter.stream()
            .map(HistorikkMigreringEndretFelt::new)
            .anyMatch(felt -> HistorikkMigreringConstants.ENDRET_FELT_BEHANDLINGSPUNKT.contains(felt.getHistorikkEndretFeltType()));
    }

    public Optional<SkjermlenkeType> getSkjermlenkeType() {
        return Optional.ofNullable(skjermlenkeType);
    }

    private SkjermlenkeType parseSkjermlenke(JsonObject skjermlenkeObj, Map<String, SkjermlenkeType> map) {
        String faktaNavn = skjermlenkeObj.getString(HistorikkMigreringConstants.FAKTA_NAVN);
        if (HistorikkMigreringConstants.BEREGNING.equals(faktaNavn)) {
            return SkjermlenkeType.BEREGNING_FORELDREPENGER;
        }
        if (HistorikkMigreringConstants.DEFAULT.equals(faktaNavn)) {
            String punktNavn = skjermlenkeObj.getString(HistorikkMigreringConstants.PUNKT_NAVN);
            return map.get(punktNavn);
        }
        return map.get(faktaNavn);
    }
}
