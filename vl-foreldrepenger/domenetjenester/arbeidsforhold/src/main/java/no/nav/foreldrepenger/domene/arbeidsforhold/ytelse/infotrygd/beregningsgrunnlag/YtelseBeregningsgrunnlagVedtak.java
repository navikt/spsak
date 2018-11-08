package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag;

import java.time.LocalDate;

import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Vedtak;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class YtelseBeregningsgrunnlagVedtak {
    private LocalDate fom;
    private LocalDate tom;
    private final Integer utbetalingsgrad;

    YtelseBeregningsgrunnlagVedtak(Vedtak vt) {
        if(vt.getAnvistPeriode() != null) {
            fom = DateUtil.convertToLocalDate(vt.getAnvistPeriode().getFom());
            tom = DateUtil.convertToLocalDate(vt.getAnvistPeriode().getTom());
        }
        utbetalingsgrad = vt.getUtbetalingsgrad();
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public Integer getUtbetalingsgrad() {
        return utbetalingsgrad;
    }
}
