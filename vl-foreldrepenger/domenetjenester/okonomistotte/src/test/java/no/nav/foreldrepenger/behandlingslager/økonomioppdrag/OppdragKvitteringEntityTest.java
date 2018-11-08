package no.nav.foreldrepenger.behandlingslager.økonomioppdrag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeAksjon;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndring;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKomponent;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiUtbetFrekvens;

public class OppdragKvitteringEntityTest {

    private OppdragKvittering.Builder oppdragKvitteringBuilder;
    private OppdragKvittering oppdragKvittering;
    private OppdragKvittering oppdragKvittering_2;

    private static final String KODEAKSJON = ØkonomiKodeAksjon.EN.name();
    private static final String KODEENDRING = ØkonomiKodeEndring.NY.name();
    private static final String KODEFAGOMRADE = ØkonomiKodeFagområde.FP.name();
    private static final String ALVORLIGHETSGRAD = "00";
    private static final String BESKR_MELDING = "Beskr melding";
    private static final String MELDING_KODE = "Melding kode";
    private static final Long FAGSYSTEMID = 250L;
    private static final String UTBETFREKVENS = ØkonomiUtbetFrekvens.MÅNED.name();
    private static final String OPPDRAGGJELDERID = "1";
    private static final LocalDate DATOOPPDRAGGJELDERFOM = LocalDate.of(2000, 1, 1);
    private static final String SAKSBEHID = "Z1236525";
    private static final Long BEHANDLINGID = 321L;
    private static final Saksnummer SAKSID = new Saksnummer("700");
    private static final Boolean VENTERKVITTERING = true;
    private static final Long TASKID = 52L;
    private static final String FORVENTET_EXCEPTION = "forventet exception";

    @Before
    public void setup() {
        oppdragKvitteringBuilder = OppdragKvittering.builder();
        oppdragKvittering = null;
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        oppdragKvittering = lagOppdragKvitteringMedPaakrevdeFelter().build();

        assertThat(oppdragKvittering.getAlvorlighetsgrad()).isEqualTo(ALVORLIGHETSGRAD);
        assertThat(oppdragKvittering.getBeskrMelding()).isEqualTo(BESKR_MELDING);
        assertThat(oppdragKvittering.getMeldingKode()).isEqualTo(MELDING_KODE);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        //mangler oppdrag110
        try {
            oppdragKvitteringBuilder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("oppdrag110");
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        oppdragKvittering = lagOppdragKvitteringMedPaakrevdeFelter().build();

        assertThat(oppdragKvittering).isNotNull();
        assertThat(oppdragKvittering).isNotEqualTo("blabla");
        assertThat(oppdragKvittering).isEqualTo(oppdragKvittering);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        oppdragKvitteringBuilder = lagOppdragKvitteringMedPaakrevdeFelter();
        oppdragKvittering = oppdragKvitteringBuilder.build();
        oppdragKvittering_2 = oppdragKvitteringBuilder.build();

        assertThat(oppdragKvittering).isEqualTo(oppdragKvittering_2);
        assertThat(oppdragKvittering_2).isEqualTo(oppdragKvittering);

        oppdragKvittering_2 = oppdragKvitteringBuilder.medAlvorlighetsgrad("01").build();
        assertThat(oppdragKvittering).isNotEqualTo(oppdragKvittering_2);
        assertThat(oppdragKvittering_2).isNotEqualTo(oppdragKvittering);
    }

    @Test
    public void skal_bruke_MeldingKode_i_equalsOgHashCode() {
        oppdragKvitteringBuilder = lagOppdragKvitteringMedPaakrevdeFelter();
        oppdragKvittering = oppdragKvitteringBuilder.build();
        oppdragKvitteringBuilder.medMeldingKode("Melding kode 2");
        oppdragKvittering_2 = oppdragKvitteringBuilder.build();

        assertThat(oppdragKvittering).isNotEqualTo(oppdragKvittering_2);
        assertThat(oppdragKvittering.hashCode()).isNotEqualTo(oppdragKvittering_2.hashCode());

    }

    private OppdragKvittering.Builder lagOppdragKvitteringMedPaakrevdeFelter() {
        return OppdragKvittering.builder()
            .medAlvorlighetsgrad(ALVORLIGHETSGRAD)
            .medBeskrMelding(BESKR_MELDING)
            .medMeldingKode(MELDING_KODE)
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
