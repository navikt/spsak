package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.AksjonspunktUtlederForBeregning;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningsperiodeTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FaktaOmBeregningTilfelleTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningInntektsmeldingTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningOpptjeningTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningsgrunnlagTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class AksjonspunktUtlederForBeregningTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MARCH, 23);


    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private BehandlingRepositoryProvider repositoryProvider = Mockito.spy(new BehandlingRepositoryProviderImpl(entityManager));

    @Inject
    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;

    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;
    private BeregningsperiodeTjeneste beregningsperiodeTjeneste = mock(BeregningsperiodeTjeneste.class);
    private AksjonspunktRepository aksjonspunktRepository = mock(AksjonspunktRepository.class);
    private Behandling behandling;

    // Test utils
    @Inject
    private BeregningIAYTestUtil iayTestUtil;
    @Inject
    private BeregningsgrunnlagTestUtil beregningTestUtil;
    @Inject
    private BeregningOpptjeningTestUtil opptjeningTestUtil;
    @Inject
    private BeregningInntektsmeldingTestUtil inntektsmeldingTestUtil;
    @Inject
    private BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;


    @Before
    public void setup() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger
            .forFødselMedGittAktørId(AKTØR_ID);
        behandling = scenario.lagre(repositoryProvider);
        when(beregningsperiodeTjeneste.skalBehandlingSettesPåVent(behandling)).thenReturn(false);
        this.aksjonspunktUtlederForBeregning = new AksjonspunktUtlederForBeregning(aksjonspunktRepository,
            faktaOmBeregningTilfelleTjeneste, beregningsperiodeTjeneste);
        beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    }

    @Test
    public void skalUtledeAksjonspunktForTYIKombinasjon() {
        // Arrange
        String arbId = "213414";
        String orgnr = "8998242402";
        String arbId2 = "2242313414";
        String orgnr2 = "8923432242402";
        LocalDate graderingStart = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1);
        HashMap<String, Periode> opptjeningMap =  new HashMap<>();
        opptjeningMap.put(orgnr, Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 12));
        opptjeningMap.put(orgnr2, Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 12));
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningMap);
        iayTestUtil.lagOppgittOpptjeningForSN(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, true);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        List<List<PeriodeÅrsak>> periodeÅrsaker = Collections.singletonList(Collections.singletonList(PeriodeÅrsak.GRADERING));
        List<LocalDateInterval> perioder = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, graderingStart.minusDays(1)), new LocalDateInterval(graderingStart, null));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, perioder, periodeÅrsaker,
            AktivitetStatus.TILSTØTENDE_YTELSE, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        Gradering gradering = new GraderingEntitet(graderingStart, null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, 50000);

        // Act
        List<AksjonspunktResultat> resultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(resultat.size()).isEqualTo(1);
        assertThat(resultat.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling);
        List<FaktaOmBeregningTilfelle> tilfeller = bg.getFaktaOmBeregningTilfeller();
        assertThat(tilfeller.size()).isEqualTo(4);
        assertThat(tilfeller).containsExactlyInAnyOrder(FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE,
            FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD,
            FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET,
            FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
    }

    @Test
    public void skalUtledeAksjonspunktForTYIKombinasjonATFLSammeOrgLønnsendring() {
        // Arrange
        String arbId = "213414";
        String orgnr = "8998242402";
        String arbId3 = "76575";
        String orgnr3 = "567755757";
        HashMap<String, Periode> opptjeningMap =  new HashMap<>();
        opptjeningMap.put(orgnr, Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 12));
        opptjeningMap.put(orgnr3, Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 12));
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningMap);

        iayTestUtil.leggTilOppgittOpptjeningForFL(behandling, true);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), true);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10), arbId3, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr3));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING,SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), null, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr),
            ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER, false, BigDecimal.TEN, false);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, AktivitetStatus.TILSTØTENDE_YTELSE, AktivitetStatus.KOMBINERT_AT_FL);
        beregningTestUtil.leggTilFLTilknyttetOrganisasjon(behandling, orgnr3, arbId3);

        // Act
        List<AksjonspunktResultat> resultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(resultat.size()).isEqualTo(1);
        assertThat(resultat.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling);
        List<FaktaOmBeregningTilfelle> tilfeller = bg.getFaktaOmBeregningTilfeller();
        assertThat(tilfeller).containsExactlyInAnyOrder(FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE,
            FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON,
            FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING,
            FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL);
    }


    @Test
    public void skalUtledeAksjonspunktForFellesTilfeller() {
        // Act
        String arbId = "213414";
        String orgnr = "8998242402";
        iayTestUtil.lagOppgittOpptjeningForSN(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, true);
        HashMap<String, Periode> opptjeningMap =  new HashMap<>();
        opptjeningMap.put(orgnr, Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 12));
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING,SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, AktivitetStatus.KOMBINERT_AT_SN, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        List<AksjonspunktResultat> resultater = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(resultater.size()).isEqualTo(1);
        assertThat(resultater).anySatisfy(resultat ->
            assertThat(resultat.getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN)
        );
    }

    @Test
    public void skalSettePåVentNårFørRapporteringsfrist() {
        // Arrange
        when(beregningsperiodeTjeneste.skalBehandlingSettesPåVent(behandling)).thenReturn(true);
        LocalDate frist = LocalDate.of(2018, 10, 5);
        when(beregningsperiodeTjeneste.utledBehandlingPåVentFrist(behandling)).thenReturn(frist);

        //Act
        List<AksjonspunktResultat> resultater = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(resultater).hasSize(1);
        AksjonspunktResultat aksjonspunktResultat = resultater.get(0);
        assertThat(aksjonspunktResultat.getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.AUTO_VENT_PÅ_INNTEKT_RAPPORTERINGSFRIST);
        assertThat(aksjonspunktResultat.getAksjonspunktModifiserer()).isNotNull();

        aksjonspunktResultat.getAksjonspunktModifiserer().accept(null);
        ArgumentCaptor<LocalDateTime> localDateTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(aksjonspunktRepository).setFrist(any(), localDateTimeCaptor.capture(), eq(Venteårsak.VENT_INNTEKT_RAPPORTERINGSFRIST));
        assertThat(localDateTimeCaptor.getValue().toLocalDate()).isEqualTo(frist);
    }
}
