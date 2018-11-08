package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.InnhenteOpplysningerDokument.FLETTEFELT_SOKERS_NAVN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentAdresse;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.FlettefeltJsonObjectMapper;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.FeriePeriodeDto;
import no.nav.foreldrepenger.dokumentbestiller.brev.DokumentToBrevDataMapper;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.BehandlingstypeType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.PersonstatusKode;

public class DokumentTypeMapperTest {
    private List<DokumentTypeData> dokumentTypeDataListe = new ArrayList<>();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Mock
    private DokumentData dokumentData;
    @Mock
    private DokumentFelles dokumentFelles;
    @Mock
    private DokumentAdresse adresse;
    @Mock
    private DokumentAdresse returadresse;
    @Mock
    private KodeverkRepository kodeverkRepository;
    @Mock
    private VilkårKodeverkRepository vilkårKodeverkRepository;
    @Mock
    private BehandlingRepositoryProvider repositoryProvider;
    private FellesType fellesType;
    private DokumentToBrevDataMapper dokumentToBrevDataMapper;

    @Before
    public void setup() {
        when(adresse.getAdresselinje1()).thenReturn("Stien 5");
        when(adresse.getPostnummer()).thenReturn("1234");
        when(adresse.getPoststed()).thenReturn("Oslo");
        when(dokumentFelles.getMottakerAdresse()).thenReturn(adresse);
        when(dokumentFelles.getPostadresse()).thenReturn(adresse);
        when(dokumentFelles.getSakspartId()).thenReturn("123");
        when(dokumentFelles.getSakspartNavn()).thenReturn("Søkeren");
        when(dokumentFelles.getSaksnummer()).thenReturn(new Saksnummer("123"));
        when(dokumentFelles.getMottakerId()).thenReturn("123");
        when(dokumentFelles.getMottakerNavn()).thenReturn("Søkeren");
        when(dokumentFelles.getNavnAvsenderEnhet()).thenReturn("NAV");
        when(returadresse.getAdresselinje1()).thenReturn("Adresse");
        when(returadresse.getPoststed()).thenReturn("Oslo");
        when(dokumentFelles.getReturadresse()).thenReturn(returadresse);
        when(dokumentFelles.getKontaktTlf()).thenReturn("98745512");
        when(dokumentFelles.getDokumentTypeDataListe()).thenReturn(dokumentTypeDataListe);
        when(dokumentData.getDokumentFelles()).thenReturn(Sets.newSet(dokumentFelles));
        when(dokumentFelles.getSpråkkode()).thenReturn(Språkkode.nb);

        when(vilkårKodeverkRepository.getKodeverkRepository()).thenReturn(kodeverkRepository);
        when(kodeverkRepository.finn(Mockito.eq(no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder.class), Mockito.anyString())).thenReturn(Landkoder.NOR);
        dokumentToBrevDataMapper = new DokumentToBrevDataMapper(repositoryProvider);

        fellesType = dokumentToBrevDataMapper.mapFellesType(dokumentFelles);
    }

