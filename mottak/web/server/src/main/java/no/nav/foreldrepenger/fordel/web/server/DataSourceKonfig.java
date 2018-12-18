package no.nav.foreldrepenger.fordel.web.server;

import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.jetty.plus.jndi.EnvEntry;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

class DataSourceKonfig {

	DataSource konfigurer() throws NamingException {
		// FIXME: Les db props fra Env vars

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(PropertyUtil.getProperty("defaultDS.url"));
		config.setUsername(PropertyUtil.getProperty("defaultDS.username"));
		config.setPassword(PropertyUtil.getProperty("defaultDS.password")); // NOSONAR false positive

		config.setConnectionTimeout(1000);
		config.setMinimumIdle(1);
		config.setMaximumPoolSize(30);
		config.setConnectionTestQuery("select 1");
		config.setDriverClassName("org.postgresql.Driver");

		Properties dsProperties = new Properties();
		config.setDataSourceProperties(dsProperties);

		HikariDataSource hikariDataSource = new HikariDataSource(config);

		// registrer i JNDI
		new EnvEntry("jdbc/defaultDS", hikariDataSource);  // NOSONAR
		return hikariDataSource;
	}

}
