package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.EndringsresultatSjekker;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class EndringsresultatSjekkerImplTest {

    private static final LocalDateTime nå = LocalDateTime.now();
    private static final Long personGrunnlagID = 1L;
    private static final Long medlemGrunnlagID = 3L;
    private static final Long iayGrunnlagID = 4L;
    private static final Long opptjeningGrunnlagID = 6L;
    private static final Long beregningsGrunnlagID = 7L;
    private static final Long uttakGrunnlagID = 8L;
    private static final Long uttakPeriodeGrenseGrunnlagID = 9L;
    private static final Long vilkårGrunnlagID = EndringsresultatSjekkerImpl.mapFraLocalDateTimeTilLong(nå);
    private static final Long beregningsResultatGrunnlagID = EndringsresultatSjekkerImpl.mapFraLocalDateTimeTilLong(nå);
    private EndringsresultatSjekker endringsresultatSjekker;

    private PersonopplysningTjeneste personopplysningTjeneste;

    private MedlemTjeneste medlemTjeneste;

    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    private GrunnlagRepositoryProvider grunnlagRepositoryProvider;

    private ResultatRepositoryProvider resultatRepositoryProvider;

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    private BeregningsresultatRepository beregningsresultatRepository;

    private OpptjeningRepository opptjeningRepository;

    private UttakRepository uttakRepository;

    private Behandling behandling;

    private Behandlingsresultat behandlingsresultat;

    private VilkårResultat vilkårResultat;

    private BeregningsResultat beregningResultat;

    @Before
    public void setup() {
        personopplysningTjeneste = mock(PersonopplysningTjeneste.class);
        medlemTjeneste = mock(MedlemTjeneste.class);
        inntektArbeidYtelseTjeneste = mock(InntektArbeidYtelseTjeneste.class);

        opptjeningRepository = mock(OpptjeningRepository.class);
        beregningsgrunnlagRepository = mock(BeregningsgrunnlagRepository.class);
        beregningsresultatRepository = mock(BeregningsresultatRepository.class);
        uttakRepository = mock(UttakRepository.class);

        behandling = mock(Behandling.class);

        behandlingsresultat = mock(Behandlingsresultat.class);
        vilkårResultat = mock(VilkårResultat.class);
        beregningResultat = mock(BeregningsResultat.class);


        resultatRepositoryProvider = mock(ResultatRepositoryProvider.class);
        grunnlagRepositoryProvider = mock(GrunnlagRepositoryProvider.class);
        when(resultatRepositoryProvider.getBeregningsgrunnlagRepository()).thenReturn(beregningsgrunnlagRepository);
        when(resultatRepositoryProvider.getOpptjeningRepository()).thenReturn(opptjeningRepository);
        when(resultatRepositoryProvider.getUttakRepository()).thenReturn(uttakRepository);
        when(resultatRepositoryProvider.getBeregningsresultatRepository()).thenReturn(beregningsresultatRepository);


        endringsresultatSjekker = new EndringsresultatSjekkerImpl(personopplysningTjeneste, medlemTjeneste, inntektArbeidYtelseTjeneste, resultatRepositoryProvider);
        opprettMockTjenesteResponse();
    }

    private void opprettMockTjenesteResponse() {
        when(personopplysningTjeneste.finnAktivGrunnlagId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(PersonInformasjon.class, personGrunnlagID));
        when(medlemTjeneste.finnAktivGrunnlagId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(MedlemskapAggregat.class, medlemGrunnlagID));
        when(inntektArbeidYtelseTjeneste.finnAktivAggregatId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(InntektArbeidYtelseGrunnlag.class, iayGrunnlagID));

        when(opptjeningRepository.finnAktivGrunnlagId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(Opptjening.class, opptjeningGrunnlagID));
        when(beregningsgrunnlagRepository.finnAktivAggregatId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(Beregningsgrunnlag.class, beregningsGrunnlagID));
        when(uttakRepository.finnAktivAggregatId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(UttakResultatEntitet.class, uttakGrunnlagID));
        when(uttakRepository.finnAktivUttakPeriodeGrenseAggregatId(any(Behandling.class))).thenReturn(EndringsresultatSnapshot.medSnapshot(Uttaksperiodegrense.class, uttakPeriodeGrenseGrunnlagID));

        when(behandling.getBehandlingsresultat()).thenReturn(behandlingsresultat);
        when(behandlingsresultat.getVilkårResultat()).thenReturn(vilkårResultat);

        when(beregningsresultatRepository.hentHvisEksistererFor(any(Behandlingsresultat.class))).thenReturn(Optional.of(beregningResultat));

        when(vilkårResultat.getOpprettetTidspunkt()).thenReturn(nå);
        when(beregningResultat.getOpprettetTidspunkt()).thenReturn(nå);
    }

    @Test
    public void test_opprett_endringsresultatSnapshot() {
        EndringsresultatSnapshot endringsresultatSnapshot = endringsresultatSjekker.opprettEndringsresultatIdPåBehandlingSnapshot(behandling);

        assertThat(personGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(PersonInformasjon.class).get().getGrunnlagId());
        assertThat(medlemGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(MedlemskapAggregat.class).get().getGrunnlagId());
        assertThat(iayGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(InntektArbeidYtelseGrunnlag.class).get().getGrunnlagId());

        assertThat(opptjeningGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(Opptjening.class).get().getGrunnlagId());
        assertThat(beregningsGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(Beregningsgrunnlag.class).get().getGrunnlagId());
        assertThat(uttakGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(UttakResultatEntitet.class).get().getGrunnlagId());
        assertThat(uttakPeriodeGrenseGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(Uttaksperiodegrense.class).get().getGrunnlagId());

        assertThat(vilkårGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(VilkårResultat.class).get().getGrunnlagId());
        assertThat(beregningsResultatGrunnlagID).isEqualTo(endringsresultatSnapshot.hentDelresultat(BeregningsResultat.class).get().getGrunnlagId());
    }
}
