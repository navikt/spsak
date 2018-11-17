package no.nav.foreldrepenger.økonomistøtte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.IntervallUtil;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Attestant180;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Grad170;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragsenhet120;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Refusjonsinfo156;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeAksjon;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndring;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndringLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKlassifik;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKomponent;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiUtbetFrekvens;

public class OppdragskontrollTjenesteImplFPTest extends OppdragskontrollTjenesteImplFPBaseTest {

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void opprettOppdragTestFPUtenFeriepenger() {
        // Arrange
        final Long prosessTaskId = 23L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP();
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);

        // Act
        oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);

        // Assert
        Oppdragskontroll oppdrkontrollLest = verifiserOppdragskontrollFraRepo(prosessTaskId);
        List<Oppdrag110> oppdrag110LestListe = verifiserOppdrag110FraRepo(oppdrkontrollLest);
        verifiserAvstemming115FraRepo(oppdrag110LestListe);
        verifiserOppdragsenhet120FraRepo(oppdrag110LestListe);
        List<Oppdragslinje150> oppdragslinje150LestListe = verifiserOppdragslinje150FraRepo(oppdrag110LestListe);
        verifiserGrad170FraRepo(oppdragslinje150LestListe);
        verifiserRefusjonInfo156FraRepo();
        verifiserAttestant180FraRepo(oppdragslinje150LestListe);
    }

    @Test
    public void opprettOppdragTestMedFlereKlassekodeForFørstegangsoppdrag() {
        // Arrange
        final Long prosessTaskId = 23L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatMedFlereInntektskategoriFP(true);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll oppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdrag);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdrag);
        verifiserOppdrag110FraRepo(oppdrag);
        verifiserOppdragslinje150MedFlereKlassekode(oppdrag);
    }

    @Test
    public void opprettOppdragTestMedFlereArbeidsgiverFørstegangOppdarag() {
        // Arrange
        final Long prosessTaskId = 23L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatMedFlereAndelerSomArbeidsgiver();
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll oppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdrag);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdrag);
        verifiserOppdrag110FraRepo(oppdrag);
    }

    @Test
    public void opprettOppdragTestMedFlereAndelerHvorBrukerErIkkeMottakerForFørstegangsoppdrag() {
        // Arrange
        final Long prosessTaskId = 23L;
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatFP, 1, 7);
        buildBeregningsresultatAndel(brPeriode1, true, 1500, BigDecimal.valueOf(80), virksomhet);

        BeregningsresultatPeriode brPeriode3 = buildBeregningsresultatPeriode(beregningsresultatFP, 16, 22);
        buildBeregningsresultatAndel(brPeriode3, true, 0, BigDecimal.valueOf(80), virksomhet3);

        BeregningsresultatPeriode brPeriode4 = buildBeregningsresultatPeriode(beregningsresultatFP, 23, 30);
        buildBeregningsresultatAndel(brPeriode4, false, 2160, BigDecimal.valueOf(80), virksomhet3);
        buildBeregningsresultatAndel(brPeriode4, false, 0, BigDecimal.valueOf(80), virksomhet3);

        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll oppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        verifiserOppdragslinje150MedFlereKlassekode(oppdrag);
        List<Oppdragslinje150> oppdragslinje150Liste = oppdrag.getOppdrag110Liste().stream()
            .flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream())
            .collect(Collectors.toList());
        assertThat(oppdragslinje150Liste.size()).isEqualTo(2);
        assertThat(oppdragslinje150Liste.stream()
            .anyMatch(odl150 -> IntervallUtil.byggIntervall(odl150.getDatoVedtakFom(), odl150.getDatoVedtakTom())
                .equals(IntervallUtil.byggIntervall(LocalDate.now().plusDays(23), LocalDate.now().plusDays(30))))).isTrue();
        assertThat(oppdragslinje150Liste.stream()
            .anyMatch(odl150 -> IntervallUtil.byggIntervall(odl150.getDatoVedtakFom(), odl150.getDatoVedtakTom())
                .equals(IntervallUtil.byggIntervall(LocalDate.now().plusDays(16), LocalDate.now().plusDays(22))))).isFalse();
    }

    @Test
    public void opprettOppdragTestNårDetErFlereKlassekodeIForrigeOppdragOgEnNyKlassekodeINyOppdrag() {
        // Arrange
        final Long prosessTaskId = 29L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatMedFlereInntektskategoriFP(true);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        List<Oppdrag110> originaltOppdrag110Liste = originaltOppdrag.getOppdrag110Liste();
        List<Oppdragslinje150> originaltOppdragslinje150 = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(originaltOppdrag);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        LocalDate førsteDatoVedtakFom = beregningsresultatFP.getBeregningsresultatPerioder().stream().min(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom))
            .map(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom).get();
        endringsdato = førsteDatoVedtakFom.plusDays(3);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(endringsdato);
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingFP(AktivitetStatus.DAGPENGER, Inntektskategori.DAGPENGER);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 462L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        List<Oppdragslinje150> opp150RevurdListe = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        verifiserOppdrag110_ENDR(oppdragId, originaltOppdrag110Liste, true);
        verifiserOppdr150SomErOpphørt(opp150RevurdListe, originaltOppdragslinje150, true, true);
        verifiserOppdr150SomErNy(opp150RevurdListe, originaltOppdragslinje150);
        oppdragskontrollTestVerktøy.verifiserOppdr150MedNyKlassekode(opp150RevurdListe);
    }

    @Test
    public void opprettOppdragTestNårEnMottakerHarFlereAndelerMedSammeKlassekodeIEnPeriode() {
        // Arrange
        final Long prosessTaskId = 30L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatMedFlereInntektskategoriFP(true, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        List<Oppdrag110> originaltOppdrag110Liste = originaltOppdrag.getOppdrag110Liste();
        List<Oppdragslinje150> originaltOppdragslinje150 = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(originaltOppdrag);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        LocalDate førsteDatoVedtakFom = beregningsresultatFP.getBeregningsresultatPerioder().stream().min(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom))
            .map(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom).get();
        endringsdato = førsteDatoVedtakFom.plusDays(3);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(endringsdato);
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingMedFlereInntektskategoriFP(AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 463L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        List<Oppdragslinje150> opp150RevurdListe = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        verifiserOppdrag110_ENDR(oppdragId, originaltOppdrag110Liste, true);
        verifiserOppdr150SomErOpphørt(opp150RevurdListe, originaltOppdragslinje150, true, true);
        verifiserOppdr150SomErNy(opp150RevurdListe, originaltOppdragslinje150);
        oppdragskontrollTestVerktøy.verifiserOppdr150SomAndelerSlåSammen(originaltOppdrag, oppdragRevurdering);
    }

    @Test
    public void skalOppretteEndringsoppdragNårBehandlingsresultatErOpphørOgOpphørsdatoErEtterStp() {
        // Arrange
        final Long prosessTaskId = 40L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatMedFlereInntektskategoriFP(true, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);

        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        List<Oppdrag110> originaltOppdrag110Liste = originaltOppdrag.getOppdrag110Liste();
        List<Oppdragslinje150> originaltOppdragslinje150 = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(originaltOppdrag);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, true, false);
        LocalDate førsteDatoVedtakFom = beregningsresultatFP.getBeregningsresultatPerioder().stream().min(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom))
            .map(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom).get();
        endringsdato = førsteDatoVedtakFom.plusDays(3);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(endringsdato);
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingMedFlereInntektskategoriFP(AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);
        UttakResultatPerioderEntitet perioder = buildUttakResultatPerioderEntitet();
        uttakRepository.lagreOpprinneligUttakResultatPerioder(revurdering, perioder);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 463L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        List<Oppdragslinje150> opp150RevurdListe = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        verifiserOppdrag110_ENDR(oppdragId, originaltOppdrag110Liste, true);
        verifiserOppdr150SomErOpphørt(opp150RevurdListe, originaltOppdragslinje150, true, true);
        verifiserOppdr150SomErNy(opp150RevurdListe, originaltOppdragslinje150);
        oppdragskontrollTestVerktøy.verifiserOppdr150SomAndelerSlåSammen(originaltOppdrag, oppdragRevurdering);
    }

    @Test
    public void hentOppdragskontrollTestFP() {
        // Arrange
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP();
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long oppdrkontrollId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), 67L);
        assertThat(oppdrkontrollId).isNotNull();

        // Act
        Oppdragskontroll oppdrkontroll = oppdragskontrollTjeneste.hentOppdragskontroll(oppdrkontrollId);

        // Assert
        assertThat(oppdrkontroll).isNotNull();
        assertThat(oppdrkontroll.getOppdrag110Liste()).hasSize(4);

        List<Oppdrag110> oppdrag110LestListe = oppdrkontroll.getOppdrag110Liste();
        assertThat(oppdrag110LestListe).isNotNull();
        for (Oppdrag110 oppdrag110Lest : oppdrag110LestListe) {
            assertThat(oppdrag110Lest.getOppdragslinje150Liste()).isNotNull();
            assertThat(oppdrag110Lest.getOppdragsenhet120Liste()).isNotNull();
            assertThat(oppdrag110Lest.getAvstemming115()).isNotNull();

            List<Oppdragslinje150> oppdrlinje150LestListe = oppdrag110Lest.getOppdragslinje150Liste();
            for (Oppdragslinje150 oppdrlinje150Lest : oppdrlinje150LestListe) {
                assertThat(oppdrlinje150Lest).isNotNull();
                assertThat(oppdrlinje150Lest.getOppdrag110()).isNotNull();
                assertThat(oppdrlinje150Lest.getAttestant180Liste()).hasSize(1);
                assertThat(oppdrlinje150Lest.getAttestant180Liste().get(0)).isNotNull();
            }
        }
    }

    @Test
    public void opprettOppdragTestMedEnKlassekodeIForrigeOgFlereNyOppdrag() {
        // Førstegang behandling
        final Long prosessTaskId = 28L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatBrukerFP();
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        @SuppressWarnings("unused")
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);

        // Ny revurdering behandling
        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(endringsdato);
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingMedFlereInntektskategoriFP(AktivitetStatus.FRILANSER, Inntektskategori.FRILANSER);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 461L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        List<Oppdragslinje150> oppdragslinje150LestListe = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(oppdragRevurdering);
        boolean invalidAttestantId = oppdragslinje150LestListe.stream()
            .flatMap(oppdragslinje150 -> oppdragslinje150.getAttestant180Liste().stream())
            .anyMatch(attestant180 -> (attestant180.getAttestantId().isEmpty() || attestant180.getAttestantId() == null));
        assertThat(invalidAttestantId).isFalse();
    }

    private void verifiserOppdragslinje150MedFlereKlassekode(Oppdragskontroll oppdrag) {
        List<Oppdragslinje150> oppdr150ListeArbeidsgiver = oppdrag.getOppdrag110Liste().stream().filter(opp110 -> opp110.getKodeFagomrade().equals(ØkonomiKodeFagområde.FPREF.name()))
            .flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream()).filter(opp150 -> !opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik()))
            .collect(Collectors.toList());
        List<Oppdragslinje150> oppdr150ListeAT = oppdragskontrollTestVerktøy.getOppdragslinje150MedKlassekode(oppdrag, ØkonomiKodeKlassifik.FPATORD.getKodeKlassifik());
        List<Oppdragslinje150> oppdr150ListeFL = oppdragskontrollTestVerktøy.getOppdragslinje150MedKlassekode(oppdrag, ØkonomiKodeKlassifik.FPATFRI.getKodeKlassifik());
        List<BeregningsresultatAndel> andelersListe = hentAndeler();
        List<BeregningsresultatAndel> brukersandelerListeAT = andelersListe.stream().filter(BeregningsresultatAndel::erBrukerMottaker)
            .filter(andel -> andel.getInntektskategori().equals(Inntektskategori.ARBEIDSTAKER)).collect(Collectors.toList());
        List<BeregningsresultatAndel> brukersandelerListeFL = andelersListe.stream().filter(BeregningsresultatAndel::erBrukerMottaker)
            .filter(andel -> andel.getInntektskategori().equals(Inntektskategori.FRILANSER)).collect(Collectors.toList());
        List<BeregningsresultatAndel> arbeidsgiversandelerListe = andelersListe.stream().filter(andel -> !andel.erBrukerMottaker()).collect(Collectors.toList());

        verifiserOppdragslinje150MedFlereKlassekode(oppdr150ListeAT, brukersandelerListeAT);
        verifiserOppdragslinje150MedFlereKlassekode(oppdr150ListeFL, brukersandelerListeFL);
        verifiserOppdragslinje150MedFlereKlassekode(oppdr150ListeArbeidsgiver, arbeidsgiversandelerListe);
        oppdragskontrollTestVerktøy.verifiserKjedingForOppdragslinje150(oppdr150ListeAT, oppdr150ListeFL);
    }

    private void verifiserOppdragslinje150MedFlereKlassekode(List<Oppdragslinje150> oppdr150Liste, List<BeregningsresultatAndel> brukersandelerListe) {
        int ix150 = 0;
        for (Oppdragslinje150 opp150 : oppdr150Liste) {
            BeregningsresultatAndel andel = brukersandelerListe.get(ix150++);
            Boolean brukerErMottaker = andel.erBrukerMottaker();
            String kodeklassifik;
            if (brukerErMottaker) {
                kodeklassifik = andel.getInntektskategori().equals(Inntektskategori.ARBEIDSTAKER) ? ØkonomiKodeKlassifik.FPATORD.getKodeKlassifik() : ØkonomiKodeKlassifik.FPATFRI.getKodeKlassifik();
            } else {
                kodeklassifik = ØkonomiKodeKlassifik.FPREFAG_IOP.getKodeKlassifik();
            }
            String utbetalesTilId = brukerErMottaker ? personInfo.getPersonIdent().getIdent() : andel.getArbeidsforholdOrgnr();
            assertThat(opp150.getKodeEndringLinje()).isEqualTo(ØkonomiKodeEndringLinje.NY.name());
            assertThat(opp150.getVedtakId()).isEqualTo(behVedtakFP.getVedtaksdato().toString());
            assertThat(opp150.getKodeKlassifik()).isEqualTo(kodeklassifik);
            assertThat(opp150.getDatoVedtakFom()).isEqualTo(andel.getBeregningsresultatPeriode().getBeregningsresultatPeriodeFom());
            assertThat(opp150.getDatoVedtakTom()).isEqualTo(andel.getBeregningsresultatPeriode().getBeregningsresultatPeriodeTom());
            assertThat(opp150.getSats()).isEqualTo(andel.getDagsats());
            assertThat(opp150.getTypeSats()).isEqualTo(TYPE_SATS_FP_YTELSE);
            assertThat(opp150.getHenvisning()).isEqualTo(behandlingFP.getId());
            assertThat(opp150.getSaksbehId()).isEqualTo(behVedtakFP.getAnsvarligSaksbehandler());
            assertThat(opp150.getBrukKjoreplan()).isEqualTo("N");
            assertThat(opp150.getAttestant180Liste()).hasSize(1);
            assertThat(opp150.getGrad170Liste()).hasSize(1);
            if (brukerErMottaker) {
                assertThat(opp150.getUtbetalesTilId()).isEqualTo(oppdragskontrollTestVerktøy.endreTilElleveSiffer(utbetalesTilId));
            } else {
                assertThat(opp150.getUtbetalesTilId()).isNull();
                Refusjonsinfo156 ref156 = opp150.getRefusjonsinfo156();
                assertThat(ref156.getRefunderesId()).isEqualTo(oppdragskontrollTestVerktøy.endreTilElleveSiffer(utbetalesTilId));
            }
        }
    }

    private void verifiserAttestant180FraRepo(List<Oppdragslinje150> oppdragslinje150) {
        List<Attestant180> attestant180ListFraRepo = repository.hentAlle(Attestant180.class);

        int ix180 = 0;
        for (Attestant180 attestant180FraRepo : attestant180ListFraRepo) {
            assertThat(attestant180FraRepo.getAttestantId()).isEqualTo(behVedtakFP.getAnsvarligSaksbehandler());
            assertThat(attestant180FraRepo.getOppdragslinje150()).isEqualTo(oppdragslinje150.get(ix180++));
        }
    }

    private List<Oppdragslinje150> verifiserOppdragslinje150FraRepo(List<Oppdrag110> oppdrag110Liste) {
        List<Oppdragslinje150> oppdragslinje150ListFraRepo = repository.hentAlle(Oppdragslinje150.class);

        List<Long> delYtelseIdListe = new ArrayList<>();
        int jx = 0;
        for (Oppdrag110 oppdrag110 : oppdrag110Liste) {
            assertThat(oppdrag110.getOppdragslinje150Liste()).isNotEmpty();
            verifiserOppdragslinje150FraRepoFP(oppdrag110.getOppdragslinje150Liste(), delYtelseIdListe, oppdrag110, jx++);
        }
        return oppdragslinje150ListFraRepo;
    }

    private void verifiserAvstemming115FraRepo(List<Oppdrag110> oppdrag110LestListe) {
        List<Avstemming115> avstemming115ListHentet = repository.hentAlle(Avstemming115.class);

        int size = oppdrag110LestListe.size();
        assertThat(avstemming115ListHentet).hasSize(size);
        for (Avstemming115 avstemming115Lest : avstemming115ListHentet) {
            assertThat(avstemming115Lest.getKodekomponent()).isEqualTo(ØkonomiKodeKomponent.VLFP.getKodeKomponent());
        }
    }

    private void verifiserOppdragsenhet120FraRepo(List<Oppdrag110> oppdrag110Liste) {
        List<Oppdragsenhet120> oppdragsenhet120ListFraRepo = repository.hentAlle(Oppdragsenhet120.class);
        assertThat(oppdragsenhet120ListFraRepo).hasSameSizeAs(oppdrag110Liste);

        int ix120 = 0;
        for (Oppdragsenhet120 oppdragsenhet120FraRepo : oppdragsenhet120ListFraRepo) {
            assertThat(oppdragsenhet120FraRepo.getTypeEnhet()).isEqualTo("BOS");
            assertThat(oppdragsenhet120FraRepo.getEnhet()).isEqualTo("8020");
            assertThat(oppdragsenhet120FraRepo.getDatoEnhetFom()).isEqualTo(LocalDate.of(1900, 1, 1));
            assertThat(oppdragsenhet120FraRepo.getOppdrag110()).isEqualTo(oppdrag110Liste.get(ix120++));
        }
    }

    private void verifiserGrad170FraRepo(List<Oppdragslinje150> oppdragslinje150) {
        List<Grad170> grad170ListHentet = repository.hentAlle(Grad170.class);

        assertThat(grad170ListHentet).hasSameSizeAs(oppdragslinje150);
        int ix150 = 0;
        for (Grad170 grad170Lest : grad170ListHentet) {
            assertThat(grad170Lest.getTypeGrad()).isEqualTo("UFOR");
            if (oppdragskontrollTestVerktøy.opp150MedGradering(oppdragslinje150.get(ix150))) {
                assertThat(grad170Lest.getGrad()).isEqualTo(80);
            } else {
                assertThat(grad170Lest.getGrad()).isEqualTo(100);
            }
            assertThat(grad170Lest.getOppdragslinje150()).isEqualTo(oppdragslinje150.get(ix150++));
        }
    }

    private void verifiserRefusjonInfo156FraRepo() {
        List<Refusjonsinfo156> refusjonsinfo156ListHentet = repository.hentAlle(Refusjonsinfo156.class);
        List<BeregningsresultatAndel> andeler = hentAndeler();
        List<BeregningsresultatAndel> arbeidsgiverAndelListe = andeler.stream()
            .filter(andel -> !andel.erBrukerMottaker())
            .filter(andel -> andel.getDagsats() > 0)
            .collect(Collectors.toList());
        assertThat(refusjonsinfo156ListHentet).hasSameSizeAs(arbeidsgiverAndelListe);

        int ix156 = 0;
        for (Refusjonsinfo156 refusjonsinfo156Lest : refusjonsinfo156ListHentet) {
            String refunderesId = oppdragskontrollTestVerktøy.endreTilElleveSiffer(arbeidsgiverAndelListe.get(ix156++).getArbeidsforholdOrgnr());
            if (refunderesId.equals(oppdragskontrollTestVerktøy.endreTilElleveSiffer(ARBEIDSFORHOLD_ID))) {
                assertThat(refusjonsinfo156Lest.getMaksDato()).isEqualTo(LocalDate.now().plusDays(7));
            } else if (refunderesId.equals(oppdragskontrollTestVerktøy.endreTilElleveSiffer(ARBEIDSFORHOLD_ID_2))) {
                assertThat(refusjonsinfo156Lest.getMaksDato()).isEqualTo(LocalDate.now().plusDays(15));
            } else {
                assertThat(refusjonsinfo156Lest.getMaksDato()).isEqualTo(LocalDate.now().plusDays(22));
            }
            assertThat(refusjonsinfo156Lest.getRefunderesId()).isEqualTo(refunderesId);
            assertThat(refusjonsinfo156Lest.getDatoFom()).isEqualTo(behVedtakFP.getVedtaksdato());
        }
    }

    private void verifiserOppdragslinje150FraRepoFP(List<Oppdragslinje150> oppdragslinje150ListFraRepo, List<Long> delYtelseIdListe, Oppdrag110 oppdrag110, int jx) {
        LocalDate vedtaksdatoFP = behVedtakFP.getVedtaksdato();
        Long fagsystemId = oppdrag110.getFagsystemId();

        List<List<BeregningsresultatAndel>> andelerSorted = sortAndelerSomListOfLists();
        assertThat(andelerSorted).isNotNull();

        long løpenummer = 100L;
        int ix150 = 0;
        List<BeregningsresultatAndel> andelerList = andelerSorted.get(jx);
        for (Oppdragslinje150 oppdragslinje150FraRepo : oppdragslinje150ListFraRepo) {
            BeregningsresultatAndel andel = andelerList.get(ix150++);
            Boolean brukerErMottaker = andel.erBrukerMottaker();
            delYtelseIdListe.add(oppdragslinje150FraRepo.getDelytelseId());

            String utbetalesTilId = brukerErMottaker ? personInfo.getPersonIdent().getIdent() : andel.getArbeidsforholdOrgnr();
            assertThat(oppdragslinje150FraRepo.getKodeEndringLinje()).isEqualTo(ØkonomiKodeEndringLinje.NY.name());
            assertThat(oppdragslinje150FraRepo.getVedtakId()).isEqualTo(vedtaksdatoFP.toString());
            assertThat(oppdragslinje150FraRepo.getDelytelseId()).isEqualTo(concatenateValues(fagsystemId, løpenummer));
            assertThat(oppdragslinje150FraRepo.getKodeKlassifik()).isEqualTo(brukerErMottaker ? ØkonomiKodeKlassifik.FPATORD.getKodeKlassifik() : ØkonomiKodeKlassifik.FPREFAG_IOP.getKodeKlassifik());
            assertThat(oppdragslinje150FraRepo.getDatoVedtakFom()).isEqualTo(andel.getBeregningsresultatPeriode().getBeregningsresultatPeriodeFom());
            assertThat(oppdragslinje150FraRepo.getDatoVedtakTom()).isEqualTo(andel.getBeregningsresultatPeriode().getBeregningsresultatPeriodeTom());
            assertThat(oppdragslinje150FraRepo.getSats()).isEqualTo(andel.getDagsats());
            assertThat(oppdragslinje150FraRepo.getTypeSats()).isEqualTo(TYPE_SATS_FP_YTELSE);
            assertThat(oppdragslinje150FraRepo.getHenvisning()).isEqualTo(behandlingFP.getId());
            assertThat(oppdragslinje150FraRepo.getSaksbehId()).isEqualTo(behVedtakFP.getAnsvarligSaksbehandler());
            assertThat(oppdragslinje150FraRepo.getBrukKjoreplan()).isEqualTo("N");
            assertThat(oppdragslinje150FraRepo.getOppdrag110()).isEqualTo(oppdrag110);
            assertThat(oppdragslinje150FraRepo.getAttestant180Liste()).hasSize(1);
            assertThat(oppdragslinje150FraRepo.getGrad170Liste()).hasSize(1);
            if (brukerErMottaker) {
                assertThat(oppdragslinje150FraRepo.getUtbetalesTilId()).isEqualTo(oppdragskontrollTestVerktøy.endreTilElleveSiffer(utbetalesTilId));
            } else {
                assertThat(oppdragslinje150FraRepo.getUtbetalesTilId()).isNull();
                Refusjonsinfo156 ref156 = oppdragslinje150FraRepo.getRefusjonsinfo156();
                assertThat(ref156.getRefunderesId()).isEqualTo(oppdragskontrollTestVerktøy.endreTilElleveSiffer(utbetalesTilId));
            }
            if (løpenummer > 100L) {
                int kx = (int) (løpenummer - 101);
                assertThat(oppdragslinje150FraRepo.getRefFagsystemId()).isEqualTo(fagsystemId);
                assertThat(oppdragslinje150FraRepo.getRefDelytelseId()).isEqualTo(delYtelseIdListe.get(kx));
            }
            løpenummer++;
        }
    }

    private List<List<BeregningsresultatAndel>> sortAndelerSomListOfLists() {
        List<List<BeregningsresultatAndel>> andelerSorted = new ArrayList<>();

        List<BeregningsresultatAndel> beregningsresultatAndelListe = hentAndeler();
        List<BeregningsresultatAndel> brukersAndelListe = beregningsresultatAndelListe.stream()
            .filter(BeregningsresultatAndel::erBrukerMottaker)
            .filter(a -> a.getDagsats() > 0)
            .collect(Collectors.toList());

        List<BeregningsresultatAndel> arbeidsgiversAndelListe = beregningsresultatAndelListe.stream()
            .filter(a -> !a.erBrukerMottaker())
            .filter(a -> a.getDagsats() > 0)
            .collect(Collectors.toList());

        Map<String, List<BeregningsresultatAndel>> groupedById = arbeidsgiversAndelListe.stream()
            .collect(Collectors.groupingBy(
                BeregningsresultatAndel::getArbeidsforholdOrgnr,
                LinkedHashMap::new,
                Collectors.mapping(Function.identity(), Collectors.toList())));

        andelerSorted.add(brukersAndelListe);
        andelerSorted.add(groupedById.get(ARBEIDSFORHOLD_ID));
        andelerSorted.add(groupedById.get(ARBEIDSFORHOLD_ID_2));
        andelerSorted.add(groupedById.get(ARBEIDSFORHOLD_ID_3));

        return andelerSorted;
    }

    private List<Oppdrag110> verifiserOppdrag110FraRepo(Oppdragskontroll oppdragskontroll) {

        List<Oppdrag110> oppdrag110ListFraRepo = repository.hentAlle(Oppdrag110.class);

        List<Avstemming115> avstemming115ListFraRepo = repository.hentAlle(Avstemming115.class);
        assertThat(avstemming115ListFraRepo).hasSameSizeAs(oppdrag110ListFraRepo);

        int ix110 = 0;
        Long initialLøpenummer = 100L;
        for (Oppdrag110 oppdrag110FraRepo : oppdrag110ListFraRepo) {
            assertThat(oppdrag110FraRepo.getKodeAksjon()).isEqualTo(ØkonomiKodeAksjon.EN.getKodeAksjon());
            assertThat(oppdrag110FraRepo.getKodeEndring()).isEqualTo(ØkonomiKodeEndring.NY.name());
            Boolean brukerErMottaker = ix110 == 0;
            assertThat(oppdrag110FraRepo.getKodeFagomrade()).isEqualTo(brukerErMottaker ? ØkonomiKodeFagområde.FP.name() : ØkonomiKodeFagområde.FPREF.name());
            assertThat(oppdrag110FraRepo.getFagsystemId()).isEqualTo(concatenateValues(Long.parseLong(fagsakFP.getSaksnummer().getVerdi()), initialLøpenummer++));
            assertThat(oppdrag110FraRepo.getSaksbehId()).isEqualTo(behVedtakFP.getAnsvarligSaksbehandler());
            assertThat(oppdrag110FraRepo.getUtbetFrekvens()).isEqualTo(ØkonomiUtbetFrekvens.MÅNED.getUtbetFrekvens());
            assertThat(oppdrag110FraRepo.getOppdragGjelderId()).isEqualTo(personInfo.getPersonIdent().getIdent());
            assertThat(oppdrag110FraRepo.getOppdragskontroll()).isEqualTo(oppdragskontroll);
            assertThat(oppdrag110FraRepo.getAvstemming115()).isEqualTo(avstemming115ListFraRepo.get(ix110++));
        }

        return oppdrag110ListFraRepo;
    }

    private Oppdragskontroll verifiserOppdragskontrollFraRepo(Long prosessTaskId) {
        List<Oppdragskontroll> oppdrkontrollListHentet = repository.hentAlle(Oppdragskontroll.class);
        Oppdragskontroll oppdrskontrollLest = oppdrkontrollListHentet.get(0);

        assertThat(oppdrkontrollListHentet).hasSize(1);
        assertThat(oppdrskontrollLest.getSaksnummer()).isEqualTo(fagsakFP.getSaksnummer());
        assertThat(oppdrskontrollLest.getVenterKvittering()).isEqualTo(Boolean.TRUE);
        assertThat(oppdrskontrollLest.getProsessTaskId()).isEqualTo(prosessTaskId);

        return oppdrskontrollLest;
    }

    private Long concatenateValues(Long... values) {
        List<Long> valueList = Arrays.asList(values);
        String result = valueList.stream().map(Object::toString).collect(Collectors.joining());

        return Long.valueOf(result);
    }

    private List<BeregningsresultatAndel> hentAndeler() {
        BeregningsresultatFP beregningsresultatFP = beregningsresultatFPRepository.hentBeregningsresultatFP(behandlingFP).orElseThrow(() ->
            new IllegalStateException("Mangler Beregningsresultat for behandling " + behandlingFP.getId()));
        List<BeregningsresultatPeriode> brPeriodeListe = beregningsresultatFP.getBeregningsresultatPerioder().stream()
            .sorted(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom)).collect(Collectors.toList());
        List<BeregningsresultatAndel> andeler = brPeriodeListe.stream().map(BeregningsresultatPeriode::getBeregningsresultatAndelList).flatMap(List::stream).collect(Collectors.toList());

        return andeler.stream().filter(a -> a.getDagsats() > 0).collect(Collectors.toList());
    }

    private UttakResultatPerioderEntitet buildUttakResultatPerioderEntitet() {
        UttakResultatPerioderEntitet opprinneligPerioder = new UttakResultatPerioderEntitet();

        UttakResultatPeriodeEntitet periode = new UttakResultatPeriodeEntitet.Builder(LocalDate.now().minusMonths(1), LocalDate.now())
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT).build();
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("id"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivitet)
            .medArbeidsprosent(BigDecimal.ZERO)
            .medTrekkdager(1)
            .medUtbetalingsprosent(BigDecimal.TEN)
            .build();
        periode.leggTilAktivitet(periodeAktivitet);
        opprinneligPerioder.leggTilPeriode(periode);

        return opprinneligPerioder;
    }
}
