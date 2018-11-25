package no.nav.foreldrepenger.datavarehus.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.datavarehus.BehandlingDvh;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class BehandlingDvhMapperTest {

    private static final long VEDTAK_ID = 1L;
    private static final String BEHANDLENDE_ENHET = "behandlendeEnhet";
    private static final String ANSVARLIG_BESLUTTER = "ansvarligBeslutter";
    private static final String ANSVARLIG_SAKSBEHANDLER = "ansvarligSaksbehandler";
    private static final AktørId BRUKER_AKTØR_ID = new AktørId("10000000");
    private static final Saksnummer SAKSNUMMER  = new Saksnummer("12345");
    private static LocalDateTime OPPRETTET_TID = LocalDateTime.now();

    private BehandlingDvhMapper mapper = new BehandlingDvhMapper();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Test
    public void skal_mappe_til_behandling_dvh_uten_vedtak() {
        Behandling behandling = byggBehandling(BehandlingResultatType.IKKE_FASTSATT, false);

        BehandlingDvh dvh = mapper.map(behandling, Optional.empty());
        assertThat(dvh).isNotNull();
        assertThat(dvh.getAnsvarligBeslutter()).isEqualTo(ANSVARLIG_BESLUTTER);
        assertThat(dvh.getAnsvarligSaksbehandler()).isEqualTo(ANSVARLIG_SAKSBEHANDLER);
        assertThat(dvh.getBehandlendeEnhet()).isEqualTo(BEHANDLENDE_ENHET);
        assertThat(dvh.getBehandlingId()).isEqualTo(behandling.getId());
        assertThat(dvh.getBehandlingResultatType()).isEqualTo(BehandlingResultatType.IKKE_FASTSATT.getKode());
        assertThat(dvh.getBehandlingStatus()).isEqualTo(BehandlingStatus.OPPRETTET.getKode());
        assertThat(dvh.getBehandlingType()).isEqualTo(BehandlingType.FØRSTEGANGSSØKNAD.getKode());
        assertThat(dvh.getEndretAv()).isEqualTo("OpprettetAv");
        assertThat(dvh.getFagsakId()).isEqualTo(behandling.getFagsakId());
        assertThat(dvh.getFunksjonellTid()).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
        assertThat(dvh.getOpprettetDato()).isEqualTo(OPPRETTET_TID.toLocalDate());
        assertThat(dvh.getUtlandstilsnitt()).isEqualTo("NASJONAL");
        assertThat(dvh.getVedtakId()).isNull();
    }

    @Test
    public void skal_mappe_til_behandling_dvh_ikke_vedtatt() {
        Behandling behandling = byggBehandling(BehandlingResultatType.OPPHØR, false);

        BehandlingDvh dvh = mapper.map(behandling, Optional.empty());
        assertThat(dvh).isNotNull();
        assertThat(dvh.isVedtatt()).isFalse();
    }

    @Test
    public void skal_mappe_til_behandling_dvh_vedtatt() {
        Behandling behandling = byggBehandling(BehandlingResultatType.AVSLÅTT, true);

        BehandlingDvh dvh = mapper.map(behandling, Optional.empty());
        assertThat(dvh).isNotNull();
        assertThat(dvh.isVedtatt()).isTrue();
    }

    @Test
    public void skal_mappe_til_behandling_dvh_ferdig() {
        Behandling behandling = byggBehandling(BehandlingResultatType.AVSLÅTT, true);

        BehandlingDvh dvh = mapper.map(behandling, Optional.empty());
        assertThat(dvh).isNotNull();
        assertThat(dvh.isFerdig()).isTrue();
    }

    @Test
    public void skal_mappe_til_behandling_dvh_ikke_ferdig() {
        Behandling behandling = byggBehandling(BehandlingResultatType.AVSLÅTT, false);

        BehandlingDvh dvh = mapper.map(behandling, Optional.empty());
        assertThat(dvh).isNotNull();
        assertThat(dvh.isFerdig()).isFalse();
    }

    @Test
    public void skal_mappe_til_behandling_dvh_abrutt() {
        Behandling behandling = byggBehandling(BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET, true);

        BehandlingDvh dvh = mapper.map(behandling, Optional.empty());
        assertThat(dvh).isNotNull();
        assertThat(dvh.isAvbrutt()).isTrue();
    }

    @Test
    public void skal_mappe_til_behandling_dvh_ikke_abrutt() {
        Behandling behandling = byggBehandling(BehandlingResultatType.IKKE_FASTSATT, false);

        BehandlingDvh dvh = mapper.map(behandling, Optional.empty());
        assertThat(dvh).isNotNull();
        assertThat(dvh.isAvbrutt()).isFalse();
    }

    @Test
    public void skal_mappe_vedtak_id() {
        Behandling behandling = byggBehandling(BehandlingResultatType.INNVILGET, true);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.opprettFor(behandling);
        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder().medVedtakResultatType(VedtakResultatType.INNVILGET).medBehandlingsresultat(behandlingsresultat)
                .medVedtaksdato(LocalDate.now()).medAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER).build();
        Whitebox.setInternalState(behandlingVedtak, "id", VEDTAK_ID);

        BehandlingDvh dvh = mapper.map(behandling, Optional.of(behandlingVedtak));

        assertThat(dvh.getVedtakId()).isEqualTo(VEDTAK_ID);
    }

    private Behandling byggBehandling(BehandlingResultatType behandlingResultatType, boolean avsluttetFagsak) {
        ScenarioMorSøkerEngangsstønad morSøkerEngangsstønad = ScenarioMorSøkerEngangsstønad.forDefaultAktør()
                .medBruker(BRUKER_AKTØR_ID, NavBrukerKjønn.KVINNE)
                .medSaksnummer(SAKSNUMMER);
        Behandling behandling = morSøkerEngangsstønad.lagMocked();
        behandling.setAnsvarligBeslutter(ANSVARLIG_BESLUTTER);
        behandling.setAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER);
        behandling.setBehandlendeEnhet(new OrganisasjonsEnhet(BEHANDLENDE_ENHET, null));
        opprettBehandlingsresultat(behandling, behandlingResultatType);
        setFaksak(behandling, avsluttetFagsak);

        Whitebox.setInternalState(behandling, "opprettetAv", "OpprettetAv");
        Whitebox.setInternalState(behandling, "opprettetTidspunkt", OPPRETTET_TID);
        return behandling;
    }

    private void opprettBehandlingsresultat(Behandling behandling, BehandlingResultatType behandlingResultatType) {
        Behandlingsresultat.builder().medBehandlingResultatType(behandlingResultatType).buildFor(behandling);
    }

    private void setFaksak(Behandling behandling, boolean avsluttet) {
        if (avsluttet) {
            behandling.getFagsak().setAvsluttet();
        }
    }
}
