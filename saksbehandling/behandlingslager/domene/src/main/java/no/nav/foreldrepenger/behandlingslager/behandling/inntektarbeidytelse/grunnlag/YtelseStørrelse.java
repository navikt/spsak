package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.typer.Beløp;


public interface YtelseStørrelse {

    Optional<Virksomhet> getVirksomhet();

    Beløp getBeløp();

    InntektPeriodeType getHyppighet();
}
