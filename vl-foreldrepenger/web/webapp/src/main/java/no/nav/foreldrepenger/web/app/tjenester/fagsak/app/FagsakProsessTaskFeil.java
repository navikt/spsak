package no.nav.foreldrepenger.web.app.tjenester.fagsak.app;

import java.time.LocalDateTime;

import no.nav.foreldrepenger.web.app.tjenester.VurderProsessTaskStatusForPollingApi.ProsessTaskFeilmelder;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

interface FagsakProsessTaskFeil extends DeklarerteFeil, ProsessTaskFeilmelder {

    static final String FEIL_I_TASK = "FP-293308";
    static final String FORSINKELSE_I_TASK = "FP-293309";
    static final String FORSINKELSE_VENTER_SVAR = "FP-293310";

    @Override
    @TekniskFeil(feilkode = FEIL_I_TASK, feilmelding = "[%1$s]. Forespørsel på fagsak [id=%2$s] som ikke kan fortsette, Problemer med task gruppe [%3$s]. Siste prosesstask[id=%4$s] status=%5$s", logLevel = LogLevel.WARN)
    Feil feilIProsessTaskGruppe(String callId, Long fagsakId, String gruppe, Long taskId, ProsessTaskStatus taskStatus);

    @Override
    @TekniskFeil(feilkode = FORSINKELSE_I_TASK, feilmelding = "[%1$s]. Forespørsel på fagsak [id=%2$s] som er utsatt i påvente av task [id=%4$s], Gruppe [%3$s] kjøres ikke før senere. Task status=%5$s, planlagt neste kjøring=%6$s", logLevel = LogLevel.WARN)
    Feil utsattKjøringAvProsessTask(String callId, Long fagsakId, String gruppe, Long taskId, ProsessTaskStatus taskStatus, LocalDateTime nesteKjøringEtter);


    @Override
    @TekniskFeil(feilkode = FORSINKELSE_VENTER_SVAR, feilmelding = "[%1$s]. Forespørsel på behandling [id=%2$s] som venter på svar fra annet system (task [id=%4$s], gruppe [%3$s] kjøres ikke før det er mottatt). Task status=%5$s", logLevel = LogLevel.WARN)
    Feil venterPåSvar(String callId, Long entityId, String gruppe, Long id, ProsessTaskStatus status);

}
