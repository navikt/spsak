package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;

@ApplicationScoped
@DokumentGruppeRef("ENDRINGSSØKNAD")
class DokumentmottakerEndringssøknad extends DokumentmottakerYtelsesesrelatertDokument {

    private KøKontroller køKontroller;

    @Inject
    public DokumentmottakerEndringssøknad(GrunnlagRepositoryProvider repositoryProvider,
                                          ResultatRepositoryProvider resultatRepositoryProvider,
                                          DokumentmottakerFelles dokumentmottakerFelles,
                                          MottatteDokumentTjeneste mottatteDokumentTjeneste,
                                          Behandlingsoppretter behandlingsoppretter,
                                          Kompletthetskontroller kompletthetskontroller,
                                          KøKontroller køKontroller) {
        super(dokumentmottakerFelles,
            mottatteDokumentTjeneste,
            behandlingsoppretter,
            kompletthetskontroller,
            repositoryProvider, resultatRepositoryProvider);
        this.køKontroller = køKontroller;
    }

    @Override
    void oppdaterÅpenBehandlingMedDokument(Behandling behandling, InngåendeSaksdokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType) {
        dokumentmottakerFelles.opprettHistorikk(behandling, mottattDokument.getJournalpostId());
        // TODO SP : Vurder hva vi skal gjøre her.
        BehandlingÅrsakType årsakEndringFraBruker = BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER;
        dokumentmottakerFelles.opprettHistorikkinnslagForBehandlingOppdatertMedNyeOpplysninger(behandling, årsakEndringFraBruker);
        Behandling nyBehandling = oppdatereViaHenleggelse(behandling, mottattDokument, årsakEndringFraBruker);
        køKontroller.dekøFørsteBehandlingISakskompleks(nyBehandling);
    }

    @Override
    void håndterKøetBehandling(InngåendeSaksdokument mottattDokument, Behandling køetBehandling, BehandlingÅrsakType behandlingÅrsakType) {
        // Oppdatere behandling gjennom henleggelse
        // TODO SP : Vurder hva vi skal gjøre her.
        Behandling nyKøetBehandling = behandlingsoppretter.oppdaterBehandlingViaHenleggelse(køetBehandling, BehandlingÅrsakType.KØET_BEHANDLING);
        behandlingsoppretter.settSomKøet(nyKøetBehandling);
        Optional<LocalDate> søknadsdato = revurderingRepository.finnSøknadsdatoFraHenlagtBehandling(nyKøetBehandling);
        kompletthetskontroller.persisterKøetDokumentOgVurderKompletthet(nyKøetBehandling, mottattDokument, søknadsdato);
    }

    private Behandling oppdatereViaHenleggelse(Behandling behandling, InngåendeSaksdokument mottattDokument, BehandlingÅrsakType behandlingÅrsak) {
        Behandling nyBehandling = behandlingsoppretter.oppdaterBehandlingViaHenleggelse(behandling, behandlingÅrsak);
        Optional<LocalDate> søknadsdato = revurderingRepository.finnSøknadsdatoFraHenlagtBehandling(nyBehandling);
        mottatteDokumentTjeneste.persisterDokumentinnhold(nyBehandling, mottattDokument, søknadsdato);
        return nyBehandling;
    }

    @Override
    void håndterAvsluttetTidligereBehandling(InngåendeSaksdokument mottattDokument, Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType) {
        // Opprett revurdering
        Behandling revurdering = behandlingsoppretter.opprettRevurdering(fagsak, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);
        mottatteDokumentTjeneste.persisterDokumentinnhold(revurdering, mottattDokument, Optional.empty());
        dokumentmottakerFelles.opprettHistorikk(revurdering, mottattDokument.getJournalpostId());
    }

    @Override
    void håndterIngenTidligereBehandling(Fagsak fagsak, InngåendeSaksdokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType) {
        // Kan ikke håndtere endringssøknad når ingen behandling finnes -> Opprett manuell task
        dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, null, mottattDokument);
    }

    private boolean kompletthetErPassert(Behandling behandling) {
        return behandlingsoppretter.erKompletthetssjekkPassert(behandling);
    }
}
