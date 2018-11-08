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
    public void første_uttaksdato_og_skjæringsdatoOpptjening_12_uker_før_fødsel() {
        // Arrange
        LocalDate terminDato = LocalDate.of(2018, Month.MAY, 1);
        LocalDate uttaksDato = LocalDate.of(2018, Month.FEBRUARY, 6);
        OpptjeningsperiodeGrunnlag regelmodell = opprettOpptjeningsperiodeGrunnlagForMorFødsel(terminDato, terminDato, uttaksDato);

        // Act
        new RegelFastsettOpptjeningsperiode().evaluer(regelmodell, new OpptjeningsPeriode());

        // Assert
        assertThat(regelmodell.getSkjæringsdatoOpptjening()).isEqualTo(uttaksDato);
    }

    @Test
    public void første_uttaksdato_13_uker_før_fødsel_skjæringsdatoOpptjening_12_uker_før_fødsel() {
        // Arrange
        LocalDate terminDato = LocalDate.of(2018, Month.MAY, 1);
        LocalDate uttaksDato = LocalDate.of(2018, Month.FEBRUARY, 5);
        OpptjeningsperiodeGrunnlag regelmodell = opprettOpptjeningsperiodeGrunnlagForMorFødsel(terminDato, terminDato, uttaksDato);

        // Act
        new RegelFastsettOpptjeningsperiode().evaluer(regelmodell, new OpptjeningsPeriode());

        // Assert
        LocalDate tidligsteLovligeUttaksdato = terminDato.minusWeeks(12);
        assertThat(regelmodell.getSkjæringsdatoOpptjening()).isEqualTo(tidligsteLovligeUttaksdato);
    }

    @Test
    public void skalFastsetteDatoLikTermindatoMinusTreUkerMF() {
        // Arrange
        LocalDate terminDato = LocalDate.of(2018, Month.FEBRUARY, 1);
        LocalDate uttaksDato = LocalDate.of(2018, Month.JANUARY, 15);
        OpptjeningsperiodeGrunnlag regelmodell = opprettOpptjeningsperiodeGrunnlagForMorFødsel(terminDato, terminDato, uttaksDato);

        // Act
        new RegelFastsettOpptjeningsperiode().evaluer(regelmodell, new OpptjeningsPeriode());

        // Assert
        assertThat(regelmodell.getSkjæringsdatoOpptjening()).isEqualTo(terminDato.minusWeeks(3));
        //assertThat(evaluation.getEvaluationProperties()).isNotEmpty();
    }

    @Test
    public void skalFastsetteDatoLikUttaksDatoMF() {
        // Arrange
        LocalDate terminDato = LocalDate.of(2018, Month.FEBRUARY, 1);
        LocalDate uttaksDato = LocalDate.of(2018, Month.JANUARY, 1);
        OpptjeningsperiodeGrunnlag regelmodell = opprettOpptjeningsperiodeGrunnlagForMorFødsel(terminDato, terminDato, uttaksDato);

        // Act
        new RegelFastsettOpptjeningsperiode().evaluer(regelmodell, new OpptjeningsPeriode());
        // Assert

        assertThat(regelmodell.getSkjæringsdatoOpptjening()).isEqualTo(uttaksDato);
    }

    @Test
    public void skalFastsetteDatoLikUttaksDatoFA() {
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

    @Test
    public void skalFastsetteDatoLikOmsorgsovertakelsesDatoFA() {
        // Arrange
        LocalDate omsorgsDato = LocalDate.of(2018, Month.FEBRUARY, 1);
        LocalDate uttaksDato = LocalDate.of(2018, Month.JANUARY, 15);
        OpptjeningsperiodeGrunnlag regelmodell = new OpptjeningsperiodeGrunnlag(FagsakÅrsak.ADOPSJON, SoekerRolle.FARA,
            uttaksDato, omsorgsDato, null);
        regelmodell.setPeriodeLengde(Period.parse("P10M"));

        // Act
        new RegelFastsettOpptjeningsperiode().evaluer(regelmodell, new OpptjeningsPeriode());
        // Assert
        assertThat(regelmodell.getSkjæringsdatoOpptjening()).isEqualTo(omsorgsDato);
    }

    @Test
    public void skalFastsetteDatoLikMorsMaksdatoPlusEnDagForFar() {
        // Arrange
        LocalDate fødselsdato = LocalDate.of(2018, Month.FEBRUARY, 1);
        LocalDate uttaksDato = LocalDate.of(2018, Month.DECEMBER, 15);
        LocalDate morsMaksDato = uttaksDato.minusDays(2);
        OpptjeningsperiodeGrunnlag regelmodell = new OpptjeningsperiodeGrunnlag(FagsakÅrsak.FØDSEL, SoekerRolle.FARA,
            uttaksDato, fødselsdato, fødselsdato);
        regelmodell.setPeriodeLengde(Period.parse("P10M"));
        regelmodell.setMorsMaksdato(morsMaksDato);

        // Act
        new RegelFastsettOpptjeningsperiode().evaluer(regelmodell, new OpptjeningsPeriode());
        // Assert
        assertThat(regelmodell.getSkjæringsdatoOpptjening()).isEqualTo(morsMaksDato.plusDays(1));
    }

    @Test
    public void skalFastsetteDatoLikFødselsdatoForFar() {
        // Arrange
        LocalDate fødselsdato = LocalDate.of(2018, Month.FEBRUARY, 1);
        LocalDate uttaksDato = LocalDate.of(2018, Month.DECEMBER, 15);
        LocalDate morsMaksDato = uttaksDato.plusWeeks(7);
        OpptjeningsperiodeGrunnlag regelmodell = new OpptjeningsperiodeGrunnlag(FagsakÅrsak.FØDSEL, SoekerRolle.FARA,
            uttaksDato, fødselsdato, fødselsdato);
        regelmodell.setPeriodeLengde(Period.parse("P10M"));
        regelmodell.setMorsMaksdato(morsMaksDato);

        // Act
        new RegelFastsettOpptjeningsperiode().evaluer(regelmodell, new OpptjeningsPeriode());
        // Assert
        assertThat(regelmodell.getSkjæringsdatoOpptjening()).isEqualTo(uttaksDato);
    }

    @Test
    public void skalFastsetteDatoLikFørsteUttaksdatoForFar() {
        // Arrange
        LocalDate fødselsdato = LocalDate.of(2018, Month.FEBRUARY, 1);
        LocalDate uttaksDato = fødselsdato.minusDays(1);
        OpptjeningsperiodeGrunnlag regelmodell = new OpptjeningsperiodeGrunnlag(FagsakÅrsak.FØDSEL, SoekerRolle.FARA,
            uttaksDato, fødselsdato, fødselsdato);
        regelmodell.setPeriodeLengde(Period.parse("P10M"));

        // Act
        new RegelFastsettOpptjeningsperiode().evaluer(regelmodell, new OpptjeningsPeriode());
        // Assert
        assertThat(regelmodell.getSkjæringsdatoOpptjening()).isEqualTo(fødselsdato);
    }


    private OpptjeningsperiodeGrunnlag opprettOpptjeningsperiodeGrunnlagForMorFødsel(LocalDate terminDato, LocalDate hendelsesDato, LocalDate uttaksDato) {
        OpptjeningsperiodeGrunnlag regelmodell = new OpptjeningsperiodeGrunnlag(FagsakÅrsak.FØDSEL, SoekerRolle.MORA,
            uttaksDato, hendelsesDato, terminDato);
        regelmodell.setPeriodeLengde(Period.parse("P10M"));
        regelmodell.setTidligsteUttakFørFødselPeriode(Period.parse("P12W"));
        return regelmodell;
    }
}
