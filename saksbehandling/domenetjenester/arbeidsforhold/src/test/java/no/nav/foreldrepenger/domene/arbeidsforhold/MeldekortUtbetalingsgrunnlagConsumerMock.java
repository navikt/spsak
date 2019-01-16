package no.nav.foreldrepenger.domene.arbeidsforhold;

import static no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil.convertToXMLGregorianCalendar;

import java.time.LocalDate;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Meldekort;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.ObjectFactory;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Sak;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Saksstatuser;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Vedtak;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Vedtaksstatuser;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeRequest;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeResponse;
import no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagConsumer;

@Dependent
@Alternative
@Priority(1)
class MeldekortUtbetalingsgrunnlagConsumerMock implements MeldekortUtbetalingsgrunnlagConsumer {
    private FinnMeldekortUtbetalingsgrunnlagListeResponse finnMeldekortUtbetalingsgrunnlagListeResponse;
    private ObjectFactory of = new ObjectFactory();

    MeldekortUtbetalingsgrunnlagConsumerMock(){
        finnMeldekortUtbetalingsgrunnlagListeResponse = new FinnMeldekortUtbetalingsgrunnlagListeResponse();
    }

    @Override
    public FinnMeldekortUtbetalingsgrunnlagListeResponse finnMeldekortUtbetalingsgrunnlagListe(FinnMeldekortUtbetalingsgrunnlagListeRequest var1) {
        return finnMeldekortUtbetalingsgrunnlagListeResponse;
    }

    public MeldekortUtbetalingsgrunnlagConsumerMock clean(){
        finnMeldekortUtbetalingsgrunnlagListeResponse.getMeldekortUtbetalingsgrunnlagListe().clear();
        return this;
    }

    public MeldekortUtbetalingsgrunnlagConsumerMock lagSakUtenVedtak(Saksnummer saksnummer) {
        Sak sak = of.createSak();
        sak.setFagsystemSakId(saksnummer.getVerdi());
        Tema tema = of.createTema();
        tema.setValue("DAG");
        sak.setTema(tema);
        Saksstatuser saksstatus = of.createSaksstatuser();
        saksstatus.setValue("AKTIV");
        saksstatus.setTermnavn("Aktiv");
        sak.setSaksstatus(saksstatus);
        finnMeldekortUtbetalingsgrunnlagListeResponse.getMeldekortUtbetalingsgrunnlagListe().add(sak);
        return this;
    }

    public MeldekortUtbetalingsgrunnlagConsumerMock lagSakVedtak(Saksnummer saksnummer) throws DatatypeConfigurationException {
        Sak sak = of.createSak();
        sak.setFagsystemSakId(saksnummer.getVerdi());
        Tema tema = of.createTema();
        tema.setValue("DAG");
        sak.setTema(tema);
        Saksstatuser saksstatus = of.createSaksstatuser();
        saksstatus.setValue("AKTIV");
        saksstatus.setTermnavn("Aktiv");
        sak.setSaksstatus(saksstatus);

        Vedtak vedtak = of.createVedtak();
        Vedtaksstatuser vedtaksstatus = of.createVedtaksstatuser();
        vedtaksstatus.setValue("IVERK");
        vedtaksstatus.setTermnavn("Iverksatt");
        vedtak.setVedtaksstatus(vedtaksstatus);

        Meldekort meldekort = of.createMeldekort();
        meldekort.setUtbetalingsgrad(150);
        meldekort.setDagsats(1000);
        meldekort.setBeloep(7500);

            vedtak.setDatoKravMottatt(convertToXMLGregorianCalendar(LocalDate.of(2017, 12, 12)));
            vedtak.setVedtaksdato(convertToXMLGregorianCalendar(LocalDate.of(2017, 12, 24)));
            Periode periode = of.createPeriode();
            periode.setFom(convertToXMLGregorianCalendar(LocalDate.of(2018, 1, 22)));
            periode.setTom(convertToXMLGregorianCalendar(LocalDate.of(2018, 7, 21)));
            vedtak.setVedtaksperiode(periode);
            Periode pm = of.createPeriode();
            pm.setFom(convertToXMLGregorianCalendar(LocalDate.of(2018, 1, 22)));
            pm.setTom(convertToXMLGregorianCalendar(LocalDate.of(2018, 2, 4)));
            meldekort.setMeldekortperiode(pm);

        vedtak.getMeldekortListe().add(meldekort);
        sak.getVedtakListe().add(vedtak);
        finnMeldekortUtbetalingsgrunnlagListeResponse.getMeldekortUtbetalingsgrunnlagListe().add(sak);

        return this;
    }

}
