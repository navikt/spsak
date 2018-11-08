package no.nav.foreldrepenger.dokumentbestiller.api.mal.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.sortering.LovhjemmelComparator;

public class DokumentTypeMedPerioderDto extends DokumentTypeDto {
    private LocalDate stønadsperiodeFom;
    private LocalDate stønadsperiodeTom;
    private String arbeidsgiversNavn;
    private Boolean annenForelderHarRett;
    private String aleneomsorg;
    private Integer dagerTaptFørTermin;
    private Integer antallPerioder;
    private Boolean avslagFinnes;
    private LocalDate sisteDagIFellesPeriode;
    private LocalDate sisteUtbetalingsdag;
    private LocalDate sisteDagMedUtsettelse;
    private Set<PeriodeDto> periode = new TreeSet<>();
    private Integer disponibleDager;
    private Integer disponibleFellesDager;
    private Set<String> lovhjemmelVurdering = new TreeSet<>(new LovhjemmelComparator());
    private Integer foreldrepengeperiodenUtvidetUker;
    private int antallTapteDager = 0;
    private LocalDate mottattInntektsmelding;
    private Boolean innvilgetFinnes;
    private boolean overstyrtBeløpBeregning;
    private boolean inntektMottattArbgiver;
    private boolean innvilgetGraderingFinnes;
    private LocalDate sisteDagAvSistePeriode;
    private List<FeriePeriodeDto> feriePerioder = new ArrayList<>();

    public DokumentTypeMedPerioderDto(Long behandlingId) {
        super(behandlingId);
    }

    public int getAntallTapteDager() {
        return antallTapteDager;
    }

    public boolean isInnvilgetGraderingFinnes() {
        return innvilgetGraderingFinnes;
    }

    public boolean isOverstyrtBeløpBeregning() {
        return overstyrtBeløpBeregning;
    }

    public void setOverstyrtBeløpBeregning(boolean overstyrtBeløpBeregning) {
        this.overstyrtBeløpBeregning = overstyrtBeløpBeregning;
    }

    public LocalDate getStønadsperiodeFom() {
        return stønadsperiodeFom;
    }

    public void setStønadsperiodeFom(LocalDate stønadsperiodeFom) {
        this.stønadsperiodeFom = stønadsperiodeFom;
    }

    public LocalDate getStønadsperiodeTom() {
        return stønadsperiodeTom;
    }

    public void setStønadsperiodeTom(LocalDate stønadsperiodeTom) {
        this.stønadsperiodeTom = stønadsperiodeTom;
    }

    public Boolean getAnnenForelderHarRett() {
        return annenForelderHarRett;
    }

    public void setAnnenForelderHarRett(Boolean annenForelderHarRett) {
        this.annenForelderHarRett = annenForelderHarRett;
    }

    public String getAleneomsorg() {
        return aleneomsorg;
    }

    public void setAleneomsorg(String aleneomsorg) {
        this.aleneomsorg = aleneomsorg;
    }

    public Integer getDagerTaptFørTermin() {
        return dagerTaptFørTermin;
    }

    public void setDagerTaptFørTermin(Integer dagerTaptFørTermin) {
        this.dagerTaptFørTermin = dagerTaptFørTermin;
    }

    public Integer getAntallPerioder() {
        return antallPerioder;
    }

    public void setAntallPerioder(Integer antallPerioder) {
        this.antallPerioder = antallPerioder;
    }

    public Boolean getAvslagFinnes() {
        return avslagFinnes;
    }

    public void setAvslagFinnes(Boolean avslagFinnes) {
        this.avslagFinnes = avslagFinnes;
    }

    @Override
    public LocalDate getSisteDagIFellesPeriode() {
        return sisteDagIFellesPeriode;
    }

    @Override
    public void setSisteDagIFellesPeriode(LocalDate sisteDagIFellesPeriode) {
        this.sisteDagIFellesPeriode = sisteDagIFellesPeriode;
    }

    public LocalDate getSisteUtbetalingsdag() {
        return sisteUtbetalingsdag;
    }

    public void setSisteUtbetalingsdag(LocalDate sisteUtbetalingsdag) {
        this.sisteUtbetalingsdag = sisteUtbetalingsdag;
    }

    public LocalDate getSisteDagMedUtsettelse() {
        return sisteDagMedUtsettelse;
    }

    public void setSisteDagMedUtsettelse(LocalDate sisteDagMedUtsettelse) {
        this.sisteDagMedUtsettelse = sisteDagMedUtsettelse;
    }

