package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjening;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.time.LocalDate;
import java.time.Period;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårJsonObjectMapper;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårData;
import no.nav.foreldrepenger.domene.inngangsvilkaar.impl.VilkårUtfallOversetter;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.Opptjeningsgrunnlag;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.OpptjeningsvilkårResultat;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class InngangsvilkårForeldrepengerOpptjeningTest {

    @Test
    public void ikke_duplikat_mellom_avslått_periode_og_mellomliggende() throws Exception {
        VilkårJsonObjectMapper jsonMapper = new VilkårJsonObjectMapper();

        URL resource = InngangsvilkårForeldrepengerOpptjening.class.getResource("/opptjening/ingen-mellomliggende.json");
        Opptjeningsgrunnlag grunnlag = jsonMapper.readValue(resource, Opptjeningsgrunnlag.class);

        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        Evaluation evaluation = new Opptjeningsvilkår().evaluer(grunnlag, output);

        VilkårKodeverkRepositoryImpl vilkårKodeverkRepository = new VilkårKodeverkRepositoryImpl(null, KodeverkTestHelper.getKodeverkRepository());
        VilkårUtfallOversetter vilkårUtfallOversetter = new VilkårUtfallOversetter(vilkårKodeverkRepository);
        VilkårData vilkårData = vilkårUtfallOversetter.oversett(VilkårType.OPPTJENINGSVILKÅRET, evaluation, grunnlag);

        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);

        assertThat(output.getResultatOpptjent()).isEqualTo(Period.parse("P9M19D"));
        assertThat(output.getAkseptertMellomliggendePerioder()).isEmpty();
    }

    @Test
    public void test_beregn_opptjening_fra_vilkår_input_data_som_gir_opptjening_P9M18D() {
        VilkårJsonObjectMapper jsonMapper = new VilkårJsonObjectMapper();

        URL resource = InngangsvilkårForeldrepengerOpptjening.class.getResource("/opptjening/fpfeil-1252.json");
        Opptjeningsgrunnlag grunnlag = jsonMapper.readValue(resource, Opptjeningsgrunnlag.class);

        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        Evaluation evaluation = new Opptjeningsvilkår().evaluer(grunnlag, output);

        VilkårKodeverkRepositoryImpl vilkårKodeverkRepository = new VilkårKodeverkRepositoryImpl(null, KodeverkTestHelper.getKodeverkRepository());
        VilkårUtfallOversetter vilkårUtfallOversetter = new VilkårUtfallOversetter(vilkårKodeverkRepository);
        VilkårData vilkårData = vilkårUtfallOversetter.oversett(VilkårType.OPPTJENINGSVILKÅRET, evaluation, grunnlag);

        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);

        assertThat(output.getResultatOpptjent()).isEqualTo(Period.parse("P9M18D"));
        assertThat(output.getAkseptertMellomliggendePerioder()).isEmpty();
        assertThat(output.getUnderkjentePerioder()).hasSize(1);
        assertThat(output.getBekreftetGodkjentePerioder()).hasSize(1);

        LocalDateTimeline<Boolean> expectedTimeline = new LocalDateTimeline<>(new LocalDateInterval(LocalDate.parse("2016-10-14"), LocalDate.parse("2017-07-31")), Boolean.TRUE);
        assertThat(output.getResultatTidslinje()).isEqualTo(expectedTimeline);
    }

    @Test
    public void test_beregn_opptjening_fra_vilkår_input_data_som_gir_opptjening_P7M18D_med_utlandsk_arbeidshold() {
        VilkårJsonObjectMapper jsonMapper = new VilkårJsonObjectMapper();

        URL resource = InngangsvilkårForeldrepengerOpptjening.class.getResource("/opptjening/pk-53505_1.json");
        Opptjeningsgrunnlag grunnlag = jsonMapper.readValue(resource, Opptjeningsgrunnlag.class);

        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        Evaluation evaluation = new Opptjeningsvilkår().evaluer(grunnlag, output);

        VilkårKodeverkRepositoryImpl vilkårKodeverkRepository = new VilkårKodeverkRepositoryImpl(null, KodeverkTestHelper.getKodeverkRepository());
        VilkårUtfallOversetter vilkårUtfallOversetter = new VilkårUtfallOversetter(vilkårKodeverkRepository);
        VilkårData vilkårData = vilkårUtfallOversetter.oversett(VilkårType.OPPTJENINGSVILKÅRET, evaluation, grunnlag);

        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);

        assertThat(output.getResultatOpptjent()).isEqualTo(Period.parse("P7M18D"));
        assertThat(output.getAkseptertMellomliggendePerioder()).isEmpty();
        assertThat(output.getUnderkjentePerioder()).hasSize(1);
        assertThat(output.getBekreftetGodkjentePerioder()).hasSize(2);

        LocalDateTimeline<Boolean> expectedTimeline = new LocalDateTimeline<>(new LocalDateInterval(LocalDate.parse("2016-10-14"), LocalDate.parse("2017-05-31")), Boolean.TRUE);
        assertThat(output.getResultatTidslinje()).isEqualTo(expectedTimeline);
    }

    @Test
    public void test_beregn_opptjening_fra_vilkår_input_data_som_gir_opptjening_med_utlandsk_arbeidshold_før_norsk_P4M() {
        VilkårJsonObjectMapper jsonMapper = new VilkårJsonObjectMapper();

        URL resource = InngangsvilkårForeldrepengerOpptjening.class.getResource("/opptjening/pk-53505_2.json");
        Opptjeningsgrunnlag grunnlag = jsonMapper.readValue(resource, Opptjeningsgrunnlag.class);

        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        Evaluation evaluation = new Opptjeningsvilkår().evaluer(grunnlag, output);

        VilkårKodeverkRepositoryImpl vilkårKodeverkRepository = new VilkårKodeverkRepositoryImpl(null, KodeverkTestHelper.getKodeverkRepository());
        VilkårUtfallOversetter vilkårUtfallOversetter = new VilkårUtfallOversetter(vilkårKodeverkRepository);
        VilkårData vilkårData = vilkårUtfallOversetter.oversett(VilkårType.OPPTJENINGSVILKÅRET, evaluation, grunnlag);

        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);

        assertThat(output.getResultatOpptjent()).isEqualTo(Period.parse("P9M5D"));
        assertThat(output.getAkseptertMellomliggendePerioder()).isEmpty();
        assertThat(output.getUnderkjentePerioder()).hasSize(1);
        assertThat(output.getBekreftetGodkjentePerioder()).hasSize(2);

        assertThat(output.getResultatTidslinje().getMinLocalDate()).isEqualTo(LocalDate.parse("2017-03-31"));
        assertThat(output.getResultatTidslinje().getMaxLocalDate()).isEqualTo(LocalDate.parse("2018-01-30"));
    }
    @Test
    public void avslå_med_kun_utlandsk_arbeidshold_før_norsk() {
        VilkårJsonObjectMapper jsonMapper = new VilkårJsonObjectMapper();

        URL resource = InngangsvilkårForeldrepengerOpptjening.class.getResource("/opptjening/pk-53505_3.json");
        Opptjeningsgrunnlag grunnlag = jsonMapper.readValue(resource, Opptjeningsgrunnlag.class);

        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        Evaluation evaluation = new Opptjeningsvilkår().evaluer(grunnlag, output);

        VilkårKodeverkRepositoryImpl vilkårKodeverkRepository = new VilkårKodeverkRepositoryImpl(null, KodeverkTestHelper.getKodeverkRepository());
        VilkårUtfallOversetter vilkårUtfallOversetter = new VilkårUtfallOversetter(vilkårKodeverkRepository);
        VilkårData vilkårData = vilkårUtfallOversetter.oversett(VilkårType.OPPTJENINGSVILKÅRET, evaluation, grunnlag);

        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);

        assertThat(output.getAkseptertMellomliggendePerioder()).isEmpty();
        assertThat(output.getUnderkjentePerioder()).hasSize(1);
        assertThat(output.getBekreftetGodkjentePerioder()).isEmpty();
    }

}
