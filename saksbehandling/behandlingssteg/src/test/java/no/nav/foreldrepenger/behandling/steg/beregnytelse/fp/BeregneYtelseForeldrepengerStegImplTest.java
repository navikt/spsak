package no.nav.foreldrepenger.behandling.steg.beregnytelse.fp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.beregning.ytelse.BeregnFeriepengerTjeneste;
import no.nav.foreldrepenger.domene.beregning.ytelse.FastsettBeregningsresultatTjeneste;
import no.nav.foreldrepenger.domene.beregning.ytelse.FinnEndringsdatoBeregningsresultatFPTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.util.Tuple;

@RunWith(CdiRunner.class)
public class BeregneYtelseForeldrepengerStegImplTest {
    private static final AktørId AKTØR_ID = new AktørId("100000");
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BeregningsresultatFPRepository beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
    private final UttakRepository uttakRepository = repositoryProvider.getUttakRepository();
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    @Mock
    private FastsettBeregningsresultatTjeneste fastsettBeregningsresultatTjeneste = mock(FastsettBeregningsresultatTjeneste.class);
    private BeregnFeriepengerTjeneste beregnFeriepengerTjeneste = mock(BeregnFeriepengerTjeneste.class);
    @Inject
    private FinnEndringsdatoBeregningsresultatFPTjeneste finnEndringsdatoBeregningsresultatFPTjeneste;

    private BeregneYtelseForeldrepengerStegImpl steg;
    private BeregningsresultatFP beregningsresultatFP;

    @Before
    public void setup() {
        beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("regelInput")
            .medRegelSporing("regelSporing")
            .build();
        steg = new BeregneYtelseForeldrepengerStegImpl(repositoryProvider, fastsettBeregningsresultatTjeneste, beregnFeriepengerTjeneste, finnEndringsdatoBeregningsresultatFPTjeneste);
    }

    @Test
    public void skalUtførStegForFørstegangsbehandling() {
        // Arrange
        ArgumentCaptor<Beregningsgrunnlag> beregningsgrunnlagCaptor = ArgumentCaptor.forClass(Beregningsgrunnlag.class);
        ArgumentCaptor<UttakResultatEntitet> uttakResultatPlanCaptor = ArgumentCaptor.forClass(UttakResultatEntitet.class);
        ArgumentCaptor<Behandling> behandlingCaptor = ArgumentCaptor.forClass(Behandling.class);
        when(fastsettBeregningsresultatTjeneste.fastsettBeregningsresultat(beregningsgrunnlagCaptor.capture(), uttakResultatPlanCaptor.capture(), behandlingCaptor.capture())).thenReturn(beregningsresultatFP);

        Tuple<Behandling, BehandlingskontrollKontekst> behandlingKontekst = byggGrunnlag(true, true);
        Behandling behandling = behandlingKontekst.getElement1();
        BehandlingskontrollKontekst kontekst = behandlingKontekst.getElement2();

        // Act
        BehandleStegResultat stegResultat = steg.utførSteg(kontekst);

        // Assert
        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(beregningsgrunnlagCaptor.getValue()).isNotNull();
        assertThat(uttakResultatPlanCaptor.getValue()).isNotNull();

        Optional<BeregningsresultatFP> beregningsresultatFP = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling);
        assertThat(beregningsresultatFP).hasValueSatisfying(resultat -> {
            assertThat(resultat).isNotNull();
            assertThat(resultat.getRegelInput()).as("regelInput").isEqualTo("regelInput");
            assertThat(resultat.getRegelSporing()).as("regelSporing").isEqualTo("regelSporing");
        });
    }

    @Test
    public void skalSletteBeregningsresultatFPVedTilbakehopp() {
        // Arrange
        Tuple<Behandling, BehandlingskontrollKontekst> behandlingKontekst = byggGrunnlag(true, true);
        Behandling behandling = behandlingKontekst.getElement1();
        BehandlingskontrollKontekst kontekst = behandlingKontekst.getElement2();
        beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);

        // Act
        steg.vedHoppOverBakover(kontekst, behandling, null, null, null);

        // Assert
        Optional<BeregningsresultatFP> resultat = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling);
        assertThat(resultat).isNotPresent();
    }

    @Test
    public void skalKasteFeilNårUttakResultatPlanMangler() {
        // Assert
        expectedException.expect(NoResultException.class);
        expectedException.expectMessage("Fant ikke uttak resultat på behandlingen");

        // Arrange
        Tuple<Behandling, BehandlingskontrollKontekst> behandlingKontekst = byggGrunnlag(true, false);
        BehandlingskontrollKontekst kontekst = behandlingKontekst.getElement2();

        // Act
        steg.utførSteg(kontekst);
    }

    @Test
    public void skalKasteFeilNårBeregningsgrunnlagMangler() {
        // Assert
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Mangler Beregningsgrunnlag for behandling");

        // Arrange
        Tuple<Behandling, BehandlingskontrollKontekst> behandlingKontekst = byggGrunnlag(false, true);
        BehandlingskontrollKontekst kontekst = behandlingKontekst.getElement2();

        // Act
        steg.utførSteg(kontekst);
    }

    private Tuple<Behandling, BehandlingskontrollKontekst> byggGrunnlag(boolean medBeregningsgrunnlag, boolean medUttaksPlanResultat) {
        ScenarioMorSøkerForeldrepenger scenarioMorSøkerForeldrepenger = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenarioMorSøkerForeldrepenger.medBruker(AKTØR_ID, NavBrukerKjønn.KVINNE);
        scenarioMorSøkerForeldrepenger.medDefaultInntektArbeidYtelse();
        if (medBeregningsgrunnlag) {
            scenarioMorSøkerForeldrepenger.medBeregningsgrunnlag()
                .medSkjæringstidspunkt(LocalDate.now())
                .medDekningsgrad(100L)
                .medOpprinneligSkjæringstidspunkt(LocalDate.now())
                .medGrunnbeløp(BigDecimal.valueOf(90000))
                .medRedusertGrunnbeløp(BigDecimal.valueOf(90000));
        }
        Behandling behandling = scenarioMorSøkerForeldrepenger.lagre(repositoryProvider);
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        if (medUttaksPlanResultat) {
            byggUttakPlanResultat(behandling);
        }
        return new Tuple<>(behandling, kontekst);
    }

    private void byggUttakPlanResultat(Behandling behandling) {
        UttakResultatPeriodeEntitet periode = new UttakResultatPeriodeEntitet.Builder(LocalDate.now().minusDays(3), LocalDate.now().minusDays(1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medOrgnr("000000000").oppdatertOpplysningerNå().build();
        repoRule.getRepository().lagre(virksomhet);

        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("1234"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivitet)
            .medTrekkdager(15)
            .medArbeidsprosent(BigDecimal.ZERO)
            .medUtbetalingsprosent(BigDecimal.valueOf(100))
            .build();

        periode.leggTilAktivitet(periodeAktivitet);

        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        perioder.leggTilPeriode(periode);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, perioder);
    }
}
