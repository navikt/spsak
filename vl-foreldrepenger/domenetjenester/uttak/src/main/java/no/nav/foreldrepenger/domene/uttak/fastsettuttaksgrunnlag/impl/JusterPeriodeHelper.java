package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.threeten.extra.Days;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OppholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.fpsak.tidsserie.LocalDateInterval;

class JusterPeriodeHelper {


    OppgittFordeling juster(OppgittFordeling oppgittFordeling, LocalDate gammelFamiliehendelse, LocalDate nyFamiliehendelse) {
        List<OppgittPeriode> oppgittPerioder = OppgittPeriodeUtil.sorterEtterFom(oppgittFordeling.getOppgittePerioder());
        List<OppgittPeriode> justert = juster(oppgittPerioder, gammelFamiliehendelse, nyFamiliehendelse);
        return new OppgittFordelingEntitet(kopier(justert), oppgittFordeling.getErAnnenForelderInformert());
    }

    private List<OppgittPeriode> kopier(List<OppgittPeriode> justert) {
        return justert.stream().map(op -> OppgittPeriodeBuilder.fraEksisterende(op).build()).collect(Collectors.toList());
    }

    private List<OppgittPeriode> juster(List<OppgittPeriode> oppgittePerioder, LocalDate gammelFamiliehendelse, LocalDate nyFamiliehendelse) {
        if (gammelFamiliehendelse.equals(nyFamiliehendelse)) {
            return oppgittePerioder;
        }
        if (oppgittePerioder.isEmpty()) {
            return oppgittePerioder;
        }
        LocalDate sisteSøkteDato = oppgittePerioder.get(oppgittePerioder.size() - 1).getTom();
        List<OppgittPeriode> oppgittePerioderMedHull = fyllHull(oppgittePerioder);
        List<OppgittPeriode> ikkeFlyttbarePerioder = oppgittePerioderMedHull.stream().filter(p -> !sjekkOmPeriodenErFlyttbar(p)).collect(Collectors.toList());
        List<OppgittPeriode> flyttbarePerioder = oppgittePerioderMedHull.stream().filter(p -> sjekkOmPeriodenErFlyttbar(p)).collect(Collectors.toList());
        LocalDate førsteLovligeUttaksdato = førsteLovligeUttaksdato(oppgittePerioder, nyFamiliehendelse);

        List<OppgittPeriode> resultatPerioder = new ArrayList<>();

        int delta = Days.between(gammelFamiliehendelse, nyFamiliehendelse).getAmount();
        if (nyFamiliehendelse.isBefore(gammelFamiliehendelse)) {
            leggTilDagerPåSisteFlyttbarePeriodeEtterForrigeFamiliehendelse(flyttbarePerioder, gammelFamiliehendelse, delta);
            List<OppgittPeriode> ikkeHåndtertePerioder = justerForTidligFødsel(delta, ikkeFlyttbarePerioder, flyttbarePerioder, førsteLovligeUttaksdato, resultatPerioder);
            for (OppgittPeriode ikkeHåndtertePeriode: ikkeHåndtertePerioder) {
                resultatPerioder.addAll(PeriodeSplitter.splittPeriodeMotVenstre(ikkeFlyttbarePerioder, ikkeHåndtertePeriode, delta));
            }
        } else {
            List<OppgittPeriode> ikkeHåndtertePerioder = justerForSenFødsel(delta, oppgittePerioderMedHull, ikkeFlyttbarePerioder, flyttbarePerioder, resultatPerioder);
            for (OppgittPeriode ikkeHåndtertePeriode: ikkeHåndtertePerioder) {
                resultatPerioder.addAll(PeriodeSplitter.splittPeriodeMotHøyre(ikkeFlyttbarePerioder, ikkeHåndtertePeriode, delta));
            }
        }
        resultatPerioder = fjernPerioderEtterSisteSøkteDato(resultatPerioder, sisteSøkteDato);
        resultatPerioder.addAll(ikkeFlyttbarePerioder);
        resultatPerioder = OppgittPeriodeUtil.sorterEtterFom(resultatPerioder);
        return resultatPerioder;
    }

    private List<OppgittPeriode> fjernPerioderEtterSisteSøkteDato(List<OppgittPeriode> oppgittePerioder, LocalDate sisteSøkteDato) {
        return oppgittePerioder.stream().filter(p -> p.getFom().isBefore(sisteSøkteDato) || p.getFom().isEqual(sisteSøkteDato)).map(p -> {
            if (p.getTom().isAfter(sisteSøkteDato)) {
                return OppgittPeriodeBuilder.fraEksisterende(p).medPeriode(p.getFom(), sisteSøkteDato).build();
            }
            return p;
        }).collect(Collectors.toList());
    }

