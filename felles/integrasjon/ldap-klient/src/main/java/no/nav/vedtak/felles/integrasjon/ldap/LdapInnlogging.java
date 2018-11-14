package no.nav.vedtak.felles.integrasjon.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import no.nav.vedtak.konfig.PropertyUtil;

public class LdapInnlogging {

    private LdapInnlogging() {
        throw new IllegalArgumentException("skal ikke instansieres");
    }

    public static LdapContext lagLdapContext() {
        String authMode = getProperty("ldap.auth", "simple");
        String url = getRequiredProperty("ldap.url");

        Hashtable<String, Object> environment = new Hashtable<>(); // NOSONAR //metodeparameter krever Hashtable
        environment.put(Context.INITIAL_CONTEXT_FACTORY, getProperty("ldap.ctxfactory", "com.sun.jndi.ldap.LdapCtxFactory"));
        environment.put(Context.PROVIDER_URL, url);
        environment.put(Context.SECURITY_AUTHENTICATION, authMode);

        if ("simple".equals(authMode)) {
            String user = getRequiredProperty("ldap.username") + "@" + getRequiredProperty("ldap.domain");
            environment.put(Context.SECURITY_CREDENTIALS, getRequiredProperty("ldap.password"));
            environment.put(Context.SECURITY_PRINCIPAL, user);
        } else if ("none".equals(authMode)) {
            // do nothing
        } else {
            // støtter ikke [java.naming.security.authentication]="strong" eller andre. Ignorerer også foreløpig.
        }

        try {
            return new InitialLdapContext(environment, null);
        } catch (NamingException e) {
            throw LdapFeil.FACTORY.klarteIkkeKobleTilLdap(url, e).toException();
        }
    }

    static String getRequiredProperty(String navn) {
        String verdi = PropertyUtil.getProperty(navn);
        if (verdi == null || verdi.isEmpty()) {
            throw LdapFeil.FACTORY.manglerLdapKonfigurasjon(navn).toException();
        }
        return verdi;

    }

    static String getProperty(String navn, String def) {
        String prop = PropertyUtil.getProperty(navn);
        if (prop == null) {
            return def;
        } else {
            return prop;
        }
    }
}
