package no.nav.foreldrepenger.domene.registerinnhenting.behandlingårsak;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;

@Dependent
class BehandlingÅrsakUtlederAktørYtelse {

    @Inject
    public BehandlingÅrsakUtlederAktørYtelse() {
        //For CDI
    }

    BehandlingÅrsakType utledBehandlingÅrsak() {
        return BehandlingÅrsakType.RE_OPPLYSNINGER_OM_YTELSER;
    }
}
