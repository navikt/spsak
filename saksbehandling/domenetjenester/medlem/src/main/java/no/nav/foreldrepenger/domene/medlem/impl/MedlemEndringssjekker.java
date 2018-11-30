package no.nav.foreldrepenger.domene.medlem.impl;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;

public abstract class MedlemEndringssjekker {

    abstract RegisterdataDiffsjekker opprettNyDiffer();

    public boolean erEndret(Optional<MedlemskapAggregat> medlemskap, List<RegistrertMedlemskapPerioder> list1, List<RegistrertMedlemskapPerioder> list2) {
        RegisterdataDiffsjekker differ = opprettNyDiffer();
        return !medlemskap.isPresent() || differ.erForskjellPå(list1, list2);
    }

    public boolean erEndring(Optional<MedlemskapAggregat> nyttMedlemskap, Optional<MedlemskapAggregat> eksisterendeMedlemskap) {

        if (!eksisterendeMedlemskap.isPresent() && !nyttMedlemskap.isPresent()) {
            return false;
        }
        if (eksisterendeMedlemskap.isPresent() && !nyttMedlemskap.isPresent()) {
            return true;
        }
        if (!eksisterendeMedlemskap.isPresent() && nyttMedlemskap.isPresent()) { // NOSONAR - "redundant" her er false pos.
            return true;
        }

        RegisterdataDiffsjekker differ = opprettNyDiffer();
        return !differ.erForskjellPå(nyttMedlemskap.get().getRegistrertMedlemskapPerioder(), eksisterendeMedlemskap.get().getRegistrertMedlemskapPerioder());
    }

    public boolean erEndring(RegistrertMedlemskapPerioder perioder1, RegistrertMedlemskapPerioder perioder2) {
        RegisterdataDiffsjekker differ = opprettNyDiffer();
        return differ.erForskjellPå(perioder1, perioder2);
    }
}
