package no.nav.foreldrepenger.migrering.konverter;

import static no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter.getNullableString;
import static no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter.parseJSON;

import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringAarsak;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringBegrunnelse;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringEndretFelt;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringHendelse;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringOpplysning;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringResultat;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringSkjermlenke;

class HistorikkKonverterMal5 implements HistorikkMigreringKonverter {
    HistorikkKonverterMal5() {
        super();
    }

    @Override
    public List<HistorikkinnslagDel> konverter(Historikkinnslag historikkinnslag) {
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();

        JsonObject tekstObject = parseJSON(historikkinnslag);

        String begrunnelse = getNullableString(tekstObject, "begrunnelse");
        HistorikkMigreringBegrunnelse migreringBegrunnelse = new HistorikkMigreringBegrunnelse(begrunnelse);
        migreringBegrunnelse.addToBuilder(tekstBuilder);

        String hendelse = getNullableString(tekstObject, "hendelse");
        HistorikkMigreringHendelse migreringHendelse = new HistorikkMigreringHendelse(hendelse);
        migreringHendelse.getHistorikkinnslagType().ifPresent(tekstBuilder::medHendelse);

        String aarsak = getNullableString(tekstObject, "aarsak");
        HistorikkMigreringAarsak migreringAarsak = new HistorikkMigreringAarsak(aarsak);
        migreringAarsak.getAarsak().ifPresent(tekstBuilder::medÅrsak);

        JsonObject skjermlenke = tekstObject.getJsonObject("skjermlinke");
        HistorikkMigreringSkjermlenke migreringSkjermlenke = new HistorikkMigreringSkjermlenke(historikkinnslag.getType(), tekstObject, skjermlenke);
        migreringSkjermlenke.getSkjermlenkeType().ifPresent(tekstBuilder::medSkjermlenke);

        String resultat = getNullableString(tekstObject, "resultat");
        HistorikkMigreringResultat migreringResultat = new HistorikkMigreringResultat(resultat);
        migreringResultat.getResultat().ifPresent(tekstBuilder::medResultat);

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
