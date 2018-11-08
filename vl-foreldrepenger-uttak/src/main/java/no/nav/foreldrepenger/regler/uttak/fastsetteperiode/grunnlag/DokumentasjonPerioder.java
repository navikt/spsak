package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.ArrayList;
import java.util.List;

class DokumentasjonPerioder {
    private List<GyldigGrunnPeriode> gyldigGrunnPerioder = new ArrayList<>();
    private List<PeriodeUtenOmsorg> perioderUtenOmsorg = new ArrayList<>();
    private List<PeriodeMedAleneomsorg> perioderMedAleneomsorg = new ArrayList<>();
    private List<PeriodeMedFerie> perioderMedFerie = new ArrayList<>();
    private List<PeriodeMedSykdomEllerSkade> perioderMedSykdomEllerSkade = new ArrayList<>();
    private List<PeriodeMedInnleggelse> perioderMedInnleggelse = new ArrayList<>();
    private List<PeriodeMedBarnInnlagt> perioderMedBarnInnlagt = new ArrayList<>();

    List<GyldigGrunnPeriode> getGyldigGrunnPerioder() {
        return gyldigGrunnPerioder;
    }

    void setGyldigGrunnPerioder(List<GyldigGrunnPeriode> gyldigGrunnPerioder) {
        this.gyldigGrunnPerioder = gyldigGrunnPerioder;
    }

    List<PeriodeUtenOmsorg> getPerioderUtenOmsorg() {
        return perioderUtenOmsorg;
    }

    void setPerioderUtenOmsorg(List<PeriodeUtenOmsorg> perioderUtenOmsorg) {
        this.perioderUtenOmsorg = perioderUtenOmsorg;
    }

    List<PeriodeMedAleneomsorg> getPerioderMedAleneomsorg() {
        return perioderMedAleneomsorg;
    }

    void setPerioderMedAleneomsorg(List<PeriodeMedAleneomsorg> perioderMedAleneomsorg) {
        this.perioderMedAleneomsorg = perioderMedAleneomsorg;
    }

    List<PeriodeMedFerie> getPerioderMedFerie() {
        return perioderMedFerie;
    }

    void setPerioderMedFerie(List<PeriodeMedFerie> perioderMedFerie) {
        this.perioderMedFerie = perioderMedFerie;
    }

    List<PeriodeMedSykdomEllerSkade> getPerioderMedSykdomEllerSkade() {
        return perioderMedSykdomEllerSkade;
    }

    void setPerioderMedSykdomEllerSkade(List<PeriodeMedSykdomEllerSkade> perioderMedSykdomEllerSkade) {
        this.perioderMedSykdomEllerSkade = perioderMedSykdomEllerSkade;
    }

    List<PeriodeMedInnleggelse> getPerioderMedInnleggelse() {
        return perioderMedInnleggelse;
    }

    void setPerioderMedInnleggelse(List<PeriodeMedInnleggelse> perioderMedInnleggelse) {
        this.perioderMedInnleggelse = perioderMedInnleggelse;
    }

    List<PeriodeMedBarnInnlagt> getPerioderMedBarnInnlagt() {
        return perioderMedBarnInnlagt;
    }

    void setPerioderMedBarnInnlagt(List<PeriodeMedBarnInnlagt> perioderMedBarnInnlagt) {
        this.perioderMedBarnInnlagt = perioderMedBarnInnlagt;
    }
}
