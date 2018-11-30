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
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.diff.Node;
import no.nav.foreldrepenger.behandlingslager.diff.Pair;

@Dependent
public class StatsborgerskapEndringIdentifiserer {

    @Inject
    StatsborgerskapEndringIdentifiserer() {
        //For CDI
    }

    public boolean erEndret(PersonopplysningGrunnlag grunnlag1, PersonopplysningGrunnlag grunnlag2) {
        return erStatsborgerskapEndret(grunnlag1.getRegisterVersjon(), grunnlag2.getRegisterVersjon());
    }

    private boolean erStatsborgerskapEndret(PersonInformasjon personopplysningNy, PersonInformasjon personopplysningOrginal) {
        List<Statsborgerskap> statsborgerskapNy = new ArrayList<>(personopplysningNy.getStatsborgerskap());
        boolean statsborgerskapFraOrginalErLikNy = personopplysningOrginal.getStatsborgerskap().stream().allMatch(statsborgerskapNy::remove);
        boolean alleStatsborgerskapIOrginalFunnetINy = statsborgerskapNy.isEmpty();
        return !(statsborgerskapFraOrginalErLikNy && alleStatsborgerskapIOrginalFunnetINy);
    }

    public boolean erEndretFørSkjæringstidspunkt(PersonopplysningGrunnlag grunnlag1, PersonopplysningGrunnlag grunnlag2, LocalDate skjæringstidspunkt) {
        RegisterdataDiffsjekker differ = new RegisterdataDiffsjekker(true);
        final Map<Node, Pair> nodeEndringer = differ.finnForskjellerPå(grunnlag1.getRegisterVersjon().getStatsborgerskap(),
            grunnlag2.getRegisterVersjon().getStatsborgerskap());
        return nodeEndringer.keySet().stream()
            .map(Node::getObject)
            .filter(it -> it instanceof Statsborgerskap)
            .anyMatch(adr -> ((Statsborgerskap) adr).getPeriode().getFomDato().isBefore(skjæringstidspunkt));
    }
}
