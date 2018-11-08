package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.familiehendelse.BekreftAdopsjonsAksjonspunktDto;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftMannAdoptererAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = BekreftMannAdoptererAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
public class BekreftMannAdoptererOppdaterer implements AksjonspunktOppdaterer<BekreftMannAdoptererAksjonspunktDto> {
    private FamilieHendelseTjeneste hendelseTjeneste;

    private BehandlingRepositoryProvider repositoryProvider;

    private AksjonspunktRepository aksjonspunktRepository;

    private HistorikkTjenesteAdapter historikkAdapter;

    BekreftMannAdoptererOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public BekreftMannAdoptererOppdaterer(BehandlingRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkAdapter,
            FamilieHendelseTjeneste hendelseTjeneste) {
        this.hendelseTjeneste = hendelseTjeneste;
        this.historikkAdapter = historikkAdapter;
        this.repositoryProvider = repositoryProvider;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public OppdateringResultat oppdater(BekreftMannAdoptererAksjonspunktDto dto, Behandling behandling) {

        håndterEndringHistorikk(dto, behandling);

        hendelseTjeneste.aksjonspunktBekreftMannAdopterer(behandling, new BekreftAdopsjonsAksjonspunktDto(dto.getMannAdoptererAlene()));
        return OppdateringResultat.utenOveropp();
    }

    private void håndterEndringHistorikk(BekreftMannAdoptererAksjonspunktDto dto, Behandling behandling) {
        Optional<Boolean> mannAdoptererAlene = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling)
                .getOverstyrtVersjon()
                .flatMap(FamilieHendelse::getAdopsjon)
                .map(Adopsjon::getAdoptererAlene);

        boolean erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.MANN_ADOPTERER, konvertBooleanTilFaktaEndretVerdiType(mannAdoptererAlene.orElse(null)),
                konvertBooleanTilFaktaEndretVerdiType(dto.getMannAdoptererAlene()));

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        historikkAdapter.tekstBuilder()
                .medBegrunnelse(dto.getBegrunnelse(),
                        aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon,
                                dto.getBegrunnelse()))
                .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        if (erEndret) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
        }
    }

    private HistorikkEndretFeltVerdiType konvertBooleanTilFaktaEndretVerdiType(Boolean mannAdoptererAlene) {
        if (mannAdoptererAlene == null) {
            return null;
        }
        return mannAdoptererAlene ? HistorikkEndretFeltVerdiType.ADOPTERER_ALENE : HistorikkEndretFeltVerdiType.ADOPTERER_IKKE_ALENE;
    }

    private boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, HistorikkEndretFeltVerdiType original, HistorikkEndretFeltVerdiType bekreftet) {
        if (!Objects.equals(bekreftet, original)) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, original, bekreftet);
            return true;
        }
        return false;
    }

}
