package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmTomForAlleSineKontoerTest {

    @Test
    public void når_søknadstype_er_fødsel_og_søker_er_mor_og_begge_har_rett_skal_søker_sine_kontoer_vær_MK_FP_og_FORELDREPENGER() { // 1

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medSøkerMor(true)
            .medMorRett(true)
            .medFarRett(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .build();

        SjekkOmTomForAlleSineKontoer sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();

        List<Stønadskontotype> stønadskontotypene = sjekkOmTomForAlleSineKontoer.hentSøkerSineKonto(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søker_har_kontoene_FPFF_MK_FP_er_søker_ikke_tom_for_alle_sine_konto_selvom_en_konto_er_tom() {
        LocalDate periodeStart = LocalDate.of(2018, 1, 8);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt, PeriodeVurderingType.PERIODE_OK)
            .medSøkerMor(true)
            .medMorRett(true)
            .medFarRett(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medSaldo(Stønadskontotype.MØDREKVOTE, 15*5)
            .medSaldo(Stønadskontotype.FELLESPERIODE, 10*5)
            .build();

        SjekkOmTomForAlleSineKontoer sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();
        Evaluation evaluation = sjekkOmTomForAlleSineKontoer.evaluate(grunnlag);
        Assertions.assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void når_søknadstype_er_fødsel_og_søker_er_mor_og_kun_mor_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 2

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medSøkerMor(true)
            .medMorRett(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .build();

        SjekkOmTomForAlleSineKontoer sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();

        List<Stønadskontotype> stønadskontotypene = sjekkOmTomForAlleSineKontoer.hentSøkerSineKonto(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_fødsel_og_søker_er_far_og_begge_har_rett_skal_søker_sine_kontoer_være_FK_FP_og_FORELDREPENGER() { // 3

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medSøkerMor(false)
            .medMorRett(true)
            .medFarRett(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .build();

        SjekkOmTomForAlleSineKontoer sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();

        List<Stønadskontotype> stønadskontotypene = sjekkOmTomForAlleSineKontoer.hentSøkerSineKonto(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FEDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_fødsel_og_søker_er_far_og_kun_far_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 4

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medSøkerMor(false)
            .medFarRett(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .build();

        SjekkOmTomForAlleSineKontoer sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();

        List<Stønadskontotype> stønadskontotypene = sjekkOmTomForAlleSineKontoer.hentSøkerSineKonto(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_adopsjon_og_søker_er_mor_og_begge_har_rett_skal_søker_sine_kontoer_være_MK_FP_og_FORELDREPENGER() { // 5

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medSøkerMor(true)
            .medMorRett(true)
            .medFarRett(true)
            .medSøknadstype(Søknadstype.ADOPSJON)
            .build();

        SjekkOmTomForAlleSineKontoer sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();

        List<Stønadskontotype> stønadskontotypene = sjekkOmTomForAlleSineKontoer.hentSøkerSineKonto(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_adopsjon_og_søker_er_mor_og_kun_mor_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 6

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medSøkerMor(true)
            .medMorRett(true)
            .medSøknadstype(Søknadstype.ADOPSJON)
            .build();

        SjekkOmTomForAlleSineKontoer sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();

        List<Stønadskontotype> stønadskontotypene = sjekkOmTomForAlleSineKontoer.hentSøkerSineKonto(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_adopsjon_og_søker_er_far_og_begge_har_rett_skal_søker_sine_kontoer_være_FK_FP_og_FORELDREPENGER() { // 7

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medSøkerMor(false)
            .medMorRett(true)
            .medFarRett(true)
            .medSøknadstype(Søknadstype.ADOPSJON)
            .build();

        SjekkOmTomForAlleSineKontoer sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();

        List<Stønadskontotype> stønadskontotypene = sjekkOmTomForAlleSineKontoer.hentSøkerSineKonto(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FEDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_adopsjon_og_søker_er_far_og_kun_en_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 8

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medSøkerMor(false)
            .medFarRett(true)
            .medSøknadstype(Søknadstype.ADOPSJON)
            .build();

        SjekkOmTomForAlleSineKontoer sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();

        List<Stønadskontotype> stønadskontotypene = sjekkOmTomForAlleSineKontoer.hentSøkerSineKonto(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }
}