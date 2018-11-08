package no.nav.foreldrepenger.domene.kontrollerfakta.fødsel;

import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.JA;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.NEI;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Aksjonspunkter for søknad om engangsstønad for fødsel
 */
abstract class AksjonspunktUtlederForFødsel implements AksjonspunktUtleder {

    protected FamilieHendelseRepository familieGrunnlagRepository;
    protected AksjonspunktRepository aksjonspunktRepository;
    protected InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;


    AksjonspunktUtlederForFødsel(BehandlingRepositoryProvider repositoryProvider, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    AksjonspunktUtlederForFødsel() {
    }


    Utfall erSøkerRegistrertArbeidstakerMedLøpendeArbeidsforholdIAARegisteret(Behandling behandling) {
        if (inntektArbeidYtelseRepository.harArbeidsforholdMedArbeidstyperSomAngitt(behandling, ArbeidType.AA_REGISTER_TYPER,
            skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling))) {
            return JA;
        }
        return NEI;
    }

    Utfall samsvarerAntallBarnISøknadMedAntallBarnITps(FamilieHendelse søknadsVersjon, FamilieHendelse bekreftetVersjon) {
        return søknadsVersjon.getAntallBarn().equals(bekreftetVersjon.getAntallBarn()) ? JA : NEI;
    }

    Utfall erDagensDato14DagerEtterOppgittFødselsdato(FamilieHendelse søknadsVersjon) {
        final Optional<LocalDate> fødselsdato = søknadsVersjon.getBarna().stream()
            .map(UidentifisertBarn::getFødselsdato)
            .findFirst();
        return fødselsdato
            .filter(localDate -> LocalDate.now(FPDateUtil.getOffset()).isAfter(localDate.plusDays(14)))
            .map(utfall -> JA)
            .orElse(NEI);
    }

    LocalDateTime utledVentefrist(FamilieHendelse søknadsVersjon) {
        LocalDate venteFrist = søknadsVersjon.getBarna().stream()
            .map(barn -> barn.getFødselsdato().plusDays(14))
            .findFirst()
            .orElse(LocalDate.now(FPDateUtil.getOffset()));
        return LocalDateTime.of(venteFrist, LocalDateTime.now(FPDateUtil.getOffset()).toLocalTime());
    }

    Utfall erDagensDato25DagerEtterOppgittTerminsdato(FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        final Optional<Terminbekreftelse> gjellendeTerminbekreftelse = familieHendelseGrunnlag.getGjeldendeTerminbekreftelse();
        return gjellendeTerminbekreftelse.filter(terminbekreftelse -> LocalDate.now(FPDateUtil.getOffset()).isAfter(terminbekreftelse.getTermindato().plusDays(25)))
            .map(terminbekreftelse -> JA).orElse(NEI);
    }

    Utfall harSøkerOppgittFødselISøknad(FamilieHendelse versjon) {
        final Optional<UidentifisertBarn> uidentifisertBarn = versjon.getBarna().stream().findFirst();
        return uidentifisertBarn.isPresent() ? JA : NEI;
    }

    Utfall erFødselenRegistrertITps(FamilieHendelse familieHendelse) {
        return (familieHendelse == null || familieHendelse.getBarna().isEmpty()) ? NEI : JA;
    }

    protected abstract Utfall gjelderSøknadenForeldrepenger();

}
