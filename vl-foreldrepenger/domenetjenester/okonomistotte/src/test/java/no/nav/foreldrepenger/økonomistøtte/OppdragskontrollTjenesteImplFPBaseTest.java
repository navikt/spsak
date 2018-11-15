package no.nav.foreldrepenger.økonomistøtte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.Rule;

import no.nav.foreldrepenger.behandling.revurdering.EndringsdatoRevurderingUtleder;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.RettenTil;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFeriepenger;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFeriepengerPrÅr;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Refusjonsinfo156;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndring;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndringLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKlassifik;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeStatusLinje;
import no.nav.foreldrepenger.økonomistøtte.fp.OppdragskontrollEndringFP;
import no.nav.foreldrepenger.økonomistøtte.fp.OppdragskontrollFørstegangFP;
import no.nav.foreldrepenger.økonomistøtte.fp.OppdragskontrollOpphørFP;
import no.nav.vedtak.felles.testutilities.db.Repository;

abstract public class OppdragskontrollTjenesteImplFPBaseTest {

    private static final Long PROSESS_TASK_ID_1 = 123L;

    protected static final String TYPE_SATS_FP_FERIEPG = "ENG";
    protected static final String TYPE_SATS_FP_YTELSE = "DAG";
    protected static final String ARBEIDSFORHOLD_ID = "987987456";
    protected static final String ARBEIDSFORHOLD_ID_2 = "123456789";
    protected static final String ARBEIDSFORHOLD_ID_3 = "789123456";
    protected static final String ARBEIDSFORHOLD_ID_4 = "654321987";

    protected final List<Integer> feriepengeårListe = Arrays.asList(LocalDate.now().plusYears(1).getYear(), LocalDate.now().plusYears(2).getYear());
    protected final OppdragskontrollTestVerktøy oppdragskontrollTestVerktøy = new OppdragskontrollTestVerktøy();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    protected final EntityManager entityManager = repoRule.getEntityManager();
    protected Repository repository = repoRule.getRepository();
    protected ØkonomioppdragRepository økonomioppdragRepository = new ØkonomioppdragRepositoryImpl(entityManager);
    protected BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    protected final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    protected BeregningRepository beregningRepository = new BeregningRepositoryImpl(entityManager);
    protected BeregningsresultatFPRepository beregningsresultatFPRepository = new BeregningsresultatFPRepositoryImpl(entityManager);
    protected UttakRepository uttakRepository = new UttakRepositoryImpl(entityManager);
    protected OppdragskontrollTjeneste oppdragskontrollTjeneste;
    protected EndringsdatoRevurderingUtleder endringsdatoUtleder;
    protected Behandling behandlingFP;
    protected Fagsak fagsakFP;
    protected Personinfo personInfo;
    protected BehandlingVedtak behVedtakFP;
    protected LocalDate endringsdato = LocalDate.now().plusDays(5);
    protected VirksomhetEntitet virksomhet;
    protected VirksomhetEntitet virksomhet2;
    protected VirksomhetEntitet virksomhet3;
    protected VirksomhetEntitet virksomhet4;


