package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.FiktiveFnr;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;

/**
 * NB bruk syntetiske fødselsnummer her
 */
/**
 * @deprecated bruk heller {@link TpsTestSett}
 */
@Deprecated
public class TpsRepo {
    private static final FiktiveFnr FNR = new FiktiveFnr();
    private static TpsRepo instance;

    // Simulering av Tps sin datamodell
    private static Map<AktørId, String> FNR_VED_AKTØR_ID = new HashMap<>();
    private static Map<String, AktørId> AKTØR_ID_VED_FNR = new HashMap<>();
    private static Map<String, Person> PERSON_VED_FNR = new HashMap<>();

    private static final String RELASJONSTYPE_BARN = "BARN";
    private static final String RELASJONSTYPE_MORA = "MORA";
    private static final String RELASJONSTYPE_FARA = "FARA";

    // Navn (gjenbrukes på tvers av brukere for å spare jobb)
    public static final String MANN_FORNAVN = "Mann";
    public static final String KVINNE_FORNAVN = "Kvinne";
    public static final String BARN_FORNAVN = "Barn";
    public static final String ETTERNAVN = "Etternavn";

    // "Standard" brukere
    public static final AktørId STD_MANN_AKTØR_ID = new AktørId("9001000100035");
    public static final String STD_MANN_FNR = FNR.nesteMannFnr();

    public static final AktørId STD_KVINNE_AKTØR_ID = new AktørId("9001000100036");
    public static final String STD_KVINNE_FNR = FNR.nesteKvinneFnr();

    public static final String STD_BARN_FNR = "11011773747"; // hardkodet, noe logikk knyttet til søknadsfristvilkåret er avhengig av dette
    public static final AktørId STD_BARN_AKTØR_ID = new AktørId("9001000100037");

    // Medmor-brukere
    public static final AktørId MOR_MED_MEDMOR_AKTØR_ID = new AktørId("9001001001014");
    public static final String MOR_MED_MEDMOR_FNR = FNR.nesteKvinneFnr();

    public static final AktørId MEDMOR_AKTØR_ID = new AktørId("9010010010015");
    public static final String MEDMOR_FNR = FNR.nesteKvinneFnr();

    public static final String BARN_FOR_MEDMOR_FNR = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy")) + "00101";
    public static final AktørId BARN_FOR_MEDMOR_AKTØR_ID = new AktørId("9000100010037");

    // Uten barn
    public static final AktørId KVINNE_UTEN_BARN_AKTØR_ID = new AktørId("9001001001040");
    public static final String KVINNE_UTEN_BARN_FNR = FNR.nesteKvinneFnr();

    // Medlemskap
    public static final AktørId KVINNE_MEDL_ENDELIG_PERIODE_AKTØRID = new AktørId("1");
    public static final String KVINNE_MEDL_ENDELIG_PERIODE_FNR = FNR.nesteKvinneFnr();
    public static final AktørId KVINNE_MEDL_UAVKL_PERIODE_AKTØRID = new AktørId("2");
    public static final String KVINNE_MEDL_UAVKL_PERIODE_FNR = FNR.nesteKvinneFnr();
    public static final AktørId KVINNE_MEDL_UTVANDRET_AKTØRID = new AktørId("3");
    public static final String KVINNE_MEDL_UTVANDRET_PERIODE_FNR = FNR.nesteKvinneFnr();
    public static final AktørId KVINNE_MEDL_EØSBORGER_BOSATT_UTLAND_AKTØRID = new AktørId("4");
    public static final String KVINNE_MEDL_EØSBORGER_BOSATT_UTLAND_PERIODE_FNR = FNR.nesteKvinneFnr();
    public static final AktørId KVINNE_MEDL_USA_AKTØRID = new AktørId("5");
    public static final String KVINNE_MEDL_USA_PERIODE_FNR = FNR.nesteKvinneFnr();
    public static final AktørId KVINNE_MEDL_FEIL_PERSONSTATUS_AKTØRID = new AktørId("6");
    public static final String KVINNE_MEDL_FEIL_PERSONSTATUS_FNR = FNR.nesteKvinneFnr();
    public static final AktørId KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID = new AktørId("7");
    public static final String KVINNE_MEDL_EØSBORGER_BOSATT_NOR_FNR = FNR.nesteKvinneFnr();

