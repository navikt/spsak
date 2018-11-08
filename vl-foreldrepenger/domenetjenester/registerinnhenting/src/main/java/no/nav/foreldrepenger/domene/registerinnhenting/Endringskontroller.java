package no.nav.foreldrepenger.domene.registerinnhenting;

import java.time.LocalDateTime;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;

public interface Endringskontroller {
    void gjenoppta(Behandling behandling);

    boolean settPåVent(Behandling behandling, AksjonspunktDefinisjon apDef, LocalDateTime fristTid, Venteårsak oppdateringÅpenBehandling);

    void taAvVent(Behandling behandling, AksjonspunktDefinisjon autopunkt);

    void spolTilSteg(Behandling behandling, BehandlingStegType behandlingStegType);

    void spolTilStartpunkt(Behandling behandling, EndringsresultatDiff endringsresultat);
}
