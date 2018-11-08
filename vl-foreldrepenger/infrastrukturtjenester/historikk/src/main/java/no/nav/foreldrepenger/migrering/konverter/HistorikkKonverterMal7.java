package no.nav.foreldrepenger.migrering.konverter;

import static no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter.getNullableString;
import static no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter.parseJSON;

import java.util.List;

import javax.json.JsonObject;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringEndretFelt;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringResultat;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringSkjermlenke;

class HistorikkKonverterMal7 implements HistorikkMigreringKonverter {
    HistorikkKonverterMal7() {
        super();
    }

    @Override
    public List<HistorikkinnslagDel> konverter(Historikkinnslag historikkinnslag) {
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();

        JsonObject tekstObject = parseJSON(historikkinnslag);
        String begrunnelse = getNullableString(tekstObject, "begrunnelse");
        tekstBuilder.medBegrunnelse(begrunnelse);

        String resultat = getNullableString(tekstObject, "resultat");
        HistorikkMigreringResultat migreringResultat = new HistorikkMigreringResultat(resultat);
        migreringResultat.getResultat().ifPresent(tekstBuilder::medResultat);

        JsonObject skjermlenke = tekstObject.getJsonObject("skjermlinke");
        HistorikkMigreringSkjermlenke migreringSkjermlenke = new HistorikkMigreringSkjermlenke(historikkinnslag.getType(), tekstObject, skjermlenke);
        migreringSkjermlenke.getSkjermlenkeType().ifPresent(tekstBuilder::medSkjermlenke);

        HistorikkMigreringEndretFelt.parseEndredeFelter(tekstBuilder, tekstObject);
        return tekstBuilder.build(historikkinnslag);
    }
}
