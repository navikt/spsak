package no.nav.foreldrepenger.domene.familiehendelse.omsorg.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.familiehendelse.omsorg.OmsorghendelseTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.AvklarForeldreansvarAksjonspunktData;
import no.nav.foreldrepenger.domene.personopplysning.AvklarOmsorgOgForeldreansvarAksjonspunktData;

@ApplicationScoped
public class OmsorghendelseTjenesteImpl implements OmsorghendelseTjeneste {
    private BehandlingRepositoryProvider repositoryProvider;

    OmsorghendelseTjenesteImpl() {
        // CDI
    }
    
    @Inject
    public OmsorghendelseTjenesteImpl(BehandlingRepositoryProvider repositoryProvider) {
        this.repositoryProvider = repositoryProvider;}

    @Override
    public void aksjonspunktAvklarOmsorgOgForeldreansvar(Behandling behandling, AvklarOmsorgOgForeldreansvarAksjonspunktData data) {
        new AvklarOmsorgOgForeldreansvarAksjonspunkt(repositoryProvider).oppdater(behandling, data);
    }

    @Override
    public void aksjonspunktAvklarForeldreansvar(Behandling behandling, AvklarForeldreansvarAksjonspunktData data) {
        new AvklarOmsorgOgForeldreansvarAksjonspunkt(repositoryProvider).oppdater(behandling, data);
    }

    @Override
    public void aksjonspunktOmsorgsvilkår(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        new OmsorgsvilkårAksjonspunkt(repositoryProvider).oppdater(behandling, aksjonspunktDefinisjon);
    }
}
