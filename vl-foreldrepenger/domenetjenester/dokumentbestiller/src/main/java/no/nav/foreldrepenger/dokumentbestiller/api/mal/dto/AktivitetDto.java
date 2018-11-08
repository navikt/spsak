package no.nav.foreldrepenger.dokumentbestiller.api.mal.dto;

public interface AktivitetDto extends Comparable<AktivitetDto> {

    boolean getGradering();

    int getUttaksgrad();

    long getDagsats();

    default int compareTo(AktivitetDto o) {
        int graderingResult = Boolean.compare(o.getGradering(), this.getGradering());
        if (graderingResult == 0) {
            return Integer.compare(o.getUttaksgrad(), this.getUttaksgrad());
        } else {
            return graderingResult;
        }
    }
}
