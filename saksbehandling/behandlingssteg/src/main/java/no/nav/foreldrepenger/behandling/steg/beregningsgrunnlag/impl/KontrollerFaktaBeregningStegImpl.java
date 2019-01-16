package no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.BeregningsgrunnlagSteg;
import no.nav.foreldrepenger.behandlingskontroll.*;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.AksjonspunktUtlederForBeregning;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.OpprettBeregningsgrunnlagTjeneste;

@BehandlingStegRef(kode = "KOFAKBER")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
@Named("KontrollerFaktaBeregning")
public class KontrollerFaktaBeregningStegImpl implements BeregningsgrunnlagSteg {
    private OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste;
    private BehandlingRepository behandlingRepository;
    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    protected KontrollerFaktaBeregningStegImpl() {
        // for CDI proxy
    }

    @Inject
    public KontrollerFaktaBeregningStegImpl(ResultatRepositoryProvider repositoryProvider,
                                            AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning,
                                            OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste) {
        this.opprettBeregningsgrunnlagTjeneste = opprettBeregningsgrunnlagTjeneste;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.aksjonspunktUtlederForBeregning = aksjonspunktUtlederForBeregning;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        opprettBeregningsgrunnlagTjeneste.opprettOgLagreBeregningsgrunnlag(behandling);
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);
        if (aksjonspunkter.isEmpty()) {
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        } else {
            return BehandleStegResultat.utførtMedAksjonspunktResultater(aksjonspunkter);
        }
    }

    @Override
    public void vedHoppOverBakover(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, BehandlingStegType tilSteg, BehandlingStegType fraSteg) {
        RyddBeregningsgrunnlag ryddBeregningsgrunnlag = new RyddBeregningsgrunnlag(behandlingRepository, beregningsgrunnlagRepository, behandling, kontekst);
        if (BehandlingStegType.KONTROLLER_FAKTA_BEREGNING.equals(tilSteg)) {
            ryddBeregningsgrunnlag.gjenopprettFørsteBeregningsgrunnlag();
        } else {
            ryddBeregningsgrunnlag.ryddKontrollFaktaBeregningVedTilbakeføring();
        }
    }

}
