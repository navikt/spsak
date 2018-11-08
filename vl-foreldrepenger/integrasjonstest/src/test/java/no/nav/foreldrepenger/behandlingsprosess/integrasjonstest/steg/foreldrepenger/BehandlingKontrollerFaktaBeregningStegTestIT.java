package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.foreldrepenger;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VURDER_ARBEIDSFORHOLD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.AVBRUTT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.BEREGNINGSGRUNNLAGVILKÅR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_OPPFYLT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostInntektsmeldingBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostSøknadBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.ArbeidsforholdConsumerProducerMock;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.GjenopptaBehandlingDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.ArbeidsforholdTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.FørstegangssøknadTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InfotrygdVedtakTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InntektTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.AksjonspunktRestTjenesteTestAPI;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.BehandlingRestTjenesteTestAPI;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.FordelRestTjenesteTestAPI;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktGodkjenningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.ArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsattBeløpTilstøtendeYtelseAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettBGTilstøtendeYtelseDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettBruttoBeregningsgrunnlagSNDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettMånedsinntektUtenInntektsmeldingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FatterVedtakAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.ForeslaVedtakAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.KontrollAvManueltOpprettetRevurderingsbehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.RedigerbarAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.SjekkManglendeFodselDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderFaktaOmBeregningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderLønnsendringAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderLønnsendringDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderVarigEndringEllerNyoppstartetSNDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.NyBehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftTerminbekreftelseAksjonspunktDto;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;

