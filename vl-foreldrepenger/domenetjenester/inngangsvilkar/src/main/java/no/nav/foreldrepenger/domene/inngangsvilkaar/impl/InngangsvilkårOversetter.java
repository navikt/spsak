package no.nav.foreldrepenger.domene.inngangsvilkaar.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Arbeidsgiverperiode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.Sykefravær;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.Sykemelding;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.Sykemeldinger;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.domene.inngangsvilkaar.PersonStatusType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårData;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.VilkårGrunnlag;
import no.nav.foreldrepenger.domene.medlem.api.MedlemskapPerioderTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class InngangsvilkårOversetter {

    private VilkårKodeverkRepository kodeverkRepository;
    private MedlemskapRepository medlemskapRepository;
    private MedlemskapPerioderTjeneste medlemskapPerioderTjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private SykefraværRepository sykefraværRepository;

    InngangsvilkårOversetter() {
        // for CDI proxy
    }

    @Inject
    public InngangsvilkårOversetter(BehandlingRepositoryProvider repositoryProvider,
                                    MedlemskapPerioderTjeneste medlemskapPerioderTjeneste,
                                    SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                    BasisPersonopplysningTjeneste personopplysningTjeneste) {
        this.kodeverkRepository = repositoryProvider.getVilkårKodeverkRepository();
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.sykefraværRepository = repositoryProvider.getSykefraværRepository();
        this.medlemskapPerioderTjeneste = medlemskapPerioderTjeneste;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.personopplysningTjeneste = personopplysningTjeneste;
    }

    public MedlemskapsvilkårGrunnlag oversettTilRegelModellMedlemskap(Behandling behandling) {
        PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentPersonopplysninger(behandling);

        Optional<MedlemskapAggregat> medlemskap = medlemskapRepository.hentMedlemskap(behandling);

        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskap.flatMap(MedlemskapAggregat::getVurdertMedlemskap);

        MedlemskapsvilkårGrunnlag grunnlag = new MedlemskapsvilkårGrunnlag(
            brukerErMedlemEllerIkkeRelevantPeriode(medlemskap, behandling, personopplysninger), // FP VK 2.13
            tilPersonStatusType(personopplysninger), // FP VK 2.1
            brukerNorskNordisk(personopplysninger), // FP VK 2.11
            brukerBorgerAvEOS(vurdertMedlemskap, personopplysninger), // FP VIK 2.12
            sjekkOmSaksbehandlerHarSattOophørtIMedlemskapPgaEndringerITps(vurdertMedlemskap));

        grunnlag.setHarSøkerArbeidsforholdOgInntekt(finnOmSøkerHarArbeidsforholdOgInntekt(behandling));

        // defaulter uavklarte fakta til true
        grunnlag.setBrukerAvklartLovligOppholdINorge(
            vurdertMedlemskap.map(VurdertMedlemskap::getLovligOppholdVurdering).orElse(true));
        grunnlag.setBrukerAvklartBosatt(
            vurdertMedlemskap.map(VurdertMedlemskap::getBosattVurdering).orElse(true));
        grunnlag.setBrukerAvklartOppholdsrett(
            vurdertMedlemskap.map(VurdertMedlemskap::getOppholdsrettVurdering).orElse(true));

        // FP VK 2.2 Er bruker avklart som pliktig eller frivillig medlem?
        grunnlag.setBrukerAvklartPliktigEllerFrivillig(erAvklartSomPliktigEllerFrivillingMedlem(behandling, medlemskap));

        return grunnlag;
    }

    private boolean finnOmSøkerHarArbeidsforholdOgInntekt(Behandling behandling) {
        LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOptional = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling,
            skjæringstidspunkt);

        if (inntektArbeidYtelseGrunnlagOptional.isPresent()) {
            InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseGrunnlagOptional.get();
            Optional<AktørArbeid> søkersArbeid = inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp(behandling.getAktørId());
            if (!søkersArbeid.isPresent()) {
                return false;
            }

            Optional<List<Arbeidsgiver>> arbeidsgivere = finnRelevanteArbeidsgivereMedLøpendeAvtaleEllerAvtaleSomErGyldigPåStp(skjæringstidspunkt,
                søkersArbeid.get());
            if (!arbeidsgivere.isPresent()) {
                return false;
            }

            Optional<AktørInntekt> aktørInntekt = inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp(behandling.getAktørId());
            if (!aktørInntekt.isPresent()) {
                return false;
            }

            return sjekkOmGjelderRelevantArbeidsgiver(aktørInntekt.get(), arbeidsgivere.get());
        }
        return false;
    }

    private Optional<List<Arbeidsgiver>> finnRelevanteArbeidsgivereMedLøpendeAvtaleEllerAvtaleSomErGyldigPåStp(LocalDate skjæringstidspunkt,
                                                                                                               AktørArbeid søkerArbeid) {
        List<Arbeidsgiver> relevanteArbeid = new ArrayList<>();
        for (Yrkesaktivitet yrkesaktivitet : søkerArbeid.getYrkesaktiviteter()) {
            if (yrkesaktivitet.erArbeidsforhold() && yrkesaktivitet.getAnsettelsesPeriode().isPresent()) {
                AktivitetsAvtale aktivitetsAvtale = yrkesaktivitet.getAnsettelsesPeriode().get();
                // Hvis har en løpende avtale fom før skjæringstidspunktet eller den som dekker skjæringstidspunktet
                if ((aktivitetsAvtale.getErLøpende() && aktivitetsAvtale.getFraOgMed().isBefore(skjæringstidspunkt))
                    || (aktivitetsAvtale.getFraOgMed().isBefore(skjæringstidspunkt) && aktivitetsAvtale.getTilOgMed().isAfter(skjæringstidspunkt))) {
                    relevanteArbeid.add(yrkesaktivitet.getArbeidsgiver());
                }
            }
        }
        return relevanteArbeid.isEmpty() ? Optional.empty() : Optional.of(relevanteArbeid);
    }

    private boolean sjekkOmGjelderRelevantArbeidsgiver(AktørInntekt inntekt, List<Arbeidsgiver> aktørArbeid) {
        return inntekt.getInntektPensjonsgivende().stream()
            .anyMatch(e -> aktørArbeid.contains(e.getArbeidsgiver()));
    }

    private boolean sjekkOmSaksbehandlerHarSattOophørtIMedlemskapPgaEndringerITps(Optional<VurdertMedlemskap> vurdertMedlemskap) {
        if (vurdertMedlemskap.isPresent()) {
            if (MedlemskapManuellVurderingType.SAKSBEHANDLER_SETTER_OPPHØR_AV_MEDL_PGA_ENDRINGER_I_TPS
                .equals(vurdertMedlemskap.get().getMedlemsperiodeManuellVurdering())) {
                return true;
            }
        }
        return false;
    }

    /**
     * True dersom saksbehandler har vurdert til å være medlem i relevant periode
     */
    private boolean erAvklartSomPliktigEllerFrivillingMedlem(Behandling behandling, Optional<MedlemskapAggregat> medlemskap) {
        if (medlemskap.isPresent()) {
            Optional<VurdertMedlemskap> vurdertMedlemskapOpt = medlemskap.get().getVurdertMedlemskap();
            if (vurdertMedlemskapOpt.isPresent()) {
                VurdertMedlemskap vurdertMedlemskap = vurdertMedlemskapOpt.get();
                if (vurdertMedlemskap.getMedlemsperiodeManuellVurdering() != null &&
                    MedlemskapManuellVurderingType.MEDLEM.equals(vurdertMedlemskap.getMedlemsperiodeManuellVurdering())) {
                    return true;
                }
                if (vurdertMedlemskap.getMedlemsperiodeManuellVurdering() != null &&
                    MedlemskapManuellVurderingType.IKKE_RELEVANT.equals(vurdertMedlemskap.getMedlemsperiodeManuellVurdering())) {
                    return false;
                }
            }
            return medlemskapPerioderTjeneste.brukerMaskineltAvklartSomFrivilligEllerPliktigMedlem(behandling,
                medlemskap.map(MedlemskapAggregat::getRegistrertMedlemskapPerioder).orElse(Collections.emptySet()));

        } else {
            return false;
        }
    }

    /**
     * True dersom saksbehandler har vurdert til ikke å være medlem i relevant periode
     */
    private boolean erAvklartSomIkkeMedlem(Optional<VurdertMedlemskap> medlemskap) {
        return medlemskap.isPresent() && medlemskap.get().getMedlemsperiodeManuellVurdering() != null
            && MedlemskapManuellVurderingType.UNNTAK.equals(medlemskap.get().getMedlemsperiodeManuellVurdering());
    }

    private boolean brukerErMedlemEllerIkkeRelevantPeriode(Optional<MedlemskapAggregat> medlemskap, Behandling behandling,
                                                           PersonopplysningerAggregat søker) {
        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskap.flatMap(MedlemskapAggregat::getVurdertMedlemskap);
        if (vurdertMedlemskap.isPresent()
            && MedlemskapManuellVurderingType.IKKE_RELEVANT.equals(vurdertMedlemskap.get().getMedlemsperiodeManuellVurdering())) {
            return true;
        }

        Set<RegistrertMedlemskapPerioder> medlemskapPerioder = medlemskap.isPresent() ? medlemskap.get().getRegistrertMedlemskapPerioder()
            : Collections.emptySet();
        boolean erAvklartMaskineltSomIkkeMedlem = medlemskapPerioderTjeneste.brukerMaskineltAvklartSomIkkeMedlem(behandling,
            søker, medlemskapPerioder);
        boolean erAvklartManueltSomIkkeMedlem = erAvklartSomIkkeMedlem(vurdertMedlemskap);

        return !(erAvklartMaskineltSomIkkeMedlem || erAvklartManueltSomIkkeMedlem);
    }

    private boolean brukerBorgerAvEOS(Optional<VurdertMedlemskap> medlemskap, PersonopplysningerAggregat aggregat) {
        // Tar det første for det er det som er prioritert høyest rangert på region
        final Optional<Statsborgerskap> statsborgerskap = aggregat.getStatsborgerskapFor(aggregat.getSøker().getAktørId()).stream().findFirst();
        return medlemskap
            .map(VurdertMedlemskap::getErEøsBorger)
            .orElse(Region.EOS.equals(statsborgerskap.map(Statsborgerskap::getRegion).orElse(null)));
    }

    private boolean brukerNorskNordisk(PersonopplysningerAggregat aggregat) {
        // Tar det første for det er det som er prioritert høyest rangert på region
        final Optional<Statsborgerskap> statsborgerskap = aggregat.getStatsborgerskapFor(aggregat.getSøker().getAktørId()).stream().findFirst();
        return Region.NORDEN.equals(statsborgerskap.map(Statsborgerskap::getRegion).orElse(null));
    }

    private PersonStatusType tilPersonStatusType(PersonopplysningerAggregat personopplysninger) {
        // Bruker overstyrt personstatus hvis det finnes
        PersonstatusType type = Optional.ofNullable(personopplysninger.getPersonstatusFor(personopplysninger.getSøker().getAktørId()))
            .map(Personstatus::getPersonstatus).orElse(null);

        if (PersonstatusType.BOSA.equals(type)) {
            return PersonStatusType.BOSA;
        } else if (PersonstatusType.UTVA.equals(type)) {
            return PersonStatusType.UTVA;
        } else if (PersonstatusType.DØD.equals(type)) {
            return PersonStatusType.DØD;
        }

        return null;
    }

    public OpptjeningsperiodeGrunnlag oversettTilRegelModellOpptjeningsperiode(Behandling behandling) {
        Optional<SykefraværGrunnlag> sykefraværGrunnlag = sykefraværRepository.hentHvisEksistererFor(behandling.getId());
        List<SykefraværPeriode> søknadsPerioder = sykefraværGrunnlag.map(SykefraværGrunnlag::getSykefravær)
            .map(Sykefravær::getPerioder)
            .orElse(Collections.emptyList());

        Optional<LocalDate> førsteDagSøknaden = søknadsPerioder.stream()
            .filter(ikkeEgenmeldingPeriode())
            .map(SykefraværPeriode::getPeriode)
            .map(DatoIntervallEntitet::getFomDato)
            .min(LocalDate::compareTo);

        Set<Sykemelding> sykemeldinger = sykefraværGrunnlag.map(SykefraværGrunnlag::getSykemeldinger)
            .map(Sykemeldinger::getSykemeldinger)
            .orElse(Collections.emptySet());

        Optional<LocalDate> førsteSykemeldingsdag = sykemeldinger.stream()
            .map(Sykemelding::getPeriode)
            .map(DatoIntervallEntitet::getFomDato)
            .min(LocalDate::compareTo);

        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling.getId(), null);
        List<Inntektsmelding> inntektsmeldinger = inntektArbeidYtelseGrunnlag.flatMap(InntektArbeidYtelseGrunnlag::getInntektsmeldinger)
            .map(InntektsmeldingAggregat::getInntektsmeldinger)
            .orElse(Collections.emptyList());

        Optional<LocalDate> førstedagIArbeidsgiverPerioden = inntektsmeldinger.stream()
            .map(Inntektsmelding::getArbeidsgiverperiode)
            .flatMap(List::stream)
            .map(Arbeidsgiverperiode::getPeriode)
            .map(DatoIntervallEntitet::getFomDato)
            .min(LocalDate::compareTo);

        return new OpptjeningsperiodeGrunnlag(førstedagIArbeidsgiverPerioden.orElse(null),
            førsteDagSøknaden.orElse(null),
            førsteSykemeldingsdag.orElse(null));
    }

    private Predicate<SykefraværPeriode> ikkeEgenmeldingPeriode() {
        return sfp -> !SykefraværPeriodeType.EGENMELDING.equals(sfp.getType());
    }

    public VilkårData tilVilkårData(VilkårType vilkårType, Evaluation evaluation, VilkårGrunnlag grunnlag) {
        return new VilkårUtfallOversetter(kodeverkRepository).oversett(vilkårType, evaluation, grunnlag);
    }
}
