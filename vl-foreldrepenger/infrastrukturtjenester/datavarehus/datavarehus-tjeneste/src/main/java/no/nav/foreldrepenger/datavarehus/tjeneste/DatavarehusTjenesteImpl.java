package no.nav.foreldrepenger.datavarehus.tjeneste;

import java.util.Collection;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.datavarehus.AksjonspunktDvh;
import no.nav.foreldrepenger.datavarehus.BehandlingDvh;
import no.nav.foreldrepenger.datavarehus.BehandlingStegDvh;
import no.nav.foreldrepenger.datavarehus.BehandlingVedtakDvh;
import no.nav.foreldrepenger.datavarehus.DatavarehusRepository;
import no.nav.foreldrepenger.datavarehus.FagsakDvh;
import no.nav.foreldrepenger.domene.typer.AktørId;

@ApplicationScoped
public class DatavarehusTjenesteImpl implements DatavarehusTjeneste {

    private DatavarehusRepository datavarehusRepository;
    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private TotrinnRepository totrinnRepository;

    public DatavarehusTjenesteImpl() {
        //Crazy Dedicated Instructors
    }

    @Inject
    public DatavarehusTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                   DatavarehusRepository datavarehusRepository,
                                   TotrinnRepository totrinnRepository) {
        this.datavarehusRepository = datavarehusRepository;
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.totrinnRepository = totrinnRepository;
    }

    @Override
    public void lagreNedFagsak(Long fagsakId) {
        Fagsak fagsak = fagsakRepository.finnEksaktFagsak(fagsakId);
        Optional<AktørId> annenPartAktørId = Optional.empty();
        FagsakDvh fagsakDvh = new FagsakDvhMapper().map(fagsak);
        datavarehusRepository.lagre(fagsakDvh);
    }

    @Override
    public void lagreNedAksjonspunkter(Collection<Aksjonspunkt> aksjonspunkter, Long behandlingId, BehandlingStegType behandlingStegType) {
        AksjonspunktDvhMapper mapper = new AksjonspunktDvhMapper();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Optional<BehandlingStegTilstand> behandlingStegTilstand = behandling.getBehandlingStegTilstand(behandlingStegType);
        Collection<Totrinnsvurdering> totrinnsvurderings = totrinnRepository.hentTotrinnaksjonspunktvurderinger(behandling);
        for (Aksjonspunkt aksjonspunkt : aksjonspunkter) {
            if (aksjonspunkt.getId() != null) {

                boolean godkjennt = totrinnsvurderings.stream().anyMatch(ttv -> ttv.getAksjonspunktDefinisjon() == aksjonspunkt.getAksjonspunktDefinisjon() && ttv.isGodkjent());
                AksjonspunktDvh aksjonspunktDvh = mapper.map(aksjonspunkt, behandling, behandlingStegTilstand, godkjennt);
                datavarehusRepository.lagre(aksjonspunktDvh);
            }
        }
    }

    @Override
    public void lagreNedBehandlingStegTilstand(BehandlingStegTilstand tilTilstand) {
        BehandlingStegDvh behandlingStegDvh = new BehandlingStegDvhMapper().map(tilTilstand);
        datavarehusRepository.lagre(behandlingStegDvh);
    }

    @Override
    public void lagreNedBehandling(Long behandlingId) {
        lagreNedBehandling(behandlingRepository.hentBehandling(behandlingId));
    }

    @Override
    public void lagreNedBehandling(Behandling behandling) {
        Optional<BehandlingVedtak> vedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId());
        lagreNedBehandling(behandling, vedtak);
    }

    private void lagreNedBehandling(Behandling behandling, Optional<BehandlingVedtak> vedtak) {
        BehandlingDvh behandlingDvh = new BehandlingDvhMapper().map(behandling, vedtak);
        datavarehusRepository.lagre(behandlingDvh);

        lagreNedAksjonspunkter(behandling.getAksjonspunkter(), behandling.getId(), null);
    }

    @Override
    public void lagreNedVedtak(BehandlingVedtak vedtak, Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BehandlingVedtakDvh behandlingVedtakDvh = new BehandlingVedtakDvhMapper().map(vedtak, behandling);
        datavarehusRepository.lagre(behandlingVedtakDvh);

        lagreNedBehandling(behandling, Optional.of(vedtak));
    }

    @Override
    public void lagreNedBehandlingOgTilstander(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        lagreNedBehandling(behandling);
        behandling.getBehandlingStegTilstandHistorikk().map(t -> new BehandlingStegDvhMapper().map(t)).forEach(dt -> datavarehusRepository.lagre(dt));
    }
}
