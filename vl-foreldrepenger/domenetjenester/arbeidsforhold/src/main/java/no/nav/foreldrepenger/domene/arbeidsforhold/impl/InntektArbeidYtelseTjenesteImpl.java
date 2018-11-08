package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.IntervallUtil;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørYtelseEndring;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdOverstyringEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingSomIkkeKommer;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.aksjonspunkt.BekreftOpptjeningPeriodeDto;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Arbeidsforhold;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Organisasjon;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Person;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.util.FPDateUtil;

// TODO: Splitte opp
@ApplicationScoped
public class InntektArbeidYtelseTjenesteImpl implements InntektArbeidYtelseTjeneste {

    // Arbeidtyper som kommer fra AA-reg
    private static final Set<ArbeidType> AA_REG_TYPER = Collections.unmodifiableSet(new HashSet<>(asList(
        ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
        ArbeidType.MARITIMT_ARBEIDSFORHOLD,
        ArbeidType.FORENKLET_OPPGJØRSORDNING)));

    private InntektArbeidYtelseRepository repository;
    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private TpsTjeneste tpsTjeneste;
    private SøknadRepository søknadRepository;
    private BehandlingRepositoryProvider provider;
    private VirksomhetTjeneste virksomhetTjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private AksjonspunktutlederForVurderOpptjening aksjonspunktutlederForVurderOpptjening;
    private BehandlingRepository behandlingRepository;

    InntektArbeidYtelseTjenesteImpl() {
        // CDI
    }

