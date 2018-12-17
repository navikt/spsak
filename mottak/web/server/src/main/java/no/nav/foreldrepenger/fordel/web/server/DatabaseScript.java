package no.nav.foreldrepenger.fordel.web.server;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

public class DatabaseScript {
    private final DataSource dataSource;
    private final boolean cleanOnException;
    private final String locations;

    public DatabaseScript(DataSource dataSource, boolean cleanOnException, String locations) {
        this.dataSource = dataSource;
        this.cleanOnException = cleanOnException;
        this.locations = locations;
    }

    public void migrate() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations(locations);
        flyway.setBaselineOnMigrate(true);

        try {
            flyway.migrate();
        } catch (FlywayException e) {  // NOSONAR
            e.printStackTrace(); // NOSONAR
            // prøv en gang til
            if(cleanOnException) {
                flyway.clean();
                flyway.migrate();
            }
        }

    }
}
