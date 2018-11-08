package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.foreldrepenger;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType.RE_ENDRING_BEREGNINGSGRUNNLAG;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType.RE_HENDELSE_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType.RE_OPPLYSNINGER_OM_YTELSER;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType.RE_REGISTEROPPLYSNING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_STARTDATO_FOR_FORELDREPENGEPERIODEN;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.OVERSTYRING_AV_FØDSELSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.REGISTRER_PAPIRSØKNAD_FORELDREPENGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostEndringssøknadBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostInntektsmeldingBuilder;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.ReferanseType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.ReaktiveringStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.SatsType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPeriodeGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetKlassifisering;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgradEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.FordelingPeriodeKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeSøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.FødselHendelseDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.ArbeidsforholdTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InfotrygdVedtakTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InntektTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.BehandlingÅrsakTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.AksjonspunktRestTjenesteTestAPI;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.FordelRestTjenesteTestAPI;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.HendelserRestTjenesteTestAPI;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataInnhenter;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.kontrakter.abonnent.HendelseWrapperDto;
import no.nav.foreldrepenger.kontrakter.abonnent.infotrygd.InfotrygdHendelseDtoBuilder;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.OverstyrteAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringFødselsvilkåretDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;
import no.seres.xsd.nav.inntektsmelding_m._20180924.ObjectFactory;

@RunWith(CdiRunner.class)
public class BehandlingRevurderingTestIT {

    private static final BigDecimal BEREGNET_PR_ÅR = new BigDecimal(448000);
    private static final String ORGNR = "973093681";
    private static final String ARBFORHOLD_ID = "ARBEIDSFORHOLDID";

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private BehandlingRepository behandlingRepo;

    @Inject
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    @Inject
    private BeregningsresultatFPRepository beregningsresultatFPRepository;

    @Inject
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    private PersonopplysningTjeneste personopplysningTjeneste;

    @Inject
    private RegisterdataInnhenter registerdataInnhenter;

    @Inject
    private TpsAdapter tpsAdapter;

    @Inject
    private BeregningArbeidsgiverTestUtil beregningArbeidsgiverTestUtil;

    @Inject
    @FagsakYtelseTypeRef("FP")
    private IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste;

    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    @Inject
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    @Inject
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;

    private YtelsesFordelingRepository ytelsesFordelingRepository;

    private UttakRepository uttakRepository;

    private OpptjeningRepository opptjeningRepository;

    private VirksomhetRepository virksomhetRepository;

    @Inject
    private RegisterKontekst registerKontekst;
    // Test-API-er rundt REST-tjenestene
    @Inject
    private FordelRestTjenesteTestAPI fordelRestTjenesteAPI;
    @Inject
    private AksjonspunktRestTjenesteTestAPI aksjonspunktRestTjenesteAPI;
    @Inject
    private HendelserRestTjenesteTestAPI hendelserRestTjenesteTestAPI;


    @Before
    public void oppsett() {
        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.opptjeningRepository = repositoryProvider.getOpptjeningRepository();
        this.virksomhetRepository = repositoryProvider.getVirksomhetRepository();

        BeregningRepository beregningRepository = repositoryProvider.getBeregningRepository();
        grunnbeløp = BigDecimal.valueOf(beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, LocalDate.now().minusDays(5)).getVerdi());

