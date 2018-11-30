package no.nav.foreldrepenger.behandlingslager.behandling.virksomhet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class OrganisasjonsNummerValidatorTest {

    @Test
    public void erGyldig() {
        assertThat(OrganisasjonsNummerValidator.erGyldig("910909088")).isTrue();
        assertThat(OrganisasjonsNummerValidator.erGyldig("974760673")).isTrue();
        assertThat(OrganisasjonsNummerValidator.erGyldig("123123341")).isFalse();
        assertThat(OrganisasjonsNummerValidator.erGyldig("1")).isFalse();
    }
}
