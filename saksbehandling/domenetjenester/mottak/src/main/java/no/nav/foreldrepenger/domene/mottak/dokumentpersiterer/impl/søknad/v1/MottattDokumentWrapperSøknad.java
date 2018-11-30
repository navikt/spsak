package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.søknad.v1;

import java.util.List;

import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.MottattDokumentWrapper;
import no.nav.sykepenger.kontrakter.søknad.v1.SykepengesøknadV1;
import no.nav.sykepenger.kontrakter.søknad.v1.opptjening.AnnenInntektskilde;

public class MottattDokumentWrapperSøknad extends MottattDokumentWrapper<SykepengesøknadV1> {

    public MottattDokumentWrapperSøknad(SykepengesøknadV1 skjema) {
        super(skjema, "no.nav.sykepenger.kontrakter.søknad.v1.SykepengesøknadV1");
    }

    public String getArbeidstaker() {
        return getSkjema().getBrukerAktørId();
    }

    public String getArbeidsgiver() {
        return getSkjema().getArbeidsgiverId();
    }

    public List<AnnenInntektskilde> getAndreInntektskilder() {
        return getSkjema().getAndreInntektskilder();
    }

}
