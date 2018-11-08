package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.inngangsvilkaar.InngangsvilkårTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringSøknadsfristvilkåretDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = OverstyringSøknadsfristvilkåretDto.class, adapter=Overstyringshåndterer.class)
public class SøknadsfristvilkårOverstyringshåndterer extends InngangsvilkårOverstyringshåndterer<OverstyringSøknadsfristvilkåretDto> {

    SøknadsfristvilkårOverstyringshåndterer() {
        // for CDI proxy
    }

    @Inject
    public SøknadsfristvilkårOverstyringshåndterer(BehandlingRepositoryProvider repositoryProvider,
            HistorikkTjenesteAdapter historikkAdapter, InngangsvilkårTjeneste inngangsvilkårTjeneste) {
        super(repositoryProvider,
                historikkAdapter,
                AksjonspunktDefinisjon.OVERSTYRING_AV_SØKNADSFRISTVILKÅRET,
                VilkårType.SØKNADSFRISTVILKÅRET,
                inngangsvilkårTjeneste);
    }

    @Override
    protected void lagHistorikkInnslag(Behandling behandling, OverstyringSøknadsfristvilkåretDto dto) {
        lagHistorikkInnslagForOverstyrtVilkår(dto.getBegrunnelse(), dto.getErVilkarOk(), dto.getKode(), behandling);
    }

}
