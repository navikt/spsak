package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import static no.nav.foreldrepenger.domene.uttak.UttakRevurderingTestUtil.AKTØR_ID_FAR;
import static no.nav.foreldrepenger.domene.uttak.UttakRevurderingTestUtil.AKTØR_ID_MOR;
import static no.nav.foreldrepenger.domene.uttak.UttakRevurderingTestUtil.ANKOMSTDATO;
import static no.nav.foreldrepenger.domene.uttak.UttakRevurderingTestUtil.FØDSELSDATO;
import static no.nav.foreldrepenger.domene.uttak.UttakRevurderingTestUtil.FØRSTE_UTTAKSDATO;
import static no.nav.foreldrepenger.domene.uttak.UttakRevurderingTestUtil.FØRSTE_UTTAKSDATO_SØKNAD_MOR_FPFF;
import static no.nav.foreldrepenger.domene.uttak.UttakRevurderingTestUtil.MANUELT_SATT_FØRSTE_UTTAKSDATO;
import static no.nav.foreldrepenger.domene.uttak.UttakRevurderingTestUtil.OMSORGSOVERTAKELSEDATO;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.dødsfall.OpplysningerOmDødEndringIdentifiserer;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.domene.uttak.UttakRevurderingTestUtil;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class EndringsdatoRevurderingUtlederTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private UttakRevurderingTestUtil testUtil;

    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private FamilieHendelseRepository familieHendelseRepository;
    @Inject
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    @Inject
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    @Inject
    private FamilieHendelseTjeneste familieHendelseTjeneste;
    @Inject
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    private OpplysningerOmDødEndringIdentifiserer opplysningerOmDødEndringIdentifiserer;

    private EndringsdatoRevurderingUtleder endringsdatoRevurderingUtleder;

    @Before
    public void before() {
        testUtil = new UttakRevurderingTestUtil(repoRule, repositoryProvider);
        endringsdatoRevurderingUtleder = new EndringsdatoRevurderingUtleder(familieHendelseTjeneste,
            inntektArbeidYtelseTjeneste, behandlingRepositoryProvider,
            opplysningerOmDødEndringIdentifiserer);
    }

    @Test // #1.1
    public void skal_utlede_at_endringsdato_er_fødselsdato_når_fødsel_har_forekommet_før_første_uttaksdato() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        FamilieHendelseBuilder hendelseBuilder = familieHendelseRepository.opprettBuilderFor(revurdering);
        hendelseBuilder.medFødselsDato(FØDSELSDATO).medAntallBarn(1);
        familieHendelseRepository.lagre(revurdering, hendelseBuilder);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(FØDSELSDATO);
    }

    @Test // #1.2
    public void skal_utlede_at_endringsdato_er_første_uttaksdato_fra_vedtak_når_fødsel_har_forekommet_etter_første_uttaksdato() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        FamilieHendelseBuilder hendelseBuilder = familieHendelseRepository.opprettBuilderFor(revurdering);
        hendelseBuilder.medFødselsDato(FØRSTE_UTTAKSDATO.plusDays(1)).medAntallBarn(1);
        familieHendelseRepository.lagre(revurdering, hendelseBuilder);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO);
    }

    @Test // #2
    public void skal_utlede_at_endringsdato_er_første_uttaksdato_fra_søknad_når_endringssøknad_er_mottatt() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        OppgittFordeling oppgittFordeling = testUtil.byggOgLagreOppgittFordelingForMorFPFF(revurdering);
        testUtil.byggOgLagreSøknadMedOppgittFordeling(revurdering, oppgittFordeling);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO_SØKNAD_MOR_FPFF);
    }

    @Test // #2
    public void skal_utlede_at_endringsdato_er_første_uttaksdato_fra_søknad_når_endringssøknad_er_mottatt_selv_om_mottatt_dato_før_vedtaksdato_på_original_behandling() {
        ScenarioMorSøkerForeldrepenger originalScenario = ScenarioMorSøkerForeldrepenger.forFødsel();

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now(), LocalDate.now())
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .build();
        originalScenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriode), true));
        originalScenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now())
            .medAnsvarligSaksbehandler("ansvar")
            .medVedtakResultatType(VedtakResultatType.INNVILGET);
        Behandling originalBehandling = originalScenario.lagre(repositoryProvider);

        UttakResultatPerioderEntitet originaltUttak = new UttakResultatPerioderEntitet();
        originaltUttak.leggTilPeriode(new UttakResultatPeriodeEntitet.Builder(LocalDate.now(), LocalDate.now())
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build());
        repositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(originalBehandling, originaltUttak);

        ScenarioMorSøkerForeldrepenger revurderingScenario = ScenarioMorSøkerForeldrepenger.forFødsel();

        revurderingScenario.medOriginalBehandling(originalBehandling, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);
        revurderingScenario.medSøknad().medErEndringssøknad(true).medMottattDato(LocalDate.now().minusWeeks(1));
        revurderingScenario.medBehandlingType(BehandlingType.REVURDERING);
        OppgittPeriode nyOppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(LocalDate.now().plusWeeks(1), LocalDate.now().plusWeeks(2))
            .build();

        revurderingScenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(nyOppgittPeriode), true));

        Behandling revurdering = revurderingScenario.lagre(repositoryProvider);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(nyOppgittPeriode.getFom());
    }

    @Test // #3
    public void skal_utlede_at_endringsdato_er_første_uttaksdato_fra_vedtak_når_revurdering_er_manuelt_opprettet() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_DØD)
            .medOriginalBehandling(revurdering.getOriginalBehandling().get())
            .medManueltOpprettet(true);
        revurderingÅrsak.buildFor(revurdering);
        behandlingRepository.lagre(revurdering, behandlingRepository.taSkriveLås(revurdering));

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO);
    }

    @Test // #4.1
    public void skal_utlede_at_endringsdato_på_mors_berørte_behandling_er_lik_endringsdato_på_far() {
        // Arrange førstegangsbehandling mor
        Behandling behandling = testUtil.byggFørstegangsbehandlingForRevurderingBerørtSak(AKTØR_ID_MOR, testUtil.uttaksresultatBerørtSak(FØRSTE_UTTAKSDATO));

        // Arrange førstegangsbehandling far
        LocalDate fomFar = FØRSTE_UTTAKSDATO.plusDays(11);
        Behandling behandlingFar = testUtil.byggFørstegangsbehandlingForRevurderingBerørtSak(AKTØR_ID_FAR, testUtil.uttaksresultatBerørtSak(fomFar));

        // Arrange berørt behandling mor
        Behandling revurderingBerørtSak = testUtil.opprettRevurderingBerørtSak(AKTØR_ID_MOR, BehandlingÅrsakType.BERØRT_BEHANDLING, behandling);
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.BERØRT_BEHANDLING)
            .medOriginalBehandling(revurderingBerørtSak.getOriginalBehandling().get())
            .medBerørtBehandling(behandlingFar);
        revurderingÅrsak.buildFor(revurderingBerørtSak);
        behandlingRepository.lagre(revurderingBerørtSak, behandlingRepository.taSkriveLås(revurderingBerørtSak));

        // Act
        LocalDate endringsdatoMor = endringsdatoRevurderingUtleder.utledEndringsdato(revurderingBerørtSak);

        // Assert
        assertThat(endringsdatoMor).isEqualTo(fomFar);
    }

    @Test // #4.2
    public void skal_utlede_at_endringsdato_på_mors_berørte_behandling_er_første_uttaksdag_fra_vedtaket_når_fars_endringsdato_er_tidligere() {
        // Arrange førstegangsbehandling mor
        Behandling behandling = testUtil.byggFørstegangsbehandlingForRevurderingBerørtSak(AKTØR_ID_MOR, testUtil.uttaksresultatBerørtSak(FØRSTE_UTTAKSDATO));

        // Arrange førstegangsbehandling far
        LocalDate fomFar = FØRSTE_UTTAKSDATO.minusDays(2L);
        Behandling behandlingFar = testUtil.byggFørstegangsbehandlingForRevurderingBerørtSak(AKTØR_ID_FAR, testUtil.uttaksresultatBerørtSak(fomFar));

        // Arrange berørt behandling mor
        Behandling revurderingBerørtSak = testUtil.opprettRevurderingBerørtSak(AKTØR_ID_MOR, BehandlingÅrsakType.BERØRT_BEHANDLING, behandling);
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.BERØRT_BEHANDLING)
            .medOriginalBehandling(revurderingBerørtSak.getOriginalBehandling().get())
            .medBerørtBehandling(behandlingFar);
        revurderingÅrsak.buildFor(revurderingBerørtSak);
        behandlingRepository.lagre(revurderingBerørtSak, behandlingRepository.taSkriveLås  (revurderingBerørtSak));

        // Act
        LocalDate endringsdatoMor = endringsdatoRevurderingUtleder.utledEndringsdato(revurderingBerørtSak);

        // Assert
        assertThat(endringsdatoMor).isEqualTo(FØRSTE_UTTAKSDATO);
    }

    @Test // #5
    public void skal_utlede_at_endringsdato_er_manuelt_satt_første_uttaksdato() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        AvklarteUttakDatoerEntitet entitet = new AvklarteUttakDatoerEntitet(MANUELT_SATT_FØRSTE_UTTAKSDATO, null);
        ytelsesFordelingRepository.lagre(revurdering, entitet);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(MANUELT_SATT_FØRSTE_UTTAKSDATO);
    }

    @Test // #2 + #5
    public void skal_utlede_at_endringsdato_er_første_uttaksdag_fra_søknad_når_denne_er_tidligere_enn_manuelt_satt_første_uttaksdato() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        OppgittFordeling oppgittFordeling = testUtil.byggOgLagreOppgittFordelingForMorFPFF(revurdering);
        testUtil.byggOgLagreSøknadMedOppgittFordeling(revurdering, oppgittFordeling);
        AvklarteUttakDatoerEntitet entitet = new AvklarteUttakDatoerEntitet(MANUELT_SATT_FØRSTE_UTTAKSDATO, null);
        ytelsesFordelingRepository.lagre(revurdering, entitet);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO_SØKNAD_MOR_FPFF);
    }

    @Test // #6
    public void skal_utlede_at_endringsdato_er_første_uttaksdato_fra_vedtaket_når_inntektsmelding_endrer_uttak() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering(AKTØR_ID_MOR, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        testUtil.opprettInntektsmelding(revurdering);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO);
    }

    @Test // #7
    public void skal_utlede_at_endringsdato_er_første_uttaksdato_fra_vedtaket_ved_opplysninger_om_død() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        PersonInformasjon registerVersjon = repositoryProvider.getPersonopplysningRepository().hentPersonopplysninger(revurdering).getRegisterVersjon();
        PersonInformasjonBuilder builder = PersonInformasjonBuilder.oppdater(Optional.of(registerVersjon), PersonopplysningVersjonType.REGISTRERT);
        builder.leggTil(builder.getPersonopplysningBuilder(AKTØR_ID_MOR).medDødsdato(LocalDate.now()));
        repositoryProvider.getPersonopplysningRepository().lagre(revurdering, builder);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO);
    }

    @Test // #7 + #8
    public void skal_utlede_at_endringsdato_er_første_uttaksdato_fra_vedtaket_ved_endring_i_ytelser() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        leggTilAktørYtelse(revurdering);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO);
    }

    @Test
    public void skal_utlede_at_endringsdato_er_første_uttaksdato_fra_vedtaket_når_endring_i_ytelse_ikke_fører_til_endring_i_grunnlaget() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO);
    }

    @Test
    public void skal_utlede_at_endringsdato_er_første_uttaksdato_i_endring_dersom_endringer_i_ytelse_stammer_fra_samme_fagsak() {

        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        OppgittFordeling oppgittFordeling = testUtil.byggOgLagreOppgittFordelingMedPeriode(revurdering, FØRSTE_UTTAKSDATO.plusDays(11), FØRSTE_UTTAKSDATO.plusDays(50), UttakPeriodeType.FELLESPERIODE);
        testUtil.byggOgLagreSøknadMedOppgittFordeling(revurdering, oppgittFordeling);

        leggTilFpsakYtelse(revurdering);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO.plusDays(11));
    }

    @Test // Adopsjon.1
    public void skal_utlede_at_endringsdato_er_omsorgsovertakelsedato_ved_adopsjon_uten_ankomstdato() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurderingAdopsjon(null);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(OMSORGSOVERTAKELSEDATO);
    }

    @Test // Adopsjon.2
    public void skal_utlede_at_endringsdato_er_ankomstdato_ved_adopsjon_når_ankomstdatoen_er_satt() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurderingAdopsjon(ANKOMSTDATO);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(ANKOMSTDATO);
    }

    @Test
    public void skal_utlede_at_endringsdato_er_første_uttaksdato_fra_forrige_behandling_når_uten_uttaksresultat() {
        // Arrange
        OppgittFordelingEntitet oppgittFordeling = new OppgittFordelingEntitet(Collections.emptyList(), true);
        Behandling revurdering = testUtil.opprettRevurdering(AKTØR_ID_MOR, BehandlingÅrsakType.RE_HENDELSE_FØDSEL,
            Collections.emptyList(), oppgittFordeling, null);

        // Act
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);

        // Assert
        assertThat(endringsdato).isEqualTo(FØRSTE_UTTAKSDATO);
    }

    private void leggTilAktørYtelse(Behandling revurdering) {
        InntektArbeidYtelseAggregatBuilder iayBuilder = inntektArbeidYtelseRepository.opprettBuilderFor(revurdering, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = iayBuilder.getAktørYtelseBuilder(AKTØR_ID_MOR);
        YtelseBuilder ytelselseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.FPSAK, RelatertYtelseType.FORELDREPENGER, new Saksnummer("1"));
        ytelselseBuilder.tilbakestillAnvisteYtelser();
        YtelseBuilder ytelse = ytelselseBuilder.medKilde(Fagsystem.INFOTRYGD)
            .medYtelseType(RelatertYtelseType.FORELDREPENGER)
            .medStatus(RelatertYtelseTilstand.AVSLUTTET)
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(6)));
        aktørYtelseBuilder.leggTilYtelse(ytelse);
        iayBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
        inntektArbeidYtelseRepository.lagre(revurdering, iayBuilder);
    }


    private void leggTilFpsakYtelse(Behandling revurdering) {
        InntektArbeidYtelseAggregatBuilder iayBuilder = inntektArbeidYtelseRepository.opprettBuilderFor(revurdering, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = iayBuilder.getAktørYtelseBuilder(AKTØR_ID_MOR);
        YtelseBuilder ytelselseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.FPSAK, RelatertYtelseType.FORELDREPENGER, new Saksnummer("1"));
        ytelselseBuilder.tilbakestillAnvisteYtelser();
        YtelseBuilder ytelse = ytelselseBuilder.medKilde(Fagsystem.FPSAK)
            .medYtelseType(RelatertYtelseType.FORELDREPENGER)
            .medStatus(RelatertYtelseTilstand.AVSLUTTET)
            .medSaksnummer(revurdering.getFagsak().getSaksnummer())
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(6)));
        aktørYtelseBuilder.leggTilYtelse(ytelse);
        iayBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
        inntektArbeidYtelseRepository.lagre(revurdering, iayBuilder);
    }


}
