package no.nav.vedtak.felles.testutilities.db;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.feil.FeilFactory;

/**
 * Setter opp Flyway og databasemigreringer for enhetstester og testdata.
 */
public class FlywayKonfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlywayKonfig.class);

    private DataSource dataSource;
    private boolean cleanup = false;
    private String sqlLokasjon = null;
    private EntityManager entityManager = null;
    private String tabellnavn = null;

    private FlywayKonfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static FlywayKonfig lagKonfig(DataSource dataSource) {
        return new FlywayKonfig(dataSource);
    }

    public FlywayKonfig medCleanup(boolean utførFullMigrering) {
        this.cleanup = utførFullMigrering;
        return this;
    }

    public FlywayKonfig medSqlLokasjon(String sqlLokasjon) {
        this.sqlLokasjon = sqlLokasjon;
        return this;
    }

    public FlywayKonfig medMetadataTabell(String tabellnavn) {
        this.tabellnavn = tabellnavn;
        return this;
    }


    public boolean migrerDb() {
        Flyway flyway = new Flyway();
        flyway.setBaselineOnMigrate(true);
        flyway.setDataSource(dataSource);

        if (tabellnavn != null) {
            flyway.setTable(tabellnavn);
        }

        if (sqlLokasjon != null) {
            flyway.setLocations(sqlLokasjon);
        } else {
            /**
             * Default leter flyway etter classpath:db/migration.
             * Her vet vi at vi ikke skal lete i classpath
             */
            flyway.setLocations("denne/stien/finnes/ikke");
        }

        if (cleanup) {
            flyway.clean();
        }

        flyway.configure(lesFlywayPlaceholders());

        try {
            flyway.migrate();
            return true;
        } catch (FlywayException flywayException) {
            FeilFactory.create(DbMigreringFeil.class).flywayMigreringFeilet(flywayException).log(LOGGER);
            return false;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    private Properties lesFlywayPlaceholders() {
        Properties placeholders = new Properties();
        for (String prop : System.getProperties().stringPropertyNames()) {
            if (prop.startsWith("flyway.placeholders.")) {
                placeholders.setProperty(prop, System.getProperty(prop));
            }
        }
        return placeholders;
    }

    public void nullstill() {
        Flyway flyway = new Flyway();
        flyway.setBaselineOnMigrate(true);
        flyway.setDataSource(dataSource);

        if (tabellnavn != null) {
            flyway.setTable(tabellnavn);
        }
        flyway.clean();
    }
}
