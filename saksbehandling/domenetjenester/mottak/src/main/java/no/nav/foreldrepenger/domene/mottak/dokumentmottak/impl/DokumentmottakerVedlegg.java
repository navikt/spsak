package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static java.util.Arrays.asList;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;

@ApplicationScoped
@DokumentGruppeRef("VEDLEGG")
class DokumentmottakerVedlegg implements Dokumentmottaker {

    private BehandlingRepository behandlingRepository;
    private Behandlingsoppretter behandlingsoppretter;
    private DokumentmottakerFelles dokumentmottakerFelles;
    private BehandlingRevurderingRepository revurderingRepository;
    private Kompletthetskontroller kompletthetskontroller;
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;

    @Inject
    public DokumentmottakerVedlegg(GrunnlagRepositoryProvider repositoryProvider,
                                   DokumentmottakerFelles dokumentmottakerFelles,
                                   Behandlingsoppretter behandlingsoppretter,
                                   Kompletthetskontroller kompletthetskontroller, MottatteDokumentTjeneste mottatteDokumentTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.revurderingRepository = repositoryProvider.getBehandlingRevurderingRepository();
        this.behandlingsoppretter = behandlingsoppretter;
        this.dokumentmottakerFelles = dokumentmottakerFelles;
        this.kompletthetskontroller = kompletthetskontroller;
        this.mottatteDokumentTjeneste = mottatteDokumentTjeneste;
    }

    @Override
    public void mottaDokument(InngåendeSaksdokument mottattDokument, Fagsak fagsak, DokumentTypeId dokumentTypeId, BehandlingÅrsakType behandlingÅrsakType) {
        dokumentmottakerFelles.opprettHistorikkinnslagForVedlegg(mottattDokument.getFagsakId(), mottattDokument.getJournalpostId(), dokumentTypeId);

        behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsak.getSaksnummer());  //PKMANTIS-1122 lazy l.
        Optional<Behandling> åpenBehandling = behandlingRepository.hentÅpneBehandlingerForFagsakId(fagsak.getId()).stream()
            .findFirst();

        if (åpenBehandling.isPresent()) {
            håndterÅpenBehandling(fagsak, åpenBehandling.get(), mottattDokument);
        } else {
            if (skalOppretteNyFørstegangsbehandling(fagsak)) {
                Optional<Behandling> behandlingOptional = behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(fagsak.getId());
                behandlingsoppretter.opprettNyFørstegangsbehandling(mottattDokument, fagsak, behandlingOptional.get()); // NOSONAR
            } else {
                dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, null, mottattDokument);
            }
        }
    }

    @Override
    public void mottaDokumentForKøetBehandling(InngåendeSaksdokument mottattDokument, Fagsak fagsak, DokumentTypeId dokumentTypeId, BehandlingÅrsakType behandlingÅrsakType) {
        dokumentmottakerFelles.opprettHistorikkinnslagForVedlegg(mottattDokument.getFagsakId(), mottattDokument.getJournalpostId(), dokumentTypeId);

        Optional<Behandling> eksisterendeKøetBehandling = revurderingRepository.finnKøetYtelsesbehandling(fagsak.getId());
        Behandling køetBehandling = eksisterendeKøetBehandling
            .orElseGet(() -> behandlingsoppretter.opprettKøetBehandling(fagsak, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING));
        dokumentmottakerFelles.opprettKøetHistorikk(køetBehandling, eksisterendeKøetBehandling.isPresent());
        kompletthetskontroller.persisterKøetDokumentOgVurderKompletthet(køetBehandling, mottattDokument, Optional.empty());
    }

    private void håndterÅpenBehandling(Fagsak fagsak, Behandling behandling, InngåendeSaksdokument mottattDokument) {
        /** TODO (essv): Digitalen - løfte {@link FagsakYtelseType.FORELDREPENGER} til protokoll for Startpunkt,
         * slik at samme protokoll som for FP kan brukes */
        if (fagsak.getYtelseType().equals(FagsakYtelseType.FORELDREPENGER) && asList(BehandlingType.FØRSTEGANGSSØKNAD, BehandlingType.REVURDERING).contains(behandling.getType())) {
            kompletthetskontroller.persisterDokumentOgVurderKompletthet(behandling, mottattDokument);
        } else {
            dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, behandling, mottattDokument);
        }
    }

    private boolean skalOppretteNyFørstegangsbehandling(Fagsak fagsak) {
        if (mottatteDokumentTjeneste.erSisteYtelsesbehandlingAvslåttPgaManglendeDokumentasjon(fagsak)) {
            return !mottatteDokumentTjeneste.harFristForInnsendingAvDokGåttUt(fagsak);
        }
        return false;
    }

}
