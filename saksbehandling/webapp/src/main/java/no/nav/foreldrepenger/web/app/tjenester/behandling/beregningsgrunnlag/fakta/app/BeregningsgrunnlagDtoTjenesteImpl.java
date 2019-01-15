package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.SatsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.SatsType;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagPrStatusOgAndelATDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagPrStatusOgAndelFLDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagPrStatusOgAndelSNDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.FaktaOmBeregningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.SammenligningsgrunnlagDto;

@ApplicationScoped
public class BeregningsgrunnlagDtoTjenesteImpl implements BeregningsgrunnlagDtoTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private SatsRepository beregningRepository;
    private FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste;
    private BeregningsgrunnlagDtoUtil dtoUtil;
    BeregningsgrunnlagDtoTjenesteImpl() {
        // Hibernate
    }


    @Inject
    public BeregningsgrunnlagDtoTjenesteImpl(GrunnlagRepositoryProvider repositoryProvider,
                                             ResultatRepositoryProvider resultatRepositoryProvider,
                                             FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste, BeregningsgrunnlagDtoUtil dtoUtil) {
        this.beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        this.beregningRepository = repositoryProvider.getSatsRepository();
        this.faktaOmBeregningDtoTjeneste = faktaOmBeregningDtoTjeneste;
        this.dtoUtil = dtoUtil;
    }

    @Override
    public Optional<BeregningsgrunnlagDto> lagBeregningsgrunnlagDto(Behandling behandling) {
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        if (!beregningsgrunnlagOpt.isPresent()) {
            return Optional.empty();
        }

        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagOpt.get();
        BeregningsgrunnlagDto dto = new BeregningsgrunnlagDto();

        Optional<FaktaOmBeregningDto> faktaOmBeregningDto = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(behandling, beregningsgrunnlag);
        faktaOmBeregningDto.ifPresent(dto::setFaktaOmBeregning);

        Optional<List<AktivitetStatus>> aktivitetStatusDtos = lagAktivitetStatusListe(beregningsgrunnlag);
        aktivitetStatusDtos.ifPresent(dto::setAktivitetStatus);

        Optional<SammenligningsgrunnlagDto> sammenligningsgrunnlagDto = lagSammenligningsgrunnlagDto(beregningsgrunnlag);
        sammenligningsgrunnlagDto.ifPresent(dto::setSammenligningsgrunnlag);

        dto.setBeregningsgrunnlagPeriode(lagBeregningsgrunnlagPeriodeDto(beregningsgrunnlag));
        dto.setSkjaeringstidspunktBeregning(beregningsgrunnlag.getSkjæringstidspunkt());
        dto.setLedetekstBrutto("Brutto beregningsgrunnlag");
        long seksG = Math.round(beregningsgrunnlag.getGrunnbeløp().getVerdi().multiply(BigDecimal.valueOf(6)).doubleValue());
        dto.setLedetekstAvkortet("Avkortet beregningsgrunnlag (6G=" + seksG + ")");
        dto.setHalvG((double) Math.round(beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, beregningsgrunnlag.getSkjæringstidspunkt()).getVerdi() / 2.0));

        return Optional.of(dto);
    }


    private Optional<SammenligningsgrunnlagDto> lagSammenligningsgrunnlagDto(Beregningsgrunnlag beregningsgrunnlag) {
        if (beregningsgrunnlag.getSammenligningsgrunnlag() == null) {
            return Optional.empty();
        }
        Sammenligningsgrunnlag sammenligningsgrunnlag = beregningsgrunnlag.getSammenligningsgrunnlag();
        SammenligningsgrunnlagDto dto = new SammenligningsgrunnlagDto();
        dto.setSammenligningsgrunnlagFom(sammenligningsgrunnlag.getSammenligningsperiodeFom());
        dto.setSammenligningsgrunnlagTom(sammenligningsgrunnlag.getSammenligningsperiodeTom());
        dto.setRapportertPrAar(sammenligningsgrunnlag.getRapportertPrÅr());
        dto.setAvvikPromille(sammenligningsgrunnlag.getAvvikPromille());
        return Optional.of(dto);
    }

    private List<BeregningsgrunnlagPeriodeDto> lagBeregningsgrunnlagPeriodeDto(Beregningsgrunnlag beregningsgrunnlag) {
        List<BeregningsgrunnlagPeriodeDto> dtoList = new ArrayList<>();
        List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagPerioder) {
            BeregningsgrunnlagPeriodeDto dto = new BeregningsgrunnlagPeriodeDto();
            dto.setBeregningsgrunnlagPeriodeFom(periode.getBeregningsgrunnlagPeriodeFom());
            dto.setBeregningsgrunnlagPeriodeTom(periode.getBeregningsgrunnlagPeriodeTom());
            dto.setBeregnetPrAar(periode.getBeregnetPrÅr());
            dto.setBruttoPrAar(periode.getBruttoPrÅr());
            dto.setBruttoInkludertBortfaltNaturalytelsePrAar(periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndel::getBruttoInkludertNaturalYtelser)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(null));
            dto.setAvkortetPrAar(periode.getAvkortetPrÅr());
            dto.setRedusertPrAar(periode.getRedusertPrÅr());
            dto.setDagsats(periode.getDagsats());
            dto.leggTilPeriodeAarsaker(periode.getPeriodeÅrsaker());
            dto.setElementer(lagBeregningsgrunnlagPrStatusOgAndelDto(periode.getBeregningsgrunnlagPrStatusOgAndelList()));
            dtoList.add(dto);
        }
        return dtoList;
    }

    private List<BeregningsgrunnlagPrStatusOgAndelDto> lagBeregningsgrunnlagPrStatusOgAndelDto(List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelList) {

        List<BeregningsgrunnlagPrStatusOgAndelDto> usortertDtoList = new ArrayList<>();
        for (BeregningsgrunnlagPrStatusOgAndel andel : beregningsgrunnlagPrStatusOgAndelList) {
            BeregningsgrunnlagPrStatusOgAndelDto dto = opprettTilpassetDTO(andel);
            dtoUtil.lagArbeidsforholdDto(andel).ifPresent(dto::setArbeidsforhold);
            dto.setAndelsnr(andel.getAndelsnr());
            dto.setInntektskategori(andel.getInntektskategori());
            dto.setAktivitetStatus(andel.getAktivitetStatus());
            dto.setBeregningsperiodeFom(andel.getBeregningsperiodeFom());
            dto.setBeregningsperiodeTom(andel.getBeregningsperiodeTom());
            dto.setBeregnetPrAar(andel.getBeregnetPrÅr());
            dto.setOverstyrtPrAar(andel.getOverstyrtPrÅr());
            dto.setBruttoPrAar(andel.getBruttoPrÅr());
            dto.setAvkortetPrAar(andel.getAvkortetPrÅr());
            dto.setRedusertPrAar(andel.getRedusertPrÅr());
            dto.setErTidsbegrensetArbeidsforhold(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getErTidsbegrensetArbeidsforhold).orElse(null));
            dto.setErNyIArbeidslivet(andel.getNyIArbeidslivet());
            dto.setLonnsendringIBeregningsperioden(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::erLønnsendringIBeregningsperioden).orElse(null));
            dto.setBesteberegningPrAar(andel.getBesteberegningPrÅr());
            usortertDtoList.add(dto);
        }
        // Følgende gjøres for å sortere arbeidsforholdene etter beregnet årsinntekt og deretter arbedsforholdId
        List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidsarbeidstakerAndeler = usortertDtoList.stream().filter(dto -> dto.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)).collect(toList());
        List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndreAndeler = usortertDtoList.stream().filter(dto -> !dto.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)).collect(toList());
        if (dtoKanSorteres(arbeidsarbeidstakerAndeler)) {
            arbeidsarbeidstakerAndeler.sort(comparatorEtterBeregnetOgArbeidsforholdId());
        }
        List<BeregningsgrunnlagPrStatusOgAndelDto> dtoList = new ArrayList<>(arbeidsarbeidstakerAndeler);
        dtoList.addAll(alleAndreAndeler);

        return dtoList;
    }

    private boolean dtoKanSorteres(List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidsarbeidstakerAndeler) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> listMedNull = arbeidsarbeidstakerAndeler
            .stream()
            .filter(a -> a.getBeregnetPrAar() == null)
            .collect(toList());
        return listMedNull.isEmpty();
    }

    private BeregningsgrunnlagPrStatusOgAndelDto opprettTilpassetDTO(BeregningsgrunnlagPrStatusOgAndel andel) {
        if (AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(andel.getAktivitetStatus())) {
            BeregningsgrunnlagPrStatusOgAndelSNDto dtoSN = new BeregningsgrunnlagPrStatusOgAndelSNDto();
            dtoSN.setPgiSnitt(andel.getPgiSnitt());
            dtoSN.setPgi1(andel.getPgi1());
            dtoSN.setPgi2(andel.getPgi2());
            dtoSN.setPgi3(andel.getPgi3());
            return dtoSN;

        } else if (AktivitetStatus.ARBEIDSTAKER.equals(andel.getAktivitetStatus())
            && andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr).isPresent()) {
            BeregningsgrunnlagPrStatusOgAndelATDto dtoAT = new BeregningsgrunnlagPrStatusOgAndelATDto();
            dtoAT.setBortfaltNaturalytelse(andel.getBgAndelArbeidsforhold().get().getNaturalytelseBortfaltPrÅr().get()); // NOSONAR
            return dtoAT;

        } else if (AktivitetStatus.FRILANSER.equals(andel.getAktivitetStatus())) {
            BeregningsgrunnlagPrStatusOgAndelFLDto dtoFL = new BeregningsgrunnlagPrStatusOgAndelFLDto();
            dtoFL.setErNyoppstartetEllerSammeOrganisasjon(andel.getFastsattAvSaksbehandler());
            return dtoFL;
        } else {
            return new BeregningsgrunnlagPrStatusOgAndelDto();
        }
    }

    private Optional<List<AktivitetStatus>> lagAktivitetStatusListe(Beregningsgrunnlag beregningsgrunnlag) {
        ArrayList<AktivitetStatus> statusListe = new ArrayList<>();
        for (BeregningsgrunnlagAktivitetStatus status : beregningsgrunnlag.getAktivitetStatuser()) {
            statusListe.add(status.getAktivitetStatus());
        }
        return Optional.of(statusListe);
    }

    private Comparator<BeregningsgrunnlagPrStatusOgAndelDto> comparatorEtterBeregnetOgArbeidsforholdId() {
        return Comparator.comparing(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrAar)
            .reversed();
    }


}
