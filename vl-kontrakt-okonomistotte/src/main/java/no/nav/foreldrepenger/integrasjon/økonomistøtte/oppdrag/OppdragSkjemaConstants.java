package no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag;

public final class OppdragSkjemaConstants {
    public static final String NAMESPACE = "http://www.trygdeetaten.no/skjema/oppdrag";
    public static final String XSD_LOCATION = "xsd/oppdragskjema-v1.xsd";
    
    public static final Class<Oppdrag> JAXB_CLASS = Oppdrag.class;

    private OppdragSkjemaConstants() {
    }
}
