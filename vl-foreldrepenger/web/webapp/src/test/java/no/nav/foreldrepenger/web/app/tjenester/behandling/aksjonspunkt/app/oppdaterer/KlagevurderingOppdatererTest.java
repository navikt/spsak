package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioKlageEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;
import no.nav.foreldrepenger.domene.dokument.impl.DokumentBestillerTjenesteImpl;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.KlageVurderingResultatAksjonspunktDto.KlageVurderingResultatNfpAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.KlageVurderingResultatAksjonspunktDto.KlageVurderingResultatNkAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class KlagevurderingOppdatererTest {

    private HistorikkTjenesteAdapter historikkApplikasjonTjeneste;
    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;

    // TODO (FLUORITT): Renskriv tester med færre mocks
    @Before
    public void oppsett() {
        historikkApplikasjonTjeneste = mock(HistorikkTjenesteAdapter.class);
        dokumentBestillerApplikasjonTjeneste = mock(DokumentBestillerApplikasjonTjeneste.class);
        behandlingsutredningApplikasjonTjeneste = mock(BehandlingsutredningApplikasjonTjeneste.class);
    }

    @Test
    public void skalBestilleDokumentVedStadfestetYtelsesvedtakOgLagreKlageVurderingResultat() {
        // Arrange
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forFødsel();

        ScenarioKlageEngangsstønad klageScenario = ScenarioKlageEngangsstønad.forUtenVurderingResultat(scenario);
        Behandling klageBehandling = klageScenario.lagMocked();

        KlageVurdering klageVurdering = KlageVurdering.STADFESTE_YTELSESVEDTAK;
        KlageVurderingResultatNfpAksjonspunktDto dto = new KlageVurderingResultatNfpAksjonspunktDto("begrunnelse bla. bla.",
                klageVurdering, null, null, LocalDate.now());

        BehandlingRepository behandlingRepository = scenario.mockBehandlingRepository();

        // Act
        getKlageVurderer(scenario).oppdater(dto, klageBehandling, null);

        // Assert
        // verifiserer KlageVurderingResultat
        ArgumentCaptor<KlageVurderingResultat> klvResultatCaptor = ArgumentCaptor.forClass(KlageVurderingResultat.class);
        verify(behandlingRepository, times(1)).lagre(klvResultatCaptor.capture(), any(BehandlingLås.class));
        KlageVurderingResultat klageVurderingResultat = klvResultatCaptor.getValue();
        assertThat(klageVurderingResultat.getKlageVurdering()).isEqualTo(KlageVurdering.STADFESTE_YTELSESVEDTAK);
        assertThat(klageVurderingResultat.getKlageVurdertAv()).isEqualTo(KlageVurdertAv.NFP);
        assertThat(klageVurderingResultat.getBehandling()).isEqualTo(klageBehandling);
        assertThat(klageBehandling.hentKlageVurderingResultat(KlageVurdertAv.NFP)).isEqualTo(Optional.of(klageVurderingResultat));

        // verifiserer BestillBrevDto
        ArgumentCaptor<BestillBrevDto> brevDtoCaptor = ArgumentCaptor.forClass(BestillBrevDto.class);
        verify(dokumentBestillerApplikasjonTjeneste).bestillDokument(brevDtoCaptor.capture(), eq(HistorikkAktør.SAKSBEHANDLER));
        BestillBrevDto bestillBrevDto = brevDtoCaptor.getValue();
        assertThat(bestillBrevDto.getBrevmalkode()).isEqualTo(DokumentMalType.KLAGE_OVERSENDT_KLAGEINSTANS_DOK);
        assertThat(bestillBrevDto.getFritekst()).isNull();

        // Verifiserer HistorikkinnslagDto
        ArgumentCaptor<Historikkinnslag> historikkCapture = ArgumentCaptor.forClass(Historikkinnslag.class);
        verify(historikkApplikasjonTjeneste).lagInnslag(historikkCapture.capture());
        Historikkinnslag historikkinnslag = historikkCapture.getValue();
        assertThat(historikkinnslag.getType()).isEqualTo(HistorikkinnslagType.KLAGE_BEH_NFP);
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        HistorikkinnslagDel del = historikkinnslag.getHistorikkinnslagDeler().get(0);
        assertThat(del.getSkjermlenke()).as("skjermlenke").hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.KLAGE_BEH_NFP.getKode()));
        assertThat(del.getHendelse()).as("hendelse").hasValueSatisfying(hendelse -> assertThat(hendelse.getNavn()).as("navn").isEqualTo(HistorikkinnslagType.KLAGE_BEH_NFP.getKode()));
        assertThat(del.getResultat()).as("resultat").hasValueSatisfying(resultat -> assertThat(resultat).isEqualTo(HistorikkResultatType.OPPRETTHOLDT_VEDTAK.getKode()));

        // Verifiserer at behandlende enhet er byttet til NAV Klageinstans
        ArgumentCaptor<OrganisasjonsEnhet> enhetCapture = ArgumentCaptor.forClass(OrganisasjonsEnhet.class);
        verify(behandlingsutredningApplikasjonTjeneste).byttBehandlendeEnhet(anyLong(), enhetCapture.capture(), eq(""), eq(HistorikkAktør.VEDTAKSLØSNINGEN));
        OrganisasjonsEnhet enhet = enhetCapture.getValue();
        assertThat(enhet.getEnhetId()).isEqualTo("4205");
        assertThat(enhet.getEnhetNavn()).isEqualTo("NAV Klageinstans Midt-Norge");
        assertThat(klageBehandling.getBehandlingsresultat().getBehandlingResultatType())
                .isEqualTo(BehandlingResultatType.KLAGE_YTELSESVEDTAK_STADFESTET);
    }

    private KlagevurderingOppdaterer getKlageVurderer(AbstractTestScenario<?> scenario) {
        final BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        final DokumentBestillerTjenesteImpl dokumentTjeneste = new DokumentBestillerTjenesteImpl(Period.parse("P4W"), repositoryProvider, mock(OppgaveTjeneste.class),
                mock(OppgaveBehandlingKoblingRepository.class), mock(BehandlingskontrollTjeneste.class),
                dokumentBestillerApplikasjonTjeneste);
        return new KlagevurderingOppdaterer(repositoryProvider,
                historikkApplikasjonTjeneste, dokumentTjeneste, behandlingsutredningApplikasjonTjeneste);
    }

    @Test
    public void skalSetteBehandlingResultatTypeAvvisKlageForNkNårKlagetErForSent() {
        // Arrange
        KlageVurdering klageVurdering = KlageVurdering.AVVIS_KLAGE;
        KlageAvvistÅrsak klageAvvistÅrsak = KlageAvvistÅrsak.KLAGET_FOR_SENT;
        KlageVurderingResultatNkAksjonspunktDto dto = new KlageVurderingResultatNkAksjonspunktDto("begrunnelse for avvist klage NK...",
                klageVurdering, null, klageAvvistÅrsak, LocalDate.now());
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        Behandling klageBehandling = ScenarioKlageEngangsstønad.forUtenVurderingResultat(scenario).lagMocked();

        BehandlingRepository behandlingRepository = scenario.mockBehandlingRepository();

        // Act
        getKlageVurderer(scenario).oppdater(dto, klageBehandling, null);

        // Assert
        assertThat(klageBehandling.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(BehandlingResultatType.KLAGE_AVVIST);

        // verifiserer KlageVurderingResultat
        ArgumentCaptor<KlageVurderingResultat> klvResultatCaptor = ArgumentCaptor.forClass(KlageVurderingResultat.class);
        verify(behandlingRepository, times(1)).lagre(klvResultatCaptor.capture(), any(BehandlingLås.class));
        KlageVurderingResultat klageVurderingResultat = klvResultatCaptor.getValue();
        assertThat(klageVurderingResultat.getKlageVurdering()).isEqualTo(KlageVurdering.AVVIS_KLAGE);
        assertThat(klageVurderingResultat.getKlageAvvistÅrsak()).isEqualTo(KlageAvvistÅrsak.KLAGET_FOR_SENT);
        assertThat(klageVurderingResultat.getKlageVurdertAv()).isEqualTo(KlageVurdertAv.NK);
        assertThat(klageVurderingResultat.getBehandling()).isEqualTo(klageBehandling);
        assertThat(klageBehandling.hentKlageVurderingResultat(KlageVurdertAv.NK)).isEqualTo(Optional.of(klageVurderingResultat));

        // Verifiserer HistorikkinnslagDto
        ArgumentCaptor<Historikkinnslag> historikkCapture = ArgumentCaptor.forClass(Historikkinnslag.class);
        verify(historikkApplikasjonTjeneste).lagInnslag(historikkCapture.capture());
        Historikkinnslag historikkinnslag = historikkCapture.getValue();
        assertThat(historikkinnslag.getType()).isEqualTo(HistorikkinnslagType.KLAGE_BEH_NK);
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        HistorikkinnslagDel del = historikkinnslag.getHistorikkinnslagDeler().get(0);
        assertThat(del.getSkjermlenke()).as("skjermlenke").hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.KLAGE_BEH_NK.getKode()));
        assertThat(del.getHendelse()).as("hendelse").hasValueSatisfying(hendelse -> assertThat(hendelse.getNavn()).as("navn").isEqualTo(HistorikkinnslagType.KLAGE_BEH_NK.getKode()));
        assertThat(del.getResultat()).as("resultat").hasValueSatisfying(resultat -> assertThat(resultat).isEqualTo(HistorikkResultatType.AVVIS_KLAGE.getKode()));
    }

}
