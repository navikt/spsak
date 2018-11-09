package no.nav.foreldrepenger.behandlingslager.hendelser.sortering;

import static java.time.Month.JANUARY;
import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.KVINNE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.HendelseVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OppgittAnnenPart;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OppgittAnnenPartBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class HendelseSorteringRepositoryImplTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    public Repository repository = repositoryRule.getRepository();

    private HendelseSorteringRepository sorteringRepository = new HendelseSorteringRepositoryImpl(repositoryRule.getEntityManager());

    @Test
    public void skal_hente_1_aktørId_fra_fagsak() {
        AktørId aktørId = new AktørId("100");
        genererFagsaker(1, aktørId);

        List<AktørId> finnAktørIder = new ArrayList<>(Arrays.asList(aktørId));
        List<AktørId> resultat = sorteringRepository.hentEksisterendeAktørIderMedSak(finnAktørIder);

        assertThat(resultat).isNotEmpty();
        assertThat(resultat).containsExactly(aktørId);
    }

    @Test
    public void skal_hente_1_aktørId_fra_søknad() {
        AktørId aktørId = new AktørId("200");
        genererSøknader(1, aktørId);

        List<AktørId> finnAktørIder = new ArrayList<>(Arrays.asList(aktørId));
        List<AktørId> resultat = sorteringRepository.hentEksisterendeAktørIderMedSak(finnAktørIder);

        assertThat(resultat).isNotEmpty();
        assertThat(resultat).containsExactly(aktørId);
    }

    @Test
    public void skal_returnere_tom_liste_når_aktør_id_ikke_er_knyttet_til_sak() {
        // setup
        genererFagsaker(4, new AktørId("100")); // aktør ID: 100 - 103
        genererSøknader(4, new AktørId("300")); // aktør ID: 300 - 303

        List<AktørId> finnAktørIder = new ArrayList<>(
            Arrays.asList(
                104L, 345L, 404L, 896L
            )).stream().map(AktørId::new).collect(Collectors.toList());

        // act
        List<AktørId> resultat = sorteringRepository.hentEksisterendeAktørIderMedSak(finnAktørIder);

        // assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_returnere_4_aktør_ider_fra_fagsaker() {
        genererFagsaker(6, new AktørId("100"));

        List<AktørId> finnAktørIder = new ArrayList<>(
            Arrays.asList(
                100L, 101L, 102L, 103L
            )).stream().map(AktørId::new).collect(Collectors.toList());

        List<AktørId> resultat = sorteringRepository.hentEksisterendeAktørIderMedSak(finnAktørIder);
        assertThat(resultat).hasSize(4);
    }

    @Test
    public void skal_returnere_4_aktør_ider_fra_søknader() {
        genererSøknader(6, new AktørId("200"));

        List<AktørId> finnAktørIder = new ArrayList<>(
            Arrays.asList(
                200L, 201L, 202L, 203L
            )).stream().map(AktørId::new).collect(Collectors.toList());

        List<AktørId> resultat = sorteringRepository.hentEksisterendeAktørIderMedSak(finnAktørIder);

        assertThat(resultat).hasSize(4);
    }

    @Test
    public void skal_returnere_aktør_ider_fra_fagsak_og_søknad_uten_duplikater() {
        List<Personinfo> brukereFagsakList = genererFagsaker(4, new AktørId("100"));
        genererSøknader(4, new AktørId("400"));

        OppgittAnnenPart annenPart = opprettAnnenPartMedAktørId(brukereFagsakList.get(0).getAktørId());
        FamilieHendelse familieHendelse = opprettFamilieHendelse();
        Søknad søknad = opprettSøknad(annenPart, familieHendelse);

        repository.lagre(annenPart);
        repository.lagre(familieHendelse);
        repository.lagre(søknad);
        repository.flushAndClear();

        List<AktørId> finnAktørIder = new ArrayList<>(
            Arrays.asList(
                100L, 103L, 401L, 102L,
                405L, 523L, 401L, 402L,
                100L, 893L
            )).stream().map(AktørId::new).collect(Collectors.toList());

        List<AktørId> forventet = new ArrayList<>(
            Arrays.asList(
                100L, 102L, 103L, 401L,
                402L
            )).stream().map(AktørId::new).collect(Collectors.toList());
        List<AktørId> feilRes = new ArrayList<>(
            Arrays.asList(
                100L, 103L, 401L, 102L,
                401L, 402L
            )).stream().map(AktørId::new).collect(Collectors.toList());

        List<AktørId> resultat = sorteringRepository.hentEksisterendeAktørIderMedSak(finnAktørIder);
        assertThat(resultat).isEqualTo(forventet);
        assertThat(resultat).isNotEqualTo(feilRes);
    }

    @Test
    public void skal_returnere_aktør_id_til_barn() {
        List<Personinfo> personinfoList = genererPersonInfo(1, new AktørId("100"));
        NavBruker navBruker = NavBruker.opprettNy(personinfoList.get(0));
        Fagsak fagsak = opprettFagsak(navBruker, FagsakYtelseType.FORELDREPENGER);

        repository.lagre(navBruker);
        repository.lagre(fagsak);
        repository.flushAndClear();

        List<AktørId> finnAktørIder = new ArrayList<>(
            Arrays.asList(
                100L, 657L
            )).stream().map(AktørId::new).collect(Collectors.toList());

        List<AktørId> resultat = sorteringRepository.hentEksisterendeAktørIderMedSak(finnAktørIder);

        assertThat(resultat).isNotEmpty();
        assertThat(resultat).contains(new AktørId(100L));
    }

    @Test
    public void skal_ikke_publisere_videre_hendelser_på_avsluttede_saker() {
        List<Personinfo> personinfoList = genererPersonInfo(3, new AktørId("100"));

        NavBruker navBrukerMedAvsluttetSak = NavBruker.opprettNy(personinfoList.get(0));
        Fagsak fagsak1 = opprettFagsak(navBrukerMedAvsluttetSak, FagsakYtelseType.FORELDREPENGER);
        fagsak1.setAvsluttet();

        NavBruker navBrukerMedÅpenSak = NavBruker.opprettNy(personinfoList.get(1));
        Fagsak fagsak2 = opprettFagsak(navBrukerMedÅpenSak, FagsakYtelseType.FORELDREPENGER);

        NavBruker navBrukerMedÅpenOgAvsluttetSak = NavBruker.opprettNy(personinfoList.get(2));
        Fagsak fagsak3 = opprettFagsak(navBrukerMedÅpenOgAvsluttetSak, FagsakYtelseType.FORELDREPENGER);
        Fagsak fagsak4 = opprettFagsak(navBrukerMedÅpenOgAvsluttetSak, FagsakYtelseType.FORELDREPENGER);
        fagsak4.setAvsluttet();

        repository.lagre(navBrukerMedAvsluttetSak);
        repository.lagre(navBrukerMedÅpenSak);
        repository.lagre(navBrukerMedÅpenOgAvsluttetSak);
        repository.lagre(fagsak1);
        repository.lagre(fagsak2);
        repository.lagre(fagsak3);
        repository.lagre(fagsak4);
        repository.flushAndClear();

        List<AktørId> aktørList = personinfoList.stream().map(Personinfo::getAktørId).collect(Collectors.toList());
        List<AktørId> resultat = sorteringRepository.hentEksisterendeAktørIderMedSak(aktørList);

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat).contains(navBrukerMedÅpenSak.getAktørId());
        assertThat(resultat).contains(navBrukerMedÅpenOgAvsluttetSak.getAktørId());
    }

    private Fagsak opprettFagsak(NavBruker bruker, FagsakYtelseType fagsakYtelseType) {
        return Fagsak.opprettNy(fagsakYtelseType, bruker);
    }

    private Søknad opprettSøknad(OppgittAnnenPart annenPart, FamilieHendelse familieHendelse) {
        return new SøknadEntitet.Builder()
            .medSøknadsdato(LocalDate.now().minusDays(1))
            .medFamilieHendelse(familieHendelse)
            .medSøknadAnnenPart(annenPart)
            .build();
    }

    private OppgittAnnenPart opprettAnnenPartMedAktørId(AktørId aktørId) {
        return new OppgittAnnenPartBuilder().medAktørId(aktørId).build();
    }

    private FamilieHendelse opprettFamilieHendelse() {
        FamilieHendelseBuilder fhBuilder = FamilieHendelseBuilder.oppdatere(Optional.empty(), HendelseVersjonType.SØKNAD);
        fhBuilder.medFødselsDato(LocalDate.now()).medAntallBarn(1);
        return fhBuilder.build();
    }

    private List<Personinfo> genererFagsaker(int antall, AktørId startAktørId) {
        List<Personinfo> personinfoList = genererPersonInfo(antall, startAktørId);

        List<Fagsak> fagsaker = new ArrayList<>();
        List<NavBruker> navBrukere = new ArrayList<>();

        for (Personinfo pInfo : personinfoList) {
            NavBruker bruker = NavBruker.opprettNy(pInfo);
            navBrukere.add(bruker);

            fagsaker.add(opprettFagsak(bruker, FagsakYtelseType.FORELDREPENGER));
        }

        if (!fagsaker.isEmpty()) {
            repository.lagre(navBrukere);
            repository.lagre(fagsaker);
            repository.flushAndClear();
        }

        return personinfoList;
    }

    private List<Personinfo> genererSøknader(int antall, AktørId startAktørId) {
        List<Personinfo> personinfoListe = genererPersonInfo(antall, startAktørId);
        List<NavBruker> navBrukere = new ArrayList<>();
        List<OppgittAnnenPart> annenParter = new ArrayList<>();
        List<Søknad> søknader = new ArrayList<>();

        FamilieHendelse familieHendelse = opprettFamilieHendelse();

        for (Personinfo pInfo : personinfoListe) {
            OppgittAnnenPart annenPart = opprettAnnenPartMedAktørId(pInfo.getAktørId());
            annenParter.add(annenPart);

            søknader.add(opprettSøknad(annenPart, familieHendelse));

            navBrukere.add(NavBruker.opprettNy(pInfo));
        }

        repository.lagre(annenParter);
        repository.lagre(familieHendelse);
        repository.lagre(navBrukere);
        repository.lagre(søknader);
        repository.flushAndClear();

        return personinfoListe;
    }

    private List<Personinfo> genererPersonInfo(int antall, AktørId startAktørId) {
        String fnr = "123456678901";

        List<Personinfo> personinfoList = new ArrayList<>();
        for (int i = 0; i < antall; i++) {
            personinfoList.add(
                new Personinfo.Builder()
                    .medAktørId(startAktørId)
                    .medPersonIdent(new PersonIdent(fnr))
                    .medNavn("Kari Nordmann")
                    .medFødselsdato(LocalDate.of(1990, JANUARY, 1))
                    .medNavBrukerKjønn(KVINNE)
                    .medForetrukketSpråk(Språkkode.nb)
                    .build()
            );
            startAktørId = new AktørId(Long.valueOf(startAktørId.getId()) + 1);
        }
        return personinfoList;
    }
}
