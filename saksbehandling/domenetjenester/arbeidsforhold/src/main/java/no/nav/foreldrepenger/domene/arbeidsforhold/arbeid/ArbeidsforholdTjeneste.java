package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid;

import java.util.List;
import java.util.Map;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.domene.typer.PersonIdent;

public interface ArbeidsforholdTjeneste {

    Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> finnArbeidsforholdForIdentIPerioden(PersonIdent fnr, Interval interval);
}
