package no.nav.foreldrepenger.domene.mottak.hendelser.impl.oversetter;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.hendelser.ForretningshendelseType;
import no.nav.foreldrepenger.domene.familiehendelse.fødsel.FødselForretningshendelse;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.ForretningshendelseDto;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.FødselHendelse;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.integrasjon.rest.JsonMapper;

public class FødselForretningshendelseOversetterTest {

    private static final List<String> AKTØR_ID_LISTE = singletonList(new AktørId("1").getId());
    private static final LocalDate FØDSELSDATO = LocalDate.now();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private FødselForretningshendelseOversetter fødselForretningshendelseOversetter;

    @Before
    public void before() {
        fødselForretningshendelseOversetter = new FødselForretningshendelseOversetter();
    }

    @Test
    public void skal_oversette_fra_json_til_FødselForretningshendelse() {
        // Arrange
        FødselHendelse fødselHendelse = new FødselHendelse(AKTØR_ID_LISTE, FØDSELSDATO);
        String json = JsonMapper.toJson(fødselHendelse);
        ForretningshendelseDto forretningshendelse = new ForretningshendelseDto("FØDSEL", json);

        // Act
        FødselForretningshendelse resultat = fødselForretningshendelseOversetter.oversett(forretningshendelse);

        // Assert
        assertThat(resultat.getForretningshendelseType()).isEqualTo(ForretningshendelseType.FØDSEL);
        assertThat(resultat.getAktørIdListe().stream().map(AktørId::getId).collect(Collectors.toList())).isEqualTo(AKTØR_ID_LISTE);
        assertThat(resultat.getFødselsdato()).isEqualTo(FØDSELSDATO);
    }
}
