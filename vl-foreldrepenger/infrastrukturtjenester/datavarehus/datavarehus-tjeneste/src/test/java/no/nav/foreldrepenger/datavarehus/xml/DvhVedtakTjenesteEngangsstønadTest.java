package no.nav.foreldrepenger.datavarehus.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Properties;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TfradragTillegg;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeAksjon;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndring;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiTypeSats;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiUtbetFrekvens;
import no.nav.foreldrepenger.økonomistøtte.ØkonomioppdragRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;

@RunWith(CdiRunner.class)
public class DvhVedtakTjenesteEngangsstønadTest {
    private static final AktørId BRUKER_AKTØR_ID = new AktørId("10000009");
    private static final Saksnummer SAKSNUMMER = new Saksnummer("12345");
    private static final AktørId ANNEN_PART_AKTØR_ID = new AktørId("432");
    private static LocalDate VEDTAK_DATO = LocalDate.parse("2017-10-11");
    private static final IverksettingStatus IVERKSETTING_STATUS = IverksettingStatus.IKKE_IVERKSATT;
    private static final String ANSVARLIG_SAKSBEHANDLER = "fornavn etternavn";
    private static final Long OPPDRAG_FAGSYSTEM_ID = 44L;

    private static final LocalDate FØDSELSDATO_BARN = LocalDate.of(2017, Month.JANUARY, 1);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    @Inject
    @FagsakYtelseTypeRef("ES")
    private DvhVedtakTjeneste dvhVedtakTjenesteES;

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Inject
    private ØkonomioppdragRepository økonomioppdragRepository;

    @Before
    public void oppsett() {

    }

    @Test
    public void skal_opprette_vedtaks_xml_med_oppdrag() {
        Behandling behandling = byggFødselBehandlingMedVedtak(true);
        Long delytelseId = 65L;
        String delytelseXmlElement = String.format("delytelseId>%s</", delytelseId);
        String fagsystemIdXmlElement = String.format("fagsystemId>%s</", OPPDRAG_FAGSYSTEM_ID);
        buildOppdragskontroll(behandling.getId(), delytelseId);

        // Act
        String xml = dvhVedtakTjenesteES.opprettDvhVedtakXml(behandling.getId());

        // Assert
        assertNotNull(xml);
        assertThat(xml).contains(delytelseXmlElement);
        assertThat(xml).contains(fagsystemIdXmlElement);
        assertPersonopplysningDvh(BRUKER_AKTØR_ID, xml);
    }

    @Test
    public void skal_opprette_vedtaks_xml_innvilget_uten_oppdrag() {
        Behandling behandling = byggFødselBehandlingMedVedtak(true);
        String delytelseXmlElement = "delytelseId>";

        // Act
        String xml = dvhVedtakTjenesteES.opprettDvhVedtakXml(behandling.getId());

        // Assert
        assertNotNull(xml);
        assertThat(xml).doesNotContain(delytelseXmlElement);
        assertPersonopplysningDvh(BRUKER_AKTØR_ID, xml);
    }

    @Test
    public void skal_opprette_vedtaks_xml_avslag_uten_oppdrag() {
        Behandling behandling = byggFødselBehandlingMedVedtak(false);
        Long delytelseId = 65L;
        String delytelseXmlElement = "delytelseId>";

        // Act
        String xml = dvhVedtakTjenesteES.opprettDvhVedtakXml(behandling.getId());

        // Assert
        assertNotNull(xml);
        assertThat(xml).doesNotContain(delytelseXmlElement);
        assertPersonopplysningDvh(BRUKER_AKTØR_ID, xml);
    }

    @Test
    public void skal_opprette_vedtaks_xml_adopsjon() {
        Behandling behandling = byggAdopsjonMedVedtak(true);
        String adopsjonXmlElement = "adopsjon>";

        // Act
        String xml = dvhVedtakTjenesteES.opprettDvhVedtakXml(behandling.getId());

        // Assert
        assertNotNull(xml);
        assertThat(xml).contains(adopsjonXmlElement);
        assertPersonopplysningDvh(BRUKER_AKTØR_ID, xml);
    }

    /**
     * Personopplyusning for vedtaks xml til datavarehus skal ikke inneholde fødselsnummer. Men istedenfor aktørId.
     */
    private void assertPersonopplysningDvh(AktørId aktørId, String vedtaksXml) {
        String aktørIdXmlElement = String.format("aktoerId>%s</", aktørId.getId());
        String fødselsnummerXmlElement = "norskIdent>";
        assertThat(vedtaksXml).contains(aktørIdXmlElement);
        assertThat(vedtaksXml).doesNotContain(fødselsnummerXmlElement);
    }

    private Behandling byggAdopsjonMedVedtak(boolean innvilget) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(BRUKER_AKTØR_ID, NavBrukerKjønn.KVINNE)
            .medSaksnummer(SAKSNUMMER);
        scenario.medSøknadAnnenPart().medAktørId(ANNEN_PART_AKTØR_ID);

