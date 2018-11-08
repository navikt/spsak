package no.nav.foreldrepenger.regler.uttak.konfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

public class KonfigurasjonBuilderTest {

    @Test
    public void konfigurasjon_med_en_verdi() {
        LocalDate nå = LocalDate.now();
        Konfigurasjon konfigurasjon = KonfigurasjonBuilder.create()
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, nå, null, 75)
                .build();
        assertThat(konfigurasjon.getParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, nå)).isEqualTo(75);
    }

    @Test
    public void konfigurasjon_med_en_verdi_i_to_intervaller() {
        LocalDate nå = LocalDate.now();
        Konfigurasjon konfigurasjon = KonfigurasjonBuilder.create()
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, nå, nå.plusDays(6), 50)
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, nå.plusDays(7), null, 75)
                .build();
        assertThat(konfigurasjon.getParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, nå)).isEqualTo(50);
        assertThat(konfigurasjon.getParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, nå.plusDays(7))).isEqualTo(75);
        assertThat(konfigurasjon.getParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, nå.plusDays(70))).isEqualTo(75);
    }

    @Test(expected = IllegalArgumentException.class)
    public void konfigurasjon_med_en_verdi_i_to_intervaller_med_overlapp() {
        LocalDate nå = LocalDate.now();
        KonfigurasjonBuilder.create()
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, nå, nå.plusDays(6), 50)
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, nå.plusDays(5), null, 75)
                .build();
    }

}