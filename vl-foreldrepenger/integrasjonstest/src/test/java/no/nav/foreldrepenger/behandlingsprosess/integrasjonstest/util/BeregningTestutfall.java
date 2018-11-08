package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util;

/**
 *  Støtter kun enkle asserts mot den siste beregningen som ligger på beregningsresultatet.
 */
public class BeregningTestutfall {

    private Long behandlingId;
    private Long beregnetTilkjentYtelse;
    private Long opprinneligBeregnetTilkjentYtelse;
    private boolean overstyrt;

    private BeregningTestutfall(Long behandlingId, Long beregnetTilkjentYtelse, Long opprinneligBeregnetTilkjentYtelse, boolean overstyrt) {
        this.behandlingId = behandlingId;
        this.beregnetTilkjentYtelse = beregnetTilkjentYtelse;
        this.opprinneligBeregnetTilkjentYtelse = opprinneligBeregnetTilkjentYtelse;
        this.overstyrt = overstyrt;
    }

    public static BeregningTestutfall resultat(Long behandlingId, Long beregnetTilkjentYtelse, Long opprinneligBeregnetTilkjentYtelse, boolean overstyrt) {
        return new BeregningTestutfall(behandlingId, beregnetTilkjentYtelse, opprinneligBeregnetTilkjentYtelse, overstyrt);
    }

    Long getBehandlingId() {
        return behandlingId;
    }

    Long getBeregnetTilkjentYtelse() {
        return beregnetTilkjentYtelse;
    }

    Long getOpprinneligBeregnetTilkjentYtelse() {
        return opprinneligBeregnetTilkjentYtelse;
    }

    boolean erOverstyrt() {
        return overstyrt;
    }
}
