package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import java.time.LocalDate;

import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.util.Tuple;

public interface BehandlingOgFagsakTestScenario<S> {

    Fagsak lagreFagsak(GrunnlagRepositoryProvider repositoryProvider);

    Behandling lagre(GrunnlagRepositoryProvider repositoryProvider, ResultatRepositoryProvider resultatRepositoryProvider);

    Behandling lagMocked();

    Behandling getBehandling();

    S medSaksnummer(Saksnummer saksnummer);

    S medFagsakId(Long id);

    FagsakRepository mockFagsakRepository();

    S medBehandlendeEnhet(String behandlendeEnhet);

    S medBehandlingstidFrist(LocalDate behandlingstidFrist);

    void avsluttBehandling();

    void avsluttBehandling(GrunnlagRepositoryProvider repositoryProvider, Behandling behandling);

    ArgumentCaptor<Behandling> getBehandlingCaptor();

    ArgumentCaptor<Fagsak> getFagsakCaptor();

    S medBehandlingType(BehandlingType behandlingType);

    Fagsak getFagsak();

    S medBruker(AktørId aktørId, NavBrukerKjønn kjønn);

    S medOriginalBehandling(Behandling originalBehandling, BehandlingÅrsakType behandlingÅrsakType);

    BehandlingRepository mockBehandlingRepository();

    Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> mockBehandlingRepositoryProvider();

}
