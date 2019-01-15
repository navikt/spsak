package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.app;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatEngangsstønadDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatMedUttaksplanDto;

@ApplicationScoped
public class BeregningsresultatTjenesteImpl implements BeregningsresultatTjeneste {

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private BeregningsresultatRepository beregningsresultatFPRepository;
    private UttakRepository uttakRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private BehandlingRepository behandlingRepository;

    public BeregningsresultatTjenesteImpl() {
        // For CDI
    }

    @Inject
    public BeregningsresultatTjenesteImpl(GrunnlagRepositoryProvider grunnlagRepositoryProvider,
                                          ResultatRepositoryProvider resultatRepositoryProvider,
                                          SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.inntektArbeidYtelseRepository = grunnlagRepositoryProvider.getInntektArbeidYtelseRepository();
        this.beregningsresultatFPRepository = resultatRepositoryProvider.getBeregningsresultatRepository();
        this.behandlingRepository = resultatRepositoryProvider.getBehandlingRepository();
        this.uttakRepository = resultatRepositoryProvider.getUttakRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public Optional<BeregningsresultatMedUttaksplanDto> lagBeregningsresultatMedUttaksplan(Behandling behandling) {
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        return uttakResultat
            .flatMap(uttakResultatPlan -> beregningsresultatFPRepository.hentHvisEksisterer(behandlingRepository.hentResultat(behandling.getId()))
                .flatMap(beregningsresultatFP -> inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling))
                    .map(inntektArbeidYtelseGrunnlag -> BeregningsresultatMedUttaksplanMapper.lagBeregningsresultatMedUttaksplan(behandling, beregningsresultatFP))));
    }

    @Override
    public Optional<BeregningsresultatEngangsstønadDto> lagBeregningsresultatEnkel(Behandling behandling) {
        return Optional.empty();
    }
}
