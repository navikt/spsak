package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
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
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftSokersOpplysningspliktManuDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = BekreftSokersOpplysningspliktManuDto.class, adapter=AksjonspunktOppdaterer.class)
class BekreftSøkersOpplysningspliktManuellOppdaterer implements AksjonspunktOppdaterer<BekreftSokersOpplysningspliktManuDto> {

    private AksjonspunktRepository aksjonspunktRepository;
    private HistorikkTjenesteAdapter historikkTjenesteAdapter;

    BekreftSøkersOpplysningspliktManuellOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public BekreftSøkersOpplysningspliktManuellOppdaterer(GrunnlagRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkTjenesteAdapter) {
        this.historikkTjenesteAdapter = historikkTjenesteAdapter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public OppdateringResultat oppdater(BekreftSokersOpplysningspliktManuDto dto, Behandling behandling, VilkårResultat.Builder vilkårBuilder) {

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());

        final boolean erVilkårOk = dto.getErVilkarOk() &&
            dto.getInntektsmeldingerSomIkkeKommer().stream().filter(imelding -> !imelding.isBrukerHarSagtAtIkkeKommer()).collect(Collectors.toList()).isEmpty();
        leggTilEndretFeltIHistorikkInnslag(dto.getBegrunnelse(), erVilkårOk, aksjonspunktDefinisjon, behandling);

        Avslagsårsak avslagsårsak = erVilkårOk ? null : Avslagsårsak.MANGLENDE_DOKUMENTASJON;
        List<Aksjonspunkt> åpneAksjonspunkter = behandling.getÅpneAksjonspunkter();

        if (erVilkårOk) {
            // Reverser vedtak uten totrinnskontroll
            åpneAksjonspunkter.stream()
                .filter(a -> a.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.VEDTAK_UTEN_TOTRINNSKONTROLL))
                .findFirst()
                .ifPresent(a -> aksjonspunktRepository.fjernAksjonspunkt(behandling, a.getAksjonspunktDefinisjon()));

            vilkårBuilder.leggTilVilkårResultatManueltOppfylt(VilkårType.SØKERSOPPLYSNINGSPLIKT);
            vilkårBuilder.medVilkårResultatType(VilkårResultatType.IKKE_FASTSATT);

            return OppdateringResultat.utenOveropp();
        } else {
            // Hoppe rett til foreslå vedtak uten totrinnskontroll
            åpneAksjonspunkter.stream()
                .filter(a -> !a.getAksjonspunktDefinisjon().getKode().equals(dto.getKode())) // Ikke seg selv
                .forEach(a -> aksjonspunktRepository.setTilAvbrutt(a));
            aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VEDTAK_UTEN_TOTRINNSKONTROLL);

            vilkårBuilder.leggTilVilkårResultatManueltIkkeOppfylt(VilkårType.SØKERSOPPLYSNINGSPLIKT, avslagsårsak);
            vilkårBuilder.medVilkårResultatType(VilkårResultatType.AVSLÅTT);

            return OppdateringResultat.medFremoverHopp(FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK);
        }
    }

    private void leggTilEndretFeltIHistorikkInnslag(String begrunnelse, Boolean vilkårOppfylt, AksjonspunktDefinisjon aksjonspunktDefinisjon, Behandling behandling) {
        HistorikkEndretFeltVerdiType tilVerdi = Boolean.TRUE.equals(vilkårOppfylt) ? HistorikkEndretFeltVerdiType.VILKAR_OPPFYLT : HistorikkEndretFeltVerdiType.VILKAR_IKKE_OPPFYLT;

        if (begrunnelse != null) {
            historikkTjenesteAdapter.tekstBuilder().medBegrunnelse(begrunnelse);
        }
        historikkTjenesteAdapter.tekstBuilder().medEndretFelt(HistorikkEndretFeltType.SOKERSOPPLYSNINGSPLIKT, null, tilVerdi)
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);
    }
}
