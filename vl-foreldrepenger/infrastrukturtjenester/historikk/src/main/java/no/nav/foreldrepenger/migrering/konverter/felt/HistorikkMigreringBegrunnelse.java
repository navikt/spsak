package no.nav.foreldrepenger.migrering.konverter.felt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkBegrunnelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

public class HistorikkMigreringBegrunnelse {

    private static final Map<String, String> begrunnelseMap;
    private static final Map<String, Kodeliste> begrunnelseTypeMap;
    static {
        Map<String, Kodeliste> map = new HashMap<>();
        map.put("Ny behandling eller revurdering etter klage", BehandlingÅrsakType.ETTER_KLAGE);
        map.put("Manglende fødsel", BehandlingÅrsakType.RE_MANGLER_FØDSEL);
        map.put("Manglende fødsel i terminperiode", BehandlingÅrsakType.RE_MANGLER_FØDSEL);
        map.put("Ulikt antall barn", BehandlingÅrsakType.RE_AVVIK_ANTALL_BARN);
        map.put("Feil lovanvendelse", BehandlingÅrsakType.RE_FEIL_I_LOVANDVENDELSE);
        map.put("Feil regelverksforståelse", BehandlingÅrsakType.RE_FEIL_REGELVERKSFORSTÅELSE);
        map.put("Feil eller endret fakta", BehandlingÅrsakType.RE_FEIL_ELLER_ENDRET_FAKTA);
        map.put("Prosessuell feil", BehandlingÅrsakType.RE_FEIL_PROSESSUELL);
        map.put("Annet", BehandlingÅrsakType.RE_ANNET);
        map.put("Fødsel", BehandlingÅrsakType.RE_HENDELSE_FØDSEL);
        map.put("Behandle sak i VL", OppgaveÅrsak.BEHANDLE_SAK);
        map.put("Godkjenne vedtak i VL", OppgaveÅrsak.GODKJENNE_VEDTAK);
        map.put("Registrere søknad i VL", OppgaveÅrsak.REGISTRER_SØKNAD);
        map.put("Vurder dokument i VL", OppgaveÅrsak.VURDER_DOKUMENT);
        map.put("Vurder dokument", OppgaveÅrsak.VURDER_DOKUMENT);
        map.put("Vurder konsekvens for ytelse foreldrepenger", OppgaveÅrsak.VURDER_KONS_FOR_YTELSE);
        map.put("Revurdere i VL", OppgaveÅrsak.REVURDER);
        map.put("Saksbehandling starter på nytt", HistorikkBegrunnelseType.SAKSBEH_START_PA_NYTT);
        begrunnelseTypeMap = Collections.unmodifiableMap(map);
    }
    static {
        Map<String,String> map = new HashMap<>();
        map.put("Avslagsbrev", DokumentMalType.AVSLAGSVEDTAK_DOK);
        map.put("Forlenget saksbehandlingstid", DokumentMalType.FORLENGET_DOK);
        map.put("Forlenget saksbehandlingstid - medlemskap", DokumentMalType.FORLENGET_MEDL_DOK);
        map.put("Behandling avbrutt", DokumentMalType.HENLEGG_BEHANDLING_DOK);
        map.put("Innhent dokumentasjon", DokumentMalType.INNHENT_DOK);
        map.put("Vedtak om avvist klage", DokumentMalType.KLAGE_AVVIST_DOK);
        map.put("Vedtak om stadfestelse", DokumentMalType.KLAGE_YTELSESVEDTAK_STADFESTET_DOK);
        map.put("Vedtak opphevet, sendt til ny behandling", DokumentMalType.KLAGE_YTELSESVEDTAK_OPPHEVET_DOK);
        map.put("Overføring til NAV Klageinstans", DokumentMalType.KLAGE_OVERSENDT_KLAGEINSTANS_DOK);
        map.put("Positivt vedtaksbrev", DokumentMalType.POSITIVT_VEDTAK_DOK);
        map.put("Revurdering", "REVURD");
        map.put("Uendret utfall", "UENDRE");
        begrunnelseMap = Collections.unmodifiableMap(map);
    }

    private String begrunnelse;
    private Kodeliste begrunnelseType;

    public HistorikkMigreringBegrunnelse(String inputBegrunnelse) {
        begrunnelseType = begrunnelseTypeMap.get(inputBegrunnelse);
        if (begrunnelseType == null) {
            String begrunnelseStr = begrunnelseMap.get(inputBegrunnelse);
            this.begrunnelse = begrunnelseStr != null ? begrunnelseStr : inputBegrunnelse;
        }
    }

    public void addToBuilder(HistorikkInnslagTekstBuilder tekstBuilder) {
        if (begrunnelseType != null) {
            tekstBuilder.medBegrunnelse(begrunnelseType);
        } else if (begrunnelse != null) {
            tekstBuilder.medBegrunnelse(begrunnelse);
        }
    }
}
