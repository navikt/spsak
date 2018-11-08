package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;

public class TpsTestSamling {

    private final TpsPerson bruker;
    private final TpsPerson medforelder;
    private final List<TpsPerson> barn;

    public TpsTestSamling(TpsPerson bruker, TpsPerson medforelder, List<TpsPerson> barn) {
        this.bruker = bruker;
        this.medforelder = medforelder;
        this.barn = barn;
    }

    public TpsPerson getBruker() {
        return bruker;
    }

    public Optional<TpsPerson> getMedforelder() {
        return Optional.ofNullable(medforelder);
    }

    public List<TpsPerson> getBarn() {
        return barn;
    }
}
