package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.domene.medlem.api.BekreftOppholdVurderingAksjonspunktDto;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftOppholdVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftOppholdVurderingDto.BekreftLovligOppholdVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftOppholdVurderingDto.BekreftOppholdsrettVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

abstract class BekreftOppholdOppdaterer implements AksjonspunktOppdaterer<BekreftOppholdVurderingDto> {

    private MedlemTjeneste medlemTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;
    private HistorikkTjenesteAdapter historikkAdapter;

    BekreftOppholdOppdaterer() {
        // for CDI proxy
    }

    protected BekreftOppholdOppdaterer(GrunnlagRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkAdapter,
                                       MedlemTjeneste medlemTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.medlemTjeneste = medlemTjeneste;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public OppdateringResultat oppdater(BekreftOppholdVurderingDto dto, Behandling behandling) {
        Optional<MedlemskapAggregat> medlemskap = medlemTjeneste.hentMedlemskap(behandling);
        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskap.flatMap(MedlemskapAggregat::getVurdertMedlemskap);

        Boolean orginalOppholdsrettBool = vurdertMedlemskap.map(VurdertMedlemskap::getOppholdsrettVurdering).orElse(null);
        HistorikkEndretFeltVerdiType orginalOppholdsrett = mapTilOppholdsrettVerdiKode(orginalOppholdsrettBool);
        HistorikkEndretFeltVerdiType bekreftetOppholdsrett = mapTilOppholdsrettVerdiKode(dto.getOppholdsrettVurdering());

        Boolean orginalLovligOppholdBool = vurdertMedlemskap.map(VurdertMedlemskap::getLovligOppholdVurdering).orElse(null);
        HistorikkEndretFeltVerdiType originalLovligOpphold = mapTilLovligOppholdVerdiKode(orginalLovligOppholdBool);
        HistorikkEndretFeltVerdiType bekreftetLovligOpphold = mapTilLovligOppholdVerdiKode(dto.getLovligOppholdVurdering());

        boolean erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.OPPHOLDSRETT_EOS, orginalOppholdsrett, bekreftetOppholdsrett);
        erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.OPPHOLDSRETT_IKKE_EOS, originalLovligOpphold, bekreftetLovligOpphold)
                || erEndret;

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        historikkAdapter.tekstBuilder()
                .medBegrunnelse(dto.getBegrunnelse(),
                        aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon,
                                dto.getBegrunnelse()))
                .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        if (erEndret) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
        }

        final BekreftOppholdVurderingAksjonspunktDto adapter = new BekreftOppholdVurderingAksjonspunktDto(dto.getOppholdsrettVurdering(),
                dto.getLovligOppholdVurdering(), dto.getErEosBorger());

        medlemTjeneste.aksjonspunktBekreftOppholdVurdering(behandling, adapter);

        return OppdateringResultat.utenOveropp();
    }

    private HistorikkEndretFeltVerdiType mapTilLovligOppholdVerdiKode(Boolean harLovligOpphold) {
        if (harLovligOpphold == null) {
            return null;
        }
        return harLovligOpphold ? HistorikkEndretFeltVerdiType.LOVLIG_OPPHOLD : HistorikkEndretFeltVerdiType.IKKE_LOVLIG_OPPHOLD;
    }

    private HistorikkEndretFeltVerdiType mapTilOppholdsrettVerdiKode(Boolean harOppholdsrett) {
        if (harOppholdsrett == null) {
            return null;
        }
        return harOppholdsrett ? HistorikkEndretFeltVerdiType.OPPHOLDSRETT : HistorikkEndretFeltVerdiType.IKKE_OPPHOLDSRETT;
    }

    private boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, HistorikkEndretFeltVerdiType original, HistorikkEndretFeltVerdiType bekreftet) {
        if (!Objects.equals(bekreftet, original)) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, original, bekreftet);
            return true;
        }
        return false;
    }

    @ApplicationScoped
    @DtoTilServiceAdapter(dto = BekreftLovligOppholdVurderingDto.class, adapter = AksjonspunktOppdaterer.class)
    public static class BekreftLovligOppholdVurderingOppdaterer extends BekreftOppholdOppdaterer {

        BekreftLovligOppholdVurderingOppdaterer() {
            // for CDI proxy
        }

        @Inject
        public BekreftLovligOppholdVurderingOppdaterer(GrunnlagRepositoryProvider repositoryProvider,
                                                       HistorikkTjenesteAdapter historikkAdapter, MedlemTjeneste medlemTjeneste) {
            super(repositoryProvider, historikkAdapter, medlemTjeneste);
        }
    }

    @ApplicationScoped
    @DtoTilServiceAdapter(dto = BekreftOppholdsrettVurderingDto.class, adapter = AksjonspunktOppdaterer.class)
    public static class BekreftOppholdsrettVurderingOppdaterer extends BekreftOppholdOppdaterer {

        BekreftOppholdsrettVurderingOppdaterer() {
            // for CDI proxy
        }

        @Inject
        public BekreftOppholdsrettVurderingOppdaterer(GrunnlagRepositoryProvider repositoryProvider,
                                                      HistorikkTjenesteAdapter historikkAdapter, MedlemTjeneste medlemTjeneste) {
            super(repositoryProvider, historikkAdapter, medlemTjeneste);
        }
    }

}
