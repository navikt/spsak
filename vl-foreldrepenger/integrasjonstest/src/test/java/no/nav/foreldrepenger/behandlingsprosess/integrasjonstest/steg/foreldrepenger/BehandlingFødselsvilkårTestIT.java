package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.foreldrepenger;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_FØDSELREGISTRERING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_STARTDATO_FOR_FORELDREPENGEPERIODEN;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VURDER_ARBEIDSFORHOLD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VURDER_OM_VILKÅR_FOR_SYKDOM_OPPFYLT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.AVBRUTT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.BEREGNINGSGRUNNLAGVILKÅR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_MOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OPPTJENINGSPERIODEVILKÅR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OPPTJENINGSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKERSOPPLYSNINGSPLIKT;
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
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.UidentifisertBarnEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetKlassifisering;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.GjenopptaBehandlingDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.ArbeidsforholdTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.FørstegangssøknadTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InntektTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.AksjonspunktRestTjenesteTestAPI;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.BehandlingRestTjenesteTestAPI;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.FordelRestTjenesteTestAPI;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.GjenopptaBehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftTerminbekreftelseAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerDto;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.nav.vedtak.util.FPDateUtil;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;

@RunWith(CdiRunner.class)
public class BehandlingFødselsvilkårTestIT {

    private final BigDecimal MND_12 = BigDecimal.valueOf(12);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BeregningsresultatFPRepository beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
    private final UttakRepository uttakRepository = repositoryProvider.getUttakRepository();
    private final BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    private final OpptjeningRepository opptjeningRepository = repositoryProvider.getOpptjeningRepository();
    private final InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private final FamilieHendelseRepository familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    @Inject
    private PersonopplysningTjeneste personopplysningTjeneste;

