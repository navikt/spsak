package no.nav.foreldrepenger.domene.registerinnhenting.behandlingårsak;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;

@Dependent
class BehandlingÅrsakUtlederAktørArbeid {

    @Inject
    public BehandlingÅrsakUtlederAktørArbeid() {
        //For CDI
    }

    BehandlingÅrsakType utledBehandlingÅrsak() {
        return BehandlingÅrsakType.RE_REGISTEROPPLYSNING;
    }
}
