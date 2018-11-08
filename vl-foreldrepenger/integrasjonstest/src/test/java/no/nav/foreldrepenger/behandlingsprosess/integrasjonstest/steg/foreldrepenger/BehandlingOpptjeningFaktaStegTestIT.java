package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.foreldrepenger;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.BEREGNINGSGRUNNLAGVILKÅR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_MOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OPPTJENINGSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKERSOPPLYSNINGSPLIKT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_OPPFYLT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.OPPFYLT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostInntektsmeldingBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostSøknadBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.AnnenAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.ArbeidsforholdConsumerProducerMock;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.ArbeidsforholdTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.FørstegangssøknadTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InfotrygdVedtakTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InntektTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.MeldekortTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.AksjonspunktRestTjenesteTestAPI;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.FordelRestTjenesteTestAPI;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarAktivitetsPerioderDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.OpptjeningAktivitetDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderFaktaOmBeregningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurdereYtelseSammeBarnSøkerAksjonspunktDto;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.nav.vedtak.konfig.Tid;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;

@RunWith(CdiRunner.class)
public class BehandlingOpptjeningFaktaStegTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private RegisterKontekst registerKontekst;
    @Inject
    private ProsessTaskRepository prosessTaskRepository;
    // Test-API-er rundt REST-tjenestene
    @Inject
    private FordelRestTjenesteTestAPI fordelRestTjenesteAPI;
    @Inject
    private AksjonspunktRestTjenesteTestAPI aksjonspunktRestTjenesteAPI;

    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;

    private BehandlingskontrollKontekst kontekst;

    public BehandlingOpptjeningFaktaStegTestIT() {
    }

    @Before
    public void setup() {
        registerKontekst.intialiser();
        // setter verdien slik at regler blir kjørt
        System.setProperty("dato.for.nye.beregningsregler", "2010-01-01");
    }

    @After
    public void teardown() {
        registerKontekst.nullstill();
        System.setProperty("dato.for.nye.beregningsregler", "2019-01-01");
    }

    @Test
    public void skal_få_opptjeningsvilkåret_oppfylt_når_det_finnes_inntekter() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og innvilge stønad
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            singletonList(resultat(OPPTJENINGSVILKÅRET, OPPFYLT)));

        assertUtil.assertAksjonspunkter(asList(AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
    }

    @Test
    public void skal_få_opptjeningsvilkåret_oppfylt_når_det_finnes_inntekter_ytelser() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());
        InfotrygdVedtakTestSett.infotrygdsakStandard(mor.getPersonIdent().getIdent(), 35L);
        MeldekortTestSett.meldekortStandard(mor.getAktørId().getId(), 60L);

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og innvilge stønad
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Det dukker opp en AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE pga Foreldrepenger i Ytelsene
        behandling = repository.hent(Behandling.class, behandlingId);
        VurdereYtelseSammeBarnSøkerAksjonspunktDto dto = new VurdereYtelseSammeBarnSøkerAksjonspunktDto("bare tull", true);
        BekreftedeAksjonspunkterDto dtoer = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), singletonList(dto));
        aksjonspunktRestTjenesteAPI.bekreft(dtoer);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AksjonspunktDefinisjon.AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            singletonList(resultat(OPPTJENINGSVILKÅRET, OPPFYLT)));
    }

    @Test
    public void skal_få_opptjeningsvilkåret_avslått_for_lite_ytelser() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        InfotrygdVedtakTestSett.infotrygdsakStandardUtenFP(mor.getPersonIdent().getIdent(), 27L);
        MeldekortTestSett.meldekortStandard(mor.getAktørId().getId(), 27L);

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        assertUtil.assertAksjonspunkter(asList(AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            singletonList(resultat(OPPTJENINGSVILKÅRET, IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1035)));
    }

    @Test
    public void skal_få_opptjeningsvilkåret_oppfylt_lang_aap_ytelser() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        MeldekortTestSett.meldekortUtvidet(mor.getAktørId().getId(), 40L);

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            singletonList(resultat(OPPTJENINGSVILKÅRET, OPPFYLT )));
    }


    @Test
    public void skal_få_opptjeningsvilkåret_oppfylt_selv_når_saksbehandler_avviser_militærtjeneste() throws Exception {
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

        // Arrange steg 2: Sukre opptjening med opptjeningsaktivitet (hack)
        // Deretter sende inn innteksmelding
        OppgittOpptjeningBuilder builder = OppgittOpptjeningBuilder.ny();
        builder.leggTilAnnenAktivitet(new AnnenAktivitetEntitet(DatoIntervallEntitet.fraOgMedTilOgMed(fødselsdato.minusDays(60), fødselsdato), ArbeidType.MILITÆR_ELLER_SIVILTJENESTE));
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);

        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Steg 3: Saksbehandler setter som ikke godkjent
        OpptjeningAktivitetDto oaDto = new OpptjeningAktivitetDto();
        oaDto.setErGodkjent(false);
        oaDto.setOpptjeningFom(fødselsdato);
        oaDto.setBegrunnelse("Godkjenner ikke denne!");
        oaDto.setOpptjeningTom(Tid.TIDENES_ENDE);
        oaDto.setStillingsandel(BigDecimal.ZERO);
        oaDto.setErManueltOpprettet(true);
        oaDto.setAktivitetType(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE);

        OpptjeningAktivitetDto aktivitet2 = new OpptjeningAktivitetDto();
        aktivitet2.setErGodkjent(true);
        aktivitet2.setOriginalFom(fødselsdato.minusYears(3));
        aktivitet2.setOriginalTom(fødselsdato);
        aktivitet2.setOpptjeningFom(fødselsdato.minusYears(3));
        aktivitet2.setOpptjeningTom(fødselsdato);
        aktivitet2.setStillingsandel(BigDecimal.valueOf(100));
        aktivitet2.setErManueltOpprettet(false);
        aktivitet2.setErEndret(false);
        aktivitet2.setOppdragsgiverOrg(ArbeidsforholdConsumerProducerMock.MOCK_ORGNR);

        aktivitet2.setAktivitetType(OpptjeningAktivitetType.ARBEID);

        AvklarAktivitetsPerioderDto dto = new AvklarAktivitetsPerioderDto("Ser greit ut", asList(oaDto, aktivitet2));
        BekreftedeAksjonspunkterDto dtoer = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), singletonList(dto));

        // Act
        aksjonspunktRestTjenesteAPI.bekreft(dtoer);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            singletonList(resultat(OPPTJENINGSVILKÅRET, OPPFYLT)));
    }

    @Test
    public void skal_få_opptjeningsvilkåret_oppfylt_selv_når_saksbehandler_godkjenner_militærtjeneste() throws Exception {
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

        // Arrange steg 2: Sukre opptjening med opptjeningsaktivitet (hack)
        // Deretter sende inn innteksmelding
        OppgittOpptjeningBuilder builder = OppgittOpptjeningBuilder.ny();
        builder.leggTilAnnenAktivitet(new AnnenAktivitetEntitet(DatoIntervallEntitet.fraOgMedTilOgMed(fødselsdato.minusDays(60), fødselsdato), ArbeidType.MILITÆR_ELLER_SIVILTJENESTE));
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);

        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Steg 3: Saksbehandler setter godkjent
        OpptjeningAktivitetDto aktivitet1 = new OpptjeningAktivitetDto();
        aktivitet1.setErGodkjent(true);
        aktivitet1.setOpptjeningFom(fødselsdato.minusDays(60));
        aktivitet1.setOpptjeningTom(fødselsdato);
        aktivitet1.setBegrunnelse("Godkjenner dennne aktiviteten!");
        aktivitet1.setStillingsandel(BigDecimal.valueOf(100));
        aktivitet1.setErManueltOpprettet(true);
        aktivitet1.setAktivitetType(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE);

        OpptjeningAktivitetDto aktivitet2 = new OpptjeningAktivitetDto();
        aktivitet2.setErGodkjent(true);
        aktivitet2.setOriginalFom(fødselsdato.minusYears(3));
        aktivitet2.setOriginalTom(fødselsdato);
        aktivitet2.setOpptjeningFom(fødselsdato.minusYears(3));
        aktivitet2.setOpptjeningTom(fødselsdato);
        aktivitet2.setStillingsandel(BigDecimal.valueOf(100));
        aktivitet2.setErManueltOpprettet(false);
        aktivitet2.setErEndret(false);
        aktivitet2.setOppdragsgiverOrg(ArbeidsforholdConsumerProducerMock.MOCK_ORGNR);

        aktivitet2.setAktivitetType(OpptjeningAktivitetType.ARBEID);

        AvklarAktivitetsPerioderDto dto = new AvklarAktivitetsPerioderDto("Ser greit ut", asList(aktivitet1, aktivitet2));
        BekreftedeAksjonspunkterDto dtoer = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), singletonList(dto));

        // Act
        aksjonspunktRestTjenesteAPI.bekreft(dtoer);

        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            singletonList(resultat(OPPTJENINGSVILKÅRET, OPPFYLT)));
    }

    @Test
    public void skal_få_aksjonspunkt_5051_når_arbeidsforhold_har_null_stillingsprosent_og_sette_det_til_oppfylt() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdato = LocalDate.of(2017, 2, 3);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        ArbeidsforholdTestSett.stillingsprosent0(mor.getFnr());
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

        // Arrange steg 2: Sende inn innteksmelding
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertUnikFagsak(RelasjonsRolleType.MORA);
        assertUnikFødsel(fødselsdato, behandlingId);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(OPPTJENINGSVILKÅRET, IKKE_VURDERT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(VURDER_PERIODER_MED_OPPTJENING, OPPRETTET));

        // Arrange Steg 3: Saksbehandler bekrefter aksjonspunkt
        OpptjeningAktivitetDto aktivitet2 = new OpptjeningAktivitetDto();
        aktivitet2.setErGodkjent(true);
        aktivitet2.setOriginalFom(fødselsdato.minusYears(3));
        aktivitet2.setOriginalTom(fødselsdato);
        aktivitet2.setOpptjeningFom(fødselsdato.minusYears(3));
        aktivitet2.setOpptjeningTom(fødselsdato);
        aktivitet2.setStillingsandel(BigDecimal.valueOf(100));
        aktivitet2.setErManueltOpprettet(false);
        aktivitet2.setErEndret(false);
        aktivitet2.setOppdragsgiverOrg(ArbeidsforholdConsumerProducerMock.MOCK_ORGNR);
        aktivitet2.setArbeidsforholdRef(hentUtArbeidsforholdRef(behandling, ArbeidsforholdConsumerProducerMock.MOCK_ORGNR));
        aktivitet2.setAktivitetType(OpptjeningAktivitetType.ARBEID);

        AvklarAktivitetsPerioderDto dto = new AvklarAktivitetsPerioderDto("Ser greit ut", asList(aktivitet2));
        BekreftedeAksjonspunkterDto dtoer = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), singletonList(dto));

        // Act
        aksjonspunktRestTjenesteAPI.bekreft(dtoer);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(OPPTJENINGSVILKÅRET, OPPFYLT)));
    }

    private String hentUtArbeidsforholdRef(Behandling behandling, String mockOrgnr) {
        return repositoryProvider.getInntektArbeidYtelseRepository().hentAggregat(behandling, null)
            .getAktørArbeidFørStp(behandling.getAktørId())
            .flatMap(it -> it.getYrkesaktiviteter().stream().filter(yr -> yr.getArbeidsgiver() != null
                && yr.getArbeidsgiver().getIdentifikator().equals(mockOrgnr)).findFirst())
            .flatMap(Yrkesaktivitet::getArbeidsforholdRef)
            .map(ArbeidsforholdRef::getReferanse)
            .orElse(null);
    }

    @Test
    public void skal_få_aksjonspunkt_5051_når_bekreftet_frilansoppdrag_og_sette_det_til_oppfylt() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdato = LocalDate.of(2017, 12, 14);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        InntektTestSett.inntektKunFrilans(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdato).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);

        // Assert
        assertUnikFagsak(RelasjonsRolleType.MORA);
        assertUnikFødsel(fødselsdato, behandlingId);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(OPPTJENINGSVILKÅRET, IKKE_VURDERT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(VURDER_PERIODER_MED_OPPTJENING, OPPRETTET));

        // Arrange Steg 2: Saksbehandler bekrefter aksjonspunkt
        OpptjeningAktivitetDto oaDto = new OpptjeningAktivitetDto();
        oaDto.setErGodkjent(true);
        oaDto.setBegrunnelse("Godkjenner");
        oaDto.setOriginalFom(fødselsdato.minusYears(3));
        oaDto.setOriginalTom(Tid.TIDENES_ENDE);
        oaDto.setOpptjeningFom(fødselsdato.minusYears(3));
        oaDto.setOpptjeningTom(Tid.TIDENES_ENDE);
        oaDto.setStillingsandel(BigDecimal.ONE);
        oaDto.setAktivitetType(OpptjeningAktivitetType.FRILANS);
        oaDto.setErEndret(false);
        oaDto.setErManueltOpprettet(false);

        AvklarAktivitetsPerioderDto dto = new AvklarAktivitetsPerioderDto("Ser greit ut", asList(oaDto));
        BekreftedeAksjonspunkterDto dtoer = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), singletonList(dto));

        // Act
        aksjonspunktRestTjenesteAPI.bekreft(dtoer);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(OPPTJENINGSVILKÅRET, OPPFYLT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(VURDER_PERIODER_MED_OPPTJENING, UTFØRT),
            AksjonspunktTestutfall.resultat(FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS, OPPRETTET));
    }


    @Test
    public void skal_få_aksjonspunkt_5051_når_arbeidsforhold_har_opptjeningstype_militærtjeneste() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdato = LocalDate.of(2017, 11, 1);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdato).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Hacker inn en opptjeningsaktivitet (ikkje bra)
        //   Sender deretter inn inntektsmelding -> skal tas av vent og opprette aksjonspunkt
        OppgittOpptjeningBuilder builder = OppgittOpptjeningBuilder.ny();
        builder.leggTilAnnenAktivitet(new AnnenAktivitetEntitet(DatoIntervallEntitet.fraOgMed(fødselsdato.minusMonths(1)), ArbeidType.MILITÆR_ELLER_SIVILTJENESTE));
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);

        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertUnikFagsak(RelasjonsRolleType.MORA);
        assertUnikFødsel(fødselsdato, behandlingId);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(OPPTJENINGSVILKÅRET, IKKE_VURDERT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(VURDER_PERIODER_MED_OPPTJENING, OPPRETTET));

        // Arrange Steg 2: Saksbehandler bekrefter aksjonspunkt
        OpptjeningAktivitetDto oaDto = new OpptjeningAktivitetDto();
        oaDto.setErGodkjent(false);
        oaDto.setBegrunnelse("Godkjenner ikke!");
        oaDto.setOpptjeningFom(fødselsdato);
        oaDto.setOpptjeningTom(Tid.TIDENES_ENDE);
        oaDto.setStillingsandel(BigDecimal.ZERO);
        oaDto.setErManueltOpprettet(true);
        oaDto.setAktivitetType(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE);

        AvklarAktivitetsPerioderDto dto = new AvklarAktivitetsPerioderDto("Ser greit ut", singletonList(oaDto));
        BekreftedeAksjonspunkterDto dtoer = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), singletonList(dto));

        // Act
        aksjonspunktRestTjenesteAPI.bekreft(dtoer);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            singletonList(resultat(OPPTJENINGSVILKÅRET, IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1035)));

    }

    @Test
    public void skal_hente_inn_relevant_informasjon_fra_sigrun_hvis_egen_næring_er_oppgitt_i_søknad() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdato = LocalDate.of(2017, 11, 1);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdato).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Hacker inn en opptjeningsaktivitet (ikkje bra)
        //   Sender deretter inn inntektsmelding -> skal tas av vent og opprette aksjonspunkt
        OppgittOpptjeningBuilder builder = OppgittOpptjeningBuilder.ny();
        OppgittOpptjeningBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningBuilder.EgenNæringBuilder.ny();

        egenNæringBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fødselsdato.minusYears(2), fødselsdato.plusYears(2)))
            .medVirksomhetType(VirksomhetType.FISKE);

        builder.leggTilEgneNæringer(Collections.singletonList(egenNæringBuilder));
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);

        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            Collections.singletonList(resultat(BEREGNINGSGRUNNLAGVILKÅR, OPPFYLT)));
    }

    private void utførProsessSteg(Long behandlingId) {
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);

        repository.flush();
    }

    private void assertUnikFagsak(RelasjonsRolleType brukerrolle) {
        List<Fagsak> fagsaker = repository.hentAlle(Fagsak.class);
        assertThat(fagsaker).hasSize(1);
        assertThat(fagsaker.get(0).getRelasjonsRolleType().getKode()).isEqualTo(brukerrolle.getKode());
    }

    private void assertUnikFødsel(LocalDate fødselsdato, Long behandlingId) {
        final List<UidentifisertBarn> barna = repositoryProvider.getFamilieGrunnlagRepository()
            .hentAggregat(behandlingRepository.hentBehandling(behandlingId)).getGjeldendeVersjon().getBarna();
        assertThat(barna).hasSize(1);
        assertThat(barna.get(0).getFødselsdato()).isEqualTo(fødselsdato);
    }

}
