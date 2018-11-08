package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.kontrakter.feed.felles.FeedElement;

public class InfotrygdHendelseMapperTest {

    private static final String AKTOR_ID = "10000";
    private static final LocalDate DATO = LocalDate.of(2018, 5, 15);
    private static final String TYPE_YTELSE = "Type";

    @Test
    public void skal_MapFraFeedTilInfotrygdHendelse() {
        //Arrange
        InfotrygdHendelseMapper mapper = new InfotrygdHendelseMapper();
        List<InfotrygdHendelse> hendelseList = new ArrayList<>();
        List<FeedElement> feedElementList = lagElement(1,
            new InfotrygdAnnulert(),
            new InfotrygdInnvilget(),
            new InfotrygdOpphørt(),
            new InfotrygdEndret());

        int ix = 0;
        for (FeedElement element : feedElementList) {
            //Act
            hendelseList.add(mapper.mapFraFeedTilInfotrygdHendelse(element));
            //Assert
            verifiserVerdiForFelter(element, hendelseList.get(ix++));
        }
        assertThat(hendelseList).hasSize(4);

    }

    private void verifiserVerdiForFelter(FeedElement feedElement, InfotrygdHendelse infotrygdHendelse) {
        String datoStr = konverterFomDatoTilString(DATO);
        assertThat(feedElement.getType()).isEqualTo(infotrygdHendelse.getType());
        assertThat(feedElement.getSekvensId()).isEqualTo(infotrygdHendelse.getSekvensnummer());
        assertThat(infotrygdHendelse.getAktoerId()).isEqualTo(10000L);
        assertThat(infotrygdHendelse.getFom()).isEqualTo(DATO);
        assertThat(infotrygdHendelse.getTypeYtelse()).isEqualTo(TYPE_YTELSE);
        assertThat(infotrygdHendelse.getIdentDato()).isEqualTo(datoStr);
    }

    private List<FeedElement> lagElement(long sequence, Object...melding) {
        String type;
        List<FeedElement> elementList = new ArrayList<>();
        for (Object o : melding) {
            if (o instanceof InfotrygdAnnulert) {
                type = "ANNULLERT_v1";
            } else if (o instanceof InfotrygdOpphørt) {
                type = "OPPHOERT_v1";
            } else if (o instanceof InfotrygdInnvilget) {
                type = "INNVILGET_v1";
            } else {
                type = "ENDRET_v1";
            }
            elementList.add(new FeedElement.Builder()
                .medSekvensId(sequence++)
                .medType(type)
                .medInnhold(lagInnhold(o))
                .build());
        }
        return elementList;
    }

    private Innhold lagInnhold(Object melding) {
        Innhold innhold = (Innhold) melding;
        innhold.setAktoerId(AKTOR_ID);
        innhold.setFom(DATO);
        innhold.setIdentDato(konverterFomDatoTilString(DATO));
        innhold.setTypeYtelse(TYPE_YTELSE);

        return innhold;
    }

    private String konverterFomDatoTilString(LocalDate dato) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dato.format(formatter);
    }
}