    @Inject
    public InntektArbeidYtelseTjenesteImpl(BehandlingRepositoryProvider provider,
                                           ArbeidsforholdTjeneste arbeidsforholdTjeneste,
                                           TpsTjeneste tpsTjeneste,
                                           VirksomhetTjeneste virksomhetTjeneste,
                                           SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                           AksjonspunktutlederForVurderOpptjening aksjonspunktutlederForVurderOpptjening) {
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.aksjonspunktutlederForVurderOpptjening = aksjonspunktutlederForVurderOpptjening;
        Objects.requireNonNull(provider, "provider");
        this.provider = provider;
        this.repository = provider.getInntektArbeidYtelseRepository();
        this.behandlingRepository = provider.getBehandlingRepository();
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.tpsTjeneste = tpsTjeneste;
        this.søknadRepository = provider.getSøknadRepository();
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentAggregat(Behandling behandling) {
        return repository.hentAggregat(behandling, skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentAggregatHvisEksisterer(Behandling behandling) {
        return repository.hentAggregatHvisEksisterer(behandling, skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentAggregatPåIdHvisEksisterer(Long aggregatId) {
        InntektArbeidYtelseGrunnlag grunnlag = repository.hentInntektArbeidYtelsePåId(aggregatId);
        return grunnlag != null ? Optional.of(grunnlag) : Optional.empty();
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentAggregatHvisEksisterer(Long behandlingId) {
        final Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return repository.hentAggregatHvisEksisterer(behandlingId, skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentFørsteVersjon(Behandling behandling) {
        return repository.hentFørsteVersjon(behandling, skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
    }

    @Override
    public List<Inntektsmelding> hentAlleInntektsmeldinger(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> opptjeningAggregatOpt = repository.hentAggregatHvisEksisterer(behandling, null);
        if (opptjeningAggregatOpt.isPresent()) {
            return opptjeningAggregatOpt.get().getInntektsmeldinger().map(InntektsmeldingAggregat::getInntektsmeldinger).orElse(emptyList());
        }
        return emptyList();
    }

    @Override
    public List<Inntektsmelding> hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(Behandling revurdering) {
        Behandling origBehandling = revurdering.getOriginalBehandling()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Denne metoden benyttes bare for revurderinger"));

        Map<String, Inntektsmelding> revurderingIM = hentIMMedIndexKey(revurdering);
        Map<String, Inntektsmelding> origIM = hentIMMedIndexKey(origBehandling);
        return revurderingIM.entrySet().stream()
            .filter(imRevurderingEntry -> !origIM.containsKey(imRevurderingEntry.getKey())
                || !Objects.equals(origIM.get(imRevurderingEntry.getKey()).getMottattDokumentId(), imRevurderingEntry.getValue().getMottattDokumentId()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
    }

    private Map<String, Inntektsmelding> hentIMMedIndexKey(Behandling revurdering) {
        List<Inntektsmelding> inntektsmeldinger = repository.hentAggregatHvisEksisterer(revurdering, null)
            .map(InntektArbeidYtelseGrunnlag::getInntektsmeldinger)
            .filter(Optional::isPresent)
            .map(imAggregat -> imAggregat.get().getInntektsmeldinger())
            .orElse(Collections.emptyList());

        return inntektsmeldinger.stream()
            .collect(Collectors.toMap(im -> ((InntektsmeldingEntitet) im).getIndexKey(), im -> im));
    }

    @Override
    public List<InntektsmeldingSomIkkeKommer> hentAlleInntektsmeldingerSomIkkeKommer(Behandling behandling) {
        List<InntektsmeldingSomIkkeKommer> result = new ArrayList<>();
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = hentAggregatHvisEksisterer(behandling);
        inntektArbeidYtelseGrunnlag.ifPresent(iayg -> result.addAll(iayg.getInntektsmeldingerSomIkkeKommer()));
        return result;
    }

    @Override
    public List<Inntektsmelding> hentAlleInntektsmeldingerForFagsak(Long fagsakId) {
        final List<Behandling> behandlinger = provider.getBehandlingRepository().hentBehandlingerSomIkkeErAvsluttetForFagsakId(fagsakId);

        List<Inntektsmelding> inntektsmeldinger = new ArrayList<>();
        for (Behandling behandling : behandlinger) {
            inntektsmeldinger.addAll(hentAlleInntektsmeldinger(behandling));
        }
        return inntektsmeldinger;
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentForrigeVersjonAvInntektsmelding(Long behandlingId) {
        return repository.hentForrigeVersjonAvInntektsmelding(behandlingId);
    }

    @Override
    public Optional<ArbeidsforholdInformasjon> hentInformasjon(Behandling behandling) {
        return repository.hentArbeidsforholdInformasjon(behandling);
    }

    @Override
    public Optional<ArbeidsforholdInformasjon> hentInformasjon(Long inntektArbeidYtelseGrunnlagId) {
        return repository.hentArbeidsforholdInformasjon(inntektArbeidYtelseGrunnlagId);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder opprettBuilderForRegister(Behandling behandling) {
        return repository.opprettBuilderFor(behandling, VersjonType.REGISTER);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder opprettBuilderForSaksbehandlet(Behandling behandling) {
        return repository.opprettBuilderFor(behandling, VersjonType.SAKSBEHANDLET);
    }

    @Override
    public void lagre(Behandling behandling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        repository.lagre(behandling, inntektArbeidYtelseAggregatBuilder);
    }

    @Override
    public boolean erEndret(Behandling behandling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        return repository.erEndring(behandling, inntektArbeidYtelseAggregatBuilder);
    }

    @Override
    public Collection<Yrkesaktivitet> hentYrkesaktiviteterForSøker(Behandling behandling, boolean overstyrt) {
        AktørId søker = behandling.getAktørId();
        Optional<InntektArbeidYtelseGrunnlag> grunnlagOptional = repository.hentAggregatHvisEksisterer(behandling,
            skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
        if (grunnlagOptional.isPresent()) {
            return grunnlagOptional.get().hentAlleYrkesaktiviteterFørStpFor(søker, overstyrt);
        }
        return emptyList();
    }

    @Override
    public void bekreftPeriodeAksjonspunkt(Behandling behandling, List<BekreftOpptjeningPeriodeDto> adapter) {
        new BekreftOpptjeningPeriodeAksjonspunkt(provider, virksomhetTjeneste, this, aksjonspunktutlederForVurderOpptjening)
            .oppdater(behandling, adapter);
    }

    @Override
    public Map<String, Set<String>> utledManglendeInntektsmeldingerFraArkiv(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling");
        Map<String, Set<String>> påkrevdeInntektsmeldinger = utledPåkrevdeInntektsmeldingerFraArkiv(behandling);
        filtrerUtMottatteInntektsmeldinger(behandling, påkrevdeInntektsmeldinger);
        return påkrevdeInntektsmeldinger;
    }

    @Override
    public Map<String, Set<String>> utledManglendeInntektsmeldingerFraGrunnlag(Behandling behandling) {
        final Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = repository.hentAggregatHvisEksisterer(behandling,
            skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
        Map<String, Set<String>> påkrevdeInntektsmeldinger = utledPåkrevdeInntektsmeldingerFraGrunnlag(behandling, inntektArbeidYtelseGrunnlag);
        filtrerUtMottatteInntektsmeldinger(behandling, påkrevdeInntektsmeldinger);
        return påkrevdeInntektsmeldinger;
    }

    private void filtrerUtMottatteInntektsmeldinger(Behandling behandling, Map<String, Set<String>> påkrevdeInntektsmeldinger) {
        inntektsmeldingerSomHarKommet(behandling, påkrevdeInntektsmeldinger);
        fjernInntektsmeldingerSomAltErAvklart(behandling, påkrevdeInntektsmeldinger);
    }

    private void inntektsmeldingerSomHarKommet(Behandling behandling, Map<String, Set<String>> påkrevdeInntektsmeldinger) {
        List<Inntektsmelding> inntektsmeldinger;
        if (behandling.erRevurdering()) {
            inntektsmeldinger = hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(behandling);
        } else {
            inntektsmeldinger = hentAlleInntektsmeldinger(behandling);
        }

        for (Inntektsmelding inntektsmelding : inntektsmeldinger) {
            if (påkrevdeInntektsmeldinger.containsKey(inntektsmelding.getVirksomhet().getOrgnr())) {
                final Set<String> arbeidsforhold = påkrevdeInntektsmeldinger.get(inntektsmelding.getVirksomhet().getOrgnr());
                if (inntektsmelding.gjelderForEtSpesifiktArbeidsforhold()) {
                    arbeidsforhold.remove(inntektsmelding.getArbeidsforholdRef().getReferanse());
                } else {
                    arbeidsforhold.clear();
                }
                if (arbeidsforhold.isEmpty()) {
                    påkrevdeInntektsmeldinger.remove(inntektsmelding.getVirksomhet().getOrgnr());
                }
            }
        }
    }

    private void fjernInntektsmeldingerSomAltErAvklart(Behandling behandling, Map<String, Set<String>> påkrevdeInntektsmeldinger) {
        final Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon = hentInformasjon(behandling);
        if (arbeidsforholdInformasjon.isPresent()) {
            final ArbeidsforholdInformasjon informasjon = arbeidsforholdInformasjon.get();
            final List<ArbeidsforholdOverstyringEntitet> inntektsmeldingSomIkkeKommer = informasjon.getOverstyringer()
                .stream()
                .filter(it -> it.getHandling().equals(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING))
                .collect(Collectors.toList());

            for (ArbeidsforholdOverstyringEntitet im : inntektsmeldingSomIkkeKommer) {
                if (påkrevdeInntektsmeldinger.containsKey(im.getArbeidsgiver().getIdentifikator())) {
                    final Set<String> arbeidsforhold = påkrevdeInntektsmeldinger.get(im.getArbeidsgiver().getIdentifikator());
                    if (im.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
                        arbeidsforhold.remove(im.getArbeidsforholdRef().getReferanse());
                    } else {
                        arbeidsforhold.clear();
                    }
                    if (arbeidsforhold.isEmpty()) {
                        påkrevdeInntektsmeldinger.remove(im.getArbeidsgiver().getIdentifikator());
                    }
                }
            }
        }
    }

    /**
     * Utleder påkrevde inntektsmeldinger fra grunnlaget basert på informasjonen som har blitt innhentet fra aa-reg
     * (under INNREG-steget)
     * <p>
     * Sjekker opp mot mottatt dato, og melder påkrevde på de som har gjeldende(bruker var ansatt) på mottatt-dato.
     * <p>
     * Skal ikke benytte sjekk mot arkivet slik som gjøres i utledManglendeInntektsmeldingerFraArkiv da
     * disse verdiene skal ikke påvirkes av endringer i arkivet.
     */
    private Map<String, Set<String>> utledPåkrevdeInntektsmeldingerFraGrunnlag(Behandling behandling,
                                                                               Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag) {
        Map<String, Set<String>> påkrevdeInntektsmeldinger = new HashMap<>();
        final LocalDate mottattDato = utledMottattDato(behandling); // NOSONAR

        inntektArbeidYtelseGrunnlag.ifPresent(grunnlag -> {
            Optional<AktørArbeid> aktørArbeid = grunnlag.getAktørArbeidFørStp(behandling.getAktørId());
            aktørArbeid.ifPresent(arbeid -> arbeid.getYrkesaktiviteter().stream()
                .filter(ya -> AA_REG_TYPER.contains(ya.getArbeidType()))
                .filter(ya -> harRelevantAnsettelsesperiodeSomDekkerAngittDato(ya, mottattDato))
                .forEach(relevantYrkesaktivitet -> {
                    String identifikator = relevantYrkesaktivitet.getArbeidsgiver().getIdentifikator();
                    String referanse = relevantYrkesaktivitet.getArbeidsforholdRef().orElse(ArbeidsforholdRef.ref(null)).getReferanse();
                    if (påkrevdeInntektsmeldinger.containsKey(identifikator)) {
                        påkrevdeInntektsmeldinger.get(identifikator).add(referanse);
                    } else {
                        final Set<String> arbeidsforholdSet = new LinkedHashSet<>();
                        arbeidsforholdSet.add(referanse);
                        påkrevdeInntektsmeldinger.put(identifikator, arbeidsforholdSet);
                    }
                }));
        });
        return påkrevdeInntektsmeldinger;
    }

    private boolean harRelevantAnsettelsesperiodeSomDekkerAngittDato(Yrkesaktivitet yrkesaktivitet, LocalDate dato) {
        if (yrkesaktivitet.erArbeidsforhold()) {
            Optional<AktivitetsAvtale> ansettelsesPeriode = yrkesaktivitet.getAnsettelsesPeriode();
            if (ansettelsesPeriode.isPresent()) {
                AktivitetsAvtale avtale = ansettelsesPeriode.get();
                return IntervallUtil.byggIntervall(avtale.getFraOgMed(), avtale.getTilOgMed()).overlaps(IntervallUtil.tilIntervall(dato));
            }
        }
        return false;
    }

    @Override
    public Optional<Inntektsmelding> hentInntektsMeldingFor(MottattDokument mottattDokument) {
        return repository.hentInntektsMeldingFor(mottattDokument);
    }

    private Map<String, Set<String>> utledPåkrevdeInntektsmeldingerFraArkiv(Behandling behandling) {
        final PersonIdent fnr = tpsTjeneste.hentFnrForAktør(behandling.getAktørId());
        final LocalDate mottattDato = utledMottattDato(behandling);
        final Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforholds = arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(fnr,
            IntervallUtil.tilIntervall(mottattDato));

        return mapTilArbeidsgivereOgArbeidsforhold(behandling, arbeidsforholds);
    }

    private LocalDate utledMottattDato(Behandling behandling) {
        return søknadRepository.hentSøknadHvisEksisterer(behandling)
            .map(Søknad::getMottattDato)
            .orElse(LocalDate.now(FPDateUtil.getOffset()));
    }

    private Map<String, Set<String>> mapTilArbeidsgivereOgArbeidsforhold(Behandling behandling, Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforholds) {
        Map<String, Set<String>> påkrevdeInntektsmeldinger = new HashMap<>();

        for (ArbeidsforholdIdentifikator arbeidsforhold : arbeidsforholds.keySet()) {
            final String identifikator = arbeidsforhold.getArbeidsgiver().getIdentifikator();
            final ArbeidsforholdRef arbeidsforholdId = utledArbeidsforholdIdentifikator(arbeidsforhold, behandling);
            if (påkrevdeInntektsmeldinger.containsKey(identifikator)) {
                påkrevdeInntektsmeldinger.get(identifikator).add(arbeidsforholdId.getReferanse());
            } else {
                final Set<String> arbeidsforholdSet = new HashSet<>();
                arbeidsforholdSet.add(arbeidsforholdId.getReferanse());
                påkrevdeInntektsmeldinger.put(identifikator, arbeidsforholdSet);
            }
        }
        return påkrevdeInntektsmeldinger;
    }

    private ArbeidsforholdRef utledArbeidsforholdIdentifikator(ArbeidsforholdIdentifikator arbeidsforhold, Behandling behandling) {
        return finnReferanseFor(behandling, mapTilArbeidsgiver(arbeidsforhold),
            arbeidsforhold.getArbeidsforholdId(), false);
    }

    private Arbeidsgiver mapTilArbeidsgiver(ArbeidsforholdIdentifikator arbeidsforhold) {
        if (arbeidsforhold.getArbeidsgiver() instanceof Person) {
            return Arbeidsgiver.person(new AktørId(((Person) arbeidsforhold.getArbeidsgiver()).getAktørId()));
        } else if (arbeidsforhold.getArbeidsgiver() instanceof Organisasjon) {
            String orgnr = ((Organisasjon) arbeidsforhold.getArbeidsgiver()).getOrgNummer();
            return Arbeidsgiver.virksomhet(virksomhetTjeneste.hentOgLagreOrganisasjon(orgnr));
        }
        throw new IllegalArgumentException("Utvikler feil: Arbeidsgiver av ukjent type.");
    }

    @Override
    public boolean erEndret(Behandling origBehandling, Behandling nyBehandling) {
        return repository.erEndring(origBehandling, nyBehandling, skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(nyBehandling));
    }

    @Override
    public boolean erEndretInntektsmelding(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå) {
        return repository.erEndringPåInntektsmelding(før, nå);
    }

    @Override
    public boolean erEndretAktørArbeid(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå) {
        return repository.erEndringPåAktørArbeid(før, nå);
    }

    @Override
    public boolean erEndretAktørInntekt(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå) {
        return repository.erEndringPåAktørInntekt(før, nå);
    }

    @Override
    public AktørYtelseEndring endringPåAktørYtelse(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå) {
        return repository.endringPåAktørYtelse(før, nå);
    }

    @Override
    public EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling) {
        Optional<Long> funnetId = repository.hentIdPåAktivInntektArbeidYtelse(behandling);
        return funnetId
            .map(id -> EndringsresultatSnapshot.medSnapshot(InntektArbeidYtelseGrunnlag.class, id))
            .orElse(EndringsresultatSnapshot.utenSnapshot(InntektArbeidYtelseGrunnlag.class));
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentInntektArbeidYtelsePåId(Long aggregatId) {
        return repository.hentInntektArbeidYtelsePåId(aggregatId);
    }

    @Override
    public DiffResult diffResultat(EndringsresultatDiff idDiff, FagsakYtelseType ytelseType, boolean kunSporedeEndringer) {
        InntektArbeidYtelseGrunnlag grunnlag1 = repository.hentInntektArbeidYtelsePåId(idDiff.getGrunnlagId1());
        InntektArbeidYtelseGrunnlag grunnlag2 = repository.hentInntektArbeidYtelsePåId(idDiff.getGrunnlagId2());
        return repository.diffResultat((InntektArbeidYtelseGrunnlagEntitet) grunnlag1, (InntektArbeidYtelseGrunnlagEntitet) grunnlag2, ytelseType,
            kunSporedeEndringer);
    }

    @Override
    public ArbeidsforholdRef finnReferanseFor(Behandling behandling, Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef, boolean beholdErstattetVerdi) {
        final Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon = repository.hentArbeidsforholdInformasjon(behandling);
        if (arbeidsforholdInformasjon.isPresent()) {
            final ArbeidsforholdInformasjon informasjon = arbeidsforholdInformasjon.get();
            if (beholdErstattetVerdi) {
                return informasjon.finnForEksternBeholdHistoriskReferanse(arbeidsgiver, arbeidsforholdRef);
            }
            return informasjon.finnForEkstern(arbeidsgiver, arbeidsforholdRef);
        }
        return arbeidsforholdRef;
    }

    @Override
    public boolean søkerHarOppgittEgenNæring(Behandling behandling) {
        return hentAggregatHvisEksisterer(behandling)
            .flatMap(InntektArbeidYtelseGrunnlag::getOppgittOpptjening)
            .map(oppgittOpptjening -> !oppgittOpptjening.getEgenNæring().isEmpty())
            .orElse(false);
    }
}
