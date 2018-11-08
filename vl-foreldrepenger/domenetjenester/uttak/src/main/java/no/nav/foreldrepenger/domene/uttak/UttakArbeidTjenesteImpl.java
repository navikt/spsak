package no.nav.foreldrepenger.domene.uttak;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakBeregningsandelTjeneste;
import no.nav.vedtak.feil.FeilFactory;

@ApplicationScoped
public class UttakArbeidTjenesteImpl implements UttakArbeidTjeneste {

    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private UttakBeregningsandelTjeneste uttakBeregningsandelTjeneste;

    UttakArbeidTjenesteImpl() {
        //For CDI
    }

    @Inject
    public UttakArbeidTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste, UttakBeregningsandelTjeneste uttakBeregningsandelTjeneste) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.uttakBeregningsandelTjeneste = uttakBeregningsandelTjeneste;
    }

    @Override
    public List<Yrkesaktivitet> hentYrkesAktiviteterOrdinærtArbeidsforhold(Behandling behandling) {
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseTjeneste.hentAggregat(behandling);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = uttakBeregningsandelTjeneste.hentAndeler(behandling);
        InntektArbeidYtelseAggregat aggregat = hentYrkesaggregatEtterStp(inntektArbeidYtelseGrunnlag);

        return aggregat.getAktørArbeid()
            .stream()
            .filter(aktørArbeid -> Objects.equals(aktørArbeid.getAktørId(), behandling.getAktørId()))
            .flatMap(aktørArbeid -> aktørArbeid.getYrkesaktiviteter().stream())
            .filter(yrkesaktivitet -> skalYrkesaktivitetTasMed(yrkesaktivitet, andeler))
            .collect(Collectors.toList());
    }

    @Override
    public List<Yrkesaktivitet> hentYrkesAktiviteterFrilans(Behandling behandling) {
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseTjeneste.hentAggregat(behandling);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = uttakBeregningsandelTjeneste.hentAndeler(behandling);
        InntektArbeidYtelseAggregat aggregat = hentYrkesaggregatEtterStp(inntektArbeidYtelseGrunnlag);

        return aggregat.getAktørArbeid()
            .stream()
            .filter(aktørArbeid -> Objects.equals(aktørArbeid.getAktørId(), behandling.getAktørId()))
            .flatMap(aktørArbeid -> aktørArbeid.getFrilansOppdrag().stream())
            .filter(yrkesaktivitet -> skalYrkesaktivitetTasMed(yrkesaktivitet, andeler))
            .collect(Collectors.toList());
    }

    @Override
    public List<Yrkesaktivitet> hentAlleYrkesaktiviteter(Behandling behandling) {
        List<Yrkesaktivitet> aktiviteter = hentYrkesAktiviteterOrdinærtArbeidsforhold(behandling);
        aktiviteter.addAll(hentYrkesAktiviteterFrilans(behandling));
        return aktiviteter;
    }

    @Override
    public List<Inntektsmelding> hentInntektsmeldinger(Behandling behandling) {
        List<Inntektsmelding> inntektsmeldinger = inntektArbeidYtelseTjeneste.hentAlleInntektsmeldinger(behandling);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = uttakBeregningsandelTjeneste.hentAndeler(behandling);
        return filtrerUtInntektsmeldingerUtenAndeler(inntektsmeldinger, andeler);
    }

    @Override
    public boolean erArbeidstaker(Behandling behandling) {
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = uttakBeregningsandelTjeneste.hentAndeler(behandling);
        return andeler.stream().map(BeregningsgrunnlagPrStatusOgAndel::getAktivitetStatus).anyMatch(AktivitetStatus::erArbeidstaker);
    }

    private List<Inntektsmelding> filtrerUtInntektsmeldingerUtenAndeler(List<Inntektsmelding> inntektsmeldinger,
                                                                        List<BeregningsgrunnlagPrStatusOgAndel> andeler) {
        return inntektsmeldinger.stream()
            .filter(inntektsmelding -> finnesIAndeler(andeler, inntektsmelding.getVirksomhet(), inntektsmelding.getArbeidsforholdRef()))
            .collect(Collectors.toList());
    }

    private boolean finnesIAndeler(List<BeregningsgrunnlagPrStatusOgAndel> andeler, Virksomhet virksomhet, ArbeidsforholdRef arbeidsforholdRef) {
        return andeler.stream()
            .anyMatch(andel -> sammeArbeidsforhold(virksomhet, arbeidsforholdRef, andel));
    }

    private boolean sammeArbeidsforhold(Virksomhet virksomhet, ArbeidsforholdRef arbeidsforholdRef, BeregningsgrunnlagPrStatusOgAndel andel) {
        Optional<Virksomhet> virksomhetAndel = andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet);
        if (!virksomhetAndel.isPresent()) {
            return false;
        }
        return Objects.equals(virksomhet.getOrgnr(), virksomhetAndel.get().getOrgnr()) &&
            Objects.equals(arbeidsforholdRef, andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).orElse(null));
    }

    private InntektArbeidYtelseAggregat hentYrkesaggregatEtterStp(InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag) {
        return inntektArbeidYtelseGrunnlag.getOpplysningerEtterSkjæringstidspunkt()
            .orElseGet(() -> inntektArbeidYtelseGrunnlag.getOpplysningerFørSkjæringstidspunkt()
                .orElseThrow(() -> FeilFactory.create(UttakArbeidFeil.class).manglendeYrkesAktiviteter().toException()));
    }

    private boolean skalYrkesaktivitetTasMed(Yrkesaktivitet yrkesaktivitet, List<BeregningsgrunnlagPrStatusOgAndel> andeler) {
        return andeler.stream().anyMatch(andel -> sammeArbeidsforhold(yrkesaktivitet, andel));
    }

    private boolean sammeArbeidsforhold(Yrkesaktivitet yrkesaktivitet, BeregningsgrunnlagPrStatusOgAndel andel) {
        Optional<ArbeidsforholdRef> arbeidsforholdRefAndel = andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef);
        if (arbeidsforholdRefAndel.isPresent()) {
            return Objects.equals(arbeidsforholdRefAndel.get(), yrkesaktivitet.getArbeidsforholdRef().orElse(null)) &&
                Objects.equals(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).orElse(null), yrkesaktivitet.getArbeidsgiver().getVirksomhet().getOrgnr());
        }
        return Objects.equals(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).orElse(null), yrkesaktivitet.getArbeidsgiver().getVirksomhet().getOrgnr());
    }

}
