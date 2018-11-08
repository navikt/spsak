package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class StønadsPeriode extends UttakPeriode {

    public StønadsPeriode(Stønadskontotype stønadskontotype, PeriodeKilde periodeKilde, LocalDate fom, LocalDate tom, boolean samtidigUttak, boolean flerbarnsdager) {
        super(stønadskontotype, Periodetype.STØNADSPERIODE, periodeKilde, fom, tom, samtidigUttak, flerbarnsdager);
    }

    private StønadsPeriode(StønadsPeriode kilde, LocalDate fom, LocalDate tom) {
        super(kilde, fom, tom);
    }

    public static StønadsPeriode medGradering(Stønadskontotype stønadskontotype, PeriodeKilde periodeKilde, LocalDate fom, LocalDate tom, List<AktivitetIdentifikator> gradertAktiviteter,
                                              BigDecimal prosentArbeid, PeriodeVurderingType periodeResultat) {
        return medGradering(stønadskontotype, periodeKilde, fom, tom, gradertAktiviteter, prosentArbeid, periodeResultat, false, false);
    }

    public static StønadsPeriode medGradering(Stønadskontotype stønadskontotype,
                                              PeriodeKilde periodeKilde,
                                              LocalDate fom,
                                              LocalDate tom,
                                              List<AktivitetIdentifikator> gradertAktiviteter,
                                              BigDecimal prosentArbeid,
                                              PeriodeVurderingType periodeResultat,
                                              boolean samtidigUttak,
                                              boolean flerbarnsdager) {StønadsPeriode periode = new StønadsPeriode(stønadskontotype, periodeKilde, fom, tom, samtidigUttak, flerbarnsdager);
        periode.setGradertAktivitet(gradertAktiviteter, prosentArbeid);
        periode.setPeriodeVurderingType(periodeResultat);
        return periode;
    }

    public static StønadsPeriode medOverføringAvKvote(Stønadskontotype stønadskontotype,PeriodeKilde periodeKilde, LocalDate fom, LocalDate tom,
                                                      OverføringÅrsak overføringÅrsak, PeriodeVurderingType periodeResultat,
        boolean samtidigUttak,
                                                      boolean flerbarnsdager) {StønadsPeriode periode = new StønadsPeriode(stønadskontotype, periodeKilde,fom, tom, samtidigUttak, flerbarnsdager);
        periode.setOverføringÅrsak(overføringÅrsak);
        periode.setPeriodeVurderingType(periodeResultat);
        return periode;
    }

    @Override
    public StønadsPeriode kopiMedNyPeriode(LocalDate fom, LocalDate tom) {
        return new StønadsPeriode(this, fom, tom);
    }

    public boolean isGradering(AktivitetIdentifikator aktivitetIdentifikator) {
        List<AktivitetIdentifikator> graderteAktiviteter = getGradertAktiviteter();
        for (AktivitetIdentifikator gradertAktivititet : graderteAktiviteter) {
            if (Objects.equals(aktivitetIdentifikator, gradertAktivititet)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getTrekkdager(AktivitetIdentifikator aktivitetIdentifikator) {
        return getTrekkdager(isGradering(aktivitetIdentifikator));
    }

    @Override
    public int getMinimumTrekkdager() {
        return getTrekkdager(harGradering());
    }

    @Override
    public int getMaksimumTrekkdager() {
        return getTrekkdager(false);
    }

    private int getTrekkdager(boolean gradert) {
        return TrekkdagerUtregningUtil.trekkdagerFor(this, gradert, skalTrekkeDager(), getGradertArbeidsprosent(),
                getPerioderesultattype().equals(Perioderesultattype.MANUELL_BEHANDLING));
    }

    private boolean skalTrekkeDager() {
        if (!getPerioderesultattype().equals(Perioderesultattype.AVSLÅTT)) {
            return true;
        }
        Avkortingårsaktype avkortingårsak = getAvkortingårsaktype();
        return avkortingårsak != null && avkortingårsak.trekkDager();
    }

}
