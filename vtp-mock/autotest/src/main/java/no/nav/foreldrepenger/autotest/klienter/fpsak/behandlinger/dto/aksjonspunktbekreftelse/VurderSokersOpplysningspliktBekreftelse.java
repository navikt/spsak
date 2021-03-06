package no.nav.foreldrepenger.autotest.klienter.fpsak.behandlinger.dto.aksjonspunktbekreftelse;

import no.nav.foreldrepenger.autotest.klienter.fpsak.behandlinger.dto.behandling.Behandling;
import no.nav.foreldrepenger.autotest.klienter.fpsak.fagsak.dto.Fagsak;

@BekreftelseKode(kode="5017")
public class VurderSokersOpplysningspliktBekreftelse extends AksjonspunktBekreftelse{

    protected Boolean erVilkarOk;
    
    public VurderSokersOpplysningspliktBekreftelse(Fagsak fagsak, Behandling behandling) {
        super(fagsak, behandling);
    }
    
    public void bekreftGodkjent() {
        erVilkarOk = true;
    }
    
    public void bekreftAvvist() {
        erVilkarOk = false;
    }
}