    private void leggTilDagerPåSisteFlyttbarePeriodeEtterForrigeFamiliehendelse(List<OppgittPeriode> flyttbarePerioder, LocalDate forrigeFamiliehendlese, int delta) {
        if (flyttbarePerioder.isEmpty()) {
            return;
        }
        OppgittPeriode periodeSomSkalUtvides = flyttbarePerioder.get(flyttbarePerioder.size()-1);
        if (!periodeSomSkalUtvides.getFom().isBefore(forrigeFamiliehendlese)) {
            OppgittPeriode utvidetPeriode = OppgittPeriodeBuilder.fraEksisterende(periodeSomSkalUtvides).medPeriode(periodeSomSkalUtvides.getFom(), periodeSomSkalUtvides.getTom().plusDays(Math.abs(delta))).build();
            flyttbarePerioder.set(flyttbarePerioder.size()-1, utvidetPeriode);
        }
    }

    private List<OppgittPeriode> justerForTidligFødsel(int delta, List<OppgittPeriode> ikkeFlyttbarePerioder, List<OppgittPeriode> flyttbarePerioder, LocalDate førsteLovligeUttaksdato, List<OppgittPeriode> resultatPerioder) {
        List<OppgittPeriode> ikkeHåndtertePerioder = new ArrayList<>();
        for(OppgittPeriode p : flyttbarePerioder) {
            if (sjekkOmPeriodenFlyttesOverIkkeFlyttbarPeriode(p, ikkeFlyttbarePerioder, delta)) {
                ikkeHåndtertePerioder.add(p);
            } else {
                Optional<OppgittPeriode> justertEllerFjernetPeriode = justerTilVenstre(p, delta, førsteLovligeUttaksdato);
                if (justertEllerFjernetPeriode.isPresent()) {
                    resultatPerioder.add(justertEllerFjernetPeriode.get());
                }
            }
        }
        return ikkeHåndtertePerioder;
    }

    private List<OppgittPeriode> justerForSenFødsel(int delta, List<OppgittPeriode> oppgittePerioderMedHull, List<OppgittPeriode> ikkeFlyttbarePerioder, List<OppgittPeriode> flyttbarePerioder, List<OppgittPeriode> resultatPerioder) {
        List<OppgittPeriode> ikkeHåndtertePerioder = new ArrayList<>();
        for (OppgittPeriode p : flyttbarePerioder) {
            if (sjekkOmPeriodenFlyttesOverIkkeFlyttbarPeriode(p, ikkeFlyttbarePerioder, delta)) {
                ikkeHåndtertePerioder.add(p);
            } else {
                resultatPerioder.add(justerTilHøyre(p, delta));
            }
        }

        //Legg til fellesperiode i starten for å dekke periode fra første uttaksdag.
        LocalDate førsteUttaksdatoEtterJustering = førsteUttaksdato(resultatPerioder);
        LocalDate førsteUttaksdatoFørJustering = førsteUttaksdato(oppgittePerioderMedHull);
        if (førsteUttaksdatoEtterJustering != null && førsteUttaksdatoEtterJustering.isAfter(førsteUttaksdatoFørJustering)) {
            OppgittPeriode ekstraFp = OppgittPeriodeBuilder.ny()
                .medPeriodeType(finnUttakPeriodeType(oppgittePerioderMedHull))
                .medPeriode(førsteUttaksdatoFørJustering, førsteUttaksdatoEtterJustering.minusDays(1))
                .build();
            resultatPerioder.add(0, ekstraFp);
        }
        return ikkeHåndtertePerioder;
    }

    private UttakPeriodeType finnUttakPeriodeType(List<OppgittPeriode> oppgittePerioderMedHull) {
        for (OppgittPeriode oppgittPeriode : oppgittePerioderMedHull) {
            if (oppgittPeriode.getPeriodeType().equals(UttakPeriodeType.FORELDREPENGER)) {
                return UttakPeriodeType.FORELDREPENGER;
            }
        }
        return UttakPeriodeType.FELLESPERIODE;
    }

