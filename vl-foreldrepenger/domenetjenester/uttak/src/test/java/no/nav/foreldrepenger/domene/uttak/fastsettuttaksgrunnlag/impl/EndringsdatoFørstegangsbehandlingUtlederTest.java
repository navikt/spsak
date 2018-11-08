package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class EndringsdatoFørstegangsbehandlingUtlederTest {

    private static final LocalDate FØRSTE_UTTAKSDATO_OPPGITT = LocalDate.now().plusDays(10);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private EndringsdatoFørstegangsbehandlingUtleder endringsdatoFørstegangsbehandlingUtleder;

    @Before
    public void before() {
        endringsdatoFørstegangsbehandlingUtleder = new EndringsdatoFørstegangsbehandlingUtleder(repositoryProvider.getYtelsesFordelingRepository());
    }

    @Test
    public void skal_utlede_at_endringsdatoen_er_første_uttaksdato_i_søknaden_når_det_ikke_finnes_manuell_vurdering() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medFordeling(opprettOppgittFordeling());
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        LocalDate endringsdato = endringsdatoFørstegangsbehandlingUtleder.utledEndringsdato(behandling);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO_OPPGITT);
    }

    @Test
    public void skal_utlede_at_endringsdatoen_er_første_uttaksdato_i_søknaden_når_manuell_vurdering_er_senere() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medFordeling(opprettOppgittFordeling())
            .medAvklarteUttakDatoer(opprettAvklarteUttakDatoer(FØRSTE_UTTAKSDATO_OPPGITT.plusDays(1)));
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        LocalDate endringsdato = endringsdatoFørstegangsbehandlingUtleder.utledEndringsdato(behandling);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO_OPPGITT);
    }

    @Test
    public void skal_utlede_at_endringsdatoen_er_manuelt_vurdert_uttaksdato_når_manuell_vurdering_er_tidligere() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medFordeling(opprettOppgittFordeling())
            .medAvklarteUttakDatoer(opprettAvklarteUttakDatoer(FØRSTE_UTTAKSDATO_OPPGITT.minusDays(1)));
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        LocalDate endringsdato = endringsdatoFørstegangsbehandlingUtleder.utledEndringsdato(behandling);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO_OPPGITT.minusDays(1));
    }

    private OppgittFordeling opprettOppgittFordeling() {
        OppgittPeriodeBuilder periode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(FØRSTE_UTTAKSDATO_OPPGITT, FØRSTE_UTTAKSDATO_OPPGITT.plusWeeks(2))
            .medVirksomhet(opprettOgLagreVirksomhet());

        return new OppgittFordelingEntitet(singletonList(periode.build()), true);
    }

    private Virksomhet opprettOgLagreVirksomhet() {
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("75674554355")
            .medNavn("Virksomhet")
            .medRegistrert(LocalDate.now().minusYears(10L))
            .medOppstart(LocalDate.now().minusYears(10L))
            .oppdatertOpplysningerNå()
            .build();
        repoRule.getEntityManager().persist(virksomhet);
        return virksomhet;
    }

    private AvklarteUttakDatoer opprettAvklarteUttakDatoer(LocalDate førsteUttaksdato) {
        return new AvklarteUttakDatoerEntitet(førsteUttaksdato, null);
    }
}
