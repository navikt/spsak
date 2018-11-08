package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.innsyn.InnsynTjeneste;
import no.nav.foreldrepenger.behandling.innsyn.impl.InnsynHistorikkTjeneste;
import no.nav.foreldrepenger.behandling.innsyn.impl.InnsynTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OverhoppKontroll;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderInnsynDto;

public class VurderInnsynOppdatererTest {
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());

    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private LocalDate idag = LocalDate.now();

    BehandlingskontrollTjeneste behandlingskontrollTjeneste = Mockito.mock(BehandlingskontrollTjeneste.class);
    InnsynHistorikkTjeneste innsynHistorikkTjeneste = Mockito.mock(InnsynHistorikkTjeneste.class);
    BehandlendeEnhetTjeneste behandlendeEnhetTjeneste = Mockito.mock(BehandlendeEnhetTjeneste.class);
    InnsynTjeneste innsynTjeneste = new InnsynTjenesteImpl(behandlingskontrollTjeneste, innsynHistorikkTjeneste, repositoryProvider, behandlendeEnhetTjeneste);
    VurderInnsynOppdaterer oppdaterer = new VurderInnsynOppdaterer(behandlingskontrollTjeneste, innsynTjeneste);

    @Before
    public void konfigurerMocker() {
        OrganisasjonsEnhet enhet = new OrganisasjonsEnhet("enhetId", "enhetNavn");
        when(behandlendeEnhetTjeneste.finnBehandlendeEnhetFraSøker(any(Fagsak.class))).thenReturn(enhet);
        when(behandlendeEnhetTjeneste.finnBehandlendeEnhetFraSøker(any(Behandling.class))).thenReturn(enhet);
    }

    @Test
    public void skal_sette_innsynsbehandling_på_vent() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknad().medSøknadsdato(idag);
        scenario.medDefaultBekreftetTerminbekreftelse();
        Behandling behandling = scenario.lagre(repositoryProvider);

        Behandling innsynbehandling = innsynTjeneste.opprettManueltInnsyn(behandling.getFagsak().getSaksnummer());
        BehandlingLås lås = behandlingRepository.taSkriveLås(innsynbehandling);
        behandlingRepository.lagre(innsynbehandling, lås);


        // Act
        boolean sattPåVent = true;

        LocalDateTime nå = LocalDateTime.now();
        LocalDate frist = nå.toLocalDate().plusDays(3);
        VurderInnsynDto dto = new VurderInnsynDto("grunn", InnsynResultatType.INNVILGET, frist, sattPåVent, Collections.emptyList(), frist);
        OppdateringResultat oppdateringResultat = oppdaterer.oppdater(dto, innsynbehandling, VilkårResultat.builder());

        // Assert
        Assertions.assertThat(oppdateringResultat.getOverhoppKontroll()).isEqualTo(OverhoppKontroll.UTEN_OVERHOPP);
        Mockito.verify(behandlingskontrollTjeneste).settBehandlingPåVent(eq(innsynbehandling), eq(AksjonspunktDefinisjon.VENT_PÅ_SCANNING), eq(BehandlingStegType.VURDER_INNSYN), Mockito.any(LocalDateTime.class), eq(Venteårsak.SCANN));
    }


}
