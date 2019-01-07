package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

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
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.inngangsvilkaar.InngangsvilkårTjeneste;
import no.nav.foreldrepenger.domene.inngangsvilkaar.impl.InngangsvilkårTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringSokersOpplysingspliktDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapterImpl;
import no.nav.foreldrepenger.web.app.tjenester.historikk.dto.HistorikkInnslagKonverter;

public class SøkersopplysningspliktOverstyringhåndtererTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private HistorikkTjenesteAdapter historikkAdapter = new HistorikkTjenesteAdapterImpl(
            new HistorikkRepositoryImpl(repoRule.getEntityManager()), new HistorikkInnslagKonverter(repositoryProvider.getKodeverkRepository(), repositoryProvider.getAksjonspunktRepository()));
    private InngangsvilkårTjeneste inngangsvilkårTjeneste = new InngangsvilkårTjenesteImpl(null, repositoryProvider);

    @Test
    public void skal_generere_historikkinnslag_ved_avklaring_av_søkers_opplysningsplikt_overstyrt() {
        // Arrange
        // Behandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT,
                BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR);
        scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        Behandling behandling = scenario.getBehandling();
        Fagsak fagsak = behandling.getFagsak();
        // Dto
        OverstyringSokersOpplysingspliktDto overstyringspunktDto = new OverstyringSokersOpplysingspliktDto(false,
                "test av overstyring");
        assertThat(behandling.getAksjonspunkter().size()).isEqualTo(1);

        // Act
        SøkersOpplysningspliktOverstyringshåndterer overstyringshåndterer = new SøkersOpplysningspliktOverstyringshåndterer(repositoryProvider, historikkAdapter, inngangsvilkårTjeneste);
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
        assertThat(felt.getNavn()).as("navn").isEqualTo(HistorikkEndretFeltType.SOKERSOPPLYSNINGSPLIKT.getKode());
        assertThat(felt.getFraVerdi()).as("fraVerdi").isNull();
        assertThat(felt.getTilVerdi()).as("tilVerdi").isEqualTo(HistorikkEndretFeltVerdiType.IKKE_OPPFYLT.getKode());

        Set<Aksjonspunkt> aksjonspunktSet = behandling.getAksjonspunkter();

        assertThat(aksjonspunktSet).extracting("aksjonspunktDefinisjon").contains(AksjonspunktDefinisjon.SØKERS_OPPLYSNINGSPLIKT_OVST);

        assertThat(aksjonspunktSet.stream()
                .filter(ap -> ap.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.SØKERS_OPPLYSNINGSPLIKT_OVST)))
                        .anySatisfy(ap -> assertThat(ap.getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT));

        assertThat(aksjonspunktSet.stream()
                .filter(ap -> ap.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.VEDTAK_UTEN_TOTRINNSKONTROLL)))
                        .anySatisfy(ap -> assertThat(ap.getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET));

        assertThat(aksjonspunktSet).hasSize(3);
    }

}
