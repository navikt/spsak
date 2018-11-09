package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.tjeneste;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.brev.SendVarselTjeneste;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.IverksetteVedtakSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;

@BehandlingStegRef(kode = "IVEDSTEG")
@BehandlingTypeRef("BT-006") //Innsyn
@FagsakYtelseTypeRef
@ApplicationScoped
public class IverksetteInnsynVedtakSteg implements IverksetteVedtakSteg {

    private SendVarselTjeneste varselTjeneste;

    IverksetteInnsynVedtakSteg() {
        // for CDI proxy
    }

    @Inject
    IverksetteInnsynVedtakSteg(SendVarselTjeneste varselTjeneste) {
        this.varselTjeneste = varselTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        kontekstbestillVedtaksbrev(kontekst);
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private void kontekstbestillVedtaksbrev(BehandlingskontrollKontekst kontekst) {
        varselTjeneste.sendVarsel(kontekst.getBehandlingId(), "VedtaksBrev");
    }
}
