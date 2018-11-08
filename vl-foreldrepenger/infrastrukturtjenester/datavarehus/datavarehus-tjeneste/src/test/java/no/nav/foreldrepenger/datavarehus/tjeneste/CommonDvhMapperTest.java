package no.nav.foreldrepenger.datavarehus.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
import no.nav.vedtak.felles.testutilities.Whitebox;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;

@SuppressWarnings("deprecation")
public class CommonDvhMapperTest {

    private static final String OPPRETTET_AV = "OpprettetAv";
    private static final LocalDateTime OPPRETTET_TIDSPUNKT = LocalDateTime.now().minusDays(1);
    private static final Object ENDRET_AV = "EndretAv";
    private static final LocalDateTime ENDRET_TIDSPUNKT = LocalDateTime.now();

    @Test
    public void skal_mappe_til_opprettet_av() {
        assertThat(CommonDvhMapper.finnEndretAvEllerOpprettetAv(byggNyBehandling())).isEqualTo(OPPRETTET_AV);
    }

    @Test
    public void skal_mappe_til_endret_av() {
        assertThat(CommonDvhMapper.finnEndretAvEllerOpprettetAv(byggOppdatertBehandling())).isEqualTo(ENDRET_AV);
    }
   

    private Behandling byggNyBehandling() {
        Behandling behandling = ScenarioMorSøkerEngangsstønad.forFødsel().lagMocked();
        Whitebox.setInternalState(behandling, "opprettetAv", OPPRETTET_AV);
        Whitebox.setInternalState(behandling, "opprettetTidspunkt", OPPRETTET_TIDSPUNKT);
        return behandling;
    }

    private Behandling byggOppdatertBehandling() {
        Behandling behandling = byggNyBehandling();
        Whitebox.setInternalState(behandling, "endretAv", ENDRET_AV);
        Whitebox.setInternalState(behandling, "endretTidspunkt", ENDRET_TIDSPUNKT);
        return behandling;
    }
}
