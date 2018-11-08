package no.nav.foreldrepenger.domene.personopplysning.identifiserer;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.domene.typer.AktørId;

@Dependent
public class SivilstandEndringIdentifiserer {

    @Inject
    SivilstandEndringIdentifiserer() {
        // For CDI
    }

    public boolean erEndret(AktørId søker, PersonopplysningGrunnlag grunnlagNy, PersonopplysningGrunnlag grunnlagOrginal) {
        List<SivilstandType> sivilstandtyperNy = grunnlagNy.getRegisterVersjon().getPersonopplysninger().stream()
            .filter(p -> p.getAktørId().equals(søker))
            .map(Personopplysning::getSivilstand)
            .collect(Collectors.toList());
        List<Personopplysning> personopplysningerOrginal = grunnlagOrginal.getRegisterVersjon().getPersonopplysninger().stream()
            .filter(p -> p.getAktørId().equals(søker))
            .collect(Collectors.toList());

        boolean sivilstandFraOrginalErLikNy = personopplysningerOrginal.stream().map(Personopplysning::getSivilstand).allMatch(sivilstandtyperNy::remove);
        boolean alleSivilstandIOrginalFunnetINy = sivilstandtyperNy.isEmpty();
        return !(sivilstandFraOrginalErLikNy && alleSivilstandIOrginalFunnetINy);
    }
}
