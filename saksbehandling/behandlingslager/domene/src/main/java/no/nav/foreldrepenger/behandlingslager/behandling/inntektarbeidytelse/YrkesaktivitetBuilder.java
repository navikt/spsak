package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet.AktivitetsAvtaleBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet.PermisjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class YrkesaktivitetBuilder {
    private final YrkesaktivitetEntitet kladd;
    private boolean oppdaterer;

    private YrkesaktivitetBuilder(YrkesaktivitetEntitet kladd, boolean oppdaterer) {
        this.kladd = kladd;
        this.oppdaterer = oppdaterer;
    }

    static YrkesaktivitetBuilder ny() {
        return new YrkesaktivitetBuilder(new YrkesaktivitetEntitet(), false);
    }

    static YrkesaktivitetBuilder oppdatere(Yrkesaktivitet oppdatere) {
        return new YrkesaktivitetBuilder((YrkesaktivitetEntitet) oppdatere, true);
    }

    public static YrkesaktivitetBuilder oppdatere(Optional<YrkesaktivitetEntitet> oppdatere) {
        return oppdatere.map(YrkesaktivitetBuilder::oppdatere).orElseGet(YrkesaktivitetBuilder::ny);
    }

    public YrkesaktivitetBuilder medArbeidType(ArbeidType arbeidType) {
        kladd.setArbeidType(arbeidType);
        return this;
    }

    public YrkesaktivitetBuilder medArbeidsforholdId(ArbeidsforholdRef arbeidsforholdId) {
        kladd.setArbeidsforholdId(arbeidsforholdId);
        return this;
    }

    public YrkesaktivitetBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        kladd.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public YrkesaktivitetBuilder medArbeidsgiverNavn(String arbeidsgiver) {
        kladd.setNavnArbeidsgiverUtland(arbeidsgiver);
        return this;
    }

    YrkesaktivitetEntitet getKladd() {
        return kladd;
    }

    public YrkesaktivitetEntitet.PermisjonBuilder getPermisjonBuilder() {
        return nyPermisjonBuilder();
    }

    public YrkesaktivitetBuilder leggTilPermisjon(Permisjon permisjon) {
        PermisjonEntitet permisjonEntitet = (PermisjonEntitet) permisjon;
        kladd.leggTilPermisjon(permisjonEntitet);
        return this;
    }

    public YrkesaktivitetBuilder tilbakestillPermisjon() {
        kladd.tilbakestillPermisjon();
        return this;
    }

    public YrkesaktivitetBuilder tilbakestillAvtaler() {
        kladd.tilbakestillAvtaler();
        return this;
    }

    public YrkesaktivitetEntitet.AktivitetsAvtaleBuilder getAktivitetsAvtaleBuilder() {
        return nyAktivitetsAvtaleBuilder();
    }

    public YrkesaktivitetBuilder leggTilAktivitetsAvtale(AktivitetsAvtaleBuilder aktivitetsAvtale) {
        if(!aktivitetsAvtale.isOppdatering()) {
            AktivitetsAvtaleEntitet aktivitetsAvtaleEntitet = (AktivitetsAvtaleEntitet) aktivitetsAvtale.build();
            kladd.leggTilAktivitetsAvtale(aktivitetsAvtaleEntitet);
        }
        return this;
    }

    public YrkesaktivitetBuilder migrerFraRegisterTilOverstyrt() {
        this.oppdaterer = false;
        return this;
    }

    public boolean getErOppdatering() {
        return this.oppdaterer;
    }

    public Yrkesaktivitet build() {
        return kladd;
    }

    public static AktivitetsAvtaleBuilder nyAktivitetsAvtaleBuilder() {
        return AktivitetsAvtaleBuilder.ny();
    }

    public static PermisjonBuilder nyPermisjonBuilder() {
        return PermisjonBuilder.ny();
    }

    public AktivitetsAvtaleBuilder getAktivitetsAvtaleBuilder(DatoIntervallEntitet aktivitetsPeriode, boolean erAnsettelsesperioden) {
        AktivitetsAvtaleBuilder oppdater = AktivitetsAvtaleBuilder.oppdater(kladd.getAlleAktivitetsAvtaler()
            .stream()
            .filter(aa -> aa.matcherPeriode(aktivitetsPeriode)
                && (!kladd.erArbeidsforhold() || aa.erAnsettelsesPerioden() == erAnsettelsesperioden)).findFirst());
        oppdater.medPeriode(aktivitetsPeriode);
        return oppdater;
    }

    public boolean harIngenAvtaler() {
        return kladd.getAktivitetsAvtaler().isEmpty();
    }

    public void fjernPeriode(DatoIntervallEntitet aktivitetsPeriode) {
        kladd.fjernPeriode(aktivitetsPeriode);
    }

}
