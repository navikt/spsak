package no.nav.foreldrepenger.domene.kontrollerfakta.fødsel;

import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.DUMMY_CONSUMER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_FØDSELREGISTRERING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.konfig.Tid;

public class AksjonspunktUtlederForForeldrepengerFødselNårHovedsøkerErMorTest {

    private static final LocalDate TERMINDAT0_NÅ = LocalDate.now();
    private static final LocalDate TERMINDATO_27_SIDEN = LocalDate.now().minusDays(27);
    private static final LocalDate FØDSEL_17_SIDEN = LocalDate.now().minusDays(27);
    private static final String ORG_NR = "55555555";
    private static final LocalDate AVSLUTTET_TIDSPUNKT = LocalDate.now().minusMonths(2);

    public static AktørId GITT_AKTØR_ID = new AktørId("666");

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private AksjonspunktUtlederForForeldrepengerFødsel apUtleder;

    @Before
    public void oppsett() {
        apUtleder = Mockito.spy(new AksjonspunktUtlederForForeldrepengerFødsel(repositoryProvider, mock(SkjæringstidspunktTjeneste.class)));
    }

    @Test
    public void sjekk_manglende_fødsel_dersom_termindato_mer_enn_25_dager_siden() {
        Behandling behandling = opprettBehandlingMedOppgittTerminOgBehandlingType(TERMINDATO_27_SIDEN);
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);

        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL));
        verify(apUtleder).erDagensDato25DagerEtterOppgittTerminsdato(any(FamilieHendelseGrunnlag.class));
    }

    @Test
    public void sjekk_manglende_fødsel_dersom_fødsel_og_mer_enn_14_dager_siden_fødsel() {
        Behandling behandling = opprettBehandlingMedOppgittFødsel(FØDSEL_17_SIDEN);
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);

        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL));
        verify(apUtleder).erDagensDato14DagerEtterOppgittFødselsdato(any(FamilieHendelse.class));
    }

    @Test
    public void sjekk_autopunkt_vent_på_fødsel_dersom_fødsel_og_mindre_enn_14_dager_siden_fødsel() {
        Behandling behandling = opprettBehandlingMedOppgittFødsel(LocalDate.now());
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);

        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(AUTO_VENT_PÅ_FØDSELREGISTRERING));
        assertThat(utledeteAksjonspunkter.get(0).getAksjonspunktModifiserer()).isNotEqualTo(DUMMY_CONSUMER);
        verify(apUtleder).erDagensDato14DagerEtterOppgittFødselsdato(any(FamilieHendelse.class));
    }

    @Test
    public void ingen_akjsonspunkter_dersom_fødsel_registrert_i_TPS_og_antall_barn_stemmer_med_søknad() {

        // Oppsett
        BehandlingRepositoryProvider behandlingRepositoryMock = spy(repositoryProvider);
        AksjonspunktUtlederForForeldrepengerFødsel testObjekt = Mockito.spy(new AksjonspunktUtlederForForeldrepengerFødsel(behandlingRepositoryMock, mock(SkjæringstidspunktTjeneste.class)));

        Behandling behandling = opprettBehandlingForFødselRegistrertITps(LocalDate.now(), 1, 1);
        List<AksjonspunktResultat> utledeteAksjonspunkter = testObjekt.utledAksjonspunkterFor(behandling);

        assertThat(utledeteAksjonspunkter).isEmpty();
        verify(testObjekt).samsvarerAntallBarnISøknadMedAntallBarnITps(any(FamilieHendelse.class), any());
    }

    @Test
    public void sjekk_manglende_fødsel_dersom_fødsel_registrert_i_TPS_og_antall_barn_ikke_stemmer_med_søknad() {
        // Oppsett
        BehandlingRepositoryProvider behandlingRepositoryMock = spy(repositoryProvider);

        AksjonspunktUtlederForForeldrepengerFødsel testObjekt = Mockito.spy(new AksjonspunktUtlederForForeldrepengerFødsel(behandlingRepositoryMock, mock(SkjæringstidspunktTjeneste.class)));

        Behandling behandling = opprettBehandlingForFødselRegistrertITps(LocalDate.now(), 2, 1);
        List<AksjonspunktResultat> utledeteAksjonspunkter = testObjekt.utledAksjonspunkterFor(behandling);

        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL));
        verify(testObjekt).samsvarerAntallBarnISøknadMedAntallBarnITps(any(FamilieHendelse.class), any());
    }

    @Test
    public void avklar_terminbekreftelse_dersom_termindato_nå_og_mangler_løpende_arbeid() {
        //Arrange
        Behandling behandling = opprettBehandlingMedOppgittTerminOgBehandlingType(TERMINDAT0_NÅ);
        //Act
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);
        //Assert
        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(AVKLAR_TERMINBEKREFTELSE));
        verify(apUtleder).erSøkerRegistrertArbeidstakerMedLøpendeArbeidsforholdIAARegisteret(behandling);
    }

    @Test
    public void ingen_aksjonspunkter_dersom_løpende_arbeid() {
        //Arrange
        Behandling behandling = opprettBehandlingMedOppgittTerminOgArbeidsForhold(TERMINDAT0_NÅ, Tid.TIDENES_ENDE);
        //Act
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);
        //Assert
        assertThat(utledeteAksjonspunkter).isEmpty();
        verify(apUtleder).erSøkerRegistrertArbeidstakerMedLøpendeArbeidsforholdIAARegisteret(behandling);
    }

    @Test
    public void avklar_terminbekreftelse_dersom_har_avsluttet_arbeidsforhold() {
        //Arrange
        Behandling behandling = opprettBehandlingMedOppgittTerminOgArbeidsForhold(TERMINDAT0_NÅ, AVSLUTTET_TIDSPUNKT);
        //Act
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);
        //Assert
        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(AVKLAR_TERMINBEKREFTELSE));
        verify(apUtleder).erSøkerRegistrertArbeidstakerMedLøpendeArbeidsforholdIAARegisteret(behandling);
    }

    private Behandling opprettBehandlingForFødselRegistrertITps(LocalDate fødseldato, int antallBarnSøknad, int antallBarnTps) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger
            .forFødsel();
        scenario.medSøknadHendelse()
            .medFødselsDato(fødseldato)
            .medAntallBarn(antallBarnSøknad);
        scenario.medBekreftetHendelse().medFødselsDato(fødseldato).medAntallBarn(antallBarnTps);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingMedOppgittFødsel(LocalDate fødseldato) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(fødseldato);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingMedOppgittTerminOgArbeidsForhold(LocalDate termindato, LocalDate tilOgMed) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger
            .forFødselMedGittAktørId(GITT_AKTØR_ID);
        scenario.medSøknadHendelse().medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
            .medUtstedtDato(LocalDate.now())
            .medTermindato(termindato)
            .medNavnPå("LEGEN MIN"));
        leggTilSøker(scenario, NavBrukerKjønn.KVINNE);
        scenario.removeDodgyDefaultInntektArbeidYTelse();
        Behandling behandling = scenario.lagre(repositoryProvider);
        final InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(behandling, VersjonType.REGISTER);
        lagAktørArbeid(inntektArbeidYtelseAggregatBuilder, GITT_AKTØR_ID, ORG_NR, tilOgMed);
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, inntektArbeidYtelseAggregatBuilder);
        return behandling;
    }

    private Behandling opprettBehandlingMedOppgittTerminOgBehandlingType(LocalDate termindato) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger
            .forFødselMedGittAktørId(GITT_AKTØR_ID);
        scenario.medSøknadHendelse().medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
            .medUtstedtDato(LocalDate.now())
            .medTermindato(termindato)
            .medNavnPå("LEGEN MIN"));

        Behandling behandling = scenario.lagre(repositoryProvider);
        return behandling;
    }

    private void lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId, String orgNr, LocalDate tilOgMed) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder =
            inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(aktørId);

        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(Opptjeningsnøkkel.forOrgnummer(orgNr),
            ArbeidType.FORENKLET_OPPGJØRSORDNING);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusMonths(50), tilOgMed)));

        yrkesaktivitetBuilder.medArbeidType(ArbeidType.FORENKLET_OPPGJØRSORDNING);

        final VirksomhetEntitet hei = new VirksomhetEntitet.Builder().medNavn("Hei").medOrgnr(ORG_NR).oppdatertOpplysningerNå().build();
        repoRule.getEntityManager().persist(hei);
        yrkesaktivitetBuilder.medArbeidsgiver(Arbeidsgiver.virksomhet(hei));
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
    }

    private void leggTilSøker(AbstractTestScenario<?> scenario, NavBrukerKjønn kjønn) {
        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();
        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .voksenPerson(søkerAktørId, SivilstandType.UOPPGITT, kjønn, Region.UDEFINERT)
            .build();
        scenario.medRegisterOpplysninger(søker);
    }
}
