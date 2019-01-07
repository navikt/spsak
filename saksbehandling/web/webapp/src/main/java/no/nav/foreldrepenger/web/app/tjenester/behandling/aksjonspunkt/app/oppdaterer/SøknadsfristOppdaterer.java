package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad.VM_5007;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.SoknadsfristAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = SoknadsfristAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
public class SøknadsfristOppdaterer implements AksjonspunktOppdaterer<SoknadsfristAksjonspunktDto> {

    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;

    SøknadsfristOppdaterer() {
    }

    @Inject
    SøknadsfristOppdaterer(GrunnlagRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkAdapter) {
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public OppdateringResultat oppdater(SoknadsfristAksjonspunktDto dto, Behandling behandling,
            VilkårResultat.Builder vilkårBuilder) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        historikkAdapter.tekstBuilder().medEndretFelt(HistorikkEndretFeltType.SOKNADSFRISTVILKARET, null, dto.getErVilkarOk() ? HistorikkEndretFeltVerdiType.OPPFYLT : HistorikkEndretFeltVerdiType.IKKE_OPPFYLT)
                .medBegrunnelse(dto.getBegrunnelse(),
                        aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon,
                                dto.getBegrunnelse()))
                .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        if (dto.getErVilkarOk()) {
            vilkårBuilder.leggTilVilkårResultatManueltOppfylt(VilkårType.SØKNADSFRISTVILKÅRET);

            return OppdateringResultat.utenOveropp();
        } else {
            vilkårBuilder.leggTilVilkårResultatManueltIkkeOppfylt(VilkårType.SØKNADSFRISTVILKÅRET, VM_5007, Avslagsårsak.SØKT_FOR_SENT);
            vilkårBuilder.medVilkårResultatType(VilkårResultatType.AVSLÅTT);

            return OppdateringResultat.medFremoverHopp(FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK);
        }
    }
}
