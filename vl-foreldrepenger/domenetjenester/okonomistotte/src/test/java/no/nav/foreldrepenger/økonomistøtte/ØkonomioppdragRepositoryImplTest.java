package no.nav.foreldrepenger.økonomistøtte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Attestant180;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Grad170;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.OppdragKvittering;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragsenhet120;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Refusjonsinfo156;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TfradragTillegg;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TkodeStatusLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeAksjon;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndring;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKomponent;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiTypeSats;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiUtbetFrekvens;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class ØkonomioppdragRepositoryImplTest {

    private static final String KODE_KLASSIFIK_FODSEL = "FPENFOD-OP";

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final Repository repository = repoRule.getRepository();

    private final ØkonomioppdragRepository økonomioppdragRepository = new ØkonomioppdragRepositoryImpl(repoRule.getEntityManager());


    @Test
    public void lagreOgHenteOppdragskontroll() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();

        // Act
        økonomioppdragRepository.lagre(oppdrkontroll);

        // Assert
        Long id = oppdrkontroll.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Oppdragskontroll oppdrkontrollLest = økonomioppdragRepository.hentOppdragskontroll(id);

        assertThat(oppdrkontrollLest).isNotNull();
    }

    @Test
    public void lagreOgSøkeOppOppdragskontroll() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();
        Long behandlingId = oppdrkontroll.getBehandlingId();

        // Act
        økonomioppdragRepository.lagre(oppdrkontroll);

        // Assert
        Long id = oppdrkontroll.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Oppdragskontroll oppdrkontrollLest = økonomioppdragRepository.finnVentendeOppdrag(behandlingId);

        assertThat(oppdrkontrollLest).isNotNull();
    }

    @Test
    public void lagreOgSøkeOppOppdragskontrollForPeriode() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();
        Long behandlingId = oppdrkontroll.getBehandlingId();

        Avstemming115 avstemming115 = buildAvstemming115();

        buildOppdrag110(oppdrkontroll, avstemming115, 44L);

        // Act
        økonomioppdragRepository.lagre(oppdrkontroll);
        repository.flushAndClear();

        // Assert

        List<Oppdrag110> oppdragListe = økonomioppdragRepository.hentOppdrag110ForPeriodeOgFagområde(LocalDate.now(), LocalDate.now(), "REFUTG");
        assertThat(oppdragListe).hasSize(1);
        assertThat(behandlingId).isEqualTo(oppdragListe.get(0).getOppdragskontroll().getBehandlingId());
    }

    @Test
    public void lagreOgSøkeOppOppdragskontrollForPeriodeUtenResultat() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();

        // Act
        økonomioppdragRepository.lagre(oppdrkontroll);
        repository.flushAndClear();

        // Assert

        List<Oppdrag110> oppdragListe = økonomioppdragRepository.hentOppdrag110ForPeriodeOgFagområde(LocalDate.now().minusDays(1), LocalDate.now().minusDays(1), "REFUTG");
        assertThat(oppdragListe).isEmpty();
    }

    @Test
    public void lagreOgSøkeOppOppdragskontrollForBehandling() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();
        Long behandlingId = oppdrkontroll.getBehandlingId();

        // Act
        økonomioppdragRepository.lagre(oppdrkontroll);

        // Assert
        Long id = oppdrkontroll.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Optional<Oppdragskontroll> oppdrkontrollLest = økonomioppdragRepository.finnOppdragForBehandling(behandlingId);
        assertThat(oppdrkontrollLest).isPresent();
        assertThat(oppdrkontrollLest.get()).isNotNull();
    }

    @Test
    public void lagreOgSøkeOppOppdragskontrollDerKvitteringErMottatt() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();
        oppdrkontroll.setVenterKvittering(false);
        Long behandlingId = oppdrkontroll.getBehandlingId();

        // Act
        økonomioppdragRepository.lagre(oppdrkontroll);

        // Assert
        Long id = oppdrkontroll.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        try {
            økonomioppdragRepository.finnVentendeOppdrag(behandlingId);
            fail("Ventet exception");
        } catch (TekniskException te) {
            assertThat(te.getMessage()).contains("F-650018");
        }
    }

    @Test
    public void lagreOppdrag110() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();

        Avstemming115 avstemming115 = buildAvstemming115();

        buildOppdrag110(oppdrkontroll, avstemming115, 44L);

        // Act
        long id = økonomioppdragRepository.lagre(oppdrkontroll);

        // Assert

        repository.flushAndClear();
        Oppdragskontroll oppdrkontrollLest = økonomioppdragRepository.hentOppdragskontroll(id);
        assertThat(oppdrkontrollLest.getOppdrag110Liste()).hasSize(1);
        Oppdrag110 oppdr110Lest = oppdrkontrollLest.getOppdrag110Liste().get(0);
        assertThat(oppdr110Lest).isNotNull();
        assertThat(oppdr110Lest.getId()).isNotEqualTo(0);
    }

    @Test
    public void lagreOppdragKvittering() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();

        Avstemming115 avstemming115 = buildAvstemming115();

        Oppdrag110 oppdrag110 = buildOppdrag110(oppdrkontroll, avstemming115, 44L);

        buildOppdragKvittering(oppdrag110);

        // Act
        long id = økonomioppdragRepository.lagre(oppdrkontroll);

        // Assert

        repository.flushAndClear();
        Oppdragskontroll oppdrkontrollLest = økonomioppdragRepository.hentOppdragskontroll(id);
        assertThat(oppdrkontrollLest.getOppdrag110Liste()).hasSize(1);
        Oppdrag110 oppdr110Lest = oppdrkontrollLest.getOppdrag110Liste().get(0);
        assertThat(oppdr110Lest).isNotNull();
        assertThat(oppdr110Lest.getId()).isNotEqualTo(0);
        OppdragKvittering oppdrKvittering = oppdr110Lest.getOppdragKvitteringListe().get(0);
        assertThat(oppdrKvittering).isNotNull();
        assertThat(oppdrKvittering.getId()).isNotEqualTo(0);
    }

    @Test
    public void finnerSisteOppdragForSakES() {
        Saksnummer saksnr = new Saksnummer("1234");
        long fagsystemId = 123100L;

        Oppdragskontroll oppdragskontroll = buildOppdragskontroll(saksnr, 1L, false);
        buildOppdrag110(oppdragskontroll, buildAvstemming115(), fagsystemId);
        økonomioppdragRepository.lagre(oppdragskontroll);

        Oppdragskontroll nyesteOppdragskontroll = buildOppdragskontroll(saksnr, 2L, false);
        buildOppdrag110(nyesteOppdragskontroll, buildAvstemming115(), fagsystemId);
        long nyesteId = økonomioppdragRepository.lagre(nyesteOppdragskontroll);

        repository.flushAndClear();

        Optional<Oppdragskontroll> optional = økonomioppdragRepository.finnNyesteOppdragForSak(oppdragskontroll.getSaksnummer());
        assertThat(optional).isPresent();
        Oppdragskontroll hentetOppdrag = optional.get();
        assertThat(hentetOppdrag.getId()).isEqualTo(nyesteId);
        assertThat(hentetOppdrag.getBehandlingId()).isEqualTo(nyesteOppdragskontroll.getBehandlingId());
        assertThat(hentetOppdrag.getOppdrag110Liste().size()).isEqualTo(1);
        assertThat(hentetOppdrag.getOppdrag110Liste().get(0).getFagsystemId()).isEqualTo(fagsystemId);
    }

    @Test
    public void finnNyesteOppdragForSakESSkalIkkeHenteSimulering() {
        Saksnummer saksnr = new Saksnummer("1234");
        long fagsystemId = 101L;

        Oppdragskontroll oppdragskontroll = buildOppdragskontroll(saksnr, 1L, false);
        buildOppdrag110(oppdragskontroll, buildAvstemming115(), 100L);
        økonomioppdragRepository.lagre(oppdragskontroll);

        Oppdragskontroll nyesteOppdragskontroll = buildOppdragskontroll(saksnr, 2L, false);
        buildOppdrag110(nyesteOppdragskontroll, buildAvstemming115(), fagsystemId);
        long nyesteId = økonomioppdragRepository.lagre(nyesteOppdragskontroll);

        Oppdragskontroll simuleringOppdragskontroll = buildOppdragskontroll(saksnr, 3L, true);
        buildOppdrag110(simuleringOppdragskontroll, buildAvstemming115(), fagsystemId);
        økonomioppdragRepository.lagre(simuleringOppdragskontroll);

        repository.flushAndClear();

        Optional<Oppdragskontroll> optional = økonomioppdragRepository.finnNyesteOppdragForSak(oppdragskontroll.getSaksnummer());
        assertThat(optional).isPresent();
        Oppdragskontroll hentetOppdrag = optional.get();
        assertThat(hentetOppdrag.getId()).isEqualTo(nyesteId);
        assertThat(hentetOppdrag.getBehandlingId()).isEqualTo(nyesteOppdragskontroll.getBehandlingId());
        assertThat(hentetOppdrag.getOppdrag110Liste().size()).isEqualTo(1);
        assertThat(hentetOppdrag.getOppdrag110Liste().get(0).getFagsystemId()).isEqualTo(fagsystemId);
    }

    @Test
    public void finnerSisteOppdragForSakFP() {
        Saksnummer saksnr = new Saksnummer("1234");
        long fagsystemId = 100L;

        Oppdragskontroll oppdragskontroll = buildOppdragskontroll(saksnr, 1L, false);
        buildOppdrag110(oppdragskontroll, buildAvstemming115(), fagsystemId);
        økonomioppdragRepository.lagre(oppdragskontroll);

        Oppdragskontroll nyesteOppdragskontroll = buildOppdragskontroll(saksnr, 2L, false);
        buildOppdrag110(nyesteOppdragskontroll, buildAvstemming115(), fagsystemId);
        long nyesteId = økonomioppdragRepository.lagre(nyesteOppdragskontroll);

        repository.flushAndClear();

        Optional<Oppdragskontroll> optional = økonomioppdragRepository.finnNyesteOppdragForSak(oppdragskontroll.getSaksnummer());
        assertThat(optional).isPresent();
        Oppdragskontroll hentetOppdrag = optional.get();
        assertThat(hentetOppdrag.getId()).isEqualTo(nyesteId);
        assertThat(hentetOppdrag.getBehandlingId()).isEqualTo(nyesteOppdragskontroll.getBehandlingId());
        assertThat(hentetOppdrag.getOppdrag110Liste().size()).isEqualTo(1);
        assertThat(hentetOppdrag.getOppdrag110Liste().get(0).getFagsystemId()).isEqualTo(fagsystemId);
    }

    @Test
    public void finnNyesteOppdragForSakFPSkalIkkeHenteSimulering() {
        Saksnummer saksnr = new Saksnummer("1234");
        long fagsystemId = 100L;

        Oppdragskontroll oppdragskontroll = buildOppdragskontroll(saksnr, 1L, false);
        buildOppdrag110(oppdragskontroll, buildAvstemming115(), fagsystemId);
        økonomioppdragRepository.lagre(oppdragskontroll);

        Oppdragskontroll nyesteOppdragskontroll = buildOppdragskontroll(saksnr, 2L, false);
        buildOppdrag110(nyesteOppdragskontroll, buildAvstemming115(), fagsystemId);
        long nyesteId = økonomioppdragRepository.lagre(nyesteOppdragskontroll);

        Oppdragskontroll simuleringOppdragskontroll = buildOppdragskontroll(saksnr, 3L, true);
        buildOppdrag110(simuleringOppdragskontroll, buildAvstemming115(), fagsystemId);
        økonomioppdragRepository.lagre(simuleringOppdragskontroll);

        repository.flushAndClear();

        Optional<Oppdragskontroll> optional = økonomioppdragRepository.finnNyesteOppdragForSak(oppdragskontroll.getSaksnummer());
        assertThat(optional).isPresent();
        Oppdragskontroll hentetOppdrag = optional.get();
        assertThat(hentetOppdrag.getId()).isEqualTo(nyesteId);
        assertThat(hentetOppdrag.getBehandlingId()).isEqualTo(nyesteOppdragskontroll.getBehandlingId());
        assertThat(hentetOppdrag.getOppdrag110Liste().size()).isEqualTo(1);
        assertThat(hentetOppdrag.getOppdrag110Liste().get(0).getFagsystemId()).isEqualTo(fagsystemId);
    }

    @Test
    public void finnerAlleOppdragForSak() {
        Saksnummer saksnr = new Saksnummer("1234");
        long nyesteFagsystemId = 101L;

        Oppdragskontroll oppdragskontroll = buildOppdragskontroll(saksnr, 1L, false);
        buildOppdrag110(oppdragskontroll, buildAvstemming115(), 100L);
        økonomioppdragRepository.lagre(oppdragskontroll);

        Oppdragskontroll nyesteOppdragskontroll = buildOppdragskontroll(saksnr, 2L, false);
        buildOppdrag110(nyesteOppdragskontroll, buildAvstemming115(), nyesteFagsystemId);
        økonomioppdragRepository.lagre(nyesteOppdragskontroll);

        repository.flushAndClear();

        List<Oppdragskontroll> oppdragListe = økonomioppdragRepository.finnAlleOppdragForSak(saksnr);
        assertThat(oppdragListe).hasSize(2);
        assertThat(oppdragListe).containsExactlyInAnyOrder(oppdragskontroll, nyesteOppdragskontroll);

    }

    @Test
    public void lagreAvstemming115() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();

        Avstemming115 avstemming115 = buildAvstemming115();

        buildOppdrag110(oppdrkontroll, avstemming115, 44L);

        // Act
        økonomioppdragRepository.lagre(oppdrkontroll);

        // Assert
        Long id = avstemming115.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Avstemming115 avst115Lest = repository.hent(Avstemming115.class, id);
        assertThat(avst115Lest).isNotNull();
    }

    @Test
    public void lagreOppdragsenhet120() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();

        Avstemming115 avstemming115 = buildAvstemming115();

        Oppdrag110 oppdr110 = buildOppdrag110(oppdrkontroll, avstemming115, 44L);

        Oppdragsenhet120 oppdrsEnhet120 = buildOppdragsEnhet120(oppdr110);

        // Act
        økonomioppdragRepository.lagre(oppdrkontroll);

        // Assert
        Long id = oppdrsEnhet120.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Oppdragsenhet120 oppdrsEnhet120Lest = repository.hent(Oppdragsenhet120.class, id);
        assertThat(oppdrsEnhet120Lest).isNotNull();
    }

    @Test
    public void lagreOppdragslinje150() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();

        Avstemming115 avstemming115 = buildAvstemming115();

        Oppdrag110 oppdr110 = buildOppdrag110(oppdrkontroll, avstemming115, 44L);

        Oppdragslinje150 oppdrLinje150 = buildOppdragslinje150(oppdr110);

        // Act
        økonomioppdragRepository.lagre(oppdrkontroll);

        // Assert
        Long id = oppdrLinje150.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Oppdragslinje150 oppdrLinje150Lest = repository.hent(Oppdragslinje150.class, id);
        assertThat(oppdrLinje150Lest).isNotNull();
    }

    @Test
    public void lagreGrad170() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();

        Avstemming115 avstemming115 = buildAvstemming115();

        Oppdrag110 oppdr110 = buildOppdrag110(oppdrkontroll, avstemming115, 44L);

        Oppdragslinje150 oppdrLinje150 = buildOppdragslinje150(oppdr110);

        buildGrad170(oppdrLinje150);

        // Act
        økonomioppdragRepository.lagre(oppdrkontroll);

        // Assert
        Long id = oppdrLinje150.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Oppdragslinje150 oppdrLinje150Lest = repository.hent(Oppdragslinje150.class, id);
        assertThat(oppdrLinje150Lest).isNotNull();
        Grad170 grad170Lest = oppdrLinje150Lest.getGrad170Liste().get(0);
        assertThat(grad170Lest).isNotNull();
        assertThat(grad170Lest.getId()).isNotEqualTo(0);
    }

    @Test
    public void lagreRefusjonsinfo156() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();

        Avstemming115 avstemming115 = buildAvstemming115();

        Oppdrag110 oppdr110 = buildOppdrag110(oppdrkontroll, avstemming115, 44L);

        Oppdragslinje150 oppdrLinje150 = buildOppdragslinje150(oppdr110);

        buildRefusjonsinfo156(oppdrLinje150);

        // Act
        økonomioppdragRepository.lagre(oppdrkontroll);

        // Assert
        Long id = oppdrLinje150.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Oppdragslinje150 oppdrLinje150Lest = repository.hent(Oppdragslinje150.class, id);
        assertThat(oppdrLinje150Lest).isNotNull();
        Refusjonsinfo156 refusjonsinfo156Lest = oppdrLinje150Lest.getRefusjonsinfo156();
        assertThat(refusjonsinfo156Lest).isNotNull();
        assertThat(refusjonsinfo156Lest.getId()).isNotEqualTo(0);
    }

    @Test
    public void lagreAttestant180() {
        // Arrange
        Oppdragskontroll oppdrkontroll = buildOppdragskontroll();

        Avstemming115 avstemming115 = buildAvstemming115();

        Oppdrag110 oppdr110 = buildOppdrag110(oppdrkontroll, avstemming115, 44L);

        Oppdragslinje150 oppdrLinje150 = buildOppdragslinje150(oppdr110);

        Attestant180.Builder attestant180Builder = Attestant180.builder();
        Attestant180 attestant180 = attestant180Builder
            .medAttestantId("E8798765")
            .medOppdragslinje150(oppdrLinje150)
            .build();

        // Act
        økonomioppdragRepository.lagre(oppdrkontroll);

        // Assert
        Long id = attestant180.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Attestant180 attestant180Lest = repository.hent(Attestant180.class, id);
        assertThat(attestant180Lest).isNotNull();
    }

    private Oppdragslinje150 buildOppdragslinje150(Oppdrag110 oppdrag110) {
        Oppdragslinje150.Builder oppdrLinje150Builder = Oppdragslinje150.builder();

        return oppdrLinje150Builder
            .medKodeEndringLinje(TkodeStatusLinje.OPPH.value())
            .medKodeStatusLinje("ENDR")
            .medDatoStatusFom(LocalDate.now())
            .medVedtakId("456")
            .medDelytelseId(64L)
            .medKodeKlassifik(KODE_KLASSIFIK_FODSEL)
            .medVedtakFomOgTom(LocalDate.now(), LocalDate.now())
            .medSats(61122L)
            .medFradragTillegg(TfradragTillegg.F.value())
            .medTypeSats(ØkonomiTypeSats.UKE.name())
            .medBrukKjoreplan("B")
            .medSaksbehId("F2365245")
            .medUtbetalesTilId("123456789")
            .medOppdrag110(oppdrag110)
            .medHenvisning(47L)
            .build();

    }

    private Oppdragsenhet120 buildOppdragsEnhet120(Oppdrag110 oppdrag110) {
        Oppdragsenhet120.Builder oppdrsEnhet120Builder = Oppdragsenhet120.builder();

        return oppdrsEnhet120Builder
            .medTypeEnhet("BOS")
            .medEnhet("8020")
            .medDatoEnhetFom(LocalDate.now())
            .medOppdrag110(oppdrag110)
            .build();
    }

    private Avstemming115 buildAvstemming115() {
        Avstemming115.Builder avst115Builder = Avstemming115.builder();

        return avst115Builder
            .medKodekomponent(ØkonomiKodeKomponent.VLFP.getKodeKomponent())
            .medNokkelAvstemming(LocalDateTime.now())
            .medTidspnktMelding(LocalDateTime.now().minusDays(1))
            .build();
    }

    private Oppdrag110 buildOppdrag110(Oppdragskontroll oppdragskontroll, Avstemming115 avstemming115, Long fagsystemId) {
        Oppdrag110.Builder oppdr110Builder = Oppdrag110.builder();

        return oppdr110Builder
            .medKodeAksjon(ØkonomiKodeAksjon.TRE.name())
            .medKodeEndring(ØkonomiKodeEndring.NY.name())
            .medKodeFagomrade(ØkonomiKodeFagområde.REFUTG.name())
            .medFagSystemId(fagsystemId)
            .medUtbetFrekvens(ØkonomiUtbetFrekvens.DAG.name())
            .medOppdragGjelderId("22038235641")
            .medDatoOppdragGjelderFom(LocalDate.of(2000, 1, 1))
            .medSaksbehId("J5624215")
            .medOppdragskontroll(oppdragskontroll)
            .medAvstemming115(avstemming115)
            .build();
    }

    private void buildGrad170(Oppdragslinje150 oppdragslinje150) {
        Grad170.Builder grad170Builder = Grad170.builder();

        grad170Builder
            .medGrad(100)
            .medTypeGrad("UFOR")
            .medOppdragslinje150(oppdragslinje150)
            .build();
    }

    private void buildRefusjonsinfo156(Oppdragslinje150 oppdragslinje150) {
        Refusjonsinfo156.Builder refusjonsinfo156Builder = Refusjonsinfo156.builder();

        refusjonsinfo156Builder
            .medMaksDato(LocalDate.now())
            .medDatoFom(LocalDate.now())
            .medRefunderesId("123456789")
            .medOppdragslinje150(oppdragslinje150)
            .build();
    }

    private void buildOppdragKvittering(Oppdrag110 oppdrag110) {
        OppdragKvittering.Builder oppdragKvitteringBuilder = OppdragKvittering.builder();

        oppdragKvitteringBuilder
            .medAlvorlighetsgrad("00")
            .medBeskrMelding("beskrMelding")
            .medMeldingKode("melding")
            .medOppdrag110(oppdrag110)
            .build();
    }

    private Oppdragskontroll buildOppdragskontroll() {
        return buildOppdragskontroll(new Saksnummer("35"), 128L, false);
    }

    private Oppdragskontroll buildOppdragskontroll(Saksnummer saksnummer, long behandlingId, boolean simulering) {
        Oppdragskontroll.Builder oppdrkontrollBuilder = Oppdragskontroll.builder();

        return oppdrkontrollBuilder
            .medBehandlingId(behandlingId)
            .medSaksnummer(saksnummer)
            .medVenterKvittering(Boolean.TRUE)
            .medProsessTaskId(56L)
            .medSimulering(simulering)
            .build();
    }
}
