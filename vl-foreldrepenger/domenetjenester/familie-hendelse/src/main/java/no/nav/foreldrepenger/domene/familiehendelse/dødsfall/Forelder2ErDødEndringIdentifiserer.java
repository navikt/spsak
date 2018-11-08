package no.nav.foreldrepenger.domene.familiehendelse.dødsfall;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;

@Dependent
public class Forelder2ErDødEndringIdentifiserer {
    private FamilieHendelseTjeneste familiehendelseTjeneste;

    @Inject
    Forelder2ErDødEndringIdentifiserer(FamilieHendelseTjeneste familiehendelseTjeneste) {
        this.familiehendelseTjeneste = familiehendelseTjeneste;
    }

    public boolean erEndret(Behandling nyBehandling, PersonopplysningGrunnlag nyGrunnlag, PersonopplysningGrunnlag orginaltGrunnlag) {
        //1 - finn barn søkt stønad for til ny/oppdatert behandling.
        List<Personopplysning> barnSøktStønadFor = familiehendelseTjeneste.finnBarnSøktStønadFor(nyBehandling);
        Optional<Personopplysning> barnOptional = barnSøktStønadFor.stream().findFirst();//Henter ut første. Skal kun bruke relasjonen til å finne forelder2(og alle barn i søknadne har samme forelder2)

        //2 - Finn Personopplysninger om den andre forelderen for ny/oppdatert behandling.
        List<Personopplysning> personopplysningForeldre2FraNyBehandling = hentPersonopplysningAnnenForelder(nyBehandling.getAktørId(), barnOptional, nyGrunnlag);
        List<AktørId> aktørIdForeldre2FraNyBehandling = personopplysningForeldre2FraNyBehandling.stream().map(Personopplysning::getAktørId).collect(toList());

        //3 - Finn personopplysning om foreldre(minus søker) i orginal relasjon. Liste over foreldre med fra trekk for forelderen som søker, gir forelder 2.
        List<Personopplysning> personopplysningForeldre2Orginal = orginaltGrunnlag.getRegisterVersjon() != null ?
            finnPersonerFraGrunnlag(aktørIdForeldre2FraNyBehandling, orginaltGrunnlag) : emptyList();

        //4 - forelder2 søkt stønad for fra orginalt grunnlag og fra ny/oppdatert behandling bør matche.
        Set<LocalDate> forventetdeDødsdatoerForeldre2FraNyBehandling = personopplysningForeldre2FraNyBehandling.stream().map(Personopplysning::getDødsdato).collect(toSet());

        //Sjekk om dødsdatoene til foreldre2 matcher - Forventer å finne alle dødsdatoene og heller ikke ha flere i den ene listen.
        boolean datoFraOrginalErLikeFraNy = personopplysningForeldre2Orginal.stream().map(Personopplysning::getDødsdato).allMatch(forventetdeDødsdatoerForeldre2FraNyBehandling::remove);
        boolean alleDødsdatoeriOrginalFunnetINy = forventetdeDødsdatoerForeldre2FraNyBehandling.isEmpty() || forventetdeDødsdatoerForeldre2FraNyBehandling.contains(null);
        return !(datoFraOrginalErLikeFraNy && alleDødsdatoeriOrginalFunnetINy);
    }

    private List<Personopplysning> finnPersonerFraGrunnlag(List<AktørId> personer, PersonopplysningGrunnlag grunnlag) {
        if ((personer == null) || (grunnlag == null)) {
            return new ArrayList<>();
        }

        return grunnlag.getRegisterVersjon().getPersonopplysninger().stream()
            .filter(p -> personer.contains((p.getAktørId())))
            .collect(toList());
    }

    private List<Personopplysning> hentPersonopplysningAnnenForelder(AktørId søker, Optional<Personopplysning> barnOptional, PersonopplysningGrunnlag grunnlag) {
        if (barnOptional.isPresent()) {
            AktørId barn = barnOptional.get().getAktørId();
            return grunnlag.getRegisterVersjon().getRelasjoner().stream()
                .filter(rel -> rel.getAktørId().equals(barn) && RelasjonsRolleType.erRegistrertForeldre(rel.getRelasjonsrolle()))//Alle relasjoner fra barn til forelder
                .filter(relAlleForeldre -> !relAlleForeldre.getTilAktørId().equals(søker))//Filtrer bort søker. Her er vi kun interessert i forelder 2
                .flatMap(relForeldreMinusSøker -> grunnlag.getRegisterVersjon().getPersonopplysninger().stream().filter(person -> person.getAktørId().equals(relForeldreMinusSøker.getTilAktørId()))) //Personopplysninger til alle andre foreldre enn søker.
                .collect(toList());
        }

        return new ArrayList<>();
    }
}
