package no.nav.foreldrepenger.domene.medlem.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapKildeType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapPerioderBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.InntektArbeidYtelseScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.util.FPDateUtil;

@RunWith(CdiRunner.class)
public class UtledVurderingsdatoerForMedlemskapTjenesteImplTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider provider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private BehandlingRepository behandlingRepository = provider.getBehandlingRepository();
    private MedlemskapRepository medlemskapRepository = provider.getMedlemskapRepository();
    private VirksomhetRepository virksomhetRepository = provider.getVirksomhetRepository();
    private PersonopplysningRepository personopplysningRepository = provider.getPersonopplysningRepository();
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = provider.getInntektArbeidYtelseRepository();
    private FagsakRepository fagsakRepository = provider.getFagsakRepository();

    @Inject
    private UtledVurderingsdatoerForMedlemskapTjenesteImpl tjeneste;

    @Test
    public void skal_utled_vurderingsdato_ved_endring_i_medlemskapsperioder() {
        // Arrange
        LocalDate datoMedEndring = LocalDate.now().plusDays(10);
        LocalDate ettÅrSiden = LocalDate.now().minusYears(1);
        LocalDate iDag = LocalDate.now();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        RegistrertMedlemskapPerioder periode = opprettPeriode(ettÅrSiden, iDag, MedlemskapDekningType.FTL_2_6);
        scenario.leggTilMedlemskapPeriode(periode);
        Behandling behandling = scenario.lagre(provider);
        avslutterBehandlingOgFagsak(behandling);

        Behandling revudering = opprettRevudering(behandling);

        oppdaterMedlem(datoMedEndring, periode, revudering);

        // Act
        Set<LocalDate> vurderingsdatoer = tjeneste.finnVurderingsdatoer(revudering.getId());

        // Assert
        assertThat(vurderingsdatoer).containsExactly(datoMedEndring);
    }

  
    @Test
    public void skal_utled_vurderingsdato_ved_endring_personopplysninger_statsborgerskap() {
        // Arrange
        LocalDate iDag = LocalDate.now();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        DatoIntervallEntitet førsteÅr = DatoIntervallEntitet.fraOgMedTilOgMed(iDag, iDag.plusYears(1));
        DatoIntervallEntitet andreÅr = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.plusYears(1), iDag.plusYears(2));
        DatoIntervallEntitet tredjeÅr = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.plusYears(2), iDag.plusYears(3));
        Behandling behandling = scenario.lagre(provider);
        PersonopplysningGrunnlag personopplysningGrunnlag = personopplysningRepository.hentPersonopplysninger(behandling);

        PersonInformasjonBuilder personInformasjonBuilder = PersonInformasjonBuilder.oppdater(Optional.of(personopplysningGrunnlag.getRegisterVersjon()), PersonopplysningVersjonType.REGISTRERT);
        PersonInformasjonBuilder.StatsborgerskapBuilder norgeFørsteÅr = personInformasjonBuilder.getStatsborgerskapBuilder(søkerAktørId, førsteÅr, Landkoder.NOR, Region.NORDEN);
        PersonInformasjonBuilder.StatsborgerskapBuilder spaniaAndreÅr = personInformasjonBuilder.getStatsborgerskapBuilder(søkerAktørId, andreÅr, Landkoder.ESP, Region.EOS);
        PersonInformasjonBuilder.StatsborgerskapBuilder norgeTredjeÅr = personInformasjonBuilder.getStatsborgerskapBuilder(søkerAktørId, tredjeÅr, Landkoder.NOR, Region.NORDEN);
        personInformasjonBuilder.leggTil(norgeFørsteÅr);
        personInformasjonBuilder.leggTil(spaniaAndreÅr);
        personInformasjonBuilder.leggTil(norgeTredjeÅr);

        personopplysningRepository.lagre(behandling, personInformasjonBuilder);

        // Act
        Set<LocalDate> vurderingsdatoer = tjeneste.finnVurderingsdatoer(behandling.getId());

        assertThat(vurderingsdatoer).containsExactlyInAnyOrder(andreÅr.getFomDato(), tredjeÅr.getFomDato());
    }

    @Test
    public void skal_utled_vurderingsdato_ved_endring_personopplysninger_personstatus() {
        // Arrange
        LocalDate iDag = LocalDate.now();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        DatoIntervallEntitet førsteÅr = DatoIntervallEntitet.fraOgMedTilOgMed(iDag, iDag.plusYears(1));
        DatoIntervallEntitet andreÅr = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.plusYears(1), iDag.plusYears(2));
        DatoIntervallEntitet tredjeÅr = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.plusYears(2), iDag.plusYears(3));
        Behandling behandling = scenario.lagre(provider);
        PersonopplysningGrunnlag personopplysningGrunnlag = personopplysningRepository.hentPersonopplysninger(behandling);

        PersonInformasjonBuilder personInformasjonBuilder = PersonInformasjonBuilder.oppdater(Optional.of(personopplysningGrunnlag.getRegisterVersjon()), PersonopplysningVersjonType.REGISTRERT);
        PersonInformasjonBuilder.PersonstatusBuilder førsteÅrBosa = personInformasjonBuilder.getPersonstatusBuilder(søkerAktørId, førsteÅr).medPersonstatus(PersonstatusType.BOSA);
        PersonInformasjonBuilder.PersonstatusBuilder andreÅrBosa = personInformasjonBuilder.getPersonstatusBuilder(søkerAktørId, andreÅr).medPersonstatus(PersonstatusType.UTVA);
        PersonInformasjonBuilder.PersonstatusBuilder tredjeÅrBosa = personInformasjonBuilder.getPersonstatusBuilder(søkerAktørId, tredjeÅr).medPersonstatus(PersonstatusType.DØD);
        personInformasjonBuilder.leggTil(førsteÅrBosa);
        personInformasjonBuilder.leggTil(andreÅrBosa);
        personInformasjonBuilder.leggTil(tredjeÅrBosa);

        personopplysningRepository.lagre(behandling, personInformasjonBuilder);

        // Act
        Set<LocalDate> vurderingsdatoer = tjeneste.finnVurderingsdatoer(behandling.getId());

        assertThat(vurderingsdatoer).containsExactlyInAnyOrder(andreÅr.getFomDato(), tredjeÅr.getFomDato());
    }

    @Test
    public void skal_utled_vurderingsdato_ved_endring_personopplysninger_adressetype() {
        // Arrange
        LocalDate iDag = LocalDate.now();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        DatoIntervallEntitet førsteÅr = DatoIntervallEntitet.fraOgMedTilOgMed(iDag, iDag.plusYears(1));
        DatoIntervallEntitet andreÅr = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.plusYears(1), iDag.plusYears(2));
        DatoIntervallEntitet tredjeÅr = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.plusYears(2), iDag.plusYears(3));
        Behandling behandling = scenario.lagre(provider);
        PersonopplysningGrunnlag personopplysningGrunnlag = personopplysningRepository.hentPersonopplysninger(behandling);

        PersonInformasjonBuilder personInformasjonBuilder = PersonInformasjonBuilder.oppdater(Optional.of(personopplysningGrunnlag.getRegisterVersjon()), PersonopplysningVersjonType.REGISTRERT);
        PersonInformasjonBuilder.AdresseBuilder bostedFørsteÅr = personInformasjonBuilder.getAdresseBuilder(søkerAktørId, førsteÅr, AdresseType.BOSTEDSADRESSE);
        PersonInformasjonBuilder.AdresseBuilder utlandAndreÅr = personInformasjonBuilder.getAdresseBuilder(søkerAktørId, andreÅr, AdresseType.POSTADRESSE_UTLAND);
        PersonInformasjonBuilder.AdresseBuilder bostedTredjeÅr = personInformasjonBuilder.getAdresseBuilder(søkerAktørId, tredjeÅr, AdresseType.BOSTEDSADRESSE);
        personInformasjonBuilder.leggTil(bostedFørsteÅr);
        personInformasjonBuilder.leggTil(utlandAndreÅr);
        personInformasjonBuilder.leggTil(bostedTredjeÅr);

        personopplysningRepository.lagre(behandling, personInformasjonBuilder);

        // Act
        Set<LocalDate> vurderingsdatoer = tjeneste.finnVurderingsdatoer(behandling.getId());

        assertThat(vurderingsdatoer).containsExactlyInAnyOrder(andreÅr.getFomDato(), tredjeÅr.getFomDato());
    }

    @Test
    public void skal_utled_vurderingsdato_ved_bortfall_av_inntekt() {
        // Arrange
        LocalDate idag = LocalDate.now();
        LocalDate datoMedBortfall = idag.minusMonths(1);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Virksomhet virksomhet = opprettVirksomhet();
        opprettInntekt(scenario.getDefaultBrukerAktørId(), scenario, virksomhet, idag, false);
        Behandling behandling = scenario.lagre(provider);
        avslutterBehandlingOgFagsak(behandling);

        Behandling revudering = opprettRevudering(behandling);
        endreInntekt(revudering, revudering.getAktørId(), virksomhet, idag);

        // Act
        Set<LocalDate> vurderingsdatoer = tjeneste.finnVurderingsdatoer(revudering.getId());

        // Assert
        assertThat(vurderingsdatoer).containsExactly(datoMedBortfall);
    }

    @Test
    public void skal_utled_vurderingsdato_ved_bortfall_av_inntekt_tester_overlapp() {
        // Arrange
        LocalDate idag = LocalDate.now();
        LocalDate datoMedBortfall = idag.minusMonths(1);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Virksomhet virksomhet = opprettVirksomhet();

        //tester overlapp
        opprettInntekt(scenario.getDefaultBrukerAktørId(), scenario, virksomhet, idag, true);

        Behandling behandling = scenario.lagre(provider);
        avslutterBehandlingOgFagsak(behandling);

        Behandling revudering = opprettRevudering(behandling);
        endreInntekt(revudering, revudering.getAktørId(), virksomhet, idag);

        // Act
        Set<LocalDate> vurderingsdatoer = tjeneste.finnVurderingsdatoer(revudering.getId());

        // Assert
        assertThat(vurderingsdatoer).containsExactly(datoMedBortfall);
    }

    private void oppdaterMedlem(LocalDate datoMedEndring, RegistrertMedlemskapPerioder periode, Behandling behandling) {
        RegistrertMedlemskapPerioder nyPeriode = new MedlemskapPerioderBuilder()
            .medPeriode(datoMedEndring, null)
            .medDekningType(MedlemskapDekningType.FULL)
            .medMedlemskapType(MedlemskapType.ENDELIG)
            .medKildeType(MedlemskapKildeType.MEDL)
            .medMedlId(2L)
            .build();
        medlemskapRepository.lagreMedlemskapRegisterOpplysninger(behandling, asList(periode, nyPeriode));
    }

    private Behandling opprettRevudering(Behandling behandling) {
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_FEIL_ELLER_ENDRET_FAKTA)
            .medOriginalBehandling(behandling);

        Behandling revudering = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(revurderingÅrsak).build();

        behandlingRepository.lagre(revudering, behandlingRepository.taSkriveLås(revudering));
        medlemskapRepository.kopierGrunnlagFraEksisterendeBehandling(behandling, revudering);
        inntektArbeidYtelseRepository.kopierGrunnlagFraEksisterendeBehandling(behandling, revudering);
        personopplysningRepository.kopierGrunnlagFraEksisterendeBehandlingForRevurdering(behandling, revudering);

        return revudering;
    }

    private void avslutterBehandlingOgFagsak(Behandling behandling) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Behandlingsresultat.Builder behandlingsresultatBuilder = Behandlingsresultat.builderForInngangsvilkår();
        Behandlingsresultat behandlingsresultat = behandlingsresultatBuilder.buildFor(behandling);

        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandling, lås);
        provider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(behandling, lagUttaksPeriode());

        behandling.avsluttBehandling();
        behandlingRepository.lagre(behandling, lås);
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.AVSLUTTET);
    }

    private RegistrertMedlemskapPerioder opprettPeriode(LocalDate fom, LocalDate tom, MedlemskapDekningType dekningType) {
        RegistrertMedlemskapPerioder periode = new MedlemskapPerioderBuilder()
            .medDekningType(dekningType)
            .medMedlemskapType(MedlemskapType.FORELOPIG)
            .medPeriode(fom, tom)
            .medKildeType(MedlemskapKildeType.MEDL)
            .medMedlId(1L)
            .build();
        return periode;
    }

    private UttakResultatPerioderEntitet lagUttaksPeriode() {
        LocalDate idag = LocalDate.now();
        UttakResultatPeriodeEntitet periode = new UttakResultatPeriodeEntitet.Builder(idag, idag.plusDays(6))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder()
            .oppdatertOpplysningerNå()
            .medOrgnr("123")
            .medNavn("Statoil").build();
        UttakAktivitetEntitet uttakAktivtet = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("321"))
            .build();
        virksomhetRepository.lagre(virksomhet);
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivtet)
            .medUtbetalingsprosent(BigDecimal.valueOf(100L))
            .medArbeidsprosent(BigDecimal.valueOf(100L))
            .medErSøktGradering(true)
            .build();
        periode.leggTilAktivitet(periodeAktivitet);
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        perioder.leggTilPeriode(periode);
        return perioder;
    }

    private void opprettInntekt(AktørId aktør, ScenarioMorSøkerForeldrepenger scenario, Virksomhet virksomhet, LocalDate datoMedEndring, boolean overlapp) {
        InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder builder = scenario.getInntektArbeidYtelseScenarioTestBuilder();
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = builder.build();
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktør);
        AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilderForYtelser(InntektsKilde.INNTEKT_OPPTJENING);

        InntektEntitet.InntektspostBuilder inntektspost1 = InntektEntitet.InntektspostBuilder.ny()
            .medBeløp(BigDecimal.valueOf(25000L))
            .medPeriode(datoMedEndring.minusMonths(3), datoMedEndring.minusMonths(2))
            .medInntektspostType(InntektspostType.LØNN);
        InntektEntitet.InntektspostBuilder inntektspost2 = InntektEntitet.InntektspostBuilder.ny()
            .medBeløp(BigDecimal.valueOf(26000L))
            .medPeriode(datoMedEndring.minusMonths(2), datoMedEndring.minusMonths(1))
            .medInntektspostType(InntektspostType.LØNN);
        InntektEntitet.InntektspostBuilder inntektspost3 = InntektEntitet.InntektspostBuilder.ny()
            .medBeløp(BigDecimal.valueOf(28000L))
            .medPeriode(datoMedEndring.minusMonths(1), datoMedEndring)
            .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspost1).medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
        inntektBuilder.leggTilInntektspost(inntektspost2).medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
        inntektBuilder.leggTilInntektspost(inntektspost3).medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
        if (overlapp) {
            InntektEntitet.InntektspostBuilder inntektspost1Overlapp = InntektEntitet.InntektspostBuilder.ny()
                .medBeløp(BigDecimal.valueOf(20000L))
                .medPeriode(datoMedEndring.minusMonths(3), datoMedEndring.minusMonths(2))
                .medInntektspostType(InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE);
            InntektEntitet.InntektspostBuilder inntektspost1Overlapp2 = InntektEntitet.InntektspostBuilder.ny()
                .medBeløp(BigDecimal.valueOf(20000L))
                .medPeriode(datoMedEndring.minusMonths(2), datoMedEndring.minusMonths(1))
                .medInntektspostType(InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE);
            InntektEntitet.InntektspostBuilder inntektspost1Overlapp3 = InntektEntitet.InntektspostBuilder.ny()
                .medBeløp(BigDecimal.valueOf(20000L))
                .medPeriode(datoMedEndring.minusMonths(1), datoMedEndring)
                .medInntektspostType(InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE);
            inntektBuilder.leggTilInntektspost(inntektspost1Overlapp).medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
            inntektBuilder.leggTilInntektspost(inntektspost1Overlapp2).medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
            inntektBuilder.leggTilInntektspost(inntektspost1Overlapp3).medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
        }
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    private void endreInntekt(Behandling revurdering, AktørId aktørId, Virksomhet virksomhet, LocalDate datoMedEndring) {
        InntektArbeidYtelseAggregatBuilder builder = inntektArbeidYtelseRepository.opprettBuilderFor(revurdering, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = builder.getAktørInntektBuilder(aktørId);

        AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilderForYtelser(InntektsKilde.INNTEKT_OPPTJENING);

        InntektEntitet.InntektspostBuilder inntektspost1 = InntektEntitet.InntektspostBuilder.ny()
            .medBeløp(BigDecimal.valueOf(25000L))
            .medPeriode(datoMedEndring.minusMonths(3), datoMedEndring.minusMonths(2))
            .medInntektspostType(InntektspostType.LØNN);
        InntektEntitet.InntektspostBuilder inntektspost2 = InntektEntitet.InntektspostBuilder.ny()
            .medBeløp(BigDecimal.valueOf(26000L))
            .medPeriode(datoMedEndring.minusMonths(2), datoMedEndring.minusMonths(1))
            .medInntektspostType(InntektspostType.LØNN);

        inntektBuilder.leggTilInntektspost(inntektspost1).medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
        inntektBuilder.leggTilInntektspost(inntektspost2).medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
        aktørInntektBuilder.fjernInntekterFraKilde(InntektsKilde.INNTEKT_OPPTJENING).leggTilInntekt(inntektBuilder);
        builder.leggTilAktørInntekt(aktørInntektBuilder);
        inntektArbeidYtelseRepository.lagre(revurdering, builder);
    }

    private Virksomhet opprettVirksomhet() {
        String orgNr = "21542512";

        final Optional<Virksomhet> hent = virksomhetRepository.hent(orgNr);
        if (hent.isPresent()) {
            return hent.get();
        }
        String orgNavn = "Sopra Steria";
        LocalDate virksomhetRegistrert = LocalDate.now(FPDateUtil.getOffset()).minusYears(3L).minusYears(2L);
        LocalDate virksomhetOppstart = LocalDate.now(FPDateUtil.getOffset()).minusYears(3L).minusYears(1L);
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(orgNr)
            .medNavn(orgNavn)
            .medRegistrert(virksomhetRegistrert)
            .medOppstart(virksomhetOppstart)
            .oppdatertOpplysningerNå()
            .build();
        virksomhetRepository.lagre(virksomhet);
        return virksomhet;
    }
}
