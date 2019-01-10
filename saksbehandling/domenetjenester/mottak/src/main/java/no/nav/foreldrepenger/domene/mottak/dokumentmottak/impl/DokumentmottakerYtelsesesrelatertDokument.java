package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.util.Optional;

import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingUtil;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;

// Dokumentmottaker for ytelsesrelaterte dokumenter har felles protokoll som fanges her
// Variasjoner av protokollen håndteres utenfro
abstract class DokumentmottakerYtelsesesrelatertDokument implements Dokumentmottaker {

    DokumentmottakerFelles dokumentmottakerFelles;
    MottatteDokumentTjeneste mottatteDokumentTjeneste;
    Behandlingsoppretter behandlingsoppretter;
    Kompletthetskontroller kompletthetskontroller;
    BehandlingRevurderingRepository revurderingRepository;
    BehandlingRepository behandlingRepository;

    DokumentmottakerYtelsesesrelatertDokument() {
        // For CDI proxy
    }

    @Inject
    public DokumentmottakerYtelsesesrelatertDokument(DokumentmottakerFelles dokumentmottakerFelles,
                                                     MottatteDokumentTjeneste mottatteDokumentTjeneste,
                                                     Behandlingsoppretter behandlingsoppretter,
                                                     Kompletthetskontroller kompletthetskontroller,
                                                     GrunnlagRepositoryProvider repositoryProvider) {
        this.dokumentmottakerFelles = dokumentmottakerFelles;
        this.mottatteDokumentTjeneste = mottatteDokumentTjeneste;
        this.behandlingsoppretter = behandlingsoppretter;
        this.kompletthetskontroller = kompletthetskontroller;
        this.revurderingRepository = repositoryProvider.getBehandlingRevurderingRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    /* TEMPLATE-metoder som må håndteres spesifikt for hver type av ytelsesdokumenter - START */
    abstract void håndterIngenTidligereBehandling(Fagsak fagsak, InngåendeSaksdokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType);

    abstract void håndterAvsluttetTidligereBehandling(InngåendeSaksdokument mottattDokument, Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType);

    abstract void oppdaterÅpenBehandlingMedDokument(Behandling behandling, InngåendeSaksdokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType);

    abstract void håndterKøetBehandling(InngåendeSaksdokument mottattDokument, Behandling køetBehandling, BehandlingÅrsakType behandlingÅrsakType);

    /* TEMPLATE-metoder SLUTT */

    @Override
    public final void mottaDokument(InngåendeSaksdokument mottattDokument, Fagsak fagsak, DokumentTypeId dokumentTypeId,
                                    BehandlingÅrsakType behandlingÅrsakType) {
        Optional<Behandling> sisteYtelsesbehandling = revurderingRepository.hentSisteYtelsesbehandling(fagsak.getId());

        if (sisteYtelsesbehandling.isEmpty()) {
            håndterIngenTidligereBehandling(fagsak, mottattDokument, behandlingÅrsakType);
            return;
        }

        Behandling behandling = sisteYtelsesbehandling.get();
        Optional<Behandlingsresultat> behandlingsresultat = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        boolean sisteYtelseErFerdigbehandlet = sisteYtelsesbehandling.map(b -> BehandlingUtil.erSaksbehandlingAvsluttet(b, behandlingsresultat.orElse(null))).orElse(Boolean.FALSE);
        if (sisteYtelseErFerdigbehandlet) {
            håndterAvsluttetTidligereBehandling(mottattDokument, fagsak, behandlingÅrsakType);
        } else {
            oppdaterÅpenBehandlingMedDokument(behandling, mottattDokument, behandlingÅrsakType);
        }
    }

    @Override
    public void mottaDokumentForKøetBehandling(InngåendeSaksdokument mottattDokument, Fagsak fagsak, DokumentTypeId dokumentTypeId,
                                               BehandlingÅrsakType behandlingÅrsakType) {
        Optional<Behandling> eksisterendeKøetBehandling = revurderingRepository.finnKøetYtelsesbehandling(fagsak.getId());
        Behandling køetBehandling = eksisterendeKøetBehandling
            .orElseGet(() -> behandlingsoppretter.opprettKøetBehandling(fagsak, behandlingÅrsakType));
        dokumentmottakerFelles.opprettHistorikk(køetBehandling, mottattDokument.getJournalpostId());
        dokumentmottakerFelles.opprettKøetHistorikk(køetBehandling, eksisterendeKøetBehandling.isPresent());

        håndterKøetBehandling(mottattDokument, køetBehandling, behandlingÅrsakType);
    }

}
