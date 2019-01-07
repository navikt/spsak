package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app;

import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettBeregningsgrunnlagPerioderTjenesteImpl.MÅNEDER_I_1_ÅR;
import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.EndringBeregningsgrunnlagAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.EndringBeregningsgrunnlagArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.EndringBeregningsgrunnlagDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.EndringBeregningsgrunnlagPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.GraderingEllerRefusjonDto;

@ApplicationScoped
public class EndringBeregningsgrunnlagDtoTjeneste {

    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    private BeregningsgrunnlagDtoUtil dtoUtil;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    EndringBeregningsgrunnlagDtoTjeneste() {
        // Hibernate
    }


    @Inject
    public EndringBeregningsgrunnlagDtoTjeneste(KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste,
                                                BeregningsgrunnlagDtoUtil dtoUtil, BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.kontrollerFaktaBeregningTjeneste = kontrollerFaktaBeregningTjeneste;
        this.dtoUtil = dtoUtil;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    // TODO (topas) denne bør være privat eller skrives om for å være mer testbar
    public Optional<EndringBeregningsgrunnlagDto> lagEndringAvBeregningsgrunnlagDto(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        EndringBeregningsgrunnlagDto bgDto = new EndringBeregningsgrunnlagDto();
        List<BeregningsgrunnlagPeriode> bgPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        setEndredeArbeidsforhold(behandling, beregningsgrunnlag, bgDto);
        List<EndringBeregningsgrunnlagPeriodeDto> endringPerioder = bgPerioder.stream()
            .map(periode -> {
                EndringBeregningsgrunnlagPeriodeDto endringBGPeriode = new EndringBeregningsgrunnlagPeriodeDto();
                endringBGPeriode.setFom(periode.getBeregningsgrunnlagPeriodeFom());
                endringBGPeriode.setTom(periode.getBeregningsgrunnlagPeriodeTom());
                List<EndringBeregningsgrunnlagAndelDto> endringAndeler = lagEndretBGAndeler(behandling, periode, endringBGPeriode);
                endringBGPeriode.setEndringBeregningsgrunnlagAndeler(endringAndeler);
                return endringBGPeriode;
            })
            .sorted(Comparator.comparing(EndringBeregningsgrunnlagPeriodeDto::getFom)).collect(toList());
        bgDto.setEndringBeregningsgrunnlagPerioder(endringPerioder);
        return Optional.of(bgDto);
    }

    private void setEndredeArbeidsforhold(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag, EndringBeregningsgrunnlagDto bgDto) {
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForAndel(behandling, beregningsgrunnlag.getSkjæringstidspunkt(), andel.getBeregningsgrunnlagPeriode(), andel))
            .distinct()
            .forEach(distinctAndel ->
                dtoUtil.lagArbeidsforholdEndringDto(distinctAndel)
                    .ifPresent(af -> {
                        EndringBeregningsgrunnlagArbeidsforholdDto endringAf = (EndringBeregningsgrunnlagArbeidsforholdDto) af;
                        settEndretArbeidsforholdForNyttRefusjonskrav(behandling, distinctAndel, endringAf);
                        settEndretArbeidsforholdForSøktGradering(behandling, distinctAndel, endringAf);
                        if (!endringAf.getPerioderMedGraderingEllerRefusjon().isEmpty()) {
                            bgDto.leggTilEndretArbeidsforhold(endringAf);
                        }
                    }));
    }

    private void settEndretArbeidsforholdForNyttRefusjonskrav(Behandling behandling, BeregningsgrunnlagPrStatusOgAndel distinctAndel, EndringBeregningsgrunnlagArbeidsforholdDto endretArbeidsforhold) {
        Optional<Periode> refusjonsPeriodeOpt = kontrollerFaktaBeregningTjeneste.hentRefusjonsPeriodeForAndel(behandling, distinctAndel);
        if (refusjonsPeriodeOpt.isPresent()) {
            GraderingEllerRefusjonDto refusjonDto = new GraderingEllerRefusjonDto(true, false);
            Periode refusjonsperiode = refusjonsPeriodeOpt.get();
            refusjonDto.setFom(refusjonsperiode.getFomOrNull());
            refusjonDto.setTom(TIDENES_ENDE.minusDays(2).isBefore(refusjonsperiode.getTom()) ? null : refusjonsperiode.getTom());
            endretArbeidsforhold.leggTilPeriodeMedGraderingEllerRefusjon(refusjonDto);
        }
    }

