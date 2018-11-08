package no.nav.foreldrepenger.behandlingslager.behandling;

/**
 * Kun for invortes bruk (Behandlingskontroll). Evt. tester. Skal ikke aksesseres direkte av andre under normal
 * operasjon.
 */
public interface InternalManipulerBehandling {
    /** Sett til angitt steg, default steg status, default slutt status for andre åpne steg. */
    void forceOppdaterBehandlingSteg(Behandling behandling, BehandlingStegType stegType);

    /** Sett Behandling til angitt steg, angitt steg status, defalt slutt status for andre åpne steg. */
    void forceOppdaterBehandlingSteg(Behandling behandling, BehandlingStegType stegType, BehandlingStegStatus stegStatus);

    /** Sett Behandling til angitt steg, angitt steg status, angitt slutt status for andre åpne steg. */
    void forceOppdaterBehandlingSteg(Behandling behandling, BehandlingStegType stegType, BehandlingStegStatus stegStatus,
            BehandlingStegStatus sluttStatusForEksisterendeSteg);

}
