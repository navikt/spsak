package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.PersonBygger.Kjønn.MANN;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.PersonBygger;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.domene.typer.AktørId;

public class TpsTestSett {

    private static final String RELASJONSTYPE_BARN = "BARN";
    private static final String RELASJONSTYPE_MORA = "MORA";
    private static final String RELASJONSTYPE_FARA = "FARA";

    // Simulering av Tps sin datamodell
    private static Map<AktørId, String> FNR_VED_AKTØR_ID = new HashMap<>();
    private static Map<String, AktørId> AKTØR_ID_VED_FNR = new HashMap<>();
    private static Map<String, no.nav.tjeneste.virksomhet.person.v3.informasjon.Person> PERSON_VED_FNR = new HashMap<>();

    private static long AKTØR_SEKVENS = 9100000000000L;
    private static long PERSONIDENT_SEKVENS = 12345678901L;

    public static TpsTestSamling morMedBarnOgFellesFarMedMorsPersonstatus(LocalDate fødselsdato, PersonstatusType personstatusType) {
        List<TpsPerson> tpsPersoner = new ArrayList<>();

        String identMor = nesteIdent();
        String identFar = nesteIdent();
        String identBarn = nesteIdent();

        tpsPersoner.add(new TpsPerson(nesteAktørId(),
            new PersonBygger(identMor, PersonBygger.Kjønn.KVINNE)
                .leggTilRelasjon(RELASJONSTYPE_BARN, identBarn)
                .medPersonstatus(personstatusType)));
        tpsPersoner.add(new TpsPerson(nesteAktørId(),
            new PersonBygger(identFar, MANN)
                .leggTilRelasjon(RELASJONSTYPE_BARN, identBarn)));
        tpsPersoner.add(new TpsPerson(nesteAktørId(),
            new PersonBygger(identBarn, MANN)
                .medFødseldato(fødselsdato)
                .leggTilRelasjon(RELASJONSTYPE_MORA, identMor)
                .leggTilRelasjon(RELASJONSTYPE_FARA, identFar)));

        leggPersonerPåTestModell(tpsPersoner);

        return new TpsTestSamling(
            tpsPersoner.get(0),
            tpsPersoner.get(1),
            singletonList(tpsPersoner.get(2)));
    }


    public static TpsTestSamling morMedBarnOgFellesFar(LocalDate fødselsdato) {
        List<TpsPerson> tpsPersoner = new ArrayList<>();

        String identMor = nesteIdent();
        String identFar = nesteIdent();
        String identBarn = nesteIdent();

        tpsPersoner.add(new TpsPerson(nesteAktørId(),
            new PersonBygger(identMor, PersonBygger.Kjønn.KVINNE)
                .leggTilRelasjon(RELASJONSTYPE_BARN, identBarn)));
        tpsPersoner.add(new TpsPerson(nesteAktørId(),
            new PersonBygger(identFar, MANN)
                .leggTilRelasjon(RELASJONSTYPE_BARN, identBarn)));
        tpsPersoner.add(new TpsPerson(nesteAktørId(),
            new PersonBygger(identBarn, MANN)
                .medFødseldato(fødselsdato)
                .leggTilRelasjon(RELASJONSTYPE_MORA, identMor)
                .leggTilRelasjon(RELASJONSTYPE_FARA, identFar)));

        leggPersonerPåTestModell(tpsPersoner);

        return new TpsTestSamling(
            tpsPersoner.get(0),
            tpsPersoner.get(1),
            singletonList(tpsPersoner.get(2)));
    }

    public static TpsTestSamling kvinneUtenBarn() {
        List<TpsPerson> tpsPersoner = new ArrayList<>();

        String identMor = nesteIdent();

        tpsPersoner.add(new TpsPerson(nesteAktørId(),
            new PersonBygger(identMor, PersonBygger.Kjønn.KVINNE)));
        leggPersonerPåTestModell(tpsPersoner);

        return new TpsTestSamling(
            tpsPersoner.get(0),
            null,
            emptyList());
    }

    public static TpsTestSamling medmor(LocalDate fødselsdato) {
        List<TpsPerson> tpsPersoner = new ArrayList<>();

        String identMedmor = nesteIdent();
        String identMor = nesteIdent();
        String identBarn = nesteIdent();

        tpsPersoner.add(new TpsPerson(nesteAktørId(),
            new PersonBygger(identMedmor, PersonBygger.Kjønn.KVINNE)
                .leggTilRelasjon(RELASJONSTYPE_BARN, identBarn)));
        tpsPersoner.add(new TpsPerson(nesteAktørId(),
            new PersonBygger(identMor, PersonBygger.Kjønn.KVINNE)
                .leggTilRelasjon(RELASJONSTYPE_BARN, identBarn)));
        tpsPersoner.add(new TpsPerson(nesteAktørId(),
            new PersonBygger(identBarn, MANN)
                .medFødseldato(fødselsdato)
                .leggTilRelasjon(RELASJONSTYPE_MORA, identMor)
                .leggTilRelasjon(RELASJONSTYPE_FARA, identMedmor)));

        leggPersonerPåTestModell(tpsPersoner);

        return new TpsTestSamling(
            tpsPersoner.get(0),
            tpsPersoner.get(1),
            singletonList(tpsPersoner.get(2)));
    }

    private static void leggPersonerPåTestModell(List<TpsPerson> tpsPersoner) {
        // Oppdatere enkel testmodell for personer og relasjoner i TPS
        for (TpsPerson tpsPerson : tpsPersoner) {
            FNR_VED_AKTØR_ID.put(tpsPerson.getAktørId(), tpsPerson.getFnr());
            AKTØR_ID_VED_FNR.put(tpsPerson.getFnr(), tpsPerson.getAktørId());
            PERSON_VED_FNR.put(tpsPerson.getFnr(), tpsPerson.person);
        }
    }

    public static String finnIdent(AktørId aktoerId) {
        return FNR_VED_AKTØR_ID.get(aktoerId);
    }

    public static AktørId finnAktoerId(String fnr) {
        return AKTØR_ID_VED_FNR.get(fnr);
    }

    public static no.nav.tjeneste.virksomhet.person.v3.informasjon.Person finnPerson(String fnr) {
        return PERSON_VED_FNR.get(fnr);
    }

    public static void nullstill() {
        FNR_VED_AKTØR_ID.clear();
        AKTØR_ID_VED_FNR.clear();
        PERSON_VED_FNR.clear();
    }

    private static String nesteIdent() {
        return String.valueOf(PERSONIDENT_SEKVENS++);
    }

    private static AktørId nesteAktørId() {
        return new AktørId(AKTØR_SEKVENS++);
    }
}
