package no.nav.foreldrepenger.grensesnittavstemming;

import org.junit.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;


public class GrensesnittavstemmingBatchArgumentsTest {

    public static final String ANTALL_DAGER = "antallDager";
    public static final String FAGOMRÅDE = "fagomrade";
    public static final String TOM = "tom";
    public static final String FOM = "fom";

    @Test
    public void skal_parse_antall_dager() {
        final HashMap<String, String> argMap = new HashMap<>();
        argMap.put(ANTALL_DAGER, "5");
        argMap.put(FAGOMRÅDE, "FP");
        GrensesnittavstemmingBatchArguments args = new GrensesnittavstemmingBatchArguments(argMap);
        assertThat(args.isValid()).isTrue();
        assertThat(args.getFom()).isNotNull();
        assertThat(args.getTom()).isNotNull();
    }

    @Test
    public void skal_parse_antall_8_dager() {
        final HashMap<String, String> argMap = new HashMap<>();
        argMap.put(ANTALL_DAGER, "8");
        argMap.put(FAGOMRÅDE, "FP");
        GrensesnittavstemmingBatchArguments args = new GrensesnittavstemmingBatchArguments(argMap);
        assertThat(args.isValid()).isTrue();
        assertThat(args.getFom()).isNotNull();
        assertThat(args.getTom()).isNotNull();
    }

    @Test
    public void skal_parse_antall_9_dager_utover_max() {
        final HashMap<String, String> argMap = new HashMap<>();
        argMap.put(FAGOMRÅDE, "FP");
        argMap.put(ANTALL_DAGER, "9");
        GrensesnittavstemmingBatchArguments args = new GrensesnittavstemmingBatchArguments(argMap);
        assertThat(args.isValid()).isFalse();
        assertThat(args.getFom()).isNotNull();
        assertThat(args.getTom()).isNotNull();
    }

    @Test
    public void skal_parse_dato() {
        final HashMap<String, String> argMap = new HashMap<>();
        argMap.put(FOM, "01-11-2014");
        argMap.put(TOM, "07-11-2014");
        argMap.put(FAGOMRÅDE, "FP");
        GrensesnittavstemmingBatchArguments args = new GrensesnittavstemmingBatchArguments(argMap);
        assertThat(args.isValid()).isTrue();
        assertThat(args.getFom()).isNotNull();
        assertThat(args.getTom()).isNotNull();
    }

    @Test
    public void skal_parse_dato_periode_utover_max() {
        final HashMap<String, String> argMap = new HashMap<>();
        argMap.put(FOM, "01-11-2014");
        argMap.put(TOM, "09-11-2014");
        argMap.put(FAGOMRÅDE, "FP");
        GrensesnittavstemmingBatchArguments args = new GrensesnittavstemmingBatchArguments(argMap);
        assertThat(args.isValid()).isFalse();
        assertThat(args.getFom()).isNotNull();
        assertThat(args.getTom()).isNotNull();
    }

    @Test
    public void skal_parse_dato_periode_7_dager() {
        final HashMap<String, String> argMap = new HashMap<>();
        argMap.put(FOM, "01-11-2014");
        argMap.put(TOM, "08-11-2014");
        argMap.put(FAGOMRÅDE, "FP");
        GrensesnittavstemmingBatchArguments args = new GrensesnittavstemmingBatchArguments(argMap);
        assertThat(args.isValid()).isTrue();
        assertThat(args.getFom()).isNotNull();
        assertThat(args.getTom()).isNotNull();
    }

    @Test
    public void skal_ikke_feile_ved_satt_for_mange_properties() {
        final HashMap<String, String> argMap = new HashMap<>();
        argMap.put(FOM, "01-11-2014");
        argMap.put(TOM, "20-11-2014");
        argMap.put(ANTALL_DAGER, "5");
        argMap.put(FAGOMRÅDE, "FP");
        GrensesnittavstemmingBatchArguments args = new GrensesnittavstemmingBatchArguments(argMap);
        assertThat(args.isValid()).isFalse();
    }

    @Test
    public void skal_feile_fordi_satt_parametre_er_ikke_entydig() {
        final HashMap<String, String> argMap = new HashMap<>();
        argMap.put(FOM, "01-11-2014");
        argMap.put(ANTALL_DAGER, "5");
        argMap.put(FAGOMRÅDE, "FP");
        GrensesnittavstemmingBatchArguments args = new GrensesnittavstemmingBatchArguments(argMap);
        assertThat(args.isValid()).isFalse();
    }

    @Test
    public void skal_feile_fordi_fagområde_er_ikke_satt() {
        final HashMap<String, String> argMap = new HashMap<>();
        argMap.put(FOM, "01-11-2014");
        argMap.put(ANTALL_DAGER, "5");
        GrensesnittavstemmingBatchArguments args = new GrensesnittavstemmingBatchArguments(argMap);
        assertThat(args.isValid()).isFalse();
    }

    @Test
    public void skal_feile_fordi_fagområde_er_feil() {
        final HashMap<String, String> argMap = new HashMap<>();
        argMap.put(FOM, "01-11-2014");
        argMap.put(ANTALL_DAGER, "5");
        argMap.put(FAGOMRÅDE, "blabla");
        GrensesnittavstemmingBatchArguments args = new GrensesnittavstemmingBatchArguments(argMap);
        assertThat(args.isValid()).isFalse();
    }

    @Test
    public void skal_feile_fordi_fagområde_er_null() {
        final HashMap<String, String> argMap = new HashMap<>();
        argMap.put(ANTALL_DAGER, "8");
        GrensesnittavstemmingBatchArguments args = new GrensesnittavstemmingBatchArguments(argMap);
        assertThat(args.isValid()).isFalse();
        assertThat(args.getFom()).isNotNull();
        assertThat(args.getTom()).isNotNull();
    }

}
