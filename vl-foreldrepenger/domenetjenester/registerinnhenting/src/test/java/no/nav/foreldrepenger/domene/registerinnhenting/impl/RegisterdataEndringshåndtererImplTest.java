package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.OpplysningsPeriodeTjeneste;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.OpplysningsPeriodeTjenesteImpl;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepositoryImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.Gyldighetsperiode;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.Personhistorikkinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.PersonstatusPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.FinnInntektRequest;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.InntektTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.domene.kontrollerfakta.BehandlingÅrsakTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.registerinnhenting.Endringskontroller;
import no.nav.foreldrepenger.domene.registerinnhenting.EndringsresultatSjekker;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterinnhentingHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(CdiRunner.class)
public class RegisterdataEndringshåndtererImplTest {

    private static final AktørId SØKER_AKTØR_ID = new AktørId("1");
    private static final PersonstatusType PERSONSTATUS = PersonstatusType.BOSA;
    private static final Landkoder LANDKODE = Landkoder.NOR;
    private static final NavBrukerKjønn KJØNN = NavBrukerKjønn.KVINNE;
    private static final String FNR_FORELDER = "01234567890";
    private static final LocalDate FORELDER_FØDSELSDATO = LocalDate.now().minusYears(30);
    private static final String DURATION = "PT10H";

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private PersoninfoAdapter personinfoAdapter;
    @Mock
    private InntektTjeneste inntektTjeneste;
    @Mock
    private TpsAdapter tpsAdapter;
    @Mock
    private MedlemTjeneste medlemTjeneste;
    @Mock
    private VirksomhetTjeneste virksomhetTjeneste;
    @Mock
    private Instance<String> durationInstance;
    @Mock
    private ProsessTaskRepository prosessTaskRepository;
    @Mock
    private Endringskontroller endringskontroller;
    @Mock
    private EndringsresultatSjekker endringsresultatSjekker;
    @Mock
    private BehandlingÅrsakTjeneste behandlingÅrsakTjeneste;
    @Mock
    private IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste;
    @Mock
    private SigrunTjeneste sigrunTjeneste;
    @Mock
    private MedlemskapRepository medlemskapRepository;

