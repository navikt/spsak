package no.nav.foreldrepenger.grensesnittavstemming;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Attestant180;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.OppdragKvittering;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragsenhet120;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.AksjonType;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.Aksjonsdata;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.AvstemmingType;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.Avstemmingsdata;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.KildeType;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TfradragTillegg;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeAksjon;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndring;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKomponent;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiTypeSats;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiUtbetFrekvens;

public class GrensesnittavstemmingMapperTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private static final int MAKS_AVSTEMMING_MELDING_BYTES = 32000;

    private static final String KODE_KLASSIFIK_FODSEL = "FPENFOD-OP";

    private static final String MELDINGKODE = "Kode1234";

    private static final String BESKRIVENDE_MELDING = "Melding med lengde 70 tegn slik at vi tester maksimal lengde her......";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private Oppdragskontroll.Builder oppdrkontrollBuilder;
    private Oppdrag110.Builder oppdr110Builder;
    private Avstemming115.Builder avst115Builder;
    private Oppdragsenhet120.Builder oppdrsEnhet120Builder;
    private Oppdragslinje150.Builder oppdrLinje150Builder;
    private Attestant180.Builder attestant180Builder;
    private OppdragKvittering.Builder oppdragKvitteringBuilder;
    private GrensesnittavstemmingMapper grensesnittavstemmingMapper;
    private String kodeFagområde;

    private List<Oppdrag110> oppdragsliste;

    @Before
    public void setup() {
        oppdrkontrollBuilder = Oppdragskontroll.builder();
        oppdr110Builder = Oppdrag110.builder();
        avst115Builder = Avstemming115.builder();
        oppdrsEnhet120Builder = Oppdragsenhet120.builder();
        oppdrLinje150Builder = Oppdragslinje150.builder();
        attestant180Builder = Attestant180.builder();
        oppdragKvitteringBuilder = OppdragKvittering.builder();
        kodeFagområde = ØkonomiKodeFagområde.FPREF.name();
        Oppdragskontroll oppdragskontroll = opprettOppdrag(null, kodeFagområde);

        oppdragsliste = Collections.singletonList(oppdragskontroll.getOppdrag110Liste().get(0));
        grensesnittavstemmingMapper = new GrensesnittavstemmingMapper(oppdragsliste, kodeFagområde);
    }

    private Oppdragskontroll opprettOppdrag(String status, String fagområde) {
        Oppdragskontroll oppdragskontroll = buildOppdragskontroll(status == null);

        Avstemming115 avstemming115 = buildAvstemming115();
        Oppdrag110 oppdrag110 = buildOppdrag110(oppdragskontroll, avstemming115, fagområde);
        buildOppdragsEnhet120(oppdrag110);
        Oppdragslinje150 oppdragslinje150 = buildOppdragslinje150(oppdrag110);
        buildAttestant180(oppdragslinje150);

        if (status != null) {
            OppdragKvittering oppdragKvittering = buildOppdragKvittering(oppdrag110);
            oppdragKvittering.setAlvorlighetsgrad(status);
            if (!"00".equals(status)) {
                oppdragKvittering.setBeskrMelding(BESKRIVENDE_MELDING);
                oppdragKvittering.setMeldingKode(MELDINGKODE);
            }
        }
        return oppdragskontroll;
    }

    @Test
    public void testStartmeldingXML() {
        // Arrange
        // Act
        String melding = grensesnittavstemmingMapper.lagStartmelding();
        // Assert
        assertThat(melding).isNotNull();
        assertThat(melding).startsWith("<?xml");
        assertThat(melding.length()).isLessThan(MAKS_AVSTEMMING_MELDING_BYTES);
    }

    @Test
    public void testDatameldingXML() {
        // Arrange
        // Act
        List<String> meldinger = grensesnittavstemmingMapper.lagDatameldinger();
        // Assert
        assertThat(meldinger).hasSize(1);
        for (String melding : meldinger) {
            assertThat(melding).isNotNull();
            assertThat(melding).startsWith("<?xml");
            assertThat(melding.length()).isLessThan(MAKS_AVSTEMMING_MELDING_BYTES);
        }
    }

    private void setupForStoreDatamengder(String kodeFagområde) {
        oppdragsliste = new ArrayList<>();
        for (int gruppe = 0; gruppe < 60; gruppe++) {
            Oppdragskontroll oppdrag = opprettOppdrag("00", kodeFagområde);
            oppdragsliste.addAll(oppdrag.getOppdrag110Liste());

            oppdrag = opprettOppdrag(null, kodeFagområde);
            oppdragsliste.addAll(oppdrag.getOppdrag110Liste());

            oppdrag = opprettOppdrag("08", kodeFagområde);
            oppdragsliste.addAll(oppdrag.getOppdrag110Liste());

            oppdrag = opprettOppdrag("04", kodeFagområde);
            oppdragsliste.addAll(oppdrag.getOppdrag110Liste());
        }
        grensesnittavstemmingMapper = new GrensesnittavstemmingMapper(oppdragsliste, kodeFagområde);
    }

    private void setupForStørreDatamengder(String kodeFagområde) {

        oppdragsliste = new ArrayList<>();
        for (int gruppe = 0; gruppe < 560; gruppe++) {
            Oppdragskontroll oppdrag = opprettOppdrag("00", kodeFagområde);
            oppdragsliste.addAll(oppdrag.getOppdrag110Liste());

            oppdrag = opprettOppdrag(null, kodeFagområde);
            oppdragsliste.addAll(oppdrag.getOppdrag110Liste());

            oppdrag = opprettOppdrag("08", kodeFagområde);
            oppdragsliste.addAll(oppdrag.getOppdrag110Liste());

            oppdrag = opprettOppdrag("04", kodeFagområde);
            oppdragsliste.addAll(oppdrag.getOppdrag110Liste());
        }
        grensesnittavstemmingMapper = new GrensesnittavstemmingMapper(oppdragsliste, kodeFagområde);
    }

    private void opprettOppdragMedFlereOppdrag110ForForskjelligeFagområder() {
        oppdragsliste = new ArrayList<>();

        Oppdragskontroll oppdrag = opprettOppdrag("00", ØkonomiKodeFagområde.FP.name());
        Oppdragskontroll oppdrag2 = opprettOppdrag("00", kodeFagområde);
        Oppdragskontroll oppdrag3 = opprettOppdrag("00", ØkonomiKodeFagområde.REFUTG.name());
        oppdrag.getOppdrag110Liste().add(oppdrag2.getOppdrag110Liste().get(0));
        oppdrag.getOppdrag110Liste().add(oppdrag3.getOppdrag110Liste().get(0));

        oppdragsliste.addAll(oppdrag.getOppdrag110Liste());

        grensesnittavstemmingMapper = new GrensesnittavstemmingMapper(oppdragsliste, kodeFagområde);
    }

    @Test
    public void testDatameldingXMLvedStoreDatamengder() {
        // Arrange
        setupForStoreDatamengder(ØkonomiKodeFagområde.FPREF.name());
        // Act
        List<String> meldinger = grensesnittavstemmingMapper.lagDatameldinger();
        // Assert
        assertThat(meldinger).hasSize(3);
        for (String melding : meldinger) {
            assertThat(melding).isNotNull();
            assertThat(melding).startsWith("<?xml");
            assertThat(melding.length()).isLessThan(MAKS_AVSTEMMING_MELDING_BYTES);
        }
    }

    @Test
    public void testSluttmeldingXML() {
        // Arrange
        // Act
        String melding = grensesnittavstemmingMapper.lagSluttmelding();
        // Assert
        assertThat(melding).isNotNull();
        assertThat(melding).startsWith("<?xml");
        assertThat(melding.length()).isLessThan(MAKS_AVSTEMMING_MELDING_BYTES);
    }

    @Test
    public void testStartmeldingInnhold() {
        // Arrange
        // Act
        Avstemmingsdata avstemmingsdata = grensesnittavstemmingMapper.lagAvstemmingsdataFelles(AksjonType.START);
        // Assert
        sjekkAksjonsInnhold(avstemmingsdata, AksjonType.START, false, kodeFagområde);
    }

    @Test
    public void testDatameldingInnhold() {
        // Arrange
        // Act
        List<Avstemmingsdata> avstemmingsdataListe = grensesnittavstemmingMapper.lagAvstemmingsdataListe();
        // Assert
        assertThat(avstemmingsdataListe).hasSize(1);
        sjekkAksjonsInnhold(avstemmingsdataListe.get(0), AksjonType.DATA, true, kodeFagområde);
    }

    @Test
    public void testSluttmeldingInnhold() {
        // Arrange
        // Act
        Avstemmingsdata avstemmingsdata = grensesnittavstemmingMapper.lagAvstemmingsdataFelles(AksjonType.AVSL);
        // Assert
        sjekkAksjonsInnhold(avstemmingsdata, AksjonType.AVSL, false, kodeFagområde);
    }

    @Test
    public void testDatameldingVedStoreDatamengder() {
        // Arrange
        String kodeFagområde = ØkonomiKodeFagområde.REFUTG.name();
        setupForStoreDatamengder(kodeFagområde);
        // Act
        List<Avstemmingsdata> avstemmingsdataListe = grensesnittavstemmingMapper.lagAvstemmingsdataListe();
        // Assert
        assertThat(avstemmingsdataListe).hasSize(3);
        sjekkAksjonsInnhold(avstemmingsdataListe.get(0), AksjonType.DATA, true, kodeFagområde);
        sjekkAksjonsInnhold(avstemmingsdataListe.get(1), AksjonType.DATA, false, kodeFagområde);
        sjekkAksjonsInnhold(avstemmingsdataListe.get(2), AksjonType.DATA, false, kodeFagområde);
    }

    @Test
    public void testAtSisteDataHarInnslag() {
        // Arrange
        setupForStørreDatamengder(ØkonomiKodeFagområde.FP.name());
        // Act
        List<Avstemmingsdata> avstemmingsdataListe = grensesnittavstemmingMapper.lagAvstemmingsdataListe();
        // Assert
        assertThat(avstemmingsdataListe.get(avstemmingsdataListe.size() - 1).getDetalj()).isNotEmpty();
    }

    @Test
    public void testForFlereOppdrag110MedForskjelligeFagområder() {
        // Arrange
        opprettOppdragMedFlereOppdrag110ForForskjelligeFagområder();
        // Act
        List<Avstemmingsdata> avstemmingsdataListe = grensesnittavstemmingMapper.lagAvstemmingsdataListe();
        // Assert
        assertThat(avstemmingsdataListe.size()).isEqualTo(1);
        assertThat(avstemmingsdataListe.get(avstemmingsdataListe.size() - 1).getAksjon().getUnderkomponentKode()).isEqualTo(kodeFagområde);
    }

    @Test
    public void testForFlereOppdrag110ForSammeOppdragskontroll() {
        //Arrange
        oppdragsliste = new ArrayList<>();
        LocalDateTime lavAvstemmingsDato = LocalDateTime.of(2018, 10, 25, 0, 0, 1);
        LocalDateTime mellomAvstemmingsDato = LocalDateTime.of(2018, 10, 25, 12, 10, 1);
        LocalDateTime høyestAvstemmingsDato = LocalDateTime.of(2018, 10, 25, 23, 3, 1);

        Oppdragskontroll oppdrag = buildOppdragskontroll(false);

        opprettOppdrag110MedAvsetmmingsDato(oppdrag, mellomAvstemmingsDato, kodeFagområde);
        Avstemming115 forventetTom = opprettOppdrag110MedAvsetmmingsDato(oppdrag, høyestAvstemmingsDato, kodeFagområde).getAvstemming115();
        Avstemming115 forventetFom = opprettOppdrag110MedAvsetmmingsDato(oppdrag, lavAvstemmingsDato, kodeFagområde).getAvstemming115();

        oppdragsliste.addAll(oppdrag.getOppdrag110Liste());

        //Act
        grensesnittavstemmingMapper = new GrensesnittavstemmingMapper(oppdragsliste, kodeFagområde);

        List<Avstemmingsdata> avstemmingsdata = grensesnittavstemmingMapper.lagAvstemmingsdataListe();

        //Assert
        assertThat(avstemmingsdata).isNotNull();
        assertThat(avstemmingsdata.size()).isEqualTo(1);
        sjekkAksjonsInnhold(forventetFom, forventetTom, avstemmingsdata.get(0), AksjonType.DATA, true, kodeFagområde);
    }

    private Oppdrag110 opprettOppdrag110MedAvsetmmingsDato(Oppdragskontroll oppdrag, LocalDateTime lavAvstemmingsDato, String kodeFagområde) {
        Oppdrag110 oppdrag110 = buildOppdrag110(oppdrag, buildAvstemming115(lavAvstemmingsDato, lavAvstemmingsDato), kodeFagområde);
        buildOppdragsEnhet120(oppdrag110);
        Oppdragslinje150 oppdragslinje150 = buildOppdragslinje150(oppdrag110);
        buildAttestant180(oppdragslinje150);
        return oppdrag110;
    }

    private void sjekkAksjonsInnhold(Avstemming115 forvendetFom, Avstemming115 forvendetTom, Avstemmingsdata avstemmingsdata, AksjonType aksjonType, boolean første, String kodeFagområde) {
        sjekkAksjonsInnhold(avstemmingsdata, aksjonType, første, kodeFagområde);

        Aksjonsdata aksjon = avstemmingsdata.getAksjon();
        assertThat(aksjon.getNokkelFom()).isEqualTo(GrensesnittavstemmingMapper.tilSpesialkodetDatoOgKlokkeslett(forvendetFom.getNokkelAvstemming()));
        assertThat(aksjon.getNokkelTom()).isEqualTo(GrensesnittavstemmingMapper.tilSpesialkodetDatoOgKlokkeslett(forvendetTom.getNokkelAvstemming()));
    }

    private void sjekkAksjonsInnhold(Avstemmingsdata avstemmingsdata, AksjonType aksjonType, boolean første, String kodeFagområde) {
        assertThat(avstemmingsdata).isNotNull();
        if (AksjonType.DATA.equals(aksjonType)) {
            assertThat(avstemmingsdata.getDetalj()).isNotEmpty();
            if (første) {
                assertThat(avstemmingsdata.getGrunnlag()).isNotNull();
                assertThat(avstemmingsdata.getPeriode()).isNotNull();
                assertThat(avstemmingsdata.getTotal()).isNotNull();
            } else {
                assertThat(avstemmingsdata.getGrunnlag()).isNull();
                assertThat(avstemmingsdata.getPeriode()).isNull();
                assertThat(avstemmingsdata.getTotal()).isNull();
            }
        } else {
            assertThat(avstemmingsdata.getDetalj()).isEmpty();
            assertThat(avstemmingsdata.getGrunnlag()).isNull();
            assertThat(avstemmingsdata.getPeriode()).isNull();
            assertThat(avstemmingsdata.getTotal()).isNull();
        }
        Aksjonsdata aksjon = avstemmingsdata.getAksjon();
        assertThat(aksjon).isNotNull();
        assertThat(aksjon.getAksjonType()).isEqualTo(aksjonType);
        assertThat(aksjon.getAvleverendeAvstemmingId()).isNotNull();
        assertThat(aksjon.getAvleverendeKomponentKode()).isEqualTo(ØkonomiKodeKomponent.VLFP.getKodeKomponent());
        assertThat(aksjon.getAvstemmingType()).isEqualTo(AvstemmingType.GRSN);
        assertThat(aksjon.getBrukerId()).isEqualTo(GrensesnittavstemmingMapper.BRUKER_ID_FOR_VEDTAKSLØSNINGEN);
        assertThat(aksjon.getKildeType()).isEqualTo(KildeType.AVLEV);
        assertThat(aksjon.getMottakendeKomponentKode()).isEqualTo(ØkonomiKodeKomponent.OS.getKodeKomponent());
        assertThat(aksjon.getUnderkomponentKode()).isEqualTo(kodeFagområde);
    }

    //    ---------------------------------------------

    private Attestant180 buildAttestant180(Oppdragslinje150 oppdragslinje150) {
        return attestant180Builder
            .medOppdragslinje150(oppdragslinje150)
            .medAttestantId("1234")
            .build();
    }

    private Oppdragslinje150 buildOppdragslinje150(Oppdrag110 oppdrag110) {

        return oppdrLinje150Builder
            .medKodeEndringLinje("ENDR")
            .medKodeStatusLinje("OPPH")
            .medDatoStatusFom(LocalDate.now())
            .medVedtakId("345")
            .medDelytelseId(64L)
            .medKodeKlassifik(KODE_KLASSIFIK_FODSEL)
            .medVedtakFomOgTom(LocalDate.now(), LocalDate.now())
            .medSats(61122L)
            .medFradragTillegg(TfradragTillegg.F.value())
            .medTypeSats(ØkonomiTypeSats.UKE.name())
            .medBrukKjoreplan("B")
            .medSaksbehId("F2365245")
            .medUtbetalesTilId("123456789")
            .medOppdrag110(oppdrag110)
            .medHenvisning(43L)
            .build();

    }

    private Oppdragsenhet120 buildOppdragsEnhet120(Oppdrag110 oppdrag110) {
        return oppdrsEnhet120Builder
            .medTypeEnhet("BOS")
            .medEnhet("8020")
            .medDatoEnhetFom(LocalDate.now())
            .medOppdrag110(oppdrag110)
            .build();
    }

    private Avstemming115 buildAvstemming115() {
        return avst115Builder
            .medKodekomponent(ØkonomiKodeKomponent.VLFP.getKodeKomponent())
            .medNokkelAvstemming(LocalDateTime.now())
            .medTidspnktMelding(LocalDateTime.now().minusDays(1))
            .build();
    }

    private Avstemming115 buildAvstemming115(LocalDateTime nokkelAvsteming, LocalDateTime tidspunktMelding) {
        return avst115Builder
            .medKodekomponent(ØkonomiKodeKomponent.VLFP.getKodeKomponent())
            .medNokkelAvstemming(nokkelAvsteming)
            .medTidspnktMelding(tidspunktMelding)
            .build();
    }

    private Oppdrag110 buildOppdrag110(Oppdragskontroll oppdragskontroll, Avstemming115 avstemming115, String fagområde) {
        return oppdr110Builder
            .medKodeAksjon(ØkonomiKodeAksjon.TRE.getKodeAksjon())
            .medKodeEndring(ØkonomiKodeEndring.NY.name())
            .medKodeFagomrade(fagområde)
            .medFagSystemId(44L)
            .medUtbetFrekvens(ØkonomiUtbetFrekvens.DAG.getUtbetFrekvens())
            .medOppdragGjelderId("22038235641")
            .medDatoOppdragGjelderFom(LocalDate.of(2000, 1, 1))
            .medSaksbehId("J5624215")
            .medOppdragskontroll(oppdragskontroll)
            .medAvstemming115(avstemming115)
            .build();
    }

    private OppdragKvittering buildOppdragKvittering(Oppdrag110 oppdr110) {
        return oppdragKvitteringBuilder
            .medOppdrag110(oppdr110)
            .build();
    }

    private Oppdragskontroll buildOppdragskontroll(boolean venterKvittering) {
        return oppdrkontrollBuilder
            .medBehandlingId(154L)
            .medSaksnummer(new Saksnummer("35"))
            .medVenterKvittering(venterKvittering)
            .medProsessTaskId(56L)
            .medSimulering(false)
            .build();
    }
}
