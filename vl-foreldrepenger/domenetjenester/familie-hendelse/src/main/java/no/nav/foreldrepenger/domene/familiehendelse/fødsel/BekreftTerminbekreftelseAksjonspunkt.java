package no.nav.foreldrepenger.domene.familiehendelse.fødsel;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.familiehendelse.TerminbekreftelseAksjonspunktDto;

public class BekreftTerminbekreftelseAksjonspunkt {

    private FamilieHendelseRepository familieGrunnlagRepository;

    BekreftTerminbekreftelseAksjonspunkt() {
        // for CDI proxy
    }

    public BekreftTerminbekreftelseAksjonspunkt(BehandlingRepositoryProvider repositoryProvider) {
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
    }

    public void oppdater(Behandling behandling, TerminbekreftelseAksjonspunktDto adapter) {
        final FamilieHendelseGrunnlag eksisterendeGrunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        final FamilieHendelseBuilder oppdatertOverstyrtHendelse = familieGrunnlagRepository.opprettBuilderFor(behandling);
        oppdatertOverstyrtHendelse
                .medTerminbekreftelse(oppdatertOverstyrtHendelse.getTerminbekreftelseBuilder()
                    .medNavnPå(eksisterendeGrunnlag.getSøknadVersjon().getTerminbekreftelse().map(Terminbekreftelse::getNavnPå).orElse("Ukjent opphav"))
                    .medTermindato(adapter.getTermindato())
                    .medUtstedtDato(adapter.getUtstedtdato()))
                .medAntallBarn(adapter.getAntallBarn());
        familieGrunnlagRepository.lagreOverstyrtHendelse(behandling, oppdatertOverstyrtHendelse);
    }
}
