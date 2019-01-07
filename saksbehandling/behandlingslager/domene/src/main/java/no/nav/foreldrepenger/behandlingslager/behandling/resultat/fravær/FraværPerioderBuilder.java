package no.nav.foreldrepenger.behandlingslager.behandling.resultat.fravær;

import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class FraværPerioderBuilder {

    private final FraværPerioderEntitet kladd;

    private FraværPerioderBuilder(FraværPerioderEntitet entitet) {
        this.kladd = entitet;
    }

    static FraværPerioderBuilder nytt() {
        return new FraværPerioderBuilder(new FraværPerioderEntitet());
    }

    static FraværPerioderBuilder oppdatere(FraværPerioder kladd) {
        return new FraværPerioderBuilder(new FraværPerioderEntitet(kladd));
    }

    public static FraværPerioderBuilder oppdatere(Optional<FraværPerioder> kladd) {
        return kladd.map(FraværPerioderBuilder::oppdatere).orElseGet(FraværPerioderBuilder::nytt);
    }

    public FraværPerioderBuilder tilbakestillPeriode() {
        kladd.tilbakestillPerioder();
        return this;
    }

    public FraværPerioderBuilder leggTil(FraværPeriodeBuilder builder) {
        if (!builder.erOppdatering()) {
            kladd.leggTil(builder.build());
        }
        return this;
    }

    FraværPerioder build() {
        return kladd;
    }

    public FraværPeriodeBuilder opprettBuilderFor(Arbeidsgiver arbeidsgiver, DatoIntervallEntitet intervall) {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        Objects.requireNonNull(intervall, "intervall");
        Optional<FraværPeriode> periode = kladd.getPerioder()
            .stream()
            .filter(p -> p.getArbeidsgiver().equals(arbeidsgiver) && p.getPeriode().equals(intervall))
            .findAny();

        // FIXME SP - Evaluer om dette er en god ide
        return FraværPeriodeBuilder.oppdatere(periode)
            .medArbeidsgiver(arbeidsgiver)
            .medPeriode(intervall.getFomDato(), intervall.getTomDato());
    }
}
