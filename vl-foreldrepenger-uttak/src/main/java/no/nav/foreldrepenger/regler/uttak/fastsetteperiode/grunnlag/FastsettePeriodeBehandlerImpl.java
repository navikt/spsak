package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FastsettePeriodeBehandlerImpl implements FastsettePeriodeBehandler {

    private FastsettePeriodeGrunnlag grunnlag;


    public FastsettePeriodeBehandlerImpl(FastsettePeriodeGrunnlag grunnlag) {
        this.grunnlag = grunnlag;
    }

    @Override
    public void innvilgAktuellPeriode(LocalDate knekkpunkt,
                                      Årsak innvilgetÅrsak,
                                      boolean avslåGradering,
                                      GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak,
                                      Arbeidsprosenter arbeidsprosenter,
                                      boolean utbetal) {
        if (knekkpunkt != null) {
            grunnlag.knekkPeriode(knekkpunkt, Perioderesultattype.INNVILGET, innvilgetÅrsak);
        } else {
            grunnlag.hentPeriodeUnderBehandling().setPerioderesultattype(Perioderesultattype.INNVILGET);
            grunnlag.hentPeriodeUnderBehandling().setÅrsak(innvilgetÅrsak);
        }
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        if (avslåGradering) {
            uttakPeriode.opphevGradering(graderingIkkeInnvilgetÅrsak);
        }
        trekkSaldo();
        if (uttakPeriode instanceof UtsettelsePeriode) {
            oppdaterUtbetalingsgrad(uttakPeriode, arbeidsprosenter, false);
        } else {
            oppdaterUtbetalingsgrad(uttakPeriode, arbeidsprosenter, utbetal);
        }
    }

    @Override
    public void avslåAktuellPeriode(LocalDate knekkpunkt, Årsak årsak, Arbeidsprosenter arbeidsprosenter, boolean trekkDagerFraSaldo, boolean utbetal) {
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        if (knekkpunkt != null) {
            grunnlag.knekkPeriode(knekkpunkt, Perioderesultattype.AVSLÅTT, årsak);
            uttakPeriode = grunnlag.hentPeriodeUnderBehandling(); //Henter periode under behandling på nytt pga knekk.
        } else {
            uttakPeriode.setPerioderesultattype(Perioderesultattype.AVSLÅTT);
        }
        uttakPeriode.setÅrsak(årsak);
        if (trekkDagerFraSaldo) {
            trekkSaldo();
            oppdaterUtbetalingsgrad(uttakPeriode, arbeidsprosenter, utbetal);
        } else {
            oppdaterUtbetalingsgrad(uttakPeriode, arbeidsprosenter, utbetal);
        }
    }

    @Override
    public void manuellBehandling(Manuellbehandlingårsak manuellbehandlingårsak, Årsak ikkeOppfyltÅrsak, Arbeidsprosenter arbeidsprosenter, boolean utbetal) {
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        uttakPeriode.setPerioderesultattype(Perioderesultattype.MANUELL_BEHANDLING);
        uttakPeriode.setManuellbehandlingårsak(manuellbehandlingårsak);
        uttakPeriode.setÅrsak(ikkeOppfyltÅrsak);

        oppdaterUtbetalingsgrad(uttakPeriode, arbeidsprosenter, utbetal);
    }

    @Override
    public void manuellBehandling() {
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        uttakPeriode.setPerioderesultattype(Perioderesultattype.MANUELL_BEHANDLING);
    }

    private void oppdaterUtbetalingsgrad(UttakPeriode uttakPeriode, Arbeidsprosenter arbeidsprosenter, boolean utbetal) {
        for (AktivitetIdentifikator aktivitet : grunnlag.getAktiviteter()) {
            final BigDecimal utbetalingsgrad;
            if (utbetal) {
                UtbetalingsprosentUtregning utregning = bestemUtregning(uttakPeriode, aktivitet, arbeidsprosenter);
                utbetalingsgrad = utregning.resultat();
            } else {
                utbetalingsgrad = BigDecimal.ZERO;
            }
            uttakPeriode.setUtbetalingsgrad(aktivitet, utbetalingsgrad);
        }
    }

    private UtbetalingsprosentUtregning bestemUtregning(UttakPeriode uttakPeriode,
                                                                    AktivitetIdentifikator aktivitet,
                                                                    Arbeidsprosenter arbeidsprosenter) {
        if (erSøktOmGradering(uttakPeriode, aktivitet)) {
            return new UtbetalingsprosentMedGraderingUtregning(arbeidsprosenter, aktivitet, uttakPeriode);
        }
        return new UtbetalingsprosentUtenGraderingUtregning(arbeidsprosenter, aktivitet, uttakPeriode);
    }

    private boolean erSøktOmGradering(UttakPeriode uttakPeriode, AktivitetIdentifikator aktivitet) {
        //Skal ha samme utregning av utbetalingsprosent uansett om gradering i perioden er innvilget eller ikke
        return uttakPeriode.harGradering(aktivitet) || uttakPeriode.getGraderingIkkeInnvilgetÅrsak() != null;
    }

    @Override
    public FastsettePeriodeGrunnlag grunnlag() {
        return grunnlag;
    }

    private void trekkSaldo() {
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        trekkSaldo(uttakPeriode);
    }

    private void trekkSaldo(UttakPeriode uttakPeriode) {
        UttakPeriode periode = grunnlag.hentPeriodeUnderBehandling();
        grunnlag.getTrekkdagertilstand().reduserSaldo(uttakPeriode);
        if (grunnlag.getTrekkdagertilstand().harNegativSaldo()) {
            periode.setPerioderesultattype(Perioderesultattype.MANUELL_BEHANDLING);
        }
    }

}
