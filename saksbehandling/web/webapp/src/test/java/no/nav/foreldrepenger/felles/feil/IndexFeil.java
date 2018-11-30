package no.nav.foreldrepenger.felles.feil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;

/** Henter persistert index (hvis generert) eller genererer index for angitt location (typisk matcher en jar/war fil). */
class IndexFeil {
    private String jandexIndexFileName;

    IndexFeil() {
        this("jandex.idx");
    }

    public IndexFeil(String jandexIndexFileName) {
        this.jandexIndexFileName = jandexIndexFileName;
    }

    public IndexView getIndex() {
        return getPersistedJandexIndex();
    }

    // fra pre-generert index, slipper runtime scanning for raskere startup
    private IndexView getPersistedJandexIndex() {
        return getJandexIndex();
    }

    private IndexView getJandexIndex() {
        List<ClassLoader> classLoaders = Arrays.asList(getClass().getClassLoader(), Thread.currentThread().getContextClassLoader());

        List<IndexView> ivs = new ArrayList<>();
        classLoaders
            .stream()
            .flatMap(cl -> {
                try {
                    return Collections.list(cl.getResources("META-INF/" + jandexIndexFileName)).stream();
                } catch (IOException e2) {
                    throw new IllegalArgumentException("Kan ikke lese jandex index fil", e2);
                }
            })
            .forEach(url -> {
                try (InputStream is = url.openStream()) {
                    IndexReader ir = new IndexReader(is);
                    ivs.add(ir.read());
                } catch (IOException e) {
                    throw new IllegalStateException("Kunne ikke lese:" + url.toExternalForm(), e);
                }
            });

        return CompositeIndex.create(ivs);
    }

    public List<AnnotationInstance> getAnnotationInstances(Class<?>... feilAnnotation) {
        Set<AnnotationInstance> annotations = new LinkedHashSet<>(1000);

        Arrays.asList(feilAnnotation).stream().forEach(ft -> {
            DotName search = DotName.createSimple(ft.getName());
            annotations.addAll(getIndex().getAnnotations(search));
        });

        List<AnnotationInstance> types = new ArrayList<>();
        for (AnnotationInstance ann : annotations) {
            if (ann.target().kind() == Kind.METHOD) {
                types.add(ann);
            }
        }

        return types;
    }
}
