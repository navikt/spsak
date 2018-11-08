package no.nav.foreldrepenger.domene.registerinnhenting.behandlingårsak;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.domene.registerinnhenting.startpunkt.GrunnlagRef;

@ApplicationScoped
@GrunnlagRef("YtelseFordelingAggregat")
class BehandlingÅrsakUtlederYtelseFordeling implements BehandlingÅrsakUtleder {

    private static final Logger log = LoggerFactory.getLogger(BehandlingÅrsakUtlederYtelseFordeling.class);
    @Inject
    public BehandlingÅrsakUtlederYtelseFordeling() {
        //For CDI
    }

    @Override
    public Set<BehandlingÅrsakType> utledBehandlingÅrsaker(Behandling behandling, Long grunnlagId1, Long grunnlagId2) {
        log.info("Setter behandlingårsak til udefinert, har endring i YtelseFordeling, grunnlagid1: {}, grunnlagid2: {}", grunnlagId1, grunnlagId2); //$NON-NLS-1
        return java.util.Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(BehandlingÅrsakType.UDEFINERT)));
    }
}
