package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil.convertToXMLGregorianCalendar;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Arbeidskategori;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Behandlingstema;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Engangsstoenad;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Foreldrepenger;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Inntektsperiode;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.PaaroerendeSykdom;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Sykepenger;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeRequest;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeResponse;
import no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag.InfotrygdBeregningsgrunnlagConsumer;

@Dependent
@Alternative
@Priority(1)
class InfotrygdBeregningsgrunnlagConsumerMock implements InfotrygdBeregningsgrunnlagConsumer {
    private FinnGrunnlagListeResponse finnGrunnlagListeResponse;

    InfotrygdBeregningsgrunnlagConsumerMock() {
        finnGrunnlagListeResponse = new FinnGrunnlagListeResponse();
    }

    @Override
    public FinnGrunnlagListeResponse finnBeregningsgrunnlagListe(FinnGrunnlagListeRequest finnGrunnlagListeRequest) {
        return finnGrunnlagListeResponse;
    }

    public InfotrygdBeregningsgrunnlagConsumerMock clean() {
        finnGrunnlagListeResponse.getForeldrepengerListe().clear();
        finnGrunnlagListeResponse.getEngangstoenadListe().clear();
        finnGrunnlagListeResponse.getPaaroerendeSykdomListe().clear();
        finnGrunnlagListeResponse.getSykepengerListe().clear();
        return this;
    }

    private Arbeidsforhold lagArbeidsforhold() {
        Inntektsperiode inntektsperiode = new Inntektsperiode();
        inntektsperiode.setValue("M");

        Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
        arbeidsforhold.setInntektForPerioden(new BigDecimal(6666.66));
        arbeidsforhold.setInntektsPeriode(inntektsperiode);
        arbeidsforhold.setOrgnr("1234567890");
        return arbeidsforhold;
    }

    public InfotrygdBeregningsgrunnlagConsumerMock lagPaaroerendeSykdom(int identdatoMinus) throws DatatypeConfigurationException {

        PaaroerendeSykdom paaroerendeSykdom = new PaaroerendeSykdom();
        Arbeidskategori arbeidskategori = new Arbeidskategori();
        arbeidskategori.setValue("01");

        Behandlingstema behandlingstema = new Behandlingstema();
        behandlingstema.setValue("OM");

        Periode periode = new Periode();
        periode.setFom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(9)));
        periode.setTom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(1)));

        paaroerendeSykdom.setFoedselsdatoPleietrengende(convertToXMLGregorianCalendar(LocalDate.now().minusDays(1000)));
        paaroerendeSykdom.setArbeidskategori(arbeidskategori);
        paaroerendeSykdom.setBehandlingstema(behandlingstema);
        paaroerendeSykdom.setIdentdato(convertToXMLGregorianCalendar(LocalDate.now().minusDays(identdatoMinus)));
        paaroerendeSykdom.setPeriode(periode);
        paaroerendeSykdom.getArbeidsforholdListe().add(lagArbeidsforhold());
        finnGrunnlagListeResponse.getPaaroerendeSykdomListe().add(paaroerendeSykdom);
        return this;
    }

    public InfotrygdBeregningsgrunnlagConsumerMock lagSykepenger(int identdatoMinus) throws DatatypeConfigurationException {
        Sykepenger sykepenger = new Sykepenger();
        Arbeidskategori arbeidskategori = new Arbeidskategori();
        arbeidskategori.setValue("01");

        Periode periode = new Periode();
        periode.setFom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(9)));
        periode.setTom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(1)));

        sykepenger.setInntektsgrunnlagProsent(100);
        sykepenger.setArbeidskategori(arbeidskategori);
        sykepenger.setIdentdato(convertToXMLGregorianCalendar(LocalDate.now().minusDays(identdatoMinus)));
        sykepenger.setPeriode(periode);
        sykepenger.getArbeidsforholdListe().add(lagArbeidsforhold());
        finnGrunnlagListeResponse.getSykepengerListe().add(sykepenger);

        return this;
    }

    public InfotrygdBeregningsgrunnlagConsumerMock lagEngangsstoenad(int identdatoMinus) throws DatatypeConfigurationException {
        Engangsstoenad engangsstoenad = new Engangsstoenad();
        Arbeidskategori arbeidskategori = new Arbeidskategori();
        arbeidskategori.setValue("01");

        Periode periode = new Periode();
        periode.setFom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(9)));
        periode.setTom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(1)));

        engangsstoenad.setIdentdato(convertToXMLGregorianCalendar(LocalDate.now().minusDays(identdatoMinus)));
        engangsstoenad.setPeriode(periode);
        finnGrunnlagListeResponse.getEngangstoenadListe().add(engangsstoenad);
        return this;
    }

    public InfotrygdBeregningsgrunnlagConsumerMock lagForeldrepenger(int identdatoMinus) throws DatatypeConfigurationException {
        Foreldrepenger foreldrepenger = new Foreldrepenger();
        Arbeidskategori arbeidskategori = new Arbeidskategori();
        arbeidskategori.setValue("01");

        Behandlingstema behandlingstema = new Behandlingstema();
        behandlingstema.setValue("FP");

        Periode periode = new Periode();
        periode.setFom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(9)));
        periode.setTom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(1)));

        foreldrepenger.setOpprinneligIdentdato(convertToXMLGregorianCalendar(LocalDate.now().minusDays(11)));
        foreldrepenger.setDekningsgrad(100);
        foreldrepenger.setGradering(100);
        foreldrepenger.setFoedselsdatoBarn(convertToXMLGregorianCalendar(LocalDate.now().minusDays(1011)));
        foreldrepenger.setArbeidskategori(arbeidskategori);
        foreldrepenger.setBehandlingstema(behandlingstema);
        foreldrepenger.setIdentdato(convertToXMLGregorianCalendar(LocalDate.now().minusDays(identdatoMinus)));
        foreldrepenger.setPeriode(periode);
        foreldrepenger.getArbeidsforholdListe().add(lagArbeidsforhold());
        finnGrunnlagListeResponse.getForeldrepengerListe().add(foreldrepenger);
        return this;
    }
}
