package no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming;

public final class GrensesnittavstemmingSkjemaConstants {
    public static final String NAMESPACE = "http://nav.no/virksomhet/tjenester/avstemming/meldinger/v1";
    public static final String XSD_LOCATION = "xsd/grensesnittavstemmingskjema-v1.xsd";
    public static final Class<Avstemmingsdata> JAXB_CLASS = Avstemmingsdata.class;

    private GrensesnittavstemmingSkjemaConstants() {
    }
}
