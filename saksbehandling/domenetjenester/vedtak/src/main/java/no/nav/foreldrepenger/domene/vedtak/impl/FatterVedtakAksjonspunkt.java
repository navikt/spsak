package no.nav.foreldrepenger.domene.vedtak.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.domene.vedtak.VedtakAksjonspunktData;
import no.nav.foreldrepenger.domene.vedtak.VedtakTjeneste;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
public class FatterVedtakAksjonspunkt {

    private AksjonspunktRepository aksjonspunktRepository;
    private KodeverkTabellRepository kodeverkTabellRepository;
    private VedtakTjeneste vedtakTjeneste;
    private TotrinnTjeneste totrinnTjeneste;

    public FatterVedtakAksjonspunkt() {
    }

    @Inject
    public FatterVedtakAksjonspunkt(BehandlingRepositoryProvider repositoryProvider, VedtakTjeneste vedtakTjeneste, TotrinnTjeneste totrinnTjeneste) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.kodeverkTabellRepository = repositoryProvider.getKodeverkRepository().getKodeverkTabellRepository();
        this.vedtakTjeneste = vedtakTjeneste;
        this.totrinnTjeneste = totrinnTjeneste;
    }

    public void oppdater(Behandling behandling, Collection<VedtakAksjonspunktData> aksjonspunkter) {
        behandling.setAnsvarligBeslutter(SubjectHandler.getSubjectHandler().getUid());

        List<Totrinnsvurdering> totrinnsvurderinger = new ArrayList<>();

        for (VedtakAksjonspunktData aks : aksjonspunkter) {
            Aksjonspunkt aksjonspunkt = behandling.getAksjonspunktFor(aks.getAksjonspunktDefinisjon());
            if (!aks.isGodkjent()) {
                aksjonspunktRepository.setReåpnet(aksjonspunkt);
                aksjonspunktRepository.setToTrinnsBehandlingKreves(aksjonspunkt);
            }
            Collection<VurderÅrsak> vurderÅrsaker = kodeverkTabellRepository.finnVurderÅrsaker(aks.getVurderÅrsakskoder());

            Totrinnsvurdering.Builder vurderingBuilder = new Totrinnsvurdering.Builder(behandling, aks.getAksjonspunktDefinisjon());
            vurderingBuilder.medGodkjent(aks.isGodkjent());
            vurderÅrsaker.forEach(årsak -> vurderingBuilder.medVurderÅrsak(årsak));
            vurderingBuilder.medBegrunnelse(aks.getBegrunnelse());
            totrinnsvurderinger.add(vurderingBuilder.build());
        }
        totrinnTjeneste.settNyeTotrinnaksjonspunktvurderinger(behandling, totrinnsvurderinger);
        vedtakTjeneste.lagHistorikkinnslagFattVedtak(behandling);
    }
}
