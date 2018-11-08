package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.foreldrepenger;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_VURDERING_AV_KLAGE_NFP;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.REGISTRER_PAPIRSØKNAD_ENGANGSSTØNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.REGISTRER_PAPIRSØKNAD_FORELDREPENGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.REGISTRER_PAPIR_ENDRINGSØKNAD_FORELDREPENGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VENT_PÅ_SØKNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.AVBRUTT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostFleksibeltUttakSøknadBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostInntektsmeldingBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostPapirSøknadBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostSøknadBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostUtenMetadataBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo.STD_KVINNE_AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingsprosess.automatiskgjenopptagelse.tjeneste.GjenopptaBehandlingTask;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.JournalConsumerMock;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.ArbeidsforholdTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.FørstegangssøknadTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InntektTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.FordelRestTjenesteTestAPI;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.OpprettOppgaveVurderDokumentTask;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataEndringshåndterer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;

@RunWith(CdiRunner.class)
public class InntektsmeldingMottakTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    @Inject
    private RegisterdataEndringshåndterer registerdataOppdaterer;
    @Inject
    private FordelRestTjenesteTestAPI fordelRestTjenesteAPI;
    @Inject
    private RegisterKontekst registerKontekst;

    private BehandlendeEnhetTjeneste enhetsTjeneste;

    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    private OrganisasjonsEnhet organisasjonsEnhet = new OrganisasjonsEnhet("2103", "NAV Viken");

    @Before
    public void setup() throws SQLException {
        enhetsTjeneste = mock(BehandlendeEnhetTjeneste.class);
        registerKontekst.intialiser();
        System.setProperty("dato.for.nye.beregningsregler", "2010-01-01");
    }

    @After
    public void teardown() {
        registerKontekst.nullstill();
        System.setProperty("dato.for.nye.beregningsregler", "2019-01-01");
    }

    @Test
    public void behandling_startet_med_inntktsmelding_mottar_søknad_senere() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Send inn inntektsmelding som oppretter ny behandling (uten søknad) -> skal settes på vent
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent())
            .medGradering(fødselsdatoBarn.plusWeeks(10), fødselsdatoBarn.plusWeeks(25).minusDays(1), BigDecimal.valueOf(50))
            .build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(VENT_PÅ_SØKNAD, OPPRETTET));

        // Arrange/act steg 2: Send inn inntektsmelding -> behandling skal fortsatt stå på vent
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(VENT_PÅ_SØKNAD, OPPRETTET));

        // Arrange steg 3: Send inn søknad -> behandling skal tas av vent
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselGradertUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(BehandlingResultatType.INNVILGET);
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(VENT_PÅ_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)
        );
    }

    @Test
    public void behandling_startet_med_inntktsmelding_uten_mottatt_søknad_innen_frist() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now();
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Send inn inntektsmelding som oppretter ny behandling (uten søknad) -> skal settes på vent
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(VENT_PÅ_SØKNAD, OPPRETTET));

        // Arrange steg 2: Simuler at frist til utløper, deretter kjør GjenopptaBehandlingTask
        simulerAtAutopunktFristErPasert();
        behandling = behandlingRepository.hentBehandling(behandlingId);

        when(enhetsTjeneste.sjekkEnhetVedGjenopptak(any())).thenReturn(Optional.of(organisasjonsEnhet));

        ProsessTaskHandler handler = new GjenopptaBehandlingTask(behandlingRepository, behandlingskontrollTjeneste, registerdataOppdaterer, enhetsTjeneste);
        ProsessTaskData data = new ProsessTaskData("BVL004");
        data.setBehandling(fagsak.getId(), behandling.getId(), STD_KVINNE_AKTØR_ID.getId());

        // Act
        handler.doTask(data);

        // Assert
        assertThat(behandling.isBehandlingHenlagt()).isTrue();
        assertThat(behandling.getBehandlendeEnhet()).isEqualTo(organisasjonsEnhet.getEnhetId());
        assertThat(behandling.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(BehandlingResultatType.HENLAGT_SØKNAD_MANGLER);
    }

    @Test
    public void send_inn_papirsøknad_fra_gosys() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now();
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Send inn papirsøknad
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostPapirSøknadBuilder(fagsak, repositoryProvider).medForsendelseMottatt(LocalDate.now());

        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(REGISTRER_PAPIRSØKNAD_FORELDREPENGER, OPPRETTET));
    }

    @Test
    public void send_inn_papirsøknad_ES() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now();
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        // Arrange steg 1: Send inn papirsøknad
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostUtenMetadataBuilder(fagsak, repositoryProvider)
            .medForsendelseMottatt(LocalDate.now()).medBehandlingsTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL).medDokumentKategori(DokumentKategori.SØKNAD);

        // Act
        JournalConsumerMock.setEmulerManglendeDokumentTypeId(true);
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);
        JournalConsumerMock.setEmulerManglendeDokumentTypeId(false);

        // Assert
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(REGISTRER_PAPIRSØKNAD_ENGANGSSTØNAD, OPPRETTET));
    }

    @Test
    public void behandling_startet_med_inntktsmelding_mottar_papirsøknad_senere() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Send inn inntektsmelding som oppretter ny behandling (uten søknad) -> skal settes på vent
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent())
            .medGradering(fødselsdatoBarn.plusWeeks(10), fødselsdatoBarn.plusWeeks(25).minusDays(1), BigDecimal.valueOf(50))
            .build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(VENT_PÅ_SØKNAD, OPPRETTET));

        // Arrange steg 2: Send inn papirsøknad -> behandling skal tas av vent
        JournalpostMottakDtoBuilder journalpostPapirSøknadBuilder = journalpostPapirSøknadBuilder(fagsak, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostPapirSøknadBuilder);

        // Assert
        behandling = behandlingRepository.hentBehandling(behandlingId);
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(REGISTRER_PAPIRSØKNAD_FORELDREPENGER, OPPRETTET),
            AksjonspunktTestutfall.resultat(VENT_PÅ_SØKNAD, UTFØRT)
        );

        // Arrange steg 3: Send inn elektronisk søknad -> behandling skal henlegges og ny opprettes?
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselGradertUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long revurderingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(BehandlingResultatType.MERGET_OG_HENLAGT);
        behandling = behandlingRepository.hentBehandling(revurderingId);
        assertThat(behandling.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(BehandlingResultatType.INNVILGET);
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(REGISTRER_PAPIRSØKNAD_FORELDREPENGER, AVBRUTT),
            AksjonspunktTestutfall.resultat(VENT_PÅ_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)
        );

        // Arrange steg 4: Send inn papirsøknad - fleksibelt uttak
        JournalpostMottakDtoBuilder journalpostFleksibeltUttakSøknadBuilder = journalpostFleksibeltUttakSøknadBuilder(fagsak, repositoryProvider);

        // Act
        Long endringRevurderingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostFleksibeltUttakSøknadBuilder);

        // Assert
        behandling = behandlingRepository.hentBehandling(endringRevurderingId);
        assertThat(behandling.getType()).isEqualTo(BehandlingType.REVURDERING);
        assertThat(behandling.getBehandlingÅrsaker().stream().map(BehandlingÅrsak::getBehandlingÅrsakType).anyMatch(BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER::equals)).isTrue();
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(REGISTRER_PAPIRSØKNAD_FORELDREPENGER, AVBRUTT),
            AksjonspunktTestutfall.resultat(REGISTRER_PAPIR_ENDRINGSØKNAD_FORELDREPENGER, OPPRETTET),
            AksjonspunktTestutfall.resultat(VENT_PÅ_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)
        );
    }

    @Test
    public void send_inn_klage() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Send inn inntektsmelding som oppretter ny behandling (uten søknad) -> skal settes på vent
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent())
            .medGradering(fødselsdatoBarn.plusWeeks(10), fødselsdatoBarn.plusWeeks(25).minusDays(1), BigDecimal.valueOf(50))
            .build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(VENT_PÅ_SØKNAD, OPPRETTET));

        // Arrange steg 2: Send inn søknad -> behandling skal tas av vent
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselGradertUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(BehandlingResultatType.INNVILGET);
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(VENT_PÅ_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)
        );

        // Arrange steg 3: Send inn klage -> behandling skal tas av vent
        JournalpostMottakDtoBuilder journalpostKlageBuilder = JournalpostMottakDtoBuilder.journalpostKlageBuilder(fagsak, repositoryProvider).medForsendelseMottatt(LocalDate.now());

        // Act
        behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostKlageBuilder);

        // Assert
        behandling = behandlingRepository.hentBehandling(behandlingId);
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NFP, OPPRETTET),
            AksjonspunktTestutfall.resultat(VENT_PÅ_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));


        // Arrange steg 4: Send inn klage -> behandling skal tas av vent
        JournalpostMottakDtoBuilder journalpostVedleggBuilder = JournalpostMottakDtoBuilder.journalpostUtenMetadataBuilder(fagsak, repositoryProvider)
            .medDokumentTypeId(DokumentTypeId.DOK_INNLEGGELSE).medForsendelseMottatt(LocalDate.now()).medBehandlingsTema(BehandlingTema.FORELDREPENGER)
            .medEnhet(organisasjonsEnhet.getEnhetId());

        // Act
        JournalConsumerMock.setEmulerJournalFEnhet(organisasjonsEnhet.getEnhetId());
        behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostVedleggBuilder);
        JournalConsumerMock.setEmulerJournalFEnhet(null);

        // Assert
        boolean vurderDokumentViken = fordelRestTjenesteAPI.validerProsessTaskProperty(OpprettOppgaveVurderDokumentTask.TASKTYPE,
            OpprettOppgaveVurderDokumentTask.KEY_BEHANDLENDE_ENHET, organisasjonsEnhet.getEnhetId());
        assertThat(vurderDokumentViken).isTrue();

    }

    private void simulerAtAutopunktFristErPasert() {
        EntityManager entityManager = repoRule.getEntityManager();
        Query oppdatering = entityManager.createQuery(
            "UPDATE Aksjonspunkt SET fristTid=:fristTid");
        oppdatering.setParameter("fristTid", LocalDateTime.now().minusDays(7)); //$NON-NLS-1$
        oppdatering.executeUpdate();
        repository.flushAndClear();
    }
}
