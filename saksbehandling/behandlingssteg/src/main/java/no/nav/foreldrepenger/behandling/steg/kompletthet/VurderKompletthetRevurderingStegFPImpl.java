package no.nav.foreldrepenger.behandling.steg.kompletthet;

import static no.nav.foreldrepenger.behandling.steg.kompletthet.VurderKompletthetStegFelles.autopunktAlleredeUtført;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.kompletthet.api.VurderKompletthetSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetResultat;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.Kompletthetsjekker;

@BehandlingStegRef(kode = "VURDERKOMPLETT")
@BehandlingTypeRef("BT-004")
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class VurderKompletthetRevurderingStegFPImpl implements VurderKompletthetSteg {

    private Kompletthetsjekker kompletthetsjekker;
    private BehandlingRepository behandlingRepository;
    private VurderKompletthetStegFelles vurderKompletthetStegFelles;

    VurderKompletthetRevurderingStegFPImpl() {
    }

    @Inject
    public VurderKompletthetRevurderingStegFPImpl(@FagsakYtelseTypeRef("FP") @BehandlingTypeRef("BT-004") Kompletthetsjekker kompletthetsjekker,
                                                  GrunnlagRepositoryProvider provider, VurderKompletthetStegFelles vurderKompletthetStegFelles) {
        this.kompletthetsjekker = kompletthetsjekker;
        this.behandlingRepository = provider.getBehandlingRepository();
        this.vurderKompletthetStegFelles = vurderKompletthetStegFelles;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        KompletthetResultat kompletthetResultat = kompletthetsjekker.vurderForsendelseKomplett(behandling);
        if (!kompletthetResultat.erOppfylt() && !autopunktAlleredeUtført(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, behandling)) {
            return vurderKompletthetStegFelles.evaluerUoppfylt(kompletthetResultat, AUTO_VENTER_PÅ_KOMPLETT_SØKNAD);
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }
}
