package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.domene.familiehendelse.omsorg.OmsorghendelseTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.AvklarForeldreansvarAksjonspunktData;
import no.nav.foreldrepenger.domene.personopplysning.AvklartDataBarnAdapter;
import no.nav.foreldrepenger.domene.personopplysning.AvklartDataForeldreAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.AvklarFaktaForForeldreansvarAksjonspunktDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarFaktaForForeldreansvarAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklarForeldreansvarOppdaterer implements AksjonspunktOppdaterer<AvklarFaktaForForeldreansvarAksjonspunktDto> {

    private AksjonspunktRepository aksjonspunktRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private OmsorghendelseTjeneste omsorghendelseTjeneste;

    AvklarForeldreansvarOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public AvklarForeldreansvarOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                                  SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                                  OmsorghendelseTjeneste omsorghendelseTjeneste) {
        this.omsorghendelseTjeneste = omsorghendelseTjeneste;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public boolean skalReinnhenteRegisteropplysninger(Behandling behandling, LocalDate forrigeSkjæringstidspunkt) {
        // TODO (lots): Avklare med Jarek om denne blir annerledes for FP
        return !skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(behandling).equals(forrigeSkjæringstidspunkt);
    }

    @Override
    public OppdateringResultat oppdater(AvklarFaktaForForeldreansvarAksjonspunktDto dto, Behandling behandling,
                                        VilkårResultat.Builder vilkårBuilder) {

        final LocalDate forrigeSkjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(behandling);
        oppdaterAksjonspunktGrunnlag(dto, behandling);
        boolean skalReinnhenteRegisteropplysninger = skalReinnhenteRegisteropplysninger(behandling, forrigeSkjæringstidspunkt);

        return vurderSkalReinnhenteRegisteropplysninger(dto, behandling, skalReinnhenteRegisteropplysninger);
    }

    private void oppdaterAksjonspunktGrunnlag(AvklarFaktaForForeldreansvarAksjonspunktDto dto, Behandling behandling) {
        List<AvklartDataForeldreAdapter> foreldreAdapter = new ArrayList<>();
        dto.getForeldre().forEach(foreldre ->
            foreldreAdapter.add(new AvklartDataForeldreAdapter(foreldre.getAktorId(), foreldre.getDødsdato())));

        List<AvklartDataBarnAdapter> barnAdapter = new ArrayList<>();
        dto.getBarn().forEach(barn ->
            barnAdapter.add(new AvklartDataBarnAdapter(barn.getAktørId(), barn.getFodselsdato(), barn.getNummer())));

        AksjonspunktDefinisjon apDef = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());

        final AvklarForeldreansvarAksjonspunktData data = new AvklarForeldreansvarAksjonspunktData(apDef,
            dto.getOmsorgsovertakelseDato(),dto.getForeldreansvarDato(),dto.getAntallBarn(), foreldreAdapter, barnAdapter);

        omsorghendelseTjeneste.aksjonspunktAvklarForeldreansvar(behandling, data);
    }

    private OppdateringResultat vurderSkalReinnhenteRegisteropplysninger(AvklarFaktaForForeldreansvarAksjonspunktDto dto,
                                                                         Behandling behandling, boolean skalReinnhenteRegisteropplysninger) {
        Aksjonspunkt aksjonspunkt = finnAksjonspunkt(behandling, dto);
        // TODO (Maur): Må vi hoppe tilbake for å hente inn registeropplysninger her? Det virker ikke fornuftig.
        if (skalReinnhenteRegisteropplysninger) {
            aksjonspunktRepository.setSlettingVedRegisterinnhenting(aksjonspunkt, false);
            return OppdateringResultat.medTilbakehopp(BehandlingStegType.INNHENT_REGISTEROPP);
        } else {
            aksjonspunktRepository.setSlettingVedRegisterinnhenting(aksjonspunkt, true);
            return OppdateringResultat.utenOveropp();
        }
    }

    private Aksjonspunkt finnAksjonspunkt(Behandling behandling, BekreftetAksjonspunktDto dto) {
        return behandling.getAksjonspunkter().stream()
            .filter(ap -> ap.getAksjonspunktDefinisjon().getKode().equals(dto.getKode()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Aksjonspunkt som bekreftes må finnes på behandling."));
    }


}
