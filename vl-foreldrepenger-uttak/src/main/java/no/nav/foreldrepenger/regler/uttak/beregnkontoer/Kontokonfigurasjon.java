package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

class Kontokonfigurasjon {

    private Stønadskontotype stønadskontotype;
    private Parametertype parametertype;

    Kontokonfigurasjon(Stønadskontotype stønadskontotype, Parametertype parametertype) {
        this.stønadskontotype = stønadskontotype;
        this.parametertype = parametertype;
    }

    public Stønadskontotype getStønadskontotype() {
        return stønadskontotype;
    }

    public Parametertype getParametertype() {
        return parametertype;
    }
}
