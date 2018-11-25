package no.nav.foreldrepenger.domene.registerinnhenting.behandlingårsak;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.kontrollerfakta.BehandlingÅrsakTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.EndringsresultatSjekker;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BehandlingÅrsakTjenesteImplTest {

    private AktørId AKTØRID = new AktørId("1");

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private BehandlingÅrsakTjeneste tjeneste;

    @Inject
    private EndringsresultatSjekker endringsresultatSjekker;
    private Behandling behandling;

    @Inject
    @Any
    Instance<BehandlingÅrsakUtleder> utledere;

    @Mock
    private DiffResult diffResult;

    @Before
    public void setup() {
        initMocks(this);

        tjeneste = new BehandlingÅrsakTjenesteImpl(utledere, endringsresultatSjekker);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør()
            .medBruker(AKTØRID, NavBrukerKjønn.KVINNE)
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD);
        behandling = scenario.lagre(repositoryProvider);
    }

    @Test
    public void test_skal_ikke_returnere_behandlingsårsaker_hvis_ikke_endringer() {
        when(diffResult.isEmpty()).thenReturn(true); // Indikerer at det ikke finnes diff

        EndringsresultatDiff endringsresultat = EndringsresultatDiff.opprett();
        endringsresultat.leggTilSporetEndring(EndringsresultatDiff.medDiff(PersonInformasjon.class, 1L, 1L), () -> diffResult);
        endringsresultat.leggTilSporetEndring(EndringsresultatDiff.medDiff(MedlemskapAggregat.class, 1L, 1L), () -> diffResult);
        endringsresultat.leggTilSporetEndring(EndringsresultatDiff.medDiff(InntektArbeidYtelseGrunnlag.class, 1L, 1L), () -> diffResult);

        // Act/Assert
        assertThat(tjeneste.utledBehandlingÅrsakerBasertPåDiff(behandling, endringsresultat)).isEmpty();
    }

    @Test
    public void test_behandlingsårsaker_når_endring_dødsdato_søker() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(null);
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(dødsdato);

        EndringsresultatDiff endringsresultat = EndringsresultatDiff.opprett();
        when(diffResult.isEmpty()).thenReturn(false); // Indikerer at det finnes diff
        endringsresultat.leggTilSporetEndring(EndringsresultatDiff.medDiff(PersonInformasjon.class, personopplysningGrunnlag1.getId(), personopplysningGrunnlag2.getId()), () -> diffResult);

        Set<BehandlingÅrsakType> behandlingÅrsaker = tjeneste.utledBehandlingÅrsakerBasertPåDiff(behandling, endringsresultat);
        assertThat(behandlingÅrsaker).hasSize(1);
        assertThat(behandlingÅrsaker).as("Forventer behandlingsårsak").contains(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_DØD);
    }

    private PersonopplysningGrunnlag opprettPersonopplysningGrunnlag(LocalDate dødsdato) {
        PersonopplysningRepository personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        final PersonInformasjonBuilder builder = personopplysningRepository.opprettBuilderForRegisterdata(behandling);
        final PersonInformasjonBuilder.PersonopplysningBuilder personopplysningBuilder = builder.getPersonopplysningBuilder(behandling.getAktørId());
        personopplysningBuilder.medDødsdato(dødsdato);
        builder.leggTil(personopplysningBuilder);
        personopplysningRepository.lagre(behandling, builder);
        return personopplysningRepository.hentPersonopplysninger(behandling);
    }
}
