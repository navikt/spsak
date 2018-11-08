package no.nav.foreldrepenger.domene.familiehendelse.fødsel;

import java.util.stream.IntStream;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.familiehendelse.AvklarManglendeFødselAksjonspunktDto;

public class AvklarManglendeFødselAksjonspunkt {
    private FamilieHendelseRepository familieGrunnlagRepository;

    public AvklarManglendeFødselAksjonspunkt(BehandlingRepositoryProvider repositoryProvider) {
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
    }

    public void oppdater(Behandling behandling, AvklarManglendeFødselAksjonspunktDto adapter) {
        final FamilieHendelseBuilder oppdatertOverstyrtHendelse = familieGrunnlagRepository.opprettBuilderFor(behandling);
        oppdatertOverstyrtHendelse
            .tilbakestillBarn()
            .medAntallBarn(adapter.getAntallBarn())
            .erFødsel() // Settes til fødsel for å sikre at typen blir fødsel selv om det ikke er født barn.
            .medErMorForSykVedFødsel(null); // FIXME (Erlend): Hvorfor settes den her? Har egentlig ikke med dette å gjøre.
        // TODO: Legge til dødsdato.
        IntStream.range(0, adapter.getAntallBarn()).forEach(it ->
            oppdatertOverstyrtHendelse.medFødselsDato(adapter.getFodselsdato()));
        familieGrunnlagRepository.lagreOverstyrtHendelse(behandling, oppdatertOverstyrtHendelse);
    }
}
