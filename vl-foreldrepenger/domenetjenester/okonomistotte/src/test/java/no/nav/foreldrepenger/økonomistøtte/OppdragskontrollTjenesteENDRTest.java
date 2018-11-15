package no.nav.foreldrepenger.økonomistøtte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
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
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKlassifik;

public class OppdragskontrollTjenesteENDRTest extends OppdragskontrollTjenesteImplFPBaseTest {

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void opprettOppdragTestENDR() {
        // Arrange
        final Long prosessTaskId = 25L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP();
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
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingFP(false);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 458L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        verifiserOppdrag110_ENDR(oppdragId, originaltOppdrag110Liste, false);
        verifiserOppdragslinje150_ENDR(oppdragId, originaltOppdragslinje150, false, false);
    }

    @Test
    public void opprettOppdragTestFPMedFeriepenger() {
        // Arrange
        final Long prosessTaskId = 27L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP(true);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        List<Oppdrag110> originaltOppdrag110Liste = originaltOppdrag.getOppdrag110Liste();
        List<Oppdragslinje150> originaltOppdragslinje150 = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(originaltOppdrag);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingFP(true);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(endringsdato);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 460L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        verifiserOppdrag110_ENDR(oppdragId, originaltOppdrag110Liste, true);
        verifiserOppdragslinje150_ENDR(oppdragId, originaltOppdragslinje150, true, false);
        verifiserOppdr150SomErUendret(oppdragRevurdering);
    }


