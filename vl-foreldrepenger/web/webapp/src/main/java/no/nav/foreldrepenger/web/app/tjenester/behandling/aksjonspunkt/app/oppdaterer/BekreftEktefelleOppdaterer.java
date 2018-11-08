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
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftEktefelleAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = BekreftEktefelleAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
public class BekreftEktefelleOppdaterer implements AksjonspunktOppdaterer<BekreftEktefelleAksjonspunktDto> {

    private FamilieHendelseTjeneste hendelseTjeneste;

    private BehandlingRepositoryProvider repositoryProvider;

    private AksjonspunktRepository aksjonspunktRepository;

    private HistorikkTjenesteAdapter historikkAdapter;

    public BekreftEktefelleOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public BekreftEktefelleOppdaterer(BehandlingRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkAdapter, FamilieHendelseTjeneste hendelseTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.hendelseTjeneste = hendelseTjeneste;
        this.repositoryProvider = repositoryProvider;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public OppdateringResultat oppdater(BekreftEktefelleAksjonspunktDto dto, Behandling behandling) {
        Optional<Boolean> erEktefellesBarn = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling)
                .getGjeldendeBekreftetVersjon()
                .flatMap(FamilieHendelse::getAdopsjon)
                .map(Adopsjon::getErEktefellesBarn);

        boolean erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.EKTEFELLES_BARN, konvertBooleanTilFaktaEndretVerdiType(erEktefellesBarn.orElse(null)),
                konvertBooleanTilFaktaEndretVerdiType(dto.getEktefellesBarn()));

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        if (erEndret) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
        }

        historikkAdapter.tekstBuilder()
            .medBegrunnelse(dto.getBegrunnelse(), aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon, dto.getBegrunnelse()))
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);


        hendelseTjeneste.aksjonspunktBekreftEktefellesBarn(behandling, new BekreftAdopsjonsAksjonspunktDto(dto.getEktefellesBarn()));
        return OppdateringResultat.utenOveropp();
    }

    private HistorikkEndretFeltVerdiType konvertBooleanTilFaktaEndretVerdiType(Boolean ektefellesBarn) {
        if (ektefellesBarn == null) {
            return null;
        }
        return ektefellesBarn ? HistorikkEndretFeltVerdiType.EKTEFELLES_BARN : HistorikkEndretFeltVerdiType.IKKE_EKTEFELLES_BARN;
    }

    private boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, HistorikkEndretFeltVerdiType original, HistorikkEndretFeltVerdiType bekreftet) {
        if (!Objects.equals(bekreftet, original)) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, original, bekreftet);
            return true;
        }
        return false;
    }
}
