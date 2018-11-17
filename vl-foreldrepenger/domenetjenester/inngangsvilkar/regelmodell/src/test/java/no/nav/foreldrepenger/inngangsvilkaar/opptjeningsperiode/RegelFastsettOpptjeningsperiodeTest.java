package no.nav.foreldrepenger.inngangsvilkaar.opptjeningsperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;

import org.junit.Test;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.FagsakÅrsak;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.SoekerRolle;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjeningsPeriode;

public class RegelFastsettOpptjeningsperiodeTest {

    @Test
    public void skalFastsetteDatoLikUttaksDato() {
        // Arrange
        LocalDate omsorgsDato = LocalDate.of(2018, Month.JANUARY, 15);
        LocalDate uttaksDato = LocalDate.of(2018, Month.FEBRUARY, 1);
        OpptjeningsperiodeGrunnlag regelmodell = new OpptjeningsperiodeGrunnlag(FagsakÅrsak.ADOPSJON, SoekerRolle.FARA,
            uttaksDato, omsorgsDato, null);
        regelmodell.setPeriodeLengde(Period.parse("P10M"));

        // Act
        new RegelFastsettOpptjeningsperiode().evaluer(regelmodell, new OpptjeningsPeriode());

        // Assert
        assertThat(regelmodell.getSkjæringsdatoOpptjening()).isEqualTo(uttaksDato);
    }

}
