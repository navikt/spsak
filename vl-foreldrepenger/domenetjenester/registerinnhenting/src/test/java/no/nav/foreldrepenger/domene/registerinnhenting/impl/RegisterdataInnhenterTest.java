package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.OpplysningsPeriodeTjeneste;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.OpplysningsPeriodeTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataEndringshåndterer;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataInnhenter;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class RegisterdataInnhenterTest {

    private static final String DURATION = "PT10H";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    private Instance<String> durationInstance;

    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        durationInstance = mock(Instance.class);
        when(durationInstance.get()).thenReturn(DURATION);
    }

    @Test
    public void skal_innhente_registeropplysninger_på_nytt_når_det_ble_hentet_inn_i_går() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medOpplysningerOppdatertTidspunkt(LocalDateTime.now().minusDays(1));
        Behandling behandling = scenario.lagMocked();

        // Act
        RegisterdataEndringshåndterer registerdataEndringshåndterer = lagRegisterdataInnhenter(scenario, durationInstance);
        Boolean harHentetInn = registerdataEndringshåndterer.skalInnhenteRegisteropplysningerPåNytt(behandling);

        // Assert
        assertThat(harHentetInn).isTrue();
    }

    @Test
    public void skal_ikke_innhente_registeropplysninger_på_nytt_når_det_nettopp_har_blitt_hentet_inn() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medOpplysningerOppdatertTidspunkt(LocalDateTime.now());
        Behandling behandling = scenario.lagMocked();

        // Act
        RegisterdataEndringshåndterer registerdataEndringshåndterer = lagRegisterdataInnhenter(scenario, durationInstance);
        Boolean harHentetInn = registerdataEndringshåndterer.skalInnhenteRegisteropplysningerPåNytt(behandling);

        // Assert
        assertThat(harHentetInn).isFalse();
    }

    @Test
    public void skal_ikke_innhente_registeropplysninger_på_nytt_når_det_ikke_har_blitt_hentet_inn_tidligere() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medOpplysningerOppdatertTidspunkt(null);
        Behandling behandling = scenario.lagMocked();

        // Act
        RegisterdataEndringshåndterer registerdataEndringshåndterer = lagRegisterdataInnhenter(scenario, durationInstance);
        Boolean harHentetInn = registerdataEndringshåndterer.skalInnhenteRegisteropplysningerPåNytt(behandling);

        // Assert
        assertThat(harHentetInn).isFalse();
    }

    @Test
    public void skal_innhente_registeropplysninger_ut_ifra_midnatt_når_konfigurasjonsverdien_mangler() {
        // Arrange
        LocalDateTime midnatt = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medOpplysningerOppdatertTidspunkt(midnatt.minusMinutes(1));
        Behandling behandling = scenario.lagMocked();
        RegisterdataEndringshåndterer registerdataEndringshåndterer = lagRegisterdataInnhenter(scenario, null);

        // Act
        Boolean harHentetInn = registerdataEndringshåndterer.skalInnhenteRegisteropplysningerPåNytt(behandling);

        // Assert
        assertThat(harHentetInn).isTrue();
    }

    @Test
    public void skal_innhente_registeropplysninger_mellom_midnatt_og_klokken_3_men_ikke_ellers_grunnet_konfigverdien() {
        // Arrange
        LocalDateTime midnatt = LocalDate.now().atStartOfDay();
        LocalDateTime opplysningerOppdatertTidspunkt = midnatt.minusHours(1); // en time før midnatt

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medOpplysningerOppdatertTidspunkt(opplysningerOppdatertTidspunkt);

        String redigertDURATION = "PT3H";
        when(durationInstance.get()).thenReturn(redigertDURATION);
        RegisterdataEndringshåndtererImpl registerdataOppdatererEngangsstønad = lagRegisterdataInnhenter(scenario, durationInstance);

        // Act
        Boolean skalInnhente = registerdataOppdatererEngangsstønad.erOpplysningerOppdatertTidspunktFør(midnatt,
            Optional.of(opplysningerOppdatertTidspunkt));

        // Assert
        assertThat(skalInnhente).isTrue();
        assertThat(durationInstance.get()).isEqualTo(redigertDURATION);
    }

    @Test
    public void skal_ikke_innhente_opplysninger_på_nytt_selvom_det_ble_hentet_inn_i_går_fordi_konfigverdien_er_mer_enn_midnatt() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medOpplysningerOppdatertTidspunkt(LocalDateTime.now().minusHours(20));
        Behandling behandling = scenario.lagMocked();

        String redigertDURATION = "PT30H";
        when(durationInstance.get()).thenReturn(redigertDURATION);
        RegisterdataEndringshåndterer registerdataEndringshåndterer = lagRegisterdataInnhenter(scenario, durationInstance);

        // Act
        Boolean harHentetInn = registerdataEndringshåndterer.skalInnhenteRegisteropplysningerPåNytt(behandling);

        // Assert
        assertThat(harHentetInn).isFalse();
        assertThat(durationInstance.get()).isEqualTo(redigertDURATION);
    }

    private RegisterdataEndringshåndtererImpl lagRegisterdataInnhenter(AbstractTestScenario<?> scenario, Instance<String> durationInstance) {
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        return lagRegisterdataOppdaterer(repositoryProvider, durationInstance);
    }

    private RegisterdataEndringshåndtererImpl lagRegisterdataOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                                                        Instance<String> durationInstance) {

        RegisterdataInnhenter innhenter = lagRegisterdataInnhenter(repositoryProvider);

        RegisterdataEndringshåndtererImpl oppdaterer = lagRegisterdataOppdaterer(repositoryProvider, durationInstance, innhenter);
        return oppdaterer;
    }

    private RegisterdataEndringshåndtererImpl lagRegisterdataOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                                                        Instance<String> durationInstance, RegisterdataInnhenter innhenter) {

        RegisterdataEndringshåndtererImpl oppdaterer = new RegisterdataEndringshåndtererImpl(
            repositoryProvider, innhenter, durationInstance, null, null, null, null);
        return oppdaterer;
    }

    private RegisterdataInnhenterImpl lagRegisterdataInnhenter(BehandlingRepositoryProvider repositoryProvider) {
        SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
        PersoninfoAdapter personinfoAdapter = mock(PersoninfoAdapter.class);
        MedlemTjeneste medlemTjeneste = mock(MedlemTjeneste.class);
        ProsessTaskRepository prosessTaskRepository = mock(ProsessTaskRepository.class);
        OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste = new OpplysningsPeriodeTjenesteImpl(skjæringstidspunktTjeneste,
            Period.of(0, 4, 0), Period.of(1, 0, 0));
        BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste = new BehandlingskontrollTaskTjenesteImpl(prosessTaskRepository);

        return new RegisterdataInnhenterImpl(personinfoAdapter, medlemTjeneste,
            skjæringstidspunktTjeneste, behandlingskontrollTaskTjeneste, repositoryProvider, null, null,
            opplysningsPeriodeTjeneste);
    }
}
