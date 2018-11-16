package no.nav.foreldrepenger.web.app.tjenester.fagsak;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavPersoninfoBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.JsonUtil;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.app.FagsakApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.app.FagsakApplikasjonTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.dto.FagsakDto;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.dto.SokefeltDto;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class FagsakRestTjenesteTest {

    private FagsakRestTjeneste tjeneste;

    private FagsakApplikasjonTjeneste applikasjonTjeneste;
    private TpsTjeneste tpsTjeneste;
    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;
    private RevurderingTjeneste fagsakRevurderingTjeneste;
    private RevurderingTjenesteProvider fagsakRevurderingTjenesteProvider;

    @Before
    public void oppsett() {
        tpsTjeneste = mock(TpsTjeneste.class);
        fagsakRepository = mock(FagsakRepository.class);
        behandlingRepository = mock(BehandlingRepository.class);
        fagsakRevurderingTjeneste = mock(RevurderingTjeneste.class);
        fagsakRevurderingTjenesteProvider = mock(RevurderingTjenesteProvider.class);
        when(fagsakRevurderingTjenesteProvider.finnRevurderingTjenesteFor(any())).thenReturn(fagsakRevurderingTjeneste);
        when(fagsakRevurderingTjeneste.kanRevurderingOpprettes(any())).thenReturn(false);

        BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste = mock(BehandlingskontrollAsynkTjeneste.class);

        BehandlingRepositoryProvider repositoryProvider = mock(BehandlingRepositoryProvider.class);
        when(repositoryProvider.getFagsakRepository()).thenReturn(fagsakRepository);
        when(repositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepository);

        applikasjonTjeneste = new FagsakApplikasjonTjenesteImpl(repositoryProvider, behandlingskontrollAsynkTjeneste, tpsTjeneste);

        tjeneste = new FagsakRestTjeneste(applikasjonTjeneste, fagsakRevurderingTjenesteProvider);
    }

    @Test
    public void skal_returnere_tom_liste_dersom_tomt_view() throws IOException {
        Collection<FagsakDto> fagsakDtos = tjeneste.søkFagsaker(new SokefeltDto("ugyldig_søkestreng"));
        assertThat(fagsakDtos).hasSize(0);
    }

    @Test
    public void skal_generere_json_output_korrekt_formatert() throws IOException {
        // Arrange
        PersonIdent fnr = new PersonIdent("12345678901");
        AktørId aktørId = new AktørId("1");
        Personinfo personinfo = new NavPersoninfoBuilder().medAktørId(aktørId).medDiskresjonskode("6").medPersonstatusType(PersonstatusType.DØD).build();
        NavBruker navBruker = new NavBrukerBuilder().medPersonInfo(personinfo).build();

        when(tpsTjeneste.hentBrukerForFnr(fnr)).thenReturn(Optional.of(personinfo));

        Fagsak fagsak = FagsakBuilder.nyEngangstønad(RelasjonsRolleType.MORA)
            .medBruker(navBruker)
            .medSaksnummer(new Saksnummer("123"))
            .build();
        Whitebox.setInternalState(fagsak, "id", -1L);
        when(fagsakRepository.hentForBruker(aktørId)).thenReturn(Collections.singletonList(fagsak));

        LocalDate fødselsdato = LocalDate.of(2017, JANUARY, 1);
        when(behandlingRepository.hentSisteBehandlingForFagsakId(anyLong())).thenReturn(Optional.of(Behandling.forFørstegangssøknad(fagsak).build()));

        // Act
        Collection<FagsakDto> fagsakDtos = tjeneste.søkFagsaker(new SokefeltDto(fnr.getIdent()));

        // Assert
        JsonUtil jsonUtil = new JsonUtil(FagsakDto.class);
        String json = jsonUtil.toJsonString(fagsakDtos);
        List<FagsakDto> fagsakDtoFraJson = jsonUtil.fromJsonList(json);
        assertThat(fagsakDtoFraJson).isEqualTo(fagsakDtos);

        int alder = Period.between(personinfo.getFødselsdato(), LocalDate.now()).getYears();
        String expectedJson = "[" +
            "{\"saksnummer\":123,\"sakstype\":{\"kode\":\"FP\",\"navn\":\"Foreldrepenger\",\"kodeverk\":\"FAGSAK_YTELSE\"},\"status\":{\"kode\":\"OPPR\",\"navn\":\"Opprettet\",\"kodeverk\":\"FAGSAK_STATUS\"},\"barnFodt\":\"2017-01-01\",\"person\":{\"erDod\":true,\"navn\":\"Anne-Berit Hjartdal\",\"alder\":"
            + alder
            + ",\"personnummer\":\"13107221234\",\"erKvinne\":true,\"personstatusType\":{\"kode\":\"DØD\",\"navn\":null,\"kodeverk\":\"PERSONSTATUS_TYPE\"},\"diskresjonskode\":\"6\",\"dodsdato\":null},\"opprettet\":null,\"endret\":null,\"antallBarn\":0,\"kanRevurderingOpprettes\":false,\"skalBehandlesAvInfotrygd\":false}"
            +
            "]";
        assertThat(json).isEqualTo(expectedJson);
    }

}
