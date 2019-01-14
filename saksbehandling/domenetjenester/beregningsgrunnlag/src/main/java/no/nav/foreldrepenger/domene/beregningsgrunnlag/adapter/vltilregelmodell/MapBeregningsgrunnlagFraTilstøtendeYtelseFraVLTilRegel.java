package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.SatsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagAndelTilstøtendeYtelse;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.TilstøtendeYtelse;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.TilstøtendeYtelseAndel;
import no.nav.foreldrepenger.domene.typer.Stillingsprosent;

@ApplicationScoped
public class MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel {

    private static final BigDecimal BIGDECIMAL_100 = BigDecimal.valueOf(100);
    private SatsRepository satsRepository;

    private static final Map<Arbeidskategori, List<Inntektskategori>> ARBEIDSKATEGORI_MAP;
    private static final Map<InntektPeriodeType, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType> INNTEKT_PERIODE_TYPE_MAP;
    private static final Map<RelatertYtelseType, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType> RELATERT_YTELSE_TYPE_MAP;
    private static final Map<no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori, Inntektskategori > INNTEKTSKATEGORI_MAP;
    private static final Map<no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus, AktivitetStatus > AKTIVITETSTATUS_MAP;


    static {
        Map<no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori, Inntektskategori> map = new HashMap<>();
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori.FRILANSER, Inntektskategori.FRILANSER);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori.DAGPENGER, Inntektskategori.DAGPENGER);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori.ARBEIDSAVKLARINGSPENGER, Inntektskategori.ARBEIDSAVKLARINGSPENGER);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori.SJØMANN, Inntektskategori.SJØMANN);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori.DAGMAMMA, Inntektskategori.DAGMAMMA);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori.JORDBRUKER, Inntektskategori.JORDBRUKER);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori.FISKER, Inntektskategori.FISKER);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori.UDEFINERT, Inntektskategori.UDEFINERT);
        INNTEKTSKATEGORI_MAP = map;
    }
    static {
        Map<no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus, AktivitetStatus> map = new HashMap<>();
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ATFL);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.FRILANSER, AktivitetStatus.ATFL);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, AktivitetStatus.SN);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.DAGPENGER, AktivitetStatus.DP);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSAVKLARINGSPENGER, AktivitetStatus.AAP);
        map.put(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.UDEFINERT, AktivitetStatus.UDEFINERT);
        AKTIVITETSTATUS_MAP = map;
    }


    static {
        Map<Arbeidskategori, List<Inntektskategori>> map = new HashMap<>();
        map.put(Arbeidskategori.FISKER, Collections.singletonList(Inntektskategori.FISKER));
        map.put(Arbeidskategori.ARBEIDSTAKER, Collections.singletonList(Inntektskategori.ARBEIDSTAKER));
        map.put(Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Collections.singletonList(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE));
        map.put(Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_SELVSTENDIG_NÆRINGSDRIVENDE,
            Arrays.asList(Inntektskategori.ARBEIDSTAKER, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE));
        map.put(Arbeidskategori.SJØMANN, Collections.singletonList(Inntektskategori.SJØMANN));
        map.put(Arbeidskategori.JORDBRUKER, Collections.singletonList(Inntektskategori.JORDBRUKER));
        map.put(Arbeidskategori.DAGPENGER, Collections.singletonList(Inntektskategori.DAGPENGER));
        map.put(Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_JORDBRUKER, Arrays.asList(Inntektskategori.ARBEIDSTAKER, Inntektskategori.JORDBRUKER));
        map.put(Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FISKER, Arrays.asList(Inntektskategori.ARBEIDSTAKER, Inntektskategori.FISKER));
        map.put(Arbeidskategori.FRILANSER, Collections.singletonList(Inntektskategori.FRILANSER));
        map.put(Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER, Arrays.asList(Inntektskategori.ARBEIDSTAKER, Inntektskategori.FRILANSER));
        map.put(Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER, Arrays.asList(Inntektskategori.ARBEIDSTAKER, Inntektskategori.DAGPENGER));
        map.put(Arbeidskategori.DAGMAMMA, Collections.singletonList(Inntektskategori.DAGMAMMA));
        map.put(Arbeidskategori.INAKTIV, Collections.singletonList(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER));
        map.put(Arbeidskategori.UGYLDIG, Collections.singletonList(Inntektskategori.UDEFINERT));
        ARBEIDSKATEGORI_MAP = map;
    }

    static {
        Map<InntektPeriodeType, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType> map = new HashMap<>();
        map.put(InntektPeriodeType.DAGLIG, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType.DAGLIG);
        map.put(InntektPeriodeType.UKENTLIG, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType.UKENTLIG);
        map.put(InntektPeriodeType.BIUKENTLIG, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType.BIUKENTLIG);
        map.put(InntektPeriodeType.MÅNEDLIG, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType.MÅNEDLIG);
        map.put(InntektPeriodeType.ÅRLIG, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType.ÅRLIG);
        map.put(InntektPeriodeType.FASTSATT25PAVVIK, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType.FASTSETT25PAVVIK);
        map.put(InntektPeriodeType.PREMIEGRUNNLAG, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType.PREMIEGRUNNLAG);
        INNTEKT_PERIODE_TYPE_MAP = map;
    }

    static {
        Map<RelatertYtelseType, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType> map = new HashMap<>();
        map.put(RelatertYtelseType.ENSLIG_FORSØRGER, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType.ENSLIG_FORSØRGER);
        map.put(RelatertYtelseType.SYKEPENGER, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType.SYKEPENGER);
        map.put(RelatertYtelseType.SVANGERSKAPSPENGER,
            no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType.SVANGERSKAPSPENGER);
        map.put(RelatertYtelseType.FORELDREPENGER, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType.FORELDREPENGER);
        map.put(RelatertYtelseType.ENGANGSSTØNAD, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType.ENGANGSSTØNAD);
        map.put(RelatertYtelseType.PÅRØRENDESYKDOM, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType.PÅRØRENDESYKDOM);
        map.put(RelatertYtelseType.ARBEIDSAVKLARINGSPENGER,
            no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType.ARBEIDSAVKLARINGSPENGER);
        map.put(RelatertYtelseType.DAGPENGER, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType.DAGPENGER);
        RELATERT_YTELSE_TYPE_MAP = map;
    }

    MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel() {
    }

    @Inject
    public MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel(SatsRepository satsRepository) {
        this.satsRepository = satsRepository;
    }

    public BeregningsgrunnlagFraTilstøtendeYtelse map(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag,
                                                      InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag, List<Ytelse> sammenhengendeYtelser) {
        Collection<Yrkesaktivitet> yrkesaktiviteter = inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp(behandling.getAktørId())
            .map(AktørArbeid::getYrkesaktiviteter)
            .orElse(Collections.emptyList());
        TilstøtendeYtelse tilstøtendeYtelse = mapFraYtelse(sammenhengendeYtelser, yrkesaktiviteter);
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder builder = BeregningsgrunnlagFraTilstøtendeYtelse.builder()
            .medGrunnbeløpSatser(MapBeregningsgrunnlagFraVLTilRegel.mapGrunnbeløpSatser(satsRepository))
            .medYtelse(tilstøtendeYtelse);
        mapAlleredeOpprettetAndeler(beregningsgrunnlag, builder);
        return builder.build();
    }

    private void mapAlleredeOpprettetAndeler(Beregningsgrunnlag beregningsgrunnlag, BeregningsgrunnlagFraTilstøtendeYtelse.Builder builder) {
        if (beregningsgrunnlag.getBeregningsgrunnlagPerioder().size() != 1) {
            throw new IllegalStateException("Beregningsgrunnlag forventes å ha kun en periode.");
        }
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel ->
            builder.leggTilBeregningsgrunnlagAndel(byggAndel(andel)));
    }

    private BeregningsgrunnlagAndelTilstøtendeYtelse byggAndel(BeregningsgrunnlagPrStatusOgAndel andel) {
        BeregningsgrunnlagAndelTilstøtendeYtelse.Builder builder = BeregningsgrunnlagAndelTilstøtendeYtelse.builder();
        builder
            .medInntektskategori(INNTEKTSKATEGORI_MAP.get(andel.getInntektskategori()))
            .medAktivitetStatus(AKTIVITETSTATUS_MAP.getOrDefault(andel.getAktivitetStatus(), AktivitetStatus.UDEFINERT));
        andel.getBgAndelArbeidsforhold().ifPresent(bga -> {
            settArbeidsgiver(builder, bga);
            builder
                .medArbeidsforholdId(bga.getArbeidsforholdRef().map(ArbeidsforholdRef::getReferanse).orElse(null))
                .medArbeidsperiodeFom(bga.getArbeidsperiodeFom())
                .medArbeidsperiodeTom(bga.getArbeidsperiodeTom().orElse(null))
                .medRefusjonskrav(bga.getRefusjonskravPrÅr());
        });
        return builder.build();
    }

    private void settArbeidsgiver(BeregningsgrunnlagAndelTilstøtendeYtelse.Builder builder, BGAndelArbeidsforhold bga) {
        bga.getArbeidsgiver().ifPresent(arbeidsgiver -> {
            if (arbeidsgiver.getErVirksomhet()) {
                builder.medOrgnr(arbeidsgiver.getIdentifikator());
            } else {
                builder.medAktørId(arbeidsgiver.getIdentifikator());
            }
        });
    }

    private TilstøtendeYtelse mapFraYtelse(List<Ytelse> sammenhengendeYtelser, Collection<Yrkesaktivitet> yrkesaktiviteter) {
        Ytelse førsteYtelseIPeriodeAvSammenhengendeYtelser = sammenhengendeYtelser.stream()
            .min(Comparator.comparing(Ytelse::getPeriode))
            .orElseThrow(() -> new IllegalStateException("Fant ikke Ytelse fra Infotrygd"));
        Ytelse sisteYtelseFørSkjæringstidspunkt = sammenhengendeYtelser.stream()
            .max(Comparator.comparing(Ytelse::getPeriode))
            .orElseThrow(() -> new IllegalStateException("Fant ikke Ytelse fra Infotrygd"));
        YtelseGrunnlag ytelseGrunnlag = sisteYtelseFørSkjæringstidspunkt.getYtelseGrunnlag()
            .orElseThrow(() -> new IllegalStateException("Fant ikke YtelseGrunnlag"));
        BigDecimal dekningsgrad = hentDekningsgrad(sisteYtelseFørSkjæringstidspunkt, ytelseGrunnlag);
        List<Inntektskategori> inntektskategoriListe = ytelseGrunnlag.getArbeidskategori().map(this::mapFraArbeidskategoriTilInntektskategori)
            .orElse(Collections.emptyList());
        TilstøtendeYtelse.Builder tyBuilder = TilstøtendeYtelse.builder()
            .medOpprinneligSkjæringstidspunkt(førsteYtelseIPeriodeAvSammenhengendeYtelser.getPeriode().getFomDato())
            .medRelatertYtelseType(mapFraRelatertYtelseType(sisteYtelseFørSkjæringstidspunkt.getRelatertYtelseType()))
            .medKildeInfotrygd(Fagsystem.INFOTRYGD.equals(sisteYtelseFørSkjæringstidspunkt.getKilde()))
            .medInntektskategorier(inntektskategoriListe)
            .medDekningsgrad(Dekningsgrad.fraBigDecimal(dekningsgrad));
        if (inntektskategoriListe.size() > 1) {
            inntektskategoriListe.forEach(inntektskategori -> {
                YtelseStørrelse ytelseStørrelse = finnYtelseStørrelse(inntektskategori, ytelseGrunnlag.getYtelseStørrelse());
                TilstøtendeYtelseAndel tilstøtendeYtelseAndel = mapArbeidsforhold(yrkesaktiviteter, inntektskategori, ytelseStørrelse);
                tyBuilder.leggTilArbeidsforhold(tilstøtendeYtelseAndel);
            });
        } else {
            ytelseGrunnlag.getYtelseStørrelse().forEach(ytelseStørrelse -> {
                TilstøtendeYtelseAndel tilstøtendeYtelseAndel = mapArbeidsforhold(yrkesaktiviteter, inntektskategoriListe.get(0), ytelseStørrelse);
                tyBuilder.leggTilArbeidsforhold(tilstøtendeYtelseAndel);
            });
        }
        // legg til aktiviteter fra Sigrun (hvis vi ikke har AktivitetStatus=SN andel fra Infotrygd)
        return tyBuilder.build();
    }

    private YtelseStørrelse finnYtelseStørrelse(Inntektskategori inntektskategori, List<YtelseStørrelse> ytelseStørrelseListe) {
        if (AktivitetStatus.ATFL.equals(inntektskategori.getAktivitetStatus()) && !Inntektskategori.FRILANSER.equals(inntektskategori)) {
            return ytelseStørrelseListe.stream()
                .filter(ys -> ys.getVirksomhet().isPresent())
                .findFirst()
                .orElse(ytelseStørrelseListe.get(0));
        } else {
            return ytelseStørrelseListe.stream()
                .filter(ys -> !ys.getVirksomhet().isPresent())
                .findFirst()
                .orElse(ytelseStørrelseListe.get(0));
        }
    }

    private BigDecimal hentDekningsgrad(Ytelse sisteYtelseFørSkjæringstidspunkt, YtelseGrunnlag ytelseGrunnlag) {
        if (RelatertYtelseType.SYKEPENGER.equals(sisteYtelseFørSkjæringstidspunkt.getRelatertYtelseType())) {
            return ytelseGrunnlag.getInntektsgrunnlagProsent().map(Stillingsprosent::getVerdi).orElse(BIGDECIMAL_100);
        }
        return ytelseGrunnlag.getDekningsgradProsent().map(Stillingsprosent::getVerdi).orElse(BIGDECIMAL_100);
    }

    private TilstøtendeYtelseAndel mapArbeidsforhold(Collection<Yrkesaktivitet> yrkesaktiviteter, Inntektskategori inntektskategori,
                                                     YtelseStørrelse ytelseStørrelse) {
        Optional<String> orgNrOpt = AktivitetStatus.ATFL.equals(inntektskategori.getAktivitetStatus()) && ytelseStørrelse.getVirksomhet().isPresent() ? ytelseStørrelse.getVirksomhet().map(Virksomhet::getOrgnr) : Optional.empty();
        Optional<AktivitetsAvtale> aktivitetAvtale = orgNrOpt.flatMap(orgnr -> yrkesaktiviteter.stream()
            .filter(ya -> Objects.nonNull(ya.getArbeidsgiver()))
            .filter(ya -> Objects.nonNull(ya.getArbeidsgiver().getVirksomhet()))
            .filter(ya -> ya.getArbeidsgiver().getVirksomhet().getOrgnr().equals(orgnr))
            .flatMap(ya -> ya.getAktivitetsAvtaler().stream()).findFirst());
        LocalDate arbeidsperiodeFom = aktivitetAvtale.map(AktivitetsAvtale::getFraOgMed).orElse(null);
        LocalDate arbeidsperiodeTom = aktivitetAvtale.map(AktivitetsAvtale::getTilOgMed).orElse(null);
        TilstøtendeYtelseAndel.Builder tilstøtendeYtelseAndelBuilder = TilstøtendeYtelseAndel.builder()
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(inntektskategori.getAktivitetStatus())
            .medBeløp(ytelseStørrelse.getBeløp().getVerdi())
            .medHyppighet(mapFraInntektPeriodeType(ytelseStørrelse.getHyppighet()))
            .medArbeidsforholdFom(arbeidsperiodeFom)
            .medArbeidsforholdTom(arbeidsperiodeTom);
        orgNrOpt.ifPresent(tilstøtendeYtelseAndelBuilder::medOrgNr);
        return tilstøtendeYtelseAndelBuilder.build();
    }

    private no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType mapFraInntektPeriodeType(InntektPeriodeType hyppighet) {
        return INNTEKT_PERIODE_TYPE_MAP.get(hyppighet);
    }

    private no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType mapFraRelatertYtelseType(RelatertYtelseType relatertYtelseType) {
        return RELATERT_YTELSE_TYPE_MAP.get(relatertYtelseType);
    }

    private List<Inntektskategori> mapFraArbeidskategoriTilInntektskategori(Arbeidskategori arbeidskategori) {
        return ARBEIDSKATEGORI_MAP.getOrDefault(arbeidskategori, Collections.singletonList(Inntektskategori.UDEFINERT));
    }
}
