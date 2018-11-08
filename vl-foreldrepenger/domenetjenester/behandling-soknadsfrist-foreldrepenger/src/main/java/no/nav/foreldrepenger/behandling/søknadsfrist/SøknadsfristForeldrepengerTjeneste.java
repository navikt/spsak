package no.nav.foreldrepenger.behandling.søknadsfrist;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

public interface SøknadsfristForeldrepengerTjeneste {

    Optional<AksjonspunktDefinisjon> vurderSøknadsfristForForeldrepenger(BehandlingskontrollKontekst kontekst);

    void lagreVurderSøknadsfristResultat(Behandling behandling, VurderSøknadsfristAksjonspunktDto adapter);

    LocalDate finnSøknadsfristForPeriodeMedStart(LocalDate periodeStart);
}
