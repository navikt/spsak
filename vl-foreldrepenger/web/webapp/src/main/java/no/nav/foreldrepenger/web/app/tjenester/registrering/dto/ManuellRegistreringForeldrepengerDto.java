package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.web.app.tjenester.kodeverk.dto.AndreYtelserDto;

@JsonTypeName(ManuellRegistreringForeldrepengerDto.AKSJONSPUNKT_KODE)
public class ManuellRegistreringForeldrepengerDto extends ManuellRegistreringDto {

    static final String AKSJONSPUNKT_KODE = "5040";

    @Valid
    @Size(max = 50)
    private List<ArbeidsforholdDto> arbeidsforhold;

    @Valid
    private DekningsgradDto dekningsgrad;

    @Valid
    @Size(max = 100)
    private List<AndreYtelserDto> andreYtelser;

    @Valid
    private EgenVirksomhetDto egenVirksomhet;

    @Valid
    private TidsromPermisjonDto tidsromPermisjon;

    @Valid
    private FrilansDto frilans;

    public DekningsgradDto getDekningsgrad() {
        return dekningsgrad;
    }

    public void setDekningsgrad(DekningsgradDto dekningsgrad) {
        this.dekningsgrad = dekningsgrad;
    }

    public List<AndreYtelserDto> getAndreYtelser() {
        return andreYtelser;
    }

    public void setAndreYtelser(List<AndreYtelserDto> andreYtelser) {
        this.andreYtelser = andreYtelser;
    }

    public EgenVirksomhetDto getEgenVirksomhet() {
        return egenVirksomhet;
    }

    public void setEgenVirksomhet(EgenVirksomhetDto egenVirksomhet) {
        this.egenVirksomhet = egenVirksomhet;
    }

    public List<ArbeidsforholdDto> getArbeidsforhold() {
        return arbeidsforhold;
    }

    public void setArbeidsforhold(List<ArbeidsforholdDto> arbeidsforhold) {
        this.arbeidsforhold = arbeidsforhold;
    }

    public TidsromPermisjonDto getTidsromPermisjon() {
        return tidsromPermisjon;
    }

    public void setTidsromPermisjon(TidsromPermisjonDto tidsromPermisjon) {
        this.tidsromPermisjon = tidsromPermisjon;
    }

    public FrilansDto getFrilans() {
        return frilans;
    }

    public void setFrilans(FrilansDto frilans) {
        this.frilans = frilans;
    }


    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
