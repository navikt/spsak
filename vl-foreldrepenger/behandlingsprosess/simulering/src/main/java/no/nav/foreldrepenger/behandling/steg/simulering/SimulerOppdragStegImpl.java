package no.nav.foreldrepenger.behandling.steg.simulering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.vedtak.felles.integrasjon.unleash.FeatureToggle;
import no.nav.vedtak.felles.integrasjon.unleash.strategier.ByAnsvarligSaksbehandlerStrategy;

@BehandlingStegRef(kode = "SIMOPP")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class SimulerOppdragStegImpl implements SimulerOppdragSteg {

    private static final String FEATURE_TOGGLE_NAVN = "fpsak.simuler-oppdrag";

    private BehandlingRepository behandlingRepository;
    private Unleash unleash;

    SimulerOppdragStegImpl() {
        // for CDI proxy
    }

    @Inject
    public SimulerOppdragStegImpl(BehandlingRepositoryProvider repositoryProvider,
                                  @FeatureToggle("fpsak") Unleash unleash) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.unleash = unleash;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        if (simulerOppdragToggle(behandling.getAnsvarligSaksbehandler())) {
            // send oppdrag til simulering i fpoppdrag
            return BehandleStegResultat.settPåVent();
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private boolean simulerOppdragToggle(String ansvarligSaksbehandler) {
        UnleashContext build = UnleashContext.builder()
            .addProperty(ByAnsvarligSaksbehandlerStrategy.SAKSBEHANDLER_IDENT, ansvarligSaksbehandler)
            .build();
        return unleash.isEnabled(FEATURE_TOGGLE_NAVN, build);
    }
}
