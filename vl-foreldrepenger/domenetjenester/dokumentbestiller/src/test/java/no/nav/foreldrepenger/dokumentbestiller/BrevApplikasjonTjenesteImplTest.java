package no.nav.foreldrepenger.dokumentbestiller;

import static no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType.REVURDERING_DOK;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.BEHANDLINGSTYPE;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.FRITEKST;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.PERSON_STATUS;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.SØKNAD_DATO;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.YTELSE_TYPE;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.InnhenteOpplysningerDokument.FLETTEFELT_SOKERS_NAVN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioKlageEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametereImpl;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InnhenteOpplysningerDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.KlageOversendtKlageinstansDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentBehandlingsresultatMapper;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentTypeDtoMapper;
import no.nav.foreldrepenger.dokumentbestiller.brev.DokumentToBrevDataMapper;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.impl.FamilieHendelseTjenesteImpl;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.uttak.InfoOmResterendeDagerTjeneste;
import no.nav.foreldrepenger.domene.uttak.OpphørFPTjeneste;
import no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.BeregnEkstraFlerbarnsukerTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.PersonstatusKode;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.dokument.produksjon.DokumentproduksjonConsumer;

public class BrevApplikasjonTjenesteImplTest {
    @Mock
    private DokumentDataTjeneste dokumentDataTjenesteMock;
    @Mock
    private MottatteDokumentRepository mottatteDokumentRepository;
    @Mock
    private MottattDokument mottattDokument;
    @Mock
    private DokumentMalType revurderingDok;
    @Mock
    private DokumentproduksjonConsumer dokumentproduksjonConsumerMock;
    @Mock
    private HistorikkRepository historikkRepositoryMock;
    @Mock
    private DokumentData dokumentData;
    @Mock
    private LandkodeOversetter landkodeOversetter;
    @Mock
    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    @Mock
    private FamilieHendelseTjeneste familieHendelseTjeneste;
    @Mock
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    @Mock
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;
    @Mock
    private BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste;
    @Mock
    private OpphørFPTjeneste opphørFPTjeneste;
    @Mock
    private InfoOmResterendeDagerTjeneste infoOmResterendeDagerTjeneste;

    private Behandling behandling;
    private BehandlingRepositoryProvider repositoryProvider;
    private DokumentBestillerApplikasjonTjeneste tjeneste;
    private DokumentTypeDtoMapper dtoMapper;

    private DokumentMapperTjenesteProvider dokumentMapperTjenesteProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private void initBehandling(AbstractTestScenario<?> scenario) {
        this.behandling = scenario.lagMocked();
        this.repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        initBehandling(repositoryProvider);
    }

    private void initBehandling(BehandlingRepositoryProvider repositoryProvider) {
        familieHendelseTjeneste = new FamilieHendelseTjenesteImpl(personopplysningTjeneste, 16, 4, repositoryProvider);
        when(dokumentDataTjenesteMock.hentDokumentMalType(REVURDERING_DOK)).thenReturn(revurderingDok);

        BrevParametere brevParametere = new BrevParametereImpl(6, 3, Period.ofWeeks(3), Period.ofWeeks(2));

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
            beregnEkstraFlerbarnsukerTjeneste,
            opphørFPTjeneste,
            infoOmResterendeDagerTjeneste);

        DokumentBehandlingsresultatMapper behandlingsresultatMapper = new DokumentBehandlingsresultatMapper(repositoryProvider, tjenesteProvider);


        dtoMapper = new DokumentTypeDtoMapper(repositoryProvider,
            tjenesteProvider,
            brevParametere, behandlingsresultatMapper);

        when(dokumentDataTjenesteMock.getBrevParametere()).thenReturn(dtoMapper.getBrevParametere());
        BehandlingToDokumentbestillingDataMapper behandlingToDokumentbestillingDataMapper = new BehandlingToDokumentbestillingDataMapper(landkodeOversetter);
        DokumentToBrevDataMapper dokumentToBrevDataMapper = new DokumentToBrevDataMapper(repositoryProvider);
        BrevHistorikkinnslag brevHistorikkinnslag = new BrevHistorikkinnslag(dokumentDataTjenesteMock, historikkRepositoryMock);

