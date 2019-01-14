package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;

public class ArbeidsforholdOverstyringBuilder {

    private final ArbeidsforholdOverstyringEntitet kladd;
    private final boolean oppdatering;

    private ArbeidsforholdOverstyringBuilder(ArbeidsforholdOverstyringEntitet kladd, boolean oppdatering) {
        this.kladd = kladd;
        this.oppdatering = oppdatering;
    }

    static ArbeidsforholdOverstyringBuilder ny() {
        return new ArbeidsforholdOverstyringBuilder(new ArbeidsforholdOverstyringEntitet(), false);
    }

    static ArbeidsforholdOverstyringBuilder oppdatere(ArbeidsforholdOverstyringEntitet oppdatere) {
        return new ArbeidsforholdOverstyringBuilder(new ArbeidsforholdOverstyringEntitet(oppdatere), true);
    }

    public static ArbeidsforholdOverstyringBuilder oppdatere(Optional<ArbeidsforholdOverstyringEntitet> oppdatere) {
        return oppdatere.map(ArbeidsforholdOverstyringBuilder::oppdatere).orElseGet(ArbeidsforholdOverstyringBuilder::ny);
    }

    public ArbeidsforholdOverstyringBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        kladd.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public ArbeidsforholdOverstyringBuilder medArbeidsforholdRef(ArbeidsforholdRef ref) {
        kladd.setArbeidsforholdRef(ref);
        return this;
    }

    public ArbeidsforholdOverstyringBuilder medNyArbeidsforholdRef(ArbeidsforholdRef ref) {
        kladd.setNyArbeidsforholdRef(ref);
        return this;
    }

    public ArbeidsforholdOverstyringBuilder medHandling(ArbeidsforholdHandlingType type) {
        kladd.setHandling(type);
        return this;
    }

    public ArbeidsforholdOverstyringBuilder medBeskrivelse(String beskrivelse) {
        kladd.setBeskrivelse(beskrivelse);
        return this;
    }

    public ArbeidsforholdOverstyringBuilder medInformasjon(ArbeidsforholdInformasjonEntitet informasjonEntitet) {
        kladd.setInformasjon(informasjonEntitet);
        return this;
    }

    public ArbeidsforholdOverstyringEntitet build() {
        return kladd;
    }

    boolean isOppdatering() {
        return oppdatering;
    }
}
