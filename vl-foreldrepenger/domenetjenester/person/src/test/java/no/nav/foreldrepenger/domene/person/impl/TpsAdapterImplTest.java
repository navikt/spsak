package no.nav.foreldrepenger.domene.person.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.GeografiskTilknytning;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingsgrunnlagKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.geografisk.SpråkKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.domene.person.impl.TpsAdapterImpl;
import no.nav.foreldrepenger.domene.person.impl.TpsAdresseOversetter;
import no.nav.foreldrepenger.domene.person.impl.TpsOversetter;
import no.nav.foreldrepenger.domene.person.impl.TpsTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kommune;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.aktør.klient.DetFinnesFlereAktørerMedSammePersonIdentException;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

public class TpsAdapterImplTest {

    private TpsAdapterImpl tpsAdapterImpl;

    private AktørConsumerMedCache aktørConsumerMock = Mockito.mock(AktørConsumerMedCache.class);
    private PersonConsumer personProxyServiceMock = Mockito.mock(PersonConsumer.class);

    TpsTjenesteImpl tpsTjeneste = Mockito.mock(TpsTjenesteImpl.class);
    private final AktørId aktørId = new AktørId("1337");
    private final PersonIdent fnr = new PersonIdent("31018143212");

    @Before
    public void setup() {
        TpsAdresseOversetter tpsAdresseOversetter = new TpsAdresseOversetter(lagMockNavBrukerKodeverkRepository(), null);
        TpsOversetter tpsOversetter = new TpsOversetter(
            lagMockNavBrukerKodeverkRepository(), lagMockBehandlingsgrunnlagKodeverkRepository(), lagMockSpråkKodeverkRepository(), tpsAdresseOversetter);
        tpsAdapterImpl = new TpsAdapterImpl(aktørConsumerMock, personProxyServiceMock, tpsOversetter);
    }

    private NavBrukerKodeverkRepository lagMockNavBrukerKodeverkRepository() {
        NavBrukerKodeverkRepository mockNavBrukerKodeverkRepository = mock(NavBrukerKodeverkRepository.class);
        Optional<RelasjonsRolleType> optionalBrukerRolle = Optional.empty();
        when(mockNavBrukerKodeverkRepository.finnBrukerRolle(any(String.class))).thenReturn(optionalBrukerRolle);
        when(mockNavBrukerKodeverkRepository.finnBrukerKjønn(any(String.class))).thenReturn(NavBrukerKjønn.KVINNE);
        return mockNavBrukerKodeverkRepository;
    }

    private BehandlingsgrunnlagKodeverkRepository lagMockBehandlingsgrunnlagKodeverkRepository() {
        BehandlingsgrunnlagKodeverkRepository mockBehandlingsgrunnlagKodeverkRepository = mock(BehandlingsgrunnlagKodeverkRepository.class);
        when(mockBehandlingsgrunnlagKodeverkRepository.finnHøyestRangertRegion(Collections.singletonList(anyString()))).thenReturn(Region.NORDEN);
        return mockBehandlingsgrunnlagKodeverkRepository;
    }

    private SpråkKodeverkRepository lagMockSpråkKodeverkRepository() {
        SpråkKodeverkRepository språkRepo = Mockito.mock(SpråkKodeverkRepository.class);
        when(språkRepo.finnSpråkMedKodeverkEiersKode("NN")).thenReturn(Optional.of(Språkkode.nn));
        when(språkRepo.finnSpråkMedKodeverkEiersKode("NB")).thenReturn(Optional.of(Språkkode.nb));
        return språkRepo;
    }

    @Test
    public void test_hentAktørIdForPersonIdent_normal() throws Exception {
        Mockito.when(aktørConsumerMock.hentAktørIdForPersonIdent("125343412")).thenReturn(Optional.of("1234"));
        Optional<AktørId> optAktørId = tpsAdapterImpl.hentAktørIdForPersonIdent(new PersonIdent("125343412"));
        assertThat(optAktørId.get()).isEqualTo(new AktørId("1234"));
    }

    @Test
    public void test_hentAktørIdForPersonIdent_ikkeFunnet() throws Exception {
        Mockito.when(aktørConsumerMock.hentAktørIdForPersonIdent("125343412")).thenReturn(Optional.empty());

        Optional<AktørId> optAktørId = tpsAdapterImpl.hentAktørIdForPersonIdent(new PersonIdent("125343412"));
        assertThat(optAktørId).isEmpty();
    }

