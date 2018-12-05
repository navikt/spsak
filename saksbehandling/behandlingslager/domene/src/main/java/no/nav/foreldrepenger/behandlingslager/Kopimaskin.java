package no.nav.foreldrepenger.behandlingslager;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;

public class Kopimaskin {
    private static final Set<String> COMMON_FIELD_NAMES = Collections.unmodifiableSet(new HashSet<>(
        Arrays.asList("id", "versjon", "opprettet_av", "opprettet_tid", "endret_av", "endret_tid")
    ));

    private Kopimaskin() {
    }

    public static <T> T deepCopy(T object) {
        return deepCopy(object, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, P> T deepCopy(T object, P parent) {
        if (object instanceof Collection) {
            Collection collection = (Collection) object;
            return deepCopyCollection(collection, parent);
        }
        if (!(object instanceof BaseEntitet) || object instanceof VirksomhetEntitet) {
            return object;
        }
        try {
            Class objectClass = object.getClass();
            T newObject = (T) objectClass.getDeclaredConstructor().newInstance();
            for (Field field : objectClass.getDeclaredFields()) {
                if (COMMON_FIELD_NAMES.contains(field.getName())) {
                    continue;
                }
                AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                    field.setAccessible(true);
                    return null;
                });
                JsonBackReference backReference = field.getAnnotation(JsonBackReference.class);
                if (backReference != null) {
                    field.set(newObject, parent);
                } else {
                    Object fieldValue = field.get(object);
                    Object newFieldValue = deepCopy(fieldValue, newObject);
                    field.set(newObject, newFieldValue);
                }
            }
            return newObject;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to convert object ", ex);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T, P> T deepCopyCollection(Collection collection, P parent) {
        Collection newCollection = createNewCollection(collection);
        collection.forEach(element -> newCollection.add(deepCopy(element, parent)));
        return (T) newCollection;
    }

    private static <V> Collection<V> createNewCollection(Collection<V> collection) {
        if (collection instanceof Set) {
            return new HashSet<>();
        }
        return new ArrayList<>();
    }
}
