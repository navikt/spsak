package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app;


import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.EndringBeregningsgrunnlagArbeidsforholdDto;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

@ApplicationScoped
public class BeregningsgrunnlagDtoUtil {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private TpsTjeneste tpsTjeneste;

    public BeregningsgrunnlagDtoUtil() {
        // CDI
    }

    @Inject
    public BeregningsgrunnlagDtoUtil(TpsTjeneste tpsTjeneste, BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.tpsTjeneste = tpsTjeneste;
    }

    Optional<BeregningsgrunnlagArbeidsforholdDto> lagArbeidsforholdDto(BeregningsgrunnlagPrStatusOgAndel andel) {
        BeregningsgrunnlagArbeidsforholdDto dto = new BeregningsgrunnlagArbeidsforholdDto();
        return lagBeregningsgrunnlagArbeidsforholdDto(andel, dto);
    }

    Optional<BeregningsgrunnlagArbeidsforholdDto> lagArbeidsforholdEndringDto(BeregningsgrunnlagPrStatusOgAndel andel) {
        BeregningsgrunnlagArbeidsforholdDto dto = new EndringBeregningsgrunnlagArbeidsforholdDto();
        return lagBeregningsgrunnlagArbeidsforholdDto(andel, dto);
    }


    private Optional<BeregningsgrunnlagArbeidsforholdDto> lagBeregningsgrunnlagArbeidsforholdDto(BeregningsgrunnlagPrStatusOgAndel andel,
                                                                                                 BeregningsgrunnlagArbeidsforholdDto arbeidsforhold) {
        if (!andel.getBgAndelArbeidsforhold().isPresent() || andel.getArbeidsforholdType() == null) {
            return Optional.empty();
        }
        Optional<Arbeidsgiver> arbeidsgiverOpt = andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver);
        if (arbeidsgiverOpt.isPresent()) {
            Arbeidsgiver arbeidsgiver = arbeidsgiverOpt.get();
            if (arbeidsgiver.getErVirksomhet()) {
                arbeidsforhold.setArbeidsgiverNavn(arbeidsgiver.getVirksomhet().getNavn());
                arbeidsforhold.setArbeidsgiverId(arbeidsgiver.getIdentifikator());
            } else if (arbeidsgiver.erAktørId()) {
                Optional<Personinfo> personinfo = tpsTjeneste.hentBrukerForAktør(arbeidsgiver.getAktørId());
                if (personinfo.isPresent()) {
                    arbeidsforhold.setArbeidsgiverNavn(personinfo.get().getNavn());
                    arbeidsforhold.setArbeidsgiverId(personinfo.get().getPersonIdent().getIdent());
                }
            }
        }
        arbeidsforhold.setOpphoersdato(finnKorrektOpphørsdato(andel));
        arbeidsforhold.setStartdato(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsperiodeFom).orElse(null));
        arbeidsforhold.setArbeidsforholdId(andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).map(ArbeidsforholdRef::getReferanse).orElse(null));
        arbeidsforhold.setArbeidsforholdType(andel.getArbeidsforholdType());
        return Optional.of(arbeidsforhold);
    }

    private LocalDate finnKorrektOpphørsdato(BeregningsgrunnlagPrStatusOgAndel andel) {
        return andel.getBgAndelArbeidsforhold()
            .flatMap(BGAndelArbeidsforhold::getArbeidsperiodeTom)
            .filter(tom -> !TIDENES_ENDE.equals(tom))
            .orElse(null);
    }

    public Optional<BeregningsgrunnlagPeriode> finnMatchendePeriodeIForrigeBeregningsgrunnlag(Behandling behandling, BeregningsgrunnlagPeriode periode) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeGrunnlag = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        if (!forrigeGrunnlag.isPresent()) {
            return Optional.empty();
        }
        List<BeregningsgrunnlagPeriode> matchedePerioder = forrigeGrunnlag.get().getBeregningsgrunnlag()
            .getBeregningsgrunnlagPerioder().stream()
            .filter(periodeIGjeldendeGrunnlag -> periode.getPeriode().overlapper(periodeIGjeldendeGrunnlag.getPeriode())).collect(Collectors.toList());
        if (matchedePerioder.size() == 1) {
            return Optional.of(matchedePerioder.get(0));
        }
        return Optional.empty();
    }

    public List<BigDecimal> finnArbeidsprosenterIPeriode(List<Gradering> graderingForAndelIPeriode, ÅpenDatoIntervallEntitet periode) {
        List<BigDecimal> prosentAndelerIPeriode = new ArrayList<>();
        if (graderingForAndelIPeriode.isEmpty()) {
            leggTilNullProsent(prosentAndelerIPeriode);
            return prosentAndelerIPeriode;
        }
        Gradering gradering = graderingForAndelIPeriode.get(0);
        prosentAndelerIPeriode.add(gradering.getArbeidstidProsent());
        if (graderingForAndelIPeriode.size() == 1) {
            if (graderingDekkerHeilePerioden(gradering, periode)) {
                return prosentAndelerIPeriode;
            }
            prosentAndelerIPeriode.add(BigDecimal.ZERO);
            return prosentAndelerIPeriode;
        }

        for (int i = 1; i < graderingForAndelIPeriode.size(); i++) {
            Gradering nesteGradering = graderingForAndelIPeriode.get(i);
            prosentAndelerIPeriode.add(nesteGradering.getArbeidstidProsent());
            if (!gradering.getPeriode().getFomDato().isBefore(periode.getTomDato())) {
                break;
            }
            if (gradering.getPeriode().getTomDato().isBefore(nesteGradering.getPeriode().getFomDato()) &&
                !gradering.getPeriode().getTomDato().equals(nesteGradering.getPeriode().getFomDato().minusDays(1))) {
                leggTilNullProsent(prosentAndelerIPeriode);
            }
            gradering = nesteGradering;
        }

        if (periode.getTomDato() == null || gradering.getPeriode().getTomDato().isBefore(periode.getTomDato())) {
            leggTilNullProsent(prosentAndelerIPeriode);
        }

        return prosentAndelerIPeriode;
    }

    private void leggTilNullProsent(List<BigDecimal> prosentAndelerIPeriode) {
        if (!prosentAndelerIPeriode.contains(BigDecimal.ZERO)) {
            prosentAndelerIPeriode.add(BigDecimal.ZERO);
        }
    }

    private boolean graderingDekkerHeilePerioden(Gradering gradering, ÅpenDatoIntervallEntitet periode) {
        return !gradering.getPeriode().getFomDato().isAfter(periode.getFomDato()) &&
            (gradering.getPeriode().getTomDato().equals(TIDENES_ENDE) ||
                (periode.getTomDato() != null &&
                    !gradering.getPeriode().getTomDato().isBefore(periode.getTomDato())));
    }

}
