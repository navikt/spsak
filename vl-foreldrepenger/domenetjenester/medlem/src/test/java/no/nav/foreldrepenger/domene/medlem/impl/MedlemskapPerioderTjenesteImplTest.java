package no.nav.foreldrepenger.domene.medlem.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;

import org.junit.Test;

import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.medlem.api.MedlemskapPerioderTjeneste;

public class MedlemskapPerioderTjenesteImplTest {

    private static final int ANTALL_MÅNEDER_INNHENTET_FØR_SKJÆRINGSDATO = 12;
    private static final int ANTALL_MÅNEDER_INNHENTET_ETTER_SKJÆRINGSDATO = 6;

    @Test
    public void skal_beregne_skjæringsdato_for_fødselsdato_avklart_av_saksbehandler() {
        // Arrange
        LocalDate oppgittFødselsdato = LocalDate.now().minusYears(10); // Skal ikke benyttes
        LocalDate avklartFøldselsdato = LocalDate.now(); // Skal benyttes

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();

        // Act/Assert
        Behandling b = scenario.lagMocked();
        final MedlemskapPerioderTjeneste medlemskapUtil = opprettTjeneste(scenario);
        assertThat(medlemskapUtil.erNySkjæringsdatoUtenforInnhentetMedlemskapsintervall(b,
            avklartFøldselsdato.minusMonths(ANTALL_MÅNEDER_INNHENTET_FØR_SKJÆRINGSDATO))).isFalse();
        assertThat(medlemskapUtil.erNySkjæringsdatoUtenforInnhentetMedlemskapsintervall(b,
            avklartFøldselsdato.minusMonths(ANTALL_MÅNEDER_INNHENTET_FØR_SKJÆRINGSDATO + 1))).isTrue();
        assertThat(medlemskapUtil.erNySkjæringsdatoUtenforInnhentetMedlemskapsintervall(b,
            avklartFøldselsdato.plusMonths(ANTALL_MÅNEDER_INNHENTET_ETTER_SKJÆRINGSDATO))).isFalse();
        assertThat(medlemskapUtil.erNySkjæringsdatoUtenforInnhentetMedlemskapsintervall(b,
            avklartFøldselsdato.plusMonths(ANTALL_MÅNEDER_INNHENTET_ETTER_SKJÆRINGSDATO + 1))).isTrue();
    }

    @Test
    public void skal_beregne_skjæringsdato_for_fødselsdato_oppgitt_av_søker() {
        // Arrange
        LocalDate oppgittFødselsdato = LocalDate.now();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();

        // Skulle helst ha bygd med ScenarioMorSøkerEngangsstønad, men det introduserer sirkulær maven-dependency
        Behandling b = scenario.lagMocked();
        final MedlemskapPerioderTjeneste medlemskapUtil = opprettTjeneste(scenario);

        // Act/Assert
        assertThat(medlemskapUtil.erNySkjæringsdatoUtenforInnhentetMedlemskapsintervall(b,
            oppgittFødselsdato.minusMonths(ANTALL_MÅNEDER_INNHENTET_FØR_SKJÆRINGSDATO))).isFalse();
        assertThat(medlemskapUtil.erNySkjæringsdatoUtenforInnhentetMedlemskapsintervall(b,
            oppgittFødselsdato.minusMonths(ANTALL_MÅNEDER_INNHENTET_FØR_SKJÆRINGSDATO + 1))).isTrue();
        assertThat(medlemskapUtil.erNySkjæringsdatoUtenforInnhentetMedlemskapsintervall(b,
            oppgittFødselsdato.plusMonths(ANTALL_MÅNEDER_INNHENTET_ETTER_SKJÆRINGSDATO))).isFalse();
        assertThat(medlemskapUtil.erNySkjæringsdatoUtenforInnhentetMedlemskapsintervall(b,
            oppgittFødselsdato.plusMonths(ANTALL_MÅNEDER_INNHENTET_ETTER_SKJÆRINGSDATO + 1))).isTrue();
    }

    private MedlemskapPerioderTjeneste opprettTjeneste(AbstractTestScenario<?> scenario) {
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        return new MedlemskapPerioderTjenesteImpl(ANTALL_MÅNEDER_INNHENTET_FØR_SKJÆRINGSDATO, ANTALL_MÅNEDER_INNHENTET_ETTER_SKJÆRINGSDATO,
            new SkjæringstidspunktTjenesteImpl(repositoryProvider,
                new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
                Period.of(0, 3, 0),
                Period.of(0, 10, 0)));
    }
}
