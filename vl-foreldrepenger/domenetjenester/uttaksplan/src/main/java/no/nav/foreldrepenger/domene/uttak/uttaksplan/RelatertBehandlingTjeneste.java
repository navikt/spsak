package no.nav.foreldrepenger.domene.uttak.uttaksplan;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;

public interface RelatertBehandlingTjeneste {

    Optional<Behandling> hentAnnenPartsGjeldendeBehandling(Fagsak fagsak);

    Optional<UttakResultatEntitet> hentAnnenPartsGjeldendeUttaksplan(Behandling behandling);
}
