package no.nav.foreldrepenger.domene.arbeidsforhold;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.domene.typer.AktørId;

public interface IAYRegisterInnhentingTjeneste {

    InntektArbeidYtelseAggregatBuilder innhentOpptjeningForInnvolverteParter(Behandling behandling, Interval opplysningsPeriode);

    InntektArbeidYtelseAggregatBuilder innhentInntekterFor(Behandling behandling, AktørId aktørId, Interval opplysningsPeriode, InntektsKilde... kilder);

    boolean skalInnhenteNæringsInntekterFor(Behandling behandling);

    Interval beregnOpplysningsPeriode(Behandling behandling);

    InntektArbeidYtelseAggregatBuilder innhentYtelserForInvolverteParter(Behandling behandling, Interval opplysningsPeriode);

    void lagre(Behandling behandling, InntektArbeidYtelseAggregatBuilder builder);

}
