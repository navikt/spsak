package no.nav.foreldrepenger.autotest.klienter.sparkel;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.http.HttpResponse;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.autotest.util.http.HttpSession;
import no.nav.foreldrepenger.autotest.util.http.rest.JsonRest;
import no.nav.foreldrepenger.autotest.util.http.rest.StatusRange;

public class SparkelKlient extends JsonRest {

    private final String accessToken;

    public SparkelKlient(String accessToken) {
        super(new HttpSession());
        this.accessToken = accessToken;
    }

    @Override
    public String hentRestRotUrl() {
        //return MiljoKonfigurasjon.getRouteApi();
        return "http://localhost:8080";
    }

    private static String HENT_ARBEIDSFORHOLD_URL_FORMAT = "/api/arbeidsforhold/%s?fom=%tF&tom=%tF";
    //private static String HENT_INNTEKT_LISTE_URL = "/api/inntekt/%s?fom=%tF&tom=%tF";
    private static String HENT_INNTEKT_LISTE_URL = "/api/inntekt/%s?fom=%s&tom=%s";
    private static String HENT_MELDEKORT_URL = "/api/meldekort/?aktorId=%s&fom=%tF&tom=%tF";
    private static String HENT_ORANISASJON_URL = "/api/organisasjon";
    private static String HENT_PERSON_URL = "/api/person/%s";
    private static String HENT_PERSON_HISTORIKK_URL = "/api/person/%s/history";
    private static String HENT_SYKEPENGELISTE_URL = "/api/sykepengevedtak/%s?fom=%tF&tom=%tF";


    public Object hentArbeidsforhold(String aktørId, Date fom, Date tom) throws IOException {
        String url = hentRestRotUrl() + String.format(HENT_ARBEIDSFORHOLD_URL_FORMAT, aktørId, fom, tom);
        return getOgHentJson(url, Object.class, StatusRange.STATUS_200);
    }

    private static class FnrHolder {
        @JsonProperty
        public String fnr;

        public FnrHolder(String fnr) {
            this.fnr = fnr;
        }
    }

    public Object hentInntektsliste(String aktørId, Date fom, Date tom) throws IOException {

        DateFormat df = new SimpleDateFormat("yyyy-MM");

        final String url = hentRestRotUrl() + String.format(HENT_INNTEKT_LISTE_URL, aktørId, df.format(fom), df.format(tom));
        return getOgHentJson(url, Object.class, StatusRange.STATUS_200);
    }

    public Object hentMeldekortGrunnlag(String aktørId, Date fom, Date tom) throws IOException {
        final String url = hentRestRotUrl() + String.format(HENT_MELDEKORT_URL, aktørId, fom, tom);
        return getOgHentJson(url, Object.class, StatusRange.STATUS_200);
    }


    private static class OrgNrHolder {
        @JsonProperty
        public String orgnr;

        public OrgNrHolder(String orgnr) {
            this.orgnr = orgnr;
        }
    }

    public Object hentOrganisasjon(String orgnr) throws IOException {
        final String url = hentRestRotUrl() + HENT_ORANISASJON_URL;
        return postOgHentJson(url, new OrgNrHolder(orgnr), Object.class, StatusRange.STATUS_200);
    }

    public Object hentPerson(String aktørId) throws IOException {
        String url = hentRestRotUrl() + String.format(HENT_PERSON_URL, aktørId);
        return getOgHentJson(url, Object.class, StatusRange.STATUS_200);
    }

    public Object hentPersonHistorikk(String aktørId) throws IOException {
        String url = hentRestRotUrl() + String.format(HENT_PERSON_HISTORIKK_URL, aktørId);
        return getOgHentJson(url, Object.class, StatusRange.STATUS_200);
    }

    public Object hentSykepengeliste(String aktørId, Date fom, Date tom) throws IOException {
        final String url = hentRestRotUrl() + String.format(HENT_SYKEPENGELISTE_URL, aktørId, fom, tom);
        return getOgHentJson(url, Object.class, StatusRange.STATUS_200);
    }


    @Override
    protected HttpResponse getJson(String url, Map<String, String> headers) throws IOException {
        headers.put("Authorization", "Bearer " + accessToken);
        return super.getJson(url, headers);
    }

    @Override
    protected HttpResponse postJson(String url, String json, Map<String, String> headers) throws IOException {
        headers.put("Authorization", "Bearer " + accessToken);
        return super.postJson(url, json, headers);
    }

    @Override
    protected ObjectMapper hentObjectMapper() {
        ObjectMapper mapper = super.hentObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        return mapper;
    }

}
