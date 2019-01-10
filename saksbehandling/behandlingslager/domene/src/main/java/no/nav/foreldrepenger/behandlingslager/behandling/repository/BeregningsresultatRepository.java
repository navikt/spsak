package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPerioder;

public interface BeregningsresultatRepository extends BehandlingslagerRepository {

    Optional<BeregningsresultatPerioder> hentHvisEksisterer(Behandlingsresultat behandlingsresultat);

    Optional<BeregningsResultat> hentHvisEksistererFor(Behandlingsresultat behandlingsresultat);

    long lagre(Behandlingsresultat behandlingsresultat, BeregningsresultatPerioder beregningsresultat);

    void deaktiverBeregningsresultat(Behandlingsresultat behandlingsresultat, BehandlingLås skriveLås);
}
