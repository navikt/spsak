package no.nav.foreldrepenger.migrering.konverter;

import static no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter.getNullableString;
import static no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter.parseJSON;

import java.util.List;

import javax.json.JsonObject;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringHendelse;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringResultat;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringSkjermlenke;

class HistorikkKonverterMal2 implements HistorikkMigreringKonverter {

    HistorikkKonverterMal2() {
        super();
    }

    @Override
    public List<HistorikkinnslagDel> konverter(Historikkinnslag historikkinnslag) {
        JsonObject tekstObject = parseJSON(historikkinnslag);
        String hendelseStr = getNullableString(tekstObject, "hendelse");
        String resultat = getNullableString(tekstObject, "resultat");
        JsonObject skjermlenkeObj = tekstObject.getJsonObject("skjermlinke");

        HistorikkMigreringHendelse migreringHendelse = new HistorikkMigreringHendelse(hendelseStr);
        HistorikkMigreringResultat migreringResultat = new HistorikkMigreringResultat(resultat);
        HistorikkMigreringSkjermlenke migreringSkjermlenke = new HistorikkMigreringSkjermlenke(historikkinnslag.getType(), tekstObject, skjermlenkeObj);

        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
        migreringHendelse.getHistorikkinnslagType().ifPresent(tekstBuilder::medHendelse);
        migreringResultat.getResultat().ifPresent(tekstBuilder::medResultat);
        migreringSkjermlenke.getSkjermlenkeType().ifPresent(tekstBuilder::medSkjermlenke);

        return tekstBuilder.build(historikkinnslag);
    }
}
