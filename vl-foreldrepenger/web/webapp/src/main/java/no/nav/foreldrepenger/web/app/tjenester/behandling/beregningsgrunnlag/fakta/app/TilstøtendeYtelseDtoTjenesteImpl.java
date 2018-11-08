package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.beregningsgrunnlag.KontrollerFaktaBeregningTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.typer.Stillingsprosent;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.TilstøtendeYtelseAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.TilstøtendeYtelseDto;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

@ApplicationScoped
public class TilstøtendeYtelseDtoTjenesteImpl implements TilstøtendeYtelseDtoTjeneste {

    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;
    private BeregningsgrunnlagDtoUtil dtoUtil;
    private static final Map<String, Integer> PERIODE_FREKVENS_MAP = new HashMap<>();

    static {
        PERIODE_FREKVENS_MAP.put("P1D", 260);
        PERIODE_FREKVENS_MAP.put("P1W", 52);
        PERIODE_FREKVENS_MAP.put("P2W", 26);
        PERIODE_FREKVENS_MAP.put("P1M", 12);
        PERIODE_FREKVENS_MAP.put("P1Y", 1);
    }

    TilstøtendeYtelseDtoTjenesteImpl() {
        // Hibernate
    }


    @Inject
    public TilstøtendeYtelseDtoTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                            KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste,
                                            OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste,
                                            BeregningsgrunnlagDtoUtil dtoUtil) {
        this.opptjeningInntektArbeidYtelseTjeneste = opptjeningInntektArbeidYtelseTjeneste;
        this.kontrollerFaktaBeregningTjeneste = kontrollerFaktaBeregningTjeneste;
        this.dtoUtil = dtoUtil;
    }

    @Override
    public Optional<TilstøtendeYtelseDto> lagTilstøtendeYtelseDto(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        if (beregningsgrunnlag.getAktivitetStatuser().stream().noneMatch(status -> AktivitetStatus.TILSTØTENDE_YTELSE.equals(status.getAktivitetStatus()))) {
            return Optional.empty();
        }
        if (beregningsgrunnlag.getBeregningsgrunnlagPerioder().size() < 1) {
            return Optional.empty();
        }
        Optional<Ytelse> ytelseOpt = opptjeningInntektArbeidYtelseTjeneste.hentSisteInfotrygdYtelseFørSkjæringstidspunktForOpptjening(behandling);
        if (!ytelseOpt.isPresent()) {
            return Optional.empty();
        }

        Ytelse ytelse = ytelseOpt.get();
        Optional<YtelseGrunnlag> ytelsegrunnlagOpt = ytelse.getYtelseGrunnlag();
        if (!ytelsegrunnlagOpt.isPresent()) {
            return Optional.empty();
        }

        YtelseGrunnlag ytelseGrunnlag = ytelsegrunnlagOpt.get();

        BigDecimal sum = ytelsegrunnlagOpt.map(YtelseGrunnlag::getYtelseStørrelse)
            .orElse(Collections.emptyList()).stream().map(this::mapTilÅrsinntekt).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        TilstøtendeYtelseDto tilstøtendeYtelse = new TilstøtendeYtelseDto();
        Long dekningsgrad = ytelsegrunnlagOpt.flatMap(YtelseGrunnlag::getDekningsgradProsent)
            .map(Stillingsprosent::getVerdi)
            .map(BigDecimal::longValue)
            .orElse(100L);
        tilstøtendeYtelse.setDekningsgrad(dekningsgrad);
        tilstøtendeYtelse.setBruttoBG(sum);
        Optional<Arbeidskategori> arbeidskategoriOpt = ytelseGrunnlag.getArbeidskategori();
        tilstøtendeYtelse.setArbeidskategori(arbeidskategoriOpt.orElse(null));
        tilstøtendeYtelse.setYtelseType(ytelse.getRelatertYtelseType());
        tilstøtendeYtelse.setTilstøtendeYtelseAndeler(lagTilstøtendeYtelseAndeler(behandling, beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)));
        tilstøtendeYtelse.setErBesteberegning(kontrollerFaktaBeregningTjeneste.skalHaBesteberegningForFødendeKvinne(behandling));
        if (RelatertYtelseType.FORELDREPENGER.equals(ytelse.getRelatertYtelseType())) {
            tilstøtendeYtelse.setSkalReduseres(true);
        } else if (RelatertYtelseType.SYKEPENGER.equals(ytelse.getRelatertYtelseType())) {
            dekningsgrad = ytelsegrunnlagOpt.flatMap(YtelseGrunnlag::getInntektsgrunnlagProsent)
                .map(Stillingsprosent::getVerdi)
                .map(BigDecimal::longValue)
                .orElse(100L);
            tilstøtendeYtelse.setDekningsgrad(dekningsgrad);
            boolean skalReduseres = arbeidskategoriOpt.map(Arbeidskategori.INAKTIV::equals).orElse(false);
            tilstøtendeYtelse.setSkalReduseres(skalReduseres);
        } else {
            tilstøtendeYtelse.setSkalReduseres(false);
        }
        return Optional.of(tilstøtendeYtelse);
    }

    private List<TilstøtendeYtelseAndelDto> lagTilstøtendeYtelseAndeler(Behandling behandling, BeregningsgrunnlagPeriode periode) {
        List<TilstøtendeYtelseAndelDto> tilstøtendeYtelseAndeler = new ArrayList<>();
        periode.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel -> {
            TilstøtendeYtelseAndelDto andelDto = lagTilstøtendeYtelseAndel(behandling, andel);
            tilstøtendeYtelseAndeler.add(andelDto);
        });
        leggTilAndelerSomErLagtTilManueltAvSaksbehandlerIForrigeFaktaavklaring(behandling, periode, tilstøtendeYtelseAndeler);
        tilstøtendeYtelseAndeler.sort(new TilstøtendeYtelseComparer());
        return tilstøtendeYtelseAndeler;
    }

    void leggTilAndelerSomErLagtTilManueltAvSaksbehandlerIForrigeFaktaavklaring(Behandling behandling, BeregningsgrunnlagPeriode periode, List<TilstøtendeYtelseAndelDto> tilstøtendeYtelseAndeler) {

        Optional<BeregningsgrunnlagPeriode> matchetPeriode = dtoUtil.finnMatchendePeriodeIForrigeBeregningsgrunnlag(behandling, periode);
        if (!matchetPeriode.isPresent()) {
            return;
        }
        matchetPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> andel.getLagtTilAvSaksbehandler()).forEach(andel -> {
                if (!periode.getBeregningsgrunnlagPrStatusOgAndelList().contains(andel)) {
                    TilstøtendeYtelseAndelDto tilstøtendeYtelseAndel = lagTilstøtendeYtelseAndel(behandling, andel);
                    tilstøtendeYtelseAndeler.add(tilstøtendeYtelseAndel);
                }
            }
        );
    }

    TilstøtendeYtelseAndelDto lagTilstøtendeYtelseAndel(Behandling behandling, BeregningsgrunnlagPrStatusOgAndel andel) {
        TilstøtendeYtelseAndelDto andelDto = new TilstøtendeYtelseAndelDto();
        andel.getBgAndelArbeidsforhold().ifPresent(bga ->
            dtoUtil.lagArbeidsforholdDto(andel)
            .ifPresent(andelDto::setArbeidsforhold));
        andelDto.setFordelingForrigeYtelse(andel.getÅrsbeløpFraTilstøtendeYtelseVerdi());
        andelDto.setRefusjonskrav(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null));
        andelDto.setInntektskategori(andel.getInntektskategori());
        andelDto.setFastsattPrAar(andel.getOverstyrtPrÅr() != null ? andel.getBeregnetPrÅr() : null);
        andelDto.setAndelsnr(andel.getAndelsnr());
        andelDto.setAktivitetStatus(andel.getAktivitetStatus());
        andelDto.setLagtTilAvSaksbehandler(andel.getLagtTilAvSaksbehandler());
        ÅpenDatoIntervallEntitet periode = ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(andel.getBeregningsgrunnlagPeriode().getBeregningsgrunnlagPeriodeFom(), TIDENES_ENDE);
        List<Gradering> graderingForAndelIPeriode = kontrollerFaktaBeregningTjeneste.hentGraderingerForAndelIPeriode(behandling, periode, andel).stream()
            .sorted(Comparator.comparing(Gradering::getPeriode)).collect(Collectors.toList());
        dtoUtil.finnArbeidsprosenterIPeriode(graderingForAndelIPeriode, periode).forEach(andelDto::leggTilAndelIArbeid);
        return andelDto;
    }


    private BigDecimal mapTilÅrsinntekt(YtelseStørrelse ytelseStørrelse) {
        InntektPeriodeType hyppighet = ytelseStørrelse.getHyppighet();
        String periode = hyppighet.getPeriode();
        int frekvensPrÅr = PERIODE_FREKVENS_MAP.get(periode);
        return ytelseStørrelse.getBeløp().getVerdi().multiply(BigDecimal.valueOf(frekvensPrÅr));
    }

    public static class TilstøtendeYtelseComparer implements Comparator<TilstøtendeYtelseAndelDto>, Serializable {
        @Override
        public int compare(TilstøtendeYtelseAndelDto andel1, TilstøtendeYtelseAndelDto andel2) {
            if (andel1 == null || andel2 == null) {
                return 0;
            }
            if (andel1.getArbeidsforhold() != null && andel2.getArbeidsforhold() != null) {
                return andel1.getArbeidsforhold().getArbeidsgiverNavn().compareTo(andel2.getArbeidsforhold().getArbeidsgiverNavn());
            }
            if (andel1.getArbeidsforhold() != null) {
                // andel1 skal komme først
                return andel1.getArbeidsforhold().getArbeidsgiverNavn().compareTo(andel2.getAktivitetStatus().getNavn());
            }
            if (andel2.getArbeidsforhold() != null) {
                return andel1.getAktivitetStatus().getNavn().compareTo(andel2.getArbeidsforhold().getArbeidsgiverNavn());
            }
            return andel1.getAktivitetStatus().compareTo(andel2.getAktivitetStatus());
        }
    }

}
