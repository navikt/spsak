package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;

public class InntektsmeldingDto {
    private String arbeidsgiver;
    private String arbeidsgiverOrgnr;
    private LocalDate arbeidsgiverStartdato;

    private List<GraderingPeriodeDto> graderingPerioder = new ArrayList<>();

    InntektsmeldingDto() {
        // trengs for deserialisering av JSON
    }

    public InntektsmeldingDto(Inntektsmelding inntektsmelding) {
        this.arbeidsgiver = inntektsmelding.getVirksomhet().getNavn();
        this.arbeidsgiverOrgnr = inntektsmelding.getVirksomhet().getOrgnr();
        this.arbeidsgiverStartdato = inntektsmelding.getStartDatoPermisjon();

        List<Gradering> graderinger = inntektsmelding.getGraderinger();
        if(graderinger != null) {
            this.graderingPerioder.addAll(graderinger
                .stream()
                .map(GraderingPeriodeDto::new)
                .collect(Collectors.toList()));
        }
    }

    public String getArbeidsgiver() {
        return arbeidsgiver;
    }
    public String getArbeidsgiverOrgnr() {
        return arbeidsgiverOrgnr;
    }

    public void setArbeidsgiver(String arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }
    public void setArbeidsgiverOrgnr(String arbeidsgiverOrgnr) {
        this.arbeidsgiverOrgnr = arbeidsgiverOrgnr;
    }

    public LocalDate getArbeidsgiverStartdato() {
        return arbeidsgiverStartdato;
    }

    public void setArbeidsgiverStartdato(LocalDate arbeidsgiverStartdato) {
        this.arbeidsgiverStartdato = arbeidsgiverStartdato;
    }

    public List<GraderingPeriodeDto> getGraderingPerioder() {
        return graderingPerioder;
    }
}
