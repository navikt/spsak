package no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.app;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnresultatgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.VurderÅrsakTotrinnsvurdering;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.dto.TotrinnskontrollAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.dto.TotrinnskontrollVurderÅrsak;

@ApplicationScoped
public class TotrinnsaksjonspunktDtoTjeneste {
    private TotrinnsBeregningDtoTjeneste totrinnsBeregningDtoTjeneste;
    private TotrinnskontrollAktivitetDtoTjeneste totrinnskontrollAktivitetDtoTjeneste;
    private TotrinnArbeidsforholdDtoTjeneste totrinnArbeidsforholdDtoTjeneste;


    protected TotrinnsaksjonspunktDtoTjeneste() {
        // for CDI proxy
    }


    @Inject
    public TotrinnsaksjonspunktDtoTjeneste(TotrinnsBeregningDtoTjeneste totrinnsBeregningDtoTjeneste,
                                           TotrinnArbeidsforholdDtoTjeneste totrinnArbeidsforholdDtoTjeneste,
                                           TotrinnskontrollAktivitetDtoTjeneste totrinnskontrollAktivitetDtoTjeneste) {
        this.totrinnskontrollAktivitetDtoTjeneste = totrinnskontrollAktivitetDtoTjeneste;
        this.totrinnsBeregningDtoTjeneste = totrinnsBeregningDtoTjeneste;
        this.totrinnArbeidsforholdDtoTjeneste = totrinnArbeidsforholdDtoTjeneste;
    }

    public TotrinnskontrollAksjonspunkterDto lagTotrinnskontrollAksjonspunktDto(Totrinnsvurdering aksjonspunkt,
                                                                                 Behandling behandling,
                                                                                 Optional<Totrinnresultatgrunnlag> totrinnresultatgrunnlag) {
        return new TotrinnskontrollAksjonspunkterDto.Builder()
            .medAksjonspunktKode(aksjonspunkt.getAksjonspunktDefinisjon().getKode())
            .medOpptjeningAktiviteter(totrinnskontrollAktivitetDtoTjeneste.hentAktiviterEndretForOpptjening(aksjonspunkt, behandling,
                totrinnresultatgrunnlag.flatMap(Totrinnresultatgrunnlag::getInntektArbeidYtelseGrunnlagId)))
            .medBeregningDto(totrinnsBeregningDtoTjeneste.hentBeregningDto(aksjonspunkt, behandling, totrinnresultatgrunnlag.flatMap(Totrinnresultatgrunnlag::getBeregningsgrunnlagId)))
            .medBesluttersBegrunnelse(aksjonspunkt.getBegrunnelse())
            .medArbeidsforhold(totrinnArbeidsforholdDtoTjeneste.hentArbeidsforhold(aksjonspunkt, behandling, totrinnresultatgrunnlag.flatMap(Totrinnresultatgrunnlag::getInntektArbeidYtelseGrunnlagId)))
            .medTotrinnskontrollGodkjent(aksjonspunkt.isGodkjent())
            .medVurderPaNyttArsaker(hentVurderPåNyttÅrsaker(aksjonspunkt))
            .build();
    }

    private Set<TotrinnskontrollVurderÅrsak> hentVurderPåNyttÅrsaker(Totrinnsvurdering aksjonspunkt) {
        return aksjonspunkt.getVurderPåNyttÅrsaker().stream()
            .map(VurderÅrsakTotrinnsvurdering::getÅrsaksType)
            .map(arsakType -> new TotrinnskontrollVurderÅrsak(arsakType.getKode(), arsakType.getNavn()))
            .collect(Collectors.toSet());
    }
}
