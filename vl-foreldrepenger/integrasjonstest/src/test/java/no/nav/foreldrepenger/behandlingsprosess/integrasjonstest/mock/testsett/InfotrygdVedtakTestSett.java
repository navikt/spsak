package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett;

import static no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil.convertToXMLGregorianCalendar;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Arbeidskategori;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Engangsstoenad;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Foreldrepenger;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Inntektsperiode;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.PaaroerendeSykdom;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Sykepenger;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Vedtak;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeResponse;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Behandlingstema;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeResponse;

public class InfotrygdVedtakTestSett {

    private static String infotrygdSakId = "dummyInfotrygdSakId";

    public static String ORGNR = "973093681";

    private static final Map<String, Long> RESPONSE_MAP = new HashMap<>();
    private static final Map<String, Boolean> RESPONSE_FP_MAP = new HashMap<>();

    private static final long OFFSET_FOM = 13;
    private static final long OFFSET_TOM = 4;

    public static void nullstill() {
        RESPONSE_MAP.clear();
        RESPONSE_FP_MAP.clear();
    }

    public static FinnSakListeResponse finnResponse(String ident) {
        if (RESPONSE_MAP.get(ident) != null) {
            return lagInfotr(RESPONSE_MAP.get(ident), RESPONSE_FP_MAP.get(ident));
        }
        return new FinnSakListeResponse();
    }

    public static FinnGrunnlagListeResponse finnIBGResponse(String ident) {
        if (RESPONSE_MAP.get(ident) != null) {
            return lagInfotrBG(RESPONSE_MAP.get(ident), RESPONSE_FP_MAP.get(ident));
        }
        return new FinnGrunnlagListeResponse();
    }

    public static void infotrygdsakStandard(String ident, Long offset) {
        RESPONSE_MAP.put(ident, offset);
        RESPONSE_FP_MAP.put(ident, true);
    }

    public static void infotrygdsakStandardUtenFP(String ident, Long offset) {
        RESPONSE_MAP.put(ident, offset);
        RESPONSE_FP_MAP.put(ident, false);
    }

    public static FinnSakListeResponse lagInfotr(long offset, boolean medFP) {
        FinnSakListeResponse finnSakListeResponse = new FinnSakListeResponse();
        if (medFP) {
            finnSakListeResponse.getVedtakListe().add(lagITSakVedtak("FA", "FØ", LocalDate.now().minusDays(offset).minusDays(OFFSET_FOM), LocalDate.now().minusDays(offset + OFFSET_TOM)));
            finnSakListeResponse.getVedtakListe().add(lagITSakVedtak("FA", "FE", LocalDate.now().minusDays(offset).minusDays(OFFSET_FOM), null));
        }
        finnSakListeResponse.getVedtakListe().add(lagITSakVedtak("SP", "SP", LocalDate.now().minusDays(offset).minusDays(OFFSET_FOM), null));
        finnSakListeResponse.getVedtakListe().add(lagITSakVedtak("BS", "OM", LocalDate.now().minusDays(offset).minusDays(30), LocalDate.now().minusDays(offset).plusYears(5)));
        return finnSakListeResponse;
    }

    private static InfotrygdVedtak lagITSakVedtak(String tema, String bt, LocalDate fom, LocalDate tom) {
        InfotrygdVedtak infotrygdSak = new InfotrygdVedtak();
        infotrygdSak.setBehandlingstema(lagBehandlingstema(bt));
        infotrygdSak.setTema(lagTema(tema));
        infotrygdSak.setSakId(infotrygdSakId+tema);
        try {
            infotrygdSak.setIverksatt(convertToXMLGregorianCalendar(fom));
            infotrygdSak.setRegistrert(convertToXMLGregorianCalendar(fom));
            if (tom != null) {
                LocalDate opphørsdato = DayOfWeek.from(tom).getValue() > DayOfWeek.THURSDAY.getValue() ? tom.plusDays(1L + DayOfWeek.SUNDAY.getValue() - DayOfWeek.from(tom).getValue()) : tom;
                infotrygdSak.setOpphoerFom(convertToXMLGregorianCalendar(opphørsdato));
            }
        } catch (DatatypeConfigurationException ignore) {
        }
        return infotrygdSak;
    }

