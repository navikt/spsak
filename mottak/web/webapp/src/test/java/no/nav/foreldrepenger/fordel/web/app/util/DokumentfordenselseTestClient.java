package no.nav.foreldrepenger.fordel.web.app.util;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import no.nav.vedtak.log.mdc.MDCOperations;

public class DokumentfordenselseTestClient {
    // Denne må oppdateres når du skal kjøre testen
    private String ID_token = "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJFUGQ3M2h0RmdIX1IwU3Q1aGVoMWFRIiwgInN1YiI6ICJaOTkwMjk0IiwgImF1ZGl0VHJhY2tpbmdJZCI6ICIyNDdlMWZmNi0xN2U1LTQ0ZGYtYTUzNC01MjlhNmJmN2U5OWYtMTY2MjQyOCIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJmcGZvcmRlbC1sb2NhbGhvc3QiLCAib3JnLmZvcmdlcm9jay5vcGVuaWRjb25uZWN0Lm9wcyI6ICI3NTUzYmJhZi1jNmFlLTQwZWItOGNhNi00NjNmN2ZhN2U4YzEiLCAiYXpwIjogImZwZm9yZGVsLWxvY2FsaG9zdCIsICJhdXRoX3RpbWUiOiAxNTIwNTA3MjU3LCAicmVhbG0iOiAiLyIsICJleHAiOiAxNTIwNTEwODU3LCAidG9rZW5UeXBlIjogIkpXVFRva2VuIiwgImlhdCI6IDE1MjA1MDcyNTcgfQ.aZGAPhkfupZQvcjhlImmSXSGOWyv8AmIdFp62zfdR41Y8V6aNSd7YQCoxXUlk9ibh4HB_2jj9K7Du9l9-QsbLUfnUHGPQShA2hdMFhq2foCTs7oBwo67p9J-3HiA9qNq3ksKcD16JbssNWcruxihOoHIi6hAw1Mah3tWgyUbiHMJPaHFCjfhcnSfGdReCzNcMluJ6jug4evwef9QQBk88gyk4Veg8-tW4ThCHS_HcT16vSI3Mt_OsNzrGG5wzQHWmvmg30T_nXm3qrfzf3ztdFmIjudx9bcyoDfBxrhcMfYE35hCaYm9TZ4Bz3zvI6GwkM3tyukwzrumipGELmIfuA";

    public static void main(String[] args) throws IOException {
        new DokumentfordenselseTestClient().createMultipartPost();
    }

    public void createMultipartPost() throws IOException {
        String url = "http://localhost:8090/fpfordel/api/dokumentforsendelse";
        String testdataDir = new File(this.getClass().getClassLoader().getResource("testdata/metadata.json").getPath()).getParent();

        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .setMimeSubtype("mixed")
                .setMode(HttpMultipartMode.RFC6532);

        builder.addPart(buildPart("metadata", new File(testdataDir, "metadata.json"), ContentType.APPLICATION_JSON, null))
                .addPart(buildPart("hoveddokument", new File(testdataDir, "ES-F.xml"), ContentType.create("application/xml"), "<some ID 1>"))
                .addPart(buildPart("hoveddokument", new File(testdataDir, "ES-f.pdf"), ContentType.create("application/pdf"), "<some ID 2>"))
                .addPart(buildPart("vedlegg", new File(testdataDir, "terminbekreftelse.pdf"), ContentType.create("application/pdf"), "<some ID 3>"));

        HttpPost post = new HttpPost(url); // Setting up a HTTP Post method with the target url
        post.setEntity(builder.build()); // Setting the multipart Entity to the post method
        post.setHeader("Authorization", "Bearer " + ID_token);
        post.setHeader("Accept", APPLICATION_JSON);
        post.setHeader(MDCOperations.HTTP_HEADER_CONSUMER_ID, "DokumentfordenselseTestClient");
        post.setHeader(MDCOperations.HTTP_HEADER_CALL_ID, "DTC_" + MDCOperations.generateCallId());

        try (CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build();
             CloseableHttpResponse resp = client.execute(post);
             Scanner scanner = new Scanner(resp.getEntity().getContent(), "utf-8").useDelimiter("\\A")) {

            System.out.println(resp.getStatusLine());

            List<Header> headers = Arrays.asList(resp.getAllHeaders());
            for (Header header : headers) {
                System.out.println(header);
            }

            System.out.println(scanner.hasNext() ? scanner.next() : "");
        }
    }

    private FormBodyPart buildPart(String name, File file, ContentType contentType, String contentId) {
        FormBodyPartBuilder builder = FormBodyPartBuilder.create()
                .setName(name)
                .setBody(new FileBody(file, contentType));
        if (contentId != null) {
            builder.setField("Content-ID", contentId);
        }
        return builder.build();
    }

}
