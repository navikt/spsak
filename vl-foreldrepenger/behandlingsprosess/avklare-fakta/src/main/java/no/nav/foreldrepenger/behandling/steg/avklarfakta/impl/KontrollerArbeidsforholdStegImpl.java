package no.nav.foreldrepenger.behandling.steg.avklarfakta.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.avklarfakta.api.KontrollerArbeidsforholdSteg;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjeneste;


@BehandlingStegRef(kode = "KOARB")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@StartpunktRef("KONTROLLER_ARBEIDSFORHOLD")
@ApplicationScoped
public class KontrollerArbeidsforholdStegImpl implements KontrollerArbeidsforholdSteg {

    private KontrollerFaktaTjeneste tjeneste;

    public KontrollerArbeidsforholdStegImpl() {
        // for CDI proxy
    }

    @Inject
    public KontrollerArbeidsforholdStegImpl(@FagsakYtelseTypeRef("FP") @StartpunktRef("KONTROLLER_ARBEIDSFORHOLD") KontrollerFaktaTjeneste tjeneste) {
        this.tjeneste = tjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst){
        List<AksjonspunktResultat> aksjonspunktResultat = tjeneste.utledAksjonspunkter(kontekst.getBehandlingId());
        return BehandleStegResultat.utførtMedAksjonspunktResultater(aksjonspunktResultat);
    }
}
