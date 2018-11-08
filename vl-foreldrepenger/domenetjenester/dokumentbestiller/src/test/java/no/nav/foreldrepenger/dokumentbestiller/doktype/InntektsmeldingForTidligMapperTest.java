package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.integrasjon.dokument.inntektsmeldingfortidlig.BehandlingsTypeKode;

public class InntektsmeldingForTidligMapperTest {

    @Test
    public void skalMappeTilFoerstegangssoknadHvisFørstegangssøknad() {
        BehandlingsTypeKode xmlType = InntektsmeldingForTidligMapper
            .mapToXmlBehandlingsType(BehandlingType.FØRSTEGANGSSØKNAD.getKode());

        assertThat(xmlType).isEqualTo(BehandlingsTypeKode.FOERSTEGANGSBEHANDLING);
    }


    @Test
    public void skalMappeTilRevurderingHvisRevurdering() {
        BehandlingsTypeKode xmlType = InntektsmeldingForTidligMapper
            .mapToXmlBehandlingsType(BehandlingType.REVURDERING.getKode());
        assertThat(xmlType).isEqualTo(BehandlingsTypeKode.REVURDERING);
    }

    @Test
    public void skalKasteExceptionHvisInnsyn() {
        assertThatThrownBy(() -> InntektsmeldingForTidligMapper
            .mapToXmlBehandlingsType(BehandlingType.INNSYN.getKode())).isNotNull();
    }

    @Test
    public void skalKasteExceptionHvisRandom() {
        assertThatThrownBy(() -> InntektsmeldingForTidligMapper
            .mapToXmlBehandlingsType(UUID.randomUUID().toString())).isNotNull();
    }

}
