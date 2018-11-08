package no.nav.foreldrepenger.dokumentbestiller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentAdresse;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;

@RunWith(CdiRunner.class)
public class DokumentFellesTest {
    private static final Saksnummer SAKSNUMMER  = new Saksnummer("54");
    private static final Boolean AUTOMATISKBEHANDLET = true;
    private static final String SAKSPARTID = "657";
    private static final String SAKSPARTNAVN = "Ellen Martinsen";
    private static final String MOTTAKERID = "7863";
    private static final String MOTTAKERNAVN = "Morten Larsen";
    private static final String NAVNAVSENDERENHET = "AvsenderEnhet";
    private static final String KONTAKTTELEFONNUMMER = "12345678";
    private static final LocalDate DOKUMENTDATO = LocalDate.now();

    @Rule
    public DokumentRepositoryRule repoRule = new DokumentRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private DokumentFelles dokFelles;
    private DokumentAdresse adresse;
    private DokumentData dokData;
    private DokumentMalType forlengetMedlDok;

    @Before
    public void setup() {
        forlengetMedlDok = mock(DokumentMalType.class);
        adresse = opprettAdresse(repository);
        dokData = opprettDokumentData();
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        dokFelles = lagBuilderMedPaakrevdeFelter().build();

        assertThat(dokFelles.getSaksnummer()).isEqualTo(SAKSNUMMER);
        assertThat(dokFelles.getAutomatiskBehandlet()).isEqualTo(AUTOMATISKBEHANDLET);
        assertThat(dokFelles.getSakspartId()).isEqualTo(SAKSPARTID);
        assertThat(dokFelles.getSakspartNavn()).isEqualTo(SAKSPARTNAVN);
        assertThat(dokFelles.getMottakerId()).isEqualTo(MOTTAKERID);
        assertThat(dokFelles.getMottakerNavn()).isEqualTo(MOTTAKERNAVN);
        assertThat(dokFelles.getMottakerAdresse()).isEqualTo(adresse);
        assertThat(dokFelles.getNavnAvsenderEnhet()).isEqualTo(NAVNAVSENDERENHET);
        assertThat(dokFelles.getKontaktTlf()).isEqualTo(KONTAKTTELEFONNUMMER);
        assertThat(dokFelles.getReturadresse()).isEqualTo(adresse);
        assertThat(dokFelles.getPostadresse()).isEqualTo(adresse);
        assertThat(dokFelles.getDokumentDato()).isEqualTo(DOKUMENTDATO);
    }

    @Test
    public void skal_bruke_dokumentId_i_equalsOgHashCode() {
        DokumentFelles dokFelles1 = lagBuilderMedPaakrevdeFelter().medDokumentId("44").build();
        DokumentFelles dokFelles2 = lagBuilderMedPaakrevdeFelter().medDokumentId("775").build();

        assertThat(dokFelles1).isNotEqualTo(dokFelles2);
        assertThat(dokFelles1.hashCode()).isNotEqualTo(dokFelles2.hashCode());
    }

    @Test
    public void skalSetteSakspartPersonstatus() {
        String status = PersonstatusType.DØD.getKode();
        DokumentFelles dokFelles = lagBuilderMedPaakrevdeFelter().medSakspartPersonStatus(status).build();
        assertThat(dokFelles.getSakspartPersonStatus()).isEqualTo(status);
    }

    // ----------------------------------------------------

    private DokumentFelles.Builder lagBuilderMedPaakrevdeFelter() {
        return DokumentFelles.builder(dokData)
                .medSpråkkode(Språkkode.nb)
                .medSaksnummer(SAKSNUMMER)
                .medAutomatiskBehandlet(AUTOMATISKBEHANDLET)
                .medSakspartId(SAKSPARTID)
                .medSakspartNavn(SAKSPARTNAVN)
                .medMottakerId(MOTTAKERID)
                .medMottakerNavn(MOTTAKERNAVN)
                .medMottakerAdresse(adresse)
                .medNavnAvsenderEnhet(NAVNAVSENDERENHET)
                .medKontaktTelefonNummer(KONTAKTTELEFONNUMMER)
                .medReturadresse(adresse)
                .medPostadresse(adresse)
                .medDokumentDato(DOKUMENTDATO);
    }

    private DokumentData opprettDokumentData() {
        Behandling behandling = opprettOgLagreBehandling();

        DokumentData.Builder dokDatBuilder = DokumentData.builder()
                .medDokumentMalType(forlengetMedlDok)
                .medBehandling(behandling);

        return dokDatBuilder.build();
    }

    private Behandling opprettOgLagreBehandling() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
                .forFødsel();
        return scenario.lagMocked();
    }

    private DokumentAdresse opprettAdresse(Repository repository) {
        DokumentAdresse adresse = new DokumentAdresse.Builder()
        .medAdresselinje1("Jernbanetorget 1")
        .medAdresselinje2("0154 Oslo")
        .medAdresselinje3("Norge")
        .medPostNummer("0154")
        .medPoststed("Oslo")
        .build();
        repository.lagre(adresse);
        return adresse;
    }
}
