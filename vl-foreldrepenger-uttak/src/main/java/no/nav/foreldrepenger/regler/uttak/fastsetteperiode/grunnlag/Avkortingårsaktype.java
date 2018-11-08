package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public enum Avkortingårsaktype {
    SØKT_FOR_SENT(true),
    MAKSGRENSE_OVERSREDET(false),
    IKKE_OMSORG(true);

    private boolean trekkDager;

    Avkortingårsaktype(boolean trekkDager) {
        this.trekkDager = trekkDager;
    }

    public boolean trekkDager() {
        return trekkDager;
    }
}
