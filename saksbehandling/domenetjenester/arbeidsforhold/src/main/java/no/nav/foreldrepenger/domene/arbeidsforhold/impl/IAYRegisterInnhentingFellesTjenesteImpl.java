package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandling.OpplysningsPeriodeTjeneste;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.IntervallUtil;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OffentligYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.PensjonTrygdType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseAnvistBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InnhentingSamletTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Arbeidsavtale;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Arbeidsforhold;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Organisasjon;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Person;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.FrilansArbeidsforhold;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.Månedsinntekt;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlagArbeidsforhold;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlagGrunnlag;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlagVedtak;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdSakOgGrunnlag;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

abstract class IAYRegisterInnhentingFellesTjenesteImpl implements IAYRegisterInnhentingTjeneste {

    protected VirksomhetTjeneste virksomhetTjeneste;
    protected SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private ResultatRepositoryProvider resultatProvider;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private InnhentingSamletTjeneste innhentingSamletTjeneste;
    private OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste;
    private KodeverkRepository kodeverkRepository;
    private GrunnlagRepositoryProvider grunnlagRepositoryProvider;

    public IAYRegisterInnhentingFellesTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                   GrunnlagRepositoryProvider repositoryProvider,
                                                   ResultatRepositoryProvider resultatProvider,
                                                   VirksomhetTjeneste virksomhetTjeneste,
                                                   SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                                   InnhentingSamletTjeneste innhentingSamletTjeneste,
                                                   OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.resultatProvider = resultatProvider;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.innhentingSamletTjeneste = innhentingSamletTjeneste;
        this.grunnlagRepositoryProvider = repositoryProvider;
        this.opplysningsPeriodeTjeneste = opplysningsPeriodeTjeneste;
    }

    IAYRegisterInnhentingFellesTjenesteImpl() {
    }

    @Override
    public Interval beregnOpplysningsPeriode(Behandling behandling) {
        return opplysningsPeriodeTjeneste.beregn(behandling);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder innhentOpptjeningForInnvolverteParter(Behandling behandling, Interval opplysningsPeriode) {
        // For Søker
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(behandling);
        byggOpptjeningOpplysningene(behandling, behandling.getAktørId(), opplysningsPeriode, inntektArbeidYtelseAggregatBuilder);

        return inntektArbeidYtelseAggregatBuilder;
    }

    InntektArbeidYtelseAggregatBuilder innhentYtelserForInvolverteParter(Behandling behandling, Interval opplysningsPeriode, boolean medGrunnlag) {
        // For Søker
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(behandling);
        byggYtelser(behandling, behandling.getAktørId(), opplysningsPeriode, inntektArbeidYtelseAggregatBuilder, medGrunnlag);

        return inntektArbeidYtelseAggregatBuilder;
    }

    @Override
    public void lagre(Behandling behandling, InntektArbeidYtelseAggregatBuilder builder) {
        inntektArbeidYtelseTjeneste.lagre(behandling, builder);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder innhentInntekterFor(Behandling behandling, AktørId aktørId, Interval opplysningsPeriode,
                                                                  InntektsKilde... kilder) {
        final InntektArbeidYtelseAggregatBuilder builder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(behandling);
        if (kilder.length == 0) {
            return builder;
        }
        for (InntektsKilde kilde : kilder) {
            final InntektsInformasjon inntektsInformasjon = innhentingSamletTjeneste.getInntektsInformasjon(aktørId, behandling, opplysningsPeriode, kilde);
            leggTilInntekter(aktørId, builder, inntektsInformasjon);
            if (kilde.equals(InntektsKilde.INNTEKT_OPPTJENING)) {
                inntektsInformasjon.getFrilansArbeidsforhold()
                    .entrySet()
                    .forEach(frilansArbeidsforhold -> oversettFrilanseArbeidsforhold(builder, frilansArbeidsforhold, aktørId));
            }
        }
        return builder;
    }

    private void leggTilInntekter(AktørId aktørId, InntektArbeidYtelseAggregatBuilder builder, InntektsInformasjon inntektsInformasjon) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = builder.getAktørInntektBuilder(aktørId);
        InntektsKilde kilde = inntektsInformasjon.getKilde();
        aktørInntektBuilder.fjernInntekterFraKilde(kilde);

        inntektsInformasjon.getMånedsinntekterGruppertPåArbeidsgiver()
            .forEach((identifikator, inntekter) -> leggTilInntekterPåArbeidsforhold(builder, aktørInntektBuilder, inntekter, identifikator, kilde));

        final List<Månedsinntekt> ytelsesTrygdEllerPensjonInntekt = inntektsInformasjon.getYtelsesTrygdEllerPensjonInntekt();
        if (!ytelsesTrygdEllerPensjonInntekt.isEmpty()) {
            leggTilYtelseInntekter(ytelsesTrygdEllerPensjonInntekt, builder, aktørId, kilde);
        }
    }

    private void leggTilYtelseInntekter(List<Månedsinntekt> ytelsesTrygdEllerPensjonInntekt, InntektArbeidYtelseAggregatBuilder builder, AktørId aktørId,
                                        InntektsKilde inntektOpptjening) {
        final InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = builder.getAktørInntektBuilder(aktørId);
        final AktørInntektEntitet.InntektBuilder inntektBuilderForYtelser = aktørInntektBuilder.getInntektBuilderForYtelser(inntektOpptjening);
        ytelsesTrygdEllerPensjonInntekt.forEach(mi -> lagInntektsposter(mi, inntektBuilderForYtelser));

        aktørInntektBuilder.leggTilInntekt(inntektBuilderForYtelser);
        builder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    private void oversettFrilanseArbeidsforhold(InntektArbeidYtelseAggregatBuilder builder,
                                                Map.Entry<ArbeidsforholdIdentifikator, List<FrilansArbeidsforhold>> frilansArbeidsforhold, AktørId aktørId) {
        final InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);
        final ArbeidsforholdIdentifikator arbeidsforholdIdentifikator = frilansArbeidsforhold.getKey();
        final Arbeidsgiver arbeidsgiver = mapArbeidsgiver(arbeidsforholdIdentifikator);
        final Opptjeningsnøkkel nøkkel = mapOpptjeningsnøkkel(arbeidsgiver, arbeidsforholdIdentifikator.getArbeidsforholdId());
        final ArbeidType arbeidType = kodeverkRepository.finn(ArbeidType.class, arbeidsforholdIdentifikator.getType());
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(nøkkel, arbeidType);
        yrkesaktivitetBuilder.medArbeidsforholdId(arbeidsforholdIdentifikator.getArbeidsforholdId())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(arbeidType);
        for (FrilansArbeidsforhold avtale : frilansArbeidsforhold.getValue()) {
            yrkesaktivitetBuilder.leggTilAktivitetsAvtale(opprettAktivitetsAvtaleFrilans(avtale, yrkesaktivitetBuilder));
        }

        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        builder.leggTilAktørArbeid(aktørArbeidBuilder);
    }

    private void leggTilInntekterPåArbeidsforhold(InntektArbeidYtelseAggregatBuilder builder,
                                                  InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder,
                                                  Map<YearMonth, BigDecimal> månedsinntekterGruppertPåArbeidsgiver,
                                                  String arbeidsgiverIdentifikator, InntektsKilde inntektOpptjening) {

        Arbeidsgiver arbeidsgiver = getArbeidsgiverForIdentifikator(arbeidsgiverIdentifikator);
        aktørInntektBuilder.leggTilInntekt(byggInntekt(månedsinntekterGruppertPåArbeidsgiver, arbeidsgiver, aktørInntektBuilder, inntektOpptjening));
        builder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    private void byggYtelser(Behandling behandling, AktørId aktørId, Interval opplysningsPeriode,
                             InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, boolean medGrunnlag) {
        List<InfotrygdSakOgGrunnlag> sammenstilt = innhentingSamletTjeneste.getSammenstiltSakOgGrunnlag(behandling, aktørId, opplysningsPeriode, medGrunnlag);

        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(aktørId);
        for (InfotrygdSakOgGrunnlag ytelse : sammenstilt) {
            RelatertYtelseType type = ytelse.getGrunnlag().map(YtelseBeregningsgrunnlagGrunnlag::getType).orElse(ytelse.getSak().getRelatertYtelseType());
            if (skalKopiereTilYtelse(behandling, aktørId, type)) {
                oversettSakGrunnlagTilYtelse(aktørYtelseBuilder, ytelse);
            }
        }

        if (medGrunnlag) {
            List<MeldekortUtbetalingsgrunnlagSak> arena = innhentingSamletTjeneste.hentYtelserTjenester(behandling, aktørId, opplysningsPeriode);
            for (MeldekortUtbetalingsgrunnlagSak sak : arena) {
                oversettMeldekortUtbetalingsgrunnlagTilYtelse(aktørYtelseBuilder, sak);
            }
        }

        // TODO (DIAMANT): avklar om man skal ta med tidligere vedtak i gjeldende fagsak for FP. For ES skal tidligere vedtak med (!medGrunnlag?).
        oversettRelaterteYtelserFraVedtaksløsning(aktørId, behandling, opplysningsPeriode, aktørYtelseBuilder, true);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
    }

    /**
     * Bestemmer hvilke {@link RelatertYtelseType} som skal kopieres inn for søker og annenpart.
     */
    private boolean skalKopiereTilYtelse(Behandling behandling, AktørId aktørId, RelatertYtelseType relatertYtelseType) {
        List<RelatertYtelseType> ytelseTyperSomErRelevantForAnnenPart = Arrays.asList(RelatertYtelseType.FORELDREPENGER, RelatertYtelseType.ENGANGSSTØNAD);
        if (aktørId.equals(behandling.getAktørId())) {
            return true;
        }
        return !aktørId.equals(behandling.getAktørId()) && ytelseTyperSomErRelevantForAnnenPart.contains(relatertYtelseType);
    }

    private Arbeidsgiver getArbeidsgiverForIdentifikator(String arbeidsgiverIdentifikator) {
        Arbeidsgiver arbeidsgiver;
        if (OrganisasjonsNummerValidator.erGyldig(arbeidsgiverIdentifikator)) {
            arbeidsgiver = Arbeidsgiver.virksomhet(hentVirksomhet(arbeidsgiverIdentifikator));
        } else {
            arbeidsgiver = Arbeidsgiver.person(new AktørId(arbeidsgiverIdentifikator));
        }
        return arbeidsgiver;
    }

    private void byggOpptjeningOpplysningene(Behandling behandling, AktørId aktørId, Interval opplysningsPeriode,
                                             InntektArbeidYtelseAggregatBuilder builder) {
        Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold = innhentingSamletTjeneste.getArbeidsforhold(aktørId, opplysningsPeriode);
        arbeidsforhold.entrySet().forEach(forholdet -> oversettArbeidsforholdTilYrkesaktivitet(builder, forholdet, aktørId, behandling));

        final InntektsInformasjon inntektsInformasjon = innhentingSamletTjeneste.getInntektsInformasjon(aktørId, behandling, opplysningsPeriode,
            InntektsKilde.INNTEKT_OPPTJENING);
        leggTilInntekter(aktørId, builder, inntektsInformasjon);
        inntektsInformasjon.getFrilansArbeidsforhold()
            .entrySet()
            .forEach(frilansArbeidsforhold -> oversettFrilanseArbeidsforhold(builder, frilansArbeidsforhold, aktørId));
    }

    private void oversettArbeidsforholdTilYrkesaktivitet(InntektArbeidYtelseAggregatBuilder builder,
                                                         Map.Entry<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold, AktørId aktørId,
                                                         Behandling behandling) {
        final ArbeidsforholdIdentifikator arbeidsgiverIdent = arbeidsforhold.getKey();
        final Arbeidsgiver arbeidsgiver = mapArbeidsgiver(arbeidsgiverIdent);
        final String arbeidsforholdId = arbeidsgiverIdent.harArbeidsforholdRef() ? arbeidsgiverIdent.getArbeidsforholdId().getReferanse() : null;
        final ArbeidsforholdRef arbeidsforholdRef = inntektArbeidYtelseTjeneste.finnReferanseFor(behandling, arbeidsgiver,
            ArbeidsforholdRef.ref(arbeidsforholdId), true);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);
        final Opptjeningsnøkkel nøkkel = mapOpptjeningsnøkkel(arbeidsgiver, arbeidsforholdRef);
        final ArbeidType arbeidsforholdType = kodeverkRepository.finnForKodeverkEiersKode(ArbeidType.class, arbeidsgiverIdent.getType());
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(nøkkel, arbeidsforholdType);

        byggYrkesaktivitetForSøker(arbeidsforhold, arbeidsgiver, yrkesaktivitetBuilder);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        builder.leggTilAktørArbeid(aktørArbeid);
    }

    private Opptjeningsnøkkel mapOpptjeningsnøkkel(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef) {
        return new Opptjeningsnøkkel(arbeidsforholdRef.getReferanse(),
            arbeidsgiver.getErVirksomhet() ? arbeidsgiver.getIdentifikator() : null,
            !arbeidsgiver.getErVirksomhet() ? arbeidsgiver.getIdentifikator() : null);
    }

    private void byggYrkesaktivitetForSøker(Map.Entry<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold,
                                            Arbeidsgiver arbeidsgiver, YrkesaktivitetBuilder builder) {
        opprettMinimalYrkesaktivitet(arbeidsforhold.getKey(), arbeidsgiver, builder);

        builder.tilbakestillAvtaler();
        for (Arbeidsforhold arbeidsforhold1 : arbeidsforhold.getValue()) {
            arbeidsforhold1.getArbeidsavtaler()
                .stream()
                .map(a -> opprettAktivitetsAvtaler(a, builder))
                .forEach(builder::leggTilAktivitetsAvtale);

            builder.tilbakestillPermisjon();
            arbeidsforhold1.getPermisjoner()
                .stream()
                .map(p -> opprettPermisjoner(p, builder, arbeidsforhold1.getArbeidTom()))
                .forEach(builder::leggTilPermisjon);
        }
    }

    private void opprettMinimalYrkesaktivitet(ArbeidsforholdIdentifikator arbeidsforhold,
                                              Arbeidsgiver arbeidsgiver, YrkesaktivitetBuilder yrkesaktivitetBuilder) {
        yrkesaktivitetBuilder
            .medArbeidType(kodeverkRepository.finnForKodeverkEiersKode(ArbeidType.class, arbeidsforhold.getType()))
            .medArbeidsforholdId(arbeidsforhold.getArbeidsforholdId())
            .medArbeidsgiver(arbeidsgiver);
    }

    private Arbeidsgiver mapArbeidsgiver(ArbeidsforholdIdentifikator arbeidsforhold) {
        if (arbeidsforhold.getArbeidsgiver() instanceof Person) {
            return Arbeidsgiver.person(new AktørId(((Person) arbeidsforhold.getArbeidsgiver()).getAktørId()));
        } else if (arbeidsforhold.getArbeidsgiver() instanceof Organisasjon) {
            String orgnr = ((Organisasjon) arbeidsforhold.getArbeidsgiver()).getOrgNummer();
            return Arbeidsgiver.virksomhet(hentVirksomhet(orgnr));
        }
        throw new IllegalArgumentException("Utvikler feil: Arbeidsgiver av ukjent type.");
    }

    private Virksomhet hentVirksomhet(String orgnr) {
        return virksomhetTjeneste.hentOgLagreOrganisasjon(orgnr);
    }

    private YrkesaktivitetEntitet.AktivitetsAvtaleBuilder opprettAktivitetsAvtaler(Arbeidsavtale arbeidsavtale,
                                                                                   YrkesaktivitetBuilder yrkesaktivitetBuilder) {
        DatoIntervallEntitet periode;
        if (arbeidsavtale.getArbeidsavtaleTom() == null) {
            periode = DatoIntervallEntitet.fraOgMed(arbeidsavtale.getArbeidsavtaleFom());
        } else {
            periode = DatoIntervallEntitet.fraOgMedTilOgMed(arbeidsavtale.getArbeidsavtaleFom(), arbeidsavtale.getArbeidsavtaleTom());
        }
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode,
            arbeidsavtale.getErAnsettelsesPerioden());
        aktivitetsAvtaleBuilder
            .medProsentsats(arbeidsavtale.getStillingsprosent())
            .medAntallTimer(arbeidsavtale.getBeregnetAntallTimerPrUke())
            .medAntallTimerFulltid(arbeidsavtale.getAvtaltArbeidstimerPerUke())
            .medSisteLønnsendringsdato(arbeidsavtale.getSisteLønnsendringsdato())
            .medPeriode(periode);

        return aktivitetsAvtaleBuilder;
    }

    private YrkesaktivitetEntitet.AktivitetsAvtaleBuilder opprettAktivitetsAvtaleFrilans(FrilansArbeidsforhold frilansArbeidsforhold,
                                                                                         YrkesaktivitetBuilder yrkesaktivitetBuilder) {
        DatoIntervallEntitet periode;
        if (frilansArbeidsforhold.getTom() == null) {
            periode = DatoIntervallEntitet.fraOgMed(frilansArbeidsforhold.getFom());
        } else {
            periode = DatoIntervallEntitet.fraOgMedTilOgMed(frilansArbeidsforhold.getFom(), frilansArbeidsforhold.getTom());
        }
        return yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, true);
    }

    private Permisjon opprettPermisjoner(no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Permisjon permisjon,
                                         YrkesaktivitetBuilder yrkesaktivitetBuilder, LocalDate arbeidsforholdTom) {
        YrkesaktivitetEntitet.PermisjonBuilder permisjonBuilder = yrkesaktivitetBuilder.getPermisjonBuilder();
        LocalDate permisjonTom = permisjon.getPermisjonTom() == null ? arbeidsforholdTom : permisjon.getPermisjonTom();
        return permisjonBuilder
            .medProsentsats(permisjon.getPermisjonsprosent())
            .medPeriode(permisjon.getPermisjonFom(), permisjonTom)
            .medPermisjonsbeskrivelseType(kodeverkRepository.finnForKodeverkEiersKode(PermisjonsbeskrivelseType.class, permisjon.getPermisjonsÅrsak(),
                PermisjonsbeskrivelseType.UDEFINERT))
            .build();
    }

    private AktørInntektEntitet.InntektBuilder byggInntekt(Map<YearMonth, BigDecimal> inntekter, Arbeidsgiver arbeidsgiver,
                                                           InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder,
                                                           InntektsKilde inntektOpptjening) {
        AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(inntektOpptjening, new Opptjeningsnøkkel(arbeidsgiver));

        inntekter.entrySet().forEach(mi -> lagInntektsposter(mi, inntektBuilder));

        return inntektBuilder.medArbeidsgiver(arbeidsgiver);
    }

    private void lagInntektsposter(Månedsinntekt månedsinntekt, AktørInntektEntitet.InntektBuilder inntektBuilder) {
        InntektEntitet.InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();
        inntektspostBuilder
            .medBeløp(månedsinntekt.getBeløp())
            .medPeriode(månedsinntekt.getMåned().atDay(1), månedsinntekt.getMåned().atEndOfMonth())
            .medInntektspostType(InntektspostType.LØNN);
        if (månedsinntekt.isYtelse()) {
            inntektspostBuilder.medInntektspostType(InntektspostType.YTELSE)
                .medYtelse(mapTilKodeliste(månedsinntekt));
        }
        inntektBuilder.leggTilInntektspost(inntektspostBuilder);
    }

    private void lagInntektsposter(Map.Entry<YearMonth, BigDecimal> månedsinntekt, AktørInntektEntitet.InntektBuilder inntektBuilder) {
        InntektEntitet.InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();
        inntektspostBuilder
            .medBeløp(månedsinntekt.getValue())
            .medPeriode(månedsinntekt.getKey().atDay(1), månedsinntekt.getKey().atEndOfMonth())
            .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspostBuilder);
    }

    private YtelseType mapTilKodeliste(Månedsinntekt månedsinntekt) {
        if (månedsinntekt.getPensjonKode() != null) {
            return kodeverkRepository.finnForKodeverkEiersKode(PensjonTrygdType.class, månedsinntekt.getPensjonKode());
        }
        return kodeverkRepository.finnForKodeverkEiersKode(OffentligYtelseType.class, månedsinntekt.getYtelseKode());
    }

    public void oversettSakGrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder, InfotrygdSakOgGrunnlag ytelse) {
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder
            .getYtelselseBuilderForType(Fagsystem.INFOTRYGD, ytelse.getSak().getRelatertYtelseType(), ytelse.getSak().getTemaUnderkategori(),
                ytelse.getPeriode())
            .medBehandlingsTema(ytelse.getSak().getTemaUnderkategori())
            .medSaksnummer(ytelse.getSaksnummer())
            .medStatus(ytelse.getSak().getRelatertYtelseTilstand())
            .medFagsystemUnderkategori(ytelse.getSak().getFagsystemUnderkategori());
        ytelseBuilder.tilbakestillAnvisteYtelser();
        ytelse.getGrunnlag().ifPresent(grunnlag -> {
            for (YtelseBeregningsgrunnlagVedtak vedtak : grunnlag.getVedtak()) {
                final DatoIntervallEntitet intervall = vedtak.getTom() == null ? DatoIntervallEntitet.fraOgMed(vedtak.getFom())
                    : DatoIntervallEntitet.fraOgMedTilOgMed(vedtak.getFom(), vedtak.getTom());
                ytelseBuilder.medYtelseAnvist(ytelseBuilder.getAnvistBuilder()
                    .medAnvistPeriode(intervall)
                    .medUtbetalingsgradProsent(vedtak.getUtbetalingsgrad() != null ? new BigDecimal(vedtak.getUtbetalingsgrad()) : null)
                    .build());
            }
            ytelseBuilder.medYtelseGrunnlag(oversettYtelseGrunnlag(grunnlag, ytelseBuilder.getGrunnlagBuilder()));
        });
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    private YtelseGrunnlag oversettYtelseGrunnlag(YtelseBeregningsgrunnlagGrunnlag grunnlag, YtelseGrunnlagBuilder grunnlagBuilder) {
        grunnlag.mapSpesialverdier(grunnlagBuilder);
        grunnlagBuilder.medArbeidskategori(grunnlag.getArbeidskategori());
        grunnlagBuilder.tilbakestillStørrelse();
        if (grunnlag.harArbeidsForhold()) {
            leggTilArbeidsforhold(grunnlagBuilder, grunnlag.getArbeidsforhold());
        }
        return grunnlagBuilder.build();
    }

    private void oversettMeldekortUtbetalingsgrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder,
                                                               MeldekortUtbetalingsgrunnlagSak ytelse) {
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(ytelse.getKilde(), ytelse.getYtelseType(), ytelse.getSaksnummer(),
            ytelse.getVedtaksPeriodeFom());
        ytelseBuilder
            .medPeriode(ytelse.getVedtaksPeriodeTom() == null ? DatoIntervallEntitet.fraOgMed(ytelse.getVedtaksPeriodeFom())
                : DatoIntervallEntitet.fraOgMedTilOgMed(ytelse.getVedtaksPeriodeFom(), ytelse.getVedtaksPeriodeTom()))
            .medStatus(ytelse.getYtelseTilstand())
            .medYtelseGrunnlag(ytelseBuilder.getGrunnlagBuilder().medOpprinneligIdentdato(ytelse.getKravMottattDato()).build());
        ytelseBuilder.tilbakestillAnvisteYtelser();
        for (MeldekortUtbetalingsgrunnlagMeldekort meldekort : ytelse.getMeldekortene()) {
            ytelseBuilder.medYtelseAnvist(ytelseBuilder.getAnvistBuilder()
                .medAnvistPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(meldekort.getMeldekortFom(), meldekort.getMeldekortTom()))
                .medBeløp(meldekort.getBeløp())
                .medDagsats(meldekort.getDagsats())
                .medUtbetalingsgradProsent(meldekort.getUtbetalingsgrad())
                .build());
        }
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    private void leggTilArbeidsforhold(YtelseGrunnlagBuilder ygBuilder, List<YtelseBeregningsgrunnlagArbeidsforhold> arbeidsforhold) {
        if (arbeidsforhold != null)
            for (YtelseBeregningsgrunnlagArbeidsforhold arbeid : arbeidsforhold) {
                final YtelseStørrelseBuilder ysBuilder = ygBuilder.getStørrelseBuilder();
                ysBuilder.medBeløp(arbeid.getInntektForPerioden())
                    .medHyppighet(arbeid.getInntektPeriodeType());
                if (arbeid.harGyldigOrgnr()) {
                    ysBuilder.medVirksomhet(virksomhetTjeneste.hentOgLagreOrganisasjon(arbeid.getOrgnr()));
                }
                ygBuilder.medYtelseStørrelse(ysBuilder.build());
            }
    }

    private void oversettRelaterteYtelserFraVedtaksløsning(final AktørId aktørId, Behandling behandling,
                                                           Interval opplysningsPeriode,
                                                           InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder,
                                                           boolean medDenneFagsaken) {
        final List<Fagsak> fagsakListe = grunnlagRepositoryProvider.getFagsakRepository().hentForBruker(aktørId);
        BehandlingRepository behandlingRepository = grunnlagRepositoryProvider.getBehandlingRepository();

        for (Fagsak fagsak : fagsakListe) {
            if (!medDenneFagsaken && fagsak.equals(behandling.getFagsak())) {
                continue;
            }
            // TODO (DIAMANT): Avklar hvilke(n) behandling man skal ta med. For FP siste revurdering. OBS: Ikke avsluttet behandling - for 5031!
            behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(fagsak.getId())
                .map(b -> behandlingRepository.hentResultat(b.getId()))
                .map(Behandlingsresultat::getBehandlingVedtak)
                .filter(behandlingVedtak -> behandlingVedtak.getVedtakResultatType().equals(VedtakResultatType.INNVILGET))
                .map(
                    behandlingVedtak -> mapFraFagsak(fagsak, aktørYtelseBuilder, behandlingVedtak.getBehandlingsresultat().getBehandling(), opplysningsPeriode))
                .ifPresent(aktørYtelseBuilder::leggTilYtelse);
        }
    }

    private void mapFraUttakTilYtelseAnvist(Behandling behandling, YtelseBuilder ytelseBuilder) {
        Optional<UttakResultatEntitet> uttakResultat = resultatProvider.getUttakRepository().hentUttakResultatHvisEksisterer(behandling);
        ytelseBuilder.tilbakestillAnvisteYtelser();
        if (uttakResultat.isPresent()) {
            List<YtelseAnvistBuilder> ytelseAnvistBuilderList = uttakResultat.get().getGjeldendePerioder().getPerioder().stream()
                .filter(p -> PeriodeResultatType.INNVILGET.equals(p.getPeriodeResultatType()))
                .map(periode -> ytelseBuilder.getAnvistBuilder().medAnvistPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom())))
                .collect(toList());

            for (YtelseAnvistBuilder ytelseAnvistBuilder : ytelseAnvistBuilderList) {
                ytelseBuilder.medYtelseAnvist(ytelseAnvistBuilder.build());
            }
        }
    }

    private YtelseBuilder mapFraFagsak(Fagsak fagsak, InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder,
                                       Behandling behandling, Interval periodeFraRelaterteYtelserSøkesIVL) {

        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.FPSAK, map(fagsak.getYtelseType()), fagsak.getSaksnummer())
            .medStatus(map(fagsak.getStatus()));

        Optional<UttakResultatEntitet> uttakResultat = resultatProvider.getUttakRepository().hentUttakResultatHvisEksisterer(behandling);
        if (uttakResultat.isPresent()) {
            ytelseBuilder.medPeriode(hentPeriodeFraUttak(uttakResultat.get()));
        } else {
            Behandlingsresultat behandlingsresultat = grunnlagRepositoryProvider.getBehandlingRepository().hentResultat(behandling.getId());
            ytelseBuilder.medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(behandlingsresultat.getBehandlingVedtak().getVedtaksdato(),
                behandlingsresultat.getBehandlingVedtak().getVedtaksdato()));
        }
        // Sjekker om perioden for uttak faktisk er utenfor innhentingsintervalet
        if (!periodeFraRelaterteYtelserSøkesIVL.overlaps(IntervallUtil.tilIntervall(ytelseBuilder.getPeriode().getTomDato()))) {
            return null;
        }
        mapFraUttakTilYtelseAnvist(behandling, ytelseBuilder);
        mapFraBeregning(behandling, ytelseBuilder);
        return ytelseBuilder;
    }

    private void mapFraBeregning(Behandling behandling, YtelseBuilder ytelseBuilder) {
        BeregningsgrunnlagRepository beregningsgrunnlagRepository = resultatProvider.getBeregningsgrunnlagRepository();
        Optional<Beregningsgrunnlag> beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);

        if (beregningsgrunnlag.isPresent() && !beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().isEmpty()) {
            BeregningsgrunnlagPeriode siste = beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().get(0);
            for (BeregningsgrunnlagPeriode periode : beregningsgrunnlag.get().getBeregningsgrunnlagPerioder()) {
                if (siste.getBeregningsgrunnlagPeriodeFom().isBefore(periode.getBeregningsgrunnlagPeriodeFom())) {
                    siste = periode;
                }
            }
            siste.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel -> {
                YtelseGrunnlagBuilder grunnlagBuilder = ytelseBuilder.getGrunnlagBuilder();
                ytelseBuilder.medYtelseGrunnlag(grunnlagBuilder.medDekningsgradProsent(getDekningsgrad(behandling))
                    .medYtelseStørrelse(grunnlagBuilder.getStørrelseBuilder()
                        .medBeløp(andel.getBruttoPrÅr())
                        .medVirksomhet(andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet).orElse(null))
                        .medHyppighet(InntektPeriodeType.ÅRLIG)
                        .build())
                    .build());
            });
        }
    }

    @SuppressWarnings("unused")
    protected BigDecimal getDekningsgrad(Behandling behandling) {
        // FIXME SP: Fjern?
        return BigDecimal.valueOf(100L);
    }

    private RelatertYtelseType map(FagsakYtelseType type) {
        if (FagsakYtelseType.FORELDREPENGER.equals(type)) {
            return RelatertYtelseType.FORELDREPENGER;
        }
        throw new IllegalStateException("Ukjent ytelsestype " + type);
    }

    private RelatertYtelseTilstand map(FagsakStatus kode) {
        RelatertYtelseTilstand typeKode;
        switch (kode.getKode()) {
            case "OPPR":
                typeKode = RelatertYtelseTilstand.IKKE_STARTET;
                break;
            case "UBEH":
                typeKode = RelatertYtelseTilstand.ÅPEN;
                break;
            case "LOP":
                typeKode = RelatertYtelseTilstand.LØPENDE;
                break;
            case "AVSLU":
                typeKode = RelatertYtelseTilstand.AVSLUTTET;
                break;
            default:
                typeKode = RelatertYtelseTilstand.ÅPEN;
        }
        return typeKode;
    }

    private DatoIntervallEntitet hentPeriodeFraUttak(UttakResultatEntitet uttakResultatPlan) {
        final LocalDate tom = uttakResultatPlan.getGjeldendePerioder().getPerioder().stream()
            .filter(p -> PeriodeResultatType.INNVILGET.equals(p.getPeriodeResultatType()))
            .max(Comparator.comparing(UttakResultatPeriodeEntitet::getTom)).get().getTom();
        return DatoIntervallEntitet.fraOgMedTilOgMed(
            uttakResultatPlan.getGjeldendePerioder().getPerioder().stream().filter(p -> PeriodeResultatType.INNVILGET.equals(p.getPeriodeResultatType()))
                .min(Comparator.comparing(UttakResultatPeriodeEntitet::getFom)).get().getFom(),
            tom);
    }
}
