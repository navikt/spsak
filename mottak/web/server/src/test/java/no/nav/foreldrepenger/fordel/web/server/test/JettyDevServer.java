package no.nav.foreldrepenger.fordel.web.server.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import no.nav.foreldrepenger.fordel.web.server.DatabaseScript;
import no.nav.foreldrepenger.fordel.web.server.JettyServer;
import no.nav.modig.testcertificates.TestCertificates;
import no.nav.vedtak.isso.IssoApplication;

/** Setter opp jetty automatisk lokalt med riktig konfig verdier. */
public class JettyDevServer extends JettyServer {

	private static final String WEBAPP_ROOT = "../webapp/";
	private static final String WAR_CLASSES_ROOT_DIR = WEBAPP_ROOT + "target/classes/";
	private static final String WEB_INF_ROOT_DIR = WEBAPP_ROOT + "src/main/webapp/";
	private static final int DEV_SERVER_PORT = 8090;

	public JettyDevServer(){
		super(DEV_SERVER_PORT);
	}

	public static void main(String[] args) throws Exception {
		File webapproot = new File(WEBAPP_ROOT);
		if(!webapproot.exists()){
			throw new IllegalStateException("Har du satt working dir til server prosjekt? Finner ikke "+webapproot);
		}
		setupSikkerhetLokalt();

		JettyDevServer jettyDevServer = new JettyDevServer();
		HttpConfiguration https = new HttpConfiguration();
		https.addCustomizer(new SecureRequestCustomizer());

		jettyDevServer.konfigurer();
		jettyDevServer.start();
	}

	@Override
	protected List<Connector> createConnectors(Server server) {
		List<Connector> connectors = super.createConnectors(server);

		HttpConfiguration https = new HttpConfiguration();
		https.addCustomizer(new SecureRequestCustomizer());
		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath(System.getProperty("no.nav.modig.security.appcert.keystore"));
		sslContextFactory.setKeyStorePassword(System.getProperty("no.nav.modig.security.appcert.password"));
		sslContextFactory.setKeyManagerPassword(System.getProperty("no.nav.modig.security.appcert.password"));
		ServerConnector sslConnector = new ServerConnector(server,
				new SslConnectionFactory(sslContextFactory, "http/1.1"),
				new HttpConnectionFactory(https));
		sslConnector.setPort(getSslPort());
		connectors.add(sslConnector);

		return connectors;
	}

	private int getSslPort() {
		return getServerPort()-1;
	}

	@Override
	protected WebAppContext initContext() {
		WebAppContext webAppContext = super.initContext();

		System.setProperty("org.apache.geronimo.jaspic.configurationFile", "src/main/resources/jetty/jaspi-conf.xml");
		return webAppContext;
	}

	@Override
	protected WebAppContext createContext() {
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setResourceBase(WEB_INF_ROOT_DIR);
		webAppContext.setDescriptor(WEB_INF_ROOT_DIR + "/WEB-INF/web.xml");

		List<Resource> classesDirs = new ArrayList<>(webAppContext.getMetaData().getWebInfClassesDirs());
		File webappClassesLocation = new File(WAR_CLASSES_ROOT_DIR).getAbsoluteFile();
		if (!webappClassesLocation.exists()) {
			throw new IllegalStateException("Kan ikke finne webapp classes=" + webappClassesLocation);
		}
		classesDirs.add(Resource.newResource(webappClassesLocation));

		URL issoClasses = IssoApplication.class.getProtectionDomain().getCodeSource().getLocation();
		classesDirs.add(Resource.newResource(issoClasses));

		webAppContext.getMetaData().setWebInfClassesDirs(classesDirs);
		return webAppContext;
	}

	@Override
	protected int getServerPort(){
		return DEV_SERVER_PORT;
	}

	@Override
	protected void konfigurer() throws Exception {
		PropertiesUtils.lagPropertiesFilFraTemplate();
		PropertiesUtils.initProperties();
		konfigurerLogback();
		super.konfigurer();
	}

	private void konfigurerLogback() throws IOException {
		new File("./logs").mkdirs();
		System.setProperty("APP_LOG_HOME", "./logs");
		File logbackConfig = PropertiesUtils.lagLogbackConfig();

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			// Call context.reset() to clear any previous configuration, e.g. default
			// configuration. For multi-step configuration, omit calling context.reset().
			context.reset();
			configurator.doConfigure(logbackConfig.getAbsolutePath());
		} catch (JoranException je) {
			// StatusPrinter will handle this
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}

	@Override
	protected void migrerDatabaseScript(DataSource dataSource) {
		new DatabaseScript(dataSource,true, findLocations()).migrate();
	}

	private String findLocations() {
		String loc = "migreringer/src/main/resources/db/migration/";
		File base = new File(".").getAbsoluteFile();
		File file = new File(base, loc);
		while (!file.exists() && base.getParentFile() != null) {
			base = base.getParentFile();
			file = new File(base, loc);
		}

		if (!file.exists()) {
			throw new IllegalStateException("Fant ikke:" + loc);
		}
		return "filesystem:" + file.getAbsolutePath();

	}

	private static void setupSikkerhetLokalt() throws IOException {
		System.setProperty("app.confdir", "src/main/resources/jetty");
		System.setProperty("develop-local", "true");
		TestCertificates.setupKeyAndTrustStore();

		// Eksponer truststore for run-java-local.sh
		File tempTrustStore = new File(System.getProperty("javax.net.ssl.trustStore"));
		File truststore = new File("./truststore.jts");
		Files.copy(tempTrustStore.toPath(), truststore.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
}
