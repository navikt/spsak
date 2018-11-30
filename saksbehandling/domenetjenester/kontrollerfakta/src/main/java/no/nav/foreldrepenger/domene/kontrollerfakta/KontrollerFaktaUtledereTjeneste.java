package no.nav.foreldrepenger.domene.kontrollerfakta;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

import java.util.List;

public interface KontrollerFaktaUtledereTjeneste {

    List<AksjonspunktUtleder> utledUtledereFor(Behandling behandling);
}
