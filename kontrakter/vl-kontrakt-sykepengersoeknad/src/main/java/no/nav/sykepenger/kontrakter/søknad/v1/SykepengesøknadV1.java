package no.nav.sykepenger.kontrakter.søknad.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.sykepenger.kontrakter.søknad.Sykepengersøknad;
import no.nav.sykepenger.kontrakter.søknad.v1.fravær.FraværsPeriode;
import no.nav.sykepenger.kontrakter.søknad.v1.opptjening.AnnenInntektskilde;
import no.nav.sykepenger.kontrakter.søknad.v1.perioder.EgenmeldingPeriode;
import no.nav.sykepenger.kontrakter.søknad.v1.perioder.KorrigertArbeidstidPeriode;
import no.nav.sykepenger.kontrakter.søknad.v1.perioder.PapirsykemeldingPeriode;

public class SykepengesøknadV1 extends Sykepengersøknad {

    @JsonProperty("søknadId")
    private String søknadId;

    @JsonProperty("brukerAktørId")
    private String brukerAktørId;

    @JsonProperty("arbeidsgiverId")
    private String arbeidsgiverId;

    @JsonProperty("sykemeldingId")
    private String sykemeldingId;

    @JsonProperty("korrigertArbeidstid")
    private List<KorrigertArbeidstidPeriode> korrigertArbeidstid;

    @JsonProperty("fravær")
    private List<FraværsPeriode> fravær;

    @JsonProperty("utdanningsgrad")
    private Integer utdanningsgrad;

    @JsonProperty("søktOmUtenlandsopphold")
    private Boolean søktOmUtenlandsopphold;

    @JsonProperty("egenmeldinger")
    private List<EgenmeldingPeriode> egenmeldinger;

    @JsonProperty("papirsykemeldinger")
    private List<PapirsykemeldingPeriode> papirsykemeldinger;

    @JsonProperty("andreInntektskilder")
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
