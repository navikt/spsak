package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.time.LocalDate;
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
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.SjekkManglendeFodselDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.vedtak.exception.FunksjonellException;

public class AksjonspunktRestTjenesteTest {

    // skal_håndtere_overlappende_perioder data
    private static final LocalDate now = LocalDate.now();
    private static final long behandlingId = 1L;
    private static final Long behandlingVersjon = 2L;
    private static final String begrunnelse = "skal_håndtere_overlappende_perioder";
    private static final LocalDate fødselsdato = now.plusDays(40);
    private static final LocalDate termindato = now.plusDays(30);
    private static final LocalDate utstedtdato = now.minusDays(10);
    private static final int antallBarn = 1;
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
    public void skal_bekrefte_antall_barn() throws URISyntaxException {
        Collection<BekreftetAksjonspunktDto> aksjonspunkt = new ArrayList<>();
        aksjonspunkt.add(
            new SjekkManglendeFodselDto(
                begrunnelse,
                false,
                false,
                null,
                0));

        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandlingVersjon, aksjonspunkt));

        verify(aksjonspunktApplikasjonTjenesteMock).bekreftAksjonspunkter(ArgumentMatchers.anyCollection(),
            anyLong());

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

    @Test(expected = FunksjonellException.class)
    public void skal_ikke_kunne_bekrefte_andre_aksjonspunkt_ved_status_fatter_vedtak() throws URISyntaxException {
        when(behandling.getStatus()).thenReturn(BehandlingStatus.FATTER_VEDTAK);
        Collection<BekreftetAksjonspunktDto> aksjonspunkt = new ArrayList<>();
        aksjonspunkt.add(
            new SjekkManglendeFodselDto(
                begrunnelse,
                false,
                false,
                null,
                        0)
        );
        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandlingVersjon, aksjonspunkt));
    }

    private AksjonspunktGodkjenningDto opprettetGodkjentAksjonspunkt(boolean godkjent) {
        AksjonspunktGodkjenningDto endretDto = new AksjonspunktGodkjenningDto();
        endretDto.setAksjonspunktKode(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL);
        endretDto.setGodkjent(godkjent);
        return endretDto;
    }

}