    private void settEndretArbeidsforholdForSøktGradering(Behandling behandling, BeregningsgrunnlagPrStatusOgAndel distinctAndel, EndringBeregningsgrunnlagArbeidsforholdDto endretArbeidsforhold) {
        List<Gradering> graderingerForArbeidsforhold = kontrollerFaktaBeregningTjeneste.hentGraderingerForAndel(behandling, distinctAndel);
        graderingerForArbeidsforhold.forEach(gradering -> {
            GraderingEllerRefusjonDto graderingDto = new GraderingEllerRefusjonDto(false, true);
            graderingDto.setFom(gradering.getPeriode().getFomDato());
            graderingDto.setTom(gradering.getPeriode().getTomDato().isBefore(TIDENES_ENDE) ? gradering.getPeriode().getTomDato() : null);
            endretArbeidsforhold.leggTilPeriodeMedGraderingEllerRefusjon(graderingDto);
        });
    }

    private List<EndringBeregningsgrunnlagAndelDto> lagEndretBGAndeler(Behandling behandling, BeregningsgrunnlagPeriode periode, EndringBeregningsgrunnlagPeriodeDto endringBGPeriode) {
        List<EndringBeregningsgrunnlagAndelDto> endringAndeler = new ArrayList<>();
        for (BeregningsgrunnlagPrStatusOgAndel andel : periode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            EndringBeregningsgrunnlagAndelDto endringAndel = lagEndretBGAndel(behandling, periode, endringBGPeriode, andel);
            endringAndeler.add(endringAndel);
        }

        if (endringBGPeriode.isHarPeriodeAarsakGraderingEllerRefusjon()) {
            leggTilAndelerSomErLagtTilManueltVedForrigeFaktaavklaring(periode, endringAndeler, behandling, endringBGPeriode);
        }

        return endringAndeler;
    }

