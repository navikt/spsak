package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste;

import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VENT_PGA_FOR_TIDLIG_SØKNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VENT_PÅ_SØKNAD;
import static no.nav.vedtak.util.Objects.check;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;

@ApplicationScoped
public class KompletthetModell {

    private static Map<AksjonspunktDefinisjon, BiFunction<KompletthetModell, Behandling, KompletthetResultat>> KOMPLETTHETSFUNKSJONER;

    static {
        Map<AksjonspunktDefinisjon, BiFunction<KompletthetModell, Behandling, KompletthetResultat>> map = new HashMap<>();
        map.put(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, (kontroller, behandling) -> finnKompletthetssjekker(kontroller, behandling).vurderForsendelseKomplett(behandling));
        map.put(VENT_PGA_FOR_TIDLIG_SØKNAD, (kontroller, behandling) -> finnKompletthetssjekker(kontroller, behandling).vurderSøknadMottattForTidlig(behandling));
        map.put(VENT_PÅ_SØKNAD, (kontroller, behandling) -> finnKompletthetssjekker(kontroller, behandling).vurderSøknadMottatt(behandling));

        // Køet behandling kan inntreffe FØR kompletthetssteget er passert - men er ikke tilknyttet til noen kompletthetssjekk
        map.put(AUTO_KØET_BEHANDLING, (kontroller, behandling) -> KompletthetResultat.oppfylt());
        
        KOMPLETTHETSFUNKSJONER = Collections.unmodifiableMap(map);
    }

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private KompletthetsjekkerProvider kompletthetsjekkerProvider;
    private KodeverkTabellRepository kodeverkTabellRepository;

    public KompletthetModell() {
        // For CDI proxy
    }

    @Inject
    public KompletthetModell(BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                             KompletthetsjekkerProvider kompletthetsjekkerProvider,
                             KodeverkTabellRepository kodeverkTabellRepository) {
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.kompletthetsjekkerProvider = kompletthetsjekkerProvider;
        this.kodeverkTabellRepository = kodeverkTabellRepository;
    }

    private static Kompletthetsjekker finnKompletthetssjekker(KompletthetModell kompletthetModell, Behandling behandling) {
        return kompletthetModell.kompletthetsjekkerProvider.finnKompletthetsjekkerFor(behandling);
    }

    /**
     * Ranger autopunktene i kompletthetssjekk i samme rekkefølge som de ville ha blitt gjort i behandlingsstegene.
     * Dvs. at bruken av disse kompletthetssjekkene skjer UTENFOR behandlingsstegene, som introduserer risikoen for at
     * rekkefølgen avviker fra rekkefølgen INNE I behandlingsstegene. Bør være så enkel som mulig.
     **/
        // Rangering 1: Tidligste steg (dvs. autopunkt ville blitt eksekvert tidligst i behandlingsstegene)
    public List<AksjonspunktDefinisjon> rangerKompletthetsfunksjonerKnyttetTilAutopunkt(Behandling behandling) {
        Comparator<AksjonspunktDefinisjon> stegRekkefølge = (apDef1, apDef2) ->
            behandlingskontrollTjeneste.sammenlignRekkefølge(behandling, apDef1.getVurderingspunktDefinisjon().getBehandlingSteg(), apDef2.getVurderingspunktDefinisjon().getBehandlingSteg());
        // Rangering 2: Autopunkt som kjøres igjen ved gjenopptakelse blir eksekvert FØR ikke-gjenopptagende i samme behandlingssteg
        //      Det er bare en implisitt antakelse at kodes riktig i stegene der Autopunkt brukes; bør forbedre dette.
        Comparator<AksjonspunktDefinisjon> tilbakehoppRekkefølge = (apDef1, apDef2) ->
            Boolean.compare(apDef1.tilbakehoppVedGjenopptakelse(), apDef2.tilbakehoppVedGjenopptakelse());

        return KOMPLETTHETSFUNKSJONER.keySet().stream()
            .map(apDef -> kodeverkTabellRepository.finnAksjonspunktDefinisjon(apDef.getKode())) // Må oppfriskes fra Hibernate
            .sorted(stegRekkefølge
                .thenComparing(tilbakehoppRekkefølge.reversed()))
            .collect(toList());
    }

    public boolean erKompletthetssjekkPassert(Behandling behandling) {
        return behandlingskontrollTjeneste.erStegPassert(behandling, BehandlingStegType.VURDER_KOMPLETTHET);
    }

    public KompletthetResultat vurderKompletthet(Behandling behandling) {
        Optional<AksjonspunktDefinisjon> åpentAutopunkt = behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).stream()
            .map(Aksjonspunkt::getAksjonspunktDefinisjon)
            .findFirst();
        if (åpentAutopunkt.isPresent() && erAutopunktTilknyttetKompletthetssjekk(åpentAutopunkt)) {
                return vurderKompletthet(behandling, åpentAutopunkt.get());
        }
        if (!erKompletthetssjekkPassert(behandling)) {
            // Kompletthetssjekk er ikke passert, men står heller ikke på autopunkt tilknyttet kompletthet som skal sjekkes
            return KompletthetResultat.oppfylt();
        }
        // Default dersom ingen match på åpent autopunkt tilknyttet kompletthet OG kompletthetssjekk er passert
        AksjonspunktDefinisjon defaultAutopunkt = finnSisteAutopunktKnyttetTilKompletthetssjekk(behandling);
        return vurderKompletthet(behandling, defaultAutopunkt);
    }

    private boolean erAutopunktTilknyttetKompletthetssjekk(Optional<AksjonspunktDefinisjon> åpentAutopunkt) {
        return KOMPLETTHETSFUNKSJONER.containsKey(åpentAutopunkt.get());
    }

    private AksjonspunktDefinisjon finnSisteAutopunktKnyttetTilKompletthetssjekk(Behandling behandling) {
        List<AksjonspunktDefinisjon> rangerteAutopunkter = rangerKompletthetsfunksjonerKnyttetTilAutopunkt(behandling);
        check(rangerteAutopunkter.size() > 0, "Utvklerfeil: Skal alltid finnes kompletthetsfunksjoner"); //$NON-NLS-1$
        // Hent siste
        return rangerteAutopunkter.get(rangerteAutopunkter.size() - 1);
    }

    public KompletthetResultat vurderKompletthet(Behandling behandling, AksjonspunktDefinisjon autopunkt) {
        BiFunction<KompletthetModell, Behandling, KompletthetResultat> kompletthetsfunksjon = KOMPLETTHETSFUNKSJONER.get(autopunkt);
        if (kompletthetsfunksjon == null) {
            throw new IllegalStateException("Utviklerfeil: Kan ikke finne kompletthetsfunksjon for autopunkt: " + autopunkt.getKode());
        }
        return kompletthetsfunksjon.apply(this, behandling);
    }

}
