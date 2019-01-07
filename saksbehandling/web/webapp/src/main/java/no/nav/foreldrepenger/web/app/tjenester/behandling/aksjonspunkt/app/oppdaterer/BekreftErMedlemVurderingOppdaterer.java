package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.domene.medlem.api.BekreftErMedlemVurderingAksjonspunktDto;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftErMedlemVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = BekreftErMedlemVurderingDto.class, adapter = AksjonspunktOppdaterer.class)
public class BekreftErMedlemVurderingOppdaterer implements AksjonspunktOppdaterer<BekreftErMedlemVurderingDto> {
    private MedlemTjeneste medlemTjeneste;
    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;
    private MedlemskapRepository medlemskapRepository;

    BekreftErMedlemVurderingOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public BekreftErMedlemVurderingOppdaterer(GrunnlagRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkAdapter, MedlemTjeneste medlemTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.medlemTjeneste = medlemTjeneste;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
    }

    @Override
    public OppdateringResultat oppdater(BekreftErMedlemVurderingDto dto, Behandling behandling) {
        Optional<MedlemskapAggregat> medlemskap = medlemskapRepository.hentMedlemskap(behandling);
        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskap.flatMap(MedlemskapAggregat::getVurdertMedlemskap);
        Optional<MedlemskapManuellVurderingType> medlemskapManuellVurderingType = vurdertMedlemskap
            .map(VurdertMedlemskap::getMedlemsperiodeManuellVurdering);
        MedlemskapManuellVurderingType originalVurdering = medlemskapManuellVurderingType.orElse(null);
        MedlemskapManuellVurderingType bekreftetVurdering = dto.getManuellVurderingType();

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        historikkAdapter.tekstBuilder()
            .medBegrunnelse(dto.getBegrunnelse(), aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon, dto.getBegrunnelse()))
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        boolean erEndret = !Objects.equals(originalVurdering, bekreftetVurdering);
        if (erEndret) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
            historikkAdapter.tekstBuilder().medEndretFelt(HistorikkEndretFeltType.GYLDIG_MEDLEM_FOLKETRYGDEN, originalVurdering, bekreftetVurdering);
        }

        final BekreftErMedlemVurderingAksjonspunktDto adapter = new BekreftErMedlemVurderingAksjonspunktDto(dto.getManuellVurderingType().getKode());
        medlemTjeneste.aksjonspunktBekreftMeldlemVurdering(behandling, adapter);

        return OppdateringResultat.utenOveropp();
    }

}
