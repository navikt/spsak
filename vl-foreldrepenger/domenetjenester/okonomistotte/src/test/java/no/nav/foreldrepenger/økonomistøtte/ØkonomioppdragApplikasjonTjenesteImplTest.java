package no.nav.foreldrepenger.økonomistøtte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Attestant180;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Grad170;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragsenhet120;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Refusjonsinfo156;
import no.nav.foreldrepenger.økonomistøtte.api.ØkonomiKvittering;
import no.nav.foreldrepenger.økonomistøtte.api.ØkonomioppdragApplikasjonTjeneste;
import no.nav.foreldrepenger.økonomistøtte.queue.producer.ØkonomioppdragJmsProducer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHendelseMottak;

public class ØkonomioppdragApplikasjonTjenesteImplTest {

    private static final Long PROSESSTASKID = 33L;

    public static final Long FAGSYSTEMID = 124L;

    public static final Long FAGSYSTEMID_2 = 256L;

    public static final Long OPPDRAGSKONTROLLID = 55L;

    public static final String KODEAKSJON = "1";

    public static final String KODEENDRING = "NY";

    public static final String KODEFAGOMRADE_ES = "REFUTG";

    public static final String KODEFAGOMRADE_FP = "FP";

    public static final String KODEFAGOMRADE_FPREF = "FPREF";

    public static final String UTBETFREKVENS = "ENG";

    public static final String OPPDRAGGJELDERID = "01010101010";

    public static final String SAKSBEHID = "aa000000";

    public static final String TYPEENHET = "BOS";

    public static final String ENHET = "ENHET";

    public static final String KODEENDRINGLINJE = "NY";

    public static final String KODEKLASSIFIK_ES = "FPENFOD-OP";

    public static final String KODEKLASSIFIK_FP = "FPATORD";

    public static final Long SATS = 654L;

    public static final String FRADRAGTILLEGG = "T";

    public static final String TYPESATS_ES = "ENG";

    public static final String TYPESATS_FP = "DAG";

    public static final Integer GRAD = 100;

    public static final String TYPE_GRAD = "UFOR";

    public static final String REFUNDERES_ID = "123456789";

    public static final String VEDTAKID = "VedtakId";

    private static final String KVITTERING_OK = "00";

    private static final String KVITTERING_MELDING_OK = "Oppdrag utført";

    public static final Long BEHANDLINGID_ES = 126L;

    public static final Long BEHANDLINGID_FP = 237L;

    private static final String KVITTERING_FEIL = "12";

    private static final String KVITTERING_MELDING_FEIL = "Oppdrag ikke utført";

    private static final String KVITTERING_MELDINGKODE_FEIL = "QWERTY12";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Mock
    private OppdragskontrollTjeneste oppdragskontrollTjeneste;
    @Mock
    private ProsessTaskHendelseMottak hendelsesmottak;

    private Behandling behandlingES;

    private Behandling behandlingFP;

    private ØkonomioppdragApplikasjonTjeneste økonomioppdragApplikasjonTjeneste;

    @Mock
    private Oppdragskontroll oppdrag;

    @Mock
    private BeregningsresultatFP beregningsresultatFP;

    @Mock
    private Avstemming115 a115;

    @Mock
    private List<Oppdrag110> o110liste;

    @Mock
    private ØkonomioppdragRepository økonomioppdragRepository;

    @Mock
    private BeregningsresultatFPRepository beregningsresultatFPRepository;

    @Mock
    private BehandlingRepository behandlingRepository;

    @Mock
    private ØkonomioppdragJmsProducer økonomioppdragJmsProducer;


    @Before
    public void setUp() {

        o110liste = new ArrayList<>();
        a115 = mock(Avstemming115.class);

        when(a115.getKodekomponent()).thenReturn("KK");
        when(a115.getNokkelAvstemming()).thenReturn(LocalDateTime.now());
        when(a115.getTidspnktMelding()).thenReturn(LocalDateTime.now());


        when(oppdrag.getOppdrag110Liste()).thenReturn(o110liste);
        when(oppdrag.getProsessTaskId()).thenReturn(PROSESSTASKID);
        when(oppdrag.getId()).thenReturn(OPPDRAGSKONTROLLID);

        økonomioppdragApplikasjonTjeneste = new ØkonomioppdragApplikasjonTjenesteImpl(oppdragskontrollTjeneste, hendelsesmottak, økonomioppdragRepository, økonomioppdragJmsProducer);

    }

    private void setupBehandlingES() {
        behandlingES = OpprettBehandling.opprettBehandlingMedTermindato().lagMocked();

        OpprettBehandling.genererBehandlingOgResultat(behandlingES, VedtakResultatType.INNVILGET, 1);
    }

