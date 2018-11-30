package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatMedUttaksplanDto;

public class BeregningsresultatMedUttaksplanTjenesteImplTest {

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private UttakRepository uttakRepository;

    private BeregningsresultatTjenesteImpl tjeneste;

    private Behandling behandling;

    @Before
    public void setUp() {
        inntektArbeidYtelseRepository = mock(InntektArbeidYtelseRepository.class);
        beregningsresultatFPRepository = mock(BeregningsresultatFPRepository.class);
        uttakRepository = mock(UttakRepository.class);

        BehandlingRepositoryProvider behandlingRepositoryProvider = mock(BehandlingRepositoryProvider.class);

        when(behandlingRepositoryProvider.getBeregningsresultatFPRepository()).thenReturn(beregningsresultatFPRepository);
        when(behandlingRepositoryProvider.getInntektArbeidYtelseRepository()).thenReturn(inntektArbeidYtelseRepository);
        when(behandlingRepositoryProvider.getUttakRepository()).thenReturn(uttakRepository);

        tjeneste = new BeregningsresultatTjenesteImpl(behandlingRepositoryProvider, mock(SkjæringstidspunktTjeneste.class));

        behandling = mock(Behandling.class);
    }

    @Test
    public void skalReturnereDtoHvisAlleForutsetningerErTilstede() {
        lagForutsetninger(true, true, true, true, true);

        Optional<BeregningsresultatMedUttaksplanDto> dto = tjeneste.lagBeregningsresultatMedUttaksplan(behandling);

        assertThat(dto).isPresent();
    }

    @Test
    public void skalReturnereTomHvisManglerUttakResultatPlan() {
        lagForutsetninger(false, true, true, true, false);

        Optional<BeregningsresultatMedUttaksplanDto> dto = tjeneste.lagBeregningsresultatMedUttaksplan(behandling);

        assertThat(dto).isNotPresent();
    }

    @Test
    public void skalReturnereTomHvisManglerInntektYtelseGrunnlag() {
        lagForutsetninger(true, true, false, true, false);

        Optional<BeregningsresultatMedUttaksplanDto> dto = tjeneste.lagBeregningsresultatMedUttaksplan(behandling);

        assertThat(dto).isNotPresent();
    }

    private void lagForutsetninger(boolean medUttakResultatPlan, boolean medBehandlingsresultat, boolean medInntektGrunnlag, boolean medBeregningsresultatFP, boolean medPerioder) {
        Fagsak fagsak = mock(Fagsak.class);
        when(behandling.getFagsak()).thenReturn(fagsak);

        Behandlingsresultat behandlingsresultat = mock(Behandlingsresultat.class);
        when(behandling.getBehandlingsresultat())
            .thenReturn(medBehandlingsresultat ? behandlingsresultat : null);

        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = mock(InntektArbeidYtelseGrunnlag.class);
        when(inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null))
            .thenReturn(medInntektGrunnlag ? Optional.of(inntektArbeidYtelseGrunnlag) : Optional.empty());

        BeregningsresultatFP beregningsresultatFP = mock(BeregningsresultatFP.class);
        when(beregningsresultatFPRepository.hentBeregningsresultatFP(behandling))
            .thenReturn(medBeregningsresultatFP ? Optional.of(beregningsresultatFP) : Optional.empty());

        if (medUttakResultatPlan) {
            UttakResultatEntitet plan = UttakResultatEntitet.builder(behandling).build();
            when(uttakRepository.hentUttakResultatHvisEksisterer(behandling)).thenReturn(Optional.of(plan));
        } else {
            when(uttakRepository.hentUttakResultatHvisEksisterer(behandling)).thenReturn(Optional.empty());
        }
        if (medPerioder) {
            UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
            UttakResultatPeriodeEntitet opprinneligPeriode = minimumPeriode().build();
            perioder.leggTilPeriode(opprinneligPeriode);

            UttakResultatEntitet plan = UttakResultatEntitet.builder(behandling).medOpprinneligPerioder(perioder).build();
            when(uttakRepository.hentUttakResultatHvisEksisterer(behandling)).thenReturn(Optional.of(plan));
        }
    }
    private UttakResultatPeriodeEntitet.Builder minimumPeriode() {
        return new UttakResultatPeriodeEntitet.Builder(LocalDate.now().minusMonths(1), LocalDate.now())
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT);
    }
}
