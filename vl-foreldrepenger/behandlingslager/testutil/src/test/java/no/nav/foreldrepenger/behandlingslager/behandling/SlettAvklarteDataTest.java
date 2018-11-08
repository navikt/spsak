package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.UidentifisertBarnEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class SlettAvklarteDataTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final Repository repository = repoRule.getRepository();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final MedlemskapRepository medlemskapRepository = repositoryProvider.getMedlemskapRepository();
    private FamilieHendelseRepository familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();


    @Test
    public void skal_slette_avklarte_omsorgsovertakelsedata() {
        // Arrange
        final ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknadHendelse(scenario.medSøknadHendelse()
            .medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(LocalDate.now())).medAntallBarn(1));
        scenario.medBekreftetHendelse(scenario.medBekreftetHendelse()
            .medAdopsjon(scenario.medBekreftetHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(LocalDate.now())).medAntallBarn(1));

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        // Act
        familieHendelseRepository.slettAvklarteData(behandling, lås);
        repository.flushAndClear();

        // Assert
        Behandling opphentetBehandling = repository.hent(Behandling.class, behandling.getId());
        final FamilieHendelseGrunnlag grunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(opphentetBehandling);
        assertThat(grunnlag).isNotNull();
        assertThat(grunnlag.getOverstyrtVersjon().flatMap(FamilieHendelse::getAdopsjon)).isNotPresent();
        assertThat(grunnlag.getOverstyrtVersjon().map(FamilieHendelse::getAntallBarn)).isNotPresent();
        assertThat(grunnlag.getOverstyrtVersjon().map(FamilieHendelse::getBarna)).isNotPresent();
    }

    @Test
    public void skal_slette_avklarte_fødseldata() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse(scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now()).medNavnPå("LEGESEN").medUtstedtDato(LocalDate.now()))
            .medFødselsDato(LocalDate.now())
            .medAntallBarn(1));
        scenario.medBekreftetHendelse(scenario.medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now()).medNavnPå("LEGESEN").medUtstedtDato(LocalDate.now()))
            .medFødselsDato(LocalDate.now())
            .medAntallBarn(1));
        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        // Act
        familieHendelseRepository.slettAvklarteData(behandling, lås);
        repository.flushAndClear();

        // Assert
        Behandling opphentetBehandling = repository.hent(Behandling.class, behandling.getId());
        final FamilieHendelseGrunnlag grunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(opphentetBehandling);
        assertThat(grunnlag).isNotNull();
        assertThat(grunnlag.getOverstyrtVersjon().flatMap(FamilieHendelse::getTerminbekreftelse)).isNotPresent();
        assertThat(grunnlag.getOverstyrtVersjon().map(FamilieHendelse::getAntallBarn)).isNotPresent();
        assertThat(grunnlag.getOverstyrtVersjon().map(FamilieHendelse::getBarna)).isNotPresent();
    }

    @Test
    public void skal_slette_avklarte_adopsjonsdata() {
        // Arrange
        final ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        final FamilieHendelseBuilder familieHendelseBuilder = scenario.medSøknadHendelse();
        familieHendelseBuilder.medAntallBarn(1)
            .medFødselsDato(LocalDate.now())
            .medAdopsjon(familieHendelseBuilder.getAdopsjonBuilder().medOmsorgsovertakelseDato(LocalDate.now()));
        scenario.medBekreftetHendelse(scenario.medBekreftetHendelse()
            .medAdopsjon(scenario.medBekreftetHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(LocalDate.now()))
            .leggTilBarn(new UidentifisertBarnEntitet(LocalDate.now(), 1)));

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        // Act
        familieHendelseRepository.slettAvklarteData(behandling, lås);
        repository.flushAndClear();

        // Assert
        Behandling opphentetBehandling = repository.hent(Behandling.class, behandling.getId());
        final Optional<Adopsjon> adopsjon = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(opphentetBehandling).getOverstyrtVersjon().flatMap(FamilieHendelse::getAdopsjon);
        assertThat(adopsjon).isNotPresent();
    }

    @Test
    public void skal_slette_avklarte_medlemskapdata() {
        // Arrange
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        final FamilieHendelseBuilder familieHendelseBuilder = scenario.medSøknadHendelse();
        familieHendelseBuilder.medAntallBarn(1)
            .medFødselsDato(LocalDate.now())
            .medAdopsjon(familieHendelseBuilder.getAdopsjonBuilder().medOmsorgsovertakelseDato(LocalDate.now()));
        scenario.medMedlemskap().build();
        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        // Act
        medlemskapRepository.slettAvklarteMedlemskapsdata(behandling, lås);
        repository.flushAndClear();

        // Assert
        Optional<MedlemskapAggregat> medlemskap = medlemskapRepository.hentMedlemskap(behandling);
        assertThat(medlemskap).isPresent();

        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskapRepository.hentVurdertMedlemskap(behandling);
        assertThat(vurdertMedlemskap).isNotPresent();

        assertThat(medlemskap.get().getVurdertMedlemskap()).isNotPresent();
    }
}