    private void setupBehandlingFP() {
        behandlingFP = ScenarioMorSøkerForeldrepenger.forFødsel().lagMocked();

        Behandlingsresultat.builderForInngangsvilkår().buildFor(behandlingFP);

    }

    @Test
    public void utbetaleTestES() {
        // Arrange
        setupBehandlingES();
        setupOppdrag110(o110liste, a115, false);

        when(oppdragskontrollTjeneste.opprettOppdrag(behandlingES.getId(), PROSESSTASKID)).thenReturn(45L);
        when(oppdragskontrollTjeneste.hentOppdragskontroll(45L)).thenReturn(oppdrag);

        // Act
        økonomioppdragApplikasjonTjeneste.utførOppdrag(behandlingES.getId(), PROSESSTASKID, true);

        // Assert
        verify(oppdragskontrollTjeneste).opprettOppdrag(behandlingES.getId(), PROSESSTASKID);
        verify(oppdragskontrollTjeneste).hentOppdragskontroll(any());
        verify(økonomioppdragJmsProducer).sendØkonomiOppdrag(any());
    }

    @Test
    public void skalOppretteOgLagreOppdragMenSkalIkkeSendeTilØkonomiNårSendOppdragPropertyAvProsessTaskenErSatt() {
        //Arrange
        setupBehandlingES();

        //Act
        økonomioppdragApplikasjonTjeneste.utførOppdrag(behandlingES.getId(), PROSESSTASKID, false);

        //Assert
        verify(oppdragskontrollTjeneste).opprettOppdrag(behandlingES.getId(), PROSESSTASKID);
        verify(oppdragskontrollTjeneste, never()).hentOppdragskontroll(any());
        verify(økonomioppdragJmsProducer, never()).sendØkonomiOppdrag(any());
    }

    @Test
    public void utbetaleTestFP() {
        // Arrange
        setupBehandlingFP();
        setupOppdrag110(o110liste, a115, true);

        when(oppdragskontrollTjeneste.opprettOppdrag(behandlingFP.getId(), PROSESSTASKID)).thenReturn(47L);
        when(oppdragskontrollTjeneste.hentOppdragskontroll(47L)).thenReturn(oppdrag);
        when(beregningsresultatFPRepository.hentBeregningsresultatFP(behandlingFP)).thenReturn(java.util.Optional.ofNullable(beregningsresultatFP));

        // Act
        økonomioppdragApplikasjonTjeneste.utførOppdrag(behandlingFP.getId(), PROSESSTASKID, true);

        // Assert
        verify(oppdragskontrollTjeneste).opprettOppdrag(behandlingFP.getId(), PROSESSTASKID);
        verify(oppdragskontrollTjeneste).hentOppdragskontroll(any());
        verify(økonomioppdragJmsProducer, times(2)).sendØkonomiOppdrag(any());
    }

    @Test
    public void kvitteringsmottakPositivTestES() {
        // Arrange
        setupOppdrag110(o110liste, a115, false);
        when(behandlingRepository.hentBehandling(BEHANDLINGID_ES)).thenReturn(behandlingES);
        when(økonomioppdragRepository.finnVentendeOppdrag(BEHANDLINGID_ES)).thenReturn(oppdrag);
        ØkonomiKvittering kvittering = opprettKvittering(KVITTERING_OK, null, KVITTERING_MELDING_OK, FAGSYSTEMID, false);

        // Act
        økonomioppdragApplikasjonTjeneste.behandleKvittering(kvittering);

        // Assert
        verify(oppdrag).setVenterKvittering(false);
        verify(økonomioppdragRepository).lagre(oppdrag);
        verify(hendelsesmottak).mottaHendelse(PROSESSTASKID, ProsessTaskHendelse.ØKONOMI_OPPDRAG_KVITTERING);

    }

