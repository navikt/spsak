package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.foreldrepenger;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingskontroll.task.StartBehandlingTask.TASKTYPE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.BerørtBehandlingKontroller;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.task.StartBehandlingTask;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.ReferanseType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetKlassifisering;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgradEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personas;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
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
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.FørstegangssøknadTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSamling;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

@RunWith(CdiRunner.class)
public class BehandlingKøTestIT {

    private static final BigDecimal BEREGNET_PR_ÅR = new BigDecimal(448000d);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    @Inject
    @ProsessTask(TASKTYPE)
    private StartBehandlingTask startBehandlingTask;

    @Inject
    private DokumentmottakTestUtil hjelper;
    @Inject
    private BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste;
    @Inject
    private BerørtBehandlingKontroller berørtBehandlingKontroller;
    @Inject
    private BehandlingRepositoryProvider repositoryProvider;
    @Inject
    private TpsAdapter tpsAdapter;
    @Inject
    private ProsessTaskRepository prosessTaskRepository;
    @Inject
    private RegisterKontekst registerKontekst;

    private BehandlingRevurderingRepository behandlingRevurderingRepository;
    private FagsakRepository fagsakRepository;
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private FagsakRelasjonRepository fagsakRelasjonRepository;
    private BehandlingRepository behandlingRepository;
    private Fagsak fagsak;


    private UttakRepository uttakRepository;

    private OpptjeningRepository opptjeningRepository;

    @Before
    public void oppsett() {
        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.opptjeningRepository = repositoryProvider.getOpptjeningRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.fagsakRelasjonRepository = repositoryProvider.getFagsakRelasjonRepository();
        this.behandlingRevurderingRepository = repositoryProvider.getBehandlingRevurderingRepository();
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        registerKontekst.intialiser();
        System.setProperty("dato.for.nye.beregningsregler", "2010-01-01");
    }

    @After
    public void teardown() {
        registerKontekst.nullstill();
        System.setProperty("dato.for.nye.beregningsregler", "2019-01-01");
    }

    @Test
    public void skal_opprette_køet_behandling() {
        // Pre-Arrange: Registerdata
        LocalDate fødselsdatoBarn = LocalDate.now();
        TpsTestSamling tpsData = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn);
        TpsPerson mor = tpsData.getBruker();
        TpsPerson far = tpsData.getMedforelder().get();

        // Arrange
        // Mor med åpen behandling (som da er den åpne behandlingen på sakskomplekset)
        boolean settSomAvsluttet = false;
        Behandling behandling = opprettFørstegangsbehandlingForMor(settSomAvsluttet, mor);

        // Far uten noen behandling
        Fagsak fagsakFar = byggFagsak(far.getAktørId(), RelasjonsRolleType.FARA, NavBrukerKjønn.MANN, new Saksnummer("1234"));
        fagsakRelasjonRepository.opprettRelasjon(fagsakFar, Dekningsgrad._100);
        fagsakRelasjonRepository.kobleFagsaker(fagsakFar, behandling.getFagsak());

        // Bygg søknad far
        Soeknad søknad = FørstegangssøknadTestSett.farFødselStandardUttak(far.getAktørId(), fødselsdatoBarn, mor.getAktørId()).build();

        // Act
        sendSøknad(fagsakFar, søknad, far.getAktørId());

