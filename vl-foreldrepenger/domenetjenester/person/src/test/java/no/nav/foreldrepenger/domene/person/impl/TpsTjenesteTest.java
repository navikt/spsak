package no.nav.foreldrepenger.domene.person.impl;

import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.KVINNE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.GeografiskTilknytning;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.Personhistorikkinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.feil.PersonIkkeFunnet;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class TpsTjenesteTest {

    private static Map<AktørId, PersonIdent> FNR_VED_AKTØR_ID = new HashMap<>();
    private static Map<PersonIdent, AktørId> AKTØR_ID_VED_FNR = new HashMap<>();

    private static final AktørId AKTØR_ID = new AktørId("1");
    private static final AktørId ENDRET_AKTØR_ID = new AktørId("2");
    private static final AktørId AKTØR_ID_SOM_TRIGGER_EXCEPTION = new AktørId("10");
    private static final PersonIdent FNR = new PersonIdent("12345678901");
    private static final PersonIdent ENDRET_FNR = new PersonIdent("02345678901");
    private static final LocalDate FØDSELSDATO = LocalDate.of(1992, Month.OCTOBER, 13);

    private static final String NAVN = "Anne-Berit Hjartdal";
    // Familierelasjon
    private static final AktørId AKTØR_ID_RELASJON = new AktørId("3");
    private static final PersonIdent FNR_RELASJON = new PersonIdent("01345678901");
    private static final LocalDate FØDSELSDATO_RELASJON = LocalDate.of(2017, Month.JANUARY, 1);

    private TpsTjeneste tpsTjeneste;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    public Repository repository = repoRule.getRepository();

    @Before
    public void oppsett() {
        FNR_VED_AKTØR_ID.put(AKTØR_ID, FNR);
        FNR_VED_AKTØR_ID.put(ENDRET_AKTØR_ID, ENDRET_FNR);
        AKTØR_ID_VED_FNR.put(FNR, AKTØR_ID);
        AKTØR_ID_VED_FNR.put(ENDRET_FNR, ENDRET_AKTØR_ID);

        AKTØR_ID_VED_FNR.put(FNR_RELASJON, AKTØR_ID_RELASJON);

        tpsTjeneste = new TpsTjenesteImpl(new TpsAdapterMock());
    }

    @Test
    public void skal_ikke_hente_bruker_for_ukjent_aktør() {
        Optional<Personinfo> funnetBruker = tpsTjeneste.hentBrukerForAktør(new AktørId("666"));
        assertThat(funnetBruker).isNotPresent();
    }

    @Test
    public void skal_hente_bruker_for_kjent_fnr() {
        Optional<Personinfo> funnetBruker = tpsTjeneste.hentBrukerForFnr(FNR);
        assertThat(funnetBruker).isPresent();
    }

    @Test
    public void skal_ikke_hente_bruker_for_ukjent_fnr() {
        Optional<Personinfo> funnetBruker = tpsTjeneste.hentBrukerForFnr(new PersonIdent("666"));
        assertThat(funnetBruker).isNotPresent();
    }

    @Test
    public void test_hentGeografiskTilknytning_finnes() {
        GeografiskTilknytning geografiskTilknytning = tpsTjeneste.hentGeografiskTilknytning(FNR);
        assertThat(geografiskTilknytning).isNotNull();
    }

    @Test(expected = TekniskException.class)
    public void test_hentGeografiskTilknytning_finnes_ikke() {
        tpsTjeneste.hentGeografiskTilknytning(new PersonIdent("666"));
    }

    @Test
    public void skal_kaste_feil_ved_tjenesteexception_dersom_aktør_ikke_er_cachet() {
        expectedException.expect(TpsException.class);

        tpsTjeneste.hentBrukerForAktør(AKTØR_ID_SOM_TRIGGER_EXCEPTION);
    }

    private class TpsAdapterMock implements TpsAdapter {
        private static final String ADR1 = "Adresselinje1";
        private static final String ADR2 = "Adresselinje2";
        private static final String ADR3 = "Adresselinje3";
        private static final String POSTNR = "1234";
        private static final String POSTSTED = "Oslo";
        private static final String LAND = "Norge";

        @Override
        public Optional<AktørId> hentAktørIdForPersonIdent(PersonIdent fnr) {
            return Optional.ofNullable(AKTØR_ID_VED_FNR.get(fnr));
        }

        @Override
        public Optional<PersonIdent> hentIdentForAktørId(AktørId aktørId) {
            if (aktørId == AKTØR_ID_SOM_TRIGGER_EXCEPTION) {
                throw new TpsException(FeilFactory.create(TpsFeilmeldinger.class)
                    .tpsUtilgjengeligSikkerhetsbegrensning(new HentPersonSikkerhetsbegrensning("String", null)));
            }
            return Optional.ofNullable(FNR_VED_AKTØR_ID.get(aktørId));
        }

        @Override
        public Personinfo hentKjerneinformasjon(PersonIdent fnr, AktørId aktørId) {
            if (!AKTØR_ID_VED_FNR.containsKey(fnr)) {
                return null;
            }
            return new Personinfo.Builder()
                .medAktørId(aktørId)
                .medPersonIdent(fnr)
                .medNavn(NAVN)
                .medFødselsdato(FØDSELSDATO)
                .medNavBrukerKjønn(KVINNE)
                .build();
        }

        // TODO legg inn mock
        @Override
        public Personhistorikkinfo hentPersonhistorikk(AktørId aktørId, Interval periode) {
            return null;
        }

        @Override
        public Adresseinfo hentAdresseinformasjon(PersonIdent fnr) {
            return new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE, fnr, NAVN, PersonstatusType.BOSA)
                .medAdresselinje1(ADR1)
                .medAdresselinje2(ADR2)
                .medAdresselinje3(ADR3)
                .medPostNr(POSTNR)
                .medPoststed(POSTSTED)
                .medLand(LAND)
                .build();
        }

        @Override
        public GeografiskTilknytning hentGeografiskTilknytning(PersonIdent fnr) {
            if (FNR.equals(fnr)) {
                return new GeografiskTilknytning("0219", "KLIE");
            }
            throw TpsFeilmeldinger.FACTORY.geografiskTilknytningIkkeFunnet(
                new HentGeografiskTilknytningPersonIkkeFunnet("finner ikke person", new PersonIkkeFunnet())).toException();
        }
    }
}
