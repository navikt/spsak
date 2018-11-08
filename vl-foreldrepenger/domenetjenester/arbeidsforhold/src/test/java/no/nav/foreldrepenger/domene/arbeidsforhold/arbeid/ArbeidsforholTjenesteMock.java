package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.impl.ArbeidsforholdTjenesteImpl;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.AnsettelsesPeriode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforholdstyper;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.HentArbeidsforholdHistorikkResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsforhold.ArbeidsforholdConsumer;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class ArbeidsforholTjenesteMock {

    private static final String ORGNR = "973093681";
    private static final LocalDate PERIODE_FOM = LocalDate.now().minusYears(3L);
    private static final String KODEVERKS_REF = "A_ORDNINGEN";
    private final ArbeidsforholdTjeneste arbeidsforholdTjeneste;

    public ArbeidsforholTjenesteMock() throws Exception {
        FinnArbeidsforholdPrArbeidstakerResponse response = opprettResponse();

        ArbeidsforholdConsumer arbeidsforholdConsumer = mock(ArbeidsforholdConsumer.class);
        when(arbeidsforholdConsumer.finnArbeidsforholdPrArbeidstaker(any())).thenReturn(response);
        HentArbeidsforholdHistorikkResponse response1 = new HentArbeidsforholdHistorikkResponse();
        response1.setArbeidsforhold(response.getArbeidsforhold().get(0));
        when(arbeidsforholdConsumer.hentArbeidsforholdHistorikk(any())).thenReturn(response1);
        this.arbeidsforholdTjeneste = new ArbeidsforholdTjenesteImpl(arbeidsforholdConsumer, mock(TpsTjeneste.class));
    }

    public ArbeidsforholdTjeneste getMock() {
        return arbeidsforholdTjeneste;
    }

    private FinnArbeidsforholdPrArbeidstakerResponse opprettResponse() throws Exception {
        FinnArbeidsforholdPrArbeidstakerResponse response = new FinnArbeidsforholdPrArbeidstakerResponse();
        no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold = new no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold();

        no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon arbeidsgiver = new no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon();

        arbeidsgiver.setOrgnummer(ORGNR);
        arbeidsforhold.setArbeidsgiver(arbeidsgiver);

        Arbeidsforholdstyper arbeidsforholdstyper = new Arbeidsforholdstyper();
        arbeidsforholdstyper.setKodeverksRef(KODEVERKS_REF);
        arbeidsforhold.setArbeidsforholdstype(arbeidsforholdstyper);
        arbeidsforhold.setArbeidsforholdID("1");

        AnsettelsesPeriode ansettelsesPeriode = new AnsettelsesPeriode();
        Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();

        gyldighetsperiode.setFom(DateUtil.convertToXMLGregorianCalendar(PERIODE_FOM));
        ansettelsesPeriode.setPeriode(gyldighetsperiode);
        arbeidsforhold.setAnsettelsesPeriode(ansettelsesPeriode);

        response.getArbeidsforhold().add(arbeidsforhold);
        return response;
    }
}
