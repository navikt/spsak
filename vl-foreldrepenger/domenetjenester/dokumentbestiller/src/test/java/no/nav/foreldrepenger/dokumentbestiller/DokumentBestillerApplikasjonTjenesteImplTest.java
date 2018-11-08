package no.nav.foreldrepenger.dokumentbestiller;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteTestUtil.AKTØR_ID_BRUKER;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteTestUtil.AKTØR_ID_UTLENDING;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteTestUtil.AKTØR_ID_VERGE;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteTestUtil.SAKSPART_ID;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteTestUtil.SAKSPART_NAVN;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteTestUtil.lagReferanseAdresseForDokumentutsending;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteTestUtil.mockHentBrukerForAktør;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteTestUtil.oppsettForDokumentMalType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynDokumentEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.BrevMottaker;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalRestriksjon;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametereImpl;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.AvslagVedtakDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InnsynskravSvarDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InnvilgelseForeldrepengerDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.KlageAvvistDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.PositivtVedtakDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.UendretUtfallDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillVedtakBrevDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BrevmalDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentBehandlingsresultatMapper;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentTypeDtoMapper;
import no.nav.foreldrepenger.dokumentbestiller.brev.DokumentToBrevDataMapper;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.impl.FamilieHendelseTjenesteImpl;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.domene.uttak.InfoOmResterendeDagerTjeneste;
import no.nav.foreldrepenger.domene.uttak.OpphørFPTjeneste;
import no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.BeregnEkstraFlerbarnsukerTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.ProduserIkkeredigerbartDokumentDokumentErRedigerbart;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.ProduserIkkeredigerbartDokumentDokumentErVedlegg;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Dokumentbestillingsinformasjon;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Person;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.UtenlandskPostadresse;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.KnyttVedleggTilForsendelseRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastResponse;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserIkkeredigerbartDokumentRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserIkkeredigerbartDokumentResponse;
import no.nav.vedtak.felles.integrasjon.dokument.produksjon.DokumentproduksjonConsumer;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@SuppressWarnings("deprecation")
@RunWith(CdiRunner.class)
public class DokumentBestillerApplikasjonTjenesteImplTest {
    private static final Long dokumentDataId = 1337L;
    private static final long BEHANDLING_ID = 1L;
    private static final long DOKUMENT_DATA_ID = 301L;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Mock
    private DokumentRepository dokumentRepository;
    @Mock
    private HistorikkRepository historikkRepository;
    @Mock
    private OpptjeningRepository opptjeningRepository;
    @Mock
    private DokumentMalType positivtVedtakDok;
    @Mock
    private DokumentMalType avslagVedtakDok;
    @Mock
    private DokumentMalType forlengetDok;
    @Mock
    private DokumentMalType forlengetMedlDok;
    @Mock
    private DokumentMalType innhentDok;
    @Mock
    private DokumentMalType revurderingDok;
    @Mock
    private DokumentMalType opphørDok;
    @Mock
    private DokumentMalType innsynskravsvarDok;
    @Mock
    private DokumentproduksjonConsumer dokumentproduksjonConsumer;
    @Mock
    private KlageVurderingResultat klageVurderingResultat;
    @Mock
    private LandkodeOversetter landkodeOversetter;
    @Mock
    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    @Mock
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    @Mock
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;
    @Mock
    private BeregnEkstraFlerbarnsukerTjeneste ekstraFlerbarnsukerTjeneste;
    @Mock
    private OpphørFPTjeneste opphørFPTjeneste;
    @Mock
    private InfoOmResterendeDagerTjeneste infoOmResterendeDagerTjeneste;

    private FamilieHendelseTjeneste familieHendelseTjeneste;
    private Behandling behandling;
    private DokumentdataTjenesteMock dokumentdataTjenesteMock;
    private BrevParametere brevParametere;
    private Document document;
    private BehandlingToDokumentbestillingDataMapper behandlingToDokumentbestillingDataMapper;
    private TpsTjeneste tpsTjeneste;
    private BehandlingRepositoryProvider repositoryProvider;
    private DokumentBestillerApplikasjonTjenesteImpl dokumentBestillerApplikasjonTjeneste;
    private AbstractTestScenario<?> scenario;
    private DokumentToBrevDataMapper dokumentToBrevDataMapper;
    private BrevHistorikkinnslag brevHistorikkinnslag;

