package no.nav.foreldrepenger.domene.dokument.impl;

import java.time.Period;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.domene.dokument.DokumentBestillerTjeneste;
import no.nav.foreldrepenger.domene.dokument.KlageVurderingAksjonspunktDto;
import no.nav.foreldrepenger.domene.dokument.VarselRevurderingAksjonspunktDto;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class DokumentBestillerTjenesteImpl implements DokumentBestillerTjeneste {

    private Period defaultVenteFrist;
    private BehandlingRepositoryProvider repositoryProvider;
    private OppgaveTjeneste oppgaveTjeneste;
    private OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;

    DokumentBestillerTjenesteImpl() {
        // CDI
    }

    @Inject
    public DokumentBestillerTjenesteImpl(@KonfigVerdi(value = "behandling.default.ventefrist.periode") Period defaultVenteFrist, BehandlingRepositoryProvider repositoryProvider,
                                         OppgaveTjeneste oppgaveTjeneste,
                                         OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository,
                                         BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                         DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste) {
        this.defaultVenteFrist = defaultVenteFrist;
        this.repositoryProvider = repositoryProvider;
        this.oppgaveTjeneste = oppgaveTjeneste;
        this.oppgaveBehandlingKoblingRepository = oppgaveBehandlingKoblingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.dokumentBestillerApplikasjonTjeneste = dokumentBestillerApplikasjonTjeneste;
    }

    @Override
    public void aksjonspunktVarselRevurdering(Behandling behandling, VarselRevurderingAksjonspunktDto adapter) {
        new VarselRevurderingAksjonspunkt(defaultVenteFrist, oppgaveBehandlingKoblingRepository, oppgaveTjeneste, behandlingskontrollTjeneste, dokumentBestillerApplikasjonTjeneste)
            .oppdater(behandling, adapter);
    }

    @Override
    public void aksjonspunktKlageVurdering(Behandling behandling, KlageVurderingAksjonspunktDto adapter) {
        new KlageVurderingAksjonspunkt(dokumentBestillerApplikasjonTjeneste, repositoryProvider).oppdater(behandling, adapter);
    }
}
