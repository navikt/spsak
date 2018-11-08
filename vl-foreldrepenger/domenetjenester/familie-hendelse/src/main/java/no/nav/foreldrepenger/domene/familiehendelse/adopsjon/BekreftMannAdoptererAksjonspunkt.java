package no.nav.foreldrepenger.domene.familiehendelse.adopsjon;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.familiehendelse.BekreftAdopsjonsAksjonspunktDto;

public class BekreftMannAdoptererAksjonspunkt {

    private FamilieHendelseRepository familieGrunnlagRepository;

    public BekreftMannAdoptererAksjonspunkt(BehandlingRepositoryProvider repositoryProvider) {
        familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
    }

    public void oppdater(Behandling behandling, BekreftAdopsjonsAksjonspunktDto adapter) {
        final FamilieHendelseBuilder oppdatertOverstyrtHendelse = familieGrunnlagRepository.opprettBuilderFor(behandling);
        oppdatertOverstyrtHendelse
            .medAdopsjon(oppdatertOverstyrtHendelse.getAdopsjonBuilder()
                .medAdoptererAlene(adapter.getMannAdoptererAlene()));
        familieGrunnlagRepository.lagreOverstyrtHendelse(behandling, oppdatertOverstyrtHendelse);
    }
}