    private BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste = new BehandlingskontrollTaskTjenesteImpl(prosessTaskRepository);
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, Period.of(0, 10, 0));
    private OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste = new OpplysningsPeriodeTjenesteImpl(skjæringstidspunktTjeneste,
        Period.of(0, 4, 0), Period.of(1, 0, 0));
    private HistorikkRepositoryImpl historikkRepository = new HistorikkRepositoryImpl(repositoryRule.getEntityManager());
    private RegisterinnhentingHistorikkinnslagTjeneste historikkinnslagTjeneste = new RegisterinnhentingHistorikkinnslagTjenesteImpl(historikkRepository);
    private BehandlingModellRepository behandlingModellRepository = new BehandlingModellRepositoryImpl(repositoryRule.getEntityManager());

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste = Mockito
        .spy(new BehandlingskontrollTjenesteImpl(repositoryProvider,
            behandlingModellRepository, new BehandlingskontrollEventPubliserer(CDI.current().getBeanManager())));

    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);

    @Before
    public void before() {
        when(durationInstance.get()).thenReturn(DURATION);
        when(tpsAdapter.hentIdentForAktørId(Mockito.any(AktørId.class))).thenReturn(Optional.of(new PersonIdent(FNR_FORELDER)));
        when(endringsresultatSjekker.opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(any(Behandling.class)))
            .thenReturn(EndringsresultatSnapshot.opprett());
        when(endringsresultatSjekker.finnSporedeEndringerPåBehandlingsgrunnlag(any(Behandling.class), any(EndringsresultatSnapshot.class)))
            .thenReturn(EndringsresultatDiff.opprettForSporingsendringer());

        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("21542512")
            .medNavn("Arbeidsplassen AS")
            .oppdatertOpplysningerNå()
            .build();
        repositoryRule.getEntityManager().persist(virksomhet);

        when(virksomhetTjeneste.hentOgLagreOrganisasjon(any())).thenReturn(virksomhet);
        when(medlemTjeneste.oppdaterMedlemskapHvisEndret(any(), any(), any(), any()))
            .thenReturn(false);

        Mockito.doNothing().when(behandlingskontrollTjeneste).prosesserBehandling(any());
    }

    @Test
    public void skal_returnere_når_forrige_innhenting_var_i_inneværende_døgn() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medOpplysningerOppdatertTidspunkt(LocalDateTime.now());
        Behandling behandling = scenario
            .lagre(repositoryProvider);
        // Act
        lagRegisterdataEndringshåndterer()
            .oppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(behandling);

        // Assert
        verify(personinfoAdapter, times(0)).innhentSaksopplysningerForSøker(Mockito.any(AktørId.class));
        verify(inntektTjeneste, times(0)).finnInntekt(any(FinnInntektRequest.class), any());
    }

    @Test
    public void skal_starte_behandlingen_på_nytt_25_dager_etter_termin_og_ingen_fødselsdato() {
        // Arrange
        Personinfo søker = opprettSøkerinfo();
        when(personinfoAdapter.innhentSaksopplysningerForSøker(Mockito.any(AktørId.class))).thenReturn(søker);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medOpplysningerOppdatertTidspunkt(LocalDateTime.now().minusDays(1))
            .medSøker(søker)
            .medBehandlingStegStart(BehandlingStegType.KONTROLLER_FAKTA);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        lagRegisterdataEndringshåndterer()
            .oppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(behandling);

        // Assert
        final Behandling behandling1 = repositoryProvider.getBehandlingRepository().hentBehandling(behandling.getId());
        assertThat(behandling1.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.KONTROLLER_FAKTA);
    }

    @Test
    public void skal_ikke_starte_behandlingen_på_nytt_for_adopsjonssak_der_ingenting_er_endret() {
        // Arrange
        LocalDateTime opplysningerOppdatertTidspunkt = LocalDateTime.now().minusDays(1);
        Personinfo søker = opprettSøkerinfo();

        when(personinfoAdapter.innhentSaksopplysningerForSøker(Mockito.any(AktørId.class))).thenReturn(søker);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forAdopsjon()
            .medOpplysningerOppdatertTidspunkt(opplysningerOppdatertTidspunkt)
            .medSøker(søker)
            .medBehandlingStegStart(BehandlingStegType.KONTROLLER_FAKTA);
        Behandling behandling = scenario.lagre(repositoryProvider);

        when(personinfoAdapter.innhentPersonopplysningerHistorikk(Mockito.any(AktørId.class), any()))
            .thenReturn(opprettSøkerHistorikkInfo(søker.getPersonstatus()));

        // Act
        lagRegisterdataEndringshåndterer()
            .oppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(behandling);

        // Assert
        final Behandling behandling1 = repositoryProvider.getBehandlingRepository().hentBehandling(behandling.getId());
        assertThat(behandling1.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.KONTROLLER_FAKTA);
        assertThat(repositoryProvider.getBehandlingRepository().hentSistOppdatertTidspunkt(behandling).get()).isAfter(opplysningerOppdatertTidspunkt);
    }

    private Personhistorikkinfo opprettSøkerHistorikkInfo(PersonstatusType status) {
        final PersonstatusPeriode statusPeriode = new PersonstatusPeriode(Gyldighetsperiode.innenfor(FORELDER_FØDSELSDATO, TIDENES_ENDE), status);
        return Personhistorikkinfo.builder().medAktørId(String.valueOf(SØKER_AKTØR_ID)).leggTil(statusPeriode).build();
    }

    private RegisterdataEndringshåndtererImpl lagRegisterdataEndringshåndterer() {

        RegisterdataInnhenterImpl registerdataInnhenter = new TestRegisterdataInnhenter(
            personinfoAdapter,
            medlemTjeneste,
            skjæringstidspunktTjeneste,
            behandlingskontrollTaskTjeneste,
            repositoryProvider,
            sigrunTjeneste,
            inntektArbeidYtelseTjeneste,
            opplysningsPeriodeTjeneste);

        return new RegisterdataEndringshåndtererImpl(
            repositoryProvider,
            registerdataInnhenter,
            durationInstance, endringskontroller, endringsresultatSjekker, historikkinnslagTjeneste, behandlingÅrsakTjeneste);
    }

    private Personinfo opprettSøkerinfo() {
                return new Personinfo.Builder()
            .medAktørId(SØKER_AKTØR_ID)
            .medPersonIdent(new PersonIdent(FNR_FORELDER))
            .medNavn("Navn Navnesen")
            .medFødselsdato(FORELDER_FØDSELSDATO)
            .medNavBrukerKjønn(KJØNN)
            .medLandkode(LANDKODE)
            .medPersonstatusType(PERSONSTATUS)
            .medSivilstandType(SivilstandType.UGIFT)
            .medRegion(Region.EOS)
            .build();
    }

    private class TestRegisterdataInnhenter extends RegisterdataInnhenterImpl {

        TestRegisterdataInnhenter(PersoninfoAdapter personinfoAdapter, MedlemTjeneste medlemTjeneste, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                  BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste, BehandlingRepositoryProvider repositoryProvider,
                                  SigrunTjeneste sigrunTjeneste,
                                  InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                  OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste) {
            super(personinfoAdapter,
                medlemTjeneste, skjæringstidspunktTjeneste,
                behandlingskontrollTaskTjeneste, repositoryProvider,
                sigrunTjeneste,
                inntektArbeidYtelseTjeneste, opplysningsPeriodeTjeneste);
        }

        @Override
        protected IAYRegisterInnhentingTjeneste getIAYRegisterInnhenterFor(Behandling behandling) {
            return iayRegisterInnhentingTjeneste;
        }
    }
}
