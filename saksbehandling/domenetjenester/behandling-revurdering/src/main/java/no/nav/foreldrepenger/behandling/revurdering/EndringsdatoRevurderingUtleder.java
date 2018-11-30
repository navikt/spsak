package no.nav.foreldrepenger.behandling.revurdering;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

@ApplicationScoped
public class EndringsdatoRevurderingUtleder {
    EndringsdatoRevurderingUtleder() {
        // for CDI
    }

    public LocalDate utledEndringsdato(Behandling behandling) {
        // FIXME SP - Dummy implementasjon for å få det til å snurre
        return behandling.getOriginalVedtaksDato();
    }
}
