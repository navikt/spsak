package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.Familierelasjon;
import no.nav.foreldrepenger.behandlingslager.aktør.GeografiskTilknytning;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.EnhetsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public class BehandlendeEnhetTjenesteImplTest {

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
    private static LocalDate ELDRE_BARN_FØDT = LocalDate.of(2006,6,6);
    private static LocalDate BARN_FØDT = LocalDate.of(2018,3,3);

    private static Familierelasjon relasjontilEldreBarn = new Familierelasjon(ELDRE_BARN_IDENT, RelasjonsRolleType.BARN, ELDRE_BARN_FØDT, "Vei", true);
    private static Familierelasjon relasjontilBarn = new Familierelasjon(BARN_IDENT, RelasjonsRolleType.BARN, BARN_FØDT, "Vei", true);
    private static Familierelasjon relasjontilMor = new Familierelasjon(MOR_IDENT, RelasjonsRolleType.MORA, LocalDate.of(1989,12,12), "Vei", true);
    private static Familierelasjon relasjontilFar = new Familierelasjon(FAR_IDENT, RelasjonsRolleType.FARA, LocalDate.of(1991,11,11), "Vei", true);

    private static OrganisasjonsEnhet enhetNormal = new OrganisasjonsEnhet("4849", "NAV Tromsø");
    private static OrganisasjonsEnhet enhetKode6 = new OrganisasjonsEnhet("2103", "NAV Viken");

    @SuppressWarnings("unused")
    private static GeografiskTilknytning tilknytningNormal = new GeografiskTilknytning("0219", null);
    private static GeografiskTilknytning tilknytningKode6 = new GeografiskTilknytning("0219", "SPSF");

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private TpsTjeneste tpsTjeneste;
    private EnhetsTjeneste enhetsTjeneste;
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;


    @Before
    public void oppsett() {
        tpsTjeneste = mock(TpsTjeneste.class);
        enhetsTjeneste = mock(EnhetsTjeneste.class);
        behandlendeEnhetTjeneste = new BehandlendeEnhetTjenesteImpl(tpsTjeneste, enhetsTjeneste, repositoryProvider);
    }

    @Test
    public void finn_mors_enhet_normal_sak() {
        // Oppsett
        settOppTpsStrukturer(false);

        Behandling behandlingMor = opprettBehandlingMorSøkerFødselTermin(LocalDate.now(), FAR_AKTØR_ID);
        when(enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(any(), any())).thenReturn(enhetNormal);

        OrganisasjonsEnhet morEnhet = behandlendeEnhetTjeneste.finnBehandlendeEnhetFraSøker(behandlingMor);

        assertThat(morEnhet.getEnhetId()).isEqualTo(enhetNormal.getEnhetId());
    }

    @Test
    public void finn_enhet_etter_kobling_far_relasjon_kode6() {
        // Oppsett
        settOppTpsStrukturer(false);
        when(enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(any(), any())).thenReturn(enhetNormal);
        when(enhetsTjeneste.oppdaterEnhetSjekkOppgitte(any(), any(), any())).thenReturn(Optional.empty());

        Behandling behandlingMor = opprettBehandlingMorSøkerFødselRegistrertTPS(LocalDate.now(),1,  FAR_AKTØR_ID);
        Behandling behandlingFar = opprettBehandlingFarSøkerFødselRegistrertITps(LocalDate.now(), 1, MOR_AKTØR_ID);
        behandlingFar.setBehandlendeEnhet(enhetKode6);

        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(behandlingMor.getFagsak(), Dekningsgrad._100);
        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(behandlingMor.getFagsak(), behandlingFar.getFagsak());

        when(tpsTjeneste.hentGeografiskTilknytning(ELDRE_BARN_IDENT)).thenReturn(tilknytningKode6);
        when(enhetsTjeneste.oppdaterEnhetSjekkRegistrerteRelasjoner(any(), any(), any(), any(), any())).thenReturn(Optional.of(enhetKode6));
        when(enhetsTjeneste.enhetsPresedens(any(), any(), anyBoolean())).thenReturn(enhetKode6);
    }



    private Behandling opprettBehandlingMorSøkerFødselTermin(LocalDate termindato, AktørId annenPart) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(MOR_AKTØR_ID);
        leggTilSøker(scenario, NavBrukerKjønn.KVINNE);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingMorSøkerFødselRegistrertTPS(LocalDate fødselsdato, int antallBarn, AktørId annenPart) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(MOR_AKTØR_ID);
        leggTilSøker(scenario, NavBrukerKjønn.KVINNE);
        return scenario.lagre(repositoryProvider);
    }


    private Behandling opprettBehandlingFarSøkerFødselRegistrertITps(LocalDate fødseldato, int antallBarnSøknad, AktørId annenPart) {
        ScenarioFarSøkerForeldrepenger scenario = ScenarioFarSøkerForeldrepenger.forFødselMedGittAktørId(FAR_AKTØR_ID);
        leggTilSøker(scenario, NavBrukerKjønn.MANN);
        return scenario.lagre(repositoryProvider);
    }



    private void settOppTpsStrukturer(boolean medNyligFødt) {
        HashSet<Familierelasjon> tilBarna = new HashSet<>(medNyligFødt ? Arrays.asList(relasjontilEldreBarn, relasjontilBarn) : Arrays.asList(relasjontilEldreBarn));
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
