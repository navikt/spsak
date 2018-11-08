package no.nav.vedtak.isso.config;

import no.nav.vedtak.sikkerhet.ContextPathHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServerInfo {

    private static final Logger logger = LoggerFactory.getLogger(ServerInfo.class);
    public static final String PROPERTY_KEY_LOADBALANCER_URL = "loadbalancer.url";
    public static final String CALLBACK_ENDPOINT = "/cb";

    private String schemeHostPort = schemeHostPortFromSystemProperties();
    private boolean isUsingTLS = schemeHostPort.toLowerCase().startsWith("https");
    private String relativeCallbackUrl;
    private String callbackUrl;
    private String cookieDomain = cookieDomain(schemeHostPort);

    private static volatile ServerInfo instance = null;

    ServerInfo() {

    }

    public static ServerInfo instance() {
        if (instance == null) {
            synchronized (ServerInfo.class) {
                if (instance == null) {
                    instance = new ServerInfo();
                }
            }
        }
        return instance;
    }

    static void clearInstance() {
        synchronized (ServerInfo.class) {
            instance = null;
        }
    }

    public String getSchemeHostPort() {
        return schemeHostPort;
    }

    public boolean isUsingTLS() {
        return isUsingTLS;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    public String getCallbackUrl() {
        if (callbackUrl == null) {
            callbackUrl = schemeHostPort + getRelativeCallbackUrl();
        }
        return callbackUrl;
    }

    public String getRelativeCallbackUrl() {
        if(relativeCallbackUrl == null){
            relativeCallbackUrl = ContextPathHolder.instance().getContextPath() + CALLBACK_ENDPOINT;
        }
        return relativeCallbackUrl;
    }

    private static String schemeHostPortFromSystemProperties() {
        String verdi = System.getProperty(PROPERTY_KEY_LOADBALANCER_URL);
        if (verdi == null || verdi.isEmpty()) {
            throw ServerInfoFeil.FACTORY.manglerNødvendigSystemProperty(PROPERTY_KEY_LOADBALANCER_URL).toException();
        }
        return verdi;
    }

    private static String cookieDomain(String schemeHostPort) {
        return removeSchemeAndPort(schemeHostPort);
    }

    private static String removeSchemeAndPort(String schemeHostPort) {
        Pattern pattern = Pattern.compile("^https?://([\\w\\-.]+)(:\\d+)?$");
        Matcher m = pattern.matcher(schemeHostPort);
        if (m.find()) {
            String hostname = m.group(1);
            if (hostname.split("\\.").length >= 3) {
                return hostname.substring(hostname.indexOf('.') + 1);
            } else {
                ServerInfoFeil.FACTORY.uventetHostFormat(hostname).log(logger);
                return null; //null er det strengeste i cookie domain, betyr 'kun denne server'
            }
        } else {
            throw ServerInfoFeil.FACTORY.ugyldigSystemProperty(PROPERTY_KEY_LOADBALANCER_URL, schemeHostPort).toException();
        }
    }

}
