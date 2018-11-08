package no.nav.foreldrepenger.behandling.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.util.StringUtils;

public class FinnAnsvarligSaksbehandler {

    private static final String DEFAULT_ANSVARLIG_SAKSBEHANDLER = "VL";

    private FinnAnsvarligSaksbehandler() {
        // hide public contructor
    }

    public static String finn(Behandling behandling) {
        if (!StringUtils.isBlank(behandling.getAnsvarligBeslutter())) {
            return behandling.getAnsvarligBeslutter();
        } else if (!StringUtils.isBlank(behandling.getAnsvarligSaksbehandler())) {
            return behandling.getAnsvarligSaksbehandler();
        }
        return DEFAULT_ANSVARLIG_SAKSBEHANDLER;
    }
}
