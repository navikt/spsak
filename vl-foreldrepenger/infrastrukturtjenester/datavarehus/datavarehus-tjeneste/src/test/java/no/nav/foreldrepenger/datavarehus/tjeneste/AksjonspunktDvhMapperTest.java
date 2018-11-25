package no.nav.foreldrepenger.datavarehus.tjeneste;

import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.AKSJONSPUNKT_DEF;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.ANSVARLIG_BESLUTTER;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.ANSVARLIG_SAKSBEHANDLER;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BEHANDLENDE_ENHET;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BEHANDLING_STEG_ID;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BEHANDLING_STEG_TYPE;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.OPPRETTET_AV;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.OPPRETTET_TID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.datavarehus.AksjonspunktDvh;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class AksjonspunktDvhMapperTest {


    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    private AksjonspunktDvhMapper mapper = new AksjonspunktDvhMapper();

    @Test
    public void skal_mappe_til_aksjonspunkt_dvh() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.leggTilAksjonspunkt(AKSJONSPUNKT_DEF, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);

        Behandling behandling = scenario.lagMocked();

        behandling.setAnsvarligBeslutter(ANSVARLIG_BESLUTTER);
        behandling.setAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER);
        behandling.setBehandlendeEnhet(new OrganisasjonsEnhet(BEHANDLENDE_ENHET, null));

        BehandlingStegTilstand behandlingStegTilstand = new BehandlingStegTilstand(behandling, BEHANDLING_STEG_TYPE);
        Whitebox.setInternalState(behandlingStegTilstand, "id", BEHANDLING_STEG_ID);
        Whitebox.setInternalState(behandling, "behandlingStegTilstander", Collections.singletonList(behandlingStegTilstand));

        Whitebox.setInternalState(behandling.getAksjonspunktMedDefinisjonOptional(AKSJONSPUNKT_DEF).get(), OPPRETTET_AV, OPPRETTET_AV);
        Whitebox.setInternalState(behandling.getAksjonspunktMedDefinisjonOptional(AKSJONSPUNKT_DEF).get(), "opprettetTidspunkt", OPPRETTET_TID);
        Aksjonspunkt aksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(AKSJONSPUNKT_DEF).get();

        AksjonspunktDvh dvh = mapper.map(aksjonspunkt, behandling, byggBehandlingStegTilstand(behandling), true);

        assertThat(dvh).isNotNull();
        assertThat(dvh.getAksjonspunktDef()).isEqualTo(AKSJONSPUNKT_DEF.getKode());
        assertThat(dvh.getAksjonspunktId()).isEqualTo(aksjonspunkt.getId());
        assertThat(dvh.getAksjonspunktStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET.getKode());
        assertThat(dvh.getAnsvarligBeslutter()).isEqualTo(ANSVARLIG_BESLUTTER);
        assertThat(dvh.getAnsvarligSaksbehandler()).isEqualTo(ANSVARLIG_SAKSBEHANDLER);
        assertThat(dvh.getBehandlendeEnhetKode()).isEqualTo(BEHANDLENDE_ENHET);
        assertThat(dvh.getBehandlingId()).isEqualTo(behandling.getId());
        assertThat(dvh.getBehandlingStegId()).isEqualTo(BEHANDLING_STEG_ID);
        assertThat(dvh.getEndretAv()).isEqualTo(OPPRETTET_AV);
    }

    @Test
    public void skal_mappe_behandlingsteg_null() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.leggTilAksjonspunkt(AKSJONSPUNKT_DEF, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);

        Behandling behandling = scenario.lagMocked();

        behandling.setAnsvarligBeslutter(ANSVARLIG_BESLUTTER);
        behandling.setAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER);
        behandling.setBehandlendeEnhet(new OrganisasjonsEnhet(BEHANDLENDE_ENHET, null));

        BehandlingStegTilstand behandlingStegTilstand = new BehandlingStegTilstand(behandling, BEHANDLING_STEG_TYPE);
        Whitebox.setInternalState(behandlingStegTilstand, "id", BEHANDLING_STEG_ID);
        Whitebox.setInternalState(behandling, "behandlingStegTilstander", Collections.singletonList(behandlingStegTilstand));

        Whitebox.setInternalState(behandling.getAksjonspunktMedDefinisjonOptional(AKSJONSPUNKT_DEF).get(), OPPRETTET_AV, OPPRETTET_AV);
        Whitebox.setInternalState(behandling.getAksjonspunktMedDefinisjonOptional(AKSJONSPUNKT_DEF).get(), "opprettetTidspunkt", OPPRETTET_TID);
        Aksjonspunkt aksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(AKSJONSPUNKT_DEF).get();

        AksjonspunktDvh dvh = mapper.map(aksjonspunkt, behandling, Optional.empty(), true);

        assertThat(dvh).isNotNull();

        assertThat(dvh.getBehandlingStegId()).isNull();
    }

    private Optional<BehandlingStegTilstand> byggBehandlingStegTilstand(Behandling behandling) {
        BehandlingStegTilstand behandlingStegTilstand = new BehandlingStegTilstand(behandling, BEHANDLING_STEG_TYPE);
        Whitebox.setInternalState(behandlingStegTilstand, "id", BEHANDLING_STEG_ID);
        return Optional.of(behandlingStegTilstand);
    }
}
