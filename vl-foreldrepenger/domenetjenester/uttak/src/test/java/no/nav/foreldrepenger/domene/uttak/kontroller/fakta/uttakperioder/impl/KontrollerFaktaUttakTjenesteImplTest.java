package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KontrollerFaktaUttakTjenesteImplTest {

    private static AktørId FAR_AKTØR_ID = new AktørId("22");

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());

    @Inject
    @FagsakYtelseTypeRef("FP")
    private KontrollerFaktaUttakTjenesteImpl tjeneste;

    private Behandling opprettBehandlingForFarSomSøker() {
        ScenarioFarSøkerForeldrepenger scenario = ScenarioFarSøkerForeldrepenger.forFødselMedGittAktørId(FAR_AKTØR_ID);
        scenario.medSøknadHendelse().medFødselsDato(LocalDate.now());
        leggTilSøker(scenario, NavBrukerKjønn.MANN);
        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, false, false);
        scenario.medOppgittRettighet(rettighet);

        LocalDate idag = LocalDate.now();
        OppgittPeriode periode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FEDREKVOTE)
            .medPeriode(idag.plusWeeks(6), idag.plusWeeks(10))
            .build();

        scenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(periode), true));
        return scenario.lagre(repositoryProvider);
    }


    private Behandling opprettBehandlingForMorMedSøktePerioder(List<OppgittPeriode> perioder) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(LocalDate.now());
        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, true, false);
        scenario.medOppgittRettighet(rettighet);

        scenario.medFordeling(new OppgittFordelingEntitet(perioder, true));
        return scenario.lagre(repositoryProvider);
    }

    @Test
    public void aksjonspunkt_dersom_far_søker_og_ikke_oppgitt_omsorg_til_barnet() {
        //Arrange
        Behandling behandling = opprettBehandlingForFarSomSøker();
        //Act
        List<AksjonspunktResultat> aksjonspunktResultater = tjeneste.utledAksjonspunkter(behandling);

        //Assert
        assertThat(aksjonspunktResultater).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG));
    }

    @Test
    public void utsettelseFerieErTilpassetInntektsmeldingen() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);
        OppgittPeriode periode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medÅrsak(UtsettelseÅrsak.FERIE)
            .medBegrunnelse("bla bla")
            .medPeriode(fom, tom)
            .medVurdering(UttakPeriodeVurderingType.PERIODE_OK_ENDRET)
            .build();

        UttakPeriodeEditDistance uttakPeriodeEditDistance = tjeneste.mapPeriode(periode);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isTrue();
    }

    @Test
    public void utsettelseFerieKanIkkeAvklares() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);
        OppgittPeriode periode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medÅrsak(UtsettelseÅrsak.FERIE)
            .medBegrunnelse("bla bla")
            .medPeriode(fom, tom)
            .medVurdering(UttakPeriodeVurderingType.PERIODE_KAN_IKKE_AVKLARES)
            .build();

        UttakPeriodeEditDistance uttakPeriodeEditDistance = tjeneste.mapPeriode(periode);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isFalse();
    }


    @Test
    public void utsettelseArbeidErVurdertOkAvSaksbehandler() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);
        OppgittPeriode periode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medÅrsak(UtsettelseÅrsak.ARBEID)
            .medPeriode(fom, tom)
            .medBegrunnelse("bla bla")
            .medVurdering(UttakPeriodeVurderingType.PERIODE_OK)
            .build();

        UttakPeriodeEditDistance uttakPeriodeEditDistance = tjeneste.mapPeriode(periode);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isTrue();
    }

    @Test
    public void utsettelseArbeidErAutomatiskVurdertOkDokumentertAvInntektsmelding() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);
        OppgittPeriode periode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medÅrsak(UtsettelseÅrsak.ARBEID)
            .medPeriode(fom, tom)
            .medVurdering(UttakPeriodeVurderingType.PERIODE_IKKE_VURDERT)
            .build();

        UttakPeriodeEditDistance uttakPeriodeEditDistance = tjeneste.mapPeriode(periode);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isNull();
    }

    @Test
    public void utsettelseArbeidKanIkkeAvklares() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);
        OppgittPeriode periode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medÅrsak(UtsettelseÅrsak.ARBEID)
            .medPeriode(fom, tom)
            .medBegrunnelse("bla bla")
            .medVurdering(UttakPeriodeVurderingType.PERIODE_KAN_IKKE_AVKLARES)
            .build();

        UttakPeriodeEditDistance uttakPeriodeEditDistance = tjeneste.mapPeriode(periode);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isFalse();
    }


    @Test
    public void graderingErTilpassetInntektsmeldingen() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);
        OppgittPeriode periode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medArbeidsprosent(new BigDecimal(50))
            .medPeriode(fom, tom)
            .medBegrunnelse("bla bla")
            .medVurdering(UttakPeriodeVurderingType.PERIODE_OK_ENDRET)
            .build();

        UttakPeriodeEditDistance uttakPeriodeEditDistance = tjeneste.mapPeriode(periode);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isTrue();
    }

    @Test
    public void graderingKanIkkeAvklares() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);
        OppgittPeriode periode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medArbeidsprosent(new BigDecimal(50))
            .medPeriode(fom, tom)
            .medBegrunnelse("bla bla")
            .medVurdering(UttakPeriodeVurderingType.PERIODE_KAN_IKKE_AVKLARES)
            .build();

        UttakPeriodeEditDistance uttakPeriodeEditDistance = tjeneste.mapPeriode(periode);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isFalse();
    }

    @Test
    public void utsettelseSykSøkerDokumentert() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);
        OppgittPeriode periode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medÅrsak(UtsettelseÅrsak.SYKDOM)
            .medPeriode(fom, tom)
            .medVurdering(UttakPeriodeVurderingType.PERIODE_OK_ENDRET)
            .medBegrunnelse("bla bla")
            .build();

        UttakPeriodeEditDistance uttakPeriodeEditDistance = tjeneste.mapPeriode(periode);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isTrue();
    }

    @Test
    public void utsettelseSykSøkerIkkeDokumentert() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);
        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medÅrsak(UtsettelseÅrsak.SYKDOM)
            .medPeriode(fom, tom)
            .medVurdering(UttakPeriodeVurderingType.PERIODE_IKKE_VURDERT)
            .medBegrunnelse("bla bla")
            .build();

        // Ingen dokumenterte perioder
        UttakPeriodeEditDistance uttakPeriodeEditDistance = tjeneste.mapPeriode(periode1);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isFalse();

        OppgittPeriode periode2 = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medÅrsak(UtsettelseÅrsak.SYKDOM)
            .medPeriode(fom, tom)
            .medVurdering(UttakPeriodeVurderingType.PERIODE_OK)
            .medBegrunnelse("bla bla")
            .build();

        uttakPeriodeEditDistance = tjeneste.mapPeriode(periode2);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isTrue();
    }

    @Test
    public void utsettelseInnlagtSøkerDokumentert() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);
        OppgittPeriode periode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medÅrsak(UtsettelseÅrsak.INSTITUSJON_SØKER)
            .medPeriode(fom, tom)
            .medVurdering(UttakPeriodeVurderingType.PERIODE_OK)
            .medBegrunnelse("bla bla")
            .build();

        UttakPeriodeEditDistance uttakPeriodeEditDistance = tjeneste.mapPeriode(periode);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isTrue();

    }

    @Test
    public void utsettelseInnlagtBarnDokumentert() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);
        OppgittPeriode periode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medÅrsak(UtsettelseÅrsak.INSTITUSJON_BARN)
            .medPeriode(fom, tom)
            .medBegrunnelse("bla bla")
            .medVurdering(UttakPeriodeVurderingType.PERIODE_OK)
            .build();

        UttakPeriodeEditDistance uttakPeriodeEditDistance = tjeneste.mapPeriode(periode);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isTrue();
    }

    @Test
    public void mapperPeriodeSomIkkeErUtsettelseEllerGradering() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);
        OppgittPeriode periode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(fom, tom)
            .build();

        UttakPeriodeEditDistance uttakPeriodeEditDistance = tjeneste.mapPeriode(periode);
        assertThat(uttakPeriodeEditDistance.getPeriode()).isEqualTo(periode);
        assertThat(uttakPeriodeEditDistance.isPeriodeDokumentert()).isNull();
    }

    @Test
    public void finnesOverlappendePerioder() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);

        OppgittPeriode førstePeriode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(fom, tom)
            .build();

        OppgittPeriode andrePeriode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(tom, tom.plusDays(1))
            .build();
        Behandling behandling = opprettBehandlingForMorMedSøktePerioder(Arrays.asList(førstePeriode, andrePeriode));
        assertThat(tjeneste.finnesOverlappendePerioder(behandling)).isTrue();
    }

    @Test
    public void finnesIkkeOverlappendePerioder() {
        LocalDate fom = LocalDate.of(2018, 4, 18);
        LocalDate tom = fom.plusWeeks(1);

        OppgittPeriode førstePeriode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(fom, tom)
            .build();

        OppgittPeriode andrePeriode = OppgittPeriodeBuilder.ny().medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(tom.plusDays(1), tom.plusWeeks(1))
            .build();
        Behandling behandling = opprettBehandlingForMorMedSøktePerioder(Arrays.asList(førstePeriode, andrePeriode));
        assertThat(tjeneste.finnesOverlappendePerioder(behandling)).isFalse();
    }

    private void leggTilSøker(AbstractTestScenario<?> scenario, NavBrukerKjønn kjønn) {
        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();
        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .voksenPerson(søkerAktørId, SivilstandType.UOPPGITT, kjønn, Region.UDEFINERT)
            .build();
        scenario.medRegisterOpplysninger(søker);
    }
}
