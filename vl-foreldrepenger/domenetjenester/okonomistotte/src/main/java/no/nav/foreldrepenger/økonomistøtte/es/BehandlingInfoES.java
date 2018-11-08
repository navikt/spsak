package no.nav.foreldrepenger.økonomistøtte.es;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

class BehandlingInfoES {
    private final Fagsak fagsak;
    private final Behandling behandling;
    private final BehandlingVedtak behVedtak;
    private final PersonIdent personIdent;

    public BehandlingInfoES(Fagsak fagsak, Behandling behandling, BehandlingVedtak behVedtak, PersonIdent personIdent) {
        this.fagsak = fagsak;
        this.behandling = behandling;
        this.behVedtak = behVedtak;
        this.personIdent = personIdent;
    }

    public Fagsak getFagsak() {
        return fagsak;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public BehandlingVedtak getBehVedtak() {
        return behVedtak;
    }

    public PersonIdent getPersonIdent() {
        return personIdent;
    }
}
