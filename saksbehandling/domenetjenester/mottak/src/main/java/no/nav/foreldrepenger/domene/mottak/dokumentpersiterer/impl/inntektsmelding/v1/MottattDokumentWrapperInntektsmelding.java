package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.inntektsmelding.v1;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;

import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.MottattDokumentWrapper;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.seres.xsd.nav.inntektsmelding_m._201809.InntektsmeldingConstants;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Arbeidsforhold;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Arbeidsgiver;
import no.seres.xsd.nav.inntektsmelding_m._20180924.ArbeidsgiverperiodeListe;
import no.seres.xsd.nav.inntektsmelding_m._20180924.GjenopptakelseNaturalytelseListe;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;
import no.seres.xsd.nav.inntektsmelding_m._20180924.NaturalytelseDetaljer;
import no.seres.xsd.nav.inntektsmelding_m._20180924.OpphoerAvNaturalytelseListe;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Periode;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Refusjon;
import no.seres.xsd.nav.inntektsmelding_m._20180924.SykepengerIArbeidsgiverperioden;

public class MottattDokumentWrapperInntektsmelding extends MottattDokumentWrapper<InntektsmeldingM> {

    public MottattDokumentWrapperInntektsmelding(InntektsmeldingM skjema) {
        super(skjema, InntektsmeldingConstants.NAMESPACE);
    }

    public List<NaturalytelseDetaljer> getGjenopptakelserAvNaturalytelse() {
        return Optional.ofNullable(getSkjema().getSkjemainnhold().getGjenopptakelseNaturalytelseListe())
            .map(JAXBElement::getValue)
            .map(GjenopptakelseNaturalytelseListe::getNaturalytelseDetaljer)
            .orElse(Collections.emptyList());
    }

    public List<NaturalytelseDetaljer> getOpphørelseAvNaturalytelse() {
        return Optional.ofNullable(getSkjema().getSkjemainnhold().getOpphoerAvNaturalytelseListe())
            .map(JAXBElement::getValue)
            .map(OpphoerAvNaturalytelseListe::getOpphoerAvNaturalytelse)
            .orElse(Collections.emptyList());
    }

    public String getArbeidstaker() {
        return getSkjema().getSkjemainnhold().getArbeidstakerFnr();
    }

    public Arbeidsgiver getArbeidsgiver() {
        return getSkjema().getSkjemainnhold().getArbeidsgiver();
    }

    public Optional<Arbeidsforhold> getArbeidsforhold() {
        return Optional.ofNullable(getSkjema().getSkjemainnhold().getArbeidsforhold()).map(JAXBElement::getValue);
    }

    public Optional<SykepengerIArbeidsgiverperioden> getSykepengerIArbeidsgiverperioden() {
        return Optional.ofNullable(getSkjema().getSkjemainnhold().getSykepengerIArbeidsgiverperioden()).map(JAXBElement::getValue);
    }

    public Optional<String> getArbeidsforholdId() {
        return Optional.ofNullable(getSkjema().getSkjemainnhold().getArbeidsforhold())
            .map(JAXBElement::getValue)
            .map(Arbeidsforhold::getArbeidsforholdId)
            .map(JAXBElement::getValue);
    }

    public boolean getErNærRelasjon() {
        return getSkjema().getSkjemainnhold().isNaerRelasjon();
    }

    public Optional<Refusjon> getRefusjon() {
        return Optional.ofNullable(getSkjema().getSkjemainnhold().getRefusjon()).map(JAXBElement::getValue);
    }

    public List<Periode> getArbeidsgiverPeriode() {
        return getSykepengerIArbeidsgiverperioden().map(SykepengerIArbeidsgiverperioden::getArbeidsgiverperiodeListe)
            .map(JAXBElement::getValue)
            .map(ArbeidsgiverperiodeListe::getArbeidsgiverperiode)
            .orElse(Collections.emptyList());
    }

    /**
     * Hvis inntektsmeldingen kommer fra Altinn (innsendingstidspunkt ikke oppgitt), bruker vi
     * tilnæringen "LocalDateTime.now()", selv om riktig innsendingstidspunkt er arkiveringstidspunkt i joark.
     */
    public LocalDateTime getInnsendingstidspunkt() {
        return Optional.ofNullable(getSkjema().getSkjemainnhold().getAvsendersystem().getInnsendingstidspunkt())
            .map(JAXBElement::getValue)
            .map(e -> DateUtil.convertToLocalDateTime(e))
            .orElse(LocalDateTime.now());
    }
}