    @Test
    public void testBrevdataForVedtaksbrev() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.BELØP, "12345");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.BEHANDLINGSTYPE, "REVURDERING");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.KLAGE_FRIST_UKER, "6");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.SOKERSNAVN, "Obi-Wan Kenobi");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.PERSON_STATUS, PersonstatusKode.ANNET.value());
        DokumentTypeMapper mapper = new VedtaksbrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
        assertThat(resultat).contains("<behandlingsType>REVURDERING</behandlingsType>");
    }

    @Test
    public void testBrevdataForVedtaksbrevEtterKlage() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.BELØP, "12345");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.BEHANDLINGSTYPE, "MEDHOLD");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.KLAGE_FRIST_UKER, "6");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.SOKERSNAVN, "Obi-Wan Kenobi");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.PERSON_STATUS, PersonstatusKode.ANNET.value());
        DokumentTypeMapper mapper = new VedtaksbrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
        assertThat(resultat).contains("<behandlingsType>MEDHOLD</behandlingsType>");
    }

    @Test
    public void testBrevdataForHenleggBehandlingBrev() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.YTELSE_TYPE, "FP");
        DokumentTypeMapper mapper = new HenleggBehandlingBrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        // Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForInnhentopplysningerBrev() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FRIST_DATO, "2017-06-01");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FRITEKST, "Tekst som går\n"
            + "over flere linjer");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.SØKNAD_DATO, "2017-05-13");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.YTELSE_TYPE, "FP");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.BEHANDLINGSTYPE, BehandlingType.KLAGE.getKode());
        leggTilFlettefelt(dokumentTypeDataListe, FLETTEFELT_SOKERS_NAVN, "Tes Ter");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.PERSON_STATUS, PersonstatusKode.ANNET.value());
        DokumentTypeMapper mapper = new InnhentopplysningerBrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        // Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForInnsynskravSvarBrev() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.KLAGE_FRIST_UKER, "6");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FRITEKST, "Tekst som går\n");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.INNSYN_RESULTAT_TYPE, "AVVIST");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.YTELSE_TYPE, "FP");
        DokumentTypeMapper mapper = new InnsynskravSvarBrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        // Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForAvslagsbrev() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.ANTALL_BARN, "1");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.AVSLAGSAARSAK, "1020");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.BEHANDLINGSTYPE, BehandlingstypeType.SØKNAD.name());
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FRITEKST, "Fritekst\nover\nflere\nlinjer");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.GJELDER_FØDSEL, "true");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.KLAGE_FRIST_UKER, "6");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.VILKÅR_TYPE, "FP_VK_2");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.RELASJONSKODE, RelasjonsRolleType.FARA.getKode());
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FØDSELSDATO_PASSERT, "false");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.KJØNN, NavBrukerKjønn.KVINNE.getKode());

        DokumentTypeMapper mapper = new AvslagsbrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForAvslagForeldrepenger() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.BEHANDLINGSTYPE, BehandlingstypeType.SØKNAD.name());
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.SOKERSNAVN, "Trygdebeistet");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.PERSON_STATUS, "ANNET");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.RELASJONSKODE, RelasjonsRolleType.FARA.getKode());
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.MOTTATT_DATO, "2017-05-11");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.GJELDER_FØDSEL, "true");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.ANTALL_BARN, "1");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FØDSELSDATO_PASSERT, "false");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.HALV_G, "45000");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.SISTE_DAG_I_FELLES_PERIODE, "2017-05-11");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.UKER_ETTER_FELLES_PERIODE, "3");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.AVSLAGSAARSAK, "1020");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.KLAGE_FRIST_UKER, "6");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.LOV_HJEMMEL_FOR_AVSLAG, "folketrygdloven §§ 14-10, 14-13 og 22-13");

        DokumentTypeMapper mapper = new AvslagForeldrepengerMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForUendretUtfallDokument() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.YTELSE_TYPE, FagsakYtelseType.ENGANGSTØNAD.getKode());

        DokumentTypeMapper mapper = new UendretutfallBrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForRevurderingDokument() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.ANTALL_BARN, "1");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FRIST_DATO, "2017-10-01");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FRITEKST, "Blabla\nblabla");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.TERMIN_DATO, "2017-12-01");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.YTELSE_TYPE, FagsakYtelseType.ENGANGSTØNAD.getKode());
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.ADVARSEL_KODE, "ANNET");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.ENDRING_I_FREMTID, Boolean.FALSE.toString());

        DokumentTypeMapper mapper = new RevurderingBrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForForlengetDokument() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.ANTALL_BARN, "1");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.BEHANDLINGSFRIST_UKER, "3");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FORLENGET_BEHANDLINGSFRIST, "true");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.SOKNAD_DATO, "2017-08-01");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.YTELSE_TYPE, FagsakYtelseType.ENGANGSTØNAD.getKode());

        DokumentTypeMapper mapper = new ForlengetSaksbehandlingstidBrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForForlengetMedlemskapDokument() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.ANTALL_BARN, "1");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FORLENGET_BEHANDLINGSFRIST, "false");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.SOKNAD_DATO, "2017-08-01");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.YTELSE_TYPE, FagsakYtelseType.ENGANGSTØNAD.getKode());

        DokumentTypeMapper mapper = new ForlengetSaksbehandlingstidBrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForKlageAvvistDokument() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.AVVIST_GRUNN, KlageAvvistÅrsak.KLAGET_FOR_SENT.getKode());
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.KLAGE_FRIST_UKER, "6");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.YTELSE_TYPE, FagsakYtelseType.ENGANGSTØNAD.getKode());

        DokumentTypeMapper mapper = new KlageAvvistBrevMapper(kodeverkRepository);

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForKlageYtelsesvedtakStadfestetDokument() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FRITEKST, "BlaBla");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.KLAGE_FRIST_UKER, "6");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.YTELSE_TYPE, FagsakYtelseType.ENGANGSTØNAD.getKode());

        DokumentTypeMapper mapper = new KlageYtelsesvedtakStadfestetBrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForKlageYtelsesvedtakOpphevetDokument() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.ANTALL_UKER, "6");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FRIST_DATO, LocalDate.now().plusWeeks(3).toString());
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FRITEKST, "Bare Tull");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.YTELSE_TYPE, FagsakYtelseType.ENGANGSTØNAD.getKode());

        DokumentTypeMapper mapper = new KlageYtelsesvedtakOpphevetBrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForKlageOversendtKlageinstansDokument() throws Exception {
        // Arrange
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.ANTALL_UKER, "3");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FRIST_DATO, LocalDate.now().plusWeeks(3).toString());
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.FRITEKST, "Mer Tull");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.MOTTATT_DATO, LocalDate.now().toString());
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.YTELSE_TYPE, FagsakYtelseType.ENGANGSTØNAD.getKode());

        DokumentTypeMapper mapper = new KlageOversendtKlageinstansBrevMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    public void testBrevdataForInntektsmeldingForTidligDokument() throws Exception {
        // Arrange
        List<FeriePeriodeDto> perioder = Lists.newArrayList();
        FeriePeriodeDto periode = new FeriePeriodeDto();
        periode.setFeriePeriodeFom("2018-08-27");
        periode.setFeriePeriodeTom("2018-09-15");
        perioder.add(periode);
        FeriePeriodeDto periode2 = new FeriePeriodeDto();
        periode2.setFeriePeriodeFom("2018-09-30");
        periode2.setFeriePeriodeTom("2018-10-17");
        perioder.add(periode2);

        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.SOK_ANTALL_UKER_FOR, "3");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.ARBEIDSGIVER_NAVN, "Axlitech AS");
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.MOTTATT_DATO, LocalDate.now().toString());
        leggTilFlettefelt(dokumentTypeDataListe, Flettefelt.BEHANDLINGSTYPE, BehandlingType.FØRSTEGANGSSØKNAD.getKode());
        leggTilStrukturertFlettefeltListe(dokumentTypeDataListe, Flettefelt.PERIODE_LISTE, perioder);

        DokumentTypeMapper mapper = new InntektsmeldingForTidligMapper();

        // Act
        String resultat = mapper.mapTilBrevXML(fellesType, dokumentFelles);

        //Assert
        assertThat(resultat).isNotNull();
    }

    private void leggTilFlettefelt(List<DokumentTypeData> dokumentTypeDataListe, String navn, String verdi) {
        DokumentTypeData felt = new DokumentTypeData();
        felt.setDoksysId(navn);
        felt.setVerdi(verdi);
        dokumentTypeDataListe.add(felt);
    }

    private static void leggTilStrukturertFlettefeltListe(List<DokumentTypeData> dokumentTypeDataListe, String feltnavn, List<FeriePeriodeDto> feltverdier) {
        int nummer = 0;
        for (FeriePeriodeDto feltverdi : feltverdier) {
            DokumentTypeData f = new DokumentTypeData();
            f.setDoksysId(feltnavn + ":" + nummer);
            f.setStrukturertVerdi(FlettefeltJsonObjectMapper.toJson(feltverdi));
            dokumentTypeDataListe.add(f);
            nummer++;
        }
    }
}
