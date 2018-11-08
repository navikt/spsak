package no.nav.foreldrepenger.vedtak.xml;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningsgrunnlagTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.Kjoenn;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.BehandlingsresultatXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.personopplysninger.PersonopplysningXmlTjenesteForeldrepenger;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;

@RunWith(CdiRunner.class)
public class VedtakXmlTjenesteForeldrepengerTest {

    static final AktørId BRUKER_AKTØR_ID = new AktørId("10000009");
    private static final PersonIdent FNR_MOR = new PersonIdent("12345678901");
    static final Saksnummer SAKSNUMMER = new Saksnummer("12345");
    static final AktørId ANNEN_PART_AKTØR_ID = new AktørId("432");
    static LocalDate VEDTAK_DATO = LocalDate.parse("2017-10-11");
    static final IverksettingStatus IVERKSETTING_STATUS = IverksettingStatus.IKKE_IVERKSATT;
    static final String ANSVARLIG_SAKSBEHANDLER = "fornavn etternavn";

    private static final LocalDate FØDSELSDATO_BARN = LocalDate.of(2017, Month.JANUARY, 1);
    private static final LocalDate FØRSTE_UTTAKSDATO_OPPGITT = LocalDate.now().minusDays(20);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final BeregningsresultatFPRepository beregningsresultatFPRepository = new BeregningsresultatFPRepositoryImpl(repoRule.getEntityManager());

    @Inject
    private BeregningsgrunnlagTestUtil beregningTestUtil;
    private Repository repository = repoRule.getRepository();

    @Mock
    private TpsTjeneste tpsTjeneste;

    @Inject
    PersonopplysningTjeneste personopplysningTjeneste;

    @Inject
    @FagsakYtelseTypeRef("FP")
    private BehandlingsresultatXmlTjeneste behandlingsresultatXmlTjeneste;

    private VedtakXmlTjenesteForeldrepenger vedtakXmlTjenesteFP;
    private PersonopplysningXmlTjenesteForeldrepenger personopplysningXmlTjenesteForeldrepenger;

    @Before
    public void oppsett() {

        personopplysningXmlTjenesteForeldrepenger = new PersonopplysningXmlTjenesteForeldrepenger(tpsTjeneste, repositoryProvider, personopplysningTjeneste);
        vedtakXmlTjenesteFP = new VedtakXmlTjenesteForeldrepenger(repositoryProvider, personopplysningXmlTjenesteForeldrepenger, behandlingsresultatXmlTjeneste);
        when(tpsTjeneste.hentFnr(any(AktørId.class))).thenReturn(Optional.of(FNR_MOR));

    }

    @Test
    public void test_konvertering_kjønn() {
        Kjoenn søkersKjønn = Kjoenn.KVINNE;
        NavBrukerKjønn navBrukerKjønn = NavBrukerKjønn.fraKode(søkersKjønn.getKode());
        assertThat(navBrukerKjønn).isEqualTo(NavBrukerKjønn.KVINNE);
    }

    @Test
    public void skal_opprette_vedtaks_xml() {
        Behandling behandling = byggBehandlingMedVedtak();
        String avkortetXmlElement = "avkortet>";

        // Act
        String xml = vedtakXmlTjenesteFP.opprettVedtakXml(behandling.getId());

        // Assert
        assertNotNull(xml);
        assertThat(xml).contains(avkortetXmlElement);
    }

