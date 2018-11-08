package no.nav.foreldrepenger.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.typer.Beløp;

@ApplicationScoped
public class BeregningInntektsmeldingTjenesteImpl implements BeregningInntektsmeldingTjeneste {

    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    BeregningInntektsmeldingTjenesteImpl() {
        //proxy
    }

    @Inject
    public BeregningInntektsmeldingTjenesteImpl(BehandlingRepositoryProvider repositoryProvider, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
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
        Optional<YtelseFordelingAggregat> ytelsesfordelingAggregat = ytelsesFordelingRepository.hentAggregatHvisEksisterer(behandling);
        return ytelsesfordelingAggregat
            .flatMap(YtelseFordelingAggregat::getAvklarteDatoer)
            .map(AvklarteUttakDatoer::getFørsteUttaksDato)
            .orElse(inntektsmelding.getStartDatoPermisjon());
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
