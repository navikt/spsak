package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmNokTilgjengeligDagerForUgyldigBegynnelseAvPeriodenTest {

    @Test
    public void harNokTilgjengeligeDager() {
        LocalDate periodeStart = LocalDate.of(2018, 1, 8);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = LocalDate.of(2018, 1, 11);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create().medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(gyldigGrunnStart, periodeSlutt)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 3)
                .build();

        SjekkOmNokTilgjengeligDagerForUgyldigBegynnelseAvPerioden sjekkDager = new SjekkOmNokTilgjengeligDagerForUgyldigBegynnelseAvPerioden();
        Evaluation evaluation = sjekkDager.evaluate(grunnlag);
        Assertions.assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void harIkkeNokTilgjengeligDager() {
        LocalDate periodeStart = LocalDate.of(2018, 1, 8);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = LocalDate.of(2018, 1, 11);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create().medOppholdPeriode(Stønadskontotype.MØDREKVOTE, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt)
                .medGyldigGrunnForTidligOppstartPeriode(gyldigGrunnStart, periodeSlutt)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 2)
                .build();

        SjekkOmNokTilgjengeligDagerForUgyldigBegynnelseAvPerioden sjekkDager = new SjekkOmNokTilgjengeligDagerForUgyldigBegynnelseAvPerioden();
        Evaluation evaluation = sjekkDager.evaluate(grunnlag);
        Assertions.assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

}
