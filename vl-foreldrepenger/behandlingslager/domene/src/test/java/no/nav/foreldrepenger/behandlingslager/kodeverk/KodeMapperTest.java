package no.nav.foreldrepenger.behandlingslager.kodeverk;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KodeMapperTest {

    @Test
    public void skalMappeFraKodelisteTilNoeAnnet() {
        KodeMapper<TestKodeliste, String> testMapper = KodeMapper
            .medMapping(TestKodeliste.KODE_1, "MappingForKode1")
            .medMapping(TestKodeliste.KODE_2, "MappingForKode2")
            .build();

        assertThat(testMapper.map(TestKodeliste.KODE_1)).hasValue("MappingForKode1");
        assertThat(testMapper.map(TestKodeliste.KODE_2)).hasValue("MappingForKode2");
        assertThat(testMapper.map(TestKodeliste.KODE_3)).isNotPresent();
    }

    @Test
    public void skalMappeFraNoeAnnetTilKodeliste() {
        KodeMapper<TestKodeliste, String> testMapper = KodeMapper
            .medMapping(TestKodeliste.KODE_1, "MappingForKode1")
            .medMapping(TestKodeliste.KODE_2, "MappingForKode2")
            .build();

        assertThat(testMapper.omvendtMap("MappingForKode1")).hasValue(TestKodeliste.KODE_1);
        assertThat(testMapper.omvendtMap("MappingForKode2")).hasValue(TestKodeliste.KODE_2);
        assertThat(testMapper.omvendtMap("MappingForKode3")).isNotPresent();
    }

    private static class TestKodeliste extends Kodeliste {
        private static final TestKodeliste KODE_1 = new TestKodeliste("KODE_1");
        private static final TestKodeliste KODE_2 = new TestKodeliste("KODE_2");
        private static final TestKodeliste KODE_3 = new TestKodeliste("KODE_3");

        private TestKodeliste(String kode) {
            super(kode, "TestKodeliste");
        }
    }
}
