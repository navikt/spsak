package no.nav.foreldrepenger.domene.uttak;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;

public interface InntektsmeldingVilEndreUttakTjeneste {
    boolean graderingVilEndreUttak(Behandling behandling, Inntektsmelding inntektsmelding);

    boolean utsettelseArbeidVilEndreUttak(Behandling behandling, Inntektsmelding inntektsmelding);

    boolean utsettelseFerieVilEndreUttak(Behandling behandling, Inntektsmelding inntektsmelding);
}
