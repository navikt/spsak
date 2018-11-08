package no.nav.foreldrepenger.domene.vedtak;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.InntektArbeidYtelseScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.domene.vedtak.impl.VurderOmArenaYtelseSkalOpphøreImpl;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class VurderOmArenaYtelseSkalOpphøreImplTest {

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final BehandlingVedtakRepository behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
    private final Repository repository = repoRule.getRepository();
    private final BeregningsresultatFPRepository beregningsresultatFPRepository = new BeregningsresultatFPRepositoryImpl(repoRule.getEntityManager());

    private static final AktørId AKTØR_ID = new AktørId("210195");
    private static final String SAK_ID = "1200095";
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().plusMonths(3);

    private ScenarioMorSøkerForeldrepenger scenario;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private OppgaveTjeneste oppgaveTjeneste;
    private Behandling behandling;

    private VurderOmArenaYtelseSkalOpphøreImpl vurdereOmArenaYtelseSkalOpphør;
    private BeregningsresultatFP.Builder beregningsresultatFPBuilder;
    private BeregningsresultatPeriode.Builder brPeriodebuilder;

    @Before
    public void setUp() {
        AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
        inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null,
            null, null, skjæringstidspunktTjeneste, apOpptjening);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(any())).thenReturn(SKJÆRINGSTIDSPUNKT);

        scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID);

        vurdereOmArenaYtelseSkalOpphør = new VurderOmArenaYtelseSkalOpphøreImpl(beregningsresultatFPRepository,
            inntektArbeidYtelseTjeneste, behandlingVedtakRepository,oppgaveTjeneste);
    }

    @Test
    public void skal_teste_arena_ytelser_finnes_ikke() {
        // Arrange
        byggScenarioUtenYtelseIArena();
        // Act
        boolean resultat = vurdereOmArenaYtelseSkalOpphør.vurder(behandling, AKTØR_ID.getId());
        // Assert
        assertThat(resultat).isFalse();
    }

    // T1: Siste utbetalingsdato for ARENA-ytelse før vedtaksdato for foreldrepenger
    // T2: Første forventede utbetalingsdato for ARENA-ytelse etter vedtaksdato for foreldrepenger

    @Test
    public void skal_teste_startdatoFP_før_T1() {
        // Arrange
        // Startdato før T1 , vedtaksdato etter T1
        byggScenario(now(), now().plusDays(45), now().plusDays(15), now().plusDays(1), Fagsystem.ARENA);
        // Act
        boolean resultat = vurdereOmArenaYtelseSkalOpphør.vurder(behandling, AKTØR_ID.getId());
        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skal_teste_startdatoFP_etter_T2() {
        // Arrange
        // Startdato før T2, vedtaksdato etter T2
        byggScenario(now(), now().plusDays(45), now().plusDays(15), now().plusDays(32), Fagsystem.ARENA);
        // Act
        boolean resultat = vurdereOmArenaYtelseSkalOpphør.vurder(behandling, AKTØR_ID.getId());
        // Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skal_teste_startdatoFP_mellom_T1_T2_vedtaksdato_mindre_enn_8_dager_etter_T1() {
        // Arrange
        //  startdato mellom T1 og T2, vedtaksdato mellom T1 og (T1 + 8 dager)
        byggScenario(now(), now().plusDays(45), now().plusDays(16), now().plusDays(18), Fagsystem.ARENA);
        // Act
        boolean resultat = vurdereOmArenaYtelseSkalOpphør.vurder(behandling, AKTØR_ID.getId());
        // Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skal_teste_startdatoFP_mellom_T1_T2_vedtaksdato_mindre_enn_8_dager_før_T2() {
        // Arrange
        // startdato mellom T1 og T2, vedtaksdato mellom (T2 - 8 dager) og T2
        byggScenario(now(), now().plusDays(45), now().plusDays(24), now().plusDays(18), Fagsystem.ARENA);
        // Act
        boolean resultat = vurdereOmArenaYtelseSkalOpphør.vurder(behandling, AKTØR_ID.getId());
        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skal_teste_Arena_ytelse_interval_før_vedtaksdato_fom_overlapper_FP(){
        // Arrange
        // Arena ytelser før vedtaksdato og mellom startdato FP og sluttdato FP.
        byggScenario(now(),now().plusDays(28), now().plusDays(46), now().minusDays(1), Fagsystem.ARENA);
        // Act
        boolean resultat = vurdereOmArenaYtelseSkalOpphør.vurder(behandling, AKTØR_ID.getId());
        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skal_teste_Arena_ytelse_interval_før_vedtaksdato_tom_overlapper_FP(){
        // Arrange
        // Arena ytelser før vedtaksdato og mellom startdato FP og sluttdato FP.
        byggScenario(now().minusDays(15),now(), now().plusDays(46), now().minusDays(1), Fagsystem.ARENA);
        // Act
        boolean resultat = vurdereOmArenaYtelseSkalOpphør.vurder(behandling, AKTØR_ID.getId());
        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skal_teste_Arena_ytelse_interval_før_vedtaksdato_overlapper_ikke_FP(){
        // Arrange
        // Arena ytelser før vedtaksdato og utenfor startdato FP og sluttdato FP.
        byggScenario(now(),now().plusDays(28), now().plusDays(46), now().minusDays(7), Fagsystem.ARENA);
        // Act
        boolean resultat = vurdereOmArenaYtelseSkalOpphør.vurder(behandling, AKTØR_ID.getId());
        // Assert
        assertThat(resultat).isFalse();

    }

    @Test
    public void skal_finne_første_uttaksdato_fra_periodeliste() {
        // Arrange
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode beregningsresultatP1 = BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(now(), now().plusDays(2))
            .build(beregningsresultatFP);
        BeregningsresultatPeriode beregningsresultatP2 = BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(now().plusDays(2), now().plusDays(5))
            .build(beregningsresultatFP);
        BeregningsresultatAndel.builder()
            .medStillingsprosent(BigDecimal.ZERO)
            .medUtbetalingsgrad(BigDecimal.ZERO)
            .medDagsatsFraBg(0)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medDagsats(0)
            .medBrukerErMottaker(true)
            .build(beregningsresultatP1);
        BeregningsresultatAndel.builder()
            .medStillingsprosent(BigDecimal.ZERO)
            .medUtbetalingsgrad(BigDecimal.ZERO)
            .medDagsatsFraBg(0)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medDagsats(100)
            .medBrukerErMottaker(true)
            .build(beregningsresultatP2);
        // Act
        LocalDate førsteUttaksdato = vurdereOmArenaYtelseSkalOpphør.finnFørsteUttaksdato(
            Arrays.asList(beregningsresultatP1, beregningsresultatP2));
        // Assert
        assertThat(førsteUttaksdato).isEqualTo(now().plusDays(2));
    }

    private void byggScenarioUtenYtelseIArena() {
        byggScenario(now(), now().plusDays(15), now(), now(), Fagsystem.INFOTRYGD);
    }

    private void byggScenario(LocalDate ytelserFom, LocalDate ytelserTom, LocalDate vedtaksdato, LocalDate startdato, Fagsystem fagsystem) {
        InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder builder = scenario.getInntektArbeidYtelseScenarioTestBuilder();
        InntektArbeidYtelseAggregatBuilder aggregatBuilder = builder.medAktørId(AKTØR_ID).build();
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = aggregatBuilder.getAktørYtelseBuilder(AKTØR_ID);
        byggYtelser(ytelserFom, ytelserTom, fagsystem).forEach(ytelseBuilder -> aktørYtelseBuilder.leggTilYtelse(ytelseBuilder));
        aggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);

        behandling = scenario.lagre(repositoryProvider);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .buildFor(behandling);
        repository.lagre(behandlingsresultat);
        repository.lagre(behandling);
        repository.flushAndClear();
        final BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder()
            .medBehandlingsresultat(behandlingsresultat)
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medVedtaksdato(vedtaksdato)
            .medAnsvarligSaksbehandler("asdf").build();
        behandlingVedtakRepository.lagre(behandlingVedtak, behandlingRepository.taSkriveLås(behandling));

        beregningsresultatFPBuilder = BeregningsresultatFP.builder();
        brPeriodebuilder = BeregningsresultatPeriode.builder();
        BeregningsresultatFP beregningsresultatFP = byggBeregningsresultatFP(startdato, startdato.plusDays(2));
        beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);
    }

    private List<YtelseBuilder> byggYtelser(LocalDate ytelserFom, LocalDate ytelserTom, Fagsystem fagsystem) {
        // Man må sende meldekort hver 2 uker.
        final long ytelseDagerMellomrom = 14;
        List<YtelseBuilder> ytelser = new ArrayList<>();
        LocalDate fom = ytelserFom;
        LocalDate tom = ytelserFom.plusDays(ytelseDagerMellomrom);
        while (tom.isBefore(ytelserTom)) {
            ytelser.add(
                YtelseBuilder.oppdatere(Optional.empty())
                    .medKilde(fagsystem)
                    .medSaksnummer(new Saksnummer(SAK_ID))
                    .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom))
                    .medStatus(RelatertYtelseTilstand.LØPENDE)
                    .medYtelseType(RelatertYtelseType.DAGPENGER)
                    .medBehandlingsTema(TemaUnderkategori.UDEFINERT)
            );
            fom = tom.plusDays(1);
            tom = fom.plusDays(ytelseDagerMellomrom);
        }
        return ytelser;
    }

    private BeregningsresultatFP byggBeregningsresultatFP(LocalDate fom, LocalDate tom) {
        BeregningsresultatFP beregningsresultatFP = beregningsresultatFPBuilder
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        byggBeregningsresultatPeriode(beregningsresultatFP, fom, tom);
        return beregningsresultatFP;
    }

    private BeregningsresultatPeriode byggBeregningsresultatPeriode(BeregningsresultatFP beregningsresultatFP,
                                                                    LocalDate fom, LocalDate tom) {
        return brPeriodebuilder
            .medBeregningsresultatPeriodeFomOgTom(fom, tom)
            .build(beregningsresultatFP);
    }
}
