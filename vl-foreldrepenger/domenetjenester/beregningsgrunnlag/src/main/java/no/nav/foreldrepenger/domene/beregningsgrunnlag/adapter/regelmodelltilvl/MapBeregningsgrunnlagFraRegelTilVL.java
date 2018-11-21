package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.SatsType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Hjemmel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningsperiodeTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.AktivitetStatusModell;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.ReferanseType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class MapBeregningsgrunnlagFraRegelTilVL {

    private static final Map<no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus, AktivitetStatus> beregningsgrunnlagAktivitetStatusMap =
        new EnumMap<>(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.class);

    private static final Map<BeregningsgrunnlagHjemmel, Hjemmel> hjemmelMap =
        new EnumMap<>(BeregningsgrunnlagHjemmel.class);

    private static final EnumMap<Aktivitet, OpptjeningAktivitetType> aktivitetMap =
        new EnumMap<>(Aktivitet.class);

    static {
        aktivitetMap.put(Aktivitet.ARBEIDSTAKERINNTEKT, OpptjeningAktivitetType.ARBEID);
        aktivitetMap.put(Aktivitet.ETTERLØNN, OpptjeningAktivitetType.ETTERLØNN_ARBEIDSGIVER);
        aktivitetMap.put(Aktivitet.FRILANSINNTEKT, OpptjeningAktivitetType.FRILANS);
        aktivitetMap.put(Aktivitet.OPPLÆRINGSPENGER, OpptjeningAktivitetType.OPPLÆRINGSPENGER);
        aktivitetMap.put(Aktivitet.UTDANNINGSPERMISJON, OpptjeningAktivitetType.UTDANNINGSPERMISJON);
        aktivitetMap.put(Aktivitet.SLUTTPAKKE, OpptjeningAktivitetType.SLUTTPAKKE);
        aktivitetMap.put(Aktivitet.VARTPENGER, OpptjeningAktivitetType.VARTPENGER);
        aktivitetMap.put(Aktivitet.VENTELØNN, OpptjeningAktivitetType.VENTELØNN);
        aktivitetMap.put(Aktivitet.VIDERE_ETTERUTDANNING, OpptjeningAktivitetType.VIDERE_ETTERUTDANNING);

        // TODO (TOPAS): Kan denne håndteringen flyttes til MapAktivitetStatusFraRegelTilVL?
        beregningsgrunnlagAktivitetStatusMap.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.AAP, AktivitetStatus.ARBEIDSAVKLARINGSPENGER);
        beregningsgrunnlagAktivitetStatusMap.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.DP, AktivitetStatus.DAGPENGER);
        beregningsgrunnlagAktivitetStatusMap.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.MS, AktivitetStatus.MILITÆR_ELLER_SIVIL);
        beregningsgrunnlagAktivitetStatusMap.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.SN, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        beregningsgrunnlagAktivitetStatusMap.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.TY, AktivitetStatus.TILSTØTENDE_YTELSE);
        beregningsgrunnlagAktivitetStatusMap.put(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.UDEFINERT, AktivitetStatus.UDEFINERT);

        hjemmelMap.put(BeregningsgrunnlagHjemmel.F_14_7, Hjemmel.F_14_7);
        hjemmelMap.put(BeregningsgrunnlagHjemmel.F_14_7_8_30, Hjemmel.F_14_7_8_30);
        hjemmelMap.put(BeregningsgrunnlagHjemmel.F_14_7_8_35, Hjemmel.F_14_7_8_35);
        hjemmelMap.put(BeregningsgrunnlagHjemmel.F_14_7_8_38, Hjemmel.F_14_7_8_38);
        hjemmelMap.put(BeregningsgrunnlagHjemmel.F_14_7_8_40, Hjemmel.F_14_7_8_40);
        hjemmelMap.put(BeregningsgrunnlagHjemmel.F_14_7_8_41, Hjemmel.F_14_7_8_41);
        hjemmelMap.put(BeregningsgrunnlagHjemmel.F_14_7_8_42, Hjemmel.F_14_7_8_42);
        hjemmelMap.put(BeregningsgrunnlagHjemmel.F_14_7_8_43, Hjemmel.F_14_7_8_43);
        hjemmelMap.put(BeregningsgrunnlagHjemmel.F_14_7_8_47, Hjemmel.F_14_7_8_47);
        hjemmelMap.put(BeregningsgrunnlagHjemmel.F_14_7_8_49, Hjemmel.F_14_7_8_49);

    }

    private BeregningRepository beregningRepository;
    private VirksomhetRepository virksomhetRepository;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    MapBeregningsgrunnlagFraRegelTilVL() {
        // CDI
    }

    @Inject
    public MapBeregningsgrunnlagFraRegelTilVL(BehandlingRepositoryProvider repositoryProvider, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.beregningRepository = repositoryProvider.getBeregningRepository();
        this.virksomhetRepository = repositoryProvider.getVirksomhetRepository();
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
    }

    public no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag mapForeslåBeregningsgrunnlag(Beregningsgrunnlag resultatGrunnlag, String regelInput, List<RegelResultat> regelResultater, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag eksisterendeVLGrunnlag) {
        no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag nyttVLGrunnlag = eksisterendeVLGrunnlag.dypKopi();
        return map(resultatGrunnlag, regelInput, regelResultater, nyttVLGrunnlag, true);
    }

    public no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag mapFastsettBeregningsgrunnlag(Beregningsgrunnlag resultatGrunnlag, String regelInput, List<RegelResultat> regelResultater, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag eksisterendeVLGrunnlag) {
        no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag nyttVLGrunnlag = eksisterendeVLGrunnlag.dypKopi();
        return map(resultatGrunnlag, regelInput, regelResultater, nyttVLGrunnlag, false);
    }

    private no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag map(Beregningsgrunnlag resultatGrunnlag, String regelInput, List<RegelResultat> regelResultater, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag eksisterendeVLGrunnlag, boolean foreslå) {
        mapSammenligningsgrunnlag(resultatGrunnlag.getSammenligningsGrunnlag(), eksisterendeVLGrunnlag);

        Objects.requireNonNull(resultatGrunnlag, "resultatGrunnlag");
        Objects.requireNonNull(regelResultater, "regelResultater");
        if (resultatGrunnlag.getBeregningsgrunnlagPerioder().size() != regelResultater.size()) {
            throw new IllegalArgumentException("Antall beregningsresultatperioder ("
                + resultatGrunnlag.getBeregningsgrunnlagPerioder().size()
                + ") må være samme som antall regelresultater ("
                + regelResultater.size() + ")");
        }
        mapAktivitetStatuser(resultatGrunnlag.getAktivitetStatuser(), eksisterendeVLGrunnlag, resultatGrunnlag.getBeregningsgrunnlagPerioder().get(0));

        Iterator<RegelResultat> resultat = regelResultater.iterator();

        int vlBGnummer = 0;
        for (no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGPeriode : resultatGrunnlag.getBeregningsgrunnlagPerioder()) {

            RegelResultat regelResultat = resultat.next();
            BeregningsgrunnlagPeriode eksisterendePeriode = (vlBGnummer < eksisterendeVLGrunnlag.getBeregningsgrunnlagPerioder().size()) ? eksisterendeVLGrunnlag.getBeregningsgrunnlagPerioder().get(vlBGnummer) : null;
            BeregningsgrunnlagPeriode mappetPeriode = mapBeregningsgrunnlagPeriode(resultatBGPeriode, regelInput, regelResultat, eksisterendePeriode, eksisterendeVLGrunnlag, foreslå);
            for (BeregningsgrunnlagPrStatus regelAndel : resultatBGPeriode.getBeregningsgrunnlagPrStatus()) {
                if (regelAndel.getAndelNr() == null) {
                    mapAndelMedArbeidsforhold(mappetPeriode, regelAndel);
                } else {
                    mapAndel(mappetPeriode, regelAndel, foreslå);
                }
            }
            vlBGnummer++;
            fastsettAgreggerteVerdier(mappetPeriode, eksisterendeVLGrunnlag);
        }

        return eksisterendeVLGrunnlag;
    }

    private void mapAndel(BeregningsgrunnlagPeriode mappetPeriode, BeregningsgrunnlagPrStatus regelAndel, boolean foreslå) {
        mappetPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(bgpsa -> regelAndel.getAndelNr().equals(bgpsa.getAndelsnr()))
            .forEach(resultatAndel -> mapBeregningsgrunnlagPrStatus(mappetPeriode, regelAndel, resultatAndel, foreslå));
    }

    private void mapAndelMedArbeidsforhold(BeregningsgrunnlagPeriode mappetPeriode, BeregningsgrunnlagPrStatus regelAndel) {
        for (BeregningsgrunnlagPrArbeidsforhold regelAndelForArbeidsforhold : regelAndel.getArbeidsforhold()) {
            Optional<BeregningsgrunnlagPrStatusOgAndel> andelOpt = mappetPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> regelAndelForArbeidsforhold.getAndelNr().equals(bgpsa.getAndelsnr()))
                .findFirst();
            if (andelOpt.isPresent()) {
                BeregningsgrunnlagPrStatusOgAndel resultatAndel = andelOpt.get();
                mapBeregningsgrunnlagPrStatusForATKombinert(mappetPeriode, regelAndel, resultatAndel);
            }
        }
    }

    private void mapAktivitetStatuser(List<no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel> aktivitetStatuser,
                                      no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag eksisterendeVLGrunnlag, no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        for (no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel regelStatus : aktivitetStatuser) {
            AktivitetStatus modellStatus = fraRegel(regelStatus.getAktivitetStatus(), beregningsgrunnlagPeriode);
            Hjemmel hjemmel = mapFraRegel(regelStatus.getHjemmel());
            BeregningsgrunnlagAktivitetStatus.builder().medAktivitetStatus(modellStatus).medHjemmel(hjemmel).build(eksisterendeVLGrunnlag);
        }
    }

    private Hjemmel mapFraRegel(BeregningsgrunnlagHjemmel hjemmel) {
        if (hjemmel == null) {
            return Hjemmel.UDEFINERT;
        }
        return hjemmelMap.get(hjemmel);
    }

    private AktivitetStatus fraRegel(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus aktivitetStatus, no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        if (beregningsgrunnlagAktivitetStatusMap.containsKey(aktivitetStatus)) {
            return beregningsgrunnlagAktivitetStatusMap.get(aktivitetStatus);
        }
        BeregningsgrunnlagPrStatus atfl = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL);
        if (atfl == null) {
            return AktivitetStatus.ARBEIDSTAKER;
        }
        boolean frilanser = atfl.getFrilansArbeidsforhold().isPresent();
        boolean arbeidstaker = !atfl.getArbeidsforholdIkkeFrilans().isEmpty();

        if (no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL.equals(aktivitetStatus)) {
            return mapATFL(frilanser, arbeidstaker);
        } else if (no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL_SN.equals(aktivitetStatus)) {
            return mapATFL_SN(frilanser, arbeidstaker);
        }
        return beregningsgrunnlagAktivitetStatusMap.get(aktivitetStatus);
    }

    private AktivitetStatus mapATFL_SN(boolean frilanser, boolean arbeidstaker) {
        if (frilanser) {
            if (arbeidstaker) {
                return AktivitetStatus.KOMBINERT_AT_FL_SN;
            } else {
                return AktivitetStatus.KOMBINERT_FL_SN;
            }
        } else {
            return AktivitetStatus.KOMBINERT_AT_SN;
        }
    }

    private AktivitetStatus mapATFL(boolean frilanser, boolean arbeidstaker) {
        if (frilanser) {
            if (arbeidstaker) {
                return AktivitetStatus.KOMBINERT_AT_FL;
            } else {
                return AktivitetStatus.FRILANSER;
            }
        } else {
            return AktivitetStatus.ARBEIDSTAKER;
        }
    }

    private void fastsettAgreggerteVerdier(BeregningsgrunnlagPeriode periode, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag eksisterendeVLGrunnlag) {
        Optional<BigDecimal> bruttoPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(bgpsa -> bgpsa.getBruttoPrÅr() != null)
            .map(BeregningsgrunnlagPrStatusOgAndel::getBruttoPrÅr)
            .reduce(BigDecimal::add);
        Optional<BigDecimal> avkortetPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(bgpsa -> bgpsa.getAvkortetPrÅr() != null)
            .map(BeregningsgrunnlagPrStatusOgAndel::getAvkortetPrÅr)
            .reduce(BigDecimal::add);
        Optional<BigDecimal> redusertPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(bgpsa -> bgpsa.getRedusertPrÅr() != null)
            .map(BeregningsgrunnlagPrStatusOgAndel::getRedusertPrÅr)
            .reduce(BigDecimal::add);
        BeregningsgrunnlagPeriode.builder(periode)
            .medBruttoPrÅr(bruttoPrÅr.orElse(null))
            .medAvkortetPrÅr(avkortetPrÅr.orElse(null))
            .medRedusertPrÅr(redusertPrÅr.orElse(null))
            .build(eksisterendeVLGrunnlag);
    }

    private void mapBeregningsgrunnlagPrStatusForATKombinert(BeregningsgrunnlagPeriode vlBGPeriode,
                                                             BeregningsgrunnlagPrStatus resultatBGPStatus,
                                                             BeregningsgrunnlagPrStatusOgAndel vlBGPAndel) {
        for (BeregningsgrunnlagPrArbeidsforhold arbeidsforhold : resultatBGPStatus.getArbeidsforhold()) {
            if (gjelderSammeAndel(vlBGPAndel, arbeidsforhold)) {
                BeregningsgrunnlagPrStatusOgAndel.Builder andelBuilder = settFasteVerdier(vlBGPAndel, arbeidsforhold);
                if (skalByggeBGArbeidsforhold(arbeidsforhold, vlBGPAndel)) {
                    BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold = mapArbeidsforhold(vlBGPAndel, arbeidsforhold);
                    andelBuilder.medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
                }
                andelBuilder
                    .build(vlBGPeriode);
                return;
            }
        }
    }

    private BGAndelArbeidsforhold.Builder mapArbeidsforhold(BeregningsgrunnlagPrStatusOgAndel vlBGPAndel,
                                                            BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        return BGAndelArbeidsforhold
            .builder(vlBGPAndel.getBgAndelArbeidsforhold().orElse(null))
            .medNaturalytelseBortfaltPrÅr(arbeidsforhold.getNaturalytelseBortfaltPrÅr().orElse(null))
            .medNaturalytelseTilkommetPrÅr(arbeidsforhold.getNaturalytelseTilkommetPrÅr().orElse(null))
            .medRefusjonskravPrÅr(Boolean.TRUE.equals(arbeidsforhold.getLagtTilAvSaksbehandler()) ? null : arbeidsforhold.getRefusjonskravPrÅr().orElse(null));
    }

    private BeregningsgrunnlagPrStatusOgAndel.Builder settFasteVerdier(BeregningsgrunnlagPrStatusOgAndel vlBGPAndel, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        return BeregningsgrunnlagPrStatusOgAndel.builder(vlBGPAndel)
            .medBeregnetPrÅr(verifisertBeløp(arbeidsforhold.getBeregnetPrÅr()))
            .medOverstyrtPrÅr(verifisertBeløp(arbeidsforhold.getOverstyrtPrÅr()))
            .medAvkortetPrÅr(verifisertBeløp(arbeidsforhold.getAvkortetPrÅr()))
            .medRedusertPrÅr(verifisertBeløp(arbeidsforhold.getRedusertPrÅr()))
            .medMaksimalRefusjonPrÅr(arbeidsforhold.getMaksimalRefusjonPrÅr())
            .medAvkortetRefusjonPrÅr(arbeidsforhold.getAvkortetRefusjonPrÅr())
            .medRedusertRefusjonPrÅr(arbeidsforhold.getRedusertRefusjonPrÅr())
            .medAvkortetBrukersAndelPrÅr(verifisertBeløp(arbeidsforhold.getAvkortetBrukersAndelPrÅr()))
            .medRedusertBrukersAndelPrÅr(verifisertBeløp(arbeidsforhold.getRedusertBrukersAndelPrÅr()))
            .medBeregningsperiode(
                arbeidsforhold.getBeregningsperiode() == null ? null : arbeidsforhold.getBeregningsperiode().getFomOrNull(),
                arbeidsforhold.getBeregningsperiode() == null ? null : arbeidsforhold.getBeregningsperiode().getTomOrNull()
            )
            .medFastsattAvSaksbehandler(arbeidsforhold.getFastsattAvSaksbehandler())
            .medLagtTilAvSaksbehandler(arbeidsforhold.getLagtTilAvSaksbehandler())
            .medArbforholdType(aktivitetMap.get(arbeidsforhold.getArbeidsforhold().getAktivitet()))
            .medInntektskategori(MapInntektskategoriRegelTilVL.map(arbeidsforhold.getInntektskategori()));
    }

    private boolean skalByggeBGArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPrStatusOgAndel vlBGPAndel) {
        return vlBGPAndel.getBgAndelArbeidsforhold().isPresent()
            || arbeidsforhold.getNaturalytelseBortfaltPrÅr().isPresent()
            || arbeidsforhold.getNaturalytelseTilkommetPrÅr().isPresent();
    }

    private BigDecimal verifisertBeløp(BigDecimal beløp) {
        return beløp == null ? null : beløp.max(BigDecimal.ZERO);
    }

    private boolean gjelderSammeAndel(BeregningsgrunnlagPrStatusOgAndel vlBGPAndel, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        if (vlBGPAndel.getAktivitetStatus().erFrilanser()) {
            return arbeidsforhold.erFrilanser();
        }
        if (arbeidsforhold.erFrilanser()) {
            return false;
        }
        if (!vlBGPAndel.getInntektskategori().equals(MapInntektskategoriRegelTilVL.map(arbeidsforhold.getInntektskategori()))) {
            return false;
        }
        if (!matcherArbeidsgivere(vlBGPAndel, arbeidsforhold)) {
            return false;
        }
        if (!matcherOpptjeningsaktivitet(vlBGPAndel, arbeidsforhold)) {
            return false;
        }
        return vlBGPAndel.getBgAndelArbeidsforhold()
            .flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)
            .map(ref -> ref.gjelderFor(ArbeidsforholdRef.ref(arbeidsforhold.getArbeidsforhold().getArbeidsforholdId())))
            .orElse(arbeidsforhold.getArbeidsforhold().getArbeidsforholdId() == null);
    }

    private boolean matcherOpptjeningsaktivitet(BeregningsgrunnlagPrStatusOgAndel vlBGPAndel, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        if (arbeidsforhold.getArbeidsforhold() != null) {
            return Objects.equals(vlBGPAndel.getArbeidsforholdType(), aktivitetMap.get(arbeidsforhold.getArbeidsforhold().getAktivitet()));
        }
        return vlBGPAndel.getArbeidsforholdType() == null;
    }

    private boolean matcherArbeidsgivere(BeregningsgrunnlagPrStatusOgAndel andel, BeregningsgrunnlagPrArbeidsforhold forhold) {
        Arbeidsgiver arbeidsgiver = andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver).orElse(null);
        if (forhold.getArbeidsgiverId() == null) {
            return arbeidsgiver == null;
        } else return arbeidsgiver != null && Objects.equals(forhold.getArbeidsgiverId(), arbeidsgiver.getIdentifikator());
    }

    private void mapBeregningsgrunnlagPrStatus(BeregningsgrunnlagPeriode vlBGPeriode,
                                               BeregningsgrunnlagPrStatus resultatBGPStatus,
                                               BeregningsgrunnlagPrStatusOgAndel vlBGPStatusOgAndel,
                                               boolean foreslå) {
        BeregningsgrunnlagPrStatusOgAndel.builder(vlBGPStatusOgAndel)
            .medBeregnetPrÅr(verifisertBeløp(resultatBGPStatus.getBeregnetPrÅr()))
            .medOverstyrtPrÅr(verifisertBeløp(resultatBGPStatus.getOverstyrtPrÅr()))
            .medAvkortetPrÅr(verifisertBeløp(resultatBGPStatus.getAvkortetPrÅr()))
            .medRedusertPrÅr(verifisertBeløp(resultatBGPStatus.getRedusertPrÅr()))
            .medAvkortetBrukersAndelPrÅr(foreslå ? null : verifisertBeløp(resultatBGPStatus.getAvkortetPrÅr()))
            .medRedusertBrukersAndelPrÅr(foreslå ? null : verifisertBeløp(resultatBGPStatus.getRedusertPrÅr()))
            .medMaksimalRefusjonPrÅr(foreslå ? null : BigDecimal.ZERO)
            .medAvkortetRefusjonPrÅr(foreslå ? null : BigDecimal.ZERO)
            .medRedusertRefusjonPrÅr(foreslå ? null : BigDecimal.ZERO)
            .medBeregningsperiode(
                resultatBGPStatus.getBeregningsperiode() == null ? null : resultatBGPStatus.getBeregningsperiode().getFomOrNull(),
                resultatBGPStatus.getBeregningsperiode() == null ? null : resultatBGPStatus.getBeregningsperiode().getTomOrNull()
            )
            .medPgi(resultatBGPStatus.getGjennomsnittligPGI(), resultatBGPStatus.getPgiListe())
            .medÅrsbeløpFraTilstøtendeYtelse(resultatBGPStatus.getÅrsbeløpFraTilstøtendeYtelse())
            .medNyIArbeidslivet(resultatBGPStatus.getNyIArbeidslivet())
            .medInntektskategori(MapInntektskategoriRegelTilVL.map(resultatBGPStatus.getInntektskategori()))
            .medFastsattAvSaksbehandler(resultatBGPStatus.erFastsattAvSaksbehandler())
            .medLagtTilAvSaksbehandler(resultatBGPStatus.erLagtTilAvSaksbehandler())
            .medBesteberegningPrÅr(resultatBGPStatus.getBesteberegningPrÅr())
            .build(vlBGPeriode);
    }

    private BeregningsgrunnlagPeriode mapBeregningsgrunnlagPeriode(final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatGrunnlagPeriode,
                                                                   String regelInput, RegelResultat regelResultat, final BeregningsgrunnlagPeriode vlBGPeriode, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag eksisterendeVLGrunnlag, boolean foreslå) {
        if (vlBGPeriode == null) {
            BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(
                    resultatGrunnlagPeriode.getBeregningsgrunnlagPeriode().getFom(),
                    resultatGrunnlagPeriode.getBeregningsgrunnlagPeriode().getTomOrNull()
                )
                .medRegelEvaluering(foreslå, regelInput, regelResultat == null ? null : regelResultat.getRegelSporing())
                .leggTilPeriodeÅrsaker(mapPeriodeÅrsak(resultatGrunnlagPeriode.getPeriodeÅrsaker()))
                .build(eksisterendeVLGrunnlag);
            opprettBeregningsgrunnlagPrStatusOgAndel(eksisterendeVLGrunnlag.getBeregningsgrunnlagPerioder().get(0), periode);
            return periode;
        }
        BeregningsgrunnlagPeriode.builder(vlBGPeriode)
            .medBeregningsgrunnlagPeriode(
                resultatGrunnlagPeriode.getBeregningsgrunnlagPeriode().getFom(),
                resultatGrunnlagPeriode.getBeregningsgrunnlagPeriode().getTomOrNull()
            )
            .medRegelEvaluering(foreslå, regelInput, regelResultat == null ? null : regelResultat.getRegelSporing())
            .tilbakestillPeriodeÅrsaker()
            .leggTilPeriodeÅrsaker(mapPeriodeÅrsak(resultatGrunnlagPeriode.getPeriodeÅrsaker()))
            .build(eksisterendeVLGrunnlag);
        return vlBGPeriode;
    }

    private void opprettBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPeriode kopierFra, BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        kopierFra.getBeregningsgrunnlagPrStatusOgAndelList().forEach(bgpsa -> {
            BeregningsgrunnlagPrStatusOgAndel.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medArbforholdType(bgpsa.getArbeidsforholdType())
                .medAktivitetStatus(bgpsa.getAktivitetStatus())
                .medInntektskategori(bgpsa.getInntektskategori());
            Optional<Arbeidsgiver> arbeidsgiver = bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver);
            Optional<ArbeidsforholdRef> arbeidsforholdRef = bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef);
            if (arbeidsgiver.isPresent() || arbeidsforholdRef.isPresent()) {
                BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold = BGAndelArbeidsforhold.builder()
                    .medArbeidsgiver(arbeidsgiver.orElse(null))
                    .medArbforholdRef(fra(arbeidsforholdRef.orElse(null)));
                andelBuilder
                    .medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
            }
            andelBuilder.build(beregningsgrunnlagPeriode);
        });
    }

    private String fra(ArbeidsforholdRef arbeidsforholdRef) {
        return arbeidsforholdRef == null ? null : arbeidsforholdRef.getReferanse();
    }

    private void mapSammenligningsgrunnlag(final SammenligningsGrunnlag resultatSammenligningsGrunnlag, no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag eksisterendeVLGrunnlag) {
        if (resultatSammenligningsGrunnlag != null) {
            Sammenligningsgrunnlag.builder()
                .medSammenligningsperiode(
                    resultatSammenligningsGrunnlag.getSammenligningsperiode().getFom(),
                    resultatSammenligningsGrunnlag.getSammenligningsperiode().getTom()
                )
                .medRapportertPrÅr(resultatSammenligningsGrunnlag.getRapportertPrÅr())
                .medAvvikPromille(resultatSammenligningsGrunnlag.getAvvikPromille())
                .build(eksisterendeVLGrunnlag);
        }
    }

    public no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag mapForSkjæringstidspunktOgStatuser(Behandling behandling, AktivitetStatusModell regelmodell, Dekningsgrad dekningsgrad, List<String> regelInput, List<RegelResultat> regelresultater) {
        Objects.requireNonNull(regelmodell, "regelmodell");
        // Regelinput og regelresultat brukes kun til logging
        Objects.requireNonNull(regelInput, "regelInput");
        Objects.requireNonNull(regelresultater, "regelresultater");
        if (regelInput.size() != 2 || regelresultater.size() != 2) {
            throw new IllegalStateException("Antall regelinput og regelresultater må være 2 for å spore regellogg");
        }

        if (regelmodell.getAktivitetStatuser().containsAll(Arrays.asList(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.DP,
            no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.AAP))) {
            throw new IllegalStateException("Ugyldig kombinasjon av statuser: Kan ikke både ha status AAP og DP samtidig");
        }

        BigDecimal grunnbeløp = BigDecimal.valueOf(beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, regelmodell.getSkjæringstidspunktForBeregning()).getVerdi());
        no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag =
            no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag.builder()
                .medSkjæringstidspunkt(regelmodell.getSkjæringstidspunktForBeregning())
                .medDekningsgrad(getDekningsdrad(dekningsgrad))
                .medOpprinneligSkjæringstidspunkt(regelmodell.getSkjæringstidspunktForBeregning())
                .medGrunnbeløp(grunnbeløp)
                .medRedusertGrunnbeløp(grunnbeløp)
                // Logging (input -> resultat)
                .medRegelloggSkjæringstidspunkt(regelInput.get(0), regelresultater.get(0).getRegelSporing())
                .medRegelloggBrukersStatus(regelInput.get(1), regelresultater.get(1).getRegelSporing())
                .build();
        regelmodell.getAktivitetStatuser()
            .forEach(as -> BeregningsgrunnlagAktivitetStatus.builder()
                .medAktivitetStatus(mapAktivitetStatusfraRegel(regelmodell, as))
                .build(beregningsgrunnlag));
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(regelmodell.getSkjæringstidspunktForBeregning(), null)
            .build(beregningsgrunnlag);
        Collection<Yrkesaktivitet> yrkesaktiviteter = inntektArbeidYtelseTjeneste.hentYrkesaktiviteterForSøker(behandling, false);
        opprettBeregningsgrunnlagPrStatusOgAndelForSkjæringstidspunkt(yrkesaktiviteter, regelmodell, beregningsgrunnlagPeriode);
        return beregningsgrunnlag;
    }

    private void opprettBeregningsgrunnlagPrStatusOgAndelForSkjæringstidspunkt(Collection<Yrkesaktivitet> yrkesaktiviteter, AktivitetStatusModell regelmodell, BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        LocalDate skjæringstidspunkt = regelmodell.getSkjæringstidspunktForBeregning();
        DatoIntervallEntitet beregningsperiode = BeregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(skjæringstidspunkt);
        regelmodell.getBeregningsgrunnlagPrStatusListe().stream()
            .filter(bgps -> erATFL(bgps.getAktivitetStatus()))
            .forEach(bgps -> bgps.getArbeidsforholdList()
                .forEach(af -> {
                    Optional<AktivitetsAvtale> aktivitetAvtale = yrkesaktiviteter.stream()
                        .filter(ya -> matchArbeidsgiver(af, ya))
                        .filter(ya -> ya.getArbeidsforholdRef().isPresent() &&
                            ya.getArbeidsforholdRef().get().gjelderFor(ArbeidsforholdRef.ref(af.getArbeidsforholdId())))
                        .findFirst()
                        .flatMap(Yrkesaktivitet::getAnsettelsesPeriode);

                    BeregningsgrunnlagPrStatusOgAndel.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndel.builder()
                        .medArbforholdType(aktivitetMap.get(af.getAktivitet()))
                        .medAktivitetStatus(af.erFrilanser() ? AktivitetStatus.FRILANSER : AktivitetStatus.ARBEIDSTAKER)
                        .medBeregningsperiode(beregningsperiode.getFomDato(), beregningsperiode.getTomDato());
                    LocalDate arbeidsperiodeFom = aktivitetAvtale.map(AktivitetsAvtale::getFraOgMed).orElse(null);
                    LocalDate arbeidsperiodeTom = aktivitetAvtale.map(AktivitetsAvtale::getTilOgMed).orElse(null);
                    if (arbeidsperiodeFom != null || af.getReferanseType() != null || af.getArbeidsforholdId() != null) {
                        BGAndelArbeidsforhold.Builder bgArbeidsforholdBuilder = BGAndelArbeidsforhold.builder()
                            .medArbeidsgiver(lagArbeidsgiver(af))
                            .medArbforholdRef(af.getArbeidsforholdId())
                            .medArbeidsperiodeTom(arbeidsperiodeTom)
                            .medArbeidsperiodeFom(arbeidsperiodeFom);
                        andelBuilder.medBGAndelArbeidsforhold(bgArbeidsforholdBuilder);
                    }
                    andelBuilder
                        .build(beregningsgrunnlagPeriode);
                }));
        regelmodell.getBeregningsgrunnlagPrStatusListe().stream()
            .filter(bgps -> !(erATFL(bgps.getAktivitetStatus())))
            .forEach(bgps -> BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(mapAktivitetStatusfraRegel(regelmodell, bgps.getAktivitetStatus()))
                .build(beregningsgrunnlagPeriode));
    }

    private Arbeidsgiver lagArbeidsgiver(Arbeidsforhold af) {
        if (ReferanseType.AKTØR_ID.equals(af.getReferanseType())) {
            return Arbeidsgiver.person(new AktørId(af.getAktørId()));
        } else if (ReferanseType.ORG_NR.equals(af.getReferanseType())) {
            return Arbeidsgiver.virksomhet(virksomhetRepository.hent(af.getOrgnr())
                .orElseThrow(() -> new IllegalStateException("Fann ikkje virksomhet med orgnr " + af.getOrgnr())));
        }
        return null;
    }

    private boolean matchArbeidsgiver(Arbeidsforhold af, Yrkesaktivitet ya) {
        if (ya.getArbeidsgiver() == null) {
            return false;
        }
        if (ReferanseType.ORG_NR.equals(af.getReferanseType()) && ya.getArbeidsgiver().getErVirksomhet()) {
            return af.getOrgnr().equals(ya.getArbeidsgiver().getIdentifikator());
        } else if (ReferanseType.AKTØR_ID.equals(af.getReferanseType()) && !ya.getArbeidsgiver().getErVirksomhet()) {
            return af.getAktørId().equals(ya.getArbeidsgiver().getIdentifikator());
        }
        return false;
    }

    private long getDekningsdrad(Dekningsgrad dekningsgrad) {
        if (dekningsgrad.equals(Dekningsgrad.DEKNINGSGRAD_80)) {
            return 80L;
        }
        if (dekningsgrad.equals(Dekningsgrad.DEKNINGSGRAD_100)) {
            return 100L;
        }
        throw new IllegalArgumentException("Ukjent dekningsgrad: Må være enten 80% eller 100%. Kan ikke mappe dekningsgrad " + dekningsgrad);
    }

    private boolean erATFL(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus aktivitetStatus) {
        return no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL.equals(aktivitetStatus);
    }

    private boolean erATFL_SN(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus aktivitetStatus) {
        return no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL_SN.equals(aktivitetStatus);
    }

    private AktivitetStatus mapAktivitetStatusfraRegel(AktivitetStatusModell regelmodell, no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus regelVerdi) {
        if (erATFL(regelVerdi)) {
            return kombinertStatus(regelmodell, false);
        } else if (erATFL_SN(regelVerdi)) {
            return kombinertStatus(regelmodell, true);
        }
        return beregningsgrunnlagAktivitetStatusMap.get(regelVerdi);
    }

    private AktivitetStatus kombinertStatus(AktivitetStatusModell regelmodell, boolean medSN) {
        List<Arbeidsforhold> alleArbeidsforhold = regelmodell.getBeregningsgrunnlagPrStatusListe().stream()
            .flatMap(bgps -> bgps.getArbeidsforholdList().stream())
            .collect(Collectors.toList());
        Optional<Arbeidsforhold> frilanser = alleArbeidsforhold.stream().filter(Arbeidsforhold::erFrilanser).findAny();
        Optional<Arbeidsforhold> arbeidstaker = alleArbeidsforhold.stream().filter(af -> !(af.erFrilanser())).findAny();
        if (frilanser.isPresent()) {
            if (arbeidstaker.isPresent()) {
                return medSN ? AktivitetStatus.KOMBINERT_AT_FL_SN : AktivitetStatus.KOMBINERT_AT_FL;
            } else {
                return medSN ? AktivitetStatus.KOMBINERT_FL_SN : AktivitetStatus.FRILANSER;
            }
        } else {
            return medSN ? AktivitetStatus.KOMBINERT_AT_SN : AktivitetStatus.ARBEIDSTAKER;
        }
    }

    private List<PeriodeÅrsak> mapPeriodeÅrsak(List<no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak> periodeÅrsaker) {
        //TODO (TOPAS) Kodeverk eller et static map istedenfor?
        List<PeriodeÅrsak> periodeÅrsakerMapped = new ArrayList<>();
        periodeÅrsaker.forEach(periodeÅrsak -> {
            if (no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak.NATURALYTELSE_BORTFALT.equals(periodeÅrsak)) {
                periodeÅrsakerMapped.add(PeriodeÅrsak.NATURALYTELSE_BORTFALT);
            } else if (no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET.equals(periodeÅrsak)) {
                periodeÅrsakerMapped.add(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
            } else if (no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak.NATURALYTELSE_TILKOMMER.equals(periodeÅrsak)) {
                periodeÅrsakerMapped.add(PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
            } else if (no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV.equals(periodeÅrsak)) {
                periodeÅrsakerMapped.add(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
            } else if (no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak.REFUSJON_OPPHØRER.equals(periodeÅrsak)) {
                periodeÅrsakerMapped.add(PeriodeÅrsak.REFUSJON_OPPHØRER);
            } else if (no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak.GRADERING.equals(periodeÅrsak)) {
                periodeÅrsakerMapped.add(PeriodeÅrsak.GRADERING);
            }
        });
        return periodeÅrsakerMapped;
    }
}
