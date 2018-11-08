package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.ArbeidsforholdDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DuplikatVerktøyTest {

    private final int beløpPerArbForhold = 50;
    private BeregningsresultatPeriode periode = new BeregningsresultatPeriode();

    private VirksomhetEntitet virksomhet1;
    private VirksomhetEntitet virksomhet2;
    private String arbeidsforholdId1 = "989989";

    @Before
    public void setup() {
        virksomhet1 = Mockito.mock(VirksomhetEntitet.class);
        when(virksomhet1.getOrgnr()).thenReturn("11223344");
        when(virksomhet1.getNavn()).thenReturn("Sopra Steria");
        virksomhet2 = Mockito.mock(VirksomhetEntitet.class);
        when(virksomhet2.getOrgnr()).thenReturn("32165498");
        when(virksomhet2.getNavn()).thenReturn("NAV");
    }

    @Test
    public void tom_liste() {
        assertThat(DuplikatVerktøy.slåSammenArbeidsforholdDto(Collections.emptyList())).isEmpty();
    }

    @Test
    public void skal_fjerne_kun_duplikater() {
        List<ArbeidsforholdDto> dtoListe = lagDtoer();
        List<ArbeidsforholdDto> resultat = DuplikatVerktøy.slåSammenArbeidsforholdDto(dtoListe);
        assertThat(resultat).hasSize(dtoListe.size() - 3);
        assertThat(resultat.stream().filter(dto -> dto.getDagsats() == beløpPerArbForhold).collect(Collectors.toList())).hasSize(4);
    }


    private List<ArbeidsforholdDto> lagDtoer() {
        List<ArbeidsforholdDto> andelListe = new ArrayList<>();
        andelListe.add(lagDto(virksomhet1, arbeidsforholdId1, beløpPerArbForhold));
        andelListe.add(lagDto(virksomhet1, arbeidsforholdId1, 0));
        andelListe.add(lagDto(virksomhet1, arbeidsforholdId1, 0));
        andelListe.add(lagDto(virksomhet1, null, beløpPerArbForhold));
        andelListe.add(lagDto(virksomhet2, arbeidsforholdId1, beløpPerArbForhold));
        andelListe.add(lagDto(virksomhet2, arbeidsforholdId1, 0));
        andelListe.add(lagDto(virksomhet2, null, beløpPerArbForhold));
        return andelListe;
    }

    private ArbeidsforholdDto lagDto(VirksomhetEntitet virksomhet, String arbforholdId, int beløpPerArbForhold) {
        ArbeidsforholdDto dto = new ArbeidsforholdDto();
        dto.setDagsats(beløpPerArbForhold);
        dto.setArbeidsgiverNavn(virksomhet.getNavn());
        dto.setArbeidsforholdId(arbforholdId);
        return dto;
    }

}
