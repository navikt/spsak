package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureToggles;
import no.nav.vedtak.felles.integrasjon.unleash.strategier.ByAnsvarligSaksbehandlerStrategy;

public class FastsettePeriodeFeatureToggles implements FeatureToggles {

    static final String FORELDREPENGER_FØDSEL_FEATURE_TOGGLE_NAVN = "fpsak.uttak-foreldrepenger-fodsel-delregel";

    private final Unleash unleash;
    private final Behandling behandling;

    FastsettePeriodeFeatureToggles(Unleash unleash, Behandling behandling) {
        this.unleash = unleash;
        this.behandling = behandling;
    }

    @Override
    public boolean foreldrepengerFødsel() {
        UnleashContext build = UnleashContext.builder()
            .addProperty(ByAnsvarligSaksbehandlerStrategy.SAKSBEHANDLER_IDENT, behandling.getAnsvarligSaksbehandler())
            .build();
        return unleash.isEnabled(FORELDREPENGER_FØDSEL_FEATURE_TOGGLE_NAVN, build);
    }
}
