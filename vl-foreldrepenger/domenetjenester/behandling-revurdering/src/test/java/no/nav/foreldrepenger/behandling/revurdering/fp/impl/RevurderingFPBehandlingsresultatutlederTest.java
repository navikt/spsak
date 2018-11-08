package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingEndring;
import no.nav.foreldrepenger.behandling.revurdering.testutil.BeregningRevurderingTestUtil;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
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
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPeriodeGrunnlag;
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
import no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak;
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
import no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl.EndringsdatoRevurderingUtleder;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(CdiRunner.class)
public class RevurderingFPBehandlingsresultatutlederTest {
    private static final String ARBEIDSFORHOLD_ID = "987123987";
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.now();
    static final List<String> ARBEIDSFORHOLDLISTE = Arrays.asList("154", "265", "386", "412");
    static final BigDecimal TOTAL_ANDEL_NORMAL = BigDecimal.valueOf(300000);
    static final BigDecimal TOTAL_ANDEL_OPPJUSTERT = BigDecimal.valueOf(350000);

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private BeregningRevurderingTestUtil revurderingTestUtil;
    @Inject @FagsakYtelseTypeRef("FP")
    private RevurderingEndring revurderingEndring;

    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private BehandlingRepository behandlingRepository;
    private HistorikkRepository historikkRepository;
    private RevurderingTjeneste revurderingTjeneste;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private UttakRepository uttakRepository;
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
        uttakRepository = repositoryProvider.getUttakRepository();
        beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
        historikkRepository = repositoryProvider.getHistorikkRepository();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE, BehandlingStegType.KONTROLLER_FAKTA);
        behandlingSomSkalRevurderes = scenario.lagre(repositoryProvider);
        revurderingTestUtil.avsluttBehandling(behandlingSomSkalRevurderes);
        revurderingFPBehandlingsresultatutleder = new RevurderingFPBehandlingsresultatutleder(repositoryProvider, endringsdatoRevurderingUtleder);
        BehandlingModellRepository mock = mock(BehandlingModellRepository.class);
        when(mock.getModell(any(), any())).thenReturn(mock(BehandlingModell.class));
        BehandlingskontrollTjenesteImpl behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider,
            mock, mock(BehandlingskontrollEventPubliserer.class));
        revurderingTjeneste = new RevurderingFPTjenesteImpl(repositoryProvider, behandlingskontrollTjeneste, historikkRepository, revurderingEndring);
        revurdering = revurderingTjeneste
            .opprettAutomatiskRevurdering(behandlingSomSkalRevurderes.getFagsak(), BehandlingÅrsakType.RE_HENDELSE_FØDSEL);
        virksomhet = new VirksomhetEntitet.Builder().medOrgnr(ARBEIDSFORHOLD_ID).medNavn("Virksomheten").oppdatertOpplysningerNå().build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);
        endringsdato = LocalDate.now().minusMonths(3);
        when(endringsdatoRevurderingUtleder.utledEndringsdato(any(Behandling.class))).thenReturn(endringsdato);
    }


    // Case 2
    // Løpende vedtak: Ja
    // Oppfylt inngangsvilkår på skjæringstidspunktet
    // Ikkje oppfylt inngangsvilkår i perioden
    // Endring i uttaksperiode: Ja
    @Test
    public void tilfelle_2_behandlingsresultat_lik_opphør_rettentil_lik_nei_foreldrepenger_opphører() {

        // Arrange
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        // Endring i uttakperiode (ulik lengde)
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(endringsdato.minusDays(10), endringsdato.plusDays(5)));
        List<LocalDateInterval> revurderingPerioder = Arrays.asList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10)),
            new LocalDateInterval(endringsdato.plusDays(11), endringsdato.plusDays(20)));

        // Løpende vedtak
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12),
            Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );
        lagUttakResultatPlanForBehandling(revurdering,
            revurderingPerioder, StønadskontoType.FEDREKVOTE);

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
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


    // Case 3
    // Løpende vedtak: Ja
    // Oppfylt inngangsvilkår på skjæringstidspunktet
    // Oppfylt inngangsvilkår i perioden
    // Siste uttaksperiode avslått med opphørsårsak
    // Endring i uttaksperiode: Ja
    @Test
    public void tilfelle_3_behandlingsresultat_lik_opphør_rettentil_lik_nei_foreldrepenger_opphører() {

        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        // Endring i uttakperiode (ulik lengde)
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(endringsdato.minusDays(10), endringsdato.plusDays(5)));
        List<LocalDateInterval> revurderingPerioder = Arrays.asList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10)),
            new LocalDateInterval(endringsdato.plusDays(11), endringsdato.plusDays(20)));

        // Løpende vedtak og endring i uttak
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Siste periode avslått med opphørsårsak og endring
        lagUttakResultatPlanForBehandling(revurdering,
            revurderingPerioder,
            Collections.nCopies(revurderingPerioder.size(), false),
            Arrays.asList(PeriodeResultatType.INNVILGET, PeriodeResultatType.AVSLÅTT),
            Arrays.asList(PeriodeResultatÅrsak.UKJENT, IkkeOppfyltÅrsak.BARNET_ER_DØD),
            Collections.nCopies(revurderingPerioder.size(), true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);

        // Oppfylt inngangsvilkår i perioden (medlemskap)
        settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType.OPPFYLT);


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

    // Case 4
    // Løpende vedtak: Nei
    // Oppfylt inngangsvilkår på skjæringstidspunktet
    // Oppfylt inngangsvilkår i perioden
    // Siste uttaksperiode IKKJE avslått med opphørsårsak
    // Endring i uttaksperiode: Ja
    @Test
    public void tilfelle_4_med_endring_i_uttak_behandlingsresultat_lik_innvilget_rettentil_lik_ja_konsekvens_endring_i_uttak() {

        // Arrange
        LocalDate endringsdato = LocalDate.of(2018, 1, 1);
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);
        when(endringsdatoRevurderingUtleder.utledEndringsdato(any(Behandling.class))).thenReturn(endringsdato);

        // Endring i uttakperiode (ulik lengde)
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(endringsdato.minusDays(10), endringsdato.minusDays(5)));
        List<LocalDateInterval> revurderingPerioder = Arrays.asList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10)),
            new LocalDateInterval(endringsdato.plusDays(11), endringsdato.plusDays(20)));

        // Løpende vedtak og endring i uttak
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Siste periode ikkje avslått med opphørsårsak
        lagUttakResultatPlanForBehandling(revurdering,
            revurderingPerioder,
            Collections.nCopies(revurderingPerioder.size(), false),
            Arrays.asList(PeriodeResultatType.INNVILGET, PeriodeResultatType.INNVILGET),
            Arrays.asList(PeriodeResultatÅrsak.UKJENT, IkkeOppfyltÅrsak.UKJENT),
            Collections.nCopies(revurderingPerioder.size(), true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);

        // Oppfylt inngangsvilkår i perioden (medlemskap)
        settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType.OPPFYLT);


        // Act
        revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(revurdering, erVarselOmRevurderingSendt);
        Behandlingsresultat bhResultat = revurdering.getBehandlingsresultat();
        boolean uendretUtfall = revurderingTjeneste.erRevurderingMedUendretUtfall(revurdering);

        // Assert
        assertThat(bhResultat.getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.INNVILGET);
        assertThat(bhResultat.getRettenTil()).isEqualByComparingTo(RettenTil.HAR_RETT_TIL_FP);
        assertThat(bhResultat.getKonsekvenserForYtelsen()).containsExactly(KonsekvensForYtelsen.ENDRING_I_UTTAK);
        assertThat(uendretUtfall).isFalse();
    }

    // Case 4
    // Løpende vedtak: Nei
    // Oppfylt inngangsvilkår på skjæringstidspunktet
    // Oppfylt inngangsvilkår i perioden
    // Siste uttaksperiode IKKJE avslått med opphørsårsak
    // Endring i uttaksperiode: Ja
    // Endring i beregning: Ja
    @Test
    public void tilfelle_4_med_endring_i_uttak_og_beregning_behandlingsresultat_lik_innvilget_rettentil_lik_ja_konsekvens_endring_i_uttak_og_endring_i_beregning() {

        // Arrange
        LocalDate endringsdato = LocalDate.of(2018, 1,1);
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);
        when(endringsdatoRevurderingUtleder.utledEndringsdato(any(Behandling.class))).thenReturn(endringsdato);

        // Endring i uttakperiode (ulik lengde)
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(endringsdato.minusDays(10), endringsdato.minusDays(5)));
        List<LocalDateInterval> revurderingPerioder = Arrays.asList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10)),
            new LocalDateInterval(endringsdato.plusDays(11), endringsdato.plusDays(20)));

        // Løpende vedtak og endring i uttak
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Endring i beregning
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, false, bgPeriode);
        byggBeregningsgrunnlagForBehandling(revurdering, true, false, bgPeriode);

        // Siste periode ikkje avslått med opphørsårsak
        lagUttakResultatPlanForBehandling(revurdering,
            revurderingPerioder,
            Collections.nCopies(revurderingPerioder.size(), false),
            Arrays.asList(PeriodeResultatType.INNVILGET, PeriodeResultatType.INNVILGET),
            Arrays.asList(PeriodeResultatÅrsak.UKJENT, IkkeOppfyltÅrsak.UKJENT),
            Collections.nCopies(revurderingPerioder.size(), true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);

        // Oppfylt inngangsvilkår i perioden (medlemskap)
        settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType.OPPFYLT);


        // Act
        revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(revurdering, erVarselOmRevurderingSendt);
        Behandlingsresultat bhResultat = revurdering.getBehandlingsresultat();
        boolean uendretUtfall = revurderingTjeneste.erRevurderingMedUendretUtfall(revurdering);

        // Assert
        assertThat(bhResultat.getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.INNVILGET);
        assertThat(bhResultat.getRettenTil()).isEqualByComparingTo(RettenTil.HAR_RETT_TIL_FP);
        assertThat(bhResultat.getKonsekvenserForYtelsen()).containsExactly(KonsekvensForYtelsen.ENDRING_I_BEREGNING, KonsekvensForYtelsen.ENDRING_I_UTTAK);
        assertThat(uendretUtfall).isFalse();
    }


    // Case 5
    // Løpende vedtak: Ja
    // Oppfylt inngangsvilkår på skjæringstidspunktet
    // Oppfylt inngangsvilkår i perioden
    // Siste uttaksperiode IKKJE avslått med opphørsårsak
    // Endring i beregning
    // Endring i uttaksperiode: Nei
    @Test
    public void tilfelle_5_behandlingsresultat_lik_FPEndret_rettentil_lik_ja_foreldrepenger_konsekvens_Endring_i_beregning() {

        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(endringsdato.minusDays(10), endringsdato.plusDays(5)));

        // Løpende vedtak
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Like perioder, siste periode ikkje avslått
        lagUttakResultatPlanForBehandling(revurdering,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat vilkårResultat = VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);
        behandlingRepository.lagre(vilkårResultat, lås);

        // Oppfylt inngangsvilkår i perioden (medlemskap)
        vilkårResultat = settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType.OPPFYLT);

        // Endring i beregning
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, false, bgPeriode);
        byggBeregningsgrunnlagForBehandling(revurdering, true, false, bgPeriode);

        // Act
        revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(revurdering, erVarselOmRevurderingSendt);
        Behandlingsresultat bhResultat = revurdering.getBehandlingsresultat();
        boolean uendretUtfall = revurderingTjeneste.erRevurderingMedUendretUtfall(revurdering);

        // Assert
        assertThat(bhResultat.getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.FORELDREPENGER_ENDRET);
        assertThat(bhResultat.getRettenTil()).isEqualByComparingTo(RettenTil.HAR_RETT_TIL_FP);
        assertThat(bhResultat.getKonsekvenserForYtelsen()).containsExactly(KonsekvensForYtelsen.ENDRING_I_BEREGNING);
        assertThat(uendretUtfall).isFalse();
    }

    // Case 6
    // Løpende vedtak: Ja
    // Oppfylt inngangsvilkår på skjæringstidspunktet
    // Oppfylt inngangsvilkår i perioden
    // Siste uttaksperiode IKKJE avslått med opphørsårsak
    // Endring i beregning
    // Endring i uttaksperiode: Ja
    @Test
    public void tilfelle_6_behandlingsresultat_lik_FPEndret_rettentil_lik_ja_foreldrepenger_konsekvens_endring_i_beregning_og_uttak() {

        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(5)));

        // Løpende vedtak
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Endring i periode, siste periode ikkje avslått
        lagUttakResultatPlanForBehandling(revurdering,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(50), Collections.singletonList(100),
            Collections.singletonList(10), Collections.singletonList(StønadskontoType.FELLESPERIODE)
        );

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat vilkårResultat = VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);
        behandlingRepository.lagre(vilkårResultat, lås);


        // Oppfylt inngangsvilkår i perioden (medlemskap)
        vilkårResultat = settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType.OPPFYLT);

        behandlingRepository.lagre(vilkårResultat, lås);

        // Endring i beregning
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, false, bgPeriode);
        byggBeregningsgrunnlagForBehandling(revurdering, true, false, bgPeriode);

        // Act
        revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(revurdering, erVarselOmRevurderingSendt);
        Behandlingsresultat bhResultat = revurdering.getBehandlingsresultat();
        boolean uendretUtfall = revurderingTjeneste.erRevurderingMedUendretUtfall(revurdering);

        // Assert
        assertThat(bhResultat.getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.FORELDREPENGER_ENDRET);
        assertThat(bhResultat.getRettenTil()).isEqualByComparingTo(RettenTil.HAR_RETT_TIL_FP);
        assertThat(bhResultat.getKonsekvenserForYtelsen()).containsExactly(KonsekvensForYtelsen.ENDRING_I_BEREGNING, KonsekvensForYtelsen.ENDRING_I_UTTAK);
        assertThat(uendretUtfall).isFalse();
    }

    // Case 7
    // Løpende vedtak: Ja
    // Oppfylt inngangsvilkår på skjæringstidspunktet
    // Oppfylt inngangsvilkår i perioden
    // Siste uttaksperiode IKKJE avslått med opphørsårsak
    // Endring i beregning: Nei
    // Endring i uttaksperiode: Ja
    @Test
    public void tilfelle_7_behandlingsresultat_lik_FPEndret_rettentil_lik_ja_foreldrepenger_konsekven_endring_i_uttak() {

        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(5)));

        // Løpende vedtak
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Endring i periode, siste periode ikkje avslått
        lagUttakResultatPlanForBehandling(revurdering,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(50), Collections.singletonList(100),
            Collections.singletonList(10), Collections.singletonList(StønadskontoType.FELLESPERIODE)
        );

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat vilkårResultat = VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);
        behandlingRepository.lagre(vilkårResultat, lås);


        // Oppfylt inngangsvilkår i perioden (medlemskap)
        settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType.OPPFYLT);

        // Endring i beregning
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, false, bgPeriode);
        byggBeregningsgrunnlagForBehandling(revurdering, false, false, bgPeriode);

        // Act
        revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(revurdering, erVarselOmRevurderingSendt);
        Behandlingsresultat bhResultat = revurdering.getBehandlingsresultat();
        boolean uendretUtfall = revurderingTjeneste.erRevurderingMedUendretUtfall(revurdering);

        // Assert
        assertThat(bhResultat.getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.FORELDREPENGER_ENDRET);
        assertThat(bhResultat.getRettenTil()).isEqualByComparingTo(RettenTil.HAR_RETT_TIL_FP);
        assertThat(bhResultat.getKonsekvenserForYtelsen()).containsExactly(KonsekvensForYtelsen.ENDRING_I_UTTAK);
        assertThat(uendretUtfall).isFalse();
    }

    // Case 8
    // Løpende vedtak: Ja
    // Oppfylt inngangsvilkår på skjæringstidspunktet
    // Oppfylt inngangsvilkår i perioden
    // Siste uttaksperiode IKKJE avslått med opphørsårsak
    // Endring i beregning: kun endring i fordeling av ytelsen
    // Endring i uttaksperiode: Nei
    @Test
    public void tilfelle_8_behandlingsresultat_lik_FPEndret_rettentil_lik_ja_foreldrepenger_konsekven_endring_i_fordeling_av_ytelsen() {

        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(5)));

        // Løpende vedtak
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Ingen Endring i periode, siste periode ikkje avslått
        lagUttakResultatPlanForBehandling(revurdering,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat vilkårResultat = VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);
        behandlingRepository.lagre(vilkårResultat, lås);


        // Oppfylt inngangsvilkår i perioden (medlemskap)
        settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType.OPPFYLT);

        // Endring i beregning: kun endring i fordeling av ytelsen
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, false, bgPeriode);
        byggBeregningsgrunnlagForBehandling(revurdering, false, true, bgPeriode);

        // Act
        revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(revurdering, erVarselOmRevurderingSendt);
        Behandlingsresultat bhResultat = revurdering.getBehandlingsresultat();
        boolean uendretUtfall = revurderingTjeneste.erRevurderingMedUendretUtfall(revurdering);

        // Assert
        assertThat(bhResultat.getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.FORELDREPENGER_ENDRET);
        assertThat(bhResultat.getRettenTil()).isEqualByComparingTo(RettenTil.HAR_RETT_TIL_FP);
        assertThat(bhResultat.getKonsekvenserForYtelsen()).containsExactly(KonsekvensForYtelsen.ENDRING_I_FORDELING_AV_YTELSEN);
        assertThat(uendretUtfall).isFalse();
    }

    // Case 9
    // Løpende vedtak: Ja
    // Oppfylt inngangsvilkår på skjæringstidspunktet
    // Oppfylt inngangsvilkår i perioden
    // Siste uttaksperiode IKKJE avslått med opphørsårsak
    // Endring i beregning: Nei
    // Endring i uttaksperiode: Nei
    @Test
    public void tilfelle_9_behandlingsresultat_lik_ingenEndring_rettentil_lik_ja_foreldrepenger_konsekvens_ingenEndring() {

        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(5)));

        // Løpende vedtak
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Ingen Endring i periode, siste periode ikkje avslått
        lagUttakResultatPlanForBehandling(revurdering,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat vilkårResultat = VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
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

    // Case 9
    // Løpende vedtak: Ja
    // Oppfylt inngangsvilkår på skjæringstidspunktet
    // Oppfylt inngangsvilkår i perioden
    // Siste uttaksperiode IKKJE avslått med opphørsårsak
    // Endring i beregning: Nei (endring i rekkefølge av andeler, men ikkje endring i fordeling)
    // Endring i uttaksperiode: Nei
    @Test
    public void tilfelle_9_ulik_rekkefølge_av_andeler_behandlingsresultat_lik_ingenEndring_rettentil_lik_ja_foreldrepenger_konsekvens_ingenEndring() {

        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(5)));

        // Løpende vedtak
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Ingen Endring i periode, siste periode ikkje avslått
        lagUttakResultatPlanForBehandling(revurdering,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat vilkårResultat = VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);
        behandlingRepository.lagre(vilkårResultat, lås);


        // Oppfylt inngangsvilkår i perioden (medlemskap)
        settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType.OPPFYLT);


        // Endring i beregning: Ingen endring, kun endring i rekkefølge av andeler
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, true, bgPeriode, new LagToAndelerTjeneste());
        byggBeregningsgrunnlagForBehandling(revurdering, false, true, bgPeriode, new LagToAndelerMotsattRekkefølgeTjeneste());

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

        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(5)));

        // Løpende vedtak
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Ingen Endring i periode, siste periode ikkje avslått
        lagUttakResultatPlanForBehandling(revurdering,
            opprinneligePerioder,
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT),
            Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat vilkårResultat = VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
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
        assertThat(uendretUtfall).isTrue();    }



    @Test
    public void skal_gi_ingen_endring_når_original_revurdering_også_hadde_avslått_siste_uttaksperiode() {

        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        // Uttaksperiode som brukes for begge behandlinger
        List<LocalDateInterval> revurderingPerioder = Arrays.asList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10)),
            new LocalDateInterval(endringsdato.plusDays(11), endringsdato.plusDays(20)));

        // Siste periode avslått med opphørsårsak for original behandling
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            revurderingPerioder,
            Collections.nCopies(revurderingPerioder.size(), false),
            Arrays.asList(PeriodeResultatType.INNVILGET, PeriodeResultatType.AVSLÅTT),
            Arrays.asList(PeriodeResultatÅrsak.UKJENT, IkkeOppfyltÅrsak.BARNET_ER_DØD),
            Collections.nCopies(revurderingPerioder.size(), true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Siste periode avslått med opphørsårsak for revurdering
        lagUttakResultatPlanForBehandling(revurdering,
            revurderingPerioder,
            Collections.nCopies(revurderingPerioder.size(), false),
            Arrays.asList(PeriodeResultatType.INNVILGET, PeriodeResultatType.AVSLÅTT),
            Arrays.asList(PeriodeResultatÅrsak.UKJENT, IkkeOppfyltÅrsak.BARNET_ER_DØD),
            Collections.nCopies(revurderingPerioder.size(), true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);

        // Oppfylt inngangsvilkår i perioden (medlemskap)
                settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType.OPPFYLT);


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
    public void skal_gi_opphør_når_det_er_flere_perioder_som_avslås() {

        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        // Uttaksperiode som brukes for begge behandlinger
        List<LocalDateInterval> revurderingPerioder = Arrays.asList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10)),
            new LocalDateInterval(endringsdato.plusDays(11), endringsdato.plusDays(20)));

        // Siste periode avslått med opphørsårsak for original behandling
        lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            revurderingPerioder,
            Collections.nCopies(revurderingPerioder.size(), false),
            Arrays.asList(PeriodeResultatType.INNVILGET, PeriodeResultatType.AVSLÅTT),
            Arrays.asList(PeriodeResultatÅrsak.UKJENT, IkkeOppfyltÅrsak.BARNET_ER_DØD),
            Collections.nCopies(revurderingPerioder.size(), true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Alle perioder avslått med opphørsårsak for revurdering
        lagUttakResultatPlanForBehandling(revurdering,
            revurderingPerioder,
            Collections.nCopies(revurderingPerioder.size(), false),
            Arrays.asList(PeriodeResultatType.AVSLÅTT, PeriodeResultatType.AVSLÅTT),
            Arrays.asList(IkkeOppfyltÅrsak.BARNET_ER_DØD, IkkeOppfyltÅrsak.BARNET_ER_DØD),
            Collections.nCopies(revurderingPerioder.size(), true), Collections.singletonList(100), Collections.singletonList(100),
            Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Oppfylt inngangsvilkår på skjæringstidspunkt
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);

        // Oppfylt inngangsvilkår i perioden (medlemskap)
        settVilkårutfallMedlemskapPåSkjæringstidspunkt(VilkårUtfallType.OPPFYLT);


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


    @Test
    public void skal_gi_endring_i_ytelse_ved_forskjellig_fordeling_av_andeler() {
        // Arrange
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        Beregningsgrunnlag originalGrunnlag = byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, false, bgPeriode);
        Beregningsgrunnlag revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(revurdering, false, true, bgPeriode);

        // Act
        boolean endring = ErKunEndringIFordelingAvYtelsen.vurder(false, false, Optional.of(revurderingGrunnlag), Optional.of(originalGrunnlag));

        // Assert
        assertThat(endring).isTrue();
    }

    @Test
    public void skal_gi_ingen_endring_i_ytelse_ved_lik_fordeling_av_andeler() {
        // Arrange
        List<ÅpenDatoIntervallEntitet> bgPeriode = Collections.singletonList(ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        Beregningsgrunnlag originalGrunnlag = byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, false, bgPeriode);
        Beregningsgrunnlag revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(revurdering, false, false, bgPeriode);

        // Act
        boolean endring = ErKunEndringIFordelingAvYtelsen.vurder(false, false, Optional.of(revurderingGrunnlag), Optional.of(originalGrunnlag));

        // Assert
        assertThat(endring).isFalse();
    }


    @Test
    public void skal_gi_ingen_endring_i_ytelse_ved_lik_fordeling_av_andeler_ved_ulike_perioder() {
        // Arrange
        List<Integer> dagsatser = Arrays.asList(123, 5781, 5781);

        List<LocalDateInterval> originaleBGPerioder = new ArrayList<>();
        originaleBGPerioder.add(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10)));
        originaleBGPerioder.add(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(11), SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(20)));
        originaleBGPerioder.add(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(21), null));

        List<LocalDateInterval> revurderingBGPerioder = new ArrayList<>();
        revurderingBGPerioder.add(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10)));
        revurderingBGPerioder.add(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(11), SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(15)));
        revurderingBGPerioder.add(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(16), null));

        Beregningsgrunnlag originalGrunnlag = byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, originaleBGPerioder, dagsatser);
        Beregningsgrunnlag revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(revurdering, false, revurderingBGPerioder, dagsatser);

        // Act
        boolean endring = ErKunEndringIFordelingAvYtelsen.vurder(false, false, Optional.of(revurderingGrunnlag), Optional.of(originalGrunnlag));

        // Assert
        assertThat(endring).isFalse();
    }

    @Test
    public void skal_gi_endring_i_ytelse_ved_ulik_fordeling_av_andeler_ved_ulike_perioder() {
        // Arrange
        List<Integer> dagsatser = Arrays.asList(123, 5781, 5781);
        List<Integer> dagsatserRevurdering = Arrays.asList(123, 3183, 5781);


        List<LocalDateInterval> originaleBGPerioder = new ArrayList<>();
        originaleBGPerioder.add(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10)));
        originaleBGPerioder.add(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10), SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(20)));
        originaleBGPerioder.add(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(20), null));

        List<LocalDateInterval> revurderingBGPerioder = new ArrayList<>();
        revurderingBGPerioder.add(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10)));
        revurderingBGPerioder.add(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10), SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(15)));
        revurderingBGPerioder.add(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(15), null));

        Beregningsgrunnlag originalGrunnlag = byggBeregningsgrunnlagForBehandling(behandlingSomSkalRevurderes, false, originaleBGPerioder, dagsatser);
        Beregningsgrunnlag revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(revurdering, false, revurderingBGPerioder, dagsatserRevurdering);

        // Act
        boolean endring = ErKunEndringIFordelingAvYtelsen.vurder(false, false, Optional.of(revurderingGrunnlag), Optional.of(originalGrunnlag));

        // Assert
        assertThat(endring).isTrue();
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
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
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
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
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
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
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
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
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
        Optional<MedlemskapsvilkårPeriodeGrunnlag> grunnlagOpt = medlemskapVilkårPeriodeRepository.hentAggregatHvisEksisterer(revurdering);
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

        MedlemskapsvilkårPeriodeGrunnlag.Builder builder = medlemskapVilkårPeriodeRepository.hentBuilderFor(revurdering);
        MedlemskapsvilkårPerioderEntitet.Builder periode = builder.getBuilderForVurderingsdato(endringsdato);
        periode.medVilkårUtfall(utfall);
        builder.leggTilMedlemskapsvilkårPeriode(periode);
        medlemskapVilkårPeriodeRepository.lagreMedlemskapsvilkår(revurdering, builder);

        return vilkårResultat;
    }

    @Test
    public void skal_teste_positivt_medlemsskapsvilkår_gir_positivt_resultat() {
        // Arrange
        LocalDate endringsdato = SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2);
        settVilkårutfallMedlemskapPåDato(VilkårUtfallType.OPPFYLT, endringsdato);

        // Act
        Optional<MedlemskapsvilkårPeriodeGrunnlag> grunnlagOpt = medlemskapVilkårPeriodeRepository.hentAggregatHvisEksisterer(revurdering);
        boolean oppfyllerIkkjeInngangsvilkår = OppfyllerIkkjeInngangsvilkårIPerioden.vurder(grunnlagOpt, endringsdato);

        // Assert
        assertThat(oppfyllerIkkjeInngangsvilkår).isFalse();
    }

    @Test
    public void skal_teste_at_andre_vilkår_ikke_påviker_medlemsskapsvilkårsjekk() {
        // Arrange
        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT)
            .leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.IKKE_VURDERT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT)
            .buildFor(revurdering);
        LocalDate endringsdato = SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2);
        settVilkårutfallMedlemskapPåDato(VilkårUtfallType.OPPFYLT, endringsdato);

        // Act
        Optional<MedlemskapsvilkårPeriodeGrunnlag> grunnlagOpt = medlemskapVilkårPeriodeRepository.hentAggregatHvisEksisterer(revurdering);
        boolean oppfyllerIkkjeInngangsvilkår = OppfyllerIkkjeInngangsvilkårIPerioden.vurder(grunnlagOpt, endringsdato);

        // Assert
        assertThat(oppfyllerIkkjeInngangsvilkår).isFalse();
    }

    @Test
    public void skal_finne_en_uttaksperiode_etter_endringstidspunkt() {
        LocalDate endringsdato = LocalDate.now();

        UttakResultatEntitet.Builder uttakResultatPlanBuilder = UttakResultatEntitet.builder(revurdering);
        UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
        lagUttakPeriodeMedPeriodeAktivitet(uttakResultatPerioder,
            new LocalDateInterval(endringsdato.minusDays(10), endringsdato.minusDays(6)),
            false, PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT, true, Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(0), Collections.singletonList(StønadskontoType.FORELDREPENGER));
        lagUttakPeriodeMedPeriodeAktivitet(uttakResultatPerioder,
            new LocalDateInterval(endringsdato.minusDays(5), endringsdato.minusDays(1)),
            false, PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT, true, Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(0), Collections.singletonList(StønadskontoType.FORELDREPENGER));
        lagUttakPeriodeMedPeriodeAktivitet(uttakResultatPerioder,
            new LocalDateInterval(endringsdato, endringsdato.plusDays(5)),
            false, PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT, true, Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(0), Collections.singletonList(StønadskontoType.FORELDREPENGER));
        UttakResultatEntitet uttakResultat = uttakResultatPlanBuilder.medOpprinneligPerioder(uttakResultatPerioder).build();
        uttakRepository.lagreOpprinneligUttakResultatPerioder(revurdering, uttakResultat.getGjeldendePerioder());

        List<UttakResultatPeriodeEntitet> uttaksperioder = ErEndringIUttakFraEndringsdato.finnUttaksperioderEtterEndringsdato(endringsdato, Optional.of(uttakResultat));

        // Assert
        assertThat(uttaksperioder).hasSize(1);

    }

    @Test
    public void skal_finne_to_uttaksperiode_etter_endringstidspunkt() {
        LocalDate endringsdato = LocalDate.now();

        UttakResultatEntitet.Builder uttakResultatPlanBuilder = UttakResultatEntitet.builder(revurdering);
        UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
        lagUttakPeriodeMedPeriodeAktivitet(uttakResultatPerioder,
            new LocalDateInterval(endringsdato.minusDays(10), endringsdato.minusDays(6)),
            false, PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT, true, Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(0), Collections.singletonList(StønadskontoType.FORELDREPENGER));
        lagUttakPeriodeMedPeriodeAktivitet(uttakResultatPerioder,
            new LocalDateInterval(endringsdato.minusDays(5), endringsdato.minusDays(1)),
            false, PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT, true, Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(0), Collections.singletonList(StønadskontoType.FORELDREPENGER));
        lagUttakPeriodeMedPeriodeAktivitet(uttakResultatPerioder,
            new LocalDateInterval(endringsdato, endringsdato.plusDays(5)),
            false, PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT, true, Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(0), Collections.singletonList(StønadskontoType.FORELDREPENGER));
        lagUttakPeriodeMedPeriodeAktivitet(uttakResultatPerioder,
            new LocalDateInterval(endringsdato.plusDays(10), endringsdato.plusDays(50)),
            false, PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT, true, Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(0), Collections.singletonList(StønadskontoType.FORELDREPENGER));
        UttakResultatEntitet uttakResultat = uttakResultatPlanBuilder.medOpprinneligPerioder(uttakResultatPerioder).build();
        uttakRepository.lagreOpprinneligUttakResultatPerioder(revurdering, uttakResultat.getGjeldendePerioder());

        List<UttakResultatPeriodeEntitet> uttaksperioder = ErEndringIUttakFraEndringsdato.finnUttaksperioderEtterEndringsdato(endringsdato, Optional.of(uttakResultat));

        // Assert
        assertThat(uttaksperioder).hasSize(2);

    }

    @Test
    public void skal_gi_endring_i_uttak_om_det_er_avvik_i_antall_perioder_etter_endringstidspunktet() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))), StønadskontoType.FORELDREPENGER);

        UttakResultatEntitet uttakResultatRevurdering = lagUttakResultatPlanForBehandling(revurdering,
            Arrays.asList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10)),
                new LocalDateInterval(endringsdato.plusDays(11), endringsdato.plusDays(20))), StønadskontoType.FEDREKVOTE);

        // Act
        boolean endringIUttak = ErEndringIUttakFraEndringsdato.vurder(endringsdato, Optional.of(uttakResultatRevurdering), Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(endringIUttak).isTrue();
    }

    @Test
    public void skal_gi_endring_i_uttak_om_det_er_avvik_i_antall_aktiviteter_etter_endringstidspunktet() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        UttakResultatEntitet uttakResultatRevurdering = lagUttakResultatPlanForBehandling(revurdering,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.nCopies(2, 100), Collections.nCopies(2, 100), Collections.nCopies(2, 10), Collections.nCopies(2, StønadskontoType.FORELDREPENGER)
        );

        // Act
        boolean endringIUttak = ErEndringIUttakFraEndringsdato.vurder(endringsdato, Optional.of(uttakResultatRevurdering), Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(endringIUttak).isTrue();
    }

    @Test
    public void skal_gi_endring_i_uttak_om_det_er_avvik_i_antall_trekkdager_i_aktivitet_etter_endringstidspunktet() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        UttakResultatEntitet uttakResultatRevurdering = lagUttakResultatPlanForBehandling(revurdering,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(10), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Act
        boolean endringIUttak = ErEndringIUttakFraEndringsdato.vurder(endringsdato, Optional.of(uttakResultatRevurdering), Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(endringIUttak).isTrue();
    }

    @Test
    public void skal_gi_endring_i_uttak_om_det_er_avvik_i_arbeidsprosent_i_aktivitet_etter_endringstidspunktet() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        UttakResultatEntitet uttakResultatRevurdering = lagUttakResultatPlanForBehandling(revurdering,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(50), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Act
        boolean endringIUttak = ErEndringIUttakFraEndringsdato.vurder(endringsdato, Optional.of(uttakResultatRevurdering), Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(endringIUttak).isTrue();
    }

    @Test
    public void skal_gi_endring_i_uttak_om_det_er_avvik_i_utbetatlingsgrad_i_aktivitet_etter_endringstidspunktet() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        UttakResultatEntitet uttakResultatRevurdering = lagUttakResultatPlanForBehandling(revurdering,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(50), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Act
        boolean endringIUttak = ErEndringIUttakFraEndringsdato.vurder(endringsdato, Optional.of(uttakResultatRevurdering), Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(endringIUttak).isTrue();
    }

    @Test
    public void skal_gi_endring_i_uttak_om_det_er_avvik_i_stønadskonto_etter_endringstidspunktet() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))), StønadskontoType.FORELDREPENGER);

        UttakResultatEntitet uttakResultatRevurdering = lagUttakResultatPlanForBehandling(revurdering,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))), StønadskontoType.FEDREKVOTE);

        // Act
        boolean endringIUttak = ErEndringIUttakFraEndringsdato.vurder(endringsdato, Optional.of(uttakResultatRevurdering), Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(endringIUttak).isTrue();
    }

    @Test
    public void skal_gi_endring_i_uttak_om_det_er_avvik_i_resultatType_etter_endringstidspunktet() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))), Collections.singletonList(PeriodeResultatType.AVSLÅTT));

        UttakResultatEntitet uttakResultatRevurdering = lagUttakResultatPlanForBehandling(revurdering,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))), Collections.singletonList(PeriodeResultatType.INNVILGET));

        // Act
        boolean endringIUttak = ErEndringIUttakFraEndringsdato.vurder(endringsdato, Optional.of(uttakResultatRevurdering), Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(endringIUttak).isTrue();
    }

    @Test
    public void skal_gi_endring_i_uttak_om_det_er_avvik_i_samtidig_uttak_etter_endringstidspunktet() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false),
            StønadskontoType.FORELDREPENGER);

        UttakResultatEntitet uttakResultatRevurdering = lagUttakResultatPlanForBehandling(revurdering,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(true),
            StønadskontoType.FORELDREPENGER);

        // Act
        boolean endringIUttak = ErEndringIUttakFraEndringsdato.vurder(endringsdato, Optional.of(uttakResultatRevurdering), Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(endringIUttak).isTrue();
    }

    @Test
    public void skal_gi_endring_i_uttak_om_det_er_avvik_i_gradering_utfall_i_aktivitet_etter_endringstidspunktet() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        UttakResultatEntitet uttakResultatRevurdering = lagUttakResultatPlanForBehandling(revurdering,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(false), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Act
        boolean endringIUttak = ErEndringIUttakFraEndringsdato.vurder(endringsdato, Optional.of(uttakResultatRevurdering), Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(endringIUttak).isTrue();
    }

    @Test
    public void skal_ikkje_gi_endring_i_uttak_om_det_ikkje_er_avvik_etter_endringstidspunktet() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        UttakResultatEntitet uttakResultatRevurdering = lagUttakResultatPlanForBehandling(revurdering,
            Collections.singletonList(new LocalDateInterval(endringsdato, endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Act
        boolean endringIUttak = ErEndringIUttakFraEndringsdato.vurder(endringsdato, Optional.of(uttakResultatRevurdering), Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(endringIUttak).isFalse();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_aktivitetskravet_arbeid_ikke_oppfylt() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT);

        // Act

        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_aktivitetskravet_offentlig_godkjent_utdanning_ikke_oppfylt() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.AKTIVITETSKRAVET_OFFENTLIG_GODKJENT_UTDANNING_IKKE_OPPFYLT);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }


    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_aktivitetskravet_offentlig_godkjent_utdanning_i_kombinasjon_arbeid_ikke_oppfylt() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.AKTIVITETSKRAVET_OFFENTLIG_GODKJENT_UTDANNING_I_KOMBINASJON_MED_ARBEID_IKKE_OPPFYLT);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_aktivitetskravet_sykdom_ikke_oppfylt() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.AKTIVITETSKRAVET_MORS_SYKDOM_IKKE_OPPFYLT);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_aktivitetskravet_innleggelse_ikke_oppfylt() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.AKTIVITETSKRAVET_MORS_INNLEGGELSE_IKKE_OPPFYLT);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_aktivitetskravet_introduksjonsprogram_ikke_oppfylt() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.AKTIVITETSKRAVET_MORS_DELTAKELSE_PÅ_INTRODUKSJONSPROGRAM_IKKE_OPPFYLT);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_aktivitetskravet_kvalifiseringsprogram_ikke_oppfylt() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.AKTIVITETSKRAVET_MORS_DELTAKELSE_PÅ_KVALIFISERINGSPROGRAM_IKKE_OPPFYLT);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_aktivitetskravet_mottak_av_uføretrygd_ikke_oppfylt() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.MORS_MOTTAK_AV_UFØRETRYGD_IKKE_OPPFYLT);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_stebarnsadopsjon_ikke_nok_dager() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.STEBARNSADOPSJON_IKKE_NOK_DAGER);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_flerbarnsfødsel_ikke_nok_dager() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.FLERBARNSFØDSEL_IKKE_NOK_DAGER);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_mor_har_ikke_omsorg() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_far_har_ikke_omsorg() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_annen_part_syk_ikke_oppfylt() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_annen_part_innleggelse_ikke_oppfylt() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.DEN_ANDRE_PART_INNLEGGELSE_IKKE_OPPFYLT);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_barn_død() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.BARNET_ER_DØD);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_uttaksperiode_er_avslått_med_årsak_søker_død() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.SØKER_ER_DØD);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), true);

        // Assert
        assertThat(harOpphørsårsak).isTrue();
    }

    @Test
    public void skal_sjekke_at_siste_periode_ikke_gir_opphør_når_det_ikke_har_vært_endring_i_uttak() {
        // Arrange
        UttakResultatEntitet uttakresultatRevurdering = lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak.SØKER_ER_DØD);

        // Act
        boolean harOpphørsårsak = ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(Optional.of(uttakresultatRevurdering), false);

        // Assert
        assertThat(harOpphørsårsak).isFalse();
    }

    @Test
    public void skal_gi_løpende_vedtak_med_tom_for_innvilget_periode_etter_endringstidpunkt() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato.minusDays(10), endringsdato.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Act
        boolean løpendeVedtak = HarLøpendeVedtak.vurder(endringsdato, Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(løpendeVedtak).isTrue();
    }

    @Test
    public void skal_gi_løpende_vedtak_med_tom_for_innvilget_periode_på_endringstidpunkt() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato.minusDays(10), endringsdato)),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Act
        boolean løpendeVedtak = HarLøpendeVedtak.vurder(endringsdato, Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(løpendeVedtak).isTrue();
    }

    @Test
    public void skal_ikkje_gi_løpende_vedtak_med_tom_for_avslått_periode_etter_endringstidpunkt() {
        // Arrange
        LocalDate endringsdato = LocalDate.now();
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato.minusDays(10), endringsdato.plusDays(5))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.AVSLÅTT), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Act
        boolean løpendeVedtak = HarLøpendeVedtak.vurder(endringsdato, Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(løpendeVedtak).isFalse();
    }

    @Test
    public void skal_ikkje_gi_løpende_vedtak_med_tom_for_innvilget_periode_før_endringstidpunkt() {
        // Arrange
        LocalDate endringsdato = LocalDate.of(2018, 1, 1);
        lagBeregningsresultatperiodeMedEndringstidspunkt(endringsdato);
        when(endringsdatoRevurderingUtleder.utledEndringsdato(any(Behandling.class))).thenReturn(endringsdato);

        UttakResultatEntitet uttakResultatOriginal = lagUttakResultatPlanForBehandling(behandlingSomSkalRevurderes,
            Collections.singletonList(new LocalDateInterval(endringsdato.minusDays(10), endringsdato.minusDays(5))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.INNVILGET), Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );

        // Act
        boolean løpendeVedtak = HarLøpendeVedtak.vurder(endringsdato, Optional.of(uttakResultatOriginal));

        // Assert
        assertThat(løpendeVedtak).isFalse();
    }

    private UttakResultatEntitet lagUttaksplanMedIkkeOppfyltÅrsak(IkkeOppfyltÅrsak årsak) {
        LocalDate fra = LocalDate.now();
        return lagUttakResultatPlanForBehandling(revurdering,
            Collections.singletonList(new LocalDateInterval(fra, fra.plusDays(10))),
            Collections.singletonList(false), Collections.singletonList(PeriodeResultatType.AVSLÅTT),
            Collections.singletonList(årsak), Collections.singletonList(false), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(12), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );
    }

    private void lagUttakPeriodeMedPeriodeAktivitet(UttakResultatPerioderEntitet uttakResultatPerioder, LocalDateInterval periode, boolean samtidigUttak, PeriodeResultatType periodeResultatType,
                                                    PeriodeResultatÅrsak periodeResultatÅrsak, boolean graderingInnvilget, List<Integer> andelIArbeid, List<Integer> utbetalingsgrad, List<Integer> trekkdager, List<StønadskontoType> stønadskontoTyper) {
        UttakResultatPeriodeEntitet uttakResultatPeriode = byggPeriode(periode.getFomDato(), periode.getTomDato(), samtidigUttak, periodeResultatType, periodeResultatÅrsak, graderingInnvilget);

        int antallAktiviteter = stønadskontoTyper.size();
        for (int i = 0; i < antallAktiviteter; i++) {
            UttakResultatPeriodeAktivitetEntitet periodeAktivitet = lagPeriodeAktivitet(stønadskontoTyper.get(i), uttakResultatPeriode, trekkdager.get(i),
                andelIArbeid.get(i), utbetalingsgrad.get(i));
            uttakResultatPeriode.leggTilAktivitet(periodeAktivitet);
        }
        uttakResultatPerioder.leggTilPeriode(uttakResultatPeriode);
    }

    private UttakResultatPeriodeAktivitetEntitet lagPeriodeAktivitet(StønadskontoType stønadskontoType, UttakResultatPeriodeEntitet uttakResultatPeriode,
                                                                     int trekkdager, int andelIArbeid, int utbetalingsgrad) {
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref(ARBEIDSFORHOLD_ID))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        return UttakResultatPeriodeAktivitetEntitet.builder(uttakResultatPeriode,
            uttakAktivitet)
            .medTrekkonto(stønadskontoType)
            .medTrekkdager(trekkdager)
            .medArbeidsprosent(BigDecimal.valueOf(andelIArbeid))
            .medUtbetalingsprosent(new BigDecimal(utbetalingsgrad))
            .build();
    }

    private UttakResultatEntitet lagUttakResultatPlanForBehandling(Behandling behandling, List<LocalDateInterval> perioder, List<PeriodeResultatType> periodeResultatTyper) {
        return lagUttakResultatPlanForBehandling(behandling, perioder, Collections.nCopies(perioder.size(), false),
            periodeResultatTyper, Collections.singletonList(PeriodeResultatÅrsak.UKJENT), Collections.singletonList(true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(0), Collections.singletonList(StønadskontoType.FORELDREPENGER)
        );
    }


    private UttakResultatEntitet lagUttakResultatPlanForBehandling(Behandling behandling, List<LocalDateInterval> perioder, StønadskontoType stønadskontoType) {
        return lagUttakResultatPlanForBehandling(behandling, perioder, Collections.nCopies(perioder.size(), false),
            Collections.nCopies(perioder.size(), PeriodeResultatType.INNVILGET), Collections.nCopies(perioder.size(), PeriodeResultatÅrsak.UKJENT), Collections.nCopies(perioder.size(), true), Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(0), Collections.singletonList(stønadskontoType)
        );
    }

    private UttakResultatEntitet lagUttakResultatPlanForBehandling(Behandling behandling, List<LocalDateInterval> perioder, List<Boolean> samtidigUttak, StønadskontoType stønadskontoType) {
        return lagUttakResultatPlanForBehandling(behandling, perioder, samtidigUttak, Collections.nCopies(perioder.size(), PeriodeResultatType.INNVILGET),
            Collections.nCopies(perioder.size(), PeriodeResultatÅrsak.UKJENT), samtidigUttak, Collections.singletonList(100), Collections.singletonList(100), Collections.singletonList(0), Collections.singletonList(stønadskontoType)
        );
    }

    private UttakResultatEntitet lagUttakResultatPlanForBehandling(Behandling behandling, List<LocalDateInterval> perioder, List<Boolean> samtidigUttak,
                                                                   List<PeriodeResultatType> periodeResultatTyper, List<PeriodeResultatÅrsak> periodeResultatÅrsak,
                                                                   List<Boolean> graderingInnvilget, List<Integer> andelIArbeid,
                                                                   List<Integer> utbetalingsgrad, List<Integer> trekkdager, List<StønadskontoType> stønadskontoTyper) {
        UttakResultatEntitet.Builder uttakResultatPlanBuilder = UttakResultatEntitet.builder(behandling);
        UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
        assertThat(perioder).hasSize(samtidigUttak.size());
        assertThat(perioder).hasSize(periodeResultatTyper.size());
        assertThat(perioder).hasSize(periodeResultatÅrsak.size());
        assertThat(perioder).hasSize(graderingInnvilget.size());
        int antallPerioder = perioder.size();
        for (int i = 0; i < antallPerioder; i++) {
            lagUttakPeriodeMedPeriodeAktivitet(uttakResultatPerioder, perioder.get(i),
                samtidigUttak.get(i), periodeResultatTyper.get(i), periodeResultatÅrsak.get(i), graderingInnvilget.get(i), andelIArbeid, utbetalingsgrad, trekkdager, stønadskontoTyper);
        }
        UttakResultatEntitet uttakResultat = uttakResultatPlanBuilder.medOpprinneligPerioder(uttakResultatPerioder).build();
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, uttakResultat.getGjeldendePerioder());
        return uttakResultat;

    }

    private UttakResultatPeriodeEntitet byggPeriode(LocalDate fom, LocalDate tom, boolean samtidigUttak, PeriodeResultatType periodeResultatType, PeriodeResultatÅrsak periodeResultatÅrsak, boolean graderingInnvilget) {
        return new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medSamtidigUttak(samtidigUttak)
            .medPeriodeResultat(periodeResultatType, periodeResultatÅrsak)
            .medGraderingInnvilget(graderingInnvilget)
            .build();
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


    private Beregningsgrunnlag byggBeregningsgrunnlagForBehandling(Behandling behandling, boolean skalDeleAndelMellomArbeidsgiverOgBruker, List<LocalDateInterval> perioder, List<Integer> dagsatser) {
        List<Integer> dagsatsBruker = skalDeleAndelMellomArbeidsgiverOgBruker ?
            dagsatser.stream().map(dagsats -> BigDecimal.valueOf(dagsats).divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP).intValue()).collect(Collectors.toList())
            : dagsatser;
        List<Integer> dagsatsArbeidstaker = skalDeleAndelMellomArbeidsgiverOgBruker ?
            dagsatser.stream().map(dagsats -> BigDecimal.valueOf(dagsats).divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP).intValue()).collect(Collectors.toList())
            : Collections.nCopies(dagsatser.size(), 0);

        Beregningsgrunnlag grunnlag = Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(BigDecimal.valueOf(91425L))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(91425L))
            .build();
        byggBeregningsgrunnlagPeriodeOgAndeler(grunnlag, perioder, dagsatsBruker, dagsatsArbeidstaker);

        beregningsgrunnlagRepository.lagre(behandling, grunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
        return grunnlag;
    }

    private void byggBeregningsgrunnlagPeriodeOgAndeler(Beregningsgrunnlag grunnlag, List<LocalDateInterval> perioder, List<Integer> dagsatserBruker, List<Integer> dagsatserArbeidstaker) {
        for (int i = 0; i < perioder.size(); i++) {
            LocalDateInterval datoIntervall = perioder.get(i);
            Integer dagsatsBruker = dagsatserBruker.get(i);
            Integer dagsatsArbeidstaker = dagsatserArbeidstaker.get(i);
            BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(datoIntervall.getFomDato(), datoIntervall.getTomDato())
                .build(grunnlag);
            byggBeregningsgrunnlagAndel(periode, dagsatsBruker, dagsatsArbeidstaker);
            BeregningsgrunnlagPeriode.builder(periode)
                .build(grunnlag);
        }
    }

    private void byggBeregningsgrunnlagAndel(BeregningsgrunnlagPeriode periode, Integer dagsatsBruker, Integer dagsatsArbeidsgiver) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(ARBEIDSFORHOLDLISTE.get(0))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(240000))
            .medRedusertBrukersAndelPrÅr(BigDecimal.valueOf(dagsatsBruker * 260))
            .medRedusertRefusjonPrÅr(BigDecimal.valueOf(dagsatsArbeidsgiver * 260))
            .build(periode);
    }

    private Beregningsgrunnlag byggBeregningsgrunnlagForBehandling(Behandling behandling, boolean medOppjustertDagsat, boolean skalDeleAndelMellomArbeidsgiverOgBruker, List<ÅpenDatoIntervallEntitet> perioder) {
        return byggBeregningsgrunnlagForBehandling(behandling, medOppjustertDagsat, skalDeleAndelMellomArbeidsgiverOgBruker, perioder, new LagEnAndelTjeneste());
    }

    private Beregningsgrunnlag byggBeregningsgrunnlagForBehandling(Behandling behandling, boolean medOppjustertDagsat, boolean skalDeleAndelMellomArbeidsgiverOgBruker, List<ÅpenDatoIntervallEntitet> perioder, LagAndelTjeneste lagAndelTjeneste) {

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

    private <T> BeregningsgrunnlagPeriode byggBGPeriode(ÅpenDatoIntervallEntitet datoPeriode, boolean medOppjustertDagsat, boolean skalDeleAndelMellomArbeidsgiverOgBruker, LagAndelTjeneste lagAndelTjeneste) {
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(datoPeriode.getFomDato(), datoPeriode.getTomDato())
            .build(beregningsgrunnlag);
        lagAndelTjeneste.lagAndeler(periode, medOppjustertDagsat, skalDeleAndelMellomArbeidsgiverOgBruker);
        return periode;

    }

    private void lagToAndelerMotsattRekkefølge(List<Dagsatser> dagsatser, BeregningsgrunnlagPeriode periode) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(ARBEIDSFORHOLDLISTE.get(1))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(240000))
            .medRedusertBrukersAndelPrÅr(dagsatser.get(1).getDagsatsBruker())
            .medRedusertRefusjonPrÅr(dagsatser.get(1).getDagsatsArbeidstaker())
            .build(periode);
        BGAndelArbeidsforhold.Builder bga2 = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(ARBEIDSFORHOLDLISTE.get(0))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga2)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(240000))
            .medRedusertBrukersAndelPrÅr(dagsatser.get(0).getDagsatsBruker())
            .medRedusertRefusjonPrÅr(dagsatser.get(0).getDagsatsArbeidstaker())
            .build(periode);
    }

    private void lagToAndeler(List<Dagsatser> dagsatser , BeregningsgrunnlagPeriode periode) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(ARBEIDSFORHOLDLISTE.get(0))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(240000))
            .medRedusertBrukersAndelPrÅr(dagsatser.get(0).getDagsatsBruker())
            .medRedusertRefusjonPrÅr(dagsatser.get(0).getDagsatsArbeidstaker())
            .build(periode);
        BGAndelArbeidsforhold.Builder bga2 = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(ARBEIDSFORHOLDLISTE.get(1))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga2)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(240000))
            .medRedusertBrukersAndelPrÅr(dagsatser.get(1).getDagsatsBruker())
            .medRedusertRefusjonPrÅr(dagsatser.get(1).getDagsatsArbeidstaker())
            .build(periode);
    }

    private void lagEnAndel(boolean medOppjustertDagsat, boolean skalDeleAndelMellomArbeidsgiverOgBruker, BeregningsgrunnlagPeriode periode) {
        Dagsatser ds = new Dagsatser(medOppjustertDagsat, skalDeleAndelMellomArbeidsgiverOgBruker);
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(ARBEIDSFORHOLDLISTE.get(0))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(240000))
            .medRedusertBrukersAndelPrÅr(ds.getDagsatsBruker())
            .medRedusertRefusjonPrÅr(ds.getDagsatsArbeidstaker())
            .build(periode);
    }

}
