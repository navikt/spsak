package no.nav.foreldrepenger.behandling.steg.vedtak;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FORESLÅ_VEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.es.impl.RevurderingESTjenesteImpl;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingEndring;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingEndringES;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingVedtakEventPubliserer;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetsjekkerProvider;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.vedtak.VedtakTjeneste;
import no.nav.foreldrepenger.domene.vedtak.impl.VedtakTjenesteImpl;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlTjenesteEngangsstønad;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.BehandlingsresultatXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.BehandlingsresultatXmlTjenesteEngangsstønad;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.BeregningsresultatXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.BeregningsresultatXmlTjenesteEngangstønad;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.beregningsgrunnlag.BeregningsgrunnlagXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.beregningsgrunnlag.BeregningsgrunnlagXmlTjenesteEngangsstønad;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.ytelse.YtelseXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.ytelse.YtelseXmlTjenesteEngangsstønad;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.vilkår.VilkårsgrunnlagXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.vilkår.VilkårsgrunnlagXmlTjenesteEngangsstønad;
import no.nav.foreldrepenger.vedtak.xml.personopplysninger.PersonopplysningXmlTjenesteEngangsstønad;
import no.nav.foreldrepenger.vedtakslager.LagretVedtakRepository;
import no.nav.foreldrepenger.vedtakslager.LagretVedtakRepositoryImpl;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class FatteVedtakStegESImplTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final Repository repository = repoRule.getRepository();

    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private BeregningRepository beregningRepository = new BeregningRepositoryImpl(repoRule.getEntityManager());
    private final AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    private final BehandlingVedtakRepository behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
    private final InternalManipulerBehandling manipulerBehandling = new InternalManipulerBehandlingImpl(repositoryProvider);
    private FatteVedtakStegESImpl fatteVedtakSteg;

    @Mock
    private FamilieHendelseTjeneste familieHendelseTjeneste;

    private BehandlingVedtakTjeneste behandlingVedtakTjeneste;

    private static final String BEHANDLENDE_ENHET = "Stord";
    private KompletthetsjekkerProvider kompletthetssjekkerProvider = mock(KompletthetsjekkerProvider.class);

    private RevurderingEndring revurderingEndring = new RevurderingEndringES();

    @Before
    public void oppsett() {
        // TODO (TOPAS): Fin blanding av entityManager og mocks her.... kan antagelig erstatte de som bruker entitymanager med de som lages fra AbstractTestScenario?
        LagretVedtakRepository vedtakRepository = new LagretVedtakRepositoryImpl(repoRule.getEntityManager());
        HistorikkRepository historikkRepository = new HistorikkRepositoryImpl(repoRule.getEntityManager());
        RevurderingTjeneste revurderingESTjeneste = new RevurderingESTjenesteImpl(repositoryProvider, null, historikkRepository, revurderingEndring);

        OppgaveTjeneste oppgaveTjeneste = mock(OppgaveTjeneste.class);
        SøknadRepository søknadRepository = mock(SøknadRepository.class);
        TpsTjeneste tpsTjeneste = Mockito.mock(TpsTjeneste.class);
        FamilieHendelseRepository familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        PersonopplysningTjeneste personopplysningTjeneste = Mockito.mock(PersonopplysningTjeneste.class);
        PersonopplysningXmlTjenesteEngangsstønad personopplysningXmlTjeneste = new PersonopplysningXmlTjenesteEngangsstønad(tpsTjeneste, repositoryProvider, personopplysningTjeneste);
        VilkårsgrunnlagXmlTjeneste vilkårsgrunnlagXmlTjeneste = new VilkårsgrunnlagXmlTjenesteEngangsstønad(søknadRepository, familieHendelseRepository, kompletthetssjekkerProvider);
        YtelseXmlTjeneste ytelseXmlTjeneste = new YtelseXmlTjenesteEngangsstønad(repositoryProvider);
        BeregningsgrunnlagXmlTjeneste beregningsgrunnlagXmlTjeneste = new BeregningsgrunnlagXmlTjenesteEngangsstønad(repositoryProvider);
        BeregningsresultatXmlTjeneste beregningsresultatXmlTjeneste = new BeregningsresultatXmlTjenesteEngangstønad(beregningsgrunnlagXmlTjeneste, ytelseXmlTjeneste);
        BehandlingsresultatXmlTjeneste behandlingsresultatXmlTjeneste = new BehandlingsresultatXmlTjenesteEngangsstønad(beregningsresultatXmlTjeneste, vilkårsgrunnlagXmlTjeneste);
        TotrinnTjeneste totrinnTjeneste = mock(TotrinnTjeneste.class);

        Søknad søknad = new SøknadEntitet.Builder().medMottattDato(LocalDate.now()).medSøknadsdato(LocalDate.now()).build();
        when(søknadRepository.hentSøknadHvisEksisterer(any(Behandling.class))).thenReturn(Optional.ofNullable(søknad));

        VedtakXmlTjeneste vedtakXmlTjeneste = new VedtakXmlTjenesteEngangsstønad(repositoryProvider, personopplysningXmlTjeneste, behandlingsresultatXmlTjeneste);
        RevurderingTjenesteProvider revurderingTjenesteProvider = new RevurderingTjenesteProvider();
        VedtakTjeneste vedtakTjeneste = new VedtakTjenesteImpl(null, historikkRepository, revurderingTjenesteProvider, familieHendelseTjeneste, mock(TotrinnTjeneste.class));

        BehandlingVedtakEventPubliserer behandlingVedtakEventPubliserer = mock(BehandlingVedtakEventPubliserer.class);
        behandlingVedtakTjeneste = new BehandlingVedtakTjenesteImpl(revurderingTjenesteProvider, behandlingVedtakEventPubliserer, repositoryProvider);
        FatteVedtakTjenesteEngangsstønadImpl fvtei = new FatteVedtakTjenesteEngangsstønadImpl(vedtakRepository, vedtakXmlTjeneste, vedtakTjeneste,
            revurderingESTjeneste, oppgaveTjeneste, totrinnTjeneste, behandlingVedtakTjeneste);
        fatteVedtakSteg = new FatteVedtakStegESImpl(repositoryProvider, fvtei);
    }

    @Test(expected = TekniskException.class)
    public void skal_feile_hvis_behandling_i_feil_tilstand() {
        // Arrange
        int antallBarn = 1;
        BehandlingskontrollKontekst kontekst = byggBehandlingsgrunnlagForFødsel(antallBarn, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        oppdaterMedBehandlingsresultat(kontekst, true, antallBarn);

        // Act
        fatteVedtakSteg.utførSteg(kontekst);
    }

    @Test
    public void skal_fatte_positivt_vedtak() {
        // Arrange
        int antallBarn = 2;
        BehandlingskontrollKontekst kontekst = byggBehandlingsgrunnlagForFødsel(antallBarn, BehandlingStegType.FATTE_VEDTAK);
        oppdaterMedBehandlingsresultat(kontekst, true, antallBarn);

        // Act
        fatteVedtakSteg.utførSteg(kontekst);

        // Assert
        Optional<BehandlingVedtak> behandlingVedtakOpt = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(kontekst.getBehandlingId());
        assertThat(behandlingVedtakOpt).isPresent();
        BehandlingVedtak behandlingVedtak = behandlingVedtakOpt.get();
        assertThat(behandlingVedtak).isNotNull();
        assertThat(behandlingVedtak.getVedtakResultatType()).isEqualTo(VedtakResultatType.INNVILGET);
    }

    @Test
    public void revurdering_med_endret_utfall_skal_ha_nytt_vedtak() {
        // Opprinnelig behandling med vedtak
        int antallBarn = 1;
        BehandlingskontrollKontekst kontekst = byggBehandlingsgrunnlagForFødsel(antallBarn, BehandlingStegType.FATTE_VEDTAK);
        oppdaterMedBehandlingsresultat(kontekst, true, antallBarn);
        oppdaterMedVedtak(kontekst);
        Behandling originalBehandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        Behandling revurdering = Behandling.fraTidligereBehandling(originalBehandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_MANGLER_FØDSEL).medOriginalBehandling(originalBehandling)).build();

        manipulerBehandling.forceOppdaterBehandlingSteg(revurdering, BehandlingStegType.FATTE_VEDTAK);
        BehandlingLås behandlingLås = lagreBehandling(revurdering);
        opprettFamilieHendelseGrunnlag(originalBehandling, revurdering);
        Fagsak fagsak = revurdering.getFagsak();
        BehandlingskontrollKontekst revurderingKontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingLås);
        oppdaterMedBehandlingsresultat(revurderingKontekst, false, antallBarn);

        fatteVedtakSteg.utførSteg(revurderingKontekst);
        Optional<BehandlingVedtak> behandlingVedtakOpt = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(revurderingKontekst.getBehandlingId());
        assertThat(behandlingVedtakOpt).isPresent();
        BehandlingVedtak behandlingVedtak = behandlingVedtakOpt.get();
        assertThat(behandlingVedtak).isNotNull();
        assertThat(behandlingVedtak.getVedtakResultatType()).isEqualTo(VedtakResultatType.AVSLAG);
        assertThat(behandlingVedtak.isBeslutningsvedtak()).isFalse();
    }

    @Test
    public void revurdering_med_endret_antall_barn_skal_ha_nytt_vedtak() {
        int originalAntallBarn = 1;
        int faktiskAntallBarn = 2;
        BehandlingskontrollKontekst kontekst = byggBehandlingsgrunnlagForFødsel(originalAntallBarn, BehandlingStegType.FATTE_VEDTAK);
        oppdaterMedBehandlingsresultat(kontekst, true, originalAntallBarn);
        oppdaterMedVedtak(kontekst);
        Behandling originalBehandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        Behandling revurdering = Behandling.fraTidligereBehandling(originalBehandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_MANGLER_FØDSEL).medOriginalBehandling(originalBehandling)).build();

        manipulerBehandling.forceOppdaterBehandlingSteg(revurdering, BehandlingStegType.FATTE_VEDTAK);
        BehandlingLås behandlingLås = lagreBehandling(revurdering);
        opprettFamilieHendelseGrunnlag(originalBehandling, revurdering);
        Fagsak fagsak = revurdering.getFagsak();
        BehandlingskontrollKontekst revurderingKontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingLås);
        oppdaterMedBehandlingsresultat(revurderingKontekst, true, faktiskAntallBarn);

        fatteVedtakSteg.utførSteg(revurderingKontekst);
        Optional<BehandlingVedtak> behandlingVedtakOpt = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(revurderingKontekst.getBehandlingId());
        assertThat(behandlingVedtakOpt).isPresent();
        BehandlingVedtak behandlingVedtak = behandlingVedtakOpt.get();
        assertThat(behandlingVedtak).isNotNull();
        assertThat(behandlingVedtak.getVedtakResultatType()).isEqualTo(VedtakResultatType.INNVILGET);
        assertThat(behandlingVedtak.isBeslutningsvedtak()).isFalse();
    }


    @Test
    public void revurdering_med_samme_utfall_innvilget_skal_ha_beslutning() {
        int antallBarn = 1;
        BehandlingskontrollKontekst kontekst = byggBehandlingsgrunnlagForFødsel(antallBarn, BehandlingStegType.FATTE_VEDTAK);
        oppdaterMedBehandlingsresultat(kontekst, true, antallBarn);
        oppdaterMedVedtak(kontekst);
        Behandling originalBehandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        Behandling revurdering = Behandling.fraTidligereBehandling(originalBehandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_MANGLER_FØDSEL).medOriginalBehandling(originalBehandling)).build();

        manipulerBehandling.forceOppdaterBehandlingSteg(revurdering, BehandlingStegType.FATTE_VEDTAK);

        BehandlingLås behandlingLås = lagreBehandling(revurdering);

        opprettFamilieHendelseGrunnlag(originalBehandling, revurdering);
        Fagsak fagsak = revurdering.getFagsak();
        BehandlingskontrollKontekst revurderingKontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingLås);
        oppdaterMedBehandlingsresultat(revurderingKontekst, true, antallBarn);

        fatteVedtakSteg.utførSteg(revurderingKontekst);
        Optional<BehandlingVedtak> behandlingVedtakOpt = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(revurderingKontekst.getBehandlingId());
        assertThat(behandlingVedtakOpt).isPresent();
        BehandlingVedtak behandlingVedtak = behandlingVedtakOpt.get();
        assertThat(behandlingVedtak).isNotNull();
        assertThat(behandlingVedtak.getVedtakResultatType()).isEqualTo(VedtakResultatType.INNVILGET);
        assertThat(behandlingVedtak.isBeslutningsvedtak()).isTrue();
    }

    private void opprettFamilieHendelseGrunnlag(Behandling originalBehandling, Behandling revurdering) {
        repositoryProvider.getFamilieGrunnlagRepository().kopierGrunnlagFraEksisterendeBehandling(originalBehandling, revurdering);
    }

    private BehandlingLås lagreBehandling(Behandling behandling) {
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        return behandlingLås;
    }


    @Test
    public void revurdering_med_samme_utfall_avslag_skal_ha_beslutning() {
        int antallBarn = 1;
        BehandlingskontrollKontekst kontekst = byggBehandlingsgrunnlagForFødsel(antallBarn, BehandlingStegType.FATTE_VEDTAK);
        oppdaterMedBehandlingsresultat(kontekst, false, antallBarn);
        oppdaterMedVedtak(kontekst);
        Behandling originalBehandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        Behandling revurdering = Behandling.fraTidligereBehandling(originalBehandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_MANGLER_FØDSEL).medOriginalBehandling(originalBehandling)).build();

        manipulerBehandling.forceOppdaterBehandlingSteg(revurdering, BehandlingStegType.FATTE_VEDTAK);
        BehandlingLås behandlingLås = lagreBehandling(revurdering);
        opprettFamilieHendelseGrunnlag(originalBehandling, revurdering);
        Fagsak fagsak = revurdering.getFagsak();
        BehandlingskontrollKontekst revurderingKontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingLås);
        oppdaterMedBehandlingsresultat(revurderingKontekst, false, antallBarn);

        fatteVedtakSteg.utførSteg(revurderingKontekst);
        Optional<BehandlingVedtak> behandlingVedtakOpt = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(revurderingKontekst.getBehandlingId());
        assertThat(behandlingVedtakOpt).isPresent();
        BehandlingVedtak behandlingVedtak = behandlingVedtakOpt.get();
        assertThat(behandlingVedtak).isNotNull();
        assertThat(behandlingVedtak.getVedtakResultatType()).isEqualTo(VedtakResultatType.AVSLAG);
        assertThat(behandlingVedtak.isBeslutningsvedtak()).isTrue();
    }

    @Test
    public void skal_lukke_godkjent_aksjonspunkter_og_sette_steg_til_utført() {
        // Arrange
        LagretVedtakRepository vedtakRepository = new LagretVedtakRepositoryImpl(repoRule.getEntityManager());
        HistorikkRepository historikkRepository = new HistorikkRepositoryImpl(repoRule.getEntityManager());
        RevurderingTjeneste revurderingESTjeneste = new RevurderingESTjenesteImpl(repositoryProvider, null, historikkRepository, revurderingEndring);

        OppgaveTjeneste oppgaveTjeneste = mock(OppgaveTjeneste.class);
        SøknadRepository søknadRepository = mock(SøknadRepository.class);
        TpsTjeneste tpsTjeneste = Mockito.mock(TpsTjeneste.class);
        FamilieHendelseRepository familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        PersonopplysningTjeneste personopplysningTjeneste = Mockito.mock(PersonopplysningTjeneste.class);
        PersonopplysningXmlTjenesteEngangsstønad personopplysningXmlTjeneste = new PersonopplysningXmlTjenesteEngangsstønad(tpsTjeneste, repositoryProvider, personopplysningTjeneste);
        VilkårsgrunnlagXmlTjeneste vilkårsgrunnlagXmlTjeneste = new VilkårsgrunnlagXmlTjenesteEngangsstønad(søknadRepository, familieHendelseRepository, kompletthetssjekkerProvider);
        YtelseXmlTjeneste ytelseXmlTjeneste = new YtelseXmlTjenesteEngangsstønad(repositoryProvider);
        BeregningsgrunnlagXmlTjeneste beregningsgrunnlagXmlTjeneste = new BeregningsgrunnlagXmlTjenesteEngangsstønad(repositoryProvider);
        BeregningsresultatXmlTjeneste beregningsresultatXmlTjeneste = new BeregningsresultatXmlTjenesteEngangstønad(beregningsgrunnlagXmlTjeneste, ytelseXmlTjeneste);
        BehandlingsresultatXmlTjeneste behandlingsresultatXmlTjeneste = new BehandlingsresultatXmlTjenesteEngangsstønad(beregningsresultatXmlTjeneste, vilkårsgrunnlagXmlTjeneste);
        TotrinnTjeneste totrinnTjeneste = mock(TotrinnTjeneste.class);

        Søknad søknad = new SøknadEntitet.Builder().medMottattDato(LocalDate.now()).medSøknadsdato(LocalDate.now()).build();
        when(søknadRepository.hentSøknadHvisEksisterer(any(Behandling.class))).thenReturn(Optional.ofNullable(søknad));

        VedtakXmlTjeneste vedtakXmlTjeneste = new VedtakXmlTjenesteEngangsstønad(repositoryProvider, personopplysningXmlTjeneste, behandlingsresultatXmlTjeneste);
        RevurderingTjenesteProvider revurderingTjenesteProvider = new RevurderingTjenesteProvider();
        VedtakTjeneste vedtakTjeneste = new VedtakTjenesteImpl(null, historikkRepository, revurderingTjenesteProvider, familieHendelseTjeneste, mock(TotrinnTjeneste.class));



        int antallBarn = 2;
        BehandlingskontrollKontekst kontekst = byggBehandlingsgrunnlagForFødsel(antallBarn, BehandlingStegType.FATTE_VEDTAK);
        oppdaterMedBehandlingsresultat(kontekst, true, antallBarn);

        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        behandling.setToTrinnsBehandling();
        Aksjonspunkt aksjonspunkt = opprettAksjonspunkt(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, behandling);

        // Legg til data i totrinsvurdering.
        Totrinnsvurdering.Builder vurdering = new Totrinnsvurdering.Builder(behandling, AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL);
        Totrinnsvurdering ttvurdering = vurdering.medGodkjent(true).medBegrunnelse("").build();

        List<Totrinnsvurdering> totrinnsvurderings = new ArrayList<>();
        totrinnsvurderings.add(ttvurdering);
        when(totrinnTjeneste.hentTotrinnaksjonspunktvurderinger(behandling)).thenReturn(totrinnsvurderings);

        FatteVedtakTjenesteEngangsstønadImpl fvtei = new FatteVedtakTjenesteEngangsstønadImpl(vedtakRepository, vedtakXmlTjeneste, vedtakTjeneste,
            revurderingESTjeneste, oppgaveTjeneste, totrinnTjeneste, behandlingVedtakTjeneste);

        fatteVedtakSteg = new FatteVedtakStegESImpl(repositoryProvider, fvtei);
        aksjonspunktRepository.setToTrinnsBehandlingKreves(aksjonspunkt);

        BehandleStegResultat behandleStegResultat = fatteVedtakSteg.utførSteg(kontekst);

        List<AksjonspunktDefinisjon> aksjonspunktListe = behandleStegResultat.getAksjonspunktListe();
        assertThat(aksjonspunktListe.size()).isEqualTo(0);
        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
    }

    @Test
    public void tilbakefører_og_reåpner_aksjonspunkt_når_totrinnskontroll_ikke_godkjent() {
        LagretVedtakRepository vedtakRepository = new LagretVedtakRepositoryImpl(repoRule.getEntityManager());
        HistorikkRepository historikkRepository = new HistorikkRepositoryImpl(repoRule.getEntityManager());
        RevurderingTjeneste revurderingESTjeneste = new RevurderingESTjenesteImpl(repositoryProvider, null, historikkRepository, revurderingEndring);

        OppgaveTjeneste oppgaveTjeneste = mock(OppgaveTjeneste.class);
        SøknadRepository søknadRepository = mock(SøknadRepository.class);
        TpsTjeneste tpsTjeneste = Mockito.mock(TpsTjeneste.class);
        FamilieHendelseRepository familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        PersonopplysningTjeneste personopplysningTjeneste = Mockito.mock(PersonopplysningTjeneste.class);
        PersonopplysningXmlTjenesteEngangsstønad personopplysningXmlTjeneste = new PersonopplysningXmlTjenesteEngangsstønad(tpsTjeneste, repositoryProvider, personopplysningTjeneste);
        VilkårsgrunnlagXmlTjeneste vilkårsgrunnlagXmlTjeneste = new VilkårsgrunnlagXmlTjenesteEngangsstønad(søknadRepository, familieHendelseRepository, kompletthetssjekkerProvider);
        YtelseXmlTjeneste ytelseXmlTjeneste = new YtelseXmlTjenesteEngangsstønad(repositoryProvider);
        BeregningsgrunnlagXmlTjeneste beregningsgrunnlagXmlTjeneste = new BeregningsgrunnlagXmlTjenesteEngangsstønad(repositoryProvider);
        BeregningsresultatXmlTjeneste beregningsresultatXmlTjeneste = new BeregningsresultatXmlTjenesteEngangstønad(beregningsgrunnlagXmlTjeneste, ytelseXmlTjeneste);
        BehandlingsresultatXmlTjeneste behandlingsresultatXmlTjeneste = new BehandlingsresultatXmlTjenesteEngangsstønad(beregningsresultatXmlTjeneste, vilkårsgrunnlagXmlTjeneste);

        TotrinnTjeneste totrinnTjeneste = mock(TotrinnTjeneste.class);

        Søknad søknad = new SøknadEntitet.Builder().medMottattDato(LocalDate.now()).medSøknadsdato(LocalDate.now()).build();
        when(søknadRepository.hentSøknadHvisEksisterer(any(Behandling.class))).thenReturn(Optional.ofNullable(søknad));

        VedtakXmlTjeneste vedtakXmlTjeneste = new VedtakXmlTjenesteEngangsstønad(repositoryProvider, personopplysningXmlTjeneste, behandlingsresultatXmlTjeneste);
        RevurderingTjenesteProvider revurderingTjenesteProvider = new RevurderingTjenesteProvider();
        VedtakTjeneste vedtakTjeneste = new VedtakTjenesteImpl(null, historikkRepository, revurderingTjenesteProvider, familieHendelseTjeneste, mock(TotrinnTjeneste.class));

        int antallBarn = 2;
        BehandlingskontrollKontekst kontekst = byggBehandlingsgrunnlagForFødsel(antallBarn, BehandlingStegType.FATTE_VEDTAK);
        oppdaterMedBehandlingsresultat(kontekst, true, antallBarn);

        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        behandling.setToTrinnsBehandling();

        Aksjonspunkt avklarFødsel = opprettAksjonspunkt(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, behandling);
        aksjonspunktRepository.setToTrinnsBehandlingKreves(avklarFødsel);

        opprettAksjonspunkt(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET, behandling);

        // Legg til data i totrinsvurdering.
        Totrinnsvurdering.Builder vurdering = new Totrinnsvurdering.Builder(behandling, AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL);
        Totrinnsvurdering vurderesPåNytt = vurdering.medGodkjent(false).medBegrunnelse("Må vurderes på nytt").medVurderÅrsak(VurderÅrsak.FEIL_LOV).build();

        Totrinnsvurdering.Builder vurdering2 = new Totrinnsvurdering.Builder(behandling, AksjonspunktDefinisjon.MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET);
        Totrinnsvurdering vurderesOk = vurdering2.medGodkjent(true).medBegrunnelse("").build();

        List<Totrinnsvurdering> totrinnsvurderings = new ArrayList<>();
        totrinnsvurderings.add(vurderesPåNytt);
        totrinnsvurderings.add(vurderesOk);
        when(totrinnTjeneste.hentTotrinnaksjonspunktvurderinger(behandling)).thenReturn(totrinnsvurderings);

        FatteVedtakTjenesteEngangsstønadImpl fvtei = new FatteVedtakTjenesteEngangsstønadImpl(vedtakRepository, vedtakXmlTjeneste, vedtakTjeneste,
            revurderingESTjeneste, oppgaveTjeneste, totrinnTjeneste, behandlingVedtakTjeneste);

        fatteVedtakSteg = new FatteVedtakStegESImpl(repositoryProvider, fvtei);

        opprettAksjonspunkt(AksjonspunktDefinisjon.FORESLÅ_VEDTAK, behandling);

        BehandleStegResultat behandleStegResultat = fatteVedtakSteg.utførSteg(kontekst);

        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.TILBAKEFØRT_TIL_AKSJONSPUNKT);

        behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        Optional<Aksjonspunkt> oppdatertAvklarFødsel = behandling.getAksjonspunktMedDefinisjonOptional(SJEKK_MANGLENDE_FØDSEL);
        assertThat(oppdatertAvklarFødsel).isPresent();
        assertThat(oppdatertAvklarFødsel.get().getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);

        Optional<Aksjonspunkt> oppdatertForeslåVedtak = behandling.getAksjonspunktMedDefinisjonOptional(FORESLÅ_VEDTAK);
        assertThat(oppdatertForeslåVedtak).isPresent();
        assertThat(oppdatertForeslåVedtak.get().getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);

        Optional<Aksjonspunkt> oppdatertSøknFristVilkåret = behandling.getAksjonspunktMedDefinisjonOptional(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET);
        assertThat(oppdatertSøknFristVilkåret).isPresent();
    }

    @Test
    public void skal_fatte_negativt_vedtak() {
        // Arrange
        int antallBarn = 1;
        BehandlingskontrollKontekst kontekst = byggBehandlingsgrunnlagForFødsel(antallBarn, BehandlingStegType.FATTE_VEDTAK);
        oppdaterMedBehandlingsresultat(kontekst, false, antallBarn);

        // Act
        fatteVedtakSteg.utførSteg(kontekst);

        // Assert
        Optional<BehandlingVedtak> behandlingVedtakOpt = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(kontekst.getBehandlingId());
        assertThat(behandlingVedtakOpt).isPresent();
        BehandlingVedtak behandlingVedtak = behandlingVedtakOpt.get();
        assertThat(behandlingVedtak).isNotNull();
        assertThat(behandlingVedtak.getVedtakResultatType()).isEqualTo(VedtakResultatType.AVSLAG);
    }

    private BehandlingskontrollKontekst byggBehandlingsgrunnlagForFødsel(int antallBarn, BehandlingStegType behandlingStegType) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medBekreftetHendelse().medFødselsDato(LocalDate.now())
            .medAntallBarn(antallBarn);

        Behandling behandling = scenario
            .medBehandlingStegStart(behandlingStegType)
            .medBehandlendeEnhet(BEHANDLENDE_ENHET)
            .lagre(repositoryProvider);
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(behandling);
        Beregning beregning = new Beregning(1L, 1L, 1L, LocalDateTime.now());
        BeregningResultat beregningResultat = BeregningResultat.builder().medBeregning(beregning).buildFor(behandling);
        beregningRepository.lagre(beregningResultat, behandlingRepository.taSkriveLås(behandling));

        Fagsak fagsak = behandling.getFagsak();
        return new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling));
    }

    private void oppdaterMedVedtak(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder()
            .medBehandlingsresultat(behandlingsresultat)
            .medAnsvarligSaksbehandler("VL")
            .medVedtaksdato(LocalDate.now())
            .medIverksettingStatus(IverksettingStatus.IVERKSATT)
            .medBeslutning(false)
            .medVedtakResultatType(behandlingsresultat.getBehandlingResultatType()
                .equals(BehandlingResultatType.INNVILGET) ? VedtakResultatType.INNVILGET : VedtakResultatType.AVSLAG).build();

        behandlingVedtakRepository.lagre(behandlingVedtak, kontekst.getSkriveLås());
        repository.flush();
        repository.clear();
    }


    private void oppdaterMedBehandlingsresultat(BehandlingskontrollKontekst kontekst, boolean innvilget, int antallBarn) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        Behandlingsresultat.builderForInngangsvilkår()
            .medBehandlingResultatType(innvilget ? BehandlingResultatType.INNVILGET : BehandlingResultatType.AVSLÅTT)
            .buildFor(behandling);
        VilkårResultat.builder()
            .leggTilVilkårResultat(VilkårType.FØDSELSVILKÅRET_MOR, innvilget ? VilkårUtfallType.OPPFYLT : VilkårUtfallType.IKKE_OPPFYLT,
                null, new Properties(), null, false, false, null, null)
            .medVilkårResultatType(innvilget ? VilkårResultatType.INNVILGET : VilkårResultatType.AVSLÅTT)
            .buildFor(behandling);
        if (innvilget) {
            BeregningResultat.builder()
                .medBeregning(new Beregning(48500L, antallBarn, 48500L * antallBarn, LocalDateTime.now()))
                .buildFor(behandling);
        }

        BehandlingLås lås = kontekst.getSkriveLås();
        behandlingRepository.lagre(behandling.getBehandlingsresultat().getVilkårResultat(), lås);
        if (behandling.getBehandlingsresultat().getBeregningResultat() != null) {
            beregningRepository.lagre(behandling.getBehandlingsresultat().getBeregningResultat(), lås);
        }

        behandlingRepository.lagre(behandling, lås);
        repository.flush();
    }

    private Aksjonspunkt opprettAksjonspunkt(AksjonspunktDefinisjon akspktdef, Behandling behandling) {
        return aksjonspunktRepository.leggTilAksjonspunkt(behandling, akspktdef,
            BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
    }
}
