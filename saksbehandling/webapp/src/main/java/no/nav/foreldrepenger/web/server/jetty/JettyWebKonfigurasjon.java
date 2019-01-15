package no.nav.foreldrepenger.web.server.jetty;

public class JettyWebKonfigurasjon implements AppKonfigurasjon {
    private static final String CONTEXT_PATH = "/fpsak";
    private static final String SWAGGER_HASH = "sha256-2OFkVkSnWOWr0W45P5X5WhpI4DLkq4U03TPyK91dmfk=";

    private Integer serverPort;

    public JettyWebKonfigurasjon() {}

    public JettyWebKonfigurasjon(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public int getServerPort() {
        if (serverPort == null) {
            return AppKonfigurasjon.DEFAULT_SERVER_PORT;
        }
        return serverPort;
    }

    @Override
    public String getContextPath() {
        return CONTEXT_PATH;
    }

    @Override
    public int getSslPort() {
        throw new IllegalStateException("SSL port should only be used locally");
    }

    @Override
    public String getSwaggerHash() {
        return SWAGGER_HASH;
    }


}
