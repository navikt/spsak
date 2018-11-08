package no.nav.foreldrepenger.økonomistøtte;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;

public class OppdragskontrollTjenesteFeriepengerTest extends OppdragskontrollTjenesteImplFPBaseTest {

    private static final Long PROSESS_TASK_ID_2 = 89L;

    private final OppdragskontrollFeriepengerTestUtil feriepengerTestUtil = new OppdragskontrollFeriepengerTestUtil();

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void skalIkkeSendeOppdragForFeriepengerNårDetIkkeErEndringIÅrsbeløp() {
        //Arrange
        //Forrige behandling: Har feriepenger; Ny behandling: Har feriepenger; erBeløpForskjelligeForFPÅr1 = Nei, erBeløpForskjelligeForFPÅr2 = Nei
        //Førstegangsbehandling
        oppsettBeregningsresultatFP(true, 15000L, 10000L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 15000L, 10000L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        feriepengerTestUtil.verifiserOppdr150NårDetErEndringForToFeriepengeår(opp150FeriepengerListe, opp150RevurderingFeriepengerListe);
        assertThat(opp150RevurderingFeriepengerListe).isEmpty();
    }

    @Test
    public void skalSendeOppdragKunForAndreFeriepengeårSomHaddeEndringAvÅrsbeløpIRevurdering() {
        //Arrange
        //Forrige behandling: Har feriepenger; Ny behandling: Har feriepenger; erBeløpForskjelligeForFPÅr1 = Nei, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(true, 15000L, 10000L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 15000L, 11000L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        feriepengerTestUtil.verifiserOpp150NårEndringGjelderEttFeriepengeår(opp150FeriepengerListe, opp150RevurderingFeriepengerListe, false);
    }

    @Test
    public void skalSendeOppdragKunForFørsteFeriepengeårSomHaddeEndringAvÅrsbeløpIRevurdering() {
        // Arrange
        //Forrige behandling: Har feriepenger; Ny behandling: Har feriepenger; erBeløpForskjelligeForFPÅr1 = Ja, erBeløpForskjelligeForFPÅr2 = Nei
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(true, 9000L, 11000L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 7000L, 11000L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        feriepengerTestUtil.verifiserOpp150NårEndringGjelderEttFeriepengeår(opp150FeriepengerListe, opp150RevurderingFeriepengerListe, true);
    }

    @Test
    public void skalSendeOppdragForAlleFeriepengeårNårDetBlirEndringAvÅrsbeløpForAlleFeriepengeårIRevurdering() {
        //Arrange
        //Forrige behandling: Har feriepenger; Ny behandling: Har feriepenger; erBeløpForskjelligeForFPÅr1 = Ja, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(true, 6000L, 8000L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 9000L, 7000L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        for (Oppdragslinje150 forrigeOpp150 : opp150FeriepengerListe) {
            assertThat(opp150RevurderingFeriepengerListe).anySatisfy(oppdragslinje150 ->
                assertThat(oppdragslinje150.getRefDelytelseId()).isEqualTo(forrigeOpp150.getDelytelseId()));
        }
        feriepengerTestUtil.verifiserOppdr150NårDetErEndringForToFeriepengeår(opp150FeriepengerListe, opp150RevurderingFeriepengerListe);
        feriepengerTestUtil.verifiserFeriepengeår(opp150RevurderingFeriepengerListe);
    }

    @Test
    public void skalSendeOppdragForAlleFeriepengeårNårDetEksistererIngenFeriepengerIForrigeBehandling() {
        //Arrange
        //Forrige behandling: Har ikke feriepenger; Ny behandling: Har feriepenger for andre år; erBeløpForskjelligeForFPÅr1 = Nei, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(false, 0L, 0L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 11000L, 13000L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        assertThat(opp150FeriepengerListe).isEmpty();
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        feriepengerTestUtil.verifiserOppdr150NårDetErEndringForToFeriepengeår(opp150FeriepengerListe, opp150RevurderingFeriepengerListe);
        feriepengerTestUtil.verifiserFeriepengeår(opp150RevurderingFeriepengerListe);
    }

    @Test
    public void skalSendeOppdragKunForAndreFeriepengeårNårForrigeBehandlingIkkeHarFeriepengerOgRevurderingHarDetIAndreÅr() {
        //Arrange
        //Forrige behandling: Har ikke feriepenger; Ny behandling: Har feriepenger; erBeløpForskjelligeForFPÅr1 = Nei, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(false, 0L, 0L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(false, 0L, 13000L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        assertThat(opp150FeriepengerListe).isEmpty();
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        feriepengerTestUtil.verifiserOppdr150MedEttFeriepengeårKunIRevurdering(opp150RevurderingFeriepengerListe, false);
    }

    @Test
    public void skalSendeOppdragKunForFørsteFeriepengeårNårForrigeBehandlingIkkeHarFeriepengerOgRevurderingHarDetIFørsteÅr() {
        //Arrange
        //Forrige behandling: Har ikke feriepenger; Ny behandling: Har feriepenger; erBeløpForskjelligeForFPÅr1 = Nei, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(false, 0L, 0L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(false, 8000L, 0L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        assertThat(opp150FeriepengerListe).isEmpty();
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        feriepengerTestUtil.verifiserOppdr150MedEttFeriepengeårKunIRevurdering(opp150RevurderingFeriepengerListe, true);
    }

    @Test
    public void skalSendeOppdragForAlleFeriepengeårNårForrigeBehandlingIkkeHarFeriepengerForAndreÅrOgRevurderingHarDetForBeggeToÅr() {
        //Arrange
        //Forrige behandling: Har feriepenger for år1 - 2019; Ny behandling: Har feriepenger for år1 - 2019 og år2 - 2020;
        //ErBeløpForskjelligeForFPÅr1 = Ja, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(false, 6500L, 0L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 8000L, 7000L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);

        feriepengerTestUtil.verifiserRefDelytelseId(opp150FeriepengerListe, opp150RevurderingFeriepengerListe, true);
        feriepengerTestUtil.verifiserFeriepengeår(opp150RevurderingFeriepengerListe);
        assertThat(opp150RevurderingFeriepengerListe).anySatisfy(oppdragslinje150 -> {
            assertThat(oppdragslinje150.getRefDelytelseId()).isNull();
            assertThat(oppdragslinje150.getRefDelytelseId()).isNull();
        });
    }

    @Test
    public void skalSendeOppdragKunForAndreFeriepengeårNårForrigeBehandlingHarFeriepengerForFørsteÅrOgRevurderingHarDetForBeggeToÅrUtenEndringForFørsteÅr() {
        //Arrange
        //Forrige behandling: Har feriepenger for år1 - 2019; Ny behandling: Har feriepenger for år1 - 2019 og år2 - 2020;
        // ErBeløpForskjelligeForFPÅr1 = Nei, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(false, 6500L, 0L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 6500L, 7000L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        int ikkeEndretFeriepengeår = feriepengeårListe.get(0);
        int nyFeriepengeår = feriepengeårListe.get(1);
        assertThat(opp150FeriepengerListe).allSatisfy(opp150 ->
            assertThat(opp150.getDatoVedtakFom().getYear()).isNotEqualTo(nyFeriepengeår));
        assertThat(opp150RevurderingFeriepengerListe).allSatisfy(opp150 ->
            assertThat(opp150.getDatoVedtakFom().getYear()).isNotEqualTo(ikkeEndretFeriepengeår));
    }

    @Test
    public void skalSendeOppdragForAlleFeriepengeårNårForrigeBehandlingIkkeHarFeriepengerForFørsteÅrOgRevurderingHarDetForBeggeToÅr() {
        //Arrange
        //Forrige behandling: Har feriepenger for år2 - 2020; Ny behandling: Har feriepenger for år1 - 2019 og år2 - 2020;
        // ErBeløpForskjelligeForFPÅr1 = Ja, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(false, 0L, 7500L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 9500L, 7499L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        feriepengerTestUtil.verifiserRefDelytelseId(opp150FeriepengerListe, opp150RevurderingFeriepengerListe, false);
        feriepengerTestUtil.verifiserFeriepengeår(opp150RevurderingFeriepengerListe);
        assertThat(opp150RevurderingFeriepengerListe).anySatisfy(oppdragslinje150 -> {
            assertThat(oppdragslinje150.getRefDelytelseId()).isNull();
            assertThat(oppdragslinje150.getRefDelytelseId()).isNull();
        });
    }

    @Test
    public void skalSendeOppdragKunForFørsteFeriepengeårNårForrigeBehandlingHarFeriepengerForFørsteÅrOgRevurderingHarDetForBeggeToÅrUtenEndringForAndreÅr() {
        //Arrange
        //Forrige behandling: Har feriepenger for år2 - 2020; Ny behandling: Har feriepenger for år1 - 2019 og år2 - 2020;
        // ErBeløpForskjelligeForFPÅr1 = Ja, erBeløpForskjelligeForFPÅr2 = Nei
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(false, 0L, 7000L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 6500L, 7000L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        int nyFeriepengeår = feriepengeårListe.get(0);
        int ikkeEndretFeriepengeår = feriepengeårListe.get(1);
        assertThat(opp150FeriepengerListe).allSatisfy(opp150 ->
            assertThat(opp150.getDatoVedtakFom().getYear()).isNotEqualTo(nyFeriepengeår));
        assertThat(opp150RevurderingFeriepengerListe).allSatisfy(opp150 ->
            assertThat(opp150.getDatoVedtakFom().getYear()).isNotEqualTo(ikkeEndretFeriepengeår));
    }

    @Test
    public void skalSendeEnOppdragForOpphørOgEnForEndringNårFørsteFeriepengeårEndrerSegOgAndreFeriepengeårOpphørerIRevurdering() {
        //Arrange
        //Forrige behandling: Har feriepenger for år1 - 2019 og år2 - 2020; Ny behandling: Har feriepenger for år1 - 2019;
        //ErBeløpForskjelligeForFPÅr1 = Ja, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(true, 12000L, 10000L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 13000L, 0L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);

        assertThat(opp150RevurderingFeriepengerListe).anySatisfy(opp150 -> assertThat(opp150.gjelderOpphør()).isTrue());
        feriepengerTestUtil.verifiserFeriepengeår(opp150RevurderingFeriepengerListe);
        feriepengerTestUtil.verifiserOppdr150NårEttFeriepengeårSkalOpphøre(opp150FeriepengerListe, opp150RevurderingFeriepengerListe, false);
    }

    @Test
    public void skalSendeKunEnOppdragForOpphørNårFørsteFeriepengeårIkkeEndrerSegOgAndreFeriepengeårOpphørerIRevurdering() {
        //Arrange
        //Forrige behandling: Har feriepenger for år1 - 2019 og år2 - 2020; Ny behandling: Har feriepenger for år1 - 2019;
        //ErBeløpForskjelligeForFPÅr1 = Nei, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(true, 11000L, 10000L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 11000L, 0L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        feriepengerTestUtil.verifiserOpp150NårEttFPÅretOpphørerOgAndreIkkeEndrerSeg(opp150FeriepengerListe, opp150RevurderingFeriepengerListe, false);
    }

    @Test
    public void skalSendeEnOppdragForOpphørOgEnForEndringNårAndreFeriepengeårEndrerSegOgFørsteFeriepengeårOpphørerIRevurdering() {
        //Arrange
        //Forrige behandling: Har feriepenger for år1 - 2019 og år2 - 2020; Ny behandling: Har feriepenger for år1 - 2019;
        //ErBeløpForskjelligeForFPÅr1 = Ja, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(true, 12000L, 10000L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 0L, 11000L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);

        assertThat(opp150RevurderingFeriepengerListe).anySatisfy(opp150 -> assertThat(opp150.gjelderOpphør()).isTrue());
        feriepengerTestUtil.verifiserFeriepengeår(opp150RevurderingFeriepengerListe);
        feriepengerTestUtil.verifiserOppdr150NårEttFeriepengeårSkalOpphøre(opp150FeriepengerListe, opp150RevurderingFeriepengerListe, true);
    }

    @Test
    public void skalSendeKunEnOppdragForOpphørNårAndreFeriepengeårIkkeEndrerSegOgFørsteFeriepengeårOpphørerIRevurdering() {
        //Arrange
        //Forrige behandling: Har feriepenger for år1 - 2019 og år2 - 2020; Ny behandling: Har feriepenger for år1 - 2019;
        //ErBeløpForskjelligeForFPÅr1 = Nei, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(true, 11000L, 10000L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(true, 0L, 10000L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);

        feriepengerTestUtil.verifiserOpp150NårEttFPÅretOpphørerOgAndreIkkeEndrerSeg(opp150FeriepengerListe, opp150RevurderingFeriepengerListe, true);
    }

    @Test
    public void skalSendeOpphørPåForrigeOppdragForFeriepengerNårDetIkkeErFeriepengerIRevurdering() {
        //Arrange
        //Forrige behandling: Har feriepenger for år1 - 2019 og år2 - 2020; Ny behandling: Har ikke feriepenger;
        //ErBeløpForskjelligeForFPÅr1 = Ja, erBeløpForskjelligeForFPÅr2 = Ja
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(true, 11000L, 10000L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(false, 0L, 0L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        assertThat(opp150FeriepengerListe).allSatisfy(oppdragslinje150 -> {
            assertThat(oppdragslinje150.getRefDelytelseId()).isNull();
            assertThat(oppdragslinje150.getRefFagsystemId()).isNull();
        });
        List<Oppdragslinje150> opp150RevurdFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        assertThat(opp150RevurdFeriepengerListe).allSatisfy(oppdragslinje150 ->
            assertThat(oppdragslinje150.gjelderOpphør()).isTrue());
        for (Oppdragslinje150 opp150Revurd : opp150RevurdFeriepengerListe) {
            assertThat(opp150FeriepengerListe).anySatisfy(oppdragslinje150 ->
                assertThat(oppdragslinje150.getDelytelseId()).isEqualTo(opp150Revurd.getDelytelseId()));
        }
        feriepengerTestUtil.verifiserFeriepengeår(opp150RevurdFeriepengerListe);
    }

    @Test
    public void skalSendeOpphørPåFørsteFeriepengeårNårDetIkkeErFeriepengerIRevurdering() {
        //Arrange
        //Forrige behandling: Har feriepenger for år1 - 2019; Ny behandling: Har ikke feriepenger;
        //ErBeløpForskjelligeForFPÅr1 = Ja, erBeløpForskjelligeForFPÅr2 = N/A
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(false, 11000L, 0L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(false, 0L, 0L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        feriepengerTestUtil.verifiserOppdr150NårDetIkkeErFeriepengerIRevurdering(forrigeOppdrag, oppdragRevurdering, true);
    }

    @Test
    public void skalSendeOpphørPåAndreFeriepengeårNårDetIkkeErFeriepengerIRevurdering() {
        //Arrange
        //Forrige behandling: Har feriepenger for år1 - 2019; Ny behandling: Har ikke feriepenger;
        //ErBeløpForskjelligeForFPÅr1 = Ja, erBeløpForskjelligeForFPÅr2 = N/A
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(false, 0L, 9500L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(false, 0L, 0L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        feriepengerTestUtil.verifiserOppdr150NårDetIkkeErFeriepengerIRevurdering(forrigeOppdrag, oppdragRevurdering, false);
    }

    @Test
    public void skalIkkeLagesOppdragHvisDetFinnesIngenFeriepengerIFørstegangsbehandlingOgRevurdering() {
        //Arrange
        //Forrige behandling: Har ikke feriepenger; Ny behandling: Har ikke feriepenger;
        //ErBeløpForskjelligeForFPÅr1 = N/A, erBeløpForskjelligeForFPÅr2 = N/A
        //Førstegangsbehandling
        Oppdragskontroll forrigeOppdrag = oppsettBeregningsresultatFP(false, 0L, 0L);
        //Revurdering
        Behandling revurdering = oppsettBeregningsresultatFPRevurdering(false, 0L, 0L);

        //Act
        Long oppdragRevurderingId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), PROSESS_TASK_ID_2);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragRevurderingId);

        //Assert
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        List<Oppdragslinje150> opp150RevurderingFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        assertThat(opp150FeriepengerListe).isEmpty();
        assertThat(opp150RevurderingFeriepengerListe).isEmpty();
    }
}
