package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class OppholdPeriode extends UttakPeriode {

    private Oppholdårsaktype oppholdårsaktype;

    public OppholdPeriode(Stønadskontotype stønadskontotype,PeriodeKilde periodeKilde, Oppholdårsaktype oppholdårsaktype,
                          LocalDate fom, LocalDate tom, boolean samtidigUttak, boolean flerbarnsdager) {
        super(stønadskontotype, Periodetype.OPPHOLD, periodeKilde,fom, tom, samtidigUttak, flerbarnsdager);
        this.oppholdårsaktype = oppholdårsaktype;
    }

    private OppholdPeriode(OppholdPeriode kilde, LocalDate fom, LocalDate tom) {
        super(kilde, fom, tom);
        this.oppholdårsaktype = kilde.oppholdårsaktype;
    }

    public Oppholdårsaktype getOppholdårsaktype() {
        return oppholdårsaktype;
    }

    @Override
    public OppholdPeriode kopiMedNyPeriode(LocalDate fom, LocalDate tom) {
        return new OppholdPeriode(this, fom, tom);
    }

    @Override
    public String toString() {
        return "OppholdPeriode{" +
                "oppholdårsaktype=" + oppholdårsaktype +
                ", fom=" + getFom() +
                ", tom=" + getTom() +
                '}';
    }

    @Override
    public int getTrekkdager(AktivitetIdentifikator aktivitetIdentifikator) {
        return getTrekkdager();
    }

    @Override
    public int getMinimumTrekkdager() {
        return getTrekkdager();
    }

    @Override
    public int getMaksimumTrekkdager() {
        return getTrekkdager();
    }

    private int getTrekkdager() {
        return Virkedager.beregnAntallVirkedager(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        OppholdPeriode that = (OppholdPeriode) o;

        return oppholdårsaktype == that.oppholdårsaktype;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + oppholdårsaktype.hashCode();
        return result;
    }
}