        scenario.medSøknadHendelse().medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder()
            .medOmsorgsovertakelseDato(LocalDate.now().plusDays(50)))
            .leggTilBarn(FØDSELSDATO_BARN)
            .medAntallBarn(1);

        return lagreBehandlingOgVedtak(innvilget, scenario);

    }
    private Behandling byggFødselBehandlingMedVedtak(boolean innvilget) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(BRUKER_AKTØR_ID, NavBrukerKjønn.KVINNE)
            .medSaksnummer(SAKSNUMMER);
        scenario.medSøknadAnnenPart().medAktørId(ANNEN_PART_AKTØR_ID);
        scenario.medSøknadHendelse()
            .medFødselsDato(FØDSELSDATO_BARN);

        return lagreBehandlingOgVedtak(innvilget, scenario);
    }

    private Behandling lagreBehandlingOgVedtak(boolean innvilget, ScenarioMorSøkerEngangsstønad scenario) {
        Behandling behandling = scenario.lagre(repositoryProvider);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.opprettFor(behandling);
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        BehandlingVedtakRepository behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        BehandlingVedtak vedtak = BehandlingVedtak.builder()
            .medAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER)
            .medIverksettingStatus(IVERKSETTING_STATUS)
            .medVedtaksdato(VEDTAK_DATO)
            .medVedtakResultatType(innvilget ? VedtakResultatType.INNVILGET : VedtakResultatType.AVSLAG)
            .medBehandlingsresultat(behandlingsresultat)
            .build();
        behandlingVedtakRepository.lagre(vedtak, behandlingRepository.taSkriveLås(behandling));

        oppdaterMedBehandlingsresultat(behandling, innvilget);

        return behandling;
    }

    private void oppdaterMedBehandlingsresultat(Behandling behandling, boolean innvilget) {
        VilkårResultat vilkårResultat = VilkårResultat.builder()
            .leggTilVilkårResultat(VilkårType.FØDSELSVILKÅRET_MOR, innvilget ? VilkårUtfallType.OPPFYLT : VilkårUtfallType.IKKE_OPPFYLT, null, new Properties(), null, false, false, null, null)
            .medVilkårResultatType(innvilget ? VilkårResultatType.INNVILGET : VilkårResultatType.AVSLÅTT)
            .buildFor(behandling);
        repository.lagre(vilkårResultat);
        if (innvilget) {
            BeregningResultat beregningResultat = BeregningResultat.builder()
                .medBeregning(new Beregning(48500L, 1L, 48500L, LocalDateTime.now()))
                .buildFor(behandling);
            repository.lagre(beregningResultat);
        }
    }

    private void buildOppdragskontroll(Long behandlingId, Long delytelseId) {
        Oppdragskontroll oppdrag = Oppdragskontroll.builder()
            .medBehandlingId(behandlingId)
            .medSaksnummer(SAKSNUMMER)
            .medVenterKvittering(false)
            .medProsessTaskId(56L)
            .medSimulering(false)
            .build();

        Avstemming115 avstemming115 = buildAvstemming115();
        Oppdrag110 oppdrag110 = buildOppdrag110(oppdrag, avstemming115);
        buildOppdragslinje150(oppdrag110, delytelseId);

        økonomioppdragRepository.lagre(oppdrag);
    }

    private Oppdragslinje150 buildOppdragslinje150(Oppdrag110 oppdrag110, Long delytelseId) {

        return Oppdragslinje150.builder()
            .medKodeEndringLinje("ENDR")
            .medKodeStatusLinje("OPPH")
            .medDatoStatusFom(LocalDate.now())
            .medVedtakId("345")
            .medDelytelseId(delytelseId)
            .medKodeKlassifik("FPENFOD-OP")
            .medVedtakFomOgTom(LocalDate.now(), LocalDate.now())
            .medSats(61122L)
            .medFradragTillegg(TfradragTillegg.F.value())
            .medTypeSats(ØkonomiTypeSats.UKE.name())
            .medBrukKjoreplan("B")
            .medSaksbehId("F2365245")
            .medUtbetalesTilId("123456789")
            .medOppdrag110(oppdrag110)
            .medHenvisning(43L)
            .build();

    }

    private Avstemming115 buildAvstemming115() {
        return Avstemming115.builder()
            .medKodekomponent(Fagsystem.FPSAK.getOffisiellKode())
            .medNokkelAvstemming(LocalDateTime.now())
            .medTidspnktMelding(LocalDateTime.now().minusDays(1))
            .build();
    }

    private Oppdrag110 buildOppdrag110(Oppdragskontroll oppdragskontroll, Avstemming115 avstemming115) {
        return Oppdrag110.builder()
            .medKodeAksjon(ØkonomiKodeAksjon.TRE.getKodeAksjon())
            .medKodeEndring(ØkonomiKodeEndring.NY.name())
            .medKodeFagomrade(ØkonomiKodeFagområde.REFUTG.name())
            .medFagSystemId(OPPDRAG_FAGSYSTEM_ID)
            .medUtbetFrekvens(ØkonomiUtbetFrekvens.DAG.getUtbetFrekvens())
            .medOppdragGjelderId("22038235641")
            .medDatoOppdragGjelderFom(LocalDate.of(2000, 1, 1))
            .medSaksbehId("J5624215")
            .medAvstemming115(avstemming115)
            .medOppdragskontroll(oppdragskontroll)
            .build();
    }
}

