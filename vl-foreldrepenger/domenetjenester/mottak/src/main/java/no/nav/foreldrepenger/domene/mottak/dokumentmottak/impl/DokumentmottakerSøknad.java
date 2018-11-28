package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;

@ApplicationScoped
@DokumentGruppeRef("SØKNAD")
class DokumentmottakerSøknad extends DokumentmottakerYtelsesesrelatertDokument {

    private final SøknadRepository søknadRepository;

    @Inject
    public DokumentmottakerSøknad(BehandlingRepositoryProvider repositoryProvider,
                                  DokumentmottakerFelles dokumentmottakerFelles,
                                  MottatteDokumentTjeneste mottatteDokumentTjeneste,
                                  Behandlingsoppretter behandlingsoppretter,
                                  Kompletthetskontroller kompletthetskontroller) {
        super(dokumentmottakerFelles,
            mottatteDokumentTjeneste,
            behandlingsoppretter,
            kompletthetskontroller,
            repositoryProvider);
        this.søknadRepository = repositoryProvider.getSøknadRepository();
    }

    @Override
    void håndterIngenTidligereBehandling(Fagsak fagsak, InngåendeSaksdokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType) {
        // Opprett ny førstegangsbehandling
        Behandling behandling = behandlingsoppretter.finnEllerOpprettFørstegangsbehandling(fagsak);
        mottatteDokumentTjeneste.persisterDokumentinnhold(behandling, mottattDokument, Optional.empty());
        dokumentmottakerFelles.opprettTaskForÅStarteBehandling(behandling);
        dokumentmottakerFelles.opprettHistorikk(behandling, mottattDokument.getJournalpostId());
    }

    @Override
    void håndterAvsluttetTidligereBehandling(InngåendeSaksdokument mottattDokument, Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType) {
        // Start ny førstegangsbehandling av søknad
        Behandling behandling = behandlingsoppretter.opprettNyFørstegangsbehandling(behandlingÅrsakType, fagsak);
        mottatteDokumentTjeneste.persisterDokumentinnhold(behandling, mottattDokument, Optional.empty());
        dokumentmottakerFelles.opprettTaskForÅStarteBehandling(behandling);
        dokumentmottakerFelles.opprettHistorikk(behandling, mottattDokument.getJournalpostId());
    }

    @Override
    void oppdaterÅpenBehandlingMedDokument(Behandling behandling, InngåendeSaksdokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType) {
        dokumentmottakerFelles.opprettHistorikk(behandling, mottattDokument.getJournalpostId());

        Fagsak fagsak = behandling.getFagsak();
        boolean erPåVent = behandling.isBehandlingPåVent();

        if (harMottattSøknadTidligere(behandling)) {
            // Oppdatere behandling gjennom henleggelse
            Behandling nyFørstegangsbehandling = behandlingsoppretter.henleggOgOpprettNyFørstegangsbehandling(fagsak, behandling, behandlingÅrsakType);
            Optional<LocalDate> søknadsdatoFraHenlagtBehandling = revurderingRepository.finnSøknadsdatoFraHenlagtBehandling(behandling);
            mottatteDokumentTjeneste.persisterDokumentinnhold(nyFørstegangsbehandling, mottattDokument, søknadsdatoFraHenlagtBehandling);
            dokumentmottakerFelles.opprettTaskForÅStarteBehandling(nyFørstegangsbehandling);
        } else {
            if (erPåVent) {
                kompletthetskontroller.persisterDokumentOgVurderKompletthet(behandling, mottattDokument);
            } else {
                dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, behandling, mottattDokument);
            }
        }
    }

    @Override
    void håndterKøetBehandling(InngåendeSaksdokument mottattDokument, Behandling køetBehandling, BehandlingÅrsakType behandlingÅrsakType) {
        if (harMottattSøknadTidligere(køetBehandling)) {
            // Oppdatere behandling gjennom henleggelse
            Behandling nyKøetBehandling = behandlingsoppretter.henleggOgOpprettNyFørstegangsbehandling(køetBehandling.getFagsak(), køetBehandling, behandlingÅrsakType);
            behandlingsoppretter.settSomKøet(nyKøetBehandling);
            Optional<LocalDate> søknadsdato = revurderingRepository.finnSøknadsdatoFraHenlagtBehandling(nyKøetBehandling);
            kompletthetskontroller.persisterKøetDokumentOgVurderKompletthet(nyKøetBehandling, mottattDokument, søknadsdato);
        } else {
            // Oppdater køet behandling med søknad
            Optional<LocalDate> søknadsdato = Optional.empty();
            kompletthetskontroller.persisterKøetDokumentOgVurderKompletthet(køetBehandling, mottattDokument, søknadsdato);
        }
    }

    @Override
    void håndterAvslåttBehandling(InngåendeSaksdokument mottattDokument, Fagsak fagsak, Behandling avsluttetBehandling) {
        if (erAvslagGrunnetOpplysningsplikt(avsluttetBehandling)) {
            behandlingsoppretter.opprettNyFørstegangsbehandling(mottattDokument, fagsak, avsluttetBehandling);
        } else {
            dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, avsluttetBehandling, mottattDokument);
        }
    }

    private boolean harMottattSøknadTidligere(Behandling behandling) {
        return søknadRepository.hentSøknadHvisEksisterer(behandling.getId()).isPresent();
    }

}
