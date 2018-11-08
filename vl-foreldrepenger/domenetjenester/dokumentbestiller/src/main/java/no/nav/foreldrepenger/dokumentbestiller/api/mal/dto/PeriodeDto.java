package no.nav.foreldrepenger.dokumentbestiller.api.mal.dto;

import no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PeriodeDto implements Comparable<PeriodeDto>, Serializable {
    private static final long serialVersionUID = -1502390686161078719L;
    private boolean innvilget;
    private String årsak;
    private String periodeFom;
    private String periodeTom;
    private int antallTapteDager;
    private List<ArbeidsforholdDto> arbeidsforhold = new ArrayList<>();
    private NæringDto næring;
    private List<AnnenAktivitetDto> annenAktivitet = new ArrayList<>();
    private boolean graderingFinnes;

    public boolean isGraderingFinnes() {
        return graderingFinnes;
    }

    public boolean getInnvilget() {
        return innvilget;
    }

    public void setInnvilget(boolean innvilget) {
        this.innvilget = innvilget;
    }

    public String getÅrsak() {
        return årsak;
    }

    public void setÅrsak(String årsak) {
        this.årsak = årsak;
    }

    public String getPeriodeFom() {
        return periodeFom;
    }

    public void setPeriodeFom(String periodeFom) {
        this.periodeFom = periodeFom;
    }

    public String getPeriodeTom() {
        return periodeTom;
    }

    public void setPeriodeTom(String periodeTom) {
        this.periodeTom = periodeTom;
    }

    public int getAntallTapteDager() {
        return antallTapteDager;
    }

    public void setAntallTapteDager(int antallTapteDager) {
        this.antallTapteDager = antallTapteDager;
    }

    public List<ArbeidsforholdDto> getArbeidsforhold() {
        return arbeidsforhold.stream().sorted().collect(Collectors.toList());
    }

    public void setArbeidsforhold(List<ArbeidsforholdDto> arbeidsforhold) {
        this.arbeidsforhold = arbeidsforhold;
    }

    public void leggTilArbeidsforhold(ArbeidsforholdDto nytt) {
        if (nytt.getGradering()) graderingFinnes = true;
        this.arbeidsforhold.add(nytt);
    }

    public NæringDto getNæring() {
        return næring;
    }

    public void setNæring(NæringDto næring) {
        this.næring = næring;
        if (næring.getGradering()) graderingFinnes = true;
    }

    public List<AnnenAktivitetDto> getAnnenAktivitet() {
        return annenAktivitet.stream().sorted().collect(Collectors.toList());
    }

    public void leggTilAnnenAktivitet(AnnenAktivitetDto nytt) {
        if (nytt.getGradering()) graderingFinnes = true;
        this.annenAktivitet.add(nytt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeriodeDto that = (PeriodeDto) o;
        return innvilget == that.innvilget &&
            antallTapteDager == that.antallTapteDager &&
            graderingFinnes == that.graderingFinnes &&
            Objects.equals(årsak, that.årsak) &&
            Objects.equals(periodeFom, that.periodeFom) &&
            Objects.equals(periodeTom, that.periodeTom) &&
            Objects.equals(arbeidsforhold, that.arbeidsforhold) &&
            Objects.equals(næring, that.næring) &&
            Objects.equals(annenAktivitet, that.annenAktivitet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(innvilget, årsak, periodeFom, periodeTom, antallTapteDager, arbeidsforhold, næring, annenAktivitet, graderingFinnes);
    }

    @Override
    public String toString() {
        return "PeriodeDto{" +
            "innvilget=" + innvilget +
            ", årsak='" + årsak + '\'' +
            ", periodeFom='" + periodeFom + '\'' +
            ", periodeTom='" + periodeTom + '\'' +
            ", antallTapteDager=" + antallTapteDager +
            ", arbeidsforhold=" + arbeidsforhold +
            ", næring=" + næring +
            ", annenAktivitet=" + annenAktivitet +
            '}';
    }

    @Override
    public int compareTo(PeriodeDto o) {
        return DokumentTypeFelles.finnDatoVerdiAvUtenTidSone(this.getPeriodeFom()).compare(DokumentTypeFelles.finnDatoVerdiAvUtenTidSone(o.getPeriodeFom()));
    }
}
