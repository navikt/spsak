package no.nav.foreldrepenger.web.local.development;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/jetty")
public class JettyTestApplication extends Application {
    //FIXME (u139158): Denne pakken skal ligge i src/test, men sliter litt med å få det til :(
    // Må sannsynligvis legges inn i igjen i JettyDevServer#updateMetaData når den flyttes
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(JettyLoginResource.class);
        return classes;
    }
}
