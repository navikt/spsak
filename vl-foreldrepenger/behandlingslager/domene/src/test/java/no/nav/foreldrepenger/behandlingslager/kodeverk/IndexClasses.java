package no.nav.foreldrepenger.behandlingslager.kodeverk;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.slf4j.Logger;

/** Henter persistert index (hvis generert) eller genererer index for angitt location (typisk matcher en jar/war fil). */
public class IndexClasses {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(IndexClasses.class);

    private static final ConcurrentMap<URI, IndexClasses> INDEXES = new ConcurrentHashMap<>();

    private URI scanLocation;

    private IndexClasses(URI scanLocation) {
        this.scanLocation = scanLocation;
    }

    public IndexView getIndex() {
        if ("file".equals(scanLocation.getScheme())) {
            // må regenerere index fra fil system i IDE ved å scanne dir, ellers kan den mulig være utdatert (når kjører Jetty i IDE f.eks)
            return scanIndexFromFilesystem(scanLocation);
        }
        return null;
    }

    private Index scanIndexFromFilesystem(URI location) {
        try {
            Indexer indexer = new Indexer();
            Path source = Paths.get(location);
            try (Stream<Path> paths = Files.walk(source)) {
                paths.filter(Files::isRegularFile).forEach(f -> {
                    Path fileName = f.getFileName();
                    if (fileName != null && fileName.toString().endsWith(".class")) {
                        try (InputStream newInputStream = Files.newInputStream(f, StandardOpenOption.READ)) {
                            indexer.index(newInputStream);
                        } catch (IOException e) {
                            throw new IllegalStateException("Fikk ikke indeksert klasse " + f + ", kan ikke scanne klasser", e);
                        } catch (IllegalStateException e) {
                            System.out.println(fileName);
                            throw e;
                        }
                    }
                });
            }
            Index index = indexer.complete();
            return index;
        } catch (IOException e) {
            throw new IllegalStateException("Fikk ikke lest path " + location + ", kan ikke scanne klasser", e);
        }
    }

    public static IndexClasses getIndexFor(final URI location) {
        return INDEXES.computeIfAbsent(location, uri -> new IndexClasses(uri));
    }

    public List<Class<?>> getSubClassesOf(Class<?> cls) {
        DotName search = DotName.createSimple(cls.getName());
        Collection<ClassInfo> subclasses = getIndex().getAllKnownSubclasses(search);

        List<Class<?>> types = new ArrayList<>();
        for (ClassInfo subclass : subclasses) {
            try {
                types.add(Class.forName(subclass.name().toString()));
            } catch (ClassNotFoundException e) {
                log.error("Kan ikke finne klasse i Classpath, som funnet i Jandex index", e);// NOSONAR
            }
        }

        return types;
    }
}
