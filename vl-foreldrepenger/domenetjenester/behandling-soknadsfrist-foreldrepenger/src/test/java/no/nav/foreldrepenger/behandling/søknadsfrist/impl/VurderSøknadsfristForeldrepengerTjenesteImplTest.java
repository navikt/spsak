package no.nav.foreldrepenger.behandling.søknadsfrist.impl;

import no.nav.foreldrepenger.domene.typer.AktørId;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.søknadsfrist.VurderSøknadsfristAksjonspunktDto;
import no.nav.foreldrepenger.behandling.søknadsfrist.SøknadsfristForeldrepengerTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class VurderSøknadsfristForeldrepengerTjenesteImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private SøknadsfristForeldrepengerTjeneste tjeneste;
    private BehandlingRepositoryProvider behandlingRepositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private BehandlingRepository behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();


    private Behandling behandling;

    private static final int MND_SØKNADSFRIST_ETTER_FØRSTE_UTTAK = 3;


    @Before
    public void oppsett() {
        tjeneste = new SøknadsfristForeldrepengerTjenesteImpl(behandlingRepositoryProvider, MND_SØKNADSFRIST_ETTER_FØRSTE_UTTAK);
        behandling = byggBehandlingMedBehandlingsresultat();
    }


    @Test
    public void skal_oppdatere_behandlingsresultet_med_uttaksperiodegrense() {
        // Arrange
        LocalDate nyMottattDato = LocalDate.of(2018,1,15);
        LocalDate førsteLovligeUttaksdag = LocalDate.of(2017,10,1);
        VurderSøknadsfristAksjonspunktDto adapter = new VurderSøknadsfristAksjonspunktDto(nyMottattDato, "Begrunnelse");

        // Act
        tjeneste.lagreVurderSøknadsfristResultat(behandling, adapter);

        // Assert
        behandling = behandlingRepository.hentBehandling(behandling.getId());
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();

        Set<Uttaksperiodegrense> uttaksperiodegrenseList =  behandlingsresultat.getAlleUttaksperiodegrenser();
        assertThat(uttaksperiodegrenseList).hasSize(1);

        Uttaksperiodegrense uttaksperiodegrense = uttaksperiodegrenseList.iterator().next();
        assertThat(uttaksperiodegrense.getErAktivt()).isTrue();
        assertThat(uttaksperiodegrense.getMottattDato()).isEqualTo(nyMottattDato);
        assertThat(uttaksperiodegrense.getFørsteLovligeUttaksdag()).isEqualTo(førsteLovligeUttaksdag);
    }

    @Test
    public void skal_oppdatere_behandlingsresultat_med_eksisterende_uttaksperiodegrense() {
        // Arrange
        LocalDate gammelMottatDato = LocalDate.of(2018,3,15);
        String begrunnelse = "Begrunnelse";
        VurderSøknadsfristAksjonspunktDto gammelSøknadsfristGrense = new VurderSøknadsfristAksjonspunktDto(gammelMottatDato, begrunnelse);
        tjeneste.lagreVurderSøknadsfristResultat(behandling, gammelSøknadsfristGrense);

        LocalDate nyMottattDato = LocalDate.of(2018,2,28);
        LocalDate førsteLovligeUttaksdag = LocalDate.of(2017,11,1);
        VurderSøknadsfristAksjonspunktDto nySøknadsfristGrense = new VurderSøknadsfristAksjonspunktDto(nyMottattDato, begrunnelse);

        // Act
        tjeneste.lagreVurderSøknadsfristResultat(behandling, nySøknadsfristGrense);

        // Assert
        behandling = behandlingRepository.hentBehandling(behandling.getId());
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();

        List<Uttaksperiodegrense> inaktivUttaksperiodegrenseList =  behandlingsresultat.getAlleUttaksperiodegrenser().stream()
            .filter(uttaksperiodegrense ->  !uttaksperiodegrense.getErAktivt()).collect(Collectors.toList());
        assertThat(inaktivUttaksperiodegrenseList).hasSize(1);

        Uttaksperiodegrense inaktivUttaksperiodegrense = inaktivUttaksperiodegrenseList.get(0);
        assertThat(inaktivUttaksperiodegrense.getErAktivt()).isFalse();
        assertThat(inaktivUttaksperiodegrense.getMottattDato()).isEqualTo(gammelMottatDato);

        List<Uttaksperiodegrense> aktivUttaksperiodegrenseList =  behandlingsresultat.getAlleUttaksperiodegrenser().stream()
            .filter(uttaksperiodegrense ->  uttaksperiodegrense.getErAktivt()).collect(Collectors.toList());
        assertThat(aktivUttaksperiodegrenseList).hasSize(1);

        Uttaksperiodegrense uttaksperiodegrense = aktivUttaksperiodegrenseList.get(0);
        assertThat(uttaksperiodegrense.getErAktivt()).isTrue();
        assertThat(uttaksperiodegrense.getMottattDato()).isEqualTo(nyMottattDato);
        assertThat(uttaksperiodegrense.getFørsteLovligeUttaksdag()).isEqualTo(førsteLovligeUttaksdag);
    }

    @Test
    public void finnerSøknadsfristForPeriodeStartDato() {
        LocalDate periodeStart = LocalDate.of(2018, 1, 31);
        LocalDate forventetSøknadsfrist = LocalDate.of(2018, 04, 30);

        LocalDate søknadsfrist = tjeneste.finnSøknadsfristForPeriodeMedStart(periodeStart);
        assertThat(søknadsfrist).isEqualTo(forventetSøknadsfrist);

        periodeStart = LocalDate.of(2018, 1, 31);
        søknadsfrist = tjeneste.finnSøknadsfristForPeriodeMedStart(periodeStart);
        assertThat(søknadsfrist).isEqualTo(forventetSøknadsfrist);
    }


    private Behandling byggBehandlingMedBehandlingsresultat() {
        Fagsak fagsak = FagsakBuilder.nyForeldrepengerForMor().medBrukerPersonInfo(new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(new AktørId("1"))
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(PersonIdent.fra("12312312312"))
            .medForetrukketSpråk(Språkkode.nb)
            .build()).build();

        repository.lagre(fagsak.getNavBruker());
        repository.lagre(fagsak);

        Behandling nyBehandling = Behandling.forFørstegangssøknad(fagsak).build();
        nyBehandling.setAnsvarligSaksbehandler("VL");
        repository.lagre(nyBehandling);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(nyBehandling);
        repository.lagre(behandlingsresultat);
        repository.lagre(nyBehandling);
        repository.flushAndClear();

        return nyBehandling;
    }


}
