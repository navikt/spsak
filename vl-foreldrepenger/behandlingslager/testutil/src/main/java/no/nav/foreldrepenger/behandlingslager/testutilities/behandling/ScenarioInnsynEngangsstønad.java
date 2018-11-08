package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VURDER_INNSYN;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class ScenarioInnsynEngangsstønad {

    public static ScenarioInnsynEngangsstønad innsyn(AbstractTestScenario<?> abstractTestScenario) {
        return new ScenarioInnsynEngangsstønad().setup(abstractTestScenario);
    }

    private Map<AksjonspunktDefinisjon, BehandlingStegType> opprettedeAksjonspunktDefinisjoner = new HashMap<>();
    private Map<AksjonspunktDefinisjon, BehandlingStegType> utførteAksjonspunktDefinisjoner = new HashMap<>();

    private AbstractTestScenario<?> abstractTestScenario;

    private Behandling behandling;
    private BehandlingStegType startSteg;
    private String behandlendeEnhet;
    private BehandlingVedtak behandlingVedtak;

    private ScenarioInnsynEngangsstønad() {
    }

    private ScenarioInnsynEngangsstønad setup(AbstractTestScenario<?> abstractTestScenario) {
        this.abstractTestScenario = abstractTestScenario;

        // default steg (kan bli overskrevet av andre setup metoder som kaller denne)
        this.startSteg = BehandlingStegType.VURDER_INNSYN;

        this.opprettedeAksjonspunktDefinisjoner.put(VURDER_INNSYN, BehandlingStegType.VURDER_INNSYN);
        return this;
    }

    public Behandling lagre(BehandlingRepositoryProvider repositoryProvider) {
        if (behandling != null) {
            throw new IllegalStateException("build allerede kalt.  Hent Behandling via getBehandling eller opprett nytt scenario.");
        }
        abstractTestScenario.buildAvsluttet(repositoryProvider.getBehandlingRepository(), repositoryProvider);
        return buildInnsyn(repositoryProvider);
    }

    private Behandling buildInnsyn(BehandlingRepositoryProvider repositoryProvider) {
        Fagsak fagsak = abstractTestScenario.getFagsak();

        // oppprett og lagre behandling
        Behandling.Builder builder = Behandling.nyBehandlingFor(fagsak, BehandlingType.INNSYN);

        if (behandlendeEnhet != null) {
            builder.medBehandlendeEnhet(new OrganisasjonsEnhet(behandlendeEnhet, null));
        }

        behandling = builder.build();

        BehandlingLås lås = repositoryProvider.getBehandlingRepository().taSkriveLås(behandling);
        repositoryProvider.getBehandlingRepository().lagre(behandling, lås);
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.IKKE_FASTSATT)
                .buildFor(behandling);

        utførteAksjonspunktDefinisjoner.forEach(
                (apDef, stegType) -> repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, apDef, stegType));

        behandling.getAksjonspunkter().forEach(punkt -> repositoryProvider.getAksjonspunktRepository().setTilUtført(punkt, "Test"));

        opprettedeAksjonspunktDefinisjoner.forEach(
                (apDef, stegType) -> repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, apDef, stegType));

        behandling.getAksjonspunkter().forEach(punkt -> Whitebox.setInternalState(punkt, "id", AbstractTestScenario.nyId()));

        if (startSteg != null) {
            new InternalManipulerBehandlingImpl(repositoryProvider).forceOppdaterBehandlingSteg(behandling, startSteg);
        }

        return behandling;
    }

    public BehandlingRepository mockBehandlingRepository() {
        BehandlingRepository behandlingRepository = abstractTestScenario.mockBehandlingRepository();
        when(behandlingRepository.hentBehandling(behandling.getId())).thenReturn(behandling);
        return behandlingRepository;
    }

    public BehandlingRepositoryProvider mockBehandlingRepositoryProvider() {
        mockBehandlingRepository();
        return abstractTestScenario.mockBehandlingRepositoryProvider();
    }

    public Behandling lagMocked() {
        // pga det ikke går ann å flytte steg hvis mocket så settes startsteg til null
        startSteg = null;
        lagre(abstractTestScenario.mockBehandlingRepositoryProvider());
        Whitebox.setInternalState(behandling, "id", AbstractTestScenario.nyId());
        return behandling;
    }

    public Fagsak getFagsak() {
        return abstractTestScenario.getFagsak();
    }

    public BehandlingVedtak mockBehandlingVedtak() {
        if (behandlingVedtak == null) {
            behandlingVedtak = Mockito.mock(BehandlingVedtak.class);
            when(abstractTestScenario.mockBehandlingRepositoryProvider().getBehandlingVedtakRepository()
                    .hentBehandlingvedtakForBehandlingId(behandling.getId())).thenReturn(Optional.of(behandlingVedtak));
        }
        return behandlingVedtak;
    }

}
