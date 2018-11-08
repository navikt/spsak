package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.AvsluttetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.OpprettetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.impl.SakOgBehandlingAdapterMQImpl;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.BehandlingAvsluttet;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.BehandlingOpprettet;
import no.nav.vedtak.felles.integrasjon.sakogbehandling.SakOgBehandlingClient;

public class SakOgBehandlingAdapterMQImplTest {

    private static final String AKTØR_ID = "12345678";
    private static final String BEHANDLINGS_TYPE_KOD = "SØK";
    private static final String SAKSTEMA_KODE = "FOR";
    private static final String BTKODE = "BTKode";
    private static final String BEHANDLINGS_ID = "123";
    private static final String APP_NAME = Fagsystem.FPSAK.getOffisiellKode();

    @Mock
    private SakOgBehandlingClient sakOgBehandlingClient;

    private SakOgBehandlingAdapterMQImpl sakOgBehandlingAdapterMQ;

    @Before
    public void setUp(){
        sakOgBehandlingClient=mock(SakOgBehandlingClient.class);
        sakOgBehandlingAdapterMQ=new SakOgBehandlingAdapterMQImpl(sakOgBehandlingClient);
    }

    @Test
    public void test_informerOmNySak() {
        OpprettetBehandlingStatus nySakData = new OpprettetBehandlingStatus();
        LocalDate hendelsesTidspunkt = LocalDate.now();

        nySakData.setHendelsesTidspunkt(hendelsesTidspunkt);
        nySakData.setBehandlingsId(BEHANDLINGS_ID);

        nySakData.setBehandlingsTemaKode(BTKODE);
        nySakData.setBehandlingsTypeKode(BEHANDLINGS_TYPE_KOD);
        nySakData.setSakstemaKode(SAKSTEMA_KODE);

        nySakData.setAktørId(AKTØR_ID);

        ArgumentCaptor<BehandlingOpprettet> captor = ArgumentCaptor.forClass(BehandlingOpprettet.class);

        sakOgBehandlingAdapterMQ.behandlingOpprettet(nySakData);

        verify(sakOgBehandlingClient).sendBehandlingOpprettet(captor.capture());
        BehandlingOpprettet behandlingOpprettet = captor.getValue();

        assertThat(behandlingOpprettet.getAktoerREF().get(0).getAktoerId()).isEqualTo(String.valueOf(AKTØR_ID));
        assertThat(behandlingOpprettet.getBehandlingstema().getValue()).isEqualTo(BTKODE);
        assertThat(behandlingOpprettet.getBehandlingstype().getValue()).isEqualTo(BEHANDLINGS_TYPE_KOD);
        assertThat(behandlingOpprettet.getSakstema().getValue()).isEqualTo(SAKSTEMA_KODE);
        assertThat(behandlingOpprettet.getHendelsesprodusentREF().getValue()).isEqualTo(Fagsystem.FPSAK.getOffisiellKode());

        assertThat(behandlingOpprettet.getHendelsesTidspunkt().getDay()).isEqualTo(hendelsesTidspunkt.getDayOfMonth());
        assertThat(behandlingOpprettet.getHendelsesTidspunkt().getMonth()).isEqualTo(hendelsesTidspunkt.getMonth().getValue());

    }

    @Test
    public void test_informerOmAvsluttetSak() {
        AvsluttetBehandlingStatus avsluttetSakData = new AvsluttetBehandlingStatus();
        avsluttetSakData.setAvslutningsStatus("ok");
        avsluttetSakData.setAktørId(AKTØR_ID);
        avsluttetSakData.setAnsvarligEnhetRef("minEnhet");
        avsluttetSakData.setBehandlingsId(BEHANDLINGS_ID);
        avsluttetSakData.setBehandlingsTypeKode(BEHANDLINGS_TYPE_KOD);
        avsluttetSakData.setSakstemaKode(SAKSTEMA_KODE);

        ArgumentCaptor<BehandlingAvsluttet> captor = ArgumentCaptor.forClass(BehandlingAvsluttet.class);

        sakOgBehandlingAdapterMQ.behandlingAvsluttet(avsluttetSakData);

        verify(sakOgBehandlingClient).sendBehandlingAvsluttet(captor.capture());
        BehandlingAvsluttet behandlingAvsluttet = captor.getValue();

        assertThat(behandlingAvsluttet.getAvslutningsstatus().getValue()).isEqualTo(avsluttetSakData.getAvslutningsStatus());
        assertThat(behandlingAvsluttet.getAktoerREF().get(0).getAktoerId()).isEqualTo(avsluttetSakData.getAktørId());
        assertThat(behandlingAvsluttet.getAnsvarligEnhetREF()).isEqualTo(avsluttetSakData.getAnsvarligEnhetRef());
        assertThat(behandlingAvsluttet.getBehandlingsID()).isEqualTo(APP_NAME + "_" + avsluttetSakData.getBehandlingsId());
        assertThat(behandlingAvsluttet.getBehandlingstype().getValue()).isEqualTo(avsluttetSakData.getBehandlingsTypeKode());
        assertThat(behandlingAvsluttet.getSakstema().getValue()).isEqualTo(avsluttetSakData.getSakstemaKode());
    }
}
