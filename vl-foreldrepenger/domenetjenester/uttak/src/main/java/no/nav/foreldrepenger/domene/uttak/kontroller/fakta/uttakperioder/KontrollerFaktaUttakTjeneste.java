package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder;

import java.util.List;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface KontrollerFaktaUttakTjeneste {

    List<AksjonspunktResultat> utledAksjonspunkter(Behandling behandling);

    KontrollerFaktaData hentKontrollerFaktaPerioder(Behandling behandling);

    boolean finnesOverlappendePerioder(Behandling behandling);

    List<UttakPeriodeEndringDto> finnEndringMellomOppgittOgGjeldendePerioder(Behandling behandling);

    List<UttakPeriodeEndringDto> finnEndringMellomOppgittOgGjeldendePerioder(Long aggregatId);
}
