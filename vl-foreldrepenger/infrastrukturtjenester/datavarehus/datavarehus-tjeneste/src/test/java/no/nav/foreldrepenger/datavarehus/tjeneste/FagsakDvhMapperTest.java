package no.nav.foreldrepenger.datavarehus.tjeneste;

import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.ANNEN_PART_AKTØR_ID;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BRUKER_AKTØR_ID;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.OPPRETTET_TID;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.SAKSNUMMER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.vedtak.felles.testutilities.Whitebox;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.datavarehus.FagsakDvh;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

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
        scenario.medSøknadAnnenPart().medAktørId(ANNEN_PART_AKTØR_ID);

        Behandling behandling = scenario.lagMocked();
        Whitebox.setInternalState(behandling.getFagsak(), "opprettetAv", "OpprettetAv");
        Whitebox.setInternalState(behandling.getFagsak(), "opprettetTidspunkt", OPPRETTET_TID);
        Fagsak fagsak = behandling.getFagsak();

        FagsakDvh dvh = mapper.map(fagsak, Optional.of(ANNEN_PART_AKTØR_ID), Optional.of(FamilieHendelseType.FØDSEL));
        assertThat(dvh).isNotNull();
        assertThat(dvh.getFagsakId()).isEqualTo(fagsak.getId());
        assertThat(dvh.getBrukerAktørId()).isEqualTo(BRUKER_AKTØR_ID.getId());
        assertThat(dvh.getEndretAv()).isEqualTo("OpprettetAv");
        assertThat(dvh.getFagsakAarsak()).isEqualTo(FamilieHendelseType.FØDSEL.getKode());
        assertThat(dvh.getFagsakYtelse()).isEqualTo(FagsakYtelseType.ENGANGSTØNAD.getKode());
        assertThat(new Saksnummer(Long.toString(dvh.getSaksnummer()))).isEqualTo(SAKSNUMMER);
        assertThat(dvh.getOpprettetDato()).isEqualTo(OPPRETTET_TID.toLocalDate());
        assertThat(dvh.getEpsAktørId()).isEqualTo(ANNEN_PART_AKTØR_ID.getId());
    }
}
