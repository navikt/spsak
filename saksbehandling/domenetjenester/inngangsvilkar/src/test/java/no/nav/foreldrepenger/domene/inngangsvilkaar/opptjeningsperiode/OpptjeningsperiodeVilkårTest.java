package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjeningsperiode;

import java.time.LocalDate;
import java.time.Period;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingerBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårData;
import no.nav.foreldrepenger.domene.inngangsvilkaar.impl.InngangsvilkårOversetter;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.OpptjeningsPeriode;
import no.nav.foreldrepenger.domene.medlem.impl.MedlemskapPerioderTjenesteImpl;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.impl.BasisPersonopplysningTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

@Ignore("FIXME SP: skriv om vilkår til sykepenger regler")
public class OpptjeningsperiodeVilkårTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, resultatRepositoryProvider);
    private BasisPersonopplysningTjeneste personopplysningTjeneste = new BasisPersonopplysningTjenesteImpl(repositoryProvider, skjæringstidspunktTjeneste);
    private InngangsvilkårOversetter oversetter = new InngangsvilkårOversetter(repositoryProvider,
        new MedlemskapPerioderTjenesteImpl(12, 6, skjæringstidspunktTjeneste), skjæringstidspunktTjeneste, personopplysningTjeneste);

    @Test
    public void skal_fastsette_periode_ved_fødsel_mor() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        SykemeldingerBuilder builder = scenario.getSykemeldingerBuilder();
        SykemeldingBuilder sykemeldingBuilder = builder.sykemeldingBuilder("ASDF-ASDF-ASDF");
        sykemeldingBuilder.medPeriode(LocalDate.now(), LocalDate.now().plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)))
            .medGrad(new Prosentsats(100));
        builder.medSykemelding(sykemeldingBuilder);
        scenario.medSykemeldinger(builder);
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        VilkårData data = new InngangsvilkårOpptjeningsperiode(oversetter, Period.parse("P10M")).vurderVilkår(behandling);

        OpptjeningsPeriode op = (OpptjeningsPeriode) data.getEkstraVilkårresultat();
        Assertions.assertThat(op.getOpptjeningsperiodeTom()).isEqualTo(LocalDate.now().minusDays(1L));
    }

    @Test
    public void skal_fastsette_periode_ved_tidlig_uttak_termin_fødsel_mor() {
        final LocalDate skjæringstidspunkt = LocalDate.now().plusWeeks(1L).minusDays(1);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        SykemeldingerBuilder builder = scenario.getSykemeldingerBuilder();
        SykemeldingBuilder sykemeldingBuilder = builder.sykemeldingBuilder("ASDF-ASDF-ASDF");
        sykemeldingBuilder.medPeriode(skjæringstidspunkt, skjæringstidspunkt.plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)))
            .medGrad(new Prosentsats(100));
        builder.medSykemelding(sykemeldingBuilder);
        scenario.medSykemeldinger(builder);
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        VilkårData data = new InngangsvilkårOpptjeningsperiode(oversetter, Period.parse("P10M")).vurderVilkår(behandling);

        OpptjeningsPeriode op = (OpptjeningsPeriode) data.getEkstraVilkårresultat();
        Assertions.assertThat(op.getOpptjeningsperiodeTom()).isEqualTo(skjæringstidspunkt.minusDays(1L));
        Assertions.assertThat(op.getOpptjeningsperiodeFom()).isEqualTo(op.getOpptjeningsperiodeTom().plusDays(1).minusMonths(10L));
    }

    @Test
    public void skal_fastsette_periode_ved_adopsjon_mor_søker() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        SykemeldingerBuilder builder = scenario.getSykemeldingerBuilder();
        SykemeldingBuilder sykemeldingBuilder = builder.sykemeldingBuilder("ASDF-ASDF-ASDF");
        sykemeldingBuilder.medPeriode(LocalDate.now(), LocalDate.now().plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)))
            .medGrad(new Prosentsats(100));
        builder.medSykemelding(sykemeldingBuilder);
        scenario.medSykemeldinger(builder);
        Behandling behandling = scenario.lagre(this.repositoryProvider, resultatRepositoryProvider);

        VilkårData data = new InngangsvilkårOpptjeningsperiode(oversetter, Period.parse("P10M")).vurderVilkår(behandling);

        OpptjeningsPeriode op = (OpptjeningsPeriode) data.getEkstraVilkårresultat();
        Assertions.assertThat(op.getOpptjeningsperiodeTom()).isEqualTo(LocalDate.now().minusDays(1L));
    }

}