    public static final AktørId BARN_MEDL_ENDELIG_PERIODE_AKTØRID = new AktørId("11");
    public static final String BARN_MEDL_ENDELIG_PERIODE_FNR = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy")) + "10001";
    public static final AktørId BARN_MEDL_UAVKL_PERIODE_AKTØRID = new AktørId("12");
    public static final String BARN_MEDL_UAVKL_PERIODE_FNR = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy")) + "10002";
    public static final AktørId BARN_MEDL_UTVANDRET_AKTØRID = new AktørId("13");
    public static final String BARN_MEDL_UTVANDRET_PERIODE_FNR = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy")) + "10003";
    public static final AktørId BARN_MEDL_EØSBORGER_BOSATT_UTLAND_AKTØRID = new AktørId("14");
    public static final String BARN_MEDL_EØSBORGER_BOSATT_UTLAND_FNR = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy")) + "10004";
    public static final AktørId BARN_MEDL_USA_AKTØRID = new AktørId("15");
    public static final String BARN_MEDL_USA_PERIODE_FNR = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy")) + "10005";
    public static final AktørId BARN_MEDL_FEIL_PERSONSTATUS_AKTØRID = new AktørId("16");
    public static final String BARN_MEDL_FEIL_PERSONSTATUS_FNR = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy")) + "10006";
    public static final AktørId BARN_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID = new AktørId("17");
    public static final String BARN_MEDL_EØSBORGER_BOSATT_NOR_FNR = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy")) + "10007";

    public static final AktørId MANN_MEDL_EØSBORGER_BOSATT_UTLAND_AKTØRID = new AktørId("104");
    public static final String MANN_MEDL_EØSBORGER_BOSATT_UTLAND_PERIODE_FNR = FNR.nesteMannFnr();

    private static LocalDate oppfrisketDato;

    // Søker med arbeidsforhold og inntekt
    public static final AktørId MANN_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_AKTØR_ID = new AktørId("9000100010101");
    public static final String MANN_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_FNR = FNR.nesteMannFnr();

    public static final AktørId KVINNE_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_AKTØR_ID = new AktørId("9001001000102");
    public static final String KVINNE_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_FNR = FNR.nesteKvinneFnr();

    public static final AktørId BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_ID = new AktørId("9000100010103");

    // hardkodet, noe logikk knyttet til fødselsvilkåret er avhengig av dette
    // hardkodet pga frister knyttet til søknadsdato og permissjonsstart
    public static final String BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_FNR = LocalDate.now().minusDays(5).format(DateTimeFormatter.ofPattern("ddMMyy")) + "773747";

    public static final AktørId KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_AKTØR_ID = new AktørId("9001001000104");
    public static final String KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_FNR = FNR.nesteKvinneFnr();

    public static final AktørId BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_ID = new AktørId("9000100010105");

    // hardkodet, noe logikk knyttet til fødselsvilkåret er avhengig av dette
    // hardkodet pga frister knyttet til søknadsdato og permissjonsstart
    public static final String BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_FNR = LocalDate.now().minusDays(6).format(DateTimeFormatter.ofPattern("ddMMyy")) + "773747";

    // Søker med null-prosent arbeidsforhold og inntekt
    public static final AktørId KVINNE_MED_INNTEKT_OG_0_PROSENT_ARBEIDSFORHOLD_AKTØR_ID = new AktørId("9001001000110");
    public static final String KVINNE_MED_INNTEKT_OG_0_PROSENT_ARBEIDSFORHOLD_FNR = FNR.nesteKvinneFnr();

    public static final AktørId BARN_TIL_KVINNE_MED_INNTEKT_OG_0_PROSENT_ARBEIDSFORHOLD_ID = new AktørId("9001001000111");
    public static final String BARN_TIL_KVINNE_MED_INNTEKT_OG_0_PROSENT_ARBEIDSFORHOLD_FNR = FNR.nesteBarnFnr();

    // Søker med inntekt - medmor
    public static final AktørId MOR_MED_MEDMOR_OG_INNTEKT_AKTØR_ID = new AktørId("9000100100120");
    public static final String MOR_MED_MEDMOR_OG_INNTEKT_FNR = FNR.nesteKvinneFnr();

    public static final AktørId MEDMOR_MED_INNTEKT_AKTØR_ID = new AktørId("9001000100121");
    public static final String MEDMOR_MED_INNTEKT_FNR = FNR.nesteKvinneFnr();

