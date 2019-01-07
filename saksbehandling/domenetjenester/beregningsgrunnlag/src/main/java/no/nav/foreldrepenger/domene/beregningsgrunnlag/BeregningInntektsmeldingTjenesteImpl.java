package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.typer.Beløp;

@ApplicationScoped
public class BeregningInntektsmeldingTjenesteImpl implements BeregningInntektsmeldingTjeneste {

    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    BeregningInntektsmeldingTjenesteImpl() {
        //proxy
    }

    @Inject
    public BeregningInntektsmeldingTjenesteImpl(GrunnlagRepositoryProvider repositoryProvider, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
    }

    @Override
    public Beløp totaltRefusjonsbeløpFraInntektsmelding(Behandling behandling, LocalDate dato) {
        List<Inntektsmelding> inntektsmeldinger = inntektArbeidYtelseTjeneste
            .hentAlleInntektsmeldinger(behandling);
        return beregnTotaltRefusjonskravPrÅrPåDato(inntektsmeldinger, dato, behandling);
    }

    @Override
    public boolean erTotaltRefusjonskravStørreEnnSeksG(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag, LocalDate dato) {
        Beløp seksG = beregningsgrunnlag.getRedusertGrunnbeløp().multipliser(6);
        Beløp totaltRefusjonskravPrÅr = totaltRefusjonsbeløpFraInntektsmelding(behandling, dato);
        return totaltRefusjonskravPrÅr.compareTo(seksG) > 0;
    }

    @Override
    public LocalDate fastsettStartdatoInntektsmelding(Behandling behandling, Inntektsmelding inntektsmelding) {
        // FIXME SP: trengs alternativ startdato dersom tidligere sykemelding i samme sak?
        return inntektsmelding.getStartDatoPermisjon();
    }

    private Beløp beregnTotaltRefusjonskravPrÅrPåDato(List<Inntektsmelding> inntektsmeldinger, LocalDate dato, Behandling behandling) {
        Beløp refusjonskravPerMåned = inntektsmeldinger.stream()
            .filter(inntektsmelding -> !fastsettStartdatoInntektsmelding(behandling, inntektsmelding).isAfter(dato) &&
                (inntektsmelding.getRefusjonOpphører() == null || !inntektsmelding.getRefusjonOpphører().isBefore(dato)))
            .map(Inntektsmelding::getRefusjonBeløpPerMnd)
            .filter(Objects::nonNull)
            .reduce(Beløp.ZERO, Beløp::adder);
        return refusjonskravPerMåned.multipliser(12);
    }
}
