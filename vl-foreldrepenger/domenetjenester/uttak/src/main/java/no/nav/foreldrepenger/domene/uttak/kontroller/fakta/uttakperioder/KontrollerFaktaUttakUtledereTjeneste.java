package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

import java.util.List;


public interface KontrollerFaktaUttakUtledereTjeneste {

    List<AksjonspunktUtleder> utledUtledereFor(Behandling behandling);
}
