package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Oppgaveinfo;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

// Tjeneste for å opprette eller avslutte oppgaver i GSAK
public interface OppgaveTjeneste {

    String opprettBasertPåBehandlingId(Long behandlingId, OppgaveÅrsak oppgaveÅrsak);

    String opprettBehandleOppgaveForBehandling(Long behandlingId);

    String opprettBehandleOppgaveForBehandlingMedPrioritetOgFrist(Long behandlingId, String beskrivelse, boolean høyPrioritet, int fristDager);

    String opprettMedPrioritetOgBeskrivelseBasertPåFagsakId(Long fagsakId, OppgaveÅrsak oppgaveÅrsak, String enhetsId,
                                                            String beskrivelse, boolean høyPrioritet);

    void avslutt(Long behandlingId, OppgaveÅrsak oppgaveÅrsak);

    void avslutt(Long behandlingId, String oppgaveId);

    Optional<ProsessTaskData> opprettTaskAvsluttOppgave(Behandling behandling);

    Optional<ProsessTaskData> opprettTaskAvsluttOppgave(Behandling behandling, OppgaveÅrsak oppgaveÅrsak);

    Optional<ProsessTaskData> opprettTaskAvsluttOppgave(Behandling behandling, OppgaveÅrsak oppgaveÅrsak, boolean skalLagres);

    /**
     * Start prosesstask for å avslutte oppgave med en gitt oppgaveÅrsak. Deretter start en ny prosestask med en gitt taskType.
     *
     * @param behandling   behandlingen oppgavene gjelder
     * @param oppgaveÅrsak OppgaveÅrsak til oppgaven som skal lukkes
     * @param taskType     type ProsessTask som skal startes etter at oppgaven lukkes
     */
    void avsluttOppgaveOgStartTask(Behandling behandling, OppgaveÅrsak oppgaveÅrsak, String taskType);

    List<Oppgaveinfo> hentOppgaveListe(AktørId aktørId, List<String> oppgaveÅrsaker);

    List<OppgaveBehandlingKobling> hentOppgaverRelatertTilBehandling(Behandling behandling);

    /**
     * Benyttes så saken må behandles av Infotrygd
     *
     * @param behandlingId behandlings ID
     * @return oppgaveId
     */

    String opprettOppgaveSakSkalTilInfotrygd(Long behandlingId);

    /**
     * Opprett en oppgave i GSAK til NØS (NAV Økonomi Stønad) slik at saksbehandler i NØS kan stoppe utbetaling av
     * ARENA ytelse.
     * @param behandlingId behandling id
     * @param førsteUttaksdato første uttaksdato fra uttaksplan
     * @return oppgaveId
     */
    String opprettOppgaveStopUtbetalingAvARENAYtelse(long behandlingId, LocalDate førsteUttaksdato);
}
