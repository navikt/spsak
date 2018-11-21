package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder;

import java.util.Optional;

public class SykefraværBuilder {

    private final SykefraværEntitet kladd;

    private SykefraværBuilder(SykefraværEntitet entitet) {
        this.kladd = entitet;
    }

    private static SykefraværBuilder ny() {
        return new SykefraværBuilder(new SykefraværEntitet());
    }

    private static SykefraværBuilder oppdater(Sykefravær sykefravær) {
        return new SykefraværBuilder(new SykefraværEntitet(sykefravær));
    }

    public static SykefraværBuilder oppdater(Optional<Sykefravær> sykefravær) {
        return sykefravær.map(SykefraværBuilder::oppdater).orElseGet(SykefraværBuilder::ny);
    }

    public SykefraværBuilder tilbakestill() {
        // tilbakestiller alle perioder og oppdaterer fra siste
        kladd.tilbakestillPerioder();
        return this;
    }

    public SykefraværBuilder leggTil(SykefraværPeriodeBuilder builder) {
        // tilbakestiller alle perioder og oppdaterer fra siste
        kladd.leggTil(builder.build());
        return this;
    }

    public Sykefravær build() {
        return kladd;
    }

    public SykefraværPeriodeBuilder periodeBuilder() {
        return SykefraværPeriodeBuilder.ny();
    }
}
