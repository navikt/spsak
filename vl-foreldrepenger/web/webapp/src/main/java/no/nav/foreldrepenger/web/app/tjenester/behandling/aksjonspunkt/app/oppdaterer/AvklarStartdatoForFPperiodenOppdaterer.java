package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.ytelsefordeling.BekreftStartdatoForPerioden;
import no.nav.foreldrepenger.domene.ytelsefordeling.YtelseFordelingTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarStartdatoForFPperiodenDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarStartdatoForFPperiodenDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklarStartdatoForFPperiodenOppdaterer implements AksjonspunktOppdaterer<AvklarStartdatoForFPperiodenDto> {

    private HistorikkTjenesteAdapter historikkAdapter;
    private YtelseFordelingTjeneste ytelseFordelingTjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;

    AvklarStartdatoForFPperiodenOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public AvklarStartdatoForFPperiodenOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                                  HistorikkTjenesteAdapter historikkAdapter,
                                                  YtelseFordelingTjeneste ytelseFordelingTjeneste,
                                                  SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.ytelseFordelingTjeneste = ytelseFordelingTjeneste;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public OppdateringResultat oppdater(AvklarStartdatoForFPperiodenDto dto, Behandling behandling) {
        Optional<LocalDate> original = Optional.ofNullable(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(behandling));
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        if (!dto.getStartdatoFraSoknad().equals(original.get())) {
            HistorikkInnslagTekstBuilder tekstBuilder = historikkAdapter.tekstBuilder();
            tekstBuilder.medSkjermlenke(aksjonspunktDefinisjon, behandling);
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.STARTDATO_FRA_SOKNAD, original.get(), dto.getStartdatoFraSoknad());
        }

        ytelseFordelingTjeneste.aksjonspunktAvklarStartdatoForPerioden(behandling, new BekreftStartdatoForPerioden(dto.getStartdatoFraSoknad()));

        return OppdateringResultat.utenOveropp();
    }


}
