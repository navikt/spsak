package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VURDER_INNSYN;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestUtils.finnAksjonspunkt;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynDokumentEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.BehandlingRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderInnsynDokumentDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderInnsynDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.NyBehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingDtoTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;

@RunWith(CdiRunner.class)
public class BehandlingInnsynTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Inject
    private BehandlingsutredningApplikasjonTjeneste behandlingutredningTjeneste;

    @Inject
    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste;

    @Inject
    private FagsakTjeneste fagsakTjeneste;

    @Inject
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;

    @Inject
    private BehandlingDtoTjeneste behandlingDtoTjeneste;

    @Inject
    private AksjonspunktApplikasjonTjeneste applikasjonstjeneste;

    @Inject
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste;

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    private TotrinnTjeneste totrinnTjeneste;

    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;
    private BehandlingRestTjeneste behandlingRestTjeneste;

    @Before
    public void setup() {
        aksjonspunktRestTjeneste = new AksjonspunktRestTjeneste(applikasjonstjeneste,
            behandlingRepository,
            behandlingutredningTjeneste, totrinnTjeneste);
        behandlingRestTjeneste = new BehandlingRestTjeneste(repositoryProvider,
            behandlingutredningTjeneste,
            behandlingsprosessTjeneste,
            fagsakTjeneste,
            henleggBehandlingTjeneste,
            behandlingDtoTjeneste,
            relatertBehandlingTjeneste);
    }

    @Test
    public void skal_opprette_innsyn_på_fagsak() throws Exception {
        // Arrange steg 1: Opprett innsyn på behandling
        ScenarioMorSøkerEngangsstønad førstegangsscenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(TpsRepo.STD_KVINNE_AKTØR_ID, NavBrukerKjønn.KVINNE);
        Behandling behandling = førstegangsscenario.lagre(repositoryProvider);

        Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();
        NyBehandlingDto opprettInnsynDto = new NyBehandlingDto();
        opprettInnsynDto.setSaksnummer(Long.parseLong(saksnummer.getVerdi()));
        opprettInnsynDto.setBehandlingType(BehandlingType.INNSYN);

        // Act
        behandlingRestTjeneste.opprettNyBehandling(opprettInnsynDto);

        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(VURDER_INNSYN, OPPRETTET)
        );
        Behandling innsynBehandling = repository.hentAlle(Behandling.class).stream()
            .filter(b -> b.getType().equals(BehandlingType.INNSYN))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Skal ikke kunne havne her"));

        finnAksjonspunkt(innsynBehandling.getAksjonspunkter(), VURDER_INNSYN);
        LocalDate mottattDato = LocalDate.now();
        VurderInnsynDto dto = new VurderInnsynDto("begrunnelse",
            InnsynResultatType.INNVILGET, mottattDato, true, Collections.emptyList(), mottattDato.plusDays(3));

        // Act
        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(innsynBehandling.getId(),
            innsynBehandling.getVersjon(), singletonList(dto)));

        // Assert
        InnsynEntitet innsynPåVent = repository.hent(Behandling.class, innsynBehandling.getId()).getInnsyn();
        assertThat(innsynPåVent).isNotNull();
        assertThat(innsynPåVent.getMottattDato()).isEqualTo(mottattDato);

        // Arrange steg 3: Bekreft innsyn
        VurderInnsynDokumentDto innsynDokument = new VurderInnsynDokumentDto(true, "1", "1");
        dto = new VurderInnsynDto("begrunnelse",
            InnsynResultatType.INNVILGET, mottattDato, false, Arrays.asList(innsynDokument), null);

        // Act
        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(innsynBehandling.getId(),
            innsynBehandling.getVersjon(), singletonList(dto)));

        // Assert
        InnsynEntitet innsyn = repository.hent(Behandling.class, innsynBehandling.getId()).getInnsyn();
        assertThat(innsyn).isNotNull();
        assertThat(innsyn.getInnsynResultatType()).isEqualTo(InnsynResultatType.INNVILGET);
        assertThat(innsyn.getMottattDato()).isEqualTo(mottattDato);
        assertThat(innsyn.getInnsynDokumenter()).isNotNull();
        assertThat(innsyn.getInnsynDokumenter().stream().findFirst().get()).isInstanceOf(InnsynDokumentEntitet.class);
    }

    private void kjørProsessTasks() {
        repoRule.getRepository().flush();
        new KjørProsessTasks(prosessTaskRepository).utførTasks();
    }

}
