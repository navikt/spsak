package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Behandlingstema;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class InfotrygdSakTest {


    private KodeverkRepository kodeverkRepository;

    @Before
    public void setup() {
        kodeverkRepository = mock(KodeverkRepository.class);
    }

    @Test
    public void skal_sette_tomDato_til_opphørdato_minus_1_virkedag_når_det_er_mandag () throws DatatypeConfigurationException {
        LocalDate iverksattDato = LocalDate.of(2018, 3, 19);
        LocalDate registrertDato = iverksattDato.minusDays(5);
        InfotrygdVedtak infotrygdVedtak = lagInfotrygdVedtak(iverksattDato, registrertDato, 7);

        InfotrygdSak infotrygdSak = new InfotrygdSak(infotrygdVedtak, null);
        DatoIntervallEntitet periode = infotrygdSak.getPeriode();

        assertEquals(periode.getFomDato().getDayOfWeek(), DayOfWeek.MONDAY);
        assertEquals(periode.getTomDato().getDayOfWeek(), DayOfWeek.FRIDAY);
        assertEquals(periode.getTomDato(), LocalDate.of(2018, 3, 23));
    }

    @Test
    public void skal_sette_tomDato_til_opphørdato_minus_1_virkedag_når_det_er_fredag () throws DatatypeConfigurationException {
        LocalDate iverksattDato = LocalDate.of(2018, 3, 19);
        LocalDate registrertDato = iverksattDato.minusDays(5);
        InfotrygdVedtak infotrygdVedtak = lagInfotrygdVedtak(iverksattDato, registrertDato, 11);

        InfotrygdSak infotrygdSak = new InfotrygdSak(infotrygdVedtak, null);
        DatoIntervallEntitet periode = infotrygdSak.getPeriode();

        assertEquals(periode.getFomDato().getDayOfWeek(), DayOfWeek.MONDAY);
        assertEquals(periode.getTomDato().getDayOfWeek(), DayOfWeek.THURSDAY);
        assertEquals(periode.getTomDato(), LocalDate.of(2018, 3, 29));
    }

    @Test
    public void skal_sette_tomDato_til_opphørdato_minus_1_virkedag_når_det_er_onsdag () throws DatatypeConfigurationException {
        LocalDate iverksattDato = LocalDate.of(2018, 3, 19);
        LocalDate registrertDato = iverksattDato.minusDays(5);
        InfotrygdVedtak infotrygdVedtak = lagInfotrygdVedtak(iverksattDato, registrertDato, 2);

        InfotrygdSak infotrygdSak = new InfotrygdSak(infotrygdVedtak, null);
        DatoIntervallEntitet periode = infotrygdSak.getPeriode();

        assertEquals(periode.getFomDato().getDayOfWeek(), DayOfWeek.MONDAY);
        assertEquals(periode.getTomDato().getDayOfWeek(), DayOfWeek.TUESDAY);
        assertEquals(periode.getTomDato(), LocalDate.of(2018, 3, 20));
    }

    @Test
    public void skalKonvertereTilUnderkategori() throws DatatypeConfigurationException {
        Mockito.when(kodeverkRepository.finnForKodeverkEiersKode(any(), any(), any())).thenReturn(TemaUnderkategori.PÅRØRENDE_OMSORGSPENGER);
        InfotrygdSak infotrygdSak = new InfotrygdSak(lagInfotrygdVedtakMedBehandlingsTema("OM"), kodeverkRepository);
        assertThat(infotrygdSak.getTemaUnderkategori()).isEqualByComparingTo(TemaUnderkategori.PÅRØRENDE_OMSORGSPENGER);
    }


    @Test
    public void skalKonvertereTilUkjentUnderkategori() throws DatatypeConfigurationException {
        Mockito.when(kodeverkRepository.finnForKodeverkEiersKode(any(), any(), any())).thenReturn(TemaUnderkategori.UDEFINERT);
        InfotrygdSak infotrygdSak = new InfotrygdSak(lagInfotrygdVedtakMedBehandlingsTema("FINNES_IKKE"), kodeverkRepository);
        assertThat(infotrygdSak.getTemaUnderkategori()).isEqualByComparingTo(TemaUnderkategori.UDEFINERT);
    }

    @Test
    public void blirNullUtenUnderkategori() throws DatatypeConfigurationException {
        LocalDate iverksattDato = LocalDate.of(2018, 3, 19);
        LocalDate registrertDato = iverksattDato.minusDays(5);
        InfotrygdVedtak infotrygdVedtak = lagInfotrygdVedtak(iverksattDato, registrertDato, 2);
        InfotrygdSak infotrygdSak = new InfotrygdSak(infotrygdVedtak, null);
        assertThat(infotrygdSak.getTemaUnderkategori()).isEqualByComparingTo(TemaUnderkategori.UDEFINERT);
    }

    private InfotrygdVedtak lagInfotrygdVedtakMedBehandlingsTema(String behandlingstema) throws DatatypeConfigurationException {
        LocalDate iverksattDato = LocalDate.of(2018, 3, 19);
        LocalDate registrertDato = iverksattDato.minusDays(5);
        InfotrygdVedtak infotrygdVedtak = lagInfotrygdVedtak(iverksattDato, registrertDato, 2);
        Behandlingstema bt = new Behandlingstema();
        bt.setValue(behandlingstema);
        infotrygdVedtak.setBehandlingstema(bt);
        return infotrygdVedtak;
    }

    private XMLGregorianCalendar konverterTilXMLGregorianCalendarFraLocalDate (LocalDate localdate) throws DatatypeConfigurationException {
        GregorianCalendar gcal = GregorianCalendar.from(localdate.atStartOfDay(ZoneId.systemDefault()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
    }

    private InfotrygdVedtak lagInfotrygdVedtak(LocalDate iverksattDato, LocalDate registrertDato, int i) throws DatatypeConfigurationException {
        LocalDate opphoerFomDato = iverksattDato.plusDays(i); // 26. mars, som er en mandag, så tomDato skal være fredag

        InfotrygdVedtak infotrygdVedtak = new InfotrygdVedtak();
        infotrygdVedtak.setSakId(Integer.toString(i));
        infotrygdVedtak.setVedtatt(konverterTilXMLGregorianCalendarFraLocalDate(registrertDato));
        infotrygdVedtak.setIverksatt(konverterTilXMLGregorianCalendarFraLocalDate(iverksattDato));
        infotrygdVedtak.setOpphoerFom(konverterTilXMLGregorianCalendarFraLocalDate(opphoerFomDato));
        return infotrygdVedtak;
    }
}
