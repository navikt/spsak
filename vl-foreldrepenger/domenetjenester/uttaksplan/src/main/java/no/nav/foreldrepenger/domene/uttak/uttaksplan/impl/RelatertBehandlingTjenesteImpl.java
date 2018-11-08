package no.nav.foreldrepenger.domene.uttak.uttaksplan.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;

@ApplicationScoped
public class RelatertBehandlingTjenesteImpl implements RelatertBehandlingTjeneste {

    private FagsakRelasjonRepository fagsakRelasjonRepository;
    private BehandlingRepository behandlingRepository;
    private UttakRepository uttakRepository;

    RelatertBehandlingTjenesteImpl() {
        // CDI
    }

    @Inject
    public RelatertBehandlingTjenesteImpl(BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.fagsakRelasjonRepository = behandlingRepositoryProvider.getFagsakRelasjonRepository();
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.uttakRepository = behandlingRepositoryProvider.getUttakRepository();
    }

    @Override
    public Optional<Behandling> hentAnnenPartsGjeldendeBehandling(Fagsak fagsak) {
        Optional<FagsakRelasjon> optionalFagsakRelasjon = fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(fagsak);
        if (optionalFagsakRelasjon.isPresent()) {
            FagsakRelasjon fagsakRelasjon = optionalFagsakRelasjon.get();
            Optional<Fagsak> annenPartsFagsak = fagsakRelasjon.getFagsakNrEn().equals(fagsak) ? fagsakRelasjon.getFagsakNrTo() : Optional.of(fagsakRelasjon.getFagsakNrEn());
            if(annenPartsFagsak.isPresent()) {
                return behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(annenPartsFagsak.get().getId());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<UttakResultatEntitet> hentAnnenPartsGjeldendeUttaksplan(Behandling behandling) {
        Optional<Behandling> annenPartsBehandling = hentAnnenPartsGjeldendeBehandling(behandling.getFagsak());
        if(annenPartsBehandling.isPresent()) {
            return uttakRepository.hentUttakResultatHvisEksisterer(annenPartsBehandling.get());
        }
        return Optional.empty();
    }
}
