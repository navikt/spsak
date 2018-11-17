package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import static no.nav.foreldrepenger.behandlingslager.behandling.RegisterdataDiffsjekker.eksistenssjekkResultat;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdOverstyringEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdReferanseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntektspost;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseAnvist;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Refusjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.AnnenAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.EgenNæring;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.Frilansoppdrag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.diff.DiffEntity;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.diff.TraverseEntityGraph;
import no.nav.foreldrepenger.behandlingslager.diff.YtelseKode;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class InntektArbeidYtelseRepositoryImpl implements InntektArbeidYtelseRepository {
    private static final String BEH_NULL = "behandling";
    private EntityManager entityManager;

    public InntektArbeidYtelseRepositoryImpl() {
        // CDI
    }

    @Inject
    public InntektArbeidYtelseRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentAggregat(Behandling behandling, LocalDate skjæringstidspunkt) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = getAktivtInntektArbeidGrunnlag(behandling, skjæringstidspunkt);
        return grunnlag.orElseThrow(() -> InntektArbeidYtelseFeil.FACTORY.fantIkkeForventetGrunnlagPåBehandling(behandling.getId()).toException());
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentAggregatHvisEksisterer(Behandling behandling, LocalDate skjæringstidspunkt) {
        Objects.requireNonNull(behandling, BEH_NULL);
        return hentAggregatHvisEksisterer(behandling.getId(), skjæringstidspunkt);
    }

    private Optional<InntektArbeidYtelseGrunnlag> hentAggregatPåIdHvisEksisterer(Long aggregatId, LocalDate skjæringstidspunkt) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = getVersjonAvInntektArbeidYtelsePåId(aggregatId);
        grunnlag.ifPresent(gr -> gr.setSkjæringstidspunkt(skjæringstidspunkt));
        return grunnlag.isPresent() ? Optional.of(grunnlag.get()) : Optional.empty();
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentAggregatHvisEksisterer(Long behandlingId, LocalDate skjæringstidspunkt) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = getAktivtInntektArbeidGrunnlag(behandlingId, skjæringstidspunkt);
        return grunnlag.isPresent() ? Optional.of(grunnlag.get()) : Optional.empty();
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentFørsteVersjon(Behandling behandling, LocalDate skjæringstidspunkt) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = getInitielVersjonInntektArbeidGrunnlag(behandling, skjæringstidspunkt);
        return grunnlag.orElseThrow(() -> InntektArbeidYtelseFeil.FACTORY.fantIkkeForventetGrunnlagPåBehandling(behandling.getId()).toException());
    }

    @Override
    public boolean erEndret(Long grunnlagId, Behandling behandling) {
        Objects.requireNonNull(behandling.getId(), "behandlingId"); //$NON-NLS-1$ //NOSONAR
        Long aktivGrunnlagId = getAktivtInntektArbeidGrunnlag(behandling.getId(), null)
            .map(InntektArbeidYtelseGrunnlagEntitet::getId)
            .orElse(null);
        return grunnlagId.equals(aktivGrunnlagId);
    }

    @Override
    public DiffResult diffResultat(InntektArbeidYtelseGrunnlagEntitet grunnlag1, InntektArbeidYtelseGrunnlagEntitet grunnlag2, FagsakYtelseType ytelseType, boolean onlyCheckTrackedFields) {
        return new RegisterdataDiffsjekker(YtelseKode.valueOf(ytelseType.getKode()), onlyCheckTrackedFields).getDiffEntity().diff(grunnlag1, grunnlag2);
    }

    @Override
    public Optional<Inntektsmelding> hentInntektsMeldingFor(MottattDokument mottattDokument) {
        final TypedQuery<InntektsmeldingEntitet> query = entityManager.createQuery("FROM Inntektsmelding i " +
            "WHERE i.mottattDokumentId = :mottattDokumentId", InntektsmeldingEntitet.class);
        query.setParameter("mottattDokumentId", mottattDokument.getId());
        final Optional<InntektsmeldingEntitet> entitet = query.getResultList().stream().findFirst();
        return entitet.isPresent() ? Optional.of(entitet.get()) : Optional.empty();
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder opprettBuilderFor(Behandling behandling, VersjonType versjonType) {
        return opprettBuilderForBuilder(InntektArbeidYtelseGrunnlagBuilder.oppdatere(hentAggregatHvisEksisterer(behandling, null)), versjonType);
    }

    @Override
    public ArbeidsforholdInformasjonBuilder opprettInformasjonBuilderFor(Behandling behandling) {
        return ArbeidsforholdInformasjonBuilder.oppdatere(InntektArbeidYtelseGrunnlagBuilder.oppdatere(hentAggregatHvisEksisterer(behandling, null)).getInformasjon());
    }

    @Override
    public void lagre(Behandling behandling, InntektArbeidYtelseAggregatBuilder builder) {
        InntektArbeidYtelseGrunnlagBuilder opptjeningAggregatBuilder = getGrunnlagBuilder(behandling, builder);
        lagreOgFlush(behandling, opptjeningAggregatBuilder.build());
    }

    @Override
    public void lagre(Behandling behandling, OppgittOpptjeningBuilder oppgittOpptjening) {
        Objects.requireNonNull(behandling, BEH_NULL);

        if (oppgittOpptjening == null) {
            return;
        }
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidAggregat = hentAggregatHvisEksisterer(behandling, null);

        InntektArbeidYtelseGrunnlagBuilder grunnlag = InntektArbeidYtelseGrunnlagBuilder.oppdatere(inntektArbeidAggregat);
        grunnlag.medOppgittOpptjening(oppgittOpptjening);

        lagreOgFlush(behandling, grunnlag.build());
    }

    @Override
    public void lagre(Behandling behandling, ArbeidsforholdInformasjonBuilder informasjon) {
        Objects.requireNonNull(behandling, BEH_NULL);
        Objects.requireNonNull(informasjon, "informasjon"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder builder = opprettGrunnlagBuilderFor(behandling);

        builder.ryddOppErstattedeArbeidsforhold(behandling.getAktørId(), informasjon.getReverserteErstattArbeidsforhold());
        builder.ryddOppErstattedeArbeidsforhold(behandling.getAktørId(), informasjon.getErstattArbeidsforhold());
        builder.medInformasjon(informasjon.build());

        lagreOgFlush(behandling, builder.build());
    }

    @Override
    public void lagre(Behandling behandling, Inntektsmelding inntektsmelding) {
        Objects.requireNonNull(behandling, BEH_NULL);
        Objects.requireNonNull(inntektsmelding, "inntektsmelding"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder builder = opprettGrunnlagBuilderFor(behandling);

        final ArbeidsforholdInformasjon informasjon = builder.getInformasjon();
        if (inntektsmelding.gjelderForEtSpesifiktArbeidsforhold()) {
            final ArbeidsforholdRef arbeidsforholdRef = informasjon
                .finnEllerOpprett(Arbeidsgiver.virksomhet(inntektsmelding.getVirksomhet()), inntektsmelding.getArbeidsforholdRef());
            ((InntektsmeldingEntitet) inntektsmelding).setArbeidsforholdId(arbeidsforholdRef);
        }

        final InntektsmeldingAggregatEntitet inntektsmeldinger = (InntektsmeldingAggregatEntitet) builder.getInntektsmeldinger();

        // Kommet inn inntektsmelding på arbeidsforhold som vi har gått videre med uten inntektsmelding?
        if (kommetInntektsmeldingPåArbeidsforholdHvorViTidligereBehandletUtenInntektsmelding(inntektsmelding, informasjon)) {
            final ArbeidsforholdInformasjonBuilder informasjonBuilder = ArbeidsforholdInformasjonBuilder.oppdatere(informasjon);
            informasjonBuilder.fjernOverstyringVedrørende(inntektsmelding.getVirksomhet(), inntektsmelding.getArbeidsforholdRef());
            builder.medInformasjon(informasjonBuilder.build());
        }

        inntektsmeldinger.leggTil(inntektsmelding);
        builder.setInntektsmeldinger(inntektsmeldinger);

        lagreOgFlush(behandling, builder.build());
    }

    private boolean kommetInntektsmeldingPåArbeidsforholdHvorViTidligereBehandletUtenInntektsmelding(Inntektsmelding inntektsmelding,
                                                                                                     ArbeidsforholdInformasjon informasjon) {
        return informasjon.getOverstyringer()
            .stream()
            .anyMatch(ov -> (Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING)
                || Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.IKKE_BRUK))
                && ov.getArbeidsgiver().getErVirksomhet()
                && ov.getArbeidsgiver().getVirksomhet().equals(inntektsmelding.getVirksomhet())
                && ov.getArbeidsforholdRef().gjelderFor(inntektsmelding.getArbeidsforholdRef()));
    }

    private InntektArbeidYtelseGrunnlagBuilder getGrunnlagBuilder(Behandling behandling, InntektArbeidYtelseAggregatBuilder builder) {
        Objects.requireNonNull(behandling, BEH_NULL);
        Objects.requireNonNull(builder, "inntektArbeidYtelserBuilder"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder opptjeningAggregatBuilder = opprettGrunnlagBuilderFor(behandling);
        opptjeningAggregatBuilder.medData(builder);
        return opptjeningAggregatBuilder;
    }

    @Override
    public boolean erEndring(Behandling behandling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        TraverseEntityGraph traverseEntityGraph = TraverseEntityGraphFactory.build(true);

        final Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = hentAggregatHvisEksisterer(behandling, null);

        InntektArbeidYtelseGrunnlagBuilder inntektArbeidYtelseGrunnlagBuilder = getGrunnlagBuilder(behandling, inntektArbeidYtelseAggregatBuilder);
        if (!inntektArbeidYtelseGrunnlag.isPresent() && inntektArbeidYtelseGrunnlagBuilder.erOppdatert()) {
            return false;
        }

        if (inntektArbeidYtelseGrunnlag.isPresent()) {
            final InntektArbeidYtelseGrunnlag gammelt = inntektArbeidYtelseGrunnlag.get();
            final DiffResult diff = new DiffEntity(traverseEntityGraph)
                .diff(gammelt, inntektArbeidYtelseGrunnlagBuilder.getKladd());
            return !diff.isEmpty();
        }

        return true;
    }

    @Override
    public boolean erEndringPåInntektsmelding(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå) {
        Optional<InntektsmeldingAggregat> eksisterendeInntektsmelding = før.getInntektsmeldinger();
        Optional<InntektsmeldingAggregat> nyInntektsmelding = nå.getInntektsmeldinger();

        Optional<Boolean> eksistenssjekkResultat = eksistenssjekkResultat(eksisterendeInntektsmelding, nyInntektsmelding);
        if (eksistenssjekkResultat.isPresent()) {
            return eksistenssjekkResultat.get();
        }

        InntektsmeldingAggregat eksisterende = eksisterendeInntektsmelding.get(); // NOSONAR - presens sjekket ovenfor
        InntektsmeldingAggregat ny = nyInntektsmelding.get(); // NOSONAR - presens sjekket ovenfor
        DiffResult diff = new RegisterdataDiffsjekker(YtelseKode.FP).getDiffEntity().diff(eksisterende, ny);
        return !diff.isEmpty();
    }

    @Override
    public boolean erEndringPåAktørArbeid(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå) {
        Collection<AktørArbeid> eksisterendeAktørArbeid = før.getAktørArbeidFørStp();
        Collection<AktørArbeid> nyAktørArbeid = nå.getAktørArbeidFørStp();

        DiffResult diff = new RegisterdataDiffsjekker(YtelseKode.FP).getDiffEntity().diff(eksisterendeAktørArbeid, nyAktørArbeid);
        return !diff.isEmpty();
    }

    @Override
    public boolean erEndringPåAktørInntekt(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå) {
        Collection<AktørInntekt> eksisterendeAktørInntekt = før.getAktørInntektForFørStp();
        Collection<AktørInntekt> nyAktørInntekt = nå.getAktørInntektForFørStp();

        DiffResult diff = new RegisterdataDiffsjekker(YtelseKode.FP).getDiffEntity().diff(eksisterendeAktørInntekt, nyAktørInntekt);
        return !diff.isEmpty();
    }

    @Override
    public AktørYtelseEndring endringPåAktørYtelse(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå) {
        // TODO (mglittum): Skrive om slik at vi ikke differ mot egen fagsak
        Predicate<Ytelse> predikatKildeFpsak = ytelse -> ytelse.getKilde().equals(Fagsystem.FPSAK)
            && !ytelse.getSaksnummer().equals(((InntektArbeidYtelseGrunnlagEntitet)nå).getBehandling().getFagsak().getSaksnummer());
        Predicate<Ytelse> predikatKildeEksterneRegistre = ytelse -> !ytelse.getKilde().equals(Fagsystem.FPSAK);

        List<Ytelse> førYtelserFpsak = hentYtelser(før, predikatKildeFpsak);
        List<Ytelse> nåYtelserFpsak = hentYtelser(nå, predikatKildeFpsak);
        boolean ytelserFpsakEndret = !new RegisterdataDiffsjekker(YtelseKode.FP).getDiffEntity().diff(førYtelserFpsak, nåYtelserFpsak).isEmpty();

        List<Ytelse> førYtelserEkstern = hentYtelser(før, predikatKildeEksterneRegistre);
        List<Ytelse> nåYtelserEkstern = hentYtelser(nå, predikatKildeEksterneRegistre);
        boolean ytelserEksterneRegistreEndret = !new RegisterdataDiffsjekker(YtelseKode.FP).getDiffEntity().diff(førYtelserEkstern, nåYtelserEkstern).isEmpty();

        return new AktørYtelseEndring(ytelserFpsakEndret, ytelserEksterneRegistreEndret);
    }

    private List<Ytelse> hentYtelser(InntektArbeidYtelseGrunnlag ytelseGrunnlag, Predicate<Ytelse> predikatYtelseskilde) {
        return ytelseGrunnlag.getAktørYtelseFørStp().stream()
            .flatMap(it -> it.getYtelser().stream())
            .filter(predikatYtelseskilde)
            .collect(Collectors.toList());
    }

    @Override
    public boolean erEndring(Behandling behandling, Behandling nyBehandling, LocalDate skjæringstidspunkt) {
        InntektArbeidYtelseGrunnlag nyttAggregat = hentAggregat(nyBehandling, skjæringstidspunkt);
        InntektArbeidYtelseGrunnlag eksisterendeAggregat = hentAggregatHvisEksisterer(behandling, skjæringstidspunkt)
            .orElse(null);
        return erEndring(eksisterendeAggregat, nyttAggregat);
    }

    @Override
    public boolean erEndring(InntektArbeidYtelseGrunnlag aggregat, InntektArbeidYtelseGrunnlag nyttAggregat) {

        TraverseEntityGraph traverseEntityGraph = TraverseEntityGraphFactory.build(true);

        if (aggregat == null) {
            return true;
        }

        DiffResult diff = new DiffEntity(traverseEntityGraph)
            .diff(aggregat, nyttAggregat);
        return !diff.isEmpty();
    }

    @Override
    public boolean harArbeidsforholdMedArbeidstyperSomAngitt(Behandling behandling, Set<ArbeidType> angitteArbeidtyper, LocalDate skjæringstidspunkt) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> inntektArbeidYtelseGrunnlag = getAktivtInntektArbeidGrunnlag(behandling, skjæringstidspunkt);
        if (!inntektArbeidYtelseGrunnlag.isPresent()) {
            return false;
        }
        Optional<AktørArbeid> aktørArbeid = inntektArbeidYtelseGrunnlag.get().getAktørArbeidFørStp(behandling.getAktørId());
        return aktørArbeid.filter(aktørArbeid1 -> !harIngenArbeidsforholdMedLøpendeAktivitetsavtale(aktørArbeid1.getYrkesaktiviteter())).isPresent();
    }

    @Override
    public void tilbakestillOverstyring(Behandling behandling) {
        Objects.requireNonNull(behandling, BEH_NULL);

        InntektArbeidYtelseGrunnlagBuilder aggregatBuilder = opprettGrunnlagBuilderFor(behandling);
        final InntektArbeidYtelseGrunnlag build = aggregatBuilder.build();
        ((InntektArbeidYtelseGrunnlagEntitet) build).setSaksbehandlet(null);
        lagreOgFlush(behandling, build);
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandling(Behandling gammelBehandling, Behandling nyBehandling) {
        Optional<InntektArbeidYtelseGrunnlag> origAggregat = hentAggregatHvisEksisterer(gammelBehandling, null);
        origAggregat.ifPresent(orig -> {
            InntektArbeidYtelseGrunnlagEntitet entitet = new InntektArbeidYtelseGrunnlagEntitet(orig);
            lagreOgFlush(nyBehandling, entitet);
        });
    }

    private boolean harIngenArbeidsforholdMedLøpendeAktivitetsavtale(Collection<Yrkesaktivitet> yrkesaktiviteter) {
        return yrkesaktiviteter.stream()
            .filter(Yrkesaktivitet::erArbeidsforhold)
            .map(Yrkesaktivitet::getAnsettelsesPeriode)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .noneMatch(AktivitetsAvtale::getErLøpende);
    }

    private InntektArbeidYtelseGrunnlagBuilder opprettGrunnlagBuilderFor(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidAggregat = hentAggregatHvisEksisterer(behandling, null);
        return InntektArbeidYtelseGrunnlagBuilder.oppdatere(inntektArbeidAggregat);
    }

    private void lagreOgFlush(Behandling behandling, InntektArbeidYtelseGrunnlag nyttGrunnlag) {
        Objects.requireNonNull(behandling, BEH_NULL);

        if (nyttGrunnlag == null) {
            return;
        }

        Optional<InntektArbeidYtelseGrunnlagEntitet> tidligereAggregat = getAktivtInntektArbeidGrunnlag(behandling, null);

        if (tidligereAggregat.isPresent()) {
            InntektArbeidYtelseGrunnlagEntitet aggregat = tidligereAggregat.get();
            if (diffResultat(aggregat, (InntektArbeidYtelseGrunnlagEntitet) nyttGrunnlag, behandling.getFagsakYtelseType(), false).isEmpty()) {
                return;
            }
            aggregat.setAktivt(false);
            entityManager.persist(aggregat);
            entityManager.flush();

            lagreGrunnlag(nyttGrunnlag, behandling);
        } else {
            lagreGrunnlag(nyttGrunnlag, behandling);
        }
        entityManager.flush();
    }

    private void lagreGrunnlag(InntektArbeidYtelseGrunnlag nyttGrunnlag, Behandling behandling) {
        ((InntektArbeidYtelseGrunnlagEntitet) nyttGrunnlag).setBehandling(behandling);

        nyttGrunnlag.getOppgittOpptjening().ifPresent(this::lagreOppgittOpptjening);

        final Optional<InntektArbeidYtelseAggregat> registerVersjon = ((InntektArbeidYtelseGrunnlagEntitet) nyttGrunnlag).getRegisterVersjon();
        registerVersjon.ifPresent(this::lagreInntektArbeid);

        final Optional<InntektArbeidYtelseAggregat> saksbehandletFørVersjon = nyttGrunnlag.getSaksbehandletVersjon();
        saksbehandletFørVersjon.ifPresent(this::lagreInntektArbeid);

        nyttGrunnlag.getInntektsmeldinger().ifPresent(this::lagreInntektsMeldinger);

        ((InntektArbeidYtelseGrunnlagEntitet) nyttGrunnlag).getInformasjon().ifPresent(this::lagreInformasjon);
        entityManager.persist(nyttGrunnlag);
    }

    private void lagreInformasjon(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        final ArbeidsforholdInformasjonEntitet arbeidsforholdInformasjonEntitet = (ArbeidsforholdInformasjonEntitet) arbeidsforholdInformasjon; //NOSONAR
        entityManager.persist(arbeidsforholdInformasjonEntitet);
        for (ArbeidsforholdReferanseEntitet referanseEntitet : arbeidsforholdInformasjonEntitet.getReferanser()) {
            entityManager.persist(referanseEntitet);
        }
        for (ArbeidsforholdOverstyringEntitet overstyringEntitet : arbeidsforholdInformasjonEntitet.getOverstyringer()) {
            entityManager.persist(overstyringEntitet);
        }
    }


    private void lagreOppgittOpptjening(OppgittOpptjening entitet) {
        entityManager.persist(entitet);

        for (AnnenAktivitet aktivitet : entitet.getAnnenAktivitet()) {
            entityManager.persist(aktivitet);
        }

        for (EgenNæring næring : entitet.getEgenNæring()) {
            entityManager.persist(næring);
        }

        entitet.getFrilans().ifPresent(frilans -> {
            entityManager.persist(frilans);
            for (Frilansoppdrag frilansoppdrag : frilans.getFrilansoppdrag()) {
                entityManager.persist(frilansoppdrag);
            }
        });

        List<OppgittArbeidsforhold> oppgittArbeidsforhold = entitet.getOppgittArbeidsforhold();
        for (OppgittArbeidsforhold arbeidsforhold : oppgittArbeidsforhold) {
            entityManager.persist(arbeidsforhold);
        }
    }

    private void lagreInntektsMeldinger(InntektsmeldingAggregat inntektsmeldingAggregat) {
        entityManager.persist(inntektsmeldingAggregat);
        for (Inntektsmelding entitet : inntektsmeldingAggregat.getInntektsmeldinger()) {
            entityManager.persist(entitet);
            for (Gradering gradering : entitet.getGraderinger()) {
                entityManager.persist(gradering);
            }

            for (NaturalYtelse naturalYtelse : entitet.getNaturalYtelser()) {
                entityManager.persist(naturalYtelse);
            }

            for (Refusjon refusjon : entitet.getEndringerRefusjon()) {
                entityManager.persist(refusjon);
            }
        }
    }

    private void lagreInntektArbeid(InntektArbeidYtelseAggregat entitet) {
        entityManager.persist(entitet);

        for (AktørArbeid aktørArbeid : entitet.getAktørArbeid()) {
            entityManager.persist(aktørArbeid);
            lagreAktørArbeid(aktørArbeid);
        }

        for (AktørInntekt aktørInntekt : entitet.getAktørInntekt()) {
            entityManager.persist(aktørInntekt);
            lagreInntekt(aktørInntekt);
        }

        for (AktørYtelse aktørYtelse : entitet.getAktørYtelse()) {
            entityManager.persist(aktørYtelse);
            lagreAktørYtelse(aktørYtelse);
        }
    }

    private void lagreInntekt(AktørInntekt aktørInntekt) {
        for (Inntekt inntekt : ((AktørInntektEntitet) aktørInntekt).getInntekt()) {
            entityManager.persist(inntekt);
            for (Inntektspost inntektspost : inntekt.getInntektspost()) {
                entityManager.persist(inntektspost);
            }
        }
    }

    private void lagreAktørArbeid(AktørArbeid aktørArbeid) {
        for (Yrkesaktivitet yrkesaktivitet : ((AktørArbeidEntitet) aktørArbeid).hentAlleYrkesaktiviter()) {
            entityManager.persist(yrkesaktivitet);
            for (AktivitetsAvtale aktivitetsAvtale : ((YrkesaktivitetEntitet) yrkesaktivitet).getAlleAktivitetsAvtaler()) {
                entityManager.persist(aktivitetsAvtale);
            }
            for (Permisjon permisjon : yrkesaktivitet.getPermisjon()) {
                entityManager.persist(permisjon);
            }
        }
    }

    private void lagreAktørYtelse(AktørYtelse aktørYtelse) {
        for (Ytelse ytelse : aktørYtelse.getYtelser()) {
            entityManager.persist(ytelse);
            for (YtelseAnvist ytelseAnvist : ytelse.getYtelseAnvist()) {
                entityManager.persist(ytelseAnvist);
            }
            ytelse.getYtelseGrunnlag().ifPresent(yg -> {
                entityManager.persist(yg);
                for (YtelseStørrelse størrelse : yg.getYtelseStørrelse()) {
                    entityManager.persist(størrelse);
                }
            });
        }
    }

    private InntektArbeidYtelseAggregatBuilder opprettBuilderFor(Optional<InntektArbeidYtelseGrunnlag> aggregat, VersjonType versjon) {
        Objects.requireNonNull(aggregat, "aggregat"); // NOSONAR $NON-NLS-1$
        if (aggregat.isPresent()) {
            final InntektArbeidYtelseGrunnlag aggregat1 = aggregat.get();
            ((InntektArbeidYtelseGrunnlagEntitet) aggregat1).setSkjæringstidspunkt(null);
            return InntektArbeidYtelseAggregatBuilder.oppdatere(hentRiktigVersjon(versjon, aggregat1), versjon);
        }
        throw InntektArbeidYtelseFeil.FACTORY.aggregatKanIkkeVæreNull().toException();
    }

    private Optional<InntektArbeidYtelseAggregat> hentRiktigVersjon(VersjonType versjonType, InntektArbeidYtelseGrunnlag aggregat) {
        if (versjonType == VersjonType.REGISTER) {
            return ((InntektArbeidYtelseGrunnlagEntitet) aggregat).getRegisterVersjon();
        } else if (versjonType == VersjonType.SAKSBEHANDLET) {
            return aggregat.getSaksbehandletVersjon();
        }
        throw new IllegalStateException("Kunne ikke finne riktig versjon av InntektArbeidYtelseGrunnlag");
    }

    private InntektArbeidYtelseAggregatBuilder opprettBuilderForBuilder(InntektArbeidYtelseGrunnlagBuilder aggregatBuilder, VersjonType versjonType) {
        Objects.requireNonNull(aggregatBuilder, "aggregatBuilder"); // NOSONAR $NON-NLS-1$
        return opprettBuilderFor(Optional.ofNullable(aggregatBuilder.getKladd()), versjonType);

    }

    private Optional<InntektArbeidYtelseGrunnlagEntitet> getAktivtInntektArbeidGrunnlag(Behandling behandling, LocalDate skjæringstidspunkt) {
        Objects.requireNonNull(behandling, BEH_NULL);
        return getAktivtInntektArbeidGrunnlag(behandling.getId(), skjæringstidspunkt);
    }

    private Optional<InntektArbeidYtelseGrunnlagEntitet> getAktivtInntektArbeidGrunnlag(Long behandlingId, LocalDate skjæringstidspunkt) {
        final TypedQuery<InntektArbeidYtelseGrunnlagEntitet> query = entityManager.createQuery("FROM InntektArbeidGrunnlag gr " +  // NOSONAR
            "WHERE gr.behandling.id = :behandlingId " + //$NON-NLS-1$ //NOSONAR
            "AND gr.aktiv = :aktivt", InntektArbeidYtelseGrunnlagEntitet.class);
        query.setParameter("behandlingId", behandlingId); // NOSONAR $NON-NLS-1$
        query.setParameter("aktivt", true);
        final Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = HibernateVerktøy.hentUniktResultat(query);
        grunnlag.ifPresent(InntektArbeidYtelseGrunnlagEntitet::taHensynTilBetraktninger);
        grunnlag.ifPresent(gr -> gr.setSkjæringstidspunkt(skjæringstidspunkt));
        return grunnlag;
    }

    private Optional<InntektArbeidYtelseGrunnlagEntitet> getInitielVersjonInntektArbeidGrunnlag(Behandling behandling, LocalDate skjæringstidspunkt) {
        Objects.requireNonNull(behandling, BEH_NULL);
        final TypedQuery<InntektArbeidYtelseGrunnlagEntitet> query = entityManager.createQuery("FROM InntektArbeidGrunnlag gr " + // NOSONAR $NON-NLS-1$
            "WHERE gr.behandling.id = :behandlingId " + //$NON-NLS-1$ //NOSONAR
            "order by gr.opprettetTidspunkt, gr.id", InntektArbeidYtelseGrunnlagEntitet.class);
        query.setParameter("behandlingId", behandling.getId()); // NOSONAR $NON-NLS-1$

        final Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = query.getResultList().stream().findFirst();
        grunnlag.ifPresent(InntektArbeidYtelseGrunnlagEntitet::taHensynTilBetraktninger);
        grunnlag.ifPresent(gr -> gr.setSkjæringstidspunkt(skjæringstidspunkt));
        return grunnlag;
    }

    @Override
    public Optional<Long> hentIdPåAktivInntektArbeidYtelse(Behandling behandling) {
        return getAktivtInntektArbeidGrunnlag(behandling, null)
            .map(InntektArbeidYtelseGrunnlagEntitet::getId);
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentInntektArbeidYtelsePåId(Long aggregatId) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> optGrunnlag = getVersjonAvInntektArbeidYtelsePåId(aggregatId);
        return optGrunnlag.isPresent() ? optGrunnlag.get() : null;
    }


    @Override
    public Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjon(Long inntektArbeidYtelseGrunnlagId) {
        final Optional<InntektArbeidYtelseGrunnlag> grunnlag = hentAggregatPåIdHvisEksisterer(inntektArbeidYtelseGrunnlagId, null);
        return hentArbeidsforholdInformasjon(grunnlag);
    }

    private Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjon(Optional<InntektArbeidYtelseGrunnlag> grunnlag) {
        if (grunnlag.isPresent()) {
            final Optional<InntektArbeidYtelseGrunnlagEntitet> inntektArbeidYtelseGrunnlag = Optional.of((InntektArbeidYtelseGrunnlagEntitet) grunnlag.get());
            return inntektArbeidYtelseGrunnlag.flatMap(InntektArbeidYtelseGrunnlagEntitet::getInformasjon);
        }
        return Optional.empty();
    }


    @Override
    public Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjon(Behandling behandling) {
        final Optional<InntektArbeidYtelseGrunnlag> grunnlag = hentAggregatHvisEksisterer(behandling, null);
        return hentArbeidsforholdInformasjon(grunnlag);
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentForrigeVersjonAvInntektsmelding(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); // NOSONAR $NON-NLS-1$

        Query query = entityManager.createQuery(
            "SELECT gr FROM InntektArbeidGrunnlag gr " + // NOSONAR $NON-NLS-1$
                "WHERE gr.behandling.id = :behandlingId " + //$NON-NLS-1$ //NOSONAR
                "AND gr.aktiv = false " +
                "AND gr.inntektsmeldinger.id = (SELECT DISTINCT MAX(gr2.inntektsmeldinger.id) " +
                "FROM InntektArbeidGrunnlag gr2 " +
                "WHERE gr2.behandling.id = :behandlingId " + //$NON-NLS-1$ //NOSONAR
                "AND gr2.aktiv = false " +
                "and gr2.inntektsmeldinger.id <> (SELECT DISTINCT MAX(gr3.inntektsmeldinger.id) " +
                "                FROM InntektArbeidGrunnlag gr3 " +
                "                WHERE gr3.behandling.id = :behandlingId)) " + //$NON-NLS-1$ //NOSONAR
                "ORDER BY gr.opprettetTidspunkt DESC"
            , InntektArbeidYtelseGrunnlag.class);

        query.setParameter("behandlingId", behandlingId); // NOSONAR $NON-NLS-1$

        if (query.getResultList().isEmpty()) {
            return Optional.empty();
        }
        InntektArbeidYtelseGrunnlagEntitet resultat = (InntektArbeidYtelseGrunnlagEntitet) query.getResultList().get(0);
        resultat.taHensynTilBetraktninger();
        return Optional.of(resultat);
    }

    private Optional<InntektArbeidYtelseGrunnlagEntitet> getVersjonAvInntektArbeidYtelsePåId(Long aggregatId) {
        Objects.requireNonNull(aggregatId, "aggregatId"); // NOSONAR $NON-NLS-1$
        final TypedQuery<InntektArbeidYtelseGrunnlagEntitet> query = entityManager.createQuery("FROM InntektArbeidGrunnlag gr " + // NOSONAR $NON-NLS-1$
            "WHERE gr.id = :aggregatId ", InntektArbeidYtelseGrunnlagEntitet.class);
        query.setParameter("aggregatId", aggregatId);
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlagOpt = query.getResultList().stream().findFirst();
        grunnlagOpt.ifPresent(InntektArbeidYtelseGrunnlagEntitet::taHensynTilBetraktninger);
        return grunnlagOpt;
    }
}
