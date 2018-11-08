package no.nav.foreldrepenger.domene.familiehendelse.dødsfall;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.HarAktørId;

@Dependent
public class ForelderErDødEndringIdentifiserer {

    @Inject
    ForelderErDødEndringIdentifiserer() {
        // For CDI
    }

    public boolean erEndret(AktørId søker, PersonopplysningGrunnlag grunnlag1, PersonopplysningGrunnlag grunnlag2) {

        List<Personopplysning> personopplysninger1 = grunnlag1.getRegisterVersjon() != null ?
            grunnlag1.getRegisterVersjon().getPersonopplysninger() : emptyList();
        List<Personopplysning> personopplysninger2 = grunnlag2.getRegisterVersjon().getPersonopplysninger();

        Optional<LocalDate> søkersDødsdato1 = finnPerson(søker, personopplysninger1).map(Personopplysning::getDødsdato);
        Optional<LocalDate> søkersDødsdato2 = finnPerson(søker, personopplysninger2).map(Personopplysning::getDødsdato);

        Optional<LocalDate> annenPartsDødsdato1 = finnAnnenPart(grunnlag1, personopplysninger1).map(Personopplysning::getDødsdato);
        Optional<LocalDate> annenPartsDødsdato2 = finnAnnenPart(grunnlag2, personopplysninger2).map(Personopplysning::getDødsdato);

        boolean erSøkersDødsdatoEndret = !Objects.equals(søkersDødsdato1, søkersDødsdato2);
        boolean erAnnenPartsDødsdatoEndret = !Objects.equals(annenPartsDødsdato1, annenPartsDødsdato2);

        return erSøkersDødsdatoEndret || erAnnenPartsDødsdatoEndret;
    }

    private Optional<Personopplysning> finnAnnenPart(PersonopplysningGrunnlag grunnlag, List<Personopplysning> personopplysninger) {
        return grunnlag.getOppgittAnnenPart()
            .map(HarAktørId::getAktørId)
            .filter(Objects::nonNull)
            .flatMap(anneAktørId -> finnPerson(anneAktørId, personopplysninger));
    }

    private Optional<Personopplysning> finnPerson(AktørId aktørId, List<Personopplysning> personopplysninger1) {
        return personopplysninger1.stream()
            .filter(p -> p.getAktørId().equals(aktørId))
            .findFirst();
    }

}
