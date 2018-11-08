package no.nav.foreldrepenger.domene.registrerer.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.DokumentPersistererTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.registrerer.DokumentRegistrererTjeneste;
import no.nav.foreldrepenger.domene.registrerer.ManuellRegistreringAksjonspunktDto;

@ApplicationScoped
public class DokumentRegistrererTjenesteImpl implements DokumentRegistrererTjeneste {

    private BehandlingRepositoryProvider repositoryProvider;
    private MottatteDokumentRepository mottatteDokumentRepository;
    private OppgaveTjeneste oppgaveTjeneste;
    private OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository;
    private DokumentPersistererTjeneste dokumentPersistererTjeneste;

    DokumentRegistrererTjenesteImpl() {
        // CDI
    }

    @Inject
    public DokumentRegistrererTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                           MottatteDokumentRepository mottatteDokumentRepository,
                                           OppgaveTjeneste oppgaveTjeneste,
                                           OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository,
                                           DokumentPersistererTjeneste dokumentPersistererTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.mottatteDokumentRepository = mottatteDokumentRepository;
        this.oppgaveTjeneste = oppgaveTjeneste;
        this.oppgaveBehandlingKoblingRepository = oppgaveBehandlingKoblingRepository;
        this.dokumentPersistererTjeneste = dokumentPersistererTjeneste;
    }

    @Override
    public void aksjonspunktManuellRegistrering(Behandling behandling, ManuellRegistreringAksjonspunktDto adapter) {
        new ManuellRegistreringAksjonspunkt(repositoryProvider, mottatteDokumentRepository, oppgaveTjeneste, oppgaveBehandlingKoblingRepository, dokumentPersistererTjeneste)
            .oppdater(behandling, adapter);
    }

}
