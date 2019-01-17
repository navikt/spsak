package no.nav.foreldrepenger.fordel.web.server;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import no.nav.foreldrepenger.fordel.web.server.sikkerhet.JettySikkerhetKonfig;
import no.nav.vedtak.sikkerhetsfilter.SecurityFilter;

public class JettyServer {

    /**
     * nedstrippet sett med Jetty configurations for raskere startup.
     */
    private static final Configuration[] CONFIGURATIONS = new Configuration[]{new WebXmlConfiguration(),
            new AnnotationConfiguration(), new WebInfConfiguration(), new PlusConfiguration(),
            new EnvConfiguration(),};

    private static final String CONTEXT_PATH = "/fpfordel";
    private static final String SERVER_HOST = "0.0.0.0"; // NOSONAR
    private int hostPort;

    public JettyServer(int hostPort) {
        this.hostPort = hostPort;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Mangler port");
        }
        JettyServer jettyServer = new JettyServer(Integer.parseUnsignedInt(args[0]));
        jettyServer.konfigurer();
        jettyServer.start();

    }

    protected void start() throws Exception {
        Server server = new Server();
        Connector[] connectors = new Connector[]{};
        server.setConnectors(createConnectors(server).toArray(connectors));
        server.setHandler(initContext());
        server.start();
        server.join();
    }

    protected List<Connector> createConnectors(Server server) {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(getServerPort());
        connector.setHost(SERVER_HOST);
        List<Connector> connectors = new ArrayList<>();
        connectors.add(connector);
        return connectors;
    }

    protected int getServerPort() {
        return hostPort;
    }

    protected void konfigurer() throws Exception { //NOSONAR
        DataSource dataSource = opprettDataSource();
        migrerDatabaseScript(dataSource);
        hacksForManglendeStøttePåNAIS();
        konfigurerSwaggerHash();
    }

    /**
     * @see SecurityFilter#getSwaggerHash()
     */
    protected void konfigurerSwaggerHash() {
        System.setProperty(SecurityFilter.SWAGGER_HASH_KEY, "sha256-fnjhD3ruVjt/RjLjhyxwaXX9zH+duwiYM14hVrUcHCU=");
    }

    private void hacksForManglendeStøttePåNAIS() {
        computeAgentName();
        loadBalancerFqdnTilLoadBalancerUrl();
    }

    private void loadBalancerFqdnTilLoadBalancerUrl() {
        if (System.getenv("LOADBALANCER_FQDN") != null) {
            String loadbalancerFqdn = System.getenv("LOADBALANCER_FQDN");
            String protocol = (loadbalancerFqdn.startsWith("localhost")) ? "http" : "https";
            System.setProperty("loadbalancer.url", protocol + "://" + loadbalancerFqdn);
        }
    }

    // FIXME (u139158): Midlertidig workaround til automatisering for ISSO er på
    // plass i NAIS
    private void computeAgentName() {
        if ("dummy".equals(System.getenv("OPENIDCONNECT_USERNAME"))) {
            String dbUsername = System.getenv("DEFAULTDS_USERNAME");
            String username = dbUsername.toLowerCase().replace('_', '-');
            System.setProperty("OpenIdConnect.username", username);
        }
    }

    protected void migrerDatabaseScript(DataSource dataSource) {
        String locations = "classpath:/db/migration/defaultDS";
        new DatabaseScript(dataSource, false, locations).migrate();
    }

    protected DataSource opprettDataSource() throws NamingException {
        DataSource dataSource = new DataSourceKonfig().konfigurer();
        return dataSource;
    }

    protected WebAppContext initContext() {
        WebAppContext webAppContext = createContext();
        webAppContext.setContextPath(CONTEXT_PATH);
        webAppContext.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                "^.*resteasy-.*.jar$|^.*felles-.*.jar$");

        // Add configuration options. (remove unused configurations to speed up startup)
        webAppContext.setConfigurations(CONFIGURATIONS);
        new JettySikkerhetKonfig().konfigurer(webAppContext);

        return webAppContext;
    }

    protected WebAppContext createContext() {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setResourceBase(System.getProperty("webapp", "./webapp"));
        webAppContext.setDescriptor(System.getProperty("webapp", "./webapp") + "/WEB-INF/web.xml");
        return webAppContext;
    }
}
