package no.nav.foreldrepenger.domene.beregning.ytelse.adapter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatAndel;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Arbeidsforhold;

public final class AktivitetStatusMapper {

    private static final Map<no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus, AktivitetStatus> REGEL_TIL_VL_MAP;
    private static final Map<AktivitetStatus, no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus> VL_TIL_REGEL_MAP;

    private AktivitetStatusMapper() {
        //"Statisk" klasse
    }

    static {
        Map<no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus, AktivitetStatus> map = new EnumMap<>(no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.class);
        map.put(no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.AAP, AktivitetStatus.ARBEIDSAVKLARINGSPENGER);
        map.put(no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.BA, AktivitetStatus.BRUKERS_ANDEL);
        map.put(no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.DP, AktivitetStatus.DAGPENGER);
        map.put(no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.MS, AktivitetStatus.MILITÆR_ELLER_SIVIL);
        map.put(no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.SN, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        map.put(no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.TY, AktivitetStatus.TILSTØTENDE_YTELSE);
        REGEL_TIL_VL_MAP = Collections.unmodifiableMap(map);
    }

    static {
        Map<AktivitetStatus, no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus> map = new HashMap<>();
        REGEL_TIL_VL_MAP.forEach((key, value) -> map.put(value, key)); //Initialiser reversert map
        map.put(AktivitetStatus.ARBEIDSTAKER, no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.ATFL);
        map.put(AktivitetStatus.FRILANSER, no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.ATFL);
        map.put(AktivitetStatus.KOMBINERT_AT_FL, no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.ATFL);
        map.put(AktivitetStatus.KOMBINERT_AT_FL_SN, no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.ATFL_SN);
        map.put(AktivitetStatus.KOMBINERT_AT_SN, no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.ATFL_SN);
        map.put(AktivitetStatus.KOMBINERT_FL_SN, no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.ATFL_SN);
        VL_TIL_REGEL_MAP = Collections.unmodifiableMap(map);
    }

    public static AktivitetStatus fraRegelTilVl(BeregningsresultatAndel andel) {
        if (andel.getAktivitetStatus().equals(no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus.ATFL)) {
            Arbeidsforhold arbeidsforhold = andel.getArbeidsforhold();
            return arbeidsforhold != null && arbeidsforhold.erFrilanser() ? AktivitetStatus.FRILANSER : AktivitetStatus.ARBEIDSTAKER;
        }
        if (REGEL_TIL_VL_MAP.containsKey(andel.getAktivitetStatus())) {
            return REGEL_TIL_VL_MAP.get(andel.getAktivitetStatus());
        }
        throw new IllegalArgumentException("Ukjent AktivitetStatus " + andel.getAktivitetStatus().name());
    }

    public static no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus fraVLTilRegel(AktivitetStatus vlAktivitetStatus) {
        if (VL_TIL_REGEL_MAP.containsKey(vlAktivitetStatus)) {
            return VL_TIL_REGEL_MAP.get(vlAktivitetStatus);
        }
        throw new IllegalArgumentException("Ukjent AktivitetStatus " + vlAktivitetStatus);
    }
}
