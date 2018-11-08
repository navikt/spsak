package no.nav.foreldrepenger.uttaksvilkår;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

class OppholdPeriodeTjeneste {

    private OppholdPeriodeTjeneste() {
        // Skal ikke instansieres
    }

    static List<OppholdPeriode> utledOppholdForMorFraOppgittePerioder(FastsettePeriodeGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        LocalDate familiehendelseDato = grunnlag.getFamiliehendelse();
        List<LukketPeriode> uttakPerioder = slåSammenUttakForBeggeParter(grunnlag).stream().sorted(Comparator.comparing(Periode::getFom)).collect(Collectors.toList());
        List<OppholdPeriode> hullFørFødsel = finnHullFørFødsel(uttakPerioder, familiehendelseDato, grunnlag.getSøknadstype(), grunnlag.getGyldigeStønadskontotyper(), konfigurasjon);
        List<OppholdPeriode> hulletterFødsel = finnHullEtterFødsel(uttakPerioder, familiehendelseDato, grunnlag.getGyldigeStønadskontotyper(), konfigurasjon);

        return Stream.of(hullFørFødsel, hulletterFødsel)
                .flatMap(Collection::stream)
                .filter(oppholdPeriode -> !grunnlag.erRevurdering() || !oppholdPeriode.getFom().isBefore(grunnlag.getRevurderingEndringsdato()))
                .collect(Collectors.toList());
    }

    static List<OppholdPeriode> finnHullISøktePerioderOgFastsattePerioderTilAnnenPart(FastsettePeriodeGrunnlag grunnlag) {
        List<LukketPeriode> allePerioder = slåSammenUttakForBeggeParter(grunnlag);
        List<OppholdPeriode> oppholdPerioder = finnHullIPerioder(allePerioder);
        return oppholdPerioder.stream()
                .filter(oppholdPeriode -> !grunnlag.erRevurdering() || !oppholdPeriode.getFom().isBefore(grunnlag.getRevurderingEndringsdato()))
                .collect(Collectors.toList());
    }

    static List<OppholdPeriode> finnHullIPerioder(List<LukketPeriode> perioder) {
        List<LukketPeriode> sortertePerioder = perioder.stream()
                .sorted(Comparator.comparing(LukketPeriode::getFom)).collect(Collectors.toList());

        List<OppholdPeriode> oppholdPerioder = new ArrayList<>();
        LocalDate hullFom = null;
        for (LukketPeriode lukketPeriode : sortertePerioder) {
            if (hullFom == null) {
                hullFom = lukketPeriode.getTom().plusDays(1);
            } else if (hullFom.isBefore(lukketPeriode.getFom())) {
                LocalDate hullTom = lukketPeriode.getFom().minusDays(1);
                if (Virkedager.beregnAntallVirkedager(hullFom, hullTom) > 0) {
                    oppholdPerioder.add(hullPeriode(hullFom, hullTom));
                }
            }
            if (!lukketPeriode.getTom().isBefore(hullFom)) {
                hullFom = lukketPeriode.getTom().plusDays(1);
            }
        }
        return oppholdPerioder;
    }

    private static OppholdPeriode hullPeriode(LocalDate hullFom, LocalDate hullTom) {
        return hullPeriode(hullFom, hullTom, Stønadskontotype.UKJENT);
    }

    private static OppholdPeriode hullPeriode(LocalDate hullFom, LocalDate hullTom, Stønadskontotype type) {
        return new OppholdPeriode(type, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, hullFom, hullTom,
                false, false);
    }


    static List<OppholdPeriode> finnHullInnenforKontrollPeriode(List<LukketPeriode> perioder, LukketPeriode kontrollperiode) {
        Objects.requireNonNull(kontrollperiode, "kontrollperiode");
        List<LukketPeriode> sortertePerioder = perioder.stream()
                .filter(p -> !p.getTom().isBefore(kontrollperiode.getFom()) && !p.getFom().isAfter(kontrollperiode.getTom()))
                .sorted(Comparator.comparing(Periode::getFom))
                .collect(Collectors.toList());

        List<OppholdPeriode> oppholdPerioder = new ArrayList<>();
        LocalDate hullFom = kontrollperiode.getFom();
        for (LukketPeriode lukketPeriode : sortertePerioder) {
            if (hullFom.isBefore(lukketPeriode.getFom())) {
                LocalDate hullTom = lukketPeriode.getFom().minusDays(1);
                if (Virkedager.beregnAntallVirkedager(hullFom, hullTom) > 0) {
                    oppholdPerioder.add(hullPeriode(hullFom, hullTom));
                }
            }
            LocalDate nesteDatoFom = lukketPeriode.getTom().plusDays(1);
            if (nesteDatoFom.isAfter(hullFom)) {
                hullFom = nesteDatoFom;
            }
        }
        if (!hullFom.isAfter(kontrollperiode.getTom())) {
            LocalDate hullTom = kontrollperiode.getTom();
            if (Virkedager.beregnAntallVirkedager(hullFom, hullTom) > 0) {
                oppholdPerioder.add(hullPeriode(hullFom, hullTom));
            }

        }
        return oppholdPerioder;
    }

