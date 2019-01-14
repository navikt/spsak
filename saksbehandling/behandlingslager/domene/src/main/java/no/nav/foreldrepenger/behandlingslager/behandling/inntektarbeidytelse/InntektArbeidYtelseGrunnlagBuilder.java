package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.util.Tuple;

public class InntektArbeidYtelseGrunnlagBuilder {

    private InntektArbeidYtelseGrunnlagEntitet kladd;
    private boolean oppdatert = false;

    private InntektArbeidYtelseGrunnlagBuilder(InntektArbeidYtelseGrunnlagEntitet kladd) {
        this.kladd = kladd;
    }

    static InntektArbeidYtelseGrunnlagBuilder nytt() {
        return new InntektArbeidYtelseGrunnlagBuilder(new InntektArbeidYtelseGrunnlagEntitet());
    }

    static InntektArbeidYtelseGrunnlagBuilder oppdatere(InntektArbeidYtelseGrunnlag kladd) {
        return new InntektArbeidYtelseGrunnlagBuilder(new InntektArbeidYtelseGrunnlagEntitet(kladd));
    }

    public static InntektArbeidYtelseGrunnlagBuilder oppdatere(Optional<InntektArbeidYtelseGrunnlag> kladd) {
        return kladd.map(InntektArbeidYtelseGrunnlagBuilder::oppdatere).orElseGet(InntektArbeidYtelseGrunnlagBuilder::nytt);
    }

    InntektArbeidYtelseGrunnlagEntitet getKladd() {
        return kladd;
    }

    InntektsmeldingAggregat getInntektsmeldinger() {
        final Optional<InntektsmeldingAggregat> inntektsmeldinger = kladd.getInntektsmeldinger();
        return inntektsmeldinger.map(InntektsmeldingAggregatEntitet::new).orElseGet(InntektsmeldingAggregatEntitet::new);
    }

    void setInntektsmeldinger(InntektsmeldingAggregatEntitet inntektsmeldinger) {
        kladd.setInntektsmeldinger(inntektsmeldinger);
    }

    ArbeidsforholdInformasjon getInformasjon() {
        final Optional<ArbeidsforholdInformasjon> informasjon = kladd.getInformasjon();
        return informasjon.map(it -> {
            final ArbeidsforholdInformasjonEntitet informasjonEntitet = new ArbeidsforholdInformasjonEntitet(it);
            kladd.setInformasjon(informasjonEntitet);
            return informasjonEntitet;
        }).orElseGet(() -> {
            final ArbeidsforholdInformasjonEntitet informasjonEntitet = new ArbeidsforholdInformasjonEntitet();
            kladd.setInformasjon(informasjonEntitet);
            return informasjonEntitet;
        });
    }

    InntektArbeidYtelseGrunnlagBuilder medInformasjon(ArbeidsforholdInformasjon informasjon) {
        oppdatert = true;
        kladd.setInformasjon((ArbeidsforholdInformasjonEntitet) informasjon);
        return this;
    }

    private void medSaksbehandlet(InntektArbeidYtelseAggregatBuilder builder) {
        oppdatert = true;
        kladd.setSaksbehandlet((InntektArbeidYtelseAggregatEntitet) builder.build());
    }

    private void medRegister(InntektArbeidYtelseAggregatBuilder builder) {
        oppdatert = true;
        kladd.setRegister((InntektArbeidYtelseAggregatEntitet) builder.build());
    }

    InntektArbeidYtelseGrunnlagBuilder medOppgittOpptjening(OppgittOpptjeningBuilder builder) {
        if (kladd.getOppgittOpptjening().isPresent()) {
            throw new IllegalStateException("Utviklerfeil: Er ikke lov og endre oppgitt opptjening!");
        }
        kladd.setOppgittOpptjening((OppgittOpptjeningEntitet) builder.build());
        return this;
    }

    public InntektArbeidYtelseGrunnlag build() {
        final ArbeidsforholdInformasjonEntitet arbeidsforholdInfo = (ArbeidsforholdInformasjonEntitet) kladd
            .getInformasjon().orElseGet(() -> {
                final ArbeidsforholdInformasjonEntitet informasjonEntitet = new ArbeidsforholdInformasjonEntitet();
                kladd.setInformasjon(informasjonEntitet);
                return informasjonEntitet;
            });

        kladd.getRegisterVersjon().ifPresent(it -> mapArbeidsforholdRef(it, arbeidsforholdInfo));
        kladd.getSaksbehandletVersjon().ifPresent(it -> mapArbeidsforholdRef(it, arbeidsforholdInfo));

        return kladd;
    }

    private void mapArbeidsforholdRef(InntektArbeidYtelseAggregat it, ArbeidsforholdInformasjonEntitet arbeidsforholdInfo) {
        for (AktørArbeid aktørArbeid : it.getAktørArbeid()) {
            for (Yrkesaktivitet yrkesaktivitet : ((AktørArbeidEntitet) aktørArbeid).hentAlleYrkesaktiviter()) {
                if (yrkesaktivitet.getArbeidsforholdRef().isPresent() && yrkesaktivitet.getArbeidsforholdRef().get().gjelderForSpesifiktArbeidsforhold()) {
                    final ArbeidsforholdRef internReferanse = arbeidsforholdInfo
                        .finnEllerOpprett(yrkesaktivitet.getArbeidsgiver(), yrkesaktivitet.getArbeidsforholdRef().get());
                    ((YrkesaktivitetEntitet) yrkesaktivitet).setArbeidsforholdId(internReferanse);
                }
            }
        }
    }

    boolean erOppdatert() {
        return oppdatert;
    }

    public InntektArbeidYtelseGrunnlagBuilder medData(InntektArbeidYtelseAggregatBuilder builder) {
        VersjonType versjon = builder.getVersjon();

        if (versjon == VersjonType.REGISTER) {
            medRegister(builder);
        } else if (versjon == VersjonType.SAKSBEHANDLET) {
            medSaksbehandlet(builder);
        }
        return this;
    }

    void ryddOppErstattedeArbeidsforhold(AktørId søker, List<Tuple<Arbeidsgiver, Tuple<ArbeidsforholdRef, ArbeidsforholdRef>>> erstattArbeidsforhold) {
        final Optional<InntektArbeidYtelseAggregat> registerFørVersjon = kladd.getRegisterVersjon();
        for (Tuple<Arbeidsgiver, Tuple<ArbeidsforholdRef, ArbeidsforholdRef>> tuple : erstattArbeidsforhold) {
            if (registerFørVersjon.isPresent()) {
                // TODO: Vurder konsekvensen av dette.
                final InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(registerFørVersjon, VersjonType.REGISTER);
                builder.oppdaterArbeidsforholdReferanseEtterErstatting(søker, tuple.getElement1(), tuple.getElement2().getElement1(), tuple.getElement2().getElement2());
                medData(builder);
            }
        }
    }
}
