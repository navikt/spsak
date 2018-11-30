package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag;

import java.math.BigDecimal;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Sykepenger;

public class YtelseBeregningsgrunnlagSykepenger extends YtelseBeregningsgrunnlagPeriodeYtelse{

    private BigDecimal inntektsgrunnlagProsent;

    YtelseBeregningsgrunnlagSykepenger(Sykepenger sykepenger, KodeverkRepository kodeverkRepository) {
        super(RelatertYtelseType.SYKEPENGER, sykepenger, kodeverkRepository);
        if(sykepenger.getInntektsgrunnlagProsent() != null) {
            inntektsgrunnlagProsent = new BigDecimal(sykepenger.getInntektsgrunnlagProsent());
        }
    }

    public BigDecimal getInntektsgrunnlagProsent() {
        return inntektsgrunnlagProsent;
    }


    @Override
    public void mapSpesialverdier(YtelseGrunnlagBuilder builder) {
        builder.medInntektsgrunnlagProsent(inntektsgrunnlagProsent);
    }
}
