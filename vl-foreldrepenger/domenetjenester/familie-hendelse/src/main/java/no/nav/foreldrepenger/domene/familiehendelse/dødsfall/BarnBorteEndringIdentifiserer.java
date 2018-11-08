package no.nav.foreldrepenger.domene.familiehendelse.dødsfall;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.typer.AktørId;

@Dependent
public class BarnBorteEndringIdentifiserer {

    private PersonopplysningRepository personopplysningRepository;

    BarnBorteEndringIdentifiserer() {
        // For CDI
    }

    @Inject
    BarnBorteEndringIdentifiserer(BehandlingRepositoryProvider provider) {
        this.personopplysningRepository = provider.getPersonopplysningRepository();
    }

    public boolean erEndret(Behandling nyBehandling) {
        Behandling origBehandling = nyBehandling.getOriginalBehandling().orElse(null);
        if (origBehandling == null) {
            // Støtter bare deteksjon av endring i antall barn dersom det skjer mellom to behandlinger
            // (krevende å sjekke hvilket PO-grunnlag som er nyest basert kun på grunnlag-id)
            return false;
        }

        AktørId søker = nyBehandling.getAktørId();

        List<Personopplysning> origBarna = personopplysningRepository.hentPersonopplysningerHvisEksisterer(origBehandling)
            .map(origGrunnlag ->  getBarn(søker, origGrunnlag))
            .orElse(emptyList());
        List<Personopplysning> nyeBarna = personopplysningRepository.hentPersonopplysningerHvisEksisterer(nyBehandling)
            .map(origGrunnlag ->  getBarn(søker, origGrunnlag))
            .orElse(emptyList());

        // Sjekk om noen av de registrerte barna på orig grunnlag har forsvunnet på nytt grunnlag
        return origBarna.stream()
            .anyMatch(origBarn -> nyeBarna.stream()
                .noneMatch(nyttBarn -> Objects.equals(nyttBarn.getAktørId(), origBarn.getAktørId())));
    }

    private List<Personopplysning> getBarn(AktørId søker, PersonopplysningGrunnlag grunnlag) {
        if (grunnlag.getRegisterVersjon() == null) {
            return emptyList();
        }

        return grunnlag.getRegisterVersjon().getRelasjoner().stream()
            .filter(rel -> rel.getAktørId().equals(søker) && rel.getRelasjonsrolle().equals(RelasjonsRolleType.BARN))
            .map(relSøkerBarn -> grunnlag.getRegisterVersjon().getPersonopplysninger().stream()
                .filter(person -> person.getAktørId().equals(relSøkerBarn.getTilAktørId()))
                .findAny()
                .orElse(null))
            .filter(Objects::nonNull)
            .collect(toList());
    }
}