    @Test
    public void skal_returnere_tom_når_det_finnes_flere_enn_en_aktør_på_samme_ident___kan_skje_ved_dødfødsler() throws Exception {
        DetFinnesFlereAktørerMedSammePersonIdentException exception = new DetFinnesFlereAktørerMedSammePersonIdentException(Mockito.mock(Feil.class));
        String fnr2 = "125343412";
        Mockito.when(aktørConsumerMock.hentAktørIdForPersonIdent(fnr2))
            .thenThrow(exception);

        Optional<AktørId> optAktørId = tpsAdapterImpl.hentAktørIdForPersonIdent(new PersonIdent(fnr2));
        assertThat(optAktørId).isEmpty();
    }

    @Test
    public void test_hentIdentForAktørId_normal() throws Exception {
        Mockito.when(aktørConsumerMock.hentPersonIdentForAktørId("1")).thenReturn(Optional.of("1337"));
        Optional<PersonIdent> optIdent = tpsAdapterImpl.hentIdentForAktørId(new AktørId("1"));
        assertThat(optIdent.get()).isEqualTo(new PersonIdent("1337"));
    }

    @Test
    public void test_hentIdentForAktørId_ikkeFunnet() throws Exception {
        Mockito.when(aktørConsumerMock.hentPersonIdentForAktørId("1")).thenReturn(Optional.empty());
        Optional<PersonIdent> optIdent = tpsAdapterImpl.hentIdentForAktørId(new AktørId("1"));
        assertThat(optIdent).isNotPresent();
    }

    @Test
    public void test_hentKjerneinformasjon_normal() throws Exception {
        AktørId aktørId = new AktørId("1337");
        PersonIdent fnr = new PersonIdent("31018143212");
        String navn = "John Doe";
        LocalDate fødselsdato = LocalDate.of(1343, 12, 12);
        NavBrukerKjønn kjønn = NavBrukerKjønn.KVINNE;

        HentPersonResponse response = new HentPersonResponse();
        Bruker person = new Bruker();
        response.setPerson(person);
        Mockito.when(personProxyServiceMock.hentPersonResponse(Mockito.any())).thenReturn(response);

        TpsOversetter tpsOversetterMock = Mockito.mock(TpsOversetter.class);
        Personinfo personinfo0 = new Personinfo.Builder()
            .medPersonIdent(fnr)
            .medNavn(navn)
            .medFødselsdato(fødselsdato)
            .medNavBrukerKjønn(kjønn)
            .medAktørId(aktørId)
            .build();

        Mockito.when(tpsOversetterMock.tilBrukerInfo(Mockito.any(AktørId.class), eq(person))).thenReturn(personinfo0);
        tpsAdapterImpl = new TpsAdapterImpl(aktørConsumerMock, personProxyServiceMock, tpsOversetterMock);

        Personinfo personinfo = tpsAdapterImpl.hentKjerneinformasjon(fnr, aktørId);
        assertNotNull(personinfo);
        assertThat(personinfo.getAktørId()).isEqualTo(aktørId);
        assertThat(personinfo.getPersonIdent()).isEqualTo(fnr);
        assertThat(personinfo.getNavn()).isEqualTo(navn);
        assertThat(personinfo.getFødselsdato()).isEqualTo(fødselsdato);
    }

    @Test
    public void test_hentGegrafiskTilknytning_vha_fnr() throws Exception {
        final String fnr = "31018143212";
        final String diskresjonskode = "KLIE";
        final String kommune = "0219";

        HentGeografiskTilknytningResponse response = mockHentGeografiskTilknytningResponse(kommune, diskresjonskode);
        Mockito.when(personProxyServiceMock.hentGeografiskTilknytning(Mockito.any())).thenReturn(response);

        GeografiskTilknytning tilknytning = tpsAdapterImpl.hentGeografiskTilknytning(new PersonIdent(fnr));
        assertNotNull(tilknytning);
        assertThat(tilknytning.getDiskresjonskode()).isEqualTo(diskresjonskode);
        assertThat(tilknytning.getTilknytning()).isEqualTo(kommune);
    }

