package no.nav.foreldrepenger.behandling.steg.innhentsaksopplysninger;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunktMedCallback;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.innhentregisteropplysninger.api.InnhentRegisteropplysningerSteg;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataInnhenter;

@BehandlingStegRef(kode = "INREG")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class InnhentRegisteropplysningerStegFPImpl implements InnhentRegisteropplysningerSteg {

    private AksjonspunktRepository aksjonspunktRepository;
    private BehandlingRepository behandlingRepository;
    private RegisterdataInnhenter registerdataInnhenter;

    InnhentRegisteropplysningerStegFPImpl() {
        // for CDI proxy
    }

    @Inject
    public InnhentRegisteropplysningerStegFPImpl(BehandlingRepositoryProvider repositoryProvider,
                                                 RegisterdataInnhenter registerdataInnhenter) {

        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.registerdataInnhenter = registerdataInnhenter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        registerdataInnhenter.innhentPersonopplysninger(behandling);

        registerdataInnhenter.opprettProsesstaskForRelaterteYtelser(behandling);
        AksjonspunktResultat aksjonspunktResultat = opprettForAksjonspunktMedCallback(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER,
            ap -> aksjonspunktRepository.setFrist(ap, null,
                Venteårsak.VENT_REGISTERINNHENTING));
        return BehandleStegResultat.utførtMedAksjonspunktResultater(singletonList(aksjonspunktResultat));
    }


}
