package no.nav.foreldrepenger.batch;

import no.nav.foreldrepenger.batch.feil.UnknownArgumentsReceivedVLBatchException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EmptyBatchArgumentsTest {

    @Test(expected = UnknownArgumentsReceivedVLBatchException.class)
    public void skal_kaste_exception_ved_for_mange_argumenter() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("asdf", "asdf");
        new EmptyBatchArguments(map);
    }

    @Test
    public void skal_ikke_kaste_exception_ved_ingen_argumenter() throws Exception {
        Map<String, String> map = new HashMap<>();
        final EmptyBatchArguments emptyBatchArguments = new EmptyBatchArguments(map);

        assertThat(emptyBatchArguments.isValid()).isTrue();
    }
}
