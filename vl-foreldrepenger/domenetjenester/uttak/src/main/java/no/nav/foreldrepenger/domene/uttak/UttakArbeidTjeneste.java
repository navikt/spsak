package no.nav.foreldrepenger.domene.uttak;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;

public interface UttakArbeidTjeneste {

    List<Yrkesaktivitet> hentYrkesAktiviteterOrdinærtArbeidsforhold(Behandling behandling);

    List<Yrkesaktivitet> hentAlleYrkesaktiviteter(Behandling behandling);

    List<Yrkesaktivitet> hentYrkesAktiviteterFrilans(Behandling behandling);

    List<Inntektsmelding> hentInntektsmeldinger(Behandling behandling);

    boolean erArbeidstaker(Behandling behandling);
}
