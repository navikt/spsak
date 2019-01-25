package no.nav.foreldrepenger.autotest.klienter.sparkel;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.autotest.util.http.HttpSession;
import no.nav.foreldrepenger.autotest.util.http.HttpsSession;
import no.nav.foreldrepenger.autotest.util.http.rest.JsonRest;
import no.nav.foreldrepenger.autotest.util.http.rest.StatusRange;

public class FakeAccessTokenKlient extends JsonRest {

    public FakeAccessTokenKlient() {
        super(new HttpsSession());
    }

    @Override
    public String hentRestRotUrl() {
        //return System.getProperty("autotest.vtp.url")+":" + System.getProperty("autotest.vtp.port"); // + "/api";
        return "https://localhost:8063";
    }

    public TokenResponse hentTokenForSubject(String subject) throws IOException {
        HttpResponse response =  post(hentRestRotUrl() + "/isso/oauth2/access_token",
            new StringEntity("code=" + subject, ContentType.APPLICATION_FORM_URLENCODED));
        String json = hentResponseBody(response);
        //ValidateResponse(response, expectedStatusRange, url + "\n\n" + json);
        return hentObjectMapper().readValue(json, TokenResponse.class);
    }

    @Override
    protected ObjectMapper hentObjectMapper() {
        ObjectMapper mapper = super.hentObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        return mapper;
    }

    public static class TokenResponse {
        @JsonProperty("id_token")
        public String idToken;
        @JsonProperty("access_token")
        public String accessToken;
        @JsonProperty("refresh_token")
        public String refreshToken;
        @JsonProperty("expires_in")
        public long expiresIn;
        @JsonProperty("token_type")
        public String tokenType;

        @Override
        public String toString() {
            return "TokenResponse{" +
                "idToken='" + idToken + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                '}';
        }
    }
}
