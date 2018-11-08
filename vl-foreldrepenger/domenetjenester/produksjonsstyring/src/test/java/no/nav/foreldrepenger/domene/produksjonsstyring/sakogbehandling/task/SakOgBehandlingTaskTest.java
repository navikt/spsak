package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.AvsluttetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.Behandlingsstatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.OpprettetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.SakOgBehandlingTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class SakOgBehandlingTaskTest {

    public static final String AKTØR_ID = "123";
    public static final String ANSVARLIG_ENHET = "4833";
    public static final String FORELDREPENGER_KODE = "FOR";
    public static final String SØKNAD_KODE = "ae0043";
    public static final String BEHANDLINGS_ID = "111";
    private SakOgBehandlingTjeneste sakOgBehandlingTjenesteMock;

    @Before
    public void setup() {
        sakOgBehandlingTjenesteMock=mock(SakOgBehandlingTjeneste.class);
    }

    @Test
    public void skalKalleBehandlingAvsluttetNårBehandlingStatusKodeErAvsluttet() throws Exception {
        ProsessTaskData prosessTaskData = new ProsessTaskData(SakOgBehandlingTask.TASKNAME);

        prosessTaskData.setProperty(SakOgBehandlingTask.BEHANDLING_STATUS_KEY, BehandlingStatus.AVSLUTTET.getKode());

        fyllUtProsessTaskData(prosessTaskData);

        SakOgBehandlingTask sakOgBehandlingTask = new SakOgBehandlingTask(sakOgBehandlingTjenesteMock);
        ArgumentCaptor<AvsluttetBehandlingStatus> captor = ArgumentCaptor.forClass(AvsluttetBehandlingStatus.class);

        sakOgBehandlingTask.doTask(prosessTaskData);

        verify(sakOgBehandlingTjenesteMock).behandlingAvsluttet(captor.capture());
        verify(sakOgBehandlingTjenesteMock, times(0)).behandlingOpprettet(any());

        AvsluttetBehandlingStatus behandlingStatus = captor.getValue();
        verifiserBehandlingStatus(behandlingStatus);
    }

    @Test
    public void skalKalleBehandlingOppretetNårBehandlingStatusKodeErOpprettet() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(SakOgBehandlingTask.TASKNAME);

        prosessTaskData.setProperty(SakOgBehandlingTask.BEHANDLING_STATUS_KEY, BehandlingStatus.OPPRETTET.getKode());

        fyllUtProsessTaskData(prosessTaskData);

        LocalDate behandlingOpprettetTidspunkt = LocalDate.now();
        prosessTaskData.setProperty(SakOgBehandlingTask.BEHANDLING_OPPRETTET_TIDSPUNKT_KEY, behandlingOpprettetTidspunkt.toString());

        SakOgBehandlingTask sakOgBehandlingTask = new SakOgBehandlingTask(sakOgBehandlingTjenesteMock);
        ArgumentCaptor<OpprettetBehandlingStatus> captor = ArgumentCaptor.forClass(OpprettetBehandlingStatus.class);

        sakOgBehandlingTask.doTask(prosessTaskData);

        verify(sakOgBehandlingTjenesteMock).behandlingOpprettet(captor.capture());
        verify(sakOgBehandlingTjenesteMock, times(0)).behandlingAvsluttet(any());

        OpprettetBehandlingStatus behandlingStatus = captor.getValue();

        assertThat(behandlingStatus.getHendelsesTidspunkt()).isEqualTo(behandlingOpprettetTidspunkt.toString());
        verifiserBehandlingStatus(behandlingStatus);
    }

    private void verifiserBehandlingStatus(Behandlingsstatus behandlingStatus) {
        assertThat(behandlingStatus.getAktørId()).isEqualTo(AKTØR_ID);
        assertThat(behandlingStatus.getAnsvarligEnhetRef()).isEqualTo(ANSVARLIG_ENHET);
        assertThat(behandlingStatus.getBehandlingsId()).isEqualTo(BEHANDLINGS_ID);
        assertThat(behandlingStatus.getBehandlingsTypeKode()).isEqualTo(SØKNAD_KODE);
        assertThat(behandlingStatus.getSakstemaKode()).isEqualTo(FORELDREPENGER_KODE);
    }


    private void fyllUtProsessTaskData(ProsessTaskData prosessTaskData) {
        prosessTaskData.setBehandling(1L, Long.valueOf(BEHANDLINGS_ID), AKTØR_ID);
        prosessTaskData.setProperty(SakOgBehandlingTask.BEHANDLINGS_TYPE_KODE_KEY, SØKNAD_KODE);
        prosessTaskData.setProperty(SakOgBehandlingTask.SAKSTEMA_KEY, FORELDREPENGER_KODE);
        prosessTaskData.setProperty(SakOgBehandlingTask.ANSVARLIG_ENHET_KEY, ANSVARLIG_ENHET);
    }

}
