package no.nav.foreldrepenger.domene.personopplysning.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.impl.TpsAdapterImpl;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnMorsMaksdatoTjeneste;

public class PersonopplysningTjenesteImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private TpsAdapterImpl tpsAdapterImpl = Mockito.mock(TpsAdapterImpl.class);

    private NavBrukerRepository navBrukerRepository = Mockito.mock(NavBrukerRepository.class);

    private PersonopplysningTjeneste personopplysningTjeneste;

    @Before
    public void before() {
        personopplysningTjeneste = new PersonopplysningTjenesteImpl(repositoryProvider,
            tpsAdapterImpl, navBrukerRepository, new SkjæringstidspunktTjenesteImpl(repositoryProvider, Mockito.mock(BeregnMorsMaksdatoTjeneste.class),
            new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
            Period.of(0, 3, 0),
            Period.of(0, 10, 0)));
    }

    @Test
    public void skal_hente_gjeldende_personinformasjon_på_tidspunkt() {
        LocalDate tidspunkt = LocalDate.now();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medDefaultSøknadTerminbekreftelse();

        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon personInformasjon = scenario
            .opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .kvinne(søkerAktørId, SivilstandType.SAMBOER)
            .statsborgerskap(Landkoder.NOR)
            .personstatus(PersonstatusType.BOSA)
            .build();

        scenario.medRegisterOpplysninger(personInformasjon);
        scenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(LocalDate.now().plusWeeks(8), LocalDate.now().plusWeeks(12))
            .build()), true));

        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        PersonopplysningerAggregat personopplysningerAggregat = personopplysningTjeneste.hentGjeldendePersoninformasjonPåTidspunkt(behandling, tidspunkt);
        // Assert
        assertThat(personopplysningerAggregat.getPersonstatuserFor(behandling.getAktørId())).isNotEmpty();
    }

}
