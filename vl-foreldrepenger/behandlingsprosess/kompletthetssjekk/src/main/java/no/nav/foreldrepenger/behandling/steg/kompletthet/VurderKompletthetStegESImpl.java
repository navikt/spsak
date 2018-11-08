package no.nav.foreldrepenger.behandling.steg.kompletthet;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.kompletthet.api.VurderKompletthetSteg;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetResultat;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.Kompletthetsjekker;

@BehandlingStegRef(kode = "VURDERKOMPLETT")
@BehandlingTypeRef
@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class VurderKompletthetStegESImpl implements VurderKompletthetSteg {

    private Kompletthetsjekker vurderKompletthetTjeneste;
    private BehandlingRepository behandlingRepository;
    private VurderKompletthetStegFelles vurderKompletthetStegFelles;

    VurderKompletthetStegESImpl() {
    }

    @Inject
    public VurderKompletthetStegESImpl(@FagsakYtelseTypeRef("ES") Kompletthetsjekker vurderKompletthetTjeneste,
                                       BehandlingRepositoryProvider provider, VurderKompletthetStegFelles vurderKompletthetStegFelles) {
        this.vurderKompletthetTjeneste = vurderKompletthetTjeneste;
        this.behandlingRepository = provider.getBehandlingRepository();
        this.vurderKompletthetStegFelles = vurderKompletthetStegFelles;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        List<AksjonspunktResultat> aksjonspunktResultat = new ArrayList<>();
        final Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        KompletthetResultat kompletthetResultat = vurderKompletthetTjeneste.vurderForsendelseKomplett(behandling);
        if (!kompletthetResultat.erOppfylt()) {
            aksjonspunktResultat = singletonList(vurderKompletthetStegFelles.byggAutopunkt(kompletthetResultat, AUTO_VENTER_PÅ_KOMPLETT_SØKNAD));
        }
        return BehandleStegResultat.utførtMedAksjonspunktResultater(aksjonspunktResultat);
    }
}
