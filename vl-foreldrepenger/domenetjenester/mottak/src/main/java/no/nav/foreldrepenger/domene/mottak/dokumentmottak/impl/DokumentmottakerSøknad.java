package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;

@ApplicationScoped
@DokumentGruppeRef("SØKNAD")
class DokumentmottakerSøknad extends DokumentmottakerYtelsesesrelatertDokument {

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
    }

    @Override
    void håndterIngenTidligereBehandling(Fagsak fagsak, MottattDokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType) {
        // Opprett ny førstegangsbehandling
        Behandling behandling = behandlingsoppretter.finnEllerOpprettFørstegangsbehandling(fagsak);
        mottatteDokumentTjeneste.persisterDokumentinnhold(behandling, mottattDokument, Optional.empty());
        dokumentmottakerFelles.opprettTaskForÅStarteBehandling(behandling);
        dokumentmottakerFelles.opprettHistorikk(behandling, mottattDokument.getJournalpostId());
    }

    @Override
    void håndterAvsluttetTidligereBehandling(MottattDokument mottattDokument, Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType) {
        // Start ny førstegangsbehandling av søknad
        Behandling behandling = behandlingsoppretter.opprettNyFørstegangsbehandling(behandlingÅrsakType, fagsak);
        mottatteDokumentTjeneste.persisterDokumentinnhold(behandling, mottattDokument, Optional.empty());
        dokumentmottakerFelles.opprettTaskForÅStarteBehandling(behandling);
        dokumentmottakerFelles.opprettHistorikk(behandling, mottattDokument.getJournalpostId());
    }

    @Override
    void oppdaterÅpenBehandlingMedDokument(Behandling behandling, MottattDokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType) {
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
            if (!mottattDokument.getElektroniskRegistrert()) {
                if (kompletthetskontroller.støtterBehandlingstypePapirsøknad(behandling)) {
                    mottatteDokumentTjeneste.oppdaterMottattDokumentMedBehandling(mottattDokument, behandling.getId());
                    kompletthetskontroller.flyttTilbakeTilRegistreringPapirsøknad(behandling);
                } else {
                    dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, behandling, mottattDokument);
                }
                return;
            }
            if (erPåVent) {
                kompletthetskontroller.persisterDokumentOgVurderKompletthet(behandling, mottattDokument);
            } else {
                mottatteDokumentTjeneste.oppdaterMottattDokumentMedBehandling(mottattDokument, behandling.getId());
                dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, behandling, mottattDokument);
            }
        }
    }

    @Override
    void håndterKøetBehandling(MottattDokument mottattDokument, Behandling køetBehandling, BehandlingÅrsakType behandlingÅrsakType) {
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
    void håndterAvslåttBehandling(MottattDokument mottattDokument, Fagsak fagsak, Behandling avsluttetBehandling) {
        if (erAvslagGrunnetOpplysningsplikt(avsluttetBehandling)) {
            behandlingsoppretter.opprettNyFørstegangsbehandling(mottattDokument, fagsak, avsluttetBehandling);
        } else {
            dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, avsluttetBehandling, mottattDokument);
        }
    }

    private boolean harMottattSøknadTidligere(Behandling behandling) {
        return mottatteDokumentTjeneste.harMottattDokumentSet(behandling.getId(), DokumentTypeId.getSøknadTyper()) ||
            mottatteDokumentTjeneste.harMottattDokumentKat(behandling.getId(), DokumentKategori.SØKNAD);
    }

}
