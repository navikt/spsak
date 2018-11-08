package no.nav.foreldrepenger.beregningsgrunnlag;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.Kopimaskin;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Refusjon;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.beregningsgrunnlag.wrapper.IdentifisertePeriodeÅrsaker;
import no.nav.foreldrepenger.beregningsgrunnlag.wrapper.PeriodeSplittData;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class FastsettBeregningsgrunnlagPerioderTjenesteImpl implements FastsettBeregningsgrunnlagPeriodeTjeneste {
    public static final int MÅNEDER_I_1_ÅR = 12;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste;

    FastsettBeregningsgrunnlagPerioderTjenesteImpl() {
        // For CDI
    }

    @Inject
    public FastsettBeregningsgrunnlagPerioderTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                          BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.beregningInntektsmeldingTjeneste = beregningInntektsmeldingTjeneste;
    }

    @Override
    public void fastsettPerioder(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        int antallPerioder = beregningsgrunnlagPerioder.size();
        if (antallPerioder != 1) {
            throw TjenesteFeil.FEILFACTORY.kanIkkeUtvideMedNyePerioder(antallPerioder).toException();
        }
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = beregningsgrunnlagPerioder.get(0);
        IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker = identifiserPeriodeÅrsaker(behandling, beregningsgrunnlag);
        for (Map.Entry<LocalDate, Set<PeriodeSplittData>> entry : identifisertePeriodeÅrsaker.getPeriodeMap().entrySet()) {
            beregningsgrunnlagPeriode = splitBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode, entry.getKey(), entry.getValue(), behandling);
        }
    }

    private BeregningsgrunnlagPeriode splitBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, LocalDate nyPeriodeFom,
                                                                     Set<PeriodeSplittData> periodeSplittData, Behandling behandling) {
        if (nyPeriodeFom.isBefore(beregningsgrunnlagPeriode.getBeregningsgrunnlag().getSkjæringstidspunkt())) {
            return beregningsgrunnlagPeriode;
        }
        if (beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom().isEqual(nyPeriodeFom)) {
            BeregningsgrunnlagPeriode.builder(beregningsgrunnlagPeriode).leggTilPeriodeÅrsaker(getPeriodeÅrsaker(periodeSplittData));
            settRefusjonskravForFørstePeriode(beregningsgrunnlagPeriode, periodeSplittData);
            return beregningsgrunnlagPeriode;
        }
        BeregningsgrunnlagPeriode nyPeriode = oppdaterOgLagNyPeriode(beregningsgrunnlagPeriode, nyPeriodeFom, periodeSplittData);
        lagAndelerForArbeidsforholdSomTilkomEtterStp(periodeSplittData, nyPeriode, behandling);
        oppdaterRefusjonskravForAndeler(periodeSplittData, nyPeriode);
        return nyPeriode;
    }

    private void settRefusjonskravForFørstePeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, Set<PeriodeSplittData> periodeSplittData) {
        periodeSplittData.forEach(splittData -> {
            if (PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV.equals(splittData.getPeriodeÅrsak()) || PeriodeÅrsak.REFUSJON_OPPHØRER.equals(splittData.getPeriodeÅrsak())) {
                throw TjenesteFeil.FEILFACTORY.ugyldigInntektsmelding().toException();
            }
            Optional<BeregningsgrunnlagPrStatusOgAndel> andelOpt = finnAndelIPeriode(beregningsgrunnlagPeriode, splittData);
            if (splittData.getPeriodeÅrsak().equals(PeriodeÅrsak.UDEFINERT)) {
                andelOpt.ifPresent(andel -> settRefusjonsbeløp(splittData.getRefusjonsbeløp().map(Beløp::getVerdi).orElse(BigDecimal.ZERO), andel, beregningsgrunnlagPeriode));
            }
        });
    }

    private BeregningsgrunnlagPeriode oppdaterOgLagNyPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, LocalDate nyPeriodeFom, Set<PeriodeSplittData> periodeSplittData) {
        LocalDate eksisterendePeriodeTom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom();
        BeregningsgrunnlagPeriode.builder(beregningsgrunnlagPeriode)
            .medBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), nyPeriodeFom.minusDays(1));

        BeregningsgrunnlagPeriode nyPeriode = Kopimaskin.deepCopy(beregningsgrunnlagPeriode);
        BeregningsgrunnlagPeriode.builder(nyPeriode)
            .medBeregningsgrunnlagPeriode(nyPeriodeFom, eksisterendePeriodeTom)
            .tilbakestillPeriodeÅrsaker()
            .leggTilPeriodeÅrsaker(getPeriodeÅrsaker(periodeSplittData))
            .build(beregningsgrunnlagPeriode.getBeregningsgrunnlag());
        return nyPeriode;
    }

    private void lagAndelerForArbeidsforholdSomTilkomEtterStp(Set<PeriodeSplittData> periodeSplittData, BeregningsgrunnlagPeriode nyPeriode, Behandling behandling) {
        periodeSplittData.stream()
            .filter(this::skalSplittePeriodeForGraderingEllerRefusjon)
            .forEach(data -> lagAndelerForNyeArbeidsforhold(behandling, nyPeriode, data.getInntektsmelding()));
    }

    private boolean skalSplittePeriodeForGraderingEllerRefusjon(PeriodeSplittData data) {
        return Arrays.asList(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, PeriodeÅrsak.GRADERING).contains(data.getPeriodeÅrsak());
    }

    private List<PeriodeÅrsak> getPeriodeÅrsaker(Set<PeriodeSplittData> periodeSplittData) {
        return periodeSplittData.stream()
            .map(PeriodeSplittData::getPeriodeÅrsak)
            .filter(periodeÅrsak -> !PeriodeÅrsak.UDEFINERT.equals(periodeÅrsak))
            .collect(Collectors.toList());
    }

    private void oppdaterRefusjonskravForAndeler(Collection<PeriodeSplittData> periodeSplittData, BeregningsgrunnlagPeriode etterSplitt) {
        periodeSplittData.forEach(splittData -> {
            Optional<BeregningsgrunnlagPrStatusOgAndel> andelEtterSplitt = finnAndelIPeriode(etterSplitt, splittData);
            if (PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV.equals(splittData.getPeriodeÅrsak())) {
                andelEtterSplitt.ifPresent(andel -> settRefusjonsbeløp(splittData.getRefusjonsbeløp().orElse(Beløp.ZERO).getVerdi(), andel, etterSplitt));
            }
            if (PeriodeÅrsak.REFUSJON_OPPHØRER.equals(splittData.getPeriodeÅrsak())) {
                andelEtterSplitt.ifPresent(andel -> settRefusjonsbeløp(BigDecimal.ZERO, andel, etterSplitt));
            }
        });
    }

    private Optional<BeregningsgrunnlagPrStatusOgAndel> finnAndelIPeriode(BeregningsgrunnlagPeriode etterSplitt, PeriodeSplittData data) {
        return etterSplitt.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> harSammeOrgNr(andel, data))
            .filter(andel -> harSammeArbeidsforholdId(andel, data))
            .findFirst();
    }

    private boolean harSammeOrgNr(BeregningsgrunnlagPrStatusOgAndel andel, PeriodeSplittData data) {
        Optional<String> orgNr = andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr);
        return orgNr.filter(nr -> nr.equals(data.getOrgNr())).isPresent();
    }

    private boolean harSammeArbeidsforholdId(BeregningsgrunnlagPrStatusOgAndel andel, PeriodeSplittData data) {
        Optional<ArbeidsforholdRef> arbeidsforholdRef = andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef);
        return !data.getArbeidsforholdRef().isPresent() || !arbeidsforholdRef.isPresent()
            || arbeidsforholdRef.get().gjelderFor(data.getArbeidsforholdRef().get());
    }

    private void settRefusjonsbeløp(BigDecimal refusjonPrMnd, BeregningsgrunnlagPrStatusOgAndel andel, BeregningsgrunnlagPeriode periode) {
        BigDecimal refusjonsbeløp = refusjonPrMnd.multiply(BigDecimal.valueOf(MÅNEDER_I_1_ÅR));
        BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold = BGAndelArbeidsforhold
            .builder(andel.getBgAndelArbeidsforhold().orElse(null))
            .medRefusjonskravPrÅr(refusjonsbeløp);
        BeregningsgrunnlagPrStatusOgAndel.builder(andel)
            .medBGAndelArbeidsforhold(bgAndelArbeidsforhold)
            .build(periode);
    }

    @Override
    public IdentifisertePeriodeÅrsaker identifiserPeriodeÅrsaker(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();
        List<Inntektsmelding> inntektsmeldinger = inntektArbeidYtelseTjeneste.hentAlleInntektsmeldinger(behandling);

        IdentifisertePeriodeÅrsaker map = new IdentifisertePeriodeÅrsaker();
        inntektsmeldinger.forEach(inntektsmelding -> {
            identifiserPerioderForRefusjon(map, inntektsmelding, skjæringstidspunkt);
            identifiserPerioderForNaturalytelse(skjæringstidspunkt, map, inntektsmelding);
            identifiserPerioderForGradering(map, behandling, skjæringstidspunkt, inntektsmelding, beregningsgrunnlag);
        });
        return map;
    }

    private void identifiserPerioderForRefusjon(IdentifisertePeriodeÅrsaker map, Inntektsmelding inntektsmelding, LocalDate skjæringstidspunkt) {
        identifiserRefusjonFraStart(map, inntektsmelding, skjæringstidspunkt);
        identifiserPerioderForEndringIRefusjonskrav(map, inntektsmelding);
        identifiserPeriodeForOpphørAvRefusjon(map, inntektsmelding, skjæringstidspunkt);
    }

    private void identifiserRefusjonFraStart(IdentifisertePeriodeÅrsaker map, Inntektsmelding inntektsmelding, LocalDate skjæringstidspunkt) {
        if (inntektsmelding.getRefusjonBeløpPerMnd() != null) {
            PeriodeSplittData splittData = PeriodeSplittData.builder()
                .medPeriodeÅrsak(PeriodeÅrsak.UDEFINERT) //UDEFINERT årsak siden det teknisk sett ikke er en ny periode
                .medInformasjonFraInntektsmelding(inntektsmelding).build();
            map.leggTilPeriodeÅrsak(skjæringstidspunkt, splittData);
        }
    }

    private void identifiserPeriodeForOpphørAvRefusjon(IdentifisertePeriodeÅrsaker map, Inntektsmelding inntektsmelding, LocalDate skjæringstidspunkt) {
        LocalDate refusjonOpphører = Optional.ofNullable(inntektsmelding.getRefusjonOpphører()).orElse(TIDENES_ENDE);
        if (!refusjonOpphører.isEqual(TIDENES_ENDE) && refusjonOpphører.plusDays(1).isAfter(skjæringstidspunkt)) {
            PeriodeSplittData splittData = PeriodeSplittData.builder()
                .medPeriodeÅrsak(PeriodeÅrsak.REFUSJON_OPPHØRER)
                .medInformasjonFraInntektsmelding(inntektsmelding).build();
            map.leggTilPeriodeÅrsak(inntektsmelding.getRefusjonOpphører().plusDays(1), splittData);
        }
    }

    private void identifiserPerioderForGradering(IdentifisertePeriodeÅrsaker map, Behandling behandling, LocalDate skjæringstidspunkt,
                                                 Inntektsmelding inntektsmelding, Beregningsgrunnlag beregningsgrunnlag) {
        if (inntektsmelding.getRefusjonBeløpPerMnd() != null && !inntektsmelding.getRefusjonBeløpPerMnd().erNulltall()) {
            return;
        }
        inntektsmelding.getGraderinger().forEach(gradering -> {
            LocalDate graderingFom = gradering.getPeriode().getFomDato();
            boolean totaltRefusjonskravStørreEnn6G = beregningInntektsmeldingTjeneste.erTotaltRefusjonskravStørreEnnSeksG(behandling, beregningsgrunnlag, graderingFom);
            if (totaltRefusjonskravStørreEnn6G || starterArbeidsforholdEtterSkjæringstidspunkt(behandling, skjæringstidspunkt, inntektsmelding)) {
                PeriodeSplittData splittData = PeriodeSplittData.builder()
                    .medPeriodeÅrsak(PeriodeÅrsak.GRADERING)
                    .medInformasjonFraInntektsmelding(inntektsmelding).build();
                map.leggTilPeriodeÅrsak(graderingFom, splittData);
            }
        });
    }


    private void lagAndelerForNyeArbeidsforhold(Behandling behandling, BeregningsgrunnlagPeriode nyPeriode, Inntektsmelding inntektsmelding) {
        Beregningsgrunnlag beregningsgrunnlag = nyPeriode.getBeregningsgrunnlag();
        if (!starterArbeidsforholdEtterSkjæringstidspunkt(behandling, beregningsgrunnlag.getSkjæringstidspunkt(), inntektsmelding)) {
            return;
        }
        Optional<AktørArbeid> aktørArbeidEtterStp = inntektArbeidYtelseTjeneste.hentAggregat(behandling).getAktørArbeidEtterStp(behandling.getAktørId());
        if (!aktørArbeidEtterStp.isPresent()) {
            return;
        }
        Collection<Yrkesaktivitet> yrkesaktiviteterEtterStp = aktørArbeidEtterStp.get().getYrkesaktiviteter();
        Optional<AktivitetsAvtale> ansettelsesPeriode = getAnsettelsesPeriodeForInntektsmelding(inntektsmelding, yrkesaktiviteterEtterStp);
        LocalDate arbeidsperiodeFom = ansettelsesPeriode.map(AktivitetsAvtale::getFraOgMed).orElse(null);
        LocalDate arbeidsperiodeTom = ansettelsesPeriode.map(AktivitetsAvtale::getTilOgMed).orElse(null);
        DatoIntervallEntitet beregningsperiode = BeregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(beregningsgrunnlag.getSkjæringstidspunkt());
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .filter(bgPeriode -> !bgPeriode.getBeregningsgrunnlagPeriodeFom().isBefore(nyPeriode.getBeregningsgrunnlagPeriodeFom()))
            .forEach(beregningsgrunnlagPeriode -> {
                BGAndelArbeidsforhold.Builder bgArbeidsforholdBuilder = BGAndelArbeidsforhold.builder()
                    // TODO TOPAS Inntektsmelding støtter ikke at arbeidsgiver er privatperson, må endre koden her når dette støttes
                    .medArbeidsgiver(Arbeidsgiver.virksomhet(inntektsmelding.getVirksomhet()))
                    .medArbforholdRef(inntektsmelding.getArbeidsforholdRef().getReferanse())
                    .medArbeidsperiodeTom(arbeidsperiodeTom)
                    .medArbeidsperiodeFom(arbeidsperiodeFom);

                BeregningsgrunnlagPrStatusOgAndel.builder()
                    .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .medBeregningsperiode(beregningsperiode.getFomDato(), beregningsperiode.getTomDato())
                    .medBGAndelArbeidsforhold(bgArbeidsforholdBuilder)
                    .build(beregningsgrunnlagPeriode);
            });
    }

    private Optional<AktivitetsAvtale> getAnsettelsesPeriodeForInntektsmelding(Inntektsmelding inntektsmelding, Collection<Yrkesaktivitet> yrkesaktiviteter) {
        Optional<Yrkesaktivitet> yrkesaktivitet = yrkesaktiviteter.stream()
            .filter(ya -> ya.getArbeidsgiver() != null)
            .filter(ya -> gjelderSammeArbeidsforhold(ya, inntektsmelding))
            .findFirst();
        return yrkesaktivitet.flatMap(Yrkesaktivitet::getAnsettelsesPeriode);
    }

    private boolean starterArbeidsforholdEtterSkjæringstidspunkt(Behandling behandling, LocalDate skjæringstidspunkt, Inntektsmelding inntektsmelding) {
        InntektArbeidYtelseGrunnlag iayGrunnlag = inntektArbeidYtelseTjeneste.hentAggregat(behandling);
        Collection<Yrkesaktivitet> yrkesaktiviteterFørStp = iayGrunnlag.getAktørArbeidFørStp(behandling.getAktørId())
            .map(AktørArbeid::getYrkesaktiviteter)
            .orElse(Collections.emptyList());
        Optional<AktivitetsAvtale> ansettelsesPeriodeOpt = getAnsettelsesPeriodeForInntektsmelding(inntektsmelding, yrkesaktiviteterFørStp);
        if (ansettelsesPeriodeOpt.isPresent()) {
            AktivitetsAvtale ansettelsesPeriode = ansettelsesPeriodeOpt.get();
            return ansettelsesPeriode.getFraOgMed().isAfter(skjæringstidspunkt);
        }
        Collection<Yrkesaktivitet> yrkesaktiviteterEtterStp = iayGrunnlag.getAktørArbeidEtterStp(behandling.getAktørId())
            .map(AktørArbeid::getYrkesaktiviteter)
            .orElse(Collections.emptyList());
        Optional<AktivitetsAvtale> ansettelsesPeriodeEtterStpOpt = getAnsettelsesPeriodeForInntektsmelding(inntektsmelding, yrkesaktiviteterEtterStp);
        return ansettelsesPeriodeEtterStpOpt.filter(ansettelsesPeriode -> ansettelsesPeriode.getFraOgMed().isAfter(skjæringstidspunkt)).isPresent();
    }

    // gjelder Yrkesaktiviteten ya for det samme arbeidsforholdet som inntektsmeldingen
    private boolean gjelderSammeArbeidsforhold(Yrkesaktivitet ya, Inntektsmelding inntektsmelding) {
        if (!Objects.equals(ya.getArbeidsgiver().getVirksomhet(), inntektsmelding.getVirksomhet())) {
            return false;
        }
        if (!inntektsmelding.gjelderForEtSpesifiktArbeidsforhold()) {
            return true;
        }
        return ya.getArbeidsforholdRef()
            .map(ar -> ar.gjelderFor(inntektsmelding.getArbeidsforholdRef()))
            .orElse(false);
    }

    private void identifiserPerioderForNaturalytelse(LocalDate skjæringstidspunkt, IdentifisertePeriodeÅrsaker map, Inntektsmelding inntektsmelding) {
        inntektsmelding.getNaturalYtelser().forEach(naturalYtelse -> {
            LocalDate naturalYtelseFom = naturalYtelse.getPeriode().getFomDato();
            PeriodeSplittData.Builder builder = PeriodeSplittData.builder().medInformasjonFraInntektsmelding(inntektsmelding);
            if (naturalYtelseFom.isAfter(skjæringstidspunkt)) {
                PeriodeSplittData splittData = builder.medPeriodeÅrsak(PeriodeÅrsak.NATURALYTELSE_TILKOMMER).build();
                map.leggTilPeriodeÅrsak(naturalYtelseFom, splittData);
            }
            LocalDate naturalYtelseTom = naturalYtelse.getPeriode().getTomDato();
            if (!naturalYtelseTom.equals(TIDENES_ENDE) && naturalYtelseTom.isAfter(skjæringstidspunkt)) {
                LocalDate startDatoNyPeriode = naturalYtelseTom.plusDays(1);
                PeriodeSplittData splittData = builder.medPeriodeÅrsak(PeriodeÅrsak.NATURALYTELSE_BORTFALT).build();
                map.leggTilPeriodeÅrsak(startDatoNyPeriode, splittData);
            }
        });
    }

    private void identifiserPerioderForEndringIRefusjonskrav(IdentifisertePeriodeÅrsaker map, Inntektsmelding inntektsmelding) {
        List<Refusjon> endringerRefusjon = inntektsmelding.getEndringerRefusjon();
        endringerRefusjon.forEach(endringRefusjon -> {
            PeriodeSplittData splittData = PeriodeSplittData.builder()
                .medPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
                .medInntektsmelding(inntektsmelding)
                .medArbeidsforholdRef(inntektsmelding.gjelderForEtSpesifiktArbeidsforhold() ? inntektsmelding.getArbeidsforholdRef() : null)
                .medOrgNr(inntektsmelding.getVirksomhet().getOrgnr())
                .medRefusjonsBeløp(endringRefusjon.getRefusjonsbeløp())
                .build();
            map.leggTilPeriodeÅrsak(endringRefusjon.getFom(), splittData);
        });
    }

    interface TjenesteFeil extends DeklarerteFeil {
        TjenesteFeil FEILFACTORY = FeilFactory.create(TjenesteFeil.class);

        @TekniskFeil(feilkode = "FP-370603", feilmelding = "Kan bare utvide med nye perioder når det fra før finnes 1 periode, fant %s", logLevel = LogLevel.WARN)
        Feil kanIkkeUtvideMedNyePerioder(int antallPerioder);

        @TekniskFeil(feilkode = "FP-370604", feilmelding = "Ugyldig inntektsmelding. Endring eller opphør av refusjon på skjæringstidspunktet.", logLevel = LogLevel.WARN)
        Feil ugyldigInntektsmelding();
    }
}
