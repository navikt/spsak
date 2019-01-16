package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg.TransisjonType;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTransisjonEvent;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;

/** Håndtere opprydding i Aksjonspunkt og Vilkår ved overhopp framover */
@ApplicationScoped
public class BehandlingskontrollFremoverhoppTransisjonEventObserver {

    private BehandlingRepository behandlingRepository;
    private BehandlingModellRepository modellRepository;
    private AksjonspunktRepository aksjonspunktRepository;

    @Inject
    public BehandlingskontrollFremoverhoppTransisjonEventObserver(GrunnlagRepositoryProvider repositoryProvider,
                                                                  BehandlingModellRepository modellRepository) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.modellRepository = modellRepository;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    protected BehandlingskontrollFremoverhoppTransisjonEventObserver() {
        super();
        // for CDI proxy
    }

    public void observerBehandlingSteg(@Observes BehandlingTransisjonEvent transisjonEvent) {
        Behandling behandling = behandlingRepository.hentBehandling(transisjonEvent.getBehandlingId());
        BehandlingModellImpl modell = modellRepository.getModell(behandling.getType(), behandling.getFagsakYtelseType());
        StegTransisjon transisjon = modell.finnTransisjon(transisjonEvent.getTransisjonIdentifikator());
        if (!(transisjon.getClass().equals(FremoverhoppTransisjon.class)) || !transisjonEvent.erOverhopp()) {
            return;
        }

        BehandlingStegType førsteSteg = transisjonEvent.getFørsteSteg();
        BehandlingStegType sisteSteg = transisjonEvent.getSisteSteg();
        Optional<BehandlingStegStatus> førsteStegStatus = transisjonEvent.getFørsteStegStatus();

        boolean medInngangFørsteSteg = !førsteStegStatus.isPresent() || førsteStegStatus.get().erVedInngang();

        Set<String> aksjonspunktDefinisjonerEtterFra = modell.finnAksjonspunktDefinisjonerFraOgMed(førsteSteg, medInngangFørsteSteg);
        Set<String> aksjonspunktDefinisjonerEtterTil = modell.finnAksjonspunktDefinisjonerFraOgMed(sisteSteg, true);

        Set<String> mellomliggende = new HashSet<>(aksjonspunktDefinisjonerEtterFra);
        mellomliggende.removeAll(aksjonspunktDefinisjonerEtterTil);

        håndterAksjonspunkter(behandling, mellomliggende, (a) -> {
            if (a.erÅpentAksjonspunkt() && a.erAktivt()) {
                aksjonspunktRepository.setTilAvbrutt(a);
            }
        });

        if (!medInngangFørsteSteg) {
            // juster til neste steg dersom vi står ved utgang av steget.
            førsteSteg = modell.finnNesteSteg(førsteSteg).getBehandlingStegType();
        }

        final BehandlingStegType finalFørsteSteg = førsteSteg;
        modell.hvertStegFraOgMedTil(førsteSteg, sisteSteg, false)
                .forEach(s -> s.getSteg().vedTransisjon(transisjonEvent.getKontekst(), behandling, s, TransisjonType.HOPP_OVER_FRAMOVER, finalFørsteSteg, sisteSteg, transisjonEvent.getSkalTil()));

        // Lagre oppdateringer; eventhåndteringen skal være autonom og selv ferdigstille oppdateringer på behandlingen
        behandlingRepository.lagre(behandling, transisjonEvent.getKontekst().getSkriveLås());
    }

    private void håndterAksjonspunkter(Behandling behandling, Set<String> mellomliggendeAksjonspunkt,
            Consumer<Aksjonspunkt> action) {
        behandling.getAksjonspunkter().stream()
                .filter(a -> mellomliggendeAksjonspunkt.contains(a.getAksjonspunktDefinisjon().getKode()))
                .forEach(action);
    }

}
