package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;

public class MottaksdatoBeregnerTest {

    private static final LocalDate SØKNADSDATO = LocalDate.now().minusDays(2);
    private static final LocalDate KLAGEDATO = SØKNADSDATO.plusDays(1);
    private static final Long BEHANDLING_ID = 125L;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Behandling behandling;
    @Mock
    private MottatteDokumentRepository mottatteDokumentRepository;
    @Mock
    private MottattDokument søknadDokument;
    @Mock
    private MottattDokument klageDokument;

    @Before
    public void setUp() {
        when(behandling.getId()).thenReturn(BEHANDLING_ID);
        when(søknadDokument.getDokumentTypeId()).thenReturn(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        when(søknadDokument.getMottattDato()).thenReturn(LocalDate.now());
        when(mottatteDokumentRepository.hentMottatteDokument(BEHANDLING_ID))
                .thenReturn(Arrays.asList(søknadDokument, klageDokument));
    }

    @Test
    public void skal_finne_mottaksdato_for_ustrukturert_søknad() {
        // Arrange
        when(søknadDokument.getMottattDato()).thenReturn(SØKNADSDATO);
        when(klageDokument.getMottattDato()).thenReturn(KLAGEDATO);
        final Søknad build = new SøknadEntitet.Builder().build();
        // Act
        LocalDate dato = MottaksdatoBeregner.finnSøknadsdato(mottatteDokumentRepository, Optional.ofNullable(build), behandling.getId());
        // Assert
        assertThat(dato).isEqualTo(SØKNADSDATO);
    }

    @Test
    public void skal_finne_mottaksdato_for_klage() {
        // Arrange
        when(klageDokument.getDokumentTypeId()).thenReturn(DokumentTypeId.KLAGE_DOKUMENT);
        when(klageDokument.getMottattDato()).thenReturn(KLAGEDATO);
        when(behandling.getType()).thenReturn(BehandlingType.KLAGE);
        // Act
        Optional<LocalDate> dato = MottaksdatoBeregner.finnKlagedato(behandling, mottatteDokumentRepository);
        // Assert
        assertThat(dato).isPresent();
        assertThat(dato.get()).isEqualTo(KLAGEDATO);
    }

}
