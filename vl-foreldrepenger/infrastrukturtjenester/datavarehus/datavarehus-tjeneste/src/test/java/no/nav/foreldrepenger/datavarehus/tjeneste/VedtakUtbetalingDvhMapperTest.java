package no.nav.foreldrepenger.datavarehus.tjeneste;

import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.ANSVARLIG_BESLUTTER;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.ANSVARLIG_SAKSBEHANDLER;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BEHANDLENDE_ENHET;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.IVERKSETTING_STATUS;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.VEDTAK_DATO;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.datavarehus.VedtakUtbetalingDvh;

public class VedtakUtbetalingDvhMapperTest {

    public static final String XML = "xml";
    VedtakUtbetalingDvhMapper vedtakUtbetalingDvhMapper;
    private BehandlingRepositoryProvider behandlingRepositoryProvider;

    @Before
    public void setup(){
        vedtakUtbetalingDvhMapper = new VedtakUtbetalingDvhMapper();
    }

    @Test
    public void skal_mappe_til_VedtakUtbetalingDvh(){
        Behandling behandling = byggBehandling();
        BehandlingVedtak vedtak = BehandlingVedtak.builder()
            .medAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER)
            .medIverksettingStatus(IVERKSETTING_STATUS)
            .medVedtaksdato(VEDTAK_DATO)
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .build();


        VedtakUtbetalingDvh mapped = vedtakUtbetalingDvhMapper.map(XML, behandling, vedtak);
        assertThat(mapped.getBehandlingId()).isEqualTo(behandling.getId());
        assertThat(mapped.getXmlClob()).isEqualTo(XML);
        assertThat(mapped.getVedtakDato()).isEqualTo(VEDTAK_DATO);
        assertThat(mapped.getFagsakId()).isEqualTo(behandling.getFagsakId());
        assertThat(mapped.getFagsakType()).isEqualTo(behandling.getFagsak().getYtelseType().getKode());
    }


    private Behandling byggBehandling() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        Behandling behandling = scenario.lagMocked();
        behandlingRepositoryProvider = scenario.mockBehandlingRepositoryProvider();
        behandling.setAnsvarligBeslutter(ANSVARLIG_BESLUTTER);
        behandling.setBehandlendeEnhet(new OrganisasjonsEnhet(BEHANDLENDE_ENHET, null));
        return behandling;
    }
}
