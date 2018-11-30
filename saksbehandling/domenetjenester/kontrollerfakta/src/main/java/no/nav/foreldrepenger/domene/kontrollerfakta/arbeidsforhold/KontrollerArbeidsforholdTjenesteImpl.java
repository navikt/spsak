package no.nav.foreldrepenger.domene.kontrollerfakta.arbeidsforhold;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjeneste;


@FagsakYtelseTypeRef("FP")
@BehandlingTypeRef
@StartpunktRef("KONTROLLER_ARBEIDSFORHOLD")
@ApplicationScoped
public class KontrollerArbeidsforholdTjenesteImpl implements KontrollerFaktaTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(KontrollerArbeidsforholdTjenesteImpl.class);
    private BehandlingRepository behandlingRepository;
    private KontrollerArbeidsforholdUtledereTjeneste utlederTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    protected KontrollerArbeidsforholdTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public KontrollerArbeidsforholdTjenesteImpl(BehandlingRepositoryProvider repository,
                                                @FagsakYtelseTypeRef("FP") @BehandlingTypeRef @StartpunktRef("KONTROLLER_ARBEIDSFORHOLD") KontrollerArbeidsforholdUtledereTjeneste utlederTjeneste,
                                                BehandlingskontrollTjeneste behandlingskontrollTjeneste) {

        this.behandlingRepository = repository.getBehandlingRepository();
        this.utlederTjeneste = utlederTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkter(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return utled(behandling);
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterTilHøyreForStartpunkt(Long behandlingId, StartpunktType startpunktType) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        List<AksjonspunktResultat> aksjonspunktResultat = utledAksjonspunkter(behandlingId);
        return filtrerAksjonspunkterTilVenstreForStartpunkt(behandling, aksjonspunktResultat, startpunktType);
    }

    private List<AksjonspunktResultat> filtrerAksjonspunkterTilVenstreForStartpunkt(Behandling behandling, List<AksjonspunktResultat> aksjonspunktResultat, StartpunktType startpunkt) {
        // Fjerner aksjonspunkter som ikke skal løses i eller etter steget som følger av startpunktet:
        return aksjonspunktResultat.stream()
            .filter(ap -> skalBeholdeAksjonspunkt(behandling, startpunkt, ap.getAksjonspunktDefinisjon()))
            .collect(Collectors.toList());
    }

    private boolean skalBeholdeAksjonspunkt(Behandling behandling, StartpunktType startpunkt, AksjonspunktDefinisjon apDef) {
        boolean skalBeholde = behandlingskontrollTjeneste.skalAksjonspunktReaktiveresIEllerEtterSteg(
            behandling, startpunkt.getBehandlingSteg(), apDef);
        if (!skalBeholde) {
            logger.debug("Fjerner aksjonspunkt {} da det skal løses før startsteg {}.",
                apDef.getKode(), behandling.getStartpunkt().getBehandlingSteg().getKode()); //NOSONAR
        }
        return skalBeholde;
    }

    private List<AksjonspunktResultat> utled(Behandling behandling) {
        final List<AksjonspunktUtleder> aksjonspunktUtleders = utlederTjeneste.utledUtledereFor(behandling);
        List<AksjonspunktResultat> aksjonspunktResultater = new ArrayList<>();
        for (AksjonspunktUtleder aksjonspunktUtleder : aksjonspunktUtleders) {
            aksjonspunktResultater.addAll(aksjonspunktUtleder.utledAksjonspunkterFor(behandling));
        }
        return aksjonspunktResultater.stream()
            .distinct() // Unngå samme aksjonspunkt flere multipliser
            .collect(toList());
    }
}
