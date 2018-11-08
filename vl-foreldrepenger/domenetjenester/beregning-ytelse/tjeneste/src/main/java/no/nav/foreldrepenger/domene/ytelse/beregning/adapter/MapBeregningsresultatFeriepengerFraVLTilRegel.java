package no.nav.foreldrepenger.domene.ytelse.beregning.adapter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatAndel;
import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatPeriode;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Dekningsgrad;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
import no.nav.fpsak.tidsserie.LocalDateInterval;

public class MapBeregningsresultatFeriepengerFraVLTilRegel {


    MapBeregningsresultatFeriepengerFraVLTilRegel() {
        //Skal ikke instansieres
    }

    public static BeregningsresultatFeriepengerRegelModell mapFra(Beregningsgrunnlag beregningsgrunnlag, Behandling behandling, BeregningsresultatFP beregningsresultatFP, Optional<BeregningsresultatFP> annenPartsBeregningsresultatFP) {

        List<BeregningsresultatPeriode> annenPartsBeregningsresultatPerioder = annenPartsBeregningsresultatFP.map(a -> a.getBeregningsresultatPerioder().stream()
            .map(MapBeregningsresultatFeriepengerFraVLTilRegel::mapBeregningsresultatPerioder)
            .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
        List<BeregningsresultatPeriode> beregningsresultatPerioder = beregningsresultatFP.getBeregningsresultatPerioder().stream()
            .map(MapBeregningsresultatFeriepengerFraVLTilRegel::mapBeregningsresultatPerioder).collect(Collectors.toList());
        Set<Inntektskategori> inntektskategorier = mapInntektskategorier(beregningsresultatFP);
        Set<Inntektskategori> annenPartsInntektskategorier = annenPartsBeregningsresultatFP.map(MapBeregningsresultatFeriepengerFraVLTilRegel::mapInntektskategorier).orElse(Collections.emptySet());
        Dekningsgrad dekningsgrad = beregningsgrunnlag.getDekningsgrad() == 100 ? Dekningsgrad.DEKNINGSGRAD_100 : Dekningsgrad.DEKNINGSGRAD_80;
        boolean erForelder1 = RelasjonsRolleType.erMor(behandling.getFagsak().getRelasjonsRolleType());

        return BeregningsresultatFeriepengerRegelModell.builder()
            .medBeregningsresultatPerioder(beregningsresultatPerioder)
            .medInntektskategorier(inntektskategorier)
            .medAnnenPartsBeregningsresultatPerioder(annenPartsBeregningsresultatPerioder)
            .medAnnenPartsInntektskategorier(annenPartsInntektskategorier)
            .medDekningsgrad(dekningsgrad)
            .medErForelder1(erForelder1)
            .build();
    }

    private static Set<Inntektskategori> mapInntektskategorier(BeregningsresultatFP beregningsresultatFP) {
        return beregningsresultatFP.getBeregningsresultatPerioder().stream()
            .flatMap(periode -> periode.getBeregningsresultatAndelList().stream())
            .map(no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel::getInntektskategori)
            .map(InntektskategoriMapper::fraVLTilRegel)
            .distinct()
            .collect(Collectors.toSet());
    }

    private static BeregningsresultatPeriode mapBeregningsresultatPerioder(no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode beregningsresultatPerioder) {
        BeregningsresultatPeriode periode = BeregningsresultatPeriode.builder()
            .medPeriode(new LocalDateInterval(beregningsresultatPerioder.getBeregningsresultatPeriodeFom(), beregningsresultatPerioder.getBeregningsresultatPeriodeTom()))
            .build();
        beregningsresultatPerioder.getBeregningsresultatAndelList().forEach(andel -> mapBeregningsresultatAndel(andel, periode));
        return periode;
    }

    private static void mapBeregningsresultatAndel(no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel andel, BeregningsresultatPeriode periode) {
        BeregningsresultatAndel.builder()
            .medBrukerErMottaker(andel.erBrukerMottaker())
            .medDagsats((long) andel.getDagsats())
            .medDagsatsFraBg((long) andel.getDagsatsFraBg())
            .medAktivitetStatus(AktivitetStatusMapper.fraVLTilRegel(andel.getAktivitetStatus()))
            .medInntektskategori(InntektskategoriMapper.fraVLTilRegel(andel.getInntektskategori()))
            .medArbeidsforhold(mapArbeidsforhold(andel))
            .build(periode);
    }

    private static Arbeidsforhold mapArbeidsforhold(no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel andel) {
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
