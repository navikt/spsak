package no.nav.foreldrepenger.datavarehus.tjeneste;

import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BRUKER_AKTØR_ID;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.OPPRETTET_TID;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.SAKSNUMMER;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.datavarehus.FagsakDvh;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class FagsakDvhMapperTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    private FagsakDvhMapper mapper = new FagsakDvhMapper();

    @Test
    public void skal_mappe_til_fagsak_dvh() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(BRUKER_AKTØR_ID, NavBrukerKjønn.KVINNE)
            .medSaksnummer(SAKSNUMMER);

        Behandling behandling = scenario.lagMocked();
        Whitebox.setInternalState(behandling.getFagsak(), "opprettetAv", "OpprettetAv");
        Whitebox.setInternalState(behandling.getFagsak(), "opprettetTidspunkt", OPPRETTET_TID);
        Fagsak fagsak = behandling.getFagsak();

        FagsakDvh dvh = mapper.map(fagsak);
        assertThat(dvh).isNotNull();
        assertThat(dvh.getFagsakId()).isEqualTo(fagsak.getId());
        assertThat(dvh.getBrukerAktørId()).isEqualTo(BRUKER_AKTØR_ID.getId());
        assertThat(dvh.getEndretAv()).isEqualTo("OpprettetAv");
        assertThat(dvh.getFagsakYtelse()).isEqualTo(FagsakYtelseType.FORELDREPENGER.getKode());
        assertThat(new Saksnummer(Long.toString(dvh.getSaksnummer()))).isEqualTo(SAKSNUMMER);
        assertThat(dvh.getOpprettetDato()).isEqualTo(OPPRETTET_TID.toLocalDate());
    }
}
