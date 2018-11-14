package no.nav.vedtak.felles.prosesstask.doc;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class OpprettEntityManager {
    private static final String INMEMORY_DB_JDBC_URL = "jdbc:h2:./test;MODE=Oracle";
    private static final String INMEMORY_DB_USER = "sa";

    private static final String LOC = System.getProperty("doc.plugin.jdbc.db.migration.defaultDS", "classpath:db/migration");

    private static final String JNDI_NAME = System.getProperty("doc.plugin.jdbc.jndi", "jdbc/defaultDS");

    private static final String PERSISTENCE_UNIT_NAME = System.getProperty("doc.plugin.jdbc.persistenceUnit", "pu-default");

    private DataSource dataSource;

    protected void registerDataSource(DataSource ds) {
        try {
            new EnvEntry(JNDI_NAME, ds);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public EntityManager createEntityManager() {
        return createEntityManager(PERSISTENCE_UNIT_NAME);
    }

    public EntityManager createEntityManager(String persistenceUnitName) {
        getDataSource();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName);
        return emf.createEntityManager();
    }

    public synchronized DataSource getDataSource() {
        if (dataSource != null) {
            return dataSource;
        } else {
            dataSource = initDataSource();
            return dataSource;
        }

    }

    protected DataSource initDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getJdbcUrl());
        config.setUsername(getJdbcUserName());
        config.setPassword(getJdbcUsernamePassword(config.getUsername()));
        config.setAutoCommit(false);
        config.addDataSourceProperty("remarksReporting", true);

        DataSource dataSource = new HikariDataSource(config);
        initMigrations(dataSource);

        registerDataSource(dataSource);

        return dataSource;
    }

    protected void initMigrations(DataSource dataSource) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations(LOC);
        try {
            flyway.migrate();
        } catch (FlywayException e) {
            flyway.clean();
            flyway.migrate();
        }
    }

    private String getJdbcUsernamePassword(String username) {
        return System.getProperty("doc.plugin.jdbc.password.defaultDS", username);
    }

    private String getJdbcUserName() {
        return System.getProperty("doc.plugin.jdbc.username.defaultDS", INMEMORY_DB_USER);
    }

    private String getJdbcUrl() {
        return System.getProperty("doc.plugin.jdbc.url", INMEMORY_DB_JDBC_URL);
    }
}
