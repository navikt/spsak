package no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ReferanseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Hjemmel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetKlassifisering;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettBeregningsgrunnlagPeriodeTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettSkjæringstidspunktOgStatuser;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper.BeregningsgrunnlagRegelResultat;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.konfig.Tid;

public class VerdikjedeTestHjelper {

    static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.APRIL, 10);
    static final LocalDate MOTTATTDATO_INNTEKTSMELDING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final AktørId AKTØR_ID = new AktørId("210195");
    private static final LocalDate TIDENES_BEGYNNELSE = LocalDate.of(1, Month.JANUARY, 1);

    static void verifiserPeriode(BeregningsgrunnlagPeriode periode, LocalDate fom, LocalDate tom, int antallAndeler) {
        verifiserPeriode(periode, fom, tom, antallAndeler, null);
    }

    static void verifiserPeriode(BeregningsgrunnlagPeriode periode, LocalDate fom, LocalDate tom, int antallAndeler, Long dagsats) {
        assertThat(periode.getBeregningsgrunnlagPeriodeFom()).isEqualTo(fom);
        assertThat(periode.getBeregningsgrunnlagPeriodeTom()).isEqualTo(tom);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(antallAndeler);
        assertThat(periode.getDagsats()).isEqualTo(dagsats);
    }

    static void verifiserBeregningsgrunnlagBasis(BeregningsgrunnlagRegelResultat resultat, Hjemmel hjemmel) {
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(resultat.getBeregningsgrunnlag().getHjemmel()).isEqualTo(hjemmel);
    }

    static void verifiserBeregningsgrunnlagMedAksjonspunkt(BeregningsgrunnlagRegelResultat resultat) {
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).hasSize(1);
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
    }

    static void verifiserSammenligningsgrunnlag(Sammenligningsgrunnlag sammenligningsgrunnlag, double rapportertPrÅr, LocalDate fom, LocalDate tom, Long avvikPromille) {
        assertThat(sammenligningsgrunnlag.getRapportertPrÅr().doubleValue()).isEqualTo(rapportertPrÅr, within(0.01));
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeFom()).isEqualTo(fom);
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeTom()).isEqualTo(tom);
        assertThat(sammenligningsgrunnlag.getAvvikPromille()).isEqualTo(avvikPromille);
    }

    static void verifiserBGATførAvkorting(BeregningsgrunnlagPeriode periode, List<Double> bgListe, List<VirksomhetEntitet> virksomhetListe) {
        List<BeregningsgrunnlagPrStatusOgAndel> bgpsaListe = statusliste(periode, AktivitetStatus.ARBEIDSTAKER);
        for (int ix = 0; ix < bgpsaListe.size(); ix++) {
            BeregningsgrunnlagPrStatusOgAndel bgpsa = bgpsaListe.get(ix);
            assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
            final int index = ix;
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)).hasValueSatisfying(virk ->
                assertThat(virk).isEqualTo(virksomhetListe.get(index))
            );
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isEmpty();
            assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
            assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(bgListe.get(ix), within(0.01));
            assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isEqualTo(bgListe.get(ix), within(0.01));

            assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
            assertThat(bgpsa.getAvkortetPrÅr()).isNull();
            assertThat(bgpsa.getRedusertPrÅr()).isNull();

            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr)).isEmpty();

            assertThat(bgpsa.getAvkortetBrukersAndelPrÅr()).isNull();
            assertThat(bgpsa.getRedusertBrukersAndelPrÅr()).isNull();

            assertThat(bgpsa.getMaksimalRefusjonPrÅr()).isNull();
            assertThat(bgpsa.getAvkortetRefusjonPrÅr()).isNull();
            assertThat(bgpsa.getRedusertRefusjonPrÅr()).isNull();
        }
    }

    static void verifiserBGATetterOverstyring(BeregningsgrunnlagPrStatusOgAndel bgpsa,
                                              Double bg,
                                              VirksomhetEntitet virksomhet,
                                              Double overstyrt,
                                              Double avkortet,
                                              Double redusert,
                                              Double maksimalRefusjon,
                                              Double avkortetRefusjon,
                                              Double avkortetBrukersAndel,
                                              Double redusertRefusjon,
                                              Double redusertBrukersAndel) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)).hasValueSatisfying(virk ->
            assertThat(virk).isEqualTo(virksomhet));
        assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isEmpty();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
        assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(bg);
        assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isEqualTo(overstyrt);

        assertThat(bgpsa.getOverstyrtPrÅr().doubleValue()).isEqualTo(overstyrt);
        assertThat(bgpsa.getAvkortetPrÅr().doubleValue()).isCloseTo(avkortet, within(0.01));
        assertThat(bgpsa.getRedusertPrÅr().doubleValue()).isCloseTo(redusert, within(0.01));

        assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr)).isEmpty();

        assertThat(bgpsa.getMaksimalRefusjonPrÅr().doubleValue()).isCloseTo(maksimalRefusjon, within(0.01));
        assertThat(bgpsa.getAvkortetRefusjonPrÅr().doubleValue()).isCloseTo(avkortetRefusjon, within(0.01));
        assertThat(bgpsa.getRedusertRefusjonPrÅr().doubleValue()).isCloseTo(redusertRefusjon, within(0.01));

        assertThat(bgpsa.getAvkortetBrukersAndelPrÅr().doubleValue()).isCloseTo(avkortetBrukersAndel, within(0.01));
        assertThat(bgpsa.getRedusertBrukersAndelPrÅr().doubleValue()).isCloseTo(redusertBrukersAndel, within(0.01));
    }

    static void verifiserBGATetterAvkorting(BeregningsgrunnlagPeriode periode,
                                            List<Double> bgListe,
                                            List<VirksomhetEntitet> virksomhetene,
                                            List<Double> avkortetListe,
                                            List<Double> maksimalRefusjonListe,
                                            List<Double> avkortetRefusjonListe,
                                            List<Double> avkortetBrukersAndelListe, boolean overstyrt) {
        List<BeregningsgrunnlagPrStatusOgAndel> bgpsaListe = statusliste(periode, AktivitetStatus.ARBEIDSTAKER);
        assertThat(bgListe).hasSameSizeAs(bgpsaListe);
        assertThat(avkortetListe).hasSameSizeAs(bgpsaListe);
        assertThat(maksimalRefusjonListe).hasSameSizeAs(bgpsaListe);
        assertThat(avkortetRefusjonListe).hasSameSizeAs(bgpsaListe);
        assertThat(avkortetBrukersAndelListe).hasSameSizeAs(bgpsaListe);
        for (int ix = 0; ix < bgpsaListe.size(); ix++) {
            BeregningsgrunnlagPrStatusOgAndel bgpsa = bgpsaListe.get(ix);
            assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
            final int index = ix;
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)).hasValueSatisfying(virk ->
                assertThat(virk).isEqualTo(virksomhetene.get(index)));
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isEmpty();
            assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
            assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(bgListe.get(ix), within(0.01));
            assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isEqualTo(bgListe.get(ix), within(0.01));

            if (!overstyrt) {
                assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
            }
            assertThat(bgpsa.getAvkortetPrÅr().doubleValue()).isCloseTo(avkortetListe.get(ix), within(0.01));
            assertThat(bgpsa.getRedusertPrÅr().doubleValue()).isCloseTo(avkortetListe.get(ix), within(0.01));

            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr)).isEmpty();

            assertThat(bgpsa.getMaksimalRefusjonPrÅr().doubleValue()).isCloseTo(maksimalRefusjonListe.get(ix), within(0.01));
            assertThat(bgpsa.getAvkortetRefusjonPrÅr().doubleValue()).isCloseTo(avkortetRefusjonListe.get(ix), within(0.01));
            assertThat(bgpsa.getRedusertRefusjonPrÅr().doubleValue()).isCloseTo(avkortetRefusjonListe.get(ix), within(0.01));

            assertThat(bgpsa.getAvkortetBrukersAndelPrÅr().doubleValue()).isCloseTo(avkortetBrukersAndelListe.get(ix), within(0.01));
            assertThat(bgpsa.getRedusertBrukersAndelPrÅr().doubleValue()).isCloseTo(avkortetBrukersAndelListe.get(ix), within(0.01));
        }
    }

    static void verifiserFLførAvkorting(BeregningsgrunnlagPeriode periode, Double bgFL) {
        List<BeregningsgrunnlagPrStatusOgAndel> bgpsaListe = statusliste(periode, AktivitetStatus.FRILANSER);
        assertThat(bgpsaListe).hasSize(1);
        for (BeregningsgrunnlagPrStatusOgAndel bgpsa : bgpsaListe) {
            assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
            assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(bgFL);
            assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isEqualTo(bgFL);

            assertThat(bgpsa.getBeregningsperiodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3).withDayOfMonth(1));
            assertThat(bgpsa.getBeregningsperiodeTom()).isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1));

            assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
            assertThat(bgpsa.getAvkortetPrÅr()).isNull();
            assertThat(bgpsa.getRedusertPrÅr()).isNull();

            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr)).isEmpty();

            assertThat(bgpsa.getAvkortetBrukersAndelPrÅr()).isNull();
            assertThat(bgpsa.getRedusertBrukersAndelPrÅr()).isNull();

            assertThat(bgpsa.getMaksimalRefusjonPrÅr()).isNull();
            assertThat(bgpsa.getAvkortetRefusjonPrÅr()).isNull();
            assertThat(bgpsa.getRedusertRefusjonPrÅr()).isNull();
        }
    }

    static void verifiserFLetterAvkorting(BeregningsgrunnlagPeriode periode, Double bgFL, Double avkortetBgFL, Double brukersAndelFL) {
        List<BeregningsgrunnlagPrStatusOgAndel> bgpsaListe = statusliste(periode, AktivitetStatus.FRILANSER);
        assertThat(bgpsaListe).hasSize(1);
        for (BeregningsgrunnlagPrStatusOgAndel bgpsa : bgpsaListe) {
            assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
            assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(bgFL);
            assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isEqualTo(bgFL);

            assertThat(bgpsa.getBeregningsperiodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3).withDayOfMonth(1));
            assertThat(bgpsa.getBeregningsperiodeTom()).isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1));

            assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
            assertThat(bgpsa.getAvkortetPrÅr().doubleValue()).isEqualTo(avkortetBgFL, within(0.01));
            assertThat(bgpsa.getRedusertPrÅr().doubleValue()).isEqualTo(avkortetBgFL, within(0.01));

            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr)).isEmpty();

            assertThat(bgpsa.getAvkortetBrukersAndelPrÅr().doubleValue()).isCloseTo(brukersAndelFL, within(0.01));
            assertThat(bgpsa.getRedusertBrukersAndelPrÅr().doubleValue()).isCloseTo(brukersAndelFL, within(0.01));

            assertThat(bgpsa.getMaksimalRefusjonPrÅr().doubleValue()).isEqualTo(0.0d);
            assertThat(bgpsa.getAvkortetRefusjonPrÅr().doubleValue()).isEqualTo(0.0d);
            assertThat(bgpsa.getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(0.0d);
        }
    }

    static void verifiserBGSNførAvkorting(BeregningsgrunnlagPeriode periode, double forventetBrutto, int sisteÅr) {
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = statusliste(periode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(andeler).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndel andel = andeler.get(0);
        assertThat(andel.getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)).isEmpty();
        assertThat(andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isEmpty();
        assertThat(andel.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.UDEFINERT);
        assertThat(andel.getBeregnetPrÅr().doubleValue()).isEqualTo(forventetBrutto, within(0.2));
        assertThat(andel.getBruttoPrÅr().doubleValue()).isEqualTo(forventetBrutto, within(0.2));

        assertThat(andel.getBeregningsperiodeFom()).isEqualTo(LocalDate.of(sisteÅr - 2, Month.JANUARY, 1));
        assertThat(andel.getBeregningsperiodeTom()).isEqualTo(LocalDate.of(sisteÅr, Month.DECEMBER, 31));

        assertThat(andel.getOverstyrtPrÅr()).isNull();
        assertThat(andel.getAvkortetPrÅr()).isNull();
        assertThat(andel.getRedusertPrÅr()).isNull();

        assertThat(andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr)).isEmpty();

        assertThat(andel.getAvkortetBrukersAndelPrÅr()).isNull();
        assertThat(andel.getRedusertBrukersAndelPrÅr()).isNull();

        assertThat(andel.getMaksimalRefusjonPrÅr()).isNull();
        assertThat(andel.getAvkortetRefusjonPrÅr()).isNull();
        assertThat(andel.getRedusertRefusjonPrÅr()).isNull();

        assertThat(andel.getPgiSnitt()).isNotNull();
        assertThat(andel.getPgi1()).isNotNull();
        assertThat(andel.getPgi2()).isNotNull();
        assertThat(andel.getPgi3()).isNotNull();
    }

    static void verifiserBGSNetterAvkorting(BeregningsgrunnlagPeriode periode, double forventetBrutto,
                                            double forventetAvkortet, double forventetRedusert, int sisteÅr) {
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = statusliste(periode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(andeler).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndel andel = andeler.get(0);
        assertThat(andel.getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)).isEmpty();
        assertThat(andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isEmpty();
        assertThat(andel.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.UDEFINERT);
        assertThat(andel.getBeregnetPrÅr().doubleValue()).isEqualTo(forventetBrutto, within(0.2));
        assertThat(andel.getBruttoPrÅr().doubleValue()).isEqualTo(forventetBrutto, within(0.2));

        assertThat(andel.getBeregningsperiodeFom()).isEqualTo(LocalDate.of(sisteÅr - 2, Month.JANUARY, 1));
        assertThat(andel.getBeregningsperiodeTom()).isEqualTo(LocalDate.of(sisteÅr, Month.DECEMBER, 31));

        assertThat(andel.getOverstyrtPrÅr()).isNull();
        assertThat(andel.getAvkortetPrÅr().doubleValue()).isEqualTo(forventetAvkortet, within(0.2));
        assertThat(andel.getRedusertPrÅr().doubleValue()).isEqualTo(forventetRedusert, within(0.2));

        assertThat(andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr)).isEmpty();

        assertThat(andel.getMaksimalRefusjonPrÅr().doubleValue()).isEqualTo(0.0);
        assertThat(andel.getAvkortetRefusjonPrÅr().doubleValue()).isEqualTo(0.0);
        assertThat(andel.getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(0.0);

        assertThat(andel.getAvkortetBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetAvkortet, within(0.2));
        assertThat(andel.getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusert, within(0.2));

        assertThat(andel.getPgiSnitt()).isNotNull();
        assertThat(andel.getPgi1()).isNotNull();
        assertThat(andel.getPgi2()).isNotNull();
        assertThat(andel.getPgi3()).isNotNull();
    }

    private static List<BeregningsgrunnlagPrStatusOgAndel> statusliste(BeregningsgrunnlagPeriode periode, AktivitetStatus status) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(bpsa -> status.equals(bpsa.getAktivitetStatus()))
            .sorted(Comparator.comparing(bga -> bga.getBgAndelArbeidsforhold().get().getArbeidsforholdOrgnr()))
            .collect(Collectors.toList());
    }

    static void byggFrilansForBehandling(InntektArbeidYtelseRepository inntektArbeidYtelseRepository,
                                         Behandling behandling,
                                         String arbId,
                                         List<OpptjeningAktivitet> aktiviteter) {

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseRepository.opprettBuilderFor(behandling, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(AKTØR_ID);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(null, arbId, null),
            ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);

        yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER)
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(arbId)))
            .build();

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeid);
        leggTilOpptjening(arbId, aktiviteter, OpptjeningAktivitetType.FRILANS);

        inntektArbeidYtelseRepository.lagre(behandling, inntektArbeidYtelseAggregatBuilder);
    }

    static void byggMilitærForBehandling(InntektArbeidYtelseRepository inntektArbeidYtelseRepository,
                                         Behandling behandling, List<OpptjeningAktivitet> aktiviteter) {

        String SN_ID = "SN";
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseRepository.opprettBuilderFor(behandling, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(AKTØR_ID);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(SN_ID, null, null),
            ArbeidType.MILITÆR_ELLER_SIVILTJENESTE);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeid);

        leggTilOpptjening(null, aktiviteter, OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE);
        inntektArbeidYtelseRepository.lagre(behandling, inntektArbeidYtelseAggregatBuilder);

    }

    public static OpptjeningAktivitet opprettAktivitetFor(String arbId, OpptjeningAktivitetType opptjeningAktivitetType) {
        OpptjeningAktivitet opptjeningAktivitet;
        if (OpptjeningAktivitetType.NÆRING.equals(opptjeningAktivitetType) || OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE.equals(opptjeningAktivitetType)) {

            opptjeningAktivitet = new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1),
                opptjeningAktivitetType,
                OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);

        } else {

            opptjeningAktivitet = new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1),
                opptjeningAktivitetType,
                OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
                arbId,
                ReferanseType.ORG_NR);
        }
        return opptjeningAktivitet;
    }

    public static void leggTilOpptjening(String arbId, List<OpptjeningAktivitet> aktiviteter, OpptjeningAktivitetType opptjeningAktivitetType) {
        aktiviteter.add(opprettAktivitetFor(arbId, opptjeningAktivitetType));
    }

    private static void lagreInntektsmelding(BigDecimal beløp, Behandling behandling,
                                             VirksomhetRepository virksomhetRepository,
                                             InntektArbeidYtelseRepository inntektArbeidYtelseRepository,
                                             String orgnr, BigDecimal refusjonskrav, NaturalYtelse naturalYtelse) {
        LocalDate fødselsdato = SKJÆRINGSTIDSPUNKT_OPPTJENING;

        InntektsmeldingBuilder inntektsmeldingBuilder = InntektsmeldingBuilder.builder();
        inntektsmeldingBuilder.medStartDatoPermisjon(fødselsdato);
        inntektsmeldingBuilder.medInnsendingstidspunkt(LocalDateTime.now());
        inntektsmeldingBuilder.medBeløp(beløp);
        inntektsmeldingBuilder.medJournalpostId(new JournalpostId("123123123"));
        if (naturalYtelse != null) {
            inntektsmeldingBuilder.leggTil(naturalYtelse);
        }
        if (refusjonskrav != null) {
            inntektsmeldingBuilder.medRefusjon(refusjonskrav);
        }

        Optional<Virksomhet> hent = virksomhetRepository.hent(orgnr);
        if (hent.isPresent()) {
            Virksomhet virksomhet = hent.get();
            inntektsmeldingBuilder.medVirksomhet(virksomhet);
        }

        inntektArbeidYtelseRepository.lagre(behandling, inntektsmeldingBuilder.build());
    }

    public static Behandling lagBehandlingForSN(GrunnlagRepositoryProvider repositoryProvider,
                                                ResultatRepositoryProvider resultatRepositoryProvider, ScenarioMorSøkerForeldrepenger scenario,
                                                BigDecimal skattbarInntekt, int førsteÅr) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();
        for (LocalDate året = LocalDate.of(førsteÅr, Month.JANUARY, 1); året.getYear() < førsteÅr + 3; året = året.plusYears(1)) {
            lagInntektForSN(inntektArbeidYtelseBuilder, AKTØR_ID, året, skattbarInntekt);
        }
        return scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    public static Behandling lagBehandlingFor_AT_SN(GrunnlagRepositoryProvider repositoryProvider,
                                                    ResultatRepositoryProvider resultatRepositoryProvider, ScenarioMorSøkerForeldrepenger scenario,
                                                    BigDecimal skattbarInntekt, int førsteÅr, LocalDate skjæringstidspunkt,
                                                    VirksomhetEntitet beregningVirksomhet,
                                                    BigDecimal inntektSammenligningsgrunnlag, BigDecimal inntektBeregningsgrunnlag) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();
        for (LocalDate året = LocalDate.of(førsteÅr, Month.JANUARY, 1); året.getYear() < førsteÅr + 3; året = året.plusYears(1)) {
            lagInntektForSN(inntektArbeidYtelseBuilder, AKTØR_ID, året, skattbarInntekt);
        }
        LocalDate fraOgMed = skjæringstidspunkt.minusYears(1).withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);
        List<DatoIntervallEntitet> perioder = utledPerioderMellomFomTom(fraOgMed, tilOgMed);
        VerdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, AKTØR_ID, perioder,
            inntektSammenligningsgrunnlag, beregningVirksomhet);
        VerdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, AKTØR_ID, perioder,
            List.of(inntektBeregningsgrunnlag), beregningVirksomhet);
        return scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    public static void lagInntektForSN(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId,
                                       LocalDate år, BigDecimal årsinntekt) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);
        AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektsKilde.SIGRUN, null);
        InntektEntitet.InntektspostBuilder inntektspost = InntektEntitet.InntektspostBuilder.ny()
            .medBeløp(årsinntekt)
            .medPeriode(år.withMonth(1).withDayOfMonth(1), år.withMonth(12).withDayOfMonth(31))
            .medInntektspostType(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE);
        inntektBuilder.leggTilInntektspost(inntektspost);
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    public static Behandling lagBehandlingFL(GrunnlagRepositoryProvider repositoryProvider, ResultatRepositoryProvider resultatRepositoryProvider, ScenarioMorSøkerForeldrepenger scenario,
                                             BigDecimal inntektSammenligningsgrunnlag,
                                             BigDecimal inntektFrilans, VirksomhetEntitet beregningVirksomhet, LocalDate fraOgMed, LocalDate tilOgMed) {

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();
        VerdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, AKTØR_ID, fraOgMed, tilOgMed, ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER, beregningVirksomhet);

        List<DatoIntervallEntitet> perioder = utledPerioderMellomFomTom(fraOgMed, tilOgMed);
        VerdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, AKTØR_ID, perioder,
            List.of(inntektFrilans), beregningVirksomhet);
        VerdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, AKTØR_ID, perioder,
            inntektSammenligningsgrunnlag, beregningVirksomhet);

        return scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    public static List<DatoIntervallEntitet> utledPerioderMellomFomTom(LocalDate fraOgMed, LocalDate tilOgMed) {
        List<DatoIntervallEntitet> perioder = new ArrayList<>();
        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            perioder.add(DatoIntervallEntitet.fraOgMedTilOgMed(dt, dt.plusMonths(1)));
        }
        return perioder;
    }

    public static void lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId,
                                      LocalDate fom, LocalDate tom, ArbeidType arbeidType, VirksomhetEntitet... virksomheter) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder
            .getAktørArbeidBuilder(aktørId);

        for (VirksomhetEntitet virksomhet : virksomheter) {

            Opptjeningsnøkkel opptjeningsnøkkel = Opptjeningsnøkkel.forOrgnummer(virksomhet.getOrgnr());

            YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder
                .getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsnøkkel, arbeidType);
            YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

            YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
                .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom))
                .medProsentsats(BigDecimal.valueOf(100));
            yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtale)
                .medArbeidType(arbeidType)
                .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));

            aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        }
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
    }

    public static void lagInntektForSammenligning(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                                  AktørId aktørId, List<DatoIntervallEntitet> perioder, BigDecimal månedsbeløp, VirksomhetEntitet... virksomheter) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);
        BigDecimal kalkulertMånedsBeløpPerVirksomhet = månedsbeløp.divide(new BigDecimal(Stream.of(virksomheter).count()), RoundingMode.HALF_UP);
        List.of(virksomheter).forEach(virksomhet -> {
            Opptjeningsnøkkel opptjeningsnøkkel = Opptjeningsnøkkel.forOrgnummer(virksomhet.getOrgnr());
            InntektsKilde kilde = InntektsKilde.INNTEKT_SAMMENLIGNING;
            AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
            perioder.forEach(periode -> {
                InntektEntitet.InntektspostBuilder inntektspost = InntektEntitet.InntektspostBuilder.ny()
                    .medBeløp(kalkulertMånedsBeløpPerVirksomhet)
                    .medPeriode(periode.getFomDato(), periode.getTomDato())
                    .medInntektspostType(InntektspostType.LØNN);
                inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
            });
            aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        });
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    public static void lagInntektForArbeidsforhold(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                                   AktørId aktørId, List<DatoIntervallEntitet> perioder, List<BigDecimal> månedsbeløp, VirksomhetEntitet... virksomheter) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);
        int i = 0;
        for (VirksomhetEntitet virksomhet : virksomheter) {
            Opptjeningsnøkkel opptjeningsnøkkel = Opptjeningsnøkkel.forOrgnummer(virksomhet.getOrgnr());
            InntektsKilde kilde = InntektsKilde.INNTEKT_BEREGNING;
            AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
            BigDecimal månedslønn = månedsbeløp.get(i);
            Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
            perioder.forEach(periode -> {
                InntektEntitet.InntektspostBuilder inntektspost = InntektEntitet.InntektspostBuilder.ny()
                    .medBeløp(månedslønn)
                    .medPeriode(periode.getFomDato(), periode.getTomDato())
                    .medInntektspostType(InntektspostType.LØNN);
                inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(arbeidsgiver);
            });
            aktørInntektBuilder.leggTilInntekt(inntektBuilder);
            i++;
        }
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    public static void opprettInntektsmelding(GrunnlagRepositoryProvider repositoryProvider, Behandling behandling, VirksomhetEntitet beregningVirksomhet, BigDecimal inntektInntektsmelding) {
        opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet, inntektInntektsmelding, null);
    }

    public static void opprettInntektsmeldingMedRefusjonskrav(GrunnlagRepositoryProvider repositoryProvider, Behandling behandling, VirksomhetEntitet beregningVirksomhet,
                                                              BigDecimal inntektInntektsmelding, BigDecimal refusjonskrav) {
        opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet, inntektInntektsmelding, null, refusjonskrav);
    }

    public static void opprettInntektsmeldingNaturalytelseBortfaller(GrunnlagRepositoryProvider repositoryProvider, Behandling behandling, VirksomhetEntitet beregningVirksomhet, BigDecimal inntektInntektsmelding,
                                                                     BigDecimal naturalytelseBortfaller, LocalDate naturalytelseBortfallerDato) {
        opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet, inntektInntektsmelding,
            new NaturalYtelseEntitet(TIDENES_BEGYNNELSE, naturalytelseBortfallerDato, naturalytelseBortfaller, NaturalYtelseType.ANNET), null);
    }

    public static void opprettInntektsmeldingNaturalytelseTilkommer(GrunnlagRepositoryProvider repositoryProvider, Behandling behandling, VirksomhetEntitet beregningVirksomhet, BigDecimal inntektInntektsmelding,
                                                                    BigDecimal naturalytelseTilkommer, LocalDate naturalytelseTilkommerDato) {
        opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet, inntektInntektsmelding,
            new NaturalYtelseEntitet(naturalytelseTilkommerDato, Tid.TIDENES_ENDE, naturalytelseTilkommer, NaturalYtelseType.ANNET), null);
    }

    public static void opprettInntektsmeldingMedRefusjonskrav(GrunnlagRepositoryProvider repositoryProvider, Behandling behandling, VirksomhetEntitet beregningVirksomhet, BigDecimal inntektInntektsmelding,
                                                              NaturalYtelseEntitet naturalYtelse, BigDecimal refusjonskrav) {
        lagreInntektsmelding(inntektInntektsmelding, behandling,
            repositoryProvider.getVirksomhetRepository(),
            repositoryProvider.getInntektArbeidYtelseRepository(),
            beregningVirksomhet.getOrgnr(),
            refusjonskrav, naturalYtelse);
    }

    public static Beregningsgrunnlag kjørStegOgLagreGrunnlag(Behandling behandling,
                                                             FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser,
                                                             FastsettBeregningsgrunnlagPeriodeTjeneste fastsettBeregningsgrunnlagPeriodeTjeneste,
                                                             BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        Beregningsgrunnlag beregningsgrunnlag = fastsettSkjæringstidspunktOgStatuser.fastsettSkjæringstidspunktOgStatuser(behandling);
        fastsettBeregningsgrunnlagPeriodeTjeneste.fastsettPerioder(behandling, beregningsgrunnlag);
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
        return beregningsgrunnlag;
    }

}
