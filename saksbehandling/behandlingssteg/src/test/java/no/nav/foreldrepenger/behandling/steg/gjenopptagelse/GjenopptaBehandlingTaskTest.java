package no.nav.foreldrepenger.behandling.steg.gjenopptagelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.steg.gjenopptagelse.tjeneste.GjenopptaBehandlingTask;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataEndringshåndterer;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class GjenopptaBehandlingTaskTest {

    private GjenopptaBehandlingTask task; // objektet vi tester

    private BehandlingRepository mockBehandlingRepository;
    private BehandlingskontrollTjeneste mockBehandlingskontrollTjeneste;
    private RegisterdataEndringshåndterer mockRegisterdataEndringshåndterer;
    private BehandlendeEnhetTjeneste mockEnhetsTjeneste;
    private OrganisasjonsEnhet organisasjonsEnhet = new OrganisasjonsEnhet("4802", "NAV Bærum");

    @Before
    public void setup() {
        mockBehandlingRepository = mock(BehandlingRepository.class);
        mockBehandlingskontrollTjeneste = mock(BehandlingskontrollTjeneste.class);
        mockRegisterdataEndringshåndterer = mock(RegisterdataEndringshåndterer.class);
        mockEnhetsTjeneste = mock(BehandlendeEnhetTjeneste.class);

        task = new GjenopptaBehandlingTask(mockBehandlingRepository, mockBehandlingskontrollTjeneste, mockRegisterdataEndringshåndterer, mockEnhetsTjeneste);
    }

    @Test
    public void skal_gjenoppta_behandling() {
        final Long behandlingId = 10L;

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forDefaultAktør();
        Behandling behandling = scenario.lagMocked();
        behandling.setBehandlendeEnhet(organisasjonsEnhet);
        when(mockBehandlingRepository.hentBehandling(any())).thenReturn(behandling);
        when(mockEnhetsTjeneste.sjekkEnhetVedGjenopptak(any())).thenReturn(Optional.empty());

        ProsessTaskData prosessTaskData = new ProsessTaskData(GjenopptaBehandlingTask.TASKTYPE);
        prosessTaskData.setBehandling(0L, behandlingId, new AktørId(0L).toString());

        task.doTask(prosessTaskData);

        verify(mockBehandlingskontrollTjeneste).initBehandlingskontroll(Mockito.anyLong());
        verify(mockBehandlingskontrollTjeneste).taBehandlingAvVent(eq(behandling), any());
        verify(mockRegisterdataEndringshåndterer).oppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(behandling);
        verify(mockBehandlingskontrollTjeneste).settAutopunkterTilUtført(any(), eq(false));
        verify(mockBehandlingskontrollTjeneste).prosesserBehandling(any());
    }

    @Test
    public void skal_gjenoppta_behandling_bytteenhet() {
        final Long behandlingId = 10L;

        OrganisasjonsEnhet enhet = new OrganisasjonsEnhet("2103", "NAV Viken");
        BehandlingLås lås = mock(BehandlingLås.class);
        BehandlingskontrollKontekst kontekst = mock(BehandlingskontrollKontekst.class);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forDefaultAktør();
        Behandling behandling = scenario.lagMocked();
        behandling.setBehandlendeEnhet(organisasjonsEnhet);
        when(mockBehandlingRepository.hentBehandling(any())).thenReturn(behandling);
        when(mockBehandlingRepository.lagre(any(Behandling.class), any())).thenReturn(0L);
        when(mockBehandlingskontrollTjeneste.initBehandlingskontroll(Mockito.anyLong())).thenReturn(kontekst);
        when(kontekst.getSkriveLås()).thenReturn(lås);
        when(mockEnhetsTjeneste.sjekkEnhetVedGjenopptak(any())).thenReturn(Optional.of(enhet));

        ProsessTaskData prosessTaskData = new ProsessTaskData(GjenopptaBehandlingTask.TASKTYPE);
        prosessTaskData.setBehandling(0L, behandlingId, "0");

        task.doTask(prosessTaskData);

        verify(mockBehandlingskontrollTjeneste).initBehandlingskontroll(Mockito.anyLong());
        verify(mockBehandlingskontrollTjeneste).taBehandlingAvVent(eq(behandling), any());
        verify(mockRegisterdataEndringshåndterer).oppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(behandling);
        verify(mockBehandlingskontrollTjeneste).settAutopunkterTilUtført(any(), eq(false));
        verify(mockBehandlingskontrollTjeneste).prosesserBehandling(any());
        assertThat(behandling.getBehandlendeEnhet()).isEqualTo("2103");
    }
}

