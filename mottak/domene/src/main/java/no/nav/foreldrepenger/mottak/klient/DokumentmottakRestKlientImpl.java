package no.nav.foreldrepenger.mottak.klient;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class DokumentmottakRestKlientImpl implements DokumentmottakRestKlient {
    private static final String ENDPOINT_KEY = "fpsak_mottaJournalpost.url";

    private OidcRestClient oidcRestClient;
    private URI endpoint;

    public DokumentmottakRestKlientImpl() { //NOSONAR: for cdi
    }

    @Inject
    public DokumentmottakRestKlientImpl(OidcRestClient oidcRestClient, @KonfigVerdi(ENDPOINT_KEY) URI endpoint) {
        this.oidcRestClient = oidcRestClient;
        this.endpoint = endpoint;
    }

    @Override
    public void send(JournalpostMottakDto journalpostMottakDto) {
        oidcRestClient.post(endpoint, journalpostMottakDto);
    }

}
