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

public class Oppdragslinje150EntityTest {
    private Oppdragslinje150.Builder oppdragslinje150Builder;
    private Oppdragslinje150 oppdragslinje150;
    private Oppdragslinje150 oppdragslinje150_2;

    private static final String KODEENDRINGLINJE = "NY";
    private static final String KODESTATUSLINJE = TkodeStatusLinje.OPPH.name();
    private static final LocalDate DATOSTATUSFOM = LocalDate.now().minusDays(15);
    private static final String VEDTAKID = "457";
    private static final Long DELYTELSEID = 300L;
    private static final String KODEKLASSIFIK = "FPENFOD-OP";
    private static final LocalDate DATOVEDTAKFOM = LocalDate.now().minusDays(10);
    private static final LocalDate DATOVEDTAKTOM = LocalDate.now().minusDays(8);
    private static final long SATS = 50000L;
    private static final String FRADRAGTILLEGG = TfradragTillegg.T.name();
    private static final String TYPESATS = "ENG";
    private static final String BRUKKJOREPLAN = "N";
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
    private static final String FORVENTET_EXCEPTION = "forventet exception";
    private static final Long BEHANDLINGID = 321L;

    @Before
    public void setup() {
        oppdragslinje150Builder = Oppdragslinje150.builder();
        oppdragslinje150 = null;
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        oppdragslinje150 = lagBuilderMedPaakrevdeFelter().build();

        assertThat(oppdragslinje150.getKodeEndringLinje()).isEqualTo(KODEENDRINGLINJE);
        assertThat(oppdragslinje150.getKodeStatusLinje()).isEqualTo(KODESTATUSLINJE);
        assertThat(oppdragslinje150.getDatoStatusFom()).isEqualTo(DATOSTATUSFOM);
        assertThat(oppdragslinje150.getVedtakId()).isEqualTo(VEDTAKID);
        assertThat(oppdragslinje150.getDelytelseId()).isEqualTo(DELYTELSEID);
        assertThat(oppdragslinje150.getKodeKlassifik()).isEqualTo(KODEKLASSIFIK);
        assertThat(oppdragslinje150.getDatoVedtakFom()).isEqualTo(DATOVEDTAKFOM);
        assertThat(oppdragslinje150.getDatoVedtakTom()).isEqualTo(DATOVEDTAKTOM);
        assertThat(oppdragslinje150.getSats()).isEqualTo(SATS);
        assertThat(oppdragslinje150.getFradragTillegg()).isEqualTo(FRADRAGTILLEGG);
        assertThat(oppdragslinje150.getTypeSats()).isEqualTo(TYPESATS);
        assertThat(oppdragslinje150.getBrukKjoreplan()).isEqualTo(BRUKKJOREPLAN);
        assertThat(oppdragslinje150.getSaksbehId()).isEqualTo(SAKSBEHID);
        assertThat(oppdragslinje150.getUtbetalesTilId()).isEqualTo(UTBETALESTILID);
        assertThat(oppdragslinje150.getRefFagsystemId()).isEqualTo(REFFAGSYSTEMID);
        assertThat(oppdragslinje150.getRefDelytelseId()).isEqualTo(REFDELYTELSEID);

    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        //mangler kodeEndringLinje
        try {
            oppdragslinje150Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("kodeEndringLinje");
        }

        //mangler kodeKlassifik
        oppdragslinje150Builder.medKodeEndringLinje(KODEENDRINGLINJE);
        try {
            oppdragslinje150Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("kodeKlassifik");
        }

        //mangler datoVedtakFom
        oppdragslinje150Builder.medKodeKlassifik(KODEKLASSIFIK);
        try {
            oppdragslinje150Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("vedtakPeriode");
        }

        //mangler sats
        oppdragslinje150Builder.medVedtakFomOgTom(DATOVEDTAKFOM, DATOVEDTAKTOM);
        try {
            oppdragslinje150Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("sats");
        }

    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        oppdragslinje150 = lagBuilderMedPaakrevdeFelter().build();

        assertThat(oppdragslinje150).isNotNull();
        assertThat(oppdragslinje150).isNotEqualTo("blabla");
        assertThat(oppdragslinje150).isEqualTo(oppdragslinje150);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        oppdragslinje150Builder = lagBuilderMedPaakrevdeFelter();
        oppdragslinje150 = oppdragslinje150Builder.build();
        oppdragslinje150_2 = oppdragslinje150Builder.build();


        assertThat(oppdragslinje150).isEqualTo(oppdragslinje150_2);
        assertThat(oppdragslinje150_2).isEqualTo(oppdragslinje150);

        oppdragslinje150_2 = oppdragslinje150Builder.medKodeKlassifik("FPENAD-OP").build();
        assertThat(oppdragslinje150).isNotEqualTo(oppdragslinje150_2);
        assertThat(oppdragslinje150_2).isNotEqualTo(oppdragslinje150);
    }


    @Test
    public void skal_bruke_KodeKlassifik_i_equalsOgHashCode() {
        oppdragslinje150Builder = lagBuilderMedPaakrevdeFelter();
        oppdragslinje150 = oppdragslinje150Builder.build();
        oppdragslinje150Builder.medKodeKlassifik("FPENAD-OP");
        oppdragslinje150_2 = oppdragslinje150Builder.build();

        assertThat(oppdragslinje150).isNotEqualTo(oppdragslinje150_2);
        assertThat(oppdragslinje150.hashCode()).isNotEqualTo(oppdragslinje150_2.hashCode());

    }

    @Test
    public void skal_bruke_KodeEndringLinje_i_equalsOgHashCode() {
        oppdragslinje150Builder = lagBuilderMedPaakrevdeFelter();
        oppdragslinje150 = oppdragslinje150Builder.build();
        oppdragslinje150Builder.medKodeEndringLinje("ENDR");
        oppdragslinje150_2 = oppdragslinje150Builder.build();

        assertThat(oppdragslinje150).isNotEqualTo(oppdragslinje150_2);
        assertThat(oppdragslinje150.hashCode()).isNotEqualTo(oppdragslinje150_2.hashCode());

    }

    @Test
    public void skal_bruke_KodeStatusLinje_i_equalsOgHashCode() {
        oppdragslinje150Builder = lagBuilderMedPaakrevdeFelter();
        oppdragslinje150 = oppdragslinje150Builder.build();
        oppdragslinje150Builder.medKodeStatusLinje(TkodeStatusLinje.HVIL.name());
        oppdragslinje150_2 = oppdragslinje150Builder.build();

        assertThat(oppdragslinje150).isNotEqualTo(oppdragslinje150_2);
        assertThat(oppdragslinje150.hashCode()).isNotEqualTo(oppdragslinje150_2.hashCode());

    }

    private Oppdragslinje150.Builder lagBuilderMedPaakrevdeFelter() {

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
            .medHenvisning(43L)
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
