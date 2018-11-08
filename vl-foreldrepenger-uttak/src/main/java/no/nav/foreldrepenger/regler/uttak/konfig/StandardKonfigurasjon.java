package no.nav.foreldrepenger.regler.uttak.konfig;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;

public class StandardKonfigurasjon {

    public static final Konfigurasjon KONFIGURASJON;

    //Søknadsdialog trenger støtte før 2019
    public static final Konfigurasjon SØKNADSDIALOG;

    static {
        LocalDate d_2017_01_01 = LocalDate.of(2017, Month.JANUARY, 1);
        KONFIGURASJON = KonfigurasjonBuilder.create()
            //Stønadskontoer
            .leggTilParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100, d_2017_01_01, null, 85)
            .leggTilParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80, d_2017_01_01, null, 105)
            .leggTilParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100, d_2017_01_01, null, 230)
            .leggTilParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80, d_2017_01_01, null, 280)
            .leggTilParameter(Parametertype.MØDREKVOTE_DAGER_100_PROSENT, d_2017_01_01, null, 75)
            .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, d_2017_01_01, null, 75)
            .leggTilParameter(Parametertype.MØDREKVOTE_DAGER_80_PROSENT, d_2017_01_01, null, 95)
            .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_80_PROSENT, d_2017_01_01, null, 95)
            .leggTilParameter(Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER, d_2017_01_01, null, 80)
            .leggTilParameter(Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER, d_2017_01_01, null, 90)
            .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER, d_2017_01_01, null, 230)
            .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER, d_2017_01_01, null, 280)
            .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_FAR_ALENEOMSORG_DAGER, d_2017_01_01, null, 230)
            .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_FAR_ALENEOMSORG_DAGER, d_2017_01_01, null, 280)
            .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_FAR_HAR_RETT_DAGER, d_2017_01_01, null, 200)
            .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_HAR_RETT_DAGER, d_2017_01_01, null, 250)
            .leggTilParameter(Parametertype.FORELDREPENGER_FØR_FØDSEL, d_2017_01_01, null, 15)
            //Uttaksperioder
            .leggTilParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, d_2017_01_01, null, 6)
            .leggTilParameter(Parametertype.LOVLIG_UTTAK_FØR_FØDSEL_UKER, d_2017_01_01, null, 12)
            .leggTilParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, d_2017_01_01, null, 3)
            //grenser
            .leggTilParameter(Parametertype.GRENSE_ETTER_FØDSELSDATO, d_2017_01_01, null, Period.ofYears(3))
            .build();
        LocalDate d_2010_01_01 = LocalDate.of(2010, Month.JANUARY, 1);
        LocalDate d_2018_12_31 = LocalDate.of(2018, Month.DECEMBER, 31);
        LocalDate d_2019_01_01 = LocalDate.of(2019, Month.JANUARY, 1);
        SØKNADSDIALOG = KonfigurasjonBuilder.create()
                //Stønadskontoer
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100, d_2019_01_01, null, 85)
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80, d_2019_01_01, null, 105)
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100, d_2019_01_01, null, 230)
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80, d_2019_01_01, null, 280)
                .leggTilParameter(Parametertype.MØDREKVOTE_DAGER_100_PROSENT, d_2019_01_01, null, 75)
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, d_2019_01_01, null, 75)
                .leggTilParameter(Parametertype.MØDREKVOTE_DAGER_80_PROSENT, d_2019_01_01, null, 95)
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_80_PROSENT, d_2019_01_01, null, 95)
                .leggTilParameter(Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER, d_2019_01_01, null, 80)
                .leggTilParameter(Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER, d_2019_01_01, null, 90)
                .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER, d_2019_01_01, null, 230)
                .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER, d_2019_01_01, null, 280)
                .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_FAR_ALENEOMSORG_DAGER, d_2019_01_01, null, 230)
                .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_FAR_ALENEOMSORG_DAGER, d_2019_01_01, null, 280)
                .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_FAR_HAR_RETT_DAGER, d_2019_01_01, null, 200)
                .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_HAR_RETT_DAGER, d_2019_01_01, null, 250)
                .leggTilParameter(Parametertype.FORELDREPENGER_FØR_FØDSEL, d_2019_01_01, null, 15)

                .leggTilParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100, d_2010_01_01, d_2018_12_31, 85)
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80, d_2010_01_01, d_2018_12_31, 105)
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100, d_2010_01_01, d_2018_12_31, 230)
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80, d_2010_01_01, d_2018_12_31, 280)
                .leggTilParameter(Parametertype.MØDREKVOTE_DAGER_100_PROSENT, d_2010_01_01, d_2018_12_31, 75)
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, d_2010_01_01, d_2018_12_31, 75)
                .leggTilParameter(Parametertype.MØDREKVOTE_DAGER_80_PROSENT, d_2010_01_01, d_2018_12_31, 75)
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_80_PROSENT, d_2010_01_01, d_2018_12_31, 75)
                .leggTilParameter(Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER, d_2010_01_01, d_2018_12_31, 80)
                .leggTilParameter(Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER, d_2010_01_01, d_2018_12_31, 130)
                .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER, d_2010_01_01, d_2018_12_31, 230)
                .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER, d_2010_01_01, d_2018_12_31, 280)
                .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_FAR_ALENEOMSORG_DAGER, d_2010_01_01, d_2018_12_31, 230)
                .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_FAR_ALENEOMSORG_DAGER, d_2010_01_01, d_2018_12_31, 280)
                .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_FAR_HAR_RETT_DAGER, d_2010_01_01, d_2018_12_31, 200)
                .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_HAR_RETT_DAGER, d_2010_01_01, d_2018_12_31, 250)
                .leggTilParameter(Parametertype.FORELDREPENGER_FØR_FØDSEL, d_2010_01_01, d_2018_12_31, 15)
                //Uttaksperioder
                .leggTilParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, d_2019_01_01, null, 6)
                .leggTilParameter(Parametertype.LOVLIG_UTTAK_FØR_FØDSEL_UKER, d_2019_01_01, null, 12)
                .leggTilParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, d_2019_01_01, null, 3)

                .leggTilParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, d_2010_01_01, d_2018_12_31, 6)
                .leggTilParameter(Parametertype.LOVLIG_UTTAK_FØR_FØDSEL_UKER, d_2010_01_01, d_2018_12_31, 12)
                .leggTilParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, d_2010_01_01, d_2018_12_31, 3)
                //grenser
                .leggTilParameter(Parametertype.GRENSE_ETTER_FØDSELSDATO, d_2019_01_01, null, Period.ofYears(3))

                .leggTilParameter(Parametertype.GRENSE_ETTER_FØDSELSDATO, d_2010_01_01, d_2018_12_31, Period.ofYears(3))
                .build();
    }

    private StandardKonfigurasjon() {
        //For å hindre instanser.
    }

}
