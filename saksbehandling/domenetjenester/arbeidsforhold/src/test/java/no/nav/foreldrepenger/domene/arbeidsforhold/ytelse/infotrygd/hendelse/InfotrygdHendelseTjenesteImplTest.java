package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingskontrollRepository;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.feed.felles.FeedDto;
import no.nav.foreldrepenger.kontrakter.feed.felles.FeedElement;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public class InfotrygdHendelseTjenesteImplTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Mock
    private Behandling behandling;

    @Mock
    private AktørId aktørId;

    @Mock
    private BehandlingStegTilstand tilstand;

    @Mock
    private OidcRestClient oidcRestClient;
    
    @Mock
    private BehandlingskontrollRepository behandlingskontrollRepository;

    private InfotrygdHendelseMapper mapper;
    private InfotrygdHendelseTjenesteImpl tjeneste;

    private static final String BASE_URL_FEED = "https://infotrygd-hendelser-api-t10.nais.preprod.local/infotrygd/hendelser";
    private URI startUri = URI.create(BASE_URL_FEED + "?fomDato=2018-05-14&aktorId=9000000001234");
    private URI endpoint = URI.create(BASE_URL_FEED);

    @Before
    public void setUp() {
        mapper = new InfotrygdHendelseMapper();
        tjeneste = new InfotrygdHendelseTjenesteImpl(endpoint, oidcRestClient, mapper, behandlingskontrollRepository);
    }


    @Test
    public void skal_lese_fra_infotrygd_feed() {

        //Arrange
        FeedDto feed = lagTestData();
        when(oidcRestClient.get(startUri, FeedDto.class)).thenReturn(feed);
        when(behandling.getAktørId()).thenReturn(aktørId);
        when(aktørId.getId()).thenReturn("9000000001234");
        
        when(behandlingskontrollRepository.getBehandlingStegTilstandHistorikk(Mockito.anyLong())).thenReturn(List.of(tilstand));
        when(tilstand.getStegType()).thenReturn(BehandlingStegType.FATTE_VEDTAK);
        when(tilstand.getOpprettetTidspunkt()).thenReturn(LocalDateTime.of(2018, 5, 14, 9, 30));

        //Act
        List<InfotrygdHendelse> infotrygdHendelse = tjeneste.hentHendelsesListFraInfotrygdFeed(behandling);

        //Assert
        assertThat(infotrygdHendelse).hasSize(2);
    }

    private FeedDto lagTestData() {
        return new FeedDto.Builder()
            .medTittel("enhetstest")
            .medElementer(Arrays.asList(
                lagElement(1, new InfotrygdAnnulert()),
                lagElement(2, new InfotrygdInnvilget())))
            .build();
    }

    private FeedElement lagElement(long sequence, Object melding) {
        String type;
        if (melding instanceof InfotrygdAnnulert) {
            type = "ANNULERT_v1";
        } else {
            type = "INNVILGET_v1";
        }
        return new FeedElement.Builder()
            .medSekvensId(sequence)
            .medType(type)
            .medInnhold(lagInnhold(melding))
            .build();
    }

    private Innhold lagInnhold(Object melding) {
        Innhold innhold = (Innhold) melding;
        innhold.setAktoerId("9000000001234");
        innhold.setFom(LocalDate.now());
        innhold.setIdentDato(konverterFomDatoTilString(LocalDate.now()));
        innhold.setTypeYtelse("Type");

        return innhold;

    }

    private String konverterFomDatoTilString(LocalDate dato) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dato.format(formatter);
    }
}