    public static final AktørId BARN_FOR_MEDMOR_MED_INNTEKT_AKTØR_ID = new AktørId("9001000100122");
    public static final String BARN_FOR_MEDMOR_MED_INNTEKT_FNR = FNR.nesteBarnFnr();

    // Test bruker med 2 arbeidsforhold på samme arbeidsgiver og inntekt
    public static final AktørId KVINNE_MED_INNTEKT_40k_OG_2_ARBEIDSFORHOLD_AKTØR_ID = new AktørId("9000100100104");
    public static final String KVINNE_MED_INNTEKT_40k_OG_2_ARBEIDSFORHOLD_FNR = FNR.nesteKvinneFnr();

    public static final AktørId BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_2_ARBEIDSFORHOLD_ID = new AktørId("9001000100105");
    public static final String BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_2_ARBEIDSFORHOLD_FNR = FNR.nesteBarnFnr();

    // Søker med kun frilansinntekt
    public static final AktørId KVINNE_KUN_FRILANS_AKTØR_ID = new AktørId("9001001000120");
    public static final String KVINNE_KUN_FRILANS_FNR = FNR.nesteKvinneFnr();

    public static final AktørId BARN_TIL_KVINNE_KVINNE_KUN_FRILANS_ID = new AktørId("9001001000121");
    public static final String BARN_TIL_KVINNE_KVINNE_KUN_FRILANS_FNR = FNR.nesteBarnFnr();


    public static synchronized TpsRepo init() {
        if (instance == null) {
            instance = new TpsRepo();
            opprettTpsData();
            oppfrisketDato = LocalDate.now();
        }
        return instance;
    }

    private static void opprettTpsData() {
        List<TpsPerson> tpsPersoner = new ArrayList<>();
        List<TpsRelasjon> tpsRelasjoner = new ArrayList<>();
        tpsPersoner = leggTilHardkodedePersoner(tpsPersoner);
        knyttRelasjoner(tpsPersoner, tpsRelasjoner);

        FNR_VED_AKTØR_ID.clear();
        AKTØR_ID_VED_FNR.clear();
        PERSON_VED_FNR.clear();

        for (TpsPerson tpsPerson : tpsPersoner) {
            FNR_VED_AKTØR_ID.put(tpsPerson.aktørId, tpsPerson.fnr);
            AKTØR_ID_VED_FNR.put(tpsPerson.fnr, tpsPerson.aktørId);
            PERSON_VED_FNR.put(tpsPerson.fnr, tpsPerson.person);
        }
    }

