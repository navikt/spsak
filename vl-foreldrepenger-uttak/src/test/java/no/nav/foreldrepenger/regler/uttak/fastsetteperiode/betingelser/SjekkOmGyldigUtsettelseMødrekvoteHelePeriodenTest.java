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

public class SjekkOmGyldigUtsettelseMødrekvoteHelePeriodenTest {


    @Test
    public void ingenGyldigGrunnPeriode() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(2);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .build();

        SjekkOmGyldigUtsettelseMødrekvoteHelePerioden sjekkOmGyldigHelePerioden = new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden();
        Evaluation evaluation = sjekkOmGyldigHelePerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void helePeriodeErUgyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(2);
        LocalDate gyldigGrunnStart = periodeStart.minusWeeks(1);
        LocalDate gyldigGrunnSlutt = periodeStart.minusDays(1);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(gyldigGrunnStart, gyldigGrunnSlutt)
                .build();

        SjekkOmGyldigUtsettelseMødrekvoteHelePerioden sjekkOmGyldigHelePerioden = new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden();
        Evaluation evaluation = sjekkOmGyldigHelePerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }


    @Test
    public void bareBegynnelseAvPeriodeErGyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeStart.minusWeeks(1);
        LocalDate gyldigGrunnSlutt = periodeStart.plusWeeks(1);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(gyldigGrunnStart, gyldigGrunnSlutt)
                .build();

        SjekkOmGyldigUtsettelseMødrekvoteHelePerioden sjekkOmGyldigHelePerioden = new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden();
        Evaluation evaluation = sjekkOmGyldigHelePerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void sisteDagIPeriodenErUgyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeStart.minusWeeks(1);
        LocalDate gyldigGrunnSlutt = periodeSlutt.minusDays(1);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(gyldigGrunnStart, gyldigGrunnSlutt)
                .build();

        SjekkOmGyldigUtsettelseMødrekvoteHelePerioden sjekkOmGyldigHelePerioden = new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden();
        Evaluation evaluation = sjekkOmGyldigHelePerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void bareMidtenAvPeriodenErGyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeStart.plusWeeks(1);
        LocalDate gyldigGrunnSlutt = gyldigGrunnStart.plusDays(5);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(gyldigGrunnStart, gyldigGrunnSlutt)
                .build();

        SjekkOmGyldigUtsettelseMødrekvoteHelePerioden sjekkOmGyldigHelePerioden = new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden();
        Evaluation evaluation = sjekkOmGyldigHelePerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void bareSluttenAvPeriodenErGyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeSlutt.minusWeeks(1);
        LocalDate gyldigGrunnSlutt = periodeSlutt.plusDays(5);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(gyldigGrunnStart, gyldigGrunnSlutt)
                .build();

        SjekkOmGyldigUtsettelseMødrekvoteHelePerioden sjekkOmGyldigHelePerioden = new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden();
        Evaluation evaluation = sjekkOmGyldigHelePerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void bareBegynnelsenOgSluttenAvPeriodenErGyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(periodeStart.minusDays(1), periodeStart.plusDays(7))
                .medGyldigGrunnForTidligOppstartPeriode(periodeSlutt.minusDays(5), periodeSlutt.plusDays(1))
                .build();

        SjekkOmGyldigUtsettelseMødrekvoteHelePerioden sjekkOmGyldigHelePerioden = new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden();
        Evaluation evaluation = sjekkOmGyldigHelePerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void helePeriodenGyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeStart;
        LocalDate gyldigGrunnSlutt = periodeSlutt;

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(gyldigGrunnStart, gyldigGrunnSlutt)
                .build();

        SjekkOmGyldigUtsettelseMødrekvoteHelePerioden sjekkOmGyldigHelePerioden = new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden();
        Evaluation evaluation = sjekkOmGyldigHelePerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void helePeriodenGyldigMedFlereGyldigGrunnPerioder() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(periodeStart, periodeStart.plusDays(7))
                .medGyldigGrunnForTidligOppstartPeriode(periodeStart.plusDays(8), periodeSlutt)
                .build();

        SjekkOmGyldigUtsettelseMødrekvoteHelePerioden sjekkOmGyldigHelePerioden = new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden();
        Evaluation evaluation = sjekkOmGyldigHelePerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void helePeriodenGyldigMedFlereGyldigGrunnPerioderSomOverlapper() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(periodeStart, periodeStart.plusDays(10))
                .medGyldigGrunnForTidligOppstartPeriode(periodeStart.plusDays(7), periodeSlutt)
                .build();

        SjekkOmGyldigUtsettelseMødrekvoteHelePerioden sjekkOmGyldigHelePerioden = new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden();
        Evaluation evaluation = sjekkOmGyldigHelePerioden.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }
}
