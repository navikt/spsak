package no.nav.foreldrepenger.domene.registerinnhenting;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;

public interface EndringsresultatSjekker {

    EndringsresultatSnapshot opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(Behandling behandling);

    EndringsresultatDiff finnSporedeEndringerPåBehandlingsgrunnlag(Behandling behandling, EndringsresultatSnapshot idSnapshotFør);

    EndringsresultatSnapshot opprettEndringsresultatIdPåBehandlingSnapshot(Behandling behandling);

    EndringsresultatDiff finnIdEndringerPåBehandling(Behandling behandling, EndringsresultatSnapshot idSnapshotFør);
}
