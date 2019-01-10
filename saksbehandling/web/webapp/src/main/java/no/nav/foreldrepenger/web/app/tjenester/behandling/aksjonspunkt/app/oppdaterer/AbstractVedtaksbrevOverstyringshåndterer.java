package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.domene.vedtak.VedtakTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.ForeslaVedtakAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public abstract class AbstractVedtaksbrevOverstyringshåndterer {

    protected HistorikkTjenesteAdapter historikkApplikasjonTjeneste;
    protected AksjonspunktRepository aksjonspunktRepository;
    protected TotrinnTjeneste totrinnTjeneste;
    protected VedtakTjeneste vedtakTjeneste;

    AbstractVedtaksbrevOverstyringshåndterer() {
        // for CDI proxy
    }

    AbstractVedtaksbrevOverstyringshåndterer(GrunnlagRepositoryProvider repositoryProvider,
                                             HistorikkTjenesteAdapter historikkApplikasjonTjeneste,
                                             TotrinnTjeneste totrinnTjeneste,
                                             VedtakTjeneste vedtakTjeneste) {
        this.historikkApplikasjonTjeneste = historikkApplikasjonTjeneste;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.totrinnTjeneste = totrinnTjeneste;
        this.vedtakTjeneste = vedtakTjeneste;
    }

    void opprettAksjonspunktForFatterVedtak(Behandling behandling) {
        Optional<Aksjonspunkt> apOptional = behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.FATTER_VEDTAK);
        if (apOptional.isPresent()) {
            aksjonspunktRepository.setReåpnet(apOptional.get());
        } else {
            aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.FATTER_VEDTAK, BehandlingStegType.FORESLÅ_VEDTAK);
        }
    }

    void setToTrinnskontroll(Behandling behandling) {
        AksjonspunktDefinisjon foreslaVedtak = aksjonspunktRepository.finnAksjonspunktDefinisjon(ForeslaVedtakAksjonspunktDto.AKSJONSPUNKT_KODE);
        if (!behandling.harAksjonspunktMedType(foreslaVedtak)) {
            leggTilOgUtførAksjonspunkt(behandling, foreslaVedtak);
        } else {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, foreslaVedtak);
        }
    }

    private void leggTilOgUtførAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon foreslaVedtakAksjonspunkt) {
        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, foreslaVedtakAksjonspunkt);
        aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, foreslaVedtakAksjonspunkt); // må kalles før setTilUtført for å bevare UTFØRT status.
        aksjonspunktRepository.setTilUtført(aksjonspunkt, null);
    }

    void opprettHistorikkinnslag(Behandling behandling) {
        VedtakResultatType vedtakResultatType = vedtakTjeneste.utledVedtakResultatType(behandling);

        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder()
            .medResultat(vedtakResultatType)
            .medSkjermlenke(SkjermlenkeType.VEDTAK)
            .medHendelse(HistorikkinnslagType.FORSLAG_VEDTAK);

        Historikkinnslag innslag = new Historikkinnslag();
        innslag.setType(HistorikkinnslagType.FORSLAG_VEDTAK);
        innslag.setAktør(HistorikkAktør.SAKSBEHANDLER);
        innslag.setBehandlingId(behandling.getId());
        tekstBuilder.build(innslag);
        historikkApplikasjonTjeneste.lagInnslag(innslag);
    }
}
