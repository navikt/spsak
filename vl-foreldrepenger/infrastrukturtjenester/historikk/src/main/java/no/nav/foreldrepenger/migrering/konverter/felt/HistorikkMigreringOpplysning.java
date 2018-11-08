package no.nav.foreldrepenger.migrering.konverter.felt;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonValue;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkOpplysningType;

public class HistorikkMigreringOpplysning {
    private static final Map<String,HistorikkOpplysningType> OPPLYSNING_TYPE_MAP;

    static {
        Map<String,HistorikkOpplysningType> map = new HashMap<>();
        map.put("Historikk.Template.5.AntallBarn", HistorikkOpplysningType.ANTALL_BARN);
        map.put("Registrering.AntallBarn", HistorikkOpplysningType.TPS_ANTALL_BARN);
        map.put("Registrering.Fodselsdato", HistorikkOpplysningType.FODSELSDATO);
        OPPLYSNING_TYPE_MAP = map;
    }

    private HistorikkOpplysningType opplysningType;
    private Object verdi;

    public HistorikkMigreringOpplysning(JsonObject opplysning) {
        String navn = opplysning.getString("navn");
        opplysningType = OPPLYSNING_TYPE_MAP.get(navn);
        JsonValue jsonVerdi = opplysning.get("verdi");
        if (jsonVerdi.getValueType() == JsonValue.ValueType.STRING) {
            verdi = opplysning.getString("verdi");
        } else {
            verdi = jsonVerdi.toString();
        }
    }

    public HistorikkOpplysningType getOpplysningType() {
        return opplysningType;
    }

    public Object getVerdi() {
        return verdi;
    }
}
