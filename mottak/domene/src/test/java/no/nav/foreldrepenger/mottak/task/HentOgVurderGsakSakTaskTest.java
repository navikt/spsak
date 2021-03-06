package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSak;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSakTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class HentOgVurderGsakSakTaskTest {

    private static final String BRUKER_FNR = "06016921295";
    private static final String ANNEN_PART_FNR = "06016921296";
    private static final String BRUKER_AKTØR_ID = "123";
    private static final String ANNEN_PART_ID = "124";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    Instance<Period> infotrygdSakGyldigPeriodeInstance;
    @Mock
    private Instance<Period> infotrygdAnnenPartGyldigPeriodeInstance;
    @Mock
    private Instance<Period> startdatoDiffInstance;
    private HentOgVurderInfotrygdSakTask task;
    private GsakSakTjeneste mockGsakSakTjeneste;
    private InfotrygdTjeneste mockInfotrygdTjeneste;
    private AktørConsumerMedCache mockAktørConsumer;
    private KodeverkRepository kodeverkRepository;
    private List<GsakSak> sakerTom;
    private List<GsakSak> sakerMatchende;
    private List<GsakSak> sakerMatchende2;

    @Before
    public void setup() {
        ProsessTaskRepository mockProsessTaskRepository = mock(ProsessTaskRepository.class);
        mockGsakSakTjeneste = mock(GsakSakTjeneste.class);
        mockAktørConsumer = mock(AktørConsumerMedCache.class);
        mockInfotrygdTjeneste = mock(InfotrygdTjeneste.class);
        kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        when(infotrygdSakGyldigPeriodeInstance.get()).thenReturn(Period.parse("P10M"));
        when(infotrygdAnnenPartGyldigPeriodeInstance.get()).thenReturn(Period.parse("P18M"));
        when(startdatoDiffInstance.get()).thenReturn(Period.parse("P4D"));
        when(mockAktørConsumer.hentPersonIdentForAktørId(BRUKER_AKTØR_ID)).thenReturn(Optional.of(BRUKER_FNR));
        when(mockAktørConsumer.hentAktørIdForPersonIdent(BRUKER_FNR)).thenReturn(Optional.of(BRUKER_AKTØR_ID));

        when(mockAktørConsumer.hentPersonIdentForAktørId(ANNEN_PART_ID)).thenReturn(Optional.of(ANNEN_PART_FNR));
        when(mockAktørConsumer.hentAktørIdForPersonIdent(ANNEN_PART_FNR)).thenReturn(Optional.of(ANNEN_PART_ID));

        task = new HentOgVurderInfotrygdSakTask(mockProsessTaskRepository, kodeverkRepository, mockGsakSakTjeneste, mockInfotrygdTjeneste, mockAktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance, startdatoDiffInstance);

        sakerTom = new ArrayList<>();

        GsakSak sak1 = new GsakSak(BRUKER_FNR, "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak(BRUKER_FNR, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD, LocalDate.now().minusMonths(9));
        GsakSak sak4 = new GsakSak(BRUKER_FNR, "id4", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD, LocalDate.now().minusYears(4));
        sakerMatchende = Arrays.asList(sak1, sak2);
        sakerMatchende2 = Arrays.asList(sak1, sak4);
    }

    private List<InfotrygdSak> createInfotrygdSaker(boolean inkluderInntektsmelding) {
        final List<InfotrygdSak> saker = new ArrayList<>();

        InfotrygdSak sak = new InfotrygdSak("123", "SP", "SV", LocalDate.now());
        saker.add(sak);

        if (inkluderInntektsmelding) {
            sak = new InfotrygdSak("124", "FA", "FØ", LocalDate.now());
            saker.add(sak);
        }

        return saker;
    }

    @Test
    public void test_doTask_ingenMatchendeInfotrygdSak() {
        when(mockGsakSakTjeneste.finnSaker(BRUKER_FNR)).thenReturn(sakerTom);

        MottakMeldingDataWrapper wrapperIn = opprettMottaksMelding();

        MottakMeldingDataWrapper wrapperOut = doTaskWithPrecondition(wrapperIn);

        assertThat(wrapperOut).isNotNull();
        assertThat(wrapperOut.getTema()).isEqualTo(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        assertThat(wrapperOut.getAktørId()).hasValueSatisfying(s -> assertThat(s).isEqualTo(BRUKER_AKTØR_ID));
        assertThat(wrapperOut.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
    }

    private MottakMeldingDataWrapper doTaskWithPrecondition(MottakMeldingDataWrapper wrapperIn) {
        task.precondition(wrapperIn);
        return task.doTask(wrapperIn);
    }

    @Test
    public void test_doTask_infotrygdsak_i_gsak_men_ikke_relevant_sak_i_infotrygd() {
        when(mockGsakSakTjeneste.finnSaker(any())).thenReturn(sakerMatchende);
        when(mockInfotrygdTjeneste.finnSakListe(eq(BRUKER_FNR), any())).thenReturn(createInfotrygdSaker(false));

        MottakMeldingDataWrapper wrapperIn = opprettMottaksMelding();

        MottakMeldingDataWrapper wrapperOut = doTaskWithPrecondition(wrapperIn);

        assertThat(wrapperOut).isNotNull();
        assertThat(wrapperOut.getTema()).isEqualTo(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        assertThat(wrapperOut.getAktørId()).hasValueSatisfying(s -> assertThat(s).isEqualTo(BRUKER_AKTØR_ID));
        assertThat(wrapperOut.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
        verify(mockInfotrygdTjeneste, times(1)).finnSakListe(any(), any());
    }

    @Test
    public void test_doTask_gammel_infotrygdsak_i_gsak_skip_infotrygd()  {
        when(mockGsakSakTjeneste.finnSaker(any())).thenReturn(sakerMatchende2);
        when(mockInfotrygdTjeneste.finnSakListe(eq(BRUKER_FNR), any())).thenReturn(new ArrayList<>());

        MottakMeldingDataWrapper wrapperIn = opprettMottaksMelding();

        MottakMeldingDataWrapper wrapperOut = doTaskWithPrecondition(wrapperIn);

        assertThat(wrapperOut).isNotNull();
        assertThat(wrapperOut.getTema()).isEqualTo(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        assertThat(wrapperOut.getAktørId()).hasValueSatisfying(s -> assertThat(s).isEqualTo(BRUKER_AKTØR_ID));
        assertThat(wrapperOut.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
        verify(mockInfotrygdTjeneste, times(0)).finnSakListe(eq(BRUKER_FNR), any());
    }

    @Test
    public void test_doTask_infotrygdsak_i_gsak_og_relevant_i_infotrygd() {
        when(mockGsakSakTjeneste.finnSaker(BRUKER_FNR)).thenReturn(sakerMatchende);
        when(mockInfotrygdTjeneste.finnSakListe(eq(BRUKER_FNR), any())).thenReturn(createInfotrygdSaker(true));

        when(mockGsakSakTjeneste.finnSaker(ANNEN_PART_FNR)).thenReturn(sakerMatchende);
        when(mockInfotrygdTjeneste.finnSakListe(eq(ANNEN_PART_FNR), any())).thenReturn(createInfotrygdSaker(true));

        MottakMeldingDataWrapper wrapperIn = opprettMottaksMelding();

        MottakMeldingDataWrapper wrapperOut = doTaskWithPrecondition(wrapperIn);

        assertTaskResult_WhenExceptingManuellJornalføring(wrapperOut);
    }

    @Test
    public void test_validerDatagrunnlag_skal_feile_ved_manglende_temakode_og_behandlingstype() throws Exception {
        MottakMeldingDataWrapper meldingDataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME));
        meldingDataWrapper.setArkivId("123454");
        meldingDataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        expectedException.expect(TekniskException.class);

        task.precondition(meldingDataWrapper);
    }

    @Test
    public void test_validerDatagrunnlag_uten_feil() throws Exception {
        MottakMeldingDataWrapper meldingIn = opprettMottaksMelding();
        meldingIn.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        task.precondition(meldingIn);
    }

    private MottakMeldingDataWrapper opprettMottaksMelding() {
        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper wrapperIn = new MottakMeldingDataWrapper(kodeverkRepository, data);
        wrapperIn.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        wrapperIn.setAktørId(BRUKER_AKTØR_ID);
        wrapperIn.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        wrapperIn.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        wrapperIn.setInntekstmeldingStartdato(LocalDate.now());
        wrapperIn.setAnnenPartId(ANNEN_PART_ID);
        return wrapperIn;
    }

    private void assertTaskResult_WhenExceptingManuellJornalføring(MottakMeldingDataWrapper wrapperOut) {
        assertThat(wrapperOut).isNotNull();
        assertThat(wrapperOut.getTema()).isEqualTo(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        assertThat(wrapperOut.getAktørId()).hasValueSatisfying(s -> assertThat(s).isEqualTo(BRUKER_AKTØR_ID));
        assertThat(wrapperOut.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }
}
