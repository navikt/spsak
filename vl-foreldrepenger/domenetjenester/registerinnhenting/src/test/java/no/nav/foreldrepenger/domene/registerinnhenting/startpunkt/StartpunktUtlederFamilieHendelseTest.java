package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import static no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class StartpunktUtlederFamilieHendelseTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    @Mock
    SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private StartpunktUtlederFamilieHendelse utleder;

    @Before
    public void oppsett() {
        initMocks(this);
        utleder = new StartpunktUtlederFamilieHendelse(repositoryProvider.getFamilieGrunnlagRepository(), skjæringstidspunktTjeneste);
    }

    @Test
    public void skal_returnere_startpunkt_opplysningsplikt_dersom_familiehendelse_bekreftes_og_endrer_skjæringspunkt() {
        // Arrange
        LocalDate origSkjæringsdato = LocalDate.now();
        LocalDate nyBekreftetfødselsdato = origSkjæringsdato.minusDays(1); // fødselsdato før skjæringstidspunkt

        ScenarioMorSøkerForeldrepenger førstegangScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD);
        Behandling originalBehandling = førstegangScenario.lagre(repositoryProvider);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(originalBehandling)).thenReturn(origSkjæringsdato);

        ScenarioMorSøkerForeldrepenger revurderingScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.REVURDERING);
        revurderingScenario.medOriginalBehandling(originalBehandling, BehandlingÅrsakType.RE_MANGLER_FØDSEL);
        revurderingScenario.medBekreftetHendelse().medFødselsDato(nyBekreftetfødselsdato);
        Behandling revurdering = revurderingScenario.lagre(repositoryProvider);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(revurdering)).thenReturn(origSkjæringsdato);

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurdering, 1L, 2L)).isEqualTo(INNGANGSVILKÅR_OPPLYSNINGSPLIKT);
    }

    @Test
    public void skal_returnere_startpunkt_opplysningsplikt_dersom_familiehendelse_flyttes_til_tidligere_dato() {
        // Arrange
        LocalDate origSkjæringsdato = LocalDate.now();
        LocalDate origBekreftetfødselsdato = origSkjæringsdato;
        LocalDate nyBekreftetfødselsdato = origSkjæringsdato.minusDays(1); // fødselsdato før skjæringstidspunkt

        ScenarioMorSøkerForeldrepenger førstegangScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD);
        førstegangScenario.medBekreftetHendelse().medFødselsDato(origBekreftetfødselsdato);
        Behandling originalBehandling = førstegangScenario.lagre(repositoryProvider);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(originalBehandling)).thenReturn(origSkjæringsdato);

        ScenarioMorSøkerForeldrepenger revurderingScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.REVURDERING);
        revurderingScenario.medOriginalBehandling(originalBehandling, BehandlingÅrsakType.RE_MANGLER_FØDSEL);
        revurderingScenario.medBekreftetHendelse().medFødselsDato(nyBekreftetfødselsdato);
        Behandling revurdering = revurderingScenario.lagre(repositoryProvider);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(revurdering)).thenReturn(origSkjæringsdato);

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurdering, 1L, 2L)).isEqualTo(INNGANGSVILKÅR_OPPLYSNINGSPLIKT);
    }

    @Test
    public void skal_returnere_startpunkt_opplysningsplikt_dersom_orig_skjæringstidspunkt_flyttes_tidligere() {
        // Arrange
        LocalDate origSkjæringsdato = LocalDate.now();
        LocalDate nySkjæringsdato = LocalDate.now().minusDays(1);

        ScenarioMorSøkerForeldrepenger førstegangScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD);
        Behandling originalBehandling = førstegangScenario.lagre(repositoryProvider);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(originalBehandling)).thenReturn(origSkjæringsdato);

        ScenarioMorSøkerForeldrepenger revurderingScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.REVURDERING);
        revurderingScenario.medOriginalBehandling(originalBehandling, BehandlingÅrsakType.RE_MANGLER_FØDSEL);
        Behandling revurdering = revurderingScenario.lagre(repositoryProvider);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(revurdering)).thenReturn(nySkjæringsdato);

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurdering, 1L, 2L)).isEqualTo(INNGANGSVILKÅR_OPPLYSNINGSPLIKT);
    }
}
