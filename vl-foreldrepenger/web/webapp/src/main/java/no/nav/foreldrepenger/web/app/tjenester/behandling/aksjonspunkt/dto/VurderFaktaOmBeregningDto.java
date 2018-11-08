package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandlingslager.behandling.beregning.FaktaOmBeregningTilfelle;

@JsonTypeName(VurderFaktaOmBeregningDto.AKSJONSPUNKT_KODE)
public class VurderFaktaOmBeregningDto extends BekreftetAksjonspunktDto {

    public static final String AKSJONSPUNKT_KODE = "5058";

    private FastsettEndretBeregningsgrunnlagDto fastsettEndringBeregningsgrunnlag;
    private VurderNyoppstartetFLDto vurderNyoppstartetFL;
    private VurderTidsbegrensetArbeidsforholdDto vurderTidsbegrensetArbeidsforhold;
    private VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto vurderNyIArbeidslivet;
    private FastsettMånedsinntektFLDto fastsettMaanedsinntektFL;
    private VurderLønnsendringDto vurdertLonnsendring;
    private FastsettMånedsinntektUtenInntektsmeldingDto fastsatteLonnsendringer;
    private VurderATogFLiSammeOrganisasjonDto vurderATogFLiSammeOrganisasjon;
    private BesteberegningFødendeKvinneDto besteberegningAndeler;
    private FastsettBGTilstøtendeYtelseDto tilstøtendeYtelse;
    private List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller;
    private TilstotendeYtelseOgEndretBeregningsgrunnlagDto tilstotendeYtelseOgEndretBG;

    VurderFaktaOmBeregningDto() {
        // For Jackson
    }

    public VurderFaktaOmBeregningDto(String begrunnelse, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        super(begrunnelse);
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
    }

    public VurderFaktaOmBeregningDto(String begrunnelse, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller, FastsettBGTilstøtendeYtelseDto tilstøtendeYtelse) { // NOSONAR
        super(begrunnelse);
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
        this.tilstøtendeYtelse = tilstøtendeYtelse;
    }


    public VurderFaktaOmBeregningDto(String begrunnelse, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller, FastsettEndretBeregningsgrunnlagDto fastsettEndringBeregningsgrunnlag) { // NOSONAR
        super(begrunnelse);
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
        this.fastsettEndringBeregningsgrunnlag = fastsettEndringBeregningsgrunnlag;
    }

    public VurderFaktaOmBeregningDto(String begrunnelse, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller, VurderNyoppstartetFLDto vurderNyoppstartetFL) { // NOSONAR
        super(begrunnelse);
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
        this.vurderNyoppstartetFL = vurderNyoppstartetFL;
    }

    public VurderFaktaOmBeregningDto(String begrunnelse, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller, VurderTidsbegrensetArbeidsforholdDto vurderTidsbegrensetArbeidsforhold) { // NOSONAR
        super(begrunnelse);
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
        this.vurderTidsbegrensetArbeidsforhold = vurderTidsbegrensetArbeidsforhold;
    }

    public VurderFaktaOmBeregningDto(String begrunnelse, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller, TilstotendeYtelseOgEndretBeregningsgrunnlagDto tilstotendeYtelseOgEndretBG) { // NOSONAR
        super(begrunnelse);
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
        this.tilstotendeYtelseOgEndretBG = tilstotendeYtelseOgEndretBG;
    }

    public FastsettEndretBeregningsgrunnlagDto getFastsettEndringBeregningsgrunnlag() {
        return fastsettEndringBeregningsgrunnlag;
    }

    public void setFastsettEndringBeregningsgrunnlag(FastsettEndretBeregningsgrunnlagDto fastsettEndringBeregningsgrunnlag) {
        this.fastsettEndringBeregningsgrunnlag = fastsettEndringBeregningsgrunnlag;
    }

    public VurderNyoppstartetFLDto getVurderNyoppstartetFL() {
        return vurderNyoppstartetFL;
    }

    public void setVurderNyoppstartetFL(VurderNyoppstartetFLDto vurderNyoppstartetFL) {
        this.vurderNyoppstartetFL = vurderNyoppstartetFL;
    }

    public VurderTidsbegrensetArbeidsforholdDto getVurderTidsbegrensetArbeidsforhold() {
        return vurderTidsbegrensetArbeidsforhold;
    }

    public void setVurderTidsbegrensetArbeidsforhold(VurderTidsbegrensetArbeidsforholdDto vurderTidsbegrensetArbeidsforhold) {
        this.vurderTidsbegrensetArbeidsforhold = vurderTidsbegrensetArbeidsforhold;
    }

    public VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto getVurderNyIArbeidslivet() {
        return vurderNyIArbeidslivet;
    }

    public void setVurderNyIArbeidslivet(VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto vurderNyIArbeidslivet) {
        this.vurderNyIArbeidslivet = vurderNyIArbeidslivet;
    }

    public FastsettMånedsinntektFLDto getFastsettMaanedsinntektFL() {
        return fastsettMaanedsinntektFL;
    }

    public void setFastsettMaanedsinntektFL(FastsettMånedsinntektFLDto fastsettMaanedsinntektFL) {
        this.fastsettMaanedsinntektFL = fastsettMaanedsinntektFL;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller;
    }

    public void setFaktaOmBeregningTilfeller(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
    }

    public FastsettMånedsinntektUtenInntektsmeldingDto getFastsatteLonnsendringer() {
        return fastsatteLonnsendringer;
    }

    public void setFastsatteLonnsendringer(FastsettMånedsinntektUtenInntektsmeldingDto fastsatteLonnsendringer) {
        this.fastsatteLonnsendringer = fastsatteLonnsendringer;
    }

    public VurderLønnsendringDto getVurdertLonnsendring() {
        return vurdertLonnsendring;
    }

    public void setVurdertLonnsendring(VurderLønnsendringDto vurdertLonnsendring) {
        this.vurdertLonnsendring = vurdertLonnsendring;
    }

    public VurderATogFLiSammeOrganisasjonDto getVurderATogFLiSammeOrganisasjon() {
        return vurderATogFLiSammeOrganisasjon;
    }

    public void setVurderATogFLiSammeOrganisasjon(VurderATogFLiSammeOrganisasjonDto vurderATogFLiSammeOrganisasjon) {
        this.vurderATogFLiSammeOrganisasjon = vurderATogFLiSammeOrganisasjon;
    }

    public BesteberegningFødendeKvinneDto getBesteberegningAndeler() {
        return besteberegningAndeler;
    }

    public void setBesteberegningAndeler(BesteberegningFødendeKvinneDto besteberegningAndeler) {
        this.besteberegningAndeler = besteberegningAndeler;
    }

    public FastsettBGTilstøtendeYtelseDto getTilstøtendeYtelse() {
        return tilstøtendeYtelse;
    }

    public void setTilstøtendeYtelse(FastsettBGTilstøtendeYtelseDto tilstøtendeYtelse) {
        this.tilstøtendeYtelse = tilstøtendeYtelse;
    }

    public TilstotendeYtelseOgEndretBeregningsgrunnlagDto getTilstotendeYtelseOgEndretBG() {
        return tilstotendeYtelseOgEndretBG;
    }

    public void setTilstotendeYtelseOgEndretBG(TilstotendeYtelseOgEndretBeregningsgrunnlagDto tilstotendeYtelseOgEndretBG) {
        this.tilstotendeYtelseOgEndretBG = tilstotendeYtelseOgEndretBG;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
