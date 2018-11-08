package no.nav.foreldrepenger.domene.uttak.beregnkontoer.impl;

import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FEDREKVOTE;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FELLESPERIODE;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FORELDREPENGER;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgrad;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgradEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.uttak.beregnkontoer.BeregnStønadskontoerTjeneste;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class BeregnStønadskontoerTjenesteImplTest {
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private YtelsesFordelingRepository ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
    private FagsakRelasjonRepository fagsakRelasjonRepository = repositoryProvider.getFagsakRelasjonRepository();


    private Long AKTØRID = 1L;

    @Test
    public void bådeMorOgFarHarRettTermin() {
        LocalDate termindato = LocalDate.now().plusMonths(4);
        Behandling behandling = opprettBehandlingForMor(AKTØRID);

        FamilieHendelseBuilder familieHendelseBuilder = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling);
        final FamilieHendelseBuilder søknadHendelse = familieHendelseBuilder
            .medAntallBarn(1)
            .medTerminbekreftelse(familieHendelseBuilder.getTerminbekreftelseBuilder()
                .medTermindato(termindato)
                .medUtstedtDato(LocalDate.now()).medNavnPå("Doktor"));
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);
        fagsakRelasjonRepository.opprettRelasjon(behandling.getFagsak(), Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, true, false);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnStønadskontoerTjeneste beregnStønadskontoerTjeneste = new BeregnStønadskontoerTjenesteImpl(repositoryProvider);
        beregnStønadskontoerTjeneste.beregnStønadskontoer(behandling);
        repository.flushAndClear();

        // Assert
        Behandling lagretBehandling = behandlingRepository.hentBehandling(behandling.getId());
        Optional<Stønadskontoberegning> stønadskontoberegning = repositoryProvider.getFagsakRepository().finnFagsakRelasjonFor(lagretBehandling.getFagsak()).getStønadskontoberegning();
        assertThat(stønadskontoberegning).isPresent();
        Set<Stønadskonto> stønadskontoer = stønadskontoberegning.get().getStønadskontoer();

        assertThat(stønadskontoer).hasSize(4);
        assertThat(stønadskontoer).extracting(Stønadskonto::getStønadskontoType)
            .containsExactlyInAnyOrder(FORELDREPENGER_FØR_FØDSEL, MØDREKVOTE, FEDREKVOTE, FELLESPERIODE);
    }

    @Test
    public void bådeMorOgFarHarRettFødsel() {
        LocalDate fødselsdato = LocalDate.now().minusWeeks(1);
        Behandling behandling = opprettBehandlingForMor(AKTØRID + 1);

        final FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);
        fagsakRelasjonRepository.opprettRelasjon(behandling.getFagsak(), Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, true, false);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnStønadskontoerTjeneste beregnStønadskontoerTjeneste = new BeregnStønadskontoerTjenesteImpl(repositoryProvider);
        beregnStønadskontoerTjeneste.beregnStønadskontoer(behandling);
        repository.flushAndClear();

        // Assert
        Behandling lagretBehandling = behandlingRepository.hentBehandling(behandling.getId());
        Optional<Stønadskontoberegning> stønadskontoberegning = repositoryProvider.getFagsakRepository().finnFagsakRelasjonFor(lagretBehandling.getFagsak()).getStønadskontoberegning();
        assertThat(stønadskontoberegning).isPresent();
        Set<Stønadskonto> stønadskontoer = stønadskontoberegning.get().getStønadskontoer();

        assertThat(stønadskontoer).hasSize(4);
        assertThat(stønadskontoer).extracting(Stønadskonto::getStønadskontoType)
            .containsExactlyInAnyOrder(FORELDREPENGER_FØR_FØDSEL, MØDREKVOTE, FEDREKVOTE, FELLESPERIODE);
    }

    @Test
    public void morAleneomsorgFødsel() {
        LocalDate fødselsdato = LocalDate.now().minusWeeks(1);
        Behandling behandling = opprettBehandlingForMor(AKTØRID + 2);

        final FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk80();
        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);
        fagsakRelasjonRepository.opprettRelasjon(behandling.getFagsak(), Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, true, true);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnStønadskontoerTjeneste beregnStønadskontoerTjeneste = new BeregnStønadskontoerTjenesteImpl(repositoryProvider);
        beregnStønadskontoerTjeneste.beregnStønadskontoer(behandling);
        repository.flushAndClear();

        // Assert
        Behandling lagretBehandling = behandlingRepository.hentBehandling(behandling.getId());
        Optional<Stønadskontoberegning> stønadskontoberegning = repositoryProvider.getFagsakRepository().finnFagsakRelasjonFor(lagretBehandling.getFagsak()).getStønadskontoberegning();
        assertThat(stønadskontoberegning).isPresent();
        Set<Stønadskonto> stønadskontoer = stønadskontoberegning.get().getStønadskontoer();

        assertThat(stønadskontoer).hasSize(2);
        assertThat(stønadskontoer).extracting(Stønadskonto::getStønadskontoType)
            .containsExactlyInAnyOrder(FORELDREPENGER_FØR_FØDSEL, FORELDREPENGER);
    }


    @Test
    public void bareMorHarRettFødsel() {
        LocalDate fødselsdato = LocalDate.now().minusWeeks(1);
        Behandling behandling = opprettBehandlingForMor(AKTØRID + 3);

        final FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);
        fagsakRelasjonRepository.opprettRelasjon(behandling.getFagsak(), Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, true, false);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnStønadskontoerTjeneste beregnStønadskontoerTjeneste = new BeregnStønadskontoerTjenesteImpl(repositoryProvider);
        beregnStønadskontoerTjeneste.beregnStønadskontoer(behandling);
        repository.flushAndClear();

        // Assert
        Behandling lagretBehandling = behandlingRepository.hentBehandling(behandling.getId());
        Optional<Stønadskontoberegning> stønadskontoberegning = repositoryProvider.getFagsakRepository().finnFagsakRelasjonFor(lagretBehandling.getFagsak()).getStønadskontoberegning();
        assertThat(stønadskontoberegning).isPresent();
        Set<Stønadskonto> stønadskontoer = stønadskontoberegning.get().getStønadskontoer();

        assertThat(stønadskontoer).hasSize(2);
        assertThat(stønadskontoer).extracting(Stønadskonto::getStønadskontoType)
            .containsExactlyInAnyOrder(FORELDREPENGER_FØR_FØDSEL, FORELDREPENGER);
    }

    @Test
    public void barefarHarRettFødsel() {
        LocalDate fødselsdato = LocalDate.now().minusWeeks(1);
        Behandling behandling = opprettBehandlingForFar(AKTØRID + 4);

        final FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);
        fagsakRelasjonRepository.opprettRelasjon(behandling.getFagsak(), Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, true, false);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnStønadskontoerTjeneste beregnStønadskontoerTjeneste = new BeregnStønadskontoerTjenesteImpl(repositoryProvider);
        beregnStønadskontoerTjeneste.beregnStønadskontoer(behandling);
        repository.flushAndClear();

        // Assert
        Behandling lagretBehandling = behandlingRepository.hentBehandling(behandling.getId());
        Optional<Stønadskontoberegning> stønadskontoberegning = repositoryProvider.getFagsakRelasjonRepository().finnRelasjonFor(lagretBehandling.getFagsak()).getStønadskontoberegning();
        assertThat(stønadskontoberegning).isPresent();
        Set<Stønadskonto> stønadskontoer = stønadskontoberegning.get().getStønadskontoer();

        assertThat(stønadskontoer).hasSize(1);
        assertThat(stønadskontoer).extracting(Stønadskonto::getStønadskontoType)
            .containsExactlyInAnyOrder(FORELDREPENGER);
    }


    private Behandling opprettBehandlingForMor(Long aktørId) {
        Fagsak fagsak = FagsakBuilder.nyForeldrepengerForMor().medBrukerPersonInfo(new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(new AktørId(aktørId))
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12312312312"))
            .medForetrukketSpråk(Språkkode.nb)
            .build()).build();

        repositoryProvider.getFagsakRepository().opprettNy(fagsak);

        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);

        Behandling behandling = behandlingBuilder.build();
        behandling.setAnsvarligSaksbehandler("VL");
        repository.lagre(behandling);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.opprettFor(behandling);

        VilkårResultat vilkårResultat = VilkårResultat.builder().medVilkårResultatType(VilkårResultatType.INNVILGET).buildFor(behandling);
        repository.lagre(vilkårResultat);

        repository.lagre(behandlingsresultat);
        repository.flushAndClear();

        return behandling;
    }

    private Behandling opprettBehandlingForFar(Long aktørId) {
        Fagsak fagsak = FagsakBuilder.nyForeldrepengesak(RelasjonsRolleType.FARA).medBrukerPersonInfo(new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(new AktørId(aktørId))
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.MANN)
            .medPersonIdent(new PersonIdent("12312312312"))
            .medForetrukketSpråk(Språkkode.nb)
            .build()).build();

        repositoryProvider.getFagsakRepository().opprettNy(fagsak);

        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);

        Behandling behandling = behandlingBuilder.build();
        behandling.setAnsvarligSaksbehandler("VL");
        repository.lagre(behandling);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.opprettFor(behandling);
        repository.lagre(behandlingsresultat);

        VilkårResultat vilkårResultat = VilkårResultat.builder().medVilkårResultatType(VilkårResultatType.INNVILGET).buildFor(behandling);
        repository.lagre(vilkårResultat);

        repository.flushAndClear();

        return behandling;
    }


    private void byggOgLagreSøknad(OppgittDekningsgrad dekningsgrad, OppgittRettighetEntitet rettighet, Behandling behandling) {
        final Søknad søknad = new SøknadEntitet.Builder()
            .medSøknadsdato(LocalDate.now())
            .medMottattDato(LocalDate.now())
            .medElektroniskRegistrert(true)
            .medDekningsgrad(dekningsgrad)
            .medRettighet(rettighet)
            .medFamilieHendelse(repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getSøknadVersjon()).build();
        repositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);
        repository.flushAndClear();
    }


}