    @Test
    public void skalOppretteEndringsoppdragNårBehandlingsresultatErInnvilgetOgForrigeOppdragEksisterer() {
        // Arrange
        final Long prosessTaskId = 45L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP(true);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        List<Oppdrag110> originaltOppdrag110Liste = originaltOppdrag.getOppdrag110Liste();
        List<Oppdragslinje150> originaltOppdragslinje150 = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(originaltOppdrag);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, false);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(endringsdato);
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingFP(true);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 460L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        verifiserOppdrag110_ENDR(oppdragId, originaltOppdrag110Liste, true);
        verifiserOppdragslinje150_ENDR(oppdragId, originaltOppdragslinje150, true, false);
        verifiserOppdr150SomErUendret(oppdragRevurdering);
    }

    @Test
    public void opprettOppdragTestMedFlereKlassekodeBådeIForrigeOgNyOppdrag() {
        // Arrange
        final Long prosessTaskId = 28L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatMedFlereInntektskategoriFP(true);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long originaltOppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll originaltOppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(originaltOppdragId);
        List<Oppdrag110> originaltOppdrag110Liste = originaltOppdrag.getOppdrag110Liste();
        List<Oppdragslinje150> originaltOppdragslinje150 = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(originaltOppdrag);

        Behandling revurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        when(endringsdatoUtleder.utledEndringsdato(revurdering)).thenReturn(endringsdato);
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingMedFlereInntektskategoriFP(AktivitetStatus.FRILANSER, Inntektskategori.FRILANSER);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatRevurderingFP);

        // Act
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(revurdering.getId(), 461L);
        Oppdragskontroll oppdragRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        //Assert
        oppdragskontrollTestVerktøy.verifiserAvstemming115FraRepo(oppdragRevurdering);
        oppdragskontrollTestVerktøy.verifiserOppdragsenhet120FraRepo(oppdragRevurdering);
        verifiserOppdrag110_ENDR(oppdragId, originaltOppdrag110Liste, true);
        verifiserOppdragslinje150_ENDR(oppdragId, originaltOppdragslinje150, true, true);
        oppdragskontrollTestVerktøy.verifiserOppdragslinje150ForHverKlassekode(originaltOppdrag, oppdragRevurdering);
    }

    @Test
    public void skalTesteKjedingAvOppdragslinje150NårDetErFlereRevurderingerISammeSak() {
        // Arrange
        final Long prosessTaskId = 32L;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatEntenForBrukerEllerArbgvr(true, true);
        beregningsresultatFPRepository.lagre(behandlingFP, beregningsresultatFP);
        Long oppdragId = oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), prosessTaskId);
        Oppdragskontroll oppdragForFørstegangsbehandling = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);

        Behandling førsteRevurdering = opprettOgLagreRevurdering(this.behandlingFP, VedtakResultatType.INNVILGET, 1, false, true);
        when(endringsdatoUtleder.utledEndringsdato(førsteRevurdering)).thenReturn(endringsdato);
        BeregningsresultatFP beregningsresultatRevurderingFP = buildBeregningsresultatRevurderingEntenForBrukerEllerArbgvr(true, false);
        beregningsresultatFPRepository.lagre(førsteRevurdering, beregningsresultatRevurderingFP);
        Long oppdragId_2 = oppdragskontrollTjeneste.opprettOppdrag(førsteRevurdering.getId(), 471L);
        Oppdragskontroll oppdragForFørsteRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId_2);

        Behandling andreRevurdering = opprettOgLagreRevurdering(førsteRevurdering, VedtakResultatType.INNVILGET, 1, false, true);
        when(endringsdatoUtleder.utledEndringsdato(andreRevurdering)).thenReturn(endringsdato);
        BeregningsresultatFP beregningsresultatRevurderingFP_2 = buildBeregningsresultatRevurderingEntenForBrukerEllerArbgvr(true, true);
        beregningsresultatFPRepository.lagre(andreRevurdering, beregningsresultatRevurderingFP_2);

        //Act
        Long nyesteOppdragId = oppdragskontrollTjeneste.opprettOppdrag(andreRevurdering.getId(), 481L);
        Oppdragskontroll oppdragForAndreRevurdering = oppdragskontrollTjeneste.hentOppdragskontroll(nyesteOppdragId);

        //Assert
        verifiserKjedingNårDetErFlereRevurderingBehandlinger(oppdragForFørstegangsbehandling, oppdragForFørsteRevurdering, oppdragForAndreRevurdering);
        verifiserFeriepengerNårDetErFlereRevurderingBehandlinger(oppdragForFørstegangsbehandling, oppdragForFørsteRevurdering, oppdragForAndreRevurdering);
    }

    private void verifiserKjedingNårDetErFlereRevurderingBehandlinger(Oppdragskontroll førsteOppdrag, Oppdragskontroll andreOppdrag, Oppdragskontroll tredjeOppdrag) {
        List<Oppdragslinje150> førsteOpp150Liste = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(førsteOppdrag);
        List<Oppdragslinje150> andreOpp150Liste = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(andreOppdrag);
        List<Oppdragslinje150> tredjeOpp150Liste = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(tredjeOppdrag);

        for (Oppdragslinje150 nyesteOpp150 : tredjeOpp150Liste) {
            if (nyesteOpp150.gjelderOpphør()) {
                assertThat(andreOpp150Liste).anySatisfy(opp150 ->
                    assertThat(opp150.getDelytelseId()).isEqualTo(nyesteOpp150.getDelytelseId()));
                assertThat(førsteOpp150Liste).allSatisfy(opp150 ->
                    assertThat(opp150.getDelytelseId()).isNotEqualTo(nyesteOpp150.getDelytelseId()));
            } else {
                assertThat(andreOpp150Liste).allSatisfy(opp150 ->
                    assertThat(opp150.getDelytelseId()).isNotEqualTo(nyesteOpp150.getDelytelseId()));
                assertThat(førsteOpp150Liste).allSatisfy(opp150 ->
                    assertThat(opp150.getDelytelseId()).isNotEqualTo(nyesteOpp150.getDelytelseId()));
            }
        }
        for (Oppdragslinje150 opp150FraFørsteRevurdering : andreOpp150Liste) {
            if (opp150FraFørsteRevurdering.gjelderOpphør()) {
                assertThat(førsteOpp150Liste).anySatisfy(opp150 ->
                    assertThat(opp150.getDelytelseId()).isEqualTo(opp150FraFørsteRevurdering.getDelytelseId()));
            } else {
                assertThat(førsteOpp150Liste).allSatisfy(opp150 ->
                    assertThat(opp150.getDelytelseId()).isNotEqualTo(opp150FraFørsteRevurdering.getDelytelseId()));
            }
        }
    }

    private void verifiserFeriepengerNårDetErFlereRevurderingBehandlinger(Oppdragskontroll oppdragForFørstegangsbehandling, Oppdragskontroll oppdragForFørsteRevurdering,
                                                                          Oppdragskontroll oppdragForAndreRevurdering) {
        List<Oppdragslinje150> førsteOpp150Liste = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(oppdragForFørstegangsbehandling);
        List<Oppdragslinje150> andreOpp150Liste = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(oppdragForFørsteRevurdering);
        List<Oppdragslinje150> tredjeOpp150Liste = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(oppdragForAndreRevurdering);
        Optional<Oppdragslinje150> opp150OpphIFørsteRevurdering = andreOpp150Liste.stream()
            .filter(opp150 -> opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik()))
            .findFirst();
        assertThat(opp150OpphIFørsteRevurdering).hasValueSatisfying(opp150Revurdering -> {
            assertThat(førsteOpp150Liste).anySatisfy(førsteOpp150 -> assertThat(førsteOpp150.getDelytelseId()).isEqualTo(opp150Revurdering.getDelytelseId()));
            assertThat(tredjeOpp150Liste).allSatisfy(opp150FraSisteRevurd -> assertThat(opp150Revurdering.getDelytelseId()).isNotEqualTo(opp150FraSisteRevurd.getDelytelseId()));
        });
        Optional<Oppdragslinje150> opp150OpphISisteRevurdering = tredjeOpp150Liste.stream()
            .filter(opp150 -> opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik()))
            .findFirst();
        assertThat(opp150OpphISisteRevurdering).hasValueSatisfying(opp150Revurdering -> {
            assertThat(opp150Revurdering.getRefDelytelseId()).isNull();
            assertThat(opp150Revurdering.getRefFagsystemId()).isNull();
        });
    }

    private void verifiserOppdragslinje150_ENDR(Long oppdragId, List<Oppdragslinje150> originaltOpp150Liste, boolean medFeriepenger, boolean medFlereKlassekode) {
        Oppdragskontroll oppdragskontroll = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragId);
        List<Oppdragslinje150> opp150RevurdListe = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(oppdragskontroll);

        verifiserOppdr150SomErOpphørt(opp150RevurdListe, originaltOpp150Liste, medFeriepenger, medFlereKlassekode);
        verifiserOppdr150SomErNy(opp150RevurdListe, originaltOpp150Liste);
    }

    private void verifiserOppdr150SomErUendret(Oppdragskontroll oppdrag) {
        List<Oppdragslinje150> opp150RevurdListe = oppdragskontrollTestVerktøy.getOppdragslinje150Liste(oppdrag);
        List<Oppdragslinje150> opp150VirksomhetListe = opp150RevurdListe.stream()
            .filter(oppdragslinje150 -> oppdragslinje150.getRefusjonsinfo156() != null)
            .filter(oppdragslinje150 -> oppdragslinje150.getRefusjonsinfo156().getRefunderesId().equals(oppdragskontrollTestVerktøy.endreTilElleveSiffer(virksomhet.getOrgnr())))
            .filter(oppdragslinje150 -> oppdragslinje150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik()))
            .collect(Collectors.toList());
        assertThat(opp150VirksomhetListe).isEmpty();
    }
}
