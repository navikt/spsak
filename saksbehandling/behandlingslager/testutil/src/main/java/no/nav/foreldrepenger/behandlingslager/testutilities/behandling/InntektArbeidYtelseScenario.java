package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.EgenNæring;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.util.FPDateUtil;

public class InntektArbeidYtelseScenario {

    private final Map<Behandling, InntektArbeidYtelseGrunnlag> opptjeningAggregatMap = new IdentityHashMap<>();
    private InntektArbeidYtelseScenarioTestBuilder inntektArbeidYtelseScenarioTestBuilder;
    private OppgittOpptjeningBuilder oppgittOpptjeningBuilder;

    static VirksomhetRepository mockVirksomhetRepository() {
        return mock(VirksomhetRepository.class);
    }

    public InntektArbeidYtelseAggregatBuilder medDefaultInntektArbeidYtelse() {
        inntektArbeidYtelseScenarioTestBuilder = getInntektArbeidYtelseScenarioTestBuilder();
        inntektArbeidYtelseScenarioTestBuilder.build();
        return inntektArbeidYtelseScenarioTestBuilder.inntektArbeidYtelseAggregatBuilder;
    }

    public InntektArbeidYtelseScenarioTestBuilder getInntektArbeidYtelseScenarioTestBuilder() {
        if (inntektArbeidYtelseScenarioTestBuilder == null) {
            inntektArbeidYtelseScenarioTestBuilder = getInntektArbeidYtelseScenarioTestBuilder(InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER));
        }
        return inntektArbeidYtelseScenarioTestBuilder;
    }


    public void removeDodgyDefaultInntektArbeidYTelse() {
        this.inntektArbeidYtelseScenarioTestBuilder = null;
    }

    public InntektArbeidYtelseScenarioTestBuilder getInntektArbeidYtelseScenarioTestBuilder(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        return InntektArbeidYtelseScenarioTestBuilder.ny(inntektArbeidYtelseAggregatBuilder);
    }

    void lagreVirksomhet(BehandlingRepositoryProvider repositoryProvider) {
        InntektArbeidYtelseAggregatBuilder kladd = getInntektArbeidYtelseScenarioTestBuilder().getKladd();
        if (kladd != null) {
            InntektArbeidYtelseAggregat build = kladd.build();
            build.getAktørArbeid().stream()
                .map(AktørArbeid::getYrkesaktiviteter)
                .flatMap(java.util.Collection::stream)
                .forEach(yr -> {
                    if (yr.getArbeidsgiver().getErVirksomhet()) {
                        final Optional<Virksomhet> hent = repositoryProvider.getVirksomhetRepository().hentForEditering(yr.getArbeidsgiver().getIdentifikator());
                        if (hent.isPresent()) {
                            try {
                                Method m = YrkesaktivitetEntitet.class.getDeclaredMethod("setArbeidsgiver", Arbeidsgiver.class);
                                m.setAccessible(true);
                                m.invoke(yr, Arbeidsgiver.virksomhet(hent.get()));
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                throw new IllegalArgumentException("Utvikler feil");
                            }
                        }
                        repositoryProvider.getVirksomhetRepository().lagre(yr.getArbeidsgiver().getVirksomhet());
                    }
                });
        }
    }

    void lagreOpptjening(BehandlingRepositoryProvider repositoryProvider, Behandling behandling) {
        InntektArbeidYtelseAggregatBuilder kladd = getInntektArbeidYtelseScenarioTestBuilder().getKladd();
        if (kladd != null) {
            repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, kladd);
        }
    }

    public OppgittOpptjeningBuilder medOppgittOpptjening(OppgittOpptjeningBuilder oppgittOpptjeningBuilder) {
        this.oppgittOpptjeningBuilder = oppgittOpptjeningBuilder;
        return oppgittOpptjeningBuilder;
    }

    void lagreOppgittOpptjening(BehandlingRepositoryProvider repositoryProvider, Behandling behandling) {
        if (oppgittOpptjeningBuilder != null) {
            OppgittOpptjening oppgittOpptjening = oppgittOpptjeningBuilder.build();
            oppgittOpptjening.getOppgittArbeidsforhold().stream()
                .filter(oppgittArbeidsforhold -> oppgittArbeidsforhold.getVirksomhet() != null)
                .map(OppgittArbeidsforhold::getVirksomhet)
                .forEach(virksomhet -> repositoryProvider.getVirksomhetRepository().lagre(virksomhet));

            oppgittOpptjening.getEgenNæring().stream()
                .filter(egenNæring -> egenNæring.getVirksomhet() != null)
                .map(EgenNæring::getVirksomhet)
                .forEach(virksomhet -> repositoryProvider.getVirksomhetRepository().lagre(virksomhet));

            repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, oppgittOpptjeningBuilder);
        }
    }

    InntektArbeidYtelseRepository mockInntektArbeidYtelseRepository() {
        InntektArbeidYtelseRepository oRepo = mock(InntektArbeidYtelseRepository.class);

        Mockito.doAnswer(invocation -> {
            Behandling behandling = invocation.getArgument(0);
            InntektArbeidYtelseAggregatBuilder builder = invocation.getArgument(1);
            InntektArbeidYtelseGrunnlagBuilder aggregat = InntektArbeidYtelseGrunnlagBuilder.oppdatere(Optional.empty());
            aggregat.medData(builder);
            opptjeningAggregatMap.remove(behandling);
            opptjeningAggregatMap.put(behandling, aggregat.build());
            return null;
        }).when(oRepo)
            .lagre(Mockito.any(Behandling.class), Mockito.any(InntektArbeidYtelseAggregatBuilder.class));

        Mockito.doAnswer(invocation -> {
            Behandling behandling = invocation.getArgument(0);
            InntektArbeidYtelseGrunnlag aggregat = opptjeningAggregatMap.get(behandling);
            return Optional.ofNullable(aggregat);
        }).when(oRepo)
            .hentAggregatHvisEksisterer(Mockito.any(Behandling.class), Mockito.any());

        Mockito.doAnswer(invocation -> {
            Long behandlingId = invocation.getArgument(0);
            Optional<InntektArbeidYtelseGrunnlag> aggregat = opptjeningAggregatMap.entrySet().stream()
                .filter(e -> Objects.equals(behandlingId, e.getKey().getId()))
                .map(e -> e.getValue())
                .findFirst();
            return aggregat;
        }).when(oRepo)
            .hentAggregatHvisEksisterer(Mockito.anyLong(), Mockito.any());

        Mockito.doAnswer(invocation -> {
            Behandling behandling = invocation.getArgument(0);
            return opptjeningAggregatMap.get(behandling);
        }).when(oRepo)
            .hentAggregat(Mockito.any(Behandling.class), Mockito.any());

        return oRepo;
    }

    public static class InntektArbeidYtelseScenarioTestBuilder {
        private InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder;

        // Permisjon
        private LocalDate permisjonFom = LocalDate.now(FPDateUtil.getOffset()).minusWeeks(9L);
        private LocalDate permisjonTom = LocalDate.now(FPDateUtil.getOffset()).minusWeeks(2L);
        private BigDecimal permisjonsprosent = BigDecimal.valueOf(100);
        private PermisjonsbeskrivelseType permisjonsbeskrivelseType = PermisjonsbeskrivelseType.UDEFINERT;

        // AktivitetsAvtale
        private LocalDate aktivitetsAvtaleFom = LocalDate.now(FPDateUtil.getOffset()).minusYears(3L);
        private LocalDate aktivitetsAvtaleTom = LocalDate.now(FPDateUtil.getOffset());
        private BigDecimal aktivitetsAvtaleProsentsats = BigDecimal.TEN;
        private BigDecimal aktivitetsAvtaleAntallTimer = BigDecimal.valueOf(20.4d);
        private BigDecimal aktivitetsAvtaleAntallTimerFulltid = BigDecimal.valueOf(10.2d);

        // Virksomhet
        private String orgNavn = "EPLEHUSET AS";
        private String orgNr = "21542512";
        private LocalDate virksomhetRegistrert = LocalDate.now(FPDateUtil.getOffset()).minusYears(3L).minusYears(2L);
        private LocalDate virksomhetOppstart = LocalDate.now(FPDateUtil.getOffset()).minusYears(3L).minusYears(1L);
        private AktørId aktørId = new AktørId("100000");

        // Yrkesaktivitet
        private ArbeidType yrkesaktivitetArbeidType = ArbeidType.ORDINÆRT_ARBEIDSFORHOLD;
        private String yrkesaktivitetArbeidsforholdId = "2314234234";

        // Inntekt
        private InntektsKilde inntektsKilde = null;

        // Inntektspost
        private InntektspostType inntektspostType = InntektspostType.UDEFINERT;
        private BigDecimal inntektspostBeløp = BigDecimal.TEN;
        private LocalDate inntektspostFom = LocalDate.now(FPDateUtil.getOffset()).minusYears(3L);
        private LocalDate inntektspostTom = LocalDate.now(FPDateUtil.getOffset());

        // RelaterteYtelser
        private RelatertYtelseType ytelseType = null;
        private LocalDate iverksettelsesDato = LocalDate.now(FPDateUtil.getOffset()).minusYears(5L);
        private RelatertYtelseTilstand relatertYtelseTilstand = RelatertYtelseTilstand.AVSLUTTET;
        private TemaUnderkategori ytelseBehandlingstema = TemaUnderkategori.FORELDREPENGER_SVANGERSKAPSPENGER;
        private LocalDate tomDato;
        private Saksnummer saksnummer = new Saksnummer("00001");
        private Fagsystem ytelseKilde = Fagsystem.INFOTRYGD;

        private InntektArbeidYtelseScenarioTestBuilder(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
            this.inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseAggregatBuilder;
        }

        public static InntektArbeidYtelseScenarioTestBuilder ny(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
            return new InntektArbeidYtelseScenarioTestBuilder(inntektArbeidYtelseAggregatBuilder);
        }

        public InntektArbeidYtelseScenarioTestBuilder medAktørId(AktørId aktørId) {
            this.aktørId = aktørId;
            return this;
        }

        // Permisjon
        public InntektArbeidYtelseScenarioTestBuilder medPermisjonFom(LocalDate permisjonFom) {
            this.permisjonFom = permisjonFom;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medPermisjonTom(LocalDate permisjonTom) {
            this.permisjonTom = permisjonTom;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medPermisjonProsent(BigDecimal permisjonsprosent) {
            this.permisjonsprosent = permisjonsprosent;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType permisjonsbeskrivelseType) {
            this.permisjonsbeskrivelseType = permisjonsbeskrivelseType;
            return this;
        }

        // AktivitetsAvtale
        public InntektArbeidYtelseScenarioTestBuilder medAktivitetsAvtaleFom(LocalDate aktivitetsAvtaleFom) {
            this.aktivitetsAvtaleFom = aktivitetsAvtaleFom;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medAktivitetsAvtaleTom(LocalDate aktivitetsAvtaleTom) {
            this.aktivitetsAvtaleTom = aktivitetsAvtaleTom;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medAktivitetsAvtaleProsentsats(BigDecimal aktivitetsAvtaleProsentsats) {
            this.aktivitetsAvtaleProsentsats = aktivitetsAvtaleProsentsats;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medAktivitetsAvtaleAntallTimer(BigDecimal aktivitetsAvtaleAntallTimer) {
            this.aktivitetsAvtaleAntallTimer = aktivitetsAvtaleAntallTimer;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medAktivitetsAvtaleAntallTimerFulltid(BigDecimal aktivitetsAvtaleAntallTimerFulltid) {
            this.aktivitetsAvtaleAntallTimerFulltid = aktivitetsAvtaleAntallTimerFulltid;
            return this;
        }

        // Virksomhet
        public InntektArbeidYtelseScenarioTestBuilder medOrgNr(String orgNr) {
            this.orgNr = orgNr;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medOrgNavn(String orgNavn) {
            this.orgNavn = orgNavn;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medVirksomhetRegistrert(LocalDate virksomhetRegistrert) {
            this.virksomhetRegistrert = virksomhetRegistrert;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medVirksomhetOppstart(LocalDate virksomhetOppstart) {
            this.virksomhetOppstart = virksomhetOppstart;
            return this;
        }

        // Yrkesaktivitet
        public InntektArbeidYtelseScenarioTestBuilder medYrkesaktivitetArbeidType(ArbeidType yrkesaktivitetArbeidType) {
            this.yrkesaktivitetArbeidType = yrkesaktivitetArbeidType;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medYrkesaktivitetArbeidsforholdId(String yrkesaktivitetArbeidsforholdId) {
            this.yrkesaktivitetArbeidsforholdId = yrkesaktivitetArbeidsforholdId;
            return this;
        }

        // Inntektspost
        public InntektArbeidYtelseScenarioTestBuilder medInntektspostType(InntektspostType inntektspostType) {
            this.inntektspostType = inntektspostType;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medInntektspostBeløp(BigDecimal inntektspostBeløp) {
            this.inntektspostBeløp = inntektspostBeløp;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medInntektspostFom(LocalDate inntektspostFom) {
            this.inntektspostFom = inntektspostFom;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medInntektspostTom(LocalDate inntektspostTom) {
            this.inntektspostTom = inntektspostTom;
            return this;
        }

        // Inntekt
        public InntektArbeidYtelseScenarioTestBuilder medInntektsKilde(InntektsKilde inntektsKilde) {
            this.inntektsKilde = inntektsKilde;
            return this;
        }

        // Ytelse (YtelseType må settes)
        public InntektArbeidYtelseScenarioTestBuilder medYtelseType(RelatertYtelseType ytelseType) {
            this.ytelseType = ytelseType;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medYtelseTomDato(LocalDate tomDato) {
            this.tomDato = tomDato;
            return this;
        }

        public InntektArbeidYtelseScenarioTestBuilder medYtelseKilde(Fagsystem ytelseKilde) {
            this.ytelseKilde = ytelseKilde;
            return this;
        }

        public YtelseBuilder buildRelaterteYtelserGrunnlag(RelatertYtelseType ytelseType) {
            return YtelseBuilder.oppdatere(Optional.empty())
                .medKilde(ytelseKilde)
                .medSaksnummer(saksnummer)
                .medPeriode(
                    tomDato != null ? DatoIntervallEntitet.fraOgMedTilOgMed(iverksettelsesDato, tomDato) : DatoIntervallEntitet.fraOgMed(iverksettelsesDato))
                .medStatus(relatertYtelseTilstand)
                .medYtelseType(ytelseType)
                .medBehandlingsTema(ytelseBehandlingstema);
        }

        public InntektArbeidYtelseAggregatBuilder buildInntektGrunnlag() {

            InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);
            final Opptjeningsnøkkel opptjeningsnøkkel = new Opptjeningsnøkkel(yrkesaktivitetArbeidsforholdId, orgNr, aktørId.getId());
            AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektsKilde.INNTEKT_OPPTJENING, opptjeningsnøkkel);
            InntektEntitet.InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();
            if (inntektsKilde != null) {
                InntektEntitet.InntektspostBuilder inntektspost = inntektspostBuilder
                    .medBeløp(inntektspostBeløp)
                    .medPeriode(inntektspostFom, inntektspostTom)
                    .medInntektspostType(inntektspostType);

                inntektBuilder
                    .leggTilInntektspost(inntektspost)
                    .medInntektsKilde(inntektsKilde);

                InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = aktørInntektBuilder
                    .leggTilInntekt(inntektBuilder);

                inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntekt);
            }
            return inntektArbeidYtelseAggregatBuilder;
        }

        public InntektArbeidYtelseAggregatBuilder build() {
            InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);
            final Opptjeningsnøkkel opptjeningsnøkkel = new Opptjeningsnøkkel(yrkesaktivitetArbeidsforholdId, orgNr, aktørId.getId());
            AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektsKilde.INNTEKT_OPPTJENING, opptjeningsnøkkel);
            InntektEntitet.InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();

            InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(aktørId);
            YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsnøkkel,
                ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
            YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
            YrkesaktivitetEntitet.PermisjonBuilder permisjonBuilder = yrkesaktivitetBuilder.getPermisjonBuilder();

            InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(aktørId);

            Permisjon permisjon = permisjonBuilder
                .medProsentsats(permisjonsprosent)
                .medPeriode(permisjonFom, permisjonTom)
                .medPermisjonsbeskrivelseType(permisjonsbeskrivelseType)
                .build();

            YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
                .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(aktivitetsAvtaleFom, aktivitetsAvtaleTom))
                .medProsentsats(aktivitetsAvtaleProsentsats)
                .medAntallTimer(aktivitetsAvtaleAntallTimer)
                .medAntallTimerFulltid(aktivitetsAvtaleAntallTimerFulltid);

            YrkesaktivitetEntitet.AktivitetsAvtaleBuilder ansettelsesperiode = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
                .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(aktivitetsAvtaleFom, aktivitetsAvtaleTom));

            Virksomhet virksomhet = new VirksomhetEntitet.Builder()
                .medOrgnr(orgNr)
                .medNavn(orgNavn)
                .medRegistrert(virksomhetRegistrert)
                .medOppstart(virksomhetOppstart)
                .oppdatertOpplysningerNå()
                .build();

            Yrkesaktivitet yrkesaktivitet = yrkesaktivitetBuilder
                .medArbeidType(yrkesaktivitetArbeidType)
                .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
                .medArbeidsforholdId(ArbeidsforholdRef.ref(yrkesaktivitetArbeidsforholdId))
                .tilbakestillAvtaler()
                .leggTilAktivitetsAvtale(aktivitetsAvtale)
                .leggTilAktivitetsAvtale(ansettelsesperiode)
                .tilbakestillPermisjon()
                .leggTilPermisjon(permisjon)
                .build();

            InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
                .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

            if (inntektsKilde != null) {
                InntektEntitet.InntektspostBuilder inntektspost = inntektspostBuilder
                    .medBeløp(inntektspostBeløp)
                    .medPeriode(inntektspostFom, inntektspostTom)
                    .medInntektspostType(inntektspostType);

                inntektBuilder
                    .leggTilInntektspost(inntektspost)
                    .medArbeidsgiver(yrkesaktivitet.getArbeidsgiver())
                    .medInntektsKilde(inntektsKilde);

                InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = aktørInntektBuilder
                    .leggTilInntekt(inntektBuilder);

                inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntekt);
            }

            if (ytelseType != null) {
                aktørYtelseBuilder.leggTilYtelse(buildRelaterteYtelserGrunnlag(ytelseType));
                inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
            }

            inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeid);
            return inntektArbeidYtelseAggregatBuilder;
        }

        /**
         * Gir den rå buildern for å videre manipulere testdata. på samme måte som entitene bygges på.
         *
         * @return buildern
         */
        public InntektArbeidYtelseAggregatBuilder getKladd() {
            return inntektArbeidYtelseAggregatBuilder;
        }

    }
}
