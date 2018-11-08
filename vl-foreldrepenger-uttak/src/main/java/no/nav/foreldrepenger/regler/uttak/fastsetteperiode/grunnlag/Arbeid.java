package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.util.Objects;

public class Arbeid {

    private static final String ARBEIDSPROSENT_FELT = "arbeidsprosent";
    private static final String STILLINGSPROSENT_FELT = "stillingsprosent";
    private static final String PERMISJONSPROSENT_FELT = "permisjonsprosent";

    private final BigDecimal arbeidsprosent;
    private final BigDecimal stillingsprosent;
    private final BigDecimal permisjonsprosent;
    private final boolean gradert;

    Arbeid(BigDecimal arbeidsprosent, BigDecimal stillingsprosent, BigDecimal permisjonsprosent, boolean gradert) {
        this.arbeidsprosent = arbeidsprosent;
        this.stillingsprosent = stillingsprosent;
        this.permisjonsprosent = permisjonsprosent;
        this.gradert = gradert;
    }

    public BigDecimal getArbeidsprosent() {
        return arbeidsprosent;
    }

    public BigDecimal getStillingsprosent() {
        return stillingsprosent;
    }

    public BigDecimal getPermisjonsprosent() {
        return permisjonsprosent;
    }

    public boolean isGradert() {
        return gradert;
    }

    public static Arbeid forFrilans(BigDecimal arbeidsprosent) {
        Objects.requireNonNull(arbeidsprosent, ARBEIDSPROSENT_FELT);
        boolean gradert = erGradert(arbeidsprosent);
        return new Arbeid(arbeidsprosent, null, null, gradert);
    }

    public static Arbeid forSelvstendigNæringsdrivende(BigDecimal arbeidsprosent) {
        Objects.requireNonNull(arbeidsprosent, ARBEIDSPROSENT_FELT);
        boolean gradert = erGradert(arbeidsprosent);
        return new Arbeid(arbeidsprosent, null, null, gradert);
    }

    public static Arbeid forOrdinærtArbeid(BigDecimal arbeidsprosent, BigDecimal stillingsprosent, BigDecimal permisjonsprosent) {
        return forOrdinærtArbeid(arbeidsprosent, stillingsprosent, permisjonsprosent, false);
    }

    public static Arbeid forGradertOrdinærtArbeid(BigDecimal arbeidsprosent, BigDecimal stillingsprosent) {
        return forOrdinærtArbeid(arbeidsprosent, stillingsprosent, null, true);
    }

    private static Arbeid forOrdinærtArbeid(BigDecimal arbeidsprosent, BigDecimal stillingsprosent, BigDecimal permisjonsprosent, boolean gradert) {
        Objects.requireNonNull(stillingsprosent, STILLINGSPROSENT_FELT);
        Objects.requireNonNull(arbeidsprosent, ARBEIDSPROSENT_FELT);
        Objects.requireNonNull(arbeidsprosent, PERMISJONSPROSENT_FELT);
        return new Arbeid(arbeidsprosent, stillingsprosent, permisjonsprosent, gradert);
    }

    public static Arbeid forAnnet() {
        return new Arbeid(BigDecimal.ZERO, null, null, false);
    }

    private static boolean erGradert(BigDecimal arbeidsprosent) {
        return arbeidsprosent.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arbeid arbeid = (Arbeid) o;
        return gradert == arbeid.gradert &&
                Objects.equals(arbeidsprosent, arbeid.arbeidsprosent) &&
                Objects.equals(stillingsprosent, arbeid.stillingsprosent) &&
                Objects.equals(permisjonsprosent, arbeid.permisjonsprosent);
    }

    @Override
    public int hashCode() {

        return Objects.hash(arbeidsprosent, stillingsprosent, permisjonsprosent, gradert);
    }
}