    public Set<PeriodeDto> getPeriode() {
        return periode;
    }

    public void addPeriode(PeriodeDto periode) {
        this.periode.add(periode);
        if (periode.isGraderingFinnes() && periode.getInnvilget()) innvilgetGraderingFinnes = true;
        antallTapteDager += periode.getAntallTapteDager();
    }

    public Integer getDisponibleDager() {
        return disponibleDager;
    }

    public void setDisponibleDager(Integer disponibleDager) {
        this.disponibleDager = disponibleDager;
    }

    public Integer getDisponibleFellesDager() {
        return disponibleFellesDager;
    }

    public void setDisponibleFellesDager(Integer disponibleFellesDager) {
        this.disponibleFellesDager = disponibleFellesDager;
    }

    public Set<String> getLovhjemmelVurdering() {
        return lovhjemmelVurdering;
    }

    public void leggTilLovhjemmelVurdering(String lovhjemmelVurdering) {
        this.lovhjemmelVurdering.add(lovhjemmelVurdering);
    }

    public boolean isInntektMottattArbgiver() {
        return inntektMottattArbgiver;
    }

    public void setInntektMottattArbgiver(boolean inntektMottattArbgiver) {
        this.inntektMottattArbgiver = inntektMottattArbgiver;
    }

    public Integer getForeldrepengeperiodenUtvidetUker() {
        return foreldrepengeperiodenUtvidetUker;
    }

    public void setForeldrepengeperiodenUtvidetUker(Integer foreldrepengeperiodenUtvidetUker) {
        this.foreldrepengeperiodenUtvidetUker = foreldrepengeperiodenUtvidetUker;
    }

    public Boolean getInnvilgetFinnes() {
        return innvilgetFinnes;
    }

    public void setInnvilgetFinnes(Boolean innvilgetFinnes) {
        this.innvilgetFinnes = innvilgetFinnes;
    }

    public LocalDate getSisteDagAvSistePeriode() {
        return sisteDagAvSistePeriode;
    }

    public void setSisteDagAvSistePeriode(LocalDate sisteDagAvSistePeriode) {
        this.sisteDagAvSistePeriode = sisteDagAvSistePeriode;
    }

    public String getArbeidsgiversNavn() {
        return arbeidsgiversNavn;
    }

    public void setArbeidsgiversNavn(String arbeidsgiversNavn) {
        this.arbeidsgiversNavn = arbeidsgiversNavn;
    }

    public LocalDate getMottattInntektsmelding() {
        return mottattInntektsmelding;
    }

    public void setMottattInntektsmelding(LocalDate mottattInntektsmelding) {
        this.mottattInntektsmelding = mottattInntektsmelding;
    }

    public List<FeriePeriodeDto> getFeriePerioder() {
        return feriePerioder;
    }

    public void setFeriePerioder(List<FeriePeriodeDto> feriePerioder) {
        this.feriePerioder = feriePerioder;
    }

    @Override
    public String toString() {
        return "DokumentTypeMedPerioderDto{" +
            "stønadsperiodeFom=" + stønadsperiodeFom +
            ", stønadsperiodeTom=" + stønadsperiodeTom +
            ", arbeidsgiversNavn='" + arbeidsgiversNavn + '\'' +
            ", annenForelderHarRett=" + annenForelderHarRett +
            ", aleneomsorg='" + aleneomsorg + '\'' +
            ", dagerTaptFørTermin=" + dagerTaptFørTermin +
            ", antallPerioder=" + antallPerioder +
            ", avslagFinnes=" + avslagFinnes +
            ", sisteDagIFellesPeriode=" + sisteDagIFellesPeriode +
            ", sisteUtbetalingsdag=" + sisteUtbetalingsdag +
            ", sisteDagMedUtsettelse=" + sisteDagMedUtsettelse +
            ", periode=" + periode +
            ", disponibleDager=" + disponibleDager +
            ", lovhjemmelVurdering=" + lovhjemmelVurdering +
            ", foreldrepengeperiodenUtvidetUker=" + foreldrepengeperiodenUtvidetUker +
            ", mottattInntektsmelding=" + mottattInntektsmelding +
            ", innvilgetFinnes=" + innvilgetFinnes +
            ", overstyrtBeløpBeregning=" + overstyrtBeløpBeregning +
            ", inntektMottattArbgiver=" + inntektMottattArbgiver +
            ", sisteDagAvSistePeriode=" + sisteDagAvSistePeriode +
            ", feriePerioder=" + feriePerioder +
            '}';
    }
}
