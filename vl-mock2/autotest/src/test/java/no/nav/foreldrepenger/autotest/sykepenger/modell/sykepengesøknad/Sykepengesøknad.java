package no.nav.foreldrepenger.autotest.sykepenger.modell.sykepengesøknad;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sykepengesøknad {

    @JsonProperty
    private String søknadId;

    @JsonProperty
    private String brukerAktørId;

    @JsonProperty
    private String arbeidsgiverId;

    @JsonProperty
    private String sykemeldingId;

    @JsonProperty
    private List<KorrigertArbeidstidPeriode> korrigertArbeidstid;

    @JsonProperty
    private List<FraværsPeriode> fravær;

    @JsonProperty
    private Integer utdanningsgrad;

    @JsonProperty
    private Boolean søktOmUtenlandsopphold;

    @JsonProperty
    private List<EgenmeldingPeriode> egenmeldinger;

    @JsonProperty
    private List<PapirsykemeldingPeriode> papirsykemeldinger;

    @JsonProperty
    private List<AnnenInntektskilde> andreInntektskilder;

    public String getSøknadId() {
        return søknadId;
    }

    public void setSøknadId(String søknadId) {
        this.søknadId = søknadId;
    }

    public String getBrukerAktørId() {
        return brukerAktørId;
    }

    public void setBrukerAktørId(String brukerAktørId) {
        this.brukerAktørId = brukerAktørId;
    }

    public String getArbeidsgiverId() {
        return arbeidsgiverId;
    }

    public void setArbeidsgiverId(String arbeidsgiverId) {
        this.arbeidsgiverId = arbeidsgiverId;
    }

    public String getSykemeldingId() {
        return sykemeldingId;
    }

    public void setSykemeldingId(String sykemeldingId) {
        this.sykemeldingId = sykemeldingId;
    }

    public List<KorrigertArbeidstidPeriode> getKorrigertArbeidstid() {
        return korrigertArbeidstid;
    }

    public void setKorrigertArbeidstid(List<KorrigertArbeidstidPeriode> korrigertArbeidstid) {
        this.korrigertArbeidstid = korrigertArbeidstid;
    }

    public List<FraværsPeriode> getFravær() {
        return fravær;
    }

    public void setFravær(List<FraværsPeriode> fravær) {
        this.fravær = fravær;
    }

    public Integer getUtdanningsgrad() {
        return utdanningsgrad;
    }

    public void setUtdanningsgrad(Integer utdanningsgrad) {
        this.utdanningsgrad = utdanningsgrad;
    }

    public Boolean getSøktOmUtenlandsopphold() {
        return søktOmUtenlandsopphold;
    }

    public void setSøktOmUtenlandsopphold(Boolean søktOmUtenlandsopphold) {
        this.søktOmUtenlandsopphold = søktOmUtenlandsopphold;
    }

    public List<EgenmeldingPeriode> getEgenmeldinger() {
        return egenmeldinger;
    }

    public void setEgenmeldinger(List<EgenmeldingPeriode> egenmeldinger) {
        this.egenmeldinger = egenmeldinger;
    }

    public List<PapirsykemeldingPeriode> getPapirsykemeldinger() {
        return papirsykemeldinger;
    }

    public void setPapirsykemeldinger(List<PapirsykemeldingPeriode> papirsykemeldinger) {
        this.papirsykemeldinger = papirsykemeldinger;
    }

    public List<AnnenInntektskilde> getAndreInntektskilder() {
        return andreInntektskilder;
    }

    public void setAndreInntektskilder(List<AnnenInntektskilde> andreInntektskilder) {
        this.andreInntektskilder = andreInntektskilder;
    }
}
