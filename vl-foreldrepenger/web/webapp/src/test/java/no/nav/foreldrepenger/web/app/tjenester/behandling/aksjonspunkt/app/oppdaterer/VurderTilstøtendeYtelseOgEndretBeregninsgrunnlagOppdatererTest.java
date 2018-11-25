package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAndeltype;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.impl.ArbeidsgiverHistorikkinnslagTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsattBeløpTilstøtendeYtelseAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.RedigerbarAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.TilstotendeYtelseOgEndretBeregningsgrunnlagDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapterImpl;
import no.nav.foreldrepenger.web.app.tjenester.historikk.dto.HistorikkInnslagKonverter;

public class VurderTilstøtendeYtelseOgEndretBeregninsgrunnlagOppdatererTest {

    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = new Beløp(600000);
    private static final String ORGNR = "7887897435973";
    private static final String ARBEIDSFORHOLD_ID = "33332fess3";


    private VurderTilstøtendeYtelseOgEndretBeregninsgrunnlagOppdaterer oppdaterer;

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private HistorikkTjenesteAdapter historikkAdapter = new HistorikkTjenesteAdapterImpl(
        new HistorikkRepositoryImpl(repositoryRule.getEntityManager()), new HistorikkInnslagKonverter(
        repositoryProvider.getKodeverkRepository(), repositoryProvider.getAksjonspunktRepository()));
    private ScenarioMorSøkerForeldrepenger scenario;
    private Behandling behandling;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private Arbeidsgiver virksomheten;
    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;


    @Before
    public void setUp() {
        beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        BeregningArbeidsgiverTestUtil virksomhetTestUtil = new BeregningArbeidsgiverTestUtil(repositoryProvider.getVirksomhetRepository());
        this.virksomheten = virksomhetTestUtil.forArbeidsgiverVirksomhet(ORGNR);
        this.scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        this.behandling = scenario.lagre(repositoryProvider);
        this.arbeidsgiverHistorikkinnslagTjeneste = new ArbeidsgiverHistorikkinnslagTjenesteImpl(null);
        this.oppdaterer = new VurderTilstøtendeYtelseOgEndretBeregninsgrunnlagOppdaterer(repositoryProvider, historikkAdapter, arbeidsgiverHistorikkinnslagTjeneste);
    }

    @Test(expected = IllegalArgumentException.class)
    public void skalKasteExceptionOmIngenPerioder() {
        TilstotendeYtelseOgEndretBeregningsgrunnlagDto dto = new TilstotendeYtelseOgEndretBeregningsgrunnlagDto(Collections.emptyList(), false);
        oppdaterer.oppdater(dto, behandling, null);
    }

