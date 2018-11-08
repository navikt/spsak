package no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
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
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.BeregnEkstraFlerbarnsukerTjeneste;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class BeregnEkstraFlerbarnsukerTjenesteImplTest {
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private YtelsesFordelingRepository ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();


    private Long AKTØRID = 1L;

    @Test
    public void bådeMorOgFarHarRettTerminEttBarn() {
        LocalDate termindato = LocalDate.now().plusMonths(4);
        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        Behandling behandling = opprettBehandlingForMor(AKTØRID, Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        FamilieHendelseBuilder familieHendelseBuilder = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling);
        final FamilieHendelseBuilder søknadHendelse = familieHendelseBuilder
            .medAntallBarn(1)
            .medTerminbekreftelse(familieHendelseBuilder.getTerminbekreftelseBuilder()
                .medTermindato(termindato)
                .medUtstedtDato(LocalDate.now()).medNavnPå("Doktor"));
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, true, false);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste = new BeregnEkstraFlerbarnsukerTjenesteImpl(repositoryProvider);
        Integer antallEkstraDager = beregnEkstraFlerbarnsukerTjeneste.beregneEkstraFlerbarnsuker(behandling);
        repository.flushAndClear();

        // Assert
        assertThat(antallEkstraDager).isEqualTo(0);
    }

    @Test
    public void bådeMorOgFarHarRettTerminToBarn() {
        LocalDate termindato = LocalDate.now().plusMonths(7);
        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        Behandling behandling = opprettBehandlingForMor(AKTØRID, Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        FamilieHendelseBuilder familieHendelseBuilder = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling);
        final FamilieHendelseBuilder søknadHendelse = familieHendelseBuilder
            .medAntallBarn(2)
            .medTerminbekreftelse(familieHendelseBuilder.getTerminbekreftelseBuilder()
                .medTermindato(termindato)
                .medUtstedtDato(LocalDate.now()).medNavnPå("Doktor"));
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, true, false);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste = new BeregnEkstraFlerbarnsukerTjenesteImpl(repositoryProvider);
        Integer antallEkstraDager = beregnEkstraFlerbarnsukerTjeneste.beregneEkstraFlerbarnsuker(behandling);
        repository.flushAndClear();

        // Assert
        assertThat(antallEkstraDager).isNotEqualTo(0);
        assertThat(antallEkstraDager).isEqualTo(17);
    }

    @Test
    public void bådeMorOgFarHarRettTerminTreBarn() {
        LocalDate termindato = LocalDate.now().plusMonths(8);
        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        Behandling behandling = opprettBehandlingForMor(AKTØRID, Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        FamilieHendelseBuilder familieHendelseBuilder = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling);
        final FamilieHendelseBuilder søknadHendelse = familieHendelseBuilder
            .medAntallBarn(3)
            .medTerminbekreftelse(familieHendelseBuilder.getTerminbekreftelseBuilder()
                .medTermindato(termindato)
                .medUtstedtDato(LocalDate.now()).medNavnPå("Doktor"));
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, true, false);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste = new BeregnEkstraFlerbarnsukerTjenesteImpl(repositoryProvider);
        Integer antallEkstraDager = beregnEkstraFlerbarnsukerTjeneste.beregneEkstraFlerbarnsuker(behandling);
        repository.flushAndClear();

        // Assert
        assertThat(antallEkstraDager).isNotEqualTo(0);
        assertThat(antallEkstraDager).isEqualTo(46);
    }

    @Test
    public void morAleneomsorgFødselEttBarn() {
        LocalDate fødselsdato = LocalDate.now().minusWeeks(1);
        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk80();
        Behandling behandling = opprettBehandlingForMor(AKTØRID + 2, Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        final FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, true, true);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste = new BeregnEkstraFlerbarnsukerTjenesteImpl(repositoryProvider);
        Integer antallEkstraDager = beregnEkstraFlerbarnsukerTjeneste.beregneEkstraFlerbarnsuker(behandling);
        repository.flushAndClear();

        // Assert
        assertThat(antallEkstraDager).isEqualTo(0);
    }


    @Test
    public void bareMorHarRettFødselTvillinger100() {
        LocalDate fødselsdato = LocalDate.now().minusWeeks(1);
        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        Behandling behandling = opprettBehandlingForMor(AKTØRID + 3, Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        final FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(2)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, true, false);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste = new BeregnEkstraFlerbarnsukerTjenesteImpl(repositoryProvider);
        Integer antallEkstraDager = beregnEkstraFlerbarnsukerTjeneste.beregneEkstraFlerbarnsuker(behandling);
        repository.flushAndClear();

        // Assert
        assertThat(antallEkstraDager).isNotEqualTo(0); // Per 01.08.2018 er det 17
        assertThat(antallEkstraDager).isGreaterThan(14);
    }

    @Test
    public void barefarHarRettFødselTreBarn100() {
        LocalDate fødselsdato = LocalDate.now().minusWeeks(1);
        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        Behandling behandling = opprettBehandlingForFar(AKTØRID + 4, Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        final FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(3)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, true, false);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste = new BeregnEkstraFlerbarnsukerTjenesteImpl(repositoryProvider);
        Integer antallEkstraDager = beregnEkstraFlerbarnsukerTjeneste.beregneEkstraFlerbarnsuker(behandling);
        repository.flushAndClear();

        // Assert
        assertThat(antallEkstraDager).isNotEqualTo(0); // Per 01.08.2018 er det 46
        assertThat(antallEkstraDager).isGreaterThan(40);
    }

    @Test
    public void bareMorHarRettFødselTvillinger80() {
        LocalDate fødselsdato = LocalDate.now().minusWeeks(1);
        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk80();
        Behandling behandling = opprettBehandlingForMor(AKTØRID + 5, Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        final FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(2)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);


        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, true, false);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste = new BeregnEkstraFlerbarnsukerTjenesteImpl(repositoryProvider);
        Integer antallEkstraDager = beregnEkstraFlerbarnsukerTjeneste.beregneEkstraFlerbarnsuker(behandling);
        repository.flushAndClear();

        // Assert
        assertThat(antallEkstraDager).isNotEqualTo(0); // Per 01.08.2018 er det 21
        assertThat(antallEkstraDager).isGreaterThan(19);
    }

    @Test
    public void barefarHarRettFødselTreBarn80() {
        LocalDate fødselsdato = LocalDate.now().minusWeeks(1);
        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk80();
        Behandling behandling = opprettBehandlingForFar(AKTØRID + 6, Dekningsgrad.grad(dekningsgrad.getDekningsgrad()));

        final FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(3)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, true, false);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        byggOgLagreSøknad(dekningsgrad, rettighet, behandling);

        // Act
        BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste = new BeregnEkstraFlerbarnsukerTjenesteImpl(repositoryProvider);
        Integer antallEkstraDager = beregnEkstraFlerbarnsukerTjeneste.beregneEkstraFlerbarnsuker(behandling);
        repository.flushAndClear();

        // Assert
        assertThat(antallEkstraDager).isNotEqualTo(0); // Per 01.08.2018 er det 56
        assertThat(antallEkstraDager).isGreaterThan(50);
    }


    private Behandling opprettBehandlingForMor(Long aktørId, Dekningsgrad dekningsgrad) {
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
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(fagsak, dekningsgrad);

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

    private Behandling opprettBehandlingForFar(Long aktørId, Dekningsgrad dekningsgrad) {
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
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(fagsak, dekningsgrad);

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
