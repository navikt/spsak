package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil.convertToXMLGregorianCalendar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingslager.IntervallUtil;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag.MeldekortTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag.impl.MeldekortTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.InfotrygdBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.InfotrygdBeregningsgrunnlagTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdSakOgGrunnlag;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.impl.InfotrygdTjenesteImpl;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeUgyldigInput;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Behandlingstema;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Resultat;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Status;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.infotrygdsak.InfotrygdSakConsumer;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class InnhentingSamletTjenesteImplTest {

    private static final Saksnummer SAKSNUMMER = new Saksnummer("123456789");
    private static final String IDENT_NR = "12345678901";
    public static final AktørId AKTØR_ID = new AktørId("455");
    private static final String SAKSBEHANDLER_ID = "Prinsesse Leia";
    private static final String FORELDREPENGER = "FA";
    private static final String SYKEPENGER = "SP";
    private static final String FORELDREPENGER_FODSEL = "FØ";
    private static final String ENGANGSSTØNAD = "FE";
    private static final String ENSLIG_FORSØRGER = "EF";
    private static final String PÅRØRENDESYKDOM = "BS";

    private static final String TOM_BEHANDLINGSTEMA = "";
    private static final String VEDTAK_ID = "12";
    private static final String VEDTAK_ID_2 = "2";
    private static final String VEDTAK_ID_3 = "3";
    private final int identdatoMinus10 = 10;
    private final int identdatoMinus100 = 100;
    private final int identdatoMinus120 = 120;
    private final int identdatoMinus50 = 50;
    private final int identdatoMinus20 = 20;

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private InnhentingSamletTjenesteImpl innhentingSamletTjeneste;

    @Inject
    private InfotrygdBeregningsgrunnlagConsumerMock infotrygdBeregningsgrunnlagConsumer;
    @Inject
    private MeldekortUtbetalingsgrunnlagConsumerMock meldekortUtbetalingsgrunnlagConsumer;
    @Inject
    private GrunnlagRepositoryProvider repositoryProvider;

    @Inject
    private ResultatRepositoryProvider resultatProvider;

    @Mock
    private TpsTjeneste tpsTjeneste;
    @Mock
    private InfotrygdSakConsumer infotrygdSakConsumer;

    @Mock
    private KodeverkRepository kodeverkRepository;

    private InfotrygdTjeneste infotrygdTjeneste;
    private InfotrygdBeregningsgrunnlagTjeneste infotrygdBeregningsgrunnlagTjeneste;
    private MeldekortTjeneste meldekortTjeneste;

    private static final AktørId KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID = new AktørId("1");
    private static Map<AktørId, PersonIdent> FNR_VED_AKTØR_ID = new HashMap<>();

    private Behandling behandling;
    private Interval interval;
    private FinnSakListeResponse response;
    private InfotrygdHelper infotrygdHelper = new InfotrygdHelper();

    private Status åpenSak;
    private Status lukketSak;

    @Before
    public void before() throws FinnSakListeUgyldigInput, FinnSakListePersonIkkeFunnet, FinnSakListeSikkerhetsbegrensning {
        initMocks(this);
        kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());
        infotrygdBeregningsgrunnlagTjeneste = new InfotrygdBeregningsgrunnlagTjenesteImpl(infotrygdBeregningsgrunnlagConsumer,kodeverkRepository);
        infotrygdTjeneste = new InfotrygdTjenesteImpl(infotrygdSakConsumer, kodeverkRepository);
        meldekortTjeneste = new MeldekortTjenesteImpl(meldekortUtbetalingsgrunnlagConsumer);
        innhentingSamletTjeneste = new InnhentingSamletTjenesteImpl(null,tpsTjeneste,null,
            infotrygdTjeneste,infotrygdBeregningsgrunnlagTjeneste,meldekortTjeneste);

        ScenarioMorSøkerForeldrepenger førstegangsscenario;
        førstegangsscenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør()
            .medBruker(KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID, NavBrukerKjønn.KVINNE);

        behandling = førstegangsscenario.lagre(repositoryProvider, resultatProvider);
        LocalDate fomDato = LocalDate.now().minusDays(120);
        LocalDate tomDato = LocalDate.now();
        interval = IntervallUtil.byggIntervall(fomDato, tomDato);

        final PersonIdent t = new PersonIdent(IDENT_NR);
        when(tpsTjeneste.hentFnrForAktør(Mockito.any(AktørId.class))).thenReturn(t);

        response = new FinnSakListeResponse();
        when(infotrygdSakConsumer.finnSakListe(any())).thenReturn(response);

        åpenSak = new Status();
        åpenSak.setValue(RelatertYtelseStatus.UNDER_BEHANDLING.getKode());

        lukketSak = new Status();
        lukketSak.setValue(RelatertYtelseStatus.AVSLU.getKode());
    }

    @Test
    public void getArenaMeldekortGrunnlag() throws DatatypeConfigurationException {
        meldekortUtbetalingsgrunnlagConsumer.clean()
            .lagSakVedtak(SAKSNUMMER);

        List<MeldekortUtbetalingsgrunnlagSak> saker = meldekortTjeneste.hentMeldekortListe(AKTØR_ID, LocalDate.now().minusMonths(10), LocalDate.now());
        assertThat(saker.size()).isEqualTo(1);
        assertThat(saker.get(0).getMeldekortene().size()).isEqualTo(1);
    }

    @Test
    public void getArenaMeldekortGrunnlagUtenVedtak() throws DatatypeConfigurationException {
        meldekortUtbetalingsgrunnlagConsumer.clean()
            .lagSakUtenVedtak(SAKSNUMMER);

        List<MeldekortUtbetalingsgrunnlagSak> saker = meldekortTjeneste.hentMeldekortListe(AKTØR_ID, LocalDate.now().minusMonths(10), LocalDate.now());
        assertThat(saker.size()).isEqualTo(1);
        assertThat(saker.get(0).getMeldekortene().isEmpty()).isTrue();
    }

    @Test
    public void getSammenstiltSakOgGrunnlagMedGrunnlag() throws DatatypeConfigurationException {
        infotrygdBeregningsgrunnlagConsumer.clean()
            .lagForeldrepenger(identdatoMinus10)
            .lagForeldrepenger(identdatoMinus20)
            .lagEngangsstoenad(identdatoMinus50)
            .lagPaaroerendeSykdom(identdatoMinus120)
            .lagSykepenger(identdatoMinus100);

        infotrygdHelper.clean().opprettInfotrygdVedtak(VEDTAK_ID, FORELDREPENGER_FODSEL,FORELDREPENGER, identdatoMinus10, lukketSak)
            .opprettInfotrygdVedtak(VEDTAK_ID,FORELDREPENGER_FODSEL, FORELDREPENGER, identdatoMinus20)
            .opprettInfotrygdVedtak(VEDTAK_ID,ENGANGSSTØNAD,FORELDREPENGER, identdatoMinus50, lukketSak)
            .opprettInfotrygdVedtak(VEDTAK_ID, TOM_BEHANDLINGSTEMA, SYKEPENGER, identdatoMinus100, åpenSak)
            .opprettInfotrygdVedtak(VEDTAK_ID, TOM_BEHANDLINGSTEMA, PÅRØRENDESYKDOM, identdatoMinus120, åpenSak);

        List<InfotrygdSakOgGrunnlag> infoTrue = innhentingSamletTjeneste.getSammenstiltSakOgGrunnlag(behandling, AKTØR_ID, interval, true);
        assertThat(infoTrue).hasAtLeastOneElementOfType(InfotrygdSakOgGrunnlag.class);
        assertThat(infoTrue.stream().filter(it -> it.getGrunnlag().isPresent()).collect(Collectors.toList())).hasSize(5);
    }

    @Test
    public void getSammenstiltSakOgGrunnlagUtenGrunnlag() throws DatatypeConfigurationException {
        infotrygdBeregningsgrunnlagConsumer.clean()
            .lagForeldrepenger(identdatoMinus10)
            .lagEngangsstoenad(identdatoMinus50)
            .lagPaaroerendeSykdom(identdatoMinus100);

        infotrygdHelper.clean().opprettInfotrygdVedtak(VEDTAK_ID, FORELDREPENGER_FODSEL,FORELDREPENGER, identdatoMinus10, åpenSak)
            .opprettInfotrygdVedtak(VEDTAK_ID,ENGANGSSTØNAD,FORELDREPENGER, identdatoMinus100, åpenSak)
            .opprettInfotrygdVedtak(VEDTAK_ID, TOM_BEHANDLINGSTEMA, ENSLIG_FORSØRGER, identdatoMinus50, åpenSak);

        List<InfotrygdSakOgGrunnlag> infoFalse = innhentingSamletTjeneste.getSammenstiltSakOgGrunnlag(behandling, AKTØR_ID, interval, false);
        assertThat(infoFalse.size()).isGreaterThan(0);
        assertThat(infoFalse.stream().filter(it -> !it.getGrunnlag().isPresent()).collect(Collectors.toList())).hasSameSizeAs(infoFalse);
    }

    @Test
    public void getSammenstiltSakOgGrunnlagHvorDetErMerGrunnlag() throws DatatypeConfigurationException {
        infotrygdBeregningsgrunnlagConsumer.clean()
            .lagForeldrepenger(identdatoMinus10)
            .lagEngangsstoenad(identdatoMinus50)
            .lagPaaroerendeSykdom(identdatoMinus100);


        infotrygdHelper.clean().opprettInfotrygdVedtak(VEDTAK_ID, FORELDREPENGER_FODSEL,FORELDREPENGER, identdatoMinus10, åpenSak);

        List<InfotrygdSakOgGrunnlag> infoTrue = innhentingSamletTjeneste.getSammenstiltSakOgGrunnlag(behandling, AKTØR_ID, interval, true);
        assertThat(infoTrue.size()).isEqualTo(1);
        assertThat(infoTrue.get(0).getGrunnlag().isPresent()).isTrue();
    }

    @Test
    public void getSammenstiltSakOgGrunnlagMedFlereSakerEnnGrunnlag() throws DatatypeConfigurationException {
        infotrygdBeregningsgrunnlagConsumer.clean()
            .lagForeldrepenger(identdatoMinus10);

        infotrygdHelper.clean().opprettInfotrygdVedtak(VEDTAK_ID, FORELDREPENGER_FODSEL,FORELDREPENGER, identdatoMinus10, åpenSak)
            .opprettInfotrygdVedtak(VEDTAK_ID_2,ENGANGSSTØNAD,FORELDREPENGER, identdatoMinus100, lukketSak)
            .opprettInfotrygdVedtak(VEDTAK_ID_3, TOM_BEHANDLINGSTEMA, ENSLIG_FORSØRGER, identdatoMinus50, åpenSak);

        List<InfotrygdSakOgGrunnlag> infoTrue = innhentingSamletTjeneste.getSammenstiltSakOgGrunnlag(behandling, AKTØR_ID, interval, true);
        assertThat(infoTrue).hasSize(2);
        assertThat(infoTrue.stream().filter(it -> it.getGrunnlag().isPresent())).hasSize(1);
        assertThat(infoTrue.stream().filter(it -> !it.getGrunnlag().isPresent())).hasSize(1);
    }

    @Test
    public void sjekkAtBehandlinsTemaErSatt() throws DatatypeConfigurationException {
        infotrygdBeregningsgrunnlagConsumer.clean()
            .lagForeldrepenger(identdatoMinus10)
            .lagForeldrepenger(identdatoMinus100)
            .lagForeldrepenger(identdatoMinus120)
            .lagForeldrepenger(identdatoMinus50);

        infotrygdHelper.clean().opprettInfotrygdVedtak(VEDTAK_ID, FORELDREPENGER_FODSEL,FORELDREPENGER, identdatoMinus10, åpenSak)
            .opprettInfotrygdVedtak(VEDTAK_ID, FORELDREPENGER_FODSEL, FORELDREPENGER, identdatoMinus120, åpenSak)
            .opprettInfotrygdVedtak(VEDTAK_ID, FORELDREPENGER_FODSEL, FORELDREPENGER, identdatoMinus50)
            .opprettInfotrygdVedtak(VEDTAK_ID, FORELDREPENGER_FODSEL, FORELDREPENGER, identdatoMinus100);

        List<InfotrygdSakOgGrunnlag> infoTrue = innhentingSamletTjeneste.getSammenstiltSakOgGrunnlag(behandling, AKTØR_ID, interval, true);

        assertThat(infoTrue.stream().filter(x -> x.getSak().getTemaUnderkategori() != null)).hasSize(4);
    }

    @Test
    public void sjekkIndentdatoPåVedtakOgGrunnlag() throws DatatypeConfigurationException {
        infotrygdBeregningsgrunnlagConsumer.clean()
                .lagForeldrepenger(identdatoMinus10)
                .lagForeldrepenger(identdatoMinus100)
                .lagForeldrepenger(identdatoMinus120)
                .lagForeldrepenger(identdatoMinus50);

        infotrygdHelper.clean().opprettInfotrygdVedtak(VEDTAK_ID, FORELDREPENGER_FODSEL,FORELDREPENGER, identdatoMinus10, åpenSak)
                .opprettInfotrygdVedtak(VEDTAK_ID, FORELDREPENGER_FODSEL, FORELDREPENGER, identdatoMinus120, åpenSak)
                .opprettInfotrygdVedtak(VEDTAK_ID, FORELDREPENGER_FODSEL, FORELDREPENGER, identdatoMinus50)
                .opprettInfotrygdVedtak(VEDTAK_ID, FORELDREPENGER_FODSEL, FORELDREPENGER, identdatoMinus100);

        List<InfotrygdSakOgGrunnlag> infoTrue = innhentingSamletTjeneste.getSammenstiltSakOgGrunnlag(behandling, AKTØR_ID, interval, true);

        Map<String, List<InfotrygdSakOgGrunnlag>> map = infoTrue.stream().collect(Collectors.groupingBy(x -> x.getSak().getFagsystemUnderkategori().getKode()));
        assertThat(map.values()).hasSize(1);

        assertThat(map.get("INFOTRYGD_VEDTAK").stream().filter(x -> x.getGrunnlag().isPresent())).hasSize(4);
        assertThat(map.get("INFOTRYGD_VEDTAK").stream()
            .filter(x -> x.getSak().getIverksatt().toString()
                .equals(x.getGrunnlag().orElseGet(null).getIdentdato().toString()))).hasSize(4);
    }

    public Optional<PersonIdent> hentIdentForAktørId(AktørId aktørId) {
        return Optional.ofNullable(FNR_VED_AKTØR_ID.get(aktørId));
    }

    @Test
    public void getTomSakOgGrunnlag(){
        infotrygdBeregningsgrunnlagConsumer.clean();
        infotrygdHelper.clean();

        List<InfotrygdSakOgGrunnlag> infoTom = innhentingSamletTjeneste.getSammenstiltSakOgGrunnlag(behandling, AKTØR_ID, interval, true);
        assertThat(infoTom.size()).isEqualTo(0);
    }

    class InfotrygdHelper{
        private final LocalDate DATE_20170823 = LocalDate.of(2017, 8, 23);
        private final LocalDate DATE_20170401 = LocalDate.of(2017, 4, 1);

        InfotrygdHelper opprettInfotrygdVedtak(String sakId, String behandlingstema, String tema, int iverksattDatoMinus, Status status) throws DatatypeConfigurationException {
            no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak sak = new no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak();
            sak.setSakId(sakId);
            sak.setStatus(status);
            sak.setRegistrert(DateUtil.convertToXMLGregorianCalendar(DATE_20170823));
            leggTilTemaTilInfotrygdSak(tema, sak);
            leggTilbehandlinstemaTilInfortrygdssak(behandlingstema, sak);
            sak.setSaksbehandlerId(SAKSBEHANDLER_ID);
            sak.setVedtatt(DateUtil.convertToXMLGregorianCalendar(DATE_20170401));
            sak.setIverksatt(convertToXMLGregorianCalendar(LocalDate.now().minusDays(iverksattDatoMinus)));
            response.getVedtakListe().add(sak);
            return this;
        }

        private void leggTilbehandlinstemaTilInfortrygdssak(String behandlingstema, InfotrygdSak sak) {
            if(behandlingstema != null && !behandlingstema.isEmpty()) {
                sak.setBehandlingstema(lagBehandlingstema(behandlingstema));
            }
        }

        private void leggTilTemaTilInfotrygdSak(String tema, InfotrygdSak sak) {
            if(tema != null && !tema.isEmpty()) {
                sak.setTema(lagTema(tema));
            }
        }

        InfotrygdHelper opprettInfotrygdVedtak(String sakId, String behandlingstema, String tema, int iverksattDatoMinus) throws DatatypeConfigurationException {
            no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak vedtak = new no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak();
            vedtak.setSakId(sakId);
            vedtak.setRegistrert(DateUtil.convertToXMLGregorianCalendar(DATE_20170823));
            leggTilbehandlinstemaTilInfortrygdssak(behandlingstema,vedtak);
            leggTilTemaTilInfotrygdSak(tema,vedtak);
            vedtak.setResultat(lagResultat());
            vedtak.setSaksbehandlerId(SAKSBEHANDLER_ID);
            vedtak.setVedtatt(DateUtil.convertToXMLGregorianCalendar(DATE_20170401));
            vedtak.setIverksatt(convertToXMLGregorianCalendar(LocalDate.now().minusDays(iverksattDatoMinus)));
            vedtak.setOpphoerFom(DateUtil.convertToXMLGregorianCalendar(LocalDate.now().minusDays(5)));
            response.getVedtakListe().add(vedtak);
            return this;
        }

        private Resultat lagResultat() {
            final Resultat resultat = new Resultat();
            resultat.setValue("Vedtatt");
            return resultat;
        }

        private Behandlingstema lagBehandlingstema(String behandlingstemaKode) {
            Behandlingstema behandlingstema = new Behandlingstema();
            behandlingstema.setValue(behandlingstemaKode);
            behandlingstema.setKodeRef(behandlingstemaKode);
            behandlingstema.setTermnavn(behandlingstemaKode);
            return behandlingstema;
        }

        private Tema lagTema(String temaKode) {
            Tema tema = new Tema();
            tema.setValue(temaKode);
            tema.setKodeRef(temaKode);
            tema.setTermnavn(temaKode);
            return tema;
        }

        InfotrygdHelper clean(){
            response.getSakListe().clear();
            response.getVedtakListe().clear();
            return this;
        }
    }

}
