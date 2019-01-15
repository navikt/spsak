package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPerioder;
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
    private BeregningsresultatRepository beregningsresultatFPRepository;
    private UttakRepository uttakRepository;
    private BehandlingRepository behandlingRepository;

    private BeregningsresultatTjenesteImpl tjeneste;

    private Behandling behandling;

    @Before
    public void setUp() {
        inntektArbeidYtelseRepository = mock(InntektArbeidYtelseRepository.class);
        beregningsresultatFPRepository = mock(BeregningsresultatRepository.class);
        uttakRepository = mock(UttakRepository.class);
        behandlingRepository = mock(BehandlingRepository.class);

        GrunnlagRepositoryProvider grunnlagRepositoryProvider = mock(GrunnlagRepositoryProvider.class);
        ResultatRepositoryProvider resultatRepositoryProvider = mock(ResultatRepositoryProvider.class);

        when(resultatRepositoryProvider.getBeregningsresultatRepository()).thenReturn(beregningsresultatFPRepository);
        when(grunnlagRepositoryProvider.getInntektArbeidYtelseRepository()).thenReturn(inntektArbeidYtelseRepository);
        when(resultatRepositoryProvider.getUttakRepository()).thenReturn(uttakRepository);
        when(resultatRepositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepository);
        when(grunnlagRepositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepository);

        tjeneste = new BeregningsresultatTjenesteImpl(grunnlagRepositoryProvider, resultatRepositoryProvider, mock(SkjæringstidspunktTjeneste.class));

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

        when(behandlingRepository.hentResultatHvisEksisterer(anyLong()))
            .thenReturn(medBehandlingsresultat ? Optional.of(behandlingsresultat) : Optional.empty());
        when(behandlingRepository.hentResultat(anyLong()))
            .thenReturn(medBehandlingsresultat ? behandlingsresultat : null);

        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = mock(InntektArbeidYtelseGrunnlag.class);
        when(inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null))
            .thenReturn(medInntektGrunnlag ? Optional.of(inntektArbeidYtelseGrunnlag) : Optional.empty());

        BeregningsresultatPerioder beregningsresultat = mock(BeregningsresultatPerioder.class);
        when(beregningsresultatFPRepository.hentHvisEksisterer(any(Behandlingsresultat.class)))
            .thenReturn(medBeregningsresultatFP ? Optional.of(beregningsresultat) : Optional.empty());

        if (medUttakResultatPlan) {
            UttakResultatEntitet plan = UttakResultatEntitet.builder(behandlingsresultat).build();
            when(uttakRepository.hentUttakResultatHvisEksisterer(behandling)).thenReturn(Optional.of(plan));
        } else {
            when(uttakRepository.hentUttakResultatHvisEksisterer(behandling)).thenReturn(Optional.empty());
        }
        if (medPerioder) {
            UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
            UttakResultatPeriodeEntitet opprinneligPeriode = minimumPeriode().build();
            perioder.leggTilPeriode(opprinneligPeriode);

            UttakResultatEntitet plan = UttakResultatEntitet.builder(behandlingsresultat).medOpprinneligPerioder(perioder).build();
            when(uttakRepository.hentUttakResultatHvisEksisterer(behandling)).thenReturn(Optional.of(plan));
        }
    }
    private UttakResultatPeriodeEntitet.Builder minimumPeriode() {
        return new UttakResultatPeriodeEntitet.Builder(LocalDate.now().minusMonths(1), LocalDate.now())
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT);
    }
}