    private static Behandlingstema lagBehandlingstema(String bt) {
        Behandlingstema behandlingstema = new Behandlingstema();
        behandlingstema.setValue(bt);
        behandlingstema.setKodeRef(bt);
        behandlingstema.setTermnavn(bt);
        return behandlingstema;
    }

    private static Tema lagTema(String t) {
        Tema tema = new Tema();
        tema.setValue(t);
        tema.setKodeRef(t);
        tema.setTermnavn(t);
        return tema;
    }

    public static FinnGrunnlagListeResponse lagInfotrBG(long offset, boolean medFP) {
        FinnGrunnlagListeResponse finnGrunnlagListeResponse = new FinnGrunnlagListeResponse();
        finnGrunnlagListeResponse.getPaaroerendeSykdomListe().add(lagPaaroerendeSykdom(offset));
        finnGrunnlagListeResponse.getSykepengerListe().add(lagSykepenger(offset));
        if (medFP) {
            finnGrunnlagListeResponse.getEngangstoenadListe().add(lagEngangsstoenad(offset));
            finnGrunnlagListeResponse.getForeldrepengerListe().add(lagForeldrepenger(offset));
        }
        return finnGrunnlagListeResponse;
    }

    private static Arbeidsforhold lagArbeidsforhold(){
        Inntektsperiode inntektsperiode = new Inntektsperiode();
        inntektsperiode.setValue("M");

        Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
        arbeidsforhold.setInntektForPerioden(new BigDecimal(6666.66));
        arbeidsforhold.setInntektsPeriode(inntektsperiode);
        arbeidsforhold.setOrgnr(ORGNR);
        return arbeidsforhold;
    }

