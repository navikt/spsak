package no.nav.foreldrepenger.domene.medlem.impl;


import static no.nav.foreldrepenger.domene.medlem.impl.MedlemResultat.AVKLAR_OM_ER_BOSATT;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOpphold;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOppholdEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class AvklarBarnFødtUtenlandsTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider provider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private AvklarBarnFødtUtenlands tjeneste;

    @Before
    public void setUp() {
        this.tjeneste = new AvklarBarnFødtUtenlands(provider);
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_om_søker_har_oppholdt_seg_i_Norge_de_siste_12_måneder() {
        //Arrange
        LocalDate fødselsdato = LocalDate.now().minusDays(5L);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(fødselsdato);

        Behandling behandling = scenario.lagre(provider);

        // Act
        Optional<MedlemResultat> medlemResultat = tjeneste.utled(behandling, fødselsdato);

        //Assert
        assertThat(medlemResultat).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_om_det_ikke_er_søkt_på_bakgrunn_av_fødsel() {
        //Arrange
        LocalDate termindato = LocalDate.now().minusDays(5L); // Oppgir termindato, dvs. søknad ikke basert på fødsel
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel();

        scenario.medSøknadHendelse().medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
            .medUtstedtDato(LocalDate.now())
            .medTermindato(termindato)
            .medNavnPå("LEGEN MIN"));

        LocalDate oppholdStart = termindato.minusDays(2L);
        LocalDate oppholdSlutt = termindato.plusDays(2L);

        OppgittLandOpphold danmark = lagUtlandsopphold(oppholdStart, oppholdSlutt);
        scenario.medOppgittTilknytning().leggTilOpphold(danmark);

        Behandling behandling = scenario.lagre(provider);

        // Act
        Optional<MedlemResultat> medlemResultat = tjeneste.utled(behandling, termindato);

        //Assert
        assertThat(medlemResultat).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_om_søkers_barn_er_født_i_Norge() {
        //Arrange
        LocalDate fødselsdato = LocalDate.now().minusDays(2L);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(fødselsdato);

        LocalDate oppholdStart = fødselsdato.minusDays(20L);
        LocalDate oppholdSlutt = fødselsdato.minusDays(5L);

        OppgittLandOpphold danmark = lagUtlandsopphold(oppholdStart, oppholdSlutt);
        scenario.medOppgittTilknytning().leggTilOpphold(danmark);

        Behandling behandling = scenario.lagre(provider);

        // Act
        Optional<MedlemResultat> medlemResultat = tjeneste.utled(behandling, fødselsdato);

        //Assert
        assertThat(medlemResultat).isEmpty();
    }

    @Test
    public void skal_opprette_aksjonspunkt_om_søkers_barn_fra_søknad_er_født_i_utlandet() {
        //Arrange
        LocalDate fødselsdato = LocalDate.now().minusDays(5L);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(fødselsdato);

        LocalDate oppholdStart = fødselsdato.minusDays(2L);
        LocalDate oppholdSlutt = fødselsdato.plusDays(2L);

        OppgittLandOpphold danmark = lagUtlandsopphold(oppholdStart, oppholdSlutt);
        scenario.medOppgittTilknytning().leggTilOpphold(danmark);

        Behandling behandling = scenario.lagre(provider);

        // Act
        Optional<MedlemResultat> medlemResultat = tjeneste.utled(behandling, fødselsdato);

        //Assert
        assertThat(medlemResultat).contains(AVKLAR_OM_ER_BOSATT);
    }

    @Test
    public void skal_opprette_aksjonspunkt_om_søkers_barn_fra_tps_er_født_i_utlandet() {
        //Arrange
        LocalDate fødselsdato = LocalDate.now().minusDays(5L);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(fødselsdato);

        LocalDate oppholdStart = fødselsdato.minusDays(2L);
        LocalDate oppholdSlutt = fødselsdato.plusDays(2L);

        OppgittLandOpphold danmark = lagUtlandsopphold(oppholdStart, oppholdSlutt);
        scenario.medOppgittTilknytning().leggTilOpphold(danmark);

        Behandling behandling = scenario.lagre(provider);

        // Act
        Optional<MedlemResultat> medlemResultat = tjeneste.utled(behandling, fødselsdato);

        //Assert
        assertThat(medlemResultat).contains(AVKLAR_OM_ER_BOSATT);
    }

    private OppgittLandOpphold lagUtlandsopphold(LocalDate oppholdStart, LocalDate oppholdSlutt) {
        return new OppgittLandOppholdEntitet.Builder()
            .erTidligereOpphold(false)
            .medLand(Landkoder.SWE)
            .medPeriode(oppholdStart, oppholdSlutt)
            .build();
    }
}
