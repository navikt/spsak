package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.FaktaOmBeregningTilfelle;

public class FaktaOmBeregningDto {

    private List<KortvarigeArbeidsforholdDto> kortvarigeArbeidsforhold;
    private TilstøtendeYtelseDto tilstøtendeYtelse;
    private FaktaOmBeregningAndelDto frilansAndel;
    private EndringBeregningsgrunnlagDto endringBeregningsgrunnlag;

    private List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller;
    private List<ATogFLISammeOrganisasjonDto> ATogFLISammeOrganisasjonListe;
    private List<FaktaOmBeregningAndelDto> arbeidsforholdMedLønnsendringUtenIM;
    private List<TilstøtendeYtelseAndelDto> besteberegningAndeler;

    public FaktaOmBeregningDto() {
        // Hibernate
    }

    public EndringBeregningsgrunnlagDto getEndringBeregningsgrunnlag() {
        return endringBeregningsgrunnlag;
    }

    public void setEndringBeregningsgrunnlag(EndringBeregningsgrunnlagDto endringBeregningsgrunnlag) {
        this.endringBeregningsgrunnlag = endringBeregningsgrunnlag;
    }

    public List<KortvarigeArbeidsforholdDto> getKortvarigeArbeidsforhold() {
        return kortvarigeArbeidsforhold;
    }

    public TilstøtendeYtelseDto getTilstøtendeYtelse() {
        return tilstøtendeYtelse;
    }

    public void setKortvarigeArbeidsforhold(List<KortvarigeArbeidsforholdDto> kortvarigeArbeidsforhold) {
        this.kortvarigeArbeidsforhold = kortvarigeArbeidsforhold;
    }

    public void setTilstøtendeYtelse(TilstøtendeYtelseDto tilstøtendeYtelse) {
        this.tilstøtendeYtelse = tilstøtendeYtelse;
    }

    public FaktaOmBeregningAndelDto getFrilansAndel() {
        return frilansAndel;
    }

    public void setFrilansAndel(FaktaOmBeregningAndelDto frilansAndel) {
        this.frilansAndel = frilansAndel;
    }

    public List<FaktaOmBeregningAndelDto> getArbeidsforholdMedLønnsendringUtenIM() {
        return arbeidsforholdMedLønnsendringUtenIM;
    }

    public void setArbeidsforholdMedLønnsendringUtenIM(List<FaktaOmBeregningAndelDto> arbeidsforholdMedLønnsendringUtenIM) {
        this.arbeidsforholdMedLønnsendringUtenIM = arbeidsforholdMedLønnsendringUtenIM;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller;
    }

    public void setFaktaOmBeregningTilfeller(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
    }

    public List<ATogFLISammeOrganisasjonDto> getATogFLISammeOrganisasjonListe() {
        return ATogFLISammeOrganisasjonListe;
    }

    public void setATogFLISammeOrganisasjonListe(List<ATogFLISammeOrganisasjonDto> aTogFLISammeOrganisasjonListe) {
        this.ATogFLISammeOrganisasjonListe = aTogFLISammeOrganisasjonListe;
    }

    public List<TilstøtendeYtelseAndelDto> getBesteberegningAndeler() {
        return besteberegningAndeler;
    }

    public void setBesteberegningAndeler(List<TilstøtendeYtelseAndelDto> besteberegningAndeler) {
        this.besteberegningAndeler = besteberegningAndeler;
    }
}
