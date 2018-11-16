package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.vedtak.konfig.Tid;

public class InntektsmeldingBuilder {
    private final InntektsmeldingEntitet kladd;

    InntektsmeldingBuilder(InntektsmeldingEntitet kladd) {
        this.kladd = kladd;
    }

    public static InntektsmeldingBuilder builder() {
        return new InntektsmeldingBuilder(new InntektsmeldingEntitet());
    }

    public InntektsmeldingBuilder medVirksomhet(Virksomhet virksomhet) {
        kladd.setVirksomhet(virksomhet);
        return this;
    }

    public InntektsmeldingBuilder medArbeidsforholdId(String arbeidsforholdId) {
        if (arbeidsforholdId != null) {
            kladd.setArbeidsforholdId(ArbeidsforholdRef.ref(arbeidsforholdId));
        }
        return this;
    }

    public InntektsmeldingBuilder medBeløp(BigDecimal verdi) {
        kladd.setInntektBeløp(new Beløp(verdi));
        return this;
    }

    public InntektsmeldingBuilder medNærRelasjon(boolean nærRelasjon) {
        kladd.setNærRelasjon(nærRelasjon);
        return this;
    }

    public InntektsmeldingBuilder medStartDatoPermisjon(LocalDate startPermisjon) {
        kladd.setStartDatoPermisjon(startPermisjon);
        return this;
    }

    public InntektsmeldingBuilder medRefusjon(BigDecimal verdi, LocalDate opphører) {
        kladd.setRefusjonBeløpPerMnd(new Beløp(verdi));
        kladd.setRefusjonOpphører(opphører);
        return this;
    }

    public InntektsmeldingBuilder medRefusjon(BigDecimal verdi) {
        kladd.setRefusjonBeløpPerMnd(new Beløp(verdi));
        kladd.setRefusjonOpphører(Tid.TIDENES_ENDE);
        return this;
    }

    public InntektsmeldingBuilder medInnsendingstidspunkt(LocalDateTime innsendingstidspunkt) {
        Objects.requireNonNull(innsendingstidspunkt, "innsendingstidspunkt");
        kladd.setInnsendingstidspunkt(innsendingstidspunkt);
        return this;
    }

    public InntektsmeldingBuilder leggTil(NaturalYtelse naturalYtelse) {
        kladd.leggTil(naturalYtelse);
        return this;
    }

    public InntektsmeldingBuilder leggTil(UtsettelsePeriode utsettelsePeriode) {
        kladd.leggTil(utsettelsePeriode);
        return this;
    }

    public InntektsmeldingBuilder leggTil(Gradering gradering) {
        kladd.leggTil(gradering);
        return this;
    }

    public InntektsmeldingBuilder leggTil(Refusjon refusjon) {
        kladd.leggTil(refusjon);
        return this;
    }

    public InntektsmeldingBuilder leggTil(Arbeidsgiverperiode arbeidsgiverperiode) {
        kladd.leggTil(arbeidsgiverperiode);
        return this;
    }


    public InntektsmeldingBuilder medMottattDokument(MottattDokument mottattDokument) {
        kladd.setMottattDokumentId(mottattDokument.getId());
        return this;
    }

    public InntektsmeldingBuilder medInntektsmeldingaarsak(InntektsmeldingInnsendingsårsak inntektsmeldingInnsendingsårsak) {
        kladd.setInntektsmeldingInnsendingsårsak(inntektsmeldingInnsendingsårsak);
        return this;
    }

    public Inntektsmelding build() {
        return kladd;
    }
}
