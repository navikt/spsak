package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.PeriodeYtelse;

public abstract class YtelseBeregningsgrunnlagPeriodeYtelse extends YtelseBeregningsgrunnlagGrunnlag{

    private static final Map<String, Arbeidskategori> ARBEIDSKATEGORI_MAP;
    private List<YtelseBeregningsgrunnlagArbeidsforhold> arbeidsforhold;
    private Arbeidskategori arbeidskategori;

    static {
        Map<String, Arbeidskategori> map = new HashMap<>();
        map.put("00", Arbeidskategori.FISKER);
        map.put("01", Arbeidskategori.ARBEIDSTAKER);
        map.put("02", Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        map.put("03", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_SELVSTENDIG_NÆRINGSDRIVENDE);
        map.put("04", Arbeidskategori.SJØMANN);
        map.put("05", Arbeidskategori.JORDBRUKER);
        map.put("06", Arbeidskategori.DAGPENGER);
        map.put("07", Arbeidskategori.INAKTIV);
        map.put("08", Arbeidskategori.ARBEIDSTAKER);
        map.put("09", Arbeidskategori.ARBEIDSTAKER);
        map.put("10", Arbeidskategori.SJØMANN);
        map.put("11", Arbeidskategori.SJØMANN);
        map.put("12", Arbeidskategori.ARBEIDSTAKER);
        map.put("13", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_JORDBRUKER);
        map.put("14", Arbeidskategori.UGYLDIG);
        map.put("15", Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        map.put("16", Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        map.put("17", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FISKER);
        map.put("18", Arbeidskategori.UGYLDIG);
        map.put("19", Arbeidskategori.FRILANSER);
        map.put("20", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER);
        map.put("21", Arbeidskategori.ARBEIDSTAKER);
        map.put("22", Arbeidskategori.SJØMANN);
        map.put("23", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER);
        map.put("24", Arbeidskategori.FRILANSER);
        map.put("25", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER);
        map.put("26", Arbeidskategori.DAGMAMMA);
        map.put("27", Arbeidskategori.ARBEIDSTAKER);
        map.put("99", Arbeidskategori.UGYLDIG);
        ARBEIDSKATEGORI_MAP = Collections.unmodifiableMap(map);
    }

        YtelseBeregningsgrunnlagPeriodeYtelse(RelatertYtelseType type, PeriodeYtelse periodeYtelse, KodeverkRepository kodeverkRepository){
        super(type, periodeYtelse);
        if (periodeYtelse.getArbeidskategori() == null) {
            this.arbeidskategori = Arbeidskategori.UGYLDIG;
        } else {
            this.arbeidskategori = ARBEIDSKATEGORI_MAP.getOrDefault(periodeYtelse.getArbeidskategori().getValue(), Arbeidskategori.UGYLDIG);
        }
        lagArbeidsforholdListe(periodeYtelse.getArbeidsforholdListe(),kodeverkRepository);
    }

    private void lagArbeidsforholdListe(List<Arbeidsforhold> arbeidsforholdListe, KodeverkRepository kodeverkRepository) {
        ArrayList<YtelseBeregningsgrunnlagArbeidsforhold> afh = new ArrayList<>();
        for (Arbeidsforhold arbf : arbeidsforholdListe) {
            afh.add(new YtelseBeregningsgrunnlagArbeidsforhold(arbf,kodeverkRepository));
        }
        arbeidsforhold = Collections.unmodifiableList(afh);
    }

    @Override
    public List<YtelseBeregningsgrunnlagArbeidsforhold> getArbeidsforhold() {
        return arbeidsforhold;
    }

    @Override
    public boolean harArbeidsForhold() {
        return (getArbeidsforhold() != null && !getArbeidsforhold().isEmpty());
    }


    @Override
    public Arbeidskategori getArbeidskategori() {
        return arbeidskategori;
    }
}
