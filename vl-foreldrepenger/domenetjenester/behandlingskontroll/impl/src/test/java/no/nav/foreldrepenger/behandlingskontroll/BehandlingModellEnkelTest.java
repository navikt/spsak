package no.nav.foreldrepenger.behandlingskontroll;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellImpl.TriFunction;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

public class BehandlingModellEnkelTest {

    private static final BehandlingStegType STEG_1 = BehandlingStegType.INNHENT_REGISTEROPP;
    private static final BehandlingStegType STEG_2 = BehandlingStegType.SØKERS_RELASJON_TIL_BARN;
    private static final BehandlingStegType STEG_3 = BehandlingStegType.BEREGN_YTELSE;

    private static final BehandlingType BEHANDLING_TYPE = BehandlingType.UDEFINERT;

    private static final FagsakYtelseType FAGSAK_YTELSE_TYPE = FagsakYtelseType.UDEFINERT;

    private final DummySteg dummyBehandlingSteg_1 = new DummySteg();
    private final DummySteg dummyBehandlingSteg_2 = new DummySteg();
    private final DummySteg dummyBehandlingSteg_3 = new DummySteg();

    private final TriFunction<BehandlingStegType, BehandlingType, FagsakYtelseType, BehandlingSteg> finnSteg = DummySteg.map(
        Arrays.asList(
            new TestStegKonfig(STEG_1, BEHANDLING_TYPE, FAGSAK_YTELSE_TYPE, dummyBehandlingSteg_1),
            new TestStegKonfig(STEG_2, BEHANDLING_TYPE, FAGSAK_YTELSE_TYPE, dummyBehandlingSteg_2),
            new TestStegKonfig(STEG_3, BEHANDLING_TYPE, FAGSAK_YTELSE_TYPE, dummyBehandlingSteg_3)
        ));

    @Test
    public void skal_bygge_behandlingskontroll_med_ett_steg() throws Exception {
        try (BehandlingModellImpl modell = new BehandlingModellImpl(BehandlingType.FØRSTEGANGSSØKNAD, FagsakYtelseType.ENGANGSTØNAD, finnSteg)) {
            modell.leggTil(STEG_1, BEHANDLING_TYPE, FAGSAK_YTELSE_TYPE);

            assertThat(modell.finnSteg(STEG_1)).isNotNull();
            assertThat(modell.finnSteg(STEG_1).getSteg()).isEqualTo(dummyBehandlingSteg_1);

            assertThat(modell.finnForrigeSteg(STEG_1)).isNull();
            assertThat(modell.finnNesteSteg(STEG_1)).isNull();
        }
    }

    @Test
    public void skal_bygge_behandlingskontroll_med_3_steg() throws Exception {
        // Arrange
        try (BehandlingModellImpl modell = new BehandlingModellImpl(BehandlingType.FØRSTEGANGSSØKNAD, FagsakYtelseType.ENGANGSTØNAD, finnSteg)) {
            modell.leggTil(STEG_1, BEHANDLING_TYPE, FAGSAK_YTELSE_TYPE);
            modell.leggTil(STEG_2, BEHANDLING_TYPE, FAGSAK_YTELSE_TYPE);
            modell.leggTil(STEG_3, BEHANDLING_TYPE, FAGSAK_YTELSE_TYPE);

            // Act - Assert
            BehandlingStegModell finnSteg2 = modell.finnSteg(STEG_2);
            assertThat(finnSteg2).isNotNull();
            assertThat(finnSteg2.getSteg()).isSameAs(dummyBehandlingSteg_2);

            assertThat(modell.finnForrigeSteg(STEG_1)).isNull();

            assertThat(modell.finnForrigeSteg(STEG_2)).isNotNull();
            assertThat(modell.finnForrigeSteg(STEG_2).getSteg()).isSameAs(dummyBehandlingSteg_1);

            assertThat(modell.finnNesteSteg(STEG_2)).isNotNull();
            assertThat(modell.finnNesteSteg(STEG_2).getSteg()).isSameAs(dummyBehandlingSteg_3);

            assertThat(modell.finnNesteSteg(STEG_3)).isNull();
        }
    }
}
