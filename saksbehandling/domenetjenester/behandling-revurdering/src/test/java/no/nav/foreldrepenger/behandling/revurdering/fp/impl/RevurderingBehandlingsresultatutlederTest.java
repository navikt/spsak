package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.revurdering.EndringsdatoRevurderingUtleder;
import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingEndring;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.RettenTil;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(CdiRunner.class)
public class RevurderingBehandlingsresultatutlederTest {
    static final List<String> ARBEIDSFORHOLDLISTE = Arrays.asList("154", "265", "386", "412");
    static final BigDecimal TOTAL_ANDEL_NORMAL = BigDecimal.valueOf(300000);
    static final BigDecimal TOTAL_ANDEL_OPPJUSTERT = BigDecimal.valueOf(350000);
    private static final String ARBEIDSFORHOLD_ID = "987123987";
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.now();
    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    @Inject
    private RevurderingEndring revurderingEndring;
    private BehandlingRepository behandlingRepository;
    private HistorikkRepository historikkRepository;
    private RevurderingTjeneste revurderingTjeneste;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    private EndringsdatoRevurderingUtleder endringsdatoRevurderingUtleder = mock(EndringsdatoRevurderingUtleder.class);
    private RevurderingFPBehandlingsresultatutleder revurderingFPBehandlingsresultatutleder;
    private boolean erVarselOmRevurderingSendt = true;

    private Behandling behandlingSomSkalRevurderes;
    private Behandling revurdering;
    private Beregningsgrunnlag beregningsgrunnlag;
    private VirksomhetEntitet virksomhet;
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;
    private LocalDate endringsdato;

