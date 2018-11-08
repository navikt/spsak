package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class UtsettelsePeriode extends UttakPeriode {

    private Utsettelseårsaktype utsettelseårsaktype;

    public UtsettelsePeriode(Stønadskontotype stønadskontotype,PeriodeKilde periodeKilde, LocalDate fom, LocalDate tom,
                             Utsettelseårsaktype utsettelseårsaktype, PeriodeVurderingType periodeResultat, boolean samtidigUttak, boolean flerbarnsdager) {
        super(stønadskontotype, Periodetype.UTSETTELSE, periodeKilde,fom, tom, samtidigUttak, flerbarnsdager);
        this.utsettelseårsaktype = utsettelseårsaktype;
        this.setPeriodeVurderingType(periodeResultat);
    }

    public Utsettelseårsaktype getUtsettelseårsaktype() {
        return utsettelseårsaktype;
    }

    @Override
    UtsettelsePeriode kopiMedNyPeriode(LocalDate fom, LocalDate tom) {
        return new UtsettelsePeriode(getStønadskontotype(), getPeriodeKilde(), fom, tom, getUtsettelseårsaktype(), getPeriodeVurderingType(),
                isSamtidigUttak(), isFlerbarnsdager());
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
        if (Perioderesultattype.INNVILGET.equals(getPerioderesultattype())) {
            return 0; //Dersom utsettelse er innvilget, så skal det trekkes 0 dager.
        }
        return Virkedager.beregnAntallVirkedager(this); //Dersom utsettelsen ikke er innvilget, så skal det trekkes dager lik antall virkedager.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UtsettelsePeriode that = (UtsettelsePeriode) o;

        return utsettelseårsaktype == that.utsettelseårsaktype;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + utsettelseårsaktype.hashCode();
        return result;
    }
}
