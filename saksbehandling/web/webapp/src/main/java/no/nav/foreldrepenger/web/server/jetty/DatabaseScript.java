package no.nav.foreldrepenger.web.server.jetty;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DatabaseScript {
    private static final Logger log = LoggerFactory.getLogger(DatabaseScript.class);

    private final DataSource dataSource;
    private final String locations;

    DatabaseScript(DataSource dataSource, String locations) {
        this.dataSource = dataSource;
        this.locations = locations;
    }

    void migrate() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations(locations);
        flyway.setBaselineOnMigrate(true);

        flyway.migrate();
    }
}
