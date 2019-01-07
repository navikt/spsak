package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static java.util.Collections.singletonList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.FaktaOmBeregningTilfelle;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class AksjonspunktUtlederForBeregning implements AksjonspunktUtleder {

    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;
    private BeregningsperiodeTjeneste beregningsperiodeTjeneste;

    public AksjonspunktUtlederForBeregning() {
    }

    @Inject
    public AksjonspunktUtlederForBeregning(AksjonspunktRepository aksjonspunktRepository, FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste, BeregningsperiodeTjeneste beregningsperiodeTjeneste) {
        this.faktaOmBeregningTilfelleTjeneste = faktaOmBeregningTilfelleTjeneste;
        this.aksjonspunktRepository = aksjonspunktRepository;
        this.beregningsperiodeTjeneste = beregningsperiodeTjeneste;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        if (beregningsperiodeTjeneste.skalBehandlingSettesPåVent(behandling)) {
            return singletonList(opprettSettPåVentAutopunktForVentPåRapportering(behandling));
        }
        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = faktaOmBeregningTilfelleTjeneste.utledOgLagreFaktaOmBeregningTilfeller(behandling);
        if (!faktaOmBeregningTilfeller.isEmpty()) {
            return singletonList(AksjonspunktResultat.opprettForAksjonspunkt(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN));
        }
        return Collections.emptyList();
    }

    private AksjonspunktResultat opprettSettPåVentAutopunktForVentPåRapportering(Behandling behandling) {
        LocalDate fristDato = beregningsperiodeTjeneste.utledBehandlingPåVentFrist(behandling);
        LocalDateTime frist = LocalDateTime.of(fristDato, LocalDateTime.now(FPDateUtil.getOffset()).toLocalTime());
        AksjonspunktDefinisjon apDef = AksjonspunktDefinisjon.AUTO_VENT_PÅ_INNTEKT_RAPPORTERINGSFRIST;
        return AksjonspunktResultat.opprettForAksjonspunktMedCallback(apDef, ap -> aksjonspunktRepository.setFrist(ap, frist, Venteårsak.VENT_INNTEKT_RAPPORTERINGSFRIST));
    }
}
