package no.nav.foreldrepenger.domene.beregning.ytelse.adapter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatAndel;
import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatPeriode;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
import no.nav.fpsak.tidsserie.LocalDateInterval;

public class MapBeregningsresultatFeriepengerFraVLTilRegel {


    MapBeregningsresultatFeriepengerFraVLTilRegel() {
        //Skal ikke instansieres
    }

    public static BeregningsresultatFeriepengerRegelModell mapFra(Beregningsgrunnlag beregningsgrunnlag, Behandling behandling, BeregningsresultatPerioder beregningsresultat) {

        List<BeregningsresultatPeriode> beregningsresultatPerioder = beregningsresultat.getBeregningsresultatPerioder().stream()
            .map(MapBeregningsresultatFeriepengerFraVLTilRegel::mapBeregningsresultatPerioder).collect(Collectors.toList());
        Set<Inntektskategori> inntektskategorier = mapInntektskategorier(beregningsresultat);
        Dekningsgrad dekningsgrad = beregningsgrunnlag.getDekningsgrad() == 100 ? Dekningsgrad.DEKNINGSGRAD_100 : Dekningsgrad.DEKNINGSGRAD_80;

        return BeregningsresultatFeriepengerRegelModell.builder()
            .medBeregningsresultatPerioder(beregningsresultatPerioder)
            .medInntektskategorier(inntektskategorier)
            .medDekningsgrad(dekningsgrad)
            .build();
    }

    private static Set<Inntektskategori> mapInntektskategorier(BeregningsresultatPerioder beregningsresultat) {
        return beregningsresultat.getBeregningsresultatPerioder().stream()
            .flatMap(periode -> periode.getBeregningsresultatAndelList().stream())
            .map(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatAndel::getInntektskategori)
            .map(InntektskategoriMapper::fraVLTilRegel)
            .distinct()
            .collect(Collectors.toSet());
    }

    private static BeregningsresultatPeriode mapBeregningsresultatPerioder(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPeriode beregningsresultatPerioder) {
        BeregningsresultatPeriode periode = BeregningsresultatPeriode.builder()
            .medPeriode(new LocalDateInterval(beregningsresultatPerioder.getBeregningsresultatPeriodeFom(), beregningsresultatPerioder.getBeregningsresultatPeriodeTom()))
            .build();
        beregningsresultatPerioder.getBeregningsresultatAndelList().forEach(andel -> mapBeregningsresultatAndel(andel, periode));
        return periode;
    }

    private static void mapBeregningsresultatAndel(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatAndel andel, BeregningsresultatPeriode periode) {
        BeregningsresultatAndel.builder()
            .medBrukerErMottaker(andel.erBrukerMottaker())
            .medDagsats((long) andel.getDagsats())
            .medDagsatsFraBg((long) andel.getDagsatsFraBg())
            .medAktivitetStatus(AktivitetStatusMapper.fraVLTilRegel(andel.getAktivitetStatus()))
            .medInntektskategori(InntektskategoriMapper.fraVLTilRegel(andel.getInntektskategori()))
            .medArbeidsforhold(mapArbeidsforhold(andel))
            .build(periode);
    }

    private static Arbeidsforhold mapArbeidsforhold(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatAndel andel) {
        if (andel.getAktivitetStatus().erFrilanser()) {
            return Arbeidsforhold.frilansArbeidsforhold();
        } else if (andel.getVirksomhet() == null) {
            return null;
        } else {
            return Arbeidsforhold.nyttArbeidsforhold(andel.getArbeidsforholdOrgnr(),
                andel.getArbeidsforholdRef() == null ? null : andel.getArbeidsforholdRef().getReferanse());
        }
    }
}
