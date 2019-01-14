package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType.KONTROLLER_FAKTA;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType.FORENKLET_OPPGJØRSORDNING;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType.MARITIMT_ARBEIDSFORHOLD;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType.ORDINÆRT_ARBEIDSFORHOLD;
import static no.nav.foreldrepenger.domene.arbeidsforhold.impl.CollectionUtil.flatMapping;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingskontrollRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.VurderArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.util.Tuple;

@ApplicationScoped
public class VurderArbeidsforholdTjenesteImpl implements VurderArbeidsforholdTjeneste {

    private static final Set<ArbeidType> ARBEIDSFORHOLD_TYPER = Stream.of(ORDINÆRT_ARBEIDSFORHOLD, FORENKLET_OPPGJØRSORDNING, MARITIMT_ARBEIDSFORHOLD)
        .collect(Collectors.toSet());
    private static final Logger logger = LoggerFactory.getLogger(VurderArbeidsforholdTjenesteImpl.class);

    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;
    private BehandlingskontrollRepository behandlingskontrollRepository;

    VurderArbeidsforholdTjenesteImpl() {
    }

    @Inject
    public VurderArbeidsforholdTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                            VirksomhetTjeneste virksomhetTjeneste,
                                            BehandlingskontrollRepository behandlingskontrollRepository) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.behandlingskontrollRepository = behandlingskontrollRepository;
    }

    @Override
    public Map<Arbeidsgiver, Set<ArbeidsforholdRef>> vurder(Behandling behandling) {
        Map<Arbeidsgiver, Set<ArbeidsforholdRef>> result = new HashMap<>();

        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOptional = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);
        if (!inntektArbeidYtelseGrunnlagOptional.isPresent()) {
            return result;
        }

        InntektArbeidYtelseGrunnlag grunnlag = inntektArbeidYtelseGrunnlagOptional.get();

        vurderOmArbeidsforholdKanGjenkjennes(result, grunnlag, behandling);
        erAllePåkrevdeInntektsmeldingerMottatt(result, behandling);
        erRapportertNormalInntektUtenArbeidsforhold(result, grunnlag, behandling);
        erMottattInntektsmeldingUtenArbeidsforhold(result, grunnlag, behandling);

        return result;
    }

    @Override
    public Map<Arbeidsgiver, Set<ArbeidsforholdRef>> endringerIInntektsmelding(Behandling behandling) {
        Map<Arbeidsgiver, Set<ArbeidsforholdRef>> result = new HashMap<>();

        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOptional = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);
        if (!inntektArbeidYtelseGrunnlagOptional.isPresent()) {
            return result;
        }
        final InntektArbeidYtelseGrunnlag grunnlag = inntektArbeidYtelseGrunnlagOptional.get();
        Optional<InntektArbeidYtelseGrunnlag> eksisterendeGrunnlag = inntektArbeidYtelseTjeneste.hentForrigeVersjonAvInntektsmelding(behandling.getId());
        Optional<InntektsmeldingAggregat> nyAggregat = grunnlag.getInntektsmeldinger();

        final Map<Virksomhet, Set<ArbeidsforholdRef>> eksisterende = inntektsmeldingerPerVirksomhet(eksisterendeGrunnlag
            .flatMap(InntektArbeidYtelseGrunnlag::getInntektsmeldinger));
        final Map<Virksomhet, Set<ArbeidsforholdRef>> ny = inntektsmeldingerPerVirksomhet(nyAggregat);

        if (!eksisterende.equals(ny)) {
            // Klassifiser endringssjekk
            for (Map.Entry<Virksomhet, Set<ArbeidsforholdRef>> virksomhetSetEntry : ny.entrySet()) {
                endringIArbeidsforholdsId(result, virksomhetSetEntry, eksisterende, grunnlag);
            }
        }
        return result;
    }

    private void erMottattInntektsmeldingUtenArbeidsforhold(Map<Arbeidsgiver, Set<ArbeidsforholdRef>> result, InntektArbeidYtelseGrunnlag grunnlag, Behandling behandling) {
        final Optional<InntektsmeldingAggregat> inntektsmeldinger = grunnlag.getInntektsmeldinger();
        if (inntektsmeldinger.isPresent()) {
            final InntektsmeldingAggregat aggregat = inntektsmeldinger.get();
            for (Inntektsmelding inntektsmelding : aggregat.getInntektsmeldinger()) {
                final Tuple<Long, Long> antallArbeidsforIVirksomheten = antallArbeidsforIVirksomheten(behandling, grunnlag, inntektsmelding.getVirksomhet(),
                    inntektsmelding.getArbeidsforholdRef());
                if (antallArbeidsforIVirksomheten.getElement1() == 0 && antallArbeidsforIVirksomheten.getElement2() == 0
                    && ikkeTattStillingTil(inntektsmelding.getVirksomhet(), inntektsmelding.getArbeidsforholdRef(), grunnlag)) {
                    final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(inntektsmelding.getVirksomhet());
                    final Set<ArbeidsforholdRef> arbeidsforholdRefs = trekkUtRef(inntektsmelding);
                    leggTilResultat(result, arbeidsgiver, arbeidsforholdRefs);
                    logger.info("Inntektsmelding uten kjent arbeidsforhold: arbeidsgiver={}, arbeidsforholdRef={}", arbeidsgiver, arbeidsforholdRefs);
                }
            }
        }
    }

    private Set<ArbeidsforholdRef> trekkUtRef(Inntektsmelding inntektsmelding) {
        if (inntektsmelding.gjelderForEtSpesifiktArbeidsforhold()) {
            return Stream.of(inntektsmelding.getArbeidsforholdRef()).collect(Collectors.toSet());
        }
        return Stream.of(ArbeidsforholdRef.ref(null)).collect(Collectors.toSet());
    }

    private void erAllePåkrevdeInntektsmeldingerMottatt(Map<Arbeidsgiver, Set<ArbeidsforholdRef>> result, Behandling behandling) {
        final Map<String, Set<String>> manglendeInntektsmeldinger = inntektArbeidYtelseTjeneste.utledManglendeInntektsmeldingerFraGrunnlag(behandling);
        if (Objects.equals(behandling.getType(), BehandlingType.FØRSTEGANGSSØKNAD) && !manglendeInntektsmeldinger.keySet().isEmpty()) {
            for (Map.Entry<String, Set<String>> entry : manglendeInntektsmeldinger.entrySet()) {
                if(OrganisasjonsNummerValidator.erGyldig(entry.getKey())) {
                    final Optional<Virksomhet> optionalVirksomhet = virksomhetTjeneste.finnOrganisasjon(entry.getKey());
                    optionalVirksomhet.ifPresent(virksomhet -> {
                        final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
                        final Set<ArbeidsforholdRef> arbeidsforholdRefs = entry.getValue().stream().map(ArbeidsforholdRef::ref).collect(Collectors.toSet());
                        leggTilResultat(result, arbeidsgiver, arbeidsforholdRefs);
                        logger.info("Mangler inntektsmelding: arbeidsgiver={}, arbeidsforholdRef={}", arbeidsgiver, arbeidsforholdRefs);
                    });
                } else {
                    // Antar at det er personlig foretak
                    final Arbeidsgiver arbeidsgiver = Arbeidsgiver.person(new AktørId(entry.getKey()));
                    final Set<ArbeidsforholdRef> arbeidsforholdRefs = entry.getValue().stream().map(ArbeidsforholdRef::ref).collect(Collectors.toSet());
                    leggTilResultat(result, arbeidsgiver, arbeidsforholdRefs);
                    logger.info("Mangler inntektsmelding: arbeidsgiver={}, arbeidsforholdRef={}", arbeidsgiver, arbeidsforholdRefs);
                }
            }
        }
    }

    private void leggTilResultat(Map<Arbeidsgiver, Set<ArbeidsforholdRef>> result, Arbeidsgiver arbeidsgiver, Set<ArbeidsforholdRef> arbeidsforholdRefs) {
        final Set<ArbeidsforholdRef> arbeidsgiverSet = result.getOrDefault(arbeidsgiver, new HashSet<>());
        arbeidsgiverSet.addAll(arbeidsforholdRefs);
        result.put(arbeidsgiver, arbeidsgiverSet);
    }

    private void vurderOmArbeidsforholdKanGjenkjennes(Map<Arbeidsgiver, Set<ArbeidsforholdRef>> result, InntektArbeidYtelseGrunnlag grunnlag, Behandling behandling) {
        if (skalTaStillingTilEndringerIArbeidsforhold(behandling)) {
            Optional<InntektArbeidYtelseGrunnlag> eksisterendeGrunnlag = inntektArbeidYtelseTjeneste.hentForrigeVersjonAvInntektsmelding(behandling.getId());
            Optional<InntektsmeldingAggregat> nyAggregat = grunnlag.getInntektsmeldinger();

            final Map<Virksomhet, Set<ArbeidsforholdRef>> eksisterende = inntektsmeldingerPerVirksomhet(eksisterendeGrunnlag
                .flatMap(InntektArbeidYtelseGrunnlag::getInntektsmeldinger));
            final Map<Virksomhet, Set<ArbeidsforholdRef>> ny = inntektsmeldingerPerVirksomhet(nyAggregat);

            if (!eksisterende.isEmpty() && !eksisterende.equals(ny)) {
                // Klassifiser endringssjekk
                for (Map.Entry<Virksomhet, Set<ArbeidsforholdRef>> virksomhetSetEntry : ny.entrySet()) {
                    sjekkAlleArbeidsforhold(result, grunnlag, behandling, eksisterende, virksomhetSetEntry);
                    endringIArbeidsforholdsId(result, virksomhetSetEntry, eksisterende, grunnlag);
                }
            }
        }
    }

    private void sjekkAlleArbeidsforhold(Map<Arbeidsgiver, Set<ArbeidsforholdRef>> result, InntektArbeidYtelseGrunnlag grunnlag, Behandling behandling, Map<Virksomhet, Set<ArbeidsforholdRef>> eksisterende, Map.Entry<Virksomhet, Set<ArbeidsforholdRef>> virksomhetSetEntry) {
        for (ArbeidsforholdRef arbeidsforholdRef : virksomhetSetEntry.getValue()) {
            if (ikkeTattStillingTil(virksomhetSetEntry.getKey(), arbeidsforholdRef, grunnlag)) {
                erEndringIAntall(result, virksomhetSetEntry, eksisterende,
                    antallArbeidsforIVirksomheten(behandling, grunnlag, virksomhetSetEntry.getKey(), arbeidsforholdRef));
            }
        }
    }

    private boolean ikkeTattStillingTil(Virksomhet virksomhet, ArbeidsforholdRef arbeidsforholdRef, InntektArbeidYtelseGrunnlag grunnlag) {
        final Optional<ArbeidsforholdInformasjon> informasjon = ((InntektArbeidYtelseGrunnlagEntitet) grunnlag).getInformasjon();
        if (informasjon.isPresent()) {
            final ArbeidsforholdInformasjon arbeidsforholdInformasjon = informasjon.get();
            return arbeidsforholdInformasjon.getOverstyringer()
                .stream()
                .noneMatch(ov -> ov.getArbeidsgiver().equals(Arbeidsgiver.virksomhet(virksomhet))
                    && ov.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef));
        }
        return false;
    }

    private boolean skalTaStillingTilEndringerIArbeidsforhold(Behandling behandling) {
        return !Objects.equals(behandling.getType(), BehandlingType.FØRSTEGANGSSØKNAD)
            || harPassertKontrollerFakta(behandling);
    }

    private boolean harPassertKontrollerFakta(Behandling behandling) {
        return behandlingskontrollRepository.getBehandlingStegTilstandHistorikk(behandling.getId()).stream().anyMatch(steg -> Objects.equals(steg.getStegType(), KONTROLLER_FAKTA));
    }

    private void endringIArbeidsforholdsId(Map<Arbeidsgiver, Set<ArbeidsforholdRef>> result,
                                           Map.Entry<Virksomhet, Set<ArbeidsforholdRef>> entry,
                                           Map<Virksomhet, Set<ArbeidsforholdRef>> eksisterende, InntektArbeidYtelseGrunnlag grunnlag) {
        final Set<ArbeidsforholdRef> nyRefSet = entry.getValue();
        final Set<ArbeidsforholdRef> eksisterendeRefSet = eksisterende.getOrDefault(entry.getKey(), Collections.emptySet());
        if (!eksisterende.isEmpty() && !eksisterendeRefSet.equals(nyRefSet)) {
            nyRefSet.removeAll(eksisterendeRefSet);
            nyRefSet.removeIf(it -> !ikkeTattStillingTil(entry.getKey(), it, grunnlag));
            if (!nyRefSet.isEmpty() && ikkeEndretFraSpesifikIdTilNullMenKunEtArbeidsforhold(nyRefSet, eksisterendeRefSet)) {
                final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(entry.getKey());
                leggTilResultat(result, arbeidsgiver, nyRefSet);
                logger.info("Endring i arbeidsforholdsId: arbeidsgiver={}, arbeidsforholdRef={}", arbeidsgiver, nyRefSet);
            }
        }
    }

    private boolean ikkeEndretFraSpesifikIdTilNullMenKunEtArbeidsforhold(Set<ArbeidsforholdRef> nyRefSet, Set<ArbeidsforholdRef> eksisterendeRefSet) {
        return !(nyRefSet.size() == 1 && nyRefSet.size() == eksisterendeRefSet.size()
            && !nyRefSet.iterator().next().gjelderForSpesifiktArbeidsforhold());
    }

    private void erEndringIAntall(Map<Arbeidsgiver, Set<ArbeidsforholdRef>> result,
                                  Map.Entry<Virksomhet, Set<ArbeidsforholdRef>> entry,
                                  Map<Virksomhet, Set<ArbeidsforholdRef>> eksisterende,
                                  Tuple<Long, Long> antallPåHverSideAvSkjæringstidspunkt) {
        final Set<ArbeidsforholdRef> nyRefs = new LinkedHashSet<>(entry.getValue());
        final int nyttAntall = nyRefs.size();
        final Set<ArbeidsforholdRef> eksisterendeRefs = eksisterende.getOrDefault(entry.getKey(), Collections.emptySet());
        final int eksisterendeAntall = eksisterendeRefs.size();

        if (overgangFraEnTilMange(eksisterendeAntall, nyttAntall)
            || overgangFraMangeTilEn(eksisterendeAntall, nyttAntall)
            || økningIAntallFraFørSkjæringstidspunkt(eksisterendeAntall, nyttAntall, antallPåHverSideAvSkjæringstidspunkt)) {
            nyRefs.removeAll(eksisterendeRefs);
            final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(entry.getKey());
            leggTilResultat(result, arbeidsgiver, nyRefs);
            logger.info("Endring i antall arbeidsforhold: arbeidsgiver={}, gammel={} -> ny={}", arbeidsgiver, eksisterendeRefs, nyRefs);
        }
    }

    private boolean økningIAntallFraFørSkjæringstidspunkt(int eksisterendeAntall, int nyttAntall,
                                                          Tuple<Long, Long> antallPåHverSideAvSkjæringstidspunkt) {
        return nyttAntall > eksisterendeAntall
            && antallPåHverSideAvSkjæringstidspunkt.getElement1() < nyttAntall
            && antallPåHverSideAvSkjæringstidspunkt.getElement1() <= antallPåHverSideAvSkjæringstidspunkt.getElement2();
    }

    private boolean overgangFraMangeTilEn(int eksisterendeAntall, int nyttAntall) {
        return nyttAntall == 1 && nyttAntall < eksisterendeAntall;
    }

    private boolean overgangFraEnTilMange(int eksisterendeAntall, int nyttAntall) {
        return eksisterendeAntall == 1 && nyttAntall > eksisterendeAntall;
    }

    private Tuple<Long, Long> antallArbeidsforIVirksomheten(Behandling behandling, InntektArbeidYtelseGrunnlag grunnlag, Virksomhet virksomhet, ArbeidsforholdRef arbeidsforholdRef) {
        final Optional<AktørArbeid> aktørArbeidFørStp = grunnlag.getAktørArbeidFørStp(behandling.getAktørId());
        final Optional<AktørArbeid> aktørArbeidEtterStp = grunnlag.getAktørArbeidEtterStp(behandling.getAktørId());

        long antallFør = antallArbeidsfor(virksomhet, arbeidsforholdRef, aktørArbeidFørStp);
        long antallEtter = antallArbeidsfor(virksomhet, arbeidsforholdRef, aktørArbeidEtterStp);

        return new Tuple<>(antallFør, antallEtter);
    }

    private long antallArbeidsfor(Virksomhet virksomhet, ArbeidsforholdRef arbeidsforholdRef, Optional<AktørArbeid> aktørArbeid) {
        long antall = 0;
        if (aktørArbeid.isPresent()) {
            antall = aktørArbeid.get().getYrkesaktiviteter()
                .stream()
                .filter(yr -> ARBEIDSFORHOLD_TYPER.contains(yr.getArbeidType())
                    && yr.getArbeidsgiver().equals(Arbeidsgiver.virksomhet(virksomhet))
                    && yr.getArbeidsforholdRef().orElse(ArbeidsforholdRef.ref(null)).gjelderFor(arbeidsforholdRef)).count();
        }
        return antall;
    }

    private Map<Virksomhet, Set<ArbeidsforholdRef>> inntektsmeldingerPerVirksomhet(Optional<InntektsmeldingAggregat> inntektsmeldingAggregat) {
        if (!inntektsmeldingAggregat.isPresent()) {
            return Collections.emptyMap();
        }
        return inntektsmeldingAggregat.get()
            .getInntektsmeldinger()
            .stream()
            .collect(Collectors.groupingBy(Inntektsmelding::getVirksomhet,
                flatMapping(im -> Stream.of(im.getArbeidsforholdRef()), Collectors.toSet())));
    }

    private void erRapportertNormalInntektUtenArbeidsforhold(Map<Arbeidsgiver, Set<ArbeidsforholdRef>> result, InntektArbeidYtelseGrunnlag grunnlag, Behandling behandling) {
        final Optional<AktørInntekt> inntektForFørStp = grunnlag.getAktørInntektForFørStp(behandling.getAktørId());

        inntektForFørStp.ifPresent(aktørInntekt -> aktørInntekt.getInntektPensjonsgivende()
            .forEach(inntekt -> sjekkHarIkkeArbeidsforhold(behandling, grunnlag, inntekt, result)));
    }

    private void sjekkHarIkkeArbeidsforhold(Behandling behandling, InntektArbeidYtelseGrunnlag grunnlag, Inntekt inntekt, Map<Arbeidsgiver, Set<ArbeidsforholdRef>> result) {
        if (inntekt.getInntektspost().stream().anyMatch(ip -> Objects.equals(ip.getInntektspostType(), InntektspostType.LØNN))) {
            final Optional<AktørArbeid> arbeidFørStp = grunnlag.getAktørArbeidFørStp(behandling.getAktørId());
            boolean ingenFør = true;
            final Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon = inntektArbeidYtelseTjeneste.hentInformasjon(behandling);
            if (arbeidFørStp.isPresent()) {
                ingenFør = ikkeArbeidsforholdRegisterert(inntekt, arbeidFørStp.get(), arbeidsforholdInformasjon);
            }
            final Optional<AktørArbeid> arbeidEtterStp = grunnlag.getAktørArbeidEtterStp(behandling.getAktørId());
            boolean ingenEtter = true;
            if (arbeidEtterStp.isPresent()) {
                ingenEtter = ikkeArbeidsforholdRegisterert(inntekt, arbeidEtterStp.get(), arbeidsforholdInformasjon);
            }
            if (ingenFør && ingenEtter) {
                Set<ArbeidsforholdRef> arbeidsforholdRefs = Stream.of(ArbeidsforholdRef.ref(null)).collect(Collectors.toSet());
                if (grunnlag.getInntektsmeldinger().isPresent()) {
                    if (inntekt.getArbeidsgiver().getErVirksomhet()) {
                        arbeidsforholdRefs = grunnlag.getInntektsmeldinger().get()
                            .getInntektsmeldingerFor(inntekt.getArbeidsgiver().getVirksomhet())
                            .stream()
                            .map(Inntektsmelding::getArbeidsforholdRef)
                            .collect(Collectors.toSet());
                    }
                }
                leggTilResultat(result, inntekt.getArbeidsgiver(), arbeidsforholdRefs);
                logger.info("Inntekter uten kjent arbeidsforhold: arbeidsgiver={}, arbeidsforholdRef={}", inntekt.getArbeidsgiver(), arbeidsforholdRefs);
            }
        }
    }

    private boolean ikkeArbeidsforholdRegisterert(Inntekt inntekt, AktørArbeid aktørArbeid, Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon) {
        // må også sjekke mot frilans. Skal ikke be om avklaring av inntektsposter som stammer fra frilansoppdrag
        Collection<Yrkesaktivitet> yrkesaktiviteter = aktørArbeid.getFrilansOppdrag();
        if (!yrkesaktiviteter.isEmpty() && yrkesaktiviteter.stream().anyMatch(y -> Objects.equals(y.getArbeidsgiver().getIdentifikator(), inntekt.getArbeidsgiver().getIdentifikator()))) {
            return false;
        }
        return aktørArbeid.getYrkesaktiviteter()
            .stream()
            .noneMatch(yr -> ARBEIDSFORHOLD_TYPER.contains(yr.getArbeidType())
                && yr.getArbeidsgiver().equals(inntekt.getArbeidsgiver()))
            && arbeidsforholdInformasjon.map(ArbeidsforholdInformasjon::getOverstyringer).orElse(Collections.emptyList())
            .stream()
            .noneMatch(it -> Objects.equals(it.getArbeidsgiver(), inntekt.getArbeidsgiver())
                && Objects.equals(it.getHandling(), ArbeidsforholdHandlingType.IKKE_BRUK));
    }

}
