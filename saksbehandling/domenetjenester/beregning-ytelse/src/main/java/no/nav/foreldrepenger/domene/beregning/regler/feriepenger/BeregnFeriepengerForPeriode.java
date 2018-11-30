package no.nav.foreldrepenger.domene.beregning.regler.feriepenger;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatPeriode;
import no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerPrÅr;
import no.nav.fpsak.tidsserie.LocalDateInterval;

class BeregnFeriepengerForPeriode {
    private static final BigDecimal FERIEPENGER_SATS_PROSENT = BigDecimal.valueOf(0.102);

    private BeregnFeriepengerForPeriode() {
    }

    static void beregn(Map<String, Object> resultater, List<BeregningsresultatPeriode> beregningsresultatPerioder, LocalDateInterval feriepengerPeriode) {
        beregningsresultatPerioder.stream()
            .filter(periode -> periode.getPeriode().overlaps(feriepengerPeriode))
            .forEach(periode -> {
                LocalDateInterval overlapp = periode.getPeriode().overlap(feriepengerPeriode).get();//NOSONAR
                int antallFeriepengerDager = beregnAntallUkedagerMellom(overlapp.getFomDato(), overlapp.getTomDato());
                LocalDate opptjeningÅr = overlapp.getFomDato().withMonth(12).withDayOfMonth(31);

                //Regelsporing
                String periodeNavn = "perioden " + overlapp;
                resultater.put("Antall feriepengedager i " + periodeNavn, antallFeriepengerDager);
                resultater.put("Opptjeningsår i " + periodeNavn, opptjeningÅr);

                periode.getBeregningsresultatAndelList().stream()
                    .filter(andel -> andel.getInntektskategori().erArbeidstakerEllerSjømann())
                    .forEach(andel -> {
                        long feriepengerGrunnlag = andel.getDagsats() * antallFeriepengerDager;
                        BigDecimal feriepengerAndelPrÅr = BigDecimal.valueOf(feriepengerGrunnlag).multiply(FERIEPENGER_SATS_PROSENT);
                        if (feriepengerAndelPrÅr.compareTo(BigDecimal.ZERO) == 0) {
                            return;
                        }
                        BeregningsresultatFeriepengerPrÅr.builder()
                            .medOpptjeningÅr(opptjeningÅr)
                            .medÅrsbeløp(feriepengerAndelPrÅr)
                            .build(andel);

                        //Regelsporing
                        String mottaker = andel.erBrukerMottaker() ? "Bruker." : "Arbeidsgiver.";
                        String andelId = andel.getArbeidsforhold() != null ? andel.getArbeidsforhold().getOrgnr() : andel.getAktivitetStatus().name();
                        resultater.put("Feriepenger." + mottaker + andelId + " i " + periodeNavn, feriepengerAndelPrÅr);
                    });
            });
    }

    private static int beregnAntallUkedagerMellom(LocalDate fom, LocalDate tom) {
        int antallUkedager = 0;
        for (LocalDate d = fom; !d.isAfter(tom); d = d.plusDays(1)) {
            int dag = d.getDayOfWeek().getValue();
            if (dag <= DayOfWeek.FRIDAY.getValue()) {
                antallUkedager++;
            }
        }
        return antallUkedager;
    }
}
