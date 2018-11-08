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

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
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
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepositoryImpl;
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
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeStatusLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiUtbetFrekvens;
import no.nav.foreldrepenger.økonomistøtte.es.OppdragskontrollEngangsstønad;
import no.nav.foreldrepenger.økonomistøtte.fp.OppdragskontrollEndringFP;
import no.nav.foreldrepenger.økonomistøtte.fp.OppdragskontrollFørstegangFP;
import no.nav.foreldrepenger.økonomistøtte.fp.OppdragskontrollOpphørFP;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class OppdragskontrollTjenesteImplESTest {

    private static final String KODE_KLASSIFIK_FODSEL = "FPENFOD-OP";
    private static final String TYPE_SATS_ES = "ENG";

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private ØkonomioppdragRepository økonomioppdragRepository = new ØkonomioppdragRepositoryImpl(entityManager);
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private BeregningRepository beregningRepository = new BeregningRepositoryImpl(entityManager);

    private OppdragskontrollTjeneste oppdragskontrollTjeneste;

    private Behandling behandlingES;
    private Fagsak fagsakES;
    private Personinfo personInfo;
    private BehandlingVedtak behVedtakES;

    @Before
    public void setUp() {

        TpsTjeneste tpsTjeneste = mock(TpsTjeneste.class);
        OppdragskontrollFørstegangFP oppdragskontrollFørstegangFP = mock(OppdragskontrollFørstegangFP.class);
        OppdragskontrollOpphørFP oppdragskontrollOpphørFP = mock(OppdragskontrollOpphørFP.class);
        OppdragskontrollEndringFP oppdragskontrollEndringFP = mock(OppdragskontrollEndringFP.class);

        OppdragskontrollEngangsstønad oppdragskontrollEngangsstønad = new OppdragskontrollEngangsstønad(repositoryProvider, tpsTjeneste);
        UttakRepository uttakRepository = new UttakRepositoryImpl(entityManager);
        OppdragskontrollManagerFactory oppdragskontrollManagerFactory = new OppdragskontrollManagerFactory(
            oppdragskontrollEngangsstønad, oppdragskontrollFørstegangFP, oppdragskontrollEndringFP, oppdragskontrollOpphørFP, uttakRepository);
        oppdragskontrollTjeneste = new OppdragskontrollTjenesteImpl(repositoryProvider, oppdragskontrollManagerFactory, økonomioppdragRepository);

        behandlingES = opprettOgLagreBehandlingES();

        personInfo = OpprettBehandling.opprettPersonInfo();

        when(tpsTjeneste.hentFnrForAktør(any(AktørId.class))).thenReturn(personInfo.getPersonIdent());
    }

    @Test
    public void opprettOppdragTestES() {
        // Arrange
        final Long prosessTaskId = 22L;

        // Act
        oppdragskontrollTjeneste.opprettOppdrag(behandlingES.getId(), prosessTaskId);

        // Assert
        Oppdragskontroll oppdrkontrollLest = verifiserOppdragskontrollFraRepo(prosessTaskId);
        List<Oppdrag110> oppdrag110LestListe = verifiserOppdrag110FraRepo(oppdrkontrollLest);
        verifiserAvstemming115FraRepo(oppdrag110LestListe);
        verifiserOppdragsenhet120FraRepo(oppdrag110LestListe);
        List<Oppdragslinje150> oppdragslinje150LestListe = verifiserOppdragslinje150FraRepo(oppdrag110LestListe);
        verifiserAttestant180FraRepo(oppdragslinje150LestListe);
    }

    @Test
    public void hentOppdragskontrollTestES() {
        // Arrange
        Long oppdrkontrollId = oppdragskontrollTjeneste.opprettOppdrag(behandlingES.getId(), 45L);
        assertThat(oppdrkontrollId).isNotNull();

        // Act
        Oppdragskontroll oppdrkontroll = oppdragskontrollTjeneste.hentOppdragskontroll(oppdrkontrollId);

        // Assert
        assertThat(oppdrkontroll).isNotNull();
        assertThat(oppdrkontroll.getOppdrag110Liste()).hasSize(1);

        Oppdrag110 oppdrag110Lest = oppdrkontroll.getOppdrag110Liste().get(0);
        assertThat(oppdrag110Lest).isNotNull();
        assertThat(oppdrag110Lest.getOppdragslinje150Liste()).hasSize(1);
        assertThat(oppdrag110Lest.getOppdragsenhet120Liste()).hasSize(1);
        assertThat(oppdrag110Lest.getAvstemming115()).isNotNull();

        Oppdragslinje150 oppdrlinje150Lest = oppdrag110Lest.getOppdragslinje150Liste().get(0);
        assertThat(oppdrlinje150Lest).isNotNull();
        assertThat(oppdrlinje150Lest.getOppdrag110()).isNotNull();
        assertThat(oppdrlinje150Lest.getAttestant180Liste()).hasSize(1);
        assertThat(oppdrlinje150Lest.getAttestant180Liste().get(0)).isNotNull();
    }

    @Test
    public void innvilgelseSomReferererTilTidligereOppdragPåSammeSak() {
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingES.getId(), 461L);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        Oppdrag110 originaltOppdrag110 = originaltOppdrag.getOppdrag110Liste().get(0);
        Oppdragslinje150 originalOppdragslinje150 = originaltOppdrag110.getOppdragslinje150Liste().get(0);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingES, VedtakResultatType.INNVILGET, 2);
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 462L);

        Oppdragslinje150 oppdragslinje150 = verifiserOppdrag110(oppdragId, ØkonomiKodeEndring.UEND, originaltOppdrag110.getFagsystemId());
        verifiserOppdragslinje150(oppdragslinje150, ØkonomiKodeEndringLinje.NY, null, originalOppdragslinje150.getDelytelseId() + 1,
            originalOppdragslinje150.getDelytelseId(), originaltOppdrag110.getFagsystemId(), 2 * OpprettBehandling.SATS);
    }

    @Test
    public void avslagSomReferererTilTidligereOppdragPåSammeSak() {
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingES.getId(), 461L);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        Oppdrag110 originaltOppdrag110 = originaltOppdrag.getOppdrag110Liste().get(0);
        Oppdragslinje150 originalOppdragslinje150 = originaltOppdrag110.getOppdragslinje150Liste().get(0);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingES, VedtakResultatType.AVSLAG, 0);
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 462L);

        Oppdragslinje150 oppdragslinje150 = verifiserOppdrag110(oppdragId, ØkonomiKodeEndring.UEND, originaltOppdrag110.getFagsystemId());
        verifiserOppdragslinje150(oppdragslinje150, ØkonomiKodeEndringLinje.ENDR, ØkonomiKodeStatusLinje.OPPH, originalOppdragslinje150.getDelytelseId(),
            null, null, OpprettBehandling.SATS);
    }


    private Oppdragslinje150 verifiserOppdrag110(Long oppdragId, ØkonomiKodeEndring kodeEndring, Long fagsystemId) {
        Oppdragskontroll oppdragskontroll = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);
        assertThat(oppdragskontroll.getOppdrag110Liste().size()).isEqualTo(1);
        Oppdrag110 oppdrag110 = oppdragskontroll.getOppdrag110Liste().get(0);
        assertThat(oppdrag110.getKodeEndring()).isEqualTo(kodeEndring.name());
        assertThat(oppdrag110.getFagsystemId()).isEqualTo(fagsystemId);
        assertThat(oppdrag110.getOppdragslinje150Liste().size()).isEqualTo(1);
        return oppdrag110.getOppdragslinje150Liste().get(0);
    }

    private void verifiserOppdragslinje150(Oppdragslinje150 oppdragslinje150, ØkonomiKodeEndringLinje kodeEndringLinje,
                                           ØkonomiKodeStatusLinje kodeStatusLinje, Long delYtelseId, Long refDelytelseId, Long refFagsystemId, long sats) {
        assertThat(oppdragslinje150.getKodeEndringLinje()).isEqualTo(kodeEndringLinje.name());
        if (kodeStatusLinje == null) {
            assertThat(oppdragslinje150.getKodeStatusLinje()).isNull();
        } else {
            assertThat(oppdragslinje150.getKodeStatusLinje()).isEqualTo(kodeStatusLinje.name());
        }
        assertThat(oppdragslinje150.getDelytelseId()).isEqualTo(delYtelseId);
        assertThat(oppdragslinje150.getRefDelytelseId()).isEqualTo(refDelytelseId);
        assertThat(oppdragslinje150.getRefFagsystemId()).isEqualTo(refFagsystemId);
        assertThat(oppdragslinje150.getSats()).isEqualTo(sats);
    }

    private void verifiserAttestant180FraRepo(List<Oppdragslinje150> oppdragslinje150) {
        List<Attestant180> attestant180ListFraRepo = repository.hentAlle(Attestant180.class);

        int ix180 = 0;
        for (Attestant180 attestant180FraRepo : attestant180ListFraRepo) {
            assertThat(attestant180FraRepo.getAttestantId()).isEqualTo(behVedtakES.getAnsvarligSaksbehandler());
            assertThat(attestant180FraRepo.getOppdragslinje150()).isEqualTo(oppdragslinje150.get(ix180++));
        }
    }

    private List<Oppdragslinje150> verifiserOppdragslinje150FraRepo(List<Oppdrag110> oppdrag110Liste) {
        List<Oppdragslinje150> oppdragslinje150ListFraRepo = repository.hentAlle(Oppdragslinje150.class);
        LocalDate vedtaksdatoES = behVedtakES.getVedtaksdato();

        long løpenummer = 100L;
        for (Oppdrag110 oppdrag110 : oppdrag110Liste) {
            assertThat(oppdrag110.getOppdragslinje150Liste()).isNotEmpty();
            Oppdragslinje150 oppdragslinje150FraRepo = oppdragslinje150ListFraRepo.get(0);
            assertThat(oppdragslinje150FraRepo.getKodeEndringLinje()).isEqualTo(ØkonomiKodeEndringLinje.NY.name());
            assertThat(oppdragslinje150FraRepo.getVedtakId()).isEqualTo(vedtaksdatoES.toString());
            assertThat(oppdragslinje150FraRepo.getDelytelseId()).isEqualTo(concatenateValues(oppdrag110.getFagsystemId(), løpenummer));
            assertThat(oppdragslinje150FraRepo.getKodeKlassifik()).isEqualTo(KODE_KLASSIFIK_FODSEL);
            assertThat(oppdragslinje150FraRepo.getDatoVedtakFom()).isEqualTo(vedtaksdatoES);
            assertThat(oppdragslinje150FraRepo.getDatoVedtakTom()).isEqualTo(vedtaksdatoES);
            assertThat(oppdragslinje150FraRepo.getSats()).isEqualTo(behandlingES.getBehandlingsresultat()
                .getBeregningResultat().getBeregninger().get(0).getBeregnetTilkjentYtelse());
            assertThat(oppdragslinje150FraRepo.getTypeSats()).isEqualTo(TYPE_SATS_ES);
            assertThat(oppdragslinje150FraRepo.getHenvisning()).isEqualTo(behandlingES.getId());
            assertThat(oppdragslinje150FraRepo.getUtbetalesTilId()).isEqualTo(personInfo.getPersonIdent().getIdent());
            assertThat(oppdragslinje150FraRepo.getSaksbehId()).isEqualTo(behVedtakES.getAnsvarligSaksbehandler());
            assertThat(oppdragslinje150FraRepo.getBrukKjoreplan()).isEqualTo("N");
            assertThat(oppdragslinje150FraRepo.getOppdrag110()).isEqualTo(oppdrag110);
            assertThat(oppdragslinje150FraRepo.getAttestant180Liste()).hasSize(1);
        }
        return oppdragslinje150ListFraRepo;
    }

    private void verifiserAvstemming115FraRepo(List<Oppdrag110> oppdrag110LestListe) {
        List<Avstemming115> avstemming115ListHentet = repository.hentAlle(Avstemming115.class);

        int size = oppdrag110LestListe.size();
        assertThat(avstemming115ListHentet).hasSize(size);
        for (Avstemming115 avstemming115Lest : avstemming115ListHentet) {
            assertThat(avstemming115Lest.getKodekomponent()).isEqualTo(ØkonomiKodeKomponent.VLFP.getKodeKomponent());
        }
    }

    private void verifiserOppdragsenhet120FraRepo(List<Oppdrag110> oppdrag110Liste) {
        List<Oppdragsenhet120> oppdragsenhet120ListFraRepo = repository.hentAlle(Oppdragsenhet120.class);
        assertThat(oppdragsenhet120ListFraRepo).hasSameSizeAs(oppdrag110Liste);

        int ix120 = 0;
        for (Oppdragsenhet120 oppdragsenhet120FraRepo : oppdragsenhet120ListFraRepo) {
            assertThat(oppdragsenhet120FraRepo.getTypeEnhet()).isEqualTo("BOS");
            assertThat(oppdragsenhet120FraRepo.getEnhet()).isEqualTo("8020");
            assertThat(oppdragsenhet120FraRepo.getDatoEnhetFom()).isEqualTo(LocalDate.of(1900, 1, 1));
            assertThat(oppdragsenhet120FraRepo.getOppdrag110()).isEqualTo(oppdrag110Liste.get(ix120++));
        }
    }

    private List<Oppdrag110> verifiserOppdrag110FraRepo(Oppdragskontroll oppdragskontroll) {

        List<Oppdrag110> oppdrag110ListFraRepo = repository.hentAlle(Oppdrag110.class);

        List<Avstemming115> avstemming115ListFraRepo = repository.hentAlle(Avstemming115.class);
        assertThat(avstemming115ListFraRepo).hasSameSizeAs(oppdrag110ListFraRepo);

        int ix110 = 0;
        for (Oppdrag110 oppdrag110FraRepo : oppdrag110ListFraRepo) {
            assertThat(oppdrag110FraRepo.getKodeAksjon()).isEqualTo(ØkonomiKodeAksjon.EN.getKodeAksjon());
            assertThat(oppdrag110FraRepo.getKodeEndring()).isEqualTo(ØkonomiKodeEndring.NY.name());
            assertThat(oppdrag110FraRepo.getKodeFagomrade()).isEqualTo(ØkonomiKodeFagområde.REFUTG.name());
            assertThat(oppdrag110FraRepo.getFagsystemId()).isEqualTo(concatenateValues(Long.parseLong(fagsakES.getSaksnummer().getVerdi()), 100L));
            assertThat(oppdrag110FraRepo.getSaksbehId()).isEqualTo(behVedtakES.getAnsvarligSaksbehandler());
            assertThat(oppdrag110FraRepo.getUtbetFrekvens()).isEqualTo(ØkonomiUtbetFrekvens.MÅNED.getUtbetFrekvens());
            assertThat(oppdrag110FraRepo.getOppdragGjelderId()).isEqualTo(personInfo.getPersonIdent().getIdent());
            assertThat(oppdrag110FraRepo.getOppdragskontroll()).isEqualTo(oppdragskontroll);
            assertThat(oppdrag110FraRepo.getAvstemming115()).isEqualTo(avstemming115ListFraRepo.get(ix110++));
        }

        return oppdrag110ListFraRepo;
    }

    private Oppdragskontroll verifiserOppdragskontrollFraRepo(Long prosessTaskId) {
        List<Oppdragskontroll> oppdrkontrollListHentet = repository.hentAlle(Oppdragskontroll.class);
        Oppdragskontroll oppdrskontrollLest = oppdrkontrollListHentet.get(0);

        assertThat(oppdrkontrollListHentet).hasSize(1);
        assertThat(oppdrskontrollLest.getSaksnummer()).isEqualTo(fagsakES.getSaksnummer());
        assertThat(oppdrskontrollLest.getVenterKvittering()).isEqualTo(Boolean.TRUE);
        assertThat(oppdrskontrollLest.getProsessTaskId()).isEqualTo(prosessTaskId);

        return oppdrskontrollLest;
    }

    private Long concatenateValues(Long... values) {
        List<Long> valueList = Arrays.asList(values);
        String result = valueList.stream().map(Object::toString).collect(Collectors.joining());

        return Long.valueOf(result);
    }

    private Behandling opprettOgLagreRevurdering(Behandling originalBehandling, VedtakResultatType resultat, int antallbarn) {

        Behandling revurdering = Behandling.fraTidligereBehandling(originalBehandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_MANGLER_FØDSEL).medOriginalBehandling(originalBehandling)).build();

        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, behandlingLås);
        repositoryProvider.getFamilieGrunnlagRepository().kopierGrunnlagFraEksisterendeBehandling(originalBehandling, revurdering);
        OpprettBehandling.genererBehandlingOgResultat(revurdering, resultat, antallbarn);
        behandlingRepository.lagre(revurdering.getBehandlingsresultat().getVilkårResultat(), behandlingLås);
        if (VedtakResultatType.INNVILGET.equals(resultat)) {
            beregningRepository.lagre(revurdering.getBehandlingsresultat().getBeregningResultat(), behandlingLås);
        }
        repository.lagre(revurdering.getBehandlingsresultat());

        BehandlingVedtak behandlingVedtak = OpprettBehandling.opprettBehandlingVedtak(revurdering.getBehandlingsresultat(), resultat);
        repositoryProvider.getBehandlingVedtakRepository().lagre(behandlingVedtak, behandlingLås);
        repository.flush();

        return revurdering;
    }

    private Behandling opprettOgLagreBehandlingES() {
        ScenarioMorSøkerEngangsstønad scenario = OpprettBehandling.opprettBehandlingMedTermindato();

        behandlingES = scenario.lagre(repositoryProvider);
        fagsakES = scenario.getFagsak();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandlingES);

        OpprettBehandling.genererBehandlingOgResultat(behandlingES, VedtakResultatType.INNVILGET, 1);

        behandlingRepository.lagre(behandlingES.getBehandlingsresultat().getVilkårResultat(), lås);
        beregningRepository.lagre(behandlingES.getBehandlingsresultat().getBeregningResultat(), lås);
        repository.lagre(behandlingES.getBehandlingsresultat());

        behVedtakES = OpprettBehandling.opprettBehandlingVedtak(behandlingES.getBehandlingsresultat());
        repositoryProvider.getBehandlingVedtakRepository().lagre(behVedtakES, lås);

        repository.flush();

        return behandlingES;
    }

}
