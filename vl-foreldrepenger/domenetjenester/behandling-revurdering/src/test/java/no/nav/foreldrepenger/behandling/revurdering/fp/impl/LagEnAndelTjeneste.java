package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import static no.nav.foreldrepenger.behandling.revurdering.fp.impl.RevurderingFPBehandlingsresultatutlederTest.ARBEIDSFORHOLDLISTE;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;

class LagEnAndelTjeneste implements LagAndelTjeneste {

    @Override
    public void lagAndeler(BeregningsgrunnlagPeriode periode, boolean medOppjustertDagsat, boolean skalDeleAndelMellomArbeidsgiverOgBruker) {
        Dagsatser ds = new Dagsatser(medOppjustertDagsat, skalDeleAndelMellomArbeidsgiverOgBruker);
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(ARBEIDSFORHOLDLISTE.get(0))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(240000))
            .medRedusertBrukersAndelPrÅr(ds.getDagsatsBruker())
            .medRedusertRefusjonPrÅr(ds.getDagsatsArbeidstaker())
            .build(periode);
    }
}
