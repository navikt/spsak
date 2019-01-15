package no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnresultatgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.dto.TotrinnskontrollAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.dto.TotrinnskontrollSkjermlenkeContextDto;

@ApplicationScoped
public class TotrinnskontrollAksjonspunkterTjenesteImpl implements TotrinnskontrollAksjonspunkterTjeneste {

    private TotrinnsaksjonspunktDtoTjeneste totrinnsaksjonspunktDtoTjeneste;
    private TotrinnTjeneste totrinnTjeneste;

    protected TotrinnskontrollAksjonspunkterTjenesteImpl() {
        //for CDI-proxy
    }

    @Inject
    public TotrinnskontrollAksjonspunkterTjenesteImpl(TotrinnsaksjonspunktDtoTjeneste totrinnsaksjonspunktDtoTjeneste,
                                                      TotrinnTjeneste totrinnTjeneste) {
        this.totrinnsaksjonspunktDtoTjeneste = totrinnsaksjonspunktDtoTjeneste;
        this.totrinnTjeneste = totrinnTjeneste;
    }

    @Override
    public List<TotrinnskontrollSkjermlenkeContextDto> hentTotrinnsSkjermlenkeContext(Behandling behandling) {
        List<TotrinnskontrollSkjermlenkeContextDto> skjermlenkeContext = new ArrayList<>();
        List<Aksjonspunkt> aksjonspunkter = behandling.getAksjonspunkterMedTotrinnskontroll();
        Map<SkjermlenkeType, List<TotrinnskontrollAksjonspunkterDto>> skjermlenkeMap = new HashMap<>();
        Collection<Totrinnsvurdering> ttVurderinger = totrinnTjeneste.hentTotrinnaksjonspunktvurderinger(behandling);
        // Behandling er ikkje i fatte vedtak og har ingen totrinnsvurderinger -> returnerer tom liste
        if (!BehandlingStatus.FATTER_VEDTAK.equals(behandling.getStatus()) && ttVurderinger.isEmpty()) {
            return Collections.emptyList();
        }
        for (Aksjonspunkt ap : aksjonspunkter) {
            Totrinnsvurdering.Builder builder = new Totrinnsvurdering.Builder(behandling, ap.getAksjonspunktDefinisjon());
            Optional<Totrinnsvurdering> vurdering = ttVurderinger.stream().filter(v -> v.getAksjonspunktDefinisjon().equals(ap.getAksjonspunktDefinisjon())).findFirst();
            vurdering.ifPresent(ttVurdering -> {
                if (ttVurdering.isGodkjent()) {
                    builder.medGodkjent(ttVurdering.isGodkjent());
                }
            });
            lagTotrinnsaksjonspunkt(behandling, skjermlenkeMap, builder.build());
        }
        for (Map.Entry<SkjermlenkeType, List<TotrinnskontrollAksjonspunkterDto>> skjermlenke : skjermlenkeMap.entrySet()) {
            TotrinnskontrollSkjermlenkeContextDto context = new TotrinnskontrollSkjermlenkeContextDto(skjermlenke.getKey().getKode(), skjermlenke.getValue());
            skjermlenkeContext.add(context);
        }
        return skjermlenkeContext;
    }

    private void lagTotrinnsaksjonspunkt(Behandling behandling, Map<SkjermlenkeType, List<TotrinnskontrollAksjonspunkterDto>> skjermlenkeMap, Totrinnsvurdering vurdering) {
        Optional<Totrinnresultatgrunnlag> totrinnresultatOpt = totrinnTjeneste.hentTotrinngrunnlagHvisEksisterer(behandling);
        TotrinnskontrollAksjonspunkterDto totrinnsAksjonspunkt = totrinnsaksjonspunktDtoTjeneste.lagTotrinnskontrollAksjonspunktDto(vurdering, behandling, totrinnresultatOpt);
        SkjermlenkeType skjermlenkeType = SkjermlenkeTjeneste.finnSkjermlenkeType(vurdering.getAksjonspunktDefinisjon(), behandling);
        if (skjermlenkeType != SkjermlenkeType.UDEFINERT) {
            List<TotrinnskontrollAksjonspunkterDto> aksjonspktContextListe = skjermlenkeMap.computeIfAbsent(skjermlenkeType,
                k -> new ArrayList<>());
            aksjonspktContextListe.add(totrinnsAksjonspunkt);
        }
    }

    @Override
    public List<TotrinnskontrollSkjermlenkeContextDto> hentTotrinnsvurderingSkjermlenkeContext(Behandling behandling) {
        List<TotrinnskontrollSkjermlenkeContextDto> skjermlenkeContext = new ArrayList<>();
        Collection<Totrinnsvurdering> totrinnaksjonspunktvurderinger = totrinnTjeneste.hentTotrinnaksjonspunktvurderinger(behandling);
        Map<SkjermlenkeType, List<TotrinnskontrollAksjonspunkterDto>> skjermlenkeMap = new HashMap<>();
        for (Totrinnsvurdering vurdering : totrinnaksjonspunktvurderinger) {
            lagTotrinnsaksjonspunkt(behandling, skjermlenkeMap, vurdering);
        }
        for (Map.Entry<SkjermlenkeType, List<TotrinnskontrollAksjonspunkterDto>> skjermlenke : skjermlenkeMap.entrySet()) {
            TotrinnskontrollSkjermlenkeContextDto context = new TotrinnskontrollSkjermlenkeContextDto(skjermlenke.getKey().getKode(), skjermlenke.getValue());
            skjermlenkeContext.add(context);
        }
        return skjermlenkeContext;
    }
}
