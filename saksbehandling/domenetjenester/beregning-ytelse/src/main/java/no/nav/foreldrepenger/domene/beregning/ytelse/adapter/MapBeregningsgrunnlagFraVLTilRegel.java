package no.nav.foreldrepenger.domene.beregning.ytelse.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Periode;

@ApplicationScoped
public class MapBeregningsgrunnlagFraVLTilRegel {

    private MapBeregningsgrunnlagFraVLTilRegel() {
    }

    public static no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Beregningsgrunnlag map(Beregningsgrunnlag vlBeregningsgrunnlag) {
        List<AktivitetStatus> aktivitetStatuser = vlBeregningsgrunnlag.getAktivitetStatuser().stream()
            .map(vlBGAktivitetStatus -> AktivitetStatusMapper.fraVLTilRegel(vlBGAktivitetStatus.getAktivitetStatus()))
            .collect(Collectors.toList());

        List<BeregningsgrunnlagPeriode> perioder = mapBeregningsgrunnlagPerioder(vlBeregningsgrunnlag);

        return no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(vlBeregningsgrunnlag.getSkjæringstidspunkt())
            .medAktivitetStatuser(aktivitetStatuser)
            .medBeregningsgrunnlagPerioder(perioder)
            .build();
    }

    private static List<BeregningsgrunnlagPeriode> mapBeregningsgrunnlagPerioder(Beregningsgrunnlag vlBeregningsgrunnlag) {
        return vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .map(MapBeregningsgrunnlagFraVLTilRegel::mapBeregningsgrunnlagPeriode)
            .collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPeriode mapBeregningsgrunnlagPeriode(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode vlBGPeriode) {
        final BeregningsgrunnlagPeriode.Builder regelBGPeriode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(vlBGPeriode.getBeregningsgrunnlagPeriodeFom(), vlBGPeriode.getBeregningsgrunnlagPeriodeTom()));
        List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatus = mapVLBGPrStatus(vlBGPeriode);
        beregningsgrunnlagPrStatus.forEach(regelBGPeriode::medBeregningsgrunnlagPrStatus);

        return regelBGPeriode.build();
    }

    private static List<BeregningsgrunnlagPrStatus> mapVLBGPrStatus(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode vlBGPeriode) {
        List<BeregningsgrunnlagPrStatus> liste = new ArrayList<>();
        BeregningsgrunnlagPrStatus bgpsATFL = null;

        for (BeregningsgrunnlagPrStatusOgAndel vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            final AktivitetStatus regelAktivitetStatus = AktivitetStatusMapper.fraVLTilRegel(vlBGPStatus.getAktivitetStatus());
            if (AktivitetStatus.ATFL.equals(regelAktivitetStatus)) {
                if (bgpsATFL == null) {  // Alle ATFL håndteres samtidig her
                    bgpsATFL = mapVLBGPStatusForATFL(vlBGPeriode);
                    liste.add(bgpsATFL);
                }
            } else {
                BeregningsgrunnlagPrStatus bgps = mapVLBGPStatusForAlleAktivietetStatuser(vlBGPStatus);
                liste.add(bgps);
            }
        }
        return liste;
    }

    // Ikke ATFL og TY, de har separat mapping
    private static BeregningsgrunnlagPrStatus mapVLBGPStatusForAlleAktivietetStatuser(BeregningsgrunnlagPrStatusOgAndel vlBGPStatus) {
        final AktivitetStatus regelAktivitetStatus = AktivitetStatusMapper.fraVLTilRegel(vlBGPStatus.getAktivitetStatus());
        return BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(regelAktivitetStatus)
            .medRedusertBrukersAndelPrÅr(vlBGPStatus.getRedusertBrukersAndelPrÅr())
            .medInntektskategori(InntektskategoriMapper.fraVLTilRegel(vlBGPStatus.getInntektskategori()))
            .build();
    }

    private static boolean erFrilanser(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus aktivitetStatus) {
        return no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.FRILANSER.equals(aktivitetStatus);
    }

    // Felles mapping av alle statuser som mapper til ATFL
    private static BeregningsgrunnlagPrStatus mapVLBGPStatusForATFL(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode vlBGPeriode) {

        BeregningsgrunnlagPrStatus.Builder regelBGPStatusATFL = BeregningsgrunnlagPrStatus.builder().medAktivitetStatus(AktivitetStatus.ATFL);

        for (BeregningsgrunnlagPrStatusOgAndel vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            if (AktivitetStatus.ATFL.equals(AktivitetStatusMapper.fraVLTilRegel(vlBGPStatus.getAktivitetStatus()))) {
                BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold = BeregningsgrunnlagPrArbeidsforhold.builder()
                    .medArbeidsforhold(fraAndel(vlBGPStatus))
                    .medRedusertRefusjonPrÅr(vlBGPStatus.getRedusertRefusjonPrÅr())
                    .medRedusertBrukersAndelPrÅr(vlBGPStatus.getRedusertBrukersAndelPrÅr())
                    .medInntektskategori(InntektskategoriMapper.fraVLTilRegel(vlBGPStatus.getInntektskategori()))
                    .build();
                regelBGPStatusATFL.medArbeidsforhold(regelArbeidsforhold);
            }
        }
        return regelBGPStatusATFL.build();
    }

    private static Arbeidsforhold fraAndel(BeregningsgrunnlagPrStatusOgAndel vlBGPStatus) {
        // TODO (peek) Precondition sjekke på om arbeidsforholdet er tilstede?
        Optional<ArbeidsforholdRef> arbeidsforholdRef = vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef);
        if (arbeidsforholdRef.isPresent()) {
            return erFrilanser(vlBGPStatus.getAktivitetStatus()) ? Arbeidsforhold.frilansArbeidsforhold()
                : Arbeidsforhold.nyttArbeidsforhold(
                    vlBGPStatus.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).orElse(null),
                    arbeidsforholdRef.map(ArbeidsforholdRef::getReferanse).orElse(null));
        } else {
            return erFrilanser(vlBGPStatus.getAktivitetStatus()) ? Arbeidsforhold.frilansArbeidsforhold()
                : Arbeidsforhold.nyttArbeidsforhold(vlBGPStatus.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).orElse(null));
        }
    }
}
