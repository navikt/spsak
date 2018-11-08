package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidFeil;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakBeregningsandelTjeneste;
import no.nav.vedtak.feil.FeilFactory;

@ApplicationScoped
public class UttakBeregningsandelTjenesteImpl implements UttakBeregningsandelTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    UttakBeregningsandelTjenesteImpl() {
        //CDI
    }

    @Inject
    public UttakBeregningsandelTjenesteImpl(BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    @Override
    public List<BeregningsgrunnlagPrStatusOgAndel> hentAndeler(Behandling behandling) {
        Optional<Beregningsgrunnlag> beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        if (beregningsgrunnlag.isPresent()) {
            return beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().stream()
                .flatMap(beregningsgrunnlagPeriode -> beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .collect(Collectors.toList());
        }
        throw FeilFactory.create(UttakArbeidFeil.class).manglendeBeregningsgrunnlag(behandling).toException();
    }
}
