package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultatType;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
@JsonTypeName(VurderInnsynDto.AKSJONSPUNKT_KODE)
public class VurderInnsynDto extends BekreftetAksjonspunktDto implements InnsynResultat<VurderInnsynDokumentDto> {

    static final String AKSJONSPUNKT_KODE = "5037";

    @JsonProperty("innsynResultatType")
    @NotNull
    @ValidKodeverk
    private InnsynResultatType innsynResultatType;

    @JsonProperty("mottattDato")
    @NotNull
    private LocalDate mottattDato;

    @JsonProperty("innsynDokumenter")
    @Valid
    @NotNull
    @Size(max = 1000)
    private List<VurderInnsynDokumentDto> innsynDokumenter;

    @JsonProperty("sattPaVent")
    private boolean sattPaVent;

    @JsonProperty("fristDato")
    private LocalDate fristDato;

    @SuppressWarnings("unused") // NOSONAR
    private VurderInnsynDto() {
        super();
        // For Jackson
    }

    public VurderInnsynDto(String begrunnelse, InnsynResultatType innsynResultatType, LocalDate mottattDato,
                           boolean sattPaVent, List<VurderInnsynDokumentDto> innsynDokumenter, LocalDate fristDato) {
        super(begrunnelse);
        this.innsynResultatType = innsynResultatType;
        this.mottattDato = mottattDato;
        this.sattPaVent = sattPaVent;
        this.innsynDokumenter = innsynDokumenter;
        this.fristDato = fristDato;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    @Override
    public InnsynResultatType getInnsynResultatType() {
        return innsynResultatType;
    }

    @Override
    public LocalDate getMottattDato() {
        return mottattDato;
    }

    @Override
    public List<VurderInnsynDokumentDto> getInnsynDokumenter() {
        return innsynDokumenter;
    }

    public boolean isSattPaVent() {
        return sattPaVent;
    }

    public LocalDate getFristDato() {
        return fristDato;
    }
}
