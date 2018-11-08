package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.overstyring;

import static no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonFeil.FACTORY;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaUttakTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring.AbstractOverstyringshåndterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring.Overstyringshåndterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.FaktaUttakHistorikkTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.KontrollerOppgittFordelingTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.ManuellAvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = ManuellAvklarFaktaUttakDto.class, adapter = Overstyringshåndterer.class)
public class AvklarFaktaOverstyringshåndterer extends AbstractOverstyringshåndterer<ManuellAvklarFaktaUttakDto> {

    private KontrollerOppgittFordelingTjeneste kontrollerOppgittFordelingTjeneste;
    private KontrollerFaktaUttakTjeneste kontrollerFaktaUttakTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;
    private FaktaUttakHistorikkTjeneste faktaUttakHistorikkTjeneste;

    AvklarFaktaOverstyringshåndterer() {
        // for CDI proxy
    }

    @Inject
    public AvklarFaktaOverstyringshåndterer(BehandlingRepositoryProvider repositoryProvider,
                                            HistorikkTjenesteAdapter historikkAdapter,
                                            KontrollerOppgittFordelingTjeneste kontrollerOppgittFordelingTjeneste,
                                            @FagsakYtelseTypeRef("FP") KontrollerFaktaUttakTjeneste kontrollerFaktaUttakTjeneste,
                                            FaktaUttakHistorikkTjeneste faktaUttakHistorikkTjeneste) {
        super(repositoryProvider, historikkAdapter, AksjonspunktDefinisjon.MANUELL_AVKLAR_FAKTA_UTTAK);
        this.kontrollerOppgittFordelingTjeneste = kontrollerOppgittFordelingTjeneste;
        this.kontrollerFaktaUttakTjeneste = kontrollerFaktaUttakTjeneste;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.faktaUttakHistorikkTjeneste = faktaUttakHistorikkTjeneste;
    }

    @Override
    public OppdateringResultat håndterOverstyring(ManuellAvklarFaktaUttakDto dto, Behandling behandling, BehandlingskontrollKontekst kontekst) {
        //Aksjonspunkt gjelder kun manuelle revurderinger og revurderinger på grun av hendelse
        //Ikke finne aksjonspunkt avklar fakta uttak
        if(!kanAksjonspunktAktiveres(behandling)) {
            throw FACTORY.kanIkkeAktivereAksjonspunkt(dto.getKode()).toException();
        }
        kontrollerOppgittFordelingTjeneste.manuellAvklarFaktaUttaksperiode(dto, behandling);

        // Må kjøre kontroll av periodene på nytt, og legge til aksjonspunkt avklar fakta uttak
        kjøreKontrollAvPeriodenePåNytt(behandling);

        return OppdateringResultat.utenOveropp();
    }

    private void kjøreKontrollAvPeriodenePåNytt(Behandling behandling) {
        List<AksjonspunktResultat> aksjonspunkter = kontrollerFaktaUttakTjeneste.utledAksjonspunkter(behandling);
        boolean finnes = aksjonspunkter.stream().
            anyMatch(a ->
                AvklarFaktaUttakDto.AvklarFaktaUttakPerioderDto.AKSJONSPUNKT_KODE.equals(a.getAksjonspunktDefinisjon().getKode()));
        //Må bruke 5070 når det finnes, så legg til 5070 i behandling hvis utleder oprettet aksjonspunkt 5070
        if (finnes) {
            Optional<Aksjonspunkt> apOptional = behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.AVKLAR_FAKTA_UTTAK);
            if (apOptional.isPresent()) {
                aksjonspunktRepository.setReåpnet(apOptional.get());
            } else {
                aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_FAKTA_UTTAK, BehandlingStegType.VURDER_UTTAK);
            }
        }
    }

    @Override
    protected void lagHistorikkInnslag(Behandling behandling, ManuellAvklarFaktaUttakDto dto) {
        faktaUttakHistorikkTjeneste.byggHistorikkinnslagForManuellAvklarFakta(dto, behandling);
    }

    private boolean kanAksjonspunktAktiveres(Behandling behandling) {
       return  erManuellRevurdering(behandling) && kanIkkeFinnesAksjonspunktFaktaUttak(behandling);
    }

    private boolean erManuellRevurdering(Behandling behandling) {
        boolean erÅrsakHendelse = behandling.getBehandlingÅrsaker().stream().anyMatch(årsak -> Objects.equals(BehandlingÅrsakType.RE_HENDELSE_FØDSEL, årsak.getBehandlingÅrsakType()));
        return Objects.equals(BehandlingType.REVURDERING, behandling.getType()) && (behandling.erManueltOpprettet() || erÅrsakHendelse);
    }

    private boolean kanIkkeFinnesAksjonspunktFaktaUttak(Behandling behandling) {
       return behandling.getAksjonspunkter().stream()
            .noneMatch(ap -> ap.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.AVKLAR_FAKTA_UTTAK));
    }

}
