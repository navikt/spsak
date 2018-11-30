package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.domene.typer.AktørId;

public interface MeldekortTjeneste {
    List<MeldekortUtbetalingsgrunnlagSak> hentMeldekortListe(AktørId aktørId, LocalDate fom, LocalDate tom);
}
