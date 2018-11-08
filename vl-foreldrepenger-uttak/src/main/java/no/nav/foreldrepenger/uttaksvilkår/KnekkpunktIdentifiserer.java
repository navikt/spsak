package no.nav.foreldrepenger.uttaksvilkår;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.feriedager.BevegeligeHelligdagerUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

class KnekkpunktIdentifiserer {

    private KnekkpunktIdentifiserer() {
        //hindrer instansiering
    }

    static Set<LocalDate> finnKnekkpunkter(FastsettePeriodeGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        LocalDate familiehendelseDato = grunnlag.getFamiliehendelse();
        LocalDate minimumsgrenseForLovligUttak = familiehendelseDato.minusWeeks(konfigurasjon.getParameter(Parametertype.LOVLIG_UTTAK_FØR_FØDSEL_UKER, familiehendelseDato));
        LocalDate maksimumsgrenseForLovligeUttak = grunnlag.getMaksgrenseForLovligeUttaksdag(konfigurasjon);
        List<LocalDate> bevegeligeHelligdager = finnKnekkpunktPåBevegeligeHelligdagerI(new LukketPeriode(minimumsgrenseForLovligUttak, maksimumsgrenseForLovligeUttak));

        Set<LocalDate> knekkpunkter = new TreeSet<>();
        knekkpunkter.add(minimumsgrenseForLovligUttak);
        knekkpunkter.add(grunnlag.getFørsteLovligeUttaksdag());
        knekkpunkter.add(familiehendelseDato);
        knekkpunkter.add(maksimumsgrenseForLovligeUttak);
        if (grunnlag.getSøknadstype() == Søknadstype.FØDSEL) {
            leggTilKnekkpunkterForFødsel(knekkpunkter, familiehendelseDato, konfigurasjon);
        }
        if (grunnlag.getPerioderMedFerie().length != 0) {
            leggTilKnekkpunkterForUtsettelsePgaFerie(knekkpunkter, bevegeligeHelligdager, grunnlag.getPerioderMedFerie());
        }
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getGyldigGrunnPerioder());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getPerioderUtenOmsorg());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getUttakPerioder());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getPerioderMedFerie());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getPerioderMedFulltArbeid());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getPerioderMedArbeid());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getPerioderMedSykdomEllerSkade());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getPerioderMedInnleggelse());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getPerioderMedBarnInnlagt());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getTrekkdagertilstand().getUttakPerioderAnnenPart());
        if (grunnlag.erEndringssøknad()) {
            leggTilKnekkpunkterVedEndringssøknad(knekkpunkter, grunnlag);
        }
        leggTilKnekkpunkterMenIkkeHvisKnekkErMandagOgDetErKnekkIHelgaFør(knekkpunkter, grunnlag.getArbeidsprosenter().getAlleEndringstidspunkter());

        return knekkpunkter.stream()
                .filter(k -> !k.isBefore(minimumsgrenseForLovligUttak))
                .filter(k -> !k.isAfter(maksimumsgrenseForLovligeUttak))
                .collect(Collectors.toSet());
    }

    private static void leggTilKnekkpunkterVedEndringssøknad(Set<LocalDate> knekkpunkter, FastsettePeriodeGrunnlag grunnlag) {
        leggTilKnekkpunkterVedEndringssøknadGradering(knekkpunkter, grunnlag);
        leggTilKnekkpunkterVedEndringssøknadUtsettelse(knekkpunkter, grunnlag);
    }

    private static void leggTilKnekkpunkterVedEndringssøknadGradering(Set<LocalDate> knekkpunkter,
                                                                      FastsettePeriodeGrunnlag grunnlag) {
        for (UttakPeriode uttakPeriode : grunnlag.getUttakPerioder()) {
            if (uttakPeriode.harGradering() && !uttakPeriode.getFom().isAfter(grunnlag.getEndringssøknadMottattdato())) {
                knekkpunkter.add(grunnlag.getEndringssøknadMottattdato());
            }
        }
    }

    private static void leggTilKnekkpunkterVedEndringssøknadUtsettelse(Set<LocalDate> knekkpunkter, FastsettePeriodeGrunnlag grunnlag) {
        for (UttakPeriode uttakPeriode : grunnlag.getUttakPerioder()) {
            if (uttakPeriode instanceof UtsettelsePeriode && !uttakPeriode.getFom().isAfter(grunnlag.getEndringssøknadMottattdato())) {
                knekkpunkter.add(grunnlag.getEndringssøknadMottattdato());
            }
        }
    }

    private static void leggTilKnekkpunkter(Set<LocalDate> knekkpunkter, List<FastsattPeriodeAnnenPart> uttakPerioderAnnenPart) {
        if (uttakPerioderAnnenPart.isEmpty()) {
            return;
        }
        for (FastsattPeriodeAnnenPart periodeAnnenPart : uttakPerioderAnnenPart) {
            knekkpunkter.add(periodeAnnenPart.getFom());
            knekkpunkter.add(periodeAnnenPart.getTom().plusDays(1));
        }
    }


    private static List<LocalDate> finnKnekkpunktPåBevegeligeHelligdagerI(LukketPeriode uttaksperiode) {
        List<LocalDate> knekkpunkt = new ArrayList<>();
        for (LocalDate knekkpunktet : BevegeligeHelligdagerUtil.finnBevegeligeHelligdagerUtenHelg(uttaksperiode)) {
            knekkpunkt.add(knekkpunktet);
            knekkpunkt.add(knekkpunktet.plusDays(1));
        }

        return BevegeligeHelligdagerUtil.fjernHelg(knekkpunkt);
    }

    private static void leggTilKnekkpunkterForUtsettelsePgaFerie(Set<LocalDate> knekkpunkter, List<LocalDate> bevegeligeHelligdager, Periode... utsettelsePerioder) {
        for (Periode periode : utsettelsePerioder) {
            for (LocalDate helligdag : bevegeligeHelligdager) {
                if (helligdag.isAfter(periode.getFom()) && helligdag.isBefore(periode.getTom())) {
                    knekkpunkter.add(helligdag);
                }
            }
        }
    }

    private static void leggTilKnekkpunkterMenIkkeHvisKnekkErMandagOgDetErKnekkIHelgaFør(Set<LocalDate> knekkpunkter, Set<LocalDate> alleEndringstidspunkter) {
        for (LocalDate kandidat : alleEndringstidspunkter) {
            if (kandidat.getDayOfWeek() == DayOfWeek.MONDAY && (knekkpunkter.contains(kandidat.minusDays(1)) || knekkpunkter.contains(kandidat.minusDays(2)))) {
                continue;
            }
            knekkpunkter.add(kandidat);
        }
    }

    private static void leggTilKnekkpunkterForFødsel(Set<LocalDate> knekkpunkter, LocalDate familiehendelseDato, Konfigurasjon konfigurasjon) {
        knekkpunkter.add(familiehendelseDato.minusWeeks(konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, familiehendelseDato)));
        knekkpunkter.add(familiehendelseDato.plusWeeks(konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelseDato)));
    }

    private static void leggTilKnekkpunkter(Set<LocalDate> knekkpunkter, Periode... perioder) {
        for (Periode periode : perioder) {
            knekkpunkter.add(periode.getFom());
            knekkpunkter.add(periode.getTom().plusDays(1));
        }
    }

}
