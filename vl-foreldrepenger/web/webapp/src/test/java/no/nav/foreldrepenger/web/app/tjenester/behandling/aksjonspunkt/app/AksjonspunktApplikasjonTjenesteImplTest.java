package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaPeriode;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FatterVedtakAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.OmsorgsvilkårAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftTerminbekreftelseAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.BekreftetUttakPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.KontrollerFaktaPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.ManuellAvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.SlettetUttakPeriodeDto;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;
import no.nav.vedtak.util.FPDateUtil;

@RunWith(CdiRunner.class)
public class AksjonspunktApplikasjonTjenesteImplTest {

    private static final String BEGRUNNELSE = "begrunnelse";
    private static final LocalDate TERMINDATO = LocalDate.now(FPDateUtil.getOffset()).plusDays(40);
    private static final LocalDate UTSTEDTDATO = LocalDate.now(FPDateUtil.getOffset()).minusDays(7);

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private AksjonspunktApplikasjonTjeneste aksjonspunktApplikasjonTjeneste;

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private AksjonspunktRepository aksjonspunktRepository;

    private YtelsesFordelingRepository fordelingRepository = new YtelsesFordelingRepositoryImpl(repoRule.getEntityManager());
    private InternalManipulerBehandling manipulerBehandling;

    private AbstractTestScenario<?> lagScenarioMedAksjonspunkt(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medDefaultSøknadTerminbekreftelse();
        scenario.leggTilAksjonspunkt(aksjonspunktDefinisjon, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        return scenario;
    }

    @Test
    public void skal_sette_aksjonspunkt_til_utført_og_lagre_behandling() {
        // Arrange
        // Bruker BekreftTerminbekreftelseAksjonspunktDto som konkret case
        AbstractTestScenario<?> scenario = lagScenarioMedAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE);
        Behandling behandling = scenario.lagre(repositoryProvider);

        BekreftTerminbekreftelseAksjonspunktDto dto = new BekreftTerminbekreftelseAksjonspunktDto(BEGRUNNELSE, TERMINDATO, UTSTEDTDATO, 1);

        // Act
        aksjonspunktApplikasjonTjeneste.bekreftAksjonspunkter(singletonList(dto), behandling.getId());

        // Assert
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandling.getId());
        Assertions.assertThat(oppdatertBehandling.getAksjonspunkter()).first().matches(a -> a.erUtført());

    }

    @Test
    public void skal_håndtere_aksjonspunkt_for_omsorgsvilkåret() {
        AbstractTestScenario<?> scenario = lagScenarioMedAksjonspunkt(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_OMSORGSVILKÅRET);
        Behandling behandling = scenario.lagre(repositoryProvider);
        OmsorgsvilkårAksjonspunktDto dto = new OmsorgsvilkårAksjonspunktDto(BEGRUNNELSE, false,
            Avslagsårsak.SØKER_ER_IKKE_BARNETS_FAR_O.getKode());

        // Act
        aksjonspunktApplikasjonTjeneste.bekreftAksjonspunkter(singletonList(dto), behandling.getId());

        // Assert
        assertThat(behandling.getBehandlingsresultat().getVilkårResultat()).isNotNull();
        assertThat(behandling.getBehandlingsresultat().getVilkårResultat().getVilkårene()).hasSize(1);
        assertThat(behandling.getBehandlingsresultat().getVilkårResultat().getVilkårene().iterator().next().getAvslagsårsak())
            .isEqualTo(Avslagsårsak.SØKER_ER_IKKE_BARNETS_FAR_O);
    }

    @Test
    public void skal_sette_ansvarlig_saksbehandler() {
        // Arrange
        // Bruker BekreftTerminbekreftelseAksjonspunktDto som konkret case
        AksjonspunktApplikasjonTjenesteImpl aksjonspunktApplikasjonTjenesteImpl = (AksjonspunktApplikasjonTjenesteImpl) aksjonspunktApplikasjonTjeneste;
        AbstractTestScenario<?> scenario = lagScenarioMedAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE);
        Behandling behandling = scenario.lagre(repositoryProvider);
        Behandling behandlingSpy = spy(behandling);

        BekreftTerminbekreftelseAksjonspunktDto dto = new BekreftTerminbekreftelseAksjonspunktDto(BEGRUNNELSE, TERMINDATO, UTSTEDTDATO, 1);

        // Act
        aksjonspunktApplikasjonTjenesteImpl.setAnsvarligSaksbehandler(singletonList(dto), behandlingSpy);

        // Assert
        verify(behandlingSpy, times(1)).setAnsvarligSaksbehandler(any());
    }

    @Test
    public void skal_ikke_sette_ansvarlig_saksbehandler_hvis_bekreftet_aksjonspunkt_er_fatter_vedtak() {
        // Arrange
        // Bruker BekreftTerminbekreftelseAksjonspunktDto som konkret case
        AksjonspunktApplikasjonTjenesteImpl aksjonspunktApplikasjonTjenesteImpl = (AksjonspunktApplikasjonTjenesteImpl) aksjonspunktApplikasjonTjeneste;
        AbstractTestScenario<?> scenario = lagScenarioMedAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE);
        Behandling behandling = scenario.lagre(repositoryProvider);
        Behandling behandlingSpy = spy(behandling);

        FatterVedtakAksjonspunktDto dto = new FatterVedtakAksjonspunktDto(BEGRUNNELSE, Collections.emptyList());

        // Act
        aksjonspunktApplikasjonTjenesteImpl.setAnsvarligSaksbehandler(singletonList(dto), behandlingSpy);

        // Assert
        verify(behandlingSpy, never()).setAnsvarligSaksbehandler(any());
    }

    @Test
    public void skal_sette_totrinn_når_revurdering_ap_medfører_endring_i_grunnlag() {
        // Arrange
        Behandling førstegangsbehandling = opprettFørstegangsbehandlingMedAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE);
        aksjonspunktRepository.setTilUtført(førstegangsbehandling.getAksjonspunkter().iterator().next(), BEGRUNNELSE);
        Behandling revurdering = opprettRevurderingsbehandlingMedAksjonspunktFraFørstegangsbehandling(førstegangsbehandling);
        BekreftTerminbekreftelseAksjonspunktDto dto = new BekreftTerminbekreftelseAksjonspunktDto(BEGRUNNELSE, TERMINDATO.plusDays(1), UTSTEDTDATO, 1);

        // Act
        aksjonspunktApplikasjonTjeneste.bekreftAksjonspunkter(singletonList(dto), revurdering.getId());

        // Assert
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(revurdering.getId());
        Aksjonspunkt aksjonspunkt = oppdatertBehandling.getAksjonspunkter().iterator().next();
        assertThat(aksjonspunkt.isToTrinnsBehandling()).isTrue();
    }

    @Test
    public void skal_sette_totrinn_når_revurdering_ap_har_endring_i_begrunnelse() {
        // Arrange
        Behandling førstegangsbehandling = opprettFørstegangsbehandlingMedAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE);
        BekreftTerminbekreftelseAksjonspunktDto dto1 = new BekreftTerminbekreftelseAksjonspunktDto(BEGRUNNELSE, TERMINDATO, UTSTEDTDATO, 1);
        aksjonspunktApplikasjonTjeneste.bekreftAksjonspunkter(singletonList(dto1), førstegangsbehandling.getId());

        Behandling revurdering = opprettRevurderingsbehandlingMedAksjonspunktFraFørstegangsbehandling(førstegangsbehandling);
        BekreftTerminbekreftelseAksjonspunktDto dto2 = new BekreftTerminbekreftelseAksjonspunktDto(BEGRUNNELSE + "2", TERMINDATO, UTSTEDTDATO, 1);

        // Act
        aksjonspunktApplikasjonTjeneste.bekreftAksjonspunkter(singletonList(dto2), revurdering.getId());

        // Assert
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(revurdering.getId());
        Aksjonspunkt aksjonspunkt = oppdatertBehandling.getAksjonspunkter().iterator().next();
        assertThat(aksjonspunkt.isToTrinnsBehandling()).isTrue();
    }

    @Test
    public void skal_ikke_sette_totrinn_når_revurdering_ap_verken_har_endring_i_grunnlag_eller_begrunnelse() {
        // Arrange
        Behandling førstegangsbehandling = opprettFørstegangsbehandlingMedAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE);
        BekreftTerminbekreftelseAksjonspunktDto dto1 = new BekreftTerminbekreftelseAksjonspunktDto(BEGRUNNELSE, TERMINDATO, UTSTEDTDATO, 1);
        aksjonspunktApplikasjonTjeneste.bekreftAksjonspunkter(singletonList(dto1), førstegangsbehandling.getId());

        Behandling revurdering = opprettRevurderingsbehandlingMedAksjonspunktFraFørstegangsbehandling(førstegangsbehandling);
        BekreftTerminbekreftelseAksjonspunktDto dto2 = new BekreftTerminbekreftelseAksjonspunktDto(BEGRUNNELSE, TERMINDATO, UTSTEDTDATO, 1);

        // Act
        aksjonspunktApplikasjonTjeneste.bekreftAksjonspunkter(singletonList(dto2), revurdering.getId());

        // Assert
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(revurdering.getId());
        Aksjonspunkt aksjonspunkt = oppdatertBehandling.getAksjonspunkter().iterator().next();
        assertThat(aksjonspunkt.isToTrinnsBehandling()).isFalse();
    }

    @Test
    public void skal_teste_manuell_avklar_fakta_uttak_aksjonspunkt() {
        Behandling behandling = opprettRevurderingBehandling();
        manipulerBehandling = new InternalManipulerBehandlingImpl(repositoryProvider);
        manipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FATTE_VEDTAK);

        ManuellAvklarFaktaUttakDto dto = opprettManuellAvklarFaktaUttakDto();
        // Act
        aksjonspunktApplikasjonTjeneste.overstyrAksjonspunkter(singletonList(dto), behandling.getId());

        // Assert
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandling.getId());
        Assertions.assertThat(oppdatertBehandling.getAksjonspunkter()).first().matches(Aksjonspunkt::erUtført);
        Aksjonspunkt aksjonspunkt = oppdatertBehandling.getAksjonspunkter().iterator().next();
        assertThat(aksjonspunkt.isToTrinnsBehandling()).isTrue();
    }

    @Test
    public void skal_sette_totrinn_når_revurdering_på_revurdeing_har_endring_manuell_avklar_fakta_uttak() {
        Behandling revurderingBehandling = opprettRevurderingBehandling();
        manipulerBehandling = new InternalManipulerBehandlingImpl(repositoryProvider);
        manipulerBehandling.forceOppdaterBehandlingSteg(revurderingBehandling, BehandlingStegType.FATTE_VEDTAK);

        ManuellAvklarFaktaUttakDto dto = opprettManuellAvklarFaktaUttakDto();
        // Act
        aksjonspunktApplikasjonTjeneste.overstyrAksjonspunkter(singletonList(dto), revurderingBehandling.getId());

        Behandling revurderingPåRevurdering = opprettRevurderingsbehandlingMedAksjonspunktFraFørstegangsbehandling(revurderingBehandling);
        repositoryProvider.getYtelsesFordelingRepository().kopierGrunnlagFraEksisterendeBehandling(revurderingBehandling, revurderingPåRevurdering);
//        //manipulere steg
        manipulerBehandling = new InternalManipulerBehandlingImpl(repositoryProvider);
        manipulerBehandling.forceOppdaterBehandlingSteg(revurderingBehandling, BehandlingStegType.VURDER_UTTAK);
        //først bekreft
        aksjonspunktApplikasjonTjeneste.overstyrAksjonspunkter(singletonList(dto), revurderingPåRevurdering.getId());
        //manipulere steg
        manipulerBehandling = new InternalManipulerBehandlingImpl(repositoryProvider);
        manipulerBehandling.forceOppdaterBehandlingSteg(revurderingBehandling, BehandlingStegType.FATTE_VEDTAK);

        //Gjøre endring
        ManuellAvklarFaktaUttakDto dto2 = new ManuellAvklarFaktaUttakDto();
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto1 = getBekreftetUttakPeriodeDto(LocalDate.now().minusDays(25), LocalDate.now().minusDays(16));
        bekreftetUttakPeriodeDto1.setOrginalFom(LocalDate.now().minusDays(25));
        bekreftetUttakPeriodeDto1.setOrginalTom(LocalDate.now().minusDays(16));
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto2 = getBekreftetUttakPeriodeDto(LocalDate.now().minusDays(15), LocalDate.now());
        dto2.setBekreftedePerioder(Arrays.asList(bekreftetUttakPeriodeDto1, bekreftetUttakPeriodeDto2));

        // Act
        aksjonspunktApplikasjonTjeneste.overstyrAksjonspunkter(singletonList(dto2), revurderingPåRevurdering.getId());

        // Assert
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(revurderingPåRevurdering.getId());
        Assertions.assertThat(oppdatertBehandling.getAksjonspunkter()).first().matches(Aksjonspunkt::erUtført);
        Aksjonspunkt aksjonspunkt = oppdatertBehandling.getAksjonspunkter().iterator().next();
        assertThat(aksjonspunkt.isToTrinnsBehandling()).isTrue();
    }

    private Behandling opprettFørstegangsbehandlingMedAksjonspunkt(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        ScenarioMorSøkerForeldrepenger førstegangsscenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        førstegangsscenario.medSøknad().medMottattDato(LocalDate.now());
        førstegangsscenario.medSøknadHendelse()
            .medAntallBarn(1)
            .medTerminbekreftelse(førstegangsscenario.medSøknadHendelse()
                .getTerminbekreftelseBuilder()
                .medTermindato(TERMINDATO)
                .medUtstedtDato(UTSTEDTDATO));

        førstegangsscenario.leggTilAksjonspunkt(aksjonspunktDefinisjon, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        Behandling behandling = førstegangsscenario.lagre(repositoryProvider);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new AvklarteUttakDatoerEntitet(LocalDate.now(), null)); // HACK
        return behandling;
    }

    private Behandling opprettRevurderingsbehandlingMedAksjonspunktFraFørstegangsbehandling(Behandling førstegangsbehandling) {
        avsluttBehandlingOgFagsak(førstegangsbehandling);

        ScenarioMorSøkerForeldrepenger revurderingsscenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medOriginalBehandling(førstegangsbehandling, BehandlingÅrsakType.RE_HENDELSE_FØDSEL)
            .medBehandlingType(BehandlingType.REVURDERING);
        revurderingsscenario.medSøknad().medMottattDato(LocalDate.now());

        Behandling revurdering = revurderingsscenario.lagre(repositoryProvider);

        aksjonspunktRepository.kopierAlleAksjonspunkterOgSettDemInaktive(førstegangsbehandling, revurdering);
        Aksjonspunkt aksjonspunkt = revurdering.getAlleAksjonspunkterInklInaktive().iterator().next();
        aksjonspunktRepository.reaktiver(aksjonspunkt);
        aksjonspunktRepository.setReåpnet(aksjonspunkt);

        repositoryProvider.getFamilieGrunnlagRepository().kopierGrunnlagFraEksisterendeBehandling(førstegangsbehandling, revurdering);
        repositoryProvider.getYtelsesFordelingRepository().lagre(revurdering, new AvklarteUttakDatoerEntitet(LocalDate.now(), null)); // HACK
        return revurdering;
    }

    private void avsluttBehandlingOgFagsak(Behandling behandling) {
        behandling.avsluttBehandling();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.LØPENDE);
    }

    private Behandling opprettRevurderingBehandling() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medSøknad();
        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, false, true);
        scenario.medOppgittRettighet(rettighet);
        scenario.lagre(repositoryProvider);
        Behandling førstegangsbehandling = scenario.getBehandling();

        final OppgittPeriode periode_1 = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now().minusDays(15), LocalDate.now())
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .build();
        final OppgittPeriode periode_2 = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now().minusDays(25), LocalDate.now().minusDays(16))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .build();
        fordelingRepository.lagre(førstegangsbehandling, new OppgittFordelingEntitet(Arrays.asList(periode_1, periode_2), true));

        avsluttBehandlingOgFagsak(førstegangsbehandling);

        ScenarioMorSøkerForeldrepenger revurderingsscenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medOriginalBehandling(førstegangsbehandling, BehandlingÅrsakType.RE_HENDELSE_FØDSEL)
            .medBehandlingType(BehandlingType.REVURDERING);

        revurderingsscenario.medSøknad().medMottattDato(LocalDate.now());
        Behandling revurdering = revurderingsscenario.lagre(repositoryProvider);

        repositoryProvider.getFamilieGrunnlagRepository().kopierGrunnlagFraEksisterendeBehandling(førstegangsbehandling, revurdering);
        repositoryProvider.getYtelsesFordelingRepository().kopierGrunnlagFraEksisterendeBehandling(førstegangsbehandling, revurdering);
        return revurdering;
    }

    private ManuellAvklarFaktaUttakDto opprettManuellAvklarFaktaUttakDto() {
        ManuellAvklarFaktaUttakDto dto = new ManuellAvklarFaktaUttakDto();
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto = getBekreftetUttakPeriodeDto(LocalDate.now().minusDays(25), LocalDate.now().minusDays(16));
        SlettetUttakPeriodeDto slettetPeriodeDto = new SlettetUttakPeriodeDto();
        slettetPeriodeDto.setBegrunnelse("ugyldig søknadsperiode");
        slettetPeriodeDto.setUttakPeriodeType(UttakPeriodeType.FORELDREPENGER);
        slettetPeriodeDto.setFom(LocalDate.now().minusDays(15));
        slettetPeriodeDto.setTom(LocalDate.now());
        dto.setSlettedePerioder(Collections.singletonList(slettetPeriodeDto));
        dto.setBekreftedePerioder(Collections.singletonList(bekreftetUttakPeriodeDto));
        return dto;
    }

    private BekreftetUttakPeriodeDto getBekreftetUttakPeriodeDto(LocalDate fom, LocalDate tom) {
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto = new BekreftetUttakPeriodeDto();
        OppgittPeriode bekreftetperiode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .build();
        bekreftetUttakPeriodeDto.setBekreftetPeriode(new KontrollerFaktaPeriodeDto(KontrollerFaktaPeriode.ubekreftet(bekreftetperiode)));
        bekreftetUttakPeriodeDto.setOrginalFom(fom);
        bekreftetUttakPeriodeDto.setOrginalTom(tom);
        return bekreftetUttakPeriodeDto;
    }

}
