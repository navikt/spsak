package no.nav.foreldrepenger.web.app.konfig;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import io.swagger.jaxrs.listing.ApiListingResource;
import no.nav.foreldrepenger.web.app.rest.ApplicationConfig;

public class RestApiTester {

    static final List<Class<?>> UNNTATT = Collections.singletonList(ApiListingResource.class);

    static Collection<Method> finnAlleRestMetoder() {
        List<Method> liste = new ArrayList<>();
        for (Class<?> klasse : finnAlleRestTjenester()) {
            for (Method method : klasse.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    liste.add(method);
                }
            }
        }
        return liste;
    }

    static Collection<Class<?>> finnAlleRestTjenester() {
        return new ArrayList<>(finnAlleRestTjenester(new ApplicationConfig()));
    }

    static Collection<Class<?>> finnAlleRestTjenester(Application config) {
        return config.getClasses().stream()
            .filter(c -> c.getAnnotation(Path.class) != null)
            .filter(c -> !UNNTATT.contains(c))
            .collect(Collectors.toList());
    }
}
