package no.nav.foreldrepenger.behandlingslager.behandling.verge;

import no.nav.foreldrepenger.domene.typer.AktørId;

public class VergeAggregat {

    private final Verge verge;

    public VergeAggregat(Verge verge) {
        this.verge = verge;
    }

    public Verge getVerge() {
        return verge;
    }

    public BrevMottaker getBrevMottaker() {
        return verge.getBrevMottaker();
    }

    public AktørId getAktørId() {
        return verge.getBruker().getAktørId();
    }
}
