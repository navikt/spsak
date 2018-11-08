package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvslagbartAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurdereYtelseSammeBarnAnnenForelderAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurdereYtelseSammeBarnSøkerAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

/**
 * Håndterer oppdatering av Aksjonspunkt og endringshistorikk ved vurdering av ytelse knyttet til samme barn.
 */
abstract class VurdereYtelseSammeBarnOppdaterer implements AksjonspunktOppdaterer<AvslagbartAksjonspunktDto> {

    private VilkårKodeverkRepository vilkårKodeverkRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private HistorikkTjenesteAdapter historikkAdapter;

    VurdereYtelseSammeBarnOppdaterer() {
        // for CDI proxy
    }

    @Inject
    VurdereYtelseSammeBarnOppdaterer(BehandlingRepositoryProvider repositoryProvider,
            HistorikkTjenesteAdapter historikkAdapter) {
        this.historikkAdapter = historikkAdapter;
        this.vilkårKodeverkRepository = repositoryProvider.getVilkårKodeverkRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public OppdateringResultat oppdater(AvslagbartAksjonspunktDto dto, Behandling behandling,
            VilkårResultat.Builder vilkårBuilder) {

        Optional<Vilkår> relevantVilkår = finnRelevantVilkår(behandling);
        if (relevantVilkår.isPresent()) {
            Vilkår vilkår = relevantVilkår.get();
            endringsHåndtering(behandling, vilkår, dto, finnTekstForFelt(vilkår));

            if (dto.getErVilkarOk()) {
                vilkårBuilder.leggTilVilkårResultatManueltOppfylt(vilkår.getVilkårType());

                return OppdateringResultat.utenOveropp();
            } else {
                Avslagsårsak avslagsårsak = dto.getAvslagskode() == null ? null
                        : vilkårKodeverkRepository.finnAvslagÅrsak(dto.getAvslagskode());
                vilkårBuilder.leggTilVilkårResultatManueltIkkeOppfylt(vilkår.getVilkårType(), avslagsårsak);

                vilkårBuilder.medVilkårResultatType(VilkårResultatType.AVSLÅTT);

                return OppdateringResultat.medFremoverHopp(FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK);
            }
        }
        return OppdateringResultat.utenOveropp();

    }

    private void endringsHåndtering(Behandling behandling, Vilkår vilkår, AvslagbartAksjonspunktDto dto, HistorikkEndretFeltType historikkEndretFeltType) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        new HistorikkAksjonspunktAdapter(behandling, historikkAdapter, aksjonspunktRepository)
                .håndterAksjonspunkt(aksjonspunktDefinisjon, vilkår, dto.getErVilkarOk(), dto.getBegrunnelse(), historikkEndretFeltType);
    }

    private HistorikkEndretFeltType finnTekstForFelt(Vilkår vilkår) {
        VilkårType vilkårType = vilkår.getVilkårType();
        if (VilkårType.FØDSELSVILKÅRET_MOR.equals(vilkårType) || VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR.equals(vilkårType)) {
            return HistorikkEndretFeltType.FODSELSVILKARET;
        } else if (VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD.equals(vilkårType)) {
            return HistorikkEndretFeltType.ADOPSJONSVILKARET;
        }
        return HistorikkEndretFeltType.UDEFINIERT;
    }

    private Optional<Vilkår> finnRelevantVilkår(Behandling behandling) {

        List<VilkårType> relevanteVilkårTyper = Arrays.asList(VilkårType.FØDSELSVILKÅRET_MOR, VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR, VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD);
        List<Vilkår> vilkårene = behandling.getBehandlingsresultat().getVilkårResultat().getVilkårene();

        return vilkårene.stream()
                .filter(v -> relevanteVilkårTyper.contains(v.getVilkårType()))
                .findFirst();
    }

    @ApplicationScoped
    @DtoTilServiceAdapter(dto = VurdereYtelseSammeBarnSøkerAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
    public static class VurdereYtelseSammeBarnSøkerOppdaterer extends VurdereYtelseSammeBarnOppdaterer {
        VurdereYtelseSammeBarnSøkerOppdaterer() {
            // for CDI proxy
        }

        @Inject
        public VurdereYtelseSammeBarnSøkerOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                HistorikkTjenesteAdapter historikkAdapter) {
            super(repositoryProvider, historikkAdapter);
        }
    }

    @ApplicationScoped
    @DtoTilServiceAdapter(dto = VurdereYtelseSammeBarnAnnenForelderAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
    public static class VurdereYtelseSammeBarnAnnenForelderOppdaterer extends VurdereYtelseSammeBarnOppdaterer {
        public VurdereYtelseSammeBarnAnnenForelderOppdaterer() {
            // for CDI proxy
        }

        @Inject
        VurdereYtelseSammeBarnAnnenForelderOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                HistorikkTjenesteAdapter historikkAdapter) {
            super(repositoryProvider, historikkAdapter);
        }
    }
}
