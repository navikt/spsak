package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.OpplysningsPeriodeTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgrad;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgradEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InnhentingSamletTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.impl.BasisPersonopplysningTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.felles.testutilities.db.Repository;

@SuppressWarnings("deprecation")
public class InntektArbeidYtelseRegisterInnhentingTjenesteImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    private final Repository repository = repoRule.getRepository();

    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    @Mock
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    @Mock
    private InnhentingSamletTjeneste innhentingSamletTjeneste;
    private IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste;


    @Before
    public void before() {
        initMocks(this);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(any())).thenReturn(LocalDate.now());
        behandlingRepositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
        when(innhentingSamletTjeneste.getSammenstiltSakOgGrunnlag(any(), any(), any(), anyBoolean())).thenReturn(Collections.emptyList());
        when(innhentingSamletTjeneste.hentYtelserTjenester(any(), any(), any())).thenReturn(Collections.emptyList());
        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(behandlingRepositoryProvider, null, null, null, skjæringstidspunktTjeneste,
            new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste));
        iayRegisterInnhentingTjeneste = new IAYRegisterInnhentingFPTjenesteImpl(inntektArbeidYtelseTjeneste,
            behandlingRepositoryProvider,
            null,
            skjæringstidspunktTjeneste,
            innhentingSamletTjeneste,
            new BasisPersonopplysningTjenesteImpl(behandlingRepositoryProvider, skjæringstidspunktTjeneste),
            new OpplysningsPeriodeTjenesteImpl(skjæringstidspunktTjeneste, Period.of(0, 17, 0), Period.of(4, 0, 0), Period.of(1, 0, 0), Period.of(0, 6, 0)));
    }

    private UttakResultatEntitet opprettUttak(boolean innvilget, Behandling behandling, LocalDate fom, LocalDate tom, Virksomhet virksomhet) {
        UttakResultatEntitet.Builder uttakResultatPlanBuilder = UttakResultatEntitet.builder(behandling);

        UttakResultatPeriodeEntitet uttakResultatPeriode = new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(innvilget ? PeriodeResultatType.INNVILGET : PeriodeResultatType.AVSLÅTT, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold((VirksomhetEntitet) virksomhet, ArbeidsforholdRef.ref("123"))
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

        return uttakResultatPlanBuilder.medOpprinneligPerioder(uttakResultatPerioder)
            .build();
    }

    @Test
    public void en_for_gammel_ytelse() {
        YtelseHjelperTester ytelseHjelper = new YtelseHjelperTester();
        ytelseHjelper.medAktørId(new AktørId("1")).medArbeidsForhold("55L").medSaksnummer(new Saksnummer("3"))
            .medUttakFom(LocalDate.now().minusDays(6).minusYears(2)).medUttakTom(LocalDate.now().minusMonths(17).minusDays(1))
            .medKilde("FPSAK").medFagsakType("FORELDREPENGER");

        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(any(Behandling.class))).thenReturn(LocalDate.now());

        Behandling behandling = opprettForeldrePengerSakMedVedtakOgUttakOgBeregning(ytelseHjelper);
        behandling.avsluttBehandling();

        ScenarioMorSøkerForeldrepenger scenarioMorSøkerForeldrepenger1 = ScenarioMorSøkerForeldrepenger.forFødselMedGittBruker(repositoryProvider.getFagsakRepository()
            .hentForBrukerAktørId(ytelseHjelper.aktørId).get(0).getNavBruker());
        scenarioMorSøkerForeldrepenger1.removeDodgyDefaultInntektArbeidYTelse();
        Behandling nyBehandling = scenarioMorSøkerForeldrepenger1.lagre(repositoryProvider);

        Interval periode = iayRegisterInnhentingTjeneste.beregnOpplysningsPeriode(nyBehandling);
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = iayRegisterInnhentingTjeneste.innhentYtelserForInvolverteParter(nyBehandling, periode);
        assertThat(inntektArbeidYtelseAggregatBuilder.build().getAktørYtelse().iterator().next().getYtelser()).hasSize(0);
    }

    @Test
    public void en_akkurat_innenfor_på_tid_ytelse() {
        YtelseHjelperTester ytelseHjelper = new YtelseHjelperTester();
        ytelseHjelper.medAktørId(new AktørId("1")).medArbeidsForhold("55L").medSaksnummer(new Saksnummer("3"))
            .medUttakFom(LocalDate.now().minusDays(6).minusYears(2)).medUttakTom(LocalDate.now().plusDays(1).minusMonths(10))
            .medKilde("FPSAK").medFagsakType("FORELDREPENGER");

        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(any(Behandling.class))).thenReturn(LocalDate.now());

        Behandling behandling = opprettForeldrePengerSakMedVedtakOgUttakOgBeregning(ytelseHjelper);
        behandling.avsluttBehandling();

        ScenarioMorSøkerForeldrepenger scenarioMorSøkerForeldrepenger1 = ScenarioMorSøkerForeldrepenger.forFødselMedGittBruker(repositoryProvider.getFagsakRepository()
            .hentForBrukerAktørId(ytelseHjelper.aktørId).get(0).getNavBruker());
        scenarioMorSøkerForeldrepenger1.removeDodgyDefaultInntektArbeidYTelse();
        Behandling nyBehandling = scenarioMorSøkerForeldrepenger1.lagre(repositoryProvider);

        Interval periode = iayRegisterInnhentingTjeneste.beregnOpplysningsPeriode(nyBehandling);
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = iayRegisterInnhentingTjeneste.innhentYtelserForInvolverteParter(nyBehandling, periode);
        assertThat(inntektArbeidYtelseAggregatBuilder.build().getAktørYtelse().iterator().next().getYtelser()).hasSize(1);
    }

    @Test
    public void hent_flere_invilget_foreldrepenger_med_ytelse() {
        List<YtelseHjelperTester> ytelseHjelperTesterList = new ArrayList<>();
        YtelseHjelperTester ytelseHjelper = new YtelseHjelperTester();
        ytelseHjelper.medAktørId(new AktørId("1")).medArbeidsForhold("55L").medSaksnummer(new Saksnummer("3"))
            .medUttakFom(LocalDate.now().minusDays(6)).medUttakTom(LocalDate.now().minusDays(3))
            .medKilde("FPSAK").medFagsakType("FORELDREPENGER");
        ytelseHjelperTesterList.add(ytelseHjelper);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(any(Behandling.class))).thenReturn(LocalDate.now());

        Behandling behandling1 = opprettForeldrePengerSakMedVedtakOgUttakOgBeregning(ytelseHjelper);
        behandling1.avsluttBehandling();

        YtelseHjelperTester andreYtelseHjelper = new YtelseHjelperTester();
        andreYtelseHjelper.medAktørId(new AktørId("1")).medArbeidsForhold("65L").medSaksnummer(new Saksnummer("4"))
            .medUttakFom(LocalDate.now().minusDays(6)).medUttakTom(LocalDate.now().minusDays(3))
            .medKilde("FPSAK").medFagsakType("FORELDREPENGER");
        ytelseHjelperTesterList.add(andreYtelseHjelper);

        Behandling behandling2 = opprettForeldrePengerSakMedVedtakOgUttakOgBeregning(andreYtelseHjelper);
        behandling2.avsluttBehandling();

        ScenarioMorSøkerForeldrepenger scenarioMorSøkerForeldrepenger1 = ScenarioMorSøkerForeldrepenger.forFødselMedGittBruker(repositoryProvider.getFagsakRepository().hentForBrukerAktørId(ytelseHjelper.aktørId).get(0).getNavBruker());
        Behandling nyBehandling = scenarioMorSøkerForeldrepenger1.lagre(repositoryProvider);

        utførKallForÅHenteYtelse(ytelseHjelperTesterList, nyBehandling, 2);
    }

    @Test
    public void hent_invilget_foreldrepenger_med_en_ytelse() {
        YtelseHjelperTester ytelseHjelper = new YtelseHjelperTester();
        ytelseHjelper.medAktørId(new AktørId("1")).medArbeidsForhold("55L").medSaksnummer(new Saksnummer("3"))
            .medUttakFom(LocalDate.now().minusDays(6)).medUttakTom(LocalDate.now().minusDays(3))
            .medKilde("FPSAK").medFagsakType("FORELDREPENGER");

        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(any(Behandling.class))).thenReturn(LocalDate.now());

        Behandling behandling = opprettForeldrePengerSakMedVedtakOgUttakOgBeregning(ytelseHjelper);
        behandling.avsluttBehandling();

        ScenarioMorSøkerForeldrepenger scenarioMorSøkerForeldrepenger1 = ScenarioMorSøkerForeldrepenger.forFødselMedGittBruker(repositoryProvider.getFagsakRepository()
            .hentForBrukerAktørId(ytelseHjelper.aktørId).get(0).getNavBruker());
        scenarioMorSøkerForeldrepenger1.removeDodgyDefaultInntektArbeidYTelse();
        Behandling nyBehandling = scenarioMorSøkerForeldrepenger1.lagre(repositoryProvider);

        Interval periode = iayRegisterInnhentingTjeneste.beregnOpplysningsPeriode(nyBehandling);
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = iayRegisterInnhentingTjeneste.innhentYtelserForInvolverteParter(nyBehandling, periode);
        InntektArbeidYtelseAggregat build = inntektArbeidYtelseAggregatBuilder.build();
        Collection<AktørYtelse> aktørYtelse = build.getAktørYtelse();
        AktørYtelse ay = aktørYtelse.iterator().next();
        Ytelse ytelse = ay.getYtelser().iterator().next();
        sjekkVerdier(ytelse, ytelseHjelper);
    }

    private Behandling opprettForeldrePengerSakMedVedtakOgUttakOgBeregning(YtelseHjelperTester ytelseHjelper) {
        ScenarioMorSøkerForeldrepenger scenarioMorSøkerForeldrepenger = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(ytelseHjelper.aktørId).medSaksnummer(ytelseHjelper.saksnummer);
        scenarioMorSøkerForeldrepenger.removeDodgyDefaultInntektArbeidYTelse();
        Behandling behandling = scenarioMorSøkerForeldrepenger.lagre(repositoryProvider);

        opprettVedtakForBehandling(behandling);


        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(ytelseHjelper);
        repositoryProvider.getBeregningsgrunnlagRepository().lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);

        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, dekningsgrad);

        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(behandling.getFagsak(), Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        Virksomhet virksomhet = opprettOgLagreVirksomhet(ytelseHjelper);

        UttakResultatEntitet uttakResultatEntitet = opprettUttak(true, behandling, ytelseHjelper.uttakFom, ytelseHjelper.uttakTom, virksomhet);
        repositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(behandling, uttakResultatEntitet.getGjeldendePerioder());
        return behandling;
    }

    private void opprettVedtakForBehandling(Behandling gammelBehandling) {
        BehandlingVedtak vedtak = BehandlingVedtak.builder()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medVedtaksdato(LocalDate.now().minusWeeks(10))
            .medBehandlingsresultat(gammelBehandling.getBehandlingsresultat())
            .medAnsvarligSaksbehandler("Severin Saksbehandler")
            .build();
        Whitebox.setInternalState(gammelBehandling.getBehandlingsresultat(), "behandlingVedtak", vedtak);
        repository.lagre(gammelBehandling.getBehandlingsresultat());
    }

    private Virksomhet opprettOgLagreVirksomhet(YtelseHjelperTester ytelseHjelper) {
        VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();

        final Optional<Virksomhet> hent = virksomhetRepository.hent(ytelseHjelper.arbeidsForholdId);
        if (hent.isPresent()) {
            return hent.get();
        }

        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(ytelseHjelper.arbeidsForholdId)
            .medNavn("Virksomheten")
            .medRegistrert(LocalDate.now().minusYears(2L))
            .medOppstart(LocalDate.now().minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();

        virksomhetRepository.lagre(virksomhet);
        return virksomhet;
    }

    private void sjekkVerdier(Ytelse ytelse, YtelseHjelperTester ytelseHjelper) {
        assertThat(ytelse.getSaksnummer()).isEqualTo(ytelseHjelper.saksnummer);
        assertThat(ytelse.getKilde().getKode()).isEqualTo(ytelseHjelper.kilde);
        assertThat(ytelse.getPeriode().getFomDato()).isEqualTo(ytelseHjelper.uttakFom);
        assertThat(ytelse.getPeriode().getTomDato()).isEqualTo(ytelseHjelper.uttakTom);
        assertThat(ytelse.getRelatertYtelseType().getKode()).isEqualTo(ytelseHjelper.fagSakType);
    }

    @Test
    public void hent_invilget_engansstønader_med_en_ytelse() {
        YtelseHjelperTester ytelseHjelper = new YtelseHjelperTester();
        ytelseHjelper.medAktørId(new AktørId("1")).medArbeidsForhold("55L").medSaksnummer(new Saksnummer("3"))
            .medKilde("FPSAK").medFagsakType("ENGANGSSTØNAD");

        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(any(Behandling.class))).thenReturn(LocalDate.now());
        Behandling gammelBehandling = opprettEngansstønadMedVedtak(ytelseHjelper);
        gammelBehandling.avsluttBehandling();

        ytelseHjelper.medUttakFom(gammelBehandling.getBehandlingsresultat().getBehandlingVedtak().getVedtaksdato()) //andre verdier enn for foreldrepenger
            .medUttakTom(gammelBehandling.getBehandlingsresultat().getBehandlingVedtak().getVedtaksdato());

        ScenarioMorSøkerForeldrepenger scenarioMorSøkerForeldrepenger1 = ScenarioMorSøkerForeldrepenger.forFødselMedGittBruker(repositoryProvider.getFagsakRepository().hentForBrukerAktørId(ytelseHjelper.aktørId).get(0).getNavBruker());
        Behandling nyBehandling = scenarioMorSøkerForeldrepenger1.lagre(repositoryProvider);

        Interval periode = iayRegisterInnhentingTjeneste.beregnOpplysningsPeriode(nyBehandling);
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = iayRegisterInnhentingTjeneste.innhentYtelserForInvolverteParter(nyBehandling, periode);
        InntektArbeidYtelseAggregat build = inntektArbeidYtelseAggregatBuilder.build();
        Collection<AktørYtelse> aktørYtelse = build.getAktørYtelse();

        sjekkVerdier(aktørYtelse.iterator().next().getYtelser().iterator().next(), ytelseHjelper);
    }

    private Behandling opprettEngansstønadMedVedtak(YtelseHjelperTester ytelseHjelper) {
        ScenarioMorSøkerEngangsstønad scenarioMorSøkerEngangsstønad = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenarioMorSøkerEngangsstønad.medBruker(ytelseHjelper.aktørId, NavBrukerKjønn.KVINNE).medSaksnummer(new Saksnummer("3"));

        Behandling behandling = scenarioMorSøkerEngangsstønad.lagre(repositoryProvider);

        opprettVedtakForBehandling(behandling);
        return behandling;
    }

    @Ignore // FIXME (diamant): Denne testen har ikke testet annet enn at egen fagsak dukker opp
    @Test
    public void hent_invilget_foreldrePenger_for_annen_part() {

        YtelseHjelperTester ytelseHjelper = new YtelseHjelperTester();
        ytelseHjelper.medAktørId(new AktørId("1")).medAnnenPartAktørId(new AktørId("2")).medArbeidsForhold("55L").medSaksnummer(new Saksnummer("3"))
            .medUttakFom(LocalDate.now().minusDays(6)).medUttakTom(LocalDate.now().minusDays(3))
            .medKilde("FPSAK").medFagsakType("FORELDREPENGER");

        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(any(Behandling.class))).thenReturn(LocalDate.now());

        Behandling behandling = opprettForeldrePengerSakMedVedtakOgUttakOgBeregning(ytelseHjelper);
        behandling.avsluttBehandling();

        ScenarioMorSøkerForeldrepenger scenarioMorSøkerForeldrepenger1 = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(ytelseHjelper.annenPartAktørId);
        scenarioMorSøkerForeldrepenger1.medSøknadAnnenPart().medAktørId(ytelseHjelper.aktørId);
        scenarioMorSøkerForeldrepenger1.removeDodgyDefaultInntektArbeidYTelse();
        Behandling behandling2 = scenarioMorSøkerForeldrepenger1.lagre(repositoryProvider);

        Interval periode = iayRegisterInnhentingTjeneste.beregnOpplysningsPeriode(behandling2);
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = iayRegisterInnhentingTjeneste.innhentYtelserForInvolverteParter(behandling2, periode);
        InntektArbeidYtelseAggregat build = inntektArbeidYtelseAggregatBuilder.build();
        Collection<AktørYtelse> aktørYtelse = build.getAktørYtelse();

        sjekkVerdier(aktørYtelse.iterator().next().getYtelser().iterator().next(), ytelseHjelper);
    }

    private Beregningsgrunnlag buildBeregningsgrunnlag(YtelseHjelperTester ytelseHjelper) {
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(BigDecimal.valueOf(91425L))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(91425L))
            .build();
        BeregningsgrunnlagPeriode bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        buildBgPrStatusOgAndel(bgPeriode, ytelseHjelper);
        return beregningsgrunnlag;
    }

    private void utførKallForÅHenteYtelse(List<YtelseHjelperTester> ytelseHjelperTesterList, Behandling nyBehandling, int antallTreff) {
        Interval periode = iayRegisterInnhentingTjeneste.beregnOpplysningsPeriode(nyBehandling);
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = iayRegisterInnhentingTjeneste.innhentYtelserForInvolverteParter(nyBehandling, periode);
        InntektArbeidYtelseAggregat build = inntektArbeidYtelseAggregatBuilder.build();
        Collection<Ytelse> ytelser = build.getAktørYtelse().iterator().next().getYtelser();

        assertThat(ytelser.size()).isEqualTo(antallTreff);

        ytelseHjelperTesterList.forEach(ytelseHjelperTester -> sjekkVerdierForListe(ytelser, ytelseHjelperTester));
    }

    private void sjekkVerdierForListe(Collection<Ytelse> ytelser, YtelseHjelperTester ytelseHjelperTester) {
        Optional<Ytelse> ytelse = ytelser.stream().filter(y -> String.valueOf(ytelseHjelperTester.saksnummer.getVerdi()).equals(y.getSaksnummer().getVerdi())).findAny();
        assertThat(ytelse.isPresent()).isTrue();
        sjekkVerdier(ytelse.get(), ytelseHjelperTester);
    }


    private BeregningsgrunnlagPrStatusOgAndel buildBgPrStatusOgAndel(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, YtelseHjelperTester ytelseHjelper) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(fraOrgnr(ytelseHjelper.arbeidsForholdId)))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        return BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(LocalDate.now().minusDays(10), LocalDate.now().minusDays(5))
            .medOverstyrtPrÅr(BigDecimal.valueOf(4444432.32))
            .build(beregningsgrunnlagPeriode);
    }

    private VirksomhetEntitet fraOrgnr(String orgnr) {
        return (VirksomhetEntitet) repositoryProvider.getVirksomhetRepository().hent(orgnr).orElse(null);
    }

    private BeregningsgrunnlagPeriode buildBeregningsgrunnlagPeriode(Beregningsgrunnlag beregningsgrunnlag) {
        return BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(LocalDate.now().minusDays(20), LocalDate.now().minusDays(15))
            .medBruttoPrÅr(BigDecimal.valueOf(534343.55))
            .build(beregningsgrunnlag);
    }

    class YtelseHjelperTester {
        private AktørId aktørId;
        private String fagSakType;
        private LocalDate uttakFom;
        private LocalDate uttakTom;
        private String arbeidsForholdId;
        private String kilde;
        private Saksnummer saksnummer;
        private AktørId annenPartAktørId;

        YtelseHjelperTester medAktørId(AktørId aktørId) {
            this.aktørId = aktørId;
            return this;
        }

        YtelseHjelperTester medFagsakType(String fagSakType) {
            this.fagSakType = fagSakType;
            return this;
        }

        YtelseHjelperTester medUttakFom(LocalDate fom) {
            this.uttakFom = fom;
            return this;
        }

        YtelseHjelperTester medUttakTom(LocalDate tom) {
            this.uttakTom = tom;
            return this;
        }

        YtelseHjelperTester medKilde(String kilde) {
            this.kilde = kilde;
            return this;
        }

        YtelseHjelperTester medArbeidsForhold(String arbeidsForholdId) {
            this.arbeidsForholdId = arbeidsForholdId;
            return this;
        }

        YtelseHjelperTester medSaksnummer(Saksnummer saksnummer) {
            this.saksnummer = saksnummer;
            return this;
        }

        YtelseHjelperTester medAnnenPartAktørId(AktørId annenPartAktørId) {
            this.annenPartAktørId = annenPartAktørId;
            return this;
        }
    }
}

