package no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.app;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FaktaOmBeregningTilfelleTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.dto.TotrinnsBeregningDto;

@ApplicationScoped
public class TotrinnsBeregningDtoTjeneste {
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;


    protected TotrinnsBeregningDtoTjeneste() {
        // for CDI proxy
    }

    @Inject
    public TotrinnsBeregningDtoTjeneste(ResultatRepositoryProvider repositoryProvider,
                                        FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste) {
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.faktaOmBeregningTilfelleTjeneste = faktaOmBeregningTilfelleTjeneste;
    }

    TotrinnsBeregningDto hentBeregningDto(Totrinnsvurdering aksjonspunkt,
                                                  Behandling behandling,
                                                  Optional<Long> beregningsgrunnlagId) {
        TotrinnsBeregningDto dto = new TotrinnsBeregningDto();
        if (aksjonspunkt.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE)) {
            if (beregningsgrunnlagId.isPresent()) {
                dto.setFastsattVarigEndringNaering(erVarigEndringFastsattForSelvstendingNæringsdrivende(beregningsgrunnlagId.get()));
            } else {
                dto.setFastsattVarigEndringNaering(erVarigEndringFastsattForSelvstendingNæringsdrivende(behandling));
            }
        }
        if (AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN.equals(aksjonspunkt.getAksjonspunktDefinisjon())) {
            Beregningsgrunnlag bg = hentBeregningsgrunnlag(behandling, beregningsgrunnlagId);
            List<FaktaOmBeregningTilfelle> tilfeller = bg.getFaktaOmBeregningTilfeller();
            if (!tilfeller.isEmpty()) {
                dto.setFaktaOmBeregningTilfeller(tilfeller);
            } else {
                dto.setFaktaOmBeregningTilfeller(faktaOmBeregningTilfelleTjeneste.finnTilfellerForFellesAksjonspunkt(behandling));
            }
        }
        return dto;
    }

    private Beregningsgrunnlag hentBeregningsgrunnlag(Behandling behandling, Optional<Long> beregningsgrunnlagId) {
        if (beregningsgrunnlagId.isPresent()) {
            return beregningsgrunnlagRepository.hentBeregningsgrunnlag(beregningsgrunnlagId.get())
                .orElseThrow(() -> new IllegalStateException("Fant ikkje beregningsgrunnlag med id " + beregningsgrunnlagId.get()));
        } else {
            return beregningsgrunnlagRepository.hentAggregat(behandling);
        }
    }

    private boolean erVarigEndringFastsattForSelvstendingNæringsdrivende(Behandling behandling) {
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);

        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(bgps -> bgps.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
            .anyMatch(andel -> andel.getOverstyrtPrÅr() != null);
    }

    private boolean erVarigEndringFastsattForSelvstendingNæringsdrivende(Long beregningsgrunnlagId) {
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlag(beregningsgrunnlagId)
            .orElseThrow(() ->
                new IllegalStateException("Fant ingen beregningsgrunnlag med id " + beregningsgrunnlagId.toString()));
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(bgps -> bgps.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
            .anyMatch(andel -> andel.getOverstyrtPrÅr() != null);
    }
}
