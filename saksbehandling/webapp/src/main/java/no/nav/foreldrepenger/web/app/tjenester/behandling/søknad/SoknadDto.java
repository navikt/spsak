package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SoknadSykepengerDto.class)
})
public abstract class SoknadDto {

    private SøknadType soknadType;
    private LocalDate mottattDato;
    private String tilleggsopplysninger;
    private String begrunnelseForSenInnsending;
    private OppgittTilknytningDto oppgittTilknytning;
    private List<ManglendeVedleggDto> manglendeVedlegg;

    protected SoknadDto() {
    }

    public SøknadType getSoknadType() {
        return soknadType;
    }

    public LocalDate getMottattDato() {
        return mottattDato;
    }

    public String getTilleggsopplysninger() {
        return tilleggsopplysninger;
    }

    public boolean erSoknadsType(SøknadType søknadType) {
        return søknadType.equals(this.soknadType);
    }

    public String getBegrunnelseForSenInnsending() {
        return begrunnelseForSenInnsending;
    }

    public OppgittTilknytningDto getOppgittTilknytning() {
        return oppgittTilknytning;
    }

    public void setSoknadType(SøknadType soknadType) {
        this.soknadType = soknadType;
    }

    public void setMottattDato(LocalDate mottattDato) {
        this.mottattDato = mottattDato;
    }

    public void setTilleggsopplysninger(String tilleggsopplysninger) {
        this.tilleggsopplysninger = tilleggsopplysninger;
    }

    public void setBegrunnelseForSenInnsending(String begrunnelseForSenInnsending) {
        this.begrunnelseForSenInnsending = begrunnelseForSenInnsending;
    }

    public void setOppgittTilknytning(OppgittTilknytningDto oppgittTilknytning) {
        this.oppgittTilknytning = oppgittTilknytning;
    }

    public List<ManglendeVedleggDto> getManglendeVedlegg() {
        return manglendeVedlegg;
    }

    public void setManglendeVedlegg(List<ManglendeVedleggDto> manglendeVedlegg) {
        this.manglendeVedlegg = manglendeVedlegg;
    }

}
