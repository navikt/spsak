package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.Periode;
import no.nav.foreldrepenger.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.web.app.exceptions.Valideringsfeil;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.BekreftetUttakPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.KontrollerFaktaPeriodeDto;

public class AvklarFaktaUttakValidator {

    private static final String KREV_MINST_EN_SØKNADSPERIODE = "Påkrevd minst en søknadsperiode";
    private static final String IKKE_ENDRE_SØKNADSPERIODE_STARTDATO = "Startdato på søknadsperiode kan ikke endres";
    private static final String OVERLAPPENDE_PERIODER = "Periodene må ikke overlappe";

    private AvklarFaktaUttakValidator() {
        // skal ikke lages instans
    }

    public static void validerOpplysninger(List<BekreftetUttakPeriodeDto> bekreftedePerioder, List<OppgittPeriode> gjeldendeFordeling, Optional<AvklarteUttakDatoer> avklarteUttakDatoer) {
        List<FeltFeilDto> funnetFeil = Stream.of(validerSøknadsperioder(bekreftedePerioder, gjeldendeFordeling, avklarteUttakDatoer))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
        if (!funnetFeil.isEmpty()) {
            throw new Valideringsfeil(funnetFeil);
        }
    }

    public static Optional<FeltFeilDto> validerSøknadsperioder(List<BekreftetUttakPeriodeDto> bekreftedePerioder, List<OppgittPeriode> gjeldendeFordeling, Optional<AvklarteUttakDatoer> avklarteUttakDatoer) {
        String feltnavn = "søknadsperioder";

        if (ingenSøknadsperiode(bekreftedePerioder)) {
            return Optional.of(new FeltFeilDto(feltnavn, KREV_MINST_EN_SØKNADSPERIODE));
        }
        if (endretSkjæringspunkt(getBekreftetPerioder(bekreftedePerioder), gjeldendeFordeling, avklarteUttakDatoer)) {
            return Optional.of(new FeltFeilDto(feltnavn, IKKE_ENDRE_SØKNADSPERIODE_STARTDATO));
        }
        if (overlappendePerioder(getBekreftetPerioder(bekreftedePerioder))) {
            return Optional.of(new FeltFeilDto(feltnavn, OVERLAPPENDE_PERIODER));
        }
        return Optional.empty();

    }

    private static boolean ingenSøknadsperiode(List<BekreftetUttakPeriodeDto> perioder) {
        return perioder == null || perioder.isEmpty();
    }

    private static boolean endretSkjæringspunkt(List<Periode> bekreftedePerioder, List<OppgittPeriode> gjeldendeFordeling, Optional<AvklarteUttakDatoer> avklarteUttakDatoer) {

        Optional<LocalDate> førsteSøknadsdatoFraSøknaden = bekreftedePerioder.stream()
            .min(Comparator.comparing(Periode::getFom))
            .map(Periode::getFom);

        Optional<LocalDate> førsteSøknadsdatoFraBekreftet = gjeldendeFordeling.stream()
            .min(Comparator.comparing(OppgittPeriode::getFom))
            .map(OppgittPeriode::getFom);

        if (!førsteSøknadsdatoFraSøknaden.isPresent() || !førsteSøknadsdatoFraBekreftet.isPresent()) {
            throw new IllegalArgumentException("Fant ikke første søknads dato");
        }

        if (avklarteUttakDatoer.isPresent() && avklarteUttakDatoer.get().getFørsteUttaksDato() != null) {
            LocalDate førsteUttaksdato = avklarteUttakDatoer.get().getFørsteUttaksDato();
            return !førsteUttaksdato.equals(førsteSøknadsdatoFraSøknaden.get());
        }
        return !førsteSøknadsdatoFraBekreftet.get().equals(førsteSøknadsdatoFraSøknaden.get());
    }

    private static boolean perioderOverlapper(Periode p1, Periode p2) {
        if (p2.getFom() == null || p2.getTom() == null || p1.getFom() == null || p1.getTom() == null) {
            return false;
        }

        boolean p1BegynnerFørst = p1.begynnerFør(p2);
        Periode begynnerFørst = p1BegynnerFørst ? p1 : p2;
        Periode begynnerSist = p1BegynnerFørst ? p2 : p1;
        return begynnerFørst.getTom().isAfter(begynnerSist.getFom());
    }

    private static boolean overlappendePerioder(List<Periode> perioder) {
        for (int i = 0; i < perioder.size(); i++) {
            Periode periode = perioder.get(i);

            for (int y = i + 1; y < perioder.size(); y++) {
                if (perioderOverlapper(periode, perioder.get(y))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<Periode> getBekreftetPerioder(List<BekreftetUttakPeriodeDto> bekreftedePerioder) {
        return bekreftedePerioder.stream()
            .map(bekreftetUttakPeriodeDto -> mapOppgittPeriode(bekreftetUttakPeriodeDto.getBekreftetPeriode()))
            .collect(Collectors.toList());
    }

    private static Periode mapOppgittPeriode(KontrollerFaktaPeriodeDto kontrollerFaktaPeriodeDto) {
        Objects.requireNonNull(kontrollerFaktaPeriodeDto, "kontrollerFaktaPeriodeDto"); // NOSONAR $NON-NLS-1$
        return new Periode(kontrollerFaktaPeriodeDto.getFom(), kontrollerFaktaPeriodeDto.getTom());
    }
}
