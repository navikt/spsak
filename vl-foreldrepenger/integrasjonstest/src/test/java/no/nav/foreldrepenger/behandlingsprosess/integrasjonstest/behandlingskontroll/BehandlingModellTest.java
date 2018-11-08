package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.behandlingskontroll;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BehandlingModellTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private BehandlingModellRepository behandlingModellRepository;

    @Test
    public void skal_sjekke_alle_behandlingsteg_for_Engangsstønad_Førstegangssøknad_er_korrekt_definert() {
        BehandlingModell modell = behandlingModellRepository.getModell(BehandlingType.FØRSTEGANGSSØKNAD, FagsakYtelseType.ENGANGSTØNAD);
        modell.hvertSteg().forEach(e -> {
            Assertions.assertThat(e.getBehandlingStegType()).isNotNull();
            Assertions.assertThat(e.getSteg()).isNotNull();
        });
    }

    @Test
    public void skal_sjekke_alle_behandlingsteg_for_Foreldrepenger_Førstegangssøknad_er_korrekt_definert() {
        BehandlingModell modell = behandlingModellRepository.getModell(BehandlingType.FØRSTEGANGSSØKNAD, FagsakYtelseType.FORELDREPENGER);
        modell.hvertSteg().forEach(e -> {
            Assertions.assertThat(e.getBehandlingStegType()).isNotNull();
            Assertions.assertThat(e.getSteg()).isNotNull();
        });
    }
}
