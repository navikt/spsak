package no.nav.foreldrepenger.datavarehus.tjeneste;

import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.AKSJONSPUNKT_DEF;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.ANNEN_PART_AKTØR_ID;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.ANSVARLIG_BESLUTTER;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.ANSVARLIG_SAKSBEHANDLER;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BEHANDLENDE_ENHET;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BEHANDLING_STEG_STATUS;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BEHANDLING_STEG_TYPE;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BRUKER_AKTØR_ID;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.IVERKSETTING_STATUS;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.SAKSNUMMER;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.VEDTAK_DATO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.datavarehus.AksjonspunktDvh;
import no.nav.foreldrepenger.datavarehus.BehandlingDvh;
import no.nav.foreldrepenger.datavarehus.BehandlingStegDvh;
import no.nav.foreldrepenger.datavarehus.BehandlingVedtakDvh;
import no.nav.foreldrepenger.datavarehus.DatavarehusRepository;
import no.nav.foreldrepenger.datavarehus.FagsakDvh;
import no.nav.foreldrepenger.datavarehus.VedtakUtbetalingDvh;
import no.nav.foreldrepenger.datavarehus.xml.DvhVedtakTjenesteEngangsstønad;
import no.nav.foreldrepenger.datavarehus.xml.DvhVedtakTjenesteProvider;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class DatavarehusTjenesteImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private DatavarehusRepository datavarehusRepository;
    private DvhVedtakTjenesteEngangsstønad dvhVedtakTjenesteEngangsstønad;
    private DvhVedtakTjenesteProvider dvhVedtakTjenesteProvider;
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Inject
    private InternalManipulerBehandling manipulerInternBehandling;

    @Before
    public void setUp() {
        dvhVedtakTjenesteEngangsstønad = mock(DvhVedtakTjenesteEngangsstønad.class);
        dvhVedtakTjenesteProvider = mock(DvhVedtakTjenesteProvider.class);
        datavarehusRepository = mock(DatavarehusRepository.class);
        when(dvhVedtakTjenesteProvider.getVedtakTjeneste(any())).thenReturn(dvhVedtakTjenesteEngangsstønad);
    }

    @Test
    public void lagreNedFagsak() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(BRUKER_AKTØR_ID, NavBrukerKjønn.KVINNE)
            .medSaksnummer(SAKSNUMMER);
        scenario.medSøknadAnnenPart().medAktørId(ANNEN_PART_AKTØR_ID);
        Behandling behandling = scenario.lagre(repositoryProvider);
        Fagsak fagsak = behandling.getFagsak();

        ArgumentCaptor<FagsakDvh> captor = ArgumentCaptor.forClass(FagsakDvh.class);

        DatavarehusTjeneste datavarehusTjeneste = new DatavarehusTjenesteImpl(repositoryProvider, datavarehusRepository, dvhVedtakTjenesteProvider, mock(TotrinnRepository.class));
        datavarehusTjeneste.lagreNedFagsak(fagsak.getId());

        verify(datavarehusRepository).lagre(captor.capture());
        FagsakDvh fagsakDvh = captor.getValue();
        assertThat(fagsakDvh.getFagsakId()).isEqualTo(fagsak.getId());
    }

    @Test
    public void lagreNedAksjonspunkter() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.leggTilAksjonspunkt(AKSJONSPUNKT_DEF, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.medBehandlingStegStart(BEHANDLING_STEG_TYPE);
        scenario.medBehandlendeEnhet(BEHANDLENDE_ENHET);
        Behandling behandling = scenario.lagre(repositoryProvider);
        behandling.setAnsvarligBeslutter(ANSVARLIG_BESLUTTER);
        behandling.setAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER);

        List<Aksjonspunkt> aksjonspunkter = new ArrayList<>(behandling.getAksjonspunkter());
        ArgumentCaptor<AksjonspunktDvh> captor = ArgumentCaptor.forClass(AksjonspunktDvh.class);
        DatavarehusTjeneste datavarehusTjeneste = new DatavarehusTjenesteImpl(repositoryProvider, datavarehusRepository, dvhVedtakTjenesteProvider, mock(TotrinnRepository.class));

        // Act
        datavarehusTjeneste.lagreNedAksjonspunkter(aksjonspunkter, behandling.getId(), BEHANDLING_STEG_TYPE);

        verify(datavarehusRepository, times(2)).lagre(captor.capture());
        List<AksjonspunktDvh> aksjonspunktDvhList = captor.getAllValues();
        assertThat(aksjonspunktDvhList.get(0).getAksjonspunktId()).isEqualTo(aksjonspunkter.get(0).getId());
        assertThat(aksjonspunktDvhList.get(0).getBehandlingId()).isEqualTo(behandling.getId());
        assertThat(aksjonspunktDvhList.get(0).getBehandlingStegId())
            .isEqualTo(behandling.getBehandlingStegTilstand(BEHANDLING_STEG_TYPE).get().getId());
        assertThat(aksjonspunktDvhList.get(1).getAksjonspunktId()).isEqualTo(aksjonspunkter.get(1).getId());
    }

    @Test
    public void lagreNedBehandlingStegTilstand() {
        BehandlingStegTilstand behandlingStegTilstand = new BehandlingStegTilstand(ScenarioMorSøkerEngangsstønad.forFødsel().lagMocked(),
            BEHANDLING_STEG_TYPE, BEHANDLING_STEG_STATUS);

        ArgumentCaptor<BehandlingStegDvh> captor = ArgumentCaptor.forClass(BehandlingStegDvh.class);

        DatavarehusTjeneste datavarehusTjeneste = new DatavarehusTjenesteImpl(repositoryProvider, datavarehusRepository, dvhVedtakTjenesteProvider, mock(TotrinnRepository.class));
        datavarehusTjeneste.lagreNedBehandlingStegTilstand(behandlingStegTilstand);

        verify(datavarehusRepository).lagre(captor.capture());

        BehandlingStegDvh behandlingStegDvh = captor.getValue();
        assertThat(behandlingStegDvh.getBehandlingStegId()).isEqualTo(behandlingStegTilstand.getId());
        assertThat(behandlingStegDvh.getBehandlingId()).isEqualTo(behandlingStegTilstand.getBehandling().getId());
    }

    @Test
    public void lagreNedBehandling() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.leggTilAksjonspunkt(AKSJONSPUNKT_DEF, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.medBehandlendeEnhet(BEHANDLENDE_ENHET);
        Behandling behandling = scenario.lagMocked();
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, BEHANDLING_STEG_TYPE);
        behandling.setAnsvarligBeslutter(ANSVARLIG_BESLUTTER);
        behandling.setAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER);

        ArgumentCaptor<BehandlingDvh> captor = ArgumentCaptor.forClass(BehandlingDvh.class);
        DatavarehusTjeneste datavarehusTjeneste = new DatavarehusTjenesteImpl(scenario.mockBehandlingRepositoryProvider(), datavarehusRepository, dvhVedtakTjenesteProvider, mock(TotrinnRepository.class));
        datavarehusTjeneste.lagreNedBehandling(behandling);
        // Act
        verify(datavarehusRepository).lagre(captor.capture());

        assertThat(captor.getValue().getBehandlingId()).isEqualTo(behandling.getId());
    }

    @Test
    public void lagreNedBehandlingMedId() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.leggTilAksjonspunkt(AKSJONSPUNKT_DEF, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.medBehandlendeEnhet(BEHANDLENDE_ENHET);

        Behandling behandling = scenario.lagMocked();
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, BEHANDLING_STEG_TYPE);
        behandling.setAnsvarligBeslutter(ANSVARLIG_BESLUTTER);
        behandling.setAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER);

        ArgumentCaptor<BehandlingDvh> captor = ArgumentCaptor.forClass(BehandlingDvh.class);
        DatavarehusTjeneste datavarehusTjeneste = new DatavarehusTjenesteImpl(scenario.mockBehandlingRepositoryProvider(), datavarehusRepository, dvhVedtakTjenesteProvider, mock(TotrinnRepository.class));
        // Act
        datavarehusTjeneste.lagreNedBehandling(behandling.getId());

        verify(datavarehusRepository).lagre(captor.capture());
        assertThat(captor.getValue().getBehandlingId()).isEqualTo(behandling.getId());
    }

    @Test
    public void lagreNedVedtak() {
        BehandlingVedtak vedtak = byggBehandlingVedtak();
        Behandling behandling = vedtak.getBehandlingsresultat().getBehandling();
        ArgumentCaptor<BehandlingVedtakDvh> captor = ArgumentCaptor.forClass(BehandlingVedtakDvh.class);
        DatavarehusTjeneste datavarehusTjeneste = new DatavarehusTjenesteImpl(repositoryProvider, datavarehusRepository, dvhVedtakTjenesteProvider, mock(TotrinnRepository.class));
        // Act
        datavarehusTjeneste.lagreNedVedtak(vedtak, behandling.getId());

        verify(datavarehusRepository).lagre(captor.capture());
        assertThat(captor.getValue().getBehandlingId()).isEqualTo(behandling.getId());
    }

    @Test
    public void skal_lagre_Ned_Vedtak_Xml() {
        BehandlingVedtak vedtak = byggBehandlingVedtak();
        Behandling behandling = vedtak.getBehandlingsresultat().getBehandling();
        ArgumentCaptor<VedtakUtbetalingDvh> captor = ArgumentCaptor.forClass(VedtakUtbetalingDvh.class);
        String xml = "<bob>bob</bob";
        when(dvhVedtakTjenesteEngangsstønad.opprettDvhVedtakXml(any())).thenReturn(xml);

        DatavarehusTjeneste datavarehusTjeneste = new DatavarehusTjenesteImpl(repositoryProvider, datavarehusRepository, dvhVedtakTjenesteProvider, mock(TotrinnRepository.class));
        // Act
        datavarehusTjeneste.opprettOgLagreVedtakXml(behandling.getId());
        verify(datavarehusRepository).lagre(captor.capture());
        assertThat(captor.getValue().getXmlClob()).isEqualTo(xml);
    }

    private BehandlingVedtak byggBehandlingVedtak() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(BRUKER_AKTØR_ID, NavBrukerKjønn.KVINNE)
            .medSaksnummer(SAKSNUMMER);
        scenario.medSøknadAnnenPart().medAktørId(ANNEN_PART_AKTØR_ID);

        Behandling behandling = scenario.lagre(repositoryProvider);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.opprettFor(behandling);
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        BehandlingVedtakRepository behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        BehandlingVedtak vedtak = BehandlingVedtak.builder()
            .medAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER)
            .medIverksettingStatus(IVERKSETTING_STATUS)
            .medVedtaksdato(VEDTAK_DATO)
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medBehandlingsresultat(behandlingsresultat)
            .build();
        behandlingVedtakRepository.lagre(vedtak, behandlingRepository.taSkriveLås(behandling));
        return vedtak;
    }

}
