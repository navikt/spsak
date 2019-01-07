package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class FastsettInntektskategoriFraSøknadTjenesteImplTest {


    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);

    private static final AktørId AKTØR_ID = new AktørId("210195");
    private static final String ARBEIDSFORHOLD_ORGNR = "123456780";

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = Mockito.spy(new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager()));
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private Behandling behandling;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
    private FastsettInntektskategoriFraSøknadTjeneste fastsettInntektskategoriFraSøknadTjeneste = new FastsettInntektskategoriFraSøknadTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste);
    private ScenarioMorSøkerForeldrepenger scenario;
    private VirksomhetEntitet beregningVirksomhet;


    @Before
    public void setup() {
        beregningVirksomhet = new VirksomhetEntitet.Builder()
                .medOrgnr(ARBEIDSFORHOLD_ORGNR)
                .medNavn("BeregningVirksomheten")
                .oppdatertOpplysningerNå()
                .build();
        scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(any())).thenReturn(SKJÆRINGSTIDSPUNKT_OPPTJENING);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(AktivitetStatus aktivitetStatus) {
        Beregningsgrunnlag beregningsgrunnlag = scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .build();
        BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(aktivitetStatus)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(Arbeidsgiver.virksomhet(beregningVirksomhet));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);
        return beregningsgrunnlag;
    }


    private void opprettOppgittOpptjening(List<VirksomhetType> næringtyper) {
        OppgittOpptjeningBuilder oob = OppgittOpptjeningBuilder.ny();
        ArrayList<OppgittOpptjeningBuilder.EgenNæringBuilder> egneNæringBuilders = new ArrayList<>();
        LocalDate fraOgMed = LocalDate.now().minusMonths(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed);
        for (VirksomhetType type : næringtyper) {
            egneNæringBuilders.add(OppgittOpptjeningBuilder.EgenNæringBuilder.ny().medVirksomhetType(type).medPeriode(periode));
        }
        oob.leggTilEgneNæringer(egneNæringBuilders);
        scenario.medOppgittOpptjening(oob);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    @Test
    public void arbeidstakerSkalTilRiktigInntektskategori() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    public void frilanserSkalTilRiktigInntektskategori() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.FRILANSER);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
    }


    @Test
    public void dagpengerSkalTilRiktigInntektskategori() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.DAGPENGER);


        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.DAGPENGER);
    }

    @Test
    public void arbeidsavklaringspengerSkalTilRiktigInntektskategori() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSAVKLARINGSPENGER);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSAVKLARINGSPENGER);
    }


    @Test
    public void SNUtenFiskeJordbrukEllerDagmammaSkalTilRiktigInntektskategori() {
        // Arrange
        opprettOppgittOpptjening(Collections.singletonList(VirksomhetType.ANNEN));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    @Test
    public void SNMedFiskeSkalTilRiktigInntektskategori() {
        // Arrange
        opprettOppgittOpptjening(Collections.singletonList(VirksomhetType.FISKE));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void SNMedJorbrukSkalTilRiktigInntektskategori() {
        // Arrange
        opprettOppgittOpptjening(Collections.singletonList(VirksomhetType.JORDBRUK_SKOGBRUK));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.JORDBRUKER);
    }

    @Test
    public void SNMedDagmammaSkalTilRiktigInntektskategori() {
        // Arrange
        opprettOppgittOpptjening(Collections.singletonList(VirksomhetType.DAGMAMMA));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.DAGMAMMA);
    }

    @Test
    public void SNMedFiskeOgJordbrukSkalMappeTilInntektskategoriFisker() {
        // Arrange
        opprettOppgittOpptjening(Arrays.asList(VirksomhetType.FISKE, VirksomhetType.JORDBRUK_SKOGBRUK));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void SNMedFiskeOgDagmammaSkalMappeTilInntektskategoriFisker() {
        // Arrange
        opprettOppgittOpptjening(Arrays.asList(VirksomhetType.DAGMAMMA, VirksomhetType.FISKE));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void SNMedJordbrukOgDagmammaSkalMappeTilInntektskategoriJordbruker() {
        // Arrange
        opprettOppgittOpptjening(Arrays.asList(VirksomhetType.DAGMAMMA, VirksomhetType.JORDBRUK_SKOGBRUK));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.JORDBRUKER);
    }

    @Test
    public void SNMedJordbrukOgOrdinærNæringSkalMappeTilInntektskategoriJordbruker() {
        // Arrange
        opprettOppgittOpptjening(Arrays.asList(VirksomhetType.ANNEN, VirksomhetType.JORDBRUK_SKOGBRUK));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.JORDBRUKER);
    }

    @Test
    public void SNMedDagmammaOgOrdinærNæringSkalMappeTilInntektskategoriJordbruker() {
        // Arrange
        opprettOppgittOpptjening(Arrays.asList(VirksomhetType.ANNEN, VirksomhetType.DAGMAMMA));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.DAGMAMMA);
    }

    @Test
    public void SNMedFiskeOgOrdinærNæringSkalMappeTilInntektskategoriFisker() {
        // Arrange
        opprettOppgittOpptjening(Arrays.asList(VirksomhetType.ANNEN, VirksomhetType.FISKE));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);

        // Assert
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void skalReturnereFiskerSomHøgastPrioriterteInntektskategori() {
        List<Inntektskategori> inntektskategoriList = Arrays.asList(Inntektskategori.FISKER, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.DAGMAMMA, Inntektskategori.JORDBRUKER);
        Optional<Inntektskategori> prioritert = fastsettInntektskategoriFraSøknadTjeneste.finnHøgastPrioriterteInntektskategoriForSN(inntektskategoriList);
        assertThat(prioritert.get()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void skalReturnereJordbrukerSomHøgastPrioriterteInntektskategori() {
        List<Inntektskategori> inntektskategoriList = Arrays.asList(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.DAGMAMMA, Inntektskategori.JORDBRUKER);
        Optional<Inntektskategori> prioritert = fastsettInntektskategoriFraSøknadTjeneste.finnHøgastPrioriterteInntektskategoriForSN(inntektskategoriList);
        assertThat(prioritert.get()).isEqualTo(Inntektskategori.JORDBRUKER);
    }

    @Test
    public void skalReturnereDagmammaSomHøgastPrioriterteInntektskategori() {
        List<Inntektskategori> inntektskategoriList = Arrays.asList(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.DAGMAMMA);
        Optional<Inntektskategori> prioritert = fastsettInntektskategoriFraSøknadTjeneste.finnHøgastPrioriterteInntektskategoriForSN(inntektskategoriList);
        assertThat(prioritert.get()).isEqualTo(Inntektskategori.DAGMAMMA);
    }

    @Test
    public void skalReturnereSelvstendigNæringsdrivendeSomHøgastPrioriterteInntektskategori() {
        List<Inntektskategori> inntektskategoriList = Arrays.asList(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        Optional<Inntektskategori> prioritert = fastsettInntektskategoriFraSøknadTjeneste.finnHøgastPrioriterteInntektskategoriForSN(inntektskategoriList);
        assertThat(prioritert.get()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
    }
}
