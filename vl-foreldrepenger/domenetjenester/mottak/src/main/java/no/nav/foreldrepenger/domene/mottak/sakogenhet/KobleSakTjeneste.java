package no.nav.foreldrepenger.domene.mottak.sakogenhet;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;

public interface KobleSakTjeneste {

    Optional<Fagsak> finnRelatertFagsakDersomRelevant(Behandling behandling);

    Optional<FagsakRelasjon> finnFagsakRelasjonDersomOpprettet(Behandling behandling);

    void kobleRelatertFagsakHvisDetFinnesEn(Behandling behandling);
}
