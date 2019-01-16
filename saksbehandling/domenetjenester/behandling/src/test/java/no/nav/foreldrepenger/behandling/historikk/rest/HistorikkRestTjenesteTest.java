package no.nav.foreldrepenger.behandling.historikk.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import no.nav.foreldrepenger.behandling.historikk.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.rest.SaksnummerDto;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class HistorikkRestTjenesteTest {

    private HistorikkTjenesteAdapter historikkApplikasjonTjenesteMock;
    private HistorikkRestTjeneste historikkRestTjeneste;

    @Before
    public void setUp() {
        historikkApplikasjonTjenesteMock = mock(HistorikkTjenesteAdapter.class);
        historikkRestTjeneste = new HistorikkRestTjeneste(historikkApplikasjonTjenesteMock);
    }

    @Test
    public void hentAlleInnslag() {
        // Arrange
        HistorikkinnslagDto innslagDto = new HistorikkinnslagDto();
        lagHistorikkinnslagDel(innslagDto);
        innslagDto.setDokumentLinks(Collections.emptyList());
        when(historikkApplikasjonTjenesteMock.hentAlleHistorikkInnslagForSak(Mockito.any(Saksnummer.class)))
            .thenReturn(Collections.singletonList(innslagDto));

        // Act
        historikkRestTjeneste.hentAlleInnslag(null, new SaksnummerDto("1234"));

        // Assert
        verify(historikkApplikasjonTjenesteMock).hentAlleHistorikkInnslagForSak(Mockito.any(Saksnummer.class));
    }

    private void lagHistorikkinnslagDel(HistorikkinnslagDto innslagDto) {
        HistorikkinnslagDelDto delDto = new HistorikkinnslagDelDto();
        lagHendelseDto(delDto);
        innslagDto.setHistorikkinnslagDeler(Collections.singletonList(delDto));
    }

    private void lagHendelseDto(HistorikkinnslagDelDto delDto) {
        HistorikkinnslagHendelseDto hendelseDto = new HistorikkinnslagHendelseDto();
        hendelseDto.setNavn(HistorikkinnslagType.BEH_STARTET);
        delDto.setHendelse(hendelseDto);
    }
}
