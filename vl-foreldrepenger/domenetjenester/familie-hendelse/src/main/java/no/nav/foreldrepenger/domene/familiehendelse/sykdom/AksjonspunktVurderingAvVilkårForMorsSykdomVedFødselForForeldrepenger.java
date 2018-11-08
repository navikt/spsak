package no.nav.foreldrepenger.domene.familiehendelse.sykdom;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.familiehendelse.VurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger;

public class AksjonspunktVurderingAvVilkårForMorsSykdomVedFødselForForeldrepenger {

    private FamilieHendelseRepository familieGrunnlagRepository;

    public AksjonspunktVurderingAvVilkårForMorsSykdomVedFødselForForeldrepenger(BehandlingRepositoryProvider repositoryProvider) {
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
    }

    public void oppdater(Behandling behandling, VurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger syksomVedFødselForForeldrepenger) {
        final FamilieHendelseBuilder oppdatertOverstyrtHendelse = familieGrunnlagRepository.opprettBuilderFor(behandling);
        oppdatertOverstyrtHendelse
            .medErMorForSykVedFødsel(syksomVedFødselForForeldrepenger.getErMorForSykVedFøsel());
        familieGrunnlagRepository.lagreOverstyrtHendelse(behandling, oppdatertOverstyrtHendelse);
    }
}
