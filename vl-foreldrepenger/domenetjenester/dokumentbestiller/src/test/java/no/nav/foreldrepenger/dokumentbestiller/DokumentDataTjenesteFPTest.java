package no.nav.foreldrepenger.dokumentbestiller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Collections;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametereImpl;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.FlettefeltJsonObjectMapper;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.FritekstVedtakDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InnvilgelseForeldrepengerDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BeregningsgrunnlagRegelDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentBehandlingsresultatMapper;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentTypeDtoMapper;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.impl.FamilieHendelseTjenesteImpl;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.uttak.InfoOmResterendeDagerTjeneste;
import no.nav.foreldrepenger.domene.uttak.OpphørFPTjeneste;
import no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.BeregnEkstraFlerbarnsukerTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnUttaksaldoTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.PersonstatusKode;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class DokumentDataTjenesteFPTest {
    private static final String FNR = "12345678901";
    private static final String MOTTAKEREN = "Oline Pedersen";
    private static final String RETURNAVN = "returnavn";
    private static final String RETURADRESSE_1 = "returadresse1";
    private static final String RETUR_POSTNR = "1234";
    private static final String RETUR_POSTSTED = "OSLO";
    private static final String RETUR_KLAGENAVN = "NAVKlage";
    private static final int KLAGEFRIST_UKER = 6;
    private static final String NORG_2_KONTAKT_TELEFON_NUMMER = "44442222";
    private static final String NORG_2_KLAGEINSTANS_TELEFON_NUMMER = "22224444";
    private static final String ARBEIDSFORHOLD_ORGNR = "987123987";
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.now();
    @Rule
    public final DokumentRepositoryRule repoRule = new DokumentRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    private FagsakLåsRepository fagsakLåsRepository = new FagsakLåsRepositoryImpl(entityManager);
    private Repository repository = repoRule.getRepository();
    private DokumentDataTjenesteImpl tjeneste;
    private Behandling behandling;

    private BrevParametere brevParametere;

    private BeregnUttaksaldoTjeneste beregnUttaksaldoTjeneste = mock(BeregnUttaksaldoTjeneste.class);

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();

    private BeregningsresultatFPRepository beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();

    private UttakRepository uttakRepository = repositoryProvider.getUttakRepository();

    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private FagsakRelasjonRepository relasjonRepository = repositoryProvider.getFagsakRelasjonRepository();

    private BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste = mock(BeregnEkstraFlerbarnsukerTjeneste.class);

    @Mock
    private TpsTjeneste tpsTjeneste;

    @Mock
    private BasisPersonopplysningTjeneste personopplysningTjeneste;

    private FamilieHendelseTjeneste familieHendelseTjeneste;

    @Mock
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;

    @Mock
    private ProsessTaskRepository prosessTaskRepository;

    @Mock
    private OpphørFPTjeneste opphørFPTjeneste;

    @Mock
    private InfoOmResterendeDagerTjeneste infoOmResterendeDagerTjeneste;

    private Beregningsgrunnlag beregningsgrunnlag;
    private VirksomhetEntitet virksomhet;

    @Before
    public void oppsett() {
        familieHendelseTjeneste = new FamilieHendelseTjenesteImpl(personopplysningTjeneste, 16, 4, repositoryProvider);
        virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(ARBEIDSFORHOLD_ORGNR)
            .medNavn("VirksomhetNavn")
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);
        Adresseinfo adresseinfo = new Adresseinfo.Builder(AdresseType.POSTADRESSE, new PersonIdent(FNR), MOTTAKEREN, PersonstatusType.BOSA).build();
        PersonIdent personIdent = new PersonIdent("fnr");
        Personinfo personinfo = new Personinfo.Builder()
            .medAktørId(new AktørId("123"))
            .medNavn("navn")
            .medFødselsdato(LocalDate.of(1995, Month.JANUARY, 1))
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(personIdent)
            .build();
        when(tpsTjeneste.hentBrukerForAktør(Mockito.any(AktørId.class))).thenReturn(Optional.of(personinfo));
        when(tpsTjeneste.hentAdresseinformasjon(Mockito.eq(personIdent))).thenReturn(adresseinfo);
        when(beregnUttaksaldoTjeneste.beregnDisponibleDager(Mockito.any())).thenReturn(Optional.empty());

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger
            .forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(LocalDate.now()).medAntallBarn(1);
        behandling = scenario
            .lagre(repositoryProvider);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, mockOppgittFordeling());
        repository.flushAndClear();
        behandling = behandlingRepository.hentBehandling(behandling.getId());

        DokumentMapperTjenesteProvider tjenesteProvider = new DokumentMapperTjenesteProviderImpl(
            new SkjæringstidspunktTjenesteImpl(repositoryProvider,
                new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
                new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
                Period.of(0, 3, 0),
                Period.of(0, 10, 0)),
            personopplysningTjeneste,
            familieHendelseTjeneste,
            beregnUttaksaldoTjeneste,
            hentGrunnlagsdataTjeneste,
            beregnEkstraFlerbarnsukerTjeneste,
            opphørFPTjeneste,
            infoOmResterendeDagerTjeneste);

        brevParametere = new BrevParametereImpl(KLAGEFRIST_UKER, 3, Period.ofWeeks(3), Period.ofWeeks(2));
        DokumentBehandlingsresultatMapper behandlingsresultatMapper = new DokumentBehandlingsresultatMapper(repositoryProvider, tjenesteProvider);
        tjeneste = new DokumentDataTjenesteImpl(NORG_2_KONTAKT_TELEFON_NUMMER, NORG_2_KLAGEINSTANS_TELEFON_NUMMER, new DokumentRepositoryImpl(entityManager), repositoryProvider,
            tpsTjeneste, new ReturadresseKonfigurasjon(RETURNAVN, RETURADRESSE_1, RETUR_POSTNR, RETUR_POSTSTED, RETUR_KLAGENAVN),
            prosessTaskRepository,
            new DokumentTypeDtoMapper(repositoryProvider, tjenesteProvider, brevParametere, behandlingsresultatMapper));

        beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(BigDecimal.valueOf(91425L))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(91425L))
            .build();
        BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
            .build(beregningsgrunnlag);
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(240000))
            .medRedusertBrukersAndelPrÅr(BigDecimal.valueOf(240000))
            .build(periode);

        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);

        UttakResultatEntitet uttakResultat = new UttakResultatEntitet.Builder(scenario.getBehandling().getBehandlingsresultat()).build();

        behandling.setBehandlingresultat(new Behandlingsresultat.Builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(behandling));

        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        UttakResultatPeriodeEntitet uttakResultatPeriode = new UttakResultatPeriodeEntitet.Builder(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT).build();
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        perioder.leggTilPeriode(uttakResultatPeriode);
        uttakResultat.setOverstyrtPerioder(perioder);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, perioder);

        relasjonRepository.opprettRelasjon(behandling.getFagsak(), Dekningsgrad._100);
    }

    private OppgittFordeling mockOppgittFordeling() {
        final LocalDate now = LocalDate.now();
        return new OppgittFordelingEntitet(Collections.singletonList(OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(now, now.plusWeeks(6)).build()), false);
    }

    @Test
    public void skal_lagre_og_hente_dokumentdata_innvilget_foreldrepenger() {
        // Arrange
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP(true);
        beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);

        repository.lagre(behandling);
        repository.flush();

        // Act
        Long dokumentDataId = tjeneste.lagreDokumentData(behandling.getId(), new InnvilgelseForeldrepengerDokument(brevParametere));
        DokumentData data = tjeneste.hentDokumentData(dokumentDataId);

        // Assert
        assertThat(data).isNotNull();
        assertThat(data.getBehandling()).isNotNull();
        DokumentFelles felles = data.getFørsteDokumentFelles();
        assertThat(felles).isNotNull();
