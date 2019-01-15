package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningInntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.TilstøtendeYtelse;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.TilstøtendeYtelseAndel;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.util.Tuple;

public class MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegelTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    private static final BigDecimal MÅNEDSBELØP_TILSTØTENDE_YTELSE = BigDecimal.valueOf(10000L);
    private static final BigDecimal ÅRSSBELØP_TILSTØTENDE_YTELSE_2 = BigDecimal.valueOf(234000);
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2018, Month.MAY, 1);
    private static final AktørId AKTØR_ID = new AktørId(100000L);
    private static final String ORGNR_MED_NYTT_ARBEIDSFORHOLD = "21542512";
    private static final String ORGNR_UTEN_NYTT_ARBEIDSFORHOLD = "21542513";
    private static final LocalDate TWO_YEARS_AGO = SKJÆRINGSTIDSPUNKT.minusYears(2);
    private VirksomhetEntitet virksomhetEntitet1;
    private VirksomhetEntitet virksomhetEntitet2;

    private ScenarioMorSøkerForeldrepenger scenario;
    private Behandling behandling;
    private Beregningsgrunnlag beregningsgrunnlag;
    private GrunnlagRepositoryProvider repositoryProvider;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private GrunnlagRepositoryProvider realRepositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());

    private MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel mapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel =
        new MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel(realRepositoryProvider.getSatsRepository());

    @Before
    public void setUp() {
        virksomhetEntitet1 = new VirksomhetEntitet.Builder()
            .medOrgnr(ORGNR_MED_NYTT_ARBEIDSFORHOLD)
            .medNavn("Virksomhet1")
            .medRegistrert(TWO_YEARS_AGO)
            .medOppstart(TWO_YEARS_AGO)
            .oppdatertOpplysningerNå()
            .build();
        virksomhetEntitet2 = new VirksomhetEntitet.Builder()
            .medOrgnr(ORGNR_UTEN_NYTT_ARBEIDSFORHOLD)
            .medNavn("Virksomhet2")
            .medRegistrert(TWO_YEARS_AGO)
            .medOppstart(TWO_YEARS_AGO)
            .oppdatertOpplysningerNå()
            .build();
        realRepositoryProvider.getVirksomhetRepository().lagre(virksomhetEntitet1);
        realRepositoryProvider.getVirksomhetRepository().lagre(virksomhetEntitet2);
        scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
    }

    @Test
    public void sjekkMappingMedEttArbeidsforhold() {
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100,
            Arbeidskategori.ARBEIDSTAKER, virksomhetEntitet1, false, null);
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = repositoryProvider.getInntektArbeidYtelseRepository()
            .hentAggregat(behandling, skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
        List<Ytelse> sammenhengendeYtelser = opptjeningInntektArbeidYtelseTjeneste
            .hentSammenhengendeInfotrygdYtelserFørSkjæringstidspunktForOppjening(behandling);
        BeregningsgrunnlagFraTilstøtendeYtelse bgFraTY =
            mapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel.map(behandling, beregningsgrunnlag, inntektArbeidYtelseGrunnlag, sammenhengendeYtelser);

        TilstøtendeYtelse tilstøtendeYtelse = bgFraTY.getTilstøtendeYtelse();
        List<TilstøtendeYtelseAndel> tilstøtendeYtelseAndelList = tilstøtendeYtelse.getTilstøtendeYtelseAndelList();
        assertThat(tilstøtendeYtelseAndelList).hasSize(1);
        assertTilstøtendeYtelseAndel(tilstøtendeYtelseAndelList.get(0), MÅNEDSBELØP_TILSTØTENDE_YTELSE,
            no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType.MÅNEDLIG,
            Inntektskategori.ARBEIDSTAKER, no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL);
        assertThat(tilstøtendeYtelse.getDekningsgrad()).isEqualTo(Dekningsgrad.DEKNINGSGRAD_100);
    }

    @Test
    public void sjekkMappingMedFlereArbeidsforhold() {
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100,
            Arbeidskategori.ARBEIDSTAKER, virksomhetEntitet1, true, virksomhetEntitet2);
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = repositoryProvider.getInntektArbeidYtelseRepository()
            .hentAggregat(behandling, skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
        List<Ytelse> sammenhengendeYtelser = opptjeningInntektArbeidYtelseTjeneste
            .hentSammenhengendeInfotrygdYtelserFørSkjæringstidspunktForOppjening(behandling);
        BeregningsgrunnlagFraTilstøtendeYtelse bgFraTY =
            mapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel.map(behandling, beregningsgrunnlag, inntektArbeidYtelseGrunnlag, sammenhengendeYtelser);

        TilstøtendeYtelse tilstøtendeYtelse = bgFraTY.getTilstøtendeYtelse();
        List<TilstøtendeYtelseAndel> tilstøtendeYtelseAndelList = tilstøtendeYtelse.getTilstøtendeYtelseAndelList();
        assertThat(tilstøtendeYtelseAndelList).hasSize(2);
        assertTilstøtendeYtelseAndel(tilstøtendeYtelseAndelList.get(0), ÅRSSBELØP_TILSTØTENDE_YTELSE_2,
            no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType.FASTSETT25PAVVIK,
            Inntektskategori.ARBEIDSTAKER, no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL);
        assertTilstøtendeYtelseAndel(tilstøtendeYtelseAndelList.get(1), MÅNEDSBELØP_TILSTØTENDE_YTELSE,
            no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType.MÅNEDLIG,
            Inntektskategori.ARBEIDSTAKER, no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL);
        assertThat(tilstøtendeYtelse.getDekningsgrad()).isEqualTo(Dekningsgrad.DEKNINGSGRAD_100);
    }

    @Test(expected = IllegalStateException.class)
    public void sjekkTomSammenhengendeYtelserThrowException() {
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100,
            Arbeidskategori.ARBEIDSTAKER, virksomhetEntitet1, true, virksomhetEntitet2);
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = repositoryProvider.getInntektArbeidYtelseRepository()
            .hentAggregat(behandling, skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
        mapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel.map(behandling, beregningsgrunnlag, inntektArbeidYtelseGrunnlag, Collections.emptyList());
    }

    private void assertTilstøtendeYtelseAndel(TilstøtendeYtelseAndel tilstøtendeYtelseAndel, BigDecimal beløp,
                                              no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType hyppighet,
                                              Inntektskategori inntektskategori, no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus aktivitetStatus) {
        assertThat(tilstøtendeYtelseAndel.getBeløp()).isEqualTo(beløp);
        assertThat(tilstøtendeYtelseAndel.getHyppighet()).isEqualTo(hyppighet);
        assertThat(tilstøtendeYtelseAndel.getInntektskategori()).isEqualTo(inntektskategori);
        assertThat(tilstøtendeYtelseAndel.getAktivitetStatus()).isEqualTo(aktivitetStatus);
    }

    private void mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType relatertYtelseType, int dekningsgrad, Arbeidskategori arbeidskategori, VirksomhetEntitet virksomhet,
                                                    boolean medEkstraAndel, Virksomhet virksomhet2) {
        mockTidligereYtelse(scenario, relatertYtelseType, dekningsgrad, arbeidskategori, virksomhet, medEkstraAndel, virksomhet2);
        Beregningsgrunnlag.Builder beregningsgrunnlagBuilder = scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
                .medAktivitetStatus(AktivitetStatus.TILSTØTENDE_YTELSE))
            .leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(LocalDate.now(), null));
        beregningsgrunnlag = beregningsgrunnlagBuilder.build();
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenario.mockBehandlingRepositoryProvider();
        this.repositoryProvider = providerTuple.getElement1();
        when(this.repositoryProvider.getVirksomhetRepository()).thenReturn(realRepositoryProvider.getVirksomhetRepository());
        ResultatRepositoryProvider resultatRepositoryProvider = providerTuple.getElement2();
        mockOpptjeningRepository(resultatRepositoryProvider, SKJÆRINGSTIDSPUNKT);
        behandling = scenario.lagMocked();
        AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(this.repositoryProvider, resultatRepositoryProvider, skjæringstidspunktTjeneste);
        inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(this.repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
        OpptjeningsperioderTjeneste periodeTjeneste = mock(OpptjeningsperioderTjeneste.class);
        opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, resultatRepositoryProvider, periodeTjeneste);
    }

    private void mockTidligereYtelse(ScenarioMorSøkerForeldrepenger scenario, RelatertYtelseType relatertYtelseType,
                                     int dekningsgrad, Arbeidskategori arbeidskategori, VirksomhetEntitet virksomhet,
                                     boolean medEkstraAndel, Virksomhet virksomhet2) {
        YtelseBuilder ytelseBuilder = YtelseBuilder.oppdatere(Optional.empty())
            .medYtelseType(relatertYtelseType)
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusDays(6), SKJÆRINGSTIDSPUNKT.plusDays(8)))
            .medKilde(Fagsystem.INFOTRYGD);
        YtelseStørrelse ytelseStørrelse = YtelseStørrelseBuilder.ny()
            .medBeløp(MÅNEDSBELØP_TILSTØTENDE_YTELSE)
            .medHyppighet(InntektPeriodeType.MÅNEDLIG)
            .medVirksomhet(virksomhet)
            .build();
        YtelseGrunnlagBuilder ytelseGrunnlagBuilder = ytelseBuilder.getGrunnlagBuilder()
            .medArbeidskategori(arbeidskategori);
        if (medEkstraAndel) {
            YtelseStørrelse ekstraYtelseStørrelse = YtelseStørrelseBuilder.ny()
                .medBeløp(ÅRSSBELØP_TILSTØTENDE_YTELSE_2)
                .medHyppighet(InntektPeriodeType.FASTSATT25PAVVIK)
                .medVirksomhet(virksomhet2)
                .build();
            ytelseGrunnlagBuilder.medYtelseStørrelse(ekstraYtelseStørrelse);
        }
        ytelseGrunnlagBuilder.medYtelseStørrelse(ytelseStørrelse);
        if (RelatertYtelseType.FORELDREPENGER.equals(relatertYtelseType)) {
            ytelseGrunnlagBuilder.medDekningsgradProsent(BigDecimal.valueOf(dekningsgrad));
        }
        if (RelatertYtelseType.SYKEPENGER.equals(relatertYtelseType)) {
            ytelseGrunnlagBuilder.medInntektsgrunnlagProsent(BigDecimal.valueOf(dekningsgrad));
        }
        YtelseGrunnlag ytelseGrunnlag = ytelseGrunnlagBuilder
            .build();
        ytelseBuilder.medYtelseGrunnlag(ytelseGrunnlag);
        scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd().leggTilAktørYtelse(
            InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty())
                .leggTilYtelse(ytelseBuilder)
                .medAktørId(AKTØR_ID)
        );
    }

    private void mockOpptjeningRepository(ResultatRepositoryProvider repositoryProvider, LocalDate opptjeningTom) {
        OpptjeningRepository opptjeningRepository = mock(OpptjeningRepository.class);
        Opptjening opptjening = mock(Opptjening.class);
        when(opptjening.getTom()).thenReturn(opptjeningTom);
        when(opptjeningRepository.finnOpptjening(any(Behandlingsresultat.class))).thenReturn(Optional.of(opptjening));
        when(repositoryProvider.getOpptjeningRepository()).thenReturn(opptjeningRepository);
    }
}
