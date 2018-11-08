package no.nav.foreldrepenger.behandlingslager.fagsak;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;

public interface FagsakRelasjonRepository {
    FagsakRelasjon finnRelasjonFor(Fagsak fagsak);

    Optional<FagsakRelasjon> finnRelasjonForHvisEksisterer(Fagsak fagsak);

    void lagre(Behandling behandling, Stønadskontoberegning stønadskontoberegning);

    void opprettRelasjon(Fagsak fagsak, Dekningsgrad dekningsgrad);

    void kobleFagsaker(Fagsak fagsakEn, Fagsak fagsakTo);
}