    @Test
    public void skalSetteManglendePerioderLikVerdierIFørstePeriode() {
        long andelsnr = 1L;
        RedigerbarAndelDto data = lagDataForEksisterendeAndel(andelsnr, false);

        Inntektskategori inntektskategori1 = Inntektskategori.SJØMANN;
        int fastsattBeløp1 = 900000;
        int refusjon1 = 10000;
        double reduserendeFaktor = 0.8;
        FastsattBeløpTilstøtendeYtelseAndelDto andel1 = new FastsattBeløpTilstøtendeYtelseAndelDto(data, fastsattBeløp1,
            refusjon1, inntektskategori1, reduserendeFaktor);
        LocalDate periode1Tom = SKJÆRINGSTIDSPUNKT.plusMonths(2);
        TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto periodeDto1 =
            new TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto(Collections.singletonList(andel1),
                SKJÆRINGSTIDSPUNKT, periode1Tom);

        Inntektskategori inntektskategori2 = Inntektskategori.SJØMANN;
        int fastsattBeløp2 = 800000;
        int refusjon2 = 20000;
        FastsattBeløpTilstøtendeYtelseAndelDto andel2 = new FastsattBeløpTilstøtendeYtelseAndelDto(data, fastsattBeløp2,
            refusjon2, inntektskategori2, reduserendeFaktor);
        LocalDate periode3Fom = SKJÆRINGSTIDSPUNKT.plusMonths(5);
        TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto periodeDto2 =
            new TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto(Collections.singletonList(andel2),
                periode3Fom, null);
        TilstotendeYtelseOgEndretBeregningsgrunnlagDto dto = new TilstotendeYtelseOgEndretBeregningsgrunnlagDto(Arrays.asList(periodeDto1, periodeDto2), false);
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode1 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, periode1Tom)
            .build(beregningsgrunnlag);
        byggAndel(ARBEIDSFORHOLD_ID, andelsnr, periode1, Inntektskategori.ARBEIDSTAKER);
        BeregningsgrunnlagPeriode periode2 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(periode1Tom.plusDays(1), periode3Fom.minusDays(1))
            .build(beregningsgrunnlag);
        byggAndel(ARBEIDSFORHOLD_ID, andelsnr, periode2, Inntektskategori.ARBEIDSTAKER);
        BeregningsgrunnlagPeriode periode3 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(periode3Fom, null)
            .build(beregningsgrunnlag);
        byggAndel(ARBEIDSFORHOLD_ID, andelsnr, periode3, Inntektskategori.ARBEIDSTAKER);

        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);

        // Act
        Beregningsgrunnlag nyttBg = beregningsgrunnlag.dypKopi();
        oppdaterer.oppdater(dto, behandling, nyttBg);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = nyttBg.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        List<BeregningsgrunnlagPrStatusOgAndel> andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(1);
        assertAndel(inntektskategori1, fastsattBeløp1, refusjon1, andelerIFørstePeriode.get(0), AktivitetStatus.ARBEIDSTAKER, reduserendeFaktor);

        List<BeregningsgrunnlagPrStatusOgAndel> andelerIAndrePeriode = perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIAndrePeriode).hasSize(1);
        assertAndel(inntektskategori1, fastsattBeløp1, refusjon1, andelerIAndrePeriode.get(0), AktivitetStatus.ARBEIDSTAKER, reduserendeFaktor);

        List<BeregningsgrunnlagPrStatusOgAndel> andelerITredjePeriode = perioder.get(2).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerITredjePeriode).hasSize(1);
        assertAndel(inntektskategori2, fastsattBeløp2, refusjon2, andelerITredjePeriode.get(0), AktivitetStatus.ARBEIDSTAKER, reduserendeFaktor);

        // Assert historikkinnslag
        HashMap<Long, String> andelsInfoMap = new HashMap<>();
        andelsInfoMap.put(andelsnr, "Beregningvirksomhet (7887897435973) ...ess3");
        HashMap<Long, String> navnVerdiMap = new HashMap<>();
        navnVerdiMap.put(andelsnr, finnInntektskategori(inntektskategori1).getNavn());
        HashMap<Long, BigDecimal> fastsattMap = new HashMap<>();
        fastsattMap.put(andelsnr, BigDecimal.valueOf(fastsattBeløp1));
        HashMap<Long, Boolean> nyAndelMap = new HashMap<>();
        nyAndelMap.put(andelsnr, Boolean.FALSE);
        HashMap<Long, Inntektskategori> inntektskategoriMap = new HashMap<>();
        inntektskategoriMap.put(andelsnr, finnInntektskategori(inntektskategori1));
        assertHistorikkinnslagInntektskategori(behandling, inntektskategoriMap, andelsInfoMap, nyAndelMap);
        assertHistorikkinnslagNyFordeling(behandling, fastsattMap, andelsInfoMap, navnVerdiMap, nyAndelMap);
    }

    private void byggAndel(String arbeidsforholdId, long andelsnr, BeregningsgrunnlagPeriode periode3, Inntektskategori inntektskategori) {
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(virksomheten).medArbforholdRef(arbeidsforholdId).medRefusjonskravPrÅr(BigDecimal.valueOf(50000)))
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode3);
    }

    @Test
    public void skalSetteVerdierPåAndelerOmKunEinPeriode() {
        // Arrange
        RedigerbarAndelDto egenNæringAndel = lagAndelTypeData(BeregningsgrunnlagAndeltype.EGEN_NÆRING, true);
        int fastsattBeløp1 = 100000;
        FastsattBeløpTilstøtendeYtelseAndelDto andel1 = new FastsattBeløpTilstøtendeYtelseAndelDto(egenNæringAndel, fastsattBeløp1,
            null,  Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, 1.0);
        long andelsnr = 1L;
        RedigerbarAndelDto data = lagDataForEksisterendeAndel(andelsnr, false);
        Inntektskategori inntektskategori2 = Inntektskategori.SJØMANN;
        int fastsattBeløp2 = 900000;
        int refusjon2 = 10000;
        FastsattBeløpTilstøtendeYtelseAndelDto andel2 = new FastsattBeløpTilstøtendeYtelseAndelDto(data, fastsattBeløp2,
            refusjon2, inntektskategori2,1.0);

        TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto periodeDto =
            new TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto(Arrays.asList(andel1, andel2), SKJÆRINGSTIDSPUNKT, null);
        TilstotendeYtelseOgEndretBeregningsgrunnlagDto dto = new TilstotendeYtelseOgEndretBeregningsgrunnlagDto(Collections.singletonList(periodeDto), false);

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode1 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(virksomheten).medArbforholdRef(ARBEIDSFORHOLD_ID).medRefusjonskravPrÅr(BigDecimal.valueOf(50000)))
            .medInntektskategori(inntektskategori2)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode1);
        byggAndel(ARBEIDSFORHOLD_ID, andelsnr, periode1, inntektskategori2);

        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);

        // Act
        Beregningsgrunnlag nyttBg = beregningsgrunnlag.dypKopi();
        oppdaterer.oppdater(dto, behandling, nyttBg);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = nyttBg.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(2);
        BeregningsgrunnlagPrStatusOgAndel nyAndel = andeler.stream().filter(a -> a.getLagtTilAvSaksbehandler()).findFirst().get();
        assertAndel(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, fastsattBeløp1, null,
            nyAndel, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, 1.0);
        BeregningsgrunnlagPrStatusOgAndel eksisterende = andeler.stream().filter(a -> !a.getLagtTilAvSaksbehandler()).findFirst().get();
        assertAndel(inntektskategori2, fastsattBeløp2, refusjon2, eksisterende, AktivitetStatus.ARBEIDSTAKER, 1.0);


        // Assert historikkinnslag
        HashMap<Long, String> andelsInfoMap = new HashMap<>();
        andelsInfoMap.put(nyAndel.getAndelsnr(), finnAndelType(BeregningsgrunnlagAndeltype.EGEN_NÆRING).getNavn());
        andelsInfoMap.put(andelsnr, "Beregningvirksomhet (7887897435973) ...ess3");
        HashMap<Long, String> navnVerdiMap = new HashMap<>();
        navnVerdiMap.put(nyAndel.getAndelsnr(),  finnInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE).getNavn());
        navnVerdiMap.put(andelsnr, finnInntektskategori(inntektskategori2).getNavn());
        HashMap<Long, BigDecimal> fastsattMap = new HashMap<>();
        fastsattMap.put(nyAndel.getAndelsnr(),  BigDecimal.valueOf(fastsattBeløp1));
        fastsattMap.put(andelsnr, BigDecimal.valueOf(fastsattBeløp2));
        HashMap<Long, Boolean> nyAndelMap = new HashMap<>();
        nyAndelMap.put(nyAndel.getAndelsnr(),  Boolean.TRUE);
        nyAndelMap.put(andelsnr, Boolean.FALSE);
        HashMap<Long, Inntektskategori> inntektskategoriMap = new HashMap<>();
        inntektskategoriMap.put(nyAndel.getAndelsnr(), finnInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE));
        inntektskategoriMap.put(andelsnr, finnInntektskategori(inntektskategori2));
        assertHistorikkinnslagInntektskategori(behandling, inntektskategoriMap, andelsInfoMap, nyAndelMap);
        assertHistorikkinnslagNyFordeling(behandling, fastsattMap, andelsInfoMap, navnVerdiMap, nyAndelMap);
    }

    private BeregningsgrunnlagAndeltype finnAndelType(BeregningsgrunnlagAndeltype andelType) {
        return repositoryProvider.getKodeverkRepository().finn(BeregningsgrunnlagAndeltype.class, andelType);
    }


    private Inntektskategori finnInntektskategori(Inntektskategori inntektskategori) {
        return repositoryProvider.getKodeverkRepository().finn(Inntektskategori.class, inntektskategori);
    }

    @Test
    public void skalKunneLeggeTilNyeAndelerForArbeidsforhold() {
        // Arrange
        long andelsnr = 1L;
        RedigerbarAndelDto data = lagDataForEksisterendeAndel(andelsnr, false);
        Inntektskategori inntektskategori2 = Inntektskategori.SJØMANN;
        int fastsattBeløp2 = 900000;
        int refusjon2 = 10000;
        FastsattBeløpTilstøtendeYtelseAndelDto andel2 = new FastsattBeløpTilstøtendeYtelseAndelDto(data, fastsattBeløp2,
            refusjon2, inntektskategori2, 1.0);
        RedigerbarAndelDto andelLagtTil = lagDataForNyAndel(andelsnr);
        int fastsattBeløp1 = 100000;
        Inntektskategori inntektskategori1 = Inntektskategori.ARBEIDSTAKER;
        FastsattBeløpTilstøtendeYtelseAndelDto andel1 = new FastsattBeløpTilstøtendeYtelseAndelDto(andelLagtTil, fastsattBeløp1,
            null, inntektskategori1, 1.0);
        TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto periodeDto =
            new TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto(Arrays.asList(andel1, andel2), SKJÆRINGSTIDSPUNKT, null);
        TilstotendeYtelseOgEndretBeregningsgrunnlagDto dto = new TilstotendeYtelseOgEndretBeregningsgrunnlagDto(Collections.singletonList(periodeDto), false);

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode1 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);
        byggAndel(ARBEIDSFORHOLD_ID, andelsnr, periode1, inntektskategori2);

        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);

        // Act
        Beregningsgrunnlag nyttBg = beregningsgrunnlag.dypKopi();
        oppdaterer.oppdater(dto, behandling, nyttBg);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = nyttBg.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(2);
        BeregningsgrunnlagPrStatusOgAndel nyAndel = andeler.stream().filter(a -> a.getLagtTilAvSaksbehandler()).findFirst().get();
        assertAndel(inntektskategori1, fastsattBeløp1, 0,
            nyAndel, AktivitetStatus.ARBEIDSTAKER, 1.0);
        assertThat(nyAndel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef().get().getReferanse()).isEqualTo(ARBEIDSFORHOLD_ID);
        BeregningsgrunnlagPrStatusOgAndel eksisterende = andeler.stream().filter(a -> !a.getLagtTilAvSaksbehandler()).findFirst().get();
        assertAndel(inntektskategori2, fastsattBeløp2, refusjon2, eksisterende, AktivitetStatus.ARBEIDSTAKER, 1.0);


        // Assert historikkinnslag
        HashMap<Long, String> andelsInfoMap = new HashMap<>();
        String andelsinfoNyAndel = "Beregningvirksomhet (7887897435973) ...ess3";
        String andelsinfoAndel = "Beregningvirksomhet (7887897435973) ...ess3";
        andelsInfoMap.put(nyAndel.getAndelsnr(), andelsinfoNyAndel);
        andelsInfoMap.put(andelsnr, andelsinfoAndel);
        HashMap<Long, String> navnVerdiMap = new HashMap<>();
        navnVerdiMap.put(nyAndel.getAndelsnr(),  finnInntektskategori(inntektskategori1).getNavn());
        navnVerdiMap.put(andelsnr, finnInntektskategori(inntektskategori2).getNavn());
        HashMap<Long, BigDecimal> fastsattMap = new HashMap<>();
        fastsattMap.put(nyAndel.getAndelsnr(),  BigDecimal.valueOf(fastsattBeløp1));
        fastsattMap.put(andelsnr, BigDecimal.valueOf(fastsattBeløp2));
        HashMap<Long, Boolean> nyAndelMap = new HashMap<>();
        nyAndelMap.put(nyAndel.getAndelsnr(),  Boolean.TRUE);
        nyAndelMap.put(andelsnr, Boolean.FALSE);
        HashMap<Long, Inntektskategori> inntektskategoriMap = new HashMap<>();
        inntektskategoriMap.put(nyAndel.getAndelsnr(), finnInntektskategori(inntektskategori1));
        inntektskategoriMap.put(andelsnr, finnInntektskategori(inntektskategori2));
        assertHistorikkinnslagInntektskategori(behandling, inntektskategoriMap, andelsInfoMap, nyAndelMap);
        assertHistorikkinnslagNyFordeling(behandling, fastsattMap, andelsInfoMap, navnVerdiMap, nyAndelMap);
    }

    // Scenario:
    // Har 1 arbeidstakerandel fra infotrygd med inntektskategori SJØMANN
    // Har lagt til 1 arbeidstakerandel for arbeidsforholdet fra infotrygd med inntektskategori ARBEIDSTAKER
    // Har lagt til 2 BRUKERS ANDEL med inntektskategori DAGPENGER og ARBEIDSAVKLARINGSPENGER
    // Informasjonen over er lagt til i forrige faktaavklaring
    // Bekrefter så aksjonspunkt på nytt etter å ha lagt til ein BRUKERS ANDEL med inntektskategori SJØMANN
    @Test
    public void skalKunneBekrefteAksjonspunktPåNytt() {
        // Arrange
        long andelsnr = 1L;
        RedigerbarAndelDto data = lagDataForEksisterendeAndel(andelsnr, false);
        Inntektskategori inntektskategori2 = Inntektskategori.SJØMANN;
        int fastsattBeløp2 = 900000;
        int refusjon2 = 10000;
        FastsattBeløpTilstøtendeYtelseAndelDto andel2 = new FastsattBeløpTilstøtendeYtelseAndelDto(data, fastsattBeløp2,
            refusjon2, inntektskategori2, 1.0);
        long andelsnr_lagt_til_forrige = 2L;
        RedigerbarAndelDto andelLagtTil = lagDataForEksisterendeAndel(andelsnr_lagt_til_forrige, true);
        int fastsattBeløp1 = 100000;
        Inntektskategori inntektskategori_lagt_til_forrige = Inntektskategori.ARBEIDSTAKER;
        FastsattBeløpTilstøtendeYtelseAndelDto andel1 = new FastsattBeløpTilstøtendeYtelseAndelDto(andelLagtTil, fastsattBeløp1,
            null, inntektskategori_lagt_til_forrige, 1.0);

        RedigerbarAndelDto brukers_andel_data1 = lagAndelTypeData(BeregningsgrunnlagAndeltype.BRUKERS_ANDEL, true);
        long andelsnr_eksisterende_ba1 = 3L;
        RedigerbarAndelDto brukers_andel_data2 = lagDataForEksisterendeBrukersAndel(andelsnr_eksisterende_ba1);
        long andelsnr_eksisterende_ba2 = 4L;
        RedigerbarAndelDto brukers_andel_data3 = lagDataForEksisterendeBrukersAndel(andelsnr_eksisterende_ba2);
        Inntektskategori inntektskategori_brukers_andel1 = Inntektskategori.SJØMANN;
        Inntektskategori inntektskategori_brukers_andel2 = Inntektskategori.DAGPENGER;
        Inntektskategori inntektskategori_brukers_andel3 = Inntektskategori.ARBEIDSAVKLARINGSPENGER;
        int fastsattBeløp_brukers_andel = 20000;
        int refusjon_brukers_andel = 0;
        FastsattBeløpTilstøtendeYtelseAndelDto brukers_andel1 = new FastsattBeløpTilstøtendeYtelseAndelDto(brukers_andel_data1, fastsattBeløp_brukers_andel,
            refusjon_brukers_andel, inntektskategori_brukers_andel1, 1.0);
        FastsattBeløpTilstøtendeYtelseAndelDto brukers_andel2 = new FastsattBeløpTilstøtendeYtelseAndelDto(brukers_andel_data2, fastsattBeløp_brukers_andel,
            refusjon_brukers_andel, inntektskategori_brukers_andel2, 1.0);
        FastsattBeløpTilstøtendeYtelseAndelDto brukers_andel3 = new FastsattBeløpTilstøtendeYtelseAndelDto(brukers_andel_data3, fastsattBeløp_brukers_andel,
            refusjon_brukers_andel, inntektskategori_brukers_andel3, 1.0);

        TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto periodeDto =
            new TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto(Arrays.asList(andel1, andel2, brukers_andel1, brukers_andel2, brukers_andel3), SKJÆRINGSTIDSPUNKT, null);
        TilstotendeYtelseOgEndretBeregningsgrunnlagDto dto = new TilstotendeYtelseOgEndretBeregningsgrunnlagDto(Collections.singletonList(periodeDto), false);

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode1 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);
        byggAndel(ARBEIDSFORHOLD_ID, andelsnr, periode1, inntektskategori2);


        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);

        Beregningsgrunnlag bg_kofakber_ut = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        BeregningsgrunnlagPeriode periode_kofakber_ut = bg_kofakber_ut.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr_lagt_til_forrige)
            .medLagtTilAvSaksbehandler(true)
            .medBeregnetPrÅr(BigDecimal.valueOf(100000))
            .medOverstyrtPrÅr(BigDecimal.valueOf(100000))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(ARBEIDSFORHOLD_ID).medRefusjonskravPrÅr(BigDecimal.valueOf(0)))
            .medInntektskategori(inntektskategori_lagt_til_forrige)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode_kofakber_ut);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr_eksisterende_ba1)
            .medLagtTilAvSaksbehandler(true)
            .medBeregnetPrÅr(BigDecimal.valueOf(1000))
            .medOverstyrtPrÅr(BigDecimal.valueOf(1000))
            .medInntektskategori(inntektskategori_brukers_andel2)
            .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
            .build(periode_kofakber_ut);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr_eksisterende_ba2)
            .medLagtTilAvSaksbehandler(true)
            .medOverstyrtPrÅr(BigDecimal.valueOf(1500))
            .medOverstyrtPrÅr(BigDecimal.valueOf(1500))
            .medInntektskategori(inntektskategori_brukers_andel3)
            .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
            .build(periode_kofakber_ut);
        beregningsgrunnlagRepository.lagre(behandling, bg_kofakber_ut, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Act
        Beregningsgrunnlag nyttBg = beregningsgrunnlag.dypKopi();
        oppdaterer.oppdater(dto, behandling, nyttBg);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = nyttBg.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(5);
        BeregningsgrunnlagPrStatusOgAndel nyArbeidstakerAndel = andeler.stream().filter(a -> a.getAndelsnr().equals(andelsnr_lagt_til_forrige)).findFirst().get();
        assertAndel(Inntektskategori.ARBEIDSTAKER, fastsattBeløp1, 0,
            nyArbeidstakerAndel, AktivitetStatus.ARBEIDSTAKER, 1.0);
        assertThat(nyArbeidstakerAndel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef().get().getReferanse()).isEqualTo(ARBEIDSFORHOLD_ID);
        BeregningsgrunnlagPrStatusOgAndel eksisterende = andeler.stream().filter(a -> !a.getLagtTilAvSaksbehandler()).findFirst().get();
        assertAndel(inntektskategori2, fastsattBeløp2, refusjon2, eksisterende, AktivitetStatus.ARBEIDSTAKER, 1.0);
        // Assert brukers andel
        List<BeregningsgrunnlagPrStatusOgAndel> brukersAndeler = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.BRUKERS_ANDEL)).collect(Collectors.toList());
        assertThat(brukersAndeler).hasSize(3);
        assertThat(brukersAndeler.stream().anyMatch(ba -> ba.getInntektskategori().equals(inntektskategori_brukers_andel1))).isTrue();
        assertThat(brukersAndeler.stream().anyMatch(ba -> ba.getInntektskategori().equals(inntektskategori_brukers_andel2))).isTrue();
        assertThat(brukersAndeler.stream().anyMatch(ba -> ba.getInntektskategori().equals(inntektskategori_brukers_andel3))).isTrue();
        brukersAndeler.forEach(brukersAndel -> {
            assertThat(brukersAndel.getLagtTilAvSaksbehandler()).isTrue();
            assertThat(brukersAndel.getFastsattAvSaksbehandler()).isTrue();
            assertThat(brukersAndel.getBgAndelArbeidsforhold().isPresent()).isFalse();
            assertThat(brukersAndel.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsattBeløp_brukers_andel));
            assertThat(brukersAndel.getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsattBeløp_brukers_andel));
        });
    }

    private RedigerbarAndelDto lagAndelTypeData(BeregningsgrunnlagAndeltype andeltype, boolean nyAndel) {
        return new RedigerbarAndelDto(andeltype.getKode(), nyAndel,
            null, null, true);
    }

    private RedigerbarAndelDto lagDataForEksisterendeBrukersAndel(Long andelsnr) {
        return new RedigerbarAndelDto("Brukers andel", false,
            null, andelsnr, true);
    }

    private RedigerbarAndelDto lagDataForEksisterendeAndel(Long andelsnr, boolean lagtTilAvSaksbehandler) {
        return new RedigerbarAndelDto("Virksomhetsnavn (123123214)", false,
            ARBEIDSFORHOLD_ID, andelsnr, lagtTilAvSaksbehandler);
    }

    private RedigerbarAndelDto lagDataForNyAndel(Long andelsnr) {
        return new RedigerbarAndelDto("Virksomhetsnavn (123123214)", true,
            null, andelsnr, true);
    }

    private void assertAndel(Inntektskategori inntektskategori, int fastsattBeløp, Integer refusjon, BeregningsgrunnlagPrStatusOgAndel andel, AktivitetStatus aktivitetStatus, double reduserendeFaktor) {
        assertThat(andel.getAktivitetStatus()).isEqualByComparingTo(aktivitetStatus);
        assertThat(andel.getInntektskategori()).isEqualByComparingTo(inntektskategori);
        if (refusjon != null) {
            assertThat(andel.getBgAndelArbeidsforhold().get().getRefusjonskravPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(refusjon));
        }
        assertThat(andel.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsattBeløp));
        assertThat(andel.getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsattBeløp*reduserendeFaktor));
        assertThat(andel.getFastsattAvSaksbehandler()).isEqualTo(true);
    }


    private void assertHistorikkinnslagInntektskategori(Behandling behandling, Map<Long, Inntektskategori> inntektskategoriMap, Map<Long, String> andelsInfoMap, Map<Long, Boolean> nyAndelMap) {
        List<HistorikkinnslagDel> deler = historikkAdapter.tekstBuilder().getHistorikkinnslagDeler();
        Map<Long, Optional<Inntektskategori>> inntektskategoriForrigeMap = finnInntektskategoriForrige(behandling, inntektskategoriMap.keySet());
        inntektskategoriMap.forEach((key, value) -> {
            Optional<Inntektskategori> inntektskategoriForrige = inntektskategoriForrigeMap.get(key);
            HistorikkEndretFeltType endretFeltType = HistorikkEndretFeltType.INNTEKTSKATEGORI_FOR_ANDEL;
            List<HistorikkinnslagDel> inntektskategoriInnslag = deler.stream().filter(del -> del.getEndretFelt(endretFeltType).isPresent() &&
                del.getEndretFelt(endretFeltType).get().getNavnVerdi().equals(andelsInfoMap.get(key)))
                .collect(Collectors.toList());
            if (nyAndelMap.get(key) || (inntektskategoriForrige.isPresent() && inntektskategoriForrige.get().compareTo(value) == 0)) {
                assertThat(inntektskategoriInnslag.isEmpty() || inntektskategoriInnslag.stream().noneMatch(
                    innslag -> innslag.getEndretFelt(endretFeltType).get().getTilVerdi().equals(value.getKode()))).isTrue();
            } else if (!inntektskategoriForrige.isPresent()) {
                assertThat(inntektskategoriInnslag.size()).isEqualTo(1);
                assertThat(inntektskategoriInnslag.get(0).getEndretFelt(endretFeltType).get().getFraVerdi()).isNull();
                assertThat(inntektskategoriInnslag.get(0).getEndretFelt(endretFeltType).get().getTilVerdi()).isEqualTo(value.getKode());
            } else {
                assertThat(inntektskategoriInnslag.size()).isEqualTo(1);
                assertThat(inntektskategoriInnslag.get(0).getEndretFelt(endretFeltType).get().getFraVerdi()).isEqualTo(inntektskategoriForrige.get().getKode());
                assertThat(inntektskategoriInnslag.get(0).getEndretFelt(endretFeltType).get().getTilVerdi()).isEqualTo(value.getKode());
            }
        });
    }

    private Map<Long, Optional<Inntektskategori>> finnInntektskategoriForrige(Behandling behandling, Set<Long> andelsnr) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeBGEntitet = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        if (!forrigeBGEntitet.isPresent()) {
            return andelsnr.stream()
                .collect(Collectors.toMap(nr -> nr, nr -> Optional.empty()));
        }
        return andelsnr.stream()
            .collect(Collectors.toMap(nr -> nr, nr -> finnInntektskategoriForrigeForAndel(nr, forrigeBGEntitet.get().getBeregningsgrunnlag())));    }

    private void assertHistorikkinnslagNyFordeling(Behandling behandling, Map<Long, BigDecimal> fastsattMap, Map<Long, String> andelsInfoMap,
                                                   Map<Long, String> navnVerdimap, Map<Long, Boolean> nyAndelMap) {
        List<HistorikkinnslagDel> deler = historikkAdapter.tekstBuilder().getHistorikkinnslagDeler();
        Map<Long, Optional<BigDecimal>> fastsattForrigeMap = finnFastsattForrige(behandling, fastsattMap.keySet());
        fastsattMap.keySet().forEach(key -> {
            Optional<BigDecimal> fastsattForrige = fastsattForrigeMap.get(key);
            List<HistorikkinnslagDel> andelHistorikkinnslag = deler.stream().filter(del ->
                del != null &&
                    del.getTema().isPresent() &&
                    andelsInfoMap.get(key).equals(del.getTema().get().getNavnVerdi()))
                .collect(Collectors.toList());
            HistorikkEndretFeltType endretFeltType = nyAndelMap.get(key) ? HistorikkEndretFeltType.FORDELING_FOR_NY_ANDEL : HistorikkEndretFeltType.FORDELING_FOR_ANDEL;
            List<HistorikkinnslagDel> fordelingInnslag = andelHistorikkinnslag.stream().filter(del -> del.getEndretFelt(endretFeltType).isPresent() &&
            del.getEndretFelt(endretFeltType).get().getNavnVerdi().equals(navnVerdimap.get(key)))
                .collect(Collectors.toList());
            if (fastsattForrige.isPresent() && fastsattForrige.get().compareTo(fastsattMap.get(key)) == 0) {
                assertThat(fordelingInnslag.isEmpty()).isTrue();
            } else if (!fastsattForrige.isPresent()) {
                assertThat(fordelingInnslag.size()).isEqualTo(1);
                assertThat(fordelingInnslag.get(0).getEndretFelt(endretFeltType).get().getFraVerdi()).isNull();
                assertThat(fordelingInnslag.get(0).getEndretFelt(endretFeltType).get().getTilVerdi()).isEqualTo(fastsattMap.get(key).toString());
            } else {
                assertThat(fordelingInnslag.size()).isEqualTo(1);
                assertThat(fordelingInnslag.get(0).getEndretFelt(endretFeltType).get().getFraVerdi()).isEqualTo(fastsattForrige.get().toString());
                assertThat(fordelingInnslag.get(0).getEndretFelt(endretFeltType).get().getTilVerdi()).isEqualTo(fastsattMap.get(key).toString());
            }
        });
    }

    private Map<Long, Optional<BigDecimal>> finnFastsattForrige(Behandling behandling, Set<Long> andelsnr) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeBGEntitet = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        if (!forrigeBGEntitet.isPresent()) {
            return andelsnr.stream()
                .collect(Collectors.toMap(nr -> nr, nr -> Optional.empty()));
        }
        return andelsnr.stream()
            .collect(Collectors.toMap(nr -> nr, nr -> finnFastsattForrigeForAndel(nr, forrigeBGEntitet.get().getBeregningsgrunnlag())));
    }

    private Optional<BigDecimal> finnFastsattForrigeForAndel(long andelsnr, Beregningsgrunnlag forrigeBG) {
        return forrigeBG
            .getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> andel.getAndelsnr().equals(andelsnr))
            .map(BeregningsgrunnlagPrStatusOgAndel::getOverstyrtPrÅr)
            .findFirst();
    }

    private Optional<Inntektskategori> finnInntektskategoriForrigeForAndel(long andelsnr, Beregningsgrunnlag forrigeBG) {
        return forrigeBG
            .getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> andel.getAndelsnr().equals(andelsnr))
            .map(BeregningsgrunnlagPrStatusOgAndel::getInntektskategori)
            .findFirst();
    }


}
