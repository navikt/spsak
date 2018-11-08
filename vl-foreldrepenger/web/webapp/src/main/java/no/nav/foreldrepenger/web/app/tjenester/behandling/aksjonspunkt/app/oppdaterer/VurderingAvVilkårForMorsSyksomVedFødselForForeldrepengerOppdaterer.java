package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.VurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

import java.util.Objects;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerDto.class, adapter = AksjonspunktOppdaterer.class)
public class VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerOppdaterer implements AksjonspunktOppdaterer<VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerDto> {

    private FamilieHendelseTjeneste familieHendelseTjeneste;
    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;

    VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerOppdaterer() {
    }

    @Inject
    public VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerOppdaterer(FamilieHendelseTjeneste familieHendelseTjeneste, AksjonspunktRepository aksjonspunktRepository, HistorikkTjenesteAdapter historikkAdapter) {
        this.familieHendelseTjeneste = familieHendelseTjeneste;
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = aksjonspunktRepository;
    }

    @Override
    public OppdateringResultat oppdater(VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerDto dto, Behandling behandling) {
        FamilieHendelseGrunnlag familieHendelseGrunnlag = familieHendelseTjeneste.hentAggregat(behandling);
        VurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger adapter
            = new VurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger(dto.getErMorForSykVedFodsel());
        familieHendelseTjeneste.aksjonspunktVurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger(behandling, adapter);

        oppdaterVedEndretVerdi(HistorikkEndretFeltType.SYKDOM, familieHendelseGrunnlag.getGjeldendeVersjon().erMorForSykVedFødsel(), dto.getErMorForSykVedFodsel());

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        historikkAdapter.tekstBuilder()
            .medBegrunnelse(dto.getBegrunnelse(),
                aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon,
                    dto.getBegrunnelse()))
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        return OppdateringResultat.utenOveropp();
    }

    private boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, Boolean original, Boolean bekreftet) {
        if (original != null && !Objects.equals(bekreftet, original)) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, original ? HistorikkEndretFeltVerdiType.DOKUMENTERT : HistorikkEndretFeltVerdiType.IKKE_DOKUMENTERT,
                bekreftet ? HistorikkEndretFeltVerdiType.DOKUMENTERT : HistorikkEndretFeltVerdiType.IKKE_DOKUMENTERT);
            return true;
        }else {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, null,
                bekreftet ? HistorikkEndretFeltVerdiType.DOKUMENTERT : HistorikkEndretFeltVerdiType.IKKE_DOKUMENTERT);
            return false;
        }
    }
}
