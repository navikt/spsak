package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.DokumentmottakerFelles.leggTilBehandlingsårsak;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;

@ApplicationScoped
@DokumentGruppeRef("INNTEKTSMELDING")
class DokumentmottakerInntektsmelding extends DokumentmottakerYtelsesesrelatertDokument {


    @Inject
    public DokumentmottakerInntektsmelding(DokumentmottakerFelles dokumentmottakerFelles,
                                           MottatteDokumentTjeneste mottatteDokumentTjeneste,
                                           Behandlingsoppretter behandlingsoppretter,
                                           Kompletthetskontroller kompletthetskontroller,
                                           BehandlingRepositoryProvider repositoryProvider) {
        super(dokumentmottakerFelles,
            mottatteDokumentTjeneste,
            behandlingsoppretter,
            kompletthetskontroller,
            repositoryProvider);
    }

    @Override
    void håndterIngenTidligereBehandling(Fagsak fagsak, InngåendeSaksdokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType) {
        // Opprett ny førstegangsbehandling
        Behandling behandling = behandlingsoppretter.opprettFørstegangsbehandling(fagsak, BehandlingÅrsakType.UDEFINERT);
        mottatteDokumentTjeneste.persisterDokumentinnhold(behandling, mottattDokument, Optional.empty());
        dokumentmottakerFelles.opprettTaskForÅStarteBehandling(behandling);
        dokumentmottakerFelles.opprettHistorikkinnslagForVedlegg(fagsak.getId(), mottattDokument.getJournalpostId(), mottattDokument.getDokumentTypeId());
    }

    @Override
    void håndterAvsluttetTidligereBehandling(InngåendeSaksdokument mottattDokument, Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType) {
        // Opprett revurdering
        Behandling revurdering = behandlingsoppretter.opprettRevurdering(fagsak, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        mottatteDokumentTjeneste.persisterDokumentinnhold(revurdering, mottattDokument, Optional.empty());
        dokumentmottakerFelles.opprettHistorikkinnslagForVedlegg(fagsak.getId(), mottattDokument.getJournalpostId(), mottattDokument.getDokumentTypeId());
    }

    @Override
    void oppdaterÅpenBehandlingMedDokument(Behandling behandling, InngåendeSaksdokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType) {
        dokumentmottakerFelles.opprettHistorikkinnslagForVedlegg(behandling.getFagsakId(), mottattDokument.getJournalpostId(), mottattDokument.getDokumentTypeId());
        leggTilBehandlingsårsak(behandling, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        dokumentmottakerFelles.opprettHistorikkinnslagForBehandlingOppdatertMedNyeOpplysninger(behandling, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_INNTEKT);
        kompletthetskontroller.persisterDokumentOgVurderKompletthet(behandling, mottattDokument);
    }

    @Override
    void håndterKøetBehandling(InngåendeSaksdokument mottattDokument, Behandling køetBehandling, BehandlingÅrsakType behandlingÅrsakType) {
        kompletthetskontroller.persisterKøetDokumentOgVurderKompletthet(køetBehandling, mottattDokument, Optional.empty());
    }

    @Override
    void håndterAvslåttBehandling(InngåendeSaksdokument mottattDokument, Fagsak fagsak, Behandling avsluttetBehandling) {
        if (erAvslagGrunnetOpplysningsplikt(avsluttetBehandling) && skalOppretteNyFørstegangsbehandling(avsluttetBehandling)) {
            behandlingsoppretter.opprettNyFørstegangsbehandling(mottattDokument, fagsak, avsluttetBehandling);
        } else {
            dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, avsluttetBehandling, mottattDokument);
        }
    }

    private boolean skalOppretteNyFørstegangsbehandling(Behandling avslåttBehandling) {
        if (mottatteDokumentTjeneste.erSisteYtelsesbehandlingAvslåttPgaManglendeDokumentasjon(avslåttBehandling.getFagsak())) {
            return !mottatteDokumentTjeneste.harFristForInnsendingAvDokGåttUt(avslåttBehandling.getFagsak());
        }
        return false;
    }
}
