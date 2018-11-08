package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.BehandlingsTypeKode;

public class InnhentopplysningerBrevMapperUtilTest {

    @Test
    public void skalMappeTilFoerstegangssoknadHvisFørstegangssøknad() {
        BehandlingsTypeKode xmlType = InnhentopplysningerBrevMapperUtil
            .mapToXmlBehandlingsType(BehandlingType.FØRSTEGANGSSØKNAD.getKode());
        assertThat(xmlType).isEqualTo(BehandlingsTypeKode.FOERSTEGANGSBEHANDLING);
    }

    @Test
    public void skalMappeTilKlageHvisKlage() {
        BehandlingsTypeKode xmlType = InnhentopplysningerBrevMapperUtil
            .mapToXmlBehandlingsType(BehandlingType.KLAGE.getKode());
        assertThat(xmlType).isEqualTo(BehandlingsTypeKode.KLAGE);
    }

    @Test
    public void skalMappeTilRevurderingHvisRevurdering() {
        BehandlingsTypeKode xmlType = InnhentopplysningerBrevMapperUtil
            .mapToXmlBehandlingsType(BehandlingType.REVURDERING.getKode());
        assertThat(xmlType).isEqualTo(BehandlingsTypeKode.REVURDERING);
    }

    @Test
    public void skalKasteExceptionHvisInnsyn() {
        assertThatThrownBy(() -> InnhentopplysningerBrevMapperUtil
            .mapToXmlBehandlingsType(BehandlingType.INNSYN.getKode())).isNotNull();
    }

    @Test
    public void skalKasteExceptionHvisRandom() {
        assertThatThrownBy(() -> InnhentopplysningerBrevMapperUtil
            .mapToXmlBehandlingsType(UUID.randomUUID().toString())).isNotNull();
    }
}