        tjeneste = new DokumentBestillerApplikasjonTjenesteImpl(
            dokumentproduksjonConsumerMock,
            dokumentDataTjenesteMock,
            repositoryProvider,
            behandlingToDokumentbestillingDataMapper,
            dokumentToBrevDataMapper,
            brevHistorikkinnslag,
            behandlingskontrollTjeneste);
    }

    private void initBehandling(ScenarioKlageEngangsstønad scenario) {
        this.behandling = scenario.lagMocked();
        this.repositoryProvider = scenario.mockBehandlingRepositoryProvider();

        Søknad søknad = new SøknadEntitet.Builder().medMottattDato(LocalDate.now()).build();
        SøknadRepository søknadRepository = mock(SøknadRepository.class);
        when(repositoryProvider.getSøknadRepository()).thenReturn(søknadRepository);
        when(repositoryProvider.getSøknadRepository().hentSøknadHvisEksisterer(any(Behandling.class))).thenReturn(Optional.ofNullable(søknad));
        when(repositoryProvider.getSøknadRepository().hentSøknadHvisEksisterer(any(Long.class))).thenReturn(Optional.ofNullable(søknad));

        initBehandling(repositoryProvider);
    }

    @Test
    public void lagrerDokumentDataForInnhenteOpplysningerBrevEngangsstønad() {
        Long dokumentdataId = 123L;
        LocalDate mottattDato = LocalDate.now().minusWeeks(1);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medSøknadDato(mottattDato)
            .medFødselAdopsjonsdato(Collections.singletonList(mottattDato));

        initBehandling(scenario);
        when(dokumentDataTjenesteMock.lagreDokumentData(anyLong(), any())).thenReturn(dokumentdataId);

        BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), DokumentMalType.INNHENT_DOK, "Dette er en fritekst");

        Long id = tjeneste.lagreDokumentdata(bestillBrevDto);
        assertThat(id).isEqualTo(dokumentdataId);

        ArgumentCaptor<Long> behandlingIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<InnhenteOpplysningerDokument> dokument = ArgumentCaptor.forClass(InnhenteOpplysningerDokument.class);
        verify(dokumentDataTjenesteMock).lagreDokumentData(behandlingIdCaptor.capture(), dokument.capture());
        assertThat(behandlingIdCaptor.getValue()).isEqualTo(behandling.getId());
        String navn = "Test Navn";
        String type = PersonstatusKode.DOD.value();
        List<Flettefelt> flettefelter = dokument.getValue().getFlettefelter(mapToDto(behandling, navn, type));
        assertThat(flettefelter.size()).isEqualTo(7);

        Optional<Flettefelt> fritekstFlettefelt = flettefelter.stream().filter(f -> f.getFeltnavn().equals(FRITEKST))
            .findFirst();
        assertThat(fritekstFlettefelt).isPresent();
        assertThat(fritekstFlettefelt.get().getFeltverdi()).isEqualTo(bestillBrevDto.getFritekst());
        Optional<Flettefelt> mottattDatoFlettefelt = flettefelter.stream().filter(f -> f.getFeltnavn().equals(SØKNAD_DATO))
            .findFirst();
        assertThat(mottattDatoFlettefelt).isPresent();
        assertThat(mottattDatoFlettefelt.get().getFeltverdi()).isEqualTo(mottattDato.toString());
        Optional<Flettefelt> personStatusFlettefelt = flettefelter.stream().filter(f -> f.getFeltnavn().equals(PERSON_STATUS))
            .findFirst();
        assertThat(personStatusFlettefelt).isPresent();
        assertThat(personStatusFlettefelt.get().getFeltverdi()).isEqualToIgnoringCase(type);
        Optional<Flettefelt> ytelsesTypeFlettefelt = flettefelter.stream().filter(f -> f.getFeltnavn().equals(YTELSE_TYPE))
            .findFirst();
        assertThat(ytelsesTypeFlettefelt).isPresent();
        assertThat(ytelsesTypeFlettefelt.get().getFeltverdi()).isEqualToIgnoringCase("ES");
        Optional<Flettefelt> behandlingsTypeFlettefelt = flettefelter.stream().filter(f -> f.getFeltnavn().equals(BEHANDLINGSTYPE))
            .findFirst();
        assertThat(behandlingsTypeFlettefelt).isPresent();
        assertThat(behandlingsTypeFlettefelt.get().getFeltverdi()).isEqualToIgnoringCase(BehandlingType.FØRSTEGANGSSØKNAD.getKode());
        Optional<Flettefelt> søkersNavnFlettefelt = flettefelter.stream().filter(f -> f.getFeltnavn().equals(FLETTEFELT_SOKERS_NAVN))
            .findFirst();
        assertThat(søkersNavnFlettefelt).isPresent();
        assertThat(søkersNavnFlettefelt.get().getFeltverdi()).isEqualToIgnoringCase(navn);
    }

    @Test
    public void lagrerDokumentDataForInnhenteOpplysningerBrevForeldrepenger() {
        Long dokumentdataId = 123L;
        LocalDate mottattDato = LocalDate.now().minusWeeks(1);

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medSøknadDato(mottattDato);

        initBehandling(scenario);
        when(dokumentDataTjenesteMock.lagreDokumentData(anyLong(), any())).thenReturn(dokumentdataId);

        BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), DokumentMalType.INNHENT_DOK, "Dette er en fritekst");

        Long id = tjeneste.lagreDokumentdata(bestillBrevDto);
        assertThat(id).isEqualTo(dokumentdataId);

        ArgumentCaptor<Long> behandlingIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<InnhenteOpplysningerDokument> dokument = ArgumentCaptor.forClass(InnhenteOpplysningerDokument.class);
        verify(dokumentDataTjenesteMock).lagreDokumentData(behandlingIdCaptor.capture(), dokument.capture());
        assertThat(behandlingIdCaptor.getValue()).isEqualTo(behandling.getId());
        List<Flettefelt> flettefelter = dokument.getValue().getFlettefelter(mapToDto(behandling));
        assertThat(flettefelter.size()).isEqualTo(7);

        Optional<Flettefelt> ytelsesTypeFlettefelt = flettefelter.stream().filter(f -> f.getFeltnavn().equals(YTELSE_TYPE))
            .findFirst();
        assertThat(ytelsesTypeFlettefelt).isPresent();
        assertThat(ytelsesTypeFlettefelt.get().getFeltverdi()).isEqualToIgnoringCase("FP");
    }

    @Test
    public void lagrerDokumentDataForKlageOverføringBrev() {
        Long dokumentdataId = 123L;
        LocalDate mottattDato = LocalDate.now().minusWeeks(1);

        AbstractTestScenario<?> scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medSøknadDato(mottattDato);
        ScenarioKlageEngangsstønad klageScenario = ScenarioKlageEngangsstønad.forStadfestetNFP(scenario);
        initBehandling(klageScenario);

        when(dokumentDataTjenesteMock.lagreDokumentData(anyLong(), any())).thenReturn(dokumentdataId);

        BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), DokumentMalType.KLAGE_OVERSENDT_KLAGEINSTANS_DOK,
            "Dette er en fritekst");

        Long id = tjeneste.lagreDokumentdata(bestillBrevDto);
        assertThat(id).isEqualTo(dokumentdataId);

        ArgumentCaptor<Long> behandlingIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<KlageOversendtKlageinstansDokument> dokument = ArgumentCaptor.forClass(KlageOversendtKlageinstansDokument.class);
        verify(dokumentDataTjenesteMock).lagreDokumentData(behandlingIdCaptor.capture(), dokument.capture());
        assertThat(behandlingIdCaptor.getValue()).isEqualTo(behandling.getId());
        List<Flettefelt> flettefelter = dokument.getValue().getFlettefelter(mapToDto(behandling));
        assertThat(flettefelter.size()).isEqualTo(5);

        Optional<Flettefelt> fritekstFlettefelt = flettefelter.stream()
            .filter(f -> f.getFeltnavn().equals(FRITEKST)).findFirst();
        assertThat(fritekstFlettefelt).isPresent();
        assertThat(fritekstFlettefelt.get().getFeltverdi()).isEqualTo(bestillBrevDto.getFritekst());
        Optional<Flettefelt> ytelseTypeFlettefelt = flettefelter.stream()
            .filter(f -> f.getFeltnavn().equals(YTELSE_TYPE)).findFirst();
        assertThat(ytelseTypeFlettefelt).isPresent();
        assertThat(ytelseTypeFlettefelt.get().getFeltverdi()).isEqualTo(behandling.getFagsak().getYtelseType().getKode());
    }

    @Test
    public void mapperPersonstatus() {
        AbstractTestScenario<?> scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        initBehandling(scenario);

        String personstatus = "DOD";
        DokumentTypeDto dto = dtoMapper.mapToDto(behandling, "Søkers Navn", personstatus);

        assertThat(dto.getPersonstatus()).isEqualTo(personstatus);
    }

    @Test
    public void mapperSøkersNavn() {
        AbstractTestScenario<?> scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        initBehandling(scenario);

        String søkersNavn = "Søkers Navn";
        DokumentTypeDto dto = dtoMapper.mapToDto(behandling, søkersNavn, "status");

        assertThat(dto.getSøkersNavn()).isEqualTo(søkersNavn);
    }

    private DokumentTypeDto mapToDto(Behandling behandling) {
        return dtoMapper.mapToDto(behandling, "navn", "ANNET");
    }

    private DokumentTypeDto mapToDto(Behandling behandling, String navn, String type) {
        return dtoMapper.mapToDto(behandling, navn, type);
    }

    @Test
    public void lagrerDokumentDataForInnhenteOpplysningerBrevForBehandlingUtenSøknad() {
        Long dokumentdataId = 123L;
        LocalDate mottattDato = LocalDate.now().minusDays(3);

        AbstractTestScenario<?> scenario = ScenarioMorSøkerEngangsstønad.forFødselUtenSøknad()
            .medSøknadDato(mottattDato)
            .medFødselAdopsjonsdato(Collections.singletonList(mottattDato));
        initBehandling(scenario);

        when(dokumentDataTjenesteMock.lagreDokumentData(anyLong(), any())).thenReturn(dokumentdataId);

        when(mottattDokument.getDokumentTypeId())
            .thenReturn(no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        when(mottattDokument.getMottattDato()).thenReturn(LocalDate.now().minusDays(3));
        List<MottattDokument> mottatteDokumenter = new ArrayList<>();
        mottatteDokumenter.add(mottattDokument);
        when(mottatteDokumentRepository.hentMottatteDokument(behandling.getId())).thenReturn(mottatteDokumenter);

        BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), DokumentMalType.INNHENT_DOK, "Dette er en fritekst");
        Long id = tjeneste.lagreDokumentdata(bestillBrevDto);
        assertThat(id).isEqualTo(dokumentdataId);

        ArgumentCaptor<Long> behandlingIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<InnhenteOpplysningerDokument> dokument = ArgumentCaptor.forClass(InnhenteOpplysningerDokument.class);
        verify(dokumentDataTjenesteMock).lagreDokumentData(behandlingIdCaptor.capture(), dokument.capture());
        assertThat(behandlingIdCaptor.getValue()).isEqualTo(behandling.getId());
        List<Flettefelt> flettefelter = dokument.getValue().getFlettefelter(mapToDto(behandling));
        assertThat(flettefelter.size()).isEqualTo(7);

        Optional<Flettefelt> fritekstFlettefelt = flettefelter.stream().filter(f -> f.getFeltnavn().equals(FRITEKST))
            .findFirst();
        assertThat(fritekstFlettefelt).isPresent();
        assertThat(fritekstFlettefelt.get().getFeltverdi()).isEqualTo(bestillBrevDto.getFritekst());
        Optional<Flettefelt> mottattDatoFlettefelt = flettefelter.stream().filter(f -> f.getFeltnavn().equals(SØKNAD_DATO))
            .findFirst();
        assertThat(mottattDatoFlettefelt).isPresent();
        assertThat(mottattDatoFlettefelt.get().getFeltverdi()).isEqualTo(mottattDato.toString());
    }

    @Test(expected = VLException.class)
    public void lagreDokumentdataKasterFeilVedUgyldigMalId() {
        AbstractTestScenario<?> scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        initBehandling(scenario);

        BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), "Ugyldig");
        tjeneste.lagreDokumentdata(bestillBrevDto);
    }

    @Test
    public void dokumentErProdusert() {
        List<DokumentData> dokumentDataList = new ArrayList<>();

        AbstractTestScenario<?> scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        initBehandling(scenario);

        DokumentData dokumentDataMedProdusertDok = DokumentData.builder().medBehandling(behandling).medBestiltTid(LocalDateTime.now())
            .medDokumentMalType(revurderingDok).build();
        dokumentDataList.add(dokumentDataMedProdusertDok);

        when(dokumentDataTjenesteMock.hentDokumentDataListe(anyLong(), any())).thenReturn(dokumentDataList);
        assertThat(tjeneste.erDokumentProdusert(1L, REVURDERING_DOK)).isTrue();
    }

    @Test
    public void dokumentErIKKEProdusert() {
        AbstractTestScenario<?> scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        initBehandling(scenario);

        List<DokumentData> dokumentDataList = new ArrayList<>();

        DokumentData dokumentDataMedProdusertDok = DokumentData.builder().medBehandling(behandling).medBestiltTid(null)
            .medDokumentMalType(revurderingDok).build();
        dokumentDataList.add(dokumentDataMedProdusertDok);

        when(dokumentDataTjenesteMock.hentDokumentDataListe(anyLong(), any())).thenReturn(dokumentDataList);
        assertThat(tjeneste.erDokumentProdusert(1L, REVURDERING_DOK)).isFalse();
    }

    @Test
    public void dokumentErIkkeProdusert() {
        AbstractTestScenario<?> scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        initBehandling(scenario);

        List<DokumentData> dokumentDataList = new ArrayList<>();

        DokumentData dokumentDataUtenProdusertDok = DokumentData.builder().medBehandling(behandling).medDokumentMalType(revurderingDok)
            .build();
        dokumentDataList.add(dokumentDataUtenProdusertDok);

        when(dokumentDataTjenesteMock.hentDokumentDataListe(anyLong(), any())).thenReturn(dokumentDataList);
        assertThat(tjeneste.erDokumentProdusert(1L, REVURDERING_DOK)).isFalse();
    }

    @Test
    public void returnererForhandsvisning() {
        AbstractTestScenario<?> scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        initBehandling(scenario);

        long dokumentdataId = 2L;
        byte[] dokument = "forhåndsvisning".getBytes();
        DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste = mock(DokumentBestillerApplikasjonTjenesteImpl.class);
        when(dokumentBestillerApplikasjonTjeneste.lagreDokumentdata(any())).thenReturn(dokumentdataId);
        when(dokumentBestillerApplikasjonTjeneste.forhandsvisDokument(dokumentdataId)).thenReturn(dokument);
        when(dokumentBestillerApplikasjonTjeneste.hentForhåndsvisningDokument(any())).thenCallRealMethod();

        BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), REVURDERING_DOK, "fritekst");
        byte[] bytes = dokumentBestillerApplikasjonTjeneste.hentForhåndsvisningDokument(bestillBrevDto);
        assertThat(bytes).isEqualTo(dokument);

        ArgumentCaptor<BestillBrevDto> argumentCaptor = ArgumentCaptor.forClass(BestillBrevDto.class);
        verify(dokumentBestillerApplikasjonTjeneste).lagreDokumentdata(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getBrevmalkode()).isEqualTo(DokumentMalType.REVURDERING_DOK);

        verify(dokumentBestillerApplikasjonTjeneste).forhandsvisDokument(dokumentdataId);
    }

    @Test
    public void bestillerDokument() {
        AbstractTestScenario<?> scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        initBehandling(scenario);

        Long dokumentdataId = 111L;
        String dokumentMalTypeInput = DokumentMalType.INNHENT_DOK;
        BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), dokumentMalTypeInput, "fritekst");
        mockDokumentData(dokumentdataId, dokumentMalTypeInput);

        tjeneste.bestillDokument(bestillBrevDto, HistorikkAktør.SAKSBEHANDLER);

        ArgumentCaptor<Long> behandlingIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<DokumentType> dokumentTypeArgumentCaptor = ArgumentCaptor.forClass(DokumentType.class);
        verify(dokumentDataTjenesteMock).lagreDokumentData(behandlingIdArgumentCaptor.capture(), dokumentTypeArgumentCaptor.capture());
        assertThat(behandlingIdArgumentCaptor.getValue()).isEqualTo(behandling.getId());
        assertThat(dokumentTypeArgumentCaptor.getValue().getDokumentMalType()).isEqualTo(dokumentMalTypeInput);

        verify(dokumentDataTjenesteMock).opprettDokumentBestillerTask(dokumentdataId, HistorikkAktør.VEDTAKSLØSNINGEN, null);

        ArgumentCaptor<Historikkinnslag> historikkinnslagCaptor = ArgumentCaptor.forClass(Historikkinnslag.class);
        verify(historikkRepositoryMock).lagre(historikkinnslagCaptor.capture());
        Historikkinnslag historikkinnslag = historikkinnslagCaptor.getValue();
        assertThat(historikkinnslag.getType()).isEqualTo(HistorikkinnslagType.BREV_BESTILT);
    }

    private void mockDokumentData(Long dokumentdataId, String dokumentMalTypeInput) {
        when(dokumentDataTjenesteMock.lagreDokumentData(eq(behandling.getId()), any(DokumentType.class))).thenReturn(dokumentdataId);
        when(dokumentData.getBehandling()).thenReturn(behandling);
        DokumentMalType dokumentMalType = mock(DokumentMalType.class);
        when(dokumentMalType.getKode()).thenReturn(dokumentMalTypeInput);
        when(dokumentDataTjenesteMock.hentDokumentMalType(eq(dokumentMalTypeInput))).thenReturn(dokumentMalType);
        when(dokumentData.getDokumentMalType()).thenReturn(dokumentMalType);
        when(dokumentDataTjenesteMock.hentDokumentData(dokumentdataId)).thenReturn(dokumentData);
    }
}
