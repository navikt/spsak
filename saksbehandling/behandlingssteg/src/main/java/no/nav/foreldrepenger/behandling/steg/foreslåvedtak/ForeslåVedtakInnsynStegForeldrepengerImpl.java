package no.nav.foreldrepenger.behandling.steg.foreslåvedtak;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

@BehandlingStegRef(kode = "FORVEDSTEG")
@BehandlingTypeRef("BT-006") //Innsyn
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class ForeslåVedtakInnsynStegForeldrepengerImpl implements ForeslåVedtakSteg {

    ForeslåVedtakInnsynStegForeldrepengerImpl() {
        // for CDI proxy
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        return BehandleStegResultat.utførtMedAksjonspunkter(Collections.singletonList(AksjonspunktDefinisjon.FORESLÅ_VEDTAK));
    }


}
