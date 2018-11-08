package no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder;

import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.AnnenAktivitetDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.ArbeidsforholdDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.PeriodeDto;
import no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

class PeriodeMergerVerktøy {

    private PeriodeMergerVerktøy() {
        //SONAR
    }

    static PeriodeDto slåSammenPerioder(PeriodeDto periodeEn, PeriodeDto periodeTo) {
        periodeEn.setAntallTapteDager(periodeEn.getAntallTapteDager() + periodeTo.getAntallTapteDager());
        periodeEn.setPeriodeTom(periodeTo.getPeriodeTom());
        return periodeEn;
    }

    static boolean sammeStatusOgÅrsak(PeriodeDto periodeEn, PeriodeDto periodeTo) {
        return periodeEn.getInnvilget() == periodeTo.getInnvilget()
            && Objects.equals(periodeEn.getÅrsak(), periodeTo.getÅrsak());
    }

    static boolean erFomRettEtterTomDato(PeriodeDto periodeEn, PeriodeDto periodeTo) {
        LocalDate tomEnDate = DateUtil.convertToLocalDate(DokumentTypeFelles.finnDatoVerdiAvUtenTidSone(periodeEn.getPeriodeTom()));
        LocalDate fomToDate = DateUtil.convertToLocalDate(DokumentTypeFelles.finnDatoVerdiAvUtenTidSone(periodeTo.getPeriodeFom()));
        return tomEnDate.plusDays(1).isEqual(fomToDate);
    }

    static boolean likeAktiviteter(PeriodeDto periodeEn, PeriodeDto periodeTo) {
        return likeArbeidsforhold(periodeEn, periodeTo) && likNæring(periodeEn, periodeTo) && likeAndreAktiviteter(periodeEn, periodeTo);
    }

    static <T> boolean likOptionalStatusOgEqualsHvisFinnes(Optional<T> o1, Optional<T> o2) {
        return o1.isPresent() == o2.isPresent() &&
            (!o1.isPresent() || o1.equals(o2));
    }

    static boolean likeAndreAktiviteter(PeriodeDto periodeEn, PeriodeDto periodeTo) {
        boolean alleMatcher = periodeEn.getAnnenAktivitet().size() == periodeTo.getAnnenAktivitet().size();
        for (AnnenAktivitetDto akt : periodeEn.getAnnenAktivitet()) {
            if (!finnesMatch(akt, periodeTo)) {
                alleMatcher = false;
            }
        }
        return alleMatcher;
    }
    static boolean likNæring(PeriodeDto periodeEn, PeriodeDto periodeTo) {
        return Objects.equals(periodeEn.getNæring(), periodeTo.getNæring());
    }

    static boolean likeArbeidsforhold(PeriodeDto periodeEn, PeriodeDto periodeTo) {
        boolean alleMatcher = periodeEn.getArbeidsforhold().size() == periodeTo.getArbeidsforhold().size();
        for (ArbeidsforholdDto arb : periodeEn.getArbeidsforhold()) {
            if (!finnesMatch(arb, periodeTo)) {
                alleMatcher = false;
            }
        }
        return alleMatcher;
    }

    private static boolean finnesMatch(AnnenAktivitetDto akt, PeriodeDto periode) {
        boolean match = false;
        for (AnnenAktivitetDto akt2 : periode.getAnnenAktivitet()) {
            if (Objects.equals(akt, akt2)) {
                match = true;
            }
        }
        return match;
    }

    private static boolean finnesMatch(ArbeidsforholdDto arb, PeriodeDto periode) {
        boolean match = false;
        for (ArbeidsforholdDto arb2 : periode.getArbeidsforhold()) {
            if (Objects.equals(arb, arb2)) {
                match = true;
            }
        }
        return match;
    }
}
