package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OppdateringResultat;
import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.domene.medlem.api.BekreftBosattVurderingAksjonspunktDto;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftBosattVurderingDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = BekreftBosattVurderingDto.class, adapter = AksjonspunktOppdaterer.class)
public class BekreftBosattVurderingOppdaterer implements AksjonspunktOppdaterer<BekreftBosattVurderingDto> {

    private MedlemTjeneste medlemTjeneste;
    private HistorikkTjenesteAdapter historikkAdapter;
    private MedlemskapRepository medlemskapRepository;
    private AksjonspunktRepository aksjonspunktRepository;

    BekreftBosattVurderingOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public BekreftBosattVurderingOppdaterer(GrunnlagRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkAdapter, MedlemTjeneste medlemTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.medlemTjeneste = medlemTjeneste;
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public OppdateringResultat oppdater(BekreftBosattVurderingDto dto, Behandling behandling) {
        håndterEndringHistorikk(dto, behandling);

        medlemTjeneste.aksjonspunktBekreftBosattVurdering(behandling, new BekreftBosattVurderingAksjonspunktDto(dto.getBosattVurdering()));

        return OppdateringResultat.utenOveropp();
    }

    private void håndterEndringHistorikk(BekreftBosattVurderingDto dto, Behandling behandling) {
        Optional<MedlemskapAggregat> medlemskap = medlemskapRepository.hentMedlemskap(behandling);
        Boolean originalBosattBool = medlemskap.flatMap(MedlemskapAggregat::getVurdertMedlemskap)
            .map(VurdertMedlemskap::getBosattVurdering).orElse(null);
        Boolean bekreftetBosattBool = dto.getBosattVurdering();

        HistorikkEndretFeltVerdiType originalBosatt = mapTilBosattVerdiKode(originalBosattBool);
        HistorikkEndretFeltVerdiType bekreftetBosatt = mapTilBosattVerdiKode(bekreftetBosattBool);

        boolean erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.ER_SOKER_BOSATT_I_NORGE, originalBosatt, bekreftetBosatt);

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        historikkAdapter.tekstBuilder()
            .medBegrunnelse(dto.getBegrunnelse(), aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon, dto.getBegrunnelse()))
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        if (erEndret) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
        }
    }

    private HistorikkEndretFeltVerdiType mapTilBosattVerdiKode(Boolean bosattBool) {
        if (bosattBool == null) {
            return null;
        }
        return bosattBool ? HistorikkEndretFeltVerdiType.BOSATT_I_NORGE : HistorikkEndretFeltVerdiType.IKKE_BOSATT_I_NORGE;
    }

    private boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, HistorikkEndretFeltVerdiType original, HistorikkEndretFeltVerdiType bekreftet) {
        if (!Objects.equals(bekreftet, original)) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, original, bekreftet);
            return true;
        }
        return false;
    }
}
