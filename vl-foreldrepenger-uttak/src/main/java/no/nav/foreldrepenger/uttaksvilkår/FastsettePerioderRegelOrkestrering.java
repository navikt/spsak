package no.nav.foreldrepenger.uttaksvilkår;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeRegel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsprosenter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeBehandler;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeBehandlerImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.KontoUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureToggles;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;
import no.nav.foreldrepenger.uttaksvilkår.feil.UttakRegelFeil;
import no.nav.foreldrepenger.uttaksvilkår.jackson.JacksonJsonConfig;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class FastsettePerioderRegelOrkestrering {

    private JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();

    public List<FastsettePeriodeResultat> fastsettePerioder(FastsettePeriodeGrunnlag grunnlag) {
        return fastsettePerioder(grunnlag, new DefaultFeatureToggles());
    }

    public List<FastsettePeriodeResultat> fastsettePerioder(FastsettePeriodeGrunnlag grunnlag, FeatureToggles featureToggles) {
        return fastsettePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON, featureToggles);
    }

    public List<FastsettePeriodeResultat> fastsettePerioder(FastsettePeriodeGrunnlag grunnlag, Konfigurasjon konfigurasjon, FeatureToggles featureToggles) {
        FastsettePeriodeRegel fastsettePeriodeRegel = new FastsettePeriodeRegel(konfigurasjon, featureToggles);

        FastsettePeriodeBehandler fastsettePeriodeBehandler = new FastsettePeriodeBehandlerImpl(grunnlag);

        if (grunnlag.isSøkerMor() && Søknadstype.FØDSEL.equals(grunnlag.getSøknadstype())) {
            List<OppholdPeriode> ikkeSøktePerioder = OppholdPeriodeTjeneste.utledOppholdForMorFraOppgittePerioder(grunnlag, konfigurasjon);
            grunnlag.medOppholdPerioder(ikkeSøktePerioder);
        }

        List<OppholdPeriode> oppholdPerioder = OppholdPeriodeTjeneste.finnHullISøktePerioderOgFastsattePerioderTilAnnenPart(grunnlag);
        grunnlag.medOppholdPerioder(oppholdPerioder);

        grunnlag.knekkPerioder(KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, konfigurasjon));
        fastsettArbeidsprosenter(grunnlag);

        List<FastsettePeriodeResultat> resultatPerioder = new ArrayList<>();
        boolean manuellBehandlingTrigget = false;
        while (grunnlag.getAktuellPeriode().isPresent()) {
            grunnlag.getTrekkdagertilstand().trekkSaldoForAnnenPartsPerioder(grunnlag.getAktuellPeriode().get());

            FastsettePeriodeResultat fastsettePeriodeResultat;
            if (manuellBehandlingTrigget) {
                fastsettePeriodeBehandler.manuellBehandling();
                fastsettePeriodeResultat = new FastsettePeriodeResultat(grunnlag.hentPeriodeUnderBehandling(), null, null);
            } else {
                Evaluation evaluering = fastsettePeriodeRegel.evaluer(grunnlag);
                String inputJson = toJson(grunnlag);
                String regelJson = EvaluationSerializer.asJson(evaluering);
                Regelresultat regelresultat = new Regelresultat(evaluering);
                fastsettePeriodeResultat = håndterPeriode(fastsettePeriodeBehandler, grunnlag.getArbeidsprosenter(), regelresultat, inputJson, regelJson);
                if (fastsettePeriodeResultat.isManuellBehandling()) {
                    manuellBehandlingTrigget = true;
                }
            }
            resultatPerioder.add(fastsettePeriodeResultat);
            grunnlag.nestePeriode();
        }

        return resultatPerioder;
    }

    private void fastsettArbeidsprosenter(FastsettePeriodeGrunnlag grunnlag) {
        Arbeidsprosenter arbeidsprosenter = grunnlag.getArbeidsprosenter();
        for (UttakPeriode periode : grunnlag.getUttakPerioder()) {
            for (AktivitetIdentifikator aktivitet : grunnlag.getAktiviteter()) {
                BigDecimal arbeidsprosent = arbeidsprosenter.getArbeidsprosent(aktivitet, periode);
                periode.setArbeidsprosent(aktivitet, arbeidsprosent);
            }
        }
    }

    private FastsettePeriodeResultat håndterPeriode(FastsettePeriodeBehandler behandler, Arbeidsprosenter arbeidsprosenter,
                                                    Regelresultat regelresultat, String inputJson, String regelJson) {
        LocalDate knekkpunkt = regelresultat.getKnekkpunkt();
        UtfallType utfallType = regelresultat.getUtfallType();
        switch (utfallType) {
            case AVSLÅTT:
                behandler.avslåAktuellPeriode(knekkpunkt, regelresultat.getAvklaringÅrsak(), arbeidsprosenter, regelresultat.isTrekkDagerFraSaldo(), regelresultat.isUtbetal());
                break;
            case INNVILGET:
                innvilgPeriode(behandler, arbeidsprosenter, regelresultat);
                break;
            case MANUELL_BEHANDLING:
                behandler.manuellBehandling(regelresultat.getManuellbehandlingårsak(), regelresultat.getAvklaringÅrsak(), arbeidsprosenter, regelresultat.isUtbetal());
                break;
            default:
                throw new UnsupportedOperationException(String.format("Ukjent utfalltype: %s", utfallType.name()));
        }

        FastsettePeriodeGrunnlag grunnlag = behandler.grunnlag();
        UttakPeriode periode = grunnlag.hentPeriodeUnderBehandling();

        return new FastsettePeriodeResultat(periode, regelJson, inputJson);
    }

    private void innvilgPeriode(FastsettePeriodeBehandler behandler, Arbeidsprosenter arbeidsprosenter, Regelresultat regelresultat) {
        UtfallType graderingUtfall = regelresultat.getGradering();
        boolean avslåGradering = UtfallType.AVSLÅTT.equals(graderingUtfall);
        GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak = regelresultat.getGraderingIkkeInnvilgetÅrsak();
        LocalDate innvilgetKnekkpunkt = finnInnvilgetKnekkpunkt(behandler);
        behandler.innvilgAktuellPeriode(innvilgetKnekkpunkt, regelresultat.getInnvilgetÅrsak(), avslåGradering, graderingIkkeInnvilgetÅrsak, arbeidsprosenter, regelresultat.isUtbetal());
    }

    private LocalDate finnInnvilgetKnekkpunkt(FastsettePeriodeBehandler behandler) {
        if (behandler.grunnlag().hentPeriodeUnderBehandling() instanceof UtsettelsePeriode) {
            return null;
        }
        return finnKnekkpunkt(KontoUtil.datoKontoGårTom(behandler.grunnlag()), behandler.grunnlag().hentPeriodeUnderBehandling());
    }

    private LocalDate finnKnekkpunkt(LocalDate knekkpunkt, UttakPeriode periode) {
        if (knekkpunkt.isAfter(periode.getTom())) {
            return null;
        }
        return knekkpunkt;
    }

    private String toJson(FastsettePeriodeGrunnlag grunnlag) {
        try {
            return jacksonJsonConfig.toJson(grunnlag);
        } catch (JsonProcessingException e) {
            throw new UttakRegelFeil("Kunne ikke serialisere regelinput for avklaring av uttaksperioder.", e);
        }
    }

    private static class DefaultFeatureToggles implements FeatureToggles { }
}