    @Test
    public void kvitteringsmottakPositivTestFP() {
        // Arrange
        setupOppdrag110(o110liste, a115, true);

        when(behandlingRepository.hentBehandling(BEHANDLINGID_FP)).thenReturn(behandlingFP);
        when(økonomioppdragRepository.finnVentendeOppdrag(BEHANDLINGID_FP)).thenReturn(oppdrag);
        ØkonomiKvittering kvittering_1 = opprettKvittering(KVITTERING_OK, null, KVITTERING_MELDING_OK, FAGSYSTEMID, true);
        ØkonomiKvittering kvittering_2 = opprettKvittering(KVITTERING_OK, null, KVITTERING_MELDING_OK, FAGSYSTEMID_2, true);

        // Act
        økonomioppdragApplikasjonTjeneste.behandleKvittering(kvittering_1);
        økonomioppdragApplikasjonTjeneste.behandleKvittering(kvittering_2);

        // Assert
        verify(oppdrag).setVenterKvittering(false);
        verify(økonomioppdragRepository, times(2)).lagre(oppdrag);
        verify(hendelsesmottak).mottaHendelse(PROSESSTASKID, ProsessTaskHendelse.ØKONOMI_OPPDRAG_KVITTERING);
        o110liste.forEach(o110 -> {
            assertThat(o110.getOppdragKvitteringListe()).isNotEmpty();
            assertThat(o110.getOppdragKvitteringListe().get(0).getOppdrag110()).isNotNull();
        });


    }

    @Test
    public void kvitteringsmottakNegativTestES() {
        // Arrange
        setupOppdrag110(o110liste, a115, false);

        when(behandlingRepository.hentBehandling(BEHANDLINGID_ES)).thenReturn(behandlingES);
        when(økonomioppdragRepository.finnVentendeOppdrag(BEHANDLINGID_ES)).thenReturn(oppdrag);
        ØkonomiKvittering kvittering = opprettKvittering(KVITTERING_FEIL, KVITTERING_MELDINGKODE_FEIL, KVITTERING_MELDING_FEIL, FAGSYSTEMID, false);


        // Act
        økonomioppdragApplikasjonTjeneste.behandleKvittering(kvittering);

        // Assert
        verify(oppdrag).setVenterKvittering(false);
        verify(økonomioppdragRepository).lagre(oppdrag);
        verify(hendelsesmottak, never()).mottaHendelse(any(), any());
    }

    @Test
    public void kvitteringsmottakNegativTestFP() {
        // Arrange
        setupOppdrag110(o110liste, a115, true);

        when(behandlingRepository.hentBehandling(BEHANDLINGID_FP)).thenReturn(behandlingFP);
        when(økonomioppdragRepository.finnVentendeOppdrag(BEHANDLINGID_FP)).thenReturn(oppdrag);
        ØkonomiKvittering kvittering_1 = opprettKvittering(KVITTERING_FEIL, KVITTERING_MELDINGKODE_FEIL, KVITTERING_MELDING_FEIL, FAGSYSTEMID, true);
        ØkonomiKvittering kvittering_2 = opprettKvittering(KVITTERING_FEIL, KVITTERING_MELDINGKODE_FEIL, KVITTERING_MELDING_FEIL, FAGSYSTEMID_2, true);

        // Act
        økonomioppdragApplikasjonTjeneste.behandleKvittering(kvittering_1);
        økonomioppdragApplikasjonTjeneste.behandleKvittering(kvittering_2);

        // Assert
        verify(oppdrag).setVenterKvittering(false);
        verify(økonomioppdragRepository, times(2)).lagre(oppdrag);
        verify(hendelsesmottak, never()).mottaHendelse(any(), any());

    }

    private ØkonomiKvittering opprettKvittering(String alvorlighetsgrad, String meldingKode, String beskrMelding, Long fagsystemId, Boolean gjelderFP) {
        ØkonomiKvittering kvittering = new ØkonomiKvittering();
        kvittering.setAlvorlighetsgrad(alvorlighetsgrad);
        kvittering.setMeldingKode(meldingKode);
        kvittering.setBehandlingId(gjelderFP ? BEHANDLINGID_FP : BEHANDLINGID_ES);
        kvittering.setBeskrMelding(beskrMelding);
        kvittering.setFagsystemId(fagsystemId);
        return kvittering;
    }

