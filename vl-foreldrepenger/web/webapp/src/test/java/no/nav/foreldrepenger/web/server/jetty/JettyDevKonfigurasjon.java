package no.nav.foreldrepenger.web.server.jetty;

public class JettyDevKonfigurasjon extends JettyWebKonfigurasjon {
    private static final int SSL_SERVER_PORT = 8443;

    @Override
    public int getSslPort() {
        return SSL_SERVER_PORT;
    }

}
