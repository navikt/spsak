package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class AvklarAktivitetsPerioderOppdatererTest {


    @Test
    public void skal_sjekke_om_perioden_er_endret_hvis_orginal_periode_er_større() {
        AvklarAktivitetsPerioderOppdaterer oppdaterer = new AvklarAktivitetsPerioderOppdaterer();

        LocalDate idag = LocalDate.now();

        DatoIntervallEntitet beregnet = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusMonths(1), idag);
        DatoIntervallEntitet orginal = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusMonths(2), idag.plusMonths(1));

        DatoIntervallEntitet uendretIGUI = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusMonths(1), idag);
        assertThat(oppdaterer.erEndret(beregnet, uendretIGUI, orginal)).isFalse();
        DatoIntervallEntitet endretCase1 = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusDays(10), idag);
        assertThat(oppdaterer.erEndret(beregnet, endretCase1, orginal)).isTrue();
        DatoIntervallEntitet endretCase2 = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusMonths(1), idag.minusDays(10));
        assertThat(oppdaterer.erEndret(beregnet, endretCase2, orginal)).isTrue();
        DatoIntervallEntitet endretCase3 = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusDays(10), idag.minusDays(5));
        assertThat(oppdaterer.erEndret(beregnet, endretCase3, orginal)).isTrue();
    }

    @Test
    public void skal_sjekke_om_perioden_er_endret_hvis_orginal_periode_er_mindre() {
        AvklarAktivitetsPerioderOppdaterer oppdaterer = new AvklarAktivitetsPerioderOppdaterer();

        LocalDate idag = LocalDate.now();

        DatoIntervallEntitet beregnet = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusMonths(1), idag);
        DatoIntervallEntitet orginal = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusDays(20), idag.minusDays(5));

        DatoIntervallEntitet uendretIGUI = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusDays(20), idag.minusDays(5));
        assertThat(oppdaterer.erEndret(beregnet, uendretIGUI, orginal)).isFalse();
        DatoIntervallEntitet endretCase1 = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusDays(10), idag);
        assertThat(oppdaterer.erEndret(beregnet, endretCase1, orginal)).isTrue();
        DatoIntervallEntitet endretCase2 = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusMonths(1), idag.minusDays(10));
        assertThat(oppdaterer.erEndret(beregnet, endretCase2, orginal)).isTrue();
        DatoIntervallEntitet endretCase3 = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusDays(10), idag.minusDays(5));
        assertThat(oppdaterer.erEndret(beregnet, endretCase3, orginal)).isTrue();
    }

    @Test
    public void skal_sjekke_om_perioden_er_endret_hvis_orginal_periode_starte_inni_men_slutter_utenfor() {
        AvklarAktivitetsPerioderOppdaterer oppdaterer = new AvklarAktivitetsPerioderOppdaterer();

        LocalDate idag = LocalDate.now();

        DatoIntervallEntitet beregnet = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusMonths(1), idag);
        DatoIntervallEntitet orginal = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusDays(20), idag.plusDays(15));

        DatoIntervallEntitet uendretIGUI = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusDays(20), idag);
        assertThat(oppdaterer.erEndret(beregnet, uendretIGUI, orginal)).isFalse();
        DatoIntervallEntitet endretCase1 = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusDays(10), idag);
        assertThat(oppdaterer.erEndret(beregnet, endretCase1, orginal)).isTrue();
        DatoIntervallEntitet endretCase2 = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusMonths(1), idag.minusDays(10));
        assertThat(oppdaterer.erEndret(beregnet, endretCase2, orginal)).isTrue();
        DatoIntervallEntitet endretCase3 = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusDays(10), idag.minusDays(5));
        assertThat(oppdaterer.erEndret(beregnet, endretCase3, orginal)).isTrue();
    }

    @Test
    public void skal_sjekke_om_perioden_er_endret_hvis_orginal_periode_slutter_inni_men_starter_utenfor() {
        AvklarAktivitetsPerioderOppdaterer oppdaterer = new AvklarAktivitetsPerioderOppdaterer();

        LocalDate idag = LocalDate.now();

        DatoIntervallEntitet beregnet = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusMonths(1), idag);
        DatoIntervallEntitet orginal = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusMonths(2), idag.minusDays(10));

        DatoIntervallEntitet uendretIGUI = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusMonths(1), idag.minusDays(10));
        assertThat(oppdaterer.erEndret(beregnet, uendretIGUI, orginal)).isFalse();
        DatoIntervallEntitet endretCase1 = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusDays(10), idag);
        assertThat(oppdaterer.erEndret(beregnet, endretCase1, orginal)).isTrue();
        DatoIntervallEntitet endretCase2 = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusMonths(1), idag.minusDays(14));
        assertThat(oppdaterer.erEndret(beregnet, endretCase2, orginal)).isTrue();
        DatoIntervallEntitet endretCase3 = DatoIntervallEntitet.fraOgMedTilOgMed(idag.minusDays(10), idag.minusDays(5));
        assertThat(oppdaterer.erEndret(beregnet, endretCase3, orginal)).isTrue();
    }
}