        // Assert - Se at køet behandling er opprettet
        Optional<Behandling> køetBehandlingFar = behandlingRevurderingRepository.finnKøetYtelsesbehandling(fagsakFar.getId());
        assertThat(køetBehandlingFar).isPresent();
        assertThat(køetBehandlingFar.get().erKøet()).isTrue();
        assertThat(køetBehandlingFar.get().getStatus()).isEqualTo(BehandlingStatus.UTREDES);
    }

    @Test
    public void skal_opprette_berørt_behandling_på_medforelder() {
        // Pre-Arrange: Registerdata
        LocalDate fødselsdatoBarn = LocalDate.now();
        TpsTestSamling tpsData = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn);
        TpsPerson mor = tpsData.getBruker();
        TpsPerson far = tpsData.getMedforelder().get();

        // Arrange
        // Mor med avsluttet behandling som er iverksatt
        boolean settSomAvsluttet = true;
        Behandling avsluttetBehandlingMor = opprettFørstegangsbehandlingForMor(settSomAvsluttet, mor);

        // Far med avsluttet behandling som skal iverksettes
        Behandling iverksettendeBehandling = ScenarioFarSøkerForeldrepenger.forFødsel()
            .medBruker(far.getAktørId(), NavBrukerKjønn.MANN)
            .lagre(repositoryProvider);
        // Behandlingen har "andre/flere årsaker enn kun berørt behandling" (kriteriet for å opprette berørt behandling)
        BehandlingÅrsak
            .builder(Arrays.asList(BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING, BehandlingÅrsakType.BERØRT_BEHANDLING))
            .buildFor(iverksettendeBehandling);
        avsluttBehandlingOgFagsak(iverksettendeBehandling, FagsakStatus.LØPENDE);

        fagsakRelasjonRepository.opprettRelasjon(iverksettendeBehandling.getFagsak(), Dekningsgrad._100);
        fagsakRelasjonRepository.kobleFagsaker(iverksettendeBehandling.getFagsak(), avsluttetBehandlingMor.getFagsak());

        // Act
        berørtBehandlingKontroller.vurderNesteOppgaveIBehandlingskø(iverksettendeBehandling.getId());

        // Assert - skal ha opprettet berørt behandling for mor
        Optional<Behandling> berørtBehandling = behandlingRevurderingRepository.finnÅpenYtelsesbehandling(avsluttetBehandlingMor.getFagsakId());
        assertThat(berørtBehandling).isPresent();
        assertThat(berørtBehandling.get().getBehandlingÅrsaker().stream()
            .anyMatch(årsak -> BehandlingÅrsakType.BERØRT_BEHANDLING.equals(årsak.getBehandlingÅrsakType()))).isTrue();
        // Verifiser referanse tilbake til behandlingen som berørt behandling stammer fra
        assertThat(berørtBehandling.get().getBerørtBehandling().get()).isEqualTo(iverksettendeBehandling);
    }

    @Test
    public void skal_dekøe_bruker() {
        // Pre-Arrange: Registerdata
        LocalDate fødselsdatoBarn = LocalDate.now();
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();

        // Arrange
        // Mors 1. behandling er avsluttet og iverksatt
        boolean settSomAvsluttet = true;
        Behandling avsluttetBehandlingMor = opprettFørstegangsbehandlingForMor(settSomAvsluttet, mor);

        // Mors 2. behandling er køet
        ScenarioMorSøkerForeldrepenger køetBehandlingScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medOriginalBehandling(avsluttetBehandlingMor, BehandlingÅrsakType.KØET_BEHANDLING);
        køetBehandlingScenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING, BehandlingStegType.VURDER_KOMPLETTHET);
        // Triks for å stoppe behandling tidligst mulig
        køetBehandlingScenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.REGISTRER_PAPIRSØKNAD_FORELDREPENGER, BehandlingStegType.REGISTRER_SØKNAD);
        Behandling køetBehandlingMor = køetBehandlingScenario.lagre(repositoryProvider);

        // Mors 3. behandling er avsluttet og skal iverksettes.
        Behandling iverksettendeBehandlingMor = ScenarioFarSøkerForeldrepenger.forFødsel()
            .medOriginalBehandling(avsluttetBehandlingMor, BehandlingÅrsakType.BERØRT_BEHANDLING)
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE)
            .lagre(repositoryProvider);
        avsluttBehandlingOgFagsak(iverksettendeBehandlingMor, FagsakStatus.LØPENDE);

        // Act
        berørtBehandlingKontroller.vurderNesteOppgaveIBehandlingskø(iverksettendeBehandlingMor.getId());
        utførProsessSteg();

        // Assert - Behandling av køet er startet for mor
        køetBehandlingMor = behandlingRepository.hentBehandling(køetBehandlingMor.getId());
        assertThat(køetBehandlingMor.erKøet()).isFalse();
        assertThat(køetBehandlingMor.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        assertThat(køetBehandlingMor.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.REGISTRER_SØKNAD);
    }

    @Test
    public void skal_dekøe_medforelder() {
        // Pre-Arrange: Registerdata
        LocalDate fødselsdatoBarn = LocalDate.now();
        TpsTestSamling tpsData = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn);
        TpsPerson mor = tpsData.getBruker();
        TpsPerson far = tpsData.getMedforelder().get();

        // Arrange
        // Mors 1. behandling er avsluttet og iverksatt
        boolean settSomAvsluttet = true;
        Behandling avsluttetBehandlingMor = opprettFørstegangsbehandlingForMor(settSomAvsluttet, mor);

        // Fars 1. behandling er avsluttet og iverksatt
        Behandling avsluttetBehandlingFar = ScenarioFarSøkerForeldrepenger.forFødsel()
            .medBruker(far.getAktørId(), NavBrukerKjønn.MANN)
            .lagre(repositoryProvider);
        avsluttBehandlingOgFagsak(avsluttetBehandlingFar, FagsakStatus.LØPENDE);

        // Mors 2. behandling er køet
        ScenarioMorSøkerForeldrepenger køetBehandlingScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medOriginalBehandling(avsluttetBehandlingMor, BehandlingÅrsakType.KØET_BEHANDLING);
        køetBehandlingScenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING, BehandlingStegType.VURDER_KOMPLETTHET);
        // Triks for å stoppe behandling tidligst mulig
        køetBehandlingScenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.REGISTRER_PAPIRSØKNAD_FORELDREPENGER, BehandlingStegType.REGISTRER_SØKNAD);
        Behandling køetBehandlingMor = køetBehandlingScenario.lagre(repositoryProvider);

        // Fars 2. behandling er avsluttet og skal iverksettes. Er av type KUN berørt, og skal derfor ikke opprette
        // ny behandling på medforelder
        Behandling iverksettendeBehandlingFar = ScenarioFarSøkerForeldrepenger.forFødsel()
            .medOriginalBehandling(avsluttetBehandlingFar, BehandlingÅrsakType.BERØRT_BEHANDLING)
            .medBruker(far.getAktørId(), NavBrukerKjønn.MANN)
            .lagre(repositoryProvider);
        avsluttBehandlingOgFagsak(iverksettendeBehandlingFar, FagsakStatus.LØPENDE);

        fagsakRelasjonRepository.opprettRelasjon(avsluttetBehandlingFar.getFagsak(), Dekningsgrad._100);
        fagsakRelasjonRepository.kobleFagsaker(avsluttetBehandlingFar.getFagsak(), avsluttetBehandlingMor.getFagsak());

        // Act
        berørtBehandlingKontroller.vurderNesteOppgaveIBehandlingskø(iverksettendeBehandlingFar.getId());
        utførProsessSteg();

        // Assert - Behandling av køet er startet for mor
        køetBehandlingMor = behandlingRepository.hentBehandling(køetBehandlingMor.getId());
        assertThat(køetBehandlingMor.erKøet()).isFalse();
        assertThat(køetBehandlingMor.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        assertThat(køetBehandlingMor.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.REGISTRER_SØKNAD);
    }

    private void sendSøknad(Fagsak fagsak, Soeknad søknad, AktørId aktørId) {
        // TODO (essv): Prøv å erstatte denne med FordelRestTjenesteTestAPI
        // Hjelper-klassen sender inn søknad til mottak
        Long behandlingId = hjelper.byggBehandling(fagsak, søknad);
        behandlingskontrollTaskTjeneste.opprettStartBehandlingTask(fagsak.getId(), behandlingId, aktørId);

        utførProsessSteg();
    }

    private Behandling opprettFørstegangsbehandlingForMor(boolean settSomAvsluttet, TpsPerson mor) {
        LocalDate termindato = LocalDate.now();
        LocalDate uttaksdato = termindato; // Fødsel sammenfaller med uttaksdato - skjæringstidspunkt endres ikke

        ScenarioMorSøkerForeldrepenger førstegangsscenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE);
        leggTilSøkersPersonopplysning(førstegangsscenario, mor.getPersonIdent(), mor.getAktørId());
        leggTilOpprettetOgBekreftetTermin(førstegangsscenario, termindato);
        opprettBeregningsgrunnlag(førstegangsscenario);

        førstegangsscenario.medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT);
        førstegangsscenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now())
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav Navesen")
            .build();
        førstegangsscenario.medSøknad().medMottattDato(LocalDate.now());
        Behandling behandling = førstegangsscenario.lagre(repositoryProvider);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(behandling.getFagsak(), Dekningsgrad._100);

        // originalbehandling
        opprettYtelseFordelingMedFlerePerioder(uttaksdato, behandling);
        leggTilOpptjening(behandling, termindato);
        byggUttaksperiodegrense(uttaksdato, behandling);
        opprettUttakResultat(behandling, uttaksdato);

        if (settSomAvsluttet) {
            avsluttBehandlingOgFagsak(behandling, FagsakStatus.LØPENDE);
        }
        return behandling;
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

    private void avsluttBehandlingOgFagsak(Behandling behandling, FagsakStatus fagsakStatus) {
        behandling.avsluttBehandling();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), fagsakStatus);
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

    private void leggTilOpptjening(Behandling behandling, LocalDate fødselsdato) {
        LocalDate skjæringsdato = fødselsdato.minusWeeks(3);
        opptjeningRepository.lagreOpptjeningsperiode(behandling, skjæringsdato.minusYears(1), skjæringsdato);

        List<OpptjeningAktivitet> aktiviteter = new ArrayList<>();
        OpptjeningAktivitet opptjeningAktivitet = new OpptjeningAktivitet(skjæringsdato.minusMonths(10),
            skjæringsdato,
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            "123456789",
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

    private void opprettBeregningsgrunnlag(ScenarioMorSøkerForeldrepenger scenario) {
        Beregningsgrunnlag beregningsgrunnlag = scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medOpprinneligSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medDekningsgrad(100L)
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(LocalDate.now().minusDays(20), LocalDate.now().minusDays(15))
            .build(beregningsgrunnlag);

        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BEREGNET_PR_ÅR)
            .build(beregningsgrunnlagPeriode);

        beregningsgrunnlag.leggTilBeregningsgrunnlagAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(beregningsgrunnlag));
    }

    private void leggTilSøkersPersonopplysning(ScenarioMorSøkerForeldrepenger scenario, PersonIdent fnr, AktørId aktørId) {
        // Henter søkers kjerneinfo fra TpsMock og legger på behandling, blir da identisk for søker ved re-innhenting.
        // I tillegg vil re-innhenting inneholde barn, som vil være diff ift her
        Personinfo søkerPersonInfo = tpsAdapter.hentKjerneinformasjon(fnr, aktørId);
        leggTilSøker(scenario, søkerPersonInfo.getLandkode());
    }

    private void leggTilSøker(AbstractTestScenario<?> scenario, Landkoder statsborgerskap) {
        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();
        Personas persona = builderForRegisteropplysninger
            .medPersonas()
            .kvinne(søkerAktørId, SivilstandType.UOPPGITT, Region.UDEFINERT)
            .personstatus(PersonstatusType.UDEFINERT)
            .statsborgerskap(statsborgerskap);
        PersonInformasjon søker = persona.build();
        scenario.medRegisterOpplysninger(søker);
    }


    private Fagsak byggFagsak(AktørId aktørId, RelasjonsRolleType rolle, NavBrukerKjønn kjønn, Saksnummer saksnummer) {
        NavBruker navBruker = new NavBrukerBuilder()
            .medAktørId(aktørId)
            .medKjønn(kjønn)
            .build();
        fagsak = FagsakBuilder.nyForeldrepengesak(rolle)
            .medSaksnummer(saksnummer)
            .medBruker(navBruker).build();
        fagsakRepository.opprettNy(fagsak);
        return fagsak;
    }

    private void utførProsessSteg() {
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        repository.flush();
    }

}
