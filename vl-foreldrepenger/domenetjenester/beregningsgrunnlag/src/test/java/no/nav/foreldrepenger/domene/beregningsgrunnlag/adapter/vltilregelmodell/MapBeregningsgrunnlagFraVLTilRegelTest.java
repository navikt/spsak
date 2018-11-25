package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.MINUS_DAYS_10;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.MINUS_DAYS_5;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.MINUS_YEARS_1;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.MINUS_YEARS_2;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.NOW;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBGAktivitetStatus;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBGPStatus;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBGPStatusForSN;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBGPeriode;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBeregningsgrunnlag;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLSammenligningsgrunnlag;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.fp.impl.RevurderingFPTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet.InntektBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel.Type;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Periodeinntekt;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class MapBeregningsgrunnlagFraVLTilRegelTest {

    private static final int MELDEKORTSATS1 = 1000;
    private static final int MELDEKORTSATS2 = 1100;
    private static final int SIGRUN_2015 = 500000;
    private static final int SIGRUN_2016 = 600000;
    private static final int SIGRUN_2017 = 700000;
    private static final int TOTALINNTEKT_SIGRUN = SIGRUN_2015 + SIGRUN_2016 + SIGRUN_2017;

    private static final LocalDate FIRST_DAY_PREVIOUS_MONTH = LocalDate.now().minusMonths(1).withDayOfMonth(1);
    private static final Integer INNTEKT_BELOP = 25000;
    private static final AktørId AKTØR_ID = new AktørId("4444");
    private static final LocalDate OPPRINNELIG_IDENTDATO = null;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    @Mock
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;
    @Mock
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;

    private Behandling behandling;
    private YrkesaktivitetBuilder yrkesaktivitetBuilder;
    private VirksomhetEntitet virksomhetA;
    private VirksomhetEntitet virksomhetB;

    private VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
    private ScenarioMorSøkerForeldrepenger scenario;
    private InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider,
        mock(BehandlingModellRepository.class), null);
    private RevurderingFPTjenesteImpl revurderingTjeneste = new RevurderingFPTjenesteImpl(repositoryProvider, behandlingskontrollTjeneste, mock(HistorikkRepository.class), null);

    @Before
    public void setup() {
        // Virksomhet A og B
        virksomhetA = new VirksomhetEntitet.Builder().medNavn("OrgA").medOrgnr("42").oppdatertOpplysningerNå().build();
        virksomhetRepository.lagre(virksomhetA);
        virksomhetB = new VirksomhetEntitet.Builder().medNavn("OrgB").medOrgnr("47").oppdatertOpplysningerNå().build();
        virksomhetRepository.lagre(virksomhetB);
        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medNavn("OrgC").medOrgnr("123").oppdatertOpplysningerNå().build();
        virksomhetRepository.lagre(virksomhet);
        scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
    }

    private InntektArbeidYtelseAggregatBuilder opprettForBehandling(ScenarioMorSøkerForeldrepenger scenario) {
        LocalDate fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);
        inntektArbeidYtelseBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();
        lagAktørArbeid(inntektArbeidYtelseBuilder, AKTØR_ID, Arbeidsgiver.virksomhet(virksomhetA), fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, Optional.empty());
        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            lagInntekt(inntektArbeidYtelseBuilder, AKTØR_ID, virksomhetA, dt, dt.plusMonths(1));
        }
        return inntektArbeidYtelseBuilder;
    }

    private Behandling lagBehandling(ScenarioMorSøkerForeldrepenger scenario) {
        opprettForBehandling(scenario);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling lagIAYforTilstøtendeYtelser(ScenarioMorSøkerForeldrepenger scenario, Beregningsgrunnlag beregningsgrunnlag) {
        LocalDate skjæring = beregningsgrunnlag.getSkjæringstidspunkt();
        InntektArbeidYtelseAggregatBuilder iayBuilder = opprettForBehandling(scenario);
        AktørYtelseBuilder aktørYtelseBuilder = iayBuilder.getAktørYtelseBuilder(AKTØR_ID);
        YtelseBuilder ytelse = lagYtelse(aktørYtelseBuilder,
            skjæring.minusMonths(3).plusDays(1),
            skjæring.plusMonths(6),
            new BigDecimal(MELDEKORTSATS1),
            new BigDecimal(200),
            skjæring.minusMonths(1),
            skjæring.minusMonths(1).plusDays(14));
        aktørYtelseBuilder.leggTilYtelse(ytelse);
        ytelse = lagYtelse(aktørYtelseBuilder,
            skjæring.minusMonths(3),
            skjæring.minusMonths(1),
            new BigDecimal(MELDEKORTSATS2),
            new BigDecimal(100),
            skjæring.minusMonths(1).plusDays(1),
            skjæring.minusMonths(1).plusDays(15));
        aktørYtelseBuilder.leggTilYtelse(ytelse);
        iayBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
        return scenario.lagre(repositoryProvider);
    }

    private YtelseBuilder lagYtelse(AktørYtelseBuilder aktørYtelseBuilder,
                                    LocalDate fom, LocalDate tom, BigDecimal beløp, BigDecimal utbetalingsgrad,
                                    LocalDate meldekortFom, LocalDate meldekortTom) {
        Saksnummer sakId = new Saksnummer("1200094");
        YtelseBuilder ytelselseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.FPSAK, RelatertYtelseType.DAGPENGER, sakId);
        ytelselseBuilder.tilbakestillAnvisteYtelser();
        return ytelselseBuilder.medKilde(Fagsystem.ARENA)
            .medYtelseType(RelatertYtelseType.DAGPENGER)
            .medBehandlingsTema(TemaUnderkategori.UDEFINERT)
            .medStatus(RelatertYtelseTilstand.AVSLUTTET)
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom))
            .medSaksnummer(sakId)
            .medYtelseGrunnlag(
                ytelselseBuilder.getGrunnlagBuilder()
                    .medOpprinneligIdentdato(OPPRINNELIG_IDENTDATO)
                    .medInntektsgrunnlagProsent(new BigDecimal(99.00))
                    .medDekningsgradProsent(new BigDecimal(98.00))
                    .medYtelseStørrelse(YtelseStørrelseBuilder.ny()
                        .medBeløp(new BigDecimal(100000.50))
                        .build())
                    .build())
            .medYtelseAnvist(ytelselseBuilder.getAnvistBuilder()
                .medAnvistPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(meldekortFom, meldekortTom))
                .medDagsats(beløp)
                .medUtbetalingsgradProsent(utbetalingsgrad)
                .build());
    }

    private AktørArbeid lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId,
                                       Arbeidsgiver arbeidsgiver, LocalDate fom, LocalDate tom, ArbeidType arbeidType, Optional<String> arbeidsforholdRef) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder
            .getAktørArbeidBuilder(aktørId);

        Opptjeningsnøkkel opptjeningsnøkkel;
        if (arbeidsforholdRef.isPresent()) {
            opptjeningsnøkkel = new Opptjeningsnøkkel(arbeidsforholdRef.get(), arbeidsgiver.getIdentifikator(), null);
        } else {
            opptjeningsnøkkel = Opptjeningsnøkkel.forOrgnummer(arbeidsgiver.getIdentifikator());
        }
        yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsnøkkel, arbeidType);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder.medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom)).medProsentsats(BigDecimal.valueOf(100));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtale)
            .medArbeidType(arbeidType)
            .medArbeidsgiver(arbeidsgiver);

        yrkesaktivitetBuilder.medArbeidsforholdId(arbeidsforholdRef.isPresent() ? ArbeidsforholdRef.ref(arbeidsforholdRef.get()) : null);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return aktørArbeidBuilder.build();
    }

    private void lagInntekt(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId, Virksomhet virksomhet,
                            LocalDate fom, LocalDate tom) {
        Opptjeningsnøkkel opptjeningsnøkkel = Opptjeningsnøkkel.forOrgnummer(virksomhet.getOrgnr());

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);

        Stream.of(InntektsKilde.INNTEKT_BEREGNING, InntektsKilde.INNTEKT_SAMMENLIGNING).forEach(kilde -> {
            AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
            InntektEntitet.InntektspostBuilder inntektspost = InntektEntitet.InntektspostBuilder.ny()
                .medBeløp(BigDecimal.valueOf(INNTEKT_BELOP))
                .medPeriode(fom, tom)
                .medInntektspostType(InntektspostType.LØNN);
            inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(yrkesaktivitetBuilder.build().getArbeidsgiver());
            aktørInntektBuilder.leggTilInntekt(inntektBuilder);
            inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
        });
    }

    @Test
    public void skalMapBGForSN() {
        //Arrange
        behandling = lagBehandling(scenario);
        Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLSammenligningsgrunnlag(beregningsgrunnlag);
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        leggTilInntekterFraSigrun();
        BeregningsgrunnlagPeriode bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatusForSN(bgPeriode);
        MapBeregningsgrunnlagFraVLTilRegel mapper = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);

        //Act
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = mapper.map(behandling, beregningsgrunnlag);

        //Assert
        assertThat(resultatBG).isNotNull();
        verifiserInntekterFraSigrun(resultatBG, TOTALINNTEKT_SIGRUN);
        assertThat(resultatBG.getSkjæringstidspunkt()).isEqualTo(MINUS_DAYS_5);
        assertThat(resultatBG.getSammenligningsGrunnlag().getRapportertPrÅr().doubleValue()).isEqualTo(1098318.12, within(0.01));
        assertThat(resultatBG.getSammenligningsGrunnlag().getAvvikPromille()).isEqualTo(220L);
        assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().get(0);
        assertThat(resultatBGP.getBeregningsgrunnlagPeriode().getFom()).isEqualTo(resultatBG.getSkjæringstidspunkt());
        assertThat(resultatBGP.getBeregningsgrunnlagPeriode().getTom()).isEqualTo(resultatBG.getSkjæringstidspunkt().plusYears(3));
        assertThat(resultatBGP.getBruttoPrÅr().doubleValue()).isEqualTo(4444432.32, within(0.01));
        assertThat(resultatBGP.getBeregningsgrunnlagPrStatus()).hasSize(1);
        resultatBGP.getBeregningsgrunnlagPrStatus().forEach(resultatBGPS -> {
                assertThat(resultatBGPS.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.SN);
                assertThat(resultatBGPS.getBeregningsperiode().getFom()).isEqualTo(MINUS_DAYS_10);
                assertThat(resultatBGPS.getBeregningsperiode().getTom()).isEqualTo(MINUS_DAYS_5);
                assertThat(resultatBGPS.getArbeidsforhold()).hasSize(0);
                assertThat(resultatBGPS.getBeregnetPrÅr().doubleValue()).isEqualTo(1000.01, within(0.01));
                assertThat(resultatBGPS.samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isZero();
            }
        );
    }

    private void leggTilInntekterFraSigrun() {
        AktørInntektBuilder builder = inntektArbeidYtelseBuilder.getAktørInntektBuilder(AKTØR_ID);
        InntektBuilder inntektBuilder = builder.getInntektBuilder(InntektsKilde.SIGRUN, Opptjeningsnøkkel.forType(AKTØR_ID.toString(), Type.AKTØR_ID));
        inntektBuilder.leggTilInntektspost(opprettInntektspostForSigrun(2015, SIGRUN_2015));
        inntektBuilder.leggTilInntektspost(opprettInntektspostForSigrun(2016, SIGRUN_2016));
        inntektBuilder.leggTilInntektspost(opprettInntektspostForSigrun(2017, SIGRUN_2017));
        builder.leggTilInntekt(inntektBuilder);
    }

    private InntektEntitet.InntektspostBuilder opprettInntektspostForSigrun(int år, int inntekt) {
        return InntektEntitet.InntektspostBuilder.ny()
            .medBeløp(BigDecimal.valueOf(inntekt))
            .medPeriode(LocalDate.of(år, Month.JANUARY, 1), LocalDate.of(år, Month.DECEMBER, 31))
            .medInntektspostType(InntektspostType.LØNN);
    }

    private void verifiserInntekterFraSigrun(final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG, int totalinntektSigrun) {
        assertThat(resultatBG.getInntektsgrunnlag()).isNotNull();
        Inntektsgrunnlag ig = resultatBG.getInntektsgrunnlag();
        List<Periodeinntekt> fraSigrun = ig.getPeriodeinntekter().stream().filter(mi -> mi.getInntektskilde().equals(Inntektskilde.SIGRUN)).collect(Collectors.toList());
        assertThat(fraSigrun).isNotEmpty();
        int total = fraSigrun.stream().map(Periodeinntekt::getInntekt).mapToInt(BigDecimal::intValue).sum();
        assertThat(total).isEqualTo(totalinntektSigrun);
    }

    @Test
    public void skalMapBGForArebidstakerMedFlereBGPStatuser() {
        //Arrange
        behandling = lagBehandling(scenario);
        Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        BeregningsgrunnlagPeriode bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2, MINUS_YEARS_1, Arbeidsgiver.virksomhet(virksomhetA), OpptjeningAktivitetType.ARBEID);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.virksomhet(virksomhetB), OpptjeningAktivitetType.ARBEID);

        MapBeregningsgrunnlagFraVLTilRegel mapper = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        //Act
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = mapper.map(behandling, beregningsgrunnlag);

        //Assert
        assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().get(0);
        assertThat(resultatBGP.getBeregningsgrunnlagPrStatus()).hasSize(1);
        resultatBGP.getBeregningsgrunnlagPrStatus().forEach(resultatBGPS -> {
                assertThat(resultatBGPS.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL);
                assertThat(resultatBGPS.getBeregningsperiode()).isNull();
                assertThat(resultatBGPS.getBeregnetPrÅr().doubleValue()).isEqualTo(2000.02, within(0.01));
                assertThat(resultatBGPS.samletNaturalytelseBortfaltMinusTilkommetPrÅr().doubleValue()).isEqualTo(6464.64, within(0.01));
                assertThat(resultatBGPS.getArbeidsforhold()).hasSize(2);
                assertThat(resultatBGPS.getArbeidsforhold().get(0).getArbeidsgiverId()).isEqualTo("42");
                assertArbeidforhold(resultatBGPS.getArbeidsforhold().get(0), MINUS_YEARS_2, MINUS_YEARS_1);
                assertThat(resultatBGPS.getArbeidsforhold().get(1).getArbeidsgiverId()).isEqualTo("47");
                assertArbeidforhold(resultatBGPS.getArbeidsforhold().get(1), MINUS_YEARS_1, NOW);
            }
        );
    }

    @Test
    public void skal_mappe_bg_for_arbeidstaker_hos_privatperson_og_virksomhet() {
        //Arrange
        String aktørId = "123123123123";
        behandling = lagBehandling(scenario);
        Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        BeregningsgrunnlagPeriode bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.person(new AktørId(aktørId)), OpptjeningAktivitetType.ARBEID);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2, MINUS_YEARS_1, Arbeidsgiver.virksomhet(virksomhetB), OpptjeningAktivitetType.ARBEID);

        MapBeregningsgrunnlagFraVLTilRegel mapper = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        //Act
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = mapper.map(behandling, beregningsgrunnlag);

        //Assert
        assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().get(0);
        assertThat(resultatBGP.getBeregningsgrunnlagPrStatus()).hasSize(1);
        resultatBGP.getBeregningsgrunnlagPrStatus().forEach(resultatBGPS -> {
            assertThat(resultatBGPS.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL);
            assertThat(resultatBGPS.getBeregningsperiode()).isNull();
            assertThat(resultatBGPS.getBeregnetPrÅr().doubleValue()).isEqualTo(2000.02, within(0.01));
            assertThat(resultatBGPS.samletNaturalytelseBortfaltMinusTilkommetPrÅr().doubleValue()).isEqualTo(6464.64, within(0.01));
            assertThat(resultatBGPS.getArbeidsforhold()).hasSize(2);
            assertThat(resultatBGPS.getArbeidsforhold().get(0).getArbeidsforhold().getAktørId()).isEqualTo(aktørId);
            assertArbeidforhold(resultatBGPS.getArbeidsforhold().get(0), MINUS_YEARS_1, NOW);
            assertThat(resultatBGPS.getArbeidsforhold().get(1).getArbeidsgiverId()).isEqualTo("47");
            assertArbeidforhold(resultatBGPS.getArbeidsforhold().get(1), MINUS_YEARS_2, MINUS_YEARS_1);
        });
    }

    @Test
    public void skalMapBGForATogSNBeregeningGPStatuser() {
        //Arrange
        behandling = lagBehandling(scenario);
        Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        BeregningsgrunnlagPeriode bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatusForSN(bgPeriode);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.virksomhet(virksomhetA), OpptjeningAktivitetType.ARBEID);

        //Act
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5).map(behandling, beregningsgrunnlag);
        //Assert
        assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().get(0);
        assertThat(resultatBGP.getBeregningsgrunnlagPrStatus()).hasSize(2);
        resultatBGP.getBeregningsgrunnlagPrStatus().forEach(resultatBGPStatus -> {
            if (resultatBGPStatus.getAktivitetStatus().equals(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.SN)) {
                assertThat(resultatBGPStatus.getArbeidsforhold()).isEmpty();
            } else {
                assertThat(resultatBGPStatus.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL);
                assertThat(resultatBGPStatus.getArbeidsforhold()).hasSize(1);
            }
        });
    }

    @Test
    public void skalMapBGForArbeidstakerMedInntektsgrunnlag() {
        // Arrange
        behandling = lagBehandling(scenario);
        Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        BeregningsgrunnlagPeriode bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.virksomhet(virksomhetA), OpptjeningAktivitetType.ARBEID);

        // Act
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5).map(behandling, beregningsgrunnlag);

        // Assert
        assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().get(0);

        List<Periodeinntekt> månedsinntekter = resultatBG.getInntektsgrunnlag().getPeriodeinntekter();
        assertThat(antallMånedsinntekter(månedsinntekter, INNTEKTSKOMPONENTEN_BEREGNING)).isEqualTo(12);
        assertThat(antallMånedsinntekter(månedsinntekter, INNTEKTSKOMPONENTEN_SAMMENLIGNING)).isEqualTo(12);
        assertThat(månedsinntekter).hasSize(24);
        Optional<Periodeinntekt> inntektBeregning = resultatBGP.getInntektsgrunnlag().getPeriodeinntekt(INNTEKTSKOMPONENTEN_BEREGNING, FIRST_DAY_PREVIOUS_MONTH);
        Optional<Periodeinntekt> inntektSammenligning = resultatBGP.getInntektsgrunnlag().getPeriodeinntekt(INNTEKTSKOMPONENTEN_SAMMENLIGNING, FIRST_DAY_PREVIOUS_MONTH);
        assertInntektsgrunnlag(inntektBeregning);
        assertInntektsgrunnlag(inntektSammenligning);
    }

    @Test
    public void skalMappeTilstøtendeYtelserDPogAAP() {
        //Arrange
        Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        behandling = lagIAYforTilstøtendeYtelser(scenario, beregningsgrunnlag);
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        BeregningsgrunnlagPeriode bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.DAGPENGER, Inntektskategori.DAGPENGER, MINUS_DAYS_10, NOW);

        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5).map(behandling, beregningsgrunnlag);

        List<Periodeinntekt> dpMånedsInntekter = resultatBG.getInntektsgrunnlag().getPeriodeinntekter().stream()
            .filter(mi -> mi.getInntektskilde().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP))
            .collect(Collectors.toList());
        assertThat(dpMånedsInntekter).hasSize(1);
        BigDecimal månedsinntekt = BigDecimal.valueOf(MELDEKORTSATS1).multiply(BigDecimal.valueOf(260)).divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        assertThat(dpMånedsInntekter.get(0).getInntekt()).isEqualByComparingTo(månedsinntekt);
        assertThat(dpMånedsInntekter.get(0).getUtbetalingsgrad()).hasValueSatisfying(utbg ->
            assertThat(utbg).isEqualByComparingTo(BigDecimal.valueOf(200)));
    }

    @Test
    public void skalReturnereTrueForArbeidsforholdSomStarterFørSkjæringstidspunkt() {
        //Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        String arbId = "123788322";
        inntektArbeidYtelseBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();
        AktørArbeid aktørArbeid = lagAktørArbeid(inntektArbeidYtelseBuilder, AKTØR_ID, Arbeidsgiver.virksomhet(virksomhetA), skjæringstidspunkt.minusMonths(10), skjæringstidspunkt.minusDays(1),
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, Optional.of(arbId));
        Inntektsmelding im = InntektsmeldingBuilder.builder().medArbeidsforholdId(arbId).medVirksomhet(virksomhetA).build();
        // Act
        boolean harAktivitetFørStp = MapBeregningsgrunnlagFraVLTilRegel
            .harYrkesaktiviteterFørSkjæringstidspunktet(aktørArbeid, skjæringstidspunkt, im.getArbeidsforholdRef());
        // Assert
        assertThat(harAktivitetFørStp).isTrue();
    }

    @Test
    public void skalReturnereFalseForArbeidsforholdSomStarterEtterSkjæringstidspunkt() {
        //Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        String arbId = "123788322";
        inntektArbeidYtelseBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();
        AktørArbeid aktørArbeid = lagAktørArbeid(inntektArbeidYtelseBuilder, AKTØR_ID, Arbeidsgiver.virksomhet(virksomhetA), skjæringstidspunkt.plusDays(10), skjæringstidspunkt.plusMonths(1),
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, Optional.of(arbId));
        Inntektsmelding im = InntektsmeldingBuilder.builder().medInnsendingstidspunkt(LocalDateTime.now()).medArbeidsforholdId(arbId).medVirksomhet(virksomhetA).build();
        // Act
        boolean harAktivitetFørStp = MapBeregningsgrunnlagFraVLTilRegel
            .harYrkesaktiviteterFørSkjæringstidspunktet(aktørArbeid, skjæringstidspunkt, im.getArbeidsforholdRef());
        // Assert
        assertThat(harAktivitetFørStp).isFalse();
    }

    @Test
    public void skalIkkeLageSammenligningsgrunnlagForArbeidstakerNårIkkeFinnesFraFør() {
        //Arrange
        behandling = lagBehandling(scenario);
        Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        BeregningsgrunnlagPeriode bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.virksomhet(virksomhetA), OpptjeningAktivitetType.ARBEID);
        // Act
        MapBeregningsgrunnlagFraVLTilRegel mapper = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = mapper.map(behandling, beregningsgrunnlag);
        // Assert
        assertThat(resultatBG.getSammenligningsGrunnlag()).isNull();
    }

    @Test
    public void skalLageSammenligningsgrunnlagForRevurdering() {
        //Arrange
        Behandling forrigeBehandling = lagBehandling(scenario);
        forrigeBehandling.avsluttBehandling();
        BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
        behandlingRepository.lagre(forrigeBehandling, behandlingRepository.taSkriveLås(forrigeBehandling));
        FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
        fagsakRepository.oppdaterFagsakStatus(forrigeBehandling.getFagsakId(), FagsakStatus.LØPENDE);
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_ANNET);
        Beregningsgrunnlag nyttBG = buildVLBeregningsgrunnlag();
        Beregningsgrunnlag forrigeBG = buildVLBeregningsgrunnlag();
        Arrays.asList(nyttBG, forrigeBG).forEach(bg -> {
            buildVLBGAktivitetStatus(bg);
            buildVLBGPeriode(bg);
        });
        buildVLSammenligningsgrunnlag(forrigeBG);
        repositoryProvider.getBeregningsgrunnlagRepository().lagre(forrigeBehandling, forrigeBG, BeregningsgrunnlagTilstand.FORESLÅTT);
        when(hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling)).thenReturn(false);

        // Act
        MapBeregningsgrunnlagFraVLTilRegel mapper = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = mapper.map(behandling, nyttBG);

        // Assert
        SammenligningsGrunnlag resultatSG = resultatBG.getSammenligningsGrunnlag();
        assertThat(resultatSG).isNotNull();
        Sammenligningsgrunnlag forrigeSG = forrigeBG.getSammenligningsgrunnlag();
        assertThat(resultatSG.getSammenligningsperiode()).isEqualTo(Periode.of(forrigeSG.getSammenligningsperiodeFom(), forrigeSG.getSammenligningsperiodeTom()));
        assertThat(resultatSG.getRapportertPrÅr()).isEqualByComparingTo(forrigeSG.getRapportertPrÅr());
        assertThat(resultatSG.getAvvikPromille()).isEqualTo(forrigeSG.getAvvikPromille());
    }

    @Test
    public void skalLageSammenligningsgrunnlagForTilbakehopp() {
        //Arrange
        behandling = lagBehandling(scenario);
        Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        buildVLBGPeriode(beregningsgrunnlag);
        buildVLSammenligningsgrunnlag(beregningsgrunnlag);
        repositoryProvider.getBeregningsgrunnlagRepository().lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT);
        when(hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling)).thenReturn(false);

        // Act
        MapBeregningsgrunnlagFraVLTilRegel mapper = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = mapper.map(behandling, beregningsgrunnlag);

        // Assert
        SammenligningsGrunnlag resultatSG = resultatBG.getSammenligningsGrunnlag();
        assertThat(resultatSG).isNotNull();
        Sammenligningsgrunnlag forrigeSG = beregningsgrunnlag.getSammenligningsgrunnlag();
        assertThat(resultatSG.getSammenligningsperiode()).isEqualTo(Periode.of(forrigeSG.getSammenligningsperiodeFom(), forrigeSG.getSammenligningsperiodeTom()));
        assertThat(resultatSG.getRapportertPrÅr()).isEqualByComparingTo(forrigeSG.getRapportertPrÅr());
        assertThat(resultatSG.getAvvikPromille()).isEqualTo(forrigeSG.getAvvikPromille());
    }

    @Test
    public void skalIkkeLageSammenligningsgrunnlagNårHarInnhentetNyeData() {
        //Arrange
        behandling = lagBehandling(scenario);
        Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        buildVLBGPeriode(beregningsgrunnlag);
        when(hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling)).thenReturn(true);

        // Act
        MapBeregningsgrunnlagFraVLTilRegel mapper = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = mapper.map(behandling, beregningsgrunnlag);

        // Assert
        assertThat(resultatBG.getSammenligningsGrunnlag()).isNull();
    }

    private long antallMånedsinntekter(List<Periodeinntekt> månedsinntekter, Inntektskilde inntektskomponentenBeregning) {
        return månedsinntekter.stream().filter(m -> m.getInntektskilde().equals(inntektskomponentenBeregning)).count();
    }

    private void assertInntektsgrunnlag(Optional<Periodeinntekt> inntektBeregning) {
        assertThat(inntektBeregning).isPresent();
        assertThat(inntektBeregning).hasValueSatisfying(månedsinntekt -> {
            assertThat(månedsinntekt.getInntekt().intValue()).isEqualTo(INNTEKT_BELOP);
            assertThat(månedsinntekt.getFom()).isEqualTo(FIRST_DAY_PREVIOUS_MONTH);
            assertThat(månedsinntekt.fraInntektsmelding()).isFalse();
        });
    }

    private void assertArbeidforhold(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, LocalDate fom, LocalDate tom) {
        assertThat(arbeidsforhold.getBeregnetPrÅr().doubleValue()).isEqualTo(1000.01, within(0.01));
        assertThat(arbeidsforhold.getNaturalytelseBortfaltPrÅr()).hasValueSatisfying(naturalYtelseBortfalt ->
            assertThat(naturalYtelseBortfalt.doubleValue()).isEqualTo(3232.32, within(0.01))
        );
        assertThat(arbeidsforhold.getBeregningsperiode()).isEqualTo(Periode.of(fom, tom));
    }
}
