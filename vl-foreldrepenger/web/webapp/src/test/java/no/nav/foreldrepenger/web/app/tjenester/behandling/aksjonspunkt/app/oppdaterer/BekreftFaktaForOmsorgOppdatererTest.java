package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.ytelsefordeling.YtelseFordelingTjeneste;
import no.nav.foreldrepenger.domene.ytelsefordeling.impl.YtelseFordelingTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.omsorg.BekreftFaktaForOmsorgVurderingDto.BekreftAleneomsorgVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.omsorg.BekreftFaktaForOmsorgVurderingDto.BekreftOmsorgVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class BekreftFaktaForOmsorgOppdatererTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();

    private BehandlingRepositoryProvider behandlingRepositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private YtelseFordelingTjeneste ytelseFordelingTjeneste = new YtelseFordelingTjenesteImpl(behandlingRepositoryProvider);

    @Test
    public void skal_generere_historikkinnslag_ved_avklaring_av_aleneomsorg() {
        // Arrange
        boolean oppdatertAleneOmsorg = false;

        // Behandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();

        scenario.medSøknad();
        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, false, true);
        scenario.medOppgittRettighet(rettighet);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG,
            BehandlingStegType.VURDER_UTTAK);

        scenario.lagre(behandlingRepositoryProvider);

        Behandling behandling = scenario.getBehandling();

        // Dto
        BekreftAleneomsorgVurderingDto dto = new BekreftAleneomsorgVurderingDto("begrunnelse", oppdatertAleneOmsorg, null, null);

        // Act
        new BekreftFaktaForOmsorgOppdaterer(behandlingRepositoryProvider, lagMockHistory(), ytelseFordelingTjeneste){}
            .oppdater(dto, behandling);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkinnslagDeler = tekstBuilder.build(historikkinnslag);

        // Assert
        assertThat(historikkinnslagDeler).hasSize(1);
        HistorikkinnslagDel del = historikkinnslagDeler.get(0);
        Optional<HistorikkinnslagFelt> aleneomsorgOpt = del.getEndretFelt(HistorikkEndretFeltType.ALENEOMSORG);
        assertThat(aleneomsorgOpt).hasValueSatisfying(aleneomsorg -> {
            assertThat(aleneomsorg.getNavn()).isEqualTo(HistorikkEndretFeltType.ALENEOMSORG.getKode());
            assertThat(aleneomsorg.getFraVerdi()).isNull();
            assertThat(aleneomsorg.getTilVerdi()).isEqualTo(HistorikkEndretFeltVerdiType.IKKE_ALENEOMSORG.getKode());
        });
    }

    @Test
    public void skal_generere_historikkinnslag_ved_avklaring_av_omsorg() {
        // Arrange
        boolean oppdatertOmsorg = true;

        // Behandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();

        scenario.medSøknad();
        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, false, true);
        scenario.medOppgittRettighet(rettighet);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG,
            BehandlingStegType.VURDER_UTTAK);

        scenario.lagre(behandlingRepositoryProvider);

        Behandling behandling = scenario.getBehandling();

        // Dto
        BekreftOmsorgVurderingDto dto = new BekreftOmsorgVurderingDto("begrunnelse", null, oppdatertOmsorg, null);

        // Act
        new BekreftFaktaForOmsorgOppdaterer(behandlingRepositoryProvider, lagMockHistory(), ytelseFordelingTjeneste) {}
            .oppdater(dto, behandling);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkinnslagDeler = tekstBuilder.build(historikkinnslag);

        // Assert
        assertThat(historikkinnslagDeler).hasSize(1);
        HistorikkinnslagDel del = historikkinnslagDeler.get(0);
        Optional<HistorikkinnslagFelt> omsorgOpt = del.getEndretFelt(HistorikkEndretFeltType.OMSORG);
        assertThat(omsorgOpt).hasValueSatisfying(aleneomsorg -> {
            assertThat(aleneomsorg.getNavn()).isEqualTo(HistorikkEndretFeltType.OMSORG.getKode());
            assertThat(aleneomsorg.getFraVerdi()).isNull();
            assertThat(aleneomsorg.getTilVerdi()).isEqualTo("Søker har omsorg for barnet");
        });
    }

    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = Mockito.mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }
}