    private HentGeografiskTilknytningResponse mockHentGeografiskTilknytningResponse(String kommune, String diskresjonskode) {
        HentGeografiskTilknytningResponse response = new HentGeografiskTilknytningResponse();
        Kommune k = new Kommune();
        k.setGeografiskTilknytning(kommune);
        response.setGeografiskTilknytning(k);

        Diskresjonskoder dk = new Diskresjonskoder();
        dk.setValue(diskresjonskode);
        response.setDiskresjonskode(dk);

        return response;
    }

    @Test(expected = TekniskException.class)
    public void skal_få_exception_når_tjenesten_ikke_kan_finne_personen() throws Exception {
        Mockito.when(personProxyServiceMock.hentPersonResponse(Mockito.any()))
            .thenThrow(new HentPersonPersonIkkeFunnet(null, null));

        tpsAdapterImpl.hentKjerneinformasjon(fnr, aktørId);
    }

    @Test(expected = ManglerTilgangException.class)
    public void skal_få_exception_når_tjenesten_ikke_kan_aksesseres_pga_manglende_tilgang() throws Exception {
        when(personProxyServiceMock.hentPersonResponse(any(HentPersonRequest.class)))
            .thenThrow(new HentPersonSikkerhetsbegrensning(null, null));

        tpsAdapterImpl.hentKjerneinformasjon(fnr, aktørId);
    }

    @Test(expected = TekniskException.class)
    public void skal_få_exception_når_tjenesten_ikke_kan_finne_geografisk_tilknytning_for_personen() throws Exception {
        Mockito.when(personProxyServiceMock.hentGeografiskTilknytning(Mockito.any()))
            .thenThrow(new HentGeografiskTilknytningPersonIkkeFunnet(null, null));

        tpsAdapterImpl.hentGeografiskTilknytning(fnr);
    }

    @Test(expected = ManglerTilgangException.class)
    public void skal_få_exception_ved_henting_av_geografisk_tilknytning_når_tjenesten_ikke_kan_aksesseres_pga_manglende_tilgang() throws Throwable {
        when(personProxyServiceMock.hentGeografiskTilknytning(Mockito.any()))
            .thenThrow(new HentGeografiskTilknytningSikkerhetsbegrensing(null, null));

        tpsAdapterImpl.hentGeografiskTilknytning(fnr);
    }

    @Test
    public void test_hentAdresseinformasjon_normal() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        HentPersonResponse response = new HentPersonResponse();
        Bruker person = new Bruker();
        response.setPerson(person);

        ArgumentCaptor<HentPersonRequest> captor = ArgumentCaptor.forClass(HentPersonRequest.class);
        when(personProxyServiceMock.hentPersonResponse(captor.capture())).thenReturn(response);

        final String addresse = "Veien 17";

        TpsOversetter tpsOversetterMock = Mockito.mock(TpsOversetter.class);
        Adresseinfo.Builder builder = new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE,
            new PersonIdent("31018143212"),
            "Tjoms",
            PersonstatusType.BOSA);
        Adresseinfo adresseinfoExpected = builder.medAdresselinje1(addresse).build();

        when(tpsOversetterMock.tilAdresseInfo(eq(person))).thenReturn(adresseinfoExpected);
        tpsAdapterImpl = new TpsAdapterImpl(aktørConsumerMock, personProxyServiceMock, tpsOversetterMock);

        Adresseinfo adresseinfoActual = tpsAdapterImpl.hentAdresseinformasjon(fnr);

        assertThat(adresseinfoActual).isNotNull();
        assertThat(adresseinfoActual).isEqualTo(adresseinfoExpected);
        assertThat(adresseinfoActual.getAdresselinje1()).isEqualTo(adresseinfoExpected.getAdresselinje1());
    }

    @Test(expected = TekniskException.class)
    public void test_hentAdresseinformasjon_personIkkeFunnet() throws Exception {
        when(personProxyServiceMock.hentPersonResponse(any())).thenThrow(new HentPersonPersonIkkeFunnet(null, null));

        tpsAdapterImpl.hentAdresseinformasjon(fnr);
    }

    @Test(expected = ManglerTilgangException.class)
    public void test_hentAdresseinformasjon_manglende_tilgang() throws Exception {
        when(personProxyServiceMock.hentPersonResponse(any())).thenThrow(new HentPersonSikkerhetsbegrensning(null, null));

        tpsAdapterImpl.hentAdresseinformasjon(fnr);
    }
}
