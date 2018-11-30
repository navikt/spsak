package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;

public class ManglendeVedlegg {

    private final DokumentTypeId dokumentType;
    private final String arbeidsgiver;
    private Boolean brukerHarSagtAtIkkeKommer = false;

    public ManglendeVedlegg(DokumentTypeId dokumentType) {
        this(dokumentType, null);
    }

    public ManglendeVedlegg(DokumentTypeId dokumentType, String arbeidsgiver) {
        this.dokumentType = dokumentType;
        this.arbeidsgiver = arbeidsgiver;
    }

    public ManglendeVedlegg(DokumentTypeId dokumentType, String arbeidsgiver, Boolean brukerHarSagtAtIkkeKommer) {
        this.dokumentType = dokumentType;
        this.arbeidsgiver = arbeidsgiver;
        this.brukerHarSagtAtIkkeKommer = brukerHarSagtAtIkkeKommer;
    }

    public DokumentTypeId getDokumentType() {
        return dokumentType;
    }

    public String getArbeidsgiver() {
        return arbeidsgiver;
    }

    public Boolean getBrukerHarSagtAtIkkeKommer() {
        return brukerHarSagtAtIkkeKommer;
    }
}
