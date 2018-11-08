package no.nav.foreldrepenger.web.app.tjenester.behandling.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.AsyncPollingStatus.Status;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

public class BehandlingsprosessApplikasjonTjenesteTest {

    private static final String GRUPPE_1 = "gruppe1";

    @Rule
    public final ExpectedException expected = ExpectedException.none();

    private final ProsessTaskData taskData = new ProsessTaskData("taskType1");
    private Behandling behandling;

    public BehandlingsprosessApplikasjonTjenesteTest() {
        this.taskData.setGruppe(GRUPPE_1);
        this.behandling = ScenarioMorSøkerForeldrepenger.forFødsel().lagMocked();
    }

    @Test
    public void skal_returnere_gruppe_når_ikke_er_kjørt() throws Exception {

        BehandlingsprosessApplikasjonTjenesteImpl sut = initSut(GRUPPE_1, taskData);
        Optional<AsyncPollingStatus> status = sut.sjekkProsessTaskPågårForBehandling(behandling, null);
        assertThat(status.get().getStatus()).isEqualTo(Status.PENDING);

        status = sut.sjekkProsessTaskPågårForBehandling(behandling, GRUPPE_1);
        assertThat(status.get().getStatus()).isEqualTo(Status.PENDING);
    }

    @Test
    public void skal_ikke_returnere_gruppe_når_er_kjørt() throws Exception {
        markerFerdig(taskData);

        BehandlingsprosessApplikasjonTjenesteImpl sut = initSut(GRUPPE_1, taskData);
        Optional<AsyncPollingStatus> status = sut.sjekkProsessTaskPågårForBehandling(behandling, null);
        assertThat(status).isEmpty();

        status = sut.sjekkProsessTaskPågårForBehandling(behandling, GRUPPE_1);
        assertThat(status).isEmpty();

    }

    @Test
    public void skal_kaste_exception_når_task_har_feilet_null_gruppe() throws Exception {
        markerFeilet(taskData);

        BehandlingsprosessApplikasjonTjenesteImpl sut = initSut(GRUPPE_1, taskData);
        Optional<AsyncPollingStatus> status = sut.sjekkProsessTaskPågårForBehandling(behandling, null);

        assertThat(status.get().getStatus()).isEqualTo(Status.HALTED);
    }

    @Test
    public void skal_kaste_exception_når_task_har_feilet_angitt_gruppe() throws Exception {
        markerFeilet(taskData);

        BehandlingsprosessApplikasjonTjenesteImpl sut = initSut(GRUPPE_1, taskData);

        Optional<AsyncPollingStatus> status = sut.sjekkProsessTaskPågårForBehandling(behandling, GRUPPE_1);

        assertThat(status.get().getStatus()).isEqualTo(Status.HALTED);
    }

    @Test
    public void skal_kaste_exception_når_task_neste_kjøring_er_utsatt() throws Exception {
        taskData.medNesteKjøringEtter(LocalDateTime.now().plusHours(1));

        BehandlingsprosessApplikasjonTjenesteImpl sut = initSut(GRUPPE_1, taskData);
        Optional<AsyncPollingStatus> status = sut.sjekkProsessTaskPågårForBehandling(behandling, GRUPPE_1);

        assertThat(status.get().getStatus()).isEqualTo(Status.DELAYED);

    }
    

    private void markerFeilet(ProsessTaskData pt) {
        pt.setStatus(ProsessTaskStatus.FEILET);
        pt.setAntallFeiledeForsøk(pt.getAntallFeiledeForsøk()+ 1);
        pt.setNesteKjøringEtter(null);
        pt.setSistKjørt(LocalDateTime.now());
    }

    private void markerFerdig(ProsessTaskData pt) {
        pt.setStatus(ProsessTaskStatus.FERDIG);
        pt.setNesteKjøringEtter(null);
        pt.setSistKjørt(LocalDateTime.now());
    }

    private BehandlingsprosessApplikasjonTjenesteImpl initSut(String gruppe, ProsessTaskData taskData) {
        BehandlingskontrollAsynkTjeneste tjeneste = Mockito.mock(BehandlingskontrollAsynkTjeneste.class);

        Map<String, ProsessTaskData> data = new HashMap<>();
        data.put(gruppe, taskData);

        Mockito.when(tjeneste.sjekkProsessTaskPågårForBehandling(Mockito.any(), Mockito.any())).thenReturn(data);
        BehandlingsprosessApplikasjonTjenesteImpl sut = new BehandlingsprosessApplikasjonTjenesteImpl(tjeneste);
        return sut;
    }
}
