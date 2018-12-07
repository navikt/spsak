package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

public interface TestScenario<S> extends
        BehandlingOgFagsakTestScenario<S>,
        SykefraværTestScenario<S>,
        MedlemskapTestScenario<S> {

    S leggTilScenario(TestScenarioTillegg testScenarioTillegg);

}