@RunWith(CdiRunner.class)
public class BehandlingKontrollerFaktaBeregningStegTestIT {
    private static final List<AksjonspunktDefinisjon> BEREGNING_AKSJONSPUNKTER = Arrays.asList(
        FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS, FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE,
        FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD, FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET, VURDER_FAKTA_FOR_ATFL_SN);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    @Inject
    private BehandlingRepositoryProvider repositoryProvider;
    @Inject
    private BehandlingRepository behandlingRepository;
    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);
    @Inject
    private RegisterKontekst registerKontekst;
    @Inject
    private FordelRestTjenesteTestAPI fordelRestTjenesteAPI;
    @Inject
    private AksjonspunktRestTjenesteTestAPI aksjonspunktRestTjeneste;
    @Inject
    private ProsessTaskRepository prosessTaskRepository;
    @Inject
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    @Inject
    private BehandlingRestTjenesteTestAPI behandlingRestTjeneste;
    @Inject
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    public BehandlingKontrollerFaktaBeregningStegTestIT() {
        // CDI runner
    }

    @Before
    public void setup() {
        System.setProperty("dato.for.nye.beregningsregler", "2015-01-01");
        registerKontekst.intialiser();
    }

    @After
    public void tearDown() {
        System.clearProperty("dato.for.nye.beregningsregler");
        registerKontekst.nullstill();
    }

    @Test
    public void skal_henlegge_sak_og_sende_oppgave_om_sak_til_gosys_når_skjæringstidspunkt_er_før_2019_01_01() {
        System.setProperty("dato.for.nye.beregningsregler", "2019-01-01");
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdato = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselGradertUttak(mor.getAktørId(), fødselsdato).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Hack til verdier på opptjening (OJ spesial)
        behandling = behandlingRepository.hentBehandling(behandlingId);
        hackTilEgenNæringOpptjening(fødselsdato, behandling, false, null);

        // Inntektsmelding
        InntektsmeldingM inntektsmelding = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, inntektsmelding, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        Behandling resulat = repositoryProvider.getBehandlingRepository().hentBehandling(behandlingId);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            Collections.singletonList(resultat(BEREGNINGSGRUNNLAGVILKÅR, IKKE_OPPFYLT, Avslagsårsak.INGEN_BEREGNINGSREGLER_TILGJENGELIG_I_LØSNINGEN)));
        assertThat(resulat.getFagsak().getSkalTilInfotrygd()).isTrue();
        assertThat(behandling.getBehandlingsresultat().getVedtaksbrev()).isEqualTo(Vedtaksbrev.INGEN);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.AVSLUTTET);
    }

    @Test
    public void skal_teste_tilbakehopp_ved_fjerning_av_andel_tilstøtende_ytelser() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());
        InfotrygdVedtakTestSett.infotrygdsakStandardUtenFP(mor.getPersonIdent().getIdent(), 0L);

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselGradertUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Inntektsmelding
        InntektsmeldingM inntektsmelding = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, inntektsmelding, repositoryProvider);
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        //Bekreft tilstøtende ytelse aksjonspunkt
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling);
        BeregningsgrunnlagPrStatusOgAndel andel = bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        VurderFaktaOmBeregningDto dto = lagFastsettTilstøtendeYtelseDTO(andel, true);
        bekreftAksjonspunkt(behandling, dto);
        //Assert
        bg = beregningsgrunnlagRepository.hentAggregat(behandling);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertBeregningAksjonspunkterUtført(behandling);

        //Tilbakehopp
        VurderFaktaOmBeregningDto dto2 = lagFastsettTilstøtendeYtelseDTO(andel, false);
        bekreftAksjonspunkt(behandling, dto2);
        // Assert
        bg = beregningsgrunnlagRepository.hentAggregat(behandling);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertBeregningAksjonspunkterUtført(behandling);
    }

    //Test av FPFEIL-2874
    @Test
    public void skal_teste_tilbakehopp_med_ap5058_og_deretter_ap5038_med_tilbake_hopp_som_ikke_skal_gi_ap5038() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = ArbeidsforholdConsumerProducerMock.LØNNSENDRING_DATO.plusMonths(2);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());
        Arbeidsforhold arbeidsforhold = ArbeidsforholdTestSett.finnResponse(mor.getFnr()).getArbeidsforhold().get(0);
        arbeidsforhold.setArbeidsforholdIDnav(50L);
        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselGradertUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        //Gjenoppta Behandling
        behandlingRestTjeneste.gjenopptaBehandling(GjenopptaBehandlingDtoBuilder.build(behandling));

        //Bekreft aksjonspunkter
        bekreftAvklarArbeidsforhold(arbeidsforhold, behandling);
        // Bekreft lønnsendring
        bekreftAksjonspunkt(behandling, lagLønnsEndringDto(1L, 80000));

        //Assert
        assertThat(getAksjonspunkt(behandling, VURDER_FAKTA_FOR_ATFL_SN).getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        assertThat(getAksjonspunkt(behandling, FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS).getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);

        //Tilbakehopp
        bekreftAksjonspunkt(behandling, lagLønnsEndringDto(1L, 40000));

        // Assert
        assertThat(getAksjonspunkt(behandling, VURDER_FAKTA_FOR_ATFL_SN).getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        assertThat(getAksjonspunkt(behandling, FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS).getStatus()).isEqualTo(AksjonspunktStatus.AVBRUTT);
        BeregningsgrunnlagGrunnlagEntitet bgEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(behandling).get();//NOSNOAR
        assertThat(bgEntitet.getBeregningsgrunnlagTilstand()).isEqualTo(BeregningsgrunnlagTilstand.FASTSATT);
        assertBeregningAksjonspunkterUtført(behandling, FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
    }


    //Test av FPFEIL-3477
    @Test
    public void skal_teste_tilbakehopp_med_ap5039_og_deretter_ap5042_med_tilbake_hopp_som_ikke_skal_gi_ap5042() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdato = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());
        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdato).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();


        // Arrange steg 2: Hacker inn en opptjeningsaktivitet med varig endring (ikkje bra)
        hackTilEgenNæringOpptjening(fødselsdato, behandling, true, BigDecimal.valueOf(600000));

        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        //Assert
        assertThat(getAksjonspunkt(behandling, VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE).getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);

        ArrayList<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer = new ArrayList<>();
        bekreftedeAksjonspunktDtoer.add(new VurderVarigEndringEllerNyoppstartetSNDto("Begrunnelse for varig endring", true));
        int overstyrt = 200000;
        bekreftedeAksjonspunktDtoer.add(new FastsettBruttoBeregningsgrunnlagSNDto("Litt lavere grunnlag", overstyrt));

        //Bekreft aksjonspunkter
        bekreftAksjonspunkt(behandling, bekreftedeAksjonspunktDtoer);

        //Assert at aksjonspunkter er utført
        assertThat(getAksjonspunkt(behandling, VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE).getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        assertThat(getAksjonspunkt(behandling, FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE).getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        // Assert at overstyrt er satt på alle SN-andeler
        beregningsgrunnlagRepository.hentAggregat(behandling).getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getAktivitetStatus().erSelvstendigNæringsdrivende())
            .forEach(andel -> assertThat(andel.getOverstyrtPrÅr().compareTo(BigDecimal.valueOf(overstyrt)) == 0).isTrue());

        //Bekreft aksjonspunkt
        bekreftAksjonspunkt(behandling, new VurderVarigEndringEllerNyoppstartetSNDto("Begrunnelse for ikkje varig endring", false));

        // Assert
        assertThat(getAksjonspunkt(behandling, VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE).getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        assertThat(behandling.getAksjonspunkter().stream()
            .noneMatch(ap -> ap.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE))).isTrue();
        beregningsgrunnlagRepository.hentAggregat(behandling).getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getAktivitetStatus().erSelvstendigNæringsdrivende())
            .forEach(andel -> assertThat(andel.getOverstyrtPrÅr()).isNull());

    }


    //Test av FPFEIL-3477
    @Test
    public void skal_teste_bekreft_ap5039_med_ap5042_i_original_behandling_og_bekreft_ap5039_uten_ap5042_i_revurdering() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdato = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());
        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = byggTerminSøknadMedRettighetSomInnvilgesIUttak(fødselsdato, mor);
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Hacker inn en opptjeningsaktivitet med varig endring (ikkje bra)
        hackTilEgenNæringOpptjening(fødselsdato, behandling, true, BigDecimal.valueOf(600000));

        // Send in inntektsmelding
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Bekreft fødsel
        bekreftAksjonspunkt(behandling, new SjekkManglendeFodselDto("Begrunnelse for fødsel", true,
            false, fødselsdato, 1));

        //Assert aksjonspunkt varig endret næring
        assertThat(getAksjonspunkt(behandling, VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE).getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);

        // Bekreft aksjonspunkt varig endret næring (5039) med fastsettelse av beregningsgrunnlag for SN (5042)
        ArrayList<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer = new ArrayList<>();
        bekreftedeAksjonspunktDtoer.add(new VurderVarigEndringEllerNyoppstartetSNDto("Begrunnelse for varig endring", true));
        int overstyrt = 200000;
        bekreftedeAksjonspunktDtoer.add(new FastsettBruttoBeregningsgrunnlagSNDto("Litt lavere grunnlag", overstyrt));
        bekreftAksjonspunkt(behandling, bekreftedeAksjonspunktDtoer);

        //Assert at aksjonspunkter er utført
        assertThat(getAksjonspunkt(behandling, VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE).getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        assertThat(getAksjonspunkt(behandling, FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE).getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getAktivitetStatus().erSelvstendigNæringsdrivende())
            .forEach(andel -> assertThat(andel.getOverstyrtPrÅr().compareTo(BigDecimal.valueOf(overstyrt)) == 0).isTrue());

        // Bekreft foreslå vedtak
        bekreftAksjonspunkt(behandling, new ForeslaVedtakAksjonspunktDto("begrunnelse", "overskrift",
            "Fritekst", false));

        // Bekreft Fatte vedtak
        bekreftFatteVedtak(behandling);

        // Opprett manuell revurdering
        opprettManuellRevurderingEndretBeregningsgrunnlag(behandling);

        Behandling revurdering = behandlingRepository.hentSisteBehandlingForFagsakId(behandling.getFagsakId())
            .orElseThrow(() -> new IllegalStateException("Skal ikke kunne havne her"));

        // Bekreft kontroll av manuelt opprettet revurdering
        bekreftAksjonspunkt(revurdering, new KontrollAvManueltOpprettetRevurderingsbehandlingDto());

        // Bekreft fødsel på nytt for revurdering
        bekreftAksjonspunkt(revurdering, new SjekkManglendeFodselDto("Begrunnelse for fødsel", true,
            false, fødselsdato, 1));

        //Assert at man har opprettet varig endret næring på nytt
        assertThat(getAksjonspunkt(revurdering, VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE).getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);

        // Bekreft 5039 til ikkje varig endret næring uten 5042
        bekreftAksjonspunkt(revurdering, new VurderVarigEndringEllerNyoppstartetSNDto("Begrunnelse for ikkje varig endring", false));

        // Assert at 5042 har blitt slettet og at overstyrt har blitt statt til null
        assertThat(getAksjonspunkt(revurdering, VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE).getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        assertThat(revurdering.getAksjonspunkter().stream()
            .noneMatch(ap -> ap.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE))).isTrue();
        beregningsgrunnlagRepository.hentAggregat(revurdering).getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getAktivitetStatus().erSelvstendigNæringsdrivende())
            .forEach(andel -> assertThat(andel.getOverstyrtPrÅr()).isNull());
    }

    private void opprettManuellRevurderingEndretBeregningsgrunnlag(Behandling behandling) throws URISyntaxException {
        Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();
        NyBehandlingDto opprettManuellRevurderingDto = new NyBehandlingDto();
        opprettManuellRevurderingDto.setSaksnummer(Long.parseLong(saksnummer.getVerdi()));
        opprettManuellRevurderingDto.setBehandlingType(BehandlingType.REVURDERING);
        opprettManuellRevurderingDto.setBehandlingArsakType(BehandlingÅrsakType.RE_ENDRING_BEREGNINGSGRUNNLAG);
        behandlingRestTjeneste.opprettNyBehandling(opprettManuellRevurderingDto);
    }

    private void bekreftFatteVedtak(Behandling behandling) throws URISyntaxException {
        List<AksjonspunktGodkjenningDto> aksjonspunktGodkjenningDtos = behandling.getAksjonspunkter().stream()
            .filter(Aksjonspunkt::isToTrinnsBehandling)
            .map(ap -> {
                AksjonspunktGodkjenningDto apDto = new AksjonspunktGodkjenningDto();
                apDto.setAksjonspunktKode(ap.getAksjonspunktDefinisjon().getKode());
                apDto.setGodkjent(true);
                return apDto;
            }).collect(Collectors.toList());

        bekreftAksjonspunkt(behandling, new FatterVedtakAksjonspunktDto("Begrunnelse", aksjonspunktGodkjenningDtos));
    }

    private Soeknad byggTerminSøknadMedRettighetSomInnvilgesIUttak(LocalDate fødselsdato, TpsPerson mor) {
        return new SøknadTestdataBuilder().søknadForeldrepenger()
                .medMottattdato(fødselsdato.minusWeeks(2))
                .medSøker(ForeldreType.MOR, mor.getAktørId())
                .medTermin(new SøknadTestdataBuilder.TerminBuilder()
                    .medTermindato(fødselsdato)
                    .medUtsteddato(fødselsdato.minusWeeks(3)))
                .medRettighet(new SøknadTestdataBuilder.RettighetBuilder().harAleneomsorgForBarnet(false).harAnnenForelderRett(true).harOmsorgForBarnetIPeriodene(true))
                .medFordeling(new SøknadTestdataBuilder.FordelingBuilder()
                    .leggTilPeriode(fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
                    .leggTilPeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), UttakPeriodeType.MØDREKVOTE)
                    .leggTilPeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), UttakPeriodeType.FELLESPERIODE))
                .build();
    }

    private void hackTilEgenNæringOpptjening(LocalDate fødselsdato, Behandling behandling, boolean varigEndring, BigDecimal bruttoInntekt) {
        OppgittOpptjeningBuilder builder = OppgittOpptjeningBuilder.ny();
        OppgittOpptjeningBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningBuilder.EgenNæringBuilder.ny();

        egenNæringBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fødselsdato.minusYears(2), fødselsdato.plusYears(2)))
            .medVarigEndring(varigEndring)
            .medBruttoInntekt(bruttoInntekt)
            .medEndringDato(varigEndring ? fødselsdato.minusDays(3) : null)
            .medVirksomhetType(VirksomhetType.FISKE);

        builder.leggTilEgneNæringer(Collections.singletonList(egenNæringBuilder));
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);
    }

    @Test
    public void skal_teste_tilbakehopp_fra_kofakber() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate terminDato = ArbeidsforholdConsumerProducerMock.LØNNSENDRING_DATO.plusMonths(2).plusWeeks(1);
        TpsPerson mor = TpsTestSett.kvinneUtenBarn().getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());
        Arbeidsforhold arbeidsforhold = ArbeidsforholdTestSett.finnResponse(mor.getFnr()).getArbeidsforhold().get(0);
        arbeidsforhold.setArbeidsforholdIDnav(50L);
        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morTerminStandardUttak(mor.getAktørId(), terminDato.minusWeeks(1), terminDato, terminDato.minusDays(3)).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        //Gjenoppta Behandling
        behandlingRestTjeneste.gjenopptaBehandling(GjenopptaBehandlingDtoBuilder.build(behandling));

        //Bekreft aksjonspunkter
        bekreftAvklarArbeidsforhold(arbeidsforhold, behandling);
        BekreftTerminbekreftelseAksjonspunktDto dto = new BekreftTerminbekreftelseAksjonspunktDto("Noe", terminDato, terminDato.minusDays(3), 1);
        bekreftAksjonspunkt(behandling, dto);

        //Assert
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGR = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(behandling);
        assertThat(bgGR).isPresent();
        assertThat(bgGR.get().getBeregningsgrunnlagTilstand()).isEqualTo(BeregningsgrunnlagTilstand.OPPRETTET);
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(VURDER_FAKTA_FOR_ATFL_SN, OPPRETTET), AksjonspunktTestutfall.resultat(AVKLAR_TERMINBEKREFTELSE, UTFØRT),
            AksjonspunktTestutfall.resultat(VURDER_ARBEIDSFORHOLD, UTFØRT), AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT));
        //Tilbakehopp
        bekreftAvklarArbeidsforhold(arbeidsforhold, behandling);
        //Assert
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(behandling);
        assertThat(bgOpt).isNotPresent();
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(VURDER_FAKTA_FOR_ATFL_SN, AVBRUTT), AksjonspunktTestutfall.resultat(AVKLAR_TERMINBEKREFTELSE, OPPRETTET),
            AksjonspunktTestutfall.resultat(VURDER_ARBEIDSFORHOLD, UTFØRT), AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT));
    }

    private void bekreftAvklarArbeidsforhold(Arbeidsforhold arbeidsforhold, Behandling behandling) throws URISyntaxException {
        Organisasjon org = (Organisasjon) arbeidsforhold.getArbeidsgiver();
        //hent yrkesaktivitet
        Collection<Yrkesaktivitet> yrkesaktiviteter = inntektArbeidYtelseTjeneste.hentYrkesaktiviteterForSøker(behandling, false);
        String arbeidsforholdRef = yrkesaktiviteter.stream().filter(ya -> ya.getArbeidsgiver().getVirksomhet().getOrgnr().equals(org.getOrgnummer()))
            .map(ya -> ya.getArbeidsforholdRef().get().getReferanse()).findFirst().get();

        ArbeidsforholdDto arbeidsforholdDto = new ArbeidsforholdDto();
        arbeidsforholdDto.setArbeidsforholdId(arbeidsforholdRef);
        arbeidsforholdDto.setArbeidsgiverIdentifikator(org.getOrgnummer());
        arbeidsforholdDto.setFortsettBehandlingUtenInntektsmelding(true);
        arbeidsforholdDto.setBrukArbeidsforholdet(true);
        arbeidsforholdDto.setStillingsprosent(BigDecimal.valueOf(100));
        arbeidsforholdDto.setId(arbeidsforholdRef);
        AvklarArbeidsforholdDto avklarDto = new AvklarArbeidsforholdDto("Noe", Arrays.asList(arbeidsforholdDto));
        bekreftAksjonspunkt(behandling, avklarDto);
    }

    private void assertBeregningAksjonspunkterUtført(Behandling behandling, AksjonspunktDefinisjon... ignorer) {
        behandling.getAksjonspunkter().stream()
            .filter(ap -> !Arrays.asList(ignorer).contains(ap.getAksjonspunktDefinisjon()))
            .filter(ap -> BEREGNING_AKSJONSPUNKTER.contains(ap.getAksjonspunktDefinisjon()))
            .forEach(ap -> assertThat(ap.getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT));
    }


    private Aksjonspunkt getAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon apDef) {
        return behandling.getAksjonspunkter().stream().filter(ap -> ap.getAksjonspunktDefinisjon().equals(apDef)).findFirst().get();//NOSONAR
    }

    private VurderFaktaOmBeregningDto lagFastsettTilstøtendeYtelseDTO(BeregningsgrunnlagPrStatusOgAndel andel, boolean ekstraAndel) {
        String arbeidsforholdId = andel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef().get().getReferanse();
        FastsattBeløpTilstøtendeYtelseAndelDto andelDto = new FastsattBeløpTilstøtendeYtelseAndelDto(
            new RedigerbarAndelDto("", false, arbeidsforholdId, andel.getAndelsnr(), false),
            50000, 0, andel.getInntektskategori(), 1d);
        FastsattBeløpTilstøtendeYtelseAndelDto brukers_andel = new FastsattBeløpTilstøtendeYtelseAndelDto(
            new RedigerbarAndelDto("BRUKERS_ANDEL", true, arbeidsforholdId, andel.getAndelsnr(), true),
            5000, 0, andel.getInntektskategori(), 1d);
        FastsettBGTilstøtendeYtelseDto dtoTY;
        if (ekstraAndel) {
            dtoTY = new FastsettBGTilstøtendeYtelseDto(Arrays.asList(andelDto, brukers_andel));
        } else {
            dtoTY = new FastsettBGTilstøtendeYtelseDto(Arrays.asList(andelDto));
        }
        return new VurderFaktaOmBeregningDto("noe", Arrays.asList(FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE), dtoTY);
    }

    private VurderFaktaOmBeregningDto lagLønnsEndringDto(long andelsnr, int lønnsendring) {
        VurderLønnsendringDto dto1 = new VurderLønnsendringDto(true);
        VurderLønnsendringAndelDto dto2 = new VurderLønnsendringAndelDto(andelsnr, lønnsendring);
        FastsettMånedsinntektUtenInntektsmeldingDto dto3 = new FastsettMånedsinntektUtenInntektsmeldingDto();
        dto3.setVurderLønnsendringAndelListe(Arrays.asList(dto2));
        VurderFaktaOmBeregningDto vurderFaktaOmBeregningDto = new VurderFaktaOmBeregningDto("noe",
            Arrays.asList(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING, FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING));
        vurderFaktaOmBeregningDto.setVurdertLonnsendring(dto1);
        vurderFaktaOmBeregningDto.setFastsatteLonnsendringer(dto3);
        return vurderFaktaOmBeregningDto;
    }

    private void bekreftAksjonspunkt(Behandling behandling, BekreftetAksjonspunktDto dto) throws URISyntaxException {
        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandling.getId(), behandling.getVersjon(), asList(dto)));
        kjørProsessTasks();
    }

    private void bekreftAksjonspunkt(Behandling behandling, List<BekreftetAksjonspunktDto> dtos) throws URISyntaxException {
        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandling.getId(), behandling.getVersjon(), dtos));
        kjørProsessTasks();
    }
    private void kjørProsessTasks() {
        new KjørProsessTasks(prosessTaskRepository).utførTasks();
    }

}
