package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.UttakStillingsprosentTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakBeregningsandelTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedArbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedFulltArbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class FastsettePerioderRegelGrunnlagByggerImplTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste = new RelatertBehandlingTjenesteImpl(repositoryProvider);
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
    private String AREBIDSFORHOLD_ID1 = "12001";

    @Test
    public void oppgittPeriodeSkalFåRiktigArbeidsprosent() {
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", AREBIDSFORHOLD_ID1);

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        BigDecimal arbeidsprosentFraSøknad = BigDecimal.valueOf(60);
        BigDecimal arbeidsprosentFraInntektsmelding = BigDecimal.valueOf(30);
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medArbeidsprosent(arbeidsprosentFraSøknad)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .build();

        Permisjon permisjon = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(BigDecimal.valueOf(40))
            .medPeriode(fom, tom)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        Behandling behandling = setup(oppgittPeriode, virksomhet, Collections.singletonList(permisjon), BigDecimal.valueOf(100), aktivitet.getArbeidsforholdId());
        Inntektsmelding inntektsmeldingMedGradering = lagreInntektsmeldingMedGradering(behandling, Arbeidsgiver.virksomhet(virksomhet), aktivitet.getArbeidsforholdId(),
            fom, tom, arbeidsprosentFraInntektsmelding);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmeldingMedGradering.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getUttakPerioder()).hasSize(1);
        UttakPeriode uttakPeriode = grunnlag.getUttakPerioder()[0];
        assertThat(uttakPeriode.getGradertArbeidsprosent()).isEqualTo(arbeidsprosentFraSøknad);
    }

    @Test
    public void arbeidstidsprosentSkalSettesBasertPåPermisjonHvisIkkeGradering() {
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", AREBIDSFORHOLD_ID1);

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        Permisjon permisjon = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(BigDecimal.valueOf(40))
            .medPeriode(fom, tom.minusDays(5))
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        BigDecimal stillingsprosent = BigDecimal.valueOf(100);
        Behandling behandling = setup(oppgittPeriode, virksomhet, Collections.singletonList(permisjon), stillingsprosent, aktivitet.getArbeidsforholdId());
        Inntektsmelding inntektsmelding = lagreInntektsmelding(behandling, Arbeidsgiver.virksomhet(virksomhet));

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmelding.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getArbeidsprosenter().getArbeidsprosent(AktivitetIdentifikator.forArbeid(aktivitet.getOrgNr(), null),
            new LukketPeriode(fom, tom.minusDays(5)))).isEqualTo(new BigDecimal("60.00"));
        //Resten av søknadsperioden som ikke er har permisjon
        assertThat(grunnlag.getArbeidsprosenter().getArbeidsprosent(AktivitetIdentifikator.forArbeid(aktivitet.getOrgNr(), null),
            new LukketPeriode(tom.minusDays(4), tom))).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void arbeidstidsprosent_over_100_prosent_skal_reduseres_til_100_prosent() {
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", AREBIDSFORHOLD_ID1);

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        Permisjon permisjon = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(BigDecimal.valueOf(140))
            .medPeriode(fom, tom)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        BigDecimal stillingsprosent = BigDecimal.valueOf(100);
        Behandling behandling = setup(oppgittPeriode, virksomhet, Collections.singletonList(permisjon), stillingsprosent, aktivitet.getArbeidsforholdId());
        Inntektsmelding inntektsmelding = lagreInntektsmelding(behandling, Arbeidsgiver.virksomhet(virksomhet));

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmelding.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getArbeidsprosenter().getArbeidsprosent(AktivitetIdentifikator.forArbeid(aktivitet.getOrgNr(), null),
            new LukketPeriode(fom, tom))).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void arbeidstidsprosentSkalSettesBasertGraderingIInntektsmeldingSelvOmPermisjoner() {
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", AREBIDSFORHOLD_ID1);

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        Permisjon permisjon = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(BigDecimal.valueOf(40))
            .medPeriode(fom, tom.minusDays(5))
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        BigDecimal stillingsprosent = BigDecimal.valueOf(100);
        Behandling behandling = setup(oppgittPeriode, virksomhet, Collections.singletonList(permisjon), stillingsprosent, aktivitet.getArbeidsforholdId());
        BigDecimal inntektsmeldingGradering = BigDecimal.TEN;
        Inntektsmelding inntektsmelding = lagreInntektsmeldingMedGradering(behandling, Arbeidsgiver.virksomhet(virksomhet), aktivitet.getArbeidsforholdId(),
            fom, tom, inntektsmeldingGradering);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmelding.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getArbeidsprosenter().getArbeidsprosent(AktivitetIdentifikator.forArbeid(aktivitet.getOrgNr(), inntektsmelding.getArbeidsforholdRef().getReferanse()),
            new LukketPeriode(fom, tom))).isEqualTo(inntektsmeldingGradering);
    }

    @Test
    public void permisjonsprosentSkalSettesPåPerioderMedPermisjoner() {
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", AREBIDSFORHOLD_ID1);

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        BigDecimal prosentsatsPermisjon = BigDecimal.valueOf(40);
        Permisjon permisjon = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(prosentsatsPermisjon)
            .medPeriode(fom, tom.minusDays(5))
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        Behandling behandling = setup(oppgittPeriode, virksomhet, Collections.singletonList(permisjon), BigDecimal.valueOf(100), aktivitet.getArbeidsforholdId());
        Inntektsmelding inntektsmelding = lagreInntektsmelding(behandling, Arbeidsgiver.virksomhet(virksomhet));

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmelding.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getArbeidsprosenter().getPermisjonsprosent(AktivitetIdentifikator.forArbeid(aktivitet.getOrgNr(), null),
            new LukketPeriode(fom, tom.minusDays(5)))).isEqualTo(Optional.of(prosentsatsPermisjon));
        //Resten av søknadsperioden som ikke er har permisjon
        assertThat(grunnlag.getArbeidsprosenter().getPermisjonsprosent(AktivitetIdentifikator.forArbeid(aktivitet.getOrgNr(), null),
            new LukketPeriode(tom.minusDays(4), tom))).isNotPresent();
    }

    @Test
    public void permisjonsprosentSkalIkkeSettesPåPerioderUtenPermisjoner() {
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", AREBIDSFORHOLD_ID1);

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        Behandling behandling = setup(oppgittPeriode, virksomhet, Collections.emptyList(), BigDecimal.valueOf(100), aktivitet.getArbeidsforholdId());
        Inntektsmelding inntektsmelding = lagreInntektsmelding(behandling, Arbeidsgiver.virksomhet(virksomhet));

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmelding.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getArbeidsprosenter().getPermisjonsprosent(AktivitetIdentifikator.forArbeid(aktivitet.getOrgNr(), null),
            new LukketPeriode(fom, tom))).isNotPresent();
    }

    @Test
    public void arbeidstidsprosentSkalHentesFraInntektsmeldingHvisIkkeGraderingOgUtenPermisjon() {
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", AREBIDSFORHOLD_ID1);

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        Behandling behandling = setup(oppgittPeriode, virksomhet, Collections.emptyList(), BigDecimal.valueOf(60), aktivitet.getArbeidsforholdId());
        BigDecimal arbeidstidsprosentIInntektsmelding = new BigDecimal("50.55");
        Inntektsmelding inntektsmelding = lagreInntektsmeldingMedGradering(behandling, Arbeidsgiver.virksomhet(virksomhet),
            aktivitet.getArbeidsforholdId(), fom, tom, arbeidstidsprosentIInntektsmelding);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmelding.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getArbeidsprosenter().getArbeidsprosent(AktivitetIdentifikator.forArbeid(aktivitet.getOrgNr(),
            inntektsmelding.getArbeidsforholdRef().getReferanse()),
            new LukketPeriode(fom, tom))).isEqualTo(arbeidstidsprosentIInntektsmelding);
    }

    @Test
    public void arbeidstidsprosentSkalHenteSettesTil0HvisIkkeGraderingOgUtenPermisjonOgUtenGraderingIInntektsmelding() {
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", AREBIDSFORHOLD_ID1);

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        Behandling behandling = setup(oppgittPeriode, virksomhet, Collections.emptyList(), BigDecimal.valueOf(60), aktivitet.getArbeidsforholdId());
        Inntektsmelding inntektsmelding = lagreInntektsmelding(behandling, Arbeidsgiver.virksomhet(virksomhet));
        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmelding.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getArbeidsprosenter().getArbeidsprosent(AktivitetIdentifikator.forArbeid(aktivitet.getOrgNr(), null),
            new LukketPeriode(fom, tom))).isZero();
    }

    @Test
    public void permisjonsprosentIkkeSkalSettesPåPerioderMedPermisjonerHvisGradering() {
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", AREBIDSFORHOLD_ID1);

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medErArbeidstaker(true)
            .medArbeidsprosent(BigDecimal.TEN)
            .medVirksomhet(virksomhet)
            .build();

        Permisjon permisjon = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(BigDecimal.valueOf(40))
            .medPeriode(fom, tom)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        Behandling behandling = setup(oppgittPeriode, virksomhet, Collections.singletonList(permisjon), BigDecimal.valueOf(100), aktivitet.getArbeidsforholdId());
        Inntektsmelding inntektsmelding = lagreInntektsmeldingMedGradering(behandling, Arbeidsgiver.virksomhet(virksomhet), aktivitet.getArbeidsforholdId(),
            fom, tom, BigDecimal.TEN);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmelding.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getArbeidsprosenter().getPermisjonsprosent(AktivitetIdentifikator.forArbeid(aktivitet.getOrgNr(),
            inntektsmelding.getArbeidsforholdRef().getReferanse()), new LukketPeriode(fom, tom))).isNotPresent();
    }

    @Test
    public void permisjonsprosentIkkeSkalSettesPåPerioderMedPermisjonerHvisUtsettelse() {
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", AREBIDSFORHOLD_ID1);

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medErArbeidstaker(true)
            .medÅrsak(UtsettelseÅrsak.ARBEID)
            .build();

        Permisjon permisjon = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(BigDecimal.valueOf(40))
            .medPeriode(fom, tom)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        Behandling behandling = setup(oppgittPeriode, virksomhet, Collections.singletonList(permisjon), BigDecimal.valueOf(100), aktivitet.getArbeidsforholdId());
        Inntektsmelding inntektsmelding = lagreInntektsmelding(behandling, Arbeidsgiver.virksomhet(virksomhet));

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmelding.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getArbeidsprosenter().getPermisjonsprosent(AktivitetIdentifikator.forArbeid(aktivitet.getOrgNr(), null),
            new LukketPeriode(fom, tom))).isNotPresent();
    }

    @Test
    public void skalLeggeTilFlereGraderteAktiviteterIUttakPeriodeVedFlereGraderteAktiviterISammeVirksomhet() {
        String orgnr = "123123123";
        AktivitetIdentifikator aktivitet1 = AktivitetIdentifikator.forArbeid(orgnr, "123");
        AktivitetIdentifikator aktivitet2 = AktivitetIdentifikator.forArbeid(orgnr, "456");

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(orgnr);

        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medArbeidsprosent(arbeidsprosent)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .build();

        OppgittPeriode oppgittPeriode1 = OppgittPeriodeBuilder.ny()
            .medPeriode(tom.plusDays(1), tom.plusDays(4))
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        Behandling behandling = setup(Arrays.asList(oppgittPeriode, oppgittPeriode1), virksomhet, Collections.emptyList(), BigDecimal.valueOf(100),
            Arrays.asList(aktivitet1.getArbeidsforholdId(), aktivitet2.getArbeidsforholdId()));
        Inntektsmelding inntektsmelding1 = lagreInntektsmelding(behandling, Arbeidsgiver.virksomhet(virksomhet), aktivitet1.getArbeidsforholdId());
        Inntektsmelding inntektsmelding2 = lagreInntektsmelding(behandling, Arbeidsgiver.virksomhet(virksomhet), aktivitet2.getArbeidsforholdId());

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmelding1.getArbeidsforholdRef().getReferanse());
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmelding2.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getUttakPerioder()).hasSize(2);
        Optional<UttakPeriode> gradetUttakPeriode = finnGradertUttakPeriode(arbeidsprosent, grunnlag);
        assertThat(gradetUttakPeriode).isPresent();
        assertThat(gradetUttakPeriode.get().getGradertAktiviteter()).hasSize(2);
        assertThat(gradetUttakPeriode.get().getGradertAktiviteter().get(0).getOrgNr()).isEqualTo(orgnr);
        assertThat(gradetUttakPeriode.get().getGradertAktiviteter().get(1).getOrgNr()).isEqualTo(orgnr);
        assertThat(gradetUttakPeriode.get().getGradertAktiviteter().get(0).getArbeidsforholdId())
            .isNotEqualTo(gradetUttakPeriode.get().getGradertAktiviteter().get(1).getArbeidsforholdId());
    }

    @Test
    public void skalBareLeggeTilArbeidsforholdSomErGradertIInntektsmeldingenVedSøktGraderingOgAktiviterISammeVirksomhet() {
        String orgnr = "123123123";
        AktivitetIdentifikator aktivitet1 = AktivitetIdentifikator.forArbeid(orgnr, "123");
        AktivitetIdentifikator aktivitet2 = AktivitetIdentifikator.forArbeid(orgnr, "456");

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(orgnr);

        BigDecimal søknadArbeidstidsprosent = BigDecimal.valueOf(60);
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medArbeidsprosent(søknadArbeidstidsprosent)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .build();

        Behandling behandling = setup(Collections.singletonList(oppgittPeriode), virksomhet, Collections.emptyList(), BigDecimal.valueOf(100),
            Arrays.asList(aktivitet1.getArbeidsforholdId(), aktivitet2.getArbeidsforholdId()));
        Inntektsmelding inntektsmeldingMedGradering = lagreInntektsmeldingMedGradering(behandling, Arbeidsgiver.virksomhet(virksomhet), aktivitet1.getArbeidsforholdId(),
            fom, tom, søknadArbeidstidsprosent);
        Inntektsmelding inntektsmelding = lagreInntektsmelding(behandling, Arbeidsgiver.virksomhet(virksomhet), aktivitet2.getArbeidsforholdId());

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmeldingMedGradering.getArbeidsforholdRef().getReferanse());
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, inntektsmelding.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getAktiviteter()).hasSize(2);

        Optional<UttakPeriode> gradertUttakPeriode = finnGradertUttakPeriode(søknadArbeidstidsprosent, grunnlag);

        assertThat(gradertUttakPeriode).isPresent();
        assertThat(gradertUttakPeriode.get().getGradertAktiviteter()).hasSize(1);
        assertThat(gradertUttakPeriode.get().getGradertAktiviteter().get(0).getArbeidsforholdId()).isEqualTo(inntektsmeldingMedGradering.getArbeidsforholdRef().getReferanse());
        assertThat(gradertUttakPeriode.get().getGradertAktiviteter().get(0).getOrgNr()).isEqualTo(aktivitet1.getOrgNr());
        assertThat(gradertUttakPeriode.get().getGradertArbeidsprosent()).isEqualTo(søknadArbeidstidsprosent);
    }

    @Test
    public void skalLeggeTilAlleAktiviteterForVirksomhetHvisSøktOmGraderingMenBareGraderingIInntektsmeldingFraAnnenVirksomhet() {
        String orgnr1 = "123123123";
        String orgnr2 = "888888888";
        AktivitetIdentifikator aktivitet1 = AktivitetIdentifikator.forArbeid(orgnr1, "123");
        AktivitetIdentifikator aktivitet2 = AktivitetIdentifikator.forArbeid(orgnr1, "456");
        AktivitetIdentifikator aktivitetAnnen = AktivitetIdentifikator.forArbeid(orgnr2, "456");

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet1 = virksomhet(orgnr1);
        Virksomhet virksomhet2 = virksomhet(orgnr2);

        BigDecimal søknadArbeidstidsprosent = BigDecimal.valueOf(60);
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medArbeidsprosent(søknadArbeidstidsprosent)
            .medVirksomhet(virksomhet1)
            .medErArbeidstaker(true)
            .build();

        Behandling behandling = setupScenario(Collections.singletonList(oppgittPeriode));
        lagreYrkesAktiviter(behandling, Arbeidsgiver.virksomhet(virksomhet1), Arrays.asList(aktivitet1.getArbeidsforholdId(), aktivitet2.getArbeidsforholdId()),
            Collections.emptyList(), BigDecimal.valueOf(100));
        lagreYrkesAktiviter(behandling, Arbeidsgiver.virksomhet(virksomhet2), Collections.singletonList(aktivitetAnnen.getArbeidsforholdId()),
            Collections.emptyList(), BigDecimal.valueOf(100));
        Inntektsmelding inntektsmelding1 = lagreInntektsmelding(behandling, Arbeidsgiver.virksomhet(virksomhet1), aktivitet1.getArbeidsforholdId());
        Inntektsmelding inntektsmelding2 = lagreInntektsmelding(behandling, Arbeidsgiver.virksomhet(virksomhet1), aktivitet2.getArbeidsforholdId());

        Inntektsmelding inntektsmeldingMedGradering = lagreInntektsmeldingMedGradering(behandling, Arbeidsgiver.virksomhet(virksomhet2), aktivitetAnnen.getArbeidsforholdId(),
            fom, tom, søknadArbeidstidsprosent);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet1, inntektsmelding1.getArbeidsforholdRef().getReferanse());
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet1, inntektsmelding2.getArbeidsforholdRef().getReferanse());
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet2, inntektsmeldingMedGradering.getArbeidsforholdRef().getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getAktiviteter()).hasSize(3);

        Optional<UttakPeriode> gradertUttakPeriode = finnGradertUttakPeriode(søknadArbeidstidsprosent, grunnlag);

        assertThat(gradertUttakPeriode).isPresent();
        assertThat(gradertUttakPeriode.get().getGradertAktiviteter()).hasSize(2);
        assertThat(gradertUttakPeriode.get().getGradertAktiviteter().get(0).getOrgNr()).isEqualTo(aktivitet1.getOrgNr());
        assertThat(gradertUttakPeriode.get().getGradertAktiviteter().get(1).getOrgNr()).isEqualTo(aktivitet2.getOrgNr());
        assertThat(gradertUttakPeriode.get().getGradertArbeidsprosent()).isEqualTo(søknadArbeidstidsprosent);
    }

    private Optional<UttakPeriode> finnGradertUttakPeriode(BigDecimal arbeidsprosent, FastsettePeriodeGrunnlag grunnlag) {
        for (UttakPeriode uttakPeriode : grunnlag.getUttakPerioder()) {
            if (arbeidsprosent.equals(uttakPeriode.getGradertArbeidsprosent())) {
                return Optional.of(uttakPeriode);
            }
        }
        return Optional.empty();
    }

    @Test
    public void skalLeggeTilGradertFrilansUtenOrgnrISøknadSomGradertPeriode() {
        String orgnr = "123456789";

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(orgnr);

        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medArbeidsprosent(arbeidsprosent)
            .medErArbeidstaker(false)
            .build();

        Behandling behandling = setupFrilans(oppgittPeriode, virksomhet);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilFrilans();
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getUttakPerioder()).hasSize(1);
        assertThat(grunnlag.getUttakPerioder()[0].getGradertArbeidsprosent()).isEqualTo(arbeidsprosent);
        assertThat(grunnlag.getUttakPerioder()[0].getGradertAktiviteter().get(0).getAktivitetType()).isEqualTo(AktivitetType.FRILANS);
    }

    @Test
    public void gradertArbeidsforholdArbeidstakerMedSamtUgradertFrilansSkalBareGiEnGradertAktivitet() {
        String orgnr = "123456789";

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(orgnr);

        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medArbeidsprosent(arbeidsprosent)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .build();

        Behandling behandling = setupScenario(Collections.singletonList(oppgittPeriode));

        String arbeidsforholdId = "adwa";
        InntektArbeidYtelseAggregatBuilder builder = lagYrkesAktiviter(behandling, Arbeidsgiver.virksomhet(virksomhet),
            Collections.singletonList(arbeidsforholdId), Collections.emptyList(), BigDecimal.valueOf(100));


        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = lagYrkesAktivitetForArbeidsforhold(arbeidsgiver, Collections.emptyList(),
            BigDecimal.valueOf(100), LocalDate.now().minusWeeks(1), LocalDate.now().plusMonths(1), arbeidsforholdId, ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        builder.leggTilAktørArbeid(aktørArbeidBuilder);

        InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        inntektArbeidYtelseRepository.lagre(behandling, builder);
        ArbeidsforholdRef arbeidsforholdRef = getArbeidsforholdRef(behandling, arbeidsforholdId, arbeidsgiver, inntektArbeidYtelseRepository);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, arbeidsforholdRef.getReferanse());
        beregningsandelTjeneste.leggTilFrilans();
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getUttakPerioder()).hasSize(1);
        assertThat(grunnlag.getUttakPerioder()[0].getGradertArbeidsprosent()).isEqualTo(arbeidsprosent);
        assertThat(grunnlag.getUttakPerioder()[0].getGradertAktiviteter()).hasSize(1);
        assertThat(grunnlag.getUttakPerioder()[0].getGradertAktiviteter().get(0).getAktivitetType()).isEqualTo(AktivitetType.ARBEID);
        assertThat(grunnlag.getUttakPerioder()[0].getGradertAktiviteter().get(0).getOrgNr()).isEqualTo(orgnr);
    }

    //Siden søknad har frilans/selvstendig næringsdrivende i samme kategori kan vi ikke skille på hvilken som skal graderes
    //Velger nå å sette gradering på selvstendig næringsdrivende for å så fikse etter utbyggeren
    @Test
    public void søkerHarBådeFrilansOgSelvstendigArbeidsforholdOgSøkerGradering() {
        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        String selvstendigNæringsdrivendeOrgnr = "3456";
        Behandling behandling = setupFrilansOgSelvstendigNæringsdrivende(arbeidsprosent, selvstendigNæringsdrivendeOrgnr);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilSelvNæringdrivende(virksomhet(selvstendigNæringsdrivendeOrgnr));
        beregningsandelTjeneste.leggTilFrilans();
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getUttakPerioder()).hasSize(1);
        assertThat(grunnlag.getUttakPerioder()[0].getGradertArbeidsprosent()).isEqualTo(arbeidsprosent);
        assertThat(grunnlag.getUttakPerioder()[0].getGradertAktiviteter()).hasSize(1);
        assertThat(grunnlag.getUttakPerioder()[0].getGradertAktiviteter().get(0).getAktivitetType()).isEqualTo(AktivitetType.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    private Behandling setupFrilansOgSelvstendigNæringsdrivende(BigDecimal arbeidsprosent, String selvstendigNæringsdrivendeOrgnr) {
        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medArbeidsprosent(arbeidsprosent)
            .medErArbeidstaker(false)
            .build();

        OppgittOpptjeningBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningBuilder.EgenNæringBuilder.ny()
            .medVirksomhet(virksomhet(selvstendigNæringsdrivendeOrgnr))
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom));
        OppgittOpptjeningBuilder oppgittOpptjeningBuilder = OppgittOpptjeningBuilder.ny()
            .leggTilEgneNæringer(Collections.singletonList(egenNæringBuilder));

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();

        scenario.medOppgittOpptjening(oppgittOpptjeningBuilder);
        scenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriode), true));
        scenario.medOppgittRettighet(new OppgittRettighetEntitet(true, true, false));
        scenario.medBekreftetHendelse().medFødselsDato(LocalDate.now().minusWeeks(2));
        scenario.medBehandlingVedtak()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("saksbehandler")
            .medVedtaksdato(LocalDate.now());

        Behandling behandling = scenario.lagre(repositoryProvider);
        lagreUttaksperiodegrense(behandling, repositoryProvider.getUttakRepository());
        lagreStønadskontoer(behandling, repositoryProvider.getFagsakRelasjonRepository());

        InntektArbeidYtelseAggregatBuilder builder = lagYrkesAktivitFrilans(behandling, Arbeidsgiver.virksomhet(virksomhet("1234")));

        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);
        return behandling;
    }

    @Test
    public void skalIkkeHaPeriodeMedFulltEllerIArbeidSidenFullPermisjon() {

        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", AREBIDSFORHOLD_ID1);

        LocalDate fom = LocalDate.now().minusWeeks(1);
        LocalDate tom = LocalDate.now().plusMonths(1);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        Permisjon permisjon = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(BigDecimal.valueOf(100))
            .medPeriode(fom, tom)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        Behandling behandling = setup(oppgittPeriode, virksomhet, Collections.singletonList(permisjon), BigDecimal.valueOf(50), aktivitet.getArbeidsforholdId());

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, AREBIDSFORHOLD_ID1);
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getPerioderMedFulltArbeid()).hasSize(0);
        assertThat(grunnlag.getPerioderMedArbeid()).hasSize(0);
    }

    @Test
    public void skalHaPeriodeIArbeidSidenHalvPermisjon() {

        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", null);

        LocalDate fom = LocalDate.now().minusWeeks(1);
        LocalDate tom = LocalDate.now().plusMonths(1);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        BigDecimal arbeidsprosent = BigDecimal.valueOf(50);
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medArbeidsprosent(arbeidsprosent)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .build();


        LocalDate permisjonTom1 = LocalDate.now();
        Permisjon permisjon1 = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(BigDecimal.valueOf(50))
            .medPeriode(fom, permisjonTom1)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        LocalDate permisjonFom2 = permisjonTom1.plusDays(1);
        Permisjon permisjon2 = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(BigDecimal.valueOf(40))
            .medPeriode(permisjonFom2, tom)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        Behandling behandling = setup(oppgittPeriode, virksomhet, Arrays.asList(permisjon1, permisjon2), BigDecimal.valueOf(50), aktivitet.getArbeidsforholdId());

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, null);
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        PeriodeMedFulltArbeid[] fulltArbeid = grunnlag.getPerioderMedFulltArbeid();
        assertThat(fulltArbeid).hasSize(0);
        PeriodeMedArbeid[] medArbeid = grunnlag.getPerioderMedArbeid();
        assertThat(medArbeid).hasSize(2);
        assertThat(medArbeid[0].getFom()).isEqualTo(fom);
        assertThat(medArbeid[0].getTom()).isEqualTo(permisjonTom1);
        assertThat(medArbeid[1].getFom()).isEqualTo(permisjonFom2);
        assertThat(medArbeid[1].getTom()).isEqualTo(tom);

    }

    @Test
    public void skalHaPeriodeMedFulltArbeidSidenUtsettePermisjon() {

        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("123123123", null);

        LocalDate fom = LocalDate.now().minusWeeks(1);
        LocalDate tom = LocalDate.now().plusMonths(1);
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medÅrsak(UtsettelseÅrsak.ARBEID)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .build();

        Behandling behandling = setup(oppgittPeriode, virksomhet, Collections.emptyList(),
            BigDecimal.valueOf(100), aktivitet.getArbeidsforholdId());

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, aktivitet.getArbeidsforholdId());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        PeriodeMedFulltArbeid[] fulltArbeid = grunnlag.getPerioderMedFulltArbeid();
        assertThat(fulltArbeid).hasSize(1);
        assertThat(fulltArbeid[0].getFom()).isEqualTo(fom);
        assertThat(fulltArbeid[0].getTom()).isEqualTo(tom);
        assertThat(grunnlag.getPerioderMedArbeid()).hasSize(0);
    }

    @Test
    public void mapperAnnenPartsUttaksperioder() {
        Repository repository = repositoryRule.getRepository();

        // Arrange - mors behandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Behandling morsBehandling = scenario.lagre(repositoryProvider);
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();

        LocalDate start = LocalDate.of(2018, 5, 14);

        // Uttak periode 1
        UttakResultatPeriodeEntitet uttakMødrekvote = new UttakResultatPeriodeEntitet.Builder(start, start.plusWeeks(6).minusDays(1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();

        Virksomhet morsVirksomhet = virksomhet("3333");
        repository.lagre(morsVirksomhet);
        UttakAktivitetEntitet arbeidsforhold1 = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID).medArbeidsforhold((VirksomhetEntitet) morsVirksomhet, ArbeidsforholdRef.ref("1111"))
            .build();

        UttakResultatPeriodeAktivitetEntitet.builder(uttakMødrekvote, arbeidsforhold1)
            .medTrekkdager(30)
            .medTrekkonto(StønadskontoType.MØDREKVOTE)
            .medUtbetalingsprosent(BigDecimal.TEN)
            .medArbeidsprosent(BigDecimal.ZERO).build();

        perioder.leggTilPeriode(uttakMødrekvote);

        // Uttak periode 2
        UttakResultatPeriodeEntitet uttakFellesperiode = new UttakResultatPeriodeEntitet.Builder(start.plusWeeks(6), start.plusWeeks(10).minusDays(1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();

        UttakResultatPeriodeAktivitetEntitet.builder(uttakFellesperiode, arbeidsforhold1)
            .medTrekkdager(20)
            .medTrekkonto(StønadskontoType.FELLESPERIODE)
            .medUtbetalingsprosent(BigDecimal.TEN)
            .medArbeidsprosent(BigDecimal.valueOf(10)).build();

        perioder.leggTilPeriode(uttakFellesperiode);

        Behandlingsresultat behandlingsresultat = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repository.lagre(behandlingsresultat);

        repositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(morsBehandling, perioder);

        morsBehandling.avsluttBehandling();
        repository.lagre(morsBehandling);

        lagreStønadskontoer(morsBehandling, repositoryProvider.getFagsakRelasjonRepository());

        // Arrange - fars behandling
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid("1111", "2");
        Virksomhet virksomhet = virksomhet(aktivitet.getOrgNr());
        OppgittPeriode uttakFPFar = OppgittPeriodeBuilder.ny()
            .medPeriode(start.plusWeeks(10), start.plusWeeks(20).minusDays(1))
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medArbeidsprosent(BigDecimal.ZERO)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .build();

        ScenarioFarSøkerForeldrepenger scenarioFarSøkerForeldrepenger = ScenarioFarSøkerForeldrepenger.forFødsel()
            .medFordeling(new OppgittFordelingEntitet(Collections.singletonList(uttakFPFar), true));
        scenarioFarSøkerForeldrepenger.medBekreftetHendelse().medFødselsDato(start);
        scenarioFarSøkerForeldrepenger.medDefaultInntektArbeidYtelse();
        scenarioFarSøkerForeldrepenger.medOppgittRettighet(new OppgittRettighetEntitet(true, true, false));
        Behandling farsBehandling = scenarioFarSøkerForeldrepenger.lagre(repositoryProvider);
        repository.lagre(farsBehandling.getBehandlingsresultat());

        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(morsBehandling.getFagsak(), farsBehandling.getFagsak());

        lagreUttaksperiodegrense(farsBehandling, repositoryProvider.getUttakRepository());
        lagreYrkesAktiviter(farsBehandling, Arbeidsgiver.virksomhet(virksomhet),
            Collections.singletonList(aktivitet.getArbeidsforholdId()), Collections.emptyList(), BigDecimal.valueOf(100));
        repository.flushAndClear();

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, aktivitet.getArbeidsforholdId());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);
        // Act
        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(farsBehandling);

        // Assert
        AktivitetIdentifikator forventetAktivitetIdentifikator = AktivitetIdentifikator.forArbeid(arbeidsforhold1.getArbeidsforholdOrgnr(), arbeidsforhold1.getArbeidsforholdId());

        List<FastsattPeriodeAnnenPart> uttakPerioderAnnenPart = grunnlag.getTrekkdagertilstand().getUttakPerioderAnnenPart();
        assertThat(uttakPerioderAnnenPart).hasSize(2);

        FastsattPeriodeAnnenPart mappedMødreKvote = uttakPerioderAnnenPart.get(0);
        assertThat(mappedMødreKvote.getFom()).isEqualTo(uttakMødrekvote.getFom());
        assertThat(mappedMødreKvote.getTom()).isEqualTo(uttakMødrekvote.getTom());
        assertThat(mappedMødreKvote.getUttakPeriodeAktiviteter()).hasSize(1);

        UttakPeriodeAktivitet aktivitetMødrekvote = mappedMødreKvote.getUttakPeriodeAktiviteter().get(0);
        assertThat(aktivitetMødrekvote.getAktivitetIdentifikator()).isEqualTo(forventetAktivitetIdentifikator);
        assertThat(aktivitetMødrekvote.getGradertArbeidsprosent()).isEqualTo(uttakMødrekvote.getAktiviteter().get(0).getArbeidsprosent());
        assertThat(aktivitetMødrekvote.getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(aktivitetMødrekvote.getTrekkdager()).isEqualTo(uttakMødrekvote.getAktiviteter().get(0).getTrekkdager());

        FastsattPeriodeAnnenPart mappedFellesperiode = uttakPerioderAnnenPart.get(1);
        assertThat(mappedFellesperiode.getFom()).isEqualTo(uttakFellesperiode.getFom());
        assertThat(mappedFellesperiode.getTom()).isEqualTo(uttakFellesperiode.getTom());
        assertThat(mappedFellesperiode.getUttakPeriodeAktiviteter()).hasSize(1);

        UttakPeriodeAktivitet aktivitetFellesperiode = mappedFellesperiode.getUttakPeriodeAktiviteter().get(0);
        assertThat(aktivitetFellesperiode.getAktivitetIdentifikator()).isEqualTo(forventetAktivitetIdentifikator);
        assertThat(aktivitetFellesperiode.getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(aktivitetFellesperiode.getGradertArbeidsprosent()).isEqualTo(uttakFellesperiode.getAktiviteter().get(0).getArbeidsprosent());
        assertThat(aktivitetFellesperiode.getTrekkdager()).isEqualTo(uttakFellesperiode.getAktiviteter().get(0).getTrekkdager());
    }

    @Test
    public void testeSamtidigUttak() {
        String orgnr1 = "123123123";
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid(orgnr1, "123");

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(10);
        Virksomhet virksomhet = virksomhet(orgnr1);

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .medSamtidigUttak(true)
            .medSamtidigUttaksprosent(BigDecimal.TEN)
            .build();

        Behandling behandling = setupScenario(Collections.singletonList(oppgittPeriode));
        lagreYrkesAktiviter(behandling, Arbeidsgiver.virksomhet(virksomhet), Collections.singletonList(aktivitet.getArbeidsforholdId()),
            Collections.emptyList(), BigDecimal.valueOf(100));
        lagreInntektsmelding(behandling, Arbeidsgiver.virksomhet(virksomhet), aktivitet.getArbeidsforholdId());

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, aktivitet.getArbeidsforholdId());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        assertThat(grunnlag.getAktiviteter()).hasSize(1);

        UttakPeriode[] uttakPerioder = grunnlag.getUttakPerioder();

        assertThat(uttakPerioder).isNotEmpty();
        assertThat(uttakPerioder[0].isSamtidigUttak()).isTrue();
    }

    @Test
    public void skalHaPeriodeIFulltArbeidMedFlereArbeidsforhold() {

        AktivitetIdentifikator aktivitet1 = AktivitetIdentifikator.forArbeid("123123123", "123");
        AktivitetIdentifikator aktivitet2 = AktivitetIdentifikator.forArbeid("123123123", "456");

        LocalDate fom = LocalDate.now().minusWeeks(1);
        LocalDate tom = LocalDate.now().plusMonths(1);
        Virksomhet virksomhet = virksomhet(aktivitet1.getOrgNr());

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        Permisjon permisjon1 = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(BigDecimal.valueOf(1))
            .medPeriode(fom, tom)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        Permisjon permisjon2 = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(BigDecimal.valueOf(10))
            .medPeriode(fom, tom)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        Behandling behandling = setupScenario(Collections.singletonList(oppgittPeriode));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        lagreYrkesAktiviter(behandling, arbeidsgiver, Collections.singletonList(aktivitet1.getArbeidsforholdId()),
            Collections.singletonList(permisjon1), BigDecimal.valueOf(100));
        lagreYrkesAktiviter(behandling, arbeidsgiver, Collections.singletonList(aktivitet2.getArbeidsforholdId()),
            Collections.singletonList(permisjon2), BigDecimal.valueOf(20));
        lagreInntektsmelding(behandling, arbeidsgiver, aktivitet1.getArbeidsforholdId());
        lagreInntektsmelding(behandling, arbeidsgiver, aktivitet2.getArbeidsforholdId());

        ArbeidsforholdRef arbeidsforholdRef1 = getArbeidsforholdRef(behandling, aktivitet1.getArbeidsforholdId(), arbeidsgiver, repositoryProvider.getInntektArbeidYtelseRepository());
        ArbeidsforholdRef arbeidsforholdRef2 = getArbeidsforholdRef(behandling, aktivitet2.getArbeidsforholdId(), arbeidsgiver, repositoryProvider.getInntektArbeidYtelseRepository());

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, arbeidsforholdRef1.getReferanse());
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, arbeidsforholdRef2.getReferanse());
        FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger = grunnlagBygger(beregningsandelTjeneste);

        FastsettePeriodeGrunnlag grunnlag = grunnlagBygger.byggGrunnlag(behandling);

        PeriodeMedFulltArbeid[] fulltArbeid = grunnlag.getPerioderMedFulltArbeid();
        assertThat(fulltArbeid).hasSize(1);
        assertThat(fulltArbeid[0].getFom()).isEqualTo(fom);
        assertThat(fulltArbeid[0].getTom()).isEqualTo(tom);
        PeriodeMedArbeid[] medArbeid = grunnlag.getPerioderMedArbeid();
        assertThat(medArbeid).hasSize(0);
    }

    private Behandling setup(OppgittPeriode oppgittPeriode,
                             Virksomhet virksomhet,
                             List<Permisjon> permisjonList,
                             BigDecimal stillingsprosent,
                             String arbeidsforholdId) {
        return setup(Collections.singletonList(oppgittPeriode), virksomhet, permisjonList, stillingsprosent, Collections.singletonList(arbeidsforholdId));
    }

    private Behandling setup(List<OppgittPeriode> oppgittPeriode,
                             Virksomhet virksomhet,
                             List<Permisjon> permisjonList,
                             BigDecimal stillingsprosent,
                             List<String> arbeidsforholdIdList) {
        Behandling behandling = setupScenario(oppgittPeriode);
        lagreYrkesAktiviter(behandling, Arbeidsgiver.virksomhet(virksomhet), arbeidsforholdIdList, permisjonList, stillingsprosent);
        return behandling;
    }

    private Behandling setupFrilans(OppgittPeriode oppgittPeriode, Virksomhet virksomhet) {
        Behandling behandling = setupScenario(Collections.singletonList(oppgittPeriode));
        lagreYrkesAktiviterFrilans(behandling, Arbeidsgiver.virksomhet(virksomhet));
        return behandling;
    }

    private void lagreUttaksperiodegrense(Behandling behandling, UttakRepository uttakRepository) {
        Uttaksperiodegrense grense = new Uttaksperiodegrense.Builder(behandling)
            .medFørsteLovligeUttaksdag(LocalDate.now().minusMonths(6)).medMottattDato(LocalDate.now().minusWeeks(2)).build();
        uttakRepository.lagreUttaksperiodegrense(behandling, grense);
    }

    private Virksomhet virksomhet(String orgnr) {
        Optional<Virksomhet> optional = repositoryProvider.getVirksomhetRepository().hent(orgnr);
        if (optional.isPresent()) {
            return optional.get();
        }
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(orgnr)
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);
        return virksomhet;
    }

    private FastsettePerioderRegelGrunnlagByggerImpl grunnlagBygger(UttakBeregningsandelTjeneste beregningsandelTjeneste) {
        UttakArbeidTjeneste uttakArbeidTjeneste = new UttakArbeidTjenesteImpl(inntektArbeidYtelseTjeneste, beregningsandelTjeneste);
        return new FastsettePerioderRegelGrunnlagByggerImpl(repositoryProvider, new ArbeidTidslinjeTjenesteImpl(repositoryProvider,
            new UttakStillingsprosentTjenesteImpl(uttakArbeidTjeneste), beregningsandelTjeneste, uttakArbeidTjeneste), relatertBehandlingTjeneste, uttakArbeidTjeneste);
    }

    private void lagreStønadskontoer(Behandling behandling, FagsakRelasjonRepository fagsakRelasjonRepository) {
        Stønadskonto mødrekvote = Stønadskonto.builder().medStønadskontoType(StønadskontoType.MØDREKVOTE)
            .medMaxDager(30).build();
        Stønadskonto fellesperiode = Stønadskonto.builder().medStønadskontoType(StønadskontoType.FELLESPERIODE)
            .medMaxDager(15).build();
        Stønadskonto fedrekvote = Stønadskonto.builder().medStønadskontoType(StønadskontoType.FEDREKVOTE)
            .medMaxDager(50).build();
        Stønadskonto foreldrepengerFørFødsel = Stønadskonto.builder().medStønadskontoType(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)
            .medMaxDager(130).build();

        Stønadskontoberegning stønadskontoberegning = Stønadskontoberegning.builder()
            .medStønadskonto(mødrekvote)
            .medStønadskonto(fedrekvote)
            .medStønadskonto(fellesperiode)
            .medStønadskonto(foreldrepengerFørFødsel)
            .medRegelEvaluering(" ")
            .medRegelInput(" ")
            .build();

        fagsakRelasjonRepository.opprettRelasjon(behandling.getFagsak(), Dekningsgrad._100);
        fagsakRelasjonRepository.lagre(behandling, stønadskontoberegning);
    }

    private void lagreYrkesAktiviter(Behandling behandling,
                                     Arbeidsgiver virksomhet,
                                     List<String> arbeidsforholdIdList,
                                     List<Permisjon> permisjonList,
                                     BigDecimal stillingsprosent) {
        InntektArbeidYtelseAggregatBuilder builder = lagYrkesAktiviter(behandling, virksomhet, arbeidsforholdIdList, permisjonList, stillingsprosent);
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);
    }

    private InntektArbeidYtelseAggregatBuilder lagYrkesAktiviter(Behandling behandling, Arbeidsgiver virksomhet, List<String> arbeidsforholdIdList,
                                                                 List<Permisjon> permisjonList, BigDecimal stillingsprosent) {
        InntektArbeidYtelseAggregatBuilder builder = repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(behandling, VersjonType.REGISTER);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());

        LocalDate fraOgMed = LocalDate.now().minusWeeks(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);

        builder.leggTilAktørArbeid(aktørArbeidBuilder);

        for (String arbeidsforholdId : arbeidsforholdIdList) {
            YrkesaktivitetBuilder yrkesaktivitetBuilder = lagYrkesAktivitetForArbeidsforhold(virksomhet, permisjonList,
                stillingsprosent, fraOgMed, tilOgMed, arbeidsforholdId, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

            aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
            repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);
        }
        return builder;
    }

    private Inntektsmelding lagreInntektsmelding(Behandling behandling, Arbeidsgiver virksomhet, String arbeidsforholdId) {
        InntektsmeldingBuilder inntektsmeldingBuilder = lagInntektsmelding(behandling, virksomhet, arbeidsforholdId);
        Inntektsmelding inntektsmelding = inntektsmeldingBuilder.build();
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, inntektsmelding);
        return inntektsmelding;
    }

    private Inntektsmelding lagreInntektsmelding(Behandling behandling, Arbeidsgiver virksomhet) {
        return lagreInntektsmelding(behandling, virksomhet, null);
    }

    private Inntektsmelding lagreInntektsmeldingMedGradering(Behandling behandling, Arbeidsgiver virksomhet, String arbeidsforholdId,
                                                             LocalDate graderingFom, LocalDate graderingTom, BigDecimal arbeidstidsprosent) {
        InntektsmeldingBuilder inntektsmeldingBuilder = lagInntektsmelding(behandling, virksomhet, arbeidsforholdId);
        inntektsmeldingBuilder.leggTil(new GraderingEntitet(graderingFom, graderingTom, arbeidstidsprosent));
        Inntektsmelding inntektsmelding = inntektsmeldingBuilder.build();
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, inntektsmelding);
        return inntektsmelding;
    }

    private InntektsmeldingBuilder lagInntektsmelding(Behandling behandling, Arbeidsgiver virksomhet, String arbeidsforholdId) {
        MottattDokument mottattDokument = new MottattDokument.Builder()
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medDokumentTypeId(DokumentTypeId.INNTEKTSMELDING)
            .medDokumentId("foo")
            .build();
        repositoryProvider.getMottatteDokumentRepository().lagre(mottattDokument);
        return InntektsmeldingBuilder.builder()
            .medVirksomhet(virksomhet.getVirksomhet())
            .medArbeidsforholdId(arbeidsforholdId)
            .medBeløp(BigDecimal.valueOf(35000))
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medMottattDokument(mottattDokument)
            .medStartDatoPermisjon(LocalDate.now());
    }

    private void lagreYrkesAktiviterFrilans(Behandling behandling, Arbeidsgiver virksomhet) {
        InntektArbeidYtelseAggregatBuilder builder = lagYrkesAktivitFrilans(behandling, virksomhet);
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);
    }

    private InntektArbeidYtelseAggregatBuilder lagYrkesAktivitFrilans(Behandling behandling, Arbeidsgiver virksomhet) {
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder
            .oppdatere(Optional.empty(), VersjonType.REGISTER);

        LocalDate fraOgMed = LocalDate.now().minusWeeks(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());
        builder.leggTilAktørArbeid(aktørArbeidBuilder);

        YrkesaktivitetBuilder yrkesaktivitetBuilder = lagYrkesAktivitetForArbeidsforhold(virksomhet, Collections.emptyList(),
            BigDecimal.valueOf(100), fraOgMed, tilOgMed, null, ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);

        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        YrkesaktivitetBuilder yrkesaktivitetBuilder2 = lagYrkesAktivitetForFrilansOverordnet(fraOgMed, tilOgMed, ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);

        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder2);
        return builder;
    }

    private YrkesaktivitetBuilder lagYrkesAktivitetForFrilansOverordnet(LocalDate fraOgMed, LocalDate tilOgMed, ArbeidType arbeidType) {
        YrkesaktivitetBuilder yrkesaktivitetBuilder = YrkesaktivitetBuilder.oppdatere(Optional.empty());
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed));
        yrkesaktivitetBuilder
            .medArbeidType(arbeidType)
            .leggTilAktivitetsAvtale(aktivitetsAvtale);

        return yrkesaktivitetBuilder;
    }

    private YrkesaktivitetBuilder lagYrkesAktivitetForArbeidsforhold(Arbeidsgiver virksomhet,
                                                                     List<Permisjon> permisjonList,
                                                                     BigDecimal stillingsprosent,
                                                                     LocalDate fraOgMed,
                                                                     LocalDate tilOgMed,
                                                                     String arbeidsforholdId,
                                                                     ArbeidType arbeidType) {
        YrkesaktivitetBuilder yrkesaktivitetBuilder = YrkesaktivitetBuilder.oppdatere(Optional.empty());
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed))
            .medProsentsats(stillingsprosent)
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d));
        yrkesaktivitetBuilder
            .medArbeidType(arbeidType)
            .medArbeidsgiver(virksomhet)
            .medArbeidsforholdId(ArbeidsforholdRef.ref(arbeidsforholdId))
            .leggTilAktivitetsAvtale(aktivitetsAvtale);
        if (permisjonList != null) {
            permisjonList.forEach(yrkesaktivitetBuilder::leggTilPermisjon);
        }

        return yrkesaktivitetBuilder;
    }

    private Behandling setupScenario(List<OppgittPeriode> oppgittPerioder) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medFordeling(new OppgittFordelingEntitet(oppgittPerioder, true));
        scenario.medBekreftetHendelse().medFødselsDato(LocalDate.now().minusWeeks(2));
        scenario.medOppgittRettighet(new OppgittRettighetEntitet(true, true, false));
        scenario.medBehandlingVedtak()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("saksbehandler")
            .medVedtaksdato(LocalDate.now());

        Behandling behandling = scenario.lagre(repositoryProvider);
        lagreUttaksperiodegrense(behandling, repositoryProvider.getUttakRepository());
        lagreStønadskontoer(behandling, repositoryProvider.getFagsakRelasjonRepository());
        return behandling;
    }

    private ArbeidsforholdRef getArbeidsforholdRef(Behandling behandling, String arbeidsforholdId, Arbeidsgiver arbeidsgiver, InntektArbeidYtelseRepository inntektArbeidYtelseRepository) {
        Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon = inntektArbeidYtelseRepository.hentArbeidsforholdInformasjon(behandling);
        return arbeidsforholdInformasjon.isPresent() ? arbeidsforholdInformasjon.get().finnForEkstern(arbeidsgiver, ArbeidsforholdRef.ref(arbeidsforholdId)) : null;
    }
}
