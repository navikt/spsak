package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class SykefraværPeriodeBuilder {

    private final SykefraværPeriodeEntitet kladd;

    private SykefraværPeriodeBuilder(SykefraværPeriodeEntitet entitet) {
        this.kladd = entitet;
    }

    public static SykefraværPeriodeBuilder ny() {
        return new SykefraværPeriodeBuilder(new SykefraværPeriodeEntitet());
    }

    public SykefraværPeriodeBuilder medType(SykefraværPeriodeType type) {
        kladd.setType(type);
        return this;
    }

    public SykefraværPeriodeBuilder medPeriode(LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(fom, "fom");
        Objects.requireNonNull(tom, "tom");
        kladd.setPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom));
        return this;
    }

    public SykefraværPeriodeBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        kladd.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public SykefraværPeriodeBuilder medGradering(Prosentsats prosentsats) {
        kladd.setGradering(prosentsats);
        return this;
    }

    public SykefraværPeriodeBuilder medArbeidsgrad(Prosentsats arbeidsgrad) {
        kladd.setArbeidsgrad(arbeidsgrad);
        return this;
    }

    public SykefraværPeriode build() {
        Objects.requireNonNull(kladd.getPeriode(), "periode");
        Objects.requireNonNull(kladd.getArbeidsgiver(), "arbeidsgiver");
        return kladd;
    }
}
