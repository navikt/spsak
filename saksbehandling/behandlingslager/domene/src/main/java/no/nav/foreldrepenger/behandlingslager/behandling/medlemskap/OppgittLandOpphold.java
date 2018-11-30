package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;

public interface OppgittLandOpphold {

    Landkoder getLand();

    LocalDate getPeriodeFom();

    LocalDate getPeriodeTom();

    boolean isTidligereOpphold();

}
