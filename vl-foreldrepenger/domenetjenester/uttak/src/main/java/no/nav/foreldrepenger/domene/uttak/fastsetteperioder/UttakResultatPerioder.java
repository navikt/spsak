package no.nav.foreldrepenger.domene.uttak.fastsetteperioder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class UttakResultatPerioder {

    private List<UttakResultatPeriode> perioder;
    private Optional<LocalDate> endringsdato;

    public UttakResultatPerioder(List<UttakResultatPeriode> perioder) {
        this(perioder, Optional.empty());
    }
    public UttakResultatPerioder(List<UttakResultatPeriode> perioder, Optional<LocalDate> endringsdato) {
        this.perioder = perioder;
        this.endringsdato = endringsdato;
    }

    public List<UttakResultatPeriode> getPerioder() {
        return perioder;
    }

    public Optional<LocalDate> getEndringsdato() {
        return endringsdato;
    }
}
