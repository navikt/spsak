package no.nav.foreldrepenger.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Optional;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;

public class VerifiserBeregningsgrunnlag {

    public static void verifiserAtBeregningsgrunnlagBruttoIkkeErBeregnet(AktivitetStatus aktivitetStatus, BeregningsgrunnlagHjemmel hjemmel, BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgpsa = verifiserGrunnlag(aktivitetStatus, hjemmel, grunnlag);
        assertThat(bgpsa.getBeregnetPrÅr()).isEqualTo(0.0d);
    }

    public static void verifiserBeregningsgrunnlagBruttoPrPeriodeType(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagHjemmel hjemmel, AktivitetStatus aktivitetStatus, double beløp, double gjennomsnittligPGI) {
        BeregningsgrunnlagPrStatus bgpsa = verifiserGrunnlag(aktivitetStatus, hjemmel, grunnlag);
        assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isCloseTo(beløp, within(0.01));
        if (AktivitetStatus.erSelvstendigNæringsdrivende(aktivitetStatus)) {
            assertThat(bgpsa.getGjennomsnittligPGI().doubleValue()).isCloseTo(gjennomsnittligPGI, within(0.01));
            assertThat(bgpsa.getPgiListe()).isNotNull();
            assertThat(bgpsa.getPgiListe()).hasSize(3);
        }
    }

    public static void verifiserBeregningsgrunnlagBruttoPrPeriodeType(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagHjemmel hjemmel, AktivitetStatus aktivitetStatus, double beløp) {
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, hjemmel, aktivitetStatus, beløp, beløp);
    }

    public static void verifiserBeregningsgrunnlagAvkortetPrÅr(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagHjemmel hjemmel, AktivitetStatus aktivitetStatus, double beløp) {
        BeregningsgrunnlagPrStatus bgpsa = verifiserGrunnlag(aktivitetStatus, hjemmel, grunnlag);
        assertThat(bgpsa.getAvkortetPrÅr().doubleValue()).isCloseTo(beløp, within(0.01));
    }

    public static void verifiserBeregningsgrunnlagAvkortetPrÅrFrilanser(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagHjemmel hjemmel,double beløp){
        BeregningsgrunnlagPrStatus bgpsa = verifiserGrunnlag(AktivitetStatus.ATFL, hjemmel, grunnlag);
        Optional<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdOptional = bgpsa.getFrilansArbeidsforhold();
        arbeidsforholdOptional.ifPresent(af -> assertThat(af.getAvkortetPrÅr().doubleValue()).isCloseTo(beløp, within(0.01)));
    }

    public static void verifiserBeregningsgrunnlagRedusertPrÅr(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagHjemmel hjemmel, AktivitetStatus aktivitetStatus, double beløp) {
        BeregningsgrunnlagPrStatus bgpsa = verifiserGrunnlag(aktivitetStatus, hjemmel, grunnlag);
        assertThat(bgpsa.getRedusertPrÅr().doubleValue()).isCloseTo(beløp, within(0.01));
    }

    public static void verifiserBeregningsperiode(AktivitetStatus aktivitetStatus, BeregningsgrunnlagHjemmel hjemmel, BeregningsgrunnlagPeriode grunnlag, Periode periode) {
        BeregningsgrunnlagPrStatus bgpsa = verifiserGrunnlag(aktivitetStatus, hjemmel, grunnlag);
        assertThat(bgpsa.getBeregningsperiode()).isNotNull();
        assertThat(bgpsa.getBeregningsperiode()).isEqualTo(periode);
    }

    public static void verifiserBeregningsperiode(BeregningsgrunnlagPrArbeidsforhold af, Periode periode) {
        assertThat(af.getBeregningsperiode()).isNotNull();
        assertThat(af.getBeregningsperiode()).isEqualTo(periode);
    }

    private static BeregningsgrunnlagPrStatus verifiserGrunnlag(AktivitetStatus aktivitetStatus, BeregningsgrunnlagHjemmel hjemmel, BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(aktivitetStatus);
        assertThat(bgpsa).isNotNull();
        if (hjemmel != null) {
            assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(bgpsa.getAktivitetStatus()).getHjemmel()).isEqualTo(hjemmel);
        }
        return bgpsa;
    }

    public static void verifiserBeregningsgrunnlagBeregnet(BeregningsgrunnlagPeriode grunnlag, double beregnet, double avkortet) {
        verifiserBeregningsgrunnlagBeregnet(grunnlag, beregnet, avkortet, avkortet);
    }

    public static void verifiserBeregningsgrunnlagBeregnet(BeregningsgrunnlagPeriode grunnlag, double beregnet) {
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isCloseTo(beregnet, within(0.01));
        assertThat(grunnlag.getAvkortetPrÅr()).isNull();
        assertThat(grunnlag.getRedusertPrÅr()).isNull();
    }

    public static void verifiserBeregningsgrunnlagBeregnet(BeregningsgrunnlagPeriode grunnlag, double beregnet, double avkortet, double redusert) {
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isCloseTo(beregnet, within(0.01));
        assertThat(grunnlag.getAvkortetPrÅr().doubleValue()).isCloseTo(avkortet, within(0.01));
        assertThat(grunnlag.getRedusertPrÅr().doubleValue()).isCloseTo(redusert, within(0.01));
    }

    public static void verifiserRegelmerknad(RegelResultat regelResultat, String kode) {
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat.getMerknader()).hasSize(1);
        assertThat(regelResultat.getMerknader().get(0).getMerknadKode()).isEqualTo(kode);
    }
}
