package no.nav.foreldrepenger.domene.arbeidsforhold;

import java.util.List;
import java.util.Map;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Arbeidsforhold;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdSakOgGrunnlag;
import no.nav.foreldrepenger.domene.typer.AktørId;

public interface InnhentingSamletTjeneste {

    InntektsInformasjon getInntektsInformasjon(AktørId aktørId, Behandling behandling, Interval periode, InntektsKilde kilde);

    Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> getArbeidsforhold(AktørId aktørId, Interval opplysningsPeriode);

    List<InfotrygdSakOgGrunnlag> getSammenstiltSakOgGrunnlag(Behandling behandling, AktørId aktørId, Interval opplysningsPeriode, boolean medGrunnlag);

    List<MeldekortUtbetalingsgrunnlagSak> hentYtelserTjenester(Behandling behandling, AktørId aktørId, Interval opplysningsPeriode);
}
