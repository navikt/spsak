package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
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

    private static OrganisasjonsEnhet enhetNormal = new OrganisasjonsEnhet("4849", "NAV Tromsø");

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

        Behandling behandlingMor = opprettBehandling();
        when(enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(any(), any())).thenReturn(enhetNormal);

        OrganisasjonsEnhet morEnhet = behandlendeEnhetTjeneste.finnBehandlendeEnhetFraSøker(behandlingMor);

        assertThat(morEnhet.getEnhetId()).isEqualTo(enhetNormal.getEnhetId());
    }

    private Behandling opprettBehandling() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forAktør(MOR_AKTØR_ID);
        leggTilSøker(scenario, NavBrukerKjønn.KVINNE);
        return scenario.lagre(repositoryProvider);
    }

    private void settOppTpsStrukturer(boolean medNyligFødt) {
        MOR_PINFO = new Personinfo.Builder().medAktørId(MOR_AKTØR_ID).medPersonIdent(MOR_IDENT).medNavn("Kari Dunk")
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE).medFødselsdato(LocalDate.of(1989, 12, 12)).medAdresse("Vei")
            .build();
        FAR_PINFO = new Personinfo.Builder().medAktørId(FAR_AKTØR_ID).medPersonIdent(FAR_IDENT).medNavn("Ola Dunk")
            .medNavBrukerKjønn(NavBrukerKjønn.MANN).medFødselsdato(LocalDate.of(1991, 11, 11)).medAdresse("Vei")
            .build();
        ELDRE_BARN_PINFO = new Personinfo.Builder().medAktørId(ELDRE_BARN_AKTØR_ID).medPersonIdent(ELDRE_BARN_IDENT).medFødselsdato(ELDRE_BARN_FØDT)
            .medNavBrukerKjønn(NavBrukerKjønn.MANN).medNavn("Dunk junior d.e.").medAdresse("Vei").build();
        if (medNyligFødt) {
            BARN_PINFO = new Personinfo.Builder().medAktørId(BARN_AKTØR_ID).medPersonIdent(BARN_IDENT).medFødselsdato(BARN_FØDT)
                .medNavBrukerKjønn(NavBrukerKjønn.KVINNE).medNavn("Dunk junior d.y.").medAdresse("Vei").build();
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
