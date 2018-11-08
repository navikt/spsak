package no.nav.foreldrepenger.domene.personopplysning.identifiserer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.diff.Node;
import no.nav.foreldrepenger.behandlingslager.diff.Pair;

@Dependent
public class PersonstatusEndringIdentifiserer {

    @Inject
    PersonstatusEndringIdentifiserer() {
        //For CDI
    }

    public boolean erEndret(PersonopplysningGrunnlag grunnlagNy, PersonopplysningGrunnlag grunnlagOrginal) {
        return erPersonstatusEndret(grunnlagNy.getRegisterVersjon(), grunnlagOrginal.getRegisterVersjon());
    }

    public boolean erEndretFørSkjæringstidspunkt(PersonopplysningGrunnlag grunnlagNy, PersonopplysningGrunnlag grunnlagOrginal, LocalDate skjæringstidspunkt) {
        RegisterdataDiffsjekker differ = new RegisterdataDiffsjekker(true);
        final Map<Node, Pair> nodeEndringer = differ.finnForskjellerPå(grunnlagNy.getRegisterVersjon().getPersonstatus(),
            grunnlagOrginal.getRegisterVersjon().getPersonstatus());
        return nodeEndringer.keySet().stream()
            .map(Node::getObject)
            .filter(it -> it instanceof Personstatus)
            .anyMatch(adr -> ((Personstatus) adr).getPeriode().getFomDato().isBefore(skjæringstidspunkt));
    }

    private boolean erPersonstatusEndret(PersonInformasjon personopplysningNy, PersonInformasjon personopplysningOrginal) {
        List<Personstatus> personstatuserNy = new ArrayList<>(personopplysningNy.getPersonstatus());
        boolean personstatusFraOrginalErLikNy = personopplysningOrginal.getPersonstatus().stream().allMatch(personstatuserNy::remove);
        boolean allePersonstatuserIOrginalFunnetINy = personstatuserNy.isEmpty();
        return !(personstatusFraOrginalErLikNy && allePersonstatuserIOrginalFunnetINy);
    }
}
