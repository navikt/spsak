package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;

public class ManglendeVedleggDto {
    private DokumentTypeId dokumentType;
    private VirksomhetDto arbeidsgiver;
    private boolean brukerHarSagtAtIkkeKommer = false;

    public DokumentTypeId getDokumentType() {
        return dokumentType;
    }

    public void setDokumentType(DokumentTypeId dokumentType) {
        this.dokumentType = dokumentType;
    }

    public VirksomhetDto getArbeidsgiver() {
        return arbeidsgiver;
    }

    public void setArbeidsgiver(VirksomhetDto arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    public boolean getBrukerHarSagtAtIkkeKommer() {
        return brukerHarSagtAtIkkeKommer;
    }

    public void setBrukerHarSagtAtIkkeKommer(boolean brukerHarSagtAtIkkeKommer) {
        this.brukerHarSagtAtIkkeKommer = brukerHarSagtAtIkkeKommer;
    }
}
