package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl;

import java.util.EnumMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;

class MapAktivitetStatusFraRegelTilVL {
    private static final Map<AktivitetStatus, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus> AKTIVITETSTATUS_MAP;

    private MapAktivitetStatusFraRegelTilVL() {}

    static {
        Map<AktivitetStatus, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus> map = new EnumMap<>(AktivitetStatus.class);
        map.put(AktivitetStatus.ATFL, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER);
        map.put(AktivitetStatus.TY, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.TILSTØTENDE_YTELSE);
        map.put(AktivitetStatus.SN, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        map.put(AktivitetStatus.ATFL_SN, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.KOMBINERT_AT_FL_SN);
        map.put(AktivitetStatus.DP, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.DAGPENGER);
        map.put(AktivitetStatus.AAP, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.ARBEIDSAVKLARINGSPENGER);
        map.put(AktivitetStatus.MS, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.MILITÆR_ELLER_SIVIL);
        AKTIVITETSTATUS_MAP = map;
    }

    static no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus map(AktivitetStatus aktivitetStatusRegel) {
        if (!AKTIVITETSTATUS_MAP.containsKey(aktivitetStatusRegel)) {
            throw new IllegalArgumentException("Utviklerfeil: Mangler mapping fra regel til VL for AktivitetStatus " + aktivitetStatusRegel.name());
        }
        return AKTIVITETSTATUS_MAP.get(aktivitetStatusRegel);
    }
}
