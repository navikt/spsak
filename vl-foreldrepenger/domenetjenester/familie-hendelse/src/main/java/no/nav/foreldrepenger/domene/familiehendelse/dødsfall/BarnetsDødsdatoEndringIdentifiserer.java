package no.nav.foreldrepenger.domene.familiehendelse.dødsfall;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;

@Dependent
public class BarnetsDødsdatoEndringIdentifiserer {

    private FamilieHendelseTjeneste familiehendelseTjeneste;

    @Inject
    BarnetsDødsdatoEndringIdentifiserer(FamilieHendelseTjeneste familiehendelseTjeneste) {
        this.familiehendelseTjeneste = familiehendelseTjeneste;
    }

    public boolean erEndret(Behandling nyBehandling, PersonopplysningGrunnlag orginaltGrunnlag) {

        AktørId søker = nyBehandling.getAktørId();

        // Finn barna søkt stønad for i ny behandling. Og sammenlign mot matchende barn på orginalt grunnlag.
        List<Personopplysning> barnSøktStønadForNy = familiehendelseTjeneste.finnBarnSøktStønadFor(nyBehandling);
        List<Personopplysning> alleBarnIOrginalgrunnlagRelasjon = orginaltGrunnlag.getRegisterVersjon() != null ?
            getBarn(søker, orginaltGrunnlag) : emptyList();

        final Set<AktørId> barnSøktStønadForNyAktørIder = barnSøktStønadForNy.stream().map(Personopplysning::getAktørId).collect(Collectors.toSet());
        List<LocalDate> forventetDødsdatoBarnSøktStønadForNy = barnSøktStønadForNy.stream()
            .map(Personopplysning::getDødsdato)
            .filter(Objects::nonNull) // Filtrer bort barn som ikke er død
            .collect(Collectors.toList());

        // Filtrer barn fra orginal relasjon så vi får de barna som matcher de det er søkt stønad for.
        List<LocalDate> orginaleDødsdatoerFiltrertMotBarnINyBehandling = alleBarnIOrginalgrunnlagRelasjon.stream()
            .filter(p -> barnSøktStønadForNyAktørIder.contains(p.getAktørId()))
            .map(Personopplysning::getDødsdato)
            .filter(Objects::nonNull) // Filtrer bort barn som ikke er død
            .collect(toList());

        // Sammenlign de to listene med dødsdatoer(orginal grunnlag vs ny behandling). Finnes alle dødsdatoene og i begge listene er de like.
        boolean datoFraOrginalErLikeFraNy = orginaleDødsdatoerFiltrertMotBarnINyBehandling.stream()
            .allMatch(forventetDødsdatoBarnSøktStønadForNy::remove);
        boolean alleDødsdatoeriOrginalFunnetINy = forventetDødsdatoBarnSøktStønadForNy.isEmpty();
        return !(datoFraOrginalErLikeFraNy && alleDødsdatoeriOrginalFunnetINy);
    }

    private List<Personopplysning> getBarn(AktørId søker, PersonopplysningGrunnlag grunnlag) {
        return grunnlag.getRegisterVersjon().getRelasjoner().stream()
            .filter(rel -> rel.getAktørId().equals(søker) && rel.getRelasjonsrolle().equals(RelasjonsRolleType.BARN))
            .map(relSøkerBarn -> grunnlag.getRegisterVersjon().getPersonopplysninger().stream()
                .filter(person -> person.getAktørId().equals(relSøkerBarn.getTilAktørId())).findAny().orElse(null))
            .filter(Objects::nonNull)
            .collect(toList());
    }
}
