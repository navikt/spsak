package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.domene.typer.AntallTimer;
import no.nav.foreldrepenger.domene.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface AktivitetsAvtale {

    /**
     * For timelønnede så vil antallet timer i arbeidsavtalen være satt her
     * @return antall timer
     */
    AntallTimer getAntallTimer();

    /**
     * Antall timer som tilsvarer fulltid (f.eks 40 timer)
     * @return antall timer
     */
    AntallTimer getAntallTimerFulltid();

    /**
     * Avtalt prosentsats i avtalen
     * @return prosent
     */
    Stillingsprosent getProsentsats();

    /**
     * Periode start
     * @return Første dag i perioden
     */
    LocalDate getFraOgMed();

    /**
     * Periode slutt
     * @return siste dag i perioden
     */
    LocalDate getTilOgMed();

    /**
     * Periode
     * @return hele perioden
     */
    DatoIntervallEntitet getPeriode();

    /**
     * Siste lønnsendingsdato
     * @return hele perioden
     */
    LocalDate getSisteLønnsendringsdato();

    boolean matcherPeriode(DatoIntervallEntitet aktivitetsAvtale);

    /**
     * Er avtallen løpende
     * @return true/false
     */
    boolean getErLøpende();

    String getBeskrivelse();

    Yrkesaktivitet getYrkesaktivitet();

    boolean erAnsettelsesPerioden();
}
