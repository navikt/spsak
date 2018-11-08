package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUttakDokumentasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.FordelingPeriodeKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.Årsak;

public class KontrollerFaktaPeriode extends Periode {

    private final UttakPeriodeType uttakPeriodeType;
    private final Årsak årsak;
    private final BigDecimal arbeidstidsprosent;
    private final boolean tidligOppstart;
    private String begrunnelse;
    private boolean bekreftet;
    private UttakPeriodeVurderingType vurdering;

    private List<PeriodeUttakDokumentasjon> dokumentertePerioder = new ArrayList<>();
    private String orgnr;
    private String virksomhetNavn;
    private boolean arbeidstaker;
    private boolean samtidigUttak;
    private BigDecimal samtidigUttaksprosent;
    private boolean flerbarnsdager;
    private MorsAktivitet morsAktivitet;
    private FordelingPeriodeKilde periodeKilde;

    private KontrollerFaktaPeriode(OppgittPeriode oppgittPeriode, boolean bekreftet, boolean tidligOppstart) {
        super(oppgittPeriode.getFom(), oppgittPeriode.getTom());
        uttakPeriodeType = oppgittPeriode.getPeriodeType();
        årsak = oppgittPeriode.getÅrsak();
        arbeidstidsprosent = oppgittPeriode.getArbeidsprosent();
        this.begrunnelse = oppgittPeriode.getBegrunnelse().orElse(null);
        this.bekreftet = bekreftet;
        this.vurdering = oppgittPeriode.getPeriodeVurderingType();
        this.orgnr = oppgittPeriode.getVirksomhet() == null ? null : oppgittPeriode.getVirksomhet().getOrgnr();
        this.virksomhetNavn = oppgittPeriode.getVirksomhet() == null ? null : oppgittPeriode.getVirksomhet().getNavn();
        this.arbeidstaker = oppgittPeriode.getErArbeidstaker();
        this.tidligOppstart = tidligOppstart;
        this.samtidigUttak = oppgittPeriode.isSamtidigUttak();
        this.samtidigUttaksprosent = oppgittPeriode.getSamtidigUttaksprosent();
        this.morsAktivitet = oppgittPeriode.getMorsAktivitet();
        this.flerbarnsdager = oppgittPeriode.isFlerbarnsdager();
        this.periodeKilde = oppgittPeriode.getPeriodeKilde();
    }

    private static KontrollerFaktaPeriode ubekreftet(OppgittPeriode oppgittPeriode, boolean tidligOppstart) {
        return new KontrollerFaktaPeriode(oppgittPeriode, false, tidligOppstart);
    }

    public static KontrollerFaktaPeriode ubekreftet(OppgittPeriode oppgittPeriode) {
        return ubekreftet(oppgittPeriode, false);
    }

    /**
     * Tiltenkt bruk ved aksjonspunkt når far/medmor søker før uke 7
     */
    public static KontrollerFaktaPeriode ubekreftetTidligOppstart(OppgittPeriode søknadsperiode) {
        return ubekreftet(søknadsperiode, true);
    }

    public static KontrollerFaktaPeriode automatiskBekreftet(OppgittPeriode oppgittPeriode) {
        KontrollerFaktaPeriode periode = new KontrollerFaktaPeriode(oppgittPeriode, true, false);
        periode.vurdering = UttakPeriodeVurderingType.PERIODE_OK;
        return periode;
    }

    public static KontrollerFaktaPeriode manueltAvklart(OppgittPeriode oppgittPeriode, List<PeriodeUttakDokumentasjon> dokumentertPeriode) {
        KontrollerFaktaPeriode periode = new KontrollerFaktaPeriode(oppgittPeriode, true, false);
        periode.dokumentertePerioder = dokumentertPeriode;
        return periode;
    }

    public UttakPeriodeType getUttakPeriodeType() {
        return uttakPeriodeType;
    }

    public Årsak getÅrsak() {
        return årsak;
    }

    public BigDecimal getArbeidstidsprosent() {
        return arbeidstidsprosent;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public boolean erBekreftet() {
        return bekreftet;
    }

    public List<PeriodeUttakDokumentasjon> getDokumentertePerioder() {
        return dokumentertePerioder;
    }

    public UttakPeriodeVurderingType getVurdering() {
        return vurdering;
    }

    public String getOrgnr() {
        return orgnr;
    }

    public String getVirksomhetNavn() {
        return virksomhetNavn;
    }

    public boolean erArbeidstaker() {
        return arbeidstaker;
    }

    public boolean isTidligOppstart() {
        return tidligOppstart;
    }

    public boolean getSamtidigUttak() {
        return samtidigUttak;
    }
    public BigDecimal getSamtidigUttaksprosent() {
        return samtidigUttaksprosent;
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    public MorsAktivitet getMorsAktivitet() {
        return morsAktivitet;
    }

    public FordelingPeriodeKilde getPeriodeKilde() {
        return periodeKilde;
    }
}
