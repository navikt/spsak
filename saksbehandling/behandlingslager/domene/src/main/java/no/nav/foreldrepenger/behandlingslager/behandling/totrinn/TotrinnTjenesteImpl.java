package no.nav.foreldrepenger.behandlingslager.behandling.totrinn;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;

@ApplicationScoped
public class TotrinnTjenesteImpl implements TotrinnTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private UttakRepository uttakRepository;
    private TotrinnRepository totrinnRepository;

    TotrinnTjenesteImpl() {
        // CDI
    }

    @Inject
    public TotrinnTjenesteImpl(GrunnlagRepositoryProvider repositoryProvider, ResultatRepositoryProvider resultatRepositoryProvider, TotrinnRepository totrinnRepository) {
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        this.uttakRepository = resultatRepositoryProvider.getUttakRepository();
        this.totrinnRepository = totrinnRepository;
    }


    @Override
    public void settNyttTotrinnsgrunnlag(Behandling behandling) {
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        Optional<Long> inntektArbeidYtelseGrunnlagIdOpt = inntektArbeidYtelseRepository.hentIdPåAktivInntektArbeidYtelse(behandling);
        Optional<UttakResultatEntitet> uttakResultatOpt = uttakRepository.hentUttakResultatHvisEksisterer(behandling);

        Long inntektArbeidYtelseGrunnlagId = inntektArbeidYtelseGrunnlagIdOpt.orElse(null);
        Long uttakResultatId = uttakResultatOpt.map(UttakResultatEntitet::getId).orElse(null);
        Long beregningsgrunnlagId = beregningsgrunnlagOpt.map(Beregningsgrunnlag::getId).orElse(null);

        Totrinnresultatgrunnlag totrinnsresultatgrunnlag = new Totrinnresultatgrunnlag(behandling,
            inntektArbeidYtelseGrunnlagId,
            uttakResultatId,
            beregningsgrunnlagId);

        totrinnRepository.lagreOgFlush(behandling, totrinnsresultatgrunnlag);
    }

    @Override
    public Optional<Totrinnresultatgrunnlag> hentTotrinngrunnlagHvisEksisterer(Behandling behandling) {
        return totrinnRepository.hentTotrinngrunnlag(behandling);
    }

    @Override
    public Collection<Totrinnsvurdering> hentTotrinnaksjonspunktvurderinger(Behandling behandling) {
        return totrinnRepository.hentTotrinnaksjonspunktvurderinger(behandling);
    }

    @Override
    public void settNyeTotrinnaksjonspunktvurderinger(Behandling behandling, List<Totrinnsvurdering> vurderinger) {
        totrinnRepository.lagreOgFlush(behandling, vurderinger);
    }
}
