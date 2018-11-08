package no.nav.foreldrepenger.økonomistøtte.fp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKlassifik;

public class InntektskategoriKlassekodeMapper {

    private static final Map<String, ØkonomiKodeKlassifik> INNTEKTSKATEGORI_KLASSEKODE_MAP;

    private InntektskategoriKlassekodeMapper() {
    }

    static {
        final Map<String, ØkonomiKodeKlassifik> inntektskategoriKlassekodeMap = new HashMap<>();
        inntektskategoriKlassekodeMap.put(Inntektskategori.ARBEIDSTAKER.getKode(), ØkonomiKodeKlassifik.FPATORD);
        inntektskategoriKlassekodeMap.put(Inntektskategori.FRILANSER.getKode(), ØkonomiKodeKlassifik.FPATFRI);
        inntektskategoriKlassekodeMap.put(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE.getKode(), ØkonomiKodeKlassifik.FPSND_OP);
        inntektskategoriKlassekodeMap.put(Inntektskategori.DAGPENGER.getKode(), ØkonomiKodeKlassifik.FPATAL);
        inntektskategoriKlassekodeMap.put(Inntektskategori.ARBEIDSAVKLARINGSPENGER.getKode(), ØkonomiKodeKlassifik.FPATAL);
        inntektskategoriKlassekodeMap.put(Inntektskategori.SJØMANN.getKode(), ØkonomiKodeKlassifik.FPATSJO);
        inntektskategoriKlassekodeMap.put(Inntektskategori.DAGMAMMA.getKode(), ØkonomiKodeKlassifik.FPSNDDM_OP);
        inntektskategoriKlassekodeMap.put(Inntektskategori.JORDBRUKER.getKode(), ØkonomiKodeKlassifik.FPSNDJB_OP);
        inntektskategoriKlassekodeMap.put(Inntektskategori.FISKER.getKode(), ØkonomiKodeKlassifik.FPSNDFI);
        inntektskategoriKlassekodeMap.put(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER.getKode(), ØkonomiKodeKlassifik.FPATORD);
        INNTEKTSKATEGORI_KLASSEKODE_MAP = Collections.unmodifiableMap(inntektskategoriKlassekodeMap);
    }

    static String inntektskategoriTilKlassekode(Inntektskategori inntektskategori) {
        if (!INNTEKTSKATEGORI_KLASSEKODE_MAP.containsKey(inntektskategori.getKode())) {
            throw new IllegalStateException("Utvikler feil: Mangler inntektskategori");
        }
        return INNTEKTSKATEGORI_KLASSEKODE_MAP.get(inntektskategori.getKode()).getKodeKlassifik();
    }
}
