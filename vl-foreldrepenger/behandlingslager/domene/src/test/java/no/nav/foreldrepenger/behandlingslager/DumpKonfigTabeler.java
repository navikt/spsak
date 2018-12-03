package no.nav.foreldrepenger.behandlingslager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.sql.DataSource;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabell;

/**
 * @deprecated brukes til å migrere gamle konfigtabeller (PK-43460)
 */
@Deprecated
public class DumpKonfigTabeler {
    public static void main(String[] args) {
        new DumpKonfigTabeler().doWork();
    }

    private StringBuilder sb_kodeverk = new StringBuilder();
    private StringBuilder sb_kodeliste = new StringBuilder();
    private StringBuilder sb_migrerFk = new StringBuilder();
    private StringBuilder sb_dropTable = new StringBuilder();

    private void doWork() {
        DataSource dataSource = createDataSource();
        URL configFileURL = getClass().getResource("/META-INF/persistence.xml");
        Map<Object, Object> props = Collections.emptyMap();
        ParsedPersistenceXmlDescriptor persistenceUnit = PersistenceXmlParser.locateIndividualPersistenceUnit(configFileURL, props);

        EntityManagerFactoryBuilder entityManagerFactoryBuilder = Bootstrap.getEntityManagerFactoryBuilder(persistenceUnit, props);
        entityManagerFactoryBuilder.withDataSource(dataSource);

        EntityManagerFactory emf = entityManagerFactoryBuilder.build();
        Metamodel metadata = emf.getMetamodel();
        Collection<EntityType<?>> entityBindings = metadata.getEntities();

        List<EntityType<?>> simpleKodeverk = new ArrayList<>();
        List<EntityType<?>> compKodeverk = new ArrayList<>();
        List<EntityType<?>> migrertKodeverk = new ArrayList<>();

        for (EntityType<?> et : entityBindings) {
            if (isKodeverkTabell(et)) {
                if (isSimple(et.getJavaType())) {
                    simpleKodeverk.add(et);
                } else if (isMigrert(et.getJavaType())) {
                    migrertKodeverk.add(et);
                } else {
                    compKodeverk.add(et);
                }
            }
        }

        simpleKodeverk.forEach(et -> generateSimpleMigration(et));
    }

    private boolean isMigrert(Class<?> javaType) {
        return Kodeliste.class.isAssignableFrom(javaType);
    }

    private void generateSimpleMigration(EntityType<?> et) {
        String entityName = et.getName();
        String tableName = et.getJavaType().getDeclaredAnnotation(Table.class).name();
        String nameUc = tableName; // entityName.toUpperCase().replace('Å', 'A').replace("Ø", "OE").replaceAll("Æ",
                                   // "AE");

        // Oracle id max 30 (11g)
        if (nameUc.endsWith("_TYPE")) {
            nameUc = nameUc.substring(0, nameUc.lastIndexOf('_'));
        } else if (nameUc.length() > 27) {
            if (nameUc.contains("_")) {
                nameUc = nameUc.substring(0, nameUc.lastIndexOf('_'));
            } else {
                nameUc = nameUc.substring(0, 27);
            }
        }

        sb_kodeverk
                .append("insert into kodeverk (kode, navn, beskrivelse ) values ('" + nameUc + "', '" + entityName + "', '');\n");

        sb_kodeliste.append(
                "insert into kodeliste (kode, navn, beskrivelse, gyldig_fom, kodeliste) select kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), '"
                        + nameUc + "' from " + tableName + ";\n");

        sb_migrerFk.append("begin \n");
        sb_migrerFk.append("\tmigrer_kodeverk_fk('" + tableName + "', '" + nameUc + "')" + ";\n");
        sb_migrerFk.append("end;\n/\n\n");

        sb_dropTable.append("drop table " + tableName + " cascade constraints;\n");

    }

    private boolean isSimple(Class<?> javaType) {
        boolean isSimple = true;

        while (!(KodeverkTabell.class.equals(javaType)) && isSimple) {
            Field[] declaredFields = javaType.getDeclaredFields();
            for (Field f : declaredFields) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    isSimple = false;
                    break;
                }
            }
            javaType = javaType.getSuperclass();
        }
        return isSimple;
    }

    private DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:XE");
        config.setUsername("es_empty1");
        config.setPassword("es_empty1");
        config.setConnectionTestQuery("select 1");
        config.setDriverClassName("org.postgresql.Driver");
        Properties dsProperties = new Properties();
        config.setDataSourceProperties(dsProperties);

        HikariDataSource ds = new HikariDataSource(config);

        try {
            new EnvEntry("jdbc/defaultDS", ds);
            return ds;
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isKodeverkTabell(EntityType<?> et) {
        return KodeverkTabell.class.isAssignableFrom(et.getJavaType())
                && !(et.getJavaType().getPackage().getName().contains("no.nav.foreldrepenger.saksopplysninglager")); // Skal
                                                                                                                     // antagelig
                                                                                                                     // bort,
                                                                                                                     // så
                                                                                                                     // skipper
                                                                                                                     // inntil
                                                                                                                     // videre
    }
}
