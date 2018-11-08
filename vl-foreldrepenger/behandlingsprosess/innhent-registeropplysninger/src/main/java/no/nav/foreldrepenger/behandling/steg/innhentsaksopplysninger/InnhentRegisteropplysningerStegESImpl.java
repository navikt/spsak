package no.nav.foreldrepenger.behandling.steg.innhentsaksopplysninger;

import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunkt;

import java.util.Collections;

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
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataInnhenter;

@BehandlingStegRef(kode = "INREG")
@BehandlingTypeRef
@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class InnhentRegisteropplysningerStegESImpl implements InnhentRegisteropplysningerSteg {

    private BehandlingRepository behandlingRepository;
    private RegisterdataInnhenter registerdataInnhenter;

    InnhentRegisteropplysningerStegESImpl() {
        // for CDI proxy
    }

    @Inject
    public InnhentRegisteropplysningerStegESImpl(BehandlingRepositoryProvider repositoryProvider,
                                                 RegisterdataInnhenter registerdataInnhenter) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.registerdataInnhenter = registerdataInnhenter;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        registerdataInnhenter.innhentPersonopplysninger(behandling);

        registerdataInnhenter.opprettProsesstaskForRelaterteYtelser(behandling);

        AksjonspunktResultat aksjonspunktResultat = opprettForAksjonspunkt(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER);
        return BehandleStegResultat.utførtMedAksjonspunktResultater(Collections.singletonList(aksjonspunktResultat));
    }
}
