package no.nav.foreldrepenger.behandling.steg.lopendemedlemskap.impl;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandling.steg.lopendemedlemskap.impl.VurderLøpendeMedlemskapStegImpl.FPSAK_LØPENDE_MEDLEMSKAP;

import java.time.LocalDate;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.finn.unleash.FakeUnleash;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapPerioderBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.inngangsvilkaar.medlemskap.VurderLøpendeMedlemskap;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(CdiRunner.class)
public class VurderLøpendeMedlemskapStegImplTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider provider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private BehandlingRepository behandlingRepository = provider.getBehandlingRepository();
    private MedlemskapRepository medlemskapRepository = provider.getMedlemskapRepository();
    private PersonopplysningRepository personopplysningRepository = provider.getPersonopplysningRepository();
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = provider.getInntektArbeidYtelseRepository();
    private FagsakRepository fagsakRepository = provider.getFagsakRepository();

    private VurderLøpendeMedlemskapStegImpl steg;

    @Inject
    private VurderLøpendeMedlemskap vurdertLøpendeMedlemskapTjeneste;
    private FakeUnleash unleash = new FakeUnleash();

    @Before
    public void setUp() {
        steg = new VurderLøpendeMedlemskapStegImpl(unleash, vurdertLøpendeMedlemskapTjeneste, provider);
    }

    @Test
    public void skal_kjøre_steg() {
        // Arrange
        unleash.enable(FPSAK_LØPENDE_MEDLEMSKAP);
        // Arrange
        LocalDate datoMedEndring = LocalDate.now().plusDays(10);
        LocalDate ettÅrSiden = LocalDate.now().minusYears(1);
        LocalDate iDag = LocalDate.now();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        RegistrertMedlemskapPerioder periode = opprettPeriode(ettÅrSiden, iDag, MedlemskapDekningType.FTL_2_6);
        scenario.leggTilMedlemskapPeriode(periode);
        Behandling behandling = scenario.lagre(provider);
        VilkårResultat vilkårResultat = VilkårResultat.builder()
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .buildFor(behandling);

        behandlingRepository.lagre(vilkårResultat, behandlingRepository.taSkriveLås(behandling));

        avslutterBehandlingOgFagsak(behandling);

        Behandling revudering = opprettRevudering(behandling);

        oppdaterMedlem(datoMedEndring, periode, revudering);

        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(revudering);
        Fagsak fagsak = revudering.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås);

        steg.utførSteg(kontekst);
    }

    @Test
    public void skal_ikke_kjøre_steg_når_feature_er_av() {
        // Arrange
        unleash.disableAll();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Behandling behandling = scenario.lagre(provider);

        Behandling revudering = opprettRevudering(behandling);

        BehandlingLås lås = behandlingRepository.taSkriveLås(revudering);
        Fagsak fagsak = revudering.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås);

        steg.utførSteg(kontekst);
    }

    private Behandling opprettRevudering(Behandling behandling) {
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_FEIL_ELLER_ENDRET_FAKTA)
            .medOriginalBehandling(behandling);

        Behandling revudering = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(revurderingÅrsak).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(revudering);
        behandlingRepository.lagre(revudering, lås);

        Behandlingsresultat.Builder builder = Behandlingsresultat.builderForInngangsvilkår();
        Behandlingsresultat behandlingsresultat = builder.buildFor(revudering);

        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);

        medlemskapRepository.kopierGrunnlagFraEksisterendeBehandling(behandling, revudering);
        inntektArbeidYtelseRepository.kopierGrunnlagFraEksisterendeBehandling(behandling, revudering);
        personopplysningRepository.kopierGrunnlagFraEksisterendeBehandlingForRevurdering(behandling, revudering);

        return revudering;
    }

    private RegistrertMedlemskapPerioder opprettPeriode(LocalDate fom, LocalDate tom, MedlemskapDekningType dekningType) {
        RegistrertMedlemskapPerioder periode = new MedlemskapPerioderBuilder()
            .medDekningType(dekningType)
            .medMedlemskapType(MedlemskapType.FORELOPIG)
            .medPeriode(fom, tom)
            .medMedlId(1L)
            .build();
        return periode;
    }

    private void avslutterBehandlingOgFagsak(Behandling behandling) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Behandlingsresultat.Builder behandlingsresultatBuilder = Behandlingsresultat.builderForInngangsvilkår();
        Behandlingsresultat behandlingsresultat = behandlingsresultatBuilder.buildFor(behandling);

        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandling, lås);

        behandling.avsluttBehandling();
        behandlingRepository.lagre(behandling, lås);
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.AVSLUTTET);
    }

    private void oppdaterMedlem(LocalDate datoMedEndring, RegistrertMedlemskapPerioder periode, Behandling behandling) {
        RegistrertMedlemskapPerioder nyPeriode = new MedlemskapPerioderBuilder()
            .medPeriode(datoMedEndring, null)
            .medDekningType(MedlemskapDekningType.FULL)
            .medMedlemskapType(MedlemskapType.ENDELIG)
            .medMedlId(2L)
            .build();
        medlemskapRepository.lagreMedlemskapRegisterOpplysninger(behandling, asList(periode, nyPeriode));
    }
}
