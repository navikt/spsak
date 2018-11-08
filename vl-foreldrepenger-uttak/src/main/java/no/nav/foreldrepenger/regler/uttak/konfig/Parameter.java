package no.nav.foreldrepenger.regler.uttak.konfig;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;

class Parameter extends Periode {

    private Object verdi;

    Parameter(LocalDate fom, LocalDate tom, Object verdi) {
        super(fom, tom);
        this.verdi = verdi;
    }

    public Object getVerdi() {
        return verdi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Parameter parameter = (Parameter) o;

        return verdi.equals(parameter.verdi);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + verdi.hashCode();
        return result;
    }
}
