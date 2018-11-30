package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak;

import java.util.Optional;

import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlagGrunnlag;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class InfotrygdSakOgGrunnlag {
    private InfotrygdSak sak;
    private YtelseBeregningsgrunnlagGrunnlag grunnlag;
    private DatoIntervallEntitet periode;

    public InfotrygdSakOgGrunnlag(InfotrygdSak sak) {
        this.sak = sak;
        this.periode = sak.getPeriode();
    }

    public InfotrygdSak getSak() {
        return sak;
    }

    public Optional<YtelseBeregningsgrunnlagGrunnlag> getGrunnlag() {
        return Optional.ofNullable(grunnlag);
    }

    public void setGrunnlag(YtelseBeregningsgrunnlagGrunnlag grunnlag) {
        this.grunnlag = grunnlag;
    }

    public Saksnummer getSaksnummer() {
        return getSak().getSakId();
    }

    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    public void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

}
