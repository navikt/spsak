package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmDelerAvPeriodenHarGyldigGrunnTest {

    @Test
    public void begynnelsenAvPeriodenHarGyldigGrunn() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(2);
        LocalDate gyldigGrunnStart = periodeStart;
        LocalDate gyldigGrunnSlutt = periodeStart.plusDays(1);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
            .medGyldigGrunnForTidligOppstartPeriode(gyldigGrunnStart, gyldigGrunnSlutt)
            .build();

        SjekkOmDelerAvPeriodenHarGyldigGrunn sjekkOmGyldigDelerAvPerioden = new SjekkOmDelerAvPeriodenHarGyldigGrunn();
        Evaluation evaluation = sjekkOmGyldigDelerAvPerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void midtenAvPeriodenHarGyldigGrunn() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeStart.plusWeeks(3);
        LocalDate gyldigGrunnSlutt = periodeStart.plusWeeks(4);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
            .medGyldigGrunnForTidligOppstartPeriode(gyldigGrunnStart, gyldigGrunnSlutt)
            .build();

        SjekkOmDelerAvPeriodenHarGyldigGrunn sjekkOmGyldigDelerAvPerioden = new SjekkOmDelerAvPeriodenHarGyldigGrunn();
        Evaluation evaluation = sjekkOmGyldigDelerAvPerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void sluttenAvPeriodenHarGyldigGrunn() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeStart.plusWeeks(5);
        LocalDate gyldigGrunnSlutt = periodeSlutt;

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
            .medGyldigGrunnForTidligOppstartPeriode(gyldigGrunnStart, gyldigGrunnSlutt)
            .build();

        SjekkOmDelerAvPeriodenHarGyldigGrunn sjekkOmGyldigDelerAvPerioden = new SjekkOmDelerAvPeriodenHarGyldigGrunn();
        Evaluation evaluation = sjekkOmGyldigDelerAvPerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void helePeriodenErUgyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
            .medGyldigGrunnForTidligOppstartPeriode(periodeStart.minusWeeks(1), periodeStart.minusDays(1))
            .medGyldigGrunnForTidligOppstartPeriode(periodeSlutt.plusDays(1), periodeSlutt.plusWeeks(1))
            .build();

        SjekkOmDelerAvPeriodenHarGyldigGrunn sjekkOmGyldigDelerAvPerioden = new SjekkOmDelerAvPeriodenHarGyldigGrunn();
        Evaluation evaluation = sjekkOmGyldigDelerAvPerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void ingenGyldigGrunnPerioder() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
            .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
            .build();

        SjekkOmDelerAvPeriodenHarGyldigGrunn sjekkOmGyldigDelerAvPerioden = new SjekkOmDelerAvPeriodenHarGyldigGrunn();
        Evaluation evaluation = sjekkOmGyldigDelerAvPerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

}
