package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.ArbeidsforholdDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DuplikatVerktøy {

    private DuplikatVerktøy() {
        //for sonar
    }

    public static List<ArbeidsforholdDto> slåSammenArbeidsforholdDto(List<ArbeidsforholdDto> dtoListe) {
        List<ArbeidsforholdDto> nyListe = new ArrayList<>();
        nyListe.addAll(dtoListe);
        for (ArbeidsforholdDto dto : dtoListe) {
            for (ArbeidsforholdDto dto2 : dtoListe) {
                if (nyListe.contains(dto) && nyListe.contains(dto2)) {
                    if (sammeArbeidsforhold(dto, dto2)) {
                        dto.setDagsats(dto.getDagsats() + dto2.getDagsats());
                        nyListe.remove(dto2);
                    }
                }
            }
        }
        return nyListe;
    }


    private static boolean sammeArbeidsforhold(ArbeidsforholdDto dto, ArbeidsforholdDto dto2) {
        return !dto.equals(dto2)
            && Objects.equals(dto.getArbeidsgiverNavn(), dto2.getArbeidsgiverNavn())
            && Objects.equals(dto.getArbeidsforholdId(), dto2.getArbeidsforholdId());
    }

}
