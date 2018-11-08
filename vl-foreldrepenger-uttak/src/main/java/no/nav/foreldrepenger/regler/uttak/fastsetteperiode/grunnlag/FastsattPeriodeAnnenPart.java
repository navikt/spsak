package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class FastsattPeriodeAnnenPart extends LukketPeriode {

    private List<UttakPeriodeAktivitet> uttakPeriodeAktiviteter = new ArrayList<>();

    private boolean samtidigUttak;
    private boolean flerbarnsdager;
    private boolean innvilgetUtsettelse;

    public FastsattPeriodeAnnenPart(LocalDate fom, LocalDate tom, boolean samtidigUttak, boolean innvilgetUtsettelse) {
        super(fom, tom);
        this.samtidigUttak = samtidigUttak;
        this.innvilgetUtsettelse = innvilgetUtsettelse;
    }

    public List<UttakPeriodeAktivitet> getUttakPeriodeAktiviteter() {
        return Collections.unmodifiableList(uttakPeriodeAktiviteter);
    }

    public boolean isSamtidigUttak() {
        return samtidigUttak;
    }

    public boolean isInnvilgetUtsettelse() {
        return innvilgetUtsettelse;
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    public static class Builder {
        private FastsattPeriodeAnnenPart kladd;

        public Builder(LocalDate fom, LocalDate tom, boolean samtidigUttak, boolean innvilgetUtsettelse) {
            Objects.requireNonNull(fom);
            Objects.requireNonNull(tom);
            kladd = new FastsattPeriodeAnnenPart(fom, tom, samtidigUttak, innvilgetUtsettelse);
        }

        public Builder medUttakPeriodeAktivitet(UttakPeriodeAktivitet uttakPeriodeAktivitet) {
            kladd.uttakPeriodeAktiviteter.add(uttakPeriodeAktivitet);
            return this;
        }

        public Builder medSamtidigUttak(boolean samtidigUttak) {
            kladd.samtidigUttak = samtidigUttak;
            return this;
        }

        public Builder medInnvilgetUtsettelse(boolean innvilgetUtsettelse) {
            kladd.innvilgetUtsettelse = innvilgetUtsettelse;
            return this;
        }

        public Builder medFlerbarnsdager(boolean flerbarnsdager) {
            kladd.flerbarnsdager = flerbarnsdager;
            return this;
        }

        public FastsattPeriodeAnnenPart build() {
            return kladd;
        }

    }
}
