package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface SykefraværPeriode {

    LocalDate getFom();

    DatoIntervallEntitet getPeriode();

    SykefraværPeriodeType getType();

    Arbeidsgiver getArbeidsgiver();

    boolean getSkalGradere();

    Optional<Prosentsats> getGradering();

    Prosentsats getArbeidsgrad();
}
