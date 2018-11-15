package no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl;

import static no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.BeregningsgrunnlagSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.beregningsgrunnlag.FullføreBeregningsgrunnlag;

@BehandlingStegRef(kode = "FAST_BERGRUNN")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class FastsettBeregningsgrunnlagStegImpl implements BeregningsgrunnlagSteg {

    private BehandlingRepository behandlingRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private FullføreBeregningsgrunnlag fullføreBeregningsgrunnlag;
    private BehandlingRepositoryProvider repositoryProvider;

    FastsettBeregningsgrunnlagStegImpl() {
        // CDI Proxy
    }

    @Inject
    public FastsettBeregningsgrunnlagStegImpl(BehandlingRepositoryProvider repositoryProvider, FullføreBeregningsgrunnlag fullføreBeregningsgrunnlag) {
        this.repositoryProvider = repositoryProvider;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.fullføreBeregningsgrunnlag = fullføreBeregningsgrunnlag;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);

        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlag.fullføreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        beregningsgrunnlagRepository.lagre(behandling, fullførtBeregningsgrunnlag, BeregningsgrunnlagTilstand.FASTSATT);
        VilkårResultat vilkårResultat = behandling.getBehandlingsresultat().getVilkårResultat();
        behandlingRepository.lagre(vilkårResultat, kontekst.getSkriveLås());

        if (VilkårResultatType.INNVILGET.equals(vilkårResultat.getVilkårResultatType())) {
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        } else {
            return BehandleStegResultat.fremoverført(FREMHOPP_TIL_FORESLÅ_VEDTAK);
        }
    }


    @Override
    public void vedTransisjon(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, TransisjonType transisjonType, BehandlingStegType førsteSteg, BehandlingStegType sisteSteg, TransisjonType skalTil) {
        if (transisjonType.equals(TransisjonType.HOPP_OVER_BAKOVER)) {
            RyddBeregningsgrunnlag ryddBeregningsgrunnlag = new RyddBeregningsgrunnlag(repositoryProvider, behandling, kontekst);
            ryddBeregningsgrunnlag.ryddFastsettBeregningsgrunnlagVedTilbakeføring();
        }

        if (transisjonType.equals(TransisjonType.HOPP_OVER_FRAMOVER)) {
            // FIXME SP - var tidligere sjekk på at sisteSteg == SØKNADSFRIST_FORELDREPENGER.  Trengs det lenger?
            
            if (behandling.erRevurdering()) {
                // Kopier beregningsgrunnlag fra original, da uttaksresultat avhenger av denne
                behandling.getOriginalBehandling()
                    .flatMap(origBehandling -> beregningsgrunnlagRepository.hentBeregningsgrunnlag(origBehandling))
                    .ifPresent(origBeregningsgrunnlag -> beregningsgrunnlagRepository.lagre(behandling, origBeregningsgrunnlag, BeregningsgrunnlagTilstand.FASTSATT));
            }
        }
    }
}
