package no.nav.foreldrepenger.behandlingskontroll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;

/** For å få tak i riktig status konfigurasjon. */
public class BehandlingStegKonfigurasjon {

    private List<BehandlingStegStatus> statuser;

    public BehandlingStegKonfigurasjon(List<BehandlingStegStatus> list) {
        this.statuser = new ArrayList<>(list);
    }

    public BehandlingStegStatus getStartet() {
        return mapTilStatusEntitet(BehandlingStegStatus.STARTET);
    }

    public BehandlingStegStatus getInngang() {
        return mapTilStatusEntitet(BehandlingStegStatus.INNGANG);
    }

    public BehandlingStegStatus getVenter() {
        return mapTilStatusEntitet(BehandlingStegStatus.VENTER);
    }

    public BehandlingStegStatus getUtgang() {
        return mapTilStatusEntitet(BehandlingStegStatus.UTGANG);
    }

    public BehandlingStegStatus getAvbrutt() {
        return mapTilStatusEntitet(BehandlingStegStatus.AVBRUTT);
    }

    public BehandlingStegStatus getUtført() {
        return mapTilStatusEntitet(BehandlingStegStatus.UTFØRT);
    }

    public BehandlingStegStatus getTilbakeført() {
        return mapTilStatusEntitet(BehandlingStegStatus.TILBAKEFØRT);
    }

    public BehandlingStegStatus mapTilStatus(BehandlingStegResultat stegResultat) {
        BehandlingStegStatus status = BehandlingStegResultat.mapTilStatus(stegResultat);
        return mapTilStatusEntitet(status);
    }

    private BehandlingStegStatus mapTilStatusEntitet(BehandlingStegStatus status) {
        return statuser.get(statuser.indexOf(status));
    }

    /** Kun for test. Lager en dummy av alle tilgjengelige statuser. */
    public static BehandlingStegKonfigurasjon lagDummy() {
        List<BehandlingStegStatus> statuser = Arrays.asList(BehandlingStegStatus.AVBRUTT, BehandlingStegStatus.STARTET, BehandlingStegStatus.INNGANG,
                BehandlingStegStatus.VENTER, BehandlingStegStatus.UTGANG, BehandlingStegStatus.UTFØRT,
                BehandlingStegStatus.TILBAKEFØRT);
        return new BehandlingStegKonfigurasjon(statuser);
    }

}