    @Before
    public void oppsett() {
        tpsTjeneste = mockHentBrukerForAktør();
        this.scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medFødselAdopsjonsdato(Collections.singletonList(LocalDate.now().minusDays(3)));
        this.behandling = scenario.lagMocked();
        OpprettBehandling.genererBehandlingOgResultat(behandling);

        brevParametere = new BrevParametereImpl(6, 3, Period.ofWeeks(3), Period.ofWeeks(2));

        oppsettForDokumentMalType(positivtVedtakDok, DokumentMalType.POSITIVT_VEDTAK_DOK, false, DokumentMalRestriksjon.INGEN, dokumentRepository);
        oppsettForDokumentMalType(avslagVedtakDok, DokumentMalType.AVSLAGSVEDTAK_DOK, false, DokumentMalRestriksjon.INGEN, dokumentRepository);
        oppsettForDokumentMalType(innhentDok, DokumentMalType.INNHENT_DOK, true, DokumentMalRestriksjon.INGEN, dokumentRepository);
        oppsettForDokumentMalType(forlengetDok, DokumentMalType.FORLENGET_DOK, true, DokumentMalRestriksjon.ÅPEN_BEHANDLING, dokumentRepository);
        oppsettForDokumentMalType(forlengetMedlDok, DokumentMalType.FORLENGET_MEDL_DOK, true, DokumentMalRestriksjon.ÅPEN_BEHANDLING_IKKE_SENDT, dokumentRepository);
        oppsettForDokumentMalType(revurderingDok, DokumentMalType.REVURDERING_DOK, true, DokumentMalRestriksjon.REVURDERING, dokumentRepository);
        oppsettForDokumentMalType(opphørDok, DokumentMalType.OPPHØR_DOK, false, DokumentMalRestriksjon.INGEN, dokumentRepository);
        oppsettForDokumentMalType(innsynskravsvarDok, DokumentMalType.INNSYNSKRAV_SVAR, false, DokumentMalRestriksjon.INGEN, dokumentRepository);

        when(dokumentRepository.hentAlleDokumentMalTyper()).thenReturn(asList(positivtVedtakDok, avslagVedtakDok, innhentDok,
            forlengetDok, forlengetMedlDok, revurderingDok, opphørDok, innsynskravsvarDok));
        when(landkodeOversetter.tilIso2("SWE")).thenReturn("SE");
        repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        familieHendelseTjeneste = new FamilieHendelseTjenesteImpl(personopplysningTjeneste, 16, 4, repositoryProvider);
        when(repositoryProvider.getUttakRepository()).thenReturn(mock(UttakRepository.class));
        when(repositoryProvider.getOpptjeningRepository()).thenReturn(opptjeningRepository);

        DokumentMapperTjenesteProvider tjenesteProvider = new DokumentMapperTjenesteProviderImpl(
            new SkjæringstidspunktTjenesteImpl(repositoryProvider,
                new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
                new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
                Period.of(0, 3, 0),
                Period.of(0, 10, 0)),
            personopplysningTjeneste,
            familieHendelseTjeneste,
            null,
            hentGrunnlagsdataTjeneste,
            ekstraFlerbarnsukerTjeneste,
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

        behandlingToDokumentbestillingDataMapper = new BehandlingToDokumentbestillingDataMapper(landkodeOversetter);
        dokumentToBrevDataMapper = new DokumentToBrevDataMapper(repositoryProvider);
        brevHistorikkinnslag = new BrevHistorikkinnslag(dokumentdataTjenesteMock, historikkRepository);
        dokumentBestillerApplikasjonTjeneste = new DokumentBestillerApplikasjonTjenesteImpl(
            dokumentproduksjonConsumer,
            dokumentdataTjenesteMock,
            repositoryProvider,
            behandlingToDokumentbestillingDataMapper,
            dokumentToBrevDataMapper,
            brevHistorikkinnslag,
            behandlingskontrollTjeneste);
    }

    private void mockHentDokumentDataPositivtVedtak() {
        DokumentType vedtakDokument = new PositivtVedtakDokument(brevParametere);
        mockHentDokumentData(vedtakDokument);
    }

    private void mockHentDokumentData(DokumentType vedtakDokument) {
        dokumentdataTjenesteMock.lagreDokumentData(BEHANDLING_ID, vedtakDokument);
        DokumentData dokumentData = dokumentdataTjenesteMock.hentDokumentData(dokumentDataId);
        when(dokumentRepository.hentDokumentData(any(Long.class))).thenReturn(dokumentData);
    }

    @Test
    public void forhåndsvisVedtaksbrevTest() {
        OpprettBehandling.genererBehandlingOgResultat(behandling, BehandlingResultatType.INNVILGET);

        // arrange
        mockHentDokumentDataPositivtVedtak();

        ProduserDokumentutkastResponse produserDokumentutkastResponse = new ProduserDokumentutkastResponse();
        produserDokumentutkastResponse.setDokumentutkast(new byte[]{1, 1});
        ArgumentCaptor<ProduserDokumentutkastRequest> captor = ArgumentCaptor.forClass(ProduserDokumentutkastRequest.class);
        when(dokumentproduksjonConsumer.produserDokumentutkast(captor.capture())).thenReturn(produserDokumentutkastResponse);

        // act
        BestillVedtakBrevDto dto = new BestillVedtakBrevDto(behandling.getId(), null);
        dto.setSkalBrukeOverstyrendeFritekstBrev(false);
        byte[] forhandsvisVedtaksbrev = dokumentBestillerApplikasjonTjeneste.forhandsvisVedtaksbrev(dto, (b -> false));

        // assert
        ProduserDokumentutkastRequest value = captor.getValue();
        Assert.assertNotNull(forhandsvisVedtaksbrev);
        Assert.assertNotNull(value.getBrevdata());
        document = ((Node) value.getBrevdata()).getOwnerDocument();
        checkDokumentValue("sakspartId", SAKSPART_ID);
        checkDokumentValue("sakspartNavn", SAKSPART_NAVN);
        checkDokumentValue("mottakerId", SAKSPART_ID);
        checkDokumentValue("belop", "2.0");
        checkDokumentValue("postNr", "1234");
    }

    @Test
    public void forhåndsvisAvslagsbrevTest() {

        // arrange
        Avslagsårsak avslagsårsak = Avslagsårsak.SØKER_ER_IKKE_BOSATT;
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.AVSLÅTT).medAvslagsårsak(avslagsårsak)
            .medAvslagarsakFritekst("Bare Tull").buildFor(behandling);

        VilkårResultat.builder().leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_OPPFYLT)
            .buildFor(behandling);

        mockHentDokumentData(new AvslagVedtakDokument(brevParametere, null));

        ProduserDokumentutkastResponse produserDokumentutkastResponse = new ProduserDokumentutkastResponse();
        produserDokumentutkastResponse.setDokumentutkast(new byte[]{1, 1});
        ArgumentCaptor<ProduserDokumentutkastRequest> captor = ArgumentCaptor.forClass(ProduserDokumentutkastRequest.class);
        when(dokumentproduksjonConsumer.produserDokumentutkast(captor.capture())).thenReturn(produserDokumentutkastResponse);

        // act 1: for foreslå vedtak, fritekst fra dto
        BestillVedtakBrevDto dsa = new BestillVedtakBrevDto(behandling.getId(), "dsa");
        dsa.setSkalBrukeOverstyrendeFritekstBrev(false);
        byte[] forhandsvisAvslagsbrev = dokumentBestillerApplikasjonTjeneste.forhandsvisVedtaksbrev(dsa, (b -> false));

        // assert 1
        assertAvslagsbrev(captor, forhandsvisAvslagsbrev, "dsa");

        // act 2: for totrinns behandling, lagret fritekst
        BestillVedtakBrevDto dto = new BestillVedtakBrevDto(behandling.getId(), null);
        dto.setSkalBrukeOverstyrendeFritekstBrev(false);
        byte[] forhandsvisAvslagsbrev2 = dokumentBestillerApplikasjonTjeneste.forhandsvisVedtaksbrev(dto, (b -> false));

