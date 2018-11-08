package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.domene.familiehendelse.omsorg.OmsorghendelseTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvslagbartAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.Foreldreansvarsvilkår1AksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.Foreldreansvarsvilkår2AksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.OmsorgsvilkårAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

/**
 * Håndterer oppdatering av omsorgsvilkåret.
 */
abstract class OmsorgsvilkårAksjonspunktOppdaterer implements AksjonspunktOppdaterer<AvslagbartAksjonspunktDto> {

    private VilkårKodeverkRepository vilkårKodeverkRepository;
    private VilkårType vilkårType;
    private AksjonspunktRepository aksjonspunktRepository;
    private HistorikkTjenesteAdapter historikkAdapter;
    private OmsorghendelseTjeneste omsorghendelseTjeneste;

    OmsorgsvilkårAksjonspunktOppdaterer() {
        // for CDI proxy
    }

    public OmsorgsvilkårAksjonspunktOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                               OmsorghendelseTjeneste omsorghendelseTjeneste, HistorikkTjenesteAdapter historikkAdapter, VilkårType vilkårType) {
        this.omsorghendelseTjeneste = omsorghendelseTjeneste;
        this.historikkAdapter = historikkAdapter;
        this.vilkårType = vilkårType;
        this.vilkårKodeverkRepository = repositoryProvider.getVilkårKodeverkRepository();
        aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public OppdateringResultat oppdater(AvslagbartAksjonspunktDto dto, Behandling behandling,
                                        VilkårResultat.Builder vilkårBuilder) {

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        historikkAdapter.tekstBuilder()
            .medEndretFelt(getTekstKode(), null, dto.getErVilkarOk() ? HistorikkEndretFeltVerdiType.OPPFYLT : HistorikkEndretFeltVerdiType.IKKE_OPPFYLT)
            .medBegrunnelse(dto.getBegrunnelse(),
                aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon,
                    dto.getBegrunnelse()))
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);

        // Rydd opp gjenopprettede aksjonspunkt på andre omsorgsvilkår ved eventuelt tilbakehopp
        omsorghendelseTjeneste.aksjonspunktOmsorgsvilkår(behandling, aksjonspunktDefinisjon);

        if (dto.getErVilkarOk()) {
            vilkårBuilder.leggTilVilkårResultatManueltOppfylt(vilkårType);
            return OppdateringResultat.utenOveropp();
        } else {
            vilkårBuilder.leggTilVilkårResultatManueltIkkeOppfylt(vilkårType, vilkårKodeverkRepository.finnAvslagÅrsak(dto.getAvslagskode()));
            vilkårBuilder.medVilkårResultatType(VilkårResultatType.AVSLÅTT);

            return OppdateringResultat.medFremoverHopp(FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK);
        }
    }

    protected abstract HistorikkEndretFeltType getTekstKode();

    @ApplicationScoped
    @DtoTilServiceAdapter(dto = Foreldreansvarsvilkår1AksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
    public static class Foreldreansvarsvilkår1Oppdaterer extends OmsorgsvilkårAksjonspunktOppdaterer {

        Foreldreansvarsvilkår1Oppdaterer() {
            // for CDI proxy
        }

        @Inject
        public Foreldreansvarsvilkår1Oppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                                OmsorghendelseTjeneste omsorghendelseTjeneste,
                                                HistorikkTjenesteAdapter historikkAdapter) {
            super(repositoryProvider, omsorghendelseTjeneste, historikkAdapter, VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD);
        }

        @Override
        protected HistorikkEndretFeltType getTekstKode() {
            return HistorikkEndretFeltType.FORELDREANSVARSVILKARET;
        }

    }

    @ApplicationScoped
    @DtoTilServiceAdapter(dto = Foreldreansvarsvilkår2AksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
    public static class Foreldreansvarsvilkår2Oppdaterer extends OmsorgsvilkårAksjonspunktOppdaterer {

        Foreldreansvarsvilkår2Oppdaterer() {
            // for CDI proxy
        }

        @Inject
        public Foreldreansvarsvilkår2Oppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                                OmsorghendelseTjeneste omsorghendelseTjeneste,
                                                HistorikkTjenesteAdapter historikkAdapter) {
            super(repositoryProvider, omsorghendelseTjeneste, historikkAdapter, VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD);
        }

        @Override
        protected HistorikkEndretFeltType getTekstKode() {
            return HistorikkEndretFeltType.FORELDREANSVARSVILKARET;
        }
    }

    @ApplicationScoped
    @DtoTilServiceAdapter(dto = OmsorgsvilkårAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
    public static class OmsorgsvilkårOppdaterer extends OmsorgsvilkårAksjonspunktOppdaterer {

        OmsorgsvilkårOppdaterer() {
            // for CDI proxy
        }

        @Inject
        public OmsorgsvilkårOppdaterer(BehandlingRepositoryProvider repositoryProvider, OmsorghendelseTjeneste omsorghendelseTjeneste, HistorikkTjenesteAdapter historikkAdapter) {
            super(repositoryProvider, omsorghendelseTjeneste, historikkAdapter, VilkårType.OMSORGSVILKÅRET);
        }

        @Override
        protected HistorikkEndretFeltType getTekstKode() {
            return HistorikkEndretFeltType.OMSORGSVILKAR;
        }

    }
}
