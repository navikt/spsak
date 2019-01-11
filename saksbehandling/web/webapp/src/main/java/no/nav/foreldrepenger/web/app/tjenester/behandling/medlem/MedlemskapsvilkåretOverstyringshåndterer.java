package no.nav.foreldrepenger.web.app.tjenester.behandling.medlem;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.InngangsvilkårTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring.InngangsvilkårOverstyringshåndterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring.Overstyringshåndterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringMedlemskapsvilkåretDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = OverstyringMedlemskapsvilkåretDto.class, adapter = Overstyringshåndterer.class)
public class MedlemskapsvilkåretOverstyringshåndterer extends InngangsvilkårOverstyringshåndterer<OverstyringMedlemskapsvilkåretDto> {

    protected MedlemskapsvilkåretOverstyringshåndterer() {
        // for CDI proxy
    }

    @Inject
    public MedlemskapsvilkåretOverstyringshåndterer(GrunnlagRepositoryProvider repositoryProvider,
                                                    HistorikkTjenesteAdapter historikkAdapter,
                                                    InngangsvilkårTjeneste inngangsvilkårTjeneste) {
        super(repositoryProvider,
                historikkAdapter,
                AksjonspunktDefinisjon.OVERSTYRING_AV_MEDLEMSKAPSVILKÅRET,
                VilkårType.MEDLEMSKAPSVILKÅRET,
                inngangsvilkårTjeneste);
    }

    @Override
    protected void lagHistorikkInnslag(Behandling behandling, OverstyringMedlemskapsvilkåretDto dto) {
        lagHistorikkInnslagForOverstyrtVilkår(dto.getBegrunnelse(), dto.getErVilkarOk(), dto.getKode(), behandling);
    }

  }
