package no.nav.foreldrepenger.web.app.tjenester.dokument.dto;

import javax.validation.constraints.Digits;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class DokumentIdDto implements AbacDto {
    @Digits(integer = 18, fraction = 0)
    private String dokumentId;

    public DokumentIdDto(String dokumentId) {
        this.dokumentId = dokumentId;
    }

    public String getDokumentId() {
        return dokumentId;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilDokumentId(dokumentId);
    }
}
