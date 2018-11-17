package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;

// Dokumentmottaker for ytelsesrelaterte dokumenter har felles protokoll som fanges her
// Variasjoner av protokollen håndteres utenfro
abstract class DokumentmottakerYtelsesesrelatertDokument implements Dokumentmottaker {

    private BehandlingVedtakRepository behandlingVedtakRepository;
    DokumentmottakerFelles dokumentmottakerFelles;
    MottatteDokumentTjeneste mottatteDokumentTjeneste;
    Behandlingsoppretter behandlingsoppretter;
    Kompletthetskontroller kompletthetskontroller;
    BehandlingRevurderingRepository revurderingRepository;
    private BehandlingRepository behandlingRepository;

    DokumentmottakerYtelsesesrelatertDokument() {
        // For CDI proxy
    }

    @Inject
    public DokumentmottakerYtelsesesrelatertDokument(DokumentmottakerFelles dokumentmottakerFelles,
                                                     MottatteDokumentTjeneste mottatteDokumentTjeneste,
                                                     Behandlingsoppretter behandlingsoppretter,
                                                     Kompletthetskontroller kompletthetskontroller,
                                                     BehandlingRepositoryProvider repositoryProvider) {
        this.dokumentmottakerFelles = dokumentmottakerFelles;
        this.mottatteDokumentTjeneste = mottatteDokumentTjeneste;
        this.behandlingsoppretter = behandlingsoppretter;
        this.kompletthetskontroller = kompletthetskontroller;
        this.revurderingRepository = repositoryProvider.getBehandlingRevurderingRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
    }

    /* TEMPLATE-metoder som må håndteres spesifikt for hver type av ytelsesdokumenter - START */
    abstract  void håndterIngenTidligereBehandling(Fagsak fagsak, MottattDokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType);

    abstract void håndterAvsluttetTidligereBehandling(MottattDokument mottattDokument, Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType);

    abstract void oppdaterÅpenBehandlingMedDokument(Behandling behandling, MottattDokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType);

    abstract void håndterKøetBehandling(MottattDokument mottattDokument, Behandling køetBehandling, BehandlingÅrsakType behandlingÅrsakType);

    abstract void håndterAvslåttBehandling(MottattDokument mottattDokument, Fagsak fagsak, Behandling avsluttetBehandling);

    /* TEMPLATE-metoder SLUTT */

    @Override
    public final void mottaDokument(MottattDokument mottattDokument, Fagsak fagsak, DokumentTypeId dokumentTypeId, BehandlingÅrsakType behandlingÅrsakType) {
        Optional<Behandling> sisteYtelsesbehandling = revurderingRepository.hentSisteYtelsesbehandling(fagsak.getId());

        if (!sisteYtelsesbehandling.isPresent()) {
            håndterIngenTidligereBehandling(fagsak, mottattDokument, behandlingÅrsakType);
            return;
        }

        Behandling behandling = sisteYtelsesbehandling.get();
        boolean sisteYtelseErFerdigbehandlet = sisteYtelsesbehandling.map(Behandling::erSaksbehandlingAvsluttet).orElse(Boolean.FALSE);
        if (sisteYtelseErFerdigbehandlet) {
            Optional<Behandling> avslåttBehandling = avslåttBehandlingHvisAvsluttetBehandlingErHenlagt(behandling, fagsak);
            behandling = avslåttBehandling.orElse(behandling);
            // Håndter avsluttet behandling
            if (behandlingsoppretter.erAvslåttFørstegangsbehandling(behandling)) {
                håndterAvslåttBehandling(mottattDokument, fagsak, behandling);
            } else {
                håndterAvsluttetTidligereBehandling(mottattDokument, fagsak, behandlingÅrsakType);
            }
        } else {
            oppdaterÅpenBehandlingMedDokument(behandling, mottattDokument, behandlingÅrsakType);
        }
    }

    @Override
    public void mottaDokumentForKøetBehandling(MottattDokument mottattDokument, Fagsak fagsak, DokumentTypeId dokumentTypeId, BehandlingÅrsakType behandlingÅrsakType) {
        Optional<Behandling> eksisterendeKøetBehandling = revurderingRepository.finnKøetYtelsesbehandling(fagsak.getId());
        Behandling køetBehandling = eksisterendeKøetBehandling
            .orElseGet(() -> behandlingsoppretter.opprettKøetBehandling(fagsak, behandlingÅrsakType));
        dokumentmottakerFelles.opprettHistorikk(køetBehandling, mottattDokument.getJournalpostId());
        dokumentmottakerFelles.opprettKøetHistorikk(køetBehandling, eksisterendeKøetBehandling.isPresent());

        håndterKøetBehandling(mottattDokument, køetBehandling, behandlingÅrsakType);
    }

    private Optional<Behandling> avslåttBehandlingHvisAvsluttetBehandlingErHenlagt(Behandling avsluttetBehandling, Fagsak fagsak) {
        Optional<Behandling> avslåttBehandling = Optional.empty();

        Boolean erHenglagt = hentBehandlingsvedtak(avsluttetBehandling)
            .map(BehandlingVedtak::getBehandlingsresultat)
            .map(Behandlingsresultat::getBehandlingResultatType)
            .map(BehandlingResultatType::erHenlagt)
            .orElse(false);

        if (erHenglagt) {
            avslåttBehandling = behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(fagsak.getId());
        }
        return avslåttBehandling;
    }

    final boolean erAvslagGrunnetOpplysningsplikt(Behandling avsluttetBehandling) {
        List<Vilkår> vilkårne = hentBehandlingsvedtak(avsluttetBehandling)
            .map(BehandlingVedtak::getBehandlingsresultat)
            .map(Behandlingsresultat::getVilkårResultat)
            .map(VilkårResultat::getVilkårene)
            .orElse(Collections.emptyList());

        return vilkårne.stream()
            .anyMatch(vilkår -> vilkår.getVilkårType().equals(VilkårType.SØKERSOPPLYSNINGSPLIKT) &&
                vilkår.getGjeldendeVilkårUtfall().equals(VilkårUtfallType.IKKE_OPPFYLT));
    }

    private Optional<BehandlingVedtak> hentBehandlingsvedtak(Behandling behandling) {
        return behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId());
    }
}
