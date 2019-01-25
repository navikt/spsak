package no.nav.foreldrepenger.autotest.klienter.sparkel;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.http.HttpResponse;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
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


    public Object hentArbeidsforhold(String aktørId, Date fom, Date tom) throws IOException {
        String url = hentRestRotUrl() + String.format(HENT_ARBEIDSFORHOLD_URL_FORMAT, aktørId, fom, tom);
        return getOgHentJson(url, Object.class, StatusRange.STATUS_200);
    }

    @Override
    protected HttpResponse getJson(String url, Map<String, String> headers) throws IOException {
        headers.put("Authorization", "Bearer " + accessToken);
        return super.getJson(url, headers);
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
