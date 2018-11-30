package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import java.util.Optional;

public interface PersonopplysningGrunnlag {

    PersonInformasjon getRegisterVersjon();

    Optional<PersonInformasjon> getOverstyrtVersjon();

    Long getId();
}
