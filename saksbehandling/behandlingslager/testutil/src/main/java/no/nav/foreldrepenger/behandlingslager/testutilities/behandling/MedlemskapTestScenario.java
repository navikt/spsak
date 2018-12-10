package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytningEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapBuilder;

public interface MedlemskapTestScenario<S> {

    MedlemskapRepository mockMedlemskapRepository();

    S medOppgittTilknytning(OppgittTilknytningEntitet.Builder builder);

    OppgittTilknytningEntitet.Builder medOppgittTilknytning();

    VurdertMedlemskapBuilder medMedlemskap();

    void leggTilMedlemskapPeriode(RegistrertMedlemskapPerioder medlemskapPeriode);

}