package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.IverksetteVedtakSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;

@BehandlingStegRef(kode = "IVEDSTEG")
@BehandlingTypeRef
@FagsakYtelseTypeRef
@Alternative
@Priority(1)
class IverksetteVedtakStegMock implements IverksetteVedtakSteg {
    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }
}
