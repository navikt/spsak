package no.nav.foreldrepenger.dokumentbestiller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

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
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametereImpl;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.PositivtVedtakDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentBehandlingsresultatMapper;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentTypeDtoMapper;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.impl.FamilieHendelseTjenesteImpl;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.uttak.InfoOmResterendeDagerTjeneste;
import no.nav.foreldrepenger.domene.uttak.OpphørFPTjeneste;
import no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.BeregnEkstraFlerbarnsukerTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.PersonstatusKode;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class DokumentDataTjenesteTest {
    private static final String FNR = "12345678901";
    private static final String MOTTAKEREN = "Oline Pedersen";
    private static final String RETURNAVN = "returnavn";
    private static final String RETURADRESSE_1 = "returadresse1";
    private static final String RETUR_POSTNR = "1234";
    private static final String RETUR_POSTSTED = "OSLO";
    private static final String RETUR_KLAGENAVN = "NAVKlage";
    private static final int KLAGEFRIST_UKER = 6;
    private static final int KLAGEFRIST_UKER_INNSYN = 3;
    private static final String NORG_2_KONTAKT_TELEFON_NUMMER = "44442222";
    private static final String NORG_2_KLAGEINSTANS_TELEFON_NUMMER = "22224444";
    @Rule
    public final DokumentRepositoryRule repoRule = new DokumentRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private final DokumentRepository dokumentRepository = new DokumentRepositoryImpl(entityManager);
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    private Repository repository = repoRule.getRepository();
    private DokumentDataTjenesteImpl tjeneste;
    private Behandling behandling;

    private BrevParametere brevParametere;

    @Mock
    private TpsAdapter tpsAdapter;

    @Mock
    private TpsTjeneste tpsTjeneste;

    @Mock
    private ProsessTaskRepository prosessTaskRepository;

    @Mock
    private BasisPersonopplysningTjeneste personopplysningTjeneste;

    private FamilieHendelseTjeneste familieHendelseTjeneste;

    @Mock
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;

    @Mock
    private BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste;

    @Mock
    private OpphørFPTjeneste opphørFPTjeneste;

    @Mock
    private InfoOmResterendeDagerTjeneste infoOmResterendeDagerTjeneste;

    @Before
    public void oppsett() {
        familieHendelseTjeneste = new FamilieHendelseTjenesteImpl(personopplysningTjeneste, 16, 4, repositoryProvider);
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

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medFødselAdopsjonsdato(Collections.singletonList(LocalDate.now()))
            .medDefaultBekreftetTerminbekreftelse();
        behandling = scenario
            .lagre(repositoryProvider);

        DokumentMapperTjenesteProvider tjenesteProvider = new DokumentMapperTjenesteProviderImpl(
            new SkjæringstidspunktTjenesteImpl(repositoryProvider,
                new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
                new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
                Period.of(0, 3, 0),
                Period.of(0, 10, 0)),
            personopplysningTjeneste,
            familieHendelseTjeneste,
            null,
            hentGrunnlagsdataTjeneste,
            beregnEkstraFlerbarnsukerTjeneste,
            opphørFPTjeneste,
            infoOmResterendeDagerTjeneste);

        DokumentBehandlingsresultatMapper behandlingsresultatMapper = new DokumentBehandlingsresultatMapper(repositoryProvider, tjenesteProvider);

        brevParametere = new BrevParametereImpl(KLAGEFRIST_UKER, KLAGEFRIST_UKER_INNSYN, Period.ofWeeks(3), Period.ofWeeks(2));
        tjeneste = new DokumentDataTjenesteImpl(NORG_2_KONTAKT_TELEFON_NUMMER, NORG_2_KLAGEINSTANS_TELEFON_NUMMER, new DokumentRepositoryImpl(entityManager), repositoryProvider,
            tpsTjeneste, new ReturadresseKonfigurasjon(RETURNAVN, RETURADRESSE_1, RETUR_POSTNR, RETUR_POSTSTED, RETUR_KLAGENAVN),
            prosessTaskRepository,
            new DokumentTypeDtoMapper(repositoryProvider, tjenesteProvider, brevParametere, behandlingsresultatMapper));
    }

    @Test
    public void skal_hente_dokumentdata() {
        // Arrange

        DokumentMalType dokumentMalType = dokumentRepository.hentDokumentMalType(DokumentMalType.POSITIVT_VEDTAK_DOK);
        Long dokumentDataId = DokumentDataEntityTest.opprettDokumentFellesData(repository, behandling, dokumentMalType);

        // Act
        DokumentData data = tjeneste.hentDokumentData(dokumentDataId);

        // Assert
        assertThat(data).isNotNull();
        assertThat(data.getDokumentFelles()).isNotNull();
        assertThat(data.getBehandling()).isNotNull();
    }

    @Test
    public void skal_lagre_og_hente_dokumentdata_innvilget() {
        // Arrange
        oppdaterMedBehandlingsresultat(behandling, true);

        repository.lagre(behandling);
        repository.flush();

        // Act
        Long dokumentDataId = tjeneste.lagreDokumentData(behandling.getId(), new PositivtVedtakDokument(brevParametere));
        DokumentData data = tjeneste.hentDokumentData(dokumentDataId);

        // Assert
        assertThat(data).isNotNull();
        assertThat(data.getBehandling()).isNotNull();
        DokumentFelles felles = data.getFørsteDokumentFelles();
        assertThat(felles).isNotNull();
        assertThat(felles.getDokumentTypeDataListe()).hasSize(5);
        DokumentTypeData belop = felles.getDokumentTypeDataListe().get(1);
        assertThat(belop.getDoksysId()).isEqualTo("belop");
        assertThat(belop.getVerdi()).isEqualTo("48500");
        assertThat(felles.getSakspartPersonStatus()).isNotNull();
        assertThat(felles.getSakspartPersonStatus()).isEqualToIgnoringCase(PersonstatusKode.ANNET.value());
    }

    @Test
    public void skal_lagre_og_hente_dokumentdata_avslag() {
        // Arrange
        oppdaterMedBehandlingsresultat(behandling, false);

        repository.lagre(behandling);
        repository.flush();

        // Act
        Long dokumentDataId = tjeneste.lagreDokumentData(behandling.getId(), new PositivtVedtakDokument(brevParametere));
        DokumentData data = tjeneste.hentDokumentData(dokumentDataId);

        // Assert
        assertThat(data).isNotNull();
        assertThat(data.getBehandling()).isNotNull();
        DokumentFelles felles = data.getFørsteDokumentFelles();
        assertThat(felles).isNotNull();
        assertThat(felles.getDokumentTypeDataListe()).hasSize(4);
        DokumentTypeData klageFristUker = felles.getDokumentTypeDataListe().get(1);
        assertThat(klageFristUker.getDoksysId()).isEqualTo("klageFristUker");
        assertThat(klageFristUker.getVerdi()).isEqualTo(Integer.toString(KLAGEFRIST_UKER));
        assertThat(felles.getSakspartPersonStatus()).isNotNull();
        assertThat(felles.getSakspartPersonStatus()).isEqualToIgnoringCase(PersonstatusKode.ANNET.value());
    }

    private void oppdaterMedBehandlingsresultat(Behandling behandling, boolean innvilget) {
        VilkårResultat.builder()
            .leggTilVilkårResultat(VilkårType.FØDSELSVILKÅRET_MOR, innvilget ? VilkårUtfallType.OPPFYLT : VilkårUtfallType.IKKE_OPPFYLT,
                null, new Properties(), null, false, false, null, null)
            .medVilkårResultatType(innvilget ? VilkårResultatType.INNVILGET : VilkårResultatType.AVSLÅTT)
            .buildFor(behandling);
        if (innvilget) {
            BeregningResultat.builder()
                .medBeregning(new Beregning(48500L, 1L, 48500L, LocalDateTime.now()))
                .buildFor(behandling);
        }
    }

}
