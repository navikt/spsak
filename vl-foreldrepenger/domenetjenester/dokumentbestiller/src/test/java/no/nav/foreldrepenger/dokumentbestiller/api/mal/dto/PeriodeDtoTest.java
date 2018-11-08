package no.nav.foreldrepenger.dokumentbestiller.api.mal.dto;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PeriodeDtoTest {

    PeriodeDto periodeDto = new PeriodeDto();

    @Before
    public void setup() {
        periodeDto.leggTilArbeidsforhold(opprettArbeidsforholdMedGraderingOgUttaksgrad(false, 100));
        periodeDto.leggTilArbeidsforhold(opprettArbeidsforholdMedGraderingOgUttaksgrad(false, 50));
        periodeDto.leggTilArbeidsforhold(opprettArbeidsforholdMedGraderingOgUttaksgrad(false, 70));
        periodeDto.leggTilArbeidsforhold(opprettArbeidsforholdMedGraderingOgUttaksgrad(false, 80));
        periodeDto.leggTilArbeidsforhold(opprettArbeidsforholdMedGraderingOgUttaksgrad(false, 66));
        periodeDto.leggTilArbeidsforhold(opprettArbeidsforholdMedGraderingOgUttaksgrad(false, 11));
        periodeDto.leggTilArbeidsforhold(opprettArbeidsforholdMedGraderingOgUttaksgrad(false, 100));
    }


    @Test
    public void skal_sortere_arbeidsforhold() {
        periodeDto.leggTilArbeidsforhold(opprettArbeidsforholdMedGraderingOgUttaksgrad(true, 90));

        List<ArbeidsforholdDto> arbeidsforhold = periodeDto.getArbeidsforhold();
        Iterator<ArbeidsforholdDto> iterator = arbeidsforhold.iterator();
        assertThat(iterator.next().getGradering()).isTrue();
        assertThat(iterator.next().getUttaksgrad()).isEqualTo(100);
        assertThat(iterator.next().getUttaksgrad()).isEqualTo(100);
        assertThat(iterator.next().getUttaksgrad()).isEqualTo(80);
        assertThat(iterator.next().getUttaksgrad()).isEqualTo(70);
        assertThat(iterator.next().getUttaksgrad()).isEqualTo(66);
        assertThat(iterator.next().getUttaksgrad()).isEqualTo(50);
        assertThat(iterator.next().getUttaksgrad()).isEqualTo(11);

        assertThat(periodeDto.isGraderingFinnes()).isTrue();
    }

    @Test
    public void skal_ikke_sette_gradering_finnes() {
        assertThat(periodeDto.isGraderingFinnes()).isFalse();
    }

    private ArbeidsforholdDto opprettArbeidsforholdMedGraderingOgUttaksgrad(boolean gradering, int uttaksgrad) {
        ArbeidsforholdDto arbeidsforholdDto = new ArbeidsforholdDto();
        arbeidsforholdDto.setGradering(gradering);
        arbeidsforholdDto.setUttaksgrad(uttaksgrad);
        return arbeidsforholdDto;
    }
}
