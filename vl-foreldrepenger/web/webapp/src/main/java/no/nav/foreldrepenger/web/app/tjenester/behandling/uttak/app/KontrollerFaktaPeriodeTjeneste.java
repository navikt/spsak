package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaData;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaUttakTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.KontrollerFaktaDataDto;

@ApplicationScoped
public class KontrollerFaktaPeriodeTjeneste {

    private BehandlingRepository behandlingRepository;
    private KontrollerFaktaUttakTjeneste tjeneste;


    public KontrollerFaktaPeriodeTjeneste() {
        //for CDI proxy
    }

    @Inject
    public KontrollerFaktaPeriodeTjeneste(BehandlingRepository behandlingRepository, @FagsakYtelseTypeRef("FP") KontrollerFaktaUttakTjeneste tjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.tjeneste = tjeneste;
    }

    public KontrollerFaktaDataDto hentKontrollerFaktaPerioder(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return mapTilDto(tjeneste.hentKontrollerFaktaPerioder(behandling));
    }

    private KontrollerFaktaDataDto mapTilDto(KontrollerFaktaData kontrollerFaktaData) {
        return new KontrollerFaktaDataDto(kontrollerFaktaData);
    }
}
