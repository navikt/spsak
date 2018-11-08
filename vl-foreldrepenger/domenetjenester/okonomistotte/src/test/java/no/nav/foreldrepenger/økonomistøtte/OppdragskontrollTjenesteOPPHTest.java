package no.nav.foreldrepenger.økonomistøtte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndring;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndringLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKlassifik;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeStatusLinje;

public class OppdragskontrollTjenesteOPPHTest extends OppdragskontrollTjenesteImplFPBaseTest {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void opprettOppdragTestOPPH() {
        // Arrange
        final Long prosessTaskId = 24L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP();
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.AVSLAG, 0, true, false);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 457L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        List<Oppdrag110> oppdrag110RevurderingList = verifiserOppdrag110_OPPH(oppdragRevurdering, originaltOppdrag);
        List<Oppdragslinje150> oppdragslinje150LestListe = verifiserOppdragslinje150_OPPH(oppdragId, originaltOppdrag, OppdragskontrollTjenesteOPPHTest.this);
        oppdragskontrollTestVerktøy.verifiserGrad170FraRepo(oppdragslinje150LestListe, originaltOppdrag);
        oppdragskontrollTestVerktøy.verifiserRefusjonInfo156FraRepo(oppdrag110RevurderingList, originaltOppdrag);
    }

    @Test
    public void opprettOppdragTestOPPHMedFlereKategorier() {
        final Long prosessTaskId = 24L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatMedFlereInntektskategoriFP(false);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.AVSLAG, 0, true, false);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 457L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        verifiserOppdrag110_OPPH(oppdragRevurdering, originaltOppdrag);
        verifiserOppdragslinje150MedFlereKategorier_OPPH(oppdragRevurdering, originaltOppdrag);
    }

    @Test
    public void opprettOppdragTestOPPHForBrukerSomDelAvENDR() {
        // Arrange
        final Long prosessTaskId = 26L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatBrukerFP();
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        List<Oppdrag110> originaltOppdrag110Liste = originaltOppdrag.getOppdrag110Liste();

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(endringsdato);
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingEntenForBrukerEllerArbgvr(false, true);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 459L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        verifiserOPPHForBrukerIENDR(oppdragId, originaltOppdrag110Liste);
    }

    @Test
    public void opphørSkalIkkeSendesHvisEndringstidspunktErEtterAlleTidligereOppdragForBrukerMedFlereKlassekode() {
        // Arrange
        final Long prosessTaskId = 45L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatMedFlereInntektskategoriFP(true);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(endringsdato.plusMonths(18));
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingFP(AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhet, virksomhet2, true);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 460L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        List<Oppdragslinje150> oppdragslinje150OpphørtListe = oppdragRevurdering.getOppdrag110Liste().stream().flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste()
            .stream()).filter(Oppdragslinje150::gjelderOpphør).collect(Collectors.toList());
        assertThat(oppdragslinje150OpphørtListe).isEmpty();
    }

    @Test
    public void opphørSkalIkkeSendesHvisEndringstidspunktErEtterAlleTidligereOppdrag() {
        // Arrange
        final Long prosessTaskId = 45L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatBrukerFP();
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(endringsdato.plusMonths(18));
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingFP(AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhet, virksomhet2, true);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 460L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        List<Oppdragslinje150> oppdragslinje150OpphørtListe = oppdragRevurdering.getOppdrag110Liste().stream().flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste()
            .stream()).filter(Oppdragslinje150::gjelderOpphør).collect(Collectors.toList());
        assertThat(oppdragslinje150OpphørtListe).isEmpty();
    }

    @Test
    public void opphørSkalIkkeSendesForYtelseHvisEndringstidspunktErEtterSisteDatoITidligereOppdragForArbeidsgiver() {
        // Arrange
        final Long prosessTaskId = 45L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatEntenForBrukerEllerArbgvr(false, true);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        List<Oppdrag110> originaltOppdrag110Liste = originaltOppdrag.getOppdrag110Liste();

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        LocalDate sisteDatoIForrigeOppdrag = beregningsresultatFP.getBeregningsresultatPerioder().stream().max(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeTom))
            .map(BeregningsresultatPeriode::getBeregningsresultatPeriodeTom).get();
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(sisteDatoIForrigeOppdrag.plusDays(1));
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingEntenForBrukerEllerArbgvr(false, true);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 460L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        verifiserOppdrag110OgOppdragslinje150(oppdragId, originaltOppdrag110Liste, false);
    }

    @Test
    public void opphørSkalIkkeSendesHvisEndringstidspunktErEtterSisteDatoITidligereOppdrForBrukerMedFlereKlassekodeIForrigeBeh() {
        // Arrange
        final Long prosessTaskId = 45L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatMedFlereInntektskategoriFP(false);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        List<Oppdrag110> originaltOppdrag110Liste = originaltOppdrag.getOppdrag110Liste();

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(LocalDate.now().plusDays(18));
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingFP(AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhet, virksomhet2, true);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 460L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        verifiserOppdrag110OgOppdragslinje150(oppdragId, originaltOppdrag110Liste, true);
    }

    @Test
    public void opphørsDatoenMåSettesLikFørsteDatoVedtakFomNårDenneErSenereEnnEndringstdpktBruker() {
        // Arrange
        final Long prosessTaskId = 45L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatEntenForBrukerEllerArbgvr(true, true);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        LocalDate førsteDatoVedtakFom = beregningsresultatFP.getBeregningsresultatPerioder().stream().min(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom))
            .map(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom).get();
        LocalDate opphørFom = førsteDatoVedtakFom.minusDays(5);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(opphørFom);
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingEntenForBrukerEllerArbgvr(true, true);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 460L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        Oppdragslinje150 oppdragslinje150Opphørt = oppdragRevurdering.getOppdrag110Liste().stream().flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste()
            .stream()).filter(Oppdragslinje150::gjelderOpphør).findFirst().get();
        assertThat(oppdragslinje150Opphørt.getDatoStatusFom()).isEqualTo(førsteDatoVedtakFom);
    }

    @Test
    public void opphørsDatoenMåSettesLikFørsteDatoVedtakFomNårDenneErSenereEnnEndringstdpktForBrukerMedFlereKlassekode() {
        // Arrange
        final Long prosessTaskId = 45L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatMedFlereInntektskategoriFP(false);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        LocalDate førsteDatoVedtakFom = beregningsresultatFP.getBeregningsresultatPerioder().stream().min(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom))
            .map(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom).get();
        LocalDate opphørFom = førsteDatoVedtakFom.minusDays(5);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(opphørFom);
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingFP(AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhet, virksomhet2, true);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 460L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserOpphørsdatoen(originaltOppdrag, oppdragRevurdering);
    }

    @Test
    public void opphørsDatoenMåSettesLikFørsteDatoVedtakFomNårDenneErSenereEnnEndringstdpktArbgvr() {
        // Arrange
        final Long prosessTaskId = 45L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatEntenForBrukerEllerArbgvr(false, true);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        LocalDate førsteDatoVedtakFom = beregningsresultatFP.getBeregningsresultatPerioder().stream().min(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom))
            .map(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom).get();
        LocalDate opphørFom = førsteDatoVedtakFom.minusDays(5);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(opphørFom);
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingEntenForBrukerEllerArbgvr(false, true);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 460L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        Oppdragslinje150 oppdragslinje150Opphørt = oppdragRevurdering.getOppdrag110Liste().stream().flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste()
            .stream()).filter(Oppdragslinje150::gjelderOpphør).findFirst().get();
        assertThat(oppdragslinje150Opphørt.getDatoStatusFom()).isEqualTo(førsteDatoVedtakFom);
    }

    @Test
    public void opphørSkalIkkeSendesHvisEndringstidspunktErEtterSisteDatoITidligereOppdragForBruker() {
        // Arrange
        final Long prosessTaskId = 45L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatEntenForBrukerEllerArbgvr(true, true);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        List<Oppdrag110> originaltOppdrag110Liste = originaltOppdrag.getOppdrag110Liste();

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        LocalDate sisteDatoIForrigeOppdrag = beregningsresultatFP.getBeregningsresultatPerioder().stream().max(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeTom))
            .map(BeregningsresultatPeriode::getBeregningsresultatPeriodeTom).get();
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(sisteDatoIForrigeOppdrag.plusDays(1));
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingEntenForBrukerEllerArbgvr(true, true);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 460L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        verifiserOppdrag110OgOppdragslinje150(oppdragId, originaltOppdrag110Liste, false);
    }

    private void verifiserOppdrag110OgOppdragslinje150(Long oppdragId, List<Oppdrag110> originaltOpp110Liste, boolean medFlereKlassekode) {
        Oppdragskontroll oppdragskontroll = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);
        List<Oppdrag110> nyOppdr110Liste = oppdragskontroll.getOppdrag110Liste();
        for (Oppdrag110 oppdr110Revurd : nyOppdr110Liste) {
            assertThat(oppdr110Revurd.getKodeEndring()).isEqualTo(ØkonomiKodeEndring.UEND.name());
            assertThat(oppdr110Revurd.getOppdragslinje150Liste()).isNotEmpty();
            assertThat(originaltOpp110Liste).anySatisfy(oppdrag110 ->
                assertThat(oppdrag110.getFagsystemId()).isEqualTo(oppdr110Revurd.getFagsystemId()));
        }
        List<Oppdragslinje150> opp150RevurderingListe = nyOppdr110Liste.stream().flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream()).collect(Collectors.toList());
        List<Oppdragslinje150> opp150OriginalListe = originaltOpp110Liste.stream().flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream()).collect(Collectors.toList());
        assertThat(opp150RevurderingListe).allSatisfy(opp150 -> assertThat(opp150.getKodeStatusLinje()).isNull());
        if (medFlereKlassekode) {
            oppdragskontrollTestVerktøy.verifiserDelYtelseOgFagsystemIdForEnKlassekode(opp150RevurderingListe, opp150OriginalListe);
        } else {
            oppdragskontrollTestVerktøy.verifiserDelYtelseOgFagsystemIdForFlereKlassekode(opp150RevurderingListe, opp150OriginalListe);
        }
        for (Oppdragslinje150 opp150Revurdering : opp150RevurderingListe) {
            assertThat(opp150OriginalListe).allSatisfy(opp150 ->
                assertThat(opp150.getDelytelseId()).isNotEqualTo(opp150Revurdering.getDelytelseId()));
            if (opp150Revurdering.getOppdrag110().getKodeFagomrade().equals(ØkonomiKodeFagområde.FPREF.name())) {
                assertThat(opp150Revurdering.getRefusjonsinfo156()).isNotNull();
            }
            if (!oppdragskontrollTestVerktøy.erOpp150ForFeriepenger(opp150Revurdering)) {
                assertThat(opp150Revurdering.getGrad170Liste()).isNotEmpty();
                assertThat(opp150Revurdering.getGrad170Liste()).isNotNull();
            } else {
                assertThat(opp150Revurdering.getGrad170Liste()).isEmpty();
            }
        }
    }

    private void verifiserOPPHForBrukerIENDR(Long oppdragId, List<Oppdrag110> originaltOpp110Liste) {
        Oppdragskontroll oppdragskontroll = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);
        Optional<Oppdrag110> nyOppdr110Bruker = oppdragskontroll.getOppdrag110Liste().stream().filter(oppdrag110 -> oppdrag110.getKodeFagomrade().equals(ØkonomiKodeFagområde.FP.name()))
            .findFirst();
        assertThat(nyOppdr110Bruker).isPresent();
        assertThat(nyOppdr110Bruker).hasValueSatisfying(opp110 ->
        {
            assertThat(opp110.getKodeEndring()).isEqualTo(ØkonomiKodeEndring.UEND.name());
            assertThat(opp110.getOppdragslinje150Liste()).isNotEmpty();
            assertThat(originaltOpp110Liste).anySatisfy(oppdrag110 ->
                assertThat(oppdrag110.getFagsystemId()).isEqualTo(nyOppdr110Bruker.get().getFagsystemId()));
        });
        verifiserOppdragslinje150_OPPH_Bruker_I_ENDR(originaltOpp110Liste, nyOppdr110Bruker.get());
    }

    private void verifiserOppdragslinje150_OPPH_Bruker_I_ENDR(List<Oppdrag110> opp110OriginalListe, Oppdrag110 nyOpp110Bruker) {
        List<Oppdragslinje150> originaltOpp150BrukerListe = opp110OriginalListe.stream()
            .filter(oppdrag110 -> oppdrag110.getKodeFagomrade().equals(ØkonomiKodeFagområde.FP.name()))
            .flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream())
            .collect(Collectors.toList());
        List<Oppdragslinje150> revurderingOpp150BrukerListe = nyOpp110Bruker.getOppdragslinje150Liste();

        assertThat(revurderingOpp150BrukerListe).anySatisfy(opp150 ->
            assertThat(opp150.getKodeKlassifik()).isEqualTo(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik()));

        for (int ix = 0; ix < revurderingOpp150BrukerListe.size(); ix++) {
            Oppdragslinje150 revurderingOpp150Bruker = revurderingOpp150BrukerListe.get(ix);
            Oppdragslinje150 originaltOpp150Bruker = originaltOpp150BrukerListe.get(ix);
            assertThat(revurderingOpp150Bruker.getDelytelseId()).isEqualTo(originaltOpp150Bruker.getDelytelseId());
            assertThat(revurderingOpp150Bruker.getRefDelytelseId()).isNull();
            assertThat(revurderingOpp150Bruker.getRefFagsystemId()).isNull();
            assertThat(revurderingOpp150Bruker.getKodeEndringLinje()).isEqualTo(ØkonomiKodeEndringLinje.ENDR.name());
            assertThat(revurderingOpp150Bruker.getKodeStatusLinje()).isEqualTo(ØkonomiKodeStatusLinje.OPPH.name());
            LocalDate førsteDatoVedtakFom = oppdragskontrollTestVerktøy.finnFørsteDatoVedtakFom(originaltOpp150BrukerListe, originaltOpp150Bruker);
            LocalDate datoStatusFom = førsteDatoVedtakFom.isAfter(endringsdato) ? førsteDatoVedtakFom : endringsdato;
            assertThat(revurderingOpp150Bruker.getDatoStatusFom()).isEqualTo(revurderingOpp150Bruker.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik())
                ? LocalDate.of(2019, 5, 1) : datoStatusFom);
            assertThat(revurderingOpp150Bruker.getSats()).isEqualTo(originaltOpp150Bruker.getSats());
        }
    }

    private List<Oppdrag110> verifiserOppdrag110_OPPH(Oppdragskontroll oppdragRevurdering, Oppdragskontroll originaltOppdrag) {
        List<Oppdrag110> originaltOppdrag110Liste = originaltOppdrag.getOppdrag110Liste();
        List<Oppdrag110> nyOppdr110Liste = oppdragRevurdering.getOppdrag110Liste();
        verifiserAlleOppdragOpphørt(originaltOppdrag110Liste, nyOppdr110Liste);

        assertThat(oppdragRevurdering.getOppdrag110Liste()).hasSameSizeAs(originaltOppdrag110Liste);
        for (int ix110 = 0; ix110 < nyOppdr110Liste.size(); ix110++) {
            assertThat(nyOppdr110Liste.get(ix110).getKodeEndring()).isEqualTo(ØkonomiKodeEndring.UEND.name());
            assertThat(nyOppdr110Liste.get(ix110).getFagsystemId()).isEqualTo(originaltOppdrag110Liste.get(ix110).getFagsystemId());
            assertThat(nyOppdr110Liste.get(ix110).getOppdragslinje150Liste()).isNotEmpty();
        }
        return nyOppdr110Liste;
    }

    private void verifiserAlleOppdragOpphørt(List<Oppdrag110> originaltOpp110Liste, List<Oppdrag110> nyOppdr110Liste) {
        for (Oppdrag110 originalt : originaltOpp110Liste) {
            Oppdrag110 nyttOppdrag = nyOppdr110Liste.stream()
                .filter(nytt -> originalt.getFagsystemId() == nytt.getFagsystemId())
                .findFirst().orElse(null);
            assertThat(nyttOppdrag).isNotNull();
            List<String> klassifikasjoner = originalt.getOppdragslinje150Liste().stream()
                .map(Oppdragslinje150::getKodeKlassifik)
                .distinct()
                .collect(Collectors.toList());
            for (String klassifikasjon : klassifikasjoner) {
                Optional<Oppdragslinje150> opphørslinje = nyttOppdrag.getOppdragslinje150Liste().stream()
                    .filter(opp150 -> klassifikasjon.equals(opp150.getKodeKlassifik()))
                    .filter(opp150 -> ØkonomiKodeEndringLinje.ENDR.name().equals(opp150.getKodeEndringLinje()))
                    .filter(opp150 -> ØkonomiKodeStatusLinje.OPPH.name().equals(opp150.getKodeStatusLinje()))
                    .findFirst();
                assertThat(opphørslinje)
                    .as("Mangler oppdragslinje med opphør for klassifikasjon %s i oppdrag %s", klassifikasjon,
                        nyttOppdrag.getFagsystemId())
                    .isNotEmpty();
            }
        }
    }

    private void verifiserOppdragslinje150MedFlereKategorier_OPPH(Oppdragskontroll oppdragRevurdering, Oppdragskontroll originaltOppdrag) {
        Map<String, List<Oppdragslinje150>> originaltOppLinjePerKodeKl = originaltOppdrag.getOppdrag110Liste().stream()
            .filter(oppdrag110 -> oppdrag110.getKodeFagomrade().equals(ØkonomiKodeFagområde.FP.name()))
            .flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream())
            .collect(Collectors.groupingBy(Oppdragslinje150::getKodeKlassifik));
        Map<String, List<Oppdragslinje150>> nyOpp150LinjePerKodeKl = oppdragRevurdering.getOppdrag110Liste().stream()
            .filter(oppdrag110 -> oppdrag110.getKodeFagomrade().equals(ØkonomiKodeFagområde.FP.name()))
            .flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream())
            .collect(Collectors.groupingBy(Oppdragslinje150::getKodeKlassifik));

        for (String kodeKlassifik : originaltOppLinjePerKodeKl.keySet()) {
            Oppdragslinje150 sisteOriginaltOppdragsLinje = originaltOppLinjePerKodeKl.get(kodeKlassifik).stream()
                .max(Comparator.comparing(Oppdragslinje150::getDelytelseId)).get();
            Oppdragslinje150 sisteNyOppdragsLinje = nyOpp150LinjePerKodeKl.get(kodeKlassifik).get(0);
            assertThat(sisteOriginaltOppdragsLinje.getDelytelseId()).isEqualTo(sisteNyOppdragsLinje.getDelytelseId());
            assertThat(sisteNyOppdragsLinje.getRefDelytelseId()).isNull();
            assertThat(sisteNyOppdragsLinje.getRefFagsystemId()).isNull();
            assertThat(sisteNyOppdragsLinje.getKodeEndringLinje()).isEqualTo(ØkonomiKodeEndringLinje.ENDR.name());
            assertThat(sisteNyOppdragsLinje.getKodeStatusLinje()).isEqualTo(ØkonomiKodeStatusLinje.OPPH.name());
        }
    }

    private List<Oppdragslinje150> verifiserOppdragslinje150_OPPH(Long oppdragId, Oppdragskontroll originaltOppdrag, OppdragskontrollTjenesteImplFPBaseTest oppdragskontrollTjenesteImplFPBaseTest) {
        Oppdragskontroll oppdragskontroll = oppdragskontrollTjenesteImplFPBaseTest.oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);
        List<Oppdragslinje150> originaltOpp150Liste = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(originaltOppdrag);
        List<Oppdragslinje150> nyOpp150Liste = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(oppdragskontroll);

        for (Oppdragslinje150 nyOpp150 : nyOpp150Liste) {
            Oppdragslinje150 originaltOpp150 = originaltOpp150Liste.stream()
                .filter(oppdragslinje150 -> oppdragslinje150.getDatoVedtakFom().equals(nyOpp150.getDatoVedtakFom())
                    && oppdragslinje150.getDatoVedtakTom().equals(nyOpp150.getDatoVedtakTom())
                    && oppdragslinje150.getOppdrag110().getKodeFagomrade().equals(nyOpp150.getOppdrag110().getKodeFagomrade()))
                .findFirst().get();
            assertThat(nyOpp150.getDelytelseId()).isEqualTo(originaltOpp150.getDelytelseId());
            assertThat(nyOpp150.getRefDelytelseId()).isNull();
            assertThat(nyOpp150.getRefFagsystemId()).isNull();
            assertThat(nyOpp150.getKodeEndringLinje()).isEqualTo(ØkonomiKodeEndringLinje.ENDR.name());
            assertThat(nyOpp150.getKodeStatusLinje()).isEqualTo(ØkonomiKodeStatusLinje.OPPH.name());
            assertThat(nyOpp150.getSats()).isEqualTo(originaltOpp150.getSats());
        }
        return nyOpp150Liste;
    }
}