//        assertThat(felles.getDokumentTypeDataListe()).hasSize(6);
        DokumentTypeData mottaker = felles.getDokumentTypeDataListe().get(1);
        assertThat(mottaker.getDoksysId()).isEqualTo("sokersNavn");
        assertThat(mottaker.getVerdi()).isEqualTo(MOTTAKEREN);
        assertThat(felles.getSakspartPersonStatus()).isNotNull();
        assertThat(felles.getSakspartPersonStatus()).isEqualToIgnoringCase(PersonstatusKode.ANNET.value());
        boolean funnet = false;
        for (int ix = felles.getDokumentTypeDataListe().size(); ix > 0; ix--) {
            DokumentTypeData strukturert = felles.getDokumentTypeDataListe().get(ix - 1);
            if (strukturert.getStrukturertVerdi() != null) {
                BeregningsgrunnlagRegelDto regelDto = FlettefeltJsonObjectMapper.readValue(strukturert.getStrukturertVerdi(), BeregningsgrunnlagRegelDto.class);
                assertThat(regelDto.getStatus()).isEqualTo("AT");
                assertThat(regelDto.getBeregningsgrunnlagAndelDto()).hasSize(1);
                funnet = true;
                break;
            }
        }
        if (!funnet) {
            fail("Fant ikke strukturert verdi for beregningsgrunnlag");
        }
    }

    @Test
    public void skal_lagre_og_hente_dokumentdata_for_fritektsbrev() {
        // Arrange
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP(true);
        beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);

        repository.lagre(behandling);
        repository.flush();

        // Act
        Long dokumentDataId = tjeneste.lagreDokumentData(behandling.getId(), new FritekstVedtakDokument("Overskrift", "brødtekst"));
        DokumentData data = tjeneste.hentDokumentData(dokumentDataId);

        // Assert
        assertThat(data).isNotNull();
        assertThat(data.getFørsteDokumentFelles().getDokumentTypeDataListe().get(0).getVerdi()).isEqualTo("Overskrift");
        assertThat(data.getFørsteDokumentFelles().getDokumentTypeDataListe().get(1).getStrukturertVerdi()).isEqualTo("\"brødtekst\"");
    }

    private BeregningsresultatAndel buildBeregningsresultatAndel(BeregningsresultatPeriode beregningsresultatPeriode, Boolean brukerErMottaker, int dagsats) {
        return BeregningsresultatAndel.builder()
            .medBrukerErMottaker(brukerErMottaker)
            .medVirksomhet(virksomhet)
            .medDagsats(dagsats)
            .medStillingsprosent(BigDecimal.valueOf(100))
            .medUtbetalingsgrad(BigDecimal.ZERO)
            .medDagsatsFraBg(dagsats)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetstatus(AktivitetStatus.ARBEIDSTAKER)
            .build(beregningsresultatPeriode);
    }

    private BeregningsresultatPeriode buildBeregningsresultatPeriode(BeregningsresultatFP beregningsresultatFP, int fom, int tom) {
        return BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(LocalDate.now().plusDays(fom), LocalDate.now().plusDays(tom))
            .build(beregningsresultatFP);
    }

    private BeregningsresultatFP buildBeregningsresultatFP(Boolean brukerErMottaker) {
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatFP, 11, 20);
        buildBeregningsresultatAndel(brPeriode1, brukerErMottaker, 2160);
        if (!brukerErMottaker) {
            buildBeregningsresultatAndel(brPeriode1, true, 0);
        }
        BeregningsresultatPeriode brPeriode2 = buildBeregningsresultatPeriode(beregningsresultatFP, 21, 30);
        buildBeregningsresultatAndel(brPeriode2, brukerErMottaker, 2160);
        if (!brukerErMottaker) {
            buildBeregningsresultatAndel(brPeriode2, true, 0);
        }
        return beregningsresultatFP;
    }
}