    private static List<TpsPerson> leggTilHardkodedePersoner(List<TpsPerson> tpsPersoner) {
        // Legg til hardkodete data i etterkant (brukes for integrasjonstester i Vedtaksløsningen)
        tpsPersoner.add(new TpsPerson(STD_MANN_AKTØR_ID, new PersonBygger(STD_MANN_FNR, MANN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
            .leggTilRelasjon(RELASJONSTYPE_BARN, STD_BARN_FNR, BARN_FORNAVN, ETTERNAVN)));
        tpsPersoner.add(new TpsPerson(STD_KVINNE_AKTØR_ID,
            new PersonBygger(STD_KVINNE_FNR, KVINNE_FORNAVN, ETTERNAVN,
                PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_BARN, STD_BARN_FNR, BARN_FORNAVN, ETTERNAVN)));
        tpsPersoner.add(new TpsPerson(STD_BARN_AKTØR_ID, new PersonBygger(STD_BARN_FNR, BARN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
            .leggTilRelasjon(RELASJONSTYPE_MORA, STD_KVINNE_FNR, KVINNE_FORNAVN, ETTERNAVN)
            .leggTilRelasjon(RELASJONSTYPE_FARA, STD_MANN_FNR, MANN_FORNAVN, ETTERNAVN)));

        tpsPersoner.add(new TpsPerson(KVINNE_UTEN_BARN_AKTØR_ID,
            new PersonBygger(KVINNE_UTEN_BARN_FNR, KVINNE_FORNAVN,
                ETTERNAVN, PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "NOR")));

            tpsPersoner.add(new TpsPerson(MEDMOR_AKTØR_ID, new PersonBygger(MEDMOR_FNR, KVINNE_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "NOR")
            .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_FOR_MEDMOR_FNR, BARN_FORNAVN, ETTERNAVN)));
        tpsPersoner.add(new TpsPerson(MOR_MED_MEDMOR_AKTØR_ID,
            new PersonBygger(MOR_MED_MEDMOR_FNR, KVINNE_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_FOR_MEDMOR_FNR, BARN_FORNAVN, ETTERNAVN)));
        tpsPersoner.add(new TpsPerson(BARN_FOR_MEDMOR_AKTØR_ID,
            new PersonBygger(BARN_FOR_MEDMOR_FNR, BARN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, MOR_MED_MEDMOR_FNR, KVINNE_FORNAVN, ETTERNAVN)
                .leggTilRelasjon("FARA", MEDMOR_FNR, KVINNE_FORNAVN, ETTERNAVN)));

        tpsPersoner.add(new TpsPerson(KVINNE_MEDL_ENDELIG_PERIODE_AKTØRID,
            new PersonBygger(KVINNE_MEDL_ENDELIG_PERIODE_FNR, "SILJE", "MEDL1", PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_MEDL_ENDELIG_PERIODE_FNR, RELASJONSTYPE_BARN, "BARNESEN1")));
        tpsPersoner.add(new TpsPerson(KVINNE_MEDL_UAVKL_PERIODE_AKTØRID,
            new PersonBygger(KVINNE_MEDL_UAVKL_PERIODE_FNR, "SILJE", "MEDL2", PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "POL")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_MEDL_UAVKL_PERIODE_FNR, RELASJONSTYPE_BARN, "BARNESEN2")));
        tpsPersoner.add(new TpsPerson(KVINNE_MEDL_UTVANDRET_AKTØRID,
            new PersonBygger(KVINNE_MEDL_UTVANDRET_PERIODE_FNR, "SILJE", "MEDL3", PersonBygger.Kjønn.KVINNE, "UTVA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_MEDL_UTVANDRET_PERIODE_FNR, RELASJONSTYPE_BARN, "BARNESEN3")));
        tpsPersoner.add(new TpsPerson(KVINNE_MEDL_EØSBORGER_BOSATT_UTLAND_AKTØRID,
            new PersonBygger(KVINNE_MEDL_EØSBORGER_BOSATT_UTLAND_PERIODE_FNR, "SILJE", "MEDL4", PersonBygger.Kjønn.KVINNE, "BOSA", "POL", "POL")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_MEDL_EØSBORGER_BOSATT_UTLAND_FNR, RELASJONSTYPE_BARN, "BARNESEN4")));
        tpsPersoner.add(new TpsPerson(KVINNE_MEDL_USA_AKTØRID,
            new PersonBygger(KVINNE_MEDL_USA_PERIODE_FNR, "SILJE", "MEDL5", PersonBygger.Kjønn.KVINNE, "BOSA", "USA", "USA")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_MEDL_USA_PERIODE_FNR, RELASJONSTYPE_BARN, "BARNESEN5")));
        tpsPersoner.add(new TpsPerson(KVINNE_MEDL_FEIL_PERSONSTATUS_AKTØRID,
            new PersonBygger(KVINNE_MEDL_FEIL_PERSONSTATUS_FNR, "SILJE", "MEDL6", PersonBygger.Kjønn.KVINNE, "UREG", "USA", "USA")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_MEDL_FEIL_PERSONSTATUS_FNR, RELASJONSTYPE_BARN, "BARNESEN6")));
        tpsPersoner.add(new TpsPerson(KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID,
            new PersonBygger(KVINNE_MEDL_EØSBORGER_BOSATT_NOR_FNR, "SILJE", "MEDL7", PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "POL")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_MEDL_EØSBORGER_BOSATT_NOR_FNR, RELASJONSTYPE_BARN, "BARNESEN7")));

