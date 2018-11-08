package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

public class TilgrensendeYtelserDtoTest {
    private static final LocalDate I_DAG = LocalDate.now();

    @Test
    public void skal_sortere_null_first() {
        List<TilgrensendeYtelserDto> tilgrensendeYtelserDtos = Arrays.asList(
            lagTilgrensendeYtelserDto(I_DAG.minusYears(3)),
            lagTilgrensendeYtelserDto(I_DAG.minusDays(2)),
            lagTilgrensendeYtelserDto(I_DAG.plusWeeks(3)),
            lagTilgrensendeYtelserDto(I_DAG),
            lagTilgrensendeYtelserDto(null),
            lagTilgrensendeYtelserDto(I_DAG.plusYears(2)),
            lagTilgrensendeYtelserDto(I_DAG.minusMonths(1)),
            lagTilgrensendeYtelserDto(null),
            lagTilgrensendeYtelserDto(I_DAG.minusYears(1))
        );

        tilgrensendeYtelserDtos.sort(Comparator.naturalOrder());

        assertThat(tilgrensendeYtelserDtos.get(0).getPeriodeFraDato()).isNull();
        assertThat(tilgrensendeYtelserDtos.get(1).getPeriodeFraDato()).isNull();
        assertThat(tilgrensendeYtelserDtos.get(2).getPeriodeFraDato()).isEqualTo(I_DAG.plusYears(2));
        assertThat(tilgrensendeYtelserDtos.get(3).getPeriodeFraDato()).isEqualTo(I_DAG.plusWeeks(3));
        assertThat(tilgrensendeYtelserDtos.get(4).getPeriodeFraDato()).isEqualTo(I_DAG);
        assertThat(tilgrensendeYtelserDtos.get(5).getPeriodeFraDato()).isEqualTo(I_DAG.minusDays(2));
        assertThat(tilgrensendeYtelserDtos.get(6).getPeriodeFraDato()).isEqualTo(I_DAG.minusMonths(1));
        assertThat(tilgrensendeYtelserDtos.get(7).getPeriodeFraDato()).isEqualTo(I_DAG.minusYears(1));
        assertThat(tilgrensendeYtelserDtos.get(8).getPeriodeFraDato()).isEqualTo(I_DAG.minusYears(3));
    }

    private TilgrensendeYtelserDto lagTilgrensendeYtelserDto(LocalDate periodeFraDato) {
        TilgrensendeYtelserDto tilgrensendeYtelserDto = new TilgrensendeYtelserDto();
        tilgrensendeYtelserDto.setPeriodeFraDato(periodeFraDato);
        return tilgrensendeYtelserDto;
    }
}
