package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.EndringsresultatSjekker;
import no.nav.foreldrepenger.domene.ytelsefordeling.YtelseFordelingTjeneste;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class EndringsresultatSjekkerImplTest {

    private EndringsresultatSjekker endringsresultatSjekker;

    @Mock
    private PersonopplysningTjeneste personopplysningTjeneste;
    @Mock
    private FamilieHendelseTjeneste familieHendelseTjeneste;
    @Mock
    private MedlemTjeneste medlemTjeneste;
    @Mock
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    @Mock
    private YtelseFordelingTjeneste ytelseFordelingTjeneste;
    @Mock
    private BehandlingRepositoryProvider behandlingRepositoryProvider;

    @Mock
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    @Mock
    private OpptjeningRepository opptjeningRepository;
    @Mock
    private UttakRepository uttakRepository;
    @Mock
    private Behandling behandling;
    @Mock
    private Behandlingsresultat behandlingsresultat;
    @Mock
    private VilkårResultat vilkårResultat;
    @Mock
    private BeregningResultat beregningResultat;

    private static final LocalDateTime nå = LocalDateTime.now();

    private static final Long personGrunnlagID = 1L;
    private static final Long familieGrunnlagID = 2L;
    private static final Long medlemGrunnlagID = 3L;
    private static final Long iayGrunnlagID = 4L;
    private static final Long ytelseGrunnlagID = 5L;

    private static final Long opptjeningGrunnlagID = 6L;
    private static final Long beregningsGrunnlagID = 7L;
    private static final Long uttakGrunnlagID = 8L;
    private static final Long uttakPeriodeGrenseGrunnlagID = 9L;
    private static final Long vilkårGrunnlagID = EndringsresultatSjekkerImpl.mapFraLocalDateTimeTilLong(nå);
    private static final Long beregningsResultatGrunnlagID = EndringsresultatSjekkerImpl.mapFraLocalDateTimeTilLong(nå);

    @Before
    public void setup() {
        personopplysningTjeneste = mock(PersonopplysningTjeneste.class);
        familieHendelseTjeneste = mock(FamilieHendelseTjeneste.class);
        medlemTjeneste = mock(MedlemTjeneste.class);
        inntektArbeidYtelseTjeneste = mock(InntektArbeidYtelseTjeneste.class);
        ytelseFordelingTjeneste = mock(YtelseFordelingTjeneste.class);

        opptjeningRepository = mock(OpptjeningRepository.class);
        beregningsgrunnlagRepository = mock(BeregningsgrunnlagRepository.class);
        uttakRepository = mock(UttakRepository.class);

        behandling = mock(Behandling.class);

        behandlingsresultat = mock(Behandlingsresultat.class);
        vilkårResultat = mock(VilkårResultat.class);
        beregningResultat = mock(BeregningResultat.class);


        behandlingRepositoryProvider = mock(BehandlingRepositoryProvider.class);
        when(behandlingRepositoryProvider.getBeregningsgrunnlagRepository()).thenReturn(beregningsgrunnlagRepository);
        when(behandlingRepositoryProvider.getOpptjeningRepository()).thenReturn(opptjeningRepository);
        when(behandlingRepositoryProvider.getUttakRepository()).thenReturn(uttakRepository);


        endringsresultatSjekker = new EndringsresultatSjekkerImpl(personopplysningTjeneste, familieHendelseTjeneste, medlemTjeneste, inntektArbeidYtelseTjeneste, ytelseFordelingTjeneste, behandlingRepositoryProvider);
        opprettMockTjenesteResponse();
    }

    private void opprettMockTjenesteResponse() {
        when(personopplysningTjeneste.finnAktivGrunnlagId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(PersonInformasjon.class, personGrunnlagID));
        when(familieHendelseTjeneste.finnAktivAggregatId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(FamilieHendelseGrunnlag.class, familieGrunnlagID));
        when(medlemTjeneste.finnAktivGrunnlagId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(MedlemskapAggregat.class, medlemGrunnlagID));
        when(inntektArbeidYtelseTjeneste.finnAktivAggregatId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(InntektArbeidYtelseGrunnlag.class, iayGrunnlagID));
        when(ytelseFordelingTjeneste.finnAktivAggregatId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(YtelseFordelingAggregat.class, ytelseGrunnlagID));

        when(opptjeningRepository.finnAktivGrunnlagId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(Opptjening.class, opptjeningGrunnlagID));
        when(beregningsgrunnlagRepository.finnAktivAggregatId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(Beregningsgrunnlag.class, beregningsGrunnlagID));
        when(uttakRepository.finnAktivAggregatId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(UttakResultatEntitet.class, uttakGrunnlagID));
        when(uttakRepository.finnAktivUttakPeriodeGrenseAggregatId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(Uttaksperiodegrense.class, uttakPeriodeGrenseGrunnlagID));

        when(behandling.getBehandlingsresultat()).thenReturn(behandlingsresultat);
        when(behandlingsresultat.getVilkårResultat()).thenReturn(vilkårResultat);
        when(behandlingsresultat.getBeregningResultat()).thenReturn(beregningResultat);

        when(vilkårResultat.getOpprettetTidspunkt()).thenReturn(nå);
        when(beregningResultat.getOpprettetTidspunkt()).thenReturn(nå);
    }

    @Test
    public void test_opprett_endringsresultatSnapshot() {
        EndringsresultatSnapshot endringsresultatSnapshot = endringsresultatSjekker.opprettEndringsresultatIdPåBehandlingSnapshot(behandling);

        assertThat(personGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(PersonInformasjon.class).get().getGrunnlagId());
        assertThat(familieGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(FamilieHendelseGrunnlag.class).get().getGrunnlagId());
        assertThat(medlemGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(MedlemskapAggregat.class).get().getGrunnlagId());
        assertThat(iayGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(InntektArbeidYtelseGrunnlag.class).get().getGrunnlagId());
        assertThat(ytelseGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(YtelseFordelingAggregat.class).get().getGrunnlagId());

        assertThat(opptjeningGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(Opptjening.class).get().getGrunnlagId());
        assertThat(beregningsGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(Beregningsgrunnlag.class).get().getGrunnlagId());
        assertThat(uttakGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(UttakResultatEntitet.class).get().getGrunnlagId());
        assertThat(uttakPeriodeGrenseGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(Uttaksperiodegrense.class).get().getGrunnlagId());

        assertThat(vilkårGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(VilkårResultat.class).get().getGrunnlagId());
        assertThat(beregningsResultatGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(BeregningResultat.class).get().getGrunnlagId());
    }
}
