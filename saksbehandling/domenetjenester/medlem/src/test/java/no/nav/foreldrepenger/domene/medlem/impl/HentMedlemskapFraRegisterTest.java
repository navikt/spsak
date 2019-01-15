package no.nav.foreldrepenger.domene.medlem.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapKildeType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.FiktiveFnr;
import no.nav.foreldrepenger.domene.medlem.api.FinnMedlemRequest;
import no.nav.foreldrepenger.domene.medlem.api.Medlemskapsperiode;
import no.nav.foreldrepenger.domene.medlem.api.MedlemskapsperiodeKoder;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.tjeneste.felles.v1.informasjon.ForretningsmessigUnntaksdetaljer;
import no.nav.tjeneste.virksomhet.medlemskap.v2.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.medlemskap.v2.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.Medlemsperiode;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.Studieinformasjon;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.GrunnlagstypeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.KildeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.LandkodeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.LovvalgMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.PeriodetypeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.StatuskodeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.TrygdedekningMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeListeRequest;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeListeResponse;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.medl.MedlemConsumer;

public class HentMedlemskapFraRegisterTest {

    private static final FiktiveFnr FIKTIVE_FNR = new FiktiveFnr();
    
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private MedlemConsumer medlemConsumer = mock(MedlemConsumer.class);
    private HentMedlemskapFraRegister medlemTjeneste;
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    private static final PersonIdent IDENT = FIKTIVE_FNR.nestePersonIdent();
    private static final LocalDate FRA_OG_MED = LocalDate.of(2009, 1, 1);
    private static final LocalDate TIL_OG_MED = LocalDate.of(2020, 12, 31);
    private static final long MEDL_ID_1 = 2663947L;
    private static final long MEDL_ID_2 = 2663948L;
    private static final long MEDL_ID_3 = 666L;

    @Before
    public void before() {
        medlemTjeneste = new HentMedlemskapFraRegister(medlemConsumer, kodeverkRepository);
    }

    @Test
    public void skal_hente_medlemsperioder_og_logge_dem_til_saksopplysningslageret()throws Exception {
        // Arrange
        HentPeriodeListeResponse response = opprettResponse();

        ArgumentCaptor<HentPeriodeListeRequest> requestCaptor = ArgumentCaptor.forClass(HentPeriodeListeRequest.class);
        when(medlemConsumer.hentPeriodeListe(requestCaptor.capture())).thenReturn(response);

        FinnMedlemRequest finnMedlemRequest = new FinnMedlemRequest(IDENT, FRA_OG_MED, TIL_OG_MED);

        // Act
        List<Medlemskapsperiode> medlemskapsperioder = medlemTjeneste.finnMedlemskapPerioder(finnMedlemRequest);

        // Assert
        assertThat(medlemskapsperioder.size()).isEqualTo(3);

        Medlemskapsperiode medlemskapsperiode1 = new Medlemskapsperiode.Builder()
            .medFom(LocalDate.of(2010, 8, 1))
            .medTom(LocalDate.of(2010, 12, 31))
            .medDatoBesluttet(LocalDate.of(2012, 5, 26))
            .medErMedlem(true)
            .medDekning(MedlemskapDekningType.FULL)
            .medLovvalg(MedlemskapType.ENDELIG)
            .medLovvalgsland(kodeverkRepository.finn(Landkoder.class, "UZB"))
            .medKilde(MedlemskapKildeType.AVGSYS)
            .medStudieland(kodeverkRepository.finn(Landkoder.class, "VUT"))
            .medMedlId(MEDL_ID_1)
            .build();
        Medlemskapsperiode medlemskapsperiode2 = new Medlemskapsperiode.Builder()
            .medFom(LocalDate.of(2011, 1, 1))
            .medTom(LocalDate.of(2011, 12, 31))
            .medDatoBesluttet(LocalDate.of(2012, 5, 26))
            .medErMedlem(true)
            .medDekning(MedlemskapDekningType.FTL_2_9_1_a)
            .medLovvalg(MedlemskapType.ENDELIG)
            .medKilde(MedlemskapKildeType.AVGSYS)
            .medMedlId(MEDL_ID_2)
            .build();
        Medlemskapsperiode medlemskapsperiode3 = new Medlemskapsperiode.Builder()
            .medFom(LocalDate.of(2012, 1, 1))
            .medTom(LocalDate.of(2012, 12, 31))
            .medDatoBesluttet(LocalDate.of(2013, 5, 26))
            .medErMedlem(false)
            .medDekning(MedlemskapDekningType.FULL)
            .medLovvalg(MedlemskapType.UNDER_AVKLARING)
            .medKilde(MedlemskapKildeType.LAANEKASSEN)
            .medMedlId(MEDL_ID_3)
            .build();
        assertThat(medlemskapsperioder).containsExactlyInAnyOrder(medlemskapsperiode1, medlemskapsperiode2, medlemskapsperiode3);

    }