    private static PaaroerendeSykdom lagPaaroerendeSykdom(long offset){
        PaaroerendeSykdom paaroerendeSykdom = new PaaroerendeSykdom();
        try {
            Arbeidskategori arbeidskategori = new Arbeidskategori();
            arbeidskategori.setValue("01");

            no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Behandlingstema behandlingstema = new no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Behandlingstema();
            behandlingstema.setValue("OM");

            Periode periode = new Periode();
            periode.setFom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset).minusDays(30)));
            periode.setTom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset + OFFSET_TOM)));
            paaroerendeSykdom.getVedtakListe().addAll(lagVedtakListeTreVedtakToSammenhengende(LocalDate.now().minusDays(offset).minusDays(30), LocalDate.now().minusDays(offset).minusDays(3)));
            paaroerendeSykdom.setFoedselsdatoPleietrengende(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset).minusDays(1000)));
            paaroerendeSykdom.setArbeidskategori(arbeidskategori);
            paaroerendeSykdom.setBehandlingstema(behandlingstema);
            paaroerendeSykdom.setIdentdato(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset).minusDays(30)));
            paaroerendeSykdom.setPeriode( periode );
            paaroerendeSykdom.getArbeidsforholdListe().add(lagArbeidsforhold());

        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return paaroerendeSykdom;
    }

    private static Sykepenger lagSykepenger(long offset){
        Sykepenger sykepenger = new Sykepenger();
        try{
            Arbeidskategori arbeidskategori = new Arbeidskategori();
            arbeidskategori.setValue("01");

            Periode periode = new Periode();
            periode.setFom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset + OFFSET_FOM)));
            periode.setTom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset + OFFSET_TOM)));
            no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Behandlingstema behandlingstema = new no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Behandlingstema();
            behandlingstema.setValue("SP");
            sykepenger.setBehandlingstema(behandlingstema);
            sykepenger.getVedtakListe().addAll(lagVedtakListeToSammenhengendeVedtak(LocalDate.now().minusDays(offset + OFFSET_FOM), LocalDate.now().minusDays(offset + OFFSET_TOM)));
            sykepenger.setInntektsgrunnlagProsent(100);
            sykepenger.setArbeidskategori(arbeidskategori);
            sykepenger.setIdentdato(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset + OFFSET_FOM)));
            sykepenger.setPeriode( periode );
            sykepenger.getArbeidsforholdListe().add(lagArbeidsforhold());
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }

        return sykepenger;
    }

    private static Engangsstoenad lagEngangsstoenad(long offset){
        Engangsstoenad engangsstoenad = new Engangsstoenad();
        try{
            Arbeidskategori arbeidskategori = new Arbeidskategori();
            arbeidskategori.setValue("01");

            Periode periode = new Periode();
            periode.setFom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset + OFFSET_FOM)));
            periode.setTom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset + OFFSET_FOM)));
            no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Behandlingstema behandlingstema = new no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Behandlingstema();
            behandlingstema.setValue("FE");
            engangsstoenad.setIdentdato(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset + OFFSET_FOM)));
            engangsstoenad.setPeriode( periode );
            engangsstoenad.setBehandlingstema(behandlingstema);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return engangsstoenad;
    }

    private static Foreldrepenger lagForeldrepenger(long offset){
        Foreldrepenger foreldrepenger = new Foreldrepenger();
        try {
            Arbeidskategori arbeidskategori = new Arbeidskategori();
            arbeidskategori.setValue("01");

            no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Behandlingstema behandlingstema = new no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Behandlingstema();
            behandlingstema.setValue("FP");

            Periode periode = new Periode();
            periode.setFom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset + OFFSET_FOM)));
            periode.setTom(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset + OFFSET_TOM)));
            foreldrepenger.getVedtakListe().addAll(lagVedtakListeEttVedtak(LocalDate.now().minusDays(offset + OFFSET_FOM), LocalDate.now().minusDays(offset + OFFSET_TOM)));
            foreldrepenger.setOpprinneligIdentdato(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset + OFFSET_FOM + 2)));
            foreldrepenger.setDekningsgrad(100);
            foreldrepenger.setGradering(100);
            foreldrepenger.setFoedselsdatoBarn(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset).minusDays(1011)));
            foreldrepenger.setArbeidskategori(arbeidskategori);
            foreldrepenger.setBehandlingstema(behandlingstema);
            foreldrepenger.setIdentdato(convertToXMLGregorianCalendar(LocalDate.now().minusDays(offset + OFFSET_FOM)));
            foreldrepenger.setPeriode( periode );
            foreldrepenger.getArbeidsforholdListe().add(lagArbeidsforhold());

        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return foreldrepenger;
    }

    private static List<Vedtak> lagVedtakListeEttVedtak(LocalDate fom, LocalDate tom) {
        List<Vedtak> vedtakene = new ArrayList<>();
        vedtakene.add(lagVedtak(fom, tom));
        return vedtakene;
    }

    private static List<Vedtak> lagVedtakListeToSammenhengendeVedtak(LocalDate fom, LocalDate tom) {
        List<Vedtak> vedtakene = new ArrayList<>();
        vedtakene.add(lagVedtak(fom, fom.plusDays(2)));
        vedtakene.add(lagVedtak(fom.plusDays(3), tom));
        return vedtakene;
    }

    private static List<Vedtak> lagVedtakListeTreVedtakToSammenhengende(LocalDate fom, LocalDate tom) {
        List<Vedtak> vedtakene = new ArrayList<>();
        vedtakene.add(lagVedtak(fom, fom.plusDays(2)));
        vedtakene.add(lagVedtak(fom.plusDays(3), fom.plusDays(7)));
        vedtakene.add(lagVedtak(tom.minusDays(4), tom));
        return vedtakene;
    }

    private static Vedtak lagVedtak(LocalDate fom, LocalDate tom) {
        Vedtak vedtak = new Vedtak();
        if (DayOfWeek.from(tom).getValue() > DayOfWeek.FRIDAY.getValue()) {
            tom = tom.minusDays((long)DayOfWeek.from(tom).getValue() - DayOfWeek.FRIDAY.getValue());
            if (!tom.isAfter(fom)) {
                fom = tom;
            }
        }
        try {
            Periode periode = new Periode();
            periode.setFom(convertToXMLGregorianCalendar(fom));
            periode.setTom(convertToXMLGregorianCalendar(tom));
            vedtak.setAnvistPeriode(periode);
            vedtak.setUtbetalingsgrad(100);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return vedtak;
    }

}
