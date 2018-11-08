package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class StønadsPeriodeTest {

    private static final AktivitetIdentifikator AREBEIDSFORHOLD = AktivitetIdentifikator.forArbeid("000000000", "1");
    private LocalDate enUkedag = LocalDate.of(2018, 5, 4);

    @Test
    public void skal_beregne_trekkdager_når_periode_er_avvist_pga_manglende_omsorg() {
        StønadsPeriode periode = new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, enUkedag, enUkedag, false, false);
        periode.setPerioderesultattype(Perioderesultattype.AVSLÅTT);
        periode.setAvkortingårsaktype(Avkortingårsaktype.IKKE_OMSORG);

        assertThat(periode.getMinimumTrekkdager()).isEqualTo(1);
        assertThat(periode.getMaksimumTrekkdager()).isEqualTo(1);
    }

    @Test
    public void skal_ha_0_som_trekkdager_når_det_jobbes_over_100_prosent_og_er_søkt_om_gradering() {
        StønadsPeriode periode = StønadsPeriode.medGradering(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, enUkedag, enUkedag.plusWeeks(10).minusDays(1),
                Collections.singletonList(AREBEIDSFORHOLD), BigDecimal.valueOf(120), PeriodeVurderingType.PERIODE_OK);

        assertThat(periode.getMinimumTrekkdager()).isEqualTo(0); //det som trekkes på gradert arbeidsforhold
        assertThat(periode.getMaksimumTrekkdager()).isEqualTo(10 * 5); //det som trekkes på evt. andre arbeidsforhold
    }
}
