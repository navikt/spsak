package no.nav.foreldrepenger.behandling.impl;

import static java.time.Month.JANUARY;
import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.MANN;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.aktør.BrukerTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.felles.testutilities.db.Repository;

@SuppressWarnings("deprecation")
public class FagsakTjenesteTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private Repository repository = repoRule.getRepository();
    private FagsakTjenesteImpl tjeneste;
    private BrukerTjeneste brukerTjeneste;

    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final PersonopplysningRepository personopplysningRepository = repositoryProvider.getPersonopplysningRepository();

    @Mock
    private SøknadRepository søknadRepository;

    private Fagsak fagsak;
    private Personinfo personinfo;

    private final AktørId forelderAktørId = new AktørId("154523");
    private LocalDate forelderFødselsdato = LocalDate.of(1990, JANUARY, 1);

    @Before
    public void oppsett() {
        tjeneste = new FagsakTjenesteImpl(new BehandlingRepositoryProviderImpl(entityManager), null);

        brukerTjeneste = new BrukerTjeneste(new NavBrukerRepositoryImpl(entityManager));

        personinfo = new Personinfo.Builder()
            .medAktørId(forelderAktørId)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medNavn("Kari Nordmann")
            .medFødselsdato(forelderFødselsdato)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medForetrukketSpråk(Språkkode.nb)
            .build();

        Fagsak fagsak = lagNyFagsak(personinfo);
        
        this.fagsak  = fagsak;
    }

    private Fagsak lagNyFagsak(Personinfo personinfo) {
        NavBruker søker = NavBruker.opprettNy(personinfo);
        Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.ENGANGSTØNAD, søker);
        tjeneste.opprettFagsak(fagsak, personinfo);
        return fagsak;
    }

    @Test
    public void skal_oppdatere_fagsakrelasjon_med_barn_og_endret_kjønn() {

        LocalDate barnsFødselsdato = LocalDate.of(2017, JANUARY, 1);
        AktørId barnAktørId = new AktørId("123");

        // Arrange
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);
        Behandling behandling = behandlingBuilder.build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        // TODO opplegg for å opprette PersonInformasjon og PersonopplysningerAggregat på en enklere måte
        final PersonInformasjonBuilder medBarnOgOppdatertKjønn = personopplysningRepository.opprettBuilderForRegisterdata(behandling);
        medBarnOgOppdatertKjønn
            .leggTil(
                medBarnOgOppdatertKjønn.getPersonopplysningBuilder(barnAktørId)
                    .medKjønn(MANN)
                    .medNavn("Baby Nordmann")
                    .medFødselsdato(barnsFødselsdato)
                    .medSivilstand(SivilstandType.UGIFT)
                    .medRegion(Region.NORDEN))
            .leggTil(
                medBarnOgOppdatertKjønn.getPersonopplysningBuilder(forelderAktørId)
                    .medKjønn(MANN)
                    .medSivilstand(SivilstandType.UGIFT)
                    .medFødselsdato(forelderFødselsdato)
                    .medRegion(Region.NORDEN)
                    .medNavn("Kari Nordmann"))
            .leggTil(
                medBarnOgOppdatertKjønn
                    .getRelasjonBuilder(forelderAktørId, barnAktørId, RelasjonsRolleType.BARN)
                    .harSammeBosted(true))
            .leggTil(
                medBarnOgOppdatertKjønn
                    .getRelasjonBuilder(barnAktørId, forelderAktørId, RelasjonsRolleType.FARA)
                    .harSammeBosted(true));

        Whitebox.setInternalState(fagsak, "fagsakStatus", FagsakStatus.LØPENDE); // dirty, men eksponerer ikke status nå
        personopplysningRepository.lagre(behandling, medBarnOgOppdatertKjønn);
        final PersonopplysningGrunnlag personopplysningGrunnlag = personopplysningRepository.hentPersonopplysninger(behandling);

        PersonopplysningerAggregat personopplysningerAggregat = new PersonopplysningerAggregat(personopplysningGrunnlag,
            forelderAktørId, DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now()), Collections.emptyMap());

        // Act
        tjeneste.oppdaterFagsak(behandling, personopplysningerAggregat, personopplysningerAggregat.getBarna());

        // Assert
        List<Fagsak> oppdatertFagsak = repository.hentAlle(Fagsak.class);
        assertThat(oppdatertFagsak).hasSize(1);
        assertThat(oppdatertFagsak.get(0).getRelasjonsRolleType().getKode()).isEqualTo(RelasjonsRolleType.FARA.getKode());
    }

    @Test
    public void opprettFlereFagsakerSammeBruker() throws Exception {
        // Opprett en fagsak i systemet
        Whitebox.setInternalState(fagsak, "fagsakStatus", FagsakStatus.LØPENDE); // dirty, men eksponerer ikke status nå

        // Ifølgeregler i mottak skal vi opprette en nyTerminbekreftelse sak hvis vi ikke har sak nyere enn 10 mnd:
        NavBruker søker = brukerTjeneste.hentEllerOpprettFraAktorId(personinfo);
        Fagsak fagsakNy = Fagsak.opprettNy(FagsakYtelseType.ENGANGSTØNAD, søker);
        tjeneste.opprettFagsak(fagsakNy, personinfo);
        assertThat(fagsak.getNavBruker().getId()).as("Forventer at fagsakene peker til samme bruker")
            .isEqualTo(fagsakNy.getNavBruker().getId());
    }

}
