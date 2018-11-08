package no.nav.foreldrepenger.økonomistøtte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Attestant180;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragsenhet120;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeAksjon;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndring;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndringLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKomponent;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiUtbetFrekvens;
import no.nav.foreldrepenger.økonomistøtte.api.ØkonomioppdragApplikasjonTjeneste;
import no.nav.foreldrepenger.økonomistøtte.es.OppdragskontrollEngangsstønad;
import no.nav.foreldrepenger.økonomistøtte.queue.producer.ØkonomioppdragJmsProducer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHendelseMottak;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskHendelseMottakImpl;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class ØkonomioppdragKvitteringTest {

    private static final String KODE_KLASSIFIK_FODSEL = "FPENFOD-OP";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private Repository repository = repoRule.getRepository();
    private ØkonomioppdragApplikasjonTjeneste økonomioppdragApplikasjonTjeneste;

    private BeregningRepository beregningRepository = new BeregningRepositoryImpl(entityManager);
    private ØkonomioppdragRepository økonomioppdragRepository = new ØkonomioppdragRepositoryImpl(entityManager);
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private ProsessTaskRepositoryImpl prosessTaskRepositoryImpl = new ProsessTaskRepositoryImpl(entityManager, null);
    private ProsessTaskHendelseMottak hendelsesmottak = new ProsessTaskHendelseMottakImpl(prosessTaskRepositoryImpl);

    @Mock
    private TpsTjeneste tpsTjenesteMock;

    private Behandling behandling;
    private Fagsak fagsak;
    private Personinfo personInfo;
    private BehandlingVedtak behVedtak;

    @Mock
    private ØkonomioppdragRepository økonomioppdragRepositoryMock;
    @Mock
    private ØkonomioppdragJmsProducer økonomioppdragJmsProducer;

    @Before
    public void setUp() {
        OppdragskontrollEngangsstønad oppdragskontrollEngangsstønad = new OppdragskontrollEngangsstønad(repositoryProvider, tpsTjenesteMock);
        OppdragskontrollManagerFactory oppdragskontrollManagerFactory = new OppdragskontrollManagerFactory(oppdragskontrollEngangsstønad, null, null, null, null);
        OppdragskontrollTjeneste oppdragskontrollTjeneste = new OppdragskontrollTjenesteImpl(repositoryProvider, oppdragskontrollManagerFactory,
            økonomioppdragRepository);

        økonomioppdragApplikasjonTjeneste = new ØkonomioppdragApplikasjonTjenesteImpl(oppdragskontrollTjeneste, hendelsesmottak,
            økonomioppdragRepositoryMock, økonomioppdragJmsProducer);

        behandling = opprettOgLagreBehandling();

        personInfo = OpprettBehandling.opprettPersonInfo();
        when(tpsTjenesteMock.hentFnrForAktør(any(AktørId.class))).thenReturn(personInfo.getPersonIdent());
    }

    @Test
    public void skal_kunne_lagre_oppdragsmelding_for_Økonomi() {
        // Arrange
        ProsessTaskInfo ventendetask = opprettOgLagreVentendeTask();

        // Act
        økonomioppdragApplikasjonTjeneste.utførOppdrag(behandling.getId(), ventendetask.getId(), true);

        // Assert
        verifyAvstemming115();
        Oppdragskontroll oppdrkontrollLest = verifyOppdragskontroll();
        Oppdrag110 oppdrag110Lest = verifyOppdrag110(oppdrkontrollLest);
        verifyOppdragsenhet120(oppdrag110Lest);
        Oppdragslinje150 oppdragslinje150Lest = verifyOppdragslinje150(oppdrag110Lest);
        verifyAttestant180(oppdragslinje150Lest);
    }

    private void verifyAttestant180(Oppdragslinje150 oppdragslinje150) {
        List<Attestant180> attestant180ListFraRepo = repository.hentAlle(Attestant180.class);
        Attestant180 attestant180FraRepo = attestant180ListFraRepo.get(0);

        assertThat(attestant180FraRepo.getAttestantId()).isEqualTo(behVedtak.getAnsvarligSaksbehandler());
        assertThat(attestant180FraRepo.getOppdragslinje150()).isEqualTo(oppdragslinje150);
    }

    private Oppdragslinje150 verifyOppdragslinje150(Oppdrag110 oppdrag110) {
        List<Oppdragslinje150> oppdragslinje150ListFraRepo = repository.hentAlle(Oppdragslinje150.class);
        Oppdragslinje150 oppdragslinje150FraRepo = oppdragslinje150ListFraRepo.get(0);
        LocalDate vedtaksdato = behVedtak.getVedtaksdato();

        assertThat(oppdragslinje150FraRepo.getKodeEndringLinje()).isEqualTo(ØkonomiKodeEndringLinje.NY.name());
        assertThat(oppdragslinje150FraRepo.getVedtakId()).isEqualTo(vedtaksdato.toString());
        assertThat(oppdragslinje150FraRepo.getDelytelseId()).isEqualTo(concatenateValues(oppdrag110.getFagsystemId(), 100L));
        assertThat(oppdragslinje150FraRepo.getKodeKlassifik()).isEqualTo(KODE_KLASSIFIK_FODSEL);
        assertThat(oppdragslinje150FraRepo.getDatoVedtakFom()).isEqualTo(vedtaksdato);
        assertThat(oppdragslinje150FraRepo.getDatoVedtakTom()).isEqualTo(vedtaksdato);
        assertThat(oppdragslinje150FraRepo.getSats()).isEqualTo(behandling.getBehandlingsresultat()
            .getBeregningResultat().getBeregninger().get(0).getBeregnetTilkjentYtelse());
        assertThat(oppdragslinje150FraRepo.getBrukKjoreplan()).isEqualTo("N");
        assertThat(oppdragslinje150FraRepo.getSaksbehId()).isEqualTo(behVedtak.getAnsvarligSaksbehandler());
        assertThat(oppdragslinje150FraRepo.getUtbetalesTilId()).isEqualTo(personInfo.getPersonIdent().getIdent());
        assertThat(oppdragslinje150FraRepo.getHenvisning()).isEqualTo(behandling.getId());
        assertThat(oppdragslinje150FraRepo.getOppdrag110()).isEqualTo(oppdrag110);
        assertThat(oppdragslinje150FraRepo.getAttestant180Liste()).hasSize(1);

        return oppdragslinje150FraRepo;
    }

    private void verifyAvstemming115() {
        List<Avstemming115> avstemming115ListHentet = repository.hentAlle(Avstemming115.class);
        Avstemming115 avstemming115Lest = avstemming115ListHentet.get(0);

        assertThat(avstemming115ListHentet).hasSize(1);
        assertThat(avstemming115Lest.getKodekomponent()).isEqualTo(ØkonomiKodeKomponent.VLFP.getKodeKomponent());
    }

    private void verifyOppdragsenhet120(Oppdrag110 oppdrag110) {
        List<Oppdragsenhet120> oppdragsenhet120ListFraRepo = repository.hentAlle(Oppdragsenhet120.class);
        assertThat(oppdragsenhet120ListFraRepo).hasSize(1);
        Oppdragsenhet120 oppdragsenhet120FraRepo = oppdragsenhet120ListFraRepo.get(0);

        assertThat(oppdragsenhet120FraRepo.getTypeEnhet()).isEqualTo("BOS");
        assertThat(oppdragsenhet120FraRepo.getEnhet()).isEqualTo("8020");
        assertThat(oppdragsenhet120FraRepo.getDatoEnhetFom()).isEqualTo(LocalDate.of(1900, 1, 1));
        assertThat(oppdragsenhet120FraRepo.getOppdrag110()).isEqualTo(oppdrag110);
    }

    private Oppdrag110 verifyOppdrag110(Oppdragskontroll oppdragskontroll) {
        List<Oppdrag110> oppdrag110ListFraRepo = repository.hentAlle(Oppdrag110.class);
        assertThat(oppdrag110ListFraRepo).hasSize(1);
        Oppdrag110 oppdrag110FraRepo = oppdrag110ListFraRepo.get(0);

        List<Avstemming115> avstemming115ListFraRepo = repository.hentAlle(Avstemming115.class);
        assertThat(avstemming115ListFraRepo).hasSize(1);
        Avstemming115 avst115FraRepo = avstemming115ListFraRepo.get(0);

        assertThat(oppdrag110FraRepo.getKodeAksjon()).isEqualTo(ØkonomiKodeAksjon.EN.getKodeAksjon());
        assertThat(oppdrag110FraRepo.getKodeEndring()).isEqualTo(ØkonomiKodeEndring.NY.name());
        assertThat(oppdrag110FraRepo.getKodeFagomrade()).isEqualTo(ØkonomiKodeFagområde.REFUTG.name());
        assertThat(oppdrag110FraRepo.getFagsystemId()).isEqualTo(concatenateValues(Long.parseLong(fagsak.getSaksnummer().getVerdi()), 100L));
        assertThat(oppdrag110FraRepo.getUtbetFrekvens()).isEqualTo(ØkonomiUtbetFrekvens.MÅNED.getUtbetFrekvens());
        assertThat(oppdrag110FraRepo.getOppdragGjelderId()).isEqualTo(personInfo.getPersonIdent().getIdent());
        assertThat(oppdrag110FraRepo.getSaksbehId()).isEqualTo(behVedtak.getAnsvarligSaksbehandler());
        assertThat(oppdrag110FraRepo.getOppdragskontroll()).isEqualTo(oppdragskontroll);
        assertThat(oppdrag110FraRepo.getAvstemming115()).isEqualTo(avst115FraRepo);

        return oppdrag110FraRepo;
    }

    private Oppdragskontroll verifyOppdragskontroll() {
        List<Oppdragskontroll> oppdrkontrollListHentet = repository.hentAlle(Oppdragskontroll.class);
        Oppdragskontroll oppdrskontrollLest = oppdrkontrollListHentet.get(0);

        assertThat(oppdrkontrollListHentet).hasSize(1);
        assertThat(oppdrskontrollLest.getSaksnummer()).isEqualTo(fagsak.getSaksnummer());
        assertThat(oppdrskontrollLest.getVenterKvittering()).isEqualTo(Boolean.TRUE);

        return oppdrskontrollLest;
    }

    private Long concatenateValues(Long... values) {
        List<Long> valueList = Arrays.asList(values);
        String result = valueList.stream().map(Object::toString).collect(Collectors.joining());

        return Long.valueOf(result);
    }

    private Behandling opprettOgLagreBehandling() {

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().plusDays(40))
                .medNavnPå("LEGEN MIN")
                .medUtstedtDato(LocalDate.now()))
            .medAntallBarn(1);
        scenario.medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().plusDays(40))
                .medNavnPå("LEGEN MIN")
                .medUtstedtDato(LocalDate.now().minusDays(7)))
            .medAntallBarn(1);

        Behandling beh = scenario.lagre(repositoryProvider);
        fagsak = scenario.getFagsak();

        OpprettBehandling.genererBehandlingOgResultat(beh, VedtakResultatType.INNVILGET, 1);

        BehandlingLås lås = behandlingRepository.taSkriveLås(beh);

        behandlingRepository.lagre(beh.getBehandlingsresultat().getVilkårResultat(), lås);
        beregningRepository.lagre(beh.getBehandlingsresultat().getBeregningResultat(), lås);
        repository.lagre(beh.getBehandlingsresultat());

        behVedtak = OpprettBehandling.opprettBehandlingVedtak(beh.getBehandlingsresultat());

        repositoryProvider.getBehandlingVedtakRepository().lagre(behVedtak, lås);

        repository.flush();

        return beh;
    }

    private ProsessTaskInfo opprettOgLagreVentendeTask() {
        ProsessTaskData pd = new ProsessTaskData("iverksetteVedtak.oppdragTilØkonomi");
        pd.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskRepositoryImpl.lagre(pd);

        repository.flush();

        return pd;
    }
}
