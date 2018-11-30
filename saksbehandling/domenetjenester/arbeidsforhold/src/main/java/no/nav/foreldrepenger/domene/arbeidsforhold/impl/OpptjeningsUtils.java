package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseAnvist;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

class OpptjeningsUtils {

    private OpptjeningsUtils() {
    }

    static Opptjeningsnøkkel lagOpptjeningsnøkkel(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref) {
        // tar ikke hensyn til arbeidsforhold i Opptjening, men må skille mellom organisasjon som er arbeidsgiver og person.
        return new Opptjeningsnøkkel(ref, arbeidsgiver);
    }

    static DatoIntervallEntitet hentUtDatoIntervall(Ytelse ytelse, YtelseAnvist ytelseAnvist) {
        LocalDate tom = ytelseAnvist.getAnvistTOM();
        if (tom != null) {
            if (Fagsystem.INFOTRYGD.equals(ytelse.getKilde()) && DayOfWeek.THURSDAY.getValue() < DayOfWeek.from(tom).getValue()) {
                tom = tom.plusDays((long)DayOfWeek.SUNDAY.getValue() - DayOfWeek.from(tom).getValue());
            }
            return DatoIntervallEntitet.fraOgMedTilOgMed(ytelseAnvist.getAnvistFOM(), tom);
        }
        return DatoIntervallEntitet.fraOgMed(ytelseAnvist.getAnvistFOM());
    }

    static DatoIntervallEntitet slåSammenOverlappendeDatoIntervall(DatoIntervallEntitet periode1, DatoIntervallEntitet periode2) {
        LocalDate fom = periode1.getFomDato();
        if (periode2.getFomDato().isBefore(fom)) {
            fom = periode2.getFomDato();
        }
        LocalDate tom = periode2.getTomDato();
        if (periode1.getTomDato().isAfter(tom)) {
            tom = periode1.getTomDato();
        }
        return DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    static boolean erTilgrensende(DatoIntervallEntitet periode1, DatoIntervallEntitet periode2) {
        Interval p1 = periode1.tilIntervall();
        Interval p2 = periode2.tilIntervall();
        return p1.isConnected(p2) || p2.isConnected(p1) || periode1.getTomDato().plusDays(1).equals(periode2.getFomDato()) || periode2.getTomDato().plusDays(1).equals(periode1.getFomDato());
    }

}
