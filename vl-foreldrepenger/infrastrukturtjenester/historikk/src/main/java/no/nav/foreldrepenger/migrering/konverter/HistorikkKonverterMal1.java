package no.nav.foreldrepenger.migrering.konverter;

import static no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter.getNullableString;
import static no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter.parseJSON;

import java.util.List;

import javax.json.JsonObject;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringBegrunnelse;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringHendelse;

class HistorikkKonverterMal1 implements HistorikkMigreringKonverter {

    HistorikkKonverterMal1() {
        super();
    }

    @Override
    public List<HistorikkinnslagDel> konverter(Historikkinnslag historikkinnslag) {
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
        JsonObject tekstObject = parseJSON(historikkinnslag);

        String hendelseStr = getNullableString(tekstObject, "hendelse");
        HistorikkMigreringHendelse migreringHendelse = new HistorikkMigreringHendelse(hendelseStr);
        migreringHendelse.getHistorikkinnslagType().ifPresent(tekstBuilder::medHendelse);

        String begrunnelse = getNullableString(tekstObject, "begrunnelse");
        HistorikkMigreringBegrunnelse migreringBegrunnelse = new HistorikkMigreringBegrunnelse(begrunnelse);
        migreringBegrunnelse.addToBuilder(tekstBuilder);

        return tekstBuilder.build(historikkinnslag);
    }
}
