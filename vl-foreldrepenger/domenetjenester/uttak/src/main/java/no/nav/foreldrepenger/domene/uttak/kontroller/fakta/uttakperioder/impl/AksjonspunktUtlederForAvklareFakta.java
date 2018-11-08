package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaUttakTjeneste;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_FAKTA_UTTAK;

@ApplicationScoped
public class AksjonspunktUtlederForAvklareFakta implements AksjonspunktUtleder {

    private KontrollerFaktaUttakTjeneste kontrollerFaktaUttakTjeneste;

    AksjonspunktUtlederForAvklareFakta() {
        // For CDI
    }

    @Inject
    public AksjonspunktUtlederForAvklareFakta(@FagsakYtelseTypeRef("FP") KontrollerFaktaUttakTjeneste kontrollerFaktaUttakTjeneste) {
        this.kontrollerFaktaUttakTjeneste = kontrollerFaktaUttakTjeneste;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {

        boolean finnesOverlappendePerioder = kontrollerFaktaUttakTjeneste.finnesOverlappendePerioder(behandling);
        if (finnesOverlappendePerioder || finnesPeriodeSomMåKontrolleres(behandling)) {
            return Collections.singletonList(AksjonspunktResultat.opprettForAksjonspunkt(AVKLAR_FAKTA_UTTAK));
        }
        return Collections.emptyList();
    }

    private boolean finnesPeriodeSomMåKontrolleres(Behandling behandling) {
        return kontrollerFaktaUttakTjeneste.hentKontrollerFaktaPerioder(behandling).getPerioder()
            .stream().anyMatch(kontrollerFaktaPeriode -> !kontrollerFaktaPeriode.erBekreftet());
    }
}
