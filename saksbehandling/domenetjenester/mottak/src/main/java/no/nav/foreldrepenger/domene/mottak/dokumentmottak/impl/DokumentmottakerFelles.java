package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.time.LocalDateTime;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.task.StartBehandlingTask;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@Dependent
class DokumentmottakerFelles {

    private ProsessTaskRepository prosessTaskRepository;
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    @SuppressWarnings("unused")
    private DokumentmottakerFelles() { // NOSONAR
        // For CDI
    }

    @Inject
    public DokumentmottakerFelles(ProsessTaskRepository prosessTaskRepository,
                                  BehandlendeEnhetTjeneste behandlendeEnhetTjeneste,
                                  HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.behandlendeEnhetTjeneste = behandlendeEnhetTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    static void leggTilBehandlingsårsak(Behandling behandling, BehandlingÅrsakType behandlingÅrsak) {
        BehandlingÅrsak.Builder builder = BehandlingÅrsak.builder(behandlingÅrsak);
        behandling.getOriginalBehandling().ifPresent(builder::medOriginalBehandling);
        builder.buildFor(behandling);
    }

    void opprettTaskForÅStarteBehandling(Behandling behandling) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(StartBehandlingTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(prosessTaskData);
    }

    void opprettTaskForÅVurdereDokument(Fagsak fagsak, Behandling behandling, InngåendeSaksdokument mottattDokument) {
        // FIXME : Si ifra om at noen må se på dokumentet
    }

    void opprettKøetHistorikk(Behandling køetBehandling, boolean fantesFraFør) {
        if (!fantesFraFør) {
            opprettHistorikkinnslagForVenteFristRelaterteInnslag(køetBehandling, HistorikkinnslagType.BEH_KØET, null, Venteårsak.VENT_ÅPEN_BEHANDLING);
        }
    }

    void opprettHistorikkinnslagForVenteFristRelaterteInnslag(Behandling behandling, HistorikkinnslagType historikkinnslagType, LocalDateTime frist, Venteårsak venteårsak) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForVenteFristRelaterteInnslag(behandling, historikkinnslagType, frist, venteårsak);
    }

    void opprettHistorikk(Behandling behandling, JournalpostId journalPostId) {
        historikkinnslagTjeneste.opprettHistorikkinnslag(behandling, journalPostId);
    }

    void opprettHistorikkinnslagForVedlegg(Long fagsakId, JournalpostId journalpostId, DokumentTypeId dokumentTypeId) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForVedlegg(fagsakId, journalpostId, dokumentTypeId);
    }

    String hentBehandlendeEnhetTilVurderDokumentOppgave(InngåendeSaksdokument dokument, Fagsak sak, Behandling behandling) {
        // Prod: Klageinstans + Viken sender dokumenter til scanning med forside som inneholder enhet. Journalføring og Vurder dokument skal til enheten.
        if (behandling == null) {
            return finnEnhetFraFagsak(sak);
        }
        return behandling.getBehandlendeEnhet();
    }

    private String finnEnhetFraFagsak(Fagsak sak) {
        OrganisasjonsEnhet organisasjonsEnhet = behandlendeEnhetTjeneste.finnBehandlendeEnhetFraSøker(sak);
        return organisasjonsEnhet.getEnhetId();
    }

    OrganisasjonsEnhet utledEnhetFraTidligereBehandling(Behandling nyBehandling, Behandling tidligereBehandling) {
        // Utleder basert på regler rundt sakskompleks og diskresjonskoder. Vil bruke forrige enhet med mindre noen tilsier Kode6
        return behandlendeEnhetTjeneste.sjekkEnhetVedNyAvledetBehandling(nyBehandling, tidligereBehandling.getBehandlendeOrganisasjonsEnhet());
    }

    void opprettHistorikkinnslagForBehandlingOppdatertMedNyeOpplysninger(Behandling behandling, BehandlingÅrsakType behandlingÅrsakType) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBehandlingOppdatertMedNyeOpplysninger(behandling, behandlingÅrsakType);
    }
}
