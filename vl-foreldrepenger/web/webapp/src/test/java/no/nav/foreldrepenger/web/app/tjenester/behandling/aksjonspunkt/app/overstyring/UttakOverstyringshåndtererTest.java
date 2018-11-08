package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.InnvilgetÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.FastsettePerioderTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OverhoppKontroll;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.OverstyringUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPeriodeAktivitetLagreDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPeriodeLagreDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.overstyring.UttakOverstyringshåndterer;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class UttakOverstyringshåndtererTest {

    private static final String ORGNR = "000000000";
    private static final String ARBEIDSFORHOLD_ID = "1234";

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private final Repository repository = repoRule.getRepository();
    private final UttakRepository uttakRepository = new UttakRepositoryImpl(repoRule.getEntityManager());
    private Behandling behandling;

    @Before
    public void setUp() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        behandling = scenario.lagre(repositoryProvider);
        repository.lagre(behandling.getBehandlingsresultat());
    }

    @Test
    public void skalReturnereUtenOveropp() {
        FastsettePerioderTjeneste tjeneste = mock(FastsettePerioderTjeneste.class);

        UttakOverstyringshåndterer oppdaterer = new UttakOverstyringshåndterer(repositoryProvider, historikkAdapterTjeneste(), tjeneste, aksjonspunktRepository());

        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusWeeks(2);
        UttakResultatPeriodeAktivitetLagreDto aktivitetLagreDto = new UttakResultatPeriodeAktivitetLagreDto.Builder()
            .medArbeidsforholdId(ARBEIDSFORHOLD_ID)
            .medArbeidsforholdOrgnr(ORGNR)
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        List<UttakResultatPeriodeAktivitetLagreDto> aktiviteter = Collections.singletonList(aktivitetLagreDto);
        PeriodeResultatType periodeResultatType = PeriodeResultatType.INNVILGET;
        PeriodeResultatÅrsak periodeResultatÅrsak = InnvilgetÅrsak.UTTAK_OPPFYLT;
        StønadskontoType stønadskontoType = StønadskontoType.FORELDREPENGER;
        String begrunnelse = "Dette er begrunnelsen";
        UttakResultatPeriodeLagreDto periode = new UttakResultatPeriodeLagreDto.Builder()
            .medTidsperiode(fom, tom)
            .medAktiviteter(aktiviteter)
            .medBegrunnelse(begrunnelse)
            .medType(periodeResultatType)
            .medÅrsak(periodeResultatÅrsak)
            .medFlerbarnsdager(false)
            .medSamtidigUttak(false)
            .build();

        List<UttakResultatPeriodeLagreDto> perioder = Collections.singletonList(periode);
        OverstyringUttakDto dto = new OverstyringUttakDto(perioder);

        //arrange
        UttakResultatPerioderEntitet opprinneligPerioder = opprettUttakResultatPeriode(periodeResultatType, fom, tom, stønadskontoType);
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, opprinneligPerioder);

        OppdateringResultat result = oppdaterer.håndterOverstyring(dto, behandling, null);

        ArgumentCaptor<UttakResultatPerioder> captor = ArgumentCaptor.forClass(UttakResultatPerioder.class);

        verify(tjeneste).manueltFastsettePerioder(eq(behandling), captor.capture());

        UttakResultatPerioder mapped = captor.getValue();
        assertThat(mapped.getPerioder()).hasSize(1);
        assertThat(mapped.getPerioder().get(0).getTidsperiode().getFomDato()).isEqualTo(fom);
        assertThat(mapped.getPerioder().get(0).getTidsperiode().getTomDato()).isEqualTo(tom);
        assertThat(mapped.getPerioder().get(0).getBegrunnelse()).isEqualTo(begrunnelse);
        assertThat(mapped.getPerioder().get(0).getAktiviteter()).hasSize(aktiviteter.size());
        assertThat(mapped.getPerioder().get(0).getResultatType()).isEqualTo(periodeResultatType);
        assertThat(mapped.getPerioder().get(0).getResultatÅrsak()).isEqualTo(periodeResultatÅrsak);
        assertThat(mapped.getPerioder().get(0).getAktiviteter()).hasSize(1);
        assertThat(result.getOverhoppKontroll()).isEqualTo(OverhoppKontroll.UTEN_OVERHOPP);
    }

    private AksjonspunktRepository aksjonspunktRepository() {
        AksjonspunktRepository mock = mock(AksjonspunktRepository.class);
        when(mock.finnAksjonspunktDefinisjon(anyString())).thenReturn(AksjonspunktDefinisjon.OVERSTYRING_AV_UTTAKPERIODER);
        return mock;
    }

    private HistorikkTjenesteAdapter historikkAdapterTjeneste() {
        return mock(HistorikkTjenesteAdapter.class);
    }

    private UttakResultatPerioderEntitet opprettUttakResultatPeriode(PeriodeResultatType resultat,
                                                                     LocalDate fom,
                                                                     LocalDate tom,
                                                                     StønadskontoType stønadskontoType) {
        UttakResultatPeriodeEntitet uttakResultatPeriode = new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(resultat, PeriodeResultatÅrsak.UKJENT)
            .build();

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medOrgnr(ORGNR).oppdatertOpplysningerNå().build();
        repoRule.getRepository().lagre(virksomhet);

        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref(ARBEIDSFORHOLD_ID))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(uttakResultatPeriode, uttakAktivitet)
            .medTrekkonto(stønadskontoType)
            .medTrekkdager(10)
            .medArbeidsprosent(BigDecimal.ZERO)
            .medUtbetalingsprosent(BigDecimal.valueOf(100))
            .build();

        uttakResultatPeriode.leggTilAktivitet(periodeAktivitet);

        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        perioder.leggTilPeriode(uttakResultatPeriode);

        return perioder;
    }
}
