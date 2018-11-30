package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.InngangsvilkårTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringSokersOpplysingspliktDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = OverstyringSokersOpplysingspliktDto.class, adapter = Overstyringshåndterer.class)
public class SøkersOpplysningspliktOverstyringshåndterer extends AbstractOverstyringshåndterer<OverstyringSokersOpplysingspliktDto> {

    private InngangsvilkårTjeneste inngangsvilkårTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;


    SøkersOpplysningspliktOverstyringshåndterer() {
        // for CDI proxy
    }

    @Inject
    public SøkersOpplysningspliktOverstyringshåndterer(BehandlingRepositoryProvider repositoryProvider,
                                                       HistorikkTjenesteAdapter historikkAdapter,
                                                       InngangsvilkårTjeneste inngangsvilkårTjeneste) {
        super(repositoryProvider, historikkAdapter, AksjonspunktDefinisjon.SØKERS_OPPLYSNINGSPLIKT_OVST);
        this.inngangsvilkårTjeneste = inngangsvilkårTjeneste;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();

    }

    @Override
    protected void lagHistorikkInnslag(Behandling behandling, OverstyringSokersOpplysingspliktDto dto) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        leggTilEndretFeltIHistorikkInnslag(dto.getBegrunnelse(), dto.getErVilkarOk(), aksjonspunktDefinisjon, behandling);
    }

    @Override
    public OppdateringResultat håndterOverstyring(OverstyringSokersOpplysingspliktDto dto, Behandling behandling,
                                                  BehandlingskontrollKontekst kontekst) {

        VilkårUtfallType utfall = dto.getErVilkarOk() ? VilkårUtfallType.OPPFYLT : VilkårUtfallType.IKKE_OPPFYLT;
        inngangsvilkårTjeneste.overstyrAksjonspunktForSøkersopplysningsplikt(behandling.getId(), utfall, kontekst);

        if (dto.getErVilkarOk()) {
            return OppdateringResultat.utenOveropp();
        } else {
            return OppdateringResultat.medFremoverHopp(FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK);
        }

    }

    private void leggTilEndretFeltIHistorikkInnslag(String begrunnelse, boolean vilkårOppfylt, AksjonspunktDefinisjon aksjonspunktDefinisjon, Behandling behandling) {
        HistorikkEndretFeltVerdiType tilVerdi = vilkårOppfylt ? HistorikkEndretFeltVerdiType.OPPFYLT : HistorikkEndretFeltVerdiType.IKKE_OPPFYLT;

        HistorikkInnslagTekstBuilder tekstBuilder = getHistorikkAdapter().tekstBuilder();
        if (begrunnelse != null) {
            tekstBuilder.medBegrunnelse(begrunnelse);
        }
        tekstBuilder.medEndretFelt(HistorikkEndretFeltType.SOKERSOPPLYSNINGSPLIKT, null, tilVerdi)
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);

    }
}
