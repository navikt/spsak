package no.nav.foreldrepenger.behandling.steg.uttak.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.uttak.KontrollerFaktaUttakSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaUttakTjeneste;

@BehandlingStegRef(kode = "KOFAKUT")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class KontrollerFaktaUttakStegImpl implements KontrollerFaktaUttakSteg {

    private BehandlingRepositoryProvider repositoryProvider;
    private KontrollerFaktaUttakTjeneste kontrollerFaktaUttakTjeneste;

    KontrollerFaktaUttakStegImpl() {
        // CDI
    }

    @Inject
    public KontrollerFaktaUttakStegImpl(BehandlingRepositoryProvider repositoryProvider, @FagsakYtelseTypeRef("FP") KontrollerFaktaUttakTjeneste kontrollerFaktaUttakTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.kontrollerFaktaUttakTjeneste = kontrollerFaktaUttakTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = repositoryProvider.getBehandlingRepository().hentBehandling(kontekst.getBehandlingId());
        return BehandleStegResultat.utførtMedAksjonspunktResultater(kontrollerFaktaUttakTjeneste.utledAksjonspunkter(behandling));
    }

    @Override
    public void vedHoppOverBakover(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, BehandlingStegType tilSteg, BehandlingStegType fraSteg) {
        new RyddUttakTjeneste(behandling, repositoryProvider).ryddAvklarteUttaksperioder();
    }
}