        // assert 2
        assertAvslagsbrev(captor, forhandsvisAvslagsbrev2, "Bare Tull");
    }

    private void assertAvslagsbrev(ArgumentCaptor<ProduserDokumentutkastRequest> captor, byte[] forhandsvisAvslagsbrev, String fritekst) {
        ProduserDokumentutkastRequest value = captor.getValue();
        Assert.assertNotNull(forhandsvisAvslagsbrev);
        Assert.assertNotNull(value.getBrevdata());
        document = ((Node) value.getBrevdata()).getOwnerDocument();
        checkDokumentValue("sakspartId", SAKSPART_ID);
        checkDokumentValue("sakspartNavn", SAKSPART_NAVN);
        checkDokumentValue("mottakerId", SAKSPART_ID);
        checkDokumentValue("fritekst", fritekst); // TODO (HUMLE): sjekk alle felter
        checkDokumentValue("postNr", "1234");
    }

    private void checkDokumentValue(String tagName, String value) {
        String textContent = document.getElementsByTagName(tagName).item(0).getFirstChild().getTextContent();
        Assert.assertEquals("Test for å sjekke tag: " + tagName + " som skal være verdi " + value + ".", value, textContent);
    }

    @Test
    public void produserDokumentTest()
        throws ProduserIkkeredigerbartDokumentDokumentErRedigerbart, ProduserIkkeredigerbartDokumentDokumentErVedlegg {

        // arrange
        mockHentDokumentDataPositivtVedtak();

        ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = new ProduserIkkeredigerbartDokumentResponse();
        produserIkkeredigerbartDokumentResponse.setJournalpostId("2");
        ArgumentCaptor<ProduserIkkeredigerbartDokumentRequest> captor = ArgumentCaptor
            .forClass(ProduserIkkeredigerbartDokumentRequest.class);
        when(dokumentproduksjonConsumer.produserIkkeredigerbartDokument(captor.capture()))
            .thenReturn(produserIkkeredigerbartDokumentResponse);

        // act
        dokumentBestillerApplikasjonTjeneste.produserDokument(DOKUMENT_DATA_ID, HistorikkAktør.VEDTAKSLØSNINGEN, "");

        // assert
        document = ((Node) captor.getValue().getBrevdata()).getOwnerDocument();
        checkDokumentValue("sakspartId", SAKSPART_ID);
        checkDokumentValue("sakspartNavn", SAKSPART_NAVN);
        checkDokumentValue("mottakerId", SAKSPART_ID);
        checkDokumentValue("belop", "2.0");
        checkDokumentValue("postNr", "1234");

        assertThat(captor.getAllValues()).hasSize(1);
        verify(dokumentproduksjonConsumer, times(1)).produserIkkeredigerbartDokument(any(ProduserIkkeredigerbartDokumentRequest.class));
        Dokumentbestillingsinformasjon dokumentbestinf = captor.getValue().getDokumentbestillingsinformasjon();
        Person bruker = (Person) dokumentbestinf.getBruker();
        Person mottaker = (Person) dokumentbestinf.getMottaker();
        Assert.assertEquals("Sjekker dokumentbestilling.bruker.ident: ", SAKSPART_ID, bruker.getIdent());
        Assert.assertEquals("Sjekker dokumentbestilling.bruker.navn: ", SAKSPART_NAVN, bruker.getNavn());
        Assert.assertEquals("Sjekker dokumentbestilling.mottaker.ident: ", SAKSPART_ID, mottaker.getIdent());
        Assert.assertEquals("Sjekker dokumentbestilling.mottaker.navn: ", SAKSPART_NAVN, mottaker.getNavn());
    }

    @Test
    public void produserVedtaksbrevTest()
        throws ProduserIkkeredigerbartDokumentDokumentErRedigerbart, ProduserIkkeredigerbartDokumentDokumentErVedlegg {
        // arrange
        mockHentDokumentDataPositivtVedtak();

        ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = new ProduserIkkeredigerbartDokumentResponse();
        produserIkkeredigerbartDokumentResponse.setJournalpostId("2");
        ArgumentCaptor<ProduserIkkeredigerbartDokumentRequest> captor = ArgumentCaptor
            .forClass(ProduserIkkeredigerbartDokumentRequest.class);
        when(dokumentproduksjonConsumer.produserIkkeredigerbartDokument(captor.capture()))
            .thenReturn(produserIkkeredigerbartDokumentResponse);

        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder()
            .medVedtaksdato(LocalDate.now())
            .medAnsvarligSaksbehandler("VL")
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medBehandlingsresultat(behandling.getBehandlingsresultat())
            .build();

        // act
        dokumentBestillerApplikasjonTjeneste.produserVedtaksbrev(behandlingVedtak);

        // assert
        document = ((Node) captor.getValue().getBrevdata()).getOwnerDocument();
        checkDokumentValue("sakspartId", SAKSPART_ID);
        checkDokumentValue("sakspartNavn", SAKSPART_NAVN);
        checkDokumentValue("mottakerId", SAKSPART_ID);
        checkDokumentValue("belop", "2.0");
        checkDokumentValue("postNr", "1234");

        assertThat(captor.getAllValues()).hasSize(1);
        verify(dokumentproduksjonConsumer, times(1)).produserIkkeredigerbartDokument(any(ProduserIkkeredigerbartDokumentRequest.class));
        Dokumentbestillingsinformasjon dokumentbestinf = captor.getValue().getDokumentbestillingsinformasjon();
        Person bruker = (Person) dokumentbestinf.getBruker();
        Person mottaker = (Person) dokumentbestinf.getMottaker();
        Assert.assertEquals("Sjekker dokumentbestilling.bruker.ident: ", SAKSPART_ID, bruker.getIdent());
        Assert.assertEquals("Sjekker dokumentbestilling.bruker.navn: ", SAKSPART_NAVN, bruker.getNavn());
        Assert.assertEquals("Sjekker dokumentbestilling.mottaker.ident: ", SAKSPART_ID, mottaker.getIdent());
        Assert.assertEquals("Sjekker dokumentbestilling.mottaker.navn: ", SAKSPART_NAVN, mottaker.getNavn());
    }

    @Test
    public void forhandsvisVedtaksbrevKlageTest() {
        // arrange
        Whitebox.setInternalState(BehandlingType.KLAGE, "ekstraData", "{ \"behandlingstidFristUker\" : 12, \"behandlingstidVarselbrev\" : \"N\" }");
        Whitebox.setInternalState(behandling, "behandlingType", BehandlingType.KLAGE);
        when(klageVurderingResultat.getKlageVurdering()).thenReturn(KlageVurdering.AVVIS_KLAGE);
        when(klageVurderingResultat.getKlageAvvistÅrsak()).thenReturn(KlageAvvistÅrsak.KLAGE_UGYLDIG);
        when(klageVurderingResultat.getKlageVurdertAv()).thenReturn(KlageVurdertAv.NFP);
        behandling.leggTilKlageVurderingResultat(klageVurderingResultat);
        DokumentMalType dokumentMalMock = mock(DokumentMalType.class);
        when(dokumentMalMock.getKode()).thenReturn(DokumentMalType.KLAGE_AVVIST_DOK);
        when(dokumentRepository.hentDokumentMalType(DokumentMalType.KLAGE_AVVIST_DOK)).thenReturn(dokumentMalMock);

        mockHentDokumentData(new KlageAvvistDokument(brevParametere));

        ProduserDokumentutkastResponse produserDokumentutkastResponse = new ProduserDokumentutkastResponse();
        ArgumentCaptor<ProduserDokumentutkastRequest> captor = ArgumentCaptor
            .forClass(ProduserDokumentutkastRequest.class);
        when(dokumentproduksjonConsumer.produserDokumentutkast(captor.capture()))
            .thenReturn(produserDokumentutkastResponse);

        // act
        BestillVedtakBrevDto bestillVedtakBrevDto = new BestillVedtakBrevDto(DOKUMENT_DATA_ID, "vedtak fritekst");
        bestillVedtakBrevDto.setSkalBrukeOverstyrendeFritekstBrev(false);
        dokumentBestillerApplikasjonTjeneste.forhandsvisVedtaksbrev(bestillVedtakBrevDto, (b -> false));

        // assert
        document = ((Node) captor.getValue().getBrevdata()).getOwnerDocument();
        checkDokumentValue("sakspartId", SAKSPART_ID);
        checkDokumentValue("sakspartNavn", SAKSPART_NAVN);
        checkDokumentValue("mottakerId", SAKSPART_ID);
        checkDokumentValue("postNr", "1234");
        checkDokumentValue("ytelseType", "ES");
        checkDokumentValue("avvistGrunn", "KLAGEUGYLDIG");
        checkDokumentValue("klageFristUker", "6");
    }

    @Test
    public void produserVedtakIKlagebehandlingTest()
        throws ProduserIkkeredigerbartDokumentDokumentErRedigerbart, ProduserIkkeredigerbartDokumentDokumentErVedlegg {

        // arrange
        when(klageVurderingResultat.getKlageVurdering()).thenReturn(KlageVurdering.AVVIS_KLAGE);
        when(klageVurderingResultat.getKlageAvvistÅrsak()).thenReturn(KlageAvvistÅrsak.KLAGET_FOR_SENT);
        when(klageVurderingResultat.getKlageVurdertAv()).thenReturn(KlageVurdertAv.NFP);
        behandling.leggTilKlageVurderingResultat(klageVurderingResultat);
        DokumentMalType dokumentMalMock = mock(DokumentMalType.class);
        when(dokumentMalMock.getKode()).thenReturn(DokumentMalType.KLAGE_AVVIST_DOK);
        when(dokumentRepository.hentDokumentMalType(DokumentMalType.KLAGE_AVVIST_DOK)).thenReturn(dokumentMalMock);

        mockHentDokumentData(new KlageAvvistDokument(brevParametere));

        ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = new ProduserIkkeredigerbartDokumentResponse();
        produserIkkeredigerbartDokumentResponse.setJournalpostId("2");
        ArgumentCaptor<ProduserIkkeredigerbartDokumentRequest> captor = ArgumentCaptor
            .forClass(ProduserIkkeredigerbartDokumentRequest.class);
        when(dokumentproduksjonConsumer.produserIkkeredigerbartDokument(captor.capture()))
            .thenReturn(produserIkkeredigerbartDokumentResponse);

        // act
        dokumentBestillerApplikasjonTjeneste.produserDokument(DOKUMENT_DATA_ID, HistorikkAktør.VEDTAKSLØSNINGEN, "");

        // assert
        document = ((Node) captor.getValue().getBrevdata()).getOwnerDocument();
        checkDokumentValue("sakspartId", SAKSPART_ID);
        checkDokumentValue("sakspartNavn", SAKSPART_NAVN);
        checkDokumentValue("mottakerId", SAKSPART_ID);
        checkDokumentValue("postNr", "1234");
        checkDokumentValue("ytelseType", "ES");
        checkDokumentValue("avvistGrunn", "ETTER6UKER");
        checkDokumentValue("klageFristUker", "6");
    }

    @Test
    public void produserDokumentTilToDokumentMottakereTest()
        throws ProduserIkkeredigerbartDokumentDokumentErRedigerbart, ProduserIkkeredigerbartDokumentDokumentErVedlegg {
        NavBruker navBruker1 = mock(NavBruker.class);
        when(navBruker1.getAktørId()).thenReturn(AKTØR_ID_BRUKER);
        NavBruker navBruker2 = mock(NavBruker.class);
        when(navBruker2.getAktørId()).thenReturn(AKTØR_ID_VERGE);

        VergeRepository repo = repositoryProvider.getVergeGrunnlagRepository();
        VergeBuilder builder = new VergeBuilder();
        builder.medVergeType(VergeType.BARN);
        builder.medBrevMottaker(BrevMottaker.BEGGE);
        builder.medBruker(navBruker2);
        builder.medStønadMottaker(false);
        repo.lagreOgFlush(behandling, builder);

        mockHentDokumentDataPositivtVedtak();

        ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = new ProduserIkkeredigerbartDokumentResponse();
        produserIkkeredigerbartDokumentResponse.setJournalpostId("2");
        ArgumentCaptor<ProduserIkkeredigerbartDokumentRequest> captor = ArgumentCaptor.forClass(ProduserIkkeredigerbartDokumentRequest.class);
        when(dokumentproduksjonConsumer.produserIkkeredigerbartDokument(captor.capture())).thenReturn(produserIkkeredigerbartDokumentResponse);

        DokumentBestillerApplikasjonTjenesteImpl dokumentBestillerApplikasjonTjeneste = new DokumentBestillerApplikasjonTjenesteImpl(
            dokumentproduksjonConsumer,
            dokumentdataTjenesteMock,
            repositoryProvider,
            behandlingToDokumentbestillingDataMapper,
            dokumentToBrevDataMapper,
            brevHistorikkinnslag,
            behandlingskontrollTjeneste);
        // Act
        dokumentBestillerApplikasjonTjeneste.produserDokument(DOKUMENT_DATA_ID, HistorikkAktør.VEDTAKSLØSNINGEN, "");

        // Assert
        verify(tpsTjeneste, atLeast(1)).hentBrukerForAktør(any(AktørId.class));
        assertThat(captor.getAllValues()).hasSize(2);
        verify(dokumentproduksjonConsumer, times(2)).produserIkkeredigerbartDokument(any(ProduserIkkeredigerbartDokumentRequest.class));
        verify(dokumentRepository, atLeast(2)).hentDokumentMalType(any(String.class));
    }

    @Test
    public void hentBrevmalerForRevurderingTest() {
        // arrange
        Fagsak fagsak = FagsakBuilder.nyEngangstønadForMor().build();

        behandling = Behandling
            .nyBehandlingFor(fagsak, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_ANNET).medManueltOpprettet(true))
            .build();

        scenario.resetBehandling(behandling);

        // act
        List<BrevmalDto> maler = dokumentBestillerApplikasjonTjeneste.hentBrevmalerFor(behandling.getId());

        // assert
        assertThat(maler).hasSize(2);
    }

    @Test
    public void hentBrevmalerForRevurderingBrukersEndring() {
        // arrange
        Fagsak fagsak = FagsakBuilder.nyForeldrepengerForMor().build();

        behandling = Behandling
            .nyBehandlingFor(fagsak, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_AVVIK_ANTALL_BARN).medManueltOpprettet(true))
            .build();

        scenario.resetBehandling(behandling);

        // act
        List<BrevmalDto> maler = dokumentBestillerApplikasjonTjeneste.hentBrevmalerFor(behandling.getId());

        // assert
        assertThat(maler).hasSize(2);
    }

    @Test
    public void hent_brevmal_revurdering_ikke_manuell() {
        // arrange
        Fagsak fagsak = FagsakBuilder.nyForeldrepengerForMor().build();

        behandling = Behandling
            .nyBehandlingFor(fagsak, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER).medManueltOpprettet(false))
            .build();

        scenario.resetBehandling(behandling);

        // act
        List<BrevmalDto> maler = dokumentBestillerApplikasjonTjeneste.hentBrevmalerFor(behandling.getId());

        // assert
        assertThat(maler).hasSize(4);
    }

    @Test
    public void hentBrevmalerForRevurderingMedBehandledeVarselOmRevurederingTest() {
        // arrange
        Fagsak fagsak = FagsakBuilder.nyEngangstønadForMor().build();
        behandling = Behandling
            .nyBehandlingFor(fagsak, BehandlingType.REVURDERING)
            .build();

        scenario.resetBehandling(behandling);

        AksjonspunktRepositoryImpl aksjonspunktRepositoryImpl = new AksjonspunktRepositoryImpl(null);
        Aksjonspunkt aksjonspunkt = aksjonspunktRepositoryImpl.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VARSEL_REVURDERING_MANUELL);
        aksjonspunktRepositoryImpl.setTilUtført(aksjonspunkt, "begrunnelse");

        // act
        List<BrevmalDto> maler = dokumentBestillerApplikasjonTjeneste.hentBrevmalerFor(behandling.getId());

        // assert
        assertThat(maler).hasSize(1);
        List<String> liste = maler.stream().map(BrevmalDto::getKode).collect(Collectors.toList());
        assertThat(liste).containsExactly(DokumentMalType.INNHENT_DOK);
    }

    @Test
    public void hentBrevmalerForKlage() {
        // arrange
        Fagsak fagsak = FagsakBuilder.nyEngangstønadForMor().build();
        behandling = Behandling
            .nyBehandlingFor(fagsak, BehandlingType.KLAGE)
            .build();

        scenario.resetBehandling(behandling);

        // act
        List<BrevmalDto> maler = dokumentBestillerApplikasjonTjeneste.hentBrevmalerFor(behandling.getId());

        // assert
        assertThat(maler).hasSize(1);
        List<String> liste = maler.stream().map(BrevmalDto::getKode).collect(Collectors.toList());
        assertThat(liste).containsExactly(DokumentMalType.INNHENT_DOK);
    }

    @Test
    public void hentBrevmalerForÅpenBehandlingTest() {
        // act
        List<BrevmalDto> maler = dokumentBestillerApplikasjonTjeneste.hentBrevmalerFor(behandling.getId());

        // assert
        assertThat(maler).hasSize(3);
        List<String> liste = maler.stream().map(BrevmalDto::getKode).collect(Collectors.toList());
        assertThat(liste).containsExactly(DokumentMalType.INNHENT_DOK, DokumentMalType.FORLENGET_DOK, DokumentMalType.FORLENGET_MEDL_DOK);
    }

    @Test
    public void hentBrevmalerForÅpenBehandlingDerForlengetMedlemskapsBrevErSendtTest() {
        // arrange
        List<DokumentData> liste = new ArrayList<>();
        DokumentData dokument = DokumentData.opprettNy(forlengetMedlDok, behandling);
        dokument.getDokumentFelles().forEach(df -> df.setDokumentId("12345"));
        dokument.setBestiltTid(LocalDateTime.now());
        liste.add(dokument);
        when(dokumentRepository.hentDokumentDataListe(behandling.getId(), DokumentMalType.FORLENGET_MEDL_DOK)).thenReturn(liste);

        // act
        List<BrevmalDto> maler = dokumentBestillerApplikasjonTjeneste.hentBrevmalerFor(behandling.getId());

        // assert
        assertThat(maler).hasSize(3);
        List<String> resultat = maler.stream().map(BrevmalDto::getKode).collect(Collectors.toList());
        assertThat(resultat).containsExactly(DokumentMalType.INNHENT_DOK, DokumentMalType.FORLENGET_DOK, DokumentMalType.FORLENGET_MEDL_DOK);
        List<String> tilgjengelige = maler.stream().filter(BrevmalDto::getTilgjengelig).map(BrevmalDto::getKode).collect(Collectors.toList());
        assertThat(tilgjengelige).containsExactly(DokumentMalType.INNHENT_DOK, DokumentMalType.FORLENGET_DOK);
    }

    @Test
    public void hentBrevmalerForÅpenBehandlingDerForlengetBrevErSendtTest() {
        // arrange
        List<DokumentData> liste = new ArrayList<>();
        liste.add(DokumentData.opprettNy(forlengetDok, behandling));
        when(dokumentRepository.hentDokumentDataListe(behandling.getId(), DokumentMalType.FORLENGET_DOK)).thenReturn(liste);

        // act
        List<BrevmalDto> maler = dokumentBestillerApplikasjonTjeneste.hentBrevmalerFor(behandling.getId());

        // assert
        assertThat(maler).hasSize(3);
        List<String> resultat = maler.stream().map(BrevmalDto::getKode).collect(Collectors.toList());
        assertThat(resultat).containsExactly(DokumentMalType.INNHENT_DOK, DokumentMalType.FORLENGET_DOK, DokumentMalType.FORLENGET_MEDL_DOK);
        List<String> tilgjengelige = maler.stream().filter(BrevmalDto::getTilgjengelig).map(BrevmalDto::getKode).collect(Collectors.toList());
        assertThat(tilgjengelige).containsExactly(DokumentMalType.INNHENT_DOK, DokumentMalType.FORLENGET_DOK, DokumentMalType.FORLENGET_MEDL_DOK);
    }

    @Test
    public void hentBrevmalerForLukketBehandlingTest() {
        // arrange
        behandling.avsluttBehandling();

        // act
        List<BrevmalDto> maler = dokumentBestillerApplikasjonTjeneste.hentBrevmalerFor(behandling.getId());

        // assert
        assertThat(maler).hasSize(3);
        List<String> resultat = maler.stream().map(BrevmalDto::getKode).collect(Collectors.toList());
        assertThat(resultat).containsExactly(DokumentMalType.INNHENT_DOK, DokumentMalType.FORLENGET_DOK, DokumentMalType.FORLENGET_MEDL_DOK);
        List<String> tilgjengelige = maler.stream().filter(BrevmalDto::getTilgjengelig).map(BrevmalDto::getKode).collect(Collectors.toList());
        assertThat(tilgjengelige).containsExactly(DokumentMalType.INNHENT_DOK);
    }

    @Test
    public void dokumentTilUtenlandskMottakerTest()
        throws ProduserIkkeredigerbartDokumentDokumentErRedigerbart,
        ProduserIkkeredigerbartDokumentDokumentErVedlegg {
        NavBruker mottaker = mock(NavBruker.class);
        when(mottaker.getAktørId()).thenReturn(AKTØR_ID_UTLENDING);
        mockHentDokumentDataPositivtVedtak();

        ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = new ProduserIkkeredigerbartDokumentResponse();
        produserIkkeredigerbartDokumentResponse.setJournalpostId("2");
        ArgumentCaptor<ProduserIkkeredigerbartDokumentRequest> captor = ArgumentCaptor.forClass(ProduserIkkeredigerbartDokumentRequest.class);
        when(dokumentproduksjonConsumer.produserIkkeredigerbartDokument(captor.capture())).thenReturn(produserIkkeredigerbartDokumentResponse);

        when(tpsTjeneste.hentAdresseinformasjon(Mockito.eq(new PersonIdent("fnr")))).thenReturn(lagReferanseAdresseForDokumentutsending(true));

        dokumentBestillerApplikasjonTjeneste.produserDokument(DOKUMENT_DATA_ID, HistorikkAktør.VEDTAKSLØSNINGEN, "");

        UtenlandskPostadresse adresse = (UtenlandskPostadresse) captor.getAllValues().get(0)
            .getDokumentbestillingsinformasjon()
            .getAdresse();
        assertThat(adresse.getLand().getValue()).isEqualTo("SE");
    }

    // TODO(AndersPalfi): Finnes det ikke noen annen måte å lage denne testen på enn å mocke en haug av entiteter?  Er noe herk å refactorere.
    @Test
    public void skalLeggeTilVedlegg() throws Exception {
        // arrange
        DokumentMalType dokumentMalType = mock(DokumentMalType.class);
        DokumentData dokumentData = DokumentData.builder().medDokumentMalType(dokumentMalType).medBehandling(behandling).build();
        dokumentdataTjenesteMock.lagreDokumentData(dokumentData.getBehandling().getId(), new InnsynskravSvarDokument(brevParametere, "fritekst", InnsynResultatType.INNVILGET));

        InnsynEntitet innsyn = mock(InnsynEntitet.class);
        when(innsyn.getId()).thenReturn(2L);

        String innsynDokumentId1 = "3L";
        InnsynDokumentEntitet innsynDokument1 = mock(InnsynDokumentEntitet.class);
        when(innsynDokument1.isFikkInnsyn()).thenReturn(true);
        when(innsynDokument1.getDokumentId()).thenReturn(innsynDokumentId1);
        when(innsynDokument1.getJournalpostId()).thenReturn(new JournalpostId("44"));

        String innsynDokumentId2 = "4L";
        InnsynDokumentEntitet innsynDokument2 = mock(InnsynDokumentEntitet.class);
        when(innsynDokument2.isFikkInnsyn()).thenReturn(true);
        when(innsynDokument2.getDokumentId()).thenReturn(innsynDokumentId2);
        when(innsynDokument2.getJournalpostId()).thenReturn(new JournalpostId("44"));

        String innsynDokumentId3 = "5L";
        InnsynDokumentEntitet innsynDokument3 = mock(InnsynDokumentEntitet.class);
        when(innsynDokument3.isFikkInnsyn()).thenReturn(false);
        when(innsynDokument3.getDokumentId()).thenReturn(innsynDokumentId3);
        when(innsynDokument3.getJournalpostId()).thenReturn(new JournalpostId("44"));

        when(repositoryProvider.getInnsynRepository().hentForBehandling(behandling.getId())).thenReturn(Collections.singletonList(innsyn));
        when(repositoryProvider.getInnsynRepository().hentDokumenterForInnsyn(innsyn.getId())).thenReturn(Arrays.asList(innsynDokument1, innsynDokument2));

        ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = new ProduserIkkeredigerbartDokumentResponse();
        produserIkkeredigerbartDokumentResponse.setJournalpostId("randomJournalpostId");
        when(dokumentproduksjonConsumer.produserIkkeredigerbartDokument(any(ProduserIkkeredigerbartDokumentRequest.class)))
            .thenReturn(produserIkkeredigerbartDokumentResponse);

        ArgumentCaptor<KnyttVedleggTilForsendelseRequest> captor = ArgumentCaptor
            .forClass(KnyttVedleggTilForsendelseRequest.class);

        // act
        dokumentBestillerApplikasjonTjeneste.produserDokument(DOKUMENT_DATA_ID, HistorikkAktør.VEDTAKSLØSNINGEN, "");

        verify(dokumentproduksjonConsumer, times(2)).knyttVedleggTilForsendelse(captor.capture());

        assertThat(captor.getAllValues().get(0).getDokumentId()).isIn(innsynDokumentId1, innsynDokumentId2);
        assertThat(captor.getAllValues().get(1).getDokumentId()).isIn(innsynDokumentId1, innsynDokumentId2);
    }

    // TODO(AndersPalfi): Finnes det ikke noen annen måte å lage denne testen på enn å mocke en haug av entiteter?  Er noe herk å refactorere.
    @Test
    public void skalIkkeLeggeTilDuplikateVedleggSammeInnsyn() throws Exception {

        // arrange
        DokumentMalType dokumentMalType = mock(DokumentMalType.class);
        DokumentData dokumentData = DokumentData.builder().medDokumentMalType(dokumentMalType).medBehandling(behandling).build();
        dokumentdataTjenesteMock.lagreDokumentData(dokumentData.getBehandling().getId(), new InnsynskravSvarDokument(brevParametere, "fritekst", InnsynResultatType.INNVILGET));

        InnsynEntitet innsyn = mock(InnsynEntitet.class);
        when(innsyn.getId()).thenReturn(2L);

        String innsynDokumentId = "3L";
        InnsynDokumentEntitet innsynDokument1 = mock(InnsynDokumentEntitet.class);
        when(innsynDokument1.isFikkInnsyn()).thenReturn(true);
        when(innsynDokument1.getDokumentId()).thenReturn(innsynDokumentId);
        when(innsynDokument1.getJournalpostId()).thenReturn(new JournalpostId("44"));

        InnsynDokumentEntitet innsynDokument2 = mock(InnsynDokumentEntitet.class);
        when(innsynDokument2.isFikkInnsyn()).thenReturn(true);
        when(innsynDokument2.getDokumentId()).thenReturn(innsynDokumentId);
        when(innsynDokument2.getJournalpostId()).thenReturn(new JournalpostId("44"));

        when(repositoryProvider.getInnsynRepository().hentForBehandling(behandling.getId())).thenReturn(Collections.singletonList(innsyn));
        when(repositoryProvider.getInnsynRepository().hentDokumenterForInnsyn(innsyn.getId())).thenReturn(Arrays.asList(innsynDokument1, innsynDokument2));
        ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = new ProduserIkkeredigerbartDokumentResponse();
        produserIkkeredigerbartDokumentResponse.setJournalpostId("randomJournalpostId");
        when(dokumentproduksjonConsumer.produserIkkeredigerbartDokument(any(ProduserIkkeredigerbartDokumentRequest.class)))
            .thenReturn(produserIkkeredigerbartDokumentResponse);

        ArgumentCaptor<KnyttVedleggTilForsendelseRequest> captor = ArgumentCaptor
            .forClass(KnyttVedleggTilForsendelseRequest.class);


        // act
        dokumentBestillerApplikasjonTjeneste.produserDokument(DOKUMENT_DATA_ID, HistorikkAktør.VEDTAKSLØSNINGEN, "");

        verify(dokumentproduksjonConsumer, times(1)).knyttVedleggTilForsendelse(captor.capture());

        assertThat(captor.getAllValues().get(0).getDokumentId()).isIn(innsynDokumentId);
    }

    // TODO(AndersPalfi): Finnes det ikke noen annen måte å lage denne testen på enn å mocke en haug av entiteter?  Er noe herk å refactorere.
    @Test
    public void skalIkkeLeggeTilDuplikateVedleggForskjelligInnsyn() throws Exception {

        // arrange
        DokumentMalType dokumentMalType = mock(DokumentMalType.class);
        DokumentData dokumentData = DokumentData.builder().medDokumentMalType(dokumentMalType).medBehandling(behandling).build();
        dokumentdataTjenesteMock.lagreDokumentData(dokumentData.getBehandling().getId(), new InnsynskravSvarDokument(brevParametere, "fritekst", InnsynResultatType.INNVILGET));

        InnsynEntitet innsyn = mock(InnsynEntitet.class);
        when(innsyn.getId()).thenReturn(2L);

        InnsynEntitet innsyn2 = mock(InnsynEntitet.class);
        when(innsyn2.getId()).thenReturn(3L);

        String innsynDokumentId = "3L";
        InnsynDokumentEntitet innsynDokument1 = mock(InnsynDokumentEntitet.class);
        when(innsynDokument1.isFikkInnsyn()).thenReturn(true);
        when(innsynDokument1.getDokumentId()).thenReturn(innsynDokumentId);
        when(innsynDokument1.getJournalpostId()).thenReturn(new JournalpostId("44"));

        InnsynDokumentEntitet innsynDokument2 = mock(InnsynDokumentEntitet.class);
        when(innsynDokument2.isFikkInnsyn()).thenReturn(true);
        when(innsynDokument2.getDokumentId()).thenReturn(innsynDokumentId);
        when(innsynDokument2.getJournalpostId()).thenReturn(new JournalpostId("44"));

        when(repositoryProvider.getInnsynRepository().hentForBehandling(behandling.getId())).thenReturn(Arrays.asList(innsyn, innsyn2));
        when(repositoryProvider.getInnsynRepository().hentDokumenterForInnsyn(innsyn.getId())).thenReturn(Collections.singletonList(innsynDokument1));
        when(repositoryProvider.getInnsynRepository().hentDokumenterForInnsyn(innsyn2.getId())).thenReturn(Collections.singletonList(innsynDokument2));

        ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = new ProduserIkkeredigerbartDokumentResponse();
        produserIkkeredigerbartDokumentResponse.setJournalpostId("randomJournalpostId");
        when(dokumentproduksjonConsumer.produserIkkeredigerbartDokument(any(ProduserIkkeredigerbartDokumentRequest.class)))
            .thenReturn(produserIkkeredigerbartDokumentResponse);


        ArgumentCaptor<KnyttVedleggTilForsendelseRequest> captor = ArgumentCaptor
            .forClass(KnyttVedleggTilForsendelseRequest.class);

        // act
        dokumentBestillerApplikasjonTjeneste.produserDokument(DOKUMENT_DATA_ID, HistorikkAktør.VEDTAKSLØSNINGEN, "");

        verify(dokumentproduksjonConsumer, times(1)).knyttVedleggTilForsendelse(captor.capture());

        assertThat(captor.getAllValues().get(0).getDokumentId()).isIn(innsynDokumentId);
    }

    @Test
    public void forhåndvisFPBeslutningsvedtakBrev() {
        Fagsak fagsak = FagsakBuilder.nyForeldrepengerForMor().
            medSaksnummer(new Saksnummer("01234567")).build();
        behandling = Behandling
            .nyBehandlingFor(fagsak, BehandlingType.UDEFINERT)
            .build();
        scenario.resetBehandling(behandling);
        BehandlingVedtak behandlingVedtak = mock(BehandlingVedtak.class);
        Behandlingsresultat behandlingsresultat = mock(Behandlingsresultat.class);
        // Angir de nødvendige betingelsene for å få riktig DokumentType instance
        when(behandlingVedtak.isBeslutningsvedtak()).thenReturn(true);
        when(behandlingsresultat.getBehandlingVedtak()).thenReturn(behandlingVedtak);
        behandling.setBehandlingresultat(behandlingsresultat);

        repositoryProvider.getFagsakRepository().opprettNy(fagsak);
        BehandlingLås lås = repositoryProvider.getBehandlingRepository().taSkriveLås(behandling);
        repositoryProvider.getBehandlingRepository().lagre(behandling, lås);

        DokumentMalType dokumentMalMock = mock(DokumentMalType.class);
        when(dokumentMalMock.getKode()).thenReturn(DokumentMalType.UENDRETUTFALL_DOK);
        when(dokumentRepository.hentDokumentMalType(DokumentMalType.UENDRETUTFALL_DOK)).thenReturn(dokumentMalMock);

        ArgumentCaptor<DokumentType> dokumentTypeArgumentCaptor = ArgumentCaptor.forClass(DokumentType.class);
        DokumentDataTjeneste dokumentDataTjenesteSpy = spy(dokumentdataTjenesteMock);
        dokumentBestillerApplikasjonTjeneste = new DokumentBestillerApplikasjonTjenesteImpl(
            dokumentproduksjonConsumer,
            dokumentDataTjenesteSpy,
            repositoryProvider,
            behandlingToDokumentbestillingDataMapper,
            dokumentToBrevDataMapper,
            brevHistorikkinnslag,
            behandlingskontrollTjeneste);

        BestillVedtakBrevDto bestillVedtakBrevDto = new BestillVedtakBrevDto(BEHANDLING_ID, null);
        bestillVedtakBrevDto.setSkalBrukeOverstyrendeFritekstBrev(false);
        dokumentBestillerApplikasjonTjeneste.forhandsvisVedtaksbrev(bestillVedtakBrevDto, (b -> false));
        //verifiserer UendretUtfallDokument sendes til DokumentDataTjeneste.lagreDokumentData
        verify(dokumentDataTjenesteSpy).lagreDokumentData(anyLong(), dokumentTypeArgumentCaptor.capture());
        assertThat(dokumentTypeArgumentCaptor.getAllValues().get(0)).isInstanceOf(UendretUtfallDokument.class);
    }

    @Test
    public void forhåndvisFPInnvilgetVedtakBrev() {
        Fagsak fagsak = FagsakBuilder.nyForeldrepengerForMor().
            medSaksnummer(new Saksnummer("01234567")).build();
        behandling = Behandling
            .nyBehandlingFor(fagsak, BehandlingType.UDEFINERT)
            .build();

        scenario.resetBehandling(behandling);
        Behandlingsresultat behandlingsresultat = mock(Behandlingsresultat.class);
        // Angir de nødvendige betingelsene for å få riktig DokumentType instance
        when(behandlingsresultat.isBehandlingsresultatInnvilget()).thenReturn(true);
        behandling.setBehandlingresultat(behandlingsresultat);

        repositoryProvider.getFagsakRepository().opprettNy(fagsak);
        BehandlingLås lås = repositoryProvider.getBehandlingRepository().taSkriveLås(behandling);
        repositoryProvider.getBehandlingRepository().lagre(behandling, lås);

        DokumentMalType dokumentMalMock = mock(DokumentMalType.class);
        when(dokumentMalMock.getKode()).thenReturn(DokumentMalType.INNVILGELSE_FORELDREPENGER_DOK);
        when(dokumentRepository.hentDokumentMalType(DokumentMalType.INNVILGELSE_FORELDREPENGER_DOK)).thenReturn(dokumentMalMock);

        ArgumentCaptor<DokumentType> dokumentTypeArgumentCaptor = ArgumentCaptor.forClass(DokumentType.class);
        DokumentDataTjeneste dokumentDataTjenesteSpy = spy(dokumentdataTjenesteMock);
        dokumentBestillerApplikasjonTjeneste = new DokumentBestillerApplikasjonTjenesteImpl(
            dokumentproduksjonConsumer,
            dokumentDataTjenesteSpy,
            repositoryProvider,
            behandlingToDokumentbestillingDataMapper,
            dokumentToBrevDataMapper,
            brevHistorikkinnslag,
            behandlingskontrollTjeneste);

        BestillVedtakBrevDto bestillVedtakBrevDto = new BestillVedtakBrevDto(BEHANDLING_ID, null);
        bestillVedtakBrevDto.setSkalBrukeOverstyrendeFritekstBrev(false);
        try {
            // Kaster en FunksjonellException pga DokumentdataTjenesteMock.lagreDokumentData
            // bruker ikke den riktige DokumentTypeDtoMapper i tilfelle av InnvilgelseForeldrepengerDokument
            dokumentBestillerApplikasjonTjeneste.forhandsvisVedtaksbrev(bestillVedtakBrevDto, (b -> false));
        } catch (RuntimeException ex) {
        } finally {
            //verifiserer InnvilgelseForeldrepengerDokument sendes til DokumentDataTjeneste.lagreDokumentData
            verify(dokumentDataTjenesteSpy).lagreDokumentData(anyLong(), dokumentTypeArgumentCaptor.capture());
            assertThat(dokumentTypeArgumentCaptor.getAllValues().get(0)).isInstanceOf(InnvilgelseForeldrepengerDokument.class);
        }
    }
}
