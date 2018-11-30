package no.nav.foreldrepenger.domene.kontrollerfakta;

import java.util.List;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

public interface KontrollerFaktaTjeneste {

    List<AksjonspunktResultat> utledAksjonspunkter(Long behandlingId);

    List<AksjonspunktResultat> utledAksjonspunkterTilHøyreForStartpunkt(Long behandlingId, StartpunktType startpunktType);

}