    @Inject
    private RegisterKontekst registerKontekst;
    // Test-API-er rundt REST-tjenestene
    @Inject
    private FordelRestTjenesteTestAPI fordelRestTjenesteAPI;
    @Inject
    private BehandlingRestTjenesteTestAPI behandlingRestTjenesteAPI;
    @Inject
    private AksjonspunktRestTjenesteTestAPI aksjonspunktRestTjenesteAPI;


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
    public void fødsel_happy_case() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselGradertUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(repository.hentAlle(SøknadEntitet.class)).hasSize(1);

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og innvilge stønad
        BigDecimal inntektBeløp = BigDecimal.valueOf(40000); // Fra Inntekskomponenten. Areg-inntekt = 40000
        BigDecimal refusjonsbeløpPrMnd = BigDecimal.valueOf(10000);
        LocalDate mottattDatoInntektsmelding = LocalDate.now();
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding(mor.getPersonIdent(), inntektBeløp)
            .medRefusjonsbeloepPrMnd(refusjonsbeløpPrMnd)
            .medGradering(fødselsdatoBarn.plusWeeks(10), fødselsdatoBarn.plusWeeks(25).minusDays(1), BigDecimal.valueOf(50))
            .build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        // Vilkår
        assertUnikFagsak(RelasjonsRolleType.MORA);
        assertUnikFødsel(fødselsdatoBarn, behandlingId);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(OPPTJENINGSPERIODEVILKÅR, OPPFYLT),
                resultat(OPPTJENINGSVILKÅRET, OPPFYLT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(BEREGNINGSGRUNNLAGVILKÅR, OPPFYLT)));

        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));

        // Grunnlagsaggregat
        FamilieHendelseGrunnlag familieHendelseGrunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        assertThat(familieHendelseGrunnlag.getBekreftetVersjon().map(FamilieHendelse::getFødselsdato))
            .hasValue(Optional.of(fødselsdatoBarn));

        PersonopplysningerAggregat personopplysningerAggregat = personopplysningTjeneste.hentPersonopplysninger(behandling);
        assertThat(personopplysningerAggregat.getBarna()).hasSize(1);

        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);
        // Areg
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp()).hasSize(1);
        AktørInntekt aktørInntekt = inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp().iterator().next();
        assertThat(aktørInntekt.getInntektPensjonsgivende().get(0).getInntektspost()).hasSize(36);
        assertThat(aktørInntekt.getInntektPensjonsgivende().get(0).getInntektspost().iterator().next().getBeløp().getVerdi()).isEqualTo("40000");
        // Inntektsmelding
        assertThat(inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp()).hasSize(1);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp().iterator().next().getYrkesaktiviteter()).hasSize(1);
        Yrkesaktivitet yrkesaktivitet = inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp().iterator().next().getYrkesaktiviteter().iterator().next();
        assertThat(yrkesaktivitet.getAktivitetsAvtaler()).hasSize(1);
        assertThat(yrkesaktivitet.getAktivitetsAvtaler().iterator().next().getProsentsats().getVerdi()).isEqualTo("100");
        assertThat(inntektArbeidYtelseGrunnlag.getInntektsmeldinger())
            .hasValueSatisfying(aggregat -> assertThat(aggregat.getInntektsmeldinger().get(0).getInntektBeløp().getVerdi()).isEqualTo(inntektBeløp));

        // Resultataggregat
        Opptjening opptjening = opptjeningRepository.finnOpptjening(behandling)
            .orElseThrow(() -> new IllegalStateException("Skal ha opptjening her"));
        assertThat(opptjening.getOpptjeningAktivitet()).hasSize(1);
        OpptjeningAktivitet opptjeningAktivitet = opptjening.getOpptjeningAktivitet().get(0);
        assertThat(opptjeningAktivitet.getAktivitetType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
        assertThat(opptjeningAktivitet.getKlassifisering()).isEqualTo(OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        assertThat(opptjeningAktivitet.getFom()).isEqualTo(fødselsdatoBarn.minusWeeks(3).minusMonths(10));
        assertThat(opptjeningAktivitet.getTom()).isEqualTo(fødselsdatoBarn.minusWeeks(3).minusDays(1));

        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling)
            .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBruttoPrÅr()).isEqualTo(inntektBeløp.multiply(MND_12));

        Optional<UttakResultatEntitet> uttakResultatPlan = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = uttakResultatPlan.get().getGjeldendePerioder().getPerioder();
        assertThat(uttakResultatPerioder.stream()
            .map(periode -> periode.getAktiviteter().get(0).getTrekkonto()))
            .containsExactlyInAnyOrder(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, StønadskontoType.MØDREKVOTE, StønadskontoType.MØDREKVOTE,
                StønadskontoType.FELLESPERIODE);

        // Sjekk at gradering blir håndtert riktig
        Optional<UttakResultatPeriodeEntitet> fellesperiodeOpt = uttakResultatPerioder.stream()
            .filter(p -> p.getAktiviteter().get(0).getTrekkonto().equals(StønadskontoType.FELLESPERIODE)).findFirst();
        assertThat(fellesperiodeOpt).hasValueSatisfying(fellesperiode -> {
            assertThat(fellesperiode).isNotNull();
            assertThat(fellesperiode.getAktiviteter().get(0).getTrekkdager()).isEqualTo(37); // Halvparten 75 dager, og rundet ned til gunst for bruker.
            assertThat(fellesperiode.isGraderingInnvilget()).isTrue();
        });

        assertThat(uttakResultatPerioder.stream()
            .allMatch(p -> p.getPeriodeResultatType().equals(PeriodeResultatType.INNVILGET)))
            .isTrue();

        Optional<BeregningsresultatFP> beregningsresultatFP = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling);
        List<BeregningsresultatPeriode> beregningsresultatPerioder = beregningsresultatFP
            .map(BeregningsresultatFP::getBeregningsresultatPerioder)
            .orElse(Collections.emptyList());
        assertThat(beregningsresultatPerioder).hasSize(4);
        beregningsresultatPerioder.forEach(resultat -> {
            assertThat(resultat.getBeregningsresultatAndelList()).hasSize(2);
            // Andeler av tilkjent ytelse
            BeregningsresultatAndel andelMottaker = hentAndel(resultat, true);
            BeregningsresultatAndel andelArbeidsgiver = hentAndel(resultat, false);
            assertThat(andelMottaker.getDagsats()).isEqualTo(
                (int) Math.round(inntektBeløp.subtract(refusjonsbeløpPrMnd).multiply(andelMottaker.getUtbetalingsgrad().divide(BigDecimal.valueOf(100))).multiply(BigDecimal.valueOf(12.0 / 260)).doubleValue()));
            assertThat(andelArbeidsgiver.getDagsats()).isEqualTo(
                (int) Math.round(refusjonsbeløpPrMnd.multiply(andelMottaker.getUtbetalingsgrad().divide(BigDecimal.valueOf(100))).multiply(BigDecimal.valueOf(12.0 / 260)).doubleValue()));
        });
    }

    @Test // Endringskontroller
    public void skal_utføre_tilbakehopp_til_startpunkt_ved_innsending_av_inntektsmelding_dersom_kompletthet_er_passert() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(6);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.løpendeForhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).get(0).getAksjonspunktDefinisjon())
            .isEqualTo(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD);

        // Arrange/Act steg 2: Send inn inntektsmelding -> skal tas av vent og sendes til manuell fastsetting ber.gr.
        BigDecimal inntektBeløp = BigDecimal.valueOf(20000); // Fra Inntekskomponenten. Areg-inntekt = 40000
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding(mor.getPersonIdent(), inntektBeløp)
            .medStartdatoForeldrepenger(fødselsdatoBarn.minusWeeks(3))
            .build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        // Vilkår
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(OPPTJENINGSPERIODEVILKÅR, OPPFYLT),
                resultat(OPPTJENINGSVILKÅRET, OPPFYLT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(BEREGNINGSGRUNNLAGVILKÅR, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        // Skal ha lagt til initiell behandlingsårsak
        assertThat(behandling.getBehandlingÅrsaker().stream().map(BehandlingÅrsak::getBehandlingÅrsakType).collect(toList()))
            .containsExactlyInAnyOrder(BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);

        // Arrange/Act steg 3: Send inn ny inntektsmelding -> skal tilbakepoles for å vurdere medl.vilkår pga endret startdato for foreldrepenger
        inntektBeløp = BigDecimal.valueOf(40000); // Fra Inntekskomponenten. Areg-inntekt = 40000
        // Endrer startdato for foreldrepenger
        LocalDate startdatoForeldrepenger = LocalDate.now(FPDateUtil.getOffset()).minusMonths(3).minusMonths(3);
        InntektsmeldingM im2 = InntektsmeldingMTestdataBuilder.inntektsmelding(mor.getPersonIdent(), inntektBeløp)
            .medStartdatoForeldrepenger(startdatoForeldrepenger)
            .build();
        JournalpostMottakDtoBuilder journalpostBuilderIM2 = journalpostInntektsmeldingBuilder(fagsak, im2, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM2);

        // Assert
        behandling = repository.hent(Behandling.class, behandlingId);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(OPPTJENINGSPERIODEVILKÅR, IKKE_VURDERT),
                resultat(OPPTJENINGSVILKÅRET, IKKE_VURDERT),
                resultat(BEREGNINGSGRUNNLAGVILKÅR, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS, AVBRUTT),
            AksjonspunktTestutfall.resultat(AVKLAR_STARTDATO_FOR_FORELDREPENGEPERIODEN, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        // Skal ha lagt til oppdatert behandlingsårsak i tillegg til initiell
        assertThat(behandling.getBehandlingÅrsaker().stream().map(BehandlingÅrsak::getBehandlingÅrsakType).collect(toList()))
            .containsExactlyInAnyOrder(BehandlingÅrsakType.RE_REGISTEROPPLYSNING, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
    }

    @Test
    public void skal_utføre_tilbakehopp_til_startpunkt_kontroller_arbeidsforhold_dersom_im_endrer_arbeidsforhold_id() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(6);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.løpendeForhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).get(0).getAksjonspunktDefinisjon())
            .isEqualTo(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD);

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og sendes til manuell fastsetting ber.gr.
        BigDecimal inntektBeløp = BigDecimal.valueOf(20000); // Fra Inntekskomponenten. Areg-inntekt = 40000
        LocalDate startdatoUttak = fødselsdatoBarn.minusWeeks(3);
        InntektsmeldingM inntektsmelding = InntektsmeldingMTestdataBuilder.inntektsmelding(mor.getPersonIdent(), inntektBeløp)
            .medStartdatoForeldrepenger(startdatoUttak)
            .build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, inntektsmelding, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        // Vilkår
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(OPPTJENINGSPERIODEVILKÅR, OPPFYLT),
                resultat(OPPTJENINGSVILKÅRET, OPPFYLT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(BEREGNINGSGRUNNLAGVILKÅR, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));

        // Arrange steg 3: Send inn ny inntektsmelding med endret ArbeidsforholdId -> skal spoles tilbake til KOARB
        String endretArbeidsforholdId = "123";
        InntektsmeldingM inntektsmelding2 = InntektsmeldingMTestdataBuilder.inntektsmelding(mor.getPersonIdent(), inntektBeløp)
            .medStartdatoForeldrepenger(startdatoUttak)
            .medArbeidsforholdId(endretArbeidsforholdId)
            .build();
        JournalpostMottakDtoBuilder journalpostBuilderIM2 = journalpostInntektsmeldingBuilder(fagsak, inntektsmelding2, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM2);

        // Assert
        behandling = repository.hent(Behandling.class, behandlingId);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(SØKERSOPPLYSNINGSPLIKT, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(OPPTJENINGSPERIODEVILKÅR, IKKE_VURDERT),
                resultat(OPPTJENINGSVILKÅRET, IKKE_VURDERT),
                resultat(BEREGNINGSGRUNNLAGVILKÅR, IKKE_VURDERT)));
        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.KONTROLLER_FAKTA_ARBEIDSFORHOLD);
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(VURDER_ARBEIDSFORHOLD, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS, AVBRUTT));
    }

    @Test
    public void skal_opprette_ny_førstegangsbehandling_ved_innsending_av_ny_søknad_kompletthet_ikke_passert() {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(6);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.løpendeForhold100prosent40timer(mor.getFnr());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).get(0).getAksjonspunktDefinisjon())
            .isEqualTo(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD);

        // Arrange/Act steg 2: Send inn ny førstegangssøknad
        Long nyFørstegangsbehandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(nyFørstegangsbehandlingId).isNotEqualTo(behandlingId);
        assertThat(behandling.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(BehandlingResultatType.MERGET_OG_HENLAGT);
    }

    @Test
    public void skal_opprette_ny_førstegangsbehandling_ved_innsending_av_ny_søknad_kompletthet_passert() throws SQLException, IOException, URISyntaxException {
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
        assertThat(behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).get(0).getAksjonspunktDefinisjon())
            .isEqualTo(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD);

        // Arrange steg 2: Send inn inntektsmelding - passér kompletthetssjekk
        InntektsmeldingM inntektsmelding = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, inntektsmelding, repositoryProvider);

        // Act
        behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isFalse();

        // Arrange/Act steg 3: Send inn ny førstegangssøknad -> skal opprette ny førstegangsbehandling
        Long nyFørstegangsbehandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling nyFørstegangsbehandling = repository.hent(Behandling.class, nyFørstegangsbehandlingId);
        behandling = repository.hent(Behandling.class, behandlingId);

        assertThat(nyFørstegangsbehandlingId).isNotEqualTo(behandlingId);
        assertThat(behandling.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(BehandlingResultatType.MERGET_OG_HENLAGT);
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseRepository.hentAggregat(nyFørstegangsbehandling, null);
        Optional<InntektsmeldingAggregat> inntektsmeldinger = inntektArbeidYtelseGrunnlag.getInntektsmeldinger();
        assertThat(inntektsmeldinger).isPresent();
        assertThat(inntektsmeldinger.get().getInntektsmeldinger()).hasSize(1);
    }

    @Test
    public void fødsel_med_avvik_i_antall_barn_mellom_tps_og_søknad() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1
        int antallBarnFraSøknad = 2;
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn)
            .medFødsel(new SøknadTestdataBuilder.FødselBuilder()
                .medFoedselsdato(fødselsdatoBarn)
                .medAntallBarn(antallBarnFraSøknad))
            .build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, OPPRETTET));

        // Arrange Steg 2: Send inn inntektsmelding
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertThat(repository.hentAlle(Beregning.class)).hasSize(0);
    }

    @Test
    public void fødsel_med_fødselsdato_mindre_enn_14_dager_gammel_som_ikke_er_registrert_i_tps() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        TpsPerson kvinneUtenBarn = TpsTestSett.kvinneUtenBarn().getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(kvinneUtenBarn.getFnr());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(kvinneUtenBarn.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Søkes om stønad <= 14 dager etter fødsel, barn ikke registrert i TPS
        LocalDate søknadsdato = LocalDate.now();
        LocalDate fødselsdatoFraSøknad = LocalDate.now().minusDays(14);
        Soeknad søknad = FørstegangssøknadTestSett.morFødselStandardUttak(kvinneUtenBarn.getAktørId(), fødselsdatoFraSøknad)
            .medMottattdato(søknadsdato)
            .build();
        JournalpostMottakDtoBuilder journalpostBuilderSøknad = journalpostSøknadBuilder(fagsak, søknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderSøknad);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).get(0).getAksjonspunktDefinisjon())
            .isEqualTo(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD);

        // Arrange steg 2: Send inn inntektsmelding -> skal ta behandling av vent, og deretter vente på fødselsmelding
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(kvinneUtenBarn.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, IKKE_VURDERT),
                resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_FØDSELREGISTRERING, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));

        // Arrange steg 3: Gjenoppta beh. når fødselsdato overstiger 14 dager -> oppretter ap SJEKK_MANGLENDE_FØDSEL
        behandling = behandlingRepository.hentBehandling(behandlingId);
        simulerAtFødselsdatoOverstiger14dager(fødselsdatoFraSøknad);
        GjenopptaBehandlingDto gjenopptaBehandlingDto = GjenopptaBehandlingDtoBuilder.build(behandling);

        // Act
        behandlingRestTjenesteAPI.gjenopptaBehandling(gjenopptaBehandlingDto);

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_FØDSELREGISTRERING, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT)));
    }

    /**
     * Far som søker med rolle «mor» må i utgangspunktet stoppes i selvbetjeningsløsningen, men vi må kunne håndtere dette i Vedtaksløsningen.
     * Vi trenger derfor et nytt krav på PK-53433 som sier at dersom søkers kjønn er mann,
     * skal han alltid behandles som far av VL (uavhengig av hvilken rolle det er søkt med i selvbetjening).
     *
     * @throws SQLException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Ignore("TODO (Glittum): MottattDokumentOversetterSøknad#utledRolle() vil returnere FARA, og dermed vil aldri FØDSELSVILKÅRET_FAR_MEDMOR vurderes")
    @Test
    public void far_som_søker_på_vegne_av_mor() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson far = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getMedforelder().get();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(far.getFnr());
        InntektTestSett.inntekt36mnd40000kr(far.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(far.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange
        Soeknad soeknadFarSøkerSomMor = FørstegangssøknadTestSett.morFødselStandardUttak(far.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknadFarSøkerSomMor, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Forventer at behandlingen står på vent
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).get(0).getAksjonspunktDefinisjon())
            .isEqualTo(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD);
        assertThat(fagsak.getRelasjonsRolleType()).isEqualTo(RelasjonsRolleType.FARA);

        // Arrange steg 2: Send inn inntektsmelding
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(far.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_FAR_MEDMOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT)));
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(VURDER_OM_VILKÅR_FOR_SYKDOM_OPPFYLT, OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_TERMINBEKREFTELSE, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
    }

    @Test
    public void søker_er_medmor_uten_registrert_fødsel() throws IOException, URISyntaxException, SQLException {
        // Pre-arrange: Registerdata + fagsak
        LocalDate fødselsdatoIrrelevantBarn = LocalDate.now().minusYears(10);
        TpsPerson medmor = TpsTestSett.medmor(fødselsdatoIrrelevantBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(medmor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(medmor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(medmor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange 1: send inn søknad for medmor
        LocalDate søknadsdato = LocalDate.now();
        LocalDate termindatoFraSøknad = søknadsdato.plusDays(7);
        LocalDate utstedtDatoTerminbekreftelse = søknadsdato;

        Soeknad soeknad = FørstegangssøknadTestSett.morTerminStandardUttak(medmor.getAktørId(), søknadsdato, termindatoFraSøknad, utstedtDatoTerminbekreftelse)
            .medSøker(ForeldreType.MEDMOR, medmor.getAktørId())
            .build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, OPPRETTET));

        // Arrange/Act steg 2: Send inn inntektsmelding -> behandling tas av vent, barn må avklares av saksbehandler
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(medmor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_TERMINBEKREFTELSE, OPPRETTET),
            AksjonspunktTestutfall.resultat(VURDER_OM_VILKÅR_FOR_SYKDOM_OPPFYLT, OPPRETTET));

        // Steg 3: Avklar fakta fra GUI -> vilkår oppfylles
        List<BekreftetAksjonspunktDto> bekreftetAksjonspunktDtos = byggAvklarFaktaDtoer(termindatoFraSøknad, utstedtDatoTerminbekreftelse, 1);
        BekreftedeAksjonspunkterDto dto = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), bekreftetAksjonspunktDtos);

        // Act
        aksjonspunktRestTjenesteAPI.bekreft(dto);

        // Assert
        assertThat(repository.hentAlle(UidentifisertBarnEntitet.class)).hasSize(0);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            Collections.singletonList(resultat(FØDSELSVILKÅRET_FAR_MEDMOR, OPPFYLT)));
    }

    private void simulerAtFødselsdatoOverstiger14dager(LocalDate fødselsdatoFraSøknad) {
        EntityManager entityManager = repoRule.getEntityManager();
        Query oppdatering = entityManager.createQuery(
            "UPDATE UidentifisertBarn SET fødselsdato=:fødselsdato");
        oppdatering.setParameter("fødselsdato", fødselsdatoFraSøknad.minusDays(7)); //$NON-NLS-1$
        oppdatering.executeUpdate();
        repository.flushAndClear();
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

    private BeregningsresultatAndel hentAndel(BeregningsresultatPeriode beregningsresultatPeriode, boolean erMottaker) {
        return beregningsresultatPeriode.getBeregningsresultatAndelList()
            .stream()
            .filter(a -> a.erBrukerMottaker() == erMottaker)
            .findFirst()
            .get();
    }

    private List<BekreftetAksjonspunktDto> byggAvklarFaktaDtoer(LocalDate termindato, LocalDate utstedtdato, int antallBarn) {
        VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerDto morErSyk = new VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerDto("Mor er syk",
            true);
        return asList(morErSyk, new BekreftTerminbekreftelseAksjonspunktDto("Grunn", termindato, utstedtdato, antallBarn));
    }
}
