package no.nav.foreldrepenger.domene.inngangsvilkaar.overstyring;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AbstractOverstyringshåndterer;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OppdateringResultat;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OverstyringAksjonspunktDto;
import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.InngangsvilkårTjeneste;

public abstract class InngangsvilkårOverstyringshåndterer<T extends OverstyringAksjonspunktDto> extends AbstractOverstyringshåndterer<T> {

    private VilkårType vilkårType;
    private InngangsvilkårTjeneste inngangsvilkårTjeneste;

    protected InngangsvilkårOverstyringshåndterer() {
        // for CDI proxy
    }

    public InngangsvilkårOverstyringshåndterer(GrunnlagRepositoryProvider repositoryProvider,
                                               HistorikkTjenesteAdapter historikkAdapter,
                                               AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                               VilkårType vilkårType,
                                               InngangsvilkårTjeneste inngangsvilkårTjeneste) {
        super(repositoryProvider, historikkAdapter, aksjonspunktDefinisjon);
        this.vilkårType = vilkårType;
        this.inngangsvilkårTjeneste = inngangsvilkårTjeneste;
    }

    @Override
    public OppdateringResultat håndterOverstyring(T dto, Behandling behandling, BehandlingskontrollKontekst kontekst) {
        VilkårUtfallType utfall = dto.getErVilkarOk() ? VilkårUtfallType.OPPFYLT : VilkårUtfallType.IKKE_OPPFYLT;

        inngangsvilkårTjeneste.overstyrAksjonspunkt(behandling.getId(), vilkårType, utfall, dto.getAvslagskode(), kontekst);

        if (utfall.equals(VilkårUtfallType.IKKE_OPPFYLT)) {
            // Forbedring: InngangsvilkårOverstyringshåndterer som annoterbar med FagsakYtelseType og BehandlingType
            // Her hardkodes disse parameterne
            if (behandling.getFagsak().getYtelseType().equals(FagsakYtelseType.FORELDREPENGER) && behandling.erRevurdering()) {
                return OppdateringResultat.medFremoverHopp(FellesTransisjoner.FREMHOPP_TIL_UTTAKSPLAN);
            }
            return OppdateringResultat.medFremoverHopp(FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK);
        }

        return OppdateringResultat.utenOveropp();
    }

}
