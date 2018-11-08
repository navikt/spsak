package no.nav.foreldrepenger.regler.uttak.konfig;

public interface FeatureToggles {

    boolean DEFAULT = false;

    default boolean foreldrepengerFødsel() {
        return FeatureToggles.DEFAULT;
    }
}
