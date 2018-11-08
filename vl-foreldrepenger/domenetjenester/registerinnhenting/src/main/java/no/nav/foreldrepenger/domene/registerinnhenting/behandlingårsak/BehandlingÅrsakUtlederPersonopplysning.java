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
import no.nav.foreldrepenger.domene.familiehendelse.dødsfall.BarnetsDødsdatoEndringIdentifiserer;
import no.nav.foreldrepenger.domene.familiehendelse.dødsfall.ForelderErDødEndringIdentifiserer;
import no.nav.foreldrepenger.domene.familiehendelse.dødsfall.MorErDødEndringIdentifiserer;
import no.nav.foreldrepenger.domene.registerinnhenting.startpunkt.GrunnlagRef;

@ApplicationScoped
@GrunnlagRef("PersonInformasjon")
class BehandlingÅrsakUtlederPersonopplysning implements BehandlingÅrsakUtleder {
    private static final Logger log = LoggerFactory.getLogger(BehandlingÅrsakUtlederPersonopplysning.class);

    private PersonopplysningRepository personopplysningRepository;

    private MorErDødEndringIdentifiserer morErDødEndringIdentifiserer;
    private ForelderErDødEndringIdentifiserer forelderErDødEndringIdentifiserer;
    private BarnetsDødsdatoEndringIdentifiserer barnetsDødsdatoEndringIdentifiserer;

    BehandlingÅrsakUtlederPersonopplysning() {
        // For CDI
    }

    @Inject
    BehandlingÅrsakUtlederPersonopplysning(PersonopplysningRepository personopplysningRepository,
                                           MorErDødEndringIdentifiserer morErDødEndringIdentifiserer, ForelderErDødEndringIdentifiserer forelderErDødEndringIdentifiserer,
                                           BarnetsDødsdatoEndringIdentifiserer barnetsDødsdatoEndringIdentifiserer) {
        this.personopplysningRepository = personopplysningRepository;
        this.morErDødEndringIdentifiserer = morErDødEndringIdentifiserer;
        this.forelderErDødEndringIdentifiserer = forelderErDødEndringIdentifiserer;
        this.barnetsDødsdatoEndringIdentifiserer = barnetsDødsdatoEndringIdentifiserer;

    }

    @Override
    public Set<BehandlingÅrsakType> utledBehandlingÅrsaker(Behandling behandling1, Long grunnlagId1, Long grunnlagId2) {

        PersonopplysningGrunnlag grunnlag1 = personopplysningRepository.hentPersonopplysningerPåId(grunnlagId1);
        PersonopplysningGrunnlag grunnlag2 = personopplysningRepository.hentPersonopplysningerPåId(grunnlagId2);

        boolean morErDødEndret = morErDødEndringIdentifiserer.erEndret(behandling1, grunnlag2);
        boolean forelderErDødEndret = forelderErDødEndringIdentifiserer.erEndret(behandling1.getAktørId(), grunnlag1, grunnlag2);
        boolean barnetsDødsdatoEndret = barnetsDødsdatoEndringIdentifiserer.erEndret(behandling1, grunnlag2);

        if (morErDødEndret || forelderErDødEndret || barnetsDødsdatoEndret) {
            log.info("Setter behandlingårsak til opplysning om død, har endring morErDødEndret {} forelderErDødEndret {} barnetsDødsdatoEndret {}, grunnlagid1: {}, grunnlagid2: {}", morErDødEndret, forelderErDødEndret, barnetsDødsdatoEndret, grunnlagId1, grunnlagId2); //$NON-NLS-1
            return java.util.Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_DØD)));
        }
        log.info("Setter behandlingårsak til registeropplysning, grunnlagid1: {}, grunnlagid2: {}", grunnlagId1, grunnlagId2); //$NON-NLS-1
        return java.util.Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(BehandlingÅrsakType.RE_REGISTEROPPLYSNING)));
    }
}
