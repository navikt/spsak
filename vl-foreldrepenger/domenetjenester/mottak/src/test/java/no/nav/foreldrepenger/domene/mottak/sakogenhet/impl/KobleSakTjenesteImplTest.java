package no.nav.foreldrepenger.domene.mottak.sakogenhet.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.Familierelasjon;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.sakogenhet.KobleSakTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public class KobleSakTjenesteImplTest {

    private static AktørId MOR_AKTØR_ID = new AktørId("444");
    private static PersonIdent MOR_IDENT = new PersonIdent("12128965432");
    private static Personinfo MOR_PINFO;

    private static AktørId FAR_AKTØR_ID = new AktørId("555");
    private static PersonIdent FAR_IDENT = new PersonIdent("11119164523");
    private static Personinfo FAR_PINFO;

    private static AktørId BARN_AKTØR_ID = new AktørId("333");
    private static AktørId ELDRE_BARN_AKTØR_ID = new AktørId("222");
    private static PersonIdent BARN_IDENT = new PersonIdent("03031855655");
    private static PersonIdent ELDRE_BARN_IDENT = new PersonIdent("06060633333");
    private static Personinfo BARN_PINFO;
    private static Personinfo ELDRE_BARN_PINFO;
    private static LocalDate ELDRE_BARN_FØDT = LocalDate.of(2006, 6, 6);
    private static LocalDate BARN_FØDT = LocalDate.of(2018, 3, 3);

    private static Familierelasjon relasjontilEldreBarn = new Familierelasjon(ELDRE_BARN_IDENT, RelasjonsRolleType.BARN, ELDRE_BARN_FØDT, "Vei", true);
    private static Familierelasjon relasjontilBarn = new Familierelasjon(BARN_IDENT, RelasjonsRolleType.BARN, BARN_FØDT, "Vei", true);
    private static Familierelasjon relasjontilMor = new Familierelasjon(MOR_IDENT, RelasjonsRolleType.MORA, LocalDate.of(1989, 12, 12), "Vei", true);
    private static Familierelasjon relasjontilFar = new Familierelasjon(FAR_IDENT, RelasjonsRolleType.FARA, LocalDate.of(1991, 11, 11), "Vei", true);

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private TpsTjeneste tpsTjeneste;
    private KobleSakTjeneste kobleSakTjeneste;

    @Before
    public void oppsett() {
        tpsTjeneste = mock(TpsTjeneste.class);
        kobleSakTjeneste = new KobleSakTjenesteImpl(repositoryProvider, tpsTjeneste);
    }

    @Test
    public void finn_mors_fagsak_dersom_termin_og_gjensidig_oppgitt_søknad() {
        // Oppsett
        settOppTpsStrukturer(false);

        Behandling behandlingMor = opprettBehandlingMorSøkerFødselTermin(LocalDate.now(), FAR_AKTØR_ID);
        Behandling behandlingFar = opprettBehandlingMedOppgittTerminOgBehandlingType(LocalDate.now(), MOR_AKTØR_ID);

        Optional<Fagsak> morsSak = kobleSakTjeneste.finnRelatertFagsakDersomRelevant(behandlingFar);

        assertThat(morsSak).isPresent();
        assertThat(morsSak).hasValueSatisfying(it -> assertThat(it).isEqualTo(behandlingMor.getFagsak()));
    }

    @Test
    public void finn_mors_fagsak_dersom_mor_søker_termin_får_bekreftet_fødsel_og_gjensidig_oppgitt_søknad() {
        // Oppsett
        settOppTpsStrukturer(false);

        Behandling behandlingMor = opprettBehandlingMorSøkerFødselTerminFødsel(LocalDate.now().plusWeeks(1), LocalDate.now(), FAR_AKTØR_ID);
        Behandling behandlingFar = opprettBehandlingMedOppgittFødselOgBehandlingType(LocalDate.now(), MOR_AKTØR_ID);

        Optional<Fagsak> morsSak = kobleSakTjeneste.finnRelatertFagsakDersomRelevant(behandlingFar);

        assertThat(morsSak).isPresent();
        assertThat(morsSak).hasValueSatisfying(it -> assertThat(it).isEqualTo(behandlingMor.getFagsak()));
    }

    @Test
    public void finn_mors_fagsak_dersom_termin_og_en_part_oppgir_annen_part() {
        // Oppsett
        settOppTpsStrukturer(false);

        @SuppressWarnings("unused")
        Behandling behandlingMor = opprettBehandlingMorSøkerFødselTermin(LocalDate.now(), null);
        Behandling behandlingFar = opprettBehandlingMedOppgittTerminOgBehandlingType(LocalDate.now(), MOR_AKTØR_ID);

        Optional<Fagsak> morsSak = kobleSakTjeneste.finnRelatertFagsakDersomRelevant(behandlingFar);

        assertThat(morsSak).isPresent();
        assertThat(morsSak).hasValueSatisfying(it -> assertThat(it).isEqualTo(behandlingMor.getFagsak()));
    }

    @Test
    public void finner_ikke_mors_fagsak_dersom_termin_og_ikke_oppgir_annen_part() {
        // Oppsett
        settOppTpsStrukturer(false);

        @SuppressWarnings("unused")
        Behandling behandlingMor = opprettBehandlingMorSøkerFødselTermin(LocalDate.now(), null);
        Behandling behandlingFar = opprettBehandlingMedOppgittTerminOgBehandlingType(LocalDate.now(), null);

        Optional<Fagsak> morsSak = kobleSakTjeneste.finnRelatertFagsakDersomRelevant(behandlingFar);

        assertThat(morsSak).isNotPresent();
    }

    @Test
    public void finn_mors_fagsak_dersom_surrogati_adopsjon() {
        // Oppsett
        settOppTpsSurrogatiStrukturer();

        @SuppressWarnings("unused")
        Behandling behandlingMor = opprettBehandlingMorSøkerFødselTerminBekreftetFødsel(LocalDate.now(), null);
        Behandling behandlingFar = opprettBehandlingMedAdopsjonAvEktefellesBarn(LocalDate.now(), MOR_AKTØR_ID);

        Optional<Fagsak> morsSak = kobleSakTjeneste.finnRelatertFagsakDersomRelevant(behandlingFar);

        assertThat(morsSak).isPresent();
        assertThat(morsSak).hasValueSatisfying(it -> assertThat(it).isEqualTo(behandlingMor.getFagsak()));
    }

    @Test
    public void finn_mors_fagsak_dersom_termin_og_en_part_oppgir_annen_part_og_andre_oppgir_tredje_part() {
        // Oppsett
        settOppTpsStrukturer(false);

        opprettBehandlingMorSøkerFødselTermin(LocalDate.now(), BARN_AKTØR_ID);
        Behandling behandlingFar = opprettBehandlingMedOppgittTerminOgBehandlingType(LocalDate.now(), MOR_AKTØR_ID);

        Optional<Fagsak> morsSak = kobleSakTjeneste.finnRelatertFagsakDersomRelevant(behandlingFar);

        assertThat(morsSak).isNotPresent();
    }

    @Test
    public void finn_mors_fagsak_dersom_fødsel_og_gjensidig_oppgitt_søknad() {
        // Oppsett
        settOppTpsStrukturer(true);

        Behandling behandlingMor = opprettBehandlingMorSøkerFødselRegistrertTPS(BARN_FØDT, 1, FAR_AKTØR_ID);
        Behandling behandlingFar = opprettBehandlingFarSøkerFødselRegistrertITps(BARN_FØDT, 1, MOR_AKTØR_ID);

        Optional<Fagsak> morsSak = kobleSakTjeneste.finnRelatertFagsakDersomRelevant(behandlingFar);

        assertThat(morsSak).isPresent();
        assertThat(morsSak).hasValueSatisfying(it -> assertThat(it).isEqualTo(behandlingMor.getFagsak()));
    }

    @Test
    public void mor_søker_far_har_gammel_sak() {
        // Oppsett
        settOppTpsStrukturer(false);

        Behandling behandlingMor = opprettBehandlingMorSøkerFødselTermin(LocalDate.now(), FAR_AKTØR_ID);
        opprettBehandlingFarSøkerFødselRegistrertITps(ELDRE_BARN_FØDT, 1, MOR_AKTØR_ID);

        Optional<Fagsak> farsSak = kobleSakTjeneste.finnRelatertFagsakDersomRelevant(behandlingMor);

        assertThat(farsSak).isNotPresent();
    }

    private Behandling opprettBehandlingMorSøkerFødselTermin(LocalDate termindato, AktørId annenPart) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(MOR_AKTØR_ID);
        scenario.medSøknadAnnenPart().medAktørId(annenPart).medNavn("Ola Dunk");
        scenario.medSøknadHendelse().medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
            .medUtstedtDato(LocalDate.now())
            .medTermindato(termindato)
            .medNavnPå("LEGEN MIN"))
            .medAntallBarn(1);

        leggTilMorSøker(scenario);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingMorSøkerFødselTerminFødsel(LocalDate termindato, LocalDate fødselsdato, AktørId annenPart) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(MOR_AKTØR_ID);
        scenario.medSøknadAnnenPart().medAktørId(annenPart).medNavn("Ola Dunk");
        scenario.medSøknadHendelse().medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
            .medUtstedtDato(LocalDate.now())
            .medTermindato(termindato)
            .medNavnPå("LEGEN MIN"))
            .medAntallBarn(1);
        scenario.medOverstyrtHendelse()
            .medFødselsDato(fødselsdato)
            .medAntallBarn(1)
            .medTerminbekreftelse(scenario.medOverstyrtHendelse().getTerminbekreftelseBuilder()
                .medUtstedtDato(LocalDate.now())
                .medTermindato(termindato)
                .medNavnPå("LEGEN MIN"));

        leggTilMorSøker(scenario);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingMorSøkerFødselTerminBekreftetFødsel(LocalDate termindato, AktørId annenPart) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(MOR_AKTØR_ID);
        scenario.medSøknadAnnenPart().medAktørId(annenPart).medNavn("Ola Dunk");
        scenario.medSøknadHendelse().medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
            .medUtstedtDato(LocalDate.now())
            .medTermindato(termindato)
            .medNavnPå("LEGEN MIN"))
            .medAntallBarn(1);
        scenario.medBekreftetHendelse().medFødselsDato(termindato).medAntallBarn(1);

        leggTilMorSøker(scenario);
        return scenario.lagre(repositoryProvider);
    }

    private void leggTilMorSøker(ScenarioMorSøkerForeldrepenger scenario) {
        PersonInformasjon søker = scenario.opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .kvinne(MOR_AKTØR_ID, SivilstandType.GIFT, Region.NORDEN)
            .statsborgerskap(Landkoder.NOR)
            .build();
        scenario.medRegisterOpplysninger(søker);
    }

    private void leggTilFarSøker(ScenarioFarSøkerForeldrepenger scenario) {
        PersonInformasjon søker = scenario.opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .mann(FAR_AKTØR_ID, SivilstandType.GIFT, Region.NORDEN)
            .statsborgerskap(Landkoder.NOR)
            .build();
        scenario.medRegisterOpplysninger(søker);
    }

    private Behandling opprettBehandlingMorSøkerFødselRegistrertTPS(LocalDate fødselsdato, int antallBarn, AktørId annenPart) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(MOR_AKTØR_ID);
        scenario.medSøknadAnnenPart().medAktørId(annenPart).medNavn("Ola Dunk");
        scenario.medSøknadHendelse()
            .medFødselsDato(fødselsdato)
            .medAntallBarn(antallBarn);
        leggTilMorSøker(scenario);

        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingFarSøkerFødselRegistrertITps(LocalDate fødseldato, int antallBarnSøknad, AktørId annenPart) {
        ScenarioFarSøkerForeldrepenger scenario = ScenarioFarSøkerForeldrepenger.forFødselMedGittAktørId(FAR_AKTØR_ID);
        scenario.medSøknadAnnenPart().medAktørId(annenPart).medNavn("Kari Dunk");
        scenario.medSøknadHendelse()
            .medFødselsDato(fødseldato)
            .medAntallBarn(antallBarnSøknad);
        leggTilFarSøker(scenario);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingMedOppgittTerminOgBehandlingType(LocalDate termindato, AktørId annenPart) {
        ScenarioFarSøkerForeldrepenger scenario = ScenarioFarSøkerForeldrepenger.forFødselMedGittAktørId(FAR_AKTØR_ID);
        if (annenPart != null) {
            scenario.medSøknadAnnenPart().medAktørId(annenPart).medNavn("Kari Dunk");
        }
        scenario.medSøknadHendelse().medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
            .medUtstedtDato(LocalDate.now())
            .medTermindato(termindato)
            .medNavnPå("LEGEN MIN"));
        leggTilFarSøker(scenario);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingMedOppgittFødselOgBehandlingType(LocalDate fødselsdato, AktørId annenPart) {
        ScenarioFarSøkerForeldrepenger scenario = ScenarioFarSøkerForeldrepenger.forFødselMedGittAktørId(FAR_AKTØR_ID);
        if (annenPart != null) {
            scenario.medSøknadAnnenPart().medAktørId(annenPart).medNavn("Kari Dunk");
        }
        scenario.medSøknadHendelse().medFødselsDato(fødselsdato).medAntallBarn(1);
        leggTilFarSøker(scenario);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingMedAdopsjonAvEktefellesBarn(LocalDate fødseldato, AktørId annenPart) {
        ScenarioFarSøkerForeldrepenger scenario = ScenarioFarSøkerForeldrepenger.forFødselMedGittAktørId(FAR_AKTØR_ID);
        if (annenPart != null) {
            scenario.medSøknadAnnenPart().medAktørId(annenPart).medNavn("Kari Dunk");
        }
        scenario.medSøknadHendelse().medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder()
            .medOmsorgsovertakelseDato(LocalDate.now())
            .medAdoptererAlene(false)
            .medErEktefellesBarn(true))
            .medFødselsDato(fødseldato);
        leggTilFarSøker(scenario);
        return scenario.lagre(repositoryProvider);
    }

    private void settOppTpsSurrogatiStrukturer() {
        HashSet<Familierelasjon> tilBarnaForeldreEn = new HashSet<>(Arrays.asList(relasjontilEldreBarn, relasjontilBarn));
        HashSet<Familierelasjon> tilBarnaForeldreTo = new HashSet<>(Collections.singletonList(relasjontilEldreBarn));
        HashSet<Familierelasjon> tilForeldreEn = new HashSet<>(Collections.singletonList(relasjontilMor));
        HashSet<Familierelasjon> tilForeldreTo = new HashSet<>(Arrays.asList(relasjontilMor, relasjontilFar));
        MOR_PINFO = new Personinfo.Builder().medAktørId(MOR_AKTØR_ID).medPersonIdent(MOR_IDENT).medNavn("Kari Dunk")
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE).medFødselsdato(LocalDate.of(1989, 12, 12)).medAdresse("Vei")
            .medFamilierelasjon(tilBarnaForeldreEn).build();
        FAR_PINFO = new Personinfo.Builder().medAktørId(FAR_AKTØR_ID).medPersonIdent(FAR_IDENT).medNavn("Ola Dunk")
            .medNavBrukerKjønn(NavBrukerKjønn.MANN).medFødselsdato(LocalDate.of(1991, 11, 11)).medAdresse("Vei")
            .medFamilierelasjon(tilBarnaForeldreTo).build();
        ELDRE_BARN_PINFO = new Personinfo.Builder().medAktørId(ELDRE_BARN_AKTØR_ID).medPersonIdent(ELDRE_BARN_IDENT).medFødselsdato(ELDRE_BARN_FØDT)
            .medNavBrukerKjønn(NavBrukerKjønn.MANN).medNavn("Dunk junior d.e.").medAdresse("Vei").medFamilierelasjon(tilForeldreTo).build();
        BARN_PINFO = new Personinfo.Builder().medAktørId(BARN_AKTØR_ID).medPersonIdent(BARN_IDENT).medFødselsdato(BARN_FØDT)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE).medNavn("Dunk junior d.y.").medAdresse("Vei").medFamilierelasjon(tilForeldreEn).build();
        when(tpsTjeneste.hentAktørForFnr(MOR_IDENT)).thenReturn(Optional.of(MOR_AKTØR_ID));
        when(tpsTjeneste.hentAktørForFnr(FAR_IDENT)).thenReturn(Optional.of(FAR_AKTØR_ID));
        when(tpsTjeneste.hentBrukerForAktør(MOR_AKTØR_ID)).thenReturn(Optional.of(MOR_PINFO));
        when(tpsTjeneste.hentBrukerForAktør(FAR_AKTØR_ID)).thenReturn(Optional.of(FAR_PINFO));
        when(tpsTjeneste.hentBrukerForAktør(BARN_AKTØR_ID)).thenReturn(Optional.of(BARN_PINFO));
        when(tpsTjeneste.hentBrukerForAktør(ELDRE_BARN_AKTØR_ID)).thenReturn(Optional.of(ELDRE_BARN_PINFO));
        when(tpsTjeneste.hentBrukerForFnr(ELDRE_BARN_IDENT)).thenReturn(Optional.of(ELDRE_BARN_PINFO));
        when(tpsTjeneste.hentBrukerForFnr(BARN_IDENT)).thenReturn(Optional.of(BARN_PINFO));
    }

    private void settOppTpsStrukturer(boolean medNyligFødt) {
        HashSet<Familierelasjon> tilBarna = new HashSet<>(
            medNyligFødt ? Arrays.asList(relasjontilEldreBarn, relasjontilBarn) : Arrays.asList(relasjontilEldreBarn));
        HashSet<Familierelasjon> tilForeldre = new HashSet<>(Arrays.asList(relasjontilMor, relasjontilFar));
        MOR_PINFO = new Personinfo.Builder().medAktørId(MOR_AKTØR_ID).medPersonIdent(MOR_IDENT).medNavn("Kari Dunk")
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE).medFødselsdato(LocalDate.of(1989, 12, 12)).medAdresse("Vei")
            .medFamilierelasjon(tilBarna).build();
        FAR_PINFO = new Personinfo.Builder().medAktørId(FAR_AKTØR_ID).medPersonIdent(FAR_IDENT).medNavn("Ola Dunk")
            .medNavBrukerKjønn(NavBrukerKjønn.MANN).medFødselsdato(LocalDate.of(1991, 11, 11)).medAdresse("Vei")
            .medFamilierelasjon(tilBarna).build();
        ELDRE_BARN_PINFO = new Personinfo.Builder().medAktørId(ELDRE_BARN_AKTØR_ID).medPersonIdent(ELDRE_BARN_IDENT).medFødselsdato(ELDRE_BARN_FØDT)
            .medNavBrukerKjønn(NavBrukerKjønn.MANN).medNavn("Dunk junior d.e.").medAdresse("Vei").medFamilierelasjon(tilForeldre).build();
        if (medNyligFødt) {
            BARN_PINFO = new Personinfo.Builder().medAktørId(BARN_AKTØR_ID).medPersonIdent(BARN_IDENT).medFødselsdato(BARN_FØDT)
                .medNavBrukerKjønn(NavBrukerKjønn.KVINNE).medNavn("Dunk junior d.y.").medAdresse("Vei").medFamilierelasjon(tilForeldre).build();
        } else {
            BARN_PINFO = new Personinfo.Builder().medAktørId(BARN_AKTØR_ID).medPersonIdent(BARN_IDENT).medFødselsdato(BARN_FØDT)
                .medNavBrukerKjønn(NavBrukerKjønn.KVINNE).medNavn("Dunk junior d.y.").medAdresse("Vei").build();
        }
        when(tpsTjeneste.hentAktørForFnr(MOR_IDENT)).thenReturn(Optional.of(MOR_AKTØR_ID));
        when(tpsTjeneste.hentAktørForFnr(FAR_IDENT)).thenReturn(Optional.of(FAR_AKTØR_ID));
        when(tpsTjeneste.hentBrukerForAktør(MOR_AKTØR_ID)).thenReturn(Optional.of(MOR_PINFO));
        when(tpsTjeneste.hentBrukerForAktør(FAR_AKTØR_ID)).thenReturn(Optional.of(FAR_PINFO));
        when(tpsTjeneste.hentBrukerForAktør(BARN_AKTØR_ID)).thenReturn(Optional.of(BARN_PINFO));
        when(tpsTjeneste.hentBrukerForAktør(ELDRE_BARN_AKTØR_ID)).thenReturn(Optional.of(ELDRE_BARN_PINFO));
        when(tpsTjeneste.hentBrukerForFnr(ELDRE_BARN_IDENT)).thenReturn(Optional.of(ELDRE_BARN_PINFO));
        when(tpsTjeneste.hentBrukerForFnr(BARN_IDENT)).thenReturn(Optional.of(BARN_PINFO));
    }

}