        tpsPersoner.add(new TpsPerson(BARN_MEDL_ENDELIG_PERIODE_AKTØRID,
            new PersonBygger(BARN_MEDL_ENDELIG_PERIODE_FNR, BARN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, KVINNE_MEDL_ENDELIG_PERIODE_FNR, KVINNE_FORNAVN, ETTERNAVN)));
        tpsPersoner.add(new TpsPerson(BARN_MEDL_UAVKL_PERIODE_AKTØRID,
            new PersonBygger(BARN_MEDL_UAVKL_PERIODE_FNR, BARN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, KVINNE_MEDL_UAVKL_PERIODE_FNR, KVINNE_FORNAVN, ETTERNAVN)));
        tpsPersoner.add(new TpsPerson(BARN_MEDL_UTVANDRET_AKTØRID,
            new PersonBygger(BARN_MEDL_UTVANDRET_PERIODE_FNR, BARN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, KVINNE_MEDL_UTVANDRET_PERIODE_FNR, KVINNE_FORNAVN, ETTERNAVN)));
        tpsPersoner.add(new TpsPerson(BARN_MEDL_EØSBORGER_BOSATT_UTLAND_AKTØRID,
            new PersonBygger(BARN_MEDL_EØSBORGER_BOSATT_UTLAND_FNR, BARN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, KVINNE_MEDL_EØSBORGER_BOSATT_UTLAND_PERIODE_FNR, KVINNE_FORNAVN, ETTERNAVN)));
        tpsPersoner.add(new TpsPerson(BARN_MEDL_USA_AKTØRID,
            new PersonBygger(BARN_MEDL_USA_PERIODE_FNR, BARN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, KVINNE_MEDL_USA_PERIODE_FNR, KVINNE_FORNAVN, ETTERNAVN)));
        tpsPersoner.add(new TpsPerson(BARN_MEDL_FEIL_PERSONSTATUS_AKTØRID,
            new PersonBygger(BARN_MEDL_FEIL_PERSONSTATUS_FNR, BARN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, KVINNE_MEDL_FEIL_PERSONSTATUS_FNR, KVINNE_FORNAVN, ETTERNAVN)));
        tpsPersoner.add(new TpsPerson(BARN_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID,
            new PersonBygger(BARN_MEDL_EØSBORGER_BOSATT_NOR_FNR, BARN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, KVINNE_MEDL_EØSBORGER_BOSATT_NOR_FNR, KVINNE_FORNAVN, ETTERNAVN)));

        tpsPersoner.add(new TpsPerson(MANN_MEDL_EØSBORGER_BOSATT_UTLAND_AKTØRID,
            new PersonBygger(MANN_MEDL_EØSBORGER_BOSATT_UTLAND_PERIODE_FNR, MANN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "POL", "POL")));

        // ARBEID OG INNTEKT
        tpsPersoner.add(new TpsPerson(KVINNE_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_AKTØR_ID,
            new PersonBygger(KVINNE_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_FNR, KVINNE_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_FNR, MANN_FORNAVN, ETTERNAVN)));

        tpsPersoner.add(new TpsPerson(MANN_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_AKTØR_ID,
            new PersonBygger(MANN_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_FNR, MANN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_FNR, MANN_FORNAVN, ETTERNAVN)));

        tpsPersoner.add(new TpsPerson(BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_ID,
            new PersonBygger(BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_FNR, MANN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, KVINNE_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_FNR, KVINNE_FORNAVN, ETTERNAVN)
                .leggTilRelasjon(RELASJONSTYPE_FARA, MANN_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_FNR, MANN_FORNAVN, ETTERNAVN)));

        tpsPersoner.add(new TpsPerson(KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_AKTØR_ID,
            new PersonBygger(KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_FNR, KVINNE_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_FNR, MANN_FORNAVN, ETTERNAVN)));

        tpsPersoner.add(new TpsPerson(BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_ID,
            new PersonBygger(BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_FNR, MANN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_FNR, KVINNE_FORNAVN, ETTERNAVN)));


        // 0-prosent-ARBEID OG INNTEKT
        tpsPersoner.add(new TpsPerson(KVINNE_MED_INNTEKT_OG_0_PROSENT_ARBEIDSFORHOLD_AKTØR_ID,
            new PersonBygger(KVINNE_MED_INNTEKT_OG_0_PROSENT_ARBEIDSFORHOLD_FNR, KVINNE_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_TIL_KVINNE_MED_INNTEKT_OG_0_PROSENT_ARBEIDSFORHOLD_FNR, "Børre", "Larsen")));

        tpsPersoner.add(new TpsPerson(BARN_TIL_KVINNE_MED_INNTEKT_OG_0_PROSENT_ARBEIDSFORHOLD_ID,
            new PersonBygger(BARN_TIL_KVINNE_MED_INNTEKT_OG_0_PROSENT_ARBEIDSFORHOLD_FNR, "Børre", "Larsen", PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, KVINNE_MED_INNTEKT_OG_0_PROSENT_ARBEIDSFORHOLD_FNR, KVINNE_FORNAVN, ETTERNAVN)));

