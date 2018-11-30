package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public interface BehandlendeEnhetTjeneste {

    // Brukes ved opprettelse av oppgaver før behandling har startet
    OrganisasjonsEnhet finnBehandlendeEnhetFraSøker(Fagsak fagsak);

    // Brukes ved opprettelse av førstegangsbehandling
    OrganisasjonsEnhet finnBehandlendeEnhetFraSøker(Behandling behandling);

    // Sjekk om andre angitte personer (Verge mm) har diskresjonskode som tilsier spesialenhet. Returnerer empty() hvis ingen endring.
    Optional<OrganisasjonsEnhet> endretBehandlendeEnhetFraAndrePersoner(Behandling behandling, List<AktørId> aktører);
    Optional<OrganisasjonsEnhet> endretBehandlendeEnhetFraAndrePersoner(Behandling behandling, PersonIdent relatert);

    OrganisasjonsEnhet sjekkEnhetVedNyAvledetBehandling(Behandling behandling, OrganisasjonsEnhet enhetOpprinneligBehandling);

    // Brukes for å sjekke om det er behov for å endre til spesialenheter når saken tas av vent.
    Optional<OrganisasjonsEnhet> sjekkEnhetVedGjenopptak(Behandling behandling);

    // Sjekk om angitt journalførende enhet er gyldig for enkelte oppgaver
    boolean gyldigEnhetNfpNk(Fagsak fagsak, String enhetId);
}
