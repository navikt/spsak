package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl;

import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.MottattDokumentFeil;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.inntektsmelding.v1.MottattDokumentWrapperInntektsmelding;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;

public abstract class MottattDokumentWrapper<S, V> {

    private S skjema;
    private String namespace;

    protected MottattDokumentWrapper(S skjema, String namespace) {
        this.skjema = skjema;
        this.namespace = namespace;
    }

    @SuppressWarnings("rawtypes")
    public static MottattDokumentWrapper tilXmlWrapper(Object skjema) {
        if (skjema instanceof InntektsmeldingM) {
            return new MottattDokumentWrapperInntektsmelding((InntektsmeldingM) skjema);
        }
        throw MottattDokumentFeil.FACTORY.ukjentSkjemaType(skjema.getClass().getCanonicalName()).toException();
    }

    public S getSkjema() {
        return this.skjema;
    }

    String getSkjemaType() {
        return this.namespace;
    }
}
