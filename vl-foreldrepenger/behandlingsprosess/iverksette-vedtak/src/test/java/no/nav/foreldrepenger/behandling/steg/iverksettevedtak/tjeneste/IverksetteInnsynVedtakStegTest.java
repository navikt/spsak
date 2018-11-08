package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.tjeneste;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;

public class IverksetteInnsynVedtakStegTest {

    @Test
    public void skalBestilleVedtaksbrev() {
        ScenarioMorSøkerEngangsstønad scenario = innsynsScenario();
        DokumentBestillerApplikasjonTjeneste dokumentBestillerTjeneste = mock(DokumentBestillerApplikasjonTjeneste.class);
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        IverksetteInnsynVedtakSteg steg = new IverksetteInnsynVedtakSteg(dokumentBestillerTjeneste, repositoryProvider);
        Behandling behandling = scenario.getBehandling();
        Fagsak fagsak = behandling.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), repositoryProvider.getBehandlingRepository().taSkriveLås(behandling));
        steg.utførSteg(kontekst);

        ArgumentCaptor<BestillBrevDto> argumentCaptor = ArgumentCaptor.forClass(BestillBrevDto.class);
        verify(dokumentBestillerTjeneste, times(1))
            .bestillDokument(argumentCaptor.capture(), any(HistorikkAktør.class));
    }

    @Test
    public void skalDefaulteFritekstTilMellomrom() {
        ScenarioMorSøkerEngangsstønad scenario = innsynsScenario();
        DokumentBestillerApplikasjonTjeneste dokumentBestillerTjeneste = mock(DokumentBestillerApplikasjonTjeneste.class);
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        IverksetteInnsynVedtakSteg steg = new IverksetteInnsynVedtakSteg(dokumentBestillerTjeneste, repositoryProvider);
        Behandling behandling = scenario.getBehandling();
        Fagsak fagsak = behandling.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), repositoryProvider.getBehandlingRepository().taSkriveLås(behandling));
        steg.utførSteg(kontekst);

        ArgumentCaptor<BestillBrevDto> argumentCaptor = ArgumentCaptor.forClass(BestillBrevDto.class);
        verify(dokumentBestillerTjeneste, times(1))
            .bestillDokument(argumentCaptor.capture(), any(HistorikkAktør.class));

        assertThat(argumentCaptor.getValue().getFritekst()).isEqualTo(" ");
    }

    @Test
    public void skalBrukeBegrunnelseFraAksjonspunktSomFritekst() {
        String begrunnelse = "begrunnelse!!";
        ScenarioMorSøkerEngangsstønad scenario = innsynsScenario(begrunnelse);
        DokumentBestillerApplikasjonTjeneste dokumentBestillerTjeneste = mock(DokumentBestillerApplikasjonTjeneste.class);
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        IverksetteInnsynVedtakSteg steg = new IverksetteInnsynVedtakSteg(dokumentBestillerTjeneste, repositoryProvider);
        Behandling behandling = scenario.getBehandling();
        Fagsak fagsak = behandling.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), repositoryProvider.getBehandlingRepository().taSkriveLås(behandling));
        steg.utførSteg(kontekst);
        

        ArgumentCaptor<BestillBrevDto> argumentCaptor = ArgumentCaptor.forClass(BestillBrevDto.class);
        verify(dokumentBestillerTjeneste, times(1))
            .bestillDokument(argumentCaptor.capture(), any(HistorikkAktør.class));

        assertThat(argumentCaptor.getValue().getFritekst()).isEqualTo(begrunnelse);
    }

    private ScenarioMorSøkerEngangsstønad innsynsScenario() {
        return innsynsScenario(null);
    }

    private ScenarioMorSøkerEngangsstønad innsynsScenario(String begrunnelse) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medBehandlingType(BehandlingType.INNSYN);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.FORESLÅ_VEDTAK, BehandlingStegType.FORESLÅ_VEDTAK);
        scenario.lagMocked();
        Aksjonspunkt aksjonspunkt = scenario.getBehandling().getAksjonspunktFor(AksjonspunktDefinisjon.FORESLÅ_VEDTAK);
        scenario.mockBehandlingRepositoryProvider().getAksjonspunktRepository().setTilUtført(aksjonspunkt, begrunnelse);
        return scenario;
    }

}
