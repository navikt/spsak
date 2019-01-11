package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.inngangsvilkaar.InngangsvilkårTjeneste;
import no.nav.foreldrepenger.domene.inngangsvilkaar.impl.InngangsvilkårTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringOpptjeningsvilkåretDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening.OpptjeningsvilkåretOverstyringshåndterer;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapterImpl;
import no.nav.foreldrepenger.web.app.tjenester.historikk.dto.HistorikkInnslagKonverter;
import no.nav.vedtak.exception.FunksjonellException;

// Gaar amok paa CPUen.. så ignorerer inntil videre
@Ignore
public class OpptjeningsvilkåretOverstyringshåndtererTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private HistorikkTjenesteAdapter historikkAdapter = new HistorikkTjenesteAdapterImpl(
        new HistorikkRepositoryImpl(repoRule.getEntityManager()), new HistorikkInnslagKonverter(
        repositoryProvider.getKodeverkRepository(), repositoryProvider.getAksjonspunktRepository()));
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private InngangsvilkårTjeneste inngangsvilkårTjeneste = new InngangsvilkårTjenesteImpl(null, repositoryProvider);

    @Test
    public void skal_opprette_aksjonspunkt_for_overstyring() {
        // Arrange
        // Behandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING,
            BehandlingStegType.VURDER_OPPTJENINGSVILKÅR);
        scenario.leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT);
        scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        Behandling behandling = scenario.getBehandling();
        Fagsak fagsak = behandling.getFagsak();
        // Dto
        OverstyringOpptjeningsvilkåretDto overstyringspunktDto = new OverstyringOpptjeningsvilkåretDto(false,
            "test av overstyring opptjeningsvilkåret", "1035");
        assertThat(behandling.getAksjonspunkter()).hasSize(1);

        // Act
        OpptjeningsvilkåretOverstyringshåndterer overstyringshåndterer = new OpptjeningsvilkåretOverstyringshåndterer(repositoryProvider, resultatRepositoryProvider, historikkAdapter, inngangsvilkårTjeneste);
        overstyringshåndterer.håndterOverstyring(overstyringspunktDto, behandling, new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), repositoryProvider.getBehandlingRepository().taSkriveLås(behandling)));
        overstyringshåndterer.håndterAksjonspunktForOverstyring(overstyringspunktDto, behandling);

        // Assert
        Set<Aksjonspunkt> aksjonspunktSet = behandling.getAksjonspunkter();
        assertThat(aksjonspunktSet).hasSize(2);
        assertThat(aksjonspunktSet).extracting("aksjonspunktDefinisjon")
            .contains(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
        assertThat(aksjonspunktSet.stream()
            .filter(ap -> ap.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.OVERSTYRING_AV_OPPTJENINGSVILKÅRET)))
            .anySatisfy(ap -> assertThat(ap.getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT));
    }

    @Test
    public void skal_få_historikkinnslag_når_opptjening_er_overstyrt() {
        // Arrange
        // Behandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING,
            BehandlingStegType.VURDER_OPPTJENINGSVILKÅR);
        scenario.leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT);
        scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        Behandling behandling = scenario.getBehandling();
        Fagsak fagsak = behandling.getFagsak();
        // Dto
        OverstyringOpptjeningsvilkåretDto overstyringspunktDto = new OverstyringOpptjeningsvilkåretDto(false,
            "test av overstyring opptjeningsvilkåret", "1035");

        // Act
        OpptjeningsvilkåretOverstyringshåndterer overstyringshåndterer = new OpptjeningsvilkåretOverstyringshåndterer(repositoryProvider, resultatRepositoryProvider, historikkAdapter, inngangsvilkårTjeneste);
        overstyringshåndterer.håndterOverstyring(overstyringspunktDto, behandling, new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling)));
        overstyringshåndterer.håndterAksjonspunktForOverstyring(overstyringspunktDto, behandling);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.OVERSTYRT);
        List<HistorikkinnslagDel> historikkInnslagDeler = historikkAdapter.tekstBuilder().build(historikkinnslag);

        // Assert
        assertThat(historikkInnslagDeler).hasSize(1);
        List<HistorikkinnslagFelt> feltList = historikkInnslagDeler.get(0).getEndredeFelt();
        assertThat(feltList).hasSize(1);
        HistorikkinnslagFelt felt = feltList.get(0);
        assertThat(felt.getNavn()).as("navn").isEqualTo(HistorikkEndretFeltType.OVERSTYRT_VURDERING.getKode());
        assertThat(felt.getFraVerdi()).as("fraVerdi").isEqualTo(HistorikkEndretFeltVerdiType.VILKAR_OPPFYLT.getKode());
        assertThat(felt.getTilVerdi()).as("tilVerdi").isEqualTo(HistorikkEndretFeltVerdiType.VILKAR_IKKE_OPPFYLT.getKode());
    }

    @Test
    public void skal_feile_hvis_det_forsøkes_å_overstyre_uten_aktiviteter_i_opptjening() {
        // Arrange
        // Behandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING,
            BehandlingStegType.VURDER_OPPTJENINGSVILKÅR);
        scenario.leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT);
        scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        Behandling behandling = scenario.getBehandling();
        Fagsak fagsak = behandling.getFagsak();
        // Dto
        OverstyringOpptjeningsvilkåretDto overstyringspunktDto = new OverstyringOpptjeningsvilkåretDto(true,
            "test av overstyring opptjeningsvilkåret", "1035");

        // Act
        OpptjeningsvilkåretOverstyringshåndterer overstyringshåndterer = new OpptjeningsvilkåretOverstyringshåndterer(repositoryProvider, resultatRepositoryProvider, historikkAdapter, inngangsvilkårTjeneste);
        overstyringshåndterer.håndterOverstyring(overstyringspunktDto, behandling, new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling)));
        try {
            overstyringshåndterer.håndterAksjonspunktForOverstyring(overstyringspunktDto, behandling);
            fail("Skal kaste exception");
        } catch (FunksjonellException e) {
            assertThat(e).hasMessage("FP-093923:Kan ikke overstyre vilkår. Det må være minst en aktivitet for at opptjeningsvilkåret skal kunne overstyres.");
        }
    }
}
