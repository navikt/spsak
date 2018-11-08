package no.nav.foreldrepenger.økonomistøtte.fp;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.util.FPDateUtil;

class BehandlingInfoFP {
    private final Fagsak fagsak;
    private final Behandling behandling;
    private final BehandlingVedtak behandlingVedtak;
    private final PersonIdent personIdent;
    private final BeregningsresultatFP beregningsresultatFP;
    private final String ansvarligSaksbehandler;

    public BehandlingInfoFP(Fagsak fagsak, Behandling behandling, BehandlingVedtak behandlingVedtak,
                            PersonIdent personIdent, BeregningsresultatFP beregningsresultatFP,
                            String ansvarligSaksbehandler) {
        this.fagsak = fagsak;
        this.behandling = behandling;
        this.behandlingVedtak = behandlingVedtak;
        this.personIdent = personIdent;
        this.beregningsresultatFP = beregningsresultatFP;
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
    }

    public Fagsak getFagsak() {
        return fagsak;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public Optional<BehandlingVedtak> getBehandlingVedtak() {
        return Optional.ofNullable(behandlingVedtak);
    }

    public LocalDate getVedtaksdato() {
        return getBehandlingVedtak()
            .map(BehandlingVedtak::getVedtaksdato)
            .orElse(LocalDate.now(FPDateUtil.getOffset()));
    }

    public PersonIdent getPersonIdent() {
        return personIdent;
    }

    public Optional<BeregningsresultatFP> getBeregningsresultatFP() {
        return Optional.ofNullable(beregningsresultatFP);
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }
}
