package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonRelasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.domene.typer.AktørId;

class EndringFødselIdentifiserer {

    private EndringFødselIdentifiserer() {
        // Sonar vil ha denne for å skjule implisitt public konstruktor
    }

    static boolean omfatterEndringKunNyFødsel(AktørId søkerAktørId, PersonopplysningGrunnlag poNy, PersonopplysningGrunnlag poEksisterende) {
        final PersonInformasjon nyRegisterVersjon = poNy.getRegisterVersjon();

        Set<AktørId> barn = finnAlleBarn(søkerAktørId, poNy, poEksisterende);

        Predicate<AktørId> medBarn = ai -> barn.contains(ai);
        Predicate<AktørId> utenBarn = medBarn.negate();
        
        Predicate<PersonRelasjon> søkerTilBarn = pr -> søkersBarn(søkerAktørId, pr);

        boolean endringIRelasjonerForBarn = erEndringIRelasjoner(poEksisterende.getRegisterVersjon(), nyRegisterVersjon, søkerTilBarn, medBarn);
        boolean endringIRelasjonerAndreEnnBarn = erEndringIRelasjoner(poEksisterende.getRegisterVersjon(), nyRegisterVersjon,
            pr -> !(barn.contains(pr.getAktørId()) && Objects.equals(søkerAktørId, pr.getTilAktørId()))
            , utenBarn);

        boolean endringeIPersonopplysninger = erEndringIPersonopplysninger(poEksisterende.getRegisterVersjon(), nyRegisterVersjon, utenBarn);
        boolean endringeIAdresser = erEndringIAdresser(poEksisterende.getRegisterVersjon(), nyRegisterVersjon, utenBarn);
        boolean endringeIPersonstatus = erEndringIPersonstatus(poEksisterende.getRegisterVersjon(), nyRegisterVersjon, utenBarn);
        boolean endringIStatsborgerskap = erEndringIStatsborgerskap(poEksisterende.getRegisterVersjon(), nyRegisterVersjon, utenBarn);

        return (endringIRelasjonerForBarn && (!endringIRelasjonerAndreEnnBarn
            && !endringeIPersonopplysninger
            && !endringeIAdresser
            && !endringeIPersonstatus
            && !endringIStatsborgerskap));
    }

    private static Set<AktørId> finnAlleBarn(AktørId søkerAktørId, PersonopplysningGrunnlag poNy, PersonopplysningGrunnlag poEksisterende) {
        Set<AktørId> barn = new HashSet<>();
        barn.addAll(poEksisterende.getRegisterVersjon().getRelasjoner().stream().filter(pr -> søkersBarn(søkerAktørId, pr)).map(PersonRelasjon::getTilAktørId)
            .collect(Collectors.toSet()));
        barn.addAll(poNy.getRegisterVersjon().getRelasjoner().stream().filter(pr -> søkersBarn(søkerAktørId, pr)).map(PersonRelasjon::getTilAktørId)
            .collect(Collectors.toSet()));
        return barn;
    }

    private static boolean erEndringIStatsborgerskap(PersonInformasjon poEksisterende, PersonInformasjon nyRegisterVersjon, Predicate<AktørId> inkluder) {
        Map<AktørId, Set<Statsborgerskap>> ny = inkluder(nyRegisterVersjon.getStatsborgerskap(), Statsborgerskap::getAktørId, inkluder);
        Map<AktørId, Set<Statsborgerskap>> eksisterende = inkluder(poEksisterende.getStatsborgerskap(), Statsborgerskap::getAktørId, inkluder);
        boolean endringer = !Objects.equals(ny, eksisterende);
        return endringer;
    }

    private static boolean erEndringIPersonstatus(PersonInformasjon poEksisterende, PersonInformasjon nyRegisterVersjon, Predicate<AktørId> inkluder) {
        Map<AktørId, Set<Personstatus>> ny = inkluder(nyRegisterVersjon.getPersonstatus(), Personstatus::getAktørId, inkluder);
        Map<AktørId, Set<Personstatus>> eksisterende = inkluder(poEksisterende.getPersonstatus(), Personstatus::getAktørId, inkluder);
        boolean endringer = !Objects.equals(ny, eksisterende);
        return endringer;
    }

    private static boolean erEndringIAdresser(PersonInformasjon poEksisterende, PersonInformasjon nyRegisterVersjon, Predicate<AktørId> inkluder) {
        Map<AktørId, Set<PersonAdresse>> ny = inkluder(nyRegisterVersjon.getAdresser(), PersonAdresse::getAktørId, inkluder);
        Map<AktørId, Set<PersonAdresse>> eksisterende = inkluder(poEksisterende.getAdresser(), PersonAdresse::getAktørId, inkluder);
        boolean endringer = !Objects.equals(ny, eksisterende);
        return endringer;
    }

    private static boolean erEndringIPersonopplysninger(PersonInformasjon poEksisterende, PersonInformasjon nyRegisterVersjon, Predicate<AktørId> inkluder) {
        Map<AktørId, Set<Personopplysning>> ny = inkluder(nyRegisterVersjon.getPersonopplysninger(), Personopplysning::getAktørId, inkluder);
        Map<AktørId, Set<Personopplysning>> eksisterende = inkluder(poEksisterende.getPersonopplysninger(), Personopplysning::getAktørId, inkluder);
        boolean endringer = !Objects.equals(ny, eksisterende);
        return endringer;
    }

    private static boolean erEndringIRelasjoner(PersonInformasjon poEksisterende, final PersonInformasjon nyRegisterVersjon,
                                                Predicate<PersonRelasjon> inkluderPr, Predicate<AktørId> inkluder) {
        // sjekk andre enn barn
        Map<AktørId, Set<PersonRelasjon>> ny = nyRegisterVersjon.getRelasjoner().stream()
            .filter(inkluderPr)
            .collect(Collectors.groupingBy(PersonRelasjon::getTilAktørId, Collectors.toSet()))
            .entrySet()
            .stream()
            .filter(e -> inkluder.test(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<AktørId, Set<PersonRelasjon>> eksisterende = poEksisterende.getRelasjoner().stream()
            .filter(inkluderPr)
            .collect(Collectors.groupingBy(PersonRelasjon::getTilAktørId, Collectors.toSet()))
            .entrySet()
            .stream()
            .filter(e1 -> inkluder.test(e1.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        boolean endringIRelasjoner = !Objects.equals(ny, eksisterende);
        return endringIRelasjoner;
    }

    private static <V> Map<AktørId, Set<V>> inkluder(List<V> data, Function<V, AktørId> classifier, Predicate<AktørId> inkluder) {
        return data.stream()
            .collect(Collectors.groupingBy(classifier, Collectors.toSet()))
            .entrySet()
            .stream()
            .filter(e -> inkluder.test(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }

    private static boolean søkersBarn(AktørId søkerId, PersonRelasjon relasjon) {
        return Objects.equals(søkerId, relasjon.getAktørId()) && Objects.equals(relasjon.getRelasjonsrolle(), RelasjonsRolleType.BARN);
    }

}
