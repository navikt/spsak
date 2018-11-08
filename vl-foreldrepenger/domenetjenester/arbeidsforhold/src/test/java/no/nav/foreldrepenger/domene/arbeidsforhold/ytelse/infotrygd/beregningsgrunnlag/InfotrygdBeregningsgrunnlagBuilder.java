package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag;

import static no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil.convertToXMLGregorianCalendar;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Engangsstoenad;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Foreldrepenger;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Grunnlag;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Inntektsperiode;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.PaaroerendeSykdom;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.PeriodeYtelse;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Sykepenger;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Vedtak;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeResponse;

public class InfotrygdBeregningsgrunnlagBuilder {


    private FinnGrunnlagListeResponse finnGrunnlagListeResponse;
    InfotrygdBeregningsgrunnlagBuilder(){
        finnGrunnlagListeResponse = new FinnGrunnlagListeResponse();
    }

    public InfotrygdBeregningsgrunnlagBuilder medSykepenger(Sykepenger sykepenger){
        finnGrunnlagListeResponse.getSykepengerListe().add(sykepenger);
        return this;
    }

    public InfotrygdBeregningsgrunnlagBuilder medPaaroerendeSykdom(PaaroerendeSykdom paaroerendeSykdom){
        finnGrunnlagListeResponse.getPaaroerendeSykdomListe().add(paaroerendeSykdom);
        return this;
    }

    public InfotrygdBeregningsgrunnlagBuilder medEngangstoenad(Engangsstoenad engangsstoenad){
        finnGrunnlagListeResponse.getEngangstoenadListe().add(engangsstoenad);
        return this;
    }

    public InfotrygdBeregningsgrunnlagBuilder medForeldrepenger(Foreldrepenger foreldrepenger){
        finnGrunnlagListeResponse.getForeldrepengerListe().add(foreldrepenger);
        return this;
    }

    public FinnGrunnlagListeResponse build(){
        return finnGrunnlagListeResponse;
    }

    public static class VedtakBuilder{
        private Vedtak vedtak;

        VedtakBuilder(){
            vedtak = new Vedtak();
        }

        VedtakBuilder medPeriode(LocalDate fra, LocalDate til) throws DatatypeConfigurationException {
            Periode periode = new Periode();
            periode.setFom(convertToXMLGregorianCalendar(fra));
            periode.setTom(convertToXMLGregorianCalendar(til));
            vedtak.setAnvistPeriode(periode);
            return this;
        }

        VedtakBuilder medUtbetalingsgrad(int verdi){
            vedtak.setUtbetalingsgrad(verdi);
            return this;
        }

        Vedtak build(){
            return vedtak;
        }
    }


    public static class ArbeidsforholdBuilder{
        private Arbeidsforhold arbeidsforhold;

        ArbeidsforholdBuilder(){
            arbeidsforhold = new Arbeidsforhold();
        }

        ArbeidsforholdBuilder medInntektperiodeType(String inntektperiodeType){
            Inntektsperiode inntektsperiode = new Inntektsperiode();
            inntektsperiode.setValue(inntektperiodeType);
            arbeidsforhold.setInntektsPeriode(inntektsperiode);
            return this;
        }

        ArbeidsforholdBuilder medInntekt(BigDecimal inntekt){
            arbeidsforhold.setInntektForPerioden(inntekt);
            return this;
        }

        ArbeidsforholdBuilder medOrgnr(String orgnr){
            arbeidsforhold.setOrgnr(orgnr);
            return this;
        }

        Arbeidsforhold build(){
            return arbeidsforhold;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    abstract static class GrunnlagBuilder<T extends GrunnlagBuilder, V extends Grunnlag>{
        protected V grunnlag;

        GrunnlagBuilder(V grunnlag){
            this.grunnlag = grunnlag;
        }

        V build(){
            return grunnlag;
        }

        T medIdentdato(LocalDate localDate) throws DatatypeConfigurationException {
            grunnlag.setIdentdato(convertToXMLGregorianCalendar(localDate));
            return (T) this; //$NON-NLS-1$ //NOSONAR
        }

        T medPeriode(LocalDate fra, LocalDate til) throws DatatypeConfigurationException {
            Periode periode = new Periode();
            periode.setFom(convertToXMLGregorianCalendar(fra));
            periode.setTom(convertToXMLGregorianCalendar(til));
            grunnlag.setPeriode(periode);
            return (T) this; //$NON-NLS-1$ //NOSONAR
        }

        T medVedtak(Vedtak vedtak) {
            grunnlag.getVedtakListe().add(vedtak);
            return (T) this; //$NON-NLS-1$ //NOSONAR
        }
    }

    abstract static class PeriodeYtelseBuilder<T extends PeriodeYtelseBuilder<T, V>, V extends PeriodeYtelse> extends GrunnlagBuilder<T, V>
    {

        PeriodeYtelseBuilder(V periodeYtelse){
            super(periodeYtelse);
        }

        @SuppressWarnings("unchecked")
        T medArbeidsforhold(Arbeidsforhold arbeidsforhold){
            grunnlag.getArbeidsforholdListe().add(arbeidsforhold);
            return (T) this;
        }
    }

    static class EngangsstoenadBuilder extends GrunnlagBuilder<EngangsstoenadBuilder, Engangsstoenad> {
        EngangsstoenadBuilder(){
            super(new Engangsstoenad());
        }
    }

    public static class PaaroerendeSykdomBuilder extends PeriodeYtelseBuilder<PaaroerendeSykdomBuilder, PaaroerendeSykdom> {

        PaaroerendeSykdomBuilder(){
            super(new PaaroerendeSykdom());
        }
        PaaroerendeSykdomBuilder medFoedselsdatoPleietrengende(LocalDate dato) throws DatatypeConfigurationException {
            grunnlag.setFoedselsdatoPleietrengende(convertToXMLGregorianCalendar(dato));
            return this;
        }
    }

    public static class SykepengerBuilder extends PeriodeYtelseBuilder<SykepengerBuilder, Sykepenger> {
        SykepengerBuilder(){
            super(new Sykepenger());
        }

        SykepengerBuilder medInntektsgrunnlagProsent(int prosent){
            grunnlag.setInntektsgrunnlagProsent(prosent);
            return this;
        }
    }

    public static class ForeldrePengerBuilder extends PeriodeYtelseBuilder<ForeldrePengerBuilder, Foreldrepenger> {
        ForeldrePengerBuilder(){
            super(new Foreldrepenger());
        }

        ForeldrePengerBuilder medOpprinneligIdentDato(LocalDate dato) throws DatatypeConfigurationException {
            grunnlag.setOpprinneligIdentdato(convertToXMLGregorianCalendar(dato));
            return this;
        }

        ForeldrePengerBuilder medDekningsgrad(int prosent) {
            grunnlag.setDekningsgrad(prosent);
            return this;
        }

        ForeldrePengerBuilder medGradering(int verdi) {
            grunnlag.setGradering(verdi);
            return this;
        }

        ForeldrePengerBuilder medFoedselsdatoBarn(LocalDate dato) throws DatatypeConfigurationException {
            grunnlag.setFoedselsdatoBarn(convertToXMLGregorianCalendar(dato));
            return this;
        }
    }
}
