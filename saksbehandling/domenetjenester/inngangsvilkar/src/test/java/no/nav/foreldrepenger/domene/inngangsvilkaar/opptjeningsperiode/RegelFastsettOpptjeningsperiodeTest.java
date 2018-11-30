package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjeningsperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;

import org.junit.Test;

import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.OpptjeningsPeriode;


public class RegelFastsettOpptjeningsperiodeTest {

    @Test
    public void skalFastsetteDatoLikUttaksDato() {
        // Arrange
        LocalDate førsteDagSøknad = LocalDate.of(2018, Month.JANUARY, 15);
        LocalDate førsteArbeidsgiverPeriodeDag = LocalDate.of(2018, Month.JANUARY, 19);
        LocalDate førsteSykemeldingDag = LocalDate.of(2018, Month.JANUARY, 17);
        OpptjeningsperiodeGrunnlag regelmodell = new OpptjeningsperiodeGrunnlag(førsteArbeidsgiverPeriodeDag, førsteDagSøknad, førsteSykemeldingDag);
        regelmodell.setPeriodeLengde(Period.parse("P10M"));

        // Act
        new RegelFastsettOpptjeningsperiode().evaluer(regelmodell, new OpptjeningsPeriode());

        // Assert
        assertThat(regelmodell.getSkjæringsdatoOpptjening()).isEqualTo(førsteDagSøknad);
    }

}
