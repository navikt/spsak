package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktGodkjenningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FatterVedtakAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;

public class AksjonspunktRestTjenesteTest {

    // skal_håndtere_overlappende_perioder data
    private static final long behandlingId = 1L;
    private static final Long behandlingVersjon = 2L;
    private static final String begrunnelse = "skal_håndtere_overlappende_perioder";
    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;
    private AksjonspunktApplikasjonTjeneste aksjonspunktApplikasjonTjenesteMock = mock(AksjonspunktApplikasjonTjeneste.class);
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjenesteMock = mock(BehandlingsutredningApplikasjonTjeneste.class);
    private BehandlingRepository behandlingRepository = mock(BehandlingRepository.class);
    private Behandling behandling = mock(Behandling.class);
    private Aksjonspunkt aksjonspunkt = mock(Aksjonspunkt.class);
    private AksjonspunktDefinisjon aksjonspunktDefinisjon = mock(AksjonspunktDefinisjon.class);
    private TotrinnTjeneste totrinnTjeneste = mock(TotrinnTjeneste.class);

    @Before
    public void setUp() {
        when(aksjonspunkt.getAksjonspunktDefinisjon()).thenReturn(aksjonspunktDefinisjon);
        when(behandlingRepository.hentBehandling(anyLong())).thenReturn(behandling);
        when(behandling.getStatus()).thenReturn(no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus.OPPRETTET);
        doNothing().when(behandlingsutredningApplikasjonTjenesteMock).kanEndreBehandling(anyLong(), anyLong());
        aksjonspunktRestTjeneste = new AksjonspunktRestTjeneste(aksjonspunktApplikasjonTjenesteMock, behandlingRepository, behandlingsutredningApplikasjonTjenesteMock, totrinnTjeneste);

    }


    @Test
    public void skal_bekrefte_fatte_vedtak_med_aksjonspunkt_godkjent() throws URISyntaxException {
        when(behandling.getStatus()).thenReturn(BehandlingStatus.FATTER_VEDTAK);
        Collection<BekreftetAksjonspunktDto> aksjonspunkt = new ArrayList<>();
        Collection<AksjonspunktGodkjenningDto> aksjonspunktGodkjenningDtos = new ArrayList<>();
        AksjonspunktGodkjenningDto godkjentAksjonspunkt = opprettetGodkjentAksjonspunkt(true);
        aksjonspunktGodkjenningDtos.add(godkjentAksjonspunkt);
        aksjonspunkt.add(
            new FatterVedtakAksjonspunktDto(
                begrunnelse,
                aksjonspunktGodkjenningDtos));

        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandlingVersjon, aksjonspunkt));

        verify(aksjonspunktApplikasjonTjenesteMock).bekreftAksjonspunkter(ArgumentMatchers.anyCollection(), anyLong());
    }

    private AksjonspunktGodkjenningDto opprettetGodkjentAksjonspunkt(boolean godkjent) {
        AksjonspunktGodkjenningDto endretDto = new AksjonspunktGodkjenningDto();
        endretDto.setAksjonspunktKode(AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD);
        endretDto.setGodkjent(godkjent);
        return endretDto;
    }

}