    @Test
    public void skal_handtere_personikkefunnet_exception_fra_consumer()  {
        try {
            // Arrange
            PersonIkkeFunnet personIkkeFunnet = new PersonIkkeFunnet("Feil", new ForretningsmessigUnntaksdetaljer());
            doThrow(personIkkeFunnet).when(medlemConsumer).hentPeriodeListe(any(HentPeriodeListeRequest.class));
            FinnMedlemRequest finnMedlemRequest = new FinnMedlemRequest(IDENT, FRA_OG_MED, TIL_OG_MED);

            // Act
            medlemTjeneste.finnMedlemskapPerioder(finnMedlemRequest);
            fail("Forventet VLException");
        } catch (Exception e) {
            // Assert
            assertThat(e.getCause()).isInstanceOf(PersonIkkeFunnet.class);
            assertThat(e.getCause().getMessage()).contains("Feil");
        }
    }

    @Test
    public void skal_handtere_sikkerhet_exception_fra_consumer()  {
        try {
            // Arrange
            Sikkerhetsbegrensning sikkerhetsbegrensning = new Sikkerhetsbegrensning("Feil", new ForretningsmessigUnntaksdetaljer());
            doThrow(sikkerhetsbegrensning).when(medlemConsumer).hentPeriodeListe(any(HentPeriodeListeRequest.class));
            FinnMedlemRequest finnMedlemRequest = new FinnMedlemRequest(IDENT, FRA_OG_MED, TIL_OG_MED);

            // Act
            medlemTjeneste.finnMedlemskapPerioder(finnMedlemRequest);
            fail("Forventet VLException");
        } catch (Exception e) {
            // Assert
            assertThat(e).isInstanceOf(IntegrasjonException.class);
            assertThat(e.getMessage()).contains("sikkerhetsavvik");
        }
    }

    private HentPeriodeListeResponse opprettResponse() throws Exception {
        Medlemsperiode periode1 = new Medlemsperiode()
            .withId(MEDL_ID_1)
            .withFraOgMed(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(2010, 8, 1)))
            .withTilOgMed(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(2010, 12, 31)))
            .withDatoBesluttet(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(2012, 5, 26)))
            .withStatus(new StatuskodeMedTerm().withValue(MedlemskapsperiodeKoder.PeriodeStatus.GYLD.toString()))
            .withTrygdedekning(new TrygdedekningMedTerm().withValue("Full"))
            .withType(new PeriodetypeMedTerm().withValue(MedlemskapsperiodeKoder.PeriodeType.PMMEDSKP.toString()))
            .withLovvalg(new LovvalgMedTerm().withValue("ENDL"))
            .withLand(new LandkodeMedTerm().withValue("UZB"))
            .withKilde(new KildeMedTerm().withValue("AVGSYS"))
            .withStudieinformasjon(new Studieinformasjon().withStudieland(new LandkodeMedTerm().withValue("VUT")))
            .withGrunnlagstype(new GrunnlagstypeMedTerm().withValue("MEDFT"));

        Medlemsperiode periode2 = new Medlemsperiode()
            .withId(MEDL_ID_2)
            .withFraOgMed(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(2011, 1, 1)))
            .withTilOgMed(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(2011, 12, 31)))
            .withDatoBesluttet(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(2012, 5, 26)))
            .withStatus(new StatuskodeMedTerm().withValue(MedlemskapsperiodeKoder.PeriodeStatus.GYLD.toString()))
            .withTrygdedekning(new TrygdedekningMedTerm().withValue("FTL_2-9_1_ledd_a"))
            .withType(new PeriodetypeMedTerm().withValue(MedlemskapsperiodeKoder.PeriodeType.PMMEDSKP.toString()))
            .withLovvalg(new LovvalgMedTerm().withValue("ENDL"))
            .withKilde(new KildeMedTerm().withValue("AVGSYS"))
            .withGrunnlagstype(new GrunnlagstypeMedTerm().withValue("MEDFT"));

        // Periode med type = uten medlemskap, skal oversees
        Medlemsperiode periode3 = new Medlemsperiode()
            .withId(MEDL_ID_3)
            .withFraOgMed(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(2012, 1, 1)))
            .withTilOgMed(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(2012, 12, 31)))
            .withDatoBesluttet(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(2013, 5, 26)))
            .withStatus(new StatuskodeMedTerm().withValue(MedlemskapsperiodeKoder.PeriodeStatus.UAVK.toString()))
            .withTrygdedekning(new TrygdedekningMedTerm().withValue("Full"))
            .withType(new PeriodetypeMedTerm().withValue(MedlemskapsperiodeKoder.PeriodeType.PUMEDSKP.toString()))
            .withLovvalg(new LovvalgMedTerm().withValue("UAVK"))
            .withKilde(new KildeMedTerm().withValue("LAANEKASSEN"))
            .withGrunnlagstype(new GrunnlagstypeMedTerm().withValue("MEDFT"));

        return new HentPeriodeListeResponse()
            .withPeriodeListe(periode1, periode2, periode3);
    }
}
