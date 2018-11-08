package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.app;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatEngangsstønadDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatMedUttaksplanDto;

@ApplicationScoped
public class BeregningsresultatTjenesteImpl implements BeregningsresultatTjeneste {

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private UttakRepository uttakRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    public BeregningsresultatTjenesteImpl() {
        // For CDI
    }

    @Inject
    public BeregningsresultatTjenesteImpl(BehandlingRepositoryProvider behandlingRepositoryProvider, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.inntektArbeidYtelseRepository = behandlingRepositoryProvider.getInntektArbeidYtelseRepository();
        this.beregningsresultatFPRepository = behandlingRepositoryProvider.getBeregningsresultatFPRepository();
        this.uttakRepository = behandlingRepositoryProvider.getUttakRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public Optional<BeregningsresultatMedUttaksplanDto> lagBeregningsresultatMedUttaksplan(Behandling behandling) {
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        return uttakResultat
            .flatMap(uttakResultatPlan -> beregningsresultatFPRepository.hentBeregningsresultatFP(behandling)
                .flatMap(beregningsresultatFP -> inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling))
                    .map(inntektArbeidYtelseGrunnlag -> BeregningsresultatMedUttaksplanMapper.lagBeregningsresultatMedUttaksplan(behandling, uttakResultatPlan, beregningsresultatFP))));
    }

    @Override
    public Optional<BeregningsresultatEngangsstønadDto> lagBeregningsresultatEnkel(Behandling behandling) {
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (behandlingsresultat != null) {
            BeregningResultat beregningResultat = behandlingsresultat.getBeregningResultat();
            if (beregningResultat != null) {
                Optional<Beregning> sisteBeregningOpt = beregningResultat.getSisteBeregning();
                if (sisteBeregningOpt.isPresent()) {
                    BeregningsresultatEngangsstønadDto dto = new BeregningsresultatEngangsstønadDto();
                    Beregning beregning = sisteBeregningOpt.get();
                    dto.setBeregnetTilkjentYtelse(beregning.getBeregnetTilkjentYtelse());
                    dto.setAntallBarn((int) beregning.getAntallBarn());
                    dto.setSatsVerdi(beregning.getSatsVerdi());
                    return Optional.of(dto);
                }
            }
        }
        return Optional.empty();
    }
}
