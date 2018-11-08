package no.nav.foreldrepenger.domene.uttak.beregnkontoer.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.domene.uttak.beregnkontoer.BeregnStønadskontoerTjeneste;

@ApplicationScoped
public class BeregnStønadskontoerTjenesteImpl implements BeregnStønadskontoerTjeneste {

    private FamilieHendelseRepository familieHendelseRepository;
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private FagsakRelasjonRepository fagsakRelasjonRepository;


    BeregnStønadskontoerTjenesteImpl() {
        //For CDI
    }

    @Inject
    public BeregnStønadskontoerTjenesteImpl(BehandlingRepositoryProvider repositoryProvider) {
        this.familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.fagsakRelasjonRepository = repositoryProvider.getFagsakRelasjonRepository();
    }

    @Override
    public Stønadskontoberegning beregnStønadskontoer(Behandling behandling) {
        FamilieHendelseGrunnlag familieHendelseGrunnlag = familieHendelseRepository.hentAggregat(behandling);
        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(behandling);
        Stønadskontoberegning stønadskontoberegning = new StønadskontoRegelAdapter(fagsakRelasjonRepository).beregnKontoer(behandling, familieHendelseGrunnlag, ytelseFordelingAggregat);
        fagsakRelasjonRepository.lagre(behandling, stønadskontoberegning);
        return stønadskontoberegning;
    }
}