        // Frilanser
        tpsPersoner.add(new TpsPerson(KVINNE_KUN_FRILANS_AKTØR_ID,
            new PersonBygger(KVINNE_KUN_FRILANS_FNR, KVINNE_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_TIL_KVINNE_KVINNE_KUN_FRILANS_FNR, "Børre", "Larsen")));

        tpsPersoner.add(new TpsPerson(BARN_TIL_KVINNE_KVINNE_KUN_FRILANS_ID,
            new PersonBygger(BARN_TIL_KVINNE_KVINNE_KUN_FRILANS_FNR, "Børre", "Larsen", PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, KVINNE_KUN_FRILANS_FNR, KVINNE_FORNAVN, ETTERNAVN)));



        // Medmor ARBEID OG INNTEKT
        tpsPersoner.add(new TpsPerson(MEDMOR_MED_INNTEKT_AKTØR_ID,
            new PersonBygger(MEDMOR_MED_INNTEKT_FNR, KVINNE_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_FOR_MEDMOR_MED_INNTEKT_FNR, BARN_FORNAVN, ETTERNAVN)));
        tpsPersoner.add(new TpsPerson(MOR_MED_MEDMOR_OG_INNTEKT_AKTØR_ID,
            new PersonBygger(MOR_MED_MEDMOR_OG_INNTEKT_FNR, KVINNE_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_BARN, BARN_FOR_MEDMOR_MED_INNTEKT_FNR, BARN_FORNAVN, ETTERNAVN)));
        tpsPersoner.add(new TpsPerson(BARN_FOR_MEDMOR_MED_INNTEKT_AKTØR_ID,
            new PersonBygger(BARN_FOR_MEDMOR_MED_INNTEKT_FNR, BARN_FORNAVN, ETTERNAVN, PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon(RELASJONSTYPE_MORA, MOR_MED_MEDMOR_FNR, KVINNE_FORNAVN, ETTERNAVN)
                .leggTilRelasjon(RELASJONSTYPE_FARA, MEDMOR_MED_INNTEKT_FNR, KVINNE_FORNAVN, ETTERNAVN)));

        // 2-ARBEID OG INNTEKT
        tpsPersoner.add(new TpsPerson(KVINNE_MED_INNTEKT_40k_OG_2_ARBEIDSFORHOLD_AKTØR_ID,
            new PersonBygger(KVINNE_MED_INNTEKT_40k_OG_2_ARBEIDSFORHOLD_FNR, "Helga", "MEDL104", PersonBygger.Kjønn.KVINNE, "BOSA", "NOR", "NOR")
                .leggTilRelasjon("BARN", BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_2_ARBEIDSFORHOLD_FNR, "Børre", "Larsen")));

        tpsPersoner.add(new TpsPerson(BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_2_ARBEIDSFORHOLD_ID,
            new PersonBygger(BARN_TIL_KVINNE_MED_INNTEKT_40k_OG_2_ARBEIDSFORHOLD_FNR, "Børre", "Larsen", PersonBygger.Kjønn.MANN, "BOSA", "NOR", "NOR")
                .leggTilRelasjon("MORA", KVINNE_MED_INNTEKT_40k_OG_2_ARBEIDSFORHOLD_FNR, "Helga", "MEDL104")));

        return tpsPersoner;
    }

    private static void knyttRelasjoner(List<TpsPerson> personer, List<TpsRelasjon> relasjoner) {
        relasjoner.forEach(relasjon -> {
            Optional<TpsPerson> funnetPerson = personer.stream()
                .filter(person -> person.fnr.equals(relasjon.fnr))
                .findFirst();
            funnetPerson.ifPresent(tpsPerson -> new RelasjonBygger(relasjon).byggFor(tpsPerson.person));
        });
    }

    public String finnIdent(AktørId aktoerId) {
        oppdaterVedOvergangTilNyttDøgn();
        return FNR_VED_AKTØR_ID.get(aktoerId);
    }

    public AktørId finnAktoerId(String fnr) {
        oppdaterVedOvergangTilNyttDøgn();
        return AKTØR_ID_VED_FNR.get(fnr);
    }

    public Person finnPerson(String fnr) {
        oppdaterVedOvergangTilNyttDøgn();
        return PERSON_VED_FNR.get(fnr);
    }

    private static void oppdaterVedOvergangTilNyttDøgn() {
        if (!oppfrisketDato.equals(LocalDate.now())) {
            // Må oppfriske datoer ved datoovergang, ellers vil barnas fødselsdatoer ikke fornyes
            init();
        }
    }

}