    public void setUp() {
        virksomhet = new VirksomhetEntitet.Builder().medOrgnr(ARBEIDSFORHOLD_ID).medNavn("Virksomheten").oppdatertOpplysningerNå().build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);
        virksomhet2 = new VirksomhetEntitet.Builder().medOrgnr(ARBEIDSFORHOLD_ID_2).medNavn("Virksomhet2").oppdatertOpplysningerNå().build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet2);
        virksomhet3 = new VirksomhetEntitet.Builder().medOrgnr(ARBEIDSFORHOLD_ID_3).medNavn("Virksomhet3").oppdatertOpplysningerNå().build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet3);
        virksomhet4 = new VirksomhetEntitet.Builder().medOrgnr(ARBEIDSFORHOLD_ID_4).medNavn("Virksomhet4").oppdatertOpplysningerNå().build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet4);

        PersoninfoAdapter personinfoAdapterMock = mock(PersoninfoAdapter.class);
        TpsTjeneste tpsTjeneste = mock(TpsTjeneste.class);
        endringsdatoUtleder = mock(EndringsdatoRevurderingUtleder.class);

        OppdragskontrollFørstegangFP oppdragskontrollFørstegangFP = new OppdragskontrollFørstegangFP(repositoryProvider, tpsTjeneste, økonomioppdragRepository);
        OppdragskontrollOpphørFP oppdragskontrollOpphørFP = new OppdragskontrollOpphørFP(repositoryProvider, tpsTjeneste, økonomioppdragRepository);
        OppdragskontrollEndringFP oppdragskontrollEndringFP = new OppdragskontrollEndringFP(repositoryProvider, tpsTjeneste, økonomioppdragRepository,
            oppdragskontrollOpphørFP, oppdragskontrollFørstegangFP, endringsdatoUtleder);
        OppdragskontrollManagerFactory oppdragskontrollManagerFactory = new OppdragskontrollManagerFactory(oppdragskontrollFørstegangFP, oppdragskontrollEndringFP, oppdragskontrollOpphørFP, uttakRepository);
        oppdragskontrollTjeneste = new OppdragskontrollTjenesteImpl(repositoryProvider, oppdragskontrollManagerFactory, økonomioppdragRepository);

        behandlingFP = opprettOgLagreBehandlingFP();

        personInfo = OpprettBehandling.opprettPersonInfo();
        when(personinfoAdapterMock.innhentSaksopplysningerForSøker(any(AktørId.class))).thenReturn(personInfo);
        when(tpsTjeneste.hentFnrForAktør(any(AktørId.class))).thenReturn(personInfo.getPersonIdent());
    }

    private Behandling opprettOgLagreBehandlingFP() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();

        behandlingFP = scenario.lagre(repositoryProvider);
        fagsakFP = scenario.getFagsak();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandlingFP);
        Behandlingsresultat.builderForInngangsvilkår()
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.INGEN_ENDRING)
            .medRettenTil(RettenTil.HAR_RETT_TIL_FP)
            .medVedtaksbrev(Vedtaksbrev.INGEN)
            .buildFor(behandlingFP);

        behandlingRepository.lagre(behandlingFP.getBehandlingsresultat().getVilkårResultat(), lås);
        repository.lagre(behandlingFP.getBehandlingsresultat());

        behVedtakFP = OpprettBehandling.opprettBehandlingVedtak(behandlingFP.getBehandlingsresultat());
        repositoryProvider.getBehandlingVedtakRepository().lagre(behVedtakFP, lås);

        repository.flush();

        return behandlingFP;
    }

    protected BeregningsresultatFP buildBeregningsresultatFP() {

        return buildBeregningsresultatFP(false);
    }

    protected BeregningsresultatFP buildBeregningsresultatFP(boolean medFeriepenger) {
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatFP, 1, 7);
        BeregningsresultatAndel andelBruker = buildBeregningsresultatAndel(brPeriode1, true, 1500, BigDecimal.valueOf(80), virksomhet);
        BeregningsresultatAndel andelArbeidsforhold = buildBeregningsresultatAndel(brPeriode1, false, 500, BigDecimal.valueOf(100), virksomhet);

        BeregningsresultatPeriode brPeriode3 = buildBeregningsresultatPeriode(beregningsresultatFP, 16, 22);
        buildBeregningsresultatAndel(brPeriode3, true, 0, BigDecimal.valueOf(80), virksomhet3);
        BeregningsresultatAndel andelArbeidsforhold3 = buildBeregningsresultatAndel(brPeriode3, false, 2160, BigDecimal.valueOf(80), virksomhet3);

        BeregningsresultatPeriode brPeriode4 = buildBeregningsresultatPeriode(beregningsresultatFP, 23, 30);
        buildBeregningsresultatAndel(brPeriode4, true, 2160, BigDecimal.valueOf(80), virksomhet3);
        buildBeregningsresultatAndel(brPeriode4, false, 0, BigDecimal.valueOf(80), virksomhet3);

        BeregningsresultatPeriode brPeriode2 = buildBeregningsresultatPeriode(beregningsresultatFP, 8, 15);
        buildBeregningsresultatAndel(brPeriode2, true, 1600, BigDecimal.valueOf(80), virksomhet2);
        BeregningsresultatAndel andelArbeidsforhold2 = buildBeregningsresultatAndel(brPeriode2, false, 400, BigDecimal.valueOf(100), virksomhet2);

        if (medFeriepenger) {
            BeregningsresultatFeriepenger feriepenger = buildBeregningsresultatFeriepenger(beregningsresultatFP);
            buildBeregningsresultatFeriepengerPrÅr(feriepenger, andelBruker, 20000L, Collections.singletonList(LocalDate.now()));
            buildBeregningsresultatFeriepengerPrÅr(feriepenger, andelArbeidsforhold, 15000L, Collections.singletonList(LocalDate.now()));
            buildBeregningsresultatFeriepengerPrÅr(feriepenger, andelArbeidsforhold2, 20000L, Collections.singletonList(LocalDate.now()));
            buildBeregningsresultatFeriepengerPrÅr(feriepenger, andelArbeidsforhold3, 20000L, Arrays.asList(LocalDate.now(), LocalDate.now().plusYears(1)));
        }

        return beregningsresultatFP;
    }

    protected BeregningsresultatAndel buildBeregningsresultatAndel(BeregningsresultatPeriode beregningsresultatPeriode, Boolean brukerErMottaker, int dagsats, BigDecimal utbetalingsgrad, VirksomhetEntitet virksomheten) {

        return buildBeregningsresultatAndel(beregningsresultatPeriode, brukerErMottaker, dagsats, utbetalingsgrad, virksomheten, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER);
    }

    protected BeregningsresultatAndel buildBeregningsresultatAndel(BeregningsresultatPeriode beregningsresultatPeriode, Boolean brukerErMottaker, int dagsats,
                                                                   BigDecimal utbetalingsgrad, VirksomhetEntitet virksomheten, AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori) {
        return BeregningsresultatAndel.builder()
            .medBrukerErMottaker(brukerErMottaker)
            .medVirksomhet(virksomheten)
            .medDagsats(dagsats)
            .medDagsatsFraBg(dagsats)
            .medStillingsprosent(BigDecimal.valueOf(100))
            .medUtbetalingsgrad(utbetalingsgrad)
            .medAktivitetstatus(aktivitetStatus)
            .medInntektskategori(inntektskategori)
            .build(beregningsresultatPeriode);
    }

    protected BeregningsresultatPeriode buildBeregningsresultatPeriode(BeregningsresultatFP beregningsresultatFP, int fom, int tom) {

        return BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(LocalDate.now().plusDays(fom), LocalDate.now().plusDays(tom))
            .build(beregningsresultatFP);
    }

    protected BeregningsresultatFeriepenger buildBeregningsresultatFeriepenger(BeregningsresultatFP beregningsresultatFP) {
        BeregningsresultatFeriepenger beregningsresultatFeriepenger = BeregningsresultatFeriepenger.builder()
            .medFeriepengerPeriodeFom(LocalDate.now().plusDays(1))
            .medFeriepengerPeriodeTom(LocalDate.now().plusDays(29))
            .medFeriepengerRegelInput("clob1")
            .medFeriepengerRegelSporing("clob2")
            .build(beregningsresultatFP);

        return beregningsresultatFeriepenger;
    }

    protected void buildBeregningsresultatFeriepengerPrÅr(BeregningsresultatFeriepenger beregningsresultatFeriepenger, BeregningsresultatAndel andel,
                                                          Long årsBeløp, List<LocalDate> opptjeningsårList) {
        for (LocalDate opptjeningsår : opptjeningsårList) {
            buildBeregningsresultatFeriepengerPrÅr(beregningsresultatFeriepenger, andel, årsBeløp, opptjeningsår);
        }
    }

    protected void buildBeregningsresultatFeriepengerPrÅr(BeregningsresultatFeriepenger beregningsresultatFeriepenger, BeregningsresultatAndel andel,
                                                          Long årsBeløp, LocalDate opptjeningsår) {
        BeregningsresultatFeriepengerPrÅr.builder()
            .medOpptjeningsår(opptjeningsår)
            .medÅrsbeløp(årsBeløp)
            .build(beregningsresultatFeriepenger, andel);
    }

    protected Behandling opprettOgLagreRevurdering(Behandling originalBehandling, VedtakResultatType resultat, int antallbarn, boolean gjelderOpphør, boolean gjelderEndring) {

        Behandling revurdering = Behandling.fraTidligereBehandling(originalBehandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_MANGLER_FØDSEL).medOriginalBehandling(originalBehandling)).build();

        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, behandlingLås);
        repositoryProvider.getFamilieGrunnlagRepository().kopierGrunnlagFraEksisterendeBehandling(originalBehandling, revurdering);
        OpprettBehandling.genererBehandlingOgResultat(revurdering, resultat, antallbarn);
        behandlingRepository.lagre(revurdering.getBehandlingsresultat().getVilkårResultat(), behandlingLås);
        if (VedtakResultatType.INNVILGET.equals(resultat)) {
            beregningRepository.lagre(revurdering.getBehandlingsresultat().getBeregningResultat(), behandlingLås);
        }
        if (gjelderOpphør) {
            Behandlingsresultat behandlingsresultat = revurdering.getBehandlingsresultat();
            Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.OPPHØR);
        } else if (gjelderEndring) {
            Behandlingsresultat behandlingsresultat = revurdering.getBehandlingsresultat();
            Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.FORELDREPENGER_ENDRET);
        } else {
            Behandlingsresultat behandlingsresultat = revurdering.getBehandlingsresultat();
            Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        }
        repository.lagre(revurdering.getBehandlingsresultat());

        BehandlingVedtak behandlingVedtak = OpprettBehandling.opprettBehandlingVedtak(revurdering.getBehandlingsresultat(), resultat);
        repositoryProvider.getBehandlingVedtakRepository().lagre(behandlingVedtak, behandlingLås);
        repository.flush();

        return revurdering;
    }

    protected BeregningsresultatFP buildBeregningsresultatMedFlereInntektskategoriFP(boolean medFeriepenger) {
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatFP, 1, 7);
        BeregningsresultatAndel andelBruker = buildBeregningsresultatAndel(brPeriode1, true, 1500, BigDecimal.valueOf(80), virksomhet);
        buildBeregningsresultatAndel(brPeriode1, true, 1500, BigDecimal.valueOf(80), virksomhet2, AktivitetStatus.FRILANSER, Inntektskategori.FRILANSER);
        BeregningsresultatAndel andelArbeidsforhold = buildBeregningsresultatAndel(brPeriode1, false, 500, BigDecimal.valueOf(100), virksomhet);

        BeregningsresultatPeriode brPeriode2 = buildBeregningsresultatPeriode(beregningsresultatFP, 8, 15);
        buildBeregningsresultatAndel(brPeriode2, true, 1600, BigDecimal.valueOf(80), virksomhet2);
        BeregningsresultatAndel andelArbeidsforhold2 = buildBeregningsresultatAndel(brPeriode2, false, 400, BigDecimal.valueOf(100), virksomhet2);

        if (medFeriepenger) {
            BeregningsresultatFeriepenger feriepenger = buildBeregningsresultatFeriepenger(beregningsresultatFP);
            buildBeregningsresultatFeriepengerPrÅr(feriepenger, andelBruker, 20000L, Collections.singletonList(LocalDate.now()));
            buildBeregningsresultatFeriepengerPrÅr(feriepenger, andelArbeidsforhold, 15000L, Collections.singletonList(LocalDate.now()));
            buildBeregningsresultatFeriepengerPrÅr(feriepenger, andelArbeidsforhold2, 20000L, Collections.singletonList(LocalDate.now()));
        }

        return beregningsresultatFP;
    }

    protected BeregningsresultatFP buildBeregningsresultatMedFlereAndelerSomArbeidsgiver() {
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatFP, 1, 7);
        buildBeregningsresultatAndel(brPeriode1, true, 1500, BigDecimal.valueOf(80), virksomhet);
        buildBeregningsresultatAndel(brPeriode1, true, 1500, BigDecimal.valueOf(80), virksomhet2, AktivitetStatus.FRILANSER, Inntektskategori.FRILANSER);
        buildBeregningsresultatAndel(brPeriode1, false, 500, BigDecimal.valueOf(100), virksomhet);
        buildBeregningsresultatAndel(brPeriode1, false, 1000, BigDecimal.valueOf(100), virksomhet, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);

        BeregningsresultatPeriode brPeriode2 = buildBeregningsresultatPeriode(beregningsresultatFP, 8, 15);
        buildBeregningsresultatAndel(brPeriode2, true, 1600, BigDecimal.valueOf(80), virksomhet2);
        buildBeregningsresultatAndel(brPeriode2, false, 400, BigDecimal.valueOf(100), virksomhet2);

        return beregningsresultatFP;
    }

    protected void verifiserOppdrag110_ENDR(Long oppdragId, List<Oppdrag110> originaltOpp110Liste, boolean medFeriepenger) {
        Oppdragskontroll oppdragskontroll = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);
        List<Oppdrag110> nyOppdr110Liste = oppdragskontroll.getOppdrag110Liste();
        for (Oppdrag110 oppdr110Revurd : nyOppdr110Liste) {
            Optional<Refusjonsinfo156> refusjonsinfo156 = oppdr110Revurd.getOppdragslinje150Liste().stream()
                .map(oppdr1150 -> oppdr1150.getRefusjonsinfo156())
                .filter(r -> r != null)
                .filter(r -> r.getRefunderesId().equals(oppdragskontrollTestVerktøy.endreTilElleveSiffer(ARBEIDSFORHOLD_ID_4)))
                .findFirst();
            if (refusjonsinfo156.isPresent()) {
                Oppdrag110 opp110 = refusjonsinfo156.get().getOppdragslinje150().getOppdrag110();
                assertThat(opp110.getKodeEndring()).isEqualTo(ØkonomiKodeEndring.NY.name());
            } else {
                assertThat(oppdr110Revurd.getKodeEndring()).isEqualTo(ØkonomiKodeEndring.UEND.name());
            }
            assertThat(oppdr110Revurd.getOppdragslinje150Liste()).isNotEmpty();
            boolean nyMottaker = oppdr110Revurd.getOppdragslinje150Liste().stream()
                .noneMatch(oppdr150 -> oppdr150.gjelderOpphør() &&
                    oppdr150.getKodeStatusLinje().equals(ØkonomiKodeStatusLinje.OPPH.name()));
            if (!nyMottaker) {
                assertThat(originaltOpp110Liste).anySatisfy(oppdrag110 ->
                    assertThat(oppdrag110.getFagsystemId()).isEqualTo(oppdr110Revurd.getFagsystemId()));
            }
        }
        if (medFeriepenger) {
            List<Oppdragslinje150> opp150List = nyOppdr110Liste.stream().flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream()).collect(Collectors.toList());
            assertThat(opp150List).anySatisfy(opp150 ->
                assertThat(opp150.getKodeKlassifik()).isEqualTo(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik()));
            assertThat(opp150List).anySatisfy(opp150 ->
                assertThat(opp150.getKodeKlassifik()).isEqualTo(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik()));
        }
    }

    protected void verifiserOppdr150SomErNy(List<Oppdragslinje150> opp150RevurdListe, List<Oppdragslinje150> originaltOpp150Liste) {
        List<Oppdragslinje150> opp150NyList = opp150RevurdListe.stream()
            .filter(oppdr150 -> oppdr150.getKodeEndringLinje().equals(ØkonomiKodeEndringLinje.NY.name()))
            .collect(Collectors.toList());

        List<Oppdragslinje150> opp150List = new ArrayList<>();
        assertThat(opp150NyList).isNotEmpty();
        for (Oppdragslinje150 opp150Ny : opp150NyList) {
            assertThat(opp150Ny.getKodeStatusLinje()).isNull();
            assertThat(opp150Ny.getDatoStatusFom()).isNull();
            assertThat(originaltOpp150Liste).allMatch(opp150 -> !opp150.getDelytelseId().equals(opp150Ny.getDelytelseId()));
            if (opp150Ny.getRefDelytelseId() != null) {
                assertThat(opp150RevurdListe).anySatisfy(opp150 ->
                    assertThat(opp150.getDelytelseId()).isEqualTo(opp150Ny.getRefDelytelseId()));
                Oppdragslinje150 oppdr150 = opp150RevurdListe.stream()
                    .filter(opp150 -> opp150.getDelytelseId().equals(opp150Ny.getRefDelytelseId()))
                    .findFirst()
                    .orElse(null);
                assertThat(oppdr150).isNotNull();
                assertThat(opp150Ny.getKodeKlassifik()).isEqualTo(oppdr150.getKodeKlassifik());
                opp150List.add(oppdr150);
            }
            if (opp150Ny.getOppdrag110().getKodeFagomrade().equals(ØkonomiKodeFagområde.FPREF.name())) {
                assertThat(opp150Ny.getRefusjonsinfo156()).isNotNull();
            }
            if (oppdragskontrollTestVerktøy.opp150MedGradering(opp150Ny)) {
                assertThat(opp150Ny.getGrad170Liste().get(0).getGrad()).isEqualTo(80);
            }
            if (!oppdragskontrollTestVerktøy.erOpp150ForFeriepenger(opp150Ny)) {
                assertThat(opp150Ny.getGrad170Liste()).isNotEmpty();
                assertThat(opp150Ny.getGrad170Liste()).isNotNull();
            } else {
                assertThat(opp150Ny.getGrad170Liste()).isEmpty();
                assertThat(opp150Ny.getRefFagsystemId()).isNull();
                assertThat(opp150Ny.getRefDelytelseId()).isNull();
            }
        }
        List<String> statusList = opp150List.stream()
            .filter(o150 -> o150.gjelderOpphør())
            .map(Oppdragslinje150::getKodeStatusLinje)
            .collect(Collectors.toList());
        assertThat(statusList).isNotEmpty();
        assertThat(statusList).containsOnly(ØkonomiKodeStatusLinje.OPPH.name());
    }

    protected void verifiserOppdr150SomErOpphørt(List<Oppdragslinje150> opp150RevurdListe, List<Oppdragslinje150> originaltOpp150Liste, boolean medFeriePenger, boolean medFlereKlassekode) {
        List<LocalDate> opphørsdatoVerdierForFeriepg = Arrays.asList(LocalDate.of(2019, 5, 1), LocalDate.of(2020, 5, 1));
        for (Oppdragslinje150 opp150Revurd : opp150RevurdListe) {
            Oppdragslinje150 originaltOpp150 = originaltOpp150Liste.stream()
                .filter(oppdragslinje150 -> oppdragslinje150.getDelytelseId().equals(opp150Revurd.getDelytelseId()))
                .findFirst().orElse(null);
            if (medFlereKlassekode) {
                List<String> kodeKlassifikForrigeListe = originaltOpp150Liste.stream().filter(opp150 -> !opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik())
                    && !opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik())).map(Oppdragslinje150::getKodeKlassifik).distinct().collect(Collectors.toList());
                List<String> kodeKlassifikRevurderingListe = opp150RevurdListe.stream().filter(opp150 -> !opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik())
                    && !opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik())).filter(opp150 -> opp150.gjelderOpphør()).
                    filter(opp150 -> opp150.getKodeStatusLinje().equals(ØkonomiKodeStatusLinje.OPPH.name()))
                    .map(Oppdragslinje150::getKodeKlassifik).distinct().collect(Collectors.toList());
                assertThat(kodeKlassifikRevurderingListe).containsOnlyElementsOf(kodeKlassifikForrigeListe);
            }
            if (originaltOpp150 != null) {
                assertThat(opp150Revurd.getDelytelseId()).isEqualTo(originaltOpp150.getDelytelseId());
                assertThat(opp150Revurd.getKodeEndringLinje()).isEqualTo(ØkonomiKodeEndringLinje.ENDR.name());
                assertThat(opp150Revurd.getKodeStatusLinje()).isEqualTo(ØkonomiKodeStatusLinje.OPPH.name());
                assertThat(opp150Revurd.getRefDelytelseId()).isNull();
                assertThat(opp150Revurd.getRefFagsystemId()).isNull();
                if (oppdragskontrollTestVerktøy.erOpp150ForFeriepenger(opp150Revurd)) {
                    assertThat(opp150Revurd.getDatoStatusFom()).isIn(opphørsdatoVerdierForFeriepg);
                } else {
                    LocalDate førsteDatoVedtakFom = oppdragskontrollTestVerktøy.finnFørsteDatoVedtakFom(originaltOpp150Liste, originaltOpp150);
                    LocalDate datoStatusFom = førsteDatoVedtakFom.isAfter(endringsdato) ? førsteDatoVedtakFom : endringsdato;
                    assertThat(opp150Revurd.getDatoStatusFom()).isEqualTo(datoStatusFom);
                }
                assertThat(opp150Revurd.getSats()).isEqualTo(originaltOpp150.getSats());
                assertThat(opp150Revurd.getTypeSats()).isEqualTo(originaltOpp150.getTypeSats());
                assertThat(opp150Revurd.getBrukKjoreplan()).isEqualTo(originaltOpp150.getBrukKjoreplan());
            }
        }
        if (medFeriePenger) {
            assertThat(opp150RevurdListe).anySatisfy(opp150 ->
                assertThat(opp150.getKodeKlassifik()).isEqualTo(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik()));
            assertThat(opp150RevurdListe).anySatisfy(opp150 ->
                assertThat(opp150.getKodeKlassifik()).isEqualTo(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik()));
            assertThat(opp150RevurdListe).anySatisfy(opp150 ->
                assertThat(opp150.getTypeSats()).isEqualTo(TYPE_SATS_FP_FERIEPG));
            List<Oppdragslinje150> opp150FeriepgBrukerList = opp150RevurdListe.stream().filter(o150 -> o150.getUtbetalesTilId() != null)
                .filter(opp150 -> opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik()))
                .filter(opp150 -> opp150.gjelderOpphør()).collect(Collectors.toList());
            List<Oppdragslinje150> opp150ArbeidsgiverList = opp150RevurdListe.stream()
                .filter(opp150 -> opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik())).collect(Collectors.toList());
            assertThat(opp150FeriepgBrukerList).anySatisfy(opp150 ->
                assertThat(opp150.getKodeStatusLinje()).isEqualTo(ØkonomiKodeStatusLinje.OPPH.name()));
            assertThat(opp150ArbeidsgiverList).anySatisfy(opp150 ->
                assertThat(opp150.getKodeStatusLinje()).isEqualTo(ØkonomiKodeStatusLinje.OPPH.name()));
        }

        Oppdragskontroll originaltOppdrag = originaltOpp150Liste.get(0).getOppdrag110().getOppdragskontroll();
        List<Oppdrag110> oppdrag110RevurderingList = originaltOppdrag.getOppdrag110Liste();
        oppdragskontrollTestVerktøy.verifiserGrad170FraRepo(opp150RevurdListe, originaltOppdrag);
        oppdragskontrollTestVerktøy.verifiserRefusjonInfo156FraRepo(oppdrag110RevurderingList, originaltOppdrag);
    }

    protected Oppdragskontroll oppsettBeregningsresultatFP(boolean erOpptjentOverFlereÅr, Long årsbeløp1, Long årsbeløp2) {
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFPForVerifiseringAvOpp150MedFeriepenger(erOpptjentOverFlereÅr, årsbeløp1, årsbeløp2);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), PROSESS_TASK_ID_1);
        return oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);
    }

    protected Behandling oppsettBeregningsresultatFPRevurdering(boolean erOpptjentOverFlereÅr, Long årsbeløp1, Long årsbeløp2) {

        return oppsettBeregningsresultatFPRevurdering(erOpptjentOverFlereÅr, årsbeløp1, årsbeløp2, this.behandlingFP);
    }

    protected Behandling oppsettBeregningsresultatFPRevurdering(boolean erOpptjentOverFlereÅr, Long årsbeløp1, Long årsbeløp2, Behandling behandling) {
        Behandling revurdering = opprettOgLagreRevurdering(behandling, VedtakResultatType.INNVILGET, 1, false, true);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(LocalDate.now().plusDays(5));
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatFPForVerifiseringAvOpp150MedFeriepenger(erOpptjentOverFlereÅr, årsbeløp1, årsbeløp2);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);
        return revurdering;
    }

    protected BeregningsresultatFP buildBeregningsresultatBrukerFP() {
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatFP, 1, 7);
        BeregningsresultatAndel andelBruker = buildBeregningsresultatAndel(brPeriode1, true, 1500, BigDecimal.valueOf(100), virksomhet);
        buildBeregningsresultatAndel(brPeriode1, false, 500, BigDecimal.valueOf(100), virksomhet);
        BeregningsresultatFeriepenger feriepenger = buildBeregningsresultatFeriepenger(beregningsresultatFP);
        buildBeregningsresultatFeriepengerPrÅr(feriepenger, andelBruker, 20000L, Collections.singletonList(LocalDate.now()));

        return beregningsresultatFP;
    }

    protected BeregningsresultatFP buildBeregningsresultatMedFlereInntektskategoriFP(boolean sammeKlasseKodeForFlereAndeler, AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori) {
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatFP, 1, 7);
        BeregningsresultatAndel andelBruker = buildBeregningsresultatAndel(brPeriode1, true, 1500, BigDecimal.valueOf(80), virksomhet, aktivitetStatus, inntektskategori);
        if (sammeKlasseKodeForFlereAndeler) {
            buildBeregningsresultatAndel(brPeriode1, true, 1500, BigDecimal.valueOf(80), virksomhet2, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);
        }
        BeregningsresultatPeriode brPeriode2 = buildBeregningsresultatPeriode(beregningsresultatFP, 8, 15);
        BeregningsresultatAndel andelArbeidsgiver2 = buildBeregningsresultatAndel(brPeriode2, false, 400, BigDecimal.valueOf(100), virksomhet2);

        BeregningsresultatFeriepenger feriepenger = buildBeregningsresultatFeriepenger(beregningsresultatFP);
        buildBeregningsresultatFeriepengerPrÅr(feriepenger, andelBruker, 20000L, Collections.singletonList(LocalDate.now()));
        buildBeregningsresultatFeriepengerPrÅr(feriepenger, andelArbeidsgiver2, 20000L, Collections.singletonList(LocalDate.now()));

        return beregningsresultatFP;
    }

    protected BeregningsresultatFP buildBeregningsresultatEntenForBrukerEllerArbgvr(boolean erBrukerMottaker, boolean medFeriepenger) {
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatFP, 1, 10);
        BeregningsresultatAndel andel1 = buildBeregningsresultatAndel(brPeriode1, erBrukerMottaker, 1500, BigDecimal.valueOf(100), virksomhet);
        BeregningsresultatPeriode brPeriode2 = buildBeregningsresultatPeriode(beregningsresultatFP, 11, 20);
        buildBeregningsresultatAndel(brPeriode2, erBrukerMottaker, 1500, BigDecimal.valueOf(100), virksomhet);
        if (medFeriepenger) {
            BeregningsresultatFeriepenger feriepenger = buildBeregningsresultatFeriepenger(beregningsresultatFP);
            buildBeregningsresultatFeriepengerPrÅr(feriepenger, andel1, 20000L, Collections.singletonList(LocalDate.now()));
        }

        return beregningsresultatFP;
    }

    protected BeregningsresultatFP buildBeregningsresultatFPForVerifiseringAvOpp150MedFeriepenger(boolean erOpptjentOverFlereÅr, Long årsbeløp1, Long årsbeløp2) {
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatFP, 1, 10);
        BeregningsresultatAndel andel1 = buildBeregningsresultatAndel(brPeriode1, true, 1500, BigDecimal.valueOf(100), virksomhet);
        BeregningsresultatAndel andel2 = buildBeregningsresultatAndel(brPeriode1, false, 1300, BigDecimal.valueOf(100), virksomhet);
        BeregningsresultatFeriepenger feriepenger = buildBeregningsresultatFeriepenger(beregningsresultatFP);
        oppsettFeriepenger(erOpptjentOverFlereÅr, årsbeløp1, årsbeløp2, andel1, feriepenger);
        oppsettFeriepenger(erOpptjentOverFlereÅr, årsbeløp1, årsbeløp2, andel2, feriepenger);

        return beregningsresultatFP;
    }

    private void oppsettFeriepenger(boolean erOpptjentOverFlereÅr, Long årsbeløp1, Long årsbeløp2, BeregningsresultatAndel andel1, BeregningsresultatFeriepenger feriepenger) {
        List<LocalDate> opptjeningsårListe;
        if (erOpptjentOverFlereÅr) {
            opptjeningsårListe = Arrays.asList(LocalDate.now(), LocalDate.now().plusYears(1));
        } else if (årsbeløp1 > 0 || årsbeløp2 > 0) {
            opptjeningsårListe = årsbeløp2 > 0 ? Collections.singletonList(LocalDate.now().plusYears(1))
                : Collections.singletonList(LocalDate.now());
        } else {
            opptjeningsårListe = Collections.emptyList();
        }
        List<Long> årsbeløpListe = Arrays.asList(årsbeløp1, årsbeløp2);
        int size = opptjeningsårListe.size();
        for (int i = 0; i < årsbeløpListe.size(); i++) {
            Long årsbeløp = årsbeløpListe.get(i);
            if (årsbeløp > 0) {
                LocalDate opptjeningsår = size == 2 ? opptjeningsårListe.get(i) : opptjeningsårListe.get(0);
                buildBeregningsresultatFeriepengerPrÅr(feriepenger, andel1, årsbeløp, opptjeningsår);
            }
        }
    }

    protected BeregningsresultatFP buildBeregningsresultatRevurderingFP(boolean medFeriepenger) {
        BeregningsresultatFP beregningsresultatRevurderingFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatRevurderingFP, 1, 7);
        buildBeregningsresultatAndel(brPeriode1, true, 1600, BigDecimal.valueOf(80), virksomhet);
        BeregningsresultatAndel andelRevurderingArbeidsforhold = buildBeregningsresultatAndel(brPeriode1, false, 400, BigDecimal.valueOf(100), virksomhet);

        BeregningsresultatPeriode brPeriode2 = buildBeregningsresultatPeriode(beregningsresultatRevurderingFP, 8, 15);
        buildBeregningsresultatAndel(brPeriode2, true, 1600, BigDecimal.valueOf(80), virksomhet4);
        BeregningsresultatAndel andelRevurderingArbeidsforhold4 = buildBeregningsresultatAndel(brPeriode2, false, 400, BigDecimal.valueOf(100), virksomhet4);

        BeregningsresultatPeriode brPeriode3 = buildBeregningsresultatPeriode(beregningsresultatRevurderingFP, 17, 22);
        buildBeregningsresultatAndel(brPeriode3, true, 0, BigDecimal.valueOf(80), virksomhet3);
        buildBeregningsresultatAndel(brPeriode3, false, 2160, BigDecimal.valueOf(80), virksomhet3);

        BeregningsresultatPeriode brPeriode4 = buildBeregningsresultatPeriode(beregningsresultatRevurderingFP, 23, 29);
        buildBeregningsresultatAndel(brPeriode4, true, 2160, BigDecimal.valueOf(80), virksomhet3);
        buildBeregningsresultatAndel(brPeriode4, false, 0, BigDecimal.valueOf(80), virksomhet3);

        if (medFeriepenger) {
            BeregningsresultatFeriepenger feriepengerRevurdering = buildBeregningsresultatFeriepenger(beregningsresultatRevurderingFP);
            buildBeregningsresultatFeriepengerPrÅr(feriepengerRevurdering, andelRevurderingArbeidsforhold, 15000L, Collections.singletonList(LocalDate.now()));
            buildBeregningsresultatFeriepengerPrÅr(feriepengerRevurdering, andelRevurderingArbeidsforhold4, 15000L, Collections.singletonList(LocalDate.now()));
        }
        return beregningsresultatRevurderingFP;
    }

    protected BeregningsresultatFP buildBeregningsresultatRevurderingFP(AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori) {

        return buildBeregningsresultatRevurderingFP(aktivitetStatus, inntektskategori, virksomhet, virksomhet4, true);
    }

    protected BeregningsresultatFP buildBeregningsresultatRevurderingFP(AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori, VirksomhetEntitet førsteVirksomhet,
                                                                        VirksomhetEntitet andreVirksomhet, boolean medFeriepenger) {
        BeregningsresultatFP beregningsresultatRevurderingFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatRevurderingFP, 1, 7);
        buildBeregningsresultatAndel(brPeriode1, true, 1600, BigDecimal.valueOf(80), førsteVirksomhet, aktivitetStatus, inntektskategori);
        BeregningsresultatAndel andelRevurderingArbeidsforhold = buildBeregningsresultatAndel(brPeriode1, false, 400, BigDecimal.valueOf(100), førsteVirksomhet, aktivitetStatus, inntektskategori);

        BeregningsresultatPeriode brPeriode2 = buildBeregningsresultatPeriode(beregningsresultatRevurderingFP, 8, 15);
        buildBeregningsresultatAndel(brPeriode2, true, 1600, BigDecimal.valueOf(80), andreVirksomhet, aktivitetStatus, inntektskategori);
        BeregningsresultatAndel andelRevurderingArbeidsforhold4 = buildBeregningsresultatAndel(brPeriode2, false, 400, BigDecimal.valueOf(100), andreVirksomhet, aktivitetStatus, inntektskategori);

        if (medFeriepenger) {
            BeregningsresultatFeriepenger feriepengerRevurdering = buildBeregningsresultatFeriepenger(beregningsresultatRevurderingFP);
            buildBeregningsresultatFeriepengerPrÅr(feriepengerRevurdering, andelRevurderingArbeidsforhold, 15000L, Collections.singletonList(LocalDate.now()));
            buildBeregningsresultatFeriepengerPrÅr(feriepengerRevurdering, andelRevurderingArbeidsforhold4, 15000L, Collections.singletonList(LocalDate.now()));
        }

        return beregningsresultatRevurderingFP;
    }

    protected BeregningsresultatFP buildBeregningsresultatRevurderingMedFlereInntektskategoriFP(AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori) {
        BeregningsresultatFP beregningsresultatRevurderingFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatRevurderingFP, 1, 7);
        buildBeregningsresultatAndel(brPeriode1, true, 1600, BigDecimal.valueOf(80), virksomhet4);
        BeregningsresultatAndel andelRevurderingArbeidsiver = buildBeregningsresultatAndel(brPeriode1, false, 400, BigDecimal.valueOf(100), virksomhet4);

        BeregningsresultatPeriode brPeriode2 = buildBeregningsresultatPeriode(beregningsresultatRevurderingFP, 8, 15);
        buildBeregningsresultatAndel(brPeriode2, true, 1600, BigDecimal.valueOf(80), virksomhet);
        buildBeregningsresultatAndel(brPeriode2, true, 1500, BigDecimal.valueOf(80), virksomhet4, aktivitetStatus, inntektskategori);

        BeregningsresultatFeriepenger feriepengerRevurdering = buildBeregningsresultatFeriepenger(beregningsresultatRevurderingFP);
        buildBeregningsresultatFeriepengerPrÅr(feriepengerRevurdering, andelRevurderingArbeidsiver, 16000L, Collections.singletonList(LocalDate.now()));

        return beregningsresultatRevurderingFP;
    }

    protected BeregningsresultatFP buildBeregningsresultatRevurderingEntenForBrukerEllerArbgvr(boolean erBrukerMottaker, boolean medFeriepenger) {
        BeregningsresultatFP beregningsresultatRevurderingFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatRevurderingFP, 1, 10);
        BeregningsresultatAndel andel1 = buildBeregningsresultatAndel(brPeriode1, erBrukerMottaker, 2000, BigDecimal.valueOf(100), virksomhet);
        BeregningsresultatPeriode brPeriode2 = buildBeregningsresultatPeriode(beregningsresultatRevurderingFP, 11, 20);
        buildBeregningsresultatAndel(brPeriode2, erBrukerMottaker, 1500, BigDecimal.valueOf(100), virksomhet);
        if (medFeriepenger) {
            BeregningsresultatFeriepenger feriepenger = buildBeregningsresultatFeriepenger(beregningsresultatRevurderingFP);
            buildBeregningsresultatFeriepengerPrÅr(feriepenger, andel1, 21000L, Collections.singletonList(LocalDate.now()));
        }

        return beregningsresultatRevurderingFP;
    }

    protected List<Oppdragslinje150> getOppdragslinje150Feriepenger(Oppdragskontroll oppdrag) {

        return oppdrag.getOppdrag110Liste().stream().flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste()
            .stream()).filter(opp150 -> opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik())
            || opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik())).collect(Collectors.toList());
    }
}
