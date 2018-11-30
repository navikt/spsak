package no.nav.foreldrepenger.behandlingslager.diff;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.IndexClasses;

@RunWith(Parameterized.class)
public class SjekkCollectionsOrderedIEntiteterTest {

    private String name;
    private Class<?> entityClass;

    public SjekkCollectionsOrderedIEntiteterTest(String name, Class<?> entityClass) {
        this.name = name;
        this.entityClass = entityClass;
    }

    @org.junit.runners.Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() throws Exception {
        IndexClasses indexClasses = IndexClasses.getIndexFor(Fagsak.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        Map<String, Object[]> params = new LinkedHashMap<>();

        Set<Class<?>> baseEntitetSubklasser = new LinkedHashSet<>(indexClasses.getSubClassesOf(BaseEntitet.class));
        for (Class<?> c : baseEntitetSubklasser) {
            params.put(c.getName(), new Object[] { c.getSimpleName(), c });
        }
        assertThat(params).isNotEmpty();

        Set<Class<?>> entityKlasser = new LinkedHashSet<>(indexClasses.getClassesWithAnnotation(Entity.class));
        for (Class<?> c : entityKlasser) {
            params.put(c.getName(), new Object[] { c.getSimpleName(), c });
        }
        assertThat(params).isNotEmpty();

        return params.values();
    }

    @Test
    public void sjekk_alle_lister_er_ordered() throws Exception {
        for (Field f : entityClass.getDeclaredFields()) {
            if (Collection.class.isAssignableFrom(f.getType())) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    ParameterizedType paramType = (ParameterizedType) f.getGenericType();
                    Class<?> cls = (Class<?>) paramType.getActualTypeArguments()[0];
                    Assume.assumeTrue(IndexKey.class.isAssignableFrom(cls));
                    assertThat(IndexKey.class).as(f + " definerer en liste i " + name).isAssignableFrom(cls);
                }
            }
        }
    }

}
