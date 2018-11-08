package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighet;

public class OppgittRettighetDto {
    private boolean omsorgForBarnet;
    private boolean aleneomsorgForBarnet;

    public OppgittRettighetDto() {
        // trengs for deserialisering av JSON
    }

    private OppgittRettighetDto(boolean harOmsorgForBarnet, boolean harAleneomsorg) {
        this.omsorgForBarnet = harOmsorgForBarnet;
        this.aleneomsorgForBarnet = harAleneomsorg;
    }

    public static OppgittRettighetDto mapFra(Søknad søknad) {
        OppgittRettighet oppgittRettighet = søknad.getRettighet();
        if(oppgittRettighet != null) {
            return new OppgittRettighetDto(
                oppgittRettighet.getHarOmsorgForBarnetIHelePerioden(),
                oppgittRettighet.getHarAleneomsorgForBarnet());
        }
        return null;
    }

    public boolean isOmsorgForBarnet() {
        return omsorgForBarnet;
    }

    public void setOmsorgForBarnet(boolean omsorgForBarnet) {
        this.omsorgForBarnet = omsorgForBarnet;
    }

    public boolean isAleneomsorgForBarnet() {
        return aleneomsorgForBarnet;
    }

    public void setAleneomsorgForBarnet(boolean aleneomsorgForBarnet) {
        this.aleneomsorgForBarnet = aleneomsorgForBarnet;
    }
}
