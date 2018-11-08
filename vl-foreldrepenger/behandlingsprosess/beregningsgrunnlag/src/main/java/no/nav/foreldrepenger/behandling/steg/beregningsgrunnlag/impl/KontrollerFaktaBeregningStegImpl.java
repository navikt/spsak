package no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.BeregningsgrunnlagSteg;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.beregningsgrunnlag.AksjonspunktUtlederForBeregning;
import no.nav.foreldrepenger.beregningsgrunnlag.BeregningInfotrygdsakTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.OpprettBeregningsgrunnlagTjeneste;

@BehandlingStegRef(kode = "KOFAKBER")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
@Named("KontrollerFaktaBeregning")
public class KontrollerFaktaBeregningStegImpl implements BeregningsgrunnlagSteg {
    private BehandlingRepositoryProvider repositoryProvider;
    private OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste;
    private BeregningInfotrygdsakTjeneste beregningInfotrygdsakTjeneste;
    private BehandlingRepository behandlingRepository;
    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;

    protected KontrollerFaktaBeregningStegImpl() {
        // for CDI proxy
    }

    @Inject
    public KontrollerFaktaBeregningStegImpl(BehandlingRepositoryProvider repositoryProvider,
                                            AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning,
                                            OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste,
                                            BeregningInfotrygdsakTjeneste beregningInfotrygdsakTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.opprettBeregningsgrunnlagTjeneste = opprettBeregningsgrunnlagTjeneste;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.aksjonspunktUtlederForBeregning = aksjonspunktUtlederForBeregning;
        this.beregningInfotrygdsakTjeneste = beregningInfotrygdsakTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        Optional<BehandleStegResultat> stegResultat = beregningInfotrygdsakTjeneste.vurderOgOppdaterSakSomBehandlesAvInfotrygd(behandling);
        if (stegResultat.isPresent()) {
            return stegResultat.get();
        }
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
        RyddBeregningsgrunnlag ryddBeregningsgrunnlag = new RyddBeregningsgrunnlag(repositoryProvider, behandling, kontekst);
        if (BehandlingStegType.KONTROLLER_FAKTA_BEREGNING.equals(tilSteg)) {
            ryddBeregningsgrunnlag.gjenopprettFørsteBeregningsgrunnlag();
        } else {
            ryddBeregningsgrunnlag.ryddKontrollFaktaBeregningVedTilbakeføring();
        }
    }

}
