package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelseType.AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Refusjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.inntektsmelding.v1.MottattDokumentOversetterInntektsmelding;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.inntektsmelding.v1.MottattDokumentWrapperInntektsmelding;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.xml.MottattDokumentXmlParser;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.domene.virksomhet.impl.VirksomhetTjenesteImpl;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumer;

public class MottattDokumentOversetterInntektsmeldingTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private final OrganisasjonConsumer organisasjonConsumer = mock(OrganisasjonConsumer.class);
    private final FileToStringUtil fileToStringUtil = new FileToStringUtil();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());

    private final InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private MottatteDokumentRepository mottatteDokumentRepository = new MottatteDokumentRepositoryImpl(repositoryRule.getEntityManager());
    private MottattDokumentOversetterInntektsmelding oversetter;

    @Before
    public void setUp() throws Exception {
        final HentOrganisasjonResponse hentOrganisasjonResponse = new HentOrganisasjonResponse();
        final Organisasjon value = new Organisasjon();
        final UstrukturertNavn navn = new UstrukturertNavn();
        navn.getNavnelinje().add("Color Line");
        value.setNavn(navn);
        value.setOrgnummer("119999996");
        final OrganisasjonsDetaljer detaljer = new OrganisasjonsDetaljer();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        detaljer.setRegistreringsDato(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
        value.setOrganisasjonDetaljer(detaljer);
        hentOrganisasjonResponse.setOrganisasjon(value);
        when(organisasjonConsumer.hentOrganisasjon(any())).thenReturn(hentOrganisasjonResponse);
        VirksomhetTjeneste virksomhetTjeneste = new VirksomhetTjenesteImpl(organisasjonConsumer, new VirksomhetRepositoryImpl(repositoryRule.getEntityManager()));

        oversetter = new MottattDokumentOversetterInntektsmelding(repositoryProvider, virksomhetTjeneste);
    }

    @Test
    public void mappe_inntektsmelding_til_domene() throws IOException, URISyntaxException {
        final Behandling behandling = opprettScenarioOgLagreInntektsmelding("inntektsmelding.xml");

        final InntektArbeidYtelseGrunnlag grunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);

        assertThat(grunnlag).isNotNull();

        //Hent ut alle endringsrefusjoner fra alle inntektsmeldingene.
        List<Refusjon> endringerIRefusjon = grunnlag.getInntektsmeldinger()
            .map(InntektsmeldingAggregat::getInntektsmeldinger)
            .map(i -> i.stream()
                .flatMap(im -> im.getEndringerRefusjon().stream())
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());

        assertThat(endringerIRefusjon.size()).as("Forventer at vi har en endring i refusjon lagret fra inntektsmeldingen.").isEqualTo(1);
    }

    @Test
    public void skalVedMappingLeseBeløpPerMndForNaturalytelseForGjenopptakelseFraOpphørListe() throws IOException, URISyntaxException {
        final Behandling behandling = opprettScenarioOgLagreInntektsmelding("inntektsmelding_naturalytelse_gjenopptak_ignorer_belop.xml");

        final InntektArbeidYtelseGrunnlag grunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);

        // Hent opp alle naturalytelser
        List<NaturalYtelse> naturalYtelser = grunnlag.getInntektsmeldinger()
            .map(InntektsmeldingAggregat::getInntektsmeldinger)
            .map(e -> e.stream().flatMap(im -> im.getNaturalYtelser().stream()).collect(Collectors.toList()))
            .orElse(Collections.emptyList());

        assertThat(naturalYtelser.size()).as("Forventet fire naturalytelser, to opphørt og to gjenopptatt.").isEqualTo(4);

        assertThat(naturalYtelser.stream().map(e -> e.getType()).collect(Collectors.toList())).containsOnly(AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS, ELEKTRISK_KOMMUNIKASJON);
        assertThat(naturalYtelser.stream().map(e -> e.getBeloepPerMnd())).containsOnly(new Beløp(100));
    }

    @Test
    public void skalMappeOgPersistereKorrektInnsendingsdato() throws IOException, URISyntaxException {
        // Arrange
        final Behandling behandling = opprettBehandling();
        MottattDokument mottattDokument = opprettDokument(behandling, "inntektsmelding.xml");

        final MottattDokumentWrapperInntektsmelding wrapper = (MottattDokumentWrapperInntektsmelding) MottattDokumentXmlParser.unmarshallXml(mottattDokument.getPayloadXml());

        // Act
        oversetter.trekkUtDataOgPersister(wrapper, mottattDokument, behandling, Optional.empty());

        // Assert
        final InntektArbeidYtelseGrunnlag grunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);

        Optional<LocalDateTime> innsendingstidspunkt = grunnlag.getInntektsmeldinger()
            .map(InntektsmeldingAggregat::getInntektsmeldinger)
            .stream().flatMap(e -> e.stream().map(it -> it.getInnsendingstidspunkt()))
            .collect(Collectors.toList()).stream().findFirst();

        assertThat(innsendingstidspunkt).isPresent();
        assertThat(innsendingstidspunkt).hasValue(wrapper.getInnsendingstidspunkt());

    }

    @Test
    public void skalVedMottakAvNyInntektsmeldingPåSammeArbeidsforholdIkkeOverskriveHvisPersistertErNyereEnnMottatt() throws IOException, URISyntaxException {
        // Arrange
        final Behandling behandling = opprettBehandling();
        MottattDokument mottattDokument = opprettDokument(behandling, "inntektsmelding.xml");
        MottattDokumentWrapperInntektsmelding wrapper = (MottattDokumentWrapperInntektsmelding) MottattDokumentXmlParser.unmarshallXml(mottattDokument.getPayloadXml());

        MottattDokumentWrapperInntektsmelding wrapperSpied = Mockito.spy(wrapper);

        LocalDateTime nyereDato = LocalDateTime.now();
        LocalDateTime eldreDato = nyereDato.minusMinutes(1);

        // Act
        // Motta nyere inntektsmelding først
        Mockito.doReturn(nyereDato).when(wrapperSpied).getInnsendingstidspunkt();
        oversetter.trekkUtDataOgPersister(wrapperSpied, mottattDokument, behandling, Optional.empty());

        // Så motta eldre inntektsmelding
        Mockito.doReturn(eldreDato).when(wrapperSpied).getInnsendingstidspunkt();
        oversetter.trekkUtDataOgPersister(wrapperSpied, mottattDokument, behandling, Optional.empty());

        // Assert
        final InntektArbeidYtelseGrunnlag grunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);

        Optional<LocalDateTime> innsendingstidspunkt = grunnlag.getInntektsmeldinger()
            .map(InntektsmeldingAggregat::getInntektsmeldinger)
            .stream().flatMap(e -> e.stream().map(it -> it.getInnsendingstidspunkt()))
            .collect(Collectors.toList()).stream().findFirst();

        assertThat(innsendingstidspunkt).isPresent();
        assertThat(innsendingstidspunkt).hasValue(nyereDato);
        assertThat(grunnlag.getInntektsmeldinger().map(InntektsmeldingAggregat::getInntektsmeldinger).get()).hasSize(1);

    }

    @Test
    public void skalVedMottakAvNyInntektsmeldingPåSammeArbeidsforholdOverskriveHvisPersistertErEldreEnnMottatt() throws IOException, URISyntaxException {
        // Arrange
        final Behandling behandling = opprettBehandling();
        MottattDokument mottattDokument = opprettDokument(behandling, "inntektsmelding.xml");
        MottattDokumentWrapperInntektsmelding wrapper = (MottattDokumentWrapperInntektsmelding) MottattDokumentXmlParser.unmarshallXml(mottattDokument.getPayloadXml());

        MottattDokumentWrapperInntektsmelding wrapperSpied = Mockito.spy(wrapper);

        LocalDateTime nyereDato = LocalDateTime.now();
        LocalDateTime eldreDato = nyereDato.minusMinutes(1);

        // Act
        // Motta eldre inntektsmelding først
        Mockito.doReturn(eldreDato).when(wrapperSpied).getInnsendingstidspunkt();
        oversetter.trekkUtDataOgPersister(wrapperSpied, mottattDokument, behandling, Optional.empty());

        // Så motta nyere inntektsmelding
        Mockito.doReturn(nyereDato).when(wrapperSpied).getInnsendingstidspunkt();
        oversetter.trekkUtDataOgPersister(wrapperSpied, mottattDokument, behandling, Optional.empty());

        // Assert
        final InntektArbeidYtelseGrunnlag grunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);

        Optional<LocalDateTime> innsendingstidspunkt = grunnlag.getInntektsmeldinger()
            .map(InntektsmeldingAggregat::getInntektsmeldinger)
            .stream().flatMap(e -> e.stream().map(it -> it.getInnsendingstidspunkt()))
            .collect(Collectors.toList()).stream().findFirst();

        assertThat(innsendingstidspunkt).isPresent();
        assertThat(innsendingstidspunkt).hasValue(nyereDato);
        assertThat(grunnlag.getInntektsmeldinger().map(InntektsmeldingAggregat::getInntektsmeldinger).get()).hasSize(1);
    }

    private Behandling opprettScenarioOgLagreInntektsmelding(String inntektsmeldingFilnavn) throws URISyntaxException, IOException {
        final Behandling behandling = opprettBehandling();
        MottattDokument mottattDokument = opprettDokument(behandling, inntektsmeldingFilnavn);

        final MottattDokumentWrapperInntektsmelding wrapper = (MottattDokumentWrapperInntektsmelding) MottattDokumentXmlParser.unmarshallXml(mottattDokument.getPayloadXml());

        oversetter.trekkUtDataOgPersister(wrapper, mottattDokument, behandling, Optional.empty());
        return behandling;
    }

    private Behandling opprettBehandling() {
        final ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        return scenario.lagre(repositoryProvider);
    }

    private MottattDokument opprettDokument(Behandling behandling, String inntektsmeldingFilnavn) throws IOException, URISyntaxException {
        final InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseRepository.opprettBuilderFor(behandling, VersjonType.REGISTER);
        inntektArbeidYtelseRepository.lagre(behandling, inntektArbeidYtelseAggregatBuilder);
        final String xml = fileToStringUtil.readFile(inntektsmeldingFilnavn);
        final MottattDokument.Builder builder = new MottattDokument.Builder();

        MottattDokument mottattDokument = builder.medDokumentTypeId(DokumentTypeId.INNTEKTSMELDING)
            .medFagsakId(behandling.getFagsakId())
            .medMottattDato(LocalDate.now())
            .medBehandlingId(behandling.getId())
            .medElektroniskRegistrert(true)
            .medJournalPostId(new JournalpostId("123123123"))
            .medDokumentId("123123")
            .medXmlPayload(xml)
            .build();

        mottatteDokumentRepository.lagre(mottattDokument);
        return mottattDokument;
    }
}
