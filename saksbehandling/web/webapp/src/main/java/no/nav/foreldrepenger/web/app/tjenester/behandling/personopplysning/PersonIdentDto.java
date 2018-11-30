package no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Diskresjonskode;
import no.nav.foreldrepenger.domene.typer.AktørId;

public abstract class PersonIdentDto {

    private String fnr;
    private Long aktoerId;
    private Diskresjonskode diskresjonskode;

    public Diskresjonskode getDiskresjonskode() {
        return diskresjonskode;
    }

    public AktørId getAktoerId() {
        return aktoerId == null ? null : new AktørId(aktoerId);
    }

    public String getFnr() {
        return fnr;
    }

    void setFnr(String fnr) {
        this.fnr = fnr;
    }

    void setDiskresjonskode(Diskresjonskode diskresjonskode) {
        this.diskresjonskode = diskresjonskode;
    }

    void setAktoerId(AktørId aktoerId) {
        this.aktoerId = aktoerId.longValue();
    }

}
