package no.nav.foreldrepenger.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;

import no.nav.vedtak.felles.integrasjon.ldap.LdapInnlogging;

@ApplicationScoped
public class AdHealthCheck extends ExtHealthCheck {

    @Override
    protected String getDescription() {
        return "Test av LDAP-integrasjon mot AD";
    }

    @Override
    public boolean erKritiskTjeneste() {
        return false;
    }

    @Override
    protected String getEndpoint() {
        return System.getProperty("ldap.url");
    }

    @Override
    protected InternalResult performCheck() {
        InternalResult intTestRes = new InternalResult();

        try {
            LdapInnlogging.lagLdapContext();
            intTestRes.noteResponseTime();
            intTestRes.setOk(true);
        } catch (Exception e) {
            intTestRes.setException(e);
        }
        return intTestRes;
    }
}
