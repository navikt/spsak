package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FASTSETT_UTTAKPERIODER;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.FastsettePerioderTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring.uttak.UttakPerioderMapper;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.FastsetteUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.overstyring.UttakHistorikkUtil;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsetteUttakDto.class, adapter = AksjonspunktOppdaterer.class)
public class FastsettUttakOppdaterer implements AksjonspunktOppdaterer<FastsetteUttakDto> {

    private HistorikkTjenesteAdapter historikkAdapter;
    private FastsettePerioderTjeneste tjeneste;
    private UttakRepository uttakRepository;
    private AksjonspunktRepository aksjonspunktRepository;

    FastsettUttakOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FastsettUttakOppdaterer(BehandlingRepositoryProvider repositoryProverider,
                                   HistorikkTjenesteAdapter historikkAdapter,
                                   FastsettePerioderTjeneste tjeneste,
                                   AksjonspunktRepository aksjonspunktRepository) {
        this.historikkAdapter = historikkAdapter;
        this.tjeneste = tjeneste;
        this.uttakRepository = repositoryProverider.getUttakRepository();
        this.aksjonspunktRepository = aksjonspunktRepository;
    }

    @Override
    public OppdateringResultat oppdater(FastsetteUttakDto dto, Behandling behandling) {
        UttakResultatEntitet forrigeResultat = håndterOverstyring(dto, behandling);

        lagHistorikkInnslag(behandling, dto, forrigeResultat);

        return OppdateringResultat.utenOveropp();
    }

    private UttakResultatEntitet håndterOverstyring(FastsetteUttakDto dto, Behandling behandling) {
        UttakResultatEntitet forrigeResultat = uttakRepository.hentUttakResultat(behandling);
        UttakResultatPerioder perioder = UttakPerioderMapper.map(dto.getPerioder(), forrigeResultat.getGjeldendePerioder());
        tjeneste.manueltFastsettePerioder(behandling, perioder);
        return forrigeResultat;
    }

    private void lagHistorikkInnslag(Behandling behandling, FastsetteUttakDto dto, UttakResultatEntitet forrigeResultat) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(FASTSETT_UTTAKPERIODER.getKode());
        List<Historikkinnslag> historikkinnslag = UttakHistorikkUtil.forFastsetting().lagHistorikkinnslag(
            behandling, aksjonspunktDefinisjon, dto.getPerioder(), forrigeResultat.getGjeldendePerioder());
        historikkinnslag.forEach(innslag -> historikkAdapter.lagInnslag(innslag));
    }
}
