package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

import no.nav.foreldrepenger.web.app.tjenester.behandling.SøknadType;

public class SoknadSykepengerDto extends SoknadDto {

    private String sykemeldingReferanse;
    private String søknadReferanse;

    public SoknadSykepengerDto() {
        super();
        this.setSoknadType(SøknadType.FØDSEL);
    }

    public String getSykemeldingReferanse() {
        return sykemeldingReferanse;
    }

    public void setSykemeldingReferanse(String sykemeldingReferanse) {
        this.sykemeldingReferanse = sykemeldingReferanse;
    }

    public String getSøknadReferanse() {
        return søknadReferanse;
    }

    public void setSøknadReferanse(String søknadReferanse) {
        this.søknadReferanse = søknadReferanse;
    }
}
