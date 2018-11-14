package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.overstyring;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.OVERSTYRING_AV_UTTAKPERIODER;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring.AbstractOverstyringshåndterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring.Overstyringshåndterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.OverstyringUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

//Requestscoped pga at vi må mellomlagre forrige uttaksresultat i field for å bruke til historikk
@RequestScoped
@DtoTilServiceAdapter(dto = OverstyringUttakDto.class, adapter = Overstyringshåndterer.class)
public class UttakOverstyringshåndterer extends AbstractOverstyringshåndterer<OverstyringUttakDto> {

    private UttakRepository uttakRepository;
    private UttakResultatEntitet forrigeResultat;
    private AksjonspunktRepository aksjonspunktRepository;

    UttakOverstyringshåndterer() {
        // for CDI proxy
    }

    @Inject
    public UttakOverstyringshåndterer(BehandlingRepositoryProvider repositoryProverider,
                                      HistorikkTjenesteAdapter historikkTjenesteAdapter,
                                      AksjonspunktRepository aksjonspunktRepository) {
        super(repositoryProverider, historikkTjenesteAdapter, OVERSTYRING_AV_UTTAKPERIODER);
        this.uttakRepository = repositoryProverider.getUttakRepository();
        this.aksjonspunktRepository = aksjonspunktRepository;
    }

    @Override
    public OppdateringResultat håndterOverstyring(OverstyringUttakDto dto, Behandling behandling, BehandlingskontrollKontekst kontekst) {
        this.forrigeResultat = uttakRepository.hentUttakResultat(behandling);
        return OppdateringResultat.utenOveropp();
    }

    @Override
    protected void lagHistorikkInnslag(Behandling behandling, OverstyringUttakDto dto) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(OVERSTYRING_AV_UTTAKPERIODER.getKode());
        List<Historikkinnslag> historikkinnslag = UttakHistorikkUtil.forOverstyring().lagHistorikkinnslag(
            behandling, aksjonspunktDefinisjon, dto.getPerioder(), forrigeResultat.getGjeldendePerioder());
        historikkinnslag.forEach(innslag -> getHistorikkAdapter().lagInnslag(innslag));
    }
}
