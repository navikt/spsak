package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningsperiodeTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettInntektskategoriFraSøknadTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.MatchBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagAndelTilstøtendeYtelse;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.ReferanseType;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.TilstøtendeYtelse;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class MapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL {
    private static final Map<RelatertYtelseType, no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType> RELATERT_YTELSE_TYPE_MAP;


    static {
        Map<RelatertYtelseType, no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType> map = new EnumMap<>(RelatertYtelseType.class);
        map.put(RelatertYtelseType.ENSLIG_FORSØRGER, no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType.ENSLIG_FORSØRGER);
        map.put(RelatertYtelseType.SYKEPENGER, no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType.SYKEPENGER);
        map.put(RelatertYtelseType.SVANGERSKAPSPENGER, no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType.SVANGERSKAPSPENGER);
        map.put(RelatertYtelseType.FORELDREPENGER, no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType.FORELDREPENGER);
        map.put(RelatertYtelseType.ENGANGSSTØNAD, no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType.ENGANGSSTØNAD);
        map.put(RelatertYtelseType.PÅRØRENDESYKDOM, no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType.PÅRØRENDESYKDOM);
        map.put(RelatertYtelseType.ARBEIDSAVKLARINGSPENGER, no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType.ARBEIDSAVKLARINGSPENGER);
        map.put(RelatertYtelseType.DAGPENGER, no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType.DAGPENGER);
        RELATERT_YTELSE_TYPE_MAP = map;
    }

    private FastsettInntektskategoriFraSøknadTjeneste fastsettInntektskategoriFraSøknadTjeneste;
    private VirksomhetRepository virksomhetRepository;
    private MatchBeregningsgrunnlagTjeneste matchBeregningsgrunnlagTjeneste;

    MapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL() {
        // for CDI
    }

    @Inject
    public MapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL(VirksomhetRepository virksomhetRepository,
                                                                  FastsettInntektskategoriFraSøknadTjeneste fastsettInntektskategoriFraSøknadTjeneste,
                                                                  MatchBeregningsgrunnlagTjeneste matchBeregningsgrunnlagTjeneste) {
        this.fastsettInntektskategoriFraSøknadTjeneste = fastsettInntektskategoriFraSøknadTjeneste;
        this.virksomhetRepository = virksomhetRepository;
        this.matchBeregningsgrunnlagTjeneste = matchBeregningsgrunnlagTjeneste;
    }

    public Beregningsgrunnlag map(Beregningsgrunnlag beregningsgrunnlag, BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlagFraTilstøtendeYtelse,
                                  String regelInput, String regelLogg) {
        TilstøtendeYtelse tilstøtendeYtelse = beregningsgrunnlagFraTilstøtendeYtelse.getTilstøtendeYtelse();
        Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medDekningsgrad(tilstøtendeYtelse.getDekningsgrad().tilLong())
            .medOpprinneligSkjæringstidspunkt(tilstøtendeYtelse.getOpprinneligSkjæringstidspunkt())
            .medGrunnbeløp(beregningsgrunnlagFraTilstøtendeYtelse.getGrunnbeløp())
            .medRedusertGrunnbeløp(beregningsgrunnlagFraTilstøtendeYtelse.getRedusertGrunnbeløp())
            .medRegelloggTilstøtendeYtelse(regelInput, regelLogg)
            .build();

        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        mapAndeler(beregningsgrunnlagFraTilstøtendeYtelse, beregningsgrunnlagPeriode);
        return beregningsgrunnlag;
    }

    void mapAndeler(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlagFraTilstøtendeYtelse, BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        DatoIntervallEntitet beregningsperiode = BeregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(beregningsgrunnlagPeriode.getBeregningsgrunnlag().getSkjæringstidspunkt());
        List<no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori> innntektskategorierSN = beregningsgrunnlagFraTilstøtendeYtelse.getBeregningsgrunnlagAndeler().stream()
            .filter(andel -> AktivitetStatus.erSelvstendigNæringsdrivende(andel.getAktivitetStatus()))
            .map(andel -> mapFraInntektskategori(andel.getInntektskategori())).collect(Collectors.toList());
        Optional<no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori> prioritertInntektskategori = fastsettInntektskategoriFraSøknadTjeneste.finnHøgastPrioriterteInntektskategoriForSN(innntektskategorierSN);
        boolean skalBrukeInformasjonOmFordeling = skalBrukeInformasjonOmFordelingVedTY(beregningsgrunnlagFraTilstøtendeYtelse, prioritertInntektskategori);
        beregningsgrunnlagFraTilstøtendeYtelse.getBeregningsgrunnlagAndeler()
            .forEach(andel -> {
                no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori inntektskategoriForAndel = mapFraInntektskategori(andel.getInntektskategori());
                no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus aktivitetStatusForAndel = mapFraAktivitetStatus(andel.getAktivitetStatus(), andel.getInntektskategori());
                if (AktivitetStatus.erSelvstendigNæringsdrivende(andel.getAktivitetStatus()) && prioritertInntektskategori.isPresent() && !prioritertInntektskategori.get().equals(mapFraInntektskategori(andel.getInntektskategori()))) {
                    slettAndel(beregningsgrunnlagPeriode, inntektskategoriForAndel, aktivitetStatusForAndel);
                } else {
                    byggEllerOppdaterBGAndel(beregningsgrunnlagFraTilstøtendeYtelse, beregningsgrunnlagPeriode, beregningsperiode, skalBrukeInformasjonOmFordeling, andel, inntektskategoriForAndel, aktivitetStatusForAndel);
                }
        });
    }

    private void byggEllerOppdaterBGAndel(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlagFraTilstøtendeYtelse, BeregningsgrunnlagPeriode beregningsgrunnlagPeriode,
                                          DatoIntervallEntitet beregningsperiode, boolean skalBrukeInformasjonOmFordeling, BeregningsgrunnlagAndelTilstøtendeYtelse andel,
                                          no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori inntektskategoriForAndel,
                                          no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus aktivitetStatusForAndel) {
        BeregningsgrunnlagPrStatusOgAndel.Builder builder = hentAndelBuilder(beregningsgrunnlagPeriode, inntektskategoriForAndel, aktivitetStatusForAndel, andel);
        BigDecimal refusjonskravPrÅr = andel.getRefusjonskrav().map(d -> d.multiply(BigDecimal.valueOf(12))).orElse(null);

        builder
            .medAktivitetStatus(aktivitetStatusForAndel)
            .medBeregnetPrÅr(andel.getBeregnetPrÅr())
            .medÅrsbeløpFraTilstøtendeYtelse(skalBrukeInformasjonOmFordeling ? andel.getBeregnetPrÅr() : null)
            .medInntektskategori(inntektskategoriForAndel)
            .medYtelse(mapFraRelatertYtelseType(beregningsgrunnlagFraTilstøtendeYtelse));
        if (skalMappeArbeidsforhold(andel, refusjonskravPrÅr)) {
            BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold = BGAndelArbeidsforhold.builder()
                .medArbeidsgiver(lagArbeidsgiver(andel))
                .medArbforholdRef(andel.getArbeidsforholdId())
                .medRefusjonskravPrÅr(refusjonskravPrÅr)
                .medArbeidsperiodeFom(andel.getArbeidsperiodeFom())
                .medArbeidsperiodeTom(andel.getArbeidsperiodeTom());
            builder.medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
        }
        if (andel.getAktivitetStatus().equals(AktivitetStatus.ATFL)) {
            builder.medBeregningsperiode(beregningsperiode.getFomDato(), beregningsperiode.getTomDato());
        }
        builder.build(beregningsgrunnlagPeriode);
    }

    private Arbeidsgiver lagArbeidsgiver(BeregningsgrunnlagAndelTilstøtendeYtelse andel) {
        if (ReferanseType.AKTØR_ID.equals(andel.getReferanseType())) {
            return Arbeidsgiver.person(new AktørId(andel.getIdentifikator()));
        } else if (ReferanseType.ORG_NR.equals(andel.getReferanseType())) {
            return Arbeidsgiver.virksomhet(virksomhetRepository.hent(andel.getIdentifikator())
                .orElseThrow(() -> new IllegalArgumentException("Fant ikke virksomhet med orgnr " + andel.getIdentifikator())));
        }
        return null;
    }

    private boolean skalMappeArbeidsforhold(BeregningsgrunnlagAndelTilstøtendeYtelse andel, BigDecimal refusjonskravPrÅr) {
        return refusjonskravPrÅr != null
            || andel.getIdentifikator() != null
            || andel.getArbeidsforholdId() != null
            || andel.getArbeidsperiodeFom() != null
            || andel.getArbeidsperiodeTom() != null;
    }

    boolean skalBrukeInformasjonOmFordelingVedTY(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlagFraTilstøtendeYtelse, Optional<no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori> prioritertInntektskategori) {
        return !(prioritertInntektskategori.isPresent() && beregningsgrunnlagFraTilstøtendeYtelse.getBeregningsgrunnlagAndeler().stream()
            .anyMatch(andel -> AktivitetStatus.erSelvstendigNæringsdrivende(andel.getAktivitetStatus()) &&
            !prioritertInntektskategori.get().equals(mapFraInntektskategori(andel.getInntektskategori())) &&
            andel.erFraTilstøtendeYtelse()));
    }

    private BeregningsgrunnlagPrStatusOgAndel.Builder hentAndelBuilder(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode,
                                                                       no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori inntektskategoriForAndel,
                                                                       no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus aktivitetStatusForAndel,
                                                                       BeregningsgrunnlagAndelTilstøtendeYtelse andel) {
        Optional<BeregningsgrunnlagPrStatusOgAndel> oppdatereAndel = finnAndelSomSkalOppdateres(beregningsgrunnlagPeriode, inntektskategoriForAndel, aktivitetStatusForAndel, andel);
        if (oppdatereAndel.isPresent()) {
            return BeregningsgrunnlagPrStatusOgAndel.builder(oppdatereAndel.get());
        } else {
            return BeregningsgrunnlagPrStatusOgAndel.builder();
        }
    }

    private Optional<BeregningsgrunnlagPrStatusOgAndel> finnAndelSomSkalOppdateres(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori inntektskategoriForAndel, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus aktivitetStatusForAndel, BeregningsgrunnlagAndelTilstøtendeYtelse andel) {
        return !aktivitetStatusForAndel.erArbeidstaker() ?
                matchForFLOgSN(beregningsgrunnlagPeriode, aktivitetStatusForAndel, inntektskategoriForAndel) :
                matchBeregningsgrunnlagTjeneste.matchPåTilgjengeligAndelsinformasjon(beregningsgrunnlagPeriode, aktivitetStatusForAndel, inntektskategoriForAndel,
                    andel.getIdentifikator(), andel.getArbeidsforholdId());
    }

    private Optional<BeregningsgrunnlagPrStatusOgAndel> matchForFLOgSN(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode,
                                                                       no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus aktivitetStatusForAndel,
                                                                       no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori inntektskategoriForAndel) {
            List<BeregningsgrunnlagPrStatusOgAndel> matchendeAndeler = matchBeregningsgrunnlagTjeneste.matchPåAktivitetstatusOgInntektskategori(beregningsgrunnlagPeriode, aktivitetStatusForAndel, inntektskategoriForAndel);
            return matchendeAndeler.isEmpty() ? Optional.empty() : Optional.of(matchendeAndeler.get(0));
    }

    private void slettAndel(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori inntektskategoriForAndel, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus aktivitetStatusForAndel) {
        Optional<BeregningsgrunnlagPrStatusOgAndel> fjernAndel = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getInntektskategori().equals(inntektskategoriForAndel)
                && a.getAktivitetStatus().equals(aktivitetStatusForAndel)).findFirst();
        fjernAndel.ifPresent(andel -> BeregningsgrunnlagPeriode.builder(beregningsgrunnlagPeriode).fjernBeregningsgrunnlagPrStatusOgAndel(andel));
    }

    private no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType mapFraRelatertYtelseType(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlagFraTilstøtendeYtelse) {
        return RELATERT_YTELSE_TYPE_MAP.get(beregningsgrunnlagFraTilstøtendeYtelse.getTilstøtendeYtelse().getRelatertYtelseType());
    }

    private no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus mapFraAktivitetStatus(AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori) {
        if (Inntektskategori.FRILANSER.equals(inntektskategori)) {
            return no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.FRILANSER;
        }
        return MapAktivitetStatusFraRegelTilVL.map(aktivitetStatus);
    }

    private no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori mapFraInntektskategori(Inntektskategori inntektskategori) {
        return MapInntektskategoriRegelTilVL.map(inntektskategori);
    }
}
