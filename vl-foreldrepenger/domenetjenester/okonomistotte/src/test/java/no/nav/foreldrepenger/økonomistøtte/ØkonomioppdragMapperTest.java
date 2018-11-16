package no.nav.foreldrepenger.økonomistøtte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Attestant180;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Grad170;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragsenhet120;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Refusjonsinfo156;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Oppdrag;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragsLinje150;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TfradragTillegg;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TkodeStatusLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeAksjon;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndring;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKlassifik;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKomponent;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiTypeSats;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiUtbetFrekvens;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class ØkonomioppdragMapperTest {

    private static final String KODE_KLASSIFIK_FODSEL = "FPENFOD-OP";
    private static final String TYPE_GRAD = "UFOR";
    private static final String REFUNDERES_ID = "123456789";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private Oppdragskontroll.Builder oppdrkontrollBuilder;
    private Oppdrag110.Builder oppdr110Builder;
    private Avstemming115.Builder avst115Builder;
    private Oppdragsenhet120.Builder oppdrsEnhet120Builder;
    private Oppdragslinje150.Builder oppdrLinje150Builder;
    private Attestant180.Builder attestant180Builder;
    private Grad170.Builder grad170Builder;
    private Refusjonsinfo156.Builder refusjonsinfo156Builder;
    private Oppdragskontroll oppdragskontroll;
    private ØkonomioppdragMapper økonomioppdragMapper;
    private Avstemming115 avstemming115;

    @Before
    public void setup() {
        oppdrkontrollBuilder = Oppdragskontroll.builder();
        oppdr110Builder = Oppdrag110.builder();
        avst115Builder = Avstemming115.builder();
        oppdrsEnhet120Builder = Oppdragsenhet120.builder();
        oppdrLinje150Builder = Oppdragslinje150.builder();
        attestant180Builder = Attestant180.builder();
        grad170Builder = Grad170.builder();
        refusjonsinfo156Builder = Refusjonsinfo156.builder();

        oppdragskontroll = buildOppdragskontroll();
        økonomioppdragMapper = new ØkonomioppdragMapper(oppdragskontroll);

    }

    @Test
    public void testMapVedtaksDataToOppdragES() throws DatatypeConfigurationException {
        List<Oppdrag110> oppdrag110 = opprettOppdrag110(false);
        verifyMapVedtaksDataToOppdrag(oppdrag110, false);
    }

    @Test
    public void testMapVedtaksDataToOppdragFP() throws DatatypeConfigurationException {
        List<Oppdrag110> oppdrag110 = opprettOppdrag110(true);
        verifyMapVedtaksDataToOppdrag(oppdrag110, true);
    }

    @Test
    public void testGenereringAvXmlStringES() {
        opprettOppdrag110(false);
        List<String> oppdragXmlListe = økonomioppdragMapper.generateOppdragXML();
        assertThat(oppdragXmlListe).isNotNull();
    }

    @Test
    public void testGenereringAvXmlStringFP() {
        opprettOppdrag110(true);
        List<String> oppdragXmlListe = økonomioppdragMapper.generateOppdragXML();
        assertThat(oppdragXmlListe).isNotNull();
    }

    // ---------------------------------------------

    private List<Oppdrag110> opprettOppdrag110(Boolean gjelderFP) {

        avstemming115 = buildAvstemming115();
        List<Oppdrag110> oppdrag110Liste = buildOppdrag110(oppdragskontroll, avstemming115, gjelderFP);
        buildOppdragsEnhet120(oppdrag110Liste);
        List<Oppdragslinje150> oppdragslinje150Liste = buildOppdragslinje150(oppdrag110Liste, gjelderFP);
        buildAttestant180(oppdragslinje150Liste);
        if (gjelderFP) {
            buildGrad170(oppdragslinje150Liste);
            buildRefusjonsinfo156(oppdragslinje150Liste);
        }
        return oppdrag110Liste;
    }

    private void verifyMapVedtaksDataToOppdrag(List<Oppdrag110> oppdrag110Liste, Boolean gjelderFP) throws DatatypeConfigurationException {

        for (Oppdrag110 oppdrag110 : oppdrag110Liste) {
            Oppdrag oppdrag = økonomioppdragMapper.mapVedtaksDataToOppdrag(oppdrag110);

            no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Oppdrag110 oppdrag110Generert = oppdrag.getOppdrag110();
            assertThat(oppdrag110Generert.getKodeAksjon()).isEqualTo(oppdrag110.getKodeAksjon());
            assertThat(oppdrag110Generert.getKodeEndring()).isEqualTo(oppdrag110.getKodeEndring());
            assertThat(oppdrag110Generert.getKodeFagomraade()).isEqualTo(oppdrag110.getKodeFagomrade()); //
            assertThat(oppdrag110Generert.getUtbetFrekvens()).isEqualTo(oppdrag110.getUtbetFrekvens());

            no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Avstemming115 avstemming115Generert = oppdrag110Generert.getAvstemming115();
            assertThat(avstemming115Generert.getKodeKomponent()).isEqualTo(avstemming115.getKodekomponent());
            assertThat(avstemming115Generert.getNokkelAvstemming())
                .isEqualTo(ØkonomioppdragMapper.tilSpesialkodetDatoOgKlokkeslett(avstemming115.getNokkelAvstemming()));
            assertThat(avstemming115Generert.getTidspktMelding())
                .isEqualTo(ØkonomioppdragMapper.tilSpesialkodetDatoOgKlokkeslett(avstemming115.getTidspnktMelding()));

            no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragsEnhet120 oppdragsEnhet120Generert = oppdrag110Generert.getOppdragsEnhet120().get(0);

            Oppdragsenhet120 oppdragsEnhet120 = oppdrag110.getOppdragsenhet120Liste().get(0);
            assertThat(oppdragsEnhet120Generert.getTypeEnhet()).isEqualTo(oppdragsEnhet120.getTypeEnhet());
            assertThat(oppdragsEnhet120Generert.getEnhet()).isEqualTo(oppdragsEnhet120.getEnhet());
            assertThat(oppdragsEnhet120Generert.getDatoEnhetFom())
                .isEqualTo(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(oppdragsEnhet120.getDatoEnhetFom()));

            List<no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragsLinje150> oppdragsLinje150GenerertListe = oppdrag110Generert
                .getOppdragsLinje150();

            int ix = 0;
            for (OppdragsLinje150 oppdragsLinje150Generert : oppdragsLinje150GenerertListe) {
                Oppdragslinje150 oppdragslinje150 = oppdrag110.getOppdragslinje150Liste().get(ix);
                assertThat(oppdragsLinje150Generert.getKodeEndringLinje()).isEqualTo(oppdragslinje150.getKodeEndringLinje());
                assertThat(oppdragsLinje150Generert.getKodeStatusLinje()).isEqualTo(TkodeStatusLinje.fromValue(oppdragslinje150.getKodeStatusLinje()));
                assertThat(oppdragsLinje150Generert.getDatoStatusFom())
                    .isEqualTo(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(oppdragslinje150.getDatoStatusFom()));
                assertThat(oppdragsLinje150Generert.getVedtakId()).isEqualTo(String.valueOf(oppdragslinje150.getVedtakId()));
                assertThat(oppdragsLinje150Generert.getDelytelseId()).isEqualTo(String.valueOf(oppdragslinje150.getDelytelseId()));
                assertThat(oppdragsLinje150Generert.getKodeKlassifik()).isEqualTo(oppdragslinje150.getKodeKlassifik());
                assertThat(oppdragsLinje150Generert.getDatoVedtakFom())
                    .isEqualTo(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(oppdragslinje150.getDatoVedtakFom()));
                assertThat(oppdragsLinje150Generert.getDatoVedtakTom())
                    .isEqualTo(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(oppdragslinje150.getDatoVedtakTom()));
                assertThat(oppdragsLinje150Generert.getSats()).isEqualTo(new BigDecimal(oppdragslinje150.getSats()));
                assertThat(oppdragsLinje150Generert.getFradragTillegg()).isEqualTo(TfradragTillegg.fromValue(oppdragslinje150.getFradragTillegg()));
                assertThat(oppdragsLinje150Generert.getTypeSats()).isEqualTo(oppdragslinje150.getTypeSats());
                assertThat(oppdragsLinje150Generert.getBrukKjoreplan()).isEqualTo(oppdragslinje150.getBrukKjoreplan());
                assertThat(oppdragsLinje150Generert.getSaksbehId()).isEqualTo(oppdragslinje150.getSaksbehId());
                assertThat(oppdragsLinje150Generert.getUtbetalesTilId()).isEqualTo(oppdragslinje150.getUtbetalesTilId());
                assertThat(oppdragsLinje150Generert.getHenvisning()).isEqualTo(String.valueOf(oppdragslinje150.getHenvisning()));
                if (!gjelderFP) {
                    assertThat(oppdragsLinje150Generert.getRefFagsystemId()).isNull();
                    assertThat(oppdragsLinje150Generert.getRefDelytelseId()).isNull();
                }

                List<no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Attestant180> attestant180GenerertListe = oppdragsLinje150Generert
                    .getAttestant180();
                for (no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Attestant180 attestant180Generert : attestant180GenerertListe) {
                    Attestant180 attestant180 = oppdragslinje150.getAttestant180Liste().get(0);
                    assertThat(attestant180Generert.getAttestantId()).isEqualTo(attestant180.getAttestantId());
                }

                if (gjelderFP) {
                    List<no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Grad170> grad170GenerertListe = oppdragsLinje150Generert.getGrad170();
                    for (no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Grad170 grad170Generert : grad170GenerertListe) {
                        Grad170 grad170 = oppdragslinje150.getGrad170Liste().get(0);
                        assertThat(grad170Generert.getGrad()).isEqualTo(BigInteger.valueOf(grad170.getGrad()));
                        assertThat(grad170Generert.getTypeGrad()).isEqualTo(grad170.getTypeGrad());
                    }

                    no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Refusjonsinfo156 refusjonsinfo156Generert = oppdragsLinje150Generert
                        .getRefusjonsinfo156();
                    Optional<Refusjonsinfo156> refusjonsinfo156Opt = Optional.ofNullable(oppdragslinje150.getRefusjonsinfo156());
                    refusjonsinfo156Opt.ifPresent(refusjonsinfo156 -> {
                        assertThat(refusjonsinfo156Generert.getRefunderesId()).isEqualTo(refusjonsinfo156.getRefunderesId());
                        try {
                            assertThat(refusjonsinfo156Generert.getDatoFom())
                                .isEqualTo(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(refusjonsinfo156.getDatoFom()));
                            assertThat(refusjonsinfo156Generert.getMaksDato())
                                .isEqualTo(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(refusjonsinfo156.getMaksDato()));
                        } catch (DatatypeConfigurationException e) {
                            fail(e.getMessage());
                        }
                    });
                }
                ix++;
            }
        }
    }

    private List<Attestant180> buildAttestant180(List<Oppdragslinje150> oppdragslinje150Liste) {

        List<Attestant180> attestant180Liste = new ArrayList<>();
        for (Oppdragslinje150 oppdragslinje150 : oppdragslinje150Liste) {
            attestant180Liste.add(attestant180Builder
                .medOppdragslinje150(oppdragslinje150)
                .medAttestantId("1234")
                .build());
        }
        return attestant180Liste;
    }

    private List<Grad170> buildGrad170(List<Oppdragslinje150> oppdragslinje150Liste) {
        List<Grad170> grad170Liste = new ArrayList<>();
        for (Oppdragslinje150 oppdragslinje150 : oppdragslinje150Liste) {
            grad170Liste.add(grad170Builder
                .medGrad(100)
                .medTypeGrad(TYPE_GRAD)
                .medOppdragslinje150(oppdragslinje150)
                .build());
        }
        return grad170Liste;
    }

    private List<Refusjonsinfo156> buildRefusjonsinfo156(List<Oppdragslinje150> oppdragslinje150Liste) {
        List<Refusjonsinfo156> refusjonsinfo156Liste = new ArrayList<>();
        List<Oppdragslinje150> oppdragslinje150List = oppdragslinje150Liste.stream()
            .filter(oppdragslinje150 -> oppdragslinje150.getOppdrag110().getKodeFagomrade().equals("FPREF")).collect(Collectors.toList());
        for (Oppdragslinje150 opp150 : oppdragslinje150List) {
            refusjonsinfo156Liste.add(refusjonsinfo156Builder
                .medMaksDato(LocalDate.now())
                .medDatoFom(LocalDate.now())
                .medRefunderesId(REFUNDERES_ID)
                .medOppdragslinje150(opp150)
                .build());
        }
        return refusjonsinfo156Liste;
    }

    private List<Oppdragslinje150> buildOppdragslinje150(List<Oppdrag110> oppdrag110Liste, Boolean gjelderFP) {

        List<Oppdragslinje150> oppdragslinje150Liste = new ArrayList<>();
        for (Oppdrag110 oppdrag110 : oppdrag110Liste) {
            oppdragslinje150Liste.add(buildOppdragslinje150(gjelderFP, oppdrag110));
            if (gjelderFP) {
                oppdragslinje150Liste.add(buildOppdragslinje150Feriepenger(oppdrag110));
            }
        }
        return oppdragslinje150Liste;
    }

    private Oppdragslinje150 buildOppdragslinje150(Boolean gjelderFP, Oppdrag110 oppdrag110) {
        settFellesFelterIOpp150();
        String kodeKlassifik = finnKodeKlassifikVerdi(oppdrag110);
        return oppdrLinje150Builder
            .medKodeKlassifik(gjelderFP ? kodeKlassifik : KODE_KLASSIFIK_FODSEL)
            .medTypeSats(gjelderFP ? ØkonomiTypeSats.DAG.name() : ØkonomiTypeSats.UKE.name())
            .medOppdrag110(oppdrag110)
            .build();
    }

    private Oppdragslinje150 buildOppdragslinje150Feriepenger(Oppdrag110 oppdrag110) {
        settFellesFelterIOpp150();
        String kodeKlassifik = oppdrag110.getKodeFagomrade().equals("FP") ? ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik()
            : ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik();
        return oppdrLinje150Builder
            .medKodeKlassifik(kodeKlassifik)
            .medTypeSats(ØkonomiTypeSats.ENG.name())
            .medOppdrag110(oppdrag110)
            .build();
    }

    private String finnKodeKlassifikVerdi(Oppdrag110 oppdrag110) {
        if (oppdrag110.getKodeFagomrade().equals("FP")) {
            return ØkonomiKodeKlassifik.FPATORD.getKodeKlassifik();
        }
        return ØkonomiKodeKlassifik.FPREFAG_IOP.getKodeKlassifik();
    }

    private void settFellesFelterIOpp150() {
        oppdrLinje150Builder
            .medKodeEndringLinje("ENDR")
            .medKodeStatusLinje("OPPH")
            .medDatoStatusFom(LocalDate.now())
            .medVedtakId("345")
            .medDelytelseId(64L)
            .medVedtakFomOgTom(LocalDate.now(), LocalDate.now())
            .medSats(1122L)
            .medFradragTillegg(TfradragTillegg.F.value())
            .medBrukKjoreplan("B")
            .medSaksbehId("F2365245")
            .medUtbetalesTilId("123456789")
            .medHenvisning(43L);
    }

    private List<Oppdragsenhet120> buildOppdragsEnhet120(List<Oppdrag110> oppdrag110Liste) {
        List<Oppdragsenhet120> oppdragsenhet120Liste = new ArrayList<>();
        for (Oppdrag110 oppdrag110 : oppdrag110Liste) {
            oppdragsenhet120Liste.add(oppdrsEnhet120Builder
                .medTypeEnhet("BOS")
                .medEnhet("8020")
                .medDatoEnhetFom(LocalDate.now())
                .medOppdrag110(oppdrag110)
                .build());
        }
        return oppdragsenhet120Liste;
    }

    private Avstemming115 buildAvstemming115() {
        return avst115Builder
            .medKodekomponent(ØkonomiKodeKomponent.VLFP.getKodeKomponent())
            .medNokkelAvstemming(LocalDateTime.now())
            .medTidspnktMelding(LocalDateTime.now().minusDays(1))
            .build();
    }

    private List<Oppdrag110> buildOppdrag110(Oppdragskontroll oppdragskontroll, Avstemming115 avstemming115, Boolean gjelderFP) {

        List<Oppdrag110> oppdrag110Liste = new ArrayList<>();

        Oppdrag110 oppdrag110_1 = oppdr110Builder
            .medKodeAksjon(ØkonomiKodeAksjon.TRE.getKodeAksjon())
            .medKodeEndring(ØkonomiKodeEndring.NY.name())
            .medKodeFagomrade(gjelderFP ? "FP" : "REFUTG")
            .medFagSystemId(44L)
            .medUtbetFrekvens(ØkonomiUtbetFrekvens.DAG.getUtbetFrekvens())
            .medOppdragGjelderId("22038235641")
            .medDatoOppdragGjelderFom(LocalDate.of(2000, 1, 1))
            .medSaksbehId("J5624215")
            .medOppdragskontroll(oppdragskontroll)
            .medAvstemming115(avstemming115)
            .build();

        oppdrag110Liste.add(oppdrag110_1);

        if (gjelderFP) {
            Oppdrag110 oppdrag110_2 = oppdr110Builder
                .medKodeAksjon(ØkonomiKodeAksjon.TRE.getKodeAksjon())
                .medKodeEndring(ØkonomiKodeEndring.NY.name())
                .medKodeFagomrade("FPREF")
                .medFagSystemId(55L)
                .medUtbetFrekvens(ØkonomiUtbetFrekvens.DAG.getUtbetFrekvens())
                .medOppdragGjelderId("22038235641")
                .medDatoOppdragGjelderFom(LocalDate.of(2000, 1, 1))
                .medSaksbehId("J5624215")
                .medOppdragskontroll(oppdragskontroll)
                .medAvstemming115(avstemming115)
                .build();

            oppdrag110Liste.add(oppdrag110_2);
            return oppdrag110Liste;
        }
        return oppdrag110Liste;
    }

    private Oppdragskontroll buildOppdragskontroll() {
        return oppdrkontrollBuilder
            .medBehandlingId(154L)
            .medSaksnummer(new Saksnummer("35"))
            .medVenterKvittering(Boolean.TRUE)
            .medProsessTaskId(56L)
            .medSimulering(false)
            .build();
    }
}
