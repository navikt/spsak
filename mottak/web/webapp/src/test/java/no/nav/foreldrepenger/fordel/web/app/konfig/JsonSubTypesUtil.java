package no.nav.foreldrepenger.fordel.web.app.konfig;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonSubTypesUtil {

    static Set<Class<?>> getJsonSubtypes(Class<?> klasse) {
        JsonSubTypes jst = klasse.getAnnotation(JsonSubTypes.class);
        if (jst == null) {
            return Collections.emptySet();
        }

        return Arrays.asList(jst.value()).stream()
            .map(type -> type.value())
            .collect(Collectors.toSet());
    }

}
