package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriodeAktivitet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.vedtak.feil.FeilFactory;

class IkkeNegativKontoValidering implements OverstyrUttakPerioderValidering {

    private List<Stønadskonto> stønadskontoer;

    IkkeNegativKontoValidering(List<Stønadskonto> stønadskontoer) {
        this.stønadskontoer = stønadskontoer;
    }

    @Override
    public void utfør(UttakResultatPerioder nyePerioder) {
        List<AktivitetIdentifikator> aktiviteter = hentAktiviteter(nyePerioder);
        for (AktivitetIdentifikator aktivitetIdentifikator : aktiviteter) {
            valideForAktivitet(nyePerioder, aktivitetIdentifikator);
        }
    }

    private void valideForAktivitet(UttakResultatPerioder nyePerioder, AktivitetIdentifikator aktivitetIdentifikator) {
        Map<StønadskontoType, Integer> counter = new HashMap<>();
        for (UttakResultatPeriode periode : nyePerioder.getPerioder()) {
            for (UttakResultatPeriodeAktivitet aktivitet : periode.getAktiviteter()) {
                if (tilAktivitet(aktivitet).equals(aktivitetIdentifikator)) {
                    valider(counter, periode, aktivitet);
                }
            }
        }
    }

    private void valider(Map<StønadskontoType, Integer> counter, UttakResultatPeriode periode, UttakResultatPeriodeAktivitet aktivitet) {
        StønadskontoType type = aktivitet.getTrekkonto();
        if (periode.isFlerbarnsdager()) {
            counter.put(StønadskontoType.FLERBARNSDAGER, counter.getOrDefault(StønadskontoType.FLERBARNSDAGER, 0) + aktivitet.getTrekkdager());
        }
        counter.put(type, counter.getOrDefault(type, 0) + aktivitet.getTrekkdager());
        Optional<Stønadskonto> stønadskonto = kontoForType(type);
        if (stønadskonto.isPresent() && counter.get(type) > stønadskonto.get().getMaxDager()) {
            throw FeilFactory.create(OverstyrUttakValideringFeil.class).trekkdagerOverskriderKontoMaksDager(type).toException();
        }
        Optional<Stønadskonto> flerbarnsdagerKonto = kontoForType(StønadskontoType.FLERBARNSDAGER);
        if (periode.isFlerbarnsdager() && flerbarnsdagerKonto.isPresent() && counter.get(StønadskontoType.FLERBARNSDAGER) > flerbarnsdagerKonto.get().getMaxDager()) {
            throw FeilFactory.create(OverstyrUttakValideringFeil.class).trekkdagerOverskriderKontoMaksDager(type).toException();
        }
        if (!stønadskonto.isPresent()) {
            throw FeilFactory.create(OverstyrUttakValideringFeil.class).manglerStønadskonto(type).toException();
        }
    }

    private List<AktivitetIdentifikator> hentAktiviteter(UttakResultatPerioder perioder) {
        Set<AktivitetIdentifikator> aktiviteter = new HashSet<>();
        for (UttakResultatPeriode periode : perioder.getPerioder()) {
            for (UttakResultatPeriodeAktivitet aktivitet : periode.getAktiviteter()) {
                aktiviteter.add(tilAktivitet(aktivitet));
            }
        }
        return new ArrayList<>(aktiviteter);
    }

    private AktivitetIdentifikator tilAktivitet(UttakResultatPeriodeAktivitet periode) {
        if (periode.getUttakArbeidType().equals(UttakArbeidType.ORDINÆRT_ARBEID)) {
            return AktivitetIdentifikator.forArbeid(periode.getArbeidsforholdOrgnr(), periode.getArbeidsforholdId());
        } else if (periode.getUttakArbeidType().equals(UttakArbeidType.FRILANS)) {
            return AktivitetIdentifikator.forFrilans();
        } else if (periode.getUttakArbeidType().equals(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE)) {
            return AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        } else if (periode.getUttakArbeidType().equals(UttakArbeidType.ANNET)) {
            return AktivitetIdentifikator.annenAktivitet();
        }
        return AktivitetIdentifikator.ingenAktivitet();
    }

    private Optional<Stønadskonto> kontoForType(StønadskontoType type) {
        return stønadskontoer.stream().filter(k -> Objects.equals(k.getStønadskontoType(), type)).findFirst();
    }
}
