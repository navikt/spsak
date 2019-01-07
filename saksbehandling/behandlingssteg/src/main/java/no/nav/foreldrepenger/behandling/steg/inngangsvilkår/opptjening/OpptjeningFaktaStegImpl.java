package no.nav.foreldrepenger.behandling.steg.inngangsvilkår.opptjening;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;

/**
 * Steg 82 - Kontroller fakta for opptjening
 */
@BehandlingStegRef(kode = "VURDER_OPPTJ_FAKTA")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class OpptjeningFaktaStegImpl implements BehandlingSteg {

    private GrunnlagRepositoryProvider repositoryProvider;
    private AksjonspunktutlederForVurderOpptjening aksjonspunktutleder;

    OpptjeningFaktaStegImpl() {
        // CDI
    }

    @Inject
    public OpptjeningFaktaStegImpl(GrunnlagRepositoryProvider repositoryProvider, AksjonspunktutlederForVurderOpptjening aksjonspunktutleder) {
        this.repositoryProvider = repositoryProvider;
        this.aksjonspunktutleder = aksjonspunktutleder;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = repositoryProvider.getBehandlingRepository().hentBehandling(kontekst.getBehandlingId());

        return BehandleStegResultat.utførtMedAksjonspunktResultater(aksjonspunktutleder.utledAksjonspunkterFor(behandling));
    }
}
