package no.nav.foreldrepenger.domene.beregning.ytelse.impl;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.beregning.ytelse.FinnEndringsdatoBeregningsresultatFPTjeneste;
import no.nav.foreldrepenger.domene.beregning.ytelse.impl.FinnEndringsdatoBeregningsresultatFPTjenesteImpl;
import no.nav.vedtak.exception.TekniskException;

public class FinnEndringsdatoBeregningsresultatFPTjenesteImplTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private FinnEndringsdatoBeregningsresultatFPTjeneste finnEndringsdatoBeregningsresultatFPTjeneste;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private Behandling originalBehandling;
    private Behandling revurdering;

    @Before
    public void oppsett() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD);
        originalBehandling = scenario.lagMocked();
        revurdering = opprettRevurdering(originalBehandling);
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
        finnEndringsdatoBeregningsresultatFPTjeneste = new FinnEndringsdatoBeregningsresultatFPTjenesteImpl(repositoryProvider);
    }

    // ---------------------------- //
    // TESTER                       //
    // ---------------------------- //

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_den_første_perioden_i_original_behandling_ikke_har_en_korresponderende_periode(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode originalPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust);


    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_den_siste_perioden_i_original_behandling_ikke_har_en_korresponderende_periode(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode originalPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust.plusDays(7));

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_den_siste_perioden_i_revurderingen_ikke_har_en_korresponderende_periode(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(revurderingPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust.plusDays(7));

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_den_første_perioden_i_revurderingen_ikke_har_en_korresponderende_periode(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(revurderingPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));

        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust);

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_med_forskjellig_antall_andeler_fra_original_behandling(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);
        LocalDate datoForEndring = førsteAugust.plusDays(7);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode originalPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, datoForEndring, datoForEndring.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, datoForEndring, datoForEndring.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        opprettBeregningsresultatAndel(revurderingPeriode2, true, "2", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "2", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(datoForEndring);

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_ErBrukerMottaker_i_revurdering_andel_i_andre_periode_er_endret_fra_andel_i_original_behandling(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode originalPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(revurderingPeriode2, false, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust.plusDays(7));

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_ArbeidsforholdId_i_revurdering_andel_er_endret_fra_andel_i_original_behandling(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode, true, "2", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust);

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_AktivitetStatus_i_revurdering_andel_er_endret_fra_andel_i_original_behandling(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode, true, "1", AktivitetStatus.FRILANSER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust);

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_VirksomhetOrgNr_i_revurdering_andel_er_endret_fra_andel_i_original_behandling(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);
        boolean erBrukerMottaker = true;
        String arbeidsforholdId = "1";
        int dagsats = 1000;
        BigDecimal stillingsprosent = BigDecimal.valueOf(100);
        BigDecimal utbetalingsgrad = BigDecimal.valueOf(100);
        AktivitetStatus aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        Inntektskategori inntektskategori = Inntektskategori.ARBEIDSTAKER;
        String originalBehandlingOrgNr = "1";
        String revurderingOrgNr = "2";

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode, erBrukerMottaker, arbeidsforholdId, aktivitetStatus, inntektskategori, originalBehandlingOrgNr,
            dagsats, stillingsprosent, utbetalingsgrad);
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode, erBrukerMottaker, arbeidsforholdId, aktivitetStatus, inntektskategori, revurderingOrgNr,
            dagsats, stillingsprosent, utbetalingsgrad);
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust);

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_inntektskategori_i_revurdering_andel_er_endret_fra_andel_i_original_behandling(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();

        BeregningsresultatPeriode revurderingPeriode = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.FISKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust);

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_dagsats_i_revurdering_andel_er_endret_fra_andel_i_original_behandling(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode originalPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(revurderingPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 2000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust.plusDays(7));

    }

    @Test
    public void skal_finne_en_tom_endringsdato_for_revurdering_med_ingen_endringer_fra_original_behandling(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode beregningsresultatPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(beregningsresultatPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode beregningsresultatPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(beregningsresultatPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isEmpty();

    }

    @Test
    public void skal_feile_hvis_flere_korresponderende_andeler_blir_funnet_for_en_andel(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER,
            Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode originalPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER,
            Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER,
            Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(50), BigDecimal.valueOf(80));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        BeregningsresultatAndel andel = opprettBeregningsresultatAndel(revurderingPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        opprettBeregningsresultatAndel(revurderingPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(50), BigDecimal.valueOf(80));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Expect
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage(CoreMatchers.containsString(
            String.format("Fant flere korresponderende andeler for andel med id %s", andel.getId())));

        // Act
        finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

    }

    @Test
    public void skal_feile_hvis_behandling_ikke_er_en_revurdering(){

        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD);
        originalBehandling = scenario.lagMocked();
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
        finnEndringsdatoBeregningsresultatFPTjeneste = new FinnEndringsdatoBeregningsresultatFPTjenesteImpl(repositoryProvider);

        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Expect
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage(CoreMatchers.containsString(
            String.format("Behandlingen med id %s er ikke en revurdering", originalBehandling.getId())));

        // Act
        finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(originalBehandling, beregningsresultatForOriginalBehandling);

    }

    @Test
    public void skal_feile_hvis_revurdering_ikke_har_en_original_behandling(){

        // Arrange

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medBehandlingType(BehandlingType.REVURDERING);
        Behandling revurdering = scenario.lagMocked();
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
        finnEndringsdatoBeregningsresultatFPTjeneste = new FinnEndringsdatoBeregningsresultatFPTjenesteImpl(repositoryProvider);

        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Expect
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage(CoreMatchers.containsString(
            String.format("Fant ikke en original behandling for revurdering med id %s", revurdering.getId())));

        // Act
        finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

    }

    @Test
    public void skal_feile_hvis_den_originale_behandlingen_til_revurderingen_ikke_har_et_beregningsresultalt() {

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Expect
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage(CoreMatchers.containsString(
            String.format("Fant ikke beregningsresultat for behandling med id %s", originalBehandling.getId())));

        // Act
        finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

    }

    @Test
    public void skal_feile_hvis_den_originale_behandlingen_til_revurderingen_ikke_har_noen_beregningsresultaltperioder() {

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Expect
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage(CoreMatchers.containsString(
            String.format("Fant ikke beregningsresultatperiode for beregningsresultat med id %s", beregningsresultatForOriginalBehandling.getId())));

        // Act
        finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

    }

    @Test
    public void skal_feile_hvis_revurderingen_ikke_har_noen_beregningsresultaltperioder() {

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Expect
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage(CoreMatchers.containsString(
            String.format("Fant ikke beregningsresultatperiode for beregningsresultat med id %s", beregningsresultatForRevurdering.getId())));

        // Act
        finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

    }

    @Test
    public void skal_feile_hvis_revurderingen_ikke_har_noen_andeler_i_en_beregningsresultaltperioder() {

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Expect
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage(CoreMatchers.containsString(
            String.format("Fant ikke andel for beregningsresultatperiode med id %s", revurderingPeriode.getId())));

        // Act
        finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

    }

    @Test
    public void skal_feile_hvis_den_originale_behandlingen_til_revurderingen_ikke_har_noen_andeler_i_en_beregningsresultaltperioder() {

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Expect
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage(CoreMatchers.containsString(
            String.format("Fant ikke andel for beregningsresultatperiode med id %s", originalPeriode.getId())));

        // Act
        finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_siste_periode_i_revurdering_er_kortere_enn_siste_periode_i_original_behandling_og_hvor_andelene_er_uendret(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8, 1);
        LocalDate forventetEndringsdato = førsteAugust.plusDays(11);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode originalPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(7), førsteAugust.plusDays(10));
        opprettBeregningsresultatAndel(revurderingPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(forventetEndringsdato);

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_den_første_perioden_i_revurderingen_er_korterer_enn_første_periode_i_original_behandling_og_hvor_andelene_er_undret(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8, 1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode originalPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(3));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(4), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(revurderingPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust.plusDays(4));

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_siste_periode_i_revurdering_er_delt_i_to_perioder_men_andelene_er_uendret(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8,1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1",
            1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode originalPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1",
            1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1",
            1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(7), førsteAugust.plusDays(9));
        opprettBeregningsresultatAndel(revurderingPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1",
            1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode3 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(10), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(revurderingPeriode3, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, "1",
            1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));

        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust.plusDays(10));

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_siste_periode_i_revurdering_er_lengre_enn_siste_periode_i_original_behandling_og_hvor_andelene_er_uendret(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8, 1);
        LocalDate forventetEndringsdato = førsteAugust.plusDays(14);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode originalPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(7), førsteAugust.plusDays(21));
        opprettBeregningsresultatAndel(revurderingPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(forventetEndringsdato);

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_den_første_perioden_i_revurderingen_er_lengere_enn_første_periode_i_original_behandling_og_hvor_andelene_er_undret(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8, 1);
        LocalDate forventetEndringsdato = førsteAugust.plusDays(7);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust, førsteAugust.plusDays(6));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode originalPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(9));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(10), førsteAugust.plusDays(16));
        opprettBeregningsresultatAndel(revurderingPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(forventetEndringsdato);

    }

    @Test
    public void skal_finne_endringsdato_for_revurdering_hvor_den_første_perioden_i_revurderingen_startert_før_første_periode_i_original_behandling_og_hvor_andelene_er_undret(){

        // Arrange
        LocalDate førsteAugust = LocalDate.of(2018, 8, 1);

        // Førstegangsbehandling
        BeregningsresultatFP beregningsresultatForOriginalBehandling = opprettBeregningsresultat();
        BeregningsresultatPeriode originalPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(7), førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(originalPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode originalPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForOriginalBehandling, førsteAugust.plusDays(14), førsteAugust.plusDays(21));
        opprettBeregningsresultatAndel(originalPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(originalBehandling, beregningsresultatForOriginalBehandling);

        // Revurdering
        BeregningsresultatFP beregningsresultatForRevurdering = opprettBeregningsresultat();
        BeregningsresultatPeriode revurderingPeriode1 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust, førsteAugust.plusDays(13));
        opprettBeregningsresultatAndel(revurderingPeriode1, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        BeregningsresultatPeriode revurderingPeriode2 = opprettBeregningsresultatPeriode(beregningsresultatForRevurdering, førsteAugust.plusDays(14), førsteAugust.plusDays(21));
        opprettBeregningsresultatAndel(revurderingPeriode2, true, "1", AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,
            "1", 1000, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        beregningsresultatFPRepository.lagre(revurdering, beregningsresultatForRevurdering);

        // Act
        Optional<LocalDate> endringsdato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(revurdering, beregningsresultatForRevurdering);

        // Assert
        assertThat(endringsdato).isPresent();
        assertThat(endringsdato.get()).isEqualTo(førsteAugust);

    }

    // ---------------------------- //
    // PRIVATE METODER              //
    // ---------------------------- //

    private Behandling opprettRevurdering(Behandling originalBehandling) {
        return Behandling.fraTidligereBehandling(originalBehandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(
                BehandlingÅrsak.builder(BehandlingÅrsakType.RE_MANGLER_FØDSEL)
                    .medOriginalBehandling(originalBehandling))
            .build();
    }

    private BeregningsresultatFP opprettBeregningsresultat() {
        return BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
    }

    private BeregningsresultatPeriode opprettBeregningsresultatPeriode(BeregningsresultatFP beregningsresultatFP, LocalDate fom, LocalDate tom){
        return BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(fom, tom)
            .build(beregningsresultatFP);
    }

    private BeregningsresultatAndel opprettBeregningsresultatAndel(BeregningsresultatPeriode beregningsresultatPeriode, boolean erBrukerMottaker, String arbeidsforholdId, AktivitetStatus aktivitetStatus,
                                                                   Inntektskategori inntektskategori, String orgNr, int dagsats, BigDecimal stillingsprosent, BigDecimal utbetalingsgrad) {
        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(orgNr)
            .oppdatertOpplysningerNå()
            .build();
        return BeregningsresultatAndel.builder()
            .medBrukerErMottaker(erBrukerMottaker)
            .medVirksomhet(virksomhet)
            .medArbforholdId(arbeidsforholdId)
            .medAktivitetstatus(aktivitetStatus)
            .medInntektskategori(inntektskategori)
            .medStillingsprosent(stillingsprosent)
            .medUtbetalingsgrad(utbetalingsgrad)
            .medDagsats(dagsats)
            .build(beregningsresultatPeriode);
    }

}
