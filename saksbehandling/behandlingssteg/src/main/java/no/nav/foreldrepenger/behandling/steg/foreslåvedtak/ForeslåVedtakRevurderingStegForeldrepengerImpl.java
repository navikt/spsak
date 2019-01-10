package no.nav.foreldrepenger.behandling.steg.foreslåvedtak;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;


@BehandlingStegRef(kode = "FORVEDSTEG")
@BehandlingTypeRef("BT-004") //Revurdering
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class ForeslåVedtakRevurderingStegForeldrepengerImpl implements ForeslåVedtakSteg {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private BehandlingRepository behandlingRepository;
    private ForeslåVedtakTjeneste foreslåVedtakTjeneste;

    ForeslåVedtakRevurderingStegForeldrepengerImpl() {
        // for CDI proxy
    }

    @Inject
    public ForeslåVedtakRevurderingStegForeldrepengerImpl(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                                          BehandlingRepository behandlingRepository,
                                                          @FagsakYtelseTypeRef("FP")  ForeslåVedtakTjeneste foreslåVedtakTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.behandlingRepository = behandlingRepository;
        this.foreslåVedtakTjeneste = foreslåVedtakTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst){
        Behandling revurdering = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(kontekst.getBehandlingId());
        Behandling orginalBehandling = revurdering.getOriginalBehandling()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Revurdering skal alltid ha orginal behandling"));
        BehandleStegResultat behandleStegResultat = foreslåVedtakTjeneste.foreslåVedtak(revurdering);

        if (!beregningsgrunnlagEksisterer(revurdering) || behandlingsresultat.isBehandlingsresultatAvslått()) {
            return behandleStegResultat;
        }
        //Oppretter aksjonspunkt dersom revurdering har mindre beregningsgrunnlag enn orginal
        if (erRevurderingensBeregningsgrunnlagMindreEnnOrginal(orginalBehandling,revurdering)) {
            List<AksjonspunktDefinisjon> aksjonspunkter = behandleStegResultat.getAksjonspunktResultater().stream()
                .map(AksjonspunktResultat::getAksjonspunktDefinisjon).collect(Collectors.toList());
            aksjonspunkter.add(AksjonspunktDefinisjon.KONTROLLER_REVURDERINGSBEHANDLING);
            return BehandleStegResultat.utførtMedAksjonspunkter(aksjonspunkter);
        }
        return behandleStegResultat;
    }

    private boolean beregningsgrunnlagEksisterer(Behandling behandling) {
        return beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling).isPresent();
    }

    private boolean erRevurderingensBeregningsgrunnlagMindreEnnOrginal(Behandling orginalBehandling, Behandling revurdering){
        Beregningsgrunnlag orginalBeregning = beregningsgrunnlagRepository.hentBeregningsgrunnlag(orginalBehandling)
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Skal ha Beregningsgrunnlag på orginalbehandling vedtak"));
        Beregningsgrunnlag revurderingsBeregning = beregningsgrunnlagRepository.hentBeregningsgrunnlag(revurdering)
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Skal ha Beregningsgrunnlag på positivt vedtak"));

        BigDecimal orginalBeregningSumBruttoPrÅr = orginalBeregning.getBeregningsgrunnlagPerioder().stream()
            .map(BeregningsgrunnlagPeriode::getBruttoPrÅr).reduce(new BigDecimal(0), BigDecimal::add);
        BigDecimal revurderingsBeregningSumBruttoPrÅr = revurderingsBeregning.getBeregningsgrunnlagPerioder().stream()
            .map(BeregningsgrunnlagPeriode::getBruttoPrÅr).reduce(new BigDecimal(0), BigDecimal::add);

        return revurderingsBeregningSumBruttoPrÅr.compareTo(orginalBeregningSumBruttoPrÅr) < 0;
    }

    @Override
    public void vedHoppOverBakover(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, BehandlingStegType tilSteg, BehandlingStegType fraSteg) {
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(kontekst.getBehandlingId());
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultat)
            .fjernKonsekvenserForYtelsen()
            .buildFor(behandling);
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }

}
