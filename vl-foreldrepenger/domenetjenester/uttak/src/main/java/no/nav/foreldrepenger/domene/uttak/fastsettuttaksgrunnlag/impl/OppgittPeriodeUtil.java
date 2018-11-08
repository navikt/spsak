package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OppholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.Årsak;

class OppgittPeriodeUtil {

    private OppgittPeriodeUtil() {
        //Forhindrer instanser
    }

    static List<OppgittPeriode> sorterEtterFom(List<OppgittPeriode> oppgittePerioder) {
        return oppgittePerioder.stream().sorted(Comparator.comparing(OppgittPeriode::getFom)).collect(Collectors.toList());
    }

    /**
     * Finn første dato fra søknad som ikke er en utsettelse.
     *
     * @param oppgittFordeling oppgitt fordeling.
     *
     * @return første dato fra søknad som ikke er en utsettelse.
     */
    static Optional<LocalDate> finnFørsteSøkteUttaksdato(OppgittFordeling oppgittFordeling) {
        if (oppgittFordeling == null) {
            return Optional.empty();
        }
        List<OppgittPeriode> sortertePerioder = sorterEtterFom(oppgittFordeling.getOppgittePerioder());
        List<OppgittPeriode> perioderMedUttak = sortertePerioder
            .stream()
            .filter(p -> Årsak.UDEFINERT.equals(p.getÅrsak()) || !(p.getÅrsak() instanceof OppholdÅrsak))
            .collect(Collectors.toList());

        if(perioderMedUttak.size() > 0) {
            return Optional.of(perioderMedUttak.get(0).getFom());
        }

        return Optional.empty();
    }

    /**
     * Finn første dato fra søknad.
     *
     * @param oppgittFordeling oppgitt fordeling.
     *
     * @return første dato fra søknad.
     */
    static Optional<LocalDate> finnFørsteSøknadsdato(OppgittFordeling oppgittFordeling) {
        if (oppgittFordeling == null) {
            return Optional.empty();
        }
        List<OppgittPeriode> sortertePerioder = sorterEtterFom(oppgittFordeling.getOppgittePerioder());

        if(sortertePerioder.size() > 0) {
            return Optional.of(sortertePerioder.get(0).getFom());
        }

        return Optional.empty();
    }

}
