package no.nav.vedtak.sikkerhet.oidc;

import org.jose4j.base64url.Base64Url;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;

public class KeyStoreTool {

    private static RsaJsonWebKey jwk = null;

    private static final Logger log = LoggerFactory.getLogger(KeyStoreTool.class);

    static {

        PublicKey myPublicKey;
        PrivateKey myPrivateKey;
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(KeyStoreTool.class.getClassLoader().getResourceAsStream("no/nav/modig/testcertificates/keystore.jks"), "devillokeystore1234".toCharArray());
            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection("devillokeystore1234".toCharArray());
            KeyStore.PrivateKeyEntry pk = (KeyStore.PrivateKeyEntry) ks.getEntry("app-key", protParam);
            myPrivateKey = pk.getPrivateKey();
            Certificate cert = ks.getCertificate("app-key");
            myPublicKey = cert.getPublicKey();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableEntryException e) {
            log.error("Error during loading of keystore. Is modig-testcertificates on your classpath?: ", e);
            throw new RuntimeException(e);
        }

        try {
            jwk = (RsaJsonWebKey) PublicJsonWebKey.Factory.newPublicJwk(myPublicKey);
            jwk.setPrivateKey(myPrivateKey);
            jwk.setKeyId("1");
        } catch (JoseException e) {
            log.error("Error during init of JWK: " + e);
            throw new RuntimeException(e);
        }

    }

    public static RsaJsonWebKey getJsonWebKey() {
        return jwk;
    }

    public static String getJwks() {
        String kty = "RSA";
        String kid = "1";
        String use = "sig";
        String alg = "RS256";
        String e = Base64Url.encode(jwk.getRsaPublicKey().getPublicExponent().toByteArray());
        RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();

        byte[] bytes = publicKey.getModulus().toByteArray();
        String n = Base64Url.encode(bytes);

        return String.format("{\"keys\":[{" +
                "\"kty\":\"%s\"," +
                "\"alg\":\"%s\"," +
                "\"use\":\"%s\"," +
                "\"kid\":\"%s\"," +
                "\"n\":\"%s\"," +
                "\"e\":\"%s\"" +
                "}]}", kty, alg, use, kid, n, e);
    }
}
