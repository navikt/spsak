package no.nav.foreldrepenger.behandlingslager.behandling.resultat.fravær;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface FraværPeriode {

    DatoIntervallEntitet getPeriode();

    Arbeidsgiver getArbeidsgiver();

    boolean getErGradert();

    Optional<Prosentsats> getGradering();

}
