package no.nav.foreldrepenger.web.app.tjenester.behandling.ytelsefordeling;

import static org.assertj.core.api.Assertions.assertThat;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.ytelsefordeling.YtelseFordelingTjeneste;
import no.nav.foreldrepenger.domene.ytelsefordeling.impl.YtelseFordelingTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer.BekreftFaktaForOmsorgOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.omsorg.BekreftFaktaForOmsorgVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

public class YtelseFordelingDtoTjenesteImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private YtelseFordelingTjeneste tjeneste = new YtelseFordelingTjenesteImpl(repositoryProvider);
    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();

    @Test
    public void teste_lag_ytelsefordeling_dto() {
        Behandling behandling = opprettBehandling();
        BekreftFaktaForOmsorgVurderingDto.BekreftAleneomsorgVurderingDto dto = new BekreftFaktaForOmsorgVurderingDto.BekreftAleneomsorgVurderingDto("begrunnelse", true, null, null);

        // Act
        new BekreftFaktaForOmsorgOppdaterer(repositoryProvider, lagMockHistory(), tjeneste) {}
            .oppdater(dto, behandling);
        Optional<YtelseFordelingDto> ytelseFordelingDtoOpt = new YtelseFordelingDtoTjenesteImpl(tjeneste).mapFra(behandling);
        assertThat(ytelseFordelingDtoOpt).isNotNull();
        assertThat(ytelseFordelingDtoOpt.get().getAleneOmsorgPerioder()).isNotNull();
        assertThat(ytelseFordelingDtoOpt.get().getAleneOmsorgPerioder()).hasSize(1);
        assertThat(ytelseFordelingDtoOpt.get().getEndringsDato()).isEqualTo(LocalDate.now());
    }

    private Behandling opprettBehandling() {
        // Arrange
        LocalDate termindato = LocalDate.now().plusWeeks(16);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medNavnPå("LEGENS ISNDASD")
                .medUtstedtDato(termindato)
                .medTermindato(termindato));

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, false, true);
        scenario.medOppgittRettighet(rettighet);
        scenario.medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(LocalDate.now(), LocalDate.now()));
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG,
            BehandlingStegType.VURDER_UTTAK);
        Behandling behandling = scenario.lagre(repositoryProvider);
        return behandling;
    }

    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = Mockito.mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }
}