    private static List<LukketPeriode> slåSammenUttakForBeggeParter(FastsettePeriodeGrunnlag grunnlag) {
        List<LukketPeriode> allePerioder = new ArrayList<>();
        allePerioder.addAll(grunnlag.getTrekkdagertilstand().getUttakPerioderAnnenPart());
        allePerioder.addAll(Arrays.asList(grunnlag.getUttakPerioder()));
        return allePerioder;
    }


    private static List<OppholdPeriode> finnHullFørFødsel(List<LukketPeriode> søktePerioder,
                                                          LocalDate familiehendelseDato,
                                                          Søknadstype søknadstype,
                                                          Set<Stønadskontotype> gyldigeStønadskontotyper,
                                                          Konfigurasjon konfigurasjon) {
        if (Søknadstype.ADOPSJON.equals(søknadstype)) {
            return new ArrayList<>();
        }
        int fellesperiodeFørFødselUker = konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, familiehendelseDato);
        LukketPeriode betingetPeriodeFørFødsel = new LukketPeriode(familiehendelseDato.minusWeeks(fellesperiodeFørFødselUker), familiehendelseDato.minusDays(1));

        List<OppholdPeriode> hullForForeldrepenger_før_fødsel = finnHullInnenforKontrollPeriode(søktePerioder, betingetPeriodeFørFødsel).stream()
                .map(hull -> hullPeriode(hull.getFom(), hull.getTom(), Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))
                .collect(Collectors.toList());

        List<OppholdPeriode> hullForFellesperiode_før_fødsel = new ArrayList<>();

        if (!søktePerioder.isEmpty()) {
            søktePerioder.sort((p1, p2) -> p2.getFom().isAfter(p1.getFom()) ? -1 : 1);

            if (søktePerioder.get(0).getFom().isBefore(familiehendelseDato.minusWeeks(fellesperiodeFørFødselUker))) {

                LukketPeriode betingetFellesperiodeFørFødsel = new LukketPeriode(søktePerioder.get(0).getFom(), familiehendelseDato.minusWeeks(fellesperiodeFørFødselUker).minusDays(1));
                hullForFellesperiode_før_fødsel = finnHullInnenforKontrollPeriode(søktePerioder, betingetFellesperiodeFørFødsel).stream()
                        .map(hull -> {
                            Stønadskontotype type = gyldigeStønadskontotyper.contains(Stønadskontotype.FELLESPERIODE) ? Stønadskontotype.FELLESPERIODE : Stønadskontotype.FORELDREPENGER;
                            return hullPeriode(hull.getFom(), hull.getTom(), type);
                        })
                        .collect(Collectors.toList());
            }
        }
        return Stream.of(hullForFellesperiode_før_fødsel, hullForForeldrepenger_før_fødsel)
                .flatMap(Collection::stream)
                .filter(p -> p.virkedager() > 0)
                .collect(Collectors.toList());
    }


    private static List<OppholdPeriode> finnHullEtterFødsel(List<LukketPeriode> søktePerioder,
                                                            LocalDate familiehendelseDato,
                                                            Set<Stønadskontotype> gyldigeStønadskontotyper,
                                                            Konfigurasjon konfigurasjon) {
        int mødrekvoteEtterFødselUker = konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelseDato);
        LukketPeriode betingetPeriodeEtterFødsel = new LukketPeriode(familiehendelseDato, familiehendelseDato.plusWeeks(mødrekvoteEtterFødselUker).minusDays(1));
        Stønadskontotype stønadskontotype = gyldigeStønadskontotyper.contains(Stønadskontotype.MØDREKVOTE) ? Stønadskontotype.MØDREKVOTE : Stønadskontotype.FORELDREPENGER;
        return finnHullInnenforKontrollPeriode(søktePerioder, betingetPeriodeEtterFødsel).stream()
                .map(hull -> hullPeriode(hull.getFom(), hull.getTom(), stønadskontotype))
                .collect(Collectors.toList());
    }

}
