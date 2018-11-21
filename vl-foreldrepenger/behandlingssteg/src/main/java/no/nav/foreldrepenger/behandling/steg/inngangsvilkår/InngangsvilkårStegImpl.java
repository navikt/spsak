package no.nav.foreldrepenger.behandling.steg.inngangsvilkår;

import static no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK;
import static no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner.FREMHOPP_TIL_UTTAKSPLAN;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandling.steg.inngangsvilkår.api.InngangsvilkårSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelOrkestrerer;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelResultat;

public abstract class InngangsvilkårStegImpl implements InngangsvilkårSteg {
    private BehandlingRepository behandlingRepository;
    private RegelOrkestrerer regelOrkestrerer;
    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingStegType  behandlingStegType;

    public InngangsvilkårStegImpl(BehandlingRepositoryProvider repositoryProvider, RegelOrkestrerer regelOrkestrerer, BehandlingStegType behandlingStegType) {
        this.repositoryProvider = repositoryProvider;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.regelOrkestrerer = regelOrkestrerer;
        this.behandlingStegType = behandlingStegType;
    }

    protected InngangsvilkårStegImpl() {
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        // Hent behandlingsgrunnlag og vilkårtyper
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        List<VilkårType> vilkårHåndtertAvSteg = vilkårHåndtertAvSteg();
        List<VilkårType> vilkårTyper = behandling.getBehandlingsresultat().getVilkårResultat().getVilkårene().stream()
            .map(Vilkår::getVilkårType)
            .filter(vilkårType -> vilkårHåndtertAvSteg.contains(vilkårType))
            .collect(Collectors.toList());
        if (!(vilkårHåndtertAvSteg.isEmpty() || !vilkårTyper.isEmpty())) {
            throw new IllegalArgumentException(String.format("Utviklerfeil: Steg[%s] håndterer ikke angitte vilkår %s", this.getClass(), vilkårTyper)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Kall regelmotor
        RegelResultat regelResultat = regelOrkestrerer.vurderInngangsvilkår(vilkårTyper, behandling);

        // Oppdater behandling
        behandlingRepository.lagre(regelResultat.getVilkårResultat(), kontekst.getSkriveLås());

        utførtRegler(kontekst, behandling, regelResultat);

        // Returner behandlingsresultat
        if (erNoenVilkår(regelResultat, VilkårUtfallType.IKKE_OPPFYLT) && !harÅpentOverstyringspunktForInneværendeSteg(behandling)) {
            // Forbedring: InngangsvilkårStegImpl som annoterbar med FagsakYtelseType og BehandlingType
            // Her hardkodes disse parameterne
            if (behandling.erRevurdering() && behandling.getFagsak().getYtelseType().equals(FagsakYtelseType.FORELDREPENGER)) {
                return BehandleStegResultat.fremoverførtMedAksjonspunkter(FREMHOPP_TIL_UTTAKSPLAN, regelResultat.getAksjonspunktDefinisjoner());
            }
            return stegResultatVilkårIkkeOppfylt(regelResultat);
        } else {
            return stegResultat(regelResultat);
        }
    }

    private boolean harÅpentOverstyringspunktForInneværendeSteg(Behandling behandling) {
        return behandling.getÅpneAksjonspunkter().stream()
            .filter(aksjonspunkt -> aksjonspunkt.getAksjonspunktDefinisjon().getAksjonspunktType().equals(AksjonspunktType.OVERSTYRING))
            .anyMatch(aksjonspunkt ->
            aksjonspunkt.getAksjonspunktDefinisjon().getVurderingspunktDefinisjon().getBehandlingSteg().equals(behandlingStegType));
    }

    protected BehandleStegResultat stegResultat(RegelResultat regelResultat) {
        return BehandleStegResultat.utførtMedAksjonspunkter(regelResultat.getAksjonspunktDefinisjoner());
    }

    protected BehandleStegResultat stegResultatVilkårIkkeOppfylt(RegelResultat regelResultat) {
        return BehandleStegResultat.fremoverførtMedAksjonspunkter(FREMHOPP_TIL_FORESLÅ_VEDTAK, regelResultat.getAksjonspunktDefinisjoner());
    }

    @SuppressWarnings("unused")
    protected void utførtRegler(BehandlingskontrollKontekst kontekst, Behandling behandling, RegelResultat regelResultat) {
        // template method
    }

    protected boolean erNoenVilkår(RegelResultat regelResultat, VilkårUtfallType vilkårUtfall) {
        return regelResultat.getVilkårResultat().getVilkårene().stream()
            .filter(vilkår -> vilkårHåndtertAvSteg().contains(vilkår.getVilkårType()))
            .anyMatch(v -> v.getGjeldendeVilkårUtfall().equals(vilkårUtfall));
    }

    @Override
    public void vedTransisjon(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell,
                              TransisjonType transisjonType, BehandlingStegType førsteSteg, BehandlingStegType sisteSteg, TransisjonType skalTil) {

        RyddVilkårTyper ryddVilkårTyper = new RyddVilkårTyper(modell, repositoryProvider, behandling, kontekst);
        if (Objects.equals(TransisjonType.HOPP_OVER_BAKOVER, transisjonType)) {
            if (!(førsteSteg!= null && førsteSteg.equals(behandlingStegType) && skalTil.equals(TransisjonType.ETTER_UTGANG))){
                ryddVilkårTyper.ryddVedTilbakeføring(vilkårHåndtertAvSteg());
            }
        } else if (Objects.equals(TransisjonType.HOPP_OVER_FRAMOVER, transisjonType)) {
            ryddVilkårTyper.ryddVedOverhoppFramover(vilkårHåndtertAvSteg());
        }
    }

}