        registerKontekst.intialiser();
        System.setProperty("dato.for.nye.beregningsregler", "2010-01-01");
    }

    @After
    public void teardown() {
        registerKontekst.nullstill();
        System.setProperty("dato.for.nye.beregningsregler", "2019-01-01");
    }

    private BigDecimal grunnbeløp;

    @Test
    public void skal_sette_startpunkt_inngangsvilkår_oppl_dersom_fødselshendelse_endrer_skjæringstidspunkt() {
        // Pre-Arrange: Registerdata
        LocalDate fødselsdato = LocalDate.of(2017, 1, 11);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());

        // Arrange - opprette førstegangsbehandling som skal revurderes
        LocalDate uttaksdato = fødselsdato.plusDays(1); // Fødsel er før uttaksdato - skjæringstidspunkt endres
        ScenarioMorSøkerForeldrepenger førstegangsscenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE);
        førstegangsscenario.medSøknadHendelse()
            //.medAntallBarn(1)
            .medTerminbekreftelse(førstegangsscenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medTermindato(fødselsdato)
                .medUtstedtDato(fødselsdato));
        førstegangsscenario.medOverstyrtHendelse()
            .medTerminbekreftelse(førstegangsscenario.medOverstyrtHendelse().getTerminbekreftelseBuilder()
                .medTermindato(fødselsdato)
                .medUtstedtDato(fødselsdato));
        // Legg til aksjonspunkt som skal bli historisk på revurdering
        førstegangsscenario.leggTilAksjonspunkt(REGISTRER_PAPIRSØKNAD_FORELDREPENGER, BehandlingStegType.REGISTRER_SØKNAD);
        førstegangsscenario.leggTilAksjonspunkt(AVKLAR_FAKTA_FOR_PERSONSTATUS, BehandlingStegType.INNHENT_REGISTEROPP);
        førstegangsscenario.leggTilAksjonspunkt(AVKLAR_TERMINBEKREFTELSE, BehandlingStegType.KONTROLLER_FAKTA);
        førstegangsscenario.leggTilAksjonspunkt(OVERSTYRING_AV_FØDSELSVILKÅRET, null);
        førstegangsscenario.medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT);
        førstegangsscenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now())
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav Navesen")
            .build();
        Behandling origBehandling = førstegangsscenario.lagre(repositoryProvider);

        opprettYtelseFordeling(uttaksdato, origBehandling);
        byggOgLagreSøknadMedEksisterendeOppgittFordeling(origBehandling, fødselsdato.minusMonths(1));
        leggTilArbeidsinntekt(origBehandling);
        avsluttBehandlingOgFagsak(origBehandling, FagsakStatus.LØPENDE);
        byggUttaksperiodegrense(uttaksdato, origBehandling);
        opprettUttakResultat(origBehandling, uttaksdato);
        leggTilSøkersPersonopplysning(origBehandling, mor.getPersonIdent(), mor.getAktørId());

        // Arrange - opprett forretningshendelse som trigger revurdering (fødsel)
        FødselHendelseDtoBuilder hendelseBuilder = FødselHendelseDtoBuilder.builder()
            .medForelder(mor.getAktørId())
            .medFødselsdato(fødselsdato);
        HendelseWrapperDto dto = HendelseWrapperDto.lagDto(hendelseBuilder.build());

        // Act - mottak og behandling
        hendelserRestTjenesteTestAPI.mottaHendelse(dto);

        // Assert
        Behandling revurdering = behandlingRepo.hentSisteBehandlingForFagsakId(origBehandling.getFagsakId())
            .orElseThrow(() -> new IllegalStateException("Skal ikke kunne havne her"));
        assertUtil.assertBehandlingÅrsak(BehandlingÅrsakTestutfall.resultat(revurdering.getId(), asList(RE_HENDELSE_FØDSEL, RE_REGISTEROPPLYSNING)));
        assertThat(revurdering.getType()).isEqualTo(BehandlingType.REVURDERING);
        assertThat(revurdering.getStartpunkt()).isEqualTo(StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT);

        assertThat(personopplysningTjeneste.hentPersonopplysninger(revurdering).getBarna()).hasSize(1);

        FamilieHendelse bekreftetFamilieHendelse = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(revurdering).getBekreftetVersjon()
            .orElseThrow(() -> new IllegalStateException("Skal ikke kunne havne her"));
        assertThat(bekreftetFamilieHendelse.getType()).isEqualTo(FamilieHendelseType.FØDSEL);
        assertThat(bekreftetFamilieHendelse.getBarna()).hasSize(1);
        assertThat(bekreftetFamilieHendelse.getFødselsdato().get()).isEqualTo(fødselsdato);

        assertAksjonspunktAktivt(revurdering, REGISTRER_PAPIRSØKNAD_FORELDREPENGER, false);
        assertAksjonspunktErSlettet(revurdering, AVKLAR_TERMINBEKREFTELSE);
        assertAksjonspunktAktivt(revurdering, SJEKK_MANGLENDE_FØDSEL, true);
        assertAksjonspunktAktivt(revurdering, OVERSTYRING_AV_FØDSELSVILKÅRET, true);
    }

    @Test
    public void skal_sette_startpunkt_uttaksvilkår_dersom_fødselhendelse_som_ikke_endrer_skjæringstidspunktet() {
        // Pre-Arrange: Registerdata
        LocalDate fødselsdato = LocalDate.of(2017, 1, 11);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());

        // -------------------------------
        // STAGE 1:  Førstegangsbehandling
        // -------------------------------
        // Arrange - opprette fagsak og behandling
        //LocalDate fødselsdato = hentFødselsdatoFraFnr(STD_BARN_FNR);
        LocalDate uttaksdato = fødselsdato; // Fødsel sammenfaller med uttaksdato - skjæringstidspunkt endres ikke

        ScenarioMorSøkerForeldrepenger førstegangsscenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE);
        førstegangsscenario.medSøknad()
            .medMottattDato(fødselsdato.minusMonths(1));
        leggTilOpprettetOgBekreftetTermin(førstegangsscenario, fødselsdato);
        opprettBeregningsgrunnlag(førstegangsscenario);
        førstegangsscenario.medSøknadHendelse().medAntallBarn(1);
        førstegangsscenario.medOverstyrtHendelse().medAntallBarn(1);
        førstegangsscenario.leggTilAksjonspunkt(AVKLAR_TERMINBEKREFTELSE, BehandlingStegType.KONTROLLER_FAKTA);
        førstegangsscenario.leggTilAksjonspunkt(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG, BehandlingStegType.KONTROLLER_FAKTA);
        førstegangsscenario.leggTilAksjonspunkt(OVERSTYRING_AV_FØDSELSVILKÅRET, null);
        førstegangsscenario.medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT);
        førstegangsscenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now())
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav Navesen")
            .build();
        Behandling originalBehandling = førstegangsscenario.lagre(repositoryProvider);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(originalBehandling.getFagsak(), Dekningsgrad._100);

        opprettYtelseFordeling(uttaksdato, originalBehandling);
        leggTilSøkersPersonopplysning(originalBehandling, mor.getPersonIdent(), mor.getAktørId());
        leggTilArbeidsinntekt(originalBehandling);
        leggTilOpptjening(originalBehandling, fødselsdato);
        avsluttBehandlingOgFagsak(originalBehandling, FagsakStatus.LØPENDE);
        byggUttaksperiodegrense(uttaksdato, originalBehandling);
        opprettUttakResultat(originalBehandling, uttaksdato);

        // -------------------------------
        // STAGE 2:  Opprett Revurdering
        // -------------------------------
        // Arrange - opprett forretningshendelse som trigger revurdering (fødsel)
        FødselHendelseDtoBuilder hendelseBuilder = FødselHendelseDtoBuilder.builder()
            .medForelder(mor.getAktørId())
            .medFødselsdato(fødselsdato);
        HendelseWrapperDto dto = HendelseWrapperDto.lagDto(hendelseBuilder.build());

        // Act - mottak og behandling
        hendelserRestTjenesteTestAPI.mottaHendelse(dto);

        // Assert
        Behandling revurdering = behandlingRepo.hentSisteBehandlingForFagsakId(originalBehandling.getFagsakId())
            .orElseThrow(() -> new IllegalStateException("Skal ikke kunne havne her"));
        assertThat(revurdering.getStartpunkt()).isEqualTo(StartpunktType.UTTAKSVILKÅR);

        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(revurdering);
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder().get(0).getFom()).isEqualTo(uttaksdato);

        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelse = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(revurdering, null);
        assertThat(inntektArbeidYtelse).isPresent();
        assertThat(inntektArbeidYtelse.get().getOpplysningerFørSkjæringstidspunkt()).isPresent();
        assertThat(inntektArbeidYtelse.get().getOpplysningerFørSkjæringstidspunkt().get().getAktørArbeid().iterator().next().getYrkesaktiviteter()).hasSize(1);

        VilkårResultat vilkårResultat = revurdering.getBehandlingsresultat().getVilkårResultat();
        assertThat(vilkårResultat.getVilkårResultatType()).isEqualTo(VilkårResultatType.IKKE_FASTSATT);
        assertThat(vilkårResultat.getVilkårene()).hasSize(1);
        assertThat(vilkårResultat.getVilkårene().iterator().next().getVilkårType()).isEqualTo(VilkårType.FØDSELSVILKÅRET_MOR);

        Optional<Beregningsgrunnlag> beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlag(revurdering);
        assertThat(beregningsgrunnlag).isPresent();
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().size()).isEqualTo(1);
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().get(0).getBeregnetPrÅr()).isEqualTo(BEREGNET_PR_ÅR);

        Optional<Opptjening> opptjening = opptjeningRepository.finnOpptjening(revurdering);
        assertThat(opptjening).isPresent();
        assertThat(opptjening.get().getOpptjeningAktivitet()).hasSize(1);
        assertThat(opptjening.get().getOpptjeningAktivitet().get(0).getFom())
            .isBefore(opptjening.get().getOpptjeningAktivitet().get(0).getTom());

        assertAksjonspunktAktivt(revurdering, AVKLAR_TERMINBEKREFTELSE, false);
        assertAksjonspunktErSlettet(revurdering, MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG);
        assertAksjonspunktAktivt(revurdering, OVERSTYRING_AV_FØDSELSVILKÅRET, false);
    }

    @Test
    public void skal_sette_startpunkt_søker_relasjon_til_barn_dersom_fødselhendelse_endrer_personopplysning() {
        // Pre-Arrange: Registerdata
        LocalDate fødselsdato = LocalDate.of(2017, 1, 11);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());

        // Arrange - opprette fagsak og behandling
        LocalDate uttaksdato = fødselsdato; // Fødsel sammenfaller med uttaksdato - skjæringstidspunkt endres ikke

        ScenarioMorSøkerForeldrepenger førstegangsscenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE);
        førstegangsscenario.medSøknad().medMottattDato(LocalDate.now());
        førstegangsscenario.medMedlemskap().medBosattVurdering(true).medOppholdsrettVurdering(true);
        leggTilOpprettetOgBekreftetTermin(førstegangsscenario, fødselsdato);

        førstegangsscenario.leggTilAksjonspunkt(AVKLAR_TERMINBEKREFTELSE, BehandlingStegType.KONTROLLER_FAKTA);
        førstegangsscenario.leggTilAksjonspunkt(AVKLAR_OPPHOLDSRETT, BehandlingStegType.KONTROLLER_FAKTA);
        førstegangsscenario.medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT);
        førstegangsscenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now())
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav Navesen")
            .build();
        Behandling originalBehandling = førstegangsscenario.lagre(repositoryProvider);

        opprettYtelseFordeling(uttaksdato, originalBehandling);
        avsluttBehandlingOgFagsak(originalBehandling, FagsakStatus.LØPENDE);
        byggUttaksperiodegrense(uttaksdato, originalBehandling);
        opprettUttakResultat(originalBehandling, uttaksdato);
        leggTilSøkersPersonopplysning(originalBehandling, mor.getPersonIdent(), mor.getAktørId());

        MedlemskapsvilkårPeriodeGrunnlag.Builder builder = medlemskapVilkårPeriodeRepository.hentBuilderFor(originalBehandling);
        MedlemskapsvilkårPerioderEntitet.Builder periode = builder.getBuilderForVurderingsdato(LocalDate.now());
        periode.medVilkårUtfall(VilkårUtfallType.OPPFYLT);
        builder.leggTilMedlemskapsvilkårPeriode(periode);
        medlemskapVilkårPeriodeRepository.lagreMedlemskapsvilkår(originalBehandling, builder);

        // Arrange - opprett forretningshendelse som trigger revurdering (fødsel)
        FødselHendelseDtoBuilder hendelseBuilder = FødselHendelseDtoBuilder.builder()
            .medForelder(mor.getAktørId())
            .medFødselsdato(fødselsdato);
        HendelseWrapperDto dto = HendelseWrapperDto.lagDto(hendelseBuilder.build());

        // Act - mottak og behandling
        hendelserRestTjenesteTestAPI.mottaHendelse(dto);

        // Assert
        Behandling revurdering = behandlingRepo.hentSisteBehandlingForFagsakId(originalBehandling.getFagsakId())
            .orElseThrow(() -> new IllegalStateException("Skal ikke kunne havne her"));
        assertThat(revurdering.getStartpunkt()).isEqualTo(StartpunktType.SØKERS_RELASJON_TIL_BARNET);

        VilkårResultat vilkårResultat = revurdering.getBehandlingsresultat().getVilkårResultat();
        assertThat(vilkårResultat.getOriginalBehandling()).isEqualTo(revurdering);
        assertThat(vilkårResultat.getVilkårResultatType()).isEqualTo(VilkårResultatType.IKKE_FASTSATT);
        assertThat(vilkårResultat.getVilkårene().stream().map(Vilkår::getVilkårType).collect(toList()))
            .containsExactlyInAnyOrder(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårType.FØDSELSVILKÅRET_MOR, VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårResultat.getVilkårene().stream().filter(v -> v.getVilkårType().equals(VilkårType.SØKERSOPPLYSNINGSPLIKT)).map(Vilkår::getGjeldendeVilkårUtfall)
            .findFirst().get()).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(vilkårResultat.getVilkårene().stream().filter(v -> v.getVilkårType().equals(VilkårType.FØDSELSVILKÅRET_MOR)).map(Vilkår::getGjeldendeVilkårUtfall)
            .findFirst().get()).isEqualTo(VilkårUtfallType.IKKE_VURDERT);
        assertThat(vilkårResultat.getVilkårene().stream().filter(v -> v.getVilkårType().equals(VilkårType.MEDLEMSKAPSVILKÅRET)).map(Vilkår::getGjeldendeVilkårUtfall)
            .findFirst().get()).isEqualTo(VilkårUtfallType.IKKE_VURDERT);
    }

    @Test
    public void skal_motta_endringssøknad_med_endringer_på_ytelsefordeling() {
        // Pre-Arrange: Registerdata
        TpsPerson mor = TpsTestSett.kvinneUtenBarn().getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());

        // Arrange steg 1 - opprette behandling og innsende endringssøknad - startpunkt skal settes til Inngangsvilkår
        LocalDate termindato = LocalDate.now();
        LocalDate uttaksdato = termindato; // Fødsel sammenfaller med uttaksdato - skjæringstidspunkt endres ikke

        ScenarioMorSøkerForeldrepenger førstegangsscenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE);
        leggTilOpprettetOgBekreftetTermin(førstegangsscenario, termindato);
        opprettBeregningsgrunnlag(førstegangsscenario);

        førstegangsscenario.medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT);
        førstegangsscenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now().minusDays(1))
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav Navesen")
            .build();
        medFørstegangssøknad(termindato, førstegangsscenario);
        Behandling originalBehandling = førstegangsscenario.lagre(repositoryProvider);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(originalBehandling.getFagsak(), Dekningsgrad._100);

        // originalbehandling
        leggTilSøkersPersonopplysning(originalBehandling, mor.getPersonIdent(), mor.getAktørId());
        opprettYtelseFordelingMedFlerePerioder(uttaksdato, originalBehandling);
        leggTilOpptjening(originalBehandling, termindato);
        avsluttBehandlingOgFagsak(originalBehandling, FagsakStatus.LØPENDE);
        byggUttaksperiodegrense(uttaksdato, originalBehandling);
        opprettUttakResultat(originalBehandling, termindato);

        MedlemskapsvilkårPeriodeGrunnlag.Builder builder = medlemskapVilkårPeriodeRepository.hentBuilderFor(originalBehandling);
        MedlemskapsvilkårPerioderEntitet.Builder periode = builder.getBuilderForVurderingsdato(LocalDate.now());
        periode.medVilkårUtfall(VilkårUtfallType.OPPFYLT);
        builder.leggTilMedlemskapsvilkårPeriode(periode);
        medlemskapVilkårPeriodeRepository.lagreMedlemskapsvilkår(originalBehandling, builder);

        // Endringssøknad
        LocalDate endretUttaksdato = uttaksdato.plusDays(2);
        Soeknad soeknad = endringssøknadMedOppgittFordeling(mor.getAktørId(), termindato, endretUttaksdato);
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostEndringssøknadBuilder(originalBehandling.getFagsak(), soeknad, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling revurdering = behandlingRepo.hentSisteBehandlingForFagsakId(originalBehandling.getFagsakId()).get();
        assertThat(revurdering.getStartpunkt()).isEqualTo(StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT);
        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(revurdering);
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder()).hasSize(2);
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder().stream()
            .filter(oppgittPeriode -> oppgittPeriode.getPeriodeType().equals(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL))
            .map(OppgittPeriode::getFom)
            .findFirst()
            .get())
            .isEqualTo(endretUttaksdato.minusWeeks(3));
    }

    @Test
    public void skal_henlegge_og_opprette_ny_revurdering_dersom_mottak_av_endringssøknad_på_behandling_som_har_passert_kompletthet() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata
        LocalDate fødselsdato = LocalDate.now().minusDays(6);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());

        // Arrange steg 1: Sette opp innvilget behandling og deretter innsende IM -> revurdering opprettes
        LocalDate uttaksdato = fødselsdato.minusWeeks(4); // Uttak endrer skjæringsdato - StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP

        ScenarioMorSøkerForeldrepenger førstegangsscenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE);
        leggTilOpprettetOgBekreftetTermin(førstegangsscenario, fødselsdato);
        opprettBeregningsgrunnlag(førstegangsscenario);

        førstegangsscenario.medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT);
        førstegangsscenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now().minusDays(1))
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav Navesen")
            .build();
        medFørstegangssøknad(fødselsdato, førstegangsscenario);
        Behandling originalBehandling = førstegangsscenario.lagre(repositoryProvider);
        lagBeregningsresultatFP(originalBehandling);

        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(originalBehandling.getFagsak(), Dekningsgrad._100);

        // originalbehandling
        leggTilSøkersPersonopplysning(originalBehandling, mor.getPersonIdent(), mor.getAktørId());
        opprettYtelseFordelingMedFlerePerioder(uttaksdato, originalBehandling);
        leggTilArbeidsinntekt(originalBehandling);
        leggTilOpptjening(originalBehandling, fødselsdato);
        avsluttBehandlingOgFagsak(originalBehandling, FagsakStatus.LØPENDE);
        byggUttaksperiodegrense(uttaksdato, originalBehandling);
        opprettUttakResultat(originalBehandling, uttaksdato);

        MedlemskapsvilkårPeriodeGrunnlag.Builder builder = medlemskapVilkårPeriodeRepository.hentBuilderFor(originalBehandling);
        MedlemskapsvilkårPerioderEntitet.Builder periode = builder.getBuilderForVurderingsdato(LocalDate.now());
        periode.medVilkårUtfall(VilkårUtfallType.OPPFYLT);
        builder.leggTilMedlemskapsvilkårPeriode(periode);
        medlemskapVilkårPeriodeRepository.lagreMedlemskapsvilkår(originalBehandling, builder);

        // Inntektsmelding
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent())
            .medStartdatoForeldrepenger(fødselsdato.minusWeeks(3).plusDays(1))
            .build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(originalBehandling.getFagsak(), im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        Behandling revurdering = behandlingRepo.hentSisteBehandlingForFagsakId(originalBehandling.getFagsakId()).get();
        assertThat(revurdering.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.SØKERS_RELASJON_TIL_BARN);

        // Arrange steg 2: Send inn endringssøknad - åpen behandling skal bli "oppdatert gjennom henlegging"
        LocalDate endretUttaksdato = uttaksdato.minusDays(1); // Uttak endrer skjæringsdato igjen - StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP
        Soeknad soeknad = endringssøknadMedOppgittFordeling(mor.getAktørId(), fødselsdato, endretUttaksdato);
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostEndringssøknadBuilder(originalBehandling.getFagsak(), soeknad, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        assertThat(revurdering.isBehandlingHenlagt()).isTrue();

        Behandling nyRevurdering = behandlingRepo.hentSisteBehandlingForFagsakId(originalBehandling.getFagsakId()).get();
        assertThat(nyRevurdering.getId()).isNotEqualTo(revurdering.getId());
        //assertThat(nyRevurdering.getStartpunkt()).isEqualTo(StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP);
        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(nyRevurdering);
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder()).hasSize(2);
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder().stream()
            .filter(oppgittPeriode -> oppgittPeriode.getPeriodeType().equals(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL))
            .map(OppgittPeriode::getFom)
            .findFirst()
            .get())
            .isEqualTo(endretUttaksdato.minusWeeks(3));
        InntektArbeidYtelseGrunnlag inntektAggregat = repositoryProvider.getInntektArbeidYtelseRepository().hentAggregat(nyRevurdering, null);
        assertThat(inntektAggregat.getInntektsmeldinger()).isPresent();
        assertThat(inntektAggregat.getInntektsmeldinger().get().getInntektsmeldinger()).hasSize(1);
        assertUtil.assertBehandlingÅrsak(BehandlingÅrsakTestutfall.resultat(nyRevurdering.getId(), asList(RE_ENDRING_FRA_BRUKER, RE_REGISTEROPPLYSNING, RE_ENDRET_INNTEKTSMELDING)));
    }

    @Test
    public void skal_kopiere_uttak_ved_avslått_inngangsvilkår_på_revurdering() throws URISyntaxException {
        // Pre-Arrange: Registerdata
        LocalDate fødselsdato = LocalDate.of(2017, 1, 11);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());

        // -------------------------------
        // STAGE 1:  Førstegangsbehandling
        // -------------------------------
        // Arrange - opprette fagsak og behandling
        LocalDate uttaksdato = fødselsdato; // Fødsel sammenfaller med uttaksdato - skjæringstidspunkt endres ikke

        ScenarioMorSøkerForeldrepenger førstegangsscenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE);
        førstegangsscenario.medSøknad().medMottattDato(fødselsdato.minusMonths(1));
        leggTilOpprettetOgBekreftetTermin(førstegangsscenario, fødselsdato);
        opprettBeregningsgrunnlag(førstegangsscenario);
        førstegangsscenario.medSøknadHendelse().medAntallBarn(1);
        førstegangsscenario.medOverstyrtHendelse().medAntallBarn(1);
        førstegangsscenario.medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT);
        førstegangsscenario.medBehandlingVedtak().medVedtakResultatType(VedtakResultatType.INNVILGET).build();

        Behandling originalBehandling = førstegangsscenario.lagre(repositoryProvider);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(originalBehandling.getFagsak(), Dekningsgrad._100);

        opprettYtelseFordeling(uttaksdato, originalBehandling);
        leggTilSøkersPersonopplysning(originalBehandling, mor.getPersonIdent(), mor.getAktørId());
        leggTilArbeidsinntekt(originalBehandling);
        leggTilOpptjening(originalBehandling, fødselsdato);
        avsluttBehandlingOgFagsak(originalBehandling, FagsakStatus.LØPENDE);
        byggUttaksperiodegrense(uttaksdato, originalBehandling);
        opprettUttakResultat(originalBehandling, uttaksdato);

        // -------------------------------
        // STAGE 2:  Opprett Revurdering
        // -------------------------------
        // Arrange - opprett forretningshendelse som trigger revurdering (fødsel)
        FødselHendelseDtoBuilder hendelseBuilder = FødselHendelseDtoBuilder.builder()
            .medForelder(mor.getAktørId())
            .medFødselsdato(fødselsdato);
        HendelseWrapperDto dto = HendelseWrapperDto.lagDto(hendelseBuilder.build());

        // Act
        hendelserRestTjenesteTestAPI.mottaHendelse(dto);

        // Assert
        Behandling revurdering = behandlingRepo.hentSisteBehandlingForFagsakId(originalBehandling.getFagsakId())
            .orElseThrow(() -> new IllegalStateException("Skal ikke kunne havne her"));
        assertThat(revurdering.getStartpunkt()).isEqualTo(StartpunktType.UTTAKSVILKÅR);

        // Arrange steg 2: Avslå søkers relasjon til barn -> skal kopiere uttak fra forrige behandling
        OverstyringFødselsvilkåretDto dto2 = new OverstyringFødselsvilkåretDto(false, "ovesrtyrt ikke oppfylt", Avslagsårsak.MANGLENDE_DOKUMENTASJON.getKode());

        // Act
        aksjonspunktRestTjenesteAPI.overstyr(OverstyrteAksjonspunkterDto.lagDto(revurdering.getId(), revurdering.getVersjon(), singletonList(dto2)));

        // Assert
        revurdering = behandlingRepo.hentBehandling(revurdering.getId());

        VilkårResultat vilkårResultat = revurdering.getBehandlingsresultat().getVilkårResultat();
        assertThat(vilkårResultat.getVilkårResultatType()).isEqualTo(VilkårResultatType.AVSLÅTT);

        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(revurdering);
        List<OppgittPeriode> oppgittePerioder = ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder();
        assertThat(oppgittePerioder).hasSize(1);

        OppgittPeriode oppgittPeriod = oppgittePerioder.get(0);
        assertThat(oppgittPeriod.getPeriodeKilde()).isEqualTo(FordelingPeriodeKilde.TIDLIGERE_VEDTAK);
        assertThat(oppgittPeriod.getPeriodeType()).isEqualTo(UttakPeriodeType.FORELDREPENGER);
        assertThat(oppgittPeriod.getFom()).isEqualTo(fødselsdato);
    }

    private void medFørstegangssøknad(LocalDate fødselsdato, AbstractTestScenario førstegangsscenario) {
        OppgittPeriode søknadsperiode = OppgittPeriodeBuilder.ny()
            .medPeriode(fødselsdato.minusDays(1).minusWeeks(3), fødselsdato.minusDays(1))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .build();
        OppgittFordelingEntitet fordeling = new OppgittFordelingEntitet(Collections.singletonList(søknadsperiode), true);
        førstegangsscenario.medSøknad().medFordeling(fordeling);
        førstegangsscenario.medFordeling(fordeling);
    }

    @Test
    public void skal_motta_endringssøknad_på_behandling_som_ikke_har_passert_kompletthet() throws SQLException, IOException, URISyntaxException {
        // Pre-Arrange: Registerdata
        LocalDate fødselsdato = LocalDate.now().minusDays(6);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdato).getBruker();
        ArbeidsforholdTestSett.løpendeForhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        // Arrange steg 1: Sette opp innvilget behandling og deretter innsende IM -> revurdering opprettes
        LocalDate uttaksdato = fødselsdato; // Fødsel sammenfaller med uttaksdato - skjæringstidspunkt endres ikke

        ScenarioMorSøkerForeldrepenger førstegangsscenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE);
        leggTilOpprettetOgBekreftetTermin(førstegangsscenario, fødselsdato);
        opprettBeregningsgrunnlag(førstegangsscenario);

        førstegangsscenario.medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT);
        førstegangsscenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now().minusDays(1))
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav Navesen")
            .build();
        medFørstegangssøknad(fødselsdato, førstegangsscenario);
        Behandling originalBehandling = førstegangsscenario.lagre(repositoryProvider);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(originalBehandling.getFagsak(), Dekningsgrad._100);

        // originalbehandling
        leggTilSøkersPersonopplysning(originalBehandling, mor.getPersonIdent(), mor.getAktørId());
        opprettYtelseFordelingMedFlerePerioder(uttaksdato, originalBehandling);
        leggTilArbeidsinntekt(originalBehandling);
        leggTilOpptjening(originalBehandling, fødselsdato);
        avsluttBehandlingOgFagsak(originalBehandling, FagsakStatus.LØPENDE);
        byggUttaksperiodegrense(uttaksdato, originalBehandling);
        leggTilUttaksresultat(originalBehandling, uttaksdato);


        MedlemskapsvilkårPeriodeGrunnlag.Builder builder = medlemskapVilkårPeriodeRepository.hentBuilderFor(originalBehandling);
        MedlemskapsvilkårPerioderEntitet.Builder periode = builder.getBuilderForVurderingsdato(LocalDate.now());
        periode.medVilkårUtfall(VilkårUtfallType.OPPFYLT);
        builder.leggTilMedlemskapsvilkårPeriode(periode);
        medlemskapVilkårPeriodeRepository.lagreMedlemskapsvilkår(originalBehandling, builder);

        // Act
        // Inntektsmelding
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent())
            .medStartdatoForeldrepenger(fødselsdato.minusWeeks(3))
            .medGradering(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(25).minusDays(1), BigDecimal.valueOf(50))
            .build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(originalBehandling.getFagsak(), im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        Behandling revurdering = behandlingRepo.hentSisteBehandlingForFagsakId(originalBehandling.getFagsakId()).get();
        assertThat(revurdering.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.VURDER_KOMPLETTHET);

        // Arrange steg 2: Send inn endringssøknad -> revurdering passerer kompletthetssjekk
        LocalDate endretUttaksdato = uttaksdato.minusWeeks(4);
        Soeknad soeknad = endringssøknadMedOppgittFordeling(mor.getAktørId(), fødselsdato, endretUttaksdato);
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostEndringssøknadBuilder(originalBehandling.getFagsak(), soeknad, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        assertThat(revurdering.getStartpunkt()).isEqualTo(StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_STARTDATO_FOR_FORELDREPENGEPERIODEN, OPPRETTET)));
    }

    @Test
    public void skal_sette_startpunkt_inngangsvilkår_oppl_dersom_infotrygdhendelse_endrer_opptjening() throws URISyntaxException {
        // Pre-Arrange: Registerdata
        TpsPerson mor = TpsTestSett.kvinneUtenBarn().getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());

        // Arrange steg 1 - opprette behandling og innsende endringssøknad - startpunkt skal settes til Inngangsvilkår
        LocalDate termindato = LocalDate.now();
        LocalDate uttaksdato = termindato; // Fødsel sammenfaller med uttaksdato - skjæringstidspunkt endres ikke

        ScenarioMorSøkerForeldrepenger førstegangsscenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE);
        leggTilOpprettetOgBekreftetTermin(førstegangsscenario, termindato);
        opprettBeregningsgrunnlag(førstegangsscenario);

        førstegangsscenario.medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT);
        førstegangsscenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now().minusDays(1))
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav Navesen")
            .build();
        medFørstegangssøknad(termindato, førstegangsscenario);
        Behandling originalBehandling = førstegangsscenario.lagre(repositoryProvider);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(originalBehandling.getFagsak(), Dekningsgrad._100);

        // originalbehandling
        leggTilSøkersPersonopplysning(originalBehandling, mor.getPersonIdent(), mor.getAktørId());
        opprettYtelseFordelingMedFlerePerioder(uttaksdato, originalBehandling);
        leggTilOpptjening(originalBehandling, termindato);
        avsluttBehandlingOgFagsak(originalBehandling, FagsakStatus.LØPENDE);
        byggUttaksperiodegrense(uttaksdato, originalBehandling);
        opprettUttakResultat(originalBehandling, termindato);

        MedlemskapsvilkårPeriodeGrunnlag.Builder builder = medlemskapVilkårPeriodeRepository.hentBuilderFor(originalBehandling);
        MedlemskapsvilkårPerioderEntitet.Builder periode = builder.getBuilderForVurderingsdato(LocalDate.now());
        periode.medVilkårUtfall(VilkårUtfallType.OPPFYLT);
        builder.leggTilMedlemskapsvilkårPeriode(periode);
        medlemskapVilkårPeriodeRepository.lagreMedlemskapsvilkår(originalBehandling, builder);

        // Arrange - opprett forretningshendelse som trigger revurdering - sett opp ytelse-testsett
        InfotrygdVedtakTestSett.infotrygdsakStandard(mor.getPersonIdent().getIdent(), 25L);
        InfotrygdHendelseDtoBuilder hendelseBuilder = InfotrygdHendelseDtoBuilder.endring()
            .medAktørId(mor.getAktørId().getId())
            .medFraOgMed(LocalDate.now())
            .medTypeYtelse("BS")
            .medIdentdato(LocalDate.now().toString())
            .medUnikId("8723897623976498");
        HendelseWrapperDto dto = HendelseWrapperDto.lagDto(hendelseBuilder.build());

        // Act - mottak og behandling
        hendelserRestTjenesteTestAPI.mottaHendelse(dto);

        // Assert
        Behandling revurdering = behandlingRepo.hentSisteBehandlingForFagsakId(originalBehandling.getFagsakId())
            .orElseThrow(() -> new IllegalStateException("Skal ikke kunne havne her"));
        assertUtil.assertBehandlingÅrsak(BehandlingÅrsakTestutfall.resultat(revurdering.getId(), asList(RE_REGISTEROPPLYSNING, RE_ENDRING_BEREGNINGSGRUNNLAG, RE_OPPLYSNINGER_OM_YTELSER)));
        assertThat(revurdering.getType()).isEqualTo(BehandlingType.REVURDERING);
        assertThat(revurdering.getStartpunkt()).isEqualTo(StartpunktType.SØKERS_RELASJON_TIL_BARNET);

        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_TERMINBEKREFTELSE, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

    }


    private void leggTilUttaksresultat(Behandling behandling, LocalDate uttaksdato) {
        UttakResultatPerioderEntitet opprinneligPerioder = new UttakResultatPerioderEntitet();
        UttakResultatPeriodeEntitet.Builder periodeBuilder = new UttakResultatPeriodeEntitet.Builder(uttaksdato.minusWeeks(3), uttaksdato.minusDays(1));
        periodeBuilder.medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT);
        opprinneligPerioder.leggTilPeriode(periodeBuilder.build());
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, opprinneligPerioder);
    }

    private Soeknad endringssøknadMedOppgittFordeling(AktørId aktørId, LocalDate fødselsdato, LocalDate uttaksdato) {
        return new SøknadTestdataBuilder().endringssøknadForeldrepenger()
            .medSøker(ForeldreType.MOR, aktørId)
            .medMottattdato(fødselsdato)
            .medFordeling(new SøknadTestdataBuilder.FordelingBuilder()
                .leggTilPeriode(uttaksdato.minusWeeks(3), uttaksdato.minusDays(1), UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
                .leggTilPeriode(uttaksdato, uttaksdato.plusWeeks(10).minusDays(1), UttakPeriodeType.FORELDREPENGER))
            .build();
    }

    private void leggTilSøkersPersonopplysning(Behandling behandling, PersonIdent fnr, AktørId aktørId) {
        // Henter søkers kjerneinfo fra TpsMock og legger på behandling, blir da identisk for søker ved re-innhenting.
        // I tillegg vil re-innhenting inneholde barn, som vil være diff ift her
        Personinfo søkerPersonInfo = tpsAdapter.hentKjerneinformasjon(fnr, aktørId);
        registerdataInnhenter.innhentPersonopplysninger(behandling, søkerPersonInfo, Optional.empty());
    }

    private void leggTilArbeidsinntekt(Behandling behandling, InntektsKilde kilde) {
        Interval interval = iayRegisterInnhentingTjeneste.beregnOpplysningsPeriode(behandling);
        InntektArbeidYtelseAggregatBuilder aggregatBuilder = iayRegisterInnhentingTjeneste.innhentOpptjeningForInnvolverteParter(behandling, interval);
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = aggregatBuilder.getAktørInntektBuilder(behandling.getAktørId());
        AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilderForYtelser(kilde);
        InntektEntitet.InntektspostBuilder inntektspost = InntektEntitet.InntektspostBuilder.ny()
            .medBeløp(BigDecimal.valueOf(40000L))
            .medPeriode(
                interval.getStart().atZone(ZoneId.systemDefault()).toLocalDate(),
                interval.getEnd().atZone(ZoneId.systemDefault()).toLocalDate().minusMonths(2)
            )
            .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(aggregatBuilder.getAktørArbeidBuilder(behandling.getAktørId()).getYrkesaktivitetBuilderForType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD).build().getArbeidsgiver());
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        aggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
        inntektArbeidYtelseTjeneste.lagre(behandling, aggregatBuilder);
    }

    private void leggTilArbeidsinntekt(Behandling behandling) {
        leggTilArbeidsinntekt(behandling, InntektsKilde.SIGRUN);
    }

    private void leggTilOpptjening(Behandling behandling, LocalDate fødselsdato) {
        LocalDate skjæringsdato = fødselsdato.minusWeeks(3);
        opptjeningRepository.lagreOpptjeningsperiode(behandling, skjæringsdato.minusYears(1), skjæringsdato);

        List<OpptjeningAktivitet> aktiviteter = new ArrayList<>();
        OpptjeningAktivitet opptjeningAktivitet = new OpptjeningAktivitet(skjæringsdato.minusMonths(10),
            skjæringsdato,
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORGNR,
            ReferanseType.ORG_NR);
        aktiviteter.add(opptjeningAktivitet);
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);
    }

    private void leggTilOpprettetOgBekreftetTermin(ScenarioMorSøkerForeldrepenger scenario, LocalDate termindato) {
        scenario.medSøknadHendelse()
            //.medAntallBarn(1)
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medTermindato(termindato)
                .medUtstedtDato(termindato.minusMonths(3)));
        scenario.medOverstyrtHendelse()
            .medTerminbekreftelse(scenario.medOverstyrtHendelse().getTerminbekreftelseBuilder()
                .medTermindato(termindato)
                .medUtstedtDato(termindato.minusMonths(3)));
    }

    private void opprettYtelseFordeling(LocalDate uttaksdato, Behandling behandling) {
        // TODO (essv): Heller bruke ScenarioMorSøkerForeldrepenger når den har støtte for å bygge YtelseFordeling
        OppgittFordelingEntitet fordeling = byggOppgittFordeling(uttaksdato);
        ytelsesFordelingRepository.lagre(behandling, fordeling);
        ytelsesFordelingRepository.lagre(behandling, OppgittDekningsgradEntitet.bruk100());
        ytelsesFordelingRepository.lagre(behandling, new OppgittRettighetEntitet(
            true, true, false));
    }

    private OppgittFordelingEntitet byggOppgittFordeling(LocalDate uttaksdato) {
        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(uttaksdato, uttaksdato.plusDays(1))
            .build();
        return new OppgittFordelingEntitet(singletonList(periode1), true);
    }

    private void byggOgLagreSøknadMedEksisterendeOppgittFordeling(Behandling behandling, LocalDate mottattDato) {
        OppgittFordeling oppgittFordeling = repositoryProvider.getYtelsesFordelingRepository().hentAggregat(behandling).getOppgittFordeling();

        FamilieHendelse familieHendelse = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getSøknadVersjon();
        Søknad søknad = new SøknadEntitet.Builder()
            .medRelasjonsRolleType(behandling.getRelasjonsRolleType())
            .medFordeling(oppgittFordeling)
            .medFamilieHendelse(familieHendelse)
            .medSøknadsdato(mottattDato)
            .medMottattDato(mottattDato)
            .build();
        repositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);
    }

    private void opprettYtelseFordelingMedFlerePerioder(LocalDate uttaksdato, Behandling behandling) {
        OppgittFordelingEntitet fordeling = byggOppgittFordelingMedFlerePerioder(uttaksdato);
        ytelsesFordelingRepository.lagre(behandling, fordeling);
        ytelsesFordelingRepository.lagre(behandling, OppgittDekningsgradEntitet.bruk100());
        ytelsesFordelingRepository.lagre(behandling, new OppgittRettighetEntitet(
            true, true, false));
    }

    private OppgittFordelingEntitet byggOppgittFordelingMedFlerePerioder(LocalDate uttaksdato) {
        OppgittPeriode fpFørFødselPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(uttaksdato.minusWeeks(3), uttaksdato.minusDays(1))
            .build();
        OppgittPeriode fpPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(uttaksdato, uttaksdato.plusWeeks(10).minusDays(1))
            .build();
        return new OppgittFordelingEntitet(asList(fpFørFødselPeriode, fpPeriode), true);
    }

    private void opprettBeregningsgrunnlag(ScenarioMorSøkerForeldrepenger scenario) {
        Beregningsgrunnlag beregningsgrunnlag = scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(grunnbeløp)
            .medRedusertGrunnbeløp(grunnbeløp)
            .build();

        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(LocalDate.now().minusDays(26), null)
            .medRedusertPrÅr(BigDecimal.valueOf(40000*12))
            .build(beregningsgrunnlag);
        ObjectFactory objectFactory = new ObjectFactory();
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORGNR))
            .medArbforholdRef(objectFactory.createArbeidsforholdArbeidsforholdId(ARBFORHOLD_ID).getValue());
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BEREGNET_PR_ÅR)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(bga)
            .build(beregningsgrunnlagPeriode);

        beregningsgrunnlag.leggTilBeregningsgrunnlagAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(beregningsgrunnlag));
    }

    private void avsluttBehandlingOgFagsak(Behandling behandling, FagsakStatus fagsakStatus) {
        behandling.avsluttBehandling();
        BehandlingLås lås = behandlingRepo.taSkriveLås(behandling);
        behandlingRepo.lagre(behandling, lås);

        FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), fagsakStatus);
    }

    private void assertAksjonspunktAktivt(Behandling revurdering, AksjonspunktDefinisjon apDef1, boolean erAktivt) {
        Optional<Aksjonspunkt> ap = revurdering.getAlleAksjonspunkterInklInaktive().stream()
            .filter(a -> a.getAksjonspunktDefinisjon().equals(apDef1))
            .findFirst();
        assertThat(ap).isPresent();
        assertThat(ap.get().erAktivt()).isEqualTo(erAktivt);
    }

    private void lagBeregningsresultatFP(Behandling behandling) {
        beregningsresultatFPRepository.lagre(behandling,BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build());
    }

    private void assertAksjonspunktErSlettet(Behandling revurdering, AksjonspunktDefinisjon apDef1) {
        Optional<Aksjonspunkt> ap = revurdering.getAlleAksjonspunkterInklInaktive().stream()
            .filter(a -> a.getAksjonspunktDefinisjon().equals(apDef1))
            .findFirst();
        Boolean erSlettet = ap.map(it -> it.getReaktiveringStatus().equals(ReaktiveringStatus.SLETTET)).orElse(Boolean.FALSE);
        assertThat(erSlettet).isTrue();
    }

    private void byggUttaksperiodegrense(LocalDate førsteLovligeUttaksdato, Behandling behandling) {
        Uttaksperiodegrense uttaksperiodegrense = new Uttaksperiodegrense.Builder(behandling)
            .medFørsteLovligeUttaksdag(førsteLovligeUttaksdato)
            .medMottattDato(førsteLovligeUttaksdato)
            .build();
        uttakRepository.lagreUttaksperiodegrense(behandling, uttaksperiodegrense);
    }

    private void opprettUttakResultat(Behandling behandling, LocalDate uttaksdato) {
        UttakResultatEntitet.Builder uttakResultatPlanBuilder = UttakResultatEntitet.builder(behandling);

        UttakResultatPeriodeEntitet uttakResultatPeriode = new UttakResultatPeriodeEntitet
            //.Builder(LocalDate.now().minusDays(7), LocalDate.now().minusDays(1))
            .Builder(uttaksdato, uttaksdato.plusDays(7))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .medPeriodeSoknad(new UttakResultatPeriodeSøknadEntitet.Builder().medUttakPeriodeType(UttakPeriodeType.MØDREKVOTE).build())
            .build();

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medOrgnr("000000000").oppdatertOpplysningerNå().build();
        repoRule.getRepository().lagre(virksomhet);

        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("123"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = UttakResultatPeriodeAktivitetEntitet.builder(uttakResultatPeriode,
            uttakAktivitet)
            .medTrekkonto(StønadskontoType.FORELDREPENGER)
            .medTrekkdager(10)
            .medArbeidsprosent(new BigDecimal(100))
            .medUtbetalingsprosent(new BigDecimal(100))
            .build();

        uttakResultatPeriode.leggTilAktivitet(periodeAktivitet);

        UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
        uttakResultatPerioder.leggTilPeriode(uttakResultatPeriode);

        UttakResultatEntitet uttakResultat = uttakResultatPlanBuilder.medOpprinneligPerioder(uttakResultatPerioder).build();
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, uttakResultat.getGjeldendePerioder());
    }

}
