package no.nav.foreldrepenger.web.server.jetty;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;

class DatabaseScript {

    private final MigrationDataSource migrationDataSource;
    private final String locations;

    DatabaseScript(MigrationDataSource migrationDataSource, String locations) {
        this.migrationDataSource = migrationDataSource;
        this.locations = locations;
    }

    void migrate() {
        ClassicConfiguration conf = new ClassicConfiguration();
        conf.setDataSource(migrationDataSource.dataSource);
        conf.setLocationsAsStrings(locations);
        conf.setBaselineOnMigrate(true);
        if (migrationDataSource.initSql != null) {
            conf.setInitSql(migrationDataSource.initSql);
        }
        Flyway flyway = new Flyway(conf);
        flyway.migrate();
    }

    static class MigrationDataSource {
        private final DataSource dataSource;
        private final String initSql;
        MigrationDataSource(DataSource dataSource, String initSql) {
            this.dataSource = dataSource;
            this.initSql = initSql;
        }
    }
}
