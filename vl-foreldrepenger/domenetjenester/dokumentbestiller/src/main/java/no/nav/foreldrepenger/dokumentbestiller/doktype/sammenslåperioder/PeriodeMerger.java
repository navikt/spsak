package no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder;

import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.PeriodeDto;

import java.util.ArrayList;
import java.util.List;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMergerVerktøy.erFomRettEtterTomDato;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMergerVerktøy.likeAktiviteter;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMergerVerktøy.sammeStatusOgÅrsak;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMergerVerktøy.slåSammenPerioder;

public class PeriodeMerger {

    private PeriodeMerger() {
    }

    public static List<PeriodeDto> mergePerioder(List<PeriodeDto> perioder) {
        if (perioder.size() <= 1) {
            return perioder; // ikke noe å se på.
        }
        return slåSammenSammenhengendePerioder(perioder);
    }

    private static List<PeriodeDto> slåSammenSammenhengendePerioder(List<PeriodeDto> perioder) {
        List<PeriodeDto> resultat = new ArrayList<>();
        for (int index = 0; index < perioder.size() - 1; index++) {
            boolean sistePeriode = (index == perioder.size() - 2);
            PeriodeDto periodeEn = perioder.get(index);
            PeriodeDto periodeTo = perioder.get(index + 1);
            if (erPerioderSammenhengendeOgSkalSlåSammen(periodeEn, periodeTo)) {
                PeriodeDto nyPeriode = slåSammenPerioder(periodeEn, periodeTo);
                perioder.set(index + 1, nyPeriode);
                if (sistePeriode) {
                    resultat.add(nyPeriode);
                }
            } else {
                resultat.add(periodeEn);
                if (sistePeriode) {
                    resultat.add(periodeTo);
                }
            }
        }
        return resultat;
    }


    private static boolean erPerioderSammenhengendeOgSkalSlåSammen(PeriodeDto periodeEn, PeriodeDto periodeTo) {
        return sammeStatusOgÅrsak(periodeEn, periodeTo) && likeAktiviteter(periodeEn, periodeTo) && erFomRettEtterTomDato(periodeEn, periodeTo);
    }
}
