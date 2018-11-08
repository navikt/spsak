package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakBeregningsandelTjeneste;

class UttakBeregningsandelTjenesteTestUtil implements UttakBeregningsandelTjeneste {

    private List<BeregningsgrunnlagPrStatusOgAndel> andeler = new ArrayList<>();

    @Override
    public List<BeregningsgrunnlagPrStatusOgAndel> hentAndeler(Behandling behandling) {
        return this.andeler;
    }

    void leggTilSelvNæringdrivende(Virksomhet virksomhet) {
        this.andeler.add(new BeregningsgrunnlagPrStatusOgAndel.Builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet)))
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build(periode()));
    }

    void leggTilFrilans() {
        this.andeler.add(new BeregningsgrunnlagPrStatusOgAndel.Builder()
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .build(periode()));
    }

    private BeregningsgrunnlagPeriode periode() {
        return mock(BeregningsgrunnlagPeriode.class);
    }

    void leggTilOrdinærtArbeid(Virksomhet virksomhet, String arbeidsforholdId) {
        this.andeler.add(new BeregningsgrunnlagPrStatusOgAndel.Builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet)).medArbforholdRef(arbeidsforholdId))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode()));
    }
}
