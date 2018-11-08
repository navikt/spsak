package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app;

import static no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OverhoppKontroll.FREMOVERHOPP;
import static no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OverhoppKontroll.HENLEGGELSE;
import static no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OverhoppKontroll.TILBAKEHOPP;
import static no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OverhoppKontroll.UTEN_OVERHOPP;

import no.nav.foreldrepenger.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;

public class OppdateringResultat {

    private BehandlingStegType nesteSteg;
    private OverhoppKontroll overhoppKontroll;
    private BehandlingResultatType henleggelseResultat;
    private String henleggingsbegrunnelse;
    private boolean beholdAksjonspunktÅpent = false;
    private TransisjonIdentifikator transisjonId;

    public OppdateringResultat(BehandlingStegType nesteSteg, OverhoppKontroll overhoppKontroll, TransisjonIdentifikator transisjonId) {
        this.overhoppKontroll = overhoppKontroll;
        this.nesteSteg = nesteSteg;
        this.transisjonId = transisjonId;
    }

    public OppdateringResultat(OverhoppKontroll overhoppKontroll) {
        this.overhoppKontroll = overhoppKontroll;
        this.nesteSteg = null;
    }

    public OppdateringResultat(OverhoppKontroll overhoppKontroll, BehandlingResultatType henleggelseResultat, String henleggingsbegrunnelse) {
        this.overhoppKontroll = overhoppKontroll;
        this.henleggelseResultat = henleggelseResultat;
        this.henleggingsbegrunnelse = henleggingsbegrunnelse;
    }

    public static OppdateringResultat utenOveropp() {
        return new OppdateringResultat(UTEN_OVERHOPP);
    }

    public static OppdateringResultat medTilbakehopp(BehandlingStegType behandlingStegType) {
        return new OppdateringResultat(behandlingStegType, TILBAKEHOPP, null);
    }

    public static OppdateringResultat medFremoverHopp(TransisjonIdentifikator transisjonId) {
        return new OppdateringResultat(null, FREMOVERHOPP, transisjonId);
    }

    public static OppdateringResultat medHenleggelse(BehandlingResultatType henleggelseResultat, String henleggingsbegrunnelse) {
        return new OppdateringResultat(HENLEGGELSE, henleggelseResultat, henleggingsbegrunnelse);
    }

    public static OppdateringResultat beholdAksjonspunktÅpent() {
        OppdateringResultat oppdateringResultat = new OppdateringResultat(UTEN_OVERHOPP);
        oppdateringResultat.beholdAksjonspunktÅpent = true;
        return oppdateringResultat;
    }

    public BehandlingStegType getNesteSteg() {
        return nesteSteg;
    }

    public TransisjonIdentifikator getTransisjon() {
        return transisjonId;
    }

    public OverhoppKontroll getOverhoppKontroll() {
        return overhoppKontroll;
    }

    public BehandlingResultatType getHenleggelseResultat() {
        return henleggelseResultat;
    }

    public String getHenleggingsbegrunnelse() {
        return henleggingsbegrunnelse;
    }

    public boolean skalUtføreAksjonspunkt() {
        return !beholdAksjonspunktÅpent;
    }

    @Override
    public String toString() {
        return "OppdateringResultat{" +
            "nesteSteg=" + nesteSteg +
            ", transisjonId=" + transisjonId +
            ", overhoppKontroll=" + overhoppKontroll +
            ", henleggelseResultat=" + henleggelseResultat +
            ", henleggingsbegrunnelse='" + henleggingsbegrunnelse + '\'' +
            '}';
    }
}
