package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.util.Collection;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;

public interface InntektArbeidYtelseAggregat {

    Collection<AktørInntekt> getAktørInntekt();

    Collection<AktørArbeid> getAktørArbeid();

    Collection<AktørYtelse> getAktørYtelse();

}
