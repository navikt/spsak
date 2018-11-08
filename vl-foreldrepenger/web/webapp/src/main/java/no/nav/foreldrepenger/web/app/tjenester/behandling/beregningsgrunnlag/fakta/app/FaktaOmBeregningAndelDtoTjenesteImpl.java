package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.beregningsgrunnlag.KontrollerFaktaBeregningFrilanserTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.KontrollerFaktaBeregningTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.ATogFLISammeOrganisasjonDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.FaktaOmBeregningAndelDto;

@ApplicationScoped
public class FaktaOmBeregningAndelDtoTjenesteImpl implements FaktaOmBeregningAndelDtoTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    private KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilanserTjeneste;
    private BeregningsgrunnlagDtoUtil dtoUtil;

    FaktaOmBeregningAndelDtoTjenesteImpl() {
        // Hibernate
    }

    @Inject
    public FaktaOmBeregningAndelDtoTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                                KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste,
                                                KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilanserTjeneste,
                                                BeregningsgrunnlagDtoUtil dtoUtil) {
        this.kontrollerFaktaBeregningTjeneste = kontrollerFaktaBeregningTjeneste;
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.kontrollerFaktaBeregningFrilanserTjeneste = kontrollerFaktaBeregningFrilanserTjeneste;
        this.dtoUtil = dtoUtil;
    }

    @Override
    public Optional<FaktaOmBeregningAndelDto> lagFrilansAndelDto(Beregningsgrunnlag beregningsgrunnlag) {
        if (beregningsgrunnlag.getBeregningsgrunnlagPerioder().isEmpty()) {
            return Optional.empty();
        }
        BeregningsgrunnlagPeriode førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndel frilansAndel = førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
            .findFirst()
            .orElse(null);
        if (frilansAndel != null) {
            FaktaOmBeregningAndelDto dto = new FaktaOmBeregningAndelDto();
            dtoUtil.lagArbeidsforholdDto(frilansAndel)
                .ifPresent(dto::setArbeidsforhold);
            dto.setInntektskategori(frilansAndel.getInntektskategori());
            dto.setAndelsnr(frilansAndel.getAndelsnr());
            return Optional.of(dto);
        }
        return Optional.empty();
    }


    /// ATFL I samme organisasjon
    @Override
    public List<ATogFLISammeOrganisasjonDto> lagATogFLISAmmeOrganisasjonListe(Behandling behandling) {
        Set<Arbeidsgiver> arbeidsgivere = kontrollerFaktaBeregningFrilanserTjeneste.brukerErArbeidstakerOgFrilanserISammeOrganisasjon(behandling);
        if (arbeidsgivere.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Virksomhet> arbeidsgivereSomErVirksomheter = arbeidsgivere
            .stream()
            .filter(Arbeidsgiver::getErVirksomhet)
            .map(Arbeidsgiver::getVirksomhet)
            .collect(Collectors.toSet());

        Map<Virksomhet, List<Inntektsmelding>> inntektsmeldingMap = kontrollerFaktaBeregningTjeneste.hentInntektsmeldingerForVirksomheter(behandling, arbeidsgivereSomErVirksomheter);
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);

        List<BeregningsgrunnlagPrStatusOgAndel> andeler = beregningsgrunnlag.getBeregningsgrunnlagPerioder()
            .get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(andel -> andel.getAktivitetStatus().erArbeidstaker())
            .collect(Collectors.toList());

        List<ATogFLISammeOrganisasjonDto> resultatListe = new ArrayList<>();
        for (Arbeidsgiver arbeidsgiver : arbeidsgivere) {
            andeler.stream()
                .filter(andel -> andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver).map(a -> a.equals(arbeidsgiver)).orElse(false))
                .forEach(andel -> resultatListe.add(lagATogFLISAmmeOrganisasjon(andel, inntektsmeldingMap)));
        }
        return resultatListe;
    }

    private ATogFLISammeOrganisasjonDto lagATogFLISAmmeOrganisasjon(BeregningsgrunnlagPrStatusOgAndel andel,
                                                                    Map<Virksomhet, List<Inntektsmelding>> inntektsmeldingMap) {
        ATogFLISammeOrganisasjonDto dto = new ATogFLISammeOrganisasjonDto();
        dtoUtil.lagArbeidsforholdDto(andel)
            .ifPresent(dto::setArbeidsforhold);
        dto.setAndelsnr(andel.getAndelsnr());
        dto.setInntektskategori(andel.getInntektskategori());

        // Privapersoner sender ikke inntektsmelding, disse må alltid fastsettes
        if (andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver).map(Arbeidsgiver::getErVirksomhet).orElse(false)) {
            Optional<Inntektsmelding> inntektsmelding = andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)
                .flatMap(virksomhet -> finnRiktigInntektsmelding(
                    inntektsmeldingMap,
                    virksomhet,
                    andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)));
            inntektsmelding.ifPresent(im -> dto.setInntektPrMnd(im.getInntektBeløp().getVerdi()));
        }
        return dto;
    }


    /// Arbeidsforhold uten inntektsmelding

    @Override
    public Optional<List<FaktaOmBeregningAndelDto>> lagArbeidsforholdUtenInntektsmeldingDtoList(Behandling behandling) {
        List<Yrkesaktivitet> aktiviteterMedLønnsendring = kontrollerFaktaBeregningTjeneste.finnAlleAktiviteterMedLønnsendringUtenInntektsmelding(behandling);
        if (aktiviteterMedLønnsendring.isEmpty()) {
            return Optional.empty();
        }
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet).isPresent())
            .collect(Collectors.toList());
        List<FaktaOmBeregningAndelDto> arbeidsforholdMedLønnsendringUtenIMDtoList = new ArrayList<>();
        for (Yrkesaktivitet aktivitet : aktiviteterMedLønnsendring) {
            Optional<Virksomhet> virksomhet = Optional.ofNullable(aktivitet.getArbeidsgiver()).map(Arbeidsgiver::getVirksomhet);
            BeregningsgrunnlagPrStatusOgAndel korrektAndel = finnKorrektAndelFraAktititet(andeler, virksomhet.orElse(null));
            FaktaOmBeregningAndelDto dto = lagArbeidsforholdUtenInntektsmeldingDto(korrektAndel);
            arbeidsforholdMedLønnsendringUtenIMDtoList.add(dto);
        }
        return Optional.of(arbeidsforholdMedLønnsendringUtenIMDtoList);
    }

    private FaktaOmBeregningAndelDto lagArbeidsforholdUtenInntektsmeldingDto(BeregningsgrunnlagPrStatusOgAndel andel) {
        FaktaOmBeregningAndelDto dto = new FaktaOmBeregningAndelDto();
        dtoUtil.lagArbeidsforholdDto(andel)
            .ifPresent(dto::setArbeidsforhold);
        dto.setAndelsnr(andel.getAndelsnr());
        dto.setInntektskategori(andel.getInntektskategori());
        return dto;
    }


    private BeregningsgrunnlagPrStatusOgAndel finnKorrektAndelFraAktititet(List<BeregningsgrunnlagPrStatusOgAndel> andeler, Virksomhet arbeidsgiver) {
        return andeler.stream()
            .filter(andel -> andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet).map(v -> v.equals(arbeidsgiver)).orElse(false))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Finner ikke korrekt andel for yrkesaktiviteten"));
    }


    private Optional<Inntektsmelding> finnRiktigInntektsmelding(Map<Virksomhet, List<Inntektsmelding>> inntektsmeldingMap, Virksomhet virksomhet, Optional<ArbeidsforholdRef> arbeidsforholdRef) {
        if (!inntektsmeldingMap.containsKey(virksomhet)) {
            return Optional.empty();
        }
        List<Inntektsmelding> inntektsmeldinger = inntektsmeldingMap.get(virksomhet);
        if (inntektsmeldinger.size() == 1) {
            return Optional.of(inntektsmeldinger.get(0));
        }
        return inntektsmeldinger.stream()
            .filter(im -> arbeidsforholdRef.map(ref -> ref.equals(im.getArbeidsforholdRef()))
                .orElse(false))
            .findFirst();
    }

}
