package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.inngangsvilkaar.InngangsvilkårTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringFødselsvilkåretDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = OverstyringFødselsvilkåretDto.class, adapter = Overstyringshåndterer.class)
public class FødselsvilkåretOverstyringshåndterer extends InngangsvilkårOverstyringshåndterer<OverstyringFødselsvilkåretDto> {

    FødselsvilkåretOverstyringshåndterer() {
        // for CDI proxy
    }

    @Inject
    public FødselsvilkåretOverstyringshåndterer(BehandlingRepositoryProvider repositoryProvider,
            HistorikkTjenesteAdapter historikkAdapter,
            InngangsvilkårTjeneste inngangsvilkårTjeneste) {
        super(repositoryProvider, historikkAdapter,
                AksjonspunktDefinisjon.OVERSTYRING_AV_FØDSELSVILKÅRET,
                VilkårType.FØDSELSVILKÅRET_MOR,
                inngangsvilkårTjeneste);
    }

    @Override
    protected void lagHistorikkInnslag(Behandling behandling, OverstyringFødselsvilkåretDto dto) {
        lagHistorikkInnslagForOverstyrtVilkår(dto.getBegrunnelse(), dto.getErVilkarOk(), dto.getKode(), behandling);
    }
}
