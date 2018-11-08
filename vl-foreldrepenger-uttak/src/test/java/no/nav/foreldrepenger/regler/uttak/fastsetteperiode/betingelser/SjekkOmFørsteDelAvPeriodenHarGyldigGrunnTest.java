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

public class SjekkOmFørsteDelAvPeriodenHarGyldigGrunnTest {


    @Test
    public void førsteDelGyldigGrunn() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(periodeStart, periodeStart.plusDays(1))
                .build();

        SjekkOmFørsteDelAvPeriodenHarGyldigGrunn sjekkOmFørsteDelHarGyldigGrunn = new SjekkOmFørsteDelAvPeriodenHarGyldigGrunn();
        Evaluation evaluation = sjekkOmFørsteDelHarGyldigGrunn.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void førsteDelIkkeGyldigGrunn() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(periodeSlutt.minusDays(2), periodeSlutt)
                .build();

        SjekkOmFørsteDelAvPeriodenHarGyldigGrunn sjekkOmFørsteDelHarGyldigGrunn = new SjekkOmFørsteDelAvPeriodenHarGyldigGrunn();
        Evaluation evaluation = sjekkOmFørsteDelHarGyldigGrunn.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

}
