package no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.BeregningsgrunnlagSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.ForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper.BeregningsgrunnlagRegelResultat;

@BehandlingStegRef(kode = "FORS_BERGRUNN")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
@Named("ForeslåBeregningsgrunnlagSteg") //(Ramesh) Midlertidig
public class ForeslåBeregningsgrunnlagStegImpl implements BeregningsgrunnlagSteg {

    private BehandlingRepository behandlingRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlag;

    public ForeslåBeregningsgrunnlagStegImpl() {
        // for CDI proxy
    }

    @Inject
    public ForeslåBeregningsgrunnlagStegImpl(ResultatRepositoryProvider repositoryProvider,
                                             ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlag) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.foreslåBeregningsgrunnlag = foreslåBeregningsgrunnlag;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);
        List<AksjonspunktDefinisjon> aksjonspunkter = resultat.getAksjonspunkter();
        beregningsgrunnlagRepository.lagre(behandling, resultat.getBeregningsgrunnlag(), BeregningsgrunnlagTilstand.FORESLÅTT);
        if (aksjonspunkter.isEmpty()) {
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        } else {
            return BehandleStegResultat.utførtMedAksjonspunkter(aksjonspunkter);
        }
    }

    @Override
    public void vedHoppOverBakover(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, BehandlingStegType tilSteg, BehandlingStegType fraSteg) {
        RyddBeregningsgrunnlag ryddBeregningsgrunnlag = new RyddBeregningsgrunnlag(behandlingRepository, beregningsgrunnlagRepository, behandling, kontekst);
        ryddBeregningsgrunnlag.ryddForeslåBeregningsgrunnlagVedTilbakeføring();
    }
}
