package no.nav.foreldrepenger.domene.kontrollerfakta.arbeidsforhold;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

@ApplicationScoped
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@StartpunktRef("KONTROLLER_ARBEIDSFORHOLD")
public class KontrollerArbeidsforholdUtledereTjenesteImpl implements KontrollerArbeidsforholdUtledereTjeneste {

    KontrollerArbeidsforholdUtledereTjenesteImpl() {
        // CDI
    }

    @Override
    public List<AksjonspunktUtleder> utledUtledereFor(Behandling behandling) {
        final Instance<? extends AksjonspunktUtleder> instance = CDI.current().select(AksjonspunktUtlederForVurderArbeidsforhold.class);
        AksjonspunktUtleder minInstans = instance.get();
        if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException(
                "Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
        }
        return Collections.unmodifiableList(Collections.singletonList(minInstans));
    }
}
