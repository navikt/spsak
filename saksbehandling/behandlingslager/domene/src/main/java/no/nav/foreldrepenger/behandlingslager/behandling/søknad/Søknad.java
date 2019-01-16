package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.Sykefravær;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;

public interface Søknad {

    LocalDate getSøknadsdato();

    LocalDate getMottattDato();

    String getSøknadReferanse();

    String getSykemeldingReferanse();

    Arbeidsgiver getArbeidsgiver();

    Sykefravær getOppgittSykefravær();

    OppgittOpptjening getOppgittOpptjening();

    String getTilleggsopplysninger();

}
