package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import java.util.Optional;

public interface PersonopplysningGrunnlag {

    PersonInformasjon getGjeldendeVersjon();

    PersonInformasjon getRegisterVersjon();

    Optional<PersonInformasjon> getOverstyrtVersjon();

    Optional<OppgittAnnenPart> getOppgittAnnenPart();

    Long getId();
}
