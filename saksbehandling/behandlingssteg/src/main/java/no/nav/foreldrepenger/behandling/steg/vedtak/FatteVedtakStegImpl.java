package no.nav.foreldrepenger.behandling.steg.vedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

@BehandlingStegRef(kode = "FVEDSTEG")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class FatteVedtakStegImpl implements FatteVedtakSteg {

    private BehandlingRepository behandlingRepository;
    private FatteVedtakTjeneste fatteVedtakTjeneste;

    FatteVedtakStegImpl() {
        // for CDI proxy
    }

    @Inject
    FatteVedtakStegImpl(BehandlingRepositoryProvider repositoryProvider, @FagsakYtelseTypeRef("FP") FatteVedtakTjeneste fatteVedtakTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.fatteVedtakTjeneste = fatteVedtakTjeneste;
    }


    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return fatteVedtakTjeneste.fattVedtak(kontekst, behandling);
    }
}
