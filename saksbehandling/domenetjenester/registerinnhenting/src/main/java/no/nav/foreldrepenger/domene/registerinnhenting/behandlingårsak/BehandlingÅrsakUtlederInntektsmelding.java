package no.nav.foreldrepenger.domene.registerinnhenting.behandlingårsak;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;

@Dependent
class BehandlingÅrsakUtlederInntektsmelding {

    @Inject
    public BehandlingÅrsakUtlederInntektsmelding() {
        //For CDI
    }

    BehandlingÅrsakType utledBehandlingÅrsak() {
        return BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING;
    }
}