    @Before
    public void setUp() {
        medlemskapVilkårPeriodeRepository = repositoryProvider.getMedlemskapVilkårPeriodeRepository();
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
        historikkRepository = repositoryProvider.getHistorikkRepository();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS, BehandlingStegType.KONTROLLER_FAKTA);
        behandlingSomSkalRevurderes = scenario.lagre(repositoryProvider);
        scenario.avsluttBehandling(repositoryProvider, behandlingSomSkalRevurderes);
        revurderingFPBehandlingsresultatutleder = new RevurderingFPBehandlingsresultatutleder(repositoryProvider, endringsdatoRevurderingUtleder);
        BehandlingModellRepository mock = mock(BehandlingModellRepository.class);
        when(mock.getModell(any(), any())).thenReturn(mock(BehandlingModell.class));
        BehandlingskontrollTjenesteImpl behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider,
            mock, mock(BehandlingskontrollEventPubliserer.class));
        revurderingTjeneste = new RevurderingTjenesteImpl(repositoryProvider, behandlingskontrollTjeneste, historikkRepository, revurderingEndring);
        revurdering = revurderingTjeneste
            .opprettAutomatiskRevurdering(behandlingSomSkalRevurderes.getFagsak(), BehandlingÅrsakType.RE_ANNET);
        virksomhet = new VirksomhetEntitet.Builder().medOrgnr(ARBEIDSFORHOLD_ID).medNavn("Virksomheten").oppdatertOpplysningerNå().build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);
        endringsdato = LocalDate.now().minusMonths(3);
        when(endringsdatoRevurderingUtleder.utledEndringsdato(any(Behandling.class))).thenReturn(endringsdato);
    }

    // Case 2
    // Løpende vedtak: Ja
    // Oppfylt inngangsvilkår på skjæringstidspunktet
    // Ikkje oppfylt inngangsvilkår i perioden
    @Test
    public void tilfelle_2_behandlingsresultat_lik_opphør_rettentil_lik_nei_foreldrepenger_opphører() {

        // Arrange
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);

        // Ikke oppfylt inngangsvilkår i perioden (medlemskap)
        settVilkårutfallMedlemskapPåDato(VilkårUtfallType.IKKE_OPPFYLT, endringsdato);

        // Act
        revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(revurdering, erVarselOmRevurderingSendt);
        Behandlingsresultat bhResultat = revurdering.getBehandlingsresultat();
        boolean uendretUtfall = revurderingTjeneste.erRevurderingMedUendretUtfall(revurdering);

        // Assert
        assertThat(bhResultat.getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.OPPHØR);
        assertThat(bhResultat.getRettenTil()).isEqualByComparingTo(RettenTil.HAR_IKKE_RETT_TIL_FP);
        assertThat(bhResultat.getKonsekvenserForYtelsen()).containsExactly(KonsekvensForYtelsen.FORELDREPENGER_OPPHØRER);
        assertThat(uendretUtfall).isFalse();
    }

    // Case 9
    // Løpende vedtak: Ja
    // Oppfylt inngangsvilkår på skjæringstidspunktet
    // Oppfylt inngangsvilkår i perioden
    // Endring i beregning: Nei
    @Test
    public void tilfelle_9_behandlingsresultat_lik_ingenEndring_rettentil_lik_ja_foreldrepenger_konsekvens_ingenEndring() {

        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat vilkårResultat = VilkårResultat.builder()
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);
        behandlingRepository.lagre(vilkårResultat, lås);

        // Oppfylt inngangsvilkår i perioden (medlemskap)
        settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType.OPPFYLT);

        // Endring i beregning: Ingen endring
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, false, bgPeriode);
        byggBeregningsgrunnlagForBehandling(revurdering, false, false, bgPeriode);

        // Act
        revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(revurdering, erVarselOmRevurderingSendt);
        Behandlingsresultat bhResultat = revurdering.getBehandlingsresultat();
        boolean uendretUtfall = revurderingTjeneste.erRevurderingMedUendretUtfall(revurdering);

        // Assert
        assertThat(bhResultat.getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.INGEN_ENDRING);
        assertThat(bhResultat.getRettenTil()).isEqualByComparingTo(RettenTil.HAR_RETT_TIL_FP);
        assertThat(bhResultat.getKonsekvenserForYtelsen()).containsExactly(KonsekvensForYtelsen.INGEN_ENDRING);
        assertThat(uendretUtfall).isTrue();
    }

    @Test
    public void skal_gi_ingen_vedtaksbrev_når_ingen_endring_og_varsel_om_revurdering_ikke_er_sendt() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat vilkårResultat = VilkårResultat.builder()
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);
        behandlingRepository.lagre(vilkårResultat, lås);

        // Oppfylt inngangsvilkår i perioden (medlemskap)
        settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType.OPPFYLT);

        // Endring i beregning: kun endring i fordeling av ytelsen
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, false, bgPeriode);
        byggBeregningsgrunnlagForBehandling(revurdering, false, false, bgPeriode);

        // Act
        revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(revurdering, false);
        Behandlingsresultat bhResultat = revurdering.getBehandlingsresultat();
        boolean uendretUtfall = revurderingTjeneste.erRevurderingMedUendretUtfall(revurdering);

        // Assert
        assertThat(bhResultat.getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.INGEN_ENDRING);
        assertThat(bhResultat.getRettenTil()).isEqualByComparingTo(RettenTil.HAR_RETT_TIL_FP);
        assertThat(bhResultat.getKonsekvenserForYtelsen()).containsExactly(KonsekvensForYtelsen.INGEN_ENDRING);
        assertThat(bhResultat.getVedtaksbrev()).isEqualTo(Vedtaksbrev.INGEN);
        assertThat(uendretUtfall).isTrue();
    }

    @Test
    public void skal_gi_ingen_endring_i_beregningsgrunnlag_ved_lik_dagsats_på_periodenoivå() {
        // Arrange
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        Beregningsgrunnlag originalGrunnlag = byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, true, bgPeriode);
        Beregningsgrunnlag revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(revurdering, false, true, bgPeriode);

        // Act
        boolean endring = ErEndringIBeregning.vurder(Optional.of(revurderingGrunnlag), Optional.of(originalGrunnlag));

        // Assert
        assertThat(endring).isFalse();
    }

    @Test
    public void skal_gi_endring_når_vi_mangler_beregningsgrunnlag_på_en_av_behandlingene() {
        // Arrange
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        Beregningsgrunnlag revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(revurdering, false, true, bgPeriode);

        // Act
        boolean endring = ErEndringIBeregning.vurder(Optional.of(revurderingGrunnlag), Optional.empty());

        // Assert
        assertThat(endring).isTrue();
    }

    @Test
    public void skal_gi_ingen_endring_når_vi_mangler_begge_beregningsgrunnlag() {
        // Act
        boolean endring = ErEndringIBeregning.vurder(Optional.empty(), Optional.empty());

        // Assert
        assertThat(endring).isFalse();
    }

    @Test
    public void skal_gi_ingen_endring_når_vi_har_like_mange_perioder_med_med_forskjellige_fom_og_tom() {
        // Arrange
        List<ÅpenDatoIntervallEntitet> bgPerioderNyttGrunnlag = Arrays.asList(
            ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(35)),
            ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(36), null));
        List<ÅpenDatoIntervallEntitet> bgPerioderOriginaltGrunnlag = Arrays.asList(
            ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(40)),
            ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(41), null));
        Beregningsgrunnlag originalGrunnlag = byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, true, bgPerioderOriginaltGrunnlag);
        Beregningsgrunnlag revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(revurdering, false, true, bgPerioderNyttGrunnlag);

        // Act
        boolean endring = ErEndringIBeregning.vurder(Optional.of(revurderingGrunnlag), Optional.of(originalGrunnlag));

        // Assert
        assertThat(endring).isFalse();
    }

    @Test
    public void skal_gi_ingen_endring_når_vi_har_like_mange_perioder_med_forskjellig_startdato() {
        // Arrange
        List<ÅpenDatoIntervallEntitet> bgPerioderNyttGrunnlag = Arrays.asList(
            ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1), SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(35)),
            ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(36), null));
        List<ÅpenDatoIntervallEntitet> bgPerioderOriginaltGrunnlag = Arrays.asList(
            ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(40)),
            ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(41), null));
        Beregningsgrunnlag originalGrunnlag = byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, true, bgPerioderOriginaltGrunnlag);
        Beregningsgrunnlag revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(revurdering, false, true, bgPerioderNyttGrunnlag);

        // Act
        boolean endring = ErEndringIBeregning.vurder(Optional.of(revurderingGrunnlag), Optional.of(originalGrunnlag));

        // Assert
        assertThat(endring).isFalse();
    }

    @Test
    public void skal_gi_endring_i_beregningsgrunnlag_ved_ulik_dagsats_på_periodenoivå() {
        // Arrange
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        Beregningsgrunnlag originalGrunnlag = byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, true, bgPeriode);
        Beregningsgrunnlag revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(revurdering, true, true, bgPeriode);

        // Act
        boolean endring = ErEndringIBeregning.vurder(Optional.of(revurderingGrunnlag), Optional.of(originalGrunnlag));

        // Assert
        assertThat(endring).isTrue();
    }

    @Test
    public void skal_teste_at_alle_inngangsvilkår_oppfylt_gir_positivt_utfall() {
        // Arrange
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);

        // Act
        boolean oppfyllerIkkjeInngangsvilkår = OppfyllerIkkjeInngangsvilkårPåSkjæringstidspunkt.vurder(revurdering);

        // Assert
        assertThat(oppfyllerIkkjeInngangsvilkår).isFalse();
    }

    @Test
    public void skal_teste_at_inngangsvilkår_ikke_oppfylt_gir_negativt_utfall() {
        // Arrange
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.IKKE_OPPFYLT)
            .buildFor(revurdering);

        // Act
        boolean oppfyllerIkkjeInngangsvilkår = OppfyllerIkkjeInngangsvilkårPåSkjæringstidspunkt.vurder(revurdering);

        // Assert
        assertThat(oppfyllerIkkjeInngangsvilkår).isTrue();
    }

    @Test
    public void skal_teste_at_inngangsvilkår_ikke_vurdert_gir_negativt_utfall() {
        // Arrange
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.IKKE_VURDERT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);

        // Act
        boolean oppfyllerIkkjeInngangsvilkår = OppfyllerIkkjeInngangsvilkårPåSkjæringstidspunkt.vurder(revurdering);

        // Assert
        assertThat(oppfyllerIkkjeInngangsvilkår).isTrue();
    }

    @Test
    public void skal_teste_negativ_medlemsskapsvilkår_gir_negativt_resultat() {
        // Arrange
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_OPPFYLT)
            .buildFor(revurdering);

        // Act
        boolean oppfyllerIkkjeInngangsvilkår = OppfyllerIkkjeInngangsvilkårPåSkjæringstidspunkt.vurder(revurdering);

        // Assert
        assertThat(oppfyllerIkkjeInngangsvilkår).isTrue();
    }

    @Test
    public void skal_teste_negativ_medlemsskapsvilkår_etter_stp_gir_negativt_resultat() {
        // Arrange
        LocalDate endringsdato = SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2);
        settVilkårutfallMedlemskapPåDato(VilkårUtfallType.IKKE_OPPFYLT, endringsdato);

        // Act
        Optional<MedlemskapVilkårPeriodeGrunnlag> grunnlagOpt = medlemskapVilkårPeriodeRepository.hentAggregatHvisEksisterer(revurdering);
        boolean oppfyllerIkkjeInngangsvilkår = OppfyllerIkkjeInngangsvilkårIPerioden.vurder(grunnlagOpt, endringsdato);

        // Assert
        assertThat(oppfyllerIkkjeInngangsvilkår).isTrue();
    }

    private VilkårResultat settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType utfall) {
        return settVilkårutfallMedlemskapPåDato(utfall, SKJÆRINGSTIDSPUNKT_BEREGNING);
    }

    private VilkårResultat settVilkårutfallMedlemskapPåDato(VilkårUtfallType utfall, LocalDate endringsdato) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        VilkårResultat vilkårResultat = repositoryProvider.getBehandlingVedtakRepository().hentBehandlingvedtakForBehandlingId(revurdering.getId())
            .stream().map(vedtak -> vedtak.getBehandlingsresultat().getVilkårResultat()).findFirst()
            .orElse(VilkårResultat.builder().buildFor(revurdering));
        behandlingRepository.lagre(vilkårResultat, lås);

        MedlemskapVilkårPeriodeGrunnlagEntitet.Builder builder = medlemskapVilkårPeriodeRepository.hentBuilderFor(revurdering);
        MedlemskapsvilkårPeriodeEntitet.Builder periodeBuilder = builder.getPeriodeBuilder();
        MedlemskapsvilkårPerioderEntitet.Builder periode = periodeBuilder.getBuilderForVurderingsdato(endringsdato);
        periode.medVilkårUtfall(utfall);
        periodeBuilder.leggTil(periode);
        builder.medMedlemskapsvilkårPeriode(periodeBuilder);
        medlemskapVilkårPeriodeRepository.lagreMedlemskapsvilkår(revurdering, builder);

        return vilkårResultat;
    }

    @Test
    public void skal_teste_positivt_medlemsskapsvilkår_gir_positivt_resultat() {
        // Arrange
        LocalDate endringsdato = SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2);
        settVilkårutfallMedlemskapPåDato(VilkårUtfallType.OPPFYLT, endringsdato);

        // Act
        Optional<MedlemskapVilkårPeriodeGrunnlag> grunnlagOpt = medlemskapVilkårPeriodeRepository.hentAggregatHvisEksisterer(revurdering);
        boolean oppfyllerIkkjeInngangsvilkår = OppfyllerIkkjeInngangsvilkårIPerioden.vurder(grunnlagOpt, endringsdato);

        // Assert
        assertThat(oppfyllerIkkjeInngangsvilkår).isFalse();
    }

    @Test
    public void skal_teste_at_andre_vilkår_ikke_påviker_medlemsskapsvilkårsjekk() {
        // Arrange
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.IKKE_VURDERT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);
        LocalDate endringsdato = SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2);
        settVilkårutfallMedlemskapPåDato(VilkårUtfallType.OPPFYLT, endringsdato);

        // Act
        Optional<MedlemskapVilkårPeriodeGrunnlag> grunnlagOpt = medlemskapVilkårPeriodeRepository.hentAggregatHvisEksisterer(revurdering);
        boolean oppfyllerIkkjeInngangsvilkår = OppfyllerIkkjeInngangsvilkårIPerioden.vurder(grunnlagOpt, endringsdato);

        // Assert
        assertThat(oppfyllerIkkjeInngangsvilkår).isFalse();
    }

    private void lagBeregningsresultatperiodeMedEndringstidspunkt(LocalDate endringsdato) {
        BeregningsresultatFP brFPOriginal = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();

        BeregningsresultatPeriode originalPeriode = BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(LocalDate.now().plusDays(1), LocalDate.now().plusDays(10))
            .build(brFPOriginal);

        buildBeregningsresultatAndel(originalPeriode, true, 1500);
        buildBeregningsresultatAndel(originalPeriode, false, 500);

        BeregningsresultatFP nyttResultat = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();

        BeregningsresultatPeriode nyPeriode = BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(endringsdato, LocalDate.now().plusDays(10))
            .build(nyttResultat);

        buildBeregningsresultatAndel(nyPeriode, true, 1500);
        buildBeregningsresultatAndel(nyPeriode, false, 500);

        beregningsresultatFPRepository.lagre(revurdering, nyttResultat);
        beregningsresultatFPRepository.lagre(behandlingSomSkalRevurderes, brFPOriginal);
    }

    private void buildBeregningsresultatAndel(BeregningsresultatPeriode beregningsresultatPeriode, Boolean brukerErMottaker, int dagsats) {
        BeregningsresultatAndel.builder()
            .medBrukerErMottaker(brukerErMottaker)
            .medVirksomhet(this.virksomhet)
            .medDagsats(dagsats)
            .medDagsatsFraBg(dagsats)
            .medStillingsprosent(BigDecimal.valueOf(100))
            .medUtbetalingsgrad(BigDecimal.valueOf(100))
            .medAktivitetstatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.UDEFINERT)
            .build(beregningsresultatPeriode);
    }

    private Beregningsgrunnlag byggBeregningsgrunnlagForBehandling(Behandling behandling, boolean medOppjustertDagsat,
                                                                   boolean skalDeleAndelMellomArbeidsgiverOgBruker, List<ÅpenDatoIntervallEntitet> perioder) {
        return byggBeregningsgrunnlagForBehandling(behandling, medOppjustertDagsat, skalDeleAndelMellomArbeidsgiverOgBruker, perioder,
            new LagEnAndelTjeneste());
    }

    private Beregningsgrunnlag byggBeregningsgrunnlagForBehandling(Behandling behandling, boolean medOppjustertDagsat,
                                                                   boolean skalDeleAndelMellomArbeidsgiverOgBruker, List<ÅpenDatoIntervallEntitet> perioder,
                                                                   LagAndelTjeneste lagAndelTjeneste) {

        beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(BigDecimal.valueOf(91425L))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(91425L))
            .build();
        BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(beregningsgrunnlag);
        for (ÅpenDatoIntervallEntitet datoPeriode : perioder) {
            BeregningsgrunnlagPeriode periode = byggBGPeriode(datoPeriode, medOppjustertDagsat, skalDeleAndelMellomArbeidsgiverOgBruker, lagAndelTjeneste);
            BeregningsgrunnlagPeriode.builder(periode)
                .build(beregningsgrunnlag);
        }
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagPeriode byggBGPeriode(ÅpenDatoIntervallEntitet datoPeriode, boolean medOppjustertDagsat,
                                                    boolean skalDeleAndelMellomArbeidsgiverOgBruker, LagAndelTjeneste lagAndelTjeneste) {
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(datoPeriode.getFomDato(), datoPeriode.getTomDato())
            .build(beregningsgrunnlag);
        lagAndelTjeneste.lagAndeler(periode, medOppjustertDagsat, skalDeleAndelMellomArbeidsgiverOgBruker);
        return periode;

    }

}
