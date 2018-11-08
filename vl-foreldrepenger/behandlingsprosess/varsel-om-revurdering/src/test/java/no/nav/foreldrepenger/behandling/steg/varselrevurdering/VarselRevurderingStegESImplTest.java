package no.nav.foreldrepenger.behandling.steg.varselrevurdering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class VarselRevurderingStegESImplTest {

    private static final String FRIST_PERIODE = "P3W";

    private BehandlingRepository behandlingRepository;
    private OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository;
    private VilkårKodeverkRepository vilkårKodeverkRepository;
    private OppgaveTjeneste oppgaveTjeneste;
    private BehandlingskontrollKontekst kontekst;
    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;

    private Long behandlingId = 1234L;
    private Behandling.Builder behandlingBuilder;
    private VarselRevurderingStegESImpl steg;
    private static final LocalDate BEHANDLINGSTID_FRIST = LocalDate.now().plusWeeks(6);

    @Before
    public void setup() {
        Fagsak fagsak = FagsakBuilder.nyEngangstønadForMor().build();
        behandlingBuilder = Behandling.nyBehandlingFor(fagsak, BehandlingType.REVURDERING).medBehandlingstidFrist(BEHANDLINGSTID_FRIST);

        behandlingRepository = mock(BehandlingRepository.class);
        oppgaveBehandlingKoblingRepository = mock(OppgaveBehandlingKoblingRepository.class);
        vilkårKodeverkRepository = mock(VilkårKodeverkRepository.class);
        oppgaveTjeneste = mock(OppgaveTjeneste.class);
        dokumentBestillerApplikasjonTjeneste = mock(DokumentBestillerApplikasjonTjeneste.class);
        BehandlingskontrollTjeneste behandlingskontrollTjeneste = mock(BehandlingskontrollTjeneste.class);
        AksjonspunktRepositoryImpl aksjonspunktRepositoryImpl = new AksjonspunktRepositoryImpl(null);
        when(behandlingskontrollTjeneste.settBehandlingPåVent(any(), any(), any(), any(), any()))
            .thenAnswer(invocation -> {
                Behandling beh = invocation.getArgument(0);
                AksjonspunktDefinisjon aksjonspunktDefinisjon = Mockito.mock(AksjonspunktDefinisjon.class);
                when(aksjonspunktDefinisjon.getAksjonspunktType()).thenReturn(AksjonspunktType.AUTOPUNKT);
                Aksjonspunkt aksjonspunkt = aksjonspunktRepositoryImpl.leggTilAksjonspunkt(beh, aksjonspunktDefinisjon);
                aksjonspunktRepositoryImpl.setFrist(aksjonspunkt, LocalDateTime.now().plus(Period.parse(FRIST_PERIODE)), null);
                return aksjonspunkt;
            });
        steg = new VarselRevurderingStegESImpl(behandlingRepository,
            oppgaveBehandlingKoblingRepository, oppgaveTjeneste, dokumentBestillerApplikasjonTjeneste, behandlingskontrollTjeneste);

        AksjonspunktDefinisjon ap = Mockito.mock(AksjonspunktDefinisjon.class);
        when(ap.getFristPeriode()).thenReturn(FRIST_PERIODE);
        when(ap.getAksjonspunktType()).thenReturn(AksjonspunktType.AUTOPUNKT);
        when(vilkårKodeverkRepository
            .finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.AUTO_SATT_PÅ_VENT_REVURDERING.getKode())).thenReturn(ap);

        kontekst = mock(BehandlingskontrollKontekst.class);
        when(kontekst.getBehandlingId()).thenReturn(behandlingId);
    }

    @Test
    public void utførerMedAksjonspunktVedAvvikIAntallBarn() {
        Behandling behandling = behandlingBuilder.medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_AVVIK_ANTALL_BARN)).build();
        Whitebox.setInternalState(behandling, "id", behandlingId);
        when(behandlingRepository.hentBehandling(behandlingId)).thenReturn(behandling);

        BehandleStegResultat behandleStegResultat = steg.utførSteg(kontekst);
        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        List<AksjonspunktDefinisjon> aksjonspunkter = behandleStegResultat.getAksjonspunktListe();
        assertThat(aksjonspunkter.size()).isEqualTo(1);
        assertThat(aksjonspunkter.get(0)).isEqualTo(AksjonspunktDefinisjon.VARSEL_REVURDERING_ETTERKONTROLL);
    }

    @Test
    public void utførerMedAksjonspunktVedVedtakMellomUke26Og29() {
        Behandling behandling = behandlingBuilder.medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_MANGLER_FØDSEL_I_PERIODE)).build();
        Whitebox.setInternalState(behandling, "id", behandlingId);
        when(behandlingRepository.hentBehandling(behandlingId)).thenReturn(behandling);

        BehandleStegResultat behandleStegResultat = steg.utførSteg(kontekst);
        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        List<AksjonspunktDefinisjon> aksjonspunkter = behandleStegResultat.getAksjonspunktListe();
        assertThat(aksjonspunkter.size()).isEqualTo(1);
        assertThat(aksjonspunkter.get(0)).isEqualTo(AksjonspunktDefinisjon.VARSEL_REVURDERING_ETTERKONTROLL);
    }

    @Test
    public void varslerAutomatiskOgSetterBehandlingPåVentNårIngenBarnITps() {
        Behandling behandling = behandlingBuilder.medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_MANGLER_FØDSEL)).build();
        String oppgaveId = "123";
        List<OppgaveBehandlingKobling> oppgaver = Collections.singletonList(new OppgaveBehandlingKobling(OppgaveÅrsak.REVURDER, oppgaveId, new Saksnummer(oppgaveId), behandling));
        Whitebox.setInternalState(behandling, "id", behandlingId);
        when(behandlingRepository.hentBehandling(behandlingId)).thenReturn(behandling);
        when(oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandling.getId())).thenReturn(oppgaver);

        BehandleStegResultat behandleStegResultat = steg.utførSteg(kontekst);

        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);

        // Behandling skal være på vent med frist 3 uker
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        LocalDate forventetFrist = LocalDate.now().plusWeeks(3);
        assertThat(behandling.getFristDatoBehandlingPåVent()).isEqualTo(forventetFrist);

        // Oppgave skal være avsluttet
        verify(oppgaveTjeneste).avslutt(anyLong(), any(OppgaveÅrsak.class));

        // Brev skal være bestilt
        ArgumentCaptor<BestillBrevDto> bestillBrevDtoArgumentCaptor = ArgumentCaptor.forClass(BestillBrevDto.class);
        verify(dokumentBestillerApplikasjonTjeneste).bestillDokument(bestillBrevDtoArgumentCaptor.capture(), eq(HistorikkAktør.VEDTAKSLØSNINGEN));
        BestillBrevDto bestillBrevDto = bestillBrevDtoArgumentCaptor.getValue();
        assertThat(bestillBrevDto.getBrevmalkode()).isEqualTo(DokumentMalType.REVURDERING_DOK);
    }

    @Test
    public void utførerMedAksjonspunktVedManueltOpprettetRevurdering() {
        Behandling behandling = behandlingBuilder.medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_FEIL_I_LOVANDVENDELSE)).build();
        Whitebox.setInternalState(behandling, "id", behandlingId);
        when(behandlingRepository.hentBehandling(behandlingId)).thenReturn(behandling);

        BehandleStegResultat behandleStegResultat = steg.utførSteg(kontekst);
        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        List<AksjonspunktDefinisjon> aksjonspunkter = behandleStegResultat.getAksjonspunktListe();
        assertThat(aksjonspunkter.size()).isEqualTo(1);
        assertThat(aksjonspunkter.get(0)).isEqualTo(AksjonspunktDefinisjon.VARSEL_REVURDERING_MANUELL);
    }
}
