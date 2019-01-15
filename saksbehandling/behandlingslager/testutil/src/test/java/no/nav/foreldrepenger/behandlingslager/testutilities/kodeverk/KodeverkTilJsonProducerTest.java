package no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTypeStegSekvens;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabell;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

/** Genererer kodeverk til bruk i enhetstester ved bruk av AbstractTestScenario. */
public class KodeverkTilJsonProducerTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager em = repoRule.getEntityManager();

    private final ObjectMapper om = new ObjectMapper();

    @Before
    public void setup() {
        om.disableDefaultTyping();
        om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        om.registerModule(new MyModule());

        // bruker kun fields
        om.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
        om.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);
        om.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    public static File getOutputDir() {
        File currentDir = new File(".");

        File path = new File(currentDir, "target/classes");
        while (!path.exists() && currentDir.getParentFile() != null) {
            currentDir = currentDir.getParentFile();
            path = new File(currentDir, "target/classes");
        }

        return path;

    }

    @Test
    public void skal_dumpe_kodeverk_til_json_format_for_bruk_i_scenario_tester() throws Exception {

        Map<Class<?>, Object> dump = new TreeMap<>(Comparator.comparing((Class<?> c) -> c.getName()));
        IndexClasses index = IndexClasses.getIndexFor(Fagsak.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        Set<Class<?>> classes = new LinkedHashSet<>();
        classes.addAll(index.getSubClassesOf(Kodeliste.class));
        classes.addAll(index.getSubClassesOf(KodeverkTabell.class));

        classes.forEach(c -> dump.put(c, getDump(c)));

        Condition<Class<?>> alwaysTrue = new Condition<>(c -> true, "");
        Assertions.assertThat(classes).haveAtLeast(20, alwaysTrue); // kun for å sjekk at vi i det minste finner noe
        writeToFile(dump);

    }

    @Test
    public void skal_dumpe_spesifikke_entiteter_til_fil() throws Exception {

        Map<Class<?>, Object> dump = new TreeMap<>(Comparator.comparing((Class<?> c) -> c.getName()));

        // etterprøv
        dump.put(BehandlingTypeStegSekvens.class, getDump(BehandlingTypeStegSekvens.class));
        writeToFile(dump);

        List<BehandlingTypeStegSekvens> fraFil = new KodeverkFraJson().lesKodeverkFraFil(BehandlingTypeStegSekvens.class);
        Assertions.assertThat(fraFil).isNotEmpty();

    }

    private List<?> getDump(Class<?> cls) {
        Query query = em.createQuery("from " + cls.getName());
        List<?> results = query.getResultList();
        return results;
    }

    private void writeToFile(Map<Class<?>, Object> dump) throws IOException, JsonGenerationException, JsonMappingException {

        ObjectWriter objectWriter = om.writerWithDefaultPrettyPrinter();

        File outputDir = getOutputDir();
        for (Map.Entry<Class<?>, Object> entry : dump.entrySet()) {

            if (!entry.getKey().isAnnotationPresent(DiscriminatorValue.class)) {
                if (entry.getKey().isAnnotationPresent(Entity.class)) {
                    String name = entry.getKey().getAnnotation(Entity.class).name();
                    writeKodeverk(objectWriter, outputDir, entry.getValue(), name);
                } else {
                    System.out.println("Mangler @Entity eller @Discriminator:" + entry.getKey());
                }
            } else {
                String name = entry.getKey().getAnnotation(DiscriminatorValue.class).value();
                writeKodeverk(objectWriter, outputDir, entry.getValue(), name);
            }
        }
    }

    private void writeKodeverk(ObjectWriter objectWriter, File outputDir, Object value, String name)
            throws IOException {
        File outputFile = new File(outputDir, KodeverkFraJson.FILE_NAME_PREFIX + name + KodeverkFraJson.FILE_NAME_SUFFIX);
        outputFile.delete();
        objectWriter.writeValue(outputFile, value);
    }

    public class MyModule extends SimpleModule {
        @SuppressWarnings("deprecation")
        public MyModule() {
            super("ModuleName", new Version(0, 0, 1, null));
        }

        @Override
        public void setupModule(SetupContext context) {
            context.setMixInAnnotations(Object.class, PropertyFilterMixIn.class);
            context.setMixInAnnotations(KodeverkTabell.class, PropertyFilterKodeverkTabellMixIn.class);
        }
    }


    @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property = "kode")
    public static class PropertyFilterKodeverkTabellMixIn {
    }

    @JsonIgnoreProperties({ "endretAv", "opprettetAv", "opprettetTidspunkt", "endretTidspunkt", "id", "gyldigTilOgMed",
            "gyldigFraOgMed", "displayNavn", "beskrivelse", "kodeverkEntitet", "handler" })
    public static class PropertyFilterMixIn {
    }

}
