package no.nav.foreldrepenger.web.server.jetty;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.vedtak.konfig.PropertyUtil;

class DataSourceKonfig {

    private static final String location = "classpath:/db/migration/";
    private DBConnProp defaultDatasource;
    private List<DBConnProp> dataSources;

    DataSourceKonfig() {
        defaultDatasource = new DBConnProp(createDatasource("defaultDS"), location + "defaultDS");
        dataSources = Arrays.asList(
            defaultDatasource,
            new DBConnProp(createDatasource("dvhDS"), location + "dvhDS"));
    }

    private DataSource createDatasource(String dataSourceName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(PropertyUtil.getProperty(dataSourceName + ".url"));
        config.setUsername(PropertyUtil.getProperty(dataSourceName + ".username"));
        config.setPassword(PropertyUtil.getProperty(dataSourceName + ".password")); // NOSONAR false positive

        config.setConnectionTimeout(1000);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(30);
        config.setConnectionTestQuery("select 1");
        config.setDriverClassName("org.postgresql.Driver");

        Properties dsProperties = new Properties();
        config.setDataSourceProperties(dsProperties);

        return new HikariDataSource(config);
    }

    DBConnProp getDefaultDatasource() {
        return defaultDatasource;
    }

    List<DBConnProp> getDataSources() {
        return dataSources;
    }

    class DBConnProp {
        private DataSource datasource;
        private String migrationScripts;

        public DBConnProp(DataSource datasource, String migrationScripts) {
            this.datasource = datasource;
            this.migrationScripts = migrationScripts;
        }

        public DataSource getDatasource() {
            return datasource;
        }

        public String getMigrationScripts() {
            return migrationScripts;
        }
    }

}
