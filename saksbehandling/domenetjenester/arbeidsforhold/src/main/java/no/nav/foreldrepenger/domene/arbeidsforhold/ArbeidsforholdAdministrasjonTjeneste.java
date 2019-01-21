package no.nav.foreldrepenger.domene.arbeidsforhold;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType.BRUK;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType.NYTT_ARBEIDSFORHOLD;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdKilde.INNTEKTSKOMPONENTEN;
import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørArbeidEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregatEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdOverstyringEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdWrapper;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntektspost;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class ArbeidsforholdAdministrasjonTjeneste {

    // Arbeidtyper som kommer fra AA-reg
    private static final Set<ArbeidType> AA_REG_TYPER = Set.of(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, ArbeidType.MARITIMT_ARBEIDSFORHOLD, ArbeidType.FORENKLET_OPPGJØRSORDNING);

    private InntektArbeidYtelseRepository iayRepository;
    private VurderArbeidsforholdTjeneste vurderArbeidsforholdTjeneste;
    private TpsTjeneste tpsTjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    ArbeidsforholdAdministrasjonTjeneste() {
    }

    @Inject
    public ArbeidsforholdAdministrasjonTjeneste(GrunnlagRepositoryProvider repositoryProvider,
                                                VurderArbeidsforholdTjeneste vurderArbeidsforholdTjeneste,
                                                TpsTjeneste tpsTjeneste,
                                                SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.iayRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.vurderArbeidsforholdTjeneste = vurderArbeidsforholdTjeneste;
        this.tpsTjeneste = tpsTjeneste;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    /**
     * Oppretter en builder for å lagre ned overstyringen av arbeidsforhold
     *
     * @param behandling behandlingen
     * @return buildern
     */
    public ArbeidsforholdInformasjonBuilder opprettBuilderFor(Behandling behandling) {
        return iayRepository.opprettInformasjonBuilderFor(behandling);
    }

    /**
     * Rydder opp i inntektsmeldinger som blir erstattet
     *
     * @param behandling behandlingen
     * @param builder    ArbeidsforholdsOverstyringene som skal lagrers
     */
    public void lagre(Behandling behandling, ArbeidsforholdInformasjonBuilder builder) {
        iayRepository.lagre(behandling, builder);
    }

    public Set<ArbeidsforholdWrapper> hentArbeidsforholdFerdigUtledet(Behandling behandling) {
        List<ArbeidsforholdOverstyringEntitet> overstyringer = iayRepository.hentArbeidsforholdInformasjon(behandling)
            .map(ArbeidsforholdInformasjon::getOverstyringer).orElse(emptyList());
        LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
        Optional<InntektArbeidYtelseGrunnlag> grunnlagOptional = iayRepository.hentAggregatHvisEksisterer(behandling, skjæringstidspunkt);
        Optional<InntektsmeldingAggregat> inntektsmeldingAggregat = grunnlagOptional.flatMap(InntektArbeidYtelseGrunnlag::getInntektsmeldinger);

        Collection<Yrkesaktivitet> yaFør = grunnlagOptional.flatMap(g -> g.getAktørArbeidFørStp(behandling.getAktørId()))
            .map(aa -> ((AktørArbeidEntitet) aa).hentAlleYrkesaktiviter())
            .orElse(emptyList());
        Collection<Yrkesaktivitet> yaEtter = grunnlagOptional.flatMap(g -> g.getAktørArbeidEtterStp(behandling.getAktørId()))
            .map(aa -> ((AktørArbeidEntitet) aa).hentAlleYrkesaktiviter())
            .orElse(emptyList());

        final Map<Arbeidsgiver, Set<ArbeidsforholdRef>> arbeidsgiverSetMap = vurderArbeidsforholdTjeneste.endringerIInntektsmelding(behandling);
        final List<Inntektsmelding> inntektsmeldinger = inntektsmeldingAggregat.map(InntektsmeldingAggregat::getInntektsmeldinger).orElse(emptyList());
        Set<ArbeidsforholdWrapper> arbeidsforhold = new LinkedHashSet<>(
            utledArbeidsforholdFraInntektsmeldinger(inntektsmeldinger, yaFør, yaEtter, overstyringer, arbeidsgiverSetMap, skjæringstidspunkt));
        final Optional<AktørArbeid> aktørArbeid = grunnlagOptional.flatMap(g -> g.getAktørArbeidFørStp(behandling.getAktørId()));
        aktørArbeid.ifPresent(aa -> arbeidsforhold.addAll(utledArbeidsforholdFraYrkesaktivitet(aa.getYrkesaktiviteter(), overstyringer, inntektsmeldinger, skjæringstidspunkt)));

        final Optional<AktørArbeid> aktørArbeidEtter = grunnlagOptional.flatMap(g -> g.getAktørArbeidEtterStp(behandling.getAktørId()));
        aktørArbeidEtter.ifPresent(aa -> arbeidsforhold.addAll(utledArbeidsforholdFraYrkesaktivitet(aa.getYrkesaktiviteter(), overstyringer, inntektsmeldinger, skjæringstidspunkt)));
        final List<Inntektsmelding> alleInntektsmeldinger = inntektsmeldingAggregat.map(it -> ((InntektsmeldingAggregatEntitet) it).getAlleInntektsmeldinger()).orElse(emptyList());
        arbeidsforhold.addAll(utledArbeidsforholdFraArbeidsforholdInformasjon(overstyringer, yaFør, yaEtter, alleInntektsmeldinger, skjæringstidspunkt));
        arbeidsforhold.addAll(utledArbeidsforholdFraInntekt(behandling, grunnlagOptional, arbeidsforhold, overstyringer));

        if (behandling.harÅpentAksjonspunktMedType(AksjonspunktDefinisjon.VURDER_ARBEIDSFORHOLD)) {
            final Map<Arbeidsgiver, Set<ArbeidsforholdRef>> vurder = vurderArbeidsforholdTjeneste.vurder(behandling);
            for (ArbeidsforholdWrapper arbeidsforholdWrapper : arbeidsforhold) {
                for (Map.Entry<Arbeidsgiver, Set<ArbeidsforholdRef>> arbeidsgiverSetEntry : vurder.entrySet()) {
                    if (erAksjonspunktPå(arbeidsforholdWrapper, arbeidsgiverSetEntry)) {
                        arbeidsforholdWrapper.setHarAksjonspunkt(true);
                        arbeidsforholdWrapper.setBrukArbeidsforholdet(null);
                        arbeidsforholdWrapper.setFortsettBehandlingUtenInntektsmelding(null);
                    }
                }
            }
        }

        return arbeidsforhold;
    }

    private boolean erAksjonspunktPå(ArbeidsforholdWrapper arbeidsforholdWrapper, Map.Entry<Arbeidsgiver, Set<ArbeidsforholdRef>> entry) {
        if (arbeidsforholdWrapper.getKilde() == INNTEKTSKOMPONENTEN) {
            return entry.getKey().getIdentifikator().equals(arbeidsforholdWrapper.getArbeidsgiverIdentifikator());
        }
        return entry.getKey().getIdentifikator().equals(arbeidsforholdWrapper.getArbeidsgiverIdentifikator())
            && entry.getValue().contains(ArbeidsforholdRef.ref(arbeidsforholdWrapper.getArbeidsforholdId()));
    }

    private List<ArbeidsforholdWrapper> utledArbeidsforholdFraInntektsmeldinger(List<Inntektsmelding> inntektsmeldinger,
                                                                                Collection<Yrkesaktivitet> yaFør, Collection<Yrkesaktivitet> yaEtter,
                                                                                List<ArbeidsforholdOverstyringEntitet> overstyringer,
                                                                                Map<Arbeidsgiver, Set<ArbeidsforholdRef>> arbeidsgiverSetMap, LocalDate skjæringstidspunkt) {
        return inntektsmeldinger.stream().map(i -> {
            ArbeidsforholdWrapper wrapper = new ArbeidsforholdWrapper();
            wrapper.setNavn(i.getVirksomhet().getNavn());
            wrapper.setMottattDatoInntektsmelding(i.getInnsendingstidspunkt().toLocalDate());
            ArbeidsforholdRef arbeidsforholdRef = i.getArbeidsforholdRef();
            if (arbeidsforholdRef != null) {
                wrapper.setArbeidsforholdId(arbeidsforholdRef.getReferanse());
            }
            final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(i.getVirksomhet());
            List<Yrkesaktivitet> yrkesaktiviteter = finnYrkesaktivitet(yaFør, arbeidsgiver, i.getArbeidsforholdRef());
            if (yrkesaktiviteter.isEmpty()) {
                yrkesaktiviteter = finnYrkesaktivitet(yaEtter, arbeidsgiver, i.getArbeidsforholdRef());
            }
            DatoIntervallEntitet avtale = setAvtalePeriode(wrapper, yrkesaktiviteter);
            wrapper.setKilde(avtale != null ? ArbeidsforholdKilde.AAREGISTERET : ArbeidsforholdKilde.INNTEKTSMELDING);
            // setter disse
            wrapper.setBrukArbeidsforholdet(true);
            final Boolean erNyttArbeidsforhold = erNyttArbeidsforhold(overstyringer,
                arbeidsgiver, i.getArbeidsforholdRef());
            wrapper.setErNyttArbeidsforhold(erNyttArbeidsforhold);
            wrapper.setFortsettBehandlingUtenInntektsmelding(false);
            wrapper.setIkkeRegistrertIAaRegister(avtale == null);
            wrapper.setVurderOmSkalErstattes(erNyttOgIkkeTattStillingTil(i.getVirksomhet(), i.getArbeidsforholdRef(), arbeidsgiverSetMap));
            wrapper.setStillingsprosent(finnStillingsprosent(yrkesaktiviteter, skjæringstidspunkt));
            wrapper.setHarErsattetEttEllerFlere(!i.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold());
            wrapper.setErstatterArbeidsforhold(harErstattetEtEllerFlereArbeidsforhold(arbeidsgiver, i.getArbeidsforholdRef(), overstyringer));
            wrapper.setErEndret(sjekkOmFinnesIOverstyr(overstyringer, i.getVirksomhet().getOrgnr(), i.getArbeidsforholdRef()));

            return wrapper;
        }).collect(Collectors.toList());
    }

    private Boolean sjekkOmFinnesIOverstyr(List<ArbeidsforholdOverstyringEntitet> overstyringer, String orgnr, ArbeidsforholdRef arbeidsforholdRef) {
        for (ArbeidsforholdOverstyringEntitet entitet : overstyringer) {
            if (entitet.getArbeidsgiver().getIdentifikator().equals(orgnr) &&
                entitet.getArbeidsforholdRef().equals(arbeidsforholdRef)) {
                return true;
            }
        }
        return false;
    }

    private boolean erNyttOgIkkeTattStillingTil(Virksomhet virksomhet, ArbeidsforholdRef arbeidsforholdRef, Map<Arbeidsgiver, Set<ArbeidsforholdRef>> arbeidsgiverSetMap) {
        return arbeidsgiverSetMap.getOrDefault(Arbeidsgiver.virksomhet(virksomhet), Collections.emptySet()).contains(arbeidsforholdRef);
    }

    private Boolean erNyttArbeidsforhold(List<ArbeidsforholdOverstyringEntitet> overstyringer, Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef) {
        return overstyringer.stream().anyMatch(ov -> ov.getHandling().equals(NYTT_ARBEIDSFORHOLD) && ov.getArbeidsgiver().equals(arbeidsgiver)
            && ov.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef));
    }

    private BigDecimal finnStillingsprosent(List<Yrkesaktivitet> yrkesaktiviteter, LocalDate skjæringstidspunkt) {
        if (yrkesaktiviteter.isEmpty()) {
            return BigDecimal.ZERO;
        }
        DatoIntervallEntitet stp = DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunkt, skjæringstidspunkt);
        List<Yrkesaktivitet> relevanteYA = yrkesaktiviteter.stream()
            .filter(ya ->
            {
                DatoIntervallEntitet datoIntervallEntitet = ya.getAnsettelsesPeriode().map(AktivitetsAvtale::getPeriode).orElse(DatoIntervallEntitet.fraOgMed(TIDENES_ENDE));

                return datoIntervallEntitet
                    .overlapper(stp);
            })
            .collect(Collectors.toList());

        return relevanteYA.stream().map(Yrkesaktivitet::getAktivitetsAvtaler)
            .flatMap(Collection::stream)
            .filter(aa -> aa.getPeriode().overlapper(stp))
            .map(AktivitetsAvtale::getProsentsats)
            .map(Stillingsprosent::getVerdi)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Yrkesaktivitet> finnYrkesaktivitet(Collection<Yrkesaktivitet> yrkesaktiviteter, Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef) {
        return yrkesaktiviteter
            .stream()
            .filter(yr -> (Objects.equals(yr.getArbeidsgiver(), arbeidsgiver)
                && yr.getArbeidsforholdRef().orElse(ArbeidsforholdRef.ref(null)).gjelderFor(arbeidsforholdRef)))
            .collect(Collectors.toList());
    }

    private List<ArbeidsforholdWrapper> utledArbeidsforholdFraArbeidsforholdInformasjon(List<ArbeidsforholdOverstyringEntitet> overstyringer,
                                                                                        Collection<Yrkesaktivitet> yaFør, Collection<Yrkesaktivitet> yaEtter, List<Inntektsmelding> alleInntektsmeldinger, LocalDate skjæringstidspunkt) {
        return overstyringer
            .stream()
            .filter(a -> !a.getHandling().equals(BRUK))
            .map(a -> {
                ArbeidsforholdWrapper wrapper = new ArbeidsforholdWrapper();
                List<Yrkesaktivitet> yrkesaktiviteter = finnYrkesaktivitet(yaFør, a.getArbeidsgiver(), a.getArbeidsforholdRef());
                if (yrkesaktiviteter.isEmpty()) {
                    yrkesaktiviteter = finnYrkesaktivitet(yaEtter, a.getArbeidsgiver(), a.getArbeidsforholdRef());
                }
                DatoIntervallEntitet avtale = setAvtalePeriode(wrapper, yrkesaktiviteter);
                wrapper.setIkkeRegistrertIAaRegister(avtale == null);
                mapArbeidsgiver(wrapper, a.getArbeidsgiver());
                if (a.getHandling().equals(ArbeidsforholdHandlingType.IKKE_BRUK)) {
                    wrapper.setBrukArbeidsforholdet(false);
                    wrapper.setFortsettBehandlingUtenInntektsmelding(false);
                } else if (a.getHandling().equals(SLÅTT_SAMMEN_MED_ANNET)) {
                    wrapper.setErSlettet(true);
                } else {
                    wrapper.setFortsettBehandlingUtenInntektsmelding(true);
                    wrapper.setBrukArbeidsforholdet(true);
                }
                final LocalDate mottattDatoInntektsmelding = mottattInntektsmelding(a, alleInntektsmeldinger);
                wrapper.setArbeidsforholdId(a.getArbeidsforholdRef().getReferanse());
                wrapper.setBeskrivelse(a.getBegrunnelse());
                wrapper.setKilde(utledKilde(avtale, mottattDatoInntektsmelding, a.getHandling()));
                wrapper.setStillingsprosent(finnStillingsprosent(yrkesaktiviteter, skjæringstidspunkt));
                wrapper.setMottattDatoInntektsmelding(mottattDatoInntektsmelding);
                wrapper.setErEndret(true);
                return wrapper;
            }).collect(Collectors.toList());
    }

    private DatoIntervallEntitet setAvtalePeriode(ArbeidsforholdWrapper wrapper, List<Yrkesaktivitet> yrkesaktiviteter) {
        DatoIntervallEntitet avtale = finnAnsettelsesPeriode(yrkesaktiviteter);
        wrapper.setFomDato(avtale != null ? avtale.getFomDato() : null);
        wrapper.setTomDato(avtale != null ? avtale.getTomDato() : null);
        return avtale;
    }

    private DatoIntervallEntitet finnAnsettelsesPeriode(List<Yrkesaktivitet> yrkesaktiviteter) {
        LocalDate min = yrkesaktiviteter.stream()
            .map(Yrkesaktivitet::getAnsettelsesPeriode)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(AktivitetsAvtale::getFraOgMed)
            .min(Comparator.naturalOrder())
            .orElse(Tid.TIDENES_BEGYNNELSE);
        LocalDate max = yrkesaktiviteter.stream()
            .map(Yrkesaktivitet::getAnsettelsesPeriode)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(AktivitetsAvtale::getTilOgMed)
            .max(Comparator.naturalOrder())
            .orElse(TIDENES_ENDE);
        if (min.equals(Tid.TIDENES_BEGYNNELSE) && max.equals(TIDENES_ENDE)) {
            return null;
        }
        return DatoIntervallEntitet.fraOgMedTilOgMed(min, max);
    }

    private ArbeidsforholdKilde utledKilde(DatoIntervallEntitet avtale, LocalDate mottattDatoInntektsmelding, ArbeidsforholdHandlingType handling) {
        if (avtale != null) {
            return ArbeidsforholdKilde.AAREGISTERET;
        }
        if (mottattDatoInntektsmelding != null || handling.equals(SLÅTT_SAMMEN_MED_ANNET)) {
            return ArbeidsforholdKilde.INNTEKTSMELDING;
        }
        return INNTEKTSKOMPONENTEN;
    }

    private LocalDate mottattInntektsmelding(ArbeidsforholdOverstyringEntitet a, List<Inntektsmelding> alleInntektsmeldinger) {
        final Optional<LocalDate> mottattTidspunkt = alleInntektsmeldinger.stream().filter(im -> a.getArbeidsgiver().getErVirksomhet()
            && a.getArbeidsgiver().getVirksomhet().equals(im.getVirksomhet())
            && a.getArbeidsforholdRef().gjelderFor(im.getArbeidsforholdRef())).findFirst()
            .map(Inntektsmelding::getInnsendingstidspunkt).map(LocalDateTime::toLocalDate);
        return mottattTidspunkt.orElse(null);
    }

    private List<ArbeidsforholdWrapper> utledArbeidsforholdFraInntekt(Behandling behandling, Optional<InntektArbeidYtelseGrunnlag> grunnlagOptional,
                                                                      Set<ArbeidsforholdWrapper> arbeidsforhold, List<ArbeidsforholdOverstyringEntitet> overstyringer) {
        Set<String> orgnummer = arbeidsforhold.stream().map(ArbeidsforholdWrapper::getArbeidsgiverIdentifikator).collect(Collectors.toSet());
        Optional<AktørInntekt> aktørInntekt = grunnlagOptional.flatMap(g -> g.getAktørInntektForFørStp(behandling.getAktørId()));
        List<ArbeidsforholdWrapper> arbeidsforholdWrappers = new ArrayList<>();
        if (aktørInntekt.isEmpty()) {
            return arbeidsforholdWrappers;
        }
        for (Inntekt inntekt : aktørInntekt.get().getInntektPensjonsgivende()) {
            Collection<Inntektspost> inntektspost = inntekt.getInntektspost();
            if (finnesLønnsInntekt(inntektspost) && finnesKunSporAvInntekter(behandling, orgnummer, grunnlagOptional, inntekt)) {
                ArbeidsforholdWrapper wrapper = new ArbeidsforholdWrapper();
                mapArbeidsgiver(wrapper, inntekt.getArbeidsgiver());
                wrapper.setErEndret(sjekkOmFinnesIOverstyr(overstyringer, inntekt.getArbeidsgiver().getIdentifikator(), null));
                wrapper.setFomDato(inntektspost.stream().map(Inntektspost::getFraOgMed).min(LocalDate::compareTo).orElse(null));
                wrapper.setTomDato(inntektspost.stream().map(Inntektspost::getTilOgMed).max(LocalDate::compareTo).orElse(null));
                wrapper.setBrukArbeidsforholdet(true);
                wrapper.setIkkeRegistrertIAaRegister(true);
                wrapper.setKilde(INNTEKTSKOMPONENTEN);
                arbeidsforholdWrappers.add(wrapper);
            }
        }
        return arbeidsforholdWrappers;
    }

    private boolean finnesLønnsInntekt(Collection<Inntektspost> inntektspost) {
        return inntektspost.stream().anyMatch(i -> i.getInntektspostType().equals(InntektspostType.LØNN));
    }

    private boolean finnesKunSporAvInntekter(Behandling behandling, Set<String> orgnummer, Optional<InntektArbeidYtelseGrunnlag> grunnlagOptional, Inntekt inntekt) {
        Collection<Yrkesaktivitet> yaFør = grunnlagOptional.flatMap(g -> g.getAktørArbeidFørStp(behandling.getAktørId()))
            .map(aa -> ((AktørArbeidEntitet) aa).hentAlleYrkesaktiviter())
            .orElse(emptyList());
        Collection<Yrkesaktivitet> yaEtter = grunnlagOptional.flatMap(g -> g.getAktørArbeidEtterStp(behandling.getAktørId()))
            .map(aa -> ((AktørArbeidEntitet) aa).hentAlleYrkesaktiviter())
            .orElse(emptyList());

        List<Yrkesaktivitet> yrkesaktiviteter = finnYrkesaktivitet(yaFør, inntekt.getArbeidsgiver(), null);
        if (yrkesaktiviteter.isEmpty()) {
            yrkesaktiviteter = finnYrkesaktivitet(yaEtter, inntekt.getArbeidsgiver(), null);
        }
        DatoIntervallEntitet avtale = finnAnsettelsesPeriode(yrkesaktiviteter);

        return avtale == null &&
            inntekt.getArbeidsgiver() != null && inntekt.getArbeidsgiver().getErVirksomhet()
            && !orgnummer.contains(inntekt.getArbeidsgiver().getIdentifikator());
    }

    private List<ArbeidsforholdWrapper> utledArbeidsforholdFraYrkesaktivitet(Collection<Yrkesaktivitet> yrkesaktiviteter,
                                                                             List<ArbeidsforholdOverstyringEntitet> overstyringer,
                                                                             List<Inntektsmelding> inntektsmeldinger, LocalDate skjæringstidspunkt) {
        return yrkesaktiviteter.stream()
            .filter(yr -> AA_REG_TYPER.contains(yr.getArbeidType()))
            .filter(yr -> harIkkeFåttInntektsmelding(yr, inntektsmeldinger))
            .map(yr -> {
                ArbeidsforholdWrapper wrapper = new ArbeidsforholdWrapper();
                final ArbeidsforholdRef arbeidsforholdRef = yr.getArbeidsforholdRef().orElse(ArbeidsforholdRef.ref(null));
                mapArbeidsgiver(wrapper, yr.getArbeidsgiver());
                wrapper.setStillingsprosent(finnStillingsprosent(List.of(yr), skjæringstidspunkt));
                wrapper.setFomDato(yr.getAnsettelsesPeriode().map(AktivitetsAvtale::getFraOgMed).orElse(null));
                wrapper.setTomDato(yr.getAnsettelsesPeriode().map(AktivitetsAvtale::getTilOgMed).orElse(null));
                wrapper.setArbeidsforholdId(arbeidsforholdRef.getReferanse());
                wrapper.setKilde(ArbeidsforholdKilde.AAREGISTERET);
                wrapper.setIkkeRegistrertIAaRegister(false);
                wrapper.setBrukArbeidsforholdet(true);
                wrapper.setFortsettBehandlingUtenInntektsmelding(harTattStillingTil(yr, overstyringer));
                wrapper.setErEndret(sjekkOmFinnesIOverstyr(overstyringer, yr.getArbeidsgiver().getIdentifikator(), yr.getArbeidsforholdRef().orElse(null)));

                return wrapper;
            }).collect(Collectors.toList());
    }

    private void mapArbeidsgiver(ArbeidsforholdWrapper wrapper, Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver.getErVirksomhet()) {
            final Virksomhet virksomhet = arbeidsgiver.getVirksomhet();
            wrapper.setNavn(virksomhet.getNavn());
            wrapper.setArbeidsgiverIdentifikator(arbeidsgiver.getIdentifikator());
        } else {
            // Hent fnr og navn fra TPS når arbeidsgiver er en person
            Optional<Personinfo> personinfo = tpsTjeneste.hentBrukerForAktør(arbeidsgiver.getAktørId());
            if (personinfo.isPresent()) {
                wrapper.setNavn(personinfo.get().getNavn());
                wrapper.setArbeidsgiverIdentifikator(arbeidsgiver.getAktørId().getId());
                wrapper.setPersonArbeidsgiverFnr(personinfo.get().getPersonIdent().getIdent());
            } else {
                wrapper.setNavn("N/A");
                wrapper.setArbeidsgiverIdentifikator(arbeidsgiver.getAktørId().getId());
            }
        }
    }

    private Boolean harTattStillingTil(Yrkesaktivitet yr, List<ArbeidsforholdOverstyringEntitet> overstyringer) {
        return overstyringer.stream()
            .anyMatch(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING)
                && ov.getArbeidsgiver().equals(yr.getArbeidsgiver())
                && (ov.getArbeidsforholdRef().gjelderFor(yr.getArbeidsforholdRef().orElse(null))));
    }

    private String harErstattetEtEllerFlereArbeidsforhold(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref,
                                                          List<ArbeidsforholdOverstyringEntitet> overstyringer) {
        if (ref == null || !ref.gjelderForSpesifiktArbeidsforhold()) {
            return null;
        }
        return overstyringer.stream().filter(ov -> Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
            && Objects.equals(ov.getArbeidsgiver(), arbeidsgiver) && Objects.equals(ov.getNyArbeidsforholdRef(), ref))
            .findFirst()
            .map(ov -> ov.getArbeidsgiver().getIdentifikator() + "-" + ov.getArbeidsforholdRef().getReferanse())
            .orElse(null);
    }

    private boolean harIkkeFåttInntektsmelding(Yrkesaktivitet yr, List<Inntektsmelding> inntektsmeldinger) {
        return inntektsmeldinger.stream()
            .noneMatch(i -> yr.getArbeidsgiver().getErVirksomhet()
                && Objects.equals(i.getVirksomhet(), yr.getArbeidsgiver().getVirksomhet())
                && i.getArbeidsforholdRef().gjelderFor(yr.getArbeidsforholdRef().orElse(ArbeidsforholdRef.ref(null))));
    }
}
