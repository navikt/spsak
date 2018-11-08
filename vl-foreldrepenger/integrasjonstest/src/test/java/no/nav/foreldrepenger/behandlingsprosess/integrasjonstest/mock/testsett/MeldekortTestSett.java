package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett;

import static no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil.convertToXMLGregorianCalendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Meldekort;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Sak;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Saksstatuser;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Vedtak;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Vedtaksstatuser;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeResponse;

public class MeldekortTestSett {

    private static final Map<String, Long> RESPONSE_MAP = new HashMap<>();

    private static final Map<String, Long> RESPONSE_PERIOD_MAP = new HashMap<>();

    public static void nullstill() {
        RESPONSE_MAP.clear();
    }

    public static FinnMeldekortUtbetalingsgrunnlagListeResponse finnResponse(String ident) {
        if (RESPONSE_MAP.get(ident) != null) {
            return lagRespons(RESPONSE_MAP.get(ident), RESPONSE_PERIOD_MAP.get(ident));
        }
        return new FinnMeldekortUtbetalingsgrunnlagListeResponse();
    }

    public static void meldekortStandard(String aktørId, Long offset) {
        RESPONSE_MAP.put(aktørId, offset);
        RESPONSE_PERIOD_MAP.put(aktørId, 10L);
    }

    public static void meldekortUtvidet(String aktørId, Long offset) {
        RESPONSE_MAP.put(aktørId, offset);
        RESPONSE_PERIOD_MAP.put(aktørId, 30L);
    }

    public static FinnMeldekortUtbetalingsgrunnlagListeResponse lagRespons(long offset, long uker) {
        FinnMeldekortUtbetalingsgrunnlagListeResponse finnMeldekortUtbetalingsgrunnlagListeResponse = new FinnMeldekortUtbetalingsgrunnlagListeResponse();
        Sak sak = lagSak(offset, uker);
        finnMeldekortUtbetalingsgrunnlagListeResponse.getMeldekortUtbetalingsgrunnlagListe().add(sak);
        return finnMeldekortUtbetalingsgrunnlagListeResponse;
    }

    private static Sak lagSak(long offset, long uker) {
        Sak sak = new Sak();
        Saksstatuser saksstatuser = new Saksstatuser();
        saksstatuser.setValue("IVERK");
        sak.setSaksstatus(saksstatuser);
        sak.setFagsystemSakId("Fagsystem");
        no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Tema tema = new no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Tema();
        tema.setValue("AAP");
        sak.setTema(tema);
        Vedtak vedtak = lagVedtak(offset, uker);
        sak.getVedtakListe().add(vedtak);
        return sak;
    }

    private static Vedtak lagVedtak(long offset, long uker) {
        Vedtak vedtak = new Vedtak();
        try {
            vedtak.setVedtaksdato(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset).minusWeeks(uker)));
            Periode periode = new Periode();
            periode.setFom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset).minusWeeks(uker)));
            periode.setTom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset).plusDays(5)));
            vedtak.setVedtaksperiode(periode);
            vedtak.setDatoKravMottatt(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset).minusWeeks(uker + 1)));
        }catch (DatatypeConfigurationException e){

        }
        Vedtaksstatuser vedtaksstatuser = new Vedtaksstatuser();
        vedtaksstatuser.setValue("IVERK");
        vedtak.setVedtaksstatus(vedtaksstatuser);
        --uker;
        while (uker > 2) {
            LocalDate fom1 = LocalDate.now().minusDays(offset).minusWeeks(uker);
            LocalDate fom2 = fom1.minusDays((long)DayOfWeek.from(fom1).getValue() - DayOfWeek.MONDAY.getValue());
            vedtak.getMeldekortListe().add(lagMeldekort(fom2, fom2.plusDays(13)));
            uker = uker - 2;
        }
        return vedtak;
    }

    private static Meldekort lagMeldekort(LocalDate fom, LocalDate tom) {
        Meldekort meldekort = new Meldekort();
        try {
            Periode periode = new Periode();
            periode.setFom(convertToXMLGregorianCalendar(fom));
            periode.setTom(convertToXMLGregorianCalendar(tom));
            meldekort.setBeloep(10000);
            meldekort.setDagsats(1000);
            meldekort.setUtbetalingsgrad(200);
            meldekort.setMeldekortperiode(periode);
        }catch (DatatypeConfigurationException e){

        }
        return meldekort;
    }

}