    private void leggTilAndelerSomErLagtTilManueltVedForrigeFaktaavklaring(BeregningsgrunnlagPeriode periodeINyttGrunnlag,
                                                                          List<EndringBeregningsgrunnlagAndelDto> endringAndeler,
                                                                          Behandling behandling, EndringBeregningsgrunnlagPeriodeDto endringBGPeriode) {
        Optional<BeregningsgrunnlagPeriode> matchetPeriode = dtoUtil.finnMatchendePeriodeIForrigeBeregningsgrunnlag(behandling, periodeINyttGrunnlag);
        if (!matchetPeriode.isPresent()) {
            return;
        }
        matchetPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> andel.getLagtTilAvSaksbehandler()).forEach(andel -> {
                if (!periodeINyttGrunnlag.getBeregningsgrunnlagPrStatusOgAndelList().contains(andel)) {
                    EndringBeregningsgrunnlagAndelDto endringAndel = lagEndretBGAndel(behandling,
                        matchetPeriode.get(), endringBGPeriode, andel);
                    endringAndeler.add(endringAndel);
                }
            }
        );
    }

    private EndringBeregningsgrunnlagAndelDto lagEndretBGAndel(Behandling behandling, BeregningsgrunnlagPeriode periode,
                                                               EndringBeregningsgrunnlagPeriodeDto endringBGPeriode, BeregningsgrunnlagPrStatusOgAndel andel) {
        EndringBeregningsgrunnlagAndelDto endringAndel = new EndringBeregningsgrunnlagAndelDto();
        Optional<BeregningsgrunnlagPrStatusOgAndel> andelIGjeldendeGrunnlag = kontrollerFaktaBeregningTjeneste.hentKorresponderendeAndelIGjeldendeBeregningsgrunnlag(behandling, periode, andel);
        endringAndel.setAndelsnr(andel.getAndelsnr());
        endringAndel.setAktivitetStatus(andel.getAktivitetStatus());
        endringAndel.setInntektskategori(andelIGjeldendeGrunnlag, andel);
        dtoUtil.lagArbeidsforholdDto(andel)
            .ifPresent(endringAndel::setArbeidsforhold);
        settFordelingForrigeBehandling(behandling, periode, andel, endringAndel);
        endringAndel.utledVerdierForFastsattForrige(andelIGjeldendeGrunnlag);
        endringAndel.initialiserVerdierForBeregnet(andel.getBeregnetPrÅr());
        endringAndel.setLagtTilAvSaksbehandler(andel.getLagtTilAvSaksbehandler());
        endringAndel.setFastsattAvSaksbehandler(Boolean.TRUE.equals(andel.getFastsattAvSaksbehandler()));
        List<Gradering> graderingForAndelIPeriode = kontrollerFaktaBeregningTjeneste.hentGraderingerForAndelIPeriode(behandling, periode.getPeriode(), andel).stream()
            .sorted(Comparator.comparing(Gradering::getPeriode)).collect(Collectors.toList());
        dtoUtil.finnArbeidsprosenterIPeriode(graderingForAndelIPeriode, periode.getPeriode()).forEach(endringAndel::leggTilAndelIArbeid);
        Optional<Inntektsmelding> inntektsmeldingOpt = kontrollerFaktaBeregningTjeneste.hentInntektsmeldingForAndel(behandling, andel);
        inntektsmeldingOpt.ifPresent(im -> endringAndel.setBelopFraInntektsmelding(im.getInntektBeløp().getVerdi()));
        settRefusjonskrav(behandling, andel, endringAndel, periode);
        if (kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForAndel(behandling, periode.getBeregningsgrunnlag().getSkjæringstidspunkt(), andel.getBeregningsgrunnlagPeriode(), andel)) {
            håndterGraderingEllerRefusjon(behandling, periode.getBeregningsgrunnlag().getSkjæringstidspunkt(), periode, endringBGPeriode, andel);
        }
        return endringAndel;
    }

    private void settRefusjonskrav(Behandling behandling, BeregningsgrunnlagPrStatusOgAndel andel, EndringBeregningsgrunnlagAndelDto endringAndel, BeregningsgrunnlagPeriode periode) {
        if (andel.getLagtTilAvSaksbehandler()) {
            endringAndel.setRefusjonskravFraInntektsmelding(BigDecimal.ZERO);
        } else {
            BeregningsgrunnlagGrunnlagEntitet bg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.OPPRETTET)
                .orElseThrow(() -> new IllegalStateException("Har ikke opprettet beregningsgrunnlag for behandling med id " + behandling.getId()));
            Optional<BeregningsgrunnlagPrStatusOgAndel> andelOpt = bg.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
                .filter(bgPeriode -> bgPeriode.getPeriode().inkluderer(periode.getPeriode().getFomDato()))
                .flatMap(bgPeriode -> bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .filter(bgAndel -> bgAndel.matchUtenInntektskategori(andel)).findFirst();
            if (andelOpt.isPresent()) {
                BigDecimal refusjonsKrav = andelOpt.get().getBgAndelArbeidsforhold()
                    .map(BGAndelArbeidsforhold::getRefusjonskravPrÅr)
                    .orElse(BigDecimal.ZERO).divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP);
                endringAndel.setRefusjonskravFraInntektsmelding(refusjonsKrav);
            }
        }
        endringAndel.setRefusjonskrav(andel.getBgAndelArbeidsforhold()
            .map(BGAndelArbeidsforhold::getRefusjonskravPrÅr)
            .orElse(BigDecimal.ZERO).divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP));
    }

    private void settFordelingForrigeBehandling(Behandling behandling, BeregningsgrunnlagPeriode periode, BeregningsgrunnlagPrStatusOgAndel andel, EndringBeregningsgrunnlagAndelDto endringAndel) {
        if (behandling.erRevurdering()) {
            Optional<BeregningsgrunnlagPrStatusOgAndel> korAndelOpt = kontrollerFaktaBeregningTjeneste.hentKorresponderendeAndelIOriginaltBeregningsgrunnlag(behandling, periode, andel);
            if (korAndelOpt.isPresent() && korAndelOpt.get().getBeregnetPrÅr() != null) {
                endringAndel.setFordelingForrigeBehandlingPrAar(korAndelOpt.get().getBeregnetPrÅr());
                endringAndel.setFordelingForrigeBehandling(korAndelOpt.get().getBeregnetPrÅr().divide(BigDecimal.valueOf(MÅNEDER_I_1_ÅR), 0, RoundingMode.HALF_UP));
                return;
            }
        }
        if (kontrollerFaktaBeregningTjeneste.tilkomArbeidsforholdEtterStp(behandling, periode.getBeregningsgrunnlag().getSkjæringstidspunkt(), andel)) {
            endringAndel.setFordelingForrigeBehandlingPrAar(BigDecimal.ZERO);
            endringAndel.setFordelingForrigeBehandling(BigDecimal.ZERO);
            return;
        }

        Optional<Inntektsmelding> inntektsmelding = kontrollerFaktaBeregningTjeneste.hentInntektsmeldingForAndel(behandling, andel);
        if (inntektsmelding.isPresent() && !andel.getLagtTilAvSaksbehandler()) {
            endringAndel.setFordelingForrigeBehandling(inntektsmelding.get().getInntektBeløp().getVerdi());
            endringAndel.setFordelingForrigeBehandlingPrAar(inntektsmelding.get().getInntektBeløp().multipliser(12).getVerdi());
        }
    }

    private void håndterGraderingEllerRefusjon(Behandling behandling, LocalDate skjæringstidspunkt, BeregningsgrunnlagPeriode periode, EndringBeregningsgrunnlagPeriodeDto endringBGPeriode,
                                               BeregningsgrunnlagPrStatusOgAndel andel) {
        endringBGPeriode.setHarPeriodeAarsakGraderingEllerRefusjon(true);
        endringBGPeriode.setSkalKunneEndreRefusjon(skalKunneEndreRefusjon(behandling, periode, andel, skjæringstidspunkt));
    }

    // TODO (topas) denne bør være privat eller skrives om for å være mer testbar
    // TODO (ESVE) Bør denne returnere true for alle caser med gradering og refusjon større enn 6G?
    public boolean skalKunneEndreRefusjon(Behandling behandling, BeregningsgrunnlagPeriode periode, BeregningsgrunnlagPrStatusOgAndel andel, LocalDate skjæringstidspunkt) {
        boolean arbfholdTilkomEtterStp = kontrollerFaktaBeregningTjeneste.tilkomArbeidsforholdEtterStp(behandling, skjæringstidspunkt, andel);
        List<Gradering> graderingForAndelIPeriode = kontrollerFaktaBeregningTjeneste.hentGraderingerForAndelIPeriode(behandling, periode.getPeriode(), andel);
        boolean andelHarGradering = !graderingForAndelIPeriode.isEmpty();
        Beløp refusjonskravPrÅr = new Beløp(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null));
        boolean andelHarRefusjon = !refusjonskravPrÅr.erNullEllerNulltall();

        if (andelHarGradering && !andelHarRefusjon) {
            if (arbfholdTilkomEtterStp) {
                return false;
            }
            Optional<BeregningsgrunnlagPrStatusOgAndel> korAndelOpt = kontrollerFaktaBeregningTjeneste.hentKorresponderendeAndelIGjeldendeBeregningsgrunnlag(behandling, periode, andel);
            Beløp bruttoPrÅr = new Beløp(korAndelOpt.map(BeregningsgrunnlagPrStatusOgAndel::getBruttoPrÅr).orElse(BigDecimal.ZERO));
            if (bruttoPrÅr.erNulltall()) {
                return true;
            }
            Beløp avkortetPrÅr = new Beløp(korAndelOpt.map(BeregningsgrunnlagPrStatusOgAndel::getAvkortetPrÅr).orElse(null));
            return avkortetPrÅr.erNulltall();
        } else {
            return false;
        }
    }


}
