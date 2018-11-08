package no.nav.foreldrepenger.migrering.konverter;

import static no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter.getNullableString;
import static no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter.parseJSON;

import java.util.List;

import javax.json.JsonObject;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringAarsak;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringBegrunnelse;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringHendelse;

class HistorikkKonverterMal4 implements HistorikkMigreringKonverter {

    HistorikkKonverterMal4() {
        super();
    }

    @Override
    public List<HistorikkinnslagDel> konverter(Historikkinnslag historikkinnslag) {
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
        JsonObject tekstObject = parseJSON(historikkinnslag);

        String begrunnelse = getNullableString(tekstObject, "begrunnelse");
        HistorikkMigreringBegrunnelse migreringBegrunnelse = new HistorikkMigreringBegrunnelse(begrunnelse);
        migreringBegrunnelse.addToBuilder(tekstBuilder);

        String hendelseStr = getNullableString(tekstObject, "hendelse");
        HistorikkMigreringHendelse migreringHendelse = new HistorikkMigreringHendelse(hendelseStr);
        migreringHendelse.getHistorikkinnslagType().ifPresent(historikkinnslagType -> {
            if (migreringHendelse.getVerdi().isPresent()) {
                tekstBuilder.medHendelse(historikkinnslagType, migreringHendelse.getVerdi().get());
            } else {
                tekstBuilder.medHendelse(historikkinnslagType);
            }
        });

        String aarsakStr = getNullableString(tekstObject, "aarsak");
        HistorikkMigreringAarsak migreringAarsak = new HistorikkMigreringAarsak(aarsakStr);
        migreringAarsak.getAarsak().ifPresent(tekstBuilder::medÅrsak);
        return tekstBuilder.build(historikkinnslag);
    }
}
