package no.nav.foreldrepenger.domene.vedtak;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;

public interface VurderOmArenaYtelseSkalOpphøre {

    /**
     * Ved iverksetting av vedtak skal FPSAK gjøre en sjekk av om det er overlapp mellom startdato for foreldrepenger
     * og utbetalt ytelse i ARENA. FPSAK skal benytte lagrede registerdata om meldekortperioder for å vurdere om
     * startdatoen for foreldrepenger overlapper med ytelse i ARENA.
     *
     * @param behandling behandling til saken i FP
     * @param aktørId      id fra NAV Aktør Register
     * @return true hvis det finnes en overlappende ytelse i ARENA, ellers false
     */
    boolean vurder(Behandling behandling, String aktørId);

    /**
     * Første uttaksdato fra uttaksplan.Gitt en liste av beregningsresultat perioder, returnere den første perioden
     * fra og med dato hvor den har minst en andel med dagsats() > 0 && brukerMottaker == true
     *
     * @param beregningsresultatPeriodeList BeregningsresultatPerioder
     * @return Date Første uttaksdato eller null
     */
    LocalDate finnFørsteUttaksdato(List<BeregningsresultatPeriode> beregningsresultatPeriodeList);

    /**
     * Hvis en ytelse i Arena skal opphøres, oppretter metoden en oppgave i GSAK. Oppgaven altså blir hentet
     * av NØS (NAV Økonomi Stønad) som setter utbetaling på vent.
     *
     * @param aktørId      Aktør id til bruker
     * @param behandling   Foreldrepenger behandling
     */
    void opprettOppgaveHvisArenaytelseSkalOpphøre(String aktørId, Behandling behandling);
}
