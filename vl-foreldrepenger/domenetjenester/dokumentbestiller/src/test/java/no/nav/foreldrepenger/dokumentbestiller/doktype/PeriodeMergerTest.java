package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.ArbeidsforholdDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.PeriodeDto;
import no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMerger;

public class PeriodeMergerTest {

    @Test
    public void skalMappeTilFoerstegangssoknadHvisFørstegangssøknad() {
        List<PeriodeDto> input = new ArrayList<>();

        opprettPerioder(input);

        List<PeriodeDto> periodeDtos = PeriodeMerger.mergePerioder(input);

        assertThat(periodeDtos.size()).isEqualTo(3);
        assertThat(periodeDtos.get(0).getAntallTapteDager()).isEqualTo(6);
    }

    private void opprettPerioder(List<PeriodeDto> input) {
        PeriodeDto dto = new PeriodeDto();

        dto.setÅrsak("2345");
        dto.setAntallTapteDager(2);
        dto.setInnvilget(true);
        dto.setPeriodeFom("2018-06-01");
        dto.setPeriodeTom("2018-06-30");

        ArbeidsforholdDto adt = new ArbeidsforholdDto();
        adt.setUtbetalingsgrad(100);
        adt.setGradering(false);
        dto.getArbeidsforhold().add(adt);

        input.add(dto);

        PeriodeDto dto2 = new PeriodeDto();

        dto2.setÅrsak("2345");
        dto2.setAntallTapteDager(4);
        dto2.setInnvilget(true);
        dto2.setPeriodeFom("2018-07-01");
        dto2.setPeriodeTom("2018-07-10");
        ArbeidsforholdDto adt2 = new ArbeidsforholdDto();
        adt2.setUtbetalingsgrad(100);
        adt2.setGradering(false);
        dto.getArbeidsforhold().add(adt2);

        input.add(dto2);

        PeriodeDto dto3 = new PeriodeDto();

        dto3.setÅrsak("4006");
        dto3.setAntallTapteDager(4);
        dto3.setInnvilget(false);
        dto3.setPeriodeFom("2018-07-11");
        dto3.setPeriodeTom("2018-07-15");

        input.add(dto3);

        PeriodeDto dto4 = new PeriodeDto();

        dto4.setÅrsak("4006");
        dto4.setAntallTapteDager(4);
        dto4.setInnvilget(false);
        dto4.setPeriodeFom("2018-07-16");
        dto4.setPeriodeTom("2018-07-20");

        input.add(dto4);

        PeriodeDto dto5 = new PeriodeDto();

        dto5.setÅrsak("4008");
        dto5.setAntallTapteDager(4);
        dto5.setInnvilget(false);
        dto5.setPeriodeFom("2018-07-21");
        dto5.setPeriodeTom("2018-07-30");

        input.add(dto5);
    }

}
