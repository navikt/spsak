package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Grunnlag;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Vedtak;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public abstract class YtelseBeregningsgrunnlagGrunnlag {

        private final RelatertYtelseType type;
    private final LocalDate identdato;
    private LocalDate fom;
    private LocalDate tom;
    private List<YtelseBeregningsgrunnlagVedtak> vedtak;


    YtelseBeregningsgrunnlagGrunnlag(RelatertYtelseType type, Grunnlag grunnlag){

        this.type = type;
        identdato = DateUtil.convertToLocalDate(grunnlag.getIdentdato());
        if(grunnlag.getPeriode() != null) {
            fom = DateUtil.convertToLocalDate(grunnlag.getPeriode().getFom());
            tom = DateUtil.convertToLocalDate(grunnlag.getPeriode().getTom());
        }

        //Trenger trolig ikke behandlingstema her pga behandlingstema fra infotrygdSak er den vi bruker og de er ulike..
        lagVedtak(grunnlag.getVedtakListe());
    }

    private void lagVedtak(List< Vedtak > vedtakListe) {
        ArrayList<YtelseBeregningsgrunnlagVedtak> vedtaks = new ArrayList<>();
        for (Vedtak vt : vedtakListe) {
            vedtaks.add(new YtelseBeregningsgrunnlagVedtak(vt));
        }
        vedtak = Collections.unmodifiableList(vedtaks);
    }

    public RelatertYtelseType getType() {
        return type;
    }

    public LocalDate getIdentdato() {
        return identdato;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public List<YtelseBeregningsgrunnlagVedtak> getVedtak() {
        return vedtak;
    }

    public abstract void mapSpesialverdier(YtelseGrunnlagBuilder builder);

    public abstract boolean harArbeidsForhold();

    public abstract List<YtelseBeregningsgrunnlagArbeidsforhold> getArbeidsforhold();

    public abstract Arbeidskategori getArbeidskategori();
}
