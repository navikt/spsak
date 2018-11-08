package no.nav.foreldrepenger.dokumentbestiller;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteTestUtil.SAKSPART_ID;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteTestUtil.SAKSPART_NAVN;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteTestUtil.mockHentBrukerForAktør;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.w3c.dom.Document;

import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalRestriksjon;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametereImpl;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentBehandlingsresultatMapper;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentTypeDtoMapper;
import no.nav.foreldrepenger.dokumentbestiller.brev.DokumentToBrevDataMapper;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.impl.FamilieHendelseTjenesteImpl;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.uttak.InfoOmResterendeDagerTjeneste;
import no.nav.foreldrepenger.domene.uttak.OpphørFPTjeneste;
import no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.BeregnEkstraFlerbarnsukerTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnUttaksaldoTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.vedtak.felles.integrasjon.dokument.produksjon.DokumentproduksjonConsumer;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public abstract class DokumentBestillerApplikasjonTjenesteImplFPOppsett {
    static final Long dokumentDataId = 1337L;
    static final long BEHANDLING_ID = 1L;
    static final long DOKUMENT_DATA_ID = 301L;
    static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.now();
    static final String ARBEIDSFORHOLD_ID = "987123987";
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Mock
    DokumentRepository dokumentRepository;
    @Mock
    HistorikkRepository historikkRepository;
    @Mock
    DokumentMalType positivtVedtakDok;
    @Mock
    DokumentMalType avslagVedtakDok;
    @Mock
    DokumentMalType forlengetDok;
    @Mock
    DokumentMalType forlengetMedlDok;
    @Mock
    DokumentMalType innhentDok;
    @Mock
    DokumentMalType revurderingDok;
    @Mock
    DokumentMalType opphørDok;
    @Mock
    DokumentMalType innvilgetFPVedtak;
    @Mock
    DokumentproduksjonConsumer dokumentproduksjonConsumer;
    @Mock
    LandkodeOversetter landkodeOversetter;
    @Mock
    BasisPersonopplysningTjeneste personopplysningTjeneste;
    @Mock
    FamilieHendelseTjeneste familieHendelseTjeneste;
    @Mock
    UttakRepository uttakRepository;
    @Mock
    BeregningsresultatFPRepository beregningsresultatFPRepository;
    @Mock
    BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    @Mock
    FagsakRelasjonRepository fagsakRelasjonRepository;
    @Mock
    BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    @Mock
    BeregningRepository beregningsRepository;
    @Mock
    HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;
    @Mock
    private BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste;
    @Mock
    private OpphørFPTjeneste opphørFPTjeneste;
    @Mock
    private InfoOmResterendeDagerTjeneste infoOmResterendeDagerTjeneste;

    Behandling behandling;
    DokumentdataTjenesteMock dokumentdataTjenesteMock;
    BrevParametere brevParametere;
    Document document;
    DokumentBestillerApplikasjonTjenesteImpl dokumentBestillerApplikasjonTjeneste;
    AbstractTestScenario<?> scenario;
    Beregningsgrunnlag beregningsgrunnlag;
    VirksomhetEntitet virksomhet;
    BeregnUttaksaldoTjeneste beregnUttaksaldoTjeneste = mock(BeregnUttaksaldoTjeneste.class);

    @Before
    public void oppsett() {
        virksomhet = new VirksomhetEntitet.Builder().medOrgnr(ARBEIDSFORHOLD_ID).medNavn("Virksomheten").oppdatertOpplysningerNå().build();

        TpsTjeneste tpsTjeneste = mockHentBrukerForAktør();

        this.scenario = ScenarioMorSøkerForeldrepenger
            .forFødsel();
        this.behandling = scenario.lagMocked();
        OpprettBehandling.genererBehandlingOgResultat(behandling);

        brevParametere = new BrevParametereImpl(6, 3, Period.ofWeeks(3), Period.ofWeeks(3));

        oppsettForDokumentMalType(positivtVedtakDok, DokumentMalType.POSITIVT_VEDTAK_DOK, false, DokumentMalRestriksjon.INGEN);
        oppsettForDokumentMalType(innvilgetFPVedtak, DokumentMalType.INNVILGELSE_FORELDREPENGER_DOK, false, DokumentMalRestriksjon.INGEN);
        oppsettForDokumentMalType(avslagVedtakDok, DokumentMalType.AVSLAG_FORELDREPENGER_DOK, false, DokumentMalRestriksjon.INGEN);
        oppsettForDokumentMalType(innhentDok, DokumentMalType.INNHENT_DOK, true, DokumentMalRestriksjon.INGEN);
        oppsettForDokumentMalType(forlengetDok, DokumentMalType.FORLENGET_DOK, true, DokumentMalRestriksjon.ÅPEN_BEHANDLING);
        oppsettForDokumentMalType(forlengetMedlDok, DokumentMalType.FORLENGET_MEDL_DOK, true, DokumentMalRestriksjon.ÅPEN_BEHANDLING_IKKE_SENDT);
        oppsettForDokumentMalType(revurderingDok, DokumentMalType.REVURDERING_DOK, true, DokumentMalRestriksjon.REVURDERING);
        oppsettForDokumentMalType(opphørDok, DokumentMalType.OPPHØR_DOK, false, DokumentMalRestriksjon.INGEN);


        when(dokumentRepository.hentAlleDokumentMalTyper()).thenReturn(asList(positivtVedtakDok, innvilgetFPVedtak, avslagVedtakDok, innhentDok,
            forlengetDok, forlengetMedlDok, revurderingDok, opphørDok));
        when(landkodeOversetter.tilIso2("SWE")).thenReturn("SE");
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        familieHendelseTjeneste = new FamilieHendelseTjenesteImpl(personopplysningTjeneste, 16, 4, repositoryProvider);
        when(repositoryProvider.getUttakRepository()).thenReturn(uttakRepository);
        when(repositoryProvider.getBeregningsresultatFPRepository()).thenReturn(beregningsresultatFPRepository);
        when(repositoryProvider.getBeregningsgrunnlagRepository()).thenReturn(beregningsgrunnlagRepository);
        when(repositoryProvider.getFagsakRelasjonRepository()).thenReturn(fagsakRelasjonRepository);
        when(repositoryProvider.getBeregningRepository()).thenReturn(beregningsRepository);
        when(beregnUttaksaldoTjeneste.beregnDisponibleDager(Mockito.any())).thenReturn(Optional.empty());

        DokumentMapperTjenesteProvider tjenesteProvider = new DokumentMapperTjenesteProviderImpl(
            new SkjæringstidspunktTjenesteImpl(repositoryProvider,
                new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
                new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
                Period.of(0, 3, 0),
                Period.of(0, 10, 0)),
            personopplysningTjeneste,
            familieHendelseTjeneste,
            beregnUttaksaldoTjeneste,
            hentGrunnlagsdataTjeneste,
            beregnEkstraFlerbarnsukerTjeneste,
            opphørFPTjeneste,
            infoOmResterendeDagerTjeneste);

        DokumentBehandlingsresultatMapper behandlingsresultatMapper = new DokumentBehandlingsresultatMapper(repositoryProvider, tjenesteProvider);

        DokumentTypeDtoMapper mapper = new DokumentTypeDtoMapper(repositoryProvider,
            tjenesteProvider,
            brevParametere,
            behandlingsresultatMapper);

        dokumentdataTjenesteMock = new DokumentdataTjenesteMock(behandling,
            dokumentRepository,
            tpsTjeneste,
            repositoryProvider,
            mapper);

        BehandlingToDokumentbestillingDataMapper behandlingToDokumentbestillingDataMapper = new BehandlingToDokumentbestillingDataMapper(landkodeOversetter);
        DokumentToBrevDataMapper dokumentToBrevDataMapper = new DokumentToBrevDataMapper(repositoryProvider);
        BrevHistorikkinnslag brevHistorikkinnslag = new BrevHistorikkinnslag(dokumentdataTjenesteMock, historikkRepository);
        dokumentBestillerApplikasjonTjeneste = new DokumentBestillerApplikasjonTjenesteImpl(
            dokumentproduksjonConsumer,
            dokumentdataTjenesteMock,
            repositoryProvider,
            behandlingToDokumentbestillingDataMapper,
            dokumentToBrevDataMapper,
            brevHistorikkinnslag,
            behandlingskontrollTjeneste);

        beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(BigDecimal.valueOf(91425L))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(91425L))
            .build();
        BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
            .build(beregningsgrunnlag);
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));

        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(240000))
            .medRedusertBrukersAndelPrÅr(BigDecimal.valueOf(240000))
            .build(periode);
    }

    private void oppsettForDokumentMalType(DokumentMalType dokumentMalTypeMock, String type, boolean generisk, DokumentMalRestriksjon restriksjon) {
        when(dokumentMalTypeMock.getKode()).thenReturn(type);
        when(dokumentMalTypeMock.erGenerisk()).thenReturn(generisk);
        when(dokumentMalTypeMock.getDokumentMalRestriksjon()).thenReturn(restriksjon);
        when(dokumentRepository.hentDokumentMalType(type)).thenReturn(dokumentMalTypeMock);
    }

    void mockHentDokumentData(DokumentType vedtakDokument) {
        dokumentdataTjenesteMock.lagreDokumentData(BEHANDLING_ID, vedtakDokument);
        DokumentData dokumentData = dokumentdataTjenesteMock.hentDokumentData(dokumentDataId);
        when(dokumentRepository.hentDokumentData(any(Long.class))).thenReturn(dokumentData);
    }

    void assertDokumentFelles() {
        checkDokumentValue("sakspartId", SAKSPART_ID);
        checkDokumentValue("sakspartNavn", SAKSPART_NAVN);
        checkDokumentValue("mottakerId", SAKSPART_ID);
        checkDokumentValue("postNr", "1234");
    }

    void checkDokumentValue(String tagName, String value) {
        String textContent = document.getElementsByTagName(tagName).item(0).getFirstChild().getTextContent();
        Assert.assertEquals("Test for å sjekke tag: " + tagName + " som skal være verdi " + value + ".", value, textContent);
    }
}
