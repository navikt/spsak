package no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;

public interface InntektTjeneste {

    InntektsInformasjon finnInntekt(FinnInntektRequest finnInntektRequest, InntektsKilde inntektsKilde);
}
