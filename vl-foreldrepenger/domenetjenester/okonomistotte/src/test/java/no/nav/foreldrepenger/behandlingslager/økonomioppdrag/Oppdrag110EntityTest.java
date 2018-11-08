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

public class Oppdrag110EntityTest {
    private Oppdrag110.Builder oppdrag110Builder;
    private Oppdrag110 oppdrag110;
    private Oppdrag110 oppdrag110_2;

    private static final String KODEAKSJON = ØkonomiKodeAksjon.EN.name();
    private static final String KODEENDRING = ØkonomiKodeEndring.NY.name();
    private static final String KODEFAGOMRADE = ØkonomiKodeFagområde.REFUTG.name();
    private static final Long FAGSYSTEMID = 250L;
    private static final String UTBETFREKVENS = ØkonomiUtbetFrekvens.ENGANG.name();
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
        oppdrag110Builder = Oppdrag110.builder();
        oppdrag110 = null;
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        oppdrag110 = lagBuilderMedPaakrevdeFelter().build();

        assertThat(oppdrag110.getKodeAksjon()).isEqualTo(KODEAKSJON);
        assertThat(oppdrag110.getKodeEndring()).isEqualTo(KODEENDRING);
        assertThat(oppdrag110.getKodeFagomrade()).isEqualTo(KODEFAGOMRADE);
        assertThat(oppdrag110.getFagsystemId()).isEqualTo(FAGSYSTEMID);
        assertThat(oppdrag110.getUtbetFrekvens()).isEqualTo(UTBETFREKVENS);
        assertThat(oppdrag110.getOppdragGjelderId()).isEqualTo(OPPDRAGGJELDERID);
        assertThat(oppdrag110.getSaksbehId()).isEqualTo(SAKSBEHID);

    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        //mangler kodeAksjon
        try {
            oppdrag110Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("kodeAksjon");
        }

        //mangler kodeEndring
        oppdrag110Builder.medKodeAksjon(KODEAKSJON);
        try {
            oppdrag110Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("kodeEndring");
        }

        //mangler kodeFagomrade
        oppdrag110Builder.medKodeEndring(KODEENDRING);
        try {
            oppdrag110Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("kodeFagomrade");
        }

        //mangler fagsystemId
        oppdrag110Builder.medKodeFagomrade(KODEFAGOMRADE);
        try {
            oppdrag110Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("fagsystemId");
        }

        //mangler utbetFrekvens
        oppdrag110Builder.medFagSystemId(FAGSYSTEMID);
        try {
            oppdrag110Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("utbetFrekvens");
        }

        //mangler oppdragGjelderId
        oppdrag110Builder.medUtbetFrekvens(UTBETFREKVENS);
        try {
            oppdrag110Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("oppdragGjelderId");
        }

        //mangler oppdragGjelderId
        oppdrag110Builder.medOppdragGjelderId(OPPDRAGGJELDERID);
        try {
            oppdrag110Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("datoOppdragGjelderFom");
        }

        //mangler saksbehId
        oppdrag110Builder.medDatoOppdragGjelderFom(DATOOPPDRAGGJELDERFOM);
        try {
            oppdrag110Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("saksbehId");
        }

    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        oppdrag110 = lagBuilderMedPaakrevdeFelter().build();

        assertThat(oppdrag110).isNotNull();
        assertThat(oppdrag110).isNotEqualTo("blabla");
        assertThat(oppdrag110).isEqualTo(oppdrag110);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        oppdrag110Builder = lagBuilderMedPaakrevdeFelter();
        oppdrag110 = oppdrag110Builder.build();
        oppdrag110_2 = oppdrag110Builder.build();

        assertThat(oppdrag110).isEqualTo(oppdrag110_2);
        assertThat(oppdrag110_2).isEqualTo(oppdrag110);

        oppdrag110_2 = oppdrag110Builder.medKodeEndring(ØkonomiKodeEndring.ENDR.name()).build();
        assertThat(oppdrag110).isNotEqualTo(oppdrag110_2);
        assertThat(oppdrag110_2).isNotEqualTo(oppdrag110);
    }

    @Test
    public void skal_bruke_KodeEndring_i_equalsOgHashCode() {
        oppdrag110Builder = lagBuilderMedPaakrevdeFelter();
        oppdrag110 = oppdrag110Builder.build();
        oppdrag110Builder.medKodeEndring(ØkonomiKodeEndring.ENDR.name());
        oppdrag110_2 = oppdrag110Builder.build();

        assertThat(oppdrag110).isNotEqualTo(oppdrag110_2);
        assertThat(oppdrag110.hashCode()).isNotEqualTo(oppdrag110_2.hashCode());

    }

    @Test
    public void skal_bruke_FagsystemId_i_equalsOgHashCode() {
        oppdrag110Builder = lagBuilderMedPaakrevdeFelter();
        oppdrag110 = oppdrag110Builder.build();
        oppdrag110Builder.medFagSystemId(251L);
        oppdrag110_2 = oppdrag110Builder.build();

        assertThat(oppdrag110).isNotEqualTo(oppdrag110_2);
        assertThat(oppdrag110.hashCode()).isNotEqualTo(oppdrag110_2.hashCode());

    }

    @Test
    public void skal_bruke_SaksbehId_i_equalsOgHashCode() {
        oppdrag110Builder = lagBuilderMedPaakrevdeFelter();
        oppdrag110 = oppdrag110Builder.build();
        oppdrag110Builder.medFagSystemId(201L);
        oppdrag110_2 = oppdrag110Builder.build();

        assertThat(oppdrag110).isNotEqualTo(oppdrag110_2);
        assertThat(oppdrag110.hashCode()).isNotEqualTo(oppdrag110_2.hashCode());

    }


    private Oppdrag110.Builder lagBuilderMedPaakrevdeFelter() {
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
