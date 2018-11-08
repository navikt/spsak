package no.nav.foreldrepenger.behandlingslager.økonomioppdrag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TfradragTillegg;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TkodeStatusLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeAksjon;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndring;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKomponent;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiUtbetFrekvens;

public class Attestant180EntityTest {
    private Attestant180.Builder attestant180Builder;
    private Attestant180 attestant180;
    private Attestant180 attestant180_2;
    private static final String ATTESTANT_ID = "H4567656";
    private static final String FORVENTET_EXCEPTION = "forventet exception";
    private static final String KODEENDRINGLINJE = "NY";
    private static final String KODESTATUSLINJE = TkodeStatusLinje.OPPH.name();
    private static final LocalDate DATOSTATUSFOM = LocalDate.now().minusDays(15);
    private static final String VEDTAKID = "456";
    private static final Long DELYTELSEID = 300L;
    private static final String KODEKLASSIFIK = "FPENFOD-OP";
    private static final LocalDate DATOVEDTAKFOM = LocalDate.now().minusDays(10);
    private static final LocalDate DATOVEDTAKTOM = LocalDate.now().minusDays(8);
    private static final Long SATS = 50000L;
    private static final String FRADRAGTILLEGG = TfradragTillegg.T.name();
    private static final String TYPESATS = "ENG";
    private static final String BRUKKJOREPLAN = "J";
    private static final String SAKSBEHID = "Z1236524";
    private static final LocalDate DATOOPPDRAGGJELDERFOM = LocalDate.of(2000, 1, 1);
    private static final String UTBETALESTILID = "456";
    private static final Long REFFAGSYSTEMID = 678L;
    private static final Long REFDELYTELSEID = 789L;
    private static final String KODEAKSJON = ØkonomiKodeAksjon.EN.name();
    private static final String KODEENDRING = ØkonomiKodeEndring.NY.name();
    private static final String KODEFAGOMRADE = ØkonomiKodeFagområde.REFUTG.name();
    private static final Long FAGSYSTEMID = 250L;
    private static final String UTBETFREKVENS = ØkonomiUtbetFrekvens.ENGANG.name();
    private static final String OPPDRAGGJELDERID = "1";
    private static final Saksnummer SAKSID = new Saksnummer("700");
    private static final Boolean VENTERKVITTERING = true;
    private static final Long TASKID = 52L;
    private static final Long BEHANDLINGID = 321L;

    @Before
    public void setup() {
        attestant180Builder = Attestant180.builder();
        attestant180 = null;
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        attestant180 = lagBuilderMedPaakrevdeFelter().build();

        assertThat(attestant180.getAttestantId()).isEqualTo(ATTESTANT_ID);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {

        try {
            attestant180Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("attestantId");
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        attestant180 = lagBuilderMedPaakrevdeFelter().build();

        assertThat(attestant180).isNotNull();
        assertThat(attestant180).isNotEqualTo("blabla");
        assertThat(attestant180).isEqualTo(attestant180);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        attestant180Builder = lagBuilderMedPaakrevdeFelter();
        attestant180 = attestant180Builder.build();
        attestant180_2 = attestant180Builder.build();

        assertThat(attestant180).isEqualTo(attestant180_2);
        assertThat(attestant180_2).isEqualTo(attestant180);

        attestant180_2 = attestant180Builder.medAttestantId("J3768765").build();
        assertThat(attestant180).isNotEqualTo(attestant180_2);
        assertThat(attestant180_2).isNotEqualTo(attestant180);
    }


    @Test
    public void skal_bruke_AttestantId_i_equalsOgHashCode() {
        attestant180Builder = lagBuilderMedPaakrevdeFelter();
        attestant180 = attestant180Builder.build();
        attestant180Builder.medAttestantId("I7678765");
        attestant180_2 = attestant180Builder.build();

        assertThat(attestant180).isNotEqualTo(attestant180_2);
        assertThat(attestant180.hashCode()).isNotEqualTo(attestant180_2.hashCode());

    }


    // ----------------------------------------------------------

    private Attestant180.Builder lagBuilderMedPaakrevdeFelter() {
        return Attestant180.builder()
            .medAttestantId(ATTESTANT_ID)
            .medOppdragslinje150(lagOppdragslinje150MedPaakrevdeFelter().build());
    }

    private Oppdragslinje150.Builder lagOppdragslinje150MedPaakrevdeFelter() {
        return Oppdragslinje150.builder()
            .medKodeEndringLinje(KODEENDRINGLINJE)
            .medKodeStatusLinje(KODESTATUSLINJE)
            .medDatoStatusFom(DATOSTATUSFOM)
            .medVedtakId(VEDTAKID)
            .medDelytelseId(DELYTELSEID)
            .medKodeKlassifik(KODEKLASSIFIK)
            .medVedtakFomOgTom(DATOVEDTAKFOM, DATOVEDTAKTOM)
            .medSats(SATS)
            .medFradragTillegg(FRADRAGTILLEGG)
            .medTypeSats(TYPESATS)
            .medBrukKjoreplan(BRUKKJOREPLAN)
            .medSaksbehId(SAKSBEHID)
            .medUtbetalesTilId(UTBETALESTILID)
            .medRefFagsystemId(REFFAGSYSTEMID)
            .medRefDelytelseId(REFDELYTELSEID)
            .medHenvisning(21L)
            .medOppdrag110(lagOppdrag110MedPaakrevdeFelter().build());
    }

    private Oppdrag110.Builder lagOppdrag110MedPaakrevdeFelter() {
        return Oppdrag110.builder()
            .medKodeAksjon(KODEAKSJON)
            .medKodeEndring(KODEENDRING)
            .medKodeFagomrade(KODEFAGOMRADE)
            .medFagSystemId(FAGSYSTEMID)
            .medUtbetFrekvens(UTBETFREKVENS)
            .medOppdragGjelderId(OPPDRAGGJELDERID)
            .medDatoOppdragGjelderFom(DATOOPPDRAGGJELDERFOM)
            .medSaksbehId(SAKSBEHID)
            .medOppdragskontroll(lagOppdragskontrollMedPaakrevdeFelter().build())
            .medAvstemming115(lagAvstemming115MedPaakrevdeFelter().build());
    }

    private Oppdragskontroll.Builder lagOppdragskontrollMedPaakrevdeFelter() {
        return Oppdragskontroll.builder()
            .medBehandlingId(BEHANDLINGID)
            .medSaksnummer(SAKSID)
            .medVenterKvittering(VENTERKVITTERING)
            .medProsessTaskId(TASKID)
            .medSimulering(false);
    }

    private Avstemming115.Builder lagAvstemming115MedPaakrevdeFelter() {
        return Avstemming115.builder()
            .medKodekomponent(ØkonomiKodeKomponent.VLFP.getKodeKomponent())
            .medNokkelAvstemming(LocalDateTime.now())
            .medTidspnktMelding(LocalDateTime.now());
    }
}