    private Behandling byggBehandlingMedVedtak() {
        String selvstendigNæringsdrivendeOrgnr = "3456";

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(BRUKER_AKTØR_ID, NavBrukerKjønn.KVINNE)
            .medSaksnummer(SAKSNUMMER);
        scenario.medSøknadAnnenPart().medAktørId(ANNEN_PART_AKTØR_ID);
        scenario.medSøknadHendelse()
            .medFødselsDato(FØDSELSDATO_BARN);

        scenario.medDefaultInntektArbeidYtelse();

        scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(LocalDate.now())
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now())
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(90000));

        scenario.medFordeling(opprettOppgittFordeling());

        OppgittOpptjeningBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningBuilder.EgenNæringBuilder.ny()
            .medVirksomhet(virksomhet(selvstendigNæringsdrivendeOrgnr))
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(FØRSTE_UTTAKSDATO_OPPGITT, FØRSTE_UTTAKSDATO_OPPGITT.plusWeeks(2)));
        OppgittOpptjeningBuilder oppgittOpptjeningBuilder = OppgittOpptjeningBuilder.ny()
            .leggTilEgneNæringer(Collections.singletonList(egenNæringBuilder));

        scenario.medOppgittOpptjening(oppgittOpptjeningBuilder);

        Behandling behandling = scenario.lagre(repositoryProvider);
        Behandlingsresultat behandlingsresultat = opprettBehandlingsresultatMedVilkårResultatForBehandling(behandling);
        repository.lagre(behandlingsresultat);
        repository.flushAndClear();

        Uttaksperiodegrense uttaksperiodegrense = new Uttaksperiodegrense.Builder(behandling)
            .medFørsteLovligeUttaksdag(LocalDate.now())
            .medMottattDato(LocalDate.now())
            .build();
        repositoryProvider.getUttakRepository().lagreUttaksperiodegrense(behandling, uttaksperiodegrense);

        UttakResultatPeriodeEntitet periode = new UttakResultatPeriodeEntitet.Builder(LocalDate.now(), LocalDate.now().plusDays(11))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakResultatPerioderEntitet uttakResultatPerioder1 = new UttakResultatPerioderEntitet();
        uttakResultatPerioder1.leggTilPeriode(periode);

        repositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(behandling, uttakResultatPerioder1);

        opprettStønadskontoer(behandling);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, FØRSTE_UTTAKSDATO_OPPGITT);

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP();
        beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);

        BehandlingVedtakRepository behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        BehandlingVedtak vedtak = BehandlingVedtak.builder()
            .medAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER)
            .medIverksettingStatus(IVERKSETTING_STATUS)
            .medVedtaksdato(VEDTAK_DATO)
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medBehandlingsresultat(behandlingsresultat)
            .build();
        behandlingVedtakRepository.lagre(vedtak, behandlingRepository.taSkriveLås(behandling));

        return behandling;
    }

    private BeregningsresultatFP lagBeregningsresultatFP() {
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder().medRegelInput("input").medRegelSporing("sporing").build();
        BeregningsresultatPeriode beregningsresultatPeriode = BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(FØRSTE_UTTAKSDATO_OPPGITT, FØRSTE_UTTAKSDATO_OPPGITT.plusWeeks(2))
            .build(beregningsresultatFP);
        BeregningsresultatAndel.builder()
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetstatus(AktivitetStatus.ARBEIDSTAKER)
            .medDagsats(123)
            .medDagsatsFraBg(123)
            .medBrukerErMottaker(true)
            .medUtbetalingsgrad(BigDecimal.valueOf(100))
            .medStillingsprosent(BigDecimal.valueOf(100))
            .build(beregningsresultatPeriode);
        return beregningsresultatFP;
    }

    private Behandlingsresultat opprettBehandlingsresultatMedVilkårResultatForBehandling(Behandling behandling) {

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .buildFor(behandling);
        VilkårResultat vilkårResultat = VilkårResultat.builder().medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .buildFor(behandlingsresultat);
        repository.lagre(vilkårResultat);
        behandlingsresultat.medOppdatertVilkårResultat(vilkårResultat);
        behandling.setBehandlingresultat(behandlingsresultat);

        return behandlingsresultat;
    }

    private void opprettStønadskontoer(Behandling behandling) {
        Stønadskonto foreldrepengerFørFødsel = Stønadskonto.builder()
            .medStønadskontoType(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)
            .medMaxDager(15)
            .build();
        Stønadskonto mødrekvote = Stønadskonto.builder()
            .medStønadskontoType(StønadskontoType.MØDREKVOTE)
            .medMaxDager(50)
            .build();
        Stønadskonto fellesperiode = Stønadskonto.builder()
            .medStønadskontoType(StønadskontoType.FELLESPERIODE)
            .medMaxDager(50)
            .build();
        Stønadskontoberegning stønadskontoberegning = Stønadskontoberegning.builder()
            .medRegelEvaluering("evaluering")
            .medRegelInput("grunnlag")
            .medStønadskonto(mødrekvote).medStønadskonto(fellesperiode).medStønadskonto(foreldrepengerFørFødsel).build();

        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(behandling.getFagsak(), Dekningsgrad._100);
        repositoryProvider.getFagsakRelasjonRepository().lagre(behandling, stønadskontoberegning);
    }

    private Virksomhet virksomhet(String orgnr) {
        Optional<Virksomhet> optional = repositoryProvider.getVirksomhetRepository().hent(orgnr);
        if (optional.isPresent()) {
            return optional.get();
        }
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(orgnr)
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);
        return virksomhet;
    }

    private OppgittFordeling opprettOppgittFordeling() {
        OppgittPeriodeBuilder periode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(FØRSTE_UTTAKSDATO_OPPGITT, FØRSTE_UTTAKSDATO_OPPGITT.plusWeeks(2))
            .medVirksomhet(opprettOgLagreVirksomhet());

        return new OppgittFordelingEntitet(singletonList(periode.build()), true);
    }

    private Virksomhet opprettOgLagreVirksomhet() {
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("75674554355")
            .medNavn("Virksomhet")
            .medRegistrert(LocalDate.now().minusYears(10L))
            .medOppstart(LocalDate.now().minusYears(10L))
            .oppdatertOpplysningerNå()
            .build();
        repoRule.getEntityManager().persist(virksomhet);
        return virksomhet;
    }

}
