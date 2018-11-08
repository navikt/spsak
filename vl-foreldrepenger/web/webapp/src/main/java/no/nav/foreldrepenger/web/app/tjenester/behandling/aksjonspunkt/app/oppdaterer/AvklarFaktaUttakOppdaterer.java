package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaUttakTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.FaktaUttakToTrinnsTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.FaktaUttakHistorikkTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.KontrollerOppgittFordelingTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarFaktaUttakDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklarFaktaUttakOppdaterer implements AksjonspunktOppdaterer<AvklarFaktaUttakDto> {

    private KontrollerFaktaUttakTjeneste kontrollerFaktaUttakTjeneste;
    private KontrollerOppgittFordelingTjeneste kontrollerOppgittFordelingTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;
    private FaktaUttakHistorikkTjeneste faktaUttakHistorikkTjeneste;
    private FaktaUttakToTrinnsTjeneste faktaUttakToTrinnsTjeneste;

    AvklarFaktaUttakOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public AvklarFaktaUttakOppdaterer(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                      @FagsakYtelseTypeRef("FP") KontrollerFaktaUttakTjeneste kontrollerFaktaUttakTjeneste,
                                      KontrollerOppgittFordelingTjeneste kontrollerOppgittFordelingTjeneste,
                                      FaktaUttakHistorikkTjeneste faktaUttakHistorikkTjeneste,
                                      FaktaUttakToTrinnsTjeneste faktaUttakToTrinnsTjeneste) {
        this.kontrollerFaktaUttakTjeneste = kontrollerFaktaUttakTjeneste;
        this.kontrollerOppgittFordelingTjeneste = kontrollerOppgittFordelingTjeneste;
        this.aksjonspunktRepository = behandlingRepositoryProvider.getAksjonspunktRepository();
        this.faktaUttakHistorikkTjeneste = faktaUttakHistorikkTjeneste;
        this.faktaUttakToTrinnsTjeneste = faktaUttakToTrinnsTjeneste;
    }

    @Override
    public OppdateringResultat oppdater(AvklarFaktaUttakDto dto, Behandling behandling, VilkårResultat.Builder vilkårBuilder) {
        //fjern manuell avklar fakta 6070 siden trenger ikke ha både 5070 og 6070 på en behandling
        fjernOverstyringAksjonspunkt(behandling);

        kontrollerOppgittFordelingTjeneste.avklarFaktaUttaksperiode(dto, behandling);
        faktaUttakHistorikkTjeneste.byggHistorikkinnslagForAvklarFakta(dto, behandling);
        faktaUttakToTrinnsTjeneste.oppdaterTotrinnskontrollVedEndringerFaktaUttak(dto, behandling);

        // Må kjøre kontroll av periodene på nytt, og eventuelt beholde aksjonspunktet åpent dersom det fortsatt er noe som må avklares.
        List<AksjonspunktResultat> aksjonspunkter = kontrollerFaktaUttakTjeneste.utledAksjonspunkter(behandling);
        boolean ferdig = aksjonspunkter.stream().
            noneMatch(a ->
                AvklarFaktaUttakDto.AvklarFaktaUttakPerioderDto.AKSJONSPUNKT_KODE.equals(a.getAksjonspunktDefinisjon().getKode())
                    && AvklarFaktaUttakDto.AvklarFaktaUttakFørsteUttakDatoDto.AKSJONSPUNKT_KODE.equals(a.getAksjonspunktDefinisjon().getKode())
            );

        return !ferdig ? OppdateringResultat.beholdAksjonspunktÅpent() : OppdateringResultat.utenOveropp();
    }

    private void fjernOverstyringAksjonspunkt(Behandling behandling) {
        Optional<Aksjonspunkt> aksjonspunkt = behandling.getAksjonspunkter().stream()
            .filter(ap -> ap.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.MANUELL_AVKLAR_FAKTA_UTTAK))
            .findFirst();

        if(aksjonspunkt.isPresent()) {
            aksjonspunktRepository.fjernAksjonspunkt(behandling, aksjonspunkt.get().getAksjonspunktDefinisjon());
        }
    }

}
