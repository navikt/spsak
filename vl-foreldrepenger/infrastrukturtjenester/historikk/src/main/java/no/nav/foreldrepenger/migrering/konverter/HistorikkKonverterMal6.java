package no.nav.foreldrepenger.migrering.konverter;

import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringEndretFelt;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringHendelse;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringOpplysning;

class HistorikkKonverterMal6 implements HistorikkMigreringKonverter {
    HistorikkKonverterMal6() {
        super();
    }

    @Override
    public List<HistorikkinnslagDel> konverter(Historikkinnslag historikkinnslag) {
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();

        JsonObject tekstObject = HistorikkMigreringKonverter.parseJSON(historikkinnslag);

        String hendelse = HistorikkMigreringKonverter.getNullableString(tekstObject, "hendelse");
        HistorikkMigreringHendelse migreringHendelse = new HistorikkMigreringHendelse(hendelse);
        migreringHendelse.getHistorikkinnslagType().ifPresent(tekstBuilder::medHendelse);

        HistorikkMigreringEndretFelt.parseEndredeFelter(tekstBuilder, tekstObject);
        parseOpplysninger(tekstBuilder, tekstObject);
        return tekstBuilder.build(historikkinnslag);
    }

    private void parseOpplysninger(HistorikkInnslagTekstBuilder tekstBuilder, JsonObject tekstObject) {
        JsonArray opplysningerArray = tekstObject.getJsonArray("opplysninger");
        List<JsonObject> opplysninger = opplysningerArray.getValuesAs(JsonObject.class);
        opplysninger.forEach(opplysning -> {
            HistorikkMigreringOpplysning migreringOpplysning = new HistorikkMigreringOpplysning(opplysning);
            tekstBuilder.medOpplysning(migreringOpplysning.getOpplysningType(), migreringOpplysning.getVerdi());
        });
    }
}
