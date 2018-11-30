package no.nav.foreldrepenger.domene.kontrollerfakta.arbeidsforhold;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

import java.util.List;

public interface KontrollerArbeidsforholdUtledereTjeneste {

    List<AksjonspunktUtleder> utledUtledereFor(Behandling behandling);
}
