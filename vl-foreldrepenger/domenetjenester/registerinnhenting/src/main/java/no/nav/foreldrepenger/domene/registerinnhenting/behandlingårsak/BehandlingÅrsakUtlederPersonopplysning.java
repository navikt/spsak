package no.nav.foreldrepenger.domene.registerinnhenting.behandlingårsak;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.domene.registerinnhenting.startpunkt.GrunnlagRef;
import no.nav.foreldrepenger.domene.typer.AktørId;

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
        AktørId brukerAktørId = behandling1.getNavBruker().getAktørId();

        PersonopplysningGrunnlag pers1 = personopplysningRepository.hentPersonopplysningerPåId(grunnlagId1);
        PersonopplysningGrunnlag pers2 = personopplysningRepository.hentPersonopplysningerPåId(grunnlagId2);

        LocalDate dato1 = getDødsdato(brukerAktørId, pers1);
        LocalDate dato2 = getDødsdato(brukerAktørId, pers2);
        
        boolean søkerErDødEndret = !Objects.equals(dato1, dato2);
        if (søkerErDødEndret) {
            log.info("Setter behandlingårsak til opplysning om død, har endring søkerErDødEndret {} , grunnlagid1: {}, grunnlagid2: {}", søkerErDødEndret,
                grunnlagId1, grunnlagId2); // $NON-NLS-1
            return java.util.Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_DØD)));
        }
        log.info("Setter behandlingårsak til registeropplysning, grunnlagid1: {}, grunnlagid2: {}", grunnlagId1, grunnlagId2); // $NON-NLS-1
        return java.util.Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(BehandlingÅrsakType.RE_REGISTEROPPLYSNING)));
    }

    private LocalDate getDødsdato(AktørId brukerAktørId, PersonopplysningGrunnlag pers1) {
        PersonInformasjon persinfo1 = pers1.getOverstyrtVersjon().orElse(pers1.getRegisterVersjon());
        LocalDate dato1 = persinfo1.getPersonopplysninger().stream().filter(po -> Objects.equals(po.getAktørId(), brukerAktørId))
            .findFirst().map(Personopplysning::getDødsdato).orElse(null);
        return dato1;
    }
}
