package no.nav.foreldrepenger.økonomi.simulering;


import no.nav.system.os.tjenester.oppdragservice.oppdragservicegrensesnitt.SimulerBeregningRequest;
import no.nav.system.os.tjenester.oppdragservice.oppdragservicegrensesnitt.SimulerBeregningResponse;

public final class SimuleringConstants {

    public static final String NAMESPACE = "http://nav.no/system/os/tjenester/oppdragService/oppdragServiceGrensesnitt";
    public static final String XSD_LOCATION = "xsd/tjenester/oppdragService/oppdragServiceGrensesnitt.xsd";
    public static final Class<SimulerBeregningRequest> JAXB_CLASS_REQUEST = SimulerBeregningRequest.class;
    public static final Class<SimulerBeregningResponse> JAXB_CLASS_RESPONSE = SimulerBeregningResponse.class;

    private SimuleringConstants() {
        // skal ikke instansieres
    }

}
