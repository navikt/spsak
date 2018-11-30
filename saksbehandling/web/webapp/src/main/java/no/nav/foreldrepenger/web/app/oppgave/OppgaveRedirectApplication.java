package no.nav.foreldrepenger.web.app.oppgave;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import no.nav.foreldrepenger.web.app.exceptions.RedirectExceptionMapper;
import no.nav.foreldrepenger.web.app.jackson.JacksonJsonConfig;

@ApplicationPath("oppgaveredirect")
public class OppgaveRedirectApplication extends Application {

    private static final Set<Class<?>> CLASSES;

    static {
        Set<Class<?>> klasser = new HashSet<>();
        klasser.add(OppgaveRedirectTjeneste.class);
        klasser.add(RedirectExceptionMapper.class);
        klasser.add(JacksonJsonConfig.class);
        CLASSES = Collections.unmodifiableSet(klasser);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return CLASSES;
    }
}
