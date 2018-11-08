package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.Digits;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.behandlingslager.behandling.InnsynDokument;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
public class VurderInnsynDokumentDto implements InnsynDokument {

    @JsonProperty("fikkInnsyn")
    private boolean fikkInnsyn;

    @JsonProperty("journalpostId")
    @Digits(integer = 18, fraction = 0)
    private String journalpostId;

    @JsonProperty("dokumentId")
    @Digits(integer = 18, fraction = 0)
    private String dokumentId;

    public VurderInnsynDokumentDto() {
    }

    public VurderInnsynDokumentDto(boolean fikkInnsyn, String journalpostId, String dokumentId) {
        this.fikkInnsyn = fikkInnsyn;
        this.journalpostId = journalpostId;
        this.dokumentId = dokumentId;
    }

    @Override
    public boolean isFikkInnsyn() {
        return fikkInnsyn;
    }

    @Override
    public JournalpostId getJournalpostId() {
        return new JournalpostId(journalpostId);
    }

    @Override
    public String getDokumentId() {
        return dokumentId;
    }
}
