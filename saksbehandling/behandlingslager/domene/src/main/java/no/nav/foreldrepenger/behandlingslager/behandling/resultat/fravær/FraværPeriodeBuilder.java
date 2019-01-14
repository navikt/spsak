package no.nav.foreldrepenger.behandlingslager.behandling.resultat.fravær;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class FraværPeriodeBuilder {

    private final FraværPeriodeEntitet kladd;
    private final boolean oppdatering;

    private FraværPeriodeBuilder(FraværPeriodeEntitet entitet, boolean oppdatering) {
        this.kladd = entitet;
        this.oppdatering = oppdatering;
    }

    static FraværPeriodeBuilder nytt() {
        return new FraværPeriodeBuilder(new FraværPeriodeEntitet(), false);
    }

    static FraværPeriodeBuilder oppdatere(FraværPeriode kladd) {
        return new FraværPeriodeBuilder(new FraværPeriodeEntitet(kladd), true);
    }

    public static FraværPeriodeBuilder oppdatere(Optional<FraværPeriode> kladd) {
        return kladd.map(FraværPeriodeBuilder::oppdatere).orElseGet(FraværPeriodeBuilder::nytt);
    }

    public FraværPeriodeBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        kladd.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public FraværPeriodeBuilder medPeriode(LocalDate fom, LocalDate tom) {
        kladd.setPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom));
        return this;
    }

    boolean erOppdatering() {
        return oppdatering;
    }

    FraværPeriode build() {
        return kladd;
    }

}
