package no.nav.foreldrepenger.domene.uttak;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;

@ApplicationScoped
public class UttakStillingsprosentTjenesteImpl implements UttakStillingsprosentTjeneste {

    private UttakArbeidTjeneste uttakArbeidTjeneste;

    UttakStillingsprosentTjenesteImpl() {
        //For CDI
    }

    @Inject
    public UttakStillingsprosentTjenesteImpl(UttakArbeidTjeneste uttakArbeidTjeneste) {
        this.uttakArbeidTjeneste = uttakArbeidTjeneste;
    }

    @Override
    public Optional<BigDecimal> finnStillingsprosentOrdinærtArbeid(Behandling behandling,
                                                                   String arbeidsforholdOrgnr,
                                                                   String arbeidsforholdId,
                                                                   LocalDate dato) {
        List<Yrkesaktivitet> ytelseAktiviteter = uttakArbeidTjeneste.hentYrkesAktiviteterOrdinærtArbeidsforhold(behandling);
        return finnStillingsprosentOrdinærtArbeid(arbeidsforholdOrgnr, arbeidsforholdId, ytelseAktiviteter, dato);
    }

    @Override
    public Optional<BigDecimal> finnStillingsprosentFrilans(Behandling behandling, LocalDate dato) {
        return Optional.ofNullable(BigDecimal.valueOf(100));
    }

    private Optional<BigDecimal> finnStillingsprosentOrdinærtArbeid(String arbeidsforholdOrgnr,
                                                                    String arbeidsforholdId,
                                                                    List<Yrkesaktivitet> yrkesaktivitetList,
                                                                    LocalDate dato) {
        List<BigDecimal> stillingsprosenter = yrkesaktivitetList.stream()
            .filter(ya -> ArbeidType.ORDINÆRT_ARBEIDSFORHOLD.equals(ya.getArbeidType()))
            .filter(ya -> Objects.equals(ya.getArbeidsgiver().getIdentifikator(), arbeidsforholdOrgnr))
            .filter(ya -> {
                Optional<ArbeidsforholdRef> arbeidsforholdRef = ya.getArbeidsforholdRef();
                if (arbeidsforholdId != null && arbeidsforholdRef.isPresent()) {
                    return Objects.equals(arbeidsforholdRef.get().getReferanse(), arbeidsforholdId);
                }
                return true;
            })
            .flatMap(ya -> ya.getAktivitetsAvtaler().stream())
            .filter(avtale -> riktigDato(dato, avtale))
            .map(UttakArbeidUtil::hentStillingsprosent)
            .collect(Collectors.toList());
        return sum(stillingsprosenter);
    }

    private Optional<BigDecimal> sum(List<BigDecimal> stillingsprosenter) {
        if (stillingsprosenter.isEmpty()) {
            return Optional.empty();
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal stillingsprosent : stillingsprosenter) {
            sum = sum.add(stillingsprosent);
        }
        return Optional.of(sum);
    }

    private boolean riktigDato(LocalDate dato, AktivitetsAvtale avtale) {
        return (avtale.getFraOgMed().isEqual(dato) || avtale.getFraOgMed().isBefore(dato)) &&
            (avtale.getTilOgMed().isEqual(dato) || avtale.getTilOgMed().isAfter(dato));
    }

}
