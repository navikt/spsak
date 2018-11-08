package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import static java.util.Collections.emptyList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;

@Dependent
class StartpunktUtlederInntektsmelding {
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private FørstePermisjonsdagTjeneste førstePermisjonsdagTjeneste;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;

    StartpunktUtlederInntektsmelding() {
        // For CDI
    }

    @Inject
    StartpunktUtlederInntektsmelding(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                     FørstePermisjonsdagTjeneste førstePermisjonsdagTjeneste,
                                     BeregningsresultatFPRepository beregningsresultatFPRepository) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.førstePermisjonsdagTjeneste = førstePermisjonsdagTjeneste;
        this.beregningsresultatFPRepository = beregningsresultatFPRepository;
    }

    public StartpunktType utledStartpunkt(Behandling behandling, InntektArbeidYtelseGrunnlag grunnlag1, InntektArbeidYtelseGrunnlag grunnlag2) {
        if (behandling.getType().equals(BehandlingType.FØRSTEGANGSSØKNAD)) {
            return StartpunktType.SØKERS_RELASJON_TIL_BARNET;
        }
        List<Inntektsmelding> origIm = hentInntektsmeldingerFraGrunnlag(behandling, grunnlag1, grunnlag2);
        List<Inntektsmelding> nyeIm = inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(behandling);

        Map<ArbeidforholdNøkkel, Inntektsmelding> origImMap = indekserImMotArbeidsforhold(origIm);
        Map<ArbeidforholdNøkkel, Inntektsmelding> nyeImMap = indekserImMotArbeidsforhold(nyeIm);

        StartpunktType startpunkt = nyeImMap.entrySet().stream()
            .map(nyIm -> finnStartpunktForNyIm(behandling, nyIm, origImMap))
            .min(Comparator.comparingInt(StartpunktType::getRangering))
            .orElse(StartpunktType.UDEFINERT);
        return startpunkt;
    }

    private List<Inntektsmelding> hentInntektsmeldingerFraGrunnlag(Behandling behandling, InntektArbeidYtelseGrunnlag grunnlag1, InntektArbeidYtelseGrunnlag grunnlag2) {
        Optional<InntektArbeidYtelseGrunnlag> origIayGrunnlag = finnIayGrunnlagForOrigBehandling(behandling, grunnlag1, grunnlag2);
        return origIayGrunnlag.map(it -> it.getInntektsmeldinger())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(InntektsmeldingAggregat::getInntektsmeldinger)
            .orElse(emptyList());
    }

    private StartpunktType finnStartpunktForNyIm(Behandling behandling, Map.Entry<ArbeidforholdNøkkel, Inntektsmelding> nyImEntry, Map<ArbeidforholdNøkkel, Inntektsmelding> origImMap) {
        if (erStartpunktForNyImInngangsvilkår(behandling, nyImEntry.getValue())) {//NOSONAR utrykket evaluerer ikke alltid til true
            return StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP;
        }
        if (erStartpunktForNyImBeregning(nyImEntry, origImMap, behandling)) {
            return StartpunktType.BEREGNING;
        }
        return StartpunktType.UTTAKSVILKÅR;
    }

    private boolean erStartpunktForNyImBeregning(Map.Entry<ArbeidforholdNøkkel, Inntektsmelding> nyImEntry, Map<ArbeidforholdNøkkel, Inntektsmelding> origImMap, Behandling behandling) {
        Inntektsmelding nyIm = nyImEntry.getValue();
        if (nyIm.getInntektsmeldingInnsendingsårsak().equals(InntektsmeldingInnsendingsårsak.NY)) {
            return true;
        }
        Inntektsmelding origIM = origImMap.get(nyImEntry.getKey());
        if (origIM == null) {
            // Dersom IM ikke fins originalt, så regnes også i dette tilfelle ny IM
            return true;
        }

        List<NaturalYtelse> nyeNaturalYtelser = nyIm.getNaturalYtelser();
        List<NaturalYtelse> origNaturalYtelser = origIM.getNaturalYtelser();

        return nyIm.getInntektBeløp().getVerdi().compareTo(origIM.getInntektBeløp().getVerdi()) != 0
            || erEndringPåNaturalYtelser(nyeNaturalYtelser, origNaturalYtelser)
            || erEndringPåRefusjon(nyIm, origIM)
            || erGraderingPåAktivitetUtenDagsats(nyIm, behandling);
    }


    private Optional<InntektArbeidYtelseGrunnlag> finnIayGrunnlagForOrigBehandling(Behandling behandling, InntektArbeidYtelseGrunnlag grunnlag1, InntektArbeidYtelseGrunnlag grunnlag2) {
        InntektArbeidYtelseGrunnlag gjeldendeGrunnlag = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling).orElse(null);
        if (gjeldendeGrunnlag == null) {
            return Optional.empty();
        }

        if (Objects.equals(gjeldendeGrunnlag, grunnlag1)) {
            return Optional.of(grunnlag2);
        }
        if (Objects.equals(gjeldendeGrunnlag, grunnlag2)) {
            return Optional.of(grunnlag1);
        }
        return Optional.empty();

    }

    private Map<ArbeidforholdNøkkel, Inntektsmelding> indekserImMotArbeidsforhold(List<Inntektsmelding> origIM) {
        return origIM.stream()
            .collect(Collectors.toMap(ArbeidforholdNøkkel::new, im -> im));
    }

    private boolean erStartpunktForNyImInngangsvilkår(Behandling behandling, Inntektsmelding nyIm) {
        Behandling originalBehandling = behandling.getOriginalBehandling().orElse(behandling);//Har ikke revurdering. Så vi jobber med orginalbehandling
        if (!førstePermisjonsdagTjeneste.henteFørstePermisjonsdag(originalBehandling).isPresent()) {
            return true;
        }
        return !førstePermisjonsdagTjeneste.henteFørstePermisjonsdag(originalBehandling).get().equals(nyIm.getStartDatoPermisjon());//NOSONAR optional er testet på over
    }

    private boolean erEndringPåNaturalYtelser(List<NaturalYtelse> nyA, List<NaturalYtelse> nyB) {
        return (nyA.size() != nyB.size()
            || !nyA.containsAll(nyB));
    }

    private boolean erEndringPåRefusjon(Inntektsmelding imA, Inntektsmelding imB) {
        return (!Objects.equals(imA.getRefusjonBeløpPerMnd(), imB.getRefusjonBeløpPerMnd())
            || !Objects.equals(imA.getRefusjonOpphører(), imB.getRefusjonOpphører()));

    }

    private boolean erGraderingPåAktivitetUtenDagsats(Inntektsmelding nyIm, Behandling behandling) {
        if(nyIm.getGraderinger().isEmpty()){
            return false;
        }
        Behandling orgigBehandling = behandling.getOriginalBehandling().orElse(null);
        if (orgigBehandling == null) {
            return false;
        }

        if (orgigBehandling.getBehandlingsresultat().isBehandlingsresultatAvslåttOrOpphørt()) {
            return false;
        }

        Optional<BeregningsresultatFP> origBeregningsresultatFP = beregningsresultatFPRepository.hentBeregningsresultatFP(orgigBehandling);

        if (!origBeregningsresultatFP.isPresent()) {
            return false;
        }

        return StartpunktutlederHjelper.finnesAktivitetHvorAlleHarDagsatsNull(origBeregningsresultatFP.get());
    }

    private static class ArbeidforholdNøkkel {
        private final Virksomhet virksomhet;
        private final ArbeidsforholdRef arbeidsforholdRef;

        ArbeidforholdNøkkel(Inntektsmelding inntektsmelding) {
            this.virksomhet = inntektsmelding.getVirksomhet();
            this.arbeidsforholdRef = inntektsmelding.getArbeidsforholdRef();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ArbeidforholdNøkkel)){
                return false;
            }
            ArbeidforholdNøkkel that = (ArbeidforholdNøkkel) o;

            return Objects.equals(virksomhet, that.virksomhet)
                && Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef);
        }

        @Override
        public int hashCode() {
            return Objects.hash(virksomhet, arbeidsforholdRef);
        }
    }
}


