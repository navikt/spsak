package no.nav.foreldrepenger.domene.familiehendelse.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.familiehendelse.BekreftAdopsjonsAksjonspunktDto;

class BekreftEktefelleAksjonspunkt {

    private FamilieHendelseRepository familieGrunnlagRepository;

    BekreftEktefelleAksjonspunkt(BehandlingRepositoryProvider repositoryProvider) {
        familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
    }

    void oppdater(Behandling behandling, BekreftAdopsjonsAksjonspunktDto adapter) {
        final FamilieHendelseBuilder oppdatertOverstyrtHendelse = familieGrunnlagRepository.opprettBuilderFor(behandling);
        oppdatertOverstyrtHendelse
            .medAdopsjon(oppdatertOverstyrtHendelse.getAdopsjonBuilder()
                .medErEktefellesBarn(adapter.getEktefellesBarn()));
        familieGrunnlagRepository.lagreOverstyrtHendelse(behandling, oppdatertOverstyrtHendelse);
    }
}
