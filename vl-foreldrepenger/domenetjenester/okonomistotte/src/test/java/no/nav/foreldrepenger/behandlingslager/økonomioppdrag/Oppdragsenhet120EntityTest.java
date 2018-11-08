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

public class Oppdragsenhet120EntityTest {
    private Oppdragsenhet120.Builder oppdragsenhet120Builder;
    private Oppdragsenhet120 oppdragsenhet120;
    private Oppdragsenhet120 oppdragsenhet120_2;

    private static final String TYPEENHET = "BOS";
    private static final String ENHET = "8020";
    private static final LocalDate DATOENHETFOM = LocalDate.now().minusDays(1);
    private static final String KODEAKSJON = ØkonomiKodeAksjon.EN.name();
    private static final String KODEENDRING = ØkonomiKodeEndring.NY.name();
    private static final String KODEFAGOMRADE = ØkonomiKodeFagområde.REFUTG.name();
    private static final Long FAGSYSTEMID = 250L;
    private static final String UTBETFREKVENS = ØkonomiUtbetFrekvens.ENGANG.name();
    private static final String OPPDRAGGJELDERID = "1";
    private static final LocalDate DATOOPPDRAGGJELDERFOM = LocalDate.of(2000, 1, 1);
    private static final String SAKSBEHID = "Z1236524";
    private static final Saksnummer SAKSID = new Saksnummer("700");
    private static final Boolean VENTERKVITTERING = true;
    private static final Long TASKID = 52L;
    private static final String FORVENTET_EXCEPTION = "forventet exception";
    private static final Long BEHANDLINGID = 321L;

    @Before
    public void setup() {
        oppdragsenhet120Builder = Oppdragsenhet120.builder();
        oppdragsenhet120 = null;
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        oppdragsenhet120 = lagBuilderMedPaakrevdeFelter().build();

        assertThat(oppdragsenhet120.getTypeEnhet()).isEqualTo(TYPEENHET);
        assertThat(oppdragsenhet120.getEnhet()).isEqualTo(ENHET);
        assertThat(oppdragsenhet120.getDatoEnhetFom()).isEqualTo(DATOENHETFOM);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        //mangler typeEnhet
        try {
            oppdragsenhet120Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("typeEnhet");
        }

        //mangler enhet
        oppdragsenhet120Builder.medTypeEnhet(TYPEENHET);
        try {
            oppdragsenhet120Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("enhet");
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        oppdragsenhet120 = lagBuilderMedPaakrevdeFelter().build();

        assertThat(oppdragsenhet120).isNotNull();
        assertThat(oppdragsenhet120).isNotEqualTo("blabla");
        assertThat(oppdragsenhet120).isEqualTo(oppdragsenhet120);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        oppdragsenhet120Builder = lagBuilderMedPaakrevdeFelter();
        oppdragsenhet120 = oppdragsenhet120Builder.build();
        oppdragsenhet120_2 = oppdragsenhet120Builder.build();

        assertThat(oppdragsenhet120).isEqualTo(oppdragsenhet120_2);
        assertThat(oppdragsenhet120_2).isEqualTo(oppdragsenhet120);

        oppdragsenhet120_2 = oppdragsenhet120Builder.medDatoEnhetFom(LocalDate.now().minusDays(5)).build();
        assertThat(oppdragsenhet120).isNotEqualTo(oppdragsenhet120_2);
        assertThat(oppdragsenhet120_2).isNotEqualTo(oppdragsenhet120);
    }


    @Test
    public void skal_bruke_TypeEnhet_i_equalsOgHashCode() {
        oppdragsenhet120Builder = lagBuilderMedPaakrevdeFelter();
        oppdragsenhet120 = oppdragsenhet120Builder.build();
        oppdragsenhet120Builder.medTypeEnhet("AOS");
        oppdragsenhet120_2 = oppdragsenhet120Builder.build();

        assertThat(oppdragsenhet120).isNotEqualTo(oppdragsenhet120_2);
        assertThat(oppdragsenhet120.hashCode()).isNotEqualTo(oppdragsenhet120_2.hashCode());

    }

    @Test
    public void skal_bruke_Enhet_i_equalsOgHashCode() {
        oppdragsenhet120Builder = lagBuilderMedPaakrevdeFelter();
        oppdragsenhet120 = oppdragsenhet120Builder.build();
        oppdragsenhet120Builder.medEnhet("8021");
        oppdragsenhet120_2 = oppdragsenhet120Builder.build();

        assertThat(oppdragsenhet120).isNotEqualTo(oppdragsenhet120_2);
        assertThat(oppdragsenhet120.hashCode()).isNotEqualTo(oppdragsenhet120_2.hashCode());

    }

    @Test
    public void skal_bruke_DatoEnhetFom_i_equalsOgHashCode() {
        oppdragsenhet120Builder = lagBuilderMedPaakrevdeFelter();
        oppdragsenhet120 = oppdragsenhet120Builder.build();
        oppdragsenhet120Builder.medDatoEnhetFom(LocalDate.now().minusDays(10));
        oppdragsenhet120_2 = oppdragsenhet120Builder.build();

        assertThat(oppdragsenhet120).isNotEqualTo(oppdragsenhet120_2);
        assertThat(oppdragsenhet120.hashCode()).isNotEqualTo(oppdragsenhet120_2.hashCode());
    }


    private Oppdragsenhet120.Builder lagBuilderMedPaakrevdeFelter() {
        return Oppdragsenhet120.builder()
            .medTypeEnhet(TYPEENHET)
            .medEnhet(ENHET)
            .medDatoEnhetFom(DATOENHETFOM)
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
