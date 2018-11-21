package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;

class MapInntektskategoriRegelTilVL {
    private static final Map<no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori, Inntektskategori> INNTEKTSKATEGORI_MAP;

    private MapInntektskategoriRegelTilVL() {}

    static {
        Map<no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori> map = new EnumMap<>(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.class);
        map.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER);
        map.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.FRILANSER, Inntektskategori.FRILANSER);
        map.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        map.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.DAGPENGER, Inntektskategori.DAGPENGER);
        map.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.ARBEIDSAVKLARINGSPENGER, Inntektskategori.ARBEIDSAVKLARINGSPENGER);
        map.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.SJØMANN, Inntektskategori.SJØMANN);
        map.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.DAGMAMMA, Inntektskategori.DAGMAMMA);
        map.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.JORDBRUKER, Inntektskategori.JORDBRUKER);
        map.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.FISKER, Inntektskategori.FISKER);
        map.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);
        map.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.UDEFINERT, Inntektskategori.UDEFINERT);
        INNTEKTSKATEGORI_MAP = Collections.unmodifiableMap(map);
    }

    static Inntektskategori map(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori inntektskategoriRegel) {
        if (!INNTEKTSKATEGORI_MAP.containsKey(inntektskategoriRegel)) {
            throw new IllegalArgumentException("Utviklerfeil: Mangler mapping fra regel til VL for Inntektskategori " + inntektskategoriRegel);
        }
        return INNTEKTSKATEGORI_MAP.get(inntektskategoriRegel);
    }
}
