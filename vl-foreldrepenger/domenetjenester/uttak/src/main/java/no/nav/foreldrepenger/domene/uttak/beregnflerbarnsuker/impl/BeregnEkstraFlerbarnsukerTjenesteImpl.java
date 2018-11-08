package no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.BeregnEkstraFlerbarnsukerTjeneste;

@ApplicationScoped
class BeregnEkstraFlerbarnsukerTjenesteImpl implements BeregnEkstraFlerbarnsukerTjeneste {

    private FamilieHendelseRepository familieHendelseRepository;
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private FagsakRelasjonRepository fagsakRelasjonRepository;

    BeregnEkstraFlerbarnsukerTjenesteImpl() {
        //For CDI
    }

    @Inject
    public BeregnEkstraFlerbarnsukerTjenesteImpl(BehandlingRepositoryProvider repositoryProvider) {
        this.familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.fagsakRelasjonRepository = repositoryProvider.getFagsakRelasjonRepository();
    }

    @Override
    public Integer beregneEkstraFlerbarnsuker(Behandling behandling) {
        FamilieHendelseGrunnlag familieHendelseGrunnlag = familieHendelseRepository.hentAggregat(behandling);
        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(behandling);

        FagsakRelasjon fagsakRelasjon = fagsakRelasjonRepository.finnRelasjonFor(behandling.getFagsak());
        return new EkstraFlerbarnsukerRegelAdapter().beregnEkstraFlerbarnsuker(behandling, familieHendelseGrunnlag, ytelseFordelingAggregat, fagsakRelasjon);
    }
}
