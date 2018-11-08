package no.nav.foreldrepenger.web.app.tjenester.batch;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchSupportTjeneste;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.batch.feil.InvalidArgumentsVLBatchException;
import no.nav.foreldrepenger.batch.impl.BatchSupportTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.batch.args.BatchArgumentsDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import no.nav.vedtak.felles.testutilities.Whitebox;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
public class BatchRestTjenesteTest {

    private BatchRestTjeneste tjeneste;

    private BatchSupportTjeneste batchSupportTjeneste;

    @Before
    public void setUp() throws Exception {
        batchSupportTjeneste = mock(BatchSupportTjeneste.class);
        tjeneste = new BatchRestTjeneste(batchSupportTjeneste);
    }

    @Test
    public void skal_gi_status_400_ved_ukjent_batchname() throws Exception {
        when(batchSupportTjeneste.finnBatchTjenesteForNavn(any())).thenReturn(null);
        final Response response = tjeneste.startBatch(new BatchNameDto("asdf"), null);
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.BAD_REQUEST);
    }

    @Test(expected = InvalidArgumentsVLBatchException.class)
    public void skal_gi_exception_ved_ugyldig_job_parametere() {
        final HashMap<String, BatchTjeneste> stringBatchTjenesteHashMap = new HashMap<>();
        final BatchTjeneste value = mock(BatchTjeneste.class);
        final BatchArgumentsDto args = new BatchArgumentsDto();
        final String key = "mock";

        Map<String, String> arguments = new HashMap<>();

        when(value.getBatchName()).thenReturn(key);
        when(value.createArguments(any())).thenReturn(new UgyldigeBatchArguments(arguments));
        stringBatchTjenesteHashMap.put(key, value);

        when(batchSupportTjeneste.finnBatchTjenesteForNavn(any())).thenReturn(value);

        args.setJobParameters("asdf=1");
        tjeneste.startBatch(new BatchNameDto(key), args);
    }

    @Test
    public void skal_kalle_paa_tjeneste_ved_gyldig_() {
        final HashMap<String, BatchTjeneste> stringBatchTjenesteHashMap = new HashMap<>();
        Map<String, String> arguments = new HashMap<>();
        final GyldigeBatchArguments gyldigeBatchArguments = new GyldigeBatchArguments(arguments);
        final BatchArgumentsDto args = new BatchArgumentsDto();
        final BatchTjeneste value = mock(BatchTjeneste.class);
        final String key = "mock";
        when(value.getBatchName()).thenReturn(key);
        when(value.createArguments(any())).thenReturn(gyldigeBatchArguments);
        stringBatchTjenesteHashMap.put(key, value);

        when(batchSupportTjeneste.finnBatchTjenesteForNavn(any())).thenReturn(value);

        args.setJobParameters("asdf=1");
        tjeneste.startBatch(new BatchNameDto(key), args);

        verify(value).launch(gyldigeBatchArguments);
    }

    public static class UgyldigeBatchArguments extends BatchArguments {

        UgyldigeBatchArguments(Map<String, String> arguments) {
            super(arguments);
        }

        @Override
        public boolean settParameterVerdien(String key, String value) {
            return true;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public String toString() {
            return "UgyldigeBatchArguments{}";
        }
    }

    public static class GyldigeBatchArguments extends BatchArguments {

        GyldigeBatchArguments(Map<String, String> arguments) {
            super(arguments);
        }

        @Override
        public boolean settParameterVerdien(String key, String value) {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public String toString() {
            return "GyldigeBatchArguments{}";
        }
    }
}
