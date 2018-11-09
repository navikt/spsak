package no.nav.foreldrepenger.inngangsvilkaar.opptjeningsperiode;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.medlem.impl.MedlemskapPerioderTjenesteImpl;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.impl.BasisPersonopplysningTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnMorsMaksdatoTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.inngangsvilkaar.impl.InngangsvilkårOversetter;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjeningsPeriode;

public class OpptjeningsperiodeVilkårTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private BasisPersonopplysningTjeneste personopplysningTjeneste = new BasisPersonopplysningTjenesteImpl(repositoryProvider, skjæringstidspunktTjeneste);
    private BeregnMorsMaksdatoTjeneste beregnMorsMaksdatoTjeneste = new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider));
    private InngangsvilkårOversetter oversetter = new InngangsvilkårOversetter(repositoryProvider,
        new MedlemskapPerioderTjenesteImpl(12, 6, skjæringstidspunktTjeneste), skjæringstidspunktTjeneste, personopplysningTjeneste,
        beregnMorsMaksdatoTjeneste);

    @Test
    public void skal_fastsette_periode_med_termindato() {
        final LocalDate skjæringstidspunkt = LocalDate.now().plusWeeks(1L).minusDays(1L);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().plusWeeks(4L))
                .medUtstedtDato(LocalDate.now())
                .medNavnPå("Doktor Dankel"));
        Behandling behandling = scenario.lagre(repositoryProvider);
        final OppgittPeriodeBuilder oppgittPeriodeBuilder = OppgittPeriodeBuilder.ny().medPeriode(LocalDate.now().plusWeeks(2), LocalDate.now().plusWeeks(4)).medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriodeBuilder.build()), true));

        VilkårData data = new InngangsvilkårOpptjeningsperiode(oversetter, Period.parse("P10M"), Period.parse("P12W")).vurderVilkår(behandling);

        OpptjeningsPeriode op = (OpptjeningsPeriode) data.getEkstraVilkårresultat();
        Assertions.assertThat(op.getOpptjeningsperiodeTom()).isEqualTo(skjæringstidspunkt);
        Assertions.assertThat(op.getOpptjeningsperiodeFom()).isEqualTo(op.getOpptjeningsperiodeTom().plusDays(1).minusMonths(10L));
    }

    @Test
    public void skal_fastsette_periode_ved_fødsel_mor() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medBekreftetHendelse().medFødselsDato(LocalDate.now()).medAntallBarn(Integer.valueOf(1));
        Behandling behandling = scenario.lagre(repositoryProvider);
        final OppgittPeriodeBuilder oppgittPeriodeBuilder = OppgittPeriodeBuilder.ny().medPeriode(LocalDate.now().plusWeeks(2), LocalDate.now().plusWeeks(4)).medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriodeBuilder.build()), true));

        VilkårData data = new InngangsvilkårOpptjeningsperiode(oversetter, Period.parse("P10M"), Period.parse("P12W")).vurderVilkår(behandling);

        OpptjeningsPeriode op = (OpptjeningsPeriode) data.getEkstraVilkårresultat();
        Assertions.assertThat(op.getOpptjeningsperiodeTom()).isEqualTo(LocalDate.now().minusDays(1L));
    }

    @Test
    public void skal_fastsette_periode_ved_tidlig_uttak_termin_fødsel_mor() {
        final LocalDate skjæringstidspunkt = LocalDate.now().plusWeeks(1L).minusDays(1);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().plusWeeks(13L))
                .medUtstedtDato(LocalDate.now())
                .medNavnPå("Doktor Dankel"));
        scenario.medBekreftetHendelse().medFødselsDato(LocalDate.now().plusWeeks(14)).medAntallBarn(Integer.valueOf(1));
        Behandling behandling = scenario.lagre(repositoryProvider);
        final OppgittPeriodeBuilder oppgittPeriodeBuilder = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now().plusWeeks(1), LocalDate.now().plusWeeks(10).minusDays(1)).medPeriodeType(UttakPeriodeType.FELLESPERIODE);
        final OppgittPeriodeBuilder oppgittPeriodeBuilder2 = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now().plusWeeks(10), LocalDate.now().plusWeeks(13)).medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL);
        repositoryProvider.getYtelsesFordelingRepository()
            .lagre(behandling, new OppgittFordelingEntitet(Arrays.asList(oppgittPeriodeBuilder.build(), oppgittPeriodeBuilder2.build()), true));

        VilkårData data = new InngangsvilkårOpptjeningsperiode(oversetter, Period.parse("P10M"), Period.parse("P12W")).vurderVilkår(behandling);

        OpptjeningsPeriode op = (OpptjeningsPeriode) data.getEkstraVilkårresultat();
        Assertions.assertThat(op.getOpptjeningsperiodeTom()).isEqualTo(skjæringstidspunkt);
        Assertions.assertThat(op.getOpptjeningsperiodeFom()).isEqualTo(op.getOpptjeningsperiodeTom().plusDays(1).minusMonths(10L));
    }


    @Test
    public void skal_fastsette_periode_ved_fødsel_far() {
        LocalDate fødselsdato = LocalDate.now();
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forFødsel();
        scenario.medBekreftetHendelse().medFødselsDato(fødselsdato).medAntallBarn(Integer.valueOf(1));
        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId barnAktørId = new AktørId("123");
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon fødtBarn = builderForRegisteropplysninger
            .medPersonas()
            .fødtBarn(barnAktørId, fødselsdato)
            .relasjonTil(søkerAktørId, RelasjonsRolleType.MORA, null)
            .build();

        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .kvinne(søkerAktørId, SivilstandType.GIFT, Region.NORDEN)
            .statsborgerskap(Landkoder.NOR)
            .relasjonTil(barnAktørId, RelasjonsRolleType.BARN, true)
            .build();
        scenario.medRegisterOpplysninger(søker);
        scenario.medRegisterOpplysninger(fødtBarn);
        Behandling behandling = scenario.lagre(repositoryProvider);
        final OppgittPeriodeBuilder oppgittPeriodeBuilder = OppgittPeriodeBuilder.ny().medPeriode(LocalDate.now().minusDays(1L), LocalDate.now().plusWeeks(4)).medPeriodeType(UttakPeriodeType.FEDREKVOTE);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriodeBuilder.build()), true));

        VilkårData data = new InngangsvilkårOpptjeningsperiode(oversetter, Period.parse("P10M"), Period.parse("P12W")).vurderVilkår(behandling);

        OpptjeningsPeriode op = (OpptjeningsPeriode) data.getEkstraVilkårresultat();
        Assertions.assertThat(op.getOpptjeningsperiodeTom()).isEqualTo(LocalDate.now().minusDays(1L));
    }

    @Test
    public void skal_fastsette_periode_ved_adopsjon_mor_søker() {
        Behandling behandling = this.settOppAdopsjonBehandlingForMor(10, false, NavBrukerKjønn.KVINNE, false);
        final OppgittPeriodeBuilder oppgittPeriodeBuilder = OppgittPeriodeBuilder.ny().medPeriode(LocalDate.of(2018, 1, 1).minusDays(1L), LocalDate.now().plusWeeks(4)).medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriodeBuilder.build()), true));

        VilkårData data = new InngangsvilkårOpptjeningsperiode(oversetter, Period.parse("P10M"), Period.parse("P12W")).vurderVilkår(behandling);

        OpptjeningsPeriode op = (OpptjeningsPeriode) data.getEkstraVilkårresultat();
        Assertions.assertThat(op.getOpptjeningsperiodeTom()).isEqualTo(LocalDate.of(2018, 1, 1).minusDays(1L));
    }

    @Test
    public void skal_fastsette_periode_ved_adopsjon_far_søker() {
        Behandling behandling = this.settOppAdopsjonBehandlingForMor(10, false, NavBrukerKjønn.MANN, false);
        final OppgittPeriodeBuilder oppgittPeriodeBuilder = OppgittPeriodeBuilder.ny().medPeriode(LocalDate.of(2018, 1, 1).minusDays(1L), LocalDate.now().plusWeeks(4)).medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriodeBuilder.build()), true));

        VilkårData data = new InngangsvilkårOpptjeningsperiode(oversetter, Period.parse("P10M"), Period.parse("P12W")).vurderVilkår(behandling);

        OpptjeningsPeriode op = (OpptjeningsPeriode) data.getEkstraVilkårresultat();
        Assertions.assertThat(op.getOpptjeningsperiodeTom()).isEqualTo(LocalDate.of(2018, 1, 1).minusDays(1L));
    }

    private Behandling settOppAdopsjonBehandlingForMor(int alder, boolean ektefellesBarn, NavBrukerKjønn kjønn, boolean adoptererAlene) {
        LocalDate omsorgsovertakelsedato = LocalDate.of(2018, 1, 1);
        if (kjønn.equals(NavBrukerKjønn.KVINNE)) {
            ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forAdopsjon();
            scenario.medBekreftetHendelse().medAdopsjon(scenario.medBekreftetHendelse().getAdopsjonBuilder().medOmsorgsovertakelseDato(omsorgsovertakelsedato).medErEktefellesBarn(ektefellesBarn).medAdoptererAlene(adoptererAlene)).leggTilBarn(omsorgsovertakelsedato.minusYears(alder));
            Behandling behandling = scenario.lagre(repositoryProvider);
            return behandling;
        } else {
            ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
            scenario.medBekreftetHendelse().medAdopsjon(scenario.medBekreftetHendelse().getAdopsjonBuilder().medOmsorgsovertakelseDato(omsorgsovertakelsedato).medErEktefellesBarn(ektefellesBarn).medAdoptererAlene(adoptererAlene)).leggTilBarn(omsorgsovertakelsedato.minusYears(alder));
            Behandling behandling = scenario.lagre(repositoryProvider);
            return behandling;
        }
    }
}
