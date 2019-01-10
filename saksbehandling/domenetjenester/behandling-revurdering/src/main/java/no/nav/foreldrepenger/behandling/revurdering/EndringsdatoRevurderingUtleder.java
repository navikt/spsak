package no.nav.foreldrepenger.behandling.revurdering;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;

@ApplicationScoped
public class EndringsdatoRevurderingUtleder {
    EndringsdatoRevurderingUtleder() {
        // for CDI
    }

    public LocalDate utledEndringsdato(Behandlingsresultat behandling) {
        // FIXME SP - Dummy implementasjon for å få det til å snurre
        return behandling.getBehandlingVedtak().getVedtaksdato();
    }
}
