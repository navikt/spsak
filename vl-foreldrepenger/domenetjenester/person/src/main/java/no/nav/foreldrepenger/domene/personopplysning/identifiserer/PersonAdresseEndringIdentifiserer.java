package no.nav.foreldrepenger.domene.personopplysning.identifiserer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.diff.Node;
import no.nav.foreldrepenger.behandlingslager.diff.Pair;
import no.nav.foreldrepenger.domene.typer.AktørId;

@Dependent
public class PersonAdresseEndringIdentifiserer {

    @Inject
    PersonAdresseEndringIdentifiserer() {
    }

    public boolean erEndret(Behandling behandling, PersonopplysningGrunnlag grunnlag1, PersonopplysningGrunnlag grunnlag2) {
        return erAdresseEndret(behandling.getAktørId(), grunnlag1.getRegisterVersjon(), grunnlag2.getRegisterVersjon());
    }

    private boolean erAdresseEndret(AktørId aktørId, PersonInformasjon personopplysning1, PersonInformasjon personopplysning2) {
        RegisterdataDiffsjekker differ = new RegisterdataDiffsjekker(true);
        final List<PersonAdresse> adresser = personopplysning1.getAdresser()
            .stream()
            .filter(it -> it.getAktørId().equals(aktørId))
            .collect(Collectors.toList());
        final List<PersonAdresse> adresser1 = personopplysning2.getAdresser()
            .stream()
            .filter(it -> it.getAktørId().equals(aktørId))
            .collect(Collectors.toList());
        return differ.erForskjellPå(adresser, adresser1);
    }

    public boolean erEndretFørSkjæringstidspunkt(AktørId aktørId, PersonopplysningGrunnlag grunnlag1, PersonopplysningGrunnlag grunnlag2, LocalDate skjæringstidspunkt) {
        RegisterdataDiffsjekker differ = new RegisterdataDiffsjekker(true);
        final List<PersonAdresse> adresser = grunnlag1.getRegisterVersjon().getAdresser()
            .stream()
            .filter(it -> it.getAktørId().equals(aktørId))
            .collect(Collectors.toList());
        final List<PersonAdresse> adresser1 = grunnlag2.getRegisterVersjon().getAdresser()
            .stream()
            .filter(it -> it.getAktørId().equals(aktørId))
            .collect(Collectors.toList());

        final Map<Node, Pair> nodeEndringer = differ.finnForskjellerPå(adresser, adresser1);

        return nodeEndringer.keySet().stream()
            .map(Node::getObject)
            .filter(it -> it instanceof PersonAdresse)
            .anyMatch(adr -> ((PersonAdresse) adr).getPeriode().getFomDato().isBefore(skjæringstidspunkt));
    }
}
