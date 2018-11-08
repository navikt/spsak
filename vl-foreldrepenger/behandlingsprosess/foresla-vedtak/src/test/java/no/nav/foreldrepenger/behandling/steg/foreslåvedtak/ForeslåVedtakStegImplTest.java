package no.nav.foreldrepenger.behandling.steg.foreslåvedtak;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Oppgaveinfo;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.Whitebox;

public class ForeslåVedtakStegImplTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private final EntityManager entityManager = repoRule.getEntityManager();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private BehandlingRepository behandlingRepository = spy(repositoryProvider.getBehandlingRepository());
    @Mock
    private OppgaveTjeneste oppgaveTjeneste;
    @Mock
    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;
    private HistorikkRepository historikkRepository = spy(repositoryProvider.getHistorikkRepository());
    @Mock
    private Behandling behandling;
    @Mock
    private BehandlingskontrollKontekst kontekst;
    private UttakRepository uttakRepository = repositoryProvider.getUttakRepository();

    private ForeslåVedtakStegImpl steg;

    private AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();

    private ArrayList<Oppgaveinfo> oppgaveinfoerSomReturneres = new ArrayList<>();

    @Before
    public void setUp() {
        behandling = ScenarioMorSøkerEngangsstønad.forFødsel().lagre(repositoryProvider);
        entityManager.persist(behandling.getBehandlingsresultat());
        Fagsak fagsak = behandling.getFagsak();
        kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), repositoryProvider.getBehandlingRepository().taSkriveLås(behandling));

        when(oppgaveTjeneste.hentOppgaveListe(any(AktørId.class), any())).thenReturn(oppgaveinfoerSomReturneres);
        when(dokumentBestillerApplikasjonTjeneste.erDokumentProdusert(anyLong(), anyString())).thenReturn(true);

        SjekkMotEksisterendeOppgaverTjeneste gsakTjeneste = new SjekkMotEksisterendeOppgaverTjeneste(historikkRepository, oppgaveTjeneste);
        ForeslåVedtakTjeneste foreslåVedtakTjeneste = new ForeslåVedtakTjenesteEngangsstønadImpl(repositoryProvider, gsakTjeneste);
        steg = new ForeslåVedtakStegImpl(repositoryProvider.getBehandlingRepository(), foreslåVedtakTjeneste);
    }

    @Test
    public void oppdatererBehandlingsresultatVedTotrinnskontroll() {
        Behandling behandling = ScenarioMorSøkerEngangsstønad.forFødsel().lagre(repositoryProvider);
        Fagsak fagsak = behandling.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling));
        behandling.setToTrinnsBehandling();

        steg.utførSteg(kontekst);
        assertThat(behandling.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(BehandlingResultatType.INNVILGET);
    }

    @Test
    public void oppretterAksjonspunktVedTotrinnskontrollOgSetterStegPåVent() {
        leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT, true);

        BehandleStegResultat stegResultat = steg.utførSteg(kontekst);

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(stegResultat.getAksjonspunktListe().size()).isEqualTo(1);
        assertThat(stegResultat.getAksjonspunktListe().get(0)).isEqualTo(AksjonspunktDefinisjon.FORESLÅ_VEDTAK);
    }


    @Test
    public void setterTotrinnskontrollPaBehandlingHvisIkkeSattFraFør() {
        leggTilAksjonspunkt(AksjonspunktDefinisjon.OVERSTYRING_AV_MEDLEMSKAPSVILKÅRET, false);

        steg.utførSteg(kontekst);
        assertThat(behandling.isToTrinnsBehandling()).isTrue();
    }


    @Test
    public void setterPåVentHvisÅpentAksjonspunktVedtakUtenTotrinnskontroll() {
        // Arrange
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VEDTAK_UTEN_TOTRINNSKONTROLL);

        // Act
        BehandleStegResultat stegResultat = steg.utførSteg(kontekst);

        // Verify
        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(stegResultat.getAksjonspunktListe().size()).isEqualTo(0);
    }

    @Test
    public void setterStegTilUtførtUtenAksjonspunktDersomIkkeTotorinnskontroll() {
        BehandleStegResultat stegResultat = steg.utførSteg(kontekst);

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(stegResultat.getAksjonspunktListe().size()).isEqualTo(0);
    }

    @Test
    public void setterIkkeTotrinnskontrollPaBehandlingHvisDetIkkeErTotrinnskontroll() {
        BehandleStegResultat stegResultat = steg.utførSteg(kontekst);

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(behandling.isToTrinnsBehandling()).isFalse();
        verify(behandlingRepository, never()).lagre(any(Behandling.class), any());
    }

    @Test
    public void lagerRiktigAksjonspunkterNårDetErOppgaveriGsak() {
        oppgaveinfoerSomReturneres.add(Oppgaveinfo.VURDER_KONST_YTELSE_FORELDREPENGER);
        oppgaveinfoerSomReturneres.add(Oppgaveinfo.VURDER_DOKUMENT);

        BehandleStegResultat stegResultat = steg.utførSteg(kontekst);

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        verify(historikkRepository, times(2)).lagre(any());
        assertThat(stegResultat.getAksjonspunktListe().contains(AksjonspunktDefinisjon.VURDERE_ANNEN_YTELSE_FØR_VEDTAK)).isTrue();
        assertThat(stegResultat.getAksjonspunktListe().contains(AksjonspunktDefinisjon.VURDERE_DOKUMENT_FØR_VEDTAK)).isTrue();
    }

    @Test
    public void lagerIkkeNyeAksjonspunkterNårAksjonspunkterAlleredeFinnes() {
        leggTilAksjonspunkt(AksjonspunktDefinisjon.VURDERE_ANNEN_YTELSE_FØR_VEDTAK, false);

        oppgaveinfoerSomReturneres.add(Oppgaveinfo.VURDER_KONST_YTELSE_FORELDREPENGER);
        oppgaveinfoerSomReturneres.add(Oppgaveinfo.VURDER_DOKUMENT);

        BehandleStegResultat stegResultat = steg.utførSteg(kontekst);

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        verify(historikkRepository, times(0)).lagre(any());
    }

    @Test
    public void utførerMedAksjonspunktForeslåVedtakManueltHvisRevurderingOgIkkeTotrinnskontroll() {
        Behandling behandling = ScenarioMorSøkerEngangsstønad.forFødsel().medBehandlingType(BehandlingType.REVURDERING).lagre(repositoryProvider);
        Fagsak fagsak = behandling.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling));

        BehandleStegResultat stegResultat = steg.utførSteg(kontekst);
        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(stegResultat.getAksjonspunktListe().size()).isEqualTo(1);
        assertThat(stegResultat.getAksjonspunktListe().get(0)).isEqualTo(AksjonspunktDefinisjon.FORESLÅ_VEDTAK_MANUELT);
    }

    @Test
    public void utførerMedAksjonspunktForeslåVedtakManueltHvisRevurderingOgIkkeTotrinnskontrollBehandling2TrinnIkkeReset() {
        Behandling behandling = ScenarioMorSøkerEngangsstønad.forFødsel().medBehandlingType(BehandlingType.REVURDERING).lagre(repositoryProvider);
        Fagsak fagsak = behandling.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling));
        behandling.setToTrinnsBehandling();

        BehandleStegResultat stegResultat = steg.utførSteg(kontekst);

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(stegResultat.getAksjonspunktListe().size()).isEqualTo(1);
        assertThat(stegResultat.getAksjonspunktListe().get(0)).isEqualTo(AksjonspunktDefinisjon.FORESLÅ_VEDTAK_MANUELT);
    }

    @Test
    public void oppretterAksjonspunktVedTotrinnskontrollForRevurdering() {
        behandling = ScenarioMorSøkerEngangsstønad.forFødsel().medBehandlingType(BehandlingType.REVURDERING).lagre(repositoryProvider);
        Fagsak fagsak = behandling.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling));
        leggTilAksjonspunkt(AksjonspunktDefinisjon.OVERSTYRING_AV_ADOPSJONSVILKÅRET, true);

        BehandleStegResultat stegResultat = steg.utførSteg(kontekst);

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(stegResultat.getAksjonspunktListe().size()).isEqualTo(1);
        assertThat(stegResultat.getAksjonspunktListe().get(0)).isEqualTo(AksjonspunktDefinisjon.FORESLÅ_VEDTAK);
    }

    @Test
    public void foreslårVedtakForForeldrepengerMedInnvilgetUttaksperiode() {
        SjekkMotEksisterendeOppgaverTjeneste gsakTjeneste = new SjekkMotEksisterendeOppgaverTjeneste(historikkRepository, oppgaveTjeneste);
        ForeslåVedtakTjeneste foreslåVedtakTjeneste = new ForeslåVedtakTjenesteForeldrepengerImpl(repositoryProvider, gsakTjeneste, dokumentBestillerApplikasjonTjeneste, null);
        ForeslåVedtakStegForeldrepengerImpl fpSteg = new ForeslåVedtakStegForeldrepengerImpl(behandlingRepository, foreslåVedtakTjeneste);
        UttakResultatPerioderEntitet uttak = opprettUttak(true);

        repositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(behandling, uttak);
        leggTilAksjonspunkt(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, true);

        BehandleStegResultat stegResultat = fpSteg.utførSteg(kontekst);

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(stegResultat.getAksjonspunktListe().size()).isEqualTo(1);
        assertThat(stegResultat.getAksjonspunktListe().get(0)).isEqualTo(AksjonspunktDefinisjon.FORESLÅ_VEDTAK);
        assertThat(behandling.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(BehandlingResultatType.INNVILGET);
    }

    private void leggTilAksjonspunkt(AksjonspunktDefinisjon aksjonspunktDefinisjon, boolean totrinnsbehandling) {
        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon);
        Whitebox.setInternalState(aksjonspunkt, "status", AksjonspunktStatus.UTFØRT);
        Whitebox.setInternalState(aksjonspunkt, "toTrinnsBehandling", totrinnsbehandling);
    }

    @Test
    public void foreslårVedtakForForeldrepengerMedAvslåttUttaksperiode() {

        SjekkMotEksisterendeOppgaverTjeneste gsakTjeneste = new SjekkMotEksisterendeOppgaverTjeneste(historikkRepository, oppgaveTjeneste);
        ForeslåVedtakTjeneste foreslåVedtakTjeneste = new ForeslåVedtakTjenesteForeldrepengerImpl(repositoryProvider, gsakTjeneste, dokumentBestillerApplikasjonTjeneste, null);
        ForeslåVedtakStegForeldrepengerImpl fpSteg = new ForeslåVedtakStegForeldrepengerImpl(behandlingRepository, foreslåVedtakTjeneste);
        entityManager.persist(behandling.getBehandlingsresultat());
        UttakResultatPerioderEntitet uttak = opprettUttak(false);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, uttak);

        leggTilAksjonspunkt(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, true);

        BehandleStegResultat stegResultat = fpSteg.utførSteg(kontekst);

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(stegResultat.getAksjonspunktListe().size()).isEqualTo(1);
        assertThat(stegResultat.getAksjonspunktListe().get(0)).isEqualTo(AksjonspunktDefinisjon.FORESLÅ_VEDTAK);
        assertThat(behandling.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(BehandlingResultatType.AVSLÅTT);
    }

    private UttakResultatPerioderEntitet opprettUttak(boolean innvilget) {
        UttakResultatEntitet.Builder uttakResultatBuilder = UttakResultatEntitet.builder(behandling);
        UttakResultatPeriodeEntitet uttakResultatPeriode = new UttakResultatPeriodeEntitet.Builder(LocalDate.now(),
            LocalDate.now().plusMonths(3))
            .medPeriodeResultat(innvilget ? PeriodeResultatType.INNVILGET : PeriodeResultatType.AVSLÅTT, PeriodeResultatÅrsak.UKJENT)
            .build();

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medOrgnr("000000000").oppdatertOpplysningerNå().build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("123"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = UttakResultatPeriodeAktivitetEntitet.builder(uttakResultatPeriode,
            uttakAktivitet)
            .medTrekkonto(StønadskontoType.FORELDREPENGER)
            .medTrekkdager(10)
            .medArbeidsprosent(BigDecimal.valueOf(100))
            .medUtbetalingsprosent(BigDecimal.valueOf(100))
            .build();

        uttakResultatPeriode.leggTilAktivitet(periodeAktivitet);

        UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
        uttakResultatPerioder.leggTilPeriode(uttakResultatPeriode);
        uttakResultatBuilder.medOpprinneligPerioder(uttakResultatPerioder)
            .build();
        return uttakResultatPerioder;
    }

}
