package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType.erFarEllerMedmor;
import static no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.UttakPeriodeEndringDto.TypeEndring.AVKLART;
import static no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.UttakPeriodeEndringDto.TypeEndring.ENDRET;
import static no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.UttakPeriodeEndringDto.TypeEndring.LAGT_TIL;
import static no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.UttakPeriodeEndringDto.TypeEndring.SLETTET;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaData;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaPeriode;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaUttakTjeneste;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaUttakUtledereTjeneste;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.UttakPeriodeEndringDto;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.wagnerfisher.EditDistanceOperasjon;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.wagnerfisher.WagnerFisher;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class KontrollerFaktaUttakTjenesteImpl implements KontrollerFaktaUttakTjeneste {

    private KontrollerFaktaUttakUtledereTjeneste aksjonspunktUtlederTjeneste;

    private YtelsesFordelingRepository ytelsesFordelingRepository;

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;

    private FamilieHendelseRepository familieGrunnlagRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    KontrollerFaktaUttakTjenesteImpl() {
        // For CDI
    }

    @Inject
    public KontrollerFaktaUttakTjenesteImpl(@FagsakYtelseTypeRef("FP") KontrollerFaktaUttakUtledereTjeneste uttakUtledereTjeneste,
                                            BehandlingRepositoryProvider behandlingRepositoryProvider,
                                            SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                            BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.aksjonspunktUtlederTjeneste = uttakUtledereTjeneste;
        this.ytelsesFordelingRepository = behandlingRepositoryProvider.getYtelsesFordelingRepository();
        this.inntektArbeidYtelseRepository = behandlingRepositoryProvider.getInntektArbeidYtelseRepository();
        this.familieGrunnlagRepository = behandlingRepositoryProvider.getFamilieGrunnlagRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkter(Behandling behandling) {
        final List<AksjonspunktUtleder> aksjonspunktUtleders = aksjonspunktUtlederTjeneste.utledUtledereFor(behandling);
        List<AksjonspunktResultat> aksjonspunktResultater = new ArrayList<>();
        for (AksjonspunktUtleder aksjonspunktUtleder : aksjonspunktUtleders) {
            aksjonspunktResultater.addAll(aksjonspunktUtleder.utledAksjonspunkterFor(behandling));
        }
        return aksjonspunktResultater.stream()
            .distinct() // Unngå samme aksjonspunkt flere multipliser
            .collect(toList());
    }

    @Override
    public KontrollerFaktaData hentKontrollerFaktaPerioder(Behandling behandling) {
        Optional<YtelseFordelingAggregat> ytelseFordeling = ytelsesFordelingRepository.hentAggregatHvisEksisterer(behandling);
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling,
            skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));

        //Må sjekke etter dette pga at frontend kan kalle på denne tjenesten før grunnlaget er bygget.
        if (!harGrunnlagTilÅKontrollere(behandling, ytelseFordeling.orElse(null), grunnlag.orElse(null))) {
            return utenGrunnlagResultat(ytelseFordeling);
        }

        List<Inntektsmelding> innteksmeldinger = hentInntektsmeldinger(behandling);
        LocalDate fødselsDatoTilTidligOppstart = utledOmSøkerTidligOppstart(behandling);
        boolean erArbeidstaker = true;
        return SøknadsperiodeDokumentasjonKontrollerer.kontrollerPerioder(ytelseFordeling.get(), innteksmeldinger, fødselsDatoTilTidligOppstart, erArbeidstaker);
    }

    private KontrollerFaktaData utenGrunnlagResultat(Optional<YtelseFordelingAggregat> ytelseFordeling) {
        final List<KontrollerFaktaPeriode> perioder;
        if (ytelseFordeling.isPresent()) {
            perioder = fraSøknadsperioder(ytelseFordeling.get().getGjeldendeSøknadsperioder());
        } else {
            perioder = Collections.emptyList();
        }
        return new KontrollerFaktaData(perioder);
    }

    private List<KontrollerFaktaPeriode> fraSøknadsperioder(OppgittFordeling gjeldendeSøknadsperioder) {
        return gjeldendeSøknadsperioder.getOppgittePerioder()
            .stream()
            .map(KontrollerFaktaPeriode::automatiskBekreftet)
            .collect(Collectors.toList());
    }

    private boolean harGrunnlagTilÅKontrollere(Behandling behandling, YtelseFordelingAggregat ytelseFordelingAggregat, InntektArbeidYtelseGrunnlag grunnlag) {
        Optional<Beregningsgrunnlag> beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        return ytelseFordelingAggregat != null && grunnlag != null && grunnlag.getOpplysningerFørSkjæringstidspunkt().isPresent() &&
            beregningsgrunnlag.isPresent();
    }

    private LocalDate utledOmSøkerTidligOppstart(Behandling behandling) {
        FamilieHendelse familieHendelse = familieGrunnlagRepository.hentAggregat(behandling).getGjeldendeVersjon();
        if ((erFarEllerMedmor(behandling.getRelasjonsRolleType())) && familieHendelse.getGjelderFødsel()) {
            return familieHendelse.getSkjæringstidspunkt();
        }
        return null;
    }

    @Override
    public boolean finnesOverlappendePerioder(Behandling behandling) {
        Optional<YtelseFordelingAggregat> ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregatHvisEksisterer(behandling);
        if (ytelseFordelingAggregat.isPresent()) {
            YtelseFordelingAggregat ytelseFordeling = ytelseFordelingAggregat.get();
            List<OppgittPeriode> perioder = ytelseFordeling.getGjeldendeSøknadsperioder().getOppgittePerioder()
                .stream()
                .sorted(Comparator.comparing(OppgittPeriode::getFom))
                .collect(Collectors.toList());

            for (int i = 0; i < perioder.size() - 1; i++) {
                OppgittPeriode oppgittPeriode = perioder.get(i);
                OppgittPeriode nestePeriode = perioder.get(i + 1);
                if (!nestePeriode.getFom().isAfter(oppgittPeriode.getTom())) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<UttakPeriodeEndringDto> finnEndringMellomOppgittOgGjeldendePerioder(YtelseFordelingAggregat ytelseFordelingAggregat) {
        List<UttakPeriodeEditDistance> oppgittePerioder = ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder()
            .stream()
            .map(UttakPeriodeEditDistance::new)
            .sorted(Comparator.comparing(p -> p.getPeriode().getFom()))
            .collect(Collectors.toList());

        List<UttakPeriodeEditDistance> gjeldendePerioder = ytelseFordelingAggregat.getGjeldendeSøknadsperioder().getOppgittePerioder()
            .stream()
            .map(this::mapPeriode)
            .sorted(Comparator.comparing(u -> u.getPeriode().getFom()))
            .collect(Collectors.toList());

        List<EditDistanceOperasjon<UttakPeriodeEditDistance>> operasjoner = WagnerFisher.finnEnklesteSekvens(oppgittePerioder, gjeldendePerioder);

        return operasjoner.stream()
            .map(this::mapFra)
            .sorted(Comparator.comparing(UttakPeriodeEndringDto::getFom))
            .collect(Collectors.toList());
    }

    @Override
    public List<UttakPeriodeEndringDto> finnEndringMellomOppgittOgGjeldendePerioder(Long aggregatId) {
        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentYtelsesFordelingPåId(aggregatId);
        return finnEndringMellomOppgittOgGjeldendePerioder(ytelseFordelingAggregat);
    }


    @Override
    public List<UttakPeriodeEndringDto> finnEndringMellomOppgittOgGjeldendePerioder(Behandling behandling) {
        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(behandling);
        return finnEndringMellomOppgittOgGjeldendePerioder(ytelseFordelingAggregat);
    }

    private UttakPeriodeEndringDto mapFra(EditDistanceOperasjon<UttakPeriodeEditDistance> operasjon) {
        final UttakPeriodeEndringDto.TypeEndring typeEndring;
        final OppgittPeriode periode;

        if (operasjon.erSletteOperasjon()) {
            periode = operasjon.getFør().getPeriode();
            typeEndring = SLETTET;
        } else if (operasjon.erSettInnOperasjon()) {
            periode = operasjon.getNå().getPeriode();
            typeEndring = LAGT_TIL;
        } else {
            periode = operasjon.getNå().getPeriode();
            typeEndring = operasjon.getNå().isPeriodeDokumentert() == null ? ENDRET : AVKLART;
        }

        return new UttakPeriodeEndringDto.Builder()
            .medPeriode(periode.getFom(), periode.getTom())
            .medTypeEndring(typeEndring)
            .build();
    }

    UttakPeriodeEditDistance mapPeriode(OppgittPeriode periode) {
        if (harKrevdAvklaringFraSaksbehandler(periode)) {
            return UttakPeriodeEditDistance.builder(periode)
                .medPeriodeErDokumentert(erDokumentert(periode))
                .build();
        }
        return new UttakPeriodeEditDistance(periode);
    }

    private boolean harKrevdAvklaringFraSaksbehandler(OppgittPeriode periode) {
        return periode.getBegrunnelse().isPresent();
    }

    private boolean erDokumentert(OppgittPeriode periode) {
        return Objects.equals(periode.getPeriodeVurderingType(), UttakPeriodeVurderingType.PERIODE_OK) ||
            Objects.equals(periode.getPeriodeVurderingType(), UttakPeriodeVurderingType.PERIODE_OK_ENDRET);
    }

    private List<Inntektsmelding> hentInntektsmeldinger(Behandling behandling) {
        return inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null)
            .flatMap(InntektArbeidYtelseGrunnlag::getInntektsmeldinger)
            .map(InntektsmeldingAggregat::getInntektsmeldinger)
            .orElse(Collections.emptyList()); //
    }
}
