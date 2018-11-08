package no.nav.foreldrepenger.domene.familiehendelse.dødsfall;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;

@Dependent
public class MorErDødEndringIdentifiserer {

    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    private FamilieHendelseTjeneste familiehendelseTjeneste;

    @Inject
    MorErDødEndringIdentifiserer(BasisPersonopplysningTjeneste personopplysningTjeneste, FamilieHendelseTjeneste familiehendelseTjeneste) {
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.familiehendelseTjeneste = familiehendelseTjeneste;
    }

    public boolean erEndret(Behandling nyBehandling, PersonopplysningGrunnlag orginaltGrunnlag) {
        //1 - Finn personopplysning aggregat(som har fine hjelpemetoder) til ny/oppdatert behandling
        Optional<PersonopplysningerAggregat> personopplysningerAggregatForNyBehandling = personopplysningTjeneste.hentPersonopplysningerHvisEksisterer(nyBehandling);

        //2 - finn barn søkt stønad for til ny/oppdatert behandling.
        List<Personopplysning> barnSøktStønadFor = familiehendelseTjeneste.finnBarnSøktStønadFor(nyBehandling);
        Optional<Personopplysning> barnOptional = barnSøktStønadFor.stream().findFirst();

        //3 - Finn Personopplysninger om mor for ny/oppdatert behandling.
        Optional<Personopplysning> personopplysningMorFraNyBehandling = hentMorPersonopplysning(barnOptional, personopplysningerAggregatForNyBehandling);

        //4 - Finn personopplysning om mor i orginal relasjon.
        Optional<Personopplysning> personopplysningMorOrginal = orginaltGrunnlag.getRegisterVersjon() != null ?
            finnPersonFraGrunnlag(personopplysningMorFraNyBehandling.orElse(null), orginaltGrunnlag) : Optional.empty();

        //5 - Finn dødsdato til mor
        LocalDate morDødsdatoPåOrginaltGrunnlag = personopplysningMorOrginal.map(Personopplysning::getDødsdato).orElse(null);
        LocalDate morDødsdatoFraNyBehandling = personopplysningMorFraNyBehandling.map(Personopplysning::getDødsdato).orElse(null);

        return !Objects.equals(morDødsdatoPåOrginaltGrunnlag, morDødsdatoFraNyBehandling);
    }

    private Optional<Personopplysning> finnPersonFraGrunnlag(Personopplysning person, PersonopplysningGrunnlag grunnlag) {
        if ((person == null) || (grunnlag == null)) {
            return Optional.empty();
        }

        return grunnlag.getRegisterVersjon().getPersonopplysninger().stream()
            .filter(p -> person.getAktørId().equals(p.getAktørId()))
            .findFirst();
    }

    private Optional<Personopplysning> hentMorPersonopplysning(Optional<Personopplysning> barnOptional, Optional<PersonopplysningerAggregat> personopplysningerAggregatOptional) {
        if (barnOptional.isPresent() && personopplysningerAggregatOptional.isPresent()) {
            PersonopplysningerAggregat personopplysningerAggregat = personopplysningerAggregatOptional.get();
            return personopplysningerAggregat.getTilPersonerFor(barnOptional.get().getAktørId(), RelasjonsRolleType.MORA).stream().findAny();
        }
        return Optional.empty();
    }
}
