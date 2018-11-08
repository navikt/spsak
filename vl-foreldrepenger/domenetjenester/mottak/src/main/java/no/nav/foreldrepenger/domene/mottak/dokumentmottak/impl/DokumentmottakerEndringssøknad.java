package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.DokumentmottakerFelles.leggTilBehandlingsårsak;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;

@ApplicationScoped
@DokumentGruppeRef("ENDRINGSSØKNAD")
class DokumentmottakerEndringssøknad extends DokumentmottakerYtelsesesrelatertDokument {

    private KøKontroller køKontroller;

    @Inject
    public DokumentmottakerEndringssøknad(BehandlingRepositoryProvider repositoryProvider,
                                          DokumentmottakerFelles dokumentmottakerFelles,
                                          MottatteDokumentTjeneste mottatteDokumentTjeneste,
                                          Behandlingsoppretter behandlingsoppretter,
                                          Kompletthetskontroller kompletthetskontroller,
                                          KøKontroller køKontroller) {
        super(dokumentmottakerFelles,
            mottatteDokumentTjeneste,
            behandlingsoppretter,
            kompletthetskontroller,
            repositoryProvider);
        this.køKontroller = køKontroller;
    }

    @Override
    void oppdaterÅpenBehandlingMedDokument(Behandling behandling, MottattDokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType) {
        dokumentmottakerFelles.opprettHistorikk(behandling, mottattDokument.getJournalpostId());

        BehandlingÅrsakType årsakEndringFraBruker = BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER;
        dokumentmottakerFelles.opprettHistorikkinnslagForBehandlingOppdatertMedNyeOpplysninger(behandling, årsakEndringFraBruker);
        if (harAlleredeMottattEndringssøknad(behandling)) {
            Behandling nyBehandling = oppdatereViaHenleggelse(behandling, mottattDokument, årsakEndringFraBruker);
            køKontroller.dekøFørsteBehandlingISakskompleks(nyBehandling);
        } else if (kompletthetErPassert(behandling)) {
            oppdatereViaHenleggelse(behandling, mottattDokument, årsakEndringFraBruker);
        } else {
            mottatteDokumentTjeneste.oppdaterMottattDokumentMedBehandling(mottattDokument, behandling.getId());
            // Oppdater åpen behandling med Endringssøknad
            leggTilBehandlingsårsak(behandling, årsakEndringFraBruker);
            if (!mottattDokument.getElektroniskRegistrert()) {
                kompletthetskontroller.flyttTilbakeTilRegistreringPapirsøknad(behandling);
                return;
            }
            kompletthetskontroller.persisterDokumentOgVurderKompletthet(behandling, mottattDokument);
        }
    }

    @Override
    void håndterKøetBehandling(MottattDokument mottattDokument, Behandling køetBehandling, BehandlingÅrsakType behandlingÅrsakType) {
        if (harAlleredeMottattEndringssøknad(køetBehandling)) {
            // Oppdatere behandling gjennom henleggelse
            Behandling nyKøetBehandling = behandlingsoppretter.oppdaterBehandlingViaHenleggelse(køetBehandling, BehandlingÅrsakType.KØET_BEHANDLING);
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
        if (fagsak.getYtelseType().gjelderEngangsstønad()) {
            dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, avsluttetBehandling, mottattDokument);
        }

        if (erAvslagGrunnetOpplysningsplikt(avsluttetBehandling)) {
            behandlingsoppretter.opprettNyFørstegangsbehandling(mottattDokument, fagsak, avsluttetBehandling);
        } else {
            dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, avsluttetBehandling, mottattDokument);
        }
    }

    private Behandling oppdatereViaHenleggelse(Behandling behandling, MottattDokument mottattDokument, BehandlingÅrsakType behandlingÅrsak) {
        Behandling nyBehandling = behandlingsoppretter.oppdaterBehandlingViaHenleggelse(behandling, behandlingÅrsak);
        Optional<LocalDate> søknadsdato = revurderingRepository.finnSøknadsdatoFraHenlagtBehandling(nyBehandling);
        mottatteDokumentTjeneste.persisterDokumentinnhold(nyBehandling, mottattDokument, søknadsdato);
        return nyBehandling;
    }

    @Override
    void håndterAvsluttetTidligereBehandling(MottattDokument mottattDokument, Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType) {
        // Opprett revurdering
        Behandling revurdering = behandlingsoppretter.opprettRevurdering(fagsak, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);
        mottatteDokumentTjeneste.persisterDokumentinnhold(revurdering, mottattDokument, Optional.empty());
        dokumentmottakerFelles.opprettHistorikk(revurdering, mottattDokument.getJournalpostId());
    }

    @Override
    void håndterIngenTidligereBehandling(Fagsak fagsak, MottattDokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType) {
        // Kan ikke håndtere endringssøknad når ingen behandling finnes -> Opprett manuell task
        dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, null, mottattDokument);
    }

    private boolean kompletthetErPassert(Behandling behandling) {
        return behandlingsoppretter.erKompletthetssjekkPassert(behandling);
    }

    private boolean harAlleredeMottattEndringssøknad(Behandling behandling) {
        return mottatteDokumentTjeneste.harMottattDokumentSet(behandling.getId(), DokumentTypeId.getEndringSøknadTyper());
    }

}