    private Optional<OppgittPeriode> justerTilVenstre(OppgittPeriode oppgittPeriode, int delta, LocalDate førsteLovligeUttaksdato) {
        LocalDate nyFom = oppgittPeriode.getFom().plusDays(delta);
        LocalDate nyTom = oppgittPeriode.getTom().plusDays(delta);
        if (nyFom.isBefore(førsteLovligeUttaksdato) && (nyTom.isBefore(førsteLovligeUttaksdato))) {
            return Optional.empty();
        } else if (nyFom.isBefore(førsteLovligeUttaksdato) && (nyTom.isAfter(førsteLovligeUttaksdato) || nyTom.equals(førsteLovligeUttaksdato))) {
            OppgittPeriode justertPeriode = OppgittPeriodeBuilder.fraEksisterende(oppgittPeriode).medPeriode(førsteLovligeUttaksdato, nyTom).build();
            return Optional.of(justertPeriode);
        } else {
            OppgittPeriode justertPeriode = OppgittPeriodeBuilder.fraEksisterende(oppgittPeriode).medPeriode(nyFom, nyTom).build();
            return Optional.of(justertPeriode);
        }
    }

    private OppgittPeriode justerTilHøyre(OppgittPeriode oppgittPeriode, int delta) {
        LocalDate nyFom = oppgittPeriode.getFom().plusDays(delta);
        LocalDate nyTom = oppgittPeriode.getTom().plusDays(delta);
        OppgittPeriode justertPeriode = OppgittPeriodeBuilder.fraEksisterende(oppgittPeriode).medPeriode(nyFom, nyTom).build();
        return justertPeriode;
    }

    private boolean sjekkOmPeriodenFlyttesOverIkkeFlyttbarPeriode(OppgittPeriode oppgittPeriode, List<OppgittPeriode> ikkeFlyttbarePerioder, int delta) {
        for (OppgittPeriode ikkeFlyttbarPeriode: ikkeFlyttbarePerioder) {
            LocalDateInterval ikkeFlyttbartInterval = new LocalDateInterval(ikkeFlyttbarPeriode.getFom(), ikkeFlyttbarPeriode.getTom());
            if (delta < 0) {
                if (new LocalDateInterval(oppgittPeriode.getFom().plusDays(delta), oppgittPeriode.getFom()).overlaps(ikkeFlyttbartInterval)) {
                    return true;
                }
            } else if (delta > 0) {
                if (new LocalDateInterval(oppgittPeriode.getTom(), oppgittPeriode.getTom().plusDays(delta)).overlaps(ikkeFlyttbartInterval)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<OppgittPeriode> fyllHull(List<OppgittPeriode> oppgittPerioder) {
        List<OppgittPeriode> oppgittPerioderPlusHull = new ArrayList<>();
        for(int index = 0; index < oppgittPerioder.size(); index++) {
            oppgittPerioderPlusHull.add(oppgittPerioder.get(index));
            if (index != oppgittPerioder.size() - 1) {
                //Sjekk for hull dersom det ikke er siste perioder
                OppgittPeriode periodeFør = oppgittPerioder.get(index + 1);
                long dagerMellomPerioder = Days.between(oppgittPerioder.get(index).getTom(), periodeFør.getFom()).getAmount() - 1L;
                if (dagerMellomPerioder > 0) {
                    LocalDate fom = oppgittPerioder.get(index).getTom().plusDays(1);
                    LocalDate tom = fom.plusDays(dagerMellomPerioder - 1);
                    OppgittPeriode hull = OppgittPeriodeBuilder.ny()
                        .medPeriode(fom, tom)
                        .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
                        .build();
                    oppgittPerioderPlusHull.add(hull);
                }
            }
        }
        return oppgittPerioderPlusHull;
    }

    /**
     * Sjekk om perioden er flyttbar. Perioden er ikke flyttbar dersom det er en utsettelse, opphold eller en gradert periode.
     *
     * @param periode perioden som skal sjekkes.
     *
     * @return true dersom perioden kan flyttes, ellers false.
     */
    private boolean sjekkOmPeriodenErFlyttbar(OppgittPeriode periode) {
        if (periode.getÅrsak() != null ) {
            if (periode.getÅrsak() instanceof UtsettelseÅrsak || periode.getÅrsak() instanceof OppholdÅrsak) {
                return false;
            }
        }
        if (periode.getArbeidsprosent() != null) {
            if (periode.getArbeidsprosent().compareTo(BigDecimal.ZERO) > 0) {
                return false;
            }
        }
        return true;
    }



    private LocalDate førsteLovligeUttaksdato(List<OppgittPeriode> oppgittePerioder, LocalDate fødselsdato) {
        LocalDate førsteUttaksdato = førsteUttaksdato(oppgittePerioder);
        if (fødselsdato.isBefore(førsteUttaksdato)) {
            return fødselsdato;
        }
        return førsteUttaksdato;
    }

    private LocalDate førsteUttaksdato(List<OppgittPeriode> oppgittPerioder) {
        if (oppgittPerioder.isEmpty()) {
            return null;
        }
        return oppgittPerioder.get(0).getFom();
    }

}
