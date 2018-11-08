package no.nav.foreldrepenger.migrering.konverter;

import static no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeTjeneste.finnSkjermlenkeType;
import static no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter.parseJSON;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.JsonObject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagTotrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.migrering.konverter.felt.HistorikkMigreringHendelse;

class HistorikkKonverterMal3 implements HistorikkMigreringKonverter {

    private AksjonspunktRepository aksjonspunktRepository;
    private BehandlingRepository behandlingRepository;

    private static final LocalDateTime START_TIME = LocalDateTime.now();

    HistorikkKonverterMal3(AksjonspunktRepository aksjonspunktRepository, BehandlingRepository behandlingRepository) {
        super();
        this.aksjonspunktRepository = aksjonspunktRepository;
        this.behandlingRepository = behandlingRepository;
    }

    @Override
    public List<HistorikkinnslagDel> konverter(Historikkinnslag historikkinnslag) {
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();

        JsonObject tekstObject = parseJSON(historikkinnslag);
        String hendelseStr = tekstObject.getString("hendelse");
        HistorikkMigreringHendelse migreringHendelse = new HistorikkMigreringHendelse(hendelseStr);
        migreringHendelse.getHistorikkinnslagType().ifPresent(tekstBuilder::medHendelse);
        Behandling behandling = behandlingRepository.hentBehandling(historikkinnslag.getBehandlingId());
        parseTotrinnsvurdering(behandling, tekstBuilder, tekstObject);
        return tekstBuilder.build(historikkinnslag);
    }

    private void parseTotrinnsvurdering(Behandling behandling, HistorikkInnslagTekstBuilder tekstBuilder, JsonObject tekstObject) {
        List<JsonObject> totrinnsvurderingList = tekstObject.getJsonArray("totrinnsvurdering").getValuesAs(JsonObject.class);
        Map<SkjermlenkeType, List<HistorikkinnslagTotrinnsvurdering>> vurdering = new HashMap<>();
        List<HistorikkinnslagTotrinnsvurdering> vurderingUtenLenke = new ArrayList<>();
        totrinnsvurderingList.stream().flatMap(totrinnsvurderingJson -> totrinnsvurderingJson.getJsonArray("aksjonspunkter").getValuesAs(JsonObject.class).stream())
            .forEach(aksjonspunktJson -> {
                HistorikkinnslagTotrinnsvurdering totrinnsVurdering = lagTotrinnsvurdering(aksjonspunktJson, behandling);
                AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(aksjonspunktJson.getString("kode"));
                SkjermlenkeType skjermlenkeType = finnSkjermlenkeType(aksjonspunktDefinisjon, behandling);
                if (skjermlenkeType != SkjermlenkeType.UDEFINERT) {
                    List<HistorikkinnslagTotrinnsvurdering> aksjonspktVurderingListe = vurdering.computeIfAbsent(skjermlenkeType,
                        k -> new ArrayList<>());
                    aksjonspktVurderingListe.add(totrinnsVurdering);
                } else {
                    vurderingUtenLenke.add(totrinnsVurdering);
                }

            });
        tekstBuilder.medTotrinnsvurdering(vurdering, vurderingUtenLenke);
    }


    private HistorikkinnslagTotrinnsvurdering lagTotrinnsvurdering(JsonObject aksjonspunktJson, Behandling behandling) {
        String begrunnelse = aksjonspunktJson.getString("begrunnelse");
        boolean godkjent = aksjonspunktJson.getBoolean("godkjent");
        String kode = aksjonspunktJson.getString("kode");
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(kode);
        Optional<Aksjonspunkt> aksjonspunktOpt = behandling.getAksjonspunktMedDefinisjonOptional(aksjonspunktDefinisjon);

        HistorikkinnslagTotrinnsvurdering totrinnsVurdering = new HistorikkinnslagTotrinnsvurdering();
        totrinnsVurdering.setAksjonspunktDefinisjon(aksjonspunktDefinisjon);
        totrinnsVurdering.setBegrunnelse(begrunnelse);
        totrinnsVurdering.setGodkjent(godkjent);
        LocalDateTime sistEndret = aksjonspunktOpt.map(ap -> ap.getEndretTidspunkt() != null ? ap.getEndretTidspunkt() : ap.getOpprettetTidspunkt())
            .orElse(START_TIME);
        totrinnsVurdering.setAksjonspunktSistEndret(sistEndret);
        return totrinnsVurdering;
    }

}
