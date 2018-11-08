package no.nav.foreldrepenger.behandlingskontroll.observer;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;

public class StegTransisjon {
    private BehandlingSteg.TransisjonType transisjonType;
    private BehandlingStegType behandlingStegType;

    public static StegTransisjon hoppFremoverOver(BehandlingStegType behandlingStegType) {
        return new StegTransisjon(BehandlingSteg.TransisjonType.HOPP_OVER_FRAMOVER, behandlingStegType);
    }

    static StegTransisjon hoppTilbakeOver(BehandlingStegType behandlingStegType) {
        return new StegTransisjon(BehandlingSteg.TransisjonType.HOPP_OVER_BAKOVER, behandlingStegType);
    }

    public StegTransisjon(BehandlingSteg.TransisjonType transisjonType, BehandlingStegType behandlingStegType) {
        this.transisjonType = transisjonType;
        this.behandlingStegType = behandlingStegType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StegTransisjon)) {
            return false;
        }
        StegTransisjon that = (StegTransisjon) o;
        return transisjonType == that.transisjonType
            && Objects.equals(behandlingStegType, that.behandlingStegType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transisjonType, behandlingStegType);
    }

    @Override
    public String toString() {
        return "StegTransisjon{" +
            "transisjonType=" + transisjonType +
            ", behandlingStegType=" + behandlingStegType +
            '}';
    }
}
