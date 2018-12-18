package no.nav.foreldrepenger.mottak.felles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;

import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class WrappedProsessTaskHandlerTest {

    private WrappedProsessTaskHandler wrappedProsessTaskHandler; // objektet vi tester

    private ProsessTaskRepository mockProsessTaskRepository;
    private ProsessTaskData prosessTaskData;
    private MottakMeldingDataWrapper returnedDataWrapper;
    private KodeverkRepository kodeverkRepository = mock(KodeverkRepository.class);

    @Before
    public void setup() {
        mockProsessTaskRepository = mock(ProsessTaskRepository.class);
        wrappedProsessTaskHandler = new MyWrappedProsessTaskHandler(mockProsessTaskRepository, kodeverkRepository);
        prosessTaskData = new ProsessTaskData("type");
        returnedDataWrapper = null;
    }

    @Test
    public void test_doTask_returnedWrapper() {
        returnedDataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, prosessTaskData);

        wrappedProsessTaskHandler.doTask(prosessTaskData);

        verify(mockProsessTaskRepository).lagre(prosessTaskData);

    }

    @Test
    public void skal_gi_fornuftig_navn_til_metering() throws Exception {
        final String meterNameFromTask = wrappedProsessTaskHandler.generateUniqueToFromKey(new MottakMeldingDataWrapper(kodeverkRepository, prosessTaskData), new MottakMeldingDataWrapper(kodeverkRepository, prosessTaskData));
        assertThat(meterNameFromTask).isEqualTo("mottak.tasks.fra.type.til.type");
    }

    @Test
    public void test_doTask_returnedNull() {
        wrappedProsessTaskHandler.doTask(prosessTaskData);

        verify(mockProsessTaskRepository, never()).lagre(any(ProsessTaskData.class));
    }

    //-------

    private class MyWrappedProsessTaskHandler extends WrappedProsessTaskHandler {

        public MyWrappedProsessTaskHandler(ProsessTaskRepository prosessTaskRepository, KodeverkRepository kodeverkRepository) {
            super(prosessTaskRepository, kodeverkRepository);
        }

        @Override
        public void precondition(MottakMeldingDataWrapper dataWrapper) {
            //Alt er OK her :)
        }

        @Override
        public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
            return returnedDataWrapper;
        }
    }
}
