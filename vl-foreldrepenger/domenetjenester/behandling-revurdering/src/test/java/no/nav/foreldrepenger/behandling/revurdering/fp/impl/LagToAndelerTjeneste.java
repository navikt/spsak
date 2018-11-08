package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import static no.nav.foreldrepenger.behandling.revurdering.fp.impl.RevurderingFPBehandlingsresultatutlederTest.ARBEIDSFORHOLDLISTE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;

class LagToAndelerTjeneste implements LagAndelTjeneste {

    @Override
    public void lagAndeler(BeregningsgrunnlagPeriode periode, boolean medOppjustertDagsat, boolean skalDeleAndelMellomArbeidsgiverOgBruker) {
        List<Dagsatser> dagsatser = Arrays.asList(new Dagsatser(true, skalDeleAndelMellomArbeidsgiverOgBruker),
            new Dagsatser(false, skalDeleAndelMellomArbeidsgiverOgBruker));
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(ARBEIDSFORHOLDLISTE.get(0))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(240000))
            .medRedusertBrukersAndelPrÅr(dagsatser.get(0).getDagsatsBruker())
            .medRedusertRefusjonPrÅr(dagsatser.get(0).getDagsatsArbeidstaker())
            .build(periode);
        BGAndelArbeidsforhold.Builder bga2 = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(ARBEIDSFORHOLDLISTE.get(1))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga2)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(240000))
            .medRedusertBrukersAndelPrÅr(dagsatser.get(1).getDagsatsBruker())
            .medRedusertRefusjonPrÅr(dagsatser.get(1).getDagsatsArbeidstaker())
            .build(periode);
    }
}