    private void setupOppdrag110(List<Oppdrag110> o110liste, Avstemming115 a115, Boolean gjelderFP) {
        Oppdrag110 o110_1 = new Oppdrag110.Builder()
            .medAvstemming115(a115)
            .medKodeAksjon(KODEAKSJON)
            .medKodeEndring(KODEENDRING)
            .medKodeFagomrade(hentKodeFagomrade(gjelderFP, true))
            .medFagSystemId(FAGSYSTEMID)
            .medUtbetFrekvens(UTBETFREKVENS)
            .medOppdragGjelderId(OPPDRAGGJELDERID)
            .medDatoOppdragGjelderFom(LocalDate.now())
            .medSaksbehId(SAKSBEHID)
            .medOppdragskontroll(oppdrag)
            .build();
        new Oppdragsenhet120.Builder()
            .medTypeEnhet(TYPEENHET)
            .medDatoEnhetFom(LocalDate.now())
            .medEnhet(ENHET)
            .medOppdrag110(o110_1)
            .build();
        Oppdragslinje150 o150_1 = new Oppdragslinje150.Builder()
            .medVedtakId(VEDTAKID)
            .medDelytelseId(101002100100L)
            .medKodeEndringLinje(KODEENDRINGLINJE)
            .medKodeKlassifik(hentKodeKlassifik(gjelderFP))
            .medVedtakFomOgTom(LocalDate.now(), LocalDate.now())
            .medSats(SATS)
            .medFradragTillegg(FRADRAGTILLEGG)
            .medTypeSats(hentTypeSats(gjelderFP))
            .medBrukKjoreplan("N")
            .medSaksbehId(SAKSBEHID)
            .medUtbetalesTilId(OPPDRAGGJELDERID)
            .medHenvisning(gjelderFP ? BEHANDLINGID_FP : BEHANDLINGID_ES)
            .medOppdrag110(o110_1)
            .build();
        Attestant180.builder()
            .medAttestantId(SAKSBEHID)
            .medOppdragslinje150(o150_1)
            .build();
        if (gjelderFP) {
            leggTilGrad170(Collections.singletonList(o150_1));
        }
        o110liste.add(o110_1);

        if (gjelderFP) {
            Oppdrag110 o110_2 = new Oppdrag110.Builder()
                .medAvstemming115(a115)
                .medKodeAksjon(KODEAKSJON)
                .medKodeEndring(KODEENDRING)
                .medKodeFagomrade(hentKodeFagomrade(gjelderFP, false))
                .medFagSystemId(FAGSYSTEMID_2)
                .medUtbetFrekvens(UTBETFREKVENS)
                .medOppdragGjelderId(OPPDRAGGJELDERID)
                .medDatoOppdragGjelderFom(LocalDate.now())
                .medSaksbehId(SAKSBEHID)
                .medOppdragskontroll(oppdrag)
                .build();
            new Oppdragsenhet120.Builder()
                .medTypeEnhet(TYPEENHET)
                .medDatoEnhetFom(LocalDate.now())
                .medEnhet(ENHET)
                .medOppdrag110(o110_2)
                .build();
            Oppdragslinje150 o150_2 = new Oppdragslinje150.Builder()
                .medVedtakId(VEDTAKID)
                .medDelytelseId(101002101100L)
                .medKodeEndringLinje(KODEENDRINGLINJE)
                .medKodeKlassifik(hentKodeKlassifik(gjelderFP))
                .medVedtakFomOgTom(LocalDate.now(), LocalDate.now())
                .medSats(SATS)
                .medFradragTillegg(FRADRAGTILLEGG)
                .medTypeSats(hentTypeSats(gjelderFP))
                .medBrukKjoreplan("N")
                .medSaksbehId(SAKSBEHID)
                .medHenvisning(gjelderFP ? BEHANDLINGID_FP : BEHANDLINGID_ES)
                .medOppdrag110(o110_2)
                .build();
            Attestant180.builder()
                .medAttestantId(SAKSBEHID)
                .medOppdragslinje150(o150_2)
                .build();
            leggTilGrad170(Collections.singletonList(o150_2));
            leggTilRefusjons156(Collections.singletonList(o150_2));

            o110liste.add(o110_2);

        }
    }

    private void leggTilGrad170(List<Oppdragslinje150> o150Liste) {
        for (Oppdragslinje150 o150 : o150Liste) {
            Grad170.builder()
                .medGrad(GRAD)
                .medTypeGrad(TYPE_GRAD)
                .medOppdragslinje150(o150)
                .build();
        }
    }

    private void leggTilRefusjons156(List<Oppdragslinje150> o150Liste) {
        for (Oppdragslinje150 o150 : o150Liste) {
            Refusjonsinfo156.builder()
                .medMaksDato(LocalDate.now())
                .medRefunderesId(REFUNDERES_ID)
                .medDatoFom(LocalDate.now())
                .medOppdragslinje150(o150)
                .build();
        }
    }


    private String hentKodeFagomrade(Boolean gjelderFP, Boolean brukerErMottaker) {
        if (gjelderFP) {
            return brukerErMottaker ? KODEFAGOMRADE_FP : KODEFAGOMRADE_FPREF;
        }
        return KODEFAGOMRADE_ES;
    }

    private String hentKodeKlassifik(Boolean gjelderFP) {
        if (gjelderFP) {
            return KODEKLASSIFIK_FP;
        }
        return KODEKLASSIFIK_ES;
    }

    private String hentTypeSats(Boolean gjelderFP) {
        if (gjelderFP) {
            return TYPESATS_FP;
        }
        return TYPESATS_ES;
    }
}
