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
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.domene.registerinnhenting.startpunkt.GrunnlagRef;

@ApplicationScoped
@GrunnlagRef("PersonInformasjon")
class BehandlingÅrsakUtlederPersonopplysning implements BehandlingÅrsakUtleder {
    private static final Logger log = LoggerFactory.getLogger(BehandlingÅrsakUtlederPersonopplysning.class);

    private PersonopplysningRepository personopplysningRepository;

    BehandlingÅrsakUtlederPersonopplysning() {
        // For CDI
    }

    @Inject
    BehandlingÅrsakUtlederPersonopplysning(PersonopplysningRepository personopplysningRepository) {
        this.personopplysningRepository = personopplysningRepository;
    }

    @Override
    public Set<BehandlingÅrsakType> utledBehandlingÅrsaker(Behandling behandling1, Long grunnlagId1, Long grunnlagId2) {

        PersonopplysningGrunnlag grunnlag1 = personopplysningRepository.hentPersonopplysningerPåId(grunnlagId1);
        PersonopplysningGrunnlag grunnlag2 = personopplysningRepository.hentPersonopplysningerPåId(grunnlagId2);

        // FIXME SP : Tilpasse etter behov?
        boolean morErDødEndret = false;
        boolean forelderErDødEndret = false;
        boolean barnetsDødsdatoEndret = false;

        if (morErDødEndret || forelderErDødEndret || barnetsDødsdatoEndret) {
            log.info("Setter behandlingårsak til opplysning om død, har endring morErDødEndret {} forelderErDødEndret {} barnetsDødsdatoEndret {}, grunnlagid1: {}, grunnlagid2: {}", morErDødEndret, forelderErDødEndret, barnetsDødsdatoEndret, grunnlagId1, grunnlagId2); //$NON-NLS-1
            return java.util.Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_DØD)));
        }
        log.info("Setter behandlingårsak til registeropplysning, grunnlagid1: {}, grunnlagid2: {}", grunnlagId1, grunnlagId2); //$NON-NLS-1
        return java.util.Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(BehandlingÅrsakType.RE_REGISTEROPPLYSNING)));
    }
}
