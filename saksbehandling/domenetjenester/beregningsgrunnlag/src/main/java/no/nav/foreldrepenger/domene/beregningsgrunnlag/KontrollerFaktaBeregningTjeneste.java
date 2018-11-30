package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

public interface KontrollerFaktaBeregningTjeneste {

    Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> hentAndelerForKortvarigeArbeidsforhold(Behandling behandling);

    boolean erNyIArbeidslivetMedAktivitetStatusSN(Behandling behandling);

    boolean brukerMedAktivitetStatusTY(Behandling behandling);

    boolean vurderManuellBehandlingForEndretBeregningsgrunnlag(Behandling behandling);

    boolean tilkomArbeidsforholdEtterStp(Behandling behandling, LocalDate skjæringstidspunkt, BeregningsgrunnlagPrStatusOgAndel gradertAndel);

    boolean erGjeldendeBruttoBGForAndelStørreEnnNull(BeregningsgrunnlagPrStatusOgAndel gradertAndel);

    Optional<BeregningsgrunnlagPrStatusOgAndel> hentKorresponderendeAndelIOriginaltBeregningsgrunnlag(Behandling behandling, BeregningsgrunnlagPeriode periode, BeregningsgrunnlagPrStatusOgAndel andelINyttBG);

    List<Gradering> hentGraderingerForAndelIPeriode(Behandling behandling, ÅpenDatoIntervallEntitet periode, BeregningsgrunnlagPrStatusOgAndel andel);

    Optional<BeregningsgrunnlagPrStatusOgAndel> hentKorresponderendeAndelIGjeldendeBeregningsgrunnlag(Behandling behandling, BeregningsgrunnlagPeriode periode, BeregningsgrunnlagPrStatusOgAndel andelINyttBG);

    boolean vurderManuellBehandlingForAndel(Behandling behandling, LocalDate skjæringstidspunkt, BeregningsgrunnlagPeriode periode, BeregningsgrunnlagPrStatusOgAndel andel);

    Optional<Inntektsmelding> hentInntektsmeldingForAndel(Behandling behandling, BeregningsgrunnlagPrStatusOgAndel andel);

    Optional<Periode> hentRefusjonsPeriodeForAndel(Behandling behandling, BeregningsgrunnlagPrStatusOgAndel andel);

    List<Gradering> hentGraderingerForAndel(Behandling behandling, BeregningsgrunnlagPrStatusOgAndel andel);

    boolean harPeriodeGradering(BeregningsgrunnlagPeriode periode, Behandling behandling);

    boolean harPeriodeRefusjonskrav(BeregningsgrunnlagPeriode periode, Behandling behandling);

    boolean erLønnsendringIBeregningsperioden(Behandling behandling);

    boolean brukerHarHattLønnsendringOgManglerInntektsmelding(Behandling behandling);

    List<Yrkesaktivitet> finnAlleAktiviteterMedLønnsendringUtenInntektsmelding (Behandling behandling);

    Map<Virksomhet, List<Inntektsmelding>> hentInntektsmeldingerForVirksomheter(Behandling behandling, Set<Virksomhet> virksomheter);

}
