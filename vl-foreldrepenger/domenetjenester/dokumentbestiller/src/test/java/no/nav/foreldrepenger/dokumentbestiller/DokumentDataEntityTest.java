package no.nav.foreldrepenger.dokumentbestiller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentAdresse;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class DokumentDataEntityTest {
    private static final String DOKUMENTMAL = DokumentMalType.INNHENT_DOK;
    private static final String ANNEN_DOKUMENTMAL = DokumentMalType.FORLENGET_DOK;
    private static final String FORVENTET_EXCEPTION = "forventet exception";

    @Rule
    public DokumentRepositoryRule repoRule = new DokumentRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private DokumentData.Builder dokumentDataBuilder;
    private DokumentData dokData;
    private DokumentData dokData2;
    private Behandling behandling;
    private DokumentMalType dokumentMal;
    private DokumentMalType annenDokumentMal;


    @Before
    public void setup() {
        dokumentDataBuilder = DokumentData.builder();
        behandling = opprettOgLagreBehandling().lagMocked();
        dokumentMal = repository.hent(DokumentMalType.class, DOKUMENTMAL);
        annenDokumentMal = repository.hent(DokumentMalType.class, ANNEN_DOKUMENTMAL);
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        dokData = lagBuilderMedPaakrevdeFelter().build();

        assertThat(dokData.getDokumentMalType().getKode()).isEqualTo(DOKUMENTMAL);
        assertThat(dokData.getBehandling()).isEqualTo(behandling);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {

        // mangler dokumentMalNavn
        try {
            dokumentDataBuilder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            MatcherAssert.assertThat(e.getMessage(), containsString("dokumentMalType"));
        }

        // mangler behandling
        dokumentDataBuilder.medDokumentMalType(dokumentMal);
        try {
            dokumentDataBuilder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            MatcherAssert.assertThat(e.getMessage(), containsString("behandling"));
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        dokData = lagBuilderMedPaakrevdeFelter().build();

        assertThat(dokData).isNotNull();
        assertThat(dokData).isNotEqualTo("testest");
        assertThat(dokData).isEqualTo(dokData);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        dokumentDataBuilder = lagBuilderMedPaakrevdeFelter();
        dokData = dokumentDataBuilder.build();
        dokData2 = dokumentDataBuilder.build();

        assertThat(dokData).isEqualTo(dokData2);
        assertThat(dokData2).isEqualTo(dokData);

        dokumentDataBuilder.medDokumentMalType(annenDokumentMal);
        dokData2 = dokumentDataBuilder.build();
        assertThat(dokData).isNotEqualTo(dokData2);
        assertThat(dokData2).isNotEqualTo(dokData);
    }

    @Test
    public void skal_bruke_dokumentMalNavn_i_equalsOgHashCode() {
        dokumentDataBuilder = lagBuilderMedPaakrevdeFelter();
        dokData = dokumentDataBuilder.build();

        dokumentDataBuilder.medDokumentMalType(annenDokumentMal);
        dokData2 = dokumentDataBuilder.build();

        assertThat(dokData).isNotEqualTo(dokData2);
        assertThat(dokData.hashCode()).isNotEqualTo(dokData2.hashCode());
    }

    @Test
    public void skal_bruke_forhåndvistTid_i_equalsOgHashCode() {
        dokumentDataBuilder = lagBuilderMedPaakrevdeFelter();
        dokumentDataBuilder.medForhåndsvistTid(LocalDateTime.now());
        dokData = dokumentDataBuilder.build();

        dokumentDataBuilder.medForhåndsvistTid(LocalDateTime.now().minusDays(1));
        dokData2 = dokumentDataBuilder.build();

        assertThat(dokData).isNotEqualTo(dokData2);
        assertThat(dokData.hashCode()).isNotEqualTo(dokData2.hashCode());
    }

    @Test
    public void skal_bruke_sendtTid_i_equalsOgHashCode() {
        dokumentDataBuilder = lagBuilderMedPaakrevdeFelter();
        dokumentDataBuilder.medSendtTid(LocalDateTime.now().minusDays(3));
        dokData = dokumentDataBuilder.build();

        dokumentDataBuilder.medSendtTid(LocalDateTime.now().minusDays(1));
        dokData2 = dokumentDataBuilder.build();

        assertThat(dokData).isNotEqualTo(dokData2);
        assertThat(dokData.hashCode()).isNotEqualTo(dokData2.hashCode());
    }

    @Test
    public void skal_lagre_og_hente_dokumentfellesdata() {
        // Opprett dokumentdata

        BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

        Behandling behandling = opprettOgLagreBehandling().lagre(repositoryProvider);

        opprettDokumentFellesData(repository, behandling, dokumentMal);

        DokumentData result = hentFørste(DokumentData.class);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getBehandling()).isNotNull();
        DokumentFelles felles = result.getFørsteDokumentFelles();
        assertThat(felles).isNotNull();
        assertThat(felles.getId()).isNotNull();
    }

    // -----------------------------------------

    protected static Long opprettDokumentFellesData(Repository repository, Behandling behandling, DokumentMalType dokumentMalType) {
        DokumentData data = DokumentData.opprettNy(dokumentMalType, behandling);
        DokumentAdresse adresse = opprettAdresse(repository);
        DokumentFelles felles = new DokumentFelles.Builder(data)
            .medAutomatiskBehandlet(Boolean.FALSE)
            .medDokumentDato(LocalDate.now())
            .medSaksnummer(new Saksnummer("123"))
            .medKontaktTelefonNummer("98765432")
            .medMottakerAdresse(opprettAdresse(repository))
            .medMottakerId("789456")
            .medMottakerNavn("Jens Olsen")
            .medNavnAvsenderEnhet("NAV")
            .medPostadresse(adresse)
            .medReturadresse(adresse)
            .medSakspartId("654")
            .medSakspartNavn("Jesper Hansen")
            .medSpråkkode(Språkkode.nb)
            .medSakspartPersonStatus(PersonstatusType.DØD.getKode())
            .build();

        data.addDokumentFelles(felles);
        repository.lagre(data);
        repository.lagre(felles);
        repository.flush();
        return data.getId();
    }

    private static DokumentAdresse opprettAdresse(Repository repository) {
        DokumentAdresse adresse = new DokumentAdresse.Builder()
        .medAdresselinje1("Nesbruveien 40")
        .medAdresselinje2("1396 Billingstad")
        .medAdresselinje3("Norge")
        .medPostNummer("1396")
        .medPoststed("Billingstad")
        .build();
        repository.lagre(adresse);
        return adresse;
    }

    private <T> T hentFørste(Class<T> c) {
        Collection<T> result = repository.hentAlle(c);
        Assertions.assertThat(result).hasSize(1);
        return result.iterator().next();
    }

    private DokumentData.Builder lagBuilderMedPaakrevdeFelter() {
        return DokumentData.builder()
                .medDokumentMalType(dokumentMal)
                .medBehandling(behandling);
    }

    private AbstractTestScenario<?> opprettOgLagreBehandling() {
        return ScenarioMorSøkerEngangsstønad
                .forFødsel()
                .medDefaultBekreftetTerminbekreftelse();

    }
}
